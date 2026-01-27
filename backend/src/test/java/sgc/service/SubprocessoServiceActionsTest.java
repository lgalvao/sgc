package sgc.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Ações do SubprocessoService")
class SubprocessoServiceActionsTest {
    private static final String OBSERVACOES = "Observações de teste";

    @Autowired
    private SubprocessoWorkflowService subprocessoWorkflowService;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioFacade usuarioService;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;


    @MockitoBean
    private ImpactoMapaService impactoMapaService;

    private Unidade unidade;
    private Usuario admin;
    private Usuario gestor;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        unidade = unidadeRepo.findById(9L).orElseThrow(); // SEDIA
        admin = carregarUsuarioComPerfis("6"); // Ricardo Alves - ADMIN
        gestor = carregarUsuarioComPerfis("666666666666"); // Gestor COSIS - GESTOR
    }

    private Usuario carregarUsuarioComPerfis(String titulo) {
        Usuario usuario = usuarioService.carregarUsuarioParaAutenticacao(titulo);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado: " + titulo);
        }
        return usuario;
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
    @SuppressWarnings("unused")
    class AceitarCadastroTest {
        @Test
        @Transactional
        void deveAceitarCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            subprocessoWorkflowService.aceitarCadastro(subprocesso.getCodigo(), OBSERVACOES, gestor);

            Optional<Analise> analise = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).stream().findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.orElseThrow(() -> new AssertionError("Análise não encontrada.")).getObservacoes());

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertEquals(1, movimentacoes.size());
            assertEquals("Cadastro de atividades e conhecimentos aceito", movimentacoes.getFirst().getDescricao());
        }
    }

    @Nested
    @DisplayName("Testes para homologarCadastro")
    @SuppressWarnings("unused")
    class HomologarCadastroTest {
        @Test
        @Transactional
        void deveHomologarCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            subprocessoWorkflowService.homologarCadastro(subprocesso.getCodigo(), OBSERVACOES, admin);

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow(() -> new AssertionError("Subprocesso não encontrado após" + " homologação."));
            assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, spAtualizado.getSituacao());
        }
    }

    @Nested
    @DisplayName("Testes para aceitarRevisaoCadastro")
    @SuppressWarnings("unused")
    class AceitarRevisaoCadastroTest {
        @Test
        @Transactional
        void deveAceitarRevisaoComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoWorkflowService.aceitarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, gestor);

            Optional<Analise> analise = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).stream().findFirst();
            assertTrue(analise.isPresent());
            assertEquals(OBSERVACOES, analise.get().getObservacoes());

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertEquals(1, movimentacoes.size());
            assertEquals("Revisão do cadastro de atividades e conhecimentos aceita", movimentacoes.getFirst().getDescricao());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado() {
            var exception = assertThrows(ErroEntidadeNaoEncontrada.class, () -> subprocessoWorkflowService.aceitarRevisaoCadastro(999L, OBSERVACOES, gestor));
            assertNotNull(exception);
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso sp = criarSubprocesso(processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            Long spCodigo = sp.getCodigo();
            ErroAccessoNegado erro = assertThrows(ErroAccessoNegado.class,
                    () -> subprocessoWorkflowService.aceitarRevisaoCadastro(spCodigo, OBSERVACOES, gestor));
            assertTrue(erro.getMessage().contains("situação"),
                    "Mensagem de erro deve mencionar a situação incorreta");
        }
    }

    @Nested
    @DisplayName("Testes para homologarRevisaoCadastro")
    @SuppressWarnings("unused")
    class HomologarRevisaoCadastroTest {
        @Test
        @Transactional
        void deveHomologarRevisaoComSucessoSemImpactos() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            // Primeiro, aceitar a revisão para que a situação mude para AGUARDANDO_HOMOLOGACAO_CADASTRO
            subprocessoWorkflowService.aceitarRevisaoCadastro(subprocesso.getCodigo(), OBSERVACOES, gestor);

            // Recarregar o subprocesso do repositório para garantir que o estado esteja atualizado
            Subprocesso subprocessoAposAceite = subprocessoRepo.findById(subprocesso.getCodigo())
                    .orElseThrow(() -> new AssertionError("Subprocesso não encontrado após aceite da revisão."));

            when(impactoMapaService.verificarImpactos(any(Subprocesso.class), any(Usuario.class))).thenReturn(ImpactoMapaDto.semImpacto());

            // Homologação requer perfil ADMIN
            subprocessoWorkflowService.homologarRevisaoCadastro(subprocessoAposAceite.getCodigo(), OBSERVACOES, admin);

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo())
                    .orElseThrow(() -> new AssertionError("Subprocesso não encontrado após homologação da revisão."));

            assertEquals(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO, spAtualizado.getSituacao());
        }

        @Test
        void deveLancarExcecaoSeSubprocessoNaoEncontrado_homologar() {
            var exception = assertThrows(ErroEntidadeNaoEncontrada.class, () -> subprocessoWorkflowService.homologarRevisaoCadastro(999L, OBSERVACOES, admin));
            assertNotNull(exception);
        }

        @Test
        @Transactional
        void deveLancarExcecaoSeSituacaoIncorreta_homologar() {
            Processo processo = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            Long spCodigo = subprocesso.getCodigo();
            ErroAccessoNegado erro = assertThrows(ErroAccessoNegado.class,
                    () -> subprocessoWorkflowService.homologarRevisaoCadastro(spCodigo, OBSERVACOES, admin));

            assertTrue(erro.getMessage().contains("situação"),
                    "Mensagem de erro deve mencionar a situação incorreta");
        }
    }

    @Nested
    @DisplayName("Testes para devolverCadastro")
    @SuppressWarnings("unused")
    class DevolverCadastroTest {
        @Test
        @Transactional
        void deveDevolverCadastroComSucesso() {
            Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso subprocesso = criarSubprocesso(processo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            subprocessoWorkflowService.devolverCadastro(subprocesso.getCodigo(), OBSERVACOES, gestor);

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo())
                    .orElseThrow(() -> new AssertionError("Subprocesso não encontrado após devolução."));

            assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, spAtualizado.getSituacao());
            assertNull(spAtualizado.getDataFimEtapa1());
        }
    }
}
