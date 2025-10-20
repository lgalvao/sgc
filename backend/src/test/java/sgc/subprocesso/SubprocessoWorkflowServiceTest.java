package sgc.subprocesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.analise.AnaliseService;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.processo.eventos.SubprocessoDisponibilizadoEvento;
import sgc.processo.eventos.SubprocessoRevisaoDisponibilizadaEvento;
import sgc.sgrh.Usuario;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoWorkflowServiceTest {

    @InjectMocks
    private SubprocessoWorkflowService service;

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private AnaliseService analiseService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoNotificacaoService notificacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;

    private Usuario usuario;
    private Subprocesso subprocesso;
    private Unidade unidade;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setTituloEleitoral(1L);
        unidade = new Unidade();
        mapa = new Mapa();
        mapa.setCodigo(1L);
        unidade.setTitular(usuario);
        usuario.setUnidade(unidade);
        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
    }

    @Nested
    @DisplayName("Testes para disponibilizarCadastro")
    class DisponibilizarCadastroTests {
        @Test
        @DisplayName("Deve disponibilizar cadastro com sucesso")
        void disponibilizarCadastro_Sucesso() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());

            service.disponibilizarCadastro(1L, usuario);

            verify(subprocessoRepo).save(subprocesso);
            verify(movimentacaoRepo).save(any());
            verify(notificacaoService).notificarAceiteCadastro(any(), any());
            verify(eventPublisher).publishEvent(any(SubprocessoDisponibilizadoEvento.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se usuário não for chefe da unidade")
        void disponibilizarCadastro_UsuarioNaoChefe_LancaExcecao() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setTituloEleitoral(2L);
            unidade.setTitular(outroUsuario);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            assertThrows(ErroDominioAccessoNegado.class, () -> service.disponibilizarCadastro(1L, usuario));
        }
    }

    @Nested
    @DisplayName("Testes para homologarRevisaoCadastro")
    class HomologarRevisaoCadastroTests {

        @BeforeEach
        void setup() {
            subprocesso.setSituacao(SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        }

        @Test
        @DisplayName("Deve homologar revisão de cadastro com impactos")
        void homologarRevisaoCadastro_ComImpactos_RevisaoHomologada() {
            when(impactoMapaService.verificarImpactos(anyLong(), any(Usuario.class))).thenReturn(ImpactoMapaDto.comImpactos(Collections.singletonList(mock(AtividadeImpactadaDto.class)), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(new Unidade()));

            service.homologarRevisaoCadastro(1L, "Obs", usuario);

            assertEquals(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, subprocesso.getSituacao());
            verify(movimentacaoRepo).save(any());
            verify(subprocessoRepo).save(subprocesso);
        }

        @Test
        @DisplayName("Deve homologar revisão de cadastro sem impactos")
        void homologarRevisaoCadastro_SemImpactos_MapaHomologado() {
            when(impactoMapaService.verificarImpactos(anyLong(), any(Usuario.class))).thenReturn(ImpactoMapaDto.semImpacto());

            service.homologarRevisaoCadastro(1L, "Obs", usuario);

            assertEquals(SituacaoSubprocesso.MAPA_HOMOLOGADO, subprocesso.getSituacao());
            verify(subprocessoRepo).save(subprocesso);
        }

        @Test
        @DisplayName("Deve lançar exceção se subprocesso não estiver aguardando homologação")
        void homologarRevisaoCadastro_EstadoInvalido_LancaExcecao() {
            subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            assertThrows(IllegalStateException.class, () -> service.homologarRevisaoCadastro(1L, "obs", usuario));
        }
    }
    @Test
    @DisplayName("Deve devolver cadastro com sucesso")
    void devolverCadastro_Sucesso() {
        Unidade unidadeSuperior = new Unidade();
        unidade.setUnidadeSuperior(unidadeSuperior);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

        service.devolverCadastro(1L, "Motivo", "Obs", usuario);

        verify(analiseService).criarAnalise(any());
        verify(movimentacaoRepo).save(any());
        verify(subprocessoRepo).save(subprocesso);
        verify(notificacaoService).notificarDevolucaoCadastro(any(), any(), any());
    }

    @Test
    @DisplayName("Deve aceitar cadastro com sucesso")
    void aceitarCadastro_Sucesso() {
        Unidade unidadeSuperior = new Unidade();
        unidade.setUnidadeSuperior(unidadeSuperior);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

        service.aceitarCadastro(1L, "Obs", 123L);

        verify(analiseService).criarAnalise(any());
        verify(movimentacaoRepo).save(any());
        verify(subprocessoRepo).save(subprocesso);
        verify(notificacaoService).notificarAceiteCadastro(any(), any());
    }

    @Test
    @DisplayName("Deve devolver revisão de cadastro com sucesso")
    void devolverRevisaoCadastro_Sucesso() {
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

        service.devolverRevisaoCadastro(1L, "Motivo", "Obs", usuario);

        verify(analiseService).criarAnalise(any());
        verify(movimentacaoRepo).save(any());
        verify(subprocessoRepo).save(subprocesso);
        verify(notificacaoService).notificarDevolucaoRevisaoCadastro(any(), any(), any());
    }

    @Test
    @DisplayName("Deve aceitar revisão de cadastro com sucesso")
    void aceitarRevisaoCadastro_Sucesso() {
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade unidadeSuperior = new Unidade();
        unidade.setUnidadeSuperior(unidadeSuperior);
        usuario.setUnidade(unidade);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

        service.aceitarRevisaoCadastro(1L, "Obs", usuario);

        verify(analiseService).criarAnalise(any());
        verify(movimentacaoRepo).save(any());
        verify(subprocessoRepo).save(subprocesso);
        verify(notificacaoService).notificarAceiteRevisaoCadastro(any(), any());
    }

    @Test
    @DisplayName("Deve disponibilizar revisão com sucesso")
    void disponibilizarRevisao_Sucesso() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());

        service.disponibilizarRevisao(1L, usuario);

        verify(subprocessoRepo).save(subprocesso);
        verify(movimentacaoRepo).save(any());
        verify(analiseService).removerPorSubprocesso(1L);
        verify(notificacaoService).notificarAceiteRevisaoCadastro(any(), any());
        verify(eventPublisher).publishEvent(any(SubprocessoRevisaoDisponibilizadaEvento.class));
    }

    @Test
    @DisplayName("Deve validar mapa com sucesso")
    void validarMapa_Sucesso() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

        service.validarMapa(1L, 123L);

        verify(subprocessoRepo).save(subprocesso);
        verify(movimentacaoRepo).save(any());
        verify(analiseService).removerPorSubprocesso(1L);
        verify(notificacaoService).notificarValidacao(any());
    }

    @Test
    @DisplayName("Deve homologar cadastro com sucesso")
    void homologarCadastro_Sucesso() {
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(new Unidade()));

        service.homologarCadastro(1L, "Obs", 123L);

        verify(movimentacaoRepo).save(any());
        verify(subprocessoRepo).save(subprocesso);
    }
}
