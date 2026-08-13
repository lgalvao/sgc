import path from "node:path";
import {resolverNaRaiz} from "../biblioteca/caminhos.js";
import {resolverFotografiaQualidade} from "../biblioteca/qualidade.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {VERSAO_SCHEMA_FOTOGRAFIA} from "./coleta-fotografia.js";

interface VerificacaoQualidade {
    codigo: string;
    status: string;
    sumario?: string;
}

interface FotografiaResumo {
    versaoSchema: typeof VERSAO_SCHEMA_FOTOGRAFIA;
    resumo: {
        statusGeral: string;
        totais?: {verificacoes?: number};
    };
    verificacoes: VerificacaoQualidade[];
    pontosCriticos: Array<{nome: string; risco: number}>;
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
    pontosCriticos: NonNullable<FotografiaResumo["pontosCriticos"]>;
}

function formatarVerificacao(verificacao: VerificacaoQualidade): string {
    return `${verificacao.codigo} [${verificacao.status}] ${verificacao.sumario || ""}`.trim();
}

function ehRegistro(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null;
}

function validarFotografia(valor: unknown, caminho: string): asserts valor is FotografiaResumo {
    if (!ehRegistro(valor) || valor.versaoSchema !== VERSAO_SCHEMA_FOTOGRAFIA) {
        throw new Error(`Fotografia de qualidade possui versao ausente ou incompativel: esperado ${VERSAO_SCHEMA_FOTOGRAFIA}.`);
    }

    const resumo = valor.resumo;
    const totais = ehRegistro(resumo) ? resumo.totais : undefined;
    const verificacoes = valor.verificacoes;
    const pontosCriticos = valor.pontosCriticos;
    const resumoValido = ehRegistro(resumo)
        && typeof resumo.statusGeral === "string"
        && (totais === undefined
            || (ehRegistro(totais)
                && (totais.verificacoes === undefined
                    || (typeof totais.verificacoes === "number" && Number.isFinite(totais.verificacoes) && totais.verificacoes >= 0))));
    const verificacoesValidas = Array.isArray(verificacoes)
        && verificacoes.every((item) => ehRegistro(item)
            && typeof item.codigo === "string"
            && typeof item.status === "string"
            && (item.sumario === undefined || typeof item.sumario === "string"));
    const pontosCriticosValidos = Array.isArray(pontosCriticos)
        && pontosCriticos.every((item) => ehRegistro(item)
            && typeof item.nome === "string"
            && typeof item.risco === "number"
            && Number.isFinite(item.risco));

    if (!resumoValido || !verificacoesValidas || !pontosCriticosValidos) {
        throw new Error(`Fotografia de qualidade possui estrutura invalida em ${caminho}.`);
    }
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
    escreverLinha("Pontos criticos:");
    const pontosCriticosLimitados = (fotografia.pontosCriticos ?? []).slice(0, limitePontosCriticos);
    if (pontosCriticosLimitados.length === 0) {
        escreverLinha("- Nenhum ponto critico calculado.");
        return;
    }

    for (const pontoCritico of pontosCriticosLimitados) {
        escreverLinha(`- ${pontoCritico.nome}: risco ${pontoCritico.risco}`);
    }
}

async function executarResumoQualidade(opcoes: OpcoesResumo = {}): Promise<ResultadoResumo> {
    const diretorioBase = path.resolve(opcoes.base ?? resolverNaRaiz());
    const resolvido = await resolverFotografiaQualidade<FotografiaResumo>(opcoes.arquivo ?? null, diretorioBase);
    const fotografia = resolvido.fotografia;
    validarFotografia(fotografia, resolvido.caminho);
    const saida: ResultadoResumo = {
        versaoSchema: fotografia.versaoSchema,
        caminho: path.relative(diretorioBase, resolvido.caminho).replaceAll("\\", "/"),
        resumo: fotografia.resumo,
        verificacoes: fotografia.verificacoes,
        pontosCriticos: fotografia.pontosCriticos
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
