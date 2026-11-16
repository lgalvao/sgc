package sesel.sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sesel.sgc.atribuicao.Atribuicao;
import sesel.sgc.processo.Processo;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoService {
    public void notificarInicioProcesso(Processo processo) {
        // TODO: Implementar envio real de notificações
        // Por enquanto, apenas simula o envio
        log.info("Enviando notificações para início do processo: {}", processo.getNome());
    }

    public void notificarAtribuicaoTemporaria(Atribuicao atribuicao) {
        // TODO: Implementar envio de notificação
        // Por enquanto apenas loga a notificação
        log.info("Notificação enviada para {} sobre atribuição temporária na unidade {}", atribuicao.getServidor().getNome(), atribuicao.getUnidade().getNome());
    }
}