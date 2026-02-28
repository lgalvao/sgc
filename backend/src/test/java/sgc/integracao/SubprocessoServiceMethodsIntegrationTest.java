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
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Metodos Auxiliares")
class SubprocessoServiceMethodsIntegrationTest extends BaseIntegrationTest {

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
        unidade.setSigla("TEST_MET");
        unidade.setNome("Unidade Met");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Met")
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
    @DisplayName("buscarSubprocessoComMapa: deve buscar com sucesso")
    void buscarSubprocessoComMapa_Sucesso() {
        Subprocesso result = subprocessoService.buscarSubprocessoComMapa(subprocesso.getCodigo());
        assertThat(result).isNotNull();
        assertThat(result.getCodigo()).isEqualTo(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("obterStatus: deve retornar situacao correta")
    void obterStatus_Sucesso() {
        SubprocessoSituacaoDto result = subprocessoService.obterStatus(subprocesso.getCodigo());
        assertThat(result).isNotNull();
        assertThat(result.codigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(result.situacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("obterEntidadePorCodigoMapa: deve retornar subprocesso")
    void obterEntidadePorCodigoMapa_Sucesso() {
        Subprocesso result = subprocessoService.obterEntidadePorCodigoMapa(subprocesso.getMapa().getCodigo());
        assertThat(result).isNotNull();
        assertThat(result.getCodigo()).isEqualTo(subprocesso.getCodigo());
    }

    @Test
    @DisplayName("obterEntidadePorCodigoMapa: deve lancar erro se nao encontrar")
    void obterEntidadePorCodigoMapa_NaoEncontrado() {
        assertThatThrownBy(() -> subprocessoService.obterEntidadePorCodigoMapa(999L))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("obterSugestoes: deve retornar map vazio")
    void obterSugestoes() {
        Map<String, Object> result = subprocessoService.obterSugestoes();
        assertThat(result).containsEntry("sugestoes", "");
    }

    @Test
    @DisplayName("listarEntidadesPorProcesso: deve listar por processo")
    void listarEntidadesPorProcesso() {
        List<Subprocesso> list = subprocessoService.listarEntidadesPorProcesso(processo.getCodigo());
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCodigo()).isEqualTo(subprocesso.getCodigo());
    }
}
