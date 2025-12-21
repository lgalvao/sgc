package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.eventos.*;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

@Component
@RequiredArgsConstructor
public class MovimentacaoListener {

    private final SubprocessoMovimentacaoRepo movimentacaoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoNotificacaoService notificacaoService;

    // --- Cadastro ---

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoCadastroDisponibilizado evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Disponibilização do cadastro de atividades");

        Unidade destino = resolveDestino(evento);
        if (destino != null) {
            notificacaoService.notificarDisponibilizacaoCadastro(sp, destino);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoCadastroDevolvido evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(
                sp,
                evento,
                "Devolução do cadastro de atividades para ajustes: " + evento.getMotivo());

        Unidade destino = resolveDestino(evento);
        if (destino != null) {
            notificacaoService.notificarDevolucaoCadastro(sp, destino, evento.getMotivo());
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoCadastroAceito evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Cadastro de atividades e conhecimentos aceito");

        Unidade destino = resolveDestino(evento);
        if (destino != null) {
            notificacaoService.notificarAceiteCadastro(sp, destino);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoCadastroHomologado evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Cadastro de atividades e conhecimentos homologado");
    }

    // --- Revisao ---

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoRevisaoDisponibilizada evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Disponibilização da revisão do cadastro de atividades");

        Unidade destino = resolveDestino(evento);
        if (destino != null) {
            notificacaoService.notificarDisponibilizacaoRevisaoCadastro(sp, destino);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoRevisaoDevolvida evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(
                sp, evento, "Devolução do cadastro de atividades e conhecimentos para ajustes");

        if (evento.getUnidadeOrigem() != null && evento.getUnidadeDestino() != null) {
            notificacaoService.notificarDevolucaoRevisaoCadastro(
                    sp, evento.getUnidadeOrigem(), evento.getUnidadeDestino());
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoRevisaoAceita evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Revisão do cadastro de atividades e conhecimentos aceita");

        Unidade destino = resolveDestino(evento);
        if (destino != null) {
            notificacaoService.notificarAceiteRevisaoCadastro(sp, destino);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoRevisaoHomologada evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Cadastro de atividades e conhecimentos homologado");
    }

    // --- Mapa ---

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaDisponibilizado evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Disponibilização do mapa de competências para validação");
        notificacaoService.notificarDisponibilizacaoMapa(sp);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaComSugestoes evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Sugestões apresentadas para o mapa de competências");
        notificacaoService.notificarSugestoes(sp);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaValidado evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Validação do mapa de competências");
        notificacaoService.notificarValidacao(sp);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaAceito evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Mapa de competências validado");
        notificacaoService.notificarAceite(sp);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaDevolvido evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(
                sp, evento, "Devolução da validação do mapa de competências para ajustes");

        Unidade destino = resolveDestino(evento);
        if (destino != null) {
            notificacaoService.notificarDevolucao(sp, destino);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaHomologado evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Mapa de competências homologado");
        notificacaoService.notificarHomologacaoMapa(sp);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(EventoSubprocessoMapaAjustadoSubmetido evento) {
        Subprocesso sp = buscarSubprocesso(evento.getCodSubprocesso());
        salvarMovimentacao(sp, evento, "Disponibilização do mapa de competências para validação");
        notificacaoService.notificarDisponibilizacaoMapa(sp);
    }

    private Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo
                .findById(codigo)
                .orElseThrow(
                        () -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + codigo));
    }

    private void salvarMovimentacao(
            Subprocesso sp, EventoSubprocessoBase evento, String descricao) {
        Unidade origem =
                evento.getUnidadeOrigem() != null ? evento.getUnidadeOrigem() : sp.getUnidade();
        Unidade destino =
                evento.getUnidadeDestino() != null ? evento.getUnidadeDestino() : sp.getUnidade();

        movimentacaoRepo.save(
                new Movimentacao(sp, origem, destino, descricao, evento.getUsuario()));
    }

    private Unidade resolveDestino(EventoSubprocessoBase evento) {
        return evento.getUnidadeDestino();
    }
}
