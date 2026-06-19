package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoTransicaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Datas e Alertas")
class SubprocessoServiceDatasIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoConsultaService consultaService;

    @Autowired
    private SubprocessoTransicaoService transicaoService;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private MapaManutencaoService mapaManutencaoService;

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
                .descricao("Processo teste dat")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(1))
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
        transicaoService.alterarDataLimite(subprocesso.getCodigo(), novaData);

        Subprocesso atualizado = consultaService.buscarSubprocesso(subprocesso.getCodigo());
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
        transicaoService.alterarDataLimite(subprocesso.getCodigo(), novaData);

        Subprocesso atualizado = consultaService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getDataLimiteEtapa2()).isNotNull();
        assertThat(atualizado.getDataLimiteEtapa2().toLocalDate()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("atualizarParaEmAndamento: deve atualizar de NAO_INICIADO para CADASTRO_EM_ANDAMENTO (MAPEAMENTO)")
    void atualizarParaEmAndamento_Mapeamento() {
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();
        mapaManutencaoService.criarAtividade(new CriarAtividadeRequest(subprocesso.getMapa().getCodigo(), "Atividade inicial"));

        Subprocesso atualizado = consultaService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("atualizarParaEmAndamento: deve atualizar revisao para em andamento apos alteracao")
    void atualizarParaEmAndamento_Revisao() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.save(processo);

        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();
        mapaManutencaoService.criarAtividade(new CriarAtividadeRequest(subprocesso.getMapa().getCodigo(), "Atividade inicial revisao"));

        Subprocesso atualizado = consultaService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("atualizarParaEmAndamento: deve voltar para NAO_INICIADO quando nao houver mais atividades")
    void atualizarParaEmAndamento_SemAtividades() {
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        mapaManutencaoService.criarAtividade(new CriarAtividadeRequest(subprocesso.getMapa().getCodigo(), "Atividade temporaria"));
        subprocessoRepo.flush();

        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.saveAndFlush(subprocesso);

        mapaManutencaoService.atividadesMapaCodigoSemRels(subprocesso.getMapa().getCodigo())
                .forEach(atividade -> mapaManutencaoService.excluirAtividade(atividade.getCodigo()));
        subprocessoRepo.flush();

        Subprocesso atualizado = consultaService.buscarSubprocesso(subprocesso.getCodigo());
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
    }
}
