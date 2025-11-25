package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.eventos.*;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoListenerTest {

    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoNotificacaoService notificacaoService;

    @InjectMocks
    private MovimentacaoListener listener;

    private Subprocesso mockSubprocesso(Long id) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        sp.setUnidade(u);
        return sp;
    }

    @Test
    @DisplayName("handle EventoSubprocessoCadastroDisponibilizado")
    void handleCadastroDisponibilizado() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoCadastroDisponibilizado evento = EventoSubprocessoCadastroDisponibilizado.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDisponibilizacaoCadastro(sp, destino);
    }

    @Test
    @DisplayName("handle EventoSubprocessoCadastroDevolvido")
    void handleCadastroDevolvido() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoCadastroDevolvido evento = EventoSubprocessoCadastroDevolvido.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeDestino(destino)
                .motivo("motivo")
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDevolucaoCadastro(sp, destino, "motivo");
    }

    @Test
    @DisplayName("handle EventoSubprocessoCadastroAceito")
    void handleCadastroAceito() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoCadastroAceito evento = EventoSubprocessoCadastroAceito.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarAceiteCadastro(sp, destino);
    }

    @Test
    @DisplayName("handle EventoSubprocessoCadastroHomologado")
    void handleCadastroHomologado() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoCadastroHomologado evento = EventoSubprocessoCadastroHomologado.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        // No notification for this one in the listener
    }

    @Test
    @DisplayName("handle EventoSubprocessoRevisaoDisponibilizada")
    void handleRevisaoDisponibilizada() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoRevisaoDisponibilizada evento = EventoSubprocessoRevisaoDisponibilizada.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDisponibilizacaoRevisaoCadastro(sp, destino);
    }

    @Test
    @DisplayName("handle EventoSubprocessoRevisaoDevolvida")
    void handleRevisaoDevolvida() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade origem = new Unidade();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoRevisaoDevolvida evento = EventoSubprocessoRevisaoDevolvida.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDevolucaoRevisaoCadastro(sp, origem, destino);
    }

    @Test
    @DisplayName("handle EventoSubprocessoRevisaoAceita")
    void handleRevisaoAceita() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoRevisaoAceita evento = EventoSubprocessoRevisaoAceita.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarAceiteRevisaoCadastro(sp, destino);
    }

    @Test
    @DisplayName("handle EventoSubprocessoRevisaoHomologada")
    void handleRevisaoHomologada() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoRevisaoHomologada evento = EventoSubprocessoRevisaoHomologada.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaDisponibilizado")
    void handleMapaDisponibilizado() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaDisponibilizado evento = EventoSubprocessoMapaDisponibilizado.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDisponibilizacaoMapa(sp);
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaComSugestoes")
    void handleMapaComSugestoes() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaComSugestoes evento = EventoSubprocessoMapaComSugestoes.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarSugestoes(sp);
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaValidado")
    void handleMapaValidado() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaValidado evento = EventoSubprocessoMapaValidado.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarValidacao(sp);
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaAceito")
    void handleMapaAceito() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaAceito evento = EventoSubprocessoMapaAceito.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarAceite(sp);
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaDevolvido")
    void handleMapaDevolvido() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();
        Unidade destino = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaDevolvido evento = EventoSubprocessoMapaDevolvido.builder()
                .codSubprocesso(id)
                .usuario(user)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDevolucao(sp, destino);
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaHomologado")
    void handleMapaHomologado() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaHomologado evento = EventoSubprocessoMapaHomologado.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarHomologacaoMapa(sp);
    }

    @Test
    @DisplayName("handle EventoSubprocessoMapaAjustadoSubmetido")
    void handleMapaAjustadoSubmetido() {
        Long id = 1L;
        Subprocesso sp = mockSubprocesso(id);
        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        EventoSubprocessoMapaAjustadoSubmetido evento = EventoSubprocessoMapaAjustadoSubmetido.builder()
                .codSubprocesso(id)
                .usuario(user)
                .build();

        listener.handle(evento);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(notificacaoService).notificarDisponibilizacaoMapa(sp);
    }
}
