import {isValid, parse, parseISO} from "date-fns";

type ValorComCampoValue = {
    value: unknown;
};

function ehValorComCampoValue(valor: unknown): valor is ValorComCampoValue {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor) && "value" in valor;
}

function normalizarData(data: Date): Date | null {
    return isValid(data) ? data : null;
}

function analisarDataTimestamp(timestamp: number): Date | null {
    return normalizarData(new Date(timestamp));
}

function analisarDataArray(valor: unknown[]): Date | null {
    const [ano, mes, dia, hora = 0, minuto = 0, segundo = 0] = valor;
    return normalizarData(new Date(Number(ano), Number(mes) - 1, Number(dia), Number(hora), Number(minuto), Number(segundo)));
}

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

    const valor = ehValorComCampoValue(entrada) ? entrada.value : entrada;

    if (valor instanceof Date) {
        return normalizarData(valor);
    }

    if (typeof valor === "number") {
        return analisarDataTimestamp(valor);
    }

    if (typeof valor === "string") {
        return analisarStringData(valor);
    }

    if (Array.isArray(valor)) {
        return analisarDataArray(valor);
    }

    return null;
}
