import {addDays, format, isValid} from "date-fns";
import {ptBR} from "date-fns/locale";
import {analisarData} from "./parsing";
import type {EntradaData} from "./types";

/**
 * Retorna a data de amanhã formatada para inputs (yyyy-MM-dd)
 */
export function obterAmanhaFormatado(): string {
    return format(addDays(new Date(), 1), "yyyy-MM-dd");
}

/**
 * Retorna a data de hoje formatada para inputs (yyyy-MM-dd)
 */
export function obterHojeFormatado(): string {
    return format(new Date(), "yyyy-MM-dd");
}

export function formatarDataBR(
    data: EntradaData,
    padrao = "dd/MM/yyyy",
): string {
    if (!data) return "Não informado";
    const objetoData = analisarData(data);
    if (!objetoData) return "Data inválida";
    try {
        return format(objetoData, padrao, {locale: ptBR});
    } catch {
        return "Data inválida";
    }
}

export function formatarDataParaInput(data: Date | null | undefined): string {
    if (!data || !isValid(data)) return "";
    return format(data, "yyyy-MM-dd");
}

export function formatarDataHoraBR(
    data: EntradaData,
): string {
    return formatarDataBR(data, "dd/MM/yyyy HH:mm");
}
