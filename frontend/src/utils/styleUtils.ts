import {CLASSES_BADGE_SITUACAO} from "@/constants/situacoes";

// ===== CLASSES DE BADGE =====
export function badgeClass(situacao: string): string {
    return (
        CLASSES_BADGE_SITUACAO[situacao as keyof typeof CLASSES_BADGE_SITUACAO] ||
        "bg-secondary"
    );
}

// ===== ÍCONES DE NOTIFICAÇÃO =====
// Define local type for iconeTipo since TipoNotificacao was removed
export type LocalTipoNotificacao = "success" | "error" | "warning" | "info";

export const iconeTipo = (tipo: LocalTipoNotificacao): string => {
    switch (tipo) {
        case "success":
            return "bi bi-check-circle-fill text-success";
        case "error":
            return "bi bi-exclamation-triangle-fill text-danger";
        case "warning":
            return "bi bi-exclamation-triangle-fill text-warning";
        case "info":
            return "bi bi-info-circle-fill text-info";
        default:
            return "bi bi-bell-fill";
    }
};
