import {addDays, startOfDay} from "date-fns";
import {analisarData} from "./parsing";
import type {EntradaData} from "./types";

/**
 * Valida se a data é estritamente futura (pelo menos amanhã)
 */
export function ehDataEstritamenteFutura(data: EntradaData): boolean {
    const d = analisarData(data);
    if (!d) return false;
    const hoje = startOfDay(new Date());
    const amanha = startOfDay(addDays(hoje, 1));
    const dataParaComparar = startOfDay(d);
    return dataParaComparar.getTime() >= amanha.getTime();
}
