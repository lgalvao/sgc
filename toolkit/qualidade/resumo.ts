import path from "node:path";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {resolverFotografiaQualidade} from "../lib/qualidade.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {VERSAO_SCHEMA_FOTOGRAFIA} from "./coleta-fotografia.js";

interface VerificacaoQualidade {
    codigo: string;
    status: string;
    sumario?: string;
}

interface FotografiaResumo {
    versaoSchema: typeof VERSAO_SCHEMA_FOTOGRAFIA;
    resumo?: {
        statusGeral?: string;
        totais?: {verificacoes?: number};
    };
    verificacoes?: VerificacaoQualidade[];
    hotspots?: Array<{nome: string; risco: number}>;
}

interface OpcoesResumo {
    arquivo?: string;
    base?: string;
    json?: boolean;
    limitePontosCriticos?: number;
}

interface ResultadoResumo {
    versaoSchema: typeof VERSAO_SCHEMA_FOTOGRAFIA;
    caminho: string;
    resumo: FotografiaResumo["resumo"];
    verificacoes: VerificacaoQualidade[];
    hotspots: NonNullable<FotografiaResumo["hotspots"]>;
}

function formatarVerificacao(verificacao: VerificacaoQualidade): string {
    return `${verificacao.codigo} [${verificacao.status}] ${verificacao.sumario || ""}`.trim();
}

function imprimirHumano(caminho: string, fotografia: FotografiaResumo, limitePontosCriticos: number): void {
    imprimirCabecalho("Resumo da qualidade", `Fotografia: ${caminho}`);
    escreverLinha("");
    escreverLinha(`- Status geral: ${fotografia.resumo?.statusGeral ?? "-"}`);
    escreverLinha(`- Verificacoes: ${fotografia.resumo?.totais?.verificacoes ?? fotografia.verificacoes?.length ?? 0}`);
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

async function executarResumoQualidade(opcoes: OpcoesResumo = {}): Promise<ResultadoResumo> {
    const diretorioBase = path.resolve(opcoes.base ?? resolverNaRaiz());
    const resolvido = await resolverFotografiaQualidade<FotografiaResumo>(opcoes.arquivo ?? null, diretorioBase);
    const fotografia = resolvido.fotografia;
    if (fotografia.versaoSchema !== VERSAO_SCHEMA_FOTOGRAFIA) {
        throw new Error(`Fotografia de qualidade possui versao ausente ou incompativel: esperado ${VERSAO_SCHEMA_FOTOGRAFIA}.`);
    }
    const saida: ResultadoResumo = {
        versaoSchema: fotografia.versaoSchema,
        caminho: path.relative(diretorioBase, resolvido.caminho).replaceAll("\\", "/"),
        resumo: fotografia.resumo,
        verificacoes: fotografia.verificacoes ?? [],
        hotspots: fotografia.hotspots ?? []
    };

    if (opcoes.json) {
        imprimirJson(saida);
    } else {
        imprimirHumano(saida.caminho, fotografia, opcoes.limitePontosCriticos ?? 5);
    }

    return saida;
}

export {
    executarResumoQualidade
};
