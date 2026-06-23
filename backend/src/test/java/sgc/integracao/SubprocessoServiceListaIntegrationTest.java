package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.dto.*;
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
@DisplayName("Integração: SubprocessoService - Listas, consultas e CRUD básico")
class SubprocessoServiceListaIntegrationTest extends BaseIntegrationTest {

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
        List<Subprocesso> resultado = consultaService.listarTodos();

        assertThat(resultado).extracting(Subprocesso::getCodigo).contains(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("obterEntidadePorProcessoEUnidade: deve retornar subprocesso")
    void obterEntidadePorProcessoEUnidade() {
        Subprocesso resultado = consultaService.obterEntidadePorProcessoEUnidade(processo.getCodigo(), unidade.getCodigo());

        assertThat(resultado.getCodigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(resultado.getUnidade().getCodigo()).isEqualTo(unidade.getCodigo());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades: deve retornar os subprocessos")
    void listarEntidadesPorProcessoEUnidades() {
        List<Subprocesso> resultado = consultaService.listarEntidadesPorProcessoEUnidades(processo.getCodigo(), List.of(unidade.getCodigo()));

        assertThat(resultado).extracting(Subprocesso::getCodigo).containsExactly(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades: com lista vazia deve retornar vazia")
    void listarEntidadesPorProcessoEUnidades_Vazia() {
        List<Subprocesso> resultado = consultaService.listarEntidadesPorProcessoEUnidades(processo.getCodigo(), List.of());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("criarEntidade: deve criar novo subprocesso")
    void criarEntidade() {
        Unidade unidade2 = UnidadeFixture.unidadePadrao();
        unidade2.setCodigo(null);
        unidade2.setSigla("U2");
        unidade2 = unidadeRepo.save(unidade2);

        LocalDateTime dataLimiteEtapa1 = LocalDateTime.now();
        LocalDateTime dataLimiteEtapa2 = dataLimiteEtapa1.plusDays(5);
        CriarSubprocessoRequest request = CriarSubprocessoRequest.builder()
                .codProcesso(processo.getCodigo())
                .codUnidade(unidade2.getCodigo())
                .dataLimiteEtapa1(dataLimiteEtapa1)
                .dataLimiteEtapa2(dataLimiteEtapa2)
                .build();

        Subprocesso novo = subprocessoService.criarEntidade(request);

        assertThat(novo.getCodigo()).isNotNull();
        assertThat(novo.getProcesso().getCodigo()).isEqualTo(processo.getCodigo());
        assertThat(novo.getUnidade().getCodigo()).isEqualTo(unidade2.getCodigo());
        assertThat(novo.getMapa().getCodigo()).isNotNull();
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

    @Test
    @DisplayName("buscarSubprocesso: deve buscar com sucesso")
    void buscarSubprocesso_Sucesso() {
        Subprocesso resultado = consultaService.buscarSubprocesso(subprocesso.getCodigo());

        assertThat(resultado.getCodigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(resultado.getSituacao()).isEqualTo(subprocesso.getSituacao());
        assertThat(resultado.getUnidade().getCodigo()).isEqualTo(unidade.getCodigo());
    }

    @Test
    @DisplayName("obterStatus: deve retornar situação correta")
    void obterStatus_Sucesso() {
        SubprocessoSituacaoDto resultado = consultaService.obterStatus(subprocesso.getCodigo());

        assertThat(resultado.codigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(resultado.situacao()).isEqualTo(subprocesso.getSituacao().name());
    }

    @Test
    @DisplayName("obterEntidadePorCodigoMapa: deve retornar subprocesso")
    void obterEntidadePorCodigoMapa_Sucesso() {
        Subprocesso resultado = consultaService.obterEntidadePorCodigoMapa(subprocesso.getMapa().getCodigo());

        assertThat(resultado.getCodigo()).isEqualTo(subprocesso.getCodigo());
    }



    @Test
    @DisplayName("obterSugestoes: deve retornar sugestões do mapa")
    void obterSugestoes() {
        subprocesso.getMapa().setSugestoes("Sugestão de integração");
        mapaRepo.saveAndFlush(subprocesso.getMapa());

        SugestoesDto resultado = consultaService.obterSugestoes(subprocesso.getCodigo());

        assertThat(resultado.sugestoes()).isEqualTo("Sugestão de integração");
    }

    @Test
    @DisplayName("listarEntidadesPorProcesso: deve listar subprocessos do processo")
    void listarEntidadesPorProcesso() {
        List<Subprocesso> resultado = consultaService.listarEntidadesPorProcesso(processo.getCodigo());

        assertThat(resultado).extracting(Subprocesso::getCodigo).contains(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("salvarMapa: deve salvar e manter vínculo com subprocesso")
    void salvarMapa_Sucesso() {
        Subprocesso subprocessoEmEdicao = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(subprocessoEmEdicao);

        Mapa mapaEdicao = new Mapa();
        mapaEdicao.setSubprocesso(subprocessoEmEdicao);
        mapaRepo.saveAndFlush(mapaEdicao);
        subprocessoEmEdicao.setMapa(mapaEdicao);
        SalvarMapaRequest request = new SalvarMapaRequest("Justificativa teste", List.of());

        Mapa resultado = subprocessoService.salvarMapa(subprocessoEmEdicao.getCodigo(), request);

        assertThat(resultado.getCodigo()).isEqualTo(subprocessoEmEdicao.getMapa().getCodigo());
        assertThat(resultado.getSubprocesso().getCodigo()).isEqualTo(subprocessoEmEdicao.getCodigo());
        assertThat(resultado.getObservacoesDisponibilizacao()).isEqualTo("Justificativa teste");
    }

    @Test
    @DisplayName("mapaCompletoDtoPorSubprocesso: deve retornar mapa do subprocesso")
    void mapaCompletoDtoPorSubprocesso_Sucesso() {
        MapaCompletoDto resultado = consultaService.mapaCompletoDtoPorSubprocesso(subprocesso.getCodigo());

        assertThat(resultado.codigo()).isEqualTo(subprocesso.getMapa().getCodigo());
    }
}
