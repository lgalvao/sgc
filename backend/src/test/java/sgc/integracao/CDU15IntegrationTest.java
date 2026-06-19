package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.fixture.AtividadeFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CriarCompetenciaRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@DisplayName("CDU-15: Manter mapa de competências")
class CDU15IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private SubprocessoService subprocessoService;

    private Subprocesso subprocesso;
    private Mapa mapa;
    private Atividade atividade1;
    private Atividade atividade2;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("CDU15-UND");
        unidade.setNome("Unidade CDU-15");
        unidade = unidadeRepo.save(unidade);

        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo CDU-15");
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo = processoRepo.save(processo);
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(5));
        subprocesso = subprocessoRepo.save(subprocesso);

        mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);

        atividade1 = AtividadeFixture.atividadePadrao(mapa);
        atividade1.setDescricao("Atividade 1 CDU-15");
        atividade1 = atividadeRepo.save(atividade1);

        atividade2 = AtividadeFixture.atividadePadrao(mapa);
        atividade2.setDescricao("Atividade 2 CDU-15");
        atividade2 = atividadeRepo.save(atividade2);
    }

    @Test
    @DisplayName("ADMIN cria competência e subprocesso muda para MAPA_CRIADO")
    void deveCriarCompetenciaEAlterarSituacaoParaMapaCriado() {
        CriarCompetenciaRequest request = CriarCompetenciaRequest.builder()
                .descricao("Competência CDU-15")
                .atividadesCodigos(List.of(atividade1.getCodigo(), atividade2.getCodigo()))
                .build();

        subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), request);

        List<Competencia> competencias = competenciaRepo.findByMapa_Codigo(mapa.getCodigo());
        assertThat(competencias).hasSize(1);
        assertThat(competencias.getFirst().getDescricao()).isEqualTo("Competência CDU-15");
        assertThat(competencias.getFirst().getAtividades()).hasSize(2);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
    }

    @Test
    @DisplayName("ADMIN remove última competência e subprocesso volta para CADASTRO_HOMOLOGADO")
    void deveRemoverUltimaCompetenciaEVoltarSituacaoCadastroHomologado() {
        CriarCompetenciaRequest criarReq = CriarCompetenciaRequest.builder()
                .descricao("Competência temporária")
                .atividadesCodigos(List.of(atividade1.getCodigo()))
                .build();

        subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), criarReq);

        Competencia competencia = competenciaRepo.findByMapa_Codigo(mapa.getCodigo()).getFirst();
        subprocessoService.removerCompetencia(subprocesso.getCodigo(), competencia.getCodigo());

        assertThat(competenciaRepo.findByMapa_Codigo(mapa.getCodigo())).isEmpty();

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
    }

    @Test
    @DisplayName("Não deve criar competência sem atividades")
    void naoDeveCriarCompetenciaSemAtividades() {
        CriarCompetenciaRequest request = CriarCompetenciaRequest.builder()
                .descricao("Competência inválida")
                .atividadesCodigos(List.of())
                .build();

        assertThatThrownBy(() -> subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE);

        assertThat(competenciaRepo.findByMapa_Codigo(mapa.getCodigo())).isEmpty();
        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
    }

}
