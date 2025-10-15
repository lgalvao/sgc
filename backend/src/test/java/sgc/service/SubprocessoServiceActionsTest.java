package sgc.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.AnaliseRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.notificacao.NotificacaoService;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Ações do SubprocessoService")
public class SubprocessoServiceActionsTest {
    private static final String OBSERVACOES = "Observações de teste";

    @Autowired
    private sgc.subprocesso.SubprocessoWorkflowService subprocessoWorkflowService;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private sgc.mapa.modelo.MapaRepo mapaRepo;

    @Autowired
    private sgc.subprocesso.modelo.MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private ImpactoMapaService impactoMapaService;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        unidadeSuperior = new Unidade("Unidade Superior", "US");
        unidadeRepo.save(unidadeSuperior);

        unidade = new Unidade("Unidade Teste", "UT");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(unidade);

        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral(111122223333L);
        chefe.setPerfis(java.util.Set.of(Perfil.CHEFE));
        usuarioRepo.save(chefe);
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        usuario = new Usuario();
        usuario.setTituloEleitoral(444455556666L);
        usuario.setUnidade(unidade);
        usuarioRepo.save(usuario);
    }

    private Processo criarProcesso(TipoProcesso tipo) {
        Processo processo = new Processo();
        processo.setTipo(tipo);
        processo.setDescricao("Processo de Teste");
        return processoRepo.save(processo);
    }

    private Subprocesso criarSubprocesso(Processo processo, SituacaoSubprocesso situacao) {
        Mapa mapa = new Mapa();
        mapaRepo.save(mapa);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(situacao);
        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);
        return subprocesso;
    }

    @Nested
    @DisplayName("Testes para aceitarCadastro")
    class AceitarCadastroTest {
        @Test
        @Transactional
        void deveAceitarCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);

            subprocessoWorkflowService.aceitarCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario.getTituloEleitoral());

            Optional<Analise> analise = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).stream().findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.orElseThrow(() -> new AssertionError("Análise não encontrada.")).getObservacoes());

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertEquals(1, movimentacoes.size());
            assertEquals("Cadastro de atividades e conhecimentos aceito", movimentacoes.getFirst().getDescricao());

            List<Alerta> alertas = alertaRepo.findAll();
            assertEquals(1, alertas.size());
            assertTrue(alertas.getFirst().getDescricao().contains("submetido para análise"));

            verify(notificacaoService, times(1)).enviarEmail(eq(unidadeSuperior.getSigla()), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Testes para homologarCadastro")
    class HomologarCadastroTest {
        @Test
        @Transactional
        void deveHomologarCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);

            Unidade sedoc = new Unidade("SEDOC", "SEDOC");
            unidadeRepo.save(sedoc);

            subprocessoWorkflowService.homologarCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario.getTituloEleitoral());

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow(() -> new AssertionError("Subprocesso não encontrado após homologação."));
            assertEquals(SituacaoSubprocesso.CADASTRO_HOMOLOGADO, spAtualizado.getSituacao());
        }
    }

    @Nested
    @DisplayName("Testes para aceitarRevisaoCadastro")
    class AceitarRevisaoCadastroTest {
        @Test
        @Transactional
        void deveAceitarRevisaoComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            subprocessoWorkflowService.aceitarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario);

            Optional<Analise> analise = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).stream().findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.get().getObservacoes());

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertEquals(1, movimentacoes.size());
            assertEquals("Revisão do cadastro de atividades e conhecimentos aceita", movimentacoes.getFirst().getDescricao());

            verify(notificacaoService, times(1)).enviarEmail(eq(unidadeSuperior.getSigla()), anyString(), anyString());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado() {
            assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoWorkflowService.aceitarRevisaoCadastro(999L, OBSERVACOES, usuario));
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso sp = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            assertThrows(IllegalStateException.class, () -> subprocessoWorkflowService.aceitarRevisaoCadastro(sp.getCodigo(), OBSERVACOES, usuario));
        }
    }

    @Nested
    @DisplayName("Testes para homologarRevisaoCadastro")
    class HomologarRevisaoCadastroTest {
        @Test
        @Transactional
        void deveHomologarRevisaoComSucessoSemImpactos() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            when(impactoMapaService.verificarImpactos(anyLong(), any(Usuario.class)))
                    .thenReturn(ImpactoMapaDto.semImpacto());

            subprocessoWorkflowService.homologarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario);

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow(() -> new AssertionError("Subprocesso não encontrado após homologação da revisão."));
            assertEquals(SituacaoSubprocesso.MAPA_HOMOLOGADO, spAtualizado.getSituacao());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado_homologar() {
            assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoWorkflowService.homologarRevisaoCadastro(999L, OBSERVACOES, usuario));
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta_homologar() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            assertThrows(IllegalStateException.class, () -> subprocessoWorkflowService.homologarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario));
        }
    }

    @Nested
    @DisplayName("Testes para devolverCadastro")
    class DevolverCadastroTest {
        @Test
        @Transactional
        void deveDevolverCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);

            subprocessoWorkflowService.devolverCadastro(subprocesso.getCodigo(), "Motivo Teste", OBSERVACOES, usuario);

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow(() -> new AssertionError("Subprocesso não encontrado após devolução."));
            assertEquals(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, spAtualizado.getSituacao());
            assertNull(spAtualizado.getDataFimEtapa1());
        }
    }
}