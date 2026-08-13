import path from "node:path";
import {resolverFotografiaQualidade} from "../lib/qualidade.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

interface VerificacaoQualidade {
    codigo: string;
    status: string;
    sumario?: string;
}

interface FotografiaResumo {
    resumo?: {
        statusGeral?: string;
        indiceSaude?: number | string;
        totais?: {verificacoes?: number};
    };
    verificacoes?: VerificacaoQualidade[];
    confiabilidade?: {suitesLentas?: unknown[]};
    hotspots?: Array<{nome: string; risco: number}>;
}

interface OpcoesResumo {
    arquivo?: string;
    base?: string;
    json?: boolean;
    limitePontosCriticos?: number;
}

interface ResultadoResumo {
    caminho: string;
    resumo: FotografiaResumo["resumo"];
    verificacoes: VerificacaoQualidade[];
    confiabilidade: NonNullable<FotografiaResumo["confiabilidade"]>;
    hotspots: NonNullable<FotografiaResumo["hotspots"]>;
}

function formatarVerificacao(verificacao: VerificacaoQualidade): string {
    return `${verificacao.codigo} [${verificacao.status}] ${verificacao.sumario || ""}`.trim();
}

function imprimirHumano(caminho: string, fotografia: FotografiaResumo, limitePontosCriticos: number): void {
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

async function executarResumoQualidade(opcoes: OpcoesResumo = {}): Promise<ResultadoResumo> {
    const resolvido = await resolverFotografiaQualidade<FotografiaResumo>(opcoes.arquivo ?? null, opcoes.base);
    const fotografia = resolvido.fotografia;
    const saida: ResultadoResumo = {
        caminho: path.relative(process.cwd(), resolvido.caminho).replaceAll("\\", "/"),
        resumo: fotografia.resumo,
        verificacoes: fotografia.verificacoes ?? [],
        confiabilidade: fotografia.confiabilidade ?? {},
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
