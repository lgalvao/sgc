import {CLASSES_BADGE_SITUACAO, LABELS_SITUACAO} from "@/constants/situacoes";

// ===== LOGGER =====
export {default as logger} from "@/utils/logger";

// ===== TREE UTILS =====
export {flattenTree} from "./treeUtils";

// ===== CLASSES DE BADGE =====
export function badgeClass(situacao: string): string {
    return (
        CLASSES_BADGE_SITUACAO[situacao as keyof typeof CLASSES_BADGE_SITUACAO] ||
        "bg-secondary"
    );
}

// ===== LABELS DE SITUAÇÃO =====
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

import {
    format,
    parseISO,
    isValid,
    isFuture,
    startOfDay,
    differenceInDays,
    parse,
} from "date-fns";
import { ptBR } from "date-fns/locale";

// ===== UTILITÁRIOS DE DATA =====
export type DateInput = string | number | Date | null | undefined;

function parseStringDate(s: string): Date | null {
    const trimmed = s.trim();
    if (!trimmed) return null;

    // ISO Date/DateTime
    const isoDate = parseISO(trimmed);
    if (isValid(isoDate)) return isoDate;

    // DD/MM/YYYY
    try {
        const ddmmyyyy = parse(trimmed, "dd/MM/yyyy", new Date());
        if (isValid(ddmmyyyy)) return ddmmyyyy;
    } catch {
        // ignore
    }

    // Numeric string
    if (/^\d{10,}$/.test(trimmed)) {
        const d = new Date(Number(trimmed));
        if (isValid(d)) return d;
    }

    return null;
}

export function parseDate(dateInput: DateInput): Date | null {
    if (dateInput === null || dateInput === undefined || dateInput === "") {
        return null;
    }

    if (dateInput instanceof Date) {
        return isValid(dateInput) ? dateInput : null;
    }

    if (typeof dateInput === "number") {
        const d = new Date(dateInput);
        return isValid(d) ? d : null;
    }

    if (typeof dateInput === "string") {
        return parseStringDate(dateInput);
    }

    return null;
}

export function formatDateBR(
    date: DateInput,
    pattern = "dd/MM/yyyy",
): string {
    if (!date) return "Não informado";
    const dateObj = parseDate(date);
    if (!dateObj) return "Data inválida";
    try {
        return format(dateObj, pattern, { locale: ptBR });
    } catch {
        return "Data inválida";
    }
}

export function formatDateForInput(date: Date | null | undefined): string {
    if (!date || !isValid(date)) return "";
    return format(date, "yyyy-MM-dd");
}

export function formatDateTimeBR(
    date: DateInput,
): string {
    return formatDateBR(date, "dd/MM/yyyy HH:mm");
}

export function isDateValidAndFuture(date: DateInput): boolean {
    const d = parseDate(date);
    if (!d) return false;
    const today = startOfDay(new Date());
    const dateToCompare = startOfDay(d);
    return isFuture(dateToCompare) || dateToCompare.getTime() === today.getTime();
}

export function diffInDays(date1: Date, date2: Date): number {
    return Math.abs(differenceInDays(date2, date1));
}

export function ensureValidDate(date: Date | null | undefined): Date | null {
    if (!date) return null;
    return isValid(date) ? date : null;
}
