package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Datas e Alertas")
class SubprocessoServiceDatasIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_DAT");
        unidade = unidadeRepo.save(unidade);

        Unidade superior = UnidadeFixture.unidadePadrao();
        superior.setCodigo(null);
        superior.setSigla("TEST_SUP");
        superior = unidadeRepo.save(superior);
        unidade.setUnidadeSuperior(superior);
        unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Dat")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("alterarDataLimite: deve alterar etapa 1 para CADASTRO")
    void alterarDataLimite_Etapa1() {
        LocalDate novaData = LocalDate.now().plusDays(5);
        subprocessoService.alterarDataLimite(subprocesso.getCodigo(), novaData);

        Subprocesso atualizado = subprocessoService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getDataLimiteEtapa1().toLocalDate()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("alterarDataLimite: deve alterar etapa 2 para MAPA")
    void alterarDataLimite_Etapa2() {
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        LocalDate novaData = LocalDate.now().plusDays(5);
        subprocessoService.alterarDataLimite(subprocesso.getCodigo(), novaData);

        Subprocesso atualizado = subprocessoService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getDataLimiteEtapa2().toLocalDate()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("atualizarParaEmAndamento: deve atualizar de NAO_INICIADO para CADASTRO_EM_ANDAMENTO (MAPEAMENTO)")
    void atualizarParaEmAndamento_Mapeamento() {
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        subprocessoService.atualizarParaEmAndamento(subprocesso.getMapa().getCodigo());

        Subprocesso atualizado = subprocessoService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("atualizarParaEmAndamento: deve atualizar de NAO_INICIADO para REVISAO_CADASTRO_EM_ANDAMENTO (REVISAO)")
    void atualizarParaEmAndamento_Revisao() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.save(processo);

        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        subprocessoService.atualizarParaEmAndamento(subprocesso.getMapa().getCodigo());

        Subprocesso atualizado = subprocessoService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    }
}
