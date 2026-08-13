import fs from "node:fs/promises";
import path from "node:path";

interface ResultadoJUnit extends Record<string, unknown> {
    testes: number;
    falhas: number;
    ignorados: number;
    tempoSegundos: number;
    sucessos: number;
    arquivosXml: string[];
}

interface HotspotQualidade {
    arquivo: string;
    score: number;
}

function parseJsonSeguro<T>(conteudo: string, fallback: T): T {
    try {
        return JSON.parse(conteudo);
    } catch {
        return fallback;
    }
}

async function consolidarJUnit(diretorioRelatorio: string, base: string): Promise<ResultadoJUnit> {
    const entries = await fs.readdir(diretorioRelatorio, {withFileTypes: true}).catch(() => []);
    const arquivos = entries.filter(e => e.isFile() && e.name.endsWith(".xml")).map(e => path.join(diretorioRelatorio, e.name));

    const totais: ResultadoJUnit = {testes: 0, falhas: 0, ignorados: 0, tempoSegundos: 0, sucessos: 0, arquivosXml: []};
    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        totais.testes += Number(conteudo.match(/tests="(\d+)"/)?.[1] ?? 0);
        totais.falhas += Number(conteudo.match(/failures="(\d+)"/)?.[1] ?? 0) + Number(conteudo.match(/errors="(\d+)"/)?.[1] ?? 0);
        totais.ignorados += Number(conteudo.match(/skipped="(\d+)"/)?.[1] ?? 0);
        totais.tempoSegundos += Number(conteudo.match(/time="([0-9.]+)"/)?.[1] ?? 0);
    }
    totais.sucessos = Math.max(totais.testes - totais.falhas - totais.ignorados, 0);
    totais.arquivosXml = arquivos.map((arquivo) => path.relative(base, arquivo).replace(/\\/g, "/"));
    return totais;
}

function ehHotspotQualidade(valor: unknown): valor is HotspotQualidade {
    if (!valor || typeof valor !== "object") {
        return false;
    }
    const hotspot = valor as Record<string, unknown>;
    return typeof hotspot.arquivo === "string" && typeof hotspot.score === "number";
}

function extrairHotspotsQualidade(metricas: unknown): HotspotQualidade[] {
    if (!metricas || typeof metricas !== "object") {
        return [];
    }
    const hotspots = (metricas as Record<string, unknown>).hotspots;
    return Array.isArray(hotspots) ? hotspots.filter(ehHotspotQualidade) : [];
}

export {
    consolidarJUnit,
    extrairHotspotsQualidade,
    parseJsonSeguro,
    type HotspotQualidade,
    type ResultadoJUnit
};
