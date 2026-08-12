import path from "node:path";
import {resolverFotografiaQualidade} from "../lib/qualidade.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

function formatarVerificacao(verificacao) {
    return `${verificacao.codigo} [${verificacao.status}] ${verificacao.sumario || ""}`.trim();
}

function imprimirHumano(caminho, fotografia, limitePontosCriticos) {
    imprimirCabecalho("Resumo da qualidade", `Fotografia: ${caminho}`);
    escreverLinha("");
    escreverLinha(`- Status geral: ${fotografia.resumo?.statusGeral ?? "-"}`);
    escreverLinha(`- Indice de saude: ${fotografia.resumo?.indiceSaude ?? "-"}`);
    escreverLinha(`- Verificacoes: ${fotografia.resumo?.totais?.verificacoes ?? fotografia.verificacoes?.length ?? 0}`);
    escreverLinha(`- Suites lentas: ${(fotografia.confiabilidade?.suitesLentas ?? []).length}`);
    escreverLinha("");
    escreverLinha("Verificacoes:");
    for (const verificacao of fotografia.verificacoes ?? []) {
        escreverLinha(`- ${formatarVerificacao(verificacao)}`);
    }
    escreverLinha("");
    escreverLinha("Hotspots:");
    const pontosCriticos = (fotografia.hotspots ?? []).slice(0, limitePontosCriticos);
    if (pontosCriticos.length === 0) {
        escreverLinha("- Nenhum ponto critico calculado.");
        return;
    }

    for (const pontoCritico of pontosCriticos) {
        escreverLinha(`- ${pontoCritico.nome}: risco ${pontoCritico.risco}`);
    }
}

async function executarResumoQualidade(opcoes = {}) {
    const resolvido = await resolverFotografiaQualidade(opcoes.arquivo);
    const saida = {
        caminho: path.relative(process.cwd(), resolvido.caminho).replaceAll("\\", "/"),
        resumo: resolvido.fotografia.resumo,
        verificacoes: resolvido.fotografia.verificacoes ?? [],
        confiabilidade: resolvido.fotografia.confiabilidade ?? {},
        hotspots: resolvido.fotografia.hotspots ?? []
    };

    if (opcoes.json) {
        imprimirJson(saida);
    } else {
        imprimirHumano(saida.caminho, resolvido.fotografia, opcoes.limitePontosCriticos ?? 5);
    }

    return saida;
}

export {
    executarResumoQualidade
};
