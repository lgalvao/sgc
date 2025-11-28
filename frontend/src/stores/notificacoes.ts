import {defineStore} from "pinia";
import {ref} from "vue";

export type TipoNotificacao =
    | "success"
    | "error"
    | "warning"
    | "info";

export interface Notificacao {
    id: string;
    tipo: TipoNotificacao;
    titulo: string;
    mensagem: string;
    testId?: string;
    duracao?: number; // em milissegundos
    timestamp: Date;
    mostrar: boolean; // Controla a visibilidade do toast
}

export const useNotificacoesStore = defineStore("notificacoes", () => {
    const notificacoes = ref<Notificacao[]>([]);

    const adicionarNotificacao = (notificacao: Omit<Notificacao, "id" | "timestamp" | "mostrar">) => {
        const novaNotificacao: Notificacao = {
            ...notificacao,
            id: Date.now().toString() + Math.random().toString(36).slice(2, 11),
            timestamp: new Date(),
            mostrar: true,
            testId: (notificacao as any).testId || `notificacao-${notificacao.tipo}`,
            duracao: notificacao.duracao ?? (notificacao.tipo === "success" ? 3000 : 0),
        };

        notificacoes.value.push(novaNotificacao);

        if (novaNotificacao.duracao && novaNotificacao.duracao > 0) {
            setTimeout(() => removerNotificacao(novaNotificacao.id), novaNotificacao.duracao);
        }

        return novaNotificacao.id;
    };

    const removerNotificacao = (id: string) => {
        const index = notificacoes.value.findIndex((n) => n.id === id);
        if (index > -1) {
            notificacoes.value.splice(index, 1);
        }
    };

    const limparTodas = () => {
        notificacoes.value = [];
    };

    const sucesso = (titulo: string, mensagem: string, duracao?: number) => {
        return adicionarNotificacao({tipo: "success", titulo, mensagem, duracao});
    };

    const erro = (titulo: string, mensagem: string, duracao?: number) => {
        return adicionarNotificacao({tipo: "error", titulo, mensagem, duracao});
    };

    const aviso = (titulo: string, mensagem: string, duracao?: number) => {
        return adicionarNotificacao({tipo: "warning", titulo, mensagem, duracao});
    };

    const info = (titulo: string, mensagem: string, duracao?: number) => {
        return adicionarNotificacao({tipo: "info", titulo, mensagem, duracao});
    };

    return {
        notificacoes,
        adicionarNotificacao,
        removerNotificacao,
        limparTodas,
        sucesso,
        erro,
        aviso,
        info,
    };
});
