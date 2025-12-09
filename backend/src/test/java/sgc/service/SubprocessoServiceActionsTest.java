package sgc.service;

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
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.ImpactoMapaService;
import sgc.notificacao.NotificacaoEmailService;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoWorkflowService;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

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

    @Autowired private SubprocessoWorkflowService subprocessoWorkflowService;

    @Autowired private SubprocessoRepo subprocessoRepo;

    @Autowired private UnidadeRepo unidadeRepo;

    @Autowired private UsuarioRepo usuarioRepo;

    @Autowired private AnaliseRepo analiseRepo;

    @Autowired private ProcessoRepo processoRepo;

    @Autowired private MapaRepo mapaRepo;

    @Autowired private MovimentacaoRepo movimentacaoRepo;

    @MockitoBean private NotificacaoEmailService notificacaoEmailService;

    @MockitoBean private ApplicationEventPublisher eventPublisher;

    @MockitoBean private ImpactoMapaService impactoMapaService;

    @MockitoBean
    private sgc.subprocesso.service.SubprocessoNotificacaoService subprocessoNotificacaoService;

    private Unidade unidade;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(9L).orElseThrow(); // SEDIA
        usuario = usuarioRepo.findById("1").orElseThrow(); // Ana Paula Souza
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
            Subprocesso subprocesso =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            subprocessoWorkflowService.aceitarCadastro(
                    subprocesso.getCodigo(), OBSERVACOES, usuario);

            Optional<Analise> analise =
                    analiseRepo
                            .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo())
                            .stream()
                            .findFirst();
            assertTrue(analise.isPresent());
            assertEquals(
                    OBSERVACOES,
                    analise.orElseThrow(() -> new AssertionError("Análise não encontrada."))
                            .getObservacoes());

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertEquals(1, movimentacoes.size());
            assertEquals(
                    "Cadastro de atividades e conhecimentos aceito",
                    movimentacoes.getFirst().getDescricao());

            verify(subprocessoNotificacaoService, times(1))
                    .notificarAceiteCadastro(any(Subprocesso.class), any(Unidade.class));
        }
    }

    @Nested
    @DisplayName("Testes para homologarCadastro")
    class HomologarCadastroTest {
        @Test
        @Transactional
        void deveHomologarCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            subprocessoWorkflowService.homologarCadastro(
                    subprocesso.getCodigo(), OBSERVACOES, usuario);

            Subprocesso spAtualizado =
                    subprocessoRepo
                            .findById(subprocesso.getCodigo())
                            .orElseThrow(
                                    () ->
                                            new AssertionError(
                                                    "Subprocesso não encontrado após"
                                                            + " homologação."));
            assertEquals(
                    SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, spAtualizado.getSituacao());
        }
    }

    @Nested
    @DisplayName("Testes para aceitarRevisaoCadastro")
    class AceitarRevisaoCadastroTest {
        @Test
        @Transactional
        void deveAceitarRevisaoComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            subprocessoWorkflowService.aceitarRevisaoCadastro(
                    subprocesso.getCodigo(), OBSERVACOES, usuario);

            Optional<Analise> analise =
                    analiseRepo
                            .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo())
                            .stream()
                            .findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.get().getObservacoes());

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertEquals(1, movimentacoes.size());
            assertEquals(
                    "Revisão do cadastro de atividades e conhecimentos aceita",
                    movimentacoes.getFirst().getDescricao());

            verify(subprocessoNotificacaoService, times(1))
                    .notificarAceiteRevisaoCadastro(any(Subprocesso.class), any(Unidade.class));
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado() {
            assertThrows(
                    ErroEntidadeNaoEncontrada.class,
                    () ->
                            subprocessoWorkflowService.aceitarRevisaoCadastro(
                                    999L, OBSERVACOES, usuario));
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso sp =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            assertThrows(
                    IllegalStateException.class,
                    () ->
                            subprocessoWorkflowService.aceitarRevisaoCadastro(
                                    sp.getCodigo(), OBSERVACOES, usuario));
        }
    }

    @Nested
    @DisplayName("Testes para homologarRevisaoCadastro")
    class HomologarRevisaoCadastroTest {
        @Test
        @Transactional
        void deveHomologarRevisaoComSucessoSemImpactos() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            // Primeiro, aceitar a revisão para que a situação mude para
            // AGUARDANDO_HOMOLOGACAO_CADASTRO
            subprocessoWorkflowService.aceitarRevisaoCadastro(
                    subprocesso.getCodigo(), OBSERVACOES, usuario);

            // Recarregar o subprocesso do repositório para garantir que o estado esteja
            // atualizado
            Subprocesso subprocessoAposAceite =
                    subprocessoRepo
                            .findById(subprocesso.getCodigo())
                            .orElseThrow(
                                    () ->
                                            new AssertionError(
                                                    "Subprocesso não encontrado após aceite da"
                                                            + " revisão."));

            when(impactoMapaService.verificarImpactos(anyLong(), any(Usuario.class)))
                    .thenReturn(ImpactoMapaDto.semImpacto());

            subprocessoWorkflowService.homologarRevisaoCadastro(
                    subprocessoAposAceite.getCodigo(), OBSERVACOES, usuario);

            Subprocesso spAtualizado =
                    subprocessoRepo
                            .findById(subprocesso.getCodigo())
                            .orElseThrow(
                                    () ->
                                            new AssertionError(
                                                    "Subprocesso não encontrado após homologação da"
                                                            + " revisão."));
            assertEquals(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO, spAtualizado.getSituacao());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado_homologar() {
            assertThrows(
                    ErroEntidadeNaoEncontrada.class,
                    () ->
                            subprocessoWorkflowService.homologarRevisaoCadastro(
                                    999L, OBSERVACOES, usuario));
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta_homologar() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            assertThrows(
                    IllegalStateException.class,
                    () ->
                            subprocessoWorkflowService.homologarRevisaoCadastro(
                                    subprocesso.getCodigo(), OBSERVACOES, usuario));
        }
    }

    @Nested
    @DisplayName("Testes para devolverCadastro")
    class DevolverCadastroTest {
        @Test
        @Transactional
        void deveDevolverCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso =
                    criarSubprocesso(
                            processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            subprocessoWorkflowService.devolverCadastro(
                    subprocesso.getCodigo(), OBSERVACOES, usuario);

            Subprocesso spAtualizado =
                    subprocessoRepo
                            .findById(subprocesso.getCodigo())
                            .orElseThrow(
                                    () ->
                                            new AssertionError(
                                                    "Subprocesso não encontrado após devolução."));

            assertEquals(
                    SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                    spAtualizado.getSituacao());
            assertNull(spAtualizado.getDataFimEtapa1());
        }
    }
}
