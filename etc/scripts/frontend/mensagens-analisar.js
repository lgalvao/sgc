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
 *   node etc/scripts/sgc.js frontend mensagens analisar [--fix]
 */

'use strict';

import fs from "node:fs";
import path from "node:path";
import pc from "picocolors";

const RAIZ = path.join(import.meta.dirname, '../../..');
const INPUT_FILE = path.join(RAIZ, 'mensagens-extraidas.json');
const OUTPUT_FILE = path.join(RAIZ, 'mensagens-analise.md');
const TEXTOS_TS = path.join(RAIZ, 'frontend/src/constants/textos.ts');

// ── Utilitários ──────────────────────────────────────────────────────────────

function normalizar(texto) {
    return texto
        .toLowerCase()
        .trim()
        .replace(/[.!?;,]+$/, '')
        .replace(/^(o |a |os |as |um |uma )/, '')
        .replaceAll(/\s+/g, ' ');
}

function encontrarDuplicatas(mensagens) {
    const grupos = new Map();
    for (const msg of mensagens) {
        const chave = normalizar(msg.texto);
        if (!grupos.has(chave)) grupos.set(chave, []);
        grupos.get(chave).push(msg);
    }
    return [...grupos.values()].filter(grupo => grupo.length > 1);
}

function agruparPorTexto(mensagens) {
    const grupos = new Map();
    for (const msg of mensagens) {
        const chave = msg.texto;
        if (!grupos.has(chave)) grupos.set(chave, []);
        grupos.get(chave).push(msg);
    }
    return grupos;
}

function escaparMd(texto) {
    return texto.replaceAll('|', String.raw`\|`).replaceAll('`', "'");
}

// ── Funções de Análise ────────────────────────────────────────────────────────

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

// ── Auto-Fix de Constantes Órfãs ──────────────────────────────────────────────

function fixOrphanConstants() {
    console.log(pc.cyan('🧹 Buscando constantes órfãs em textos.ts...'));

    if (!fs.existsSync(TEXTOS_TS)) {
        console.error(pc.red(`❌ Arquivo não encontrado: ${TEXTOS_TS}`));
        return;
    }

    const content = fs.readFileSync(TEXTOS_TS, 'utf-8');
    const lines = content.split(/\r?\n/);
    const keys = [];
    let currentCategory = null;

    // Passo 1: Extrair chaves
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const categoryMatch = line.match(/^\s{2}([a-z][a-zA-Z0-9]+):\s+\{/);
        if (categoryMatch) {
            currentCategory = categoryMatch[1];
            continue;
        }
        if (currentCategory) {
            const keyMatch = line.match(/^\s{4}([A-Z0-9_]+)[\s:(]/);
            if (keyMatch) {
                keys.push({
                    full: `${currentCategory}.${keyMatch[1]}`,
                    category: currentCategory,
                    key: keyMatch[1],
                    lineIndex: i
                });
            }
            if (line.match(/^\s{2}\},/)) currentCategory = null;
        }
    }

    console.log(`🔍 Verificando uso de ${pc.bold(keys.length)} constantes...`);
    const orphans = [];

    // Passo 2: Verificar uso via busca em arquivos (usando PowerShell no Windows)
    for (const item of keys) {
        const searchPattern = `TEXTOS.${item.full}`;
        try {
            // No Windows, usamos Select-String se disponível, ou uma busca simples via Node
            // Para ser robusto e rápido, vamos carregar todos os arquivos de interesse em memória uma vez
            // ou usar uma ferramenta de shell. Como estamos no Node, vamos usar uma busca manual rápida.
            if (!global.cacheArquivosFrontend) {
                const extensões = ['.ts', '.vue'];
                const pastas = [path.join(RAIZ, 'frontend/src'), path.join(RAIZ, 'e2e')];
                global.cacheArquivosFrontend = [];

                function carregar(dir) {
                    if (!fs.existsSync(dir)) return;
                    const entradas = fs.readdirSync(dir, {withFileTypes: true});
                    for (const e of entradas) {
                        const p = path.join(dir, e.name);
                        if (e.isDirectory()) carregar(p);
                        else if (extensões.includes(path.extname(p)) && !p.includes('textos.ts')) {
                            global.cacheArquivosFrontend.push(fs.readFileSync(p, 'utf-8'));
                        }
                    }
                }

                pastas.forEach(carregar);
            }

            const isUsed = global.cacheArquivosFrontend.some(c => c.includes(searchPattern));
            if (!isUsed) {
                orphans.push(item);
            }
        } catch (e) {
            console.error(`Erro ao verificar ${item.full}: ${e.message}`);
        }
    }

    if (orphans.length === 0) {
        console.log(pc.green('✅ Nenhuma constante órfã encontrada.'));
        return;
    }

    console.log(pc.yellow(`⚠️ Encontradas ${pc.bold(orphans.length)} constantes órfãs. Removendo...`));

    // Passo 3: Remover do arquivo (de trás para frente para manter índices)
    const newLines = [...lines];
    // Agrupar por índice para remover blocos se sobrarem vazios (opcional, vamos manter simples)
    orphans.sort((a, b) => b.lineIndex - a.lineIndex).forEach(orphan => {
        console.log(pc.dim(`   - Removendo ${orphan.full}`));
        newLines.splice(orphan.lineIndex, 1);
    });

    fs.writeFileSync(TEXTOS_TS, newLines.join('\n'), 'utf-8');
    console.log(pc.green('✅ Limpeza de textos.ts concluída.'));
}

// ── Gerador de Relatório Markdown ─────────────────────────────────────────────

function gerarRelatorio(dados) {
    const linhas = [];
    const ts = new Date(dados.meta.geradoEm).toLocaleString('pt-BR', {timeZone: 'UTC'});

    linhas.push('# Relatório de Análise de Mensagens — SGC', '', `> Gerado em: ${ts} UTC | Fonte: \`mensagens-extraidas.json\``, '');

    const totalProd = dados.backend.validacao_dto.length
        + dados.backend.excecao_negocio.length
        + dados.frontend.toast_sucesso.length
        + dados.frontend.notificacao_frontend.length;
    const totalTestes = dados.testes.teste_backend.length
        + dados.testes.assertiva_e2e.length
        + dados.testes.toast_e2e.length;

    linhas.push('## 1. Sumário por Categoria', '', '| Categoria | Tipo | Quantidade |', '|---|---|---:|', `| Backend | Validação DTO | ${dados.backend.validacao_dto.length} |`, `| Backend | Exceção de Negócio | ${dados.backend.excecao_negocio.length} |`, `| Frontend | Toast de Sucesso | ${dados.frontend.toast_sucesso.length} |`, `| Frontend | Notificação/Alerta | ${dados.frontend.notificacao_frontend.length} |`, `| Frontend | Constantes | ${dados.frontend.constante_frontend.length} |`, `| Testes | Asserções Backend | ${dados.testes.teste_backend.length} |`, `| Testes | Asserções E2E | ${dados.testes.assertiva_e2e.length} |`, `| Testes | Toast em Testes Unitários | ${dados.testes.toast_e2e.length} |`, `| **Total** | | **${totalProd + totalTestes + dados.frontend.constante_frontend.length}** |`, '', '## 2. Duplicatas Exatas (mesmo texto em fontes diferentes)', '');

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
        linhas.push(`Encontradas **${duplicatasExatas.length}** mensagens com texto idêntico em múltiplas fontes:`, '', '| Texto | Ocorrências | Fontes |', '|---|---:|---|');
        for (const [texto, grupo] of duplicatasExatas.slice(0, 30)) {
            const fontes = [...new Set(grupo.map(m => m.tipo))].join(', ');
            const textoTruncado = texto.length > 60 ? texto.substring(0, 60) + '…' : texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | ${grupo.length} | ${fontes} |`);
        }
        if (duplicatasExatas.length > 30) {
            linhas.push(`| *(+${duplicatasExatas.length - 30} entradas adicionais)* | | |`);
        }
    }
    linhas.push('', '## 3. Duplicatas com Variações (pontuação, artigos, capitalização)', '');

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
        linhas.push(`Encontradas **${duplicatasVariadas.length}** mensagens com texto similar mas não idêntico:`, '', '> ⚠️ Estas mensagens provavelmente deveriam ser a mesma constante.', '');
        for (const grupo of duplicatasVariadas) {
            linhas.push(`**Variações de:** \`${normalizar(grupo[0].texto)}\``, '| Texto Exato | Arquivo | Linha | Tipo |', '|---|---|---:|---|');
            for (const msg of grupo) {
                linhas.push(`| \`${escaparMd(msg.texto)}\` | \`${msg.arquivo}\` | ${msg.linha} | ${msg.tipo} |`);
            }
            linhas.push('');
        }
    }

    const {orfaosBackend, orfaosE2e} = detectarOrfaosEmTestes(dados);
    linhas.push('## 4. Mensagens nos Testes sem Correspondência na Produção', '', '> Estas strings aparecem em testes mas não foram encontradas no código de produção.', '');

    if (orfaosBackend.length > 0) {
        linhas.push(`### 4.1 Testes Backend (${orfaosBackend.length} ocorrências)`, '', '| Texto | Arquivo | Linha |', '|---|---|---:|');
        for (const msg of orfaosBackend.slice(0, 20)) {
            const textoTruncado = msg.texto.length > 70 ? msg.texto.substring(0, 70) + '…' : msg.texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | \`${msg.arquivo}\` | ${msg.linha} |`);
        }
        if (orfaosBackend.length > 20) linhas.push(`| *(+${orfaosBackend.length - 20} mais)* | | |`);
        linhas.push('');
    }

    if (orfaosE2e.length > 0) {
        linhas.push(`### 4.2 Testes E2E (${orfaosE2e.length} ocorrências)`, '', '| Texto | Arquivo | Linha |', '|---|---|---:|');
        for (const msg of orfaosE2e.slice(0, 30)) {
            const textoTruncado = msg.texto.length > 70 ? msg.texto.substring(0, 70) + '…' : msg.texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | \`${msg.arquivo}\` | ${msg.linha} |`);
        }
        if (orfaosE2e.length > 30) linhas.push(`| *(+${orfaosE2e.length - 30} mais)* | | |`);
        linhas.push('');
    }

    if (orfaosBackend.length === 0 && orfaosE2e.length === 0) {
        linhas.push('✅ Nenhuma mensagem de teste sem correspondência encontrada.', '');
    }

    const {backendSemTeste, frontendSemTeste} = detectarSemTeste(dados);
    linhas.push('## 5. Mensagens de Produção sem Cobertura de Teste', '', '> Estas strings existem no código de produção mas não aparecem em nenhum teste.', '');

    if (backendSemTeste.length > 0) {
        linhas.push(`### 5.1 Backend sem teste (${backendSemTeste.length} ocorrências)`, '', '| Texto | Arquivo | Linha | Tipo |', '|---|---|---:|---|');
        for (const msg of backendSemTeste.slice(0, 30)) {
            const textoTruncado = msg.texto.length > 60 ? msg.texto.substring(0, 60) + '…' : msg.texto;
            linhas.push(`| \`${escaparMd(textoTruncado)}\` | \`${msg.arquivo}\` | ${msg.linha} | ${msg.tipo} |`);
        }
        if (backendSemTeste.length > 30) linhas.push(`| *(+${backendSemTeste.length - 30} mais)* | | | |`);
        linhas.push('');
    }

    if (frontendSemTeste.length > 0) {
        linhas.push(`### 5.2 Toast (frontend) sem teste (${frontendSemTeste.length} ocorrências)`, '', '| Texto | Arquivo | Linha |', '|---|---|---:|');
        for (const msg of frontendSemTeste) {
            linhas.push(`| \`${escaparMd(msg.texto)}\` | \`${msg.arquivo}\` | ${msg.linha} |`);
        }
        linhas.push('');
    }

    if (backendSemTeste.length === 0 && frontendSemTeste.length === 0) {
        linhas.push('✅ Todas as mensagens de produção possuem cobertura de teste.', '');
    }

    linhas.push('## 6. Inventário Completo de Mensagens de Produção', '');
    const secoes = [
        {
            titulo: '6.1 Validação de DTOs (Backend)',
            itens: dados.backend.validacao_dto,
            colunas: ['texto', 'anotacao', 'arquivo', 'linha']
        },
        {
            titulo: '6.2 Exceções de Negócio (Backend)',
            itens: dados.backend.excecao_negocio,
            colunas: ['texto', 'classe', 'arquivo', 'linha']
        },
        {
            titulo: '6.3 Mensagens de Sucesso / Toast (Frontend)',
            itens: dados.frontend.toast_sucesso,
            colunas: ['texto', 'arquivo', 'linha']
        },
        {
            titulo: '6.4 Notificações (Frontend)',
            itens: dados.frontend.notificacao_frontend,
            colunas: ['texto', 'arquivo', 'linha']
        },
    ];

    for (const secao of secoes) {
        linhas.push(`### ${secao.titulo} (${secao.itens.length})`, '');
        if (secao.itens.length === 0) {
            linhas.push('*Nenhuma mensagem encontrada.*', '');
            continue;
        }
        const {colunas} = secao;
        linhas.push('| ' + colunas.join(' | ') + ' |', '| ' + colunas.map((c, i) => i === colunas.length - 1 ? '---:' : '---').join(' | ') + ' |');
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
    const args = process.argv.slice(2);
    const fixMode = args.includes('--fix');

    if (fixMode) {
        fixOrphanConstants();
    }

    if (!fs.existsSync(INPUT_FILE)) {
        console.error(pc.red(`❌ Arquivo não encontrado: ${INPUT_FILE}`));
        console.error('Execute primeiro: node etc/scripts/sgc.js frontend mensagens extrair');
        process.exit(1);
    }

    console.log(pc.cyan('📊 Analisando mensagens extraídas...\n'));
    const dados = JSON.parse(fs.readFileSync(INPUT_FILE, 'utf-8'));
    const relatorio = gerarRelatorio(dados);
    fs.writeFileSync(OUTPUT_FILE, relatorio, 'utf-8');

    console.log(pc.green(`✅ Relatório gerado: ${path.relative(process.cwd(), OUTPUT_FILE)}`));

    const todasProd = [
        ...dados.backend.validacao_dto,
        ...dados.backend.excecao_negocio,
        ...dados.frontend.toast_sucesso,
    ];
    const duplicatasVariadas = encontrarDuplicatas(todasProd)
        .filter(grupo => new Set(grupo.map(m => m.texto)).size > 1);

    const {orfaosBackend, orfaosE2e} = detectarOrfaosEmTestes(dados);
    const {backendSemTeste, frontendSemTeste} = detectarSemTeste(dados);

    console.log('\n📋 Resumo da análise:');
    console.log(`   Duplicatas com variações (produção): ${pc.bold(duplicatasVariadas.length)}`);
    console.log(`   Testes backend sem correspondência:  ${pc.bold(orfaosBackend.length)}`);
    console.log(`   Testes E2E sem correspondência:      ${pc.bold(orfaosE2e.length)}`);
    console.log(`   Mensagens backend sem cobertura:     ${pc.bold(backendSemTeste.length)}`);
    console.log(`   Toast frontend sem cobertura:        ${pc.bold(frontendSemTeste.length)}`);
}

analisar();
