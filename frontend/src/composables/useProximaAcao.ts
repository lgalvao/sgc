import type {Perfil} from "@/types/tipos";

interface ContextoProximaAcao {
    perfil: Perfil | null;
    situacao?: string | null;
    podeFinalizar?: boolean;
    podeDisponibilizarCadastro?: boolean;
    podeEditarCadastro?: boolean;
    isProcessoFinalizado?: boolean;
}

export function useProximaAcao() {
    function obterProximaAcao(contexto: ContextoProximaAcao): string {
        if (!contexto.perfil) return "Selecione um perfil para continuar.";

        if (contexto.isProcessoFinalizado) return "Processo concluído.";
        if (contexto.podeFinalizar) return "Próxima ação: revisar subprocessos e finalizar o processo.";
        if (contexto.podeDisponibilizarCadastro) return "Próxima ação: validar e disponibilizar o cadastro.";
        if (contexto.podeEditarCadastro) return "Próxima ação: atualizar atividades e conhecimentos.";
        if (contexto.situacao) return `Próxima ação: acompanhar a etapa "${contexto.situacao}".`;

        return "Próxima ação: acompanhar andamento do fluxo.";
    }

    return {
        obterProximaAcao,
    };
}
