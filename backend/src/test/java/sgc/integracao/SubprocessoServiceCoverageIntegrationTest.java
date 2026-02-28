package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Validações")
class SubprocessoServiceCoverageIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private AtividadeRepo atividadeRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_COV");
        unidade.setNome("Unidade Cov");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
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
        Long subprocessoCodigo = subprocesso.getCodigo();
        assertThatThrownBy(() -> subprocessoService.validarExistenciaAtividades(subprocessoCodigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("O mapa de competências deve ter ao menos uma atividade cadastrada.");
    }

    @Test
    @DisplayName("validarExistenciaAtividades: deve lançar erro se atividades sem conhecimento")
    void validarExistenciaAtividades_SemConhecimento() {
        Atividade atividade = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Sem conhecimento").build();
        atividadeRepo.save(atividade);

        Long subprocessoCodigo = subprocesso.getCodigo();
        assertThatThrownBy(() -> subprocessoService.validarExistenciaAtividades(subprocessoCodigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
    }
}
