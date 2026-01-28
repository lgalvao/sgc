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
