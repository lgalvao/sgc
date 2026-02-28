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
                .descricao("Processo Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("atualizarEntidade: altera mapa de null para mapa existente")
    void atualizarEntidade_SemMapaAntes() {
        Mapa novoMapa = new Mapa();
        mapaRepo.save(novoMapa);

        AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(
                unidade.getCodigo(), novoMapa.getCodigo(), null, null, null, null
        );

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request);
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

        AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(
                unidade.getCodigo(), mapa.getCodigo(), null, null, null, null
        );

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request);
        assertThat(atualizado.getMapa().getCodigo()).isEqualTo(mapa.getCodigo());
    }
}
