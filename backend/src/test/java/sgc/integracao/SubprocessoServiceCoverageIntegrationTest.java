package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.AtividadeRepo;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Validações")
class SubprocessoServiceCoverageIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private AtividadeRepo atividadeRepo;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_COV");
        unidade.setNome("Unidade Cov");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Cov")
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
    @DisplayName("validarExistenciaAtividades: deve lançar erro se mapa sem atividades")
    void validarExistenciaAtividades_SemAtividades() {
        assertThatThrownBy(() -> subprocessoService.validarExistenciaAtividades(subprocesso.getCodigo()))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("O mapa de competências deve ter ao menos uma atividade cadastrada.");
    }

    @Test
    @DisplayName("validarExistenciaAtividades: deve lançar erro se atividades sem conhecimento")
    void validarExistenciaAtividades_SemConhecimento() {
        Atividade atividade = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Sem conhecimento").build();
        atividadeRepo.save(atividade);

        assertThatThrownBy(() -> subprocessoService.validarExistenciaAtividades(subprocesso.getCodigo()))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
    }
}
