import pc from "picocolors";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {coletarIdentificadores, obterDiretorioBusca, type IdentificadorTeste, type ResultadoColetaIdentificadores} from "./identificadores-teste-lib.js";

const IDENTIFICADORES_COMPARTILHADOS_PERMITIDOS = new Set<string>([
    "subprocesso-header__txt-header-unidade",
]);

interface GrupoIdentificadoresDuplicados {
    valor: string;
    itens: IdentificadorTeste[];
}

interface ResultadoIdentificadoresDuplicados {
    diretorioBusca: string;
    totalIdentificadores: number;
    totalDuplicados: number;
    totalOcorrenciasDuplicadas: number;
    duplicados: GrupoIdentificadoresDuplicados[];
}

function encontrarDuplicados(identificadores: IdentificadorTeste[]): GrupoIdentificadoresDuplicados[] {
    const identificadoresPorValor = new Map<string, IdentificadorTeste[]>();
    for (const identificador of identificadores) {
        const grupo = identificadoresPorValor.get(identificador.valor) ?? [];
        grupo.push(identificador);
        identificadoresPorValor.set(identificador.valor, grupo);
    }

    return [...identificadoresPorValor.entries()]
        .filter(([valor]) => !IDENTIFICADORES_COMPARTILHADOS_PERMITIDOS.has(valor))
        .filter(([, itens]) => itens.length > 1)
        .toSorted(([, itensA], [, itensB]) => itensB.length - itensA.length)
        .map(([valor, itens]) => ({valor, itens}));
}

function criarResultado(coleta: ResultadoColetaIdentificadores): ResultadoIdentificadoresDuplicados {
    const duplicados = encontrarDuplicados(coleta.identificadores);
    return {
        diretorioBusca: coleta.diretorioBusca,
        totalIdentificadores: coleta.identificadores.length,
        totalDuplicados: duplicados.length,
        totalOcorrenciasDuplicadas: duplicados.reduce((total, item) => total + item.itens.length, 0),
        duplicados,
    };
}

function imprimirDuplicados(resultado: ResultadoIdentificadoresDuplicados): void {
    if (resultado.totalDuplicados === 0) {
        escreverLinha("Nenhum identificador de teste duplicado encontrado.");
        return;
    }

    escreverLinha("Identificadores de teste duplicados encontrados:");
    escreverLinha();
    resultado.duplicados.forEach(({valor, itens}) => {
        escreverLinha(`>> "${valor}" — ${itens.length} ocorrências`);
        itens.forEach((item) => {
            escreverLinha(`   └─ ${item.arquivo}${item.atributo !== "data-testid" ? ` (${item.atributo})` : ""}`);
        });
        escreverLinha();
    });

    escreverLinha(`Total de identificadores duplicados distintos: ${resultado.totalDuplicados}`);
    escreverLinha(`Total de ocorrencias duplicadas: ${resultado.totalOcorrenciasDuplicadas}`);
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "cliente identificadores-teste listar-duplicados",
            scriptDireto: "cliente/identificadores-teste-listar-duplicados.ts",
            descricao: "Lista identificadores de teste duplicados nos templates Vue.",
            opcoes: [
                "--json               Emite os duplicados em JSON.",
                "--base <diretorio>   Raiz do projeto para resolver codigoCliente.",
                "--diretorio <diretorio> Sobrescreve o diretório configurado de código.",
            ],
            exemplos: [
                "npx tsx toolkit/ferramentas.ts cliente identificadores-teste listar-duplicados",
                "npx tsx toolkit/ferramentas.ts cliente identificadores-teste listar-duplicados --diretorio /tmp/cliente",
            ],
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const resultado = criarResultado(await coletarIdentificadores(obterDiretorioBusca(argumentos, diretorioBase), diretorioBase));
    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        escreverLinha(`Buscando por identificadores de teste em: ${resultado.diretorioBusca}`);
        escreverLinha();
        imprimirDuplicados(resultado);
    }

    if (resultado.totalDuplicados > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao executar a auditoria de identificadores duplicados: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
