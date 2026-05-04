import {isValid, parse, parseISO} from "date-fns";

function analisarStringData(s: string): Date | null {
    const trimmed = s.trim();
    if (!trimmed) return null;

    const isoDate = parseISO(trimmed);
    if (isValid(isoDate)) return isoDate;

    try {
        const ddmmyyyy = parse(trimmed, "dd/MM/yyyy", new Date());
        if (isValid(ddmmyyyy)) return ddmmyyyy;
    } catch {
        // ignore
    }

    if (/^\d{10,}$/.test(trimmed)) {
        const d = new Date(Number(trimmed));
        if (isValid(d)) return d;
    }

    return null;
}

export function analisarData(entrada: unknown): Date | null {
    if (entrada === null || entrada === undefined || entrada === "") {
        return null;
    }

    // Suporte para objetos de contexto do BTable (bootstrap-vue-next)
    const valor = (typeof entrada === 'object' && !Array.isArray(entrada) && entrada !== null && 'value' in entrada)
        ? (entrada as { value: unknown }).value
        : entrada;

    if (valor instanceof Date) {
        return isValid(valor) ? valor : null;
    }

    if (typeof valor === "number") {
        const d = new Date(valor);
        return isValid(d) ? d : null;
    }

    if (typeof valor === "string") {
        return analisarStringData(valor);
    }

    if (Array.isArray(valor)) {
        const [ano, mes, dia, hora = 0, minuto = 0, segundo = 0] = valor;
        // Mês no JS é 0-indexed
        const d = new Date(ano, mes - 1, dia, hora, minuto, segundo);
        return isValid(d) ? d : null;
    }

    return null;
}
