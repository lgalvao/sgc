/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-require-imports */
/**
 * analisar-mensagens.cjs
 *
 * Analisa o arquivo mensagens-extraidas.json (gerado por extrair-mensagens.cjs)
 * e produz um relatório Markdown com:
 *   - Duplicatas exatas entre fontes diferentes
 *   - Duplicatas com variações (pontuação, artigos)
 *   - Mensagens nos testes sem correspondência no código de produção
 *   - Mensagens de produção sem cobertura de teste
 *   - Inventário completo por categoria
 *
 * Uso:
 *   cd frontend
 *   node etc/scripts/extrair-mensagens.cjs   # gera mensagens-extraidas.json primeiro
 *   node etc/scripts/analisar-mensagens.cjs  # gera mensagens-analise.md na raiz
 */

'use strict';

const fs = require('fs');
const path = require('path');

const RAIZ = path.join(__dirname, '../../..');
const INPUT_FILE = path.join(RAIZ, 'mensagens-extraidas.json');
const OUTPUT_FILE = path.join(RAIZ, 'mensagens-analise.md');

// ── Utilitários ──────────────────────────────────────────────────────────────

/**
 * Normaliza uma string para comparação fuzzy:
 * - Converte para minúsculas, remove pontuação final, artigos iniciais e espaços extras.
 */
function normalizar(texto) {
    return texto
        .toLowerCase()
        .trim()
        .replace(/[.!?;,]+$/, '')
        .replace(/^(o |a |os |as |um |uma )/, '')
        .replace(/\s+/g, ' ');
}

/**
 * Agrupa mensagens por texto normalizado e retorna apenas os grupos com duplicatas.
 */
function encontrarDuplicatas(mensagens) {
    const grupos = new Map();
    for (const msg of mensagens) {
        const chave = normalizar(msg.texto);
        if (!grupos.has(chave)) grupos.set(chave, []);
        grupos.get(chave).push(msg);
    }
    return [...grupos.values()].filter(grupo => grupo.length > 1);
}

/**
 * Agrupa mensagens por texto exato.
 */
function agruparPorTexto(mensagens) {
    const grupos = new Map();
    for (const msg of mensagens) {
        const chave = msg.texto;
        if (!grupos.has(chave)) grupos.set(chave, []);
        grupos.get(chave).push(msg);
    }
    return grupos;
}

/**
 * Escapa caracteres especiais de Markdown.
 */
function escaparMd(texto) {
    return texto.replace(/\|/g, '\\|').replace(/`/g, "'");
}

// ── Funções de Análise ────────────────────────────────────────────────────────

/**
 * Detecta strings presentes nos testes mas ausentes no código de produção.
 */
function detectarOrfaosEmTestes(dados) {
    const prodBackend = new Set([
        ...dados.backend.validacao_dto.map(m => normalizar(m.texto)),
        ...dados.backend.excecao_negocio.map(m => normalizar(m.texto)),
    ]);
    const prodFrontend = new Set([
        ...dados.frontend.toast_sucesso.map(m => normalizar(m.texto)),
        ...dados.frontend.notificacao_frontend.map(m => normalizar(m.texto)),
        ...dados.frontend.constante_frontend.map(m => normalizar(m.texto)),
    ]);

    return {
        orfaosBackend: dados.testes.teste_backend.filter(
            m => !prodBackend.has(normalizar(m.texto))
        ),
        orfaosE2e: dados.testes.assertiva_e2e.filter(
            m => !prodBackend.has(normalizar(m.texto)) && !prodFrontend.has(normalizar(m.texto))
        ),
    };
}

/**
 * Detecta strings de produção que não aparecem em nenhum teste.
 */
function detectarSemTeste(dados) {
    const todasTestes = new Set([
        ...dados.testes.teste_backend.map(m => normalizar(m.texto)),
        ...dados.testes.assertiva_e2e.map(m => normalizar(m.texto)),
        ...dados.testes.toast_e2e.map(m => normalizar(m.texto)),
    ]);

    return {
        backendSemTeste: [
            ...dados.backend.validacao_dto,
            ...dados.backend.excecao_negocio,
        ].filter(m => !todasTestes.has(normalizar(m.texto))),
        frontendSemTeste: dados.frontend.toast_sucesso.filter(
            m => !todasTestes.has(normalizar(m.texto))
        ),
    };
}

// ── Gerador de Relatório Markdown ─────────────────────────────────────────────

function gerarRelatorio(dados) {
    const linhas = [];
    const ts = new Date(dados.meta.geradoEm).toLocaleString('pt-BR', { timeZone: 'UTC' });

    linhas.push('# Relatório de Análise de Mensagens — SGC');
    linhas.push('');
    linhas.push(`> Gerado em: ${ts} UTC | Fonte: \`mensagens-extraidas.json\``);
    linhas.push('');

    // ── Sumário ──
    const totalProd = dados.backend.validacao_dto.length
        + dados.backend.excecao_negocio.length
        + dados.frontend.toast_sucesso.length
        + dados.frontend.notificacao_frontend.length;
    const totalTestes = dados.testes.teste_backend.length
        + dados.testes.assertiva_e2e.length
        + dados.testes.toast_e2e.length;

    linhas.push('## 1. Sumário por Categoria');
    linhas.push('');
    linhas.push('| Categoria | Tipo | Quantidade |');
    linhas.push('|---|---|---:|');
    linhas.push(`| Backend | Validação DTO | ${dados.backend.validacao_dto.length} |`);
    linhas.push(`| Backend | Exceção de Negócio | ${dados.backend.excecao_negocio.length} |`);
    linhas.push(`| Frontend | Toast de Sucesso | ${dados.frontend.toast_sucesso.length} |`);
    linhas.push(`| Frontend | Notificação/Alerta | ${dados.frontend.notificacao_frontend.length} |`);
    linhas.push(`| Frontend | Constantes | ${dados.frontend.constante_frontend.length} |`);
    linhas.push(`| Testes | Asserções Backend | ${dados.testes.teste_backend.length} |`);
    linhas.push(`| Testes | Asserções E2E | ${dados.testes.assertiva_e2e.length} |`);
    linhas.push(`| Testes | Toast em Testes Unitários | ${dados.testes.toast_e2e.length} |`);
    linhas.push(`| **Total** | | **${totalProd + totalTestes + dados.frontend.constante_frontend.length}** |`);
    linhas.push('');

    // ── Duplicatas Exatas ──
    linhas.push('## 2. Duplicatas Exatas (mesmo texto em fontes diferentes)');
    linhas.push('');

    const todasMensagens = [
        ...dados.backend.validacao_dto,
        ...dados.backend.excecao_negocio,
        ...dados.frontend.toast_sucesso,
        ...dados.frontend.notificacao_frontend,
        ...dados.testes.teste_backend,
        ...dados.testes.assertiva_e2e,
    ];

    const agrupadas = agruparPorTexto(todasMensagens);
    const duplicatasExatas = [...agrupadas.entries()]
        .filter(([, grupo]) => grupo.length > 1)
        .sort((a, b) => b[1].length - a[1].length);

    if (duplicatasExatas.length === 0) {
        linhas.push('✅ Nenhuma duplicata exata encontrada entre fontes diferentes.');
    } else {
        linhas.push(`Encontradas **${duplicatasExatas.length}** mensagens com texto idêntico em múltiplas fontes:`);
        linhas.push('');
        linhas.push('| Texto | Ocorrências | Fontes |');
        linhas.push('|---|---:|---|');
        for (const [texto, grupo] of duplicatasExatas.slice(0, 30)) {
            const fontes = [...new Set(grupo.map(m => m.tipo))].join(', ');
            const textoTruncado = texto.length > 60 ? texto.substring(0, 60) + '…' : texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | ${grupo.length} | ${fontes} |`);
        }
        if (duplicatasExatas.length > 30) {
            linhas.push(`| *(+${duplicatasExatas.length - 30} entradas adicionais)* | | |`);
        }
    }
    linhas.push('');

    // ── Duplicatas com Variações ──
    linhas.push('## 3. Duplicatas com Variações (pontuação, artigos, capitalização)');
    linhas.push('');

    const todasProd = [
        ...dados.backend.validacao_dto,
        ...dados.backend.excecao_negocio,
        ...dados.frontend.toast_sucesso,
    ];

    const duplicatasVariadas = encontrarDuplicatas(todasProd)
        .filter(grupo => new Set(grupo.map(m => m.texto)).size > 1);

    if (duplicatasVariadas.length === 0) {
        linhas.push('✅ Nenhuma duplicata com variação encontrada no código de produção.');
    } else {
        linhas.push(`Encontradas **${duplicatasVariadas.length}** mensagens com texto similar mas não idêntico:`);
        linhas.push('');
        linhas.push('> ⚠️ Estas mensagens provavelmente deveriam ser a mesma constante.');
        linhas.push('');
        for (const grupo of duplicatasVariadas) {
            linhas.push(`**Variações de:** \`${normalizar(grupo[0].texto)}\``);
            linhas.push('');
            linhas.push('| Texto Exato | Arquivo | Linha | Tipo |');
            linhas.push('|---|---|---:|---|');
            for (const msg of grupo) {
                linhas.push(`| \`${escaparMd(msg.texto)}\` | \`${msg.arquivo}\` | ${msg.linha} | ${msg.tipo} |`);
            }
            linhas.push('');
        }
    }

    // ── Órfãos em Testes ──
    linhas.push('## 4. Mensagens nos Testes sem Correspondência na Produção');
    linhas.push('');
    linhas.push('> Estas strings aparecem em testes mas não foram encontradas no código de produção.');
    linhas.push('> Podem ser mensagens obsoletas, de fixtures de teste, ou textos de UI não capturados.');
    linhas.push('');

    const { orfaosBackend, orfaosE2e } = detectarOrfaosEmTestes(dados);

    if (orfaosBackend.length > 0) {
        linhas.push(`### 4.1 Testes Backend (${orfaosBackend.length} ocorrências)`);
        linhas.push('');
        linhas.push('| Texto | Arquivo | Linha |');
        linhas.push('|---|---|---:|');
        for (const msg of orfaosBackend.slice(0, 20)) {
            const textoTruncado = msg.texto.length > 70 ? msg.texto.substring(0, 70) + '…' : msg.texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | \`${msg.arquivo}\` | ${msg.linha} |`);
        }
        if (orfaosBackend.length > 20) linhas.push(`| *(+${orfaosBackend.length - 20} mais)* | | |`);
        linhas.push('');
    }

    if (orfaosE2e.length > 0) {
        linhas.push(`### 4.2 Testes E2E (${orfaosE2e.length} ocorrências)`);
        linhas.push('');
        linhas.push('| Texto | Arquivo | Linha |');
        linhas.push('|---|---|---:|');
        for (const msg of orfaosE2e.slice(0, 30)) {
            const textoTruncado = msg.texto.length > 70 ? msg.texto.substring(0, 70) + '…' : msg.texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | \`${msg.arquivo}\` | ${msg.linha} |`);
        }
        if (orfaosE2e.length > 30) linhas.push(`| *(+${orfaosE2e.length - 30} mais)* | | |`);
        linhas.push('');
    }

    if (orfaosBackend.length === 0 && orfaosE2e.length === 0) {
        linhas.push('✅ Nenhuma mensagem de teste sem correspondência encontrada.');
        linhas.push('');
    }

    // ── Mensagens de Produção sem Testes ──
    linhas.push('## 5. Mensagens de Produção sem Cobertura de Teste');
    linhas.push('');
    linhas.push('> Estas strings existem no código de produção mas não aparecem em nenhum teste.');
    linhas.push('');

    const { backendSemTeste, frontendSemTeste } = detectarSemTeste(dados);

    if (backendSemTeste.length > 0) {
        linhas.push(`### 5.1 Backend sem teste (${backendSemTeste.length} ocorrências)`);
        linhas.push('');
        linhas.push('| Texto | Arquivo | Linha | Tipo |');
        linhas.push('|---|---|---:|---|');
        for (const msg of backendSemTeste.slice(0, 30)) {
            const textoTruncado = msg.texto.length > 60 ? msg.texto.substring(0, 60) + '…' : msg.texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | \`${msg.arquivo}\` | ${msg.linha} | ${msg.tipo} |`);
        }
        if (backendSemTeste.length > 30) linhas.push(`| *(+${backendSemTeste.length - 30} mais)* | | | |`);
        linhas.push('');
    }

    if (frontendSemTeste.length > 0) {
        linhas.push(`### 5.2 Toast (frontend) sem teste (${frontendSemTeste.length} ocorrências)`);
        linhas.push('');
        linhas.push('| Texto | Arquivo | Linha |');
        linhas.push('|---|---|---:|');
        for (const msg of frontendSemTeste) {
            linhas.push(`| \`${escaparMd(msg.texto)}\` | \`${msg.arquivo}\` | ${msg.linha} |`);
        }
        linhas.push('');
    }

    if (backendSemTeste.length === 0 && frontendSemTeste.length === 0) {
        linhas.push('✅ Todas as mensagens de produção possuem cobertura de teste.');
        linhas.push('');
    }

    // ── Inventário Completo ──
    linhas.push('## 6. Inventário Completo de Mensagens de Produção');
    linhas.push('');

    const secoes = [
        {
            titulo: '6.1 Validação de DTOs (Backend)',
            itens: dados.backend.validacao_dto,
            colunas: ['texto', 'anotacao', 'arquivo', 'linha'],
        },
        {
            titulo: '6.2 Exceções de Negócio (Backend)',
            itens: dados.backend.excecao_negocio,
            colunas: ['texto', 'classe', 'arquivo', 'linha'],
        },
        {
            titulo: '6.3 Mensagens de Sucesso / Toast (Frontend)',
            itens: dados.frontend.toast_sucesso,
            colunas: ['texto', 'arquivo', 'linha'],
        },
        {
            titulo: '6.4 Notificações (Frontend)',
            itens: dados.frontend.notificacao_frontend,
            colunas: ['texto', 'arquivo', 'linha'],
        },
    ];

    for (const secao of secoes) {
        linhas.push(`### ${secao.titulo} (${secao.itens.length})`);
        linhas.push('');

        if (secao.itens.length === 0) {
            linhas.push('*Nenhuma mensagem encontrada.*');
            linhas.push('');
            continue;
        }

        const { colunas } = secao;
        linhas.push('| ' + colunas.join(' | ') + ' |');
        linhas.push('| ' + colunas.map((c, i) => i === colunas.length - 1 ? '---:' : '---').join(' | ') + ' |');

        for (const msg of secao.itens) {
            const valores = colunas.map(c => {
                const v = String(msg[c] || '');
                if (c === 'texto') {
                    const truncado = v.length > 60 ? v.substring(0, 60) + '…' : v;
                    return `\`${escaparMd(truncado)}\``;
                }
                return escaparMd(v);
            });
            linhas.push('| ' + valores.join(' | ') + ' |');
        }
        linhas.push('');
    }

    return linhas.join('\n');
}

// ── Função Principal ──────────────────────────────────────────────────────────

function analisar() {
    if (!fs.existsSync(INPUT_FILE)) {
        console.error(`❌ Arquivo não encontrado: ${INPUT_FILE}`);
        console.error('Execute primeiro: node etc/scripts/extrair-mensagens.cjs');
        process.exit(1);
    }

    console.log('📊 Analisando mensagens extraídas...\n');

    const dados = JSON.parse(fs.readFileSync(INPUT_FILE, 'utf-8'));
    const relatorio = gerarRelatorio(dados);
    fs.writeFileSync(OUTPUT_FILE, relatorio, 'utf-8');

    console.log(`✅ Relatório gerado: ${path.relative(process.cwd(), OUTPUT_FILE)}`);

    // Resumo no console
    const todasProd = [
        ...dados.backend.validacao_dto,
        ...dados.backend.excecao_negocio,
        ...dados.frontend.toast_sucesso,
    ];
    const duplicatasVariadas = encontrarDuplicatas(todasProd)
        .filter(grupo => new Set(grupo.map(m => m.texto)).size > 1);

    const { orfaosBackend, orfaosE2e } = detectarOrfaosEmTestes(dados);
    const { backendSemTeste, frontendSemTeste } = detectarSemTeste(dados);

    console.log('\n📋 Resumo da análise:');
    console.log(`   Duplicatas com variações (produção): ${duplicatasVariadas.length}`);
    console.log(`   Testes backend sem correspondência:  ${orfaosBackend.length}`);
    console.log(`   Testes E2E sem correspondência:      ${orfaosE2e.length}`);
    console.log(`   Mensagens backend sem cobertura:     ${backendSemTeste.length}`);
    console.log(`   Toast frontend sem cobertura:        ${frontendSemTeste.length}`);
}

analisar();
