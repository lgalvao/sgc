package sgc.integracao;

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

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de validarCadastro com multiplos cenarios")
class SubprocessoServiceValidacaoCadastroIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_VAL_CAD");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo Teste")
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
    @DisplayName("validarCadastro: deve retornar erro SEM_ATIVIDADES")
    void validarCadastro_SemAtividades() {
        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros()).hasSize(1);
        assertThat(dto.erros().getFirst().tipo()).isEqualTo("SEM_ATIVIDADES");
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro ATIVIDADE_SEM_CONHECIMENTO")
    void validarCadastro_AtividadeSemConhecimento() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Atividade 1").build();
        atividadeRepo.save(a);

        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros()).hasSize(1);
        assertThat(dto.erros().getFirst().tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
        assertThat(dto.erros().getFirst().atividadeCodigo()).isEqualTo(a.getCodigo());
    }

    @Test
    @DisplayName("validarCadastro: deve retornar valido quando tudo estiver correto")
    void validarCadastro_Valido() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Atividade 1").build();
        atividadeRepo.save(a);

        Conhecimento c = Conhecimento.builder().atividade(a).descricao("Conhecimento 1").build();
        conhecimentoRepo.save(c);
        a.getConhecimentos().add(c);
        atividadeRepo.save(a);

        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.erros()).isEmpty();
    }
}
