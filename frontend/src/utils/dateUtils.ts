import {differenceInDays, format, isFuture, isValid, parse, parseISO, startOfDay,} from "date-fns";
import {ptBR} from "date-fns/locale";

export type DateInput = string | number | Date | number[] | null | undefined;

function parseStringDate(s: string): Date | null {
    const trimmed = s.trim();
    if (!trimmed) return null;

    const isoDate = parseISO(trimmed);
    if (isValid(isoDate)) return isoDate;

    try {
        const ddmmyyyy = parse(trimmed, "dd/MM/yyyy", new Date());
        if (isValid(ddmmyyyy)) return ddmmyyyy;
    } catch {
    }

    if (/^\d{10,}$/.test(trimmed)) {
        const d = new Date(Number(trimmed));
        if (isValid(d)) return d;
    }

    return null;
}

export function parseDate(dateInput: any): Date | null {
    if (dateInput === null || dateInput === undefined || dateInput === "") {
        return null;
    }

    // Suporte para objetos de contexto do BTable (bootstrap-vue-next)
    const val = (typeof dateInput === 'object' && !Array.isArray(dateInput) && 'value' in dateInput)
        ? dateInput.value
        : dateInput;

    if (val instanceof Date) {
        return isValid(val) ? val : null;
    }

    if (typeof val === "number") {
        const d = new Date(val);
        return isValid(d) ? d : null;
    }

    if (typeof val === "string") {
        return parseStringDate(val);
    }

    if (Array.isArray(val)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = val;
        // Mês no JS é 0-indexed
        const d = new Date(year, month - 1, day, hour, minute, second);
        return isValid(d) ? d : null;
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
        return format(dateObj, pattern, {locale: ptBR});
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
