import fs from "node:fs";
import path from "node:path";
import type {ClasseCobertura} from "../../lib/dominios/cobertura-java.js";

export const EXTENSAO_JAVA = ".java" as const;
export const CATEGORIAS_PRIORITARIAS = ["controladores", "fachadas", "servicos", "mapeadores"] as const;
export const CATEGORIAS_SECUNDARIAS = ["modelos", "repositorios", "dtos", "outros"] as const;
export const SUFIXOS_TESTE = ["Test", "CoverageTest", "UnitTest", "IntegrationTest"] as const;

type Categoria = typeof CATEGORIAS_PRIORITARIAS[number] | typeof CATEGORIAS_SECUNDARIAS[number];
type PerfilFonte = "comportamental" | "estruturalContrato" | "estruturalPuro";
type EstrategiaCorrespondencia = "mesmoPacote" | "nomeCorrespondenteOutroPacote" | "nenhum";
type EvidenciaQualidade =
    | "testeDedicado"
    | "ruidoDtoEstrutural"
    | "foraEscopoJacoco"
    | "coberturaIndireta"
    | "semEvidenciaNoEscopo";

interface ArquivoFonte {
    caminhoRelativo: string;
    nomeClasse: string;
    pacote: string;
    categoria: Categoria;
}

interface OpcoesClassificacaoFonte {
    nomeClasse: string;
    conteudoFonte: string;
}

interface OpcoesClassificacaoOutro extends OpcoesClassificacaoFonte {
    caminhoRelativo: string;
}

interface CoberturaRelatorio {
    nomeClasse: string;
    coberturaLinhasPercentual: number;
    linhasCobertas: number;
    linhasTotais: number;
    coberturaRamificacoesPercentual: number;
    ramificacoesCobertas: number;
    ramificacoesTotais: number;
}

interface ItemRelatorio {
    classe: string;
    caminhoRelativo: string;
    categoria: Categoria;
    perfilDto: PerfilFonte | null;
    ruidoDtoIgnorado: boolean;
    possuiTeste: boolean;
    estaNoEscopoJacoco: boolean;
    possuiCoberturaJacoco: boolean;
    cobertaSomenteIndiretamente: boolean;
    foraEscopoJacoco: boolean;
    evidenciaQualidade: EvidenciaQualidade;
    estrategiaCorrespondencia: EstrategiaCorrespondencia;
    testesEncontrados: string[];
    cobertura: CoberturaRelatorio | null;
}

interface OpcoesCriacaoItemRelatorio {
    arquivo: ArquivoFonte;
    perfilDto: PerfilFonte | null;
    dtoEstrutural: boolean;
    possuiTeste: boolean;
    estaNoEscopoJacoco: boolean;
    possuiCoberturaJacoco: boolean;
    possuiCoberturaSomenteIndireta: boolean;
    estaForaEscopoJacoco: boolean;
    estrategia: EstrategiaCorrespondencia;
    caminhos: string[];
    coberturaClasse: ClasseCobertura | null;
}

function normalizarCaminho(caminho: string): string {
    return caminho.replaceAll("\\", "/");
}

function inferirCategoria(nomeClasse: string, caminhoRelativo: string): Categoria {
    const caminhoNormalizado = normalizarCaminho(caminhoRelativo);

    if (nomeClasse.includes("Controller")) return "controladores";
    if (nomeClasse.includes("Service") || nomeClasse.includes("Policy")) return "servicos";
    if (nomeClasse.includes("Facade")) return "fachadas";
    if (nomeClasse.includes("Mapper")) return "mapeadores";
    if (
        caminhoNormalizado.includes("/dto/")
        || nomeClasse.includes("Dto")
        || nomeClasse.includes("Request")
        || nomeClasse.includes("Response")
        || nomeClasse.includes("Command")
    ) return "dtos";
    if (nomeClasse.includes("Repo")) return "repositorios";
    if (caminhoNormalizado.includes("/model/") || caminhoNormalizado.includes("/dominio/")) return "modelos";
    return "outros";
}

function lerConteudoFonte(backendSrc: string, caminhoRelativo: string): string {
    return fs.readFileSync(path.join(backendSrc, caminhoRelativo), "utf-8");
}

function classificarPerfilDto(conteudoFonte: string): PerfilFonte {
    const conteudoSemComentarios = conteudoFonte
        .replace(/\/\*[\s\S]*?\*\//g, "")
        .replace(/\/\/.*$/gm, "");

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!record\b|class\b|interface\b|enum\b)(static\s+)?[\w@.<>[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiLogica = /\b(for|if|switch|while)\s*\(|->|\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|\breturn\b/.test(conteudoSemComentarios);
    const possuiValidacaoOuContrato = /@(NotNull|NotBlank|NotEmpty|Size|Pattern|Email|Future|Past|Positive|Negative|SanitizarHtml|JsonProperty|JsonView|JsonFormat|JsonIgnoreProperties)\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito || possuiLogica) {
        return "comportamental";
    }

    if (possuiValidacaoOuContrato) {
        return "estruturalContrato";
    }

    return "estruturalPuro";
}

function classificarPerfilModelo({nomeClasse, conteudoFonte}: OpcoesClassificacaoFonte): PerfilFonte {
    const conteudoSemComentarios = conteudoFonte
        .replace(/\/\*[\s\S]*?\*\//g, "")
        .replace(/\/\/.*$/gm, "");

    if (
        nomeClasse.endsWith("Views")
        || nomeClasse.startsWith("Tipo")
        || nomeClasse.startsWith("Situacao")
        || nomeClasse === "Perfil"
        || nomeClasse.endsWith("Id")
    ) {
        return "estruturalPuro";
    }

    if (/\bpublic\s+@interface\b/.test(conteudoSemComentarios)) {
        return "estruturalContrato";
    }

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!class\b|interface\b|enum\b|record\b)(static\s+)?[\w@.<>[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiFluxoControle = /\b(for|if|switch|while)\s*\(/.test(conteudoSemComentarios);
    const possuiOperacaoColecao = /\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|removeIf\s*\(|anyMatch\s*\(/.test(conteudoSemComentarios);
    const possuiContratoExposto = /@JsonProperty\b|@JsonView\b/.test(conteudoSemComentarios);
    const possuiDominio = /\bthrow\b|\breturn\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito && (possuiFluxoControle || possuiOperacaoColecao || possuiContratoExposto || possuiDominio)) {
        return "comportamental";
    }

    if (possuiMetodoExplicito) {
        return "estruturalContrato";
    }

    return "estruturalPuro";
}

function classificarPerfilOutro({nomeClasse, caminhoRelativo, conteudoFonte}: OpcoesClassificacaoOutro): PerfilFonte {
    const caminhoNormalizado = normalizarCaminho(caminhoRelativo);
    const conteudoSemComentarios = conteudoFonte
        .replace(/\/\*[\s\S]*?\*\//g, "")
        .replace(/\/\/.*$/gm, "");

    if (
        nomeClasse === "Sgc"
        || nomeClasse === "Mensagens"
        || nomeClasse.endsWith("Properties")
        || nomeClasse.startsWith("Config")
        || nomeClasse.startsWith("Erro")
        || nomeClasse.endsWith("SecurityConfig")
        || caminhoNormalizado.includes("/config/")
        || caminhoNormalizado.includes("/erros/")
    ) {
        return "estruturalPuro";
    }

    if (/\bpublic\s+@interface\b/.test(conteudoSemComentarios)) {
        return "estruturalContrato";
    }

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!class\b|interface\b|enum\b|record\b)(static\s+)?[\w@.<>[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiFluxoControle = /\b(for|if|switch|while)\s*\(/.test(conteudoSemComentarios);
    const possuiOperacaoColecao = /\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|removeIf\s*\(|anyMatch\s*\(/.test(conteudoSemComentarios);
    const possuiDominio = /\bthrow\b|\breturn\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito && (possuiFluxoControle || possuiOperacaoColecao || possuiDominio)) {
        return "comportamental";
    }

    if (possuiMetodoExplicito) {
        return "estruturalContrato";
    }

    return "estruturalPuro";
}

function construirNomeClasseCompleto(caminhoRelativo: string): string {
    return normalizarCaminho(caminhoRelativo).replace(/\.java$/i, "").replaceAll("/", ".");
}

function criarItemRelatorio({
    arquivo,
    perfilDto,
    dtoEstrutural,
    possuiTeste,
    estaNoEscopoJacoco,
    possuiCoberturaJacoco,
    possuiCoberturaSomenteIndireta,
    estaForaEscopoJacoco,
    estrategia,
    caminhos,
    coberturaClasse
}: OpcoesCriacaoItemRelatorio): ItemRelatorio {
    const evidenciaQualidade: EvidenciaQualidade = possuiTeste
        ? "testeDedicado"
        : (dtoEstrutural
            ? "ruidoDtoEstrutural"
            : (estaForaEscopoJacoco
                ? "foraEscopoJacoco"
                : (possuiCoberturaSomenteIndireta ? "coberturaIndireta" : "semEvidenciaNoEscopo")));

    return {
        classe: arquivo.nomeClasse,
        caminhoRelativo: arquivo.caminhoRelativo,
        categoria: arquivo.categoria,
        perfilDto: perfilDto,
        ruidoDtoIgnorado: dtoEstrutural,
        possuiTeste: possuiTeste,
        estaNoEscopoJacoco: estaNoEscopoJacoco,
        possuiCoberturaJacoco: possuiCoberturaJacoco,
        cobertaSomenteIndiretamente: possuiCoberturaSomenteIndireta,
        foraEscopoJacoco: estaForaEscopoJacoco,
        evidenciaQualidade: evidenciaQualidade,
        estrategiaCorrespondencia: estrategia,
        testesEncontrados: caminhos,
        cobertura: coberturaClasse ? {
            nomeClasse: coberturaClasse.nomeClasse,
            coberturaLinhasPercentual: Number(coberturaClasse.coberturaLinhas.toFixed(2)),
            linhasCobertas: coberturaClasse.linhasCobertas,
            linhasTotais: coberturaClasse.totalLinhas,
            coberturaRamificacoesPercentual: Number(coberturaClasse.coberturaRamificacoes.toFixed(2)),
            ramificacoesCobertas: coberturaClasse.ramificacoesCobertas,
            ramificacoesTotais: coberturaClasse.totalRamificacoes
        } : null
    };
}

export {
    normalizarCaminho,
    inferirCategoria,
    lerConteudoFonte,
    classificarPerfilDto,
    classificarPerfilModelo,
    classificarPerfilOutro,
    construirNomeClasseCompleto,
    criarItemRelatorio
};
