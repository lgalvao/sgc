package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
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
    private SubprocessoConsultaService consultaService;

    @Autowired
    private SubprocessoValidacaoService validacaoService;

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
                .descricao("Processo teste val")
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
    @DisplayName("validarAssociacoesMapa: deve lancar erro de competencia sem atividade")
    void validarAssociacoesMapa_CompetenciaSemAtividade() {
        Competencia c = new Competencia();
        c.setMapa(subprocesso.getMapa());
        c.setDescricao("Comp 1");
        competenciaRepo.save(c);

        Long mapaCodigo = subprocesso.getMapa().getCodigo();
        assertThatThrownBy(() -> validacaoService.validarAssociacoesMapa(mapaCodigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.COMPETENCIAS_SEM_ATIVIDADE);
    }

    @Test
    @DisplayName("validarAssociacoesMapa: deve lancar erro de atividade sem competencia")
    void validarAssociacoesMapa_AtividadeSemCompetencia() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a);

        Long mapaCodigo = subprocesso.getMapa().getCodigo();
        assertThatThrownBy(() -> validacaoService.validarAssociacoesMapa(mapaCodigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.ATIVIDADES_SEM_COMPETENCIA);
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro sem atividades")
    void validarCadastro_SemAtividades() {
        ValidacaoCadastroDto dto = consultaService.validarCadastro(subprocesso.getCodigo());

        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros())
                .singleElement()
                .satisfies(erro -> {
                    assertThat(erro.tipo()).isEqualTo("SEM_ATIVIDADES");
                    assertThat(erro.mensagem()).isEqualTo("O mapa não possui atividades cadastradas.");
                    assertThat(erro.atividadeCodigo()).isNull();
                });
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro de atividade sem conhecimento")
    void validarCadastro_AtividadeSemConhecimento() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a);

        ValidacaoCadastroDto dto = consultaService.validarCadastro(subprocesso.getCodigo());

        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros())
                .singleElement()
                .satisfies(erro -> {
                    assertThat(erro.tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
                    assertThat(erro.atividadeCodigo()).isEqualTo(a.getCodigo());
                    assertThat(erro.descricaoAtividade()).isEqualTo("Ativ 1");
                    assertThat(erro.mensagem()).isEqualTo("Esta atividade não possui conhecimentos associados.");
                });
    }

    @Test
    @DisplayName("validarCadastro: deve retornar válido quando todas as atividades possuem conhecimentos")
    void validarCadastro_Valido() {
        Atividade atividade = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Atividade 1").build();
        atividadeRepo.save(atividade);

        Conhecimento conhecimento = Conhecimento.builder()
                .atividade(atividade)
                .descricao("Conhecimento 1")
                .build();
        atividade.getConhecimentos().add(conhecimento);
        atividadeRepo.save(atividade);

        ValidacaoCadastroDto dto = consultaService.validarCadastro(subprocesso.getCodigo());

        assertThat(dto.valido()).isTrue();
        assertThat(dto.erros()).isEmpty();
    }

    @Test
    @DisplayName("validarSituacaoPermitida: deve informar situação atual e permitida quando status for incompatível")
    void validarSituacaoPermitida_Throw() {
        assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(subprocesso.getSituacao().name())
                .hasMessageContaining(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO.name());
    }

    @Test
    @DisplayName("validarSituacaoPermitida: deve lançar IllegalArgumentException quando status for nulo")
    void validarSituacaoPermitida_NullStatus() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);
        assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação do subprocesso não pode ser nula");
    }
}
