package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.UnidadeFixture;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Deletar")
class SubprocessoServiceDeletarIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;
    @Autowired
    private SubprocessoConsultaService consultaService;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_DEL");
        unidade.setNome("Unidade del");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo teste del")
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
    @DisplayName("excluir: deve excluir o subprocesso sem mapa")
    void excluir() {
        Long codigo = subprocesso.getCodigo();

        subprocessoService.excluir(codigo);
        entityManager.flush();

        assertThatThrownBy(() -> consultaService.buscarSubprocesso(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
