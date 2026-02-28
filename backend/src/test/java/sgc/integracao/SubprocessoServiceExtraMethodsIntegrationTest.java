package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Metodos Extras")
class SubprocessoServiceExtraMethodsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_EXTRA");
        unidade.setNome("Unidade Extra");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Extra")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("salvarMapa: deve salvar o mapa")
    void salvarMapa_Sucesso() {
        SalvarMapaRequest request = new SalvarMapaRequest("Justificativa teste", List.of());
        Mapa result = subprocessoService.salvarMapa(subprocesso.getCodigo(), request);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("mapaCompletoPorSubprocesso: deve retornar o mapa")
    void mapaCompletoPorSubprocesso_Sucesso() {
        Mapa result = subprocessoService.mapaCompletoPorSubprocesso(subprocesso.getCodigo());
        assertThat(result).isNotNull();
        assertThat(result.getCodigo()).isEqualTo(subprocesso.getMapa().getCodigo());
    }
}
