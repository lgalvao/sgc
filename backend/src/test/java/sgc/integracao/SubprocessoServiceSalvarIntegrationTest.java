package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.*;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtualizarCompetenciaRequest;
import sgc.subprocesso.dto.CriarCompetenciaRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    @Autowired
    private sgc.organizacao.model.UsuarioRepo usuarioRepo;
    @Autowired
    private sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;
    @MockitoBean
    private UsuarioAplicacaoService usuarioAplicacaoService;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_SALV");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo teste salvar")
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

        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("criarParaDiagnostico: deve criar subprocesso com copia de mapa")
    void criarParaDiagnostico() {
        Processo procDiag = Processo.builder()
                .descricao("Processo diag")
                .tipo(TipoProcesso.DIAGNOSTICO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(procDiag);

        Unidade uniDiag = UnidadeFixture.unidadePadrao();
        uniDiag.setCodigo(null);
        uniDiag.setSigla("DIAG");
        uniDiag = unidadeRepo.save(uniDiag);

        Usuario user = Usuario.builder().tituloEleitoral("12345").matricula("123").nome("User diag").email("a@a.com").build();
        usuarioRepo.save(user);

        Processo processoVigente = Processo.builder()
                .descricao("Processo vigente diag")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.FINALIZADO)
                .dataLimite(LocalDateTime.now().minusDays(1))
                .dataFinalizacao(LocalDateTime.now().minusHours(1))
                .build();
        processoRepo.save(processoVigente);

        Subprocesso subprocessoVigente = Subprocesso.builder()
                .unidade(uniDiag)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .processo(processoVigente)
                .dataLimiteEtapa1(LocalDateTime.now().minusDays(10))
                .dataFimEtapa1(LocalDateTime.now().minusDays(5))
                .dataFimEtapa2(LocalDateTime.now().minusHours(1))
                .build();
        subprocessoRepo.save(subprocessoVigente);

        Mapa mapaVigente = Mapa.builder().subprocesso(subprocessoVigente).build();
        mapaVigente = mapaRepo.save(mapaVigente);
        subprocessoVigente.setMapa(mapaVigente);
        subprocessoRepo.save(subprocessoVigente);

        UnidadeMapa um = UnidadeMapa.builder()
                .unidadeCodigo(uniDiag.getCodigo())
                .mapaVigente(mapaVigente)
                .build();
        unidadeMapaRepo.save(um);

        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(user);

        subprocessoService.criarParaDiagnostico(
                new sgc.subprocesso.service.SubprocessoService.CriarSubprocessoComMapaCommand(
                        procDiag,
                        uniDiag,
                        um,
                        subprocesso.getUnidade()
                ));

        List<Subprocesso> lista = subprocessoRepo.findAll();
        assertThat(lista).anyMatch(sp -> sp.getProcesso().getCodigo().equals(procDiag.getCodigo())
                && sp.getSituacao() == SituacaoSubprocesso.NAO_INICIADO);
    }

    @Test
    @DisplayName("salvarMapa: deve salvar mapa")
    void salvarMapa() {
        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        SalvarMapaRequest.CompetenciaRequest compReq = new SalvarMapaRequest.CompetenciaRequest(0L, "Nova comp", List.of(a1.getCodigo()));
        SalvarMapaRequest req = new SalvarMapaRequest("Justificativa", List.of(compReq));

        Mapa atualizado = subprocessoService.salvarMapa(subprocesso.getCodigo(), req);

        assertThat(atualizado).isNotNull();
    }

    @Test
    @DisplayName("salvarMapaSubprocesso: mudar situacao de MAPEAMENTO_CADASTRO_HOMOLOGADO")
    void salvarMapaSubprocesso_MapeamentoHomologado() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        SalvarMapaRequest.CompetenciaRequest compReq = new SalvarMapaRequest.CompetenciaRequest(0L, "Nova comp", List.of(a1.getCodigo()));
        SalvarMapaRequest req = new SalvarMapaRequest("Justificativa", List.of(compReq));

        subprocessoService.salvarMapaSubprocesso(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
    }

    @Test
    @DisplayName("salvarMapaSubprocesso: mudar situacao de REVISAO_CADASTRO_HOMOLOGADA")
    void salvarMapaSubprocesso_RevisaoHomologada() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        SalvarMapaRequest.CompetenciaRequest compReq = new SalvarMapaRequest.CompetenciaRequest(0L, "Nova comp", List.of(a1.getCodigo()));
        SalvarMapaRequest req = new SalvarMapaRequest("Justificativa", List.of(compReq));

        subprocessoService.salvarMapaSubprocesso(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
    }

    @Test
    @DisplayName("adicionarCompetencia: adicionar e mudar situacao")
    void adicionarCompetencia_MapeamentoHomologado() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        CriarCompetenciaRequest req = new CriarCompetenciaRequest("Nova comp", List.of(a1.getCodigo()));
        subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
    }

    @Test
    @DisplayName("adicionarCompetencia: adicionar e mudar situacao revisao")
    void adicionarCompetencia_RevisaoHomologada() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        CriarCompetenciaRequest req = new CriarCompetenciaRequest("Nova comp", List.of(a1.getCodigo()));
        subprocessoService.adicionarCompetencia(subprocesso.getCodigo(), req);

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
    }

    @Test
    @DisplayName("atualizarCompetencia: deve atualizar a competencia")
    void atualizarCompetencia_Sucesso() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocessoRepo.save(subprocesso);

        Atividade a1 = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Ativ 1").build();
        atividadeRepo.save(a1);

        Competencia comp = Competencia.builder().mapa(subprocesso.getMapa()).descricao("Comp velha").build();
        comp = competenciaRepo.save(comp);

        AtualizarCompetenciaRequest req = new AtualizarCompetenciaRequest("Comp atualizada", List.of(a1.getCodigo()));
        subprocessoService.atualizarCompetencia(subprocesso.getCodigo(), comp.getCodigo(), req);

        Competencia atualizada = competenciaRepo.findById(comp.getCodigo()).orElseThrow();
        assertThat(atualizada.getDescricao()).isEqualTo("Comp atualizada");
    }

    @Test
    @DisplayName("removerCompetencia: deve remover e mudar situacao MAPEAMENTO_MAPA_CRIADO para CADASTRO_HOMOLOGADO")
    void removerCompetencia_MapeamentoMapaCriado() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocessoRepo.save(subprocesso);

        Competencia comp = Competencia.builder().mapa(subprocesso.getMapa()).descricao("Unica comp").build();
        comp = competenciaRepo.save(comp);

        subprocessoService.removerCompetencia(subprocesso.getCodigo(), comp.getCodigo());

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        assertThat(competenciaRepo.findById(comp.getCodigo())).isEmpty();
    }

    @Test
    @DisplayName("removerCompetencia: deve remover e mudar situacao REVISAO_MAPA_AJUSTADO para CADASTRO_HOMOLOGADA")
    void removerCompetencia_RevisaoMapaAjustado() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(subprocesso);

        Competencia comp = Competencia.builder().mapa(subprocesso.getMapa()).descricao("Unica comp").build();
        comp = competenciaRepo.save(comp);

        subprocessoService.removerCompetencia(subprocesso.getCodigo(), comp.getCodigo());

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        assertThat(competenciaRepo.findById(comp.getCodigo())).isEmpty();
    }
}
