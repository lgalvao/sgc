package sgc.integracao;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Listas e CRUD Básico")
class SubprocessoServiceListaIntegrationTest extends BaseIntegrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private SubprocessoService subprocessoService;
    @Autowired
    private SubprocessoConsultaService consultaService;

    @Autowired
    private MapaRepo mapaRepo;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_LISTA");
        unidade.setNome("Unidade lista");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo teste lista")
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

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("listarEntidades: deve listar todos os subprocessos")
    void listarEntidades() {
        List<Subprocesso> list = consultaService.listarTodos();
        assertThat(list).isNotEmpty();
        assertThat(list).anyMatch(sp -> sp.getCodigo().equals(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("obterEntidadePorProcessoEUnidade: deve retornar subprocesso")
    void obterEntidadePorProcessoEUnidade() {
        Subprocesso result = consultaService.obterEntidadePorProcessoEUnidade(processo.getCodigo(), unidade.getCodigo());
        assertThat(result).isNotNull();
        assertThat(result.getCodigo()).isEqualTo(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades: deve retornar os subprocessos")
    void listarEntidadesPorProcessoEUnidades() {
        List<Subprocesso> list = consultaService.listarEntidadesPorProcessoEUnidades(processo.getCodigo(), List.of(unidade.getCodigo()));
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getCodigo()).isEqualTo(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades: com lista vazia deve retornar vazia")
    void listarEntidadesPorProcessoEUnidades_Vazia() {
        List<Subprocesso> list = consultaService.listarEntidadesPorProcessoEUnidades(processo.getCodigo(), List.of());
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("criarEntidade: deve criar novo subprocesso")
    void criarEntidade() throws Exception {
        Unidade unidade2 = UnidadeFixture.unidadePadrao();
        unidade2.setCodigo(null);
        unidade2.setSigla("U2");
        unidade2 = unidadeRepo.save(unidade2);

        LocalDateTime dataLimiteEtapa1 = LocalDateTime.now();
        LocalDateTime dataLimiteEtapa2 = dataLimiteEtapa1.plusDays(5);
        CriarSubprocessoRequest request = objectMapper.readValue("""
                {
                  "codProcesso": %d,
                  "codUnidade": %d,
                  "dataLimiteEtapa1": "%s",
                  "dataLimiteEtapa2": "%s"
                }
                """.formatted(
                processo.getCodigo(),
                unidade2.getCodigo(),
                dataLimiteEtapa1,
                dataLimiteEtapa2), CriarSubprocessoRequest.class);

        Subprocesso novo = subprocessoService.criarEntidade(request);

        assertThat(novo).isNotNull();
        assertThat(novo.getCodigo()).isNotNull();
        assertThat(novo.getProcesso().getCodigo()).isEqualTo(processo.getCodigo());
        assertThat(novo.getMapa()).isNotNull();
    }

    @Test
    @DisplayName("atualizarEntidade: deve atualizar dados")
    void atualizarEntidade() {
        LocalDateTime novaData = LocalDateTime.now().plusDays(10);
        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                .codUnidade(unidade.getCodigo())
                .codMapa(subprocesso.getMapa().getCodigo())
                .dataLimiteEtapa1(novaData)
                .dataLimiteEtapa2(novaData)
                .build();

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request.paraCommand());

        assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(novaData);
        assertThat(atualizado.getDataLimiteEtapa2()).isEqualTo(novaData);
    }
}
