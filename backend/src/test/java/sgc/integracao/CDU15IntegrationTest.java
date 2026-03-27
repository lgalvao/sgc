package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
        CompetenciaRequest request = CompetenciaRequest.builder()
                .descricao("Competência CDU-15")
                .atividadesIds(List.of(atividade1.getCodigo(), atividade2.getCodigo()))
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
        CompetenciaRequest criarReq = CompetenciaRequest.builder()
                .descricao("Competência temporária")
                .atividadesIds(List.of(atividade1.getCodigo()))
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
        CompetenciaRequest request = CompetenciaRequest.builder()
                .descricao("Competência inválida")
                .atividadesIds(List.of())
                .build();

        assertThatThrownBy(() -> subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE);

        assertThat(competenciaRepo.findByMapa_Codigo(mapa.getCodigo())).isEmpty();
        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
    }

}
