import fs from "node:fs";
import path from "node:path";
import type {ClasseCobertura} from "../../lib/dominios/cobertura-java.js";

export const EXTENSAO_JAVA = ".java" as const;
export const CATEGORIAS_PRIORITARIAS = ["Controllers", "Facades", "Services", "Mappers"] as const;
export const CATEGORIAS_SECUNDARIAS = ["Models", "Repositories", "DTOs", "Others"] as const;
export const SUFIXOS_TESTE = ["Test", "CoverageTest", "UnitTest", "IntegrationTest"] as const;

type Categoria = typeof CATEGORIAS_PRIORITARIAS[number] | typeof CATEGORIAS_SECUNDARIAS[number];
type PerfilFonte = "comportamental" | "estrutural_contrato" | "estrutural_puro";
type EstrategiaCorrespondencia = "mesmo_pacote" | "nome_correspondente_outro_pacote" | "nenhum";
type EvidenciaQualidade =
    | "teste_dedicado"
    | "ruido_dto_estrutural"
    | "fora_escopo_jacoco"
    | "cobertura_indireta"
    | "sem_evidencia_no_escopo";

interface ArquivoFonte {
    caminho_relativo: string;
    nome_classe: string;
    pacote: string;
    categoria: Categoria;
}

interface OpcoesClassificacaoFonte {
    nomeClasse: string;
    conteudoFonte: string;
}

interface OpcoesClassificacaoOther extends OpcoesClassificacaoFonte {
    caminhoRelativo: string;
}

interface CoberturaRelatorio {
    nome_classe: string;
    cobertura_linhas_percentual: number;
    linhas_cobertas: number;
    linhas_total: number;
    cobertura_ramificacoes_percentual: number;
    ramificacoes_cobertas: number;
    ramificacoes_total: number;
}

interface ItemRelatorio {
    classe: string;
    caminho_relativo: string;
    categoria: Categoria;
    perfil_dto: PerfilFonte | null;
    dto_ruido_ignorado: boolean;
    possui_teste: boolean;
    esta_no_escopo_jacoco: boolean;
    possui_cobertura_jacoco: boolean;
    coberta_somente_indiretamente: boolean;
    fora_escopo_jacoco: boolean;
    evidencia_qualidade: EvidenciaQualidade;
    estrategia_correspondencia: EstrategiaCorrespondencia;
    testes_encontrados: string[];
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

    if (nomeClasse.includes("Controller")) return "Controllers";
    if (nomeClasse.includes("Service") || nomeClasse.includes("Policy")) return "Services";
    if (nomeClasse.includes("Facade")) return "Facades";
    if (nomeClasse.includes("Mapper")) return "Mappers";
    if (
        caminhoNormalizado.includes("/dto/")
        || nomeClasse.includes("Dto")
        || nomeClasse.includes("Request")
        || nomeClasse.includes("Response")
        || nomeClasse.includes("Command")
    ) return "DTOs";
    if (nomeClasse.includes("Repo")) return "Repositories";
    if (caminhoNormalizado.includes("/model/") || caminhoNormalizado.includes("/dominio/")) return "Models";
    return "Others";
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
        return "estrutural_contrato";
    }

    return "estrutural_puro";
}

function classificarPerfilModel({nomeClasse, conteudoFonte}: OpcoesClassificacaoFonte): PerfilFonte {
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
        return "estrutural_puro";
    }

    if (/\bpublic\s+@interface\b/.test(conteudoSemComentarios)) {
        return "estrutural_contrato";
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
        return "estrutural_contrato";
    }

    return "estrutural_puro";
}

function classificarPerfilOther({nomeClasse, caminhoRelativo, conteudoFonte}: OpcoesClassificacaoOther): PerfilFonte {
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
        return "estrutural_puro";
    }

    if (/\bpublic\s+@interface\b/.test(conteudoSemComentarios)) {
        return "estrutural_contrato";
    }

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!class\b|interface\b|enum\b|record\b)(static\s+)?[\w@.<>[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiFluxoControle = /\b(for|if|switch|while)\s*\(/.test(conteudoSemComentarios);
    const possuiOperacaoColecao = /\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|removeIf\s*\(|anyMatch\s*\(/.test(conteudoSemComentarios);
    const possuiDominio = /\bthrow\b|\breturn\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito && (possuiFluxoControle || possuiOperacaoColecao || possuiDominio)) {
        return "comportamental";
    }

    if (possuiMetodoExplicito) {
        return "estrutural_contrato";
    }

    return "estrutural_puro";
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
        ? "teste_dedicado"
        : (dtoEstrutural
            ? "ruido_dto_estrutural"
            : (estaForaEscopoJacoco
                ? "fora_escopo_jacoco"
                : (possuiCoberturaSomenteIndireta ? "cobertura_indireta" : "sem_evidencia_no_escopo")));

    return {
        classe: arquivo.nome_classe,
        caminho_relativo: arquivo.caminho_relativo,
        categoria: arquivo.categoria,
        perfil_dto: perfilDto,
        dto_ruido_ignorado: dtoEstrutural,
        possui_teste: possuiTeste,
        esta_no_escopo_jacoco: estaNoEscopoJacoco,
        possui_cobertura_jacoco: possuiCoberturaJacoco,
        coberta_somente_indiretamente: possuiCoberturaSomenteIndireta,
        fora_escopo_jacoco: estaForaEscopoJacoco,
        evidencia_qualidade: evidenciaQualidade,
        estrategia_correspondencia: estrategia,
        testes_encontrados: caminhos,
        cobertura: coberturaClasse ? {
            nome_classe: coberturaClasse.nomeClasse,
            cobertura_linhas_percentual: Number(coberturaClasse.coberturaLinhas.toFixed(2)),
            linhas_cobertas: coberturaClasse.linhasCobertas,
            linhas_total: coberturaClasse.totalLinhas,
            cobertura_ramificacoes_percentual: Number(coberturaClasse.coberturaRamificacoes.toFixed(2)),
            ramificacoes_cobertas: coberturaClasse.ramificacoesCobertas,
            ramificacoes_total: coberturaClasse.totalRamificacoes
        } : null
    };
}

export {
    normalizarCaminho,
    inferirCategoria,
    lerConteudoFonte,
    classificarPerfilDto,
    classificarPerfilModel,
    classificarPerfilOther,
    construirNomeClasseCompleto,
    criarItemRelatorio
};
