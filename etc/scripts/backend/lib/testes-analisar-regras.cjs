const path = require('node:path');

const EXTENSAO_JAVA = '.java';
const CATEGORIAS_PRIORITARIAS = ['Controllers', 'Facades', 'Services', 'Mappers'];
const CATEGORIAS_SECUNDARIAS = ['Models', 'Repositories', 'DTOs', 'Others'];
const SUFIXOS_TESTE = ['Test', 'CoverageTest', 'UnitTest', 'IntegrationTest'];

function normalizarCaminho(caminho) {
    return caminho.replaceAll('\\', '/');
}

function inferirCategoria(nomeClasse, caminhoRelativo) {
    const caminhoNormalizado = normalizarCaminho(caminhoRelativo);

    if (nomeClasse.includes('Controller')) return 'Controllers';
    if (nomeClasse.includes('Service') || nomeClasse.includes('Policy')) return 'Services';
    if (nomeClasse.includes('Facade')) return 'Facades';
    if (nomeClasse.includes('Mapper')) return 'Mappers';
    if (caminhoNormalizado.includes('/dto/') || nomeClasse.includes('Dto') || nomeClasse.includes('Request') || nomeClasse.includes('Response') || nomeClasse.includes('Command')) return 'DTOs';
    if (nomeClasse.includes('Repo')) return 'Repositories';
    if (caminhoNormalizado.includes('/model/') || caminhoNormalizado.includes('/dominio/')) return 'Models';
    return 'Others';
}

function lerConteudoFonte(backendSrc, caminhoRelativo, fs = require('node:fs')) {
    return fs.readFileSync(path.join(backendSrc, caminhoRelativo), 'utf-8');
}

function classificarPerfilDto(conteudoFonte) {
    const conteudoSemComentarios = conteudoFonte
        .replace(/\/\*[\s\S]*?\*\//g, '')
        .replace(/\/\/.*$/gm, '');

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!record\b|class\b|interface\b|enum\b)(static\s+)?[\w@.<>\[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiLogica = /\b(for|if|switch|while)\s*\(|->|\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|\breturn\b/.test(conteudoSemComentarios);
    const possuiValidacaoOuContrato = /@(NotNull|NotBlank|NotEmpty|Size|Pattern|Email|Future|Past|Positive|Negative|SanitizarHtml|JsonProperty|JsonView|JsonFormat|JsonIgnoreProperties)\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito || possuiLogica) {
        return 'comportamental';
    }

    if (possuiValidacaoOuContrato) {
        return 'estrutural_contrato';
    }

    return 'estrutural_puro';
}

function classificarPerfilModel({nomeClasse, caminhoRelativo, conteudoFonte}) {
    const caminhoNormalizado = normalizarCaminho(caminhoRelativo);
    const conteudoSemComentarios = conteudoFonte
        .replace(/\/\*[\s\S]*?\*\//g, '')
        .replace(/\/\/.*$/gm, '');

    if (
        nomeClasse.endsWith('Views')
        || nomeClasse.startsWith('Tipo')
        || nomeClasse.startsWith('Situacao')
        || nomeClasse === 'Perfil'
        || nomeClasse.endsWith('Id')
    ) {
        return 'estrutural_puro';
    }

    if (/\bpublic\s+@interface\b/.test(conteudoSemComentarios)) {
        return 'estrutural_contrato';
    }

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!class\b|interface\b|enum\b|record\b)(static\s+)?[\w@.<>\[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiFluxoControle = /\b(for|if|switch|while)\s*\(/.test(conteudoSemComentarios);
    const possuiOperacaoColecao = /\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|removeIf\s*\(|anyMatch\s*\(/.test(conteudoSemComentarios);
    const possuiContratoExposto = /@JsonProperty\b|@JsonView\b/.test(conteudoSemComentarios);
    const possuiDominio = /\bthrow\b|\breturn\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito && (possuiFluxoControle || possuiOperacaoColecao || possuiContratoExposto || possuiDominio)) {
        return 'comportamental';
    }

    if (possuiMetodoExplicito) {
        return 'estrutural_contrato';
    }

    return 'estrutural_puro';
}

function classificarPerfilOther({nomeClasse, caminhoRelativo, conteudoFonte}) {
    const caminhoNormalizado = normalizarCaminho(caminhoRelativo);
    const conteudoSemComentarios = conteudoFonte
        .replace(/\/\*[\s\S]*?\*\//g, '')
        .replace(/\/\/.*$/gm, '');

    if (
        nomeClasse === 'Sgc'
        || nomeClasse === 'Mensagens'
        || nomeClasse.endsWith('Properties')
        || nomeClasse.startsWith('Config')
        || nomeClasse.startsWith('Erro')
        || nomeClasse.endsWith('SecurityConfig')
        || caminhoNormalizado.includes('/config/')
        || caminhoNormalizado.includes('/erros/')
    ) {
        return 'estrutural_puro';
    }

    if (/\bpublic\s+@interface\b/.test(conteudoSemComentarios)) {
        return 'estrutural_contrato';
    }

    const possuiMetodoExplicito = /\b(public|private|protected)\s+(?!class\b|interface\b|enum\b|record\b)(static\s+)?[\w@.<>\[\]?]+\s+\w+\s*\(/.test(conteudoSemComentarios);
    const possuiFluxoControle = /\b(for|if|switch|while)\s*\(/.test(conteudoSemComentarios);
    const possuiOperacaoColecao = /\.stream\s*\(|\.map\s*\(|\.filter\s*\(|\.collect\s*\(|removeIf\s*\(|anyMatch\s*\(/.test(conteudoSemComentarios);
    const possuiDominio = /\bthrow\b|\breturn\b/.test(conteudoSemComentarios);

    if (possuiMetodoExplicito && (possuiFluxoControle || possuiOperacaoColecao || possuiDominio)) {
        return 'comportamental';
    }

    if (possuiMetodoExplicito) {
        return 'estrutural_contrato';
    }

    return 'estrutural_puro';
}

function construirNomeClasseCompleto(caminhoRelativo) {
    return normalizarCaminho(caminhoRelativo).replace(/\.java$/i, '').replaceAll('/', '.');
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
}) {
    const evidenciaQualidade = possuiTeste
        ? 'teste_dedicado'
        : (dtoEstrutural
            ? 'ruido_dto_estrutural'
            : (estaForaEscopoJacoco
                ? 'fora_escopo_jacoco'
                : (possuiCoberturaSomenteIndireta ? 'cobertura_indireta' : 'sem_evidencia_no_escopo')));

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
            cobertura_branches_percentual: Number(coberturaClasse.coberturaBranches.toFixed(2)),
            branches_cobertos: coberturaClasse.branchesCobertos,
            branches_total: coberturaClasse.totalBranches
        } : null
    };
}

module.exports = {
    EXTENSAO_JAVA,
    CATEGORIAS_PRIORITARIAS,
    CATEGORIAS_SECUNDARIAS,
    SUFIXOS_TESTE,
    normalizarCaminho,
    inferirCategoria,
    lerConteudoFonte,
    classificarPerfilDto,
    classificarPerfilModel,
    classificarPerfilOther,
    construirNomeClasseCompleto,
    criarItemRelatorio
};
