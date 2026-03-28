import path from "node:path";
import {resolverSnapshotQa} from "../lib/qa.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

function formatarVerificacao(verificacao) {
    return `${verificacao.codigo} [${verificacao.status}] ${verificacao.sumario || ""}`.trim();
}

function imprimirHumano(caminho, snapshot, maxHotspots) {
    imprimirCabecalho("Resumo do QA Dashboard", `Snapshot: ${caminho}`);
    escreverLinha("");
    escreverLinha(`- Status geral: ${snapshot.resumo?.statusGeral ?? "-"}`);
    escreverLinha(`- Indice de saude: ${snapshot.resumo?.indiceSaude ?? "-"}`);
    escreverLinha(`- Verificacoes: ${snapshot.resumo?.totais?.verificacoes ?? snapshot.verificacoes?.length ?? 0}`);
    escreverLinha(`- Suites lentas: ${(snapshot.confiabilidade?.suitesLentas ?? []).length}`);
    escreverLinha("");
    escreverLinha("Verificacoes:");
    for (const verificacao of snapshot.verificacoes ?? []) {
        escreverLinha(`- ${formatarVerificacao(verificacao)}`);
    }
    escreverLinha("");
    escreverLinha("Hotspots:");
    const hotspots = (snapshot.hotspots ?? []).slice(0, maxHotspots);
    if (hotspots.length === 0) {
        escreverLinha("- Nenhum hotspot calculado.");
        return;
    }

    for (const hotspot of hotspots) {
        escreverLinha(`- ${hotspot.nome}: risco ${hotspot.risco}`);
    }
}

async function executarResumoQa(opcoes = {}) {
    const resolvido = await resolverSnapshotQa(opcoes.arquivo);
    const saida = {
        caminho: path.relative(process.cwd(), resolvido.caminho).replaceAll("\\", "/"),
        resumo: resolvido.snapshot.resumo,
        verificacoes: resolvido.snapshot.verificacoes ?? [],
        confiabilidade: resolvido.snapshot.confiabilidade ?? {},
        hotspots: resolvido.snapshot.hotspots ?? []
    };

    if (opcoes.json) {
        imprimirJson(saida);
    } else {
        imprimirHumano(saida.caminho, resolvido.snapshot, opcoes.maxHotspots ?? 5);
    }

    if (resolvido.snapshot.resumo?.statusGeral === "vermelho") {
        process.exitCode = 1;
    }

    return saida;
}

export {
    executarResumoQa
};
