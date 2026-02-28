package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.subprocesso.dto.CompetenciaRequest;

import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de Salvar e Competencias")
class SubprocessoServiceSalvarIntegrationTest extends BaseIntegrationTest {

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
        unidade.setSigla("TEST_SALV");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo Teste Salvar")
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

        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Autowired
    private sgc.organizacao.model.UsuarioRepo usuarioRepo;

    @Autowired
    private sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;

    @Test
    @DisplayName("criarParaDiagnostico: deve criar subprocesso com copia de mapa")
    void criarParaDiagnostico() {
        Processo procDiag = Processo.builder()
                .descricao("Processo Diag")
                .tipo(TipoProcesso.DIAGNOSTICO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(procDiag);

        Unidade uniDiag = UnidadeFixture.unidadePadrao();
        uniDiag.setCodigo(null);
        uniDiag.setSigla("DIAG");
        uniDiag = unidadeRepo.save(uniDiag);

        Usuario user = Usuario.builder().tituloEleitoral("12345").matricula("123").nome("User Diag").email("a@a.com").build();
        usuarioRepo.save(user);

        Mapa mapaVigente = new Mapa();
        mapaRepo.save(mapaVigente);

        UnidadeMapa um = UnidadeMapa.builder()
                .unidadeCodigo(uniDiag.getCodigo())
                .mapaVigente(mapaVigente)
                .build();
        unidadeMapaRepo.save(um);

        subprocessoService.criarParaDiagnostico(procDiag, uniDiag, um, subprocesso.getUnidade(), user);
        
        List<Subprocesso> lista = subprocessoRepo.findAll();
        assertThat(lista).anyMatch(sp -> sp.getProcesso().getCodigo().equals(procDiag.getCodigo()) 
                && sp.getSituacao() == SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("salvarMapa: deve salvar mapa")
    void salvarMapa() {
        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        SalvarMapaRequest.CompetenciaRequest compReq = new SalvarMapaRequest.CompetenciaRequest(0L, "Nova Comp", List.of(a1.getCodigo()));
        SalvarMapaRequest req = new SalvarMapaRequest("Justificativa", List.of(compReq));

        Mapa atualizado = subprocessoService.salvarMapa(subprocesso.getCodigo(), req);

        assertThat(atualizado).isNotNull();
    }

    @Test
    @DisplayName("salvarMapaSubprocesso: mudar situacao de MAPEAMENTO_CADASTRO_HOMOLOGADO")
    void salvarMapaSubprocesso_MapeamentoHomologado() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        SalvarMapaRequest.CompetenciaRequest compReq = new SalvarMapaRequest.CompetenciaRequest(0L, "Nova Comp", List.of(a1.getCodigo()));
        SalvarMapaRequest req = new SalvarMapaRequest("Justificativa", List.of(compReq));

        subprocessoService.salvarMapaSubprocesso(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
    }

    @Test
    @DisplayName("salvarMapaSubprocesso: mudar situacao de REVISAO_CADASTRO_HOMOLOGADA")
    void salvarMapaSubprocesso_RevisaoHomologada() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        SalvarMapaRequest.CompetenciaRequest compReq = new SalvarMapaRequest.CompetenciaRequest(0L, "Nova Comp", List.of(a1.getCodigo()));
        SalvarMapaRequest req = new SalvarMapaRequest("Justificativa", List.of(compReq));

        subprocessoService.salvarMapaSubprocesso(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
    }

    @Test
    @DisplayName("adicionarCompetencia: adicionar e mudar situacao")
    void adicionarCompetencia_MapeamentoHomologado() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        CompetenciaRequest req = new CompetenciaRequest("Nova Comp", List.of(a1.getCodigo()));
        subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
    }

    @Test
    @DisplayName("adicionarCompetencia: adicionar e mudar situacao revisao")
    void adicionarCompetencia_RevisaoHomologada() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        CompetenciaRequest req = new CompetenciaRequest("Nova Comp", List.of(a1.getCodigo()));
        subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
    }

    @Test
    @DisplayName("atualizarCompetencia: deve atualizar a competencia")
    void atualizarCompetencia_Sucesso() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        Competencia comp = Competencia.builder().mapa(subprocesso.getMapa()).descricao("Comp Velha").build();
        comp = competenciaRepo.save(comp);

        CompetenciaRequest req = new CompetenciaRequest("Comp Atualizada", List.of(a1.getCodigo()));
        subprocessoService.atualizarCompetencia(subprocesso.getCodigo(), comp.getCodigo(), req);

        Competencia atualizada = competenciaRepo.findById(comp.getCodigo()).get();
        assertThat(atualizada.getDescricao()).isEqualTo("Comp Atualizada");
    }

    @Test
    @DisplayName("removerCompetencia: deve remover e mudar situacao MAPEAMENTO_MAPA_CRIADO para CADASTRO_HOMOLOGADO")
    void removerCompetencia_MapeamentoMapaCriado() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocessoRepo.save(subprocesso);

        Competencia comp = Competencia.builder().mapa(subprocesso.getMapa()).descricao("Unica Comp").build();
        comp = competenciaRepo.save(comp);

        subprocessoService.removerCompetencia(subprocesso.getCodigo(), comp.getCodigo());

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        assertThat(competenciaRepo.findById(comp.getCodigo())).isEmpty();
    }

    @Test
    @DisplayName("removerCompetencia: deve remover e mudar situacao REVISAO_MAPA_AJUSTADO para CADASTRO_HOMOLOGADA")
    void removerCompetencia_RevisaoMapaAjustado() {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(subprocesso);

        Competencia comp = Competencia.builder().mapa(subprocesso.getMapa()).descricao("Unica Comp").build();
        comp = competenciaRepo.save(comp);

        subprocessoService.removerCompetencia(subprocesso.getCodigo(), comp.getCodigo());

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        assertThat(competenciaRepo.findById(comp.getCodigo())).isEmpty();
    }
}
