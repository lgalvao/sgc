package sgc.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.processo.modelo.TipoProcesso;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.notificacao.NotificacaoServico;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.SubprocessoService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Collections;
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
    private SubprocessoService subprocessoService;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AnaliseCadastroRepo analiseCadastroRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private NotificacaoServico notificacaoServico;

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
        chefe.setTitulo("chefe_ut");
        usuarioRepo.save(chefe);
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        usuario = new Usuario();
        usuario.setTitulo("user_test");
        usuario.setUnidade(unidade);
        usuarioRepo.save(usuario);
    }

    private Processo criarProcesso(TipoProcesso tipo) {
        Processo processo = new Processo();
        processo.setTipo(tipo);
        processo.setDescricao("Processo de Teste");
        entityManager.persist(processo);
        return processo;
    }

    private Subprocesso criarSubprocesso(Processo processo, SituacaoSubprocesso situacao) {
        Mapa mapa = new Mapa();
        entityManager.persist(mapa);

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

            SubprocessoDto result = subprocessoService.aceitarCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario.getTitulo());

            assertNotNull(result);
            Optional<AnaliseCadastro> analise = analiseCadastroRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).stream().findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.get().getObservacoes());

            List<Movimentacao> movimentacoes = entityManager.createQuery("SELECT m FROM Movimentacao m WHERE m.subprocesso.codigo = :spId", Movimentacao.class)
                .setParameter("spId", subprocesso.getCodigo())
                .getResultList();
            assertEquals(1, movimentacoes.size());
            assertEquals("Cadastro de atividades e conhecimentos aceito", movimentacoes.getFirst().getDescricao());

            List<Alerta> alertas = alertaRepo.findAll();
            assertEquals(1, alertas.size());
            assertTrue(alertas.getFirst().getDescricao().contains("submetido para análise"));

            verify(notificacaoServico, times(1)).enviarEmail(eq(unidadeSuperior.getSigla()), anyString(), anyString());
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

            SubprocessoDto result = subprocessoService.homologarCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario.getTitulo());

            assertNotNull(result);
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
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

            SubprocessoDto result = subprocessoService.aceitarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario);

            assertNotNull(result);
            Optional<AnaliseCadastro> analise = analiseCadastroRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).stream().findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.get().getObservacoes());

            List<Movimentacao> movimentacoes = entityManager.createQuery("SELECT m FROM Movimentacao m WHERE m.subprocesso.codigo = :spId", Movimentacao.class)
                .setParameter("spId", subprocesso.getCodigo())
                .getResultList();
            assertEquals(1, movimentacoes.size());
            assertEquals("Revisão do cadastro de atividades e conhecimentos aceita", movimentacoes.getFirst().getDescricao());

            verify(notificacaoServico, times(1)).enviarEmail(eq(unidadeSuperior.getSigla()), anyString(), anyString());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado() {
            assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.aceitarRevisaoCadastro(999L, OBSERVACOES, usuario));
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso sp = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            assertThrows(IllegalStateException.class, () -> subprocessoService.aceitarRevisaoCadastro(sp.getCodigo(), OBSERVACOES, usuario));
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
                .thenReturn(new ImpactoMapaDto(false, 0,0,0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));


            SubprocessoDto result = subprocessoService.homologarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario);

            assertNotNull(result);
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
            assertEquals(SituacaoSubprocesso.MAPA_HOMOLOGADO, spAtualizado.getSituacao());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado_homologar() {
            assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.homologarRevisaoCadastro(999L, OBSERVACOES, usuario));
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta_homologar() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            assertThrows(IllegalStateException.class, () -> subprocessoService.homologarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, usuario));
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

            SubprocessoDto result = subprocessoService.devolverCadastro(subprocesso.getCodigo(), "Motivo Teste", OBSERVACOES, usuario);

            assertNotNull(result);
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).get();
            assertEquals(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, spAtualizado.getSituacao());
            assertNull(spAtualizado.getDataFimEtapa1());
        }
    }
}