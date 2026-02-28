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
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Deletar")
class SubprocessoServiceDeletarIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_DEL");
        unidade.setNome("Unidade Del");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Del")
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
    @DisplayName("excluir: deve excluir o subprocesso sem mapa")
    void excluir() {
        Long codigo = subprocesso.getCodigo();

        subprocessoService.excluir(codigo);
        entityManager.flush();

        assertThatThrownBy(() -> subprocessoService.buscarSubprocesso(codigo))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
