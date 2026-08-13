import path from "node:path";
import {
    auditarCdus,
    type AuditoriaCdus
} from "./cdus-auditoria-motor.js";
import {
    inventariarCdus,
    type InventarioCdus
} from "./cdus-inventario-motor.js";

interface OpcoesCasosDeUso {
    base?: string;
    secoes?: readonly string[];
}

function resolverBase(base: string | undefined): string {
    return path.resolve(base ?? process.cwd());
}

async function inventariarCasosDeUso(opcoes: OpcoesCasosDeUso = {}): Promise<InventarioCdus> {
    return inventariarCdus(resolverBase(opcoes.base), opcoes.secoes);
}

async function auditarCasosDeUso(opcoes: OpcoesCasosDeUso = {}): Promise<AuditoriaCdus> {
    return auditarCdus(resolverBase(opcoes.base), opcoes.secoes);
}

export type {AuditoriaCdus, InventarioCdus, OpcoesCasosDeUso};
export {auditarCasosDeUso, inventariarCasosDeUso};
