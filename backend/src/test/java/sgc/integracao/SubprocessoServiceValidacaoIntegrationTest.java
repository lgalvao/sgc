package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_VAL");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
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

        assertThatThrownBy(() -> subprocessoService.validarAssociacoesMapa(subprocesso.getMapa().getCodigo()))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Existem competências que não foram associadas a nenhuma atividade.");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: deve lancar erro de atividade sem competencia")
    void validarAssociacoesMapa_AtividadeSemCompetencia() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a);

        assertThatThrownBy(() -> subprocessoService.validarAssociacoesMapa(subprocesso.getMapa().getCodigo()))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Existem atividades que não foram associadas a nenhuma competência.");
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro sem atividades")
    void validarCadastro_SemAtividades() {
        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros()).hasSize(1);
        assertThat(dto.erros().get(0).tipo()).isEqualTo("SEM_ATIVIDADES");
    }

    @Test
    @DisplayName("validarCadastro: deve retornar erro de atividade sem conhecimento")
    void validarCadastro_AtividadeSemConhecimento() {
        Atividade a = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a);

        ValidacaoCadastroDto dto = subprocessoService.validarCadastro(subprocesso.getCodigo());
        assertThat(dto.valido()).isFalse();
        assertThat(dto.erros().get(0).tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
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
