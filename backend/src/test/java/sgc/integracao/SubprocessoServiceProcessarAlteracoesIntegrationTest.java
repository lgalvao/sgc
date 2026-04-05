package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de ProcessarAlteracoes e Extras")
class SubprocessoServiceProcessarAlteracoesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    private Unidade unidade;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_PROC");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("atualizarEntidade: altera mapa de null para mapa existente")
    void atualizarEntidade_SemMapaAntes() {
        Subprocesso subprocessoComMapa = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO)
                .processo(subprocesso.getProcesso())
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(subprocessoComMapa);

        Mapa novoMapa = Mapa.builder().subprocesso(subprocessoComMapa).build();
        novoMapa = mapaRepo.save(novoMapa);
        subprocessoComMapa.setMapa(novoMapa);
        subprocessoRepo.save(subprocessoComMapa);

        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                .codUnidade(unidade.getCodigo())
                .codMapa(novoMapa.getCodigo())
                .build();

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request.paraCommand());
        assertThat(atualizado.getMapa().getCodigo()).isEqualTo(novoMapa.getCodigo());
    }

    @Test
    @DisplayName("atualizarEntidade: nao altera mapa se o codigo for o mesmo")
    void atualizarEntidade_MesmoMapa() {
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);

        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                .codUnidade(unidade.getCodigo())
                .codMapa(mapa.getCodigo())
                .build();

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request.paraCommand());
        assertThat(atualizado.getMapa().getCodigo()).isEqualTo(mapa.getCodigo());
    }
}
