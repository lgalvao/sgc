package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Listas e CRUD Básico")
class SubprocessoServiceListaIntegrationTest extends BaseIntegrationTest {

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
        unidade.setSigla("TEST_LISTA");
        unidade.setNome("Unidade Lista");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Lista")
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

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("listarEntidades: deve listar todos os subprocessos")
    void listarEntidades() {
        List<Subprocesso> list = subprocessoService.listarEntidades();
        assertThat(list).isNotEmpty();
        assertThat(list).anyMatch(sp -> sp.getCodigo().equals(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("obterEntidadePorProcessoEUnidade: deve retornar subprocesso")
    void obterEntidadePorProcessoEUnidade() {
        Subprocesso result = subprocessoService.obterEntidadePorProcessoEUnidade(processo.getCodigo(), unidade.getCodigo());
        assertThat(result).isNotNull();
        assertThat(result.getCodigo()).isEqualTo(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades: deve retornar os subprocessos")
    void listarEntidadesPorProcessoEUnidades() {
        List<Subprocesso> list = subprocessoService.listarEntidadesPorProcessoEUnidades(processo.getCodigo(), List.of(unidade.getCodigo()));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCodigo()).isEqualTo(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades: com lista vazia deve retornar vazia")
    void listarEntidadesPorProcessoEUnidades_Vazia() {
        List<Subprocesso> list = subprocessoService.listarEntidadesPorProcessoEUnidades(processo.getCodigo(), List.of());
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("criarEntidade: deve criar novo subprocesso")
    void criarEntidade() {
        Unidade unidade2 = UnidadeFixture.unidadePadrao();
        unidade2.setCodigo(null);
        unidade2.setSigla("U2");
        unidade2 = unidadeRepo.save(unidade2);

        CriarSubprocessoRequest request = new CriarSubprocessoRequest(
                processo.getCodigo(), unidade2.getCodigo(), null, LocalDateTime.now(), LocalDateTime.now().plusDays(5));

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
        AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(unidade.getCodigo(), subprocesso.getMapa().getCodigo(), novaData, null, novaData, null);

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request);

        assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(novaData);
        assertThat(atualizado.getDataLimiteEtapa2()).isEqualTo(novaData);
    }
}
