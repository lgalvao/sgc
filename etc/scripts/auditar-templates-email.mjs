#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';

const raiz = process.cwd();
const dirTemplates = path.join(raiz, 'backend/src/main/resources/templates/email');
const arquivoSubprocesso = path.join(raiz, 'backend/src/main/java/sgc/subprocesso/service/SubprocessoNotificacaoService.java');
const arquivoTipoTransicao = path.join(raiz, 'backend/src/main/java/sgc/subprocesso/model/TipoTransicao.java');
const arquivoEmailModelos = path.join(raiz, 'backend/src/main/java/sgc/alerta/EmailModelosService.java');
const arquivoProcesso = path.join(raiz, 'backend/src/main/java/sgc/processo/service/ProcessoService.java');
const arquivoAtribuicao = path.join(raiz, 'backend/src/main/java/sgc/organizacao/service/ResponsavelUnidadeService.java');

const textoSubprocesso = lerArquivo(arquivoSubprocesso);
const textoTipoTransicao = lerArquivo(arquivoTipoTransicao);
const textoEmailModelos = lerArquivo(arquivoEmailModelos);
const textoProcesso = lerArquivo(arquivoProcesso);
const textoAtribuicao = lerArquivo(arquivoAtribuicao);

const variaveisDiretas = extrairVariaveisPut(textoSubprocesso, 'criarVariaveisTemplateDireto', [
    'siglaUnidade',
    'nomeUnidade',
    'siglaUnidadeOrigem',
    'nomeUnidadeOrigem',
    'siglaUnidadeDestino',
    'nomeUnidadeDestino',
    'siglaUnidadeSuperior',
    'nomeUnidadeSuperior',
    'nomeProcesso',
    'tipoProcesso',
    'dataLimiteEtapa1',
    'dataLimiteEtapa2',
    'dataLimiteValidacao',
    'observacoes'
]);
const variaveisConsolidacao = extrairVariaveisPut(textoSubprocesso, 'criarVariaveisConsolidacao', [
    'siglaUnidadeSuperior',
    'nomeProcesso',
    'siglasUnidades',
    'dataLimiteValidacao'
]);
const variaveisEmailInicio = extrairVariaveisSetVariable(textoEmailModelos, 'criarEmailInicioProcessoConsolidado');
const variaveisEmailFinalizacaoDireta = extrairVariaveisSetVariable(textoEmailModelos, 'criarEmailProcessoFinalizadoPorUnidade');
const variaveisEmailFinalizacaoConsolidada = extrairVariaveisSetVariable(textoEmailModelos, 'criarEmailProcessoFinalizadoUnidadesSubordinadas');
const variaveisEmailLembrete = extrairVariaveisSetVariable(textoEmailModelos, 'criarEmailLembretePrazo');
const variaveisEmailAtribuicao = extrairVariaveisSetVariable(textoEmailModelos, 'criarEmailAtribuicaoTemporaria');

const assuntosPorTransicao = extrairAssuntosPorTransicao(textoSubprocesso);
const templatesPorTransicao = extrairTemplatesPorTransicao(textoTipoTransicao);

const mapeamentoEspecial = {
    'cadastro-aceito-bloco-unidade': {
        origem: 'SubprocessoNotificacaoService.criarNotificacaoDiretaAceiteCadastroBloco',
        variaveis: variaveisDiretas,
        assunto: 'criarAssunto(TipoTransicao.CADASTRO_ACEITO, sp, false)'
    },
    'cadastro-aceito-bloco-superior': {
        origem: 'SubprocessoNotificacaoService.criarNotificacaoConsolidadaAceiteCadastroBloco',
        variaveis: variaveisConsolidacao,
        assunto: 'SGC: Cadastros de atividades e conhecimentos submetidos para análise'
    },
    'validacao-mapa-aceita-bloco-unidade': {
        origem: 'SubprocessoNotificacaoService.criarNotificacaoDiretaAceiteValidacaoBloco',
        variaveis: variaveisDiretas,
        assunto: 'SGC: Validação do mapa de competências da [SIGLA] submetida para análise'
    },
    'validacao-mapa-aceita-bloco-superior': {
        origem: 'SubprocessoNotificacaoService.criarNotificacaoConsolidadaAceiteValidacaoBloco',
        variaveis: variaveisConsolidacao,
        assunto: 'SGC: Validação de mapas de competências submetida para análise'
    },
    'mapa-disponibilizado-bloco-superior': {
        origem: 'SubprocessoNotificacaoService.criarNotificacaoConsolidadaDisponibilizacaoMapaBloco',
        variaveis: variaveisConsolidacao,
        assunto: 'SGC: Mapas de competências disponibilizados'
    },
    'mapa-homologado': {
        origem: 'SubprocessoNotificacaoService.criarNotificacaoHomologacao',
        variaveis: variaveisDiretas,
        assunto: 'criarAssunto(TipoTransicao.MAPA_HOMOLOGADO, sp, false)'
    },
    'data-limite-alterada': {
        origem: 'SubprocessoNotificacaoService.notificarAlteracaoDataLimite',
        variaveis: ['titulo', 'siglaUnidade', 'nomeProcesso', 'novaData', 'etapa'],
        assunto: 'SGC: Data limite alterada'
    },
    'email-inicio-processo-consolidado': {
        origem: 'EmailModelosService.criarEmailInicioProcessoConsolidado',
        variaveis: variaveisEmailInicio,
        assunto: 'EmailModelosService.criarAssuntoInicioProcesso(tipoProcesso, isParticipante)'
    },
    'processo-finalizado-por-unidade': {
        origem: 'EmailModelosService.criarEmailProcessoFinalizadoPorUnidade',
        variaveis: variaveisEmailFinalizacaoDireta,
        assunto: 'SGC: Finalização do processo [NOME_PROCESSO]'
    },
    'processo-finalizado-unidades-subordinadas': {
        origem: 'EmailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas',
        variaveis: variaveisEmailFinalizacaoConsolidada,
        assunto: 'SGC: Finalização do processo [NOME_PROCESSO] em unidades subordinadas'
    },
    'lembrete-prazo': {
        origem: 'EmailModelosService.criarEmailLembretePrazo',
        variaveis: variaveisEmailLembrete,
        assunto: 'SGC: Lembrete de prazo - [NOME_PROCESSO]'
    },
    'atribuicao-temporaria': {
        origem: 'EmailModelosService.criarEmailAtribuicaoTemporaria',
        variaveis: variaveisEmailAtribuicao,
        assunto: 'ResponsavelUnidadeService.criarNotificacoesAtribuicaoTemporaria'
    }
};

const arquivosTemplates = fs.readdirSync(dirTemplates)
    .filter(nome => nome.endsWith('.html') && nome !== '_layout.html')
    .sort((a, b) => a.localeCompare(b, 'pt-BR'));

const resultados = arquivosTemplates.map(nomeArquivo => analisarTemplate(nomeArquivo.replace(/\.html$/, '')));
const somenteSuspeitas = process.argv.includes('--suspeitas');
const formatoJson = process.argv.includes('--json');
const nomeTemplate = argumentoOpcional('--template');
const resultadoFiltrado = resultados
    .filter(item => !nomeTemplate || item.template === nomeTemplate)
    .filter(item => !somenteSuspeitas || item.suspeitas.length > 0);

if (formatoJson) {
    console.log(JSON.stringify(resultadoFiltrado, null, 2));
    process.exit(0);
}

for (const item of resultadoFiltrado) {
    console.log(`${item.template}`);
    console.log(`  origem: ${item.origem}`);
    console.log(`  assunto: ${item.assunto}`);
    console.log(`  variaveis-template: ${item.variaveisTemplate.join(', ') || '-'}`);
    console.log(`  variaveis-fornecidas: ${item.variaveisFornecidas.join(', ') || '-'}`);
    console.log(`  faltando: ${item.variaveisFaltantes.join(', ') || '-'}`);
    console.log(`  sem-uso: ${item.variaveisSemUso.join(', ') || '-'}`);
    console.log(`  sensiveis: ${item.variaveisSensiveis.join(', ') || '-'}`);
    console.log(`  suspeitas: ${item.suspeitas.join('; ') || 'nenhuma heuristica obvia'}`);
}

function analisarTemplate(template) {
    const caminho = path.join(dirTemplates, `${template}.html`);
    const conteudo = lerArquivo(caminho);
    const variaveisTemplate = extrairVariaveisTemplate(conteudo);
    const metadados = obterMetadadosTemplate(template);
    const variaveisFornecidas = [...metadados.variaveis].sort();
    const variaveisFaltantes = variaveisTemplate.filter(variavel => !metadados.variaveis.has(variavel));
    const variaveisSemUso = variaveisFornecidas.filter(variavel => !variaveisTemplate.includes(variavel));
    const variaveisSensiveis = variaveisTemplate.filter(variavel =>
        ['siglaUnidade', 'siglaUnidadeOrigem', 'siglaUnidadeDestino', 'siglaUnidadeSuperior', 'nomeServidor'].includes(variavel)
    );
    const suspeitas = [];

    if (variaveisFaltantes.length > 0) {
        suspeitas.push(`template usa variaveis sem origem clara: ${variaveisFaltantes.join(', ')}`);
    }

    if (/assunto desconhecido/i.test(metadados.assunto)) {
        suspeitas.push('assunto nao mapeado');
    }

    if (template.includes('superior') && !variaveisTemplate.includes('siglaUnidadeSuperior')) {
        suspeitas.push('template de superior nao usa siglaUnidadeSuperior');
    }

    if (template.includes('bloco-superior') && !variaveisTemplate.includes('siglasUnidades')) {
        suspeitas.push('template consolidado nao lista siglasUnidades');
    }

    if (template === 'processo-iniciado') {
        suspeitas.push('template aparenta legado: o fluxo atual de inicio usa email-inicio-processo-consolidado');
    }

    return {
        template,
        origem: metadados.origem,
        assunto: metadados.assunto,
        variaveisTemplate,
        variaveisFornecidas,
        variaveisFaltantes,
        variaveisSemUso,
        variaveisSensiveis,
        suspeitas
    };
}

function obterMetadadosTemplate(template) {
    if (mapeamentoEspecial[template]) {
        return {
            origem: mapeamentoEspecial[template].origem,
            assunto: mapeamentoEspecial[template].assunto,
            variaveis: new Set(mapeamentoEspecial[template].variaveis)
        };
    }

    const transicao = Object.entries(templatesPorTransicao).find(([, valor]) =>
        valor.templateEmail === template || valor.templateEmailSuperior === template
    );
    if (transicao) {
        const [tipo, valor] = transicao;
        const paraSuperior = valor.templateEmailSuperior === template;
        return {
            origem: `TipoTransicao.${tipo} (${paraSuperior ? 'superior' : 'direto'})`,
            assunto: descreverAssuntoTransicao(tipo, paraSuperior),
            variaveis: new Set(paraSuperior ? variaveisDiretas : variaveisDiretas)
        };
    }

    return {
        origem: 'origem desconhecida',
        assunto: 'assunto desconhecido',
        variaveis: new Set()
    };
}

function descreverAssuntoTransicao(tipo, paraSuperior) {
    const assuntoBase = assuntosPorTransicao.get(tipo);
    if (!assuntoBase) {
        return `criarAssunto(TipoTransicao.${tipo}, ..., ${paraSuperior})`;
    }
    return paraSuperior
        ? `SGC: ${assuntoBase} - [SIGLA]`
        : `SGC: ${assuntoBase}`;
}

function extrairVariaveisTemplate(conteudo) {
    const expressoes = [...conteudo.matchAll(/\$\{([^}]+)}/g)].map(match => match[1]);
    const palavrasReservadas = new Set([
        'true', 'false', 'null', 'and', 'or', 'not', 'eq', 'ne', 'lt', 'gt', 'le', 'ge', 'instanceof'
    ]);
    const variaveis = new Set();

    for (const expressaoBruta of expressoes) {
        const expressao = expressaoBruta.replace(/'[^']*'/g, ' ');
        const regex = /\b[a-zA-Z_][a-zA-Z0-9_]*\b/g;
        let match;
        while ((match = regex.exec(expressao)) !== null) {
            const palavra = match[0];
            if (palavrasReservadas.has(palavra)) continue;

            const indice = match.index;
            const anterior = expressao[indice - 1];
            const proximo = expressao[indice + palavra.length];
            if (anterior === '#' || anterior === '.') continue;
            if (proximo === '(') continue;
            variaveis.add(palavra);
        }
    }

    return [...variaveis].sort();
}

function extrairVariaveisPut(textoClasse, nomeMetodo, fallback = []) {
    const corpo = extrairCorpoMetodo(textoClasse, nomeMetodo);
    const variaveis = [...new Set([...corpo.matchAll(/variaveis\.put\("([^"]+)"/g)].map(match => match[1]))].sort();
    return variaveis.length > 0 ? variaveis : fallback;
}

function extrairVariaveisSetVariable(textoClasse, nomeMetodo) {
    const corpo = extrairCorpoMetodo(textoClasse, nomeMetodo);
    const diretas = [...corpo.matchAll(/setVariable\("([^"]+)"/g)].map(match => match[1]);
    const porConstante = [...corpo.matchAll(/setVariable\(\s*(VAR_[A-Z_]+)\b/g)]
        .map(match => resolverConstanteVariavel(textoClasse, match[1]))
        .filter(Boolean);
    return [...new Set([...diretas, ...porConstante])].sort();
}

function resolverConstanteVariavel(textoClasse, nomeConstante) {
    const regex = new RegExp(`private static final String ${nomeConstante} = "([^"]+)";`);
    return textoClasse.match(regex)?.[1] ?? null;
}

function extrairAssuntosPorTransicao(textoClasse) {
    const blocoSwitch = textoClasse.match(/switch \(tipo\) \{([\s\S]*?)\n\s*};/);
    const mapa = new Map();
    if (!blocoSwitch) return mapa;

    const regexCase = /case ([A-Z_]+) -> "([^"]+)"(?:\s*\.formatted\(sp\.getUnidade\(\)\.getSigla\(\)\))?/g;
    let match;
    while ((match = regexCase.exec(blocoSwitch[1])) !== null) {
        const tipo = match[1];
        const texto = match[2].replace(/%s/g, '[SIGLA]');
        mapa.set(tipo, texto);
    }
    return mapa;
}

function extrairTemplatesPorTransicao(textoClasse) {
    const mapa = {};
    const regex = /([A-Z_]+)\(([\s\S]*?)\n\s*\),/g;
    let match;
    while ((match = regex.exec(textoClasse)) !== null) {
        const valores = [...match[2].matchAll(/(null|"[^"]+")/g)].map(item => item[1]);
        if (valores.length < 2) continue;
        mapa[match[1]] = {
            templateAlerta: valorOuNulo(valores.at(-3) ?? 'null'),
            templateEmail: valorOuNulo(valores.at(-2) ?? 'null'),
            templateEmailSuperior: valorOuNulo(valores.at(-1) ?? 'null')
        };
    }
    return mapa;
}

function valorOuNulo(valor) {
    if (valor === 'null') return null;
    return valor.replace(/^"/, '').replace(/"$/, '');
}

function extrairCorpoMetodo(textoClasse, nomeMetodo) {
    const regexAssinatura = new RegExp(`(?:private|public|protected)\\s+[^{;=]+\\b${nomeMetodo}\\s*\\(`);
    const assinatura = regexAssinatura.exec(textoClasse);
    const indiceMetodo = assinatura?.index ?? -1;
    if (indiceMetodo < 0) return '';
    const indiceAbertura = textoClasse.indexOf('{', indiceMetodo);
    if (indiceAbertura < 0) return '';

    let profundidade = 0;
    for (let i = indiceAbertura; i < textoClasse.length; i++) {
        const char = textoClasse[i];
        if (char === '{') profundidade++;
        if (char === '}') profundidade--;
        if (profundidade === 0) {
            return textoClasse.slice(indiceAbertura + 1, i);
        }
    }
    return '';
}

function argumentoOpcional(flag) {
    const indice = process.argv.indexOf(flag);
    if (indice < 0) return null;
    return process.argv[indice + 1] ?? null;
}

function lerArquivo(caminho) {
    return fs.readFileSync(caminho, 'utf8');
}
