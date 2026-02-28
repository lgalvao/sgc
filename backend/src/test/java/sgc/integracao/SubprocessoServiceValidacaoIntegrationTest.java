package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
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
@DisplayName("Integração: SubprocessoService - Cobertura de Validação")
class SubprocessoServiceValidacaoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_VAL");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo Teste Val")
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
    @DisplayName("validarAssociacoesMapa: deve lancar erro de competencia sem atividade")
    void validarAssociacoesMapa_CompetenciaSemAtividade() {
        Competencia c = new Competencia();
        c.setMapa(subprocesso.getMapa());
        c.setDescricao("Comp 1");
        competenciaRepo.save(c);

        Long mapaCodigo = subprocesso.getMapa().getCodigo();
        assertThatThrownBy(() -> subprocessoService.validarAssociacoesMapa(mapaCodigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Existem competências que não foram associadas a nenhuma atividade.");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: deve lancar erro de atividade sem competencia")
    void validarAssociacoesMapa_AtividadeSemCompetencia() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a);

        Long mapaCodigo = subprocesso.getMapa().getCodigo();
        assertThatThrownBy(() -> subprocessoService.validarAssociacoesMapa(mapaCodigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Existem atividades que não foram associadas a nenhuma competência.");
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro sem atividades")
    void validarCadastro_SemAtividades() {
        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros()).hasSize(1);
        assertThat(dto.erros().getFirst().tipo()).isEqualTo("SEM_ATIVIDADES");
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro de atividade sem conhecimento")
    void validarCadastro_AtividadeSemConhecimento() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a);

        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros().getFirst().tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
    }

    @Test
    @DisplayName("validarSituacaoPermitida: throw se status diferente")
    void validarSituacaoPermitida_Throw() {
        assertThatThrownBy(() -> subprocessoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("validarSituacaoPermitida: throw IllegalArgumentException se status null")
    void validarSituacaoPermitida_NullStatus() {
        Subprocesso sp = new Subprocesso();
        assertThatThrownBy(() -> subprocessoService.validarSituacaoPermitida(sp, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO))
                .isInstanceOf(ErroValidacao.class);
    }
}
