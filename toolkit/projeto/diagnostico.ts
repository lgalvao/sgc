import path from "node:path";
import net from "node:net";
import {existsSync} from "node:fs";
import which from "which";
import {access} from "node:fs/promises";
import {execa} from "execa";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado, type NomeDiretorioConfigurado} from "../lib/configuracao.js";
import {escreverLinha, formatarStatus, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const CATEGORIAS = {
    AMBIENTE: "Ambiente e Ferramentas",
    CONFIGURACAO: "Configuração do Projeto",
    INFRA: "Infraestrutura e Serviços",
    DEPENDENCIAS: "Dependências"
} as const;

type Categoria = typeof CATEGORIAS[keyof typeof CATEGORIAS];
type Status = "ok" | "alerta" | "falha";

interface RecursoBase {
    nome: string;
    obrigatorio?: boolean;
    categoria: Categoria;
}

interface RecursoComando extends RecursoBase {
    tipo: "comando";
    versaoMin?: string;
    versaoEsperada?: string;
}

interface RecursoArquivo extends RecursoBase {
    tipo: "arquivo" | "diretorio";
    caminho: string;
}

interface RecursoPorta extends RecursoBase {
    tipo: "porta";
    porta: number;
    portaPadrao?: boolean;
}

type Recurso = RecursoComando | RecursoArquivo | RecursoPorta;

type RecursoVerificado = Recurso & {
    status: Status;
    detalhe: string;
};

interface TotaisDiagnostico {
    ok: number;
    alerta: number;
    falha: number;
}

interface ResultadoConsolidado {
    statusGeral: Status;
    totais: TotaisDiagnostico;
}

interface OpcoesDiagnostico {
    base?: string;
    silencioso?: boolean;
    json?: boolean;
    recursos?: Recurso[];
    comandosRegistrados?: RecursoArquivo[];
}

interface ResultadoDiagnostico extends ResultadoConsolidado {
    diretorioBase: string;
    verificacoes: RecursoVerificado[];
}

const RECURSOS_COMANDOS_PADRAO: RecursoComando[] = [
    {tipo: "comando", nome: "node", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE, versaoMin: "26.7.0"},
    {tipo: "comando", nome: "npm", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE},
    {tipo: "comando", nome: "git", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE},
    {tipo: "comando", nome: "java", obrigatorio: true, categoria: CATEGORIAS.AMBIENTE, versaoEsperada: "25"},
    {tipo: "comando", nome: "keytool", obrigatorio: false, categoria: CATEGORIAS.AMBIENTE}
];

const COMANDOS_REGISTRADOS_PADRAO: RecursoArquivo[] = [
    {
        tipo: "arquivo",
        nome: "toolkit",
        caminho: "toolkit/sgc.ts",
        obrigatorio: true,
        categoria: CATEGORIAS.CONFIGURACAO
    },
    {
        tipo: "arquivo",
        nome: "toolkit/package.json",
        caminho: "toolkit/package.json",
        obrigatorio: true,
        categoria: CATEGORIAS.CONFIGURACAO
    }
];

function caminhoRelativo(diretorioBase: string, caminhoAbsoluto: string): string {
    const caminho = path.relative(diretorioBase, caminhoAbsoluto).replaceAll("\\", "/");
    return caminho || ".";
}

function caminhoDiretorioConfigurado(diretorioBase: string, nomeDiretorio: NomeDiretorioConfigurado): string {
    return caminhoRelativo(diretorioBase, resolverCaminhoConfigurado(nomeDiretorio, diretorioBase));
}

function caminhoDentroDiretorio(diretorio: string, nome: string): string {
    return path.posix.join(diretorio, nome);
}

function ehPerfilSgc(diretorioBase: string): boolean {
    return existsSync(path.join(diretorioBase, "toolkit", "sgc.ts"));
}

function obterRecursosPadrao(diretorioBase: string): Recurso[] {
    const diretorioFrontend = caminhoDiretorioConfigurado(diretorioBase, "frontend");
    const diretorioBackend = caminhoDiretorioConfigurado(diretorioBase, "backend");
    const diretorioTestesIntegracao = caminhoDiretorioConfigurado(diretorioBase, "testesIntegracao");
    const perfilSgc = ehPerfilSgc(diretorioBase);
    const recursos: Recurso[] = [
        ...RECURSOS_COMANDOS_PADRAO,
        {tipo: "arquivo", nome: "gradlew", caminho: "gradlew", obrigatorio: true, categoria: CATEGORIAS.CONFIGURACAO},
        {tipo: "arquivo", nome: "package.json raiz", caminho: "package.json", obrigatorio: true, categoria: CATEGORIAS.CONFIGURACAO},
        {
            tipo: "arquivo",
            nome: caminhoDentroDiretorio(diretorioFrontend, "package.json"),
            caminho: caminhoDentroDiretorio(diretorioFrontend, "package.json"),
            obrigatorio: true,
            categoria: CATEGORIAS.CONFIGURACAO
        },
        ...(perfilSgc ? [{
            tipo: "arquivo" as const,
            nome: "toolkit/package.json",
            caminho: "toolkit/package.json",
            obrigatorio: true,
            categoria: CATEGORIAS.CONFIGURACAO
        }] : []),
        {
            tipo: "arquivo",
            nome: caminhoDentroDiretorio(diretorioTestesIntegracao, "package.json"),
            caminho: caminhoDentroDiretorio(diretorioTestesIntegracao, "package.json"),
            obrigatorio: true,
            categoria: CATEGORIAS.CONFIGURACAO
        },
        {
            tipo: "arquivo",
            nome: caminhoDentroDiretorio(diretorioBackend, "build.gradle.kts"),
            caminho: caminhoDentroDiretorio(diretorioBackend, "build.gradle.kts"),
            obrigatorio: true,
            categoria: CATEGORIAS.CONFIGURACAO
        },
        ...(perfilSgc ? [
            {tipo: "arquivo" as const, nome: ".env.e2e", caminho: ".env.e2e", obrigatorio: false, categoria: CATEGORIAS.CONFIGURACAO},
            {tipo: "porta" as const, nome: "Backend (10000)", porta: 10000, obrigatorio: false, categoria: CATEGORIAS.INFRA},
            {tipo: "porta" as const, nome: "Frontend (5173)", porta: 5173, portaPadrao: true, categoria: CATEGORIAS.INFRA}
        ] : []),
        {tipo: "diretorio", nome: "node_modules raiz", caminho: "node_modules", obrigatorio: false, categoria: CATEGORIAS.DEPENDENCIAS},
        {
            tipo: "diretorio",
            nome: caminhoDentroDiretorio(diretorioFrontend, "node_modules"),
            caminho: caminhoDentroDiretorio(diretorioFrontend, "node_modules"),
            obrigatorio: false,
            categoria: CATEGORIAS.DEPENDENCIAS
        },
        ...(perfilSgc ? [{
            tipo: "diretorio" as const,
            nome: "toolkit/node_modules",
            caminho: "toolkit/node_modules",
            obrigatorio: false,
            categoria: CATEGORIAS.DEPENDENCIAS
        }] : []),
        {
            tipo: "diretorio",
            nome: caminhoDentroDiretorio(diretorioTestesIntegracao, "node_modules"),
            caminho: caminhoDentroDiretorio(diretorioTestesIntegracao, "node_modules"),
            obrigatorio: false,
            categoria: CATEGORIAS.DEPENDENCIAS
        }
    ];
    return recursos;
}

function determinarStatus(sucesso: boolean, obrigatorio: boolean | undefined): Status {
    if (sucesso) return "ok";
    return obrigatorio ? "falha" : "alerta";
}

async function verificarPorta(porta: number): Promise<boolean> {
    return new Promise((resolve) => {
        const server = net.createServer();
        server.once("error", () => resolve(false));
        server.once("listening", () => {
            server.close();
            resolve(true);
        });
        server.listen(porta, "127.0.0.1");
    });
}

async function obterVersao(comando: string): Promise<string | null> {
    try {
        const {stdout} = await execa(comando, ["--version"]);
        return stdout.trim();
    } catch {
        try {
            const {stdout} = await execa(comando, ["-v"]);
            return stdout.trim();
        } catch {
            return null;
        }
    }
}

async function verificarComando(recurso: RecursoComando): Promise<RecursoVerificado> {
    const encontrado = await which(recurso.nome, {nothrow: true});
    if (!encontrado) {
        return {
            ...recurso,
            status: determinarStatus(false, recurso.obrigatorio),
            detalhe: "nao encontrado"
        };
    }

    let detalhe = encontrado;
    let status: Status = "ok";

    if (recurso.versaoEsperada || recurso.versaoMin) {
        const versao = await obterVersao(recurso.nome);
        if (versao) {
            detalhe += ` (versao ${versao})`;
            if (recurso.versaoEsperada && !versao.includes(recurso.versaoEsperada)) {
                status = "alerta";
                detalhe += ` - esperado ${recurso.versaoEsperada}`;
            }
            if (recurso.versaoMin && !atendeVersaoMinima(versao, recurso.versaoMin)) {
                status = "falha";
                detalhe += ` - minimo ${recurso.versaoMin}`;
            }
        }
    }

    return {...recurso, status, detalhe};
}

function extrairVersaoNumerica(texto: string): number[] | null {
    const encontrada = texto.match(/\d+(?:\.\d+)+/);
    return encontrada ? encontrada[0].split(".").map(Number) : null;
}

function atendeVersaoMinima(textoVersao: string, textoMinimo: string): boolean {
    const versao = extrairVersaoNumerica(textoVersao);
    const minimo = extrairVersaoNumerica(textoMinimo);
    if (!versao || !minimo) {
        return false;
    }

    const tamanho = Math.max(versao.length, minimo.length);
    for (let indice = 0; indice < tamanho; indice += 1) {
        const parteVersao = versao[indice] ?? 0;
        const parteMinima = minimo[indice] ?? 0;
        if (parteVersao !== parteMinima) {
            return parteVersao > parteMinima;
        }
    }
    return true;
}

async function caminhoExiste(caminho: string): Promise<boolean> {
    try {
        await access(caminho);
        return true;
    } catch {
        return false;
    }
}

async function verificarComandosRegistrados(
    diretorioBase: string,
    comandos?: RecursoArquivo[]
): Promise<RecursoVerificado[]> {
    const comandosEfetivos = comandos ?? (ehPerfilSgc(diretorioBase) ? COMANDOS_REGISTRADOS_PADRAO : []);
    return Promise.all(comandosEfetivos.map(async (recurso) => ({
        ...recurso,
        status: await caminhoExiste(path.resolve(diretorioBase, recurso.caminho)) ? "ok" : "falha",
        detalhe: recurso.caminho
    })));
}

async function verificarRecurso(recurso: Recurso, diretorioBase: string): Promise<RecursoVerificado> {
    if (recurso.tipo === "comando") {
        return verificarComando(recurso);
    }

    if (recurso.tipo === "porta") {
        const livre = await verificarPorta(recurso.porta);
        return {
            ...recurso,
            status: determinarStatus(livre, recurso.obrigatorio),
            detalhe: livre ? "livre" : "ocupada"
        };
    }

    const caminhoAbsoluto = path.resolve(diretorioBase, recurso.caminho);
    let existe = await caminhoExiste(caminhoAbsoluto);
    let detalhe = existe ? caminhoAbsoluto : `${recurso.caminho} ausente`;

    if (!existe && recurso.tipo === "diretorio" && recurso.caminho.endsWith("node_modules") && recurso.caminho !== "node_modules") {
        const raizNodeModules = path.resolve(diretorioBase, "node_modules");
        if (await caminhoExiste(raizNodeModules)) {
            existe = true;
            detalhe = "gerenciado por workspaces na raiz";
        }
    }

    return {
        ...recurso,
        status: determinarStatus(existe, recurso.obrigatorio),
        detalhe
    };
}

function calcularStatusGeral(totais: TotaisDiagnostico): Status {
    if (totais.falha > 0) return "falha";
    if (totais.alerta > 0) return "alerta";
    return "ok";
}

function consolidar(resultado: RecursoVerificado[]): ResultadoConsolidado {
    const totais: TotaisDiagnostico = {
        ok: resultado.filter((item) => item.status === "ok").length,
        alerta: resultado.filter((item) => item.status === "alerta").length,
        falha: resultado.filter((item) => item.status === "falha").length
    };

    return {
        statusGeral: calcularStatusGeral(totais),
        totais
    };
}

function imprimirHumano(resultado: RecursoVerificado[], consolidado: ResultadoConsolidado): void {
    imprimirCabecalho("Diagnostico do projeto", "Valida comandos, arquivos e infraestrutura do sistema.");
    escreverLinha("");

    const porCategoria: Record<string, RecursoVerificado[]> = {};
    for (const item of resultado) {
        if (!porCategoria[item.categoria]) porCategoria[item.categoria] = [];
        porCategoria[item.categoria].push(item);
    }

    for (const [categoria, itens] of Object.entries(porCategoria)) {
        escreverLinha(pc.bold(categoria));
        for (const item of itens) {
            escreverLinha(`  - ${item.nome}: ${formatarStatus(item.status)} (${item.detalhe})`);
        }
        escreverLinha("");
    }

    escreverLinha(`Status geral: ${formatarStatus(consolidado.statusGeral)}`);
    escreverLinha(`Totais: ${consolidado.totais.ok} ok, ${consolidado.totais.alerta} alertas, ${consolidado.totais.falha} falhas`);
}

async function executarDiagnostico(opcoes: OpcoesDiagnostico = {}): Promise<ResultadoDiagnostico> {
    const diretorioBase = opcoes.base ? path.resolve(opcoes.base) : resolverNaRaiz();
    const verificacoes = [
        ...(await Promise.all((opcoes.recursos ?? obterRecursosPadrao(diretorioBase)).map((recurso) => verificarRecurso(recurso, diretorioBase)))),
        ...(await verificarComandosRegistrados(diretorioBase, opcoes.comandosRegistrados))
    ];
    const consolidado = consolidar(verificacoes);
    const saida = {
        diretorioBase,
        ...consolidado,
        verificacoes
    };

    if (!opcoes.silencioso) {
        if (opcoes.json) {
            imprimirJson(saida);
        } else {
            imprimirHumano(verificacoes, consolidado);
        }
    }

    return saida;
}

export {
    executarDiagnostico,
    obterRecursosPadrao,
    type OpcoesDiagnostico,
    type Recurso,
    type RecursoArquivo,
    type ResultadoDiagnostico
};
