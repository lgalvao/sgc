import {ref} from 'vue';

export type VarianteAlerta = 'danger' | 'warning' | 'success' | 'info';

export interface NotificacaoEstruturada {
    resumo: string;
    detalhes: string[];
}

export interface EstadoNotificacao {
    chave: number;
    mensagem?: string;
    notificacao?: NotificacaoEstruturada;
    variante: VarianteAlerta;
    dispensavel?: boolean;
    stackTrace?: string;
}

export function useNotification() {
    const notificacao = ref<EstadoNotificacao | null>(null);
    let proximaChave = 1;

    function notify(mensagem: string, variante: VarianteAlerta = 'danger', dispensavel = true) {
        notificacao.value = {chave: proximaChave++, mensagem, variante, dispensavel};
    }

    function notifyStructured(
        resumo: string,
        detalhes: string[],
        opcoes: {
            variante?: VarianteAlerta;
            stackTrace?: string;
            dispensavel?: boolean;
        } = {}
    ) {
        const {variante = 'danger', stackTrace, dispensavel = true} = opcoes;
        notificacao.value = {
            chave: proximaChave++,
            notificacao: {resumo, detalhes},
            variante,
            stackTrace,
            dispensavel,
        };
    }

    function clear() {
        notificacao.value = null;
    }

    return {notificacao, notify, notifyStructured, clear};
}
