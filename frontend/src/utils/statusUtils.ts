import { LABELS_SITUACAO } from "@/constants/situacoes";

export function situacaoLabel(situacao?: string | null): string {
    if (!situacao) return "Não disponibilizado";

    const backendLabels: Record<string, string> = {
        // Initial status
        NAO_INICIADO: "Não iniciado",

        // Mapeamento statuses
        MAPEAMENTO_CADASTRO_EM_ANDAMENTO: "Cadastro em andamento",
        MAPEAMENTO_CADASTRO_DISPONIBILIZADO: "Cadastro disponibilizado",
        MAPEAMENTO_CADASTRO_HOMOLOGADO: "Cadastro homologado",
        MAPEAMENTO_MAPA_CRIADO: "Mapa criado",
        MAPEAMENTO_MAPA_DISPONIBILIZADO: "Mapa disponibilizado",
        MAPEAMENTO_MAPA_COM_SUGESTOES: "Mapa com sugestões",
        MAPEAMENTO_MAPA_VALIDADO: "Mapa validado",
        MAPEAMENTO_MAPA_HOMOLOGADO: "Mapa homologado",

        // Revisão statuses
        REVISAO_CADASTRO_EM_ANDAMENTO: "Revisão de cadastro em andamento",
        REVISAO_CADASTRO_DISPONIBILIZADA: "Revisão de cadastro disponibilizada",
        REVISAO_CADASTRO_HOMOLOGADA: "Revisão de cadastro homologada",
        REVISAO_MAPA_AJUSTADO: "Mapa ajustado",
        REVISAO_MAPA_DISPONIBILIZADO: "Mapa disponibilizado",
        REVISAO_MAPA_COM_SUGESTOES: "Mapa com sugestões",
        REVISAO_MAPA_VALIDADO: "Mapa validado",
        REVISAO_MAPA_HOMOLOGADO: "Mapa homologado",

        // Legacy statuses (for backward compatibility)
        MAPA_DISPONIBILIZADO: "Mapa disponibilizado",
        MAPA_VALIDADO: "Mapa validado",
        MAPA_HOMOLOGADO: "Mapa homologado",
        CADASTRO_HOMOLOGADO: "Cadastro homologado",
        CADASTRO_DISPONIBILIZADO: "Cadastro disponibilizado",
        CADASTRO_EM_ANDAMENTO: "Cadastro em andamento",
    };

    if (backendLabels[situacao]) return backendLabels[situacao];

    return LABELS_SITUACAO[situacao as keyof typeof LABELS_SITUACAO] || situacao;
}
