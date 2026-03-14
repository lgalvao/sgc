/* eslint-disable no-console */
/**
 * extrair-mensagens.cjs
 *
 * Extrai todas as mensagens/strings do SGC (backend, frontend e testes) e gera
 * um arquivo JSON estruturado para análise posterior.
 *
 * Uso:
 *   cd frontend
 *   node etc/scripts/extrair-mensagens.cjs
 *
 * Saída: mensagens-extraidas.json na raiz do projeto
 */

'use strict';

const fs = require('fs');
const path = require('path');

// ── Configuração de caminhos ────────────────────────────────────────────────
const RAIZ = path.join(__dirname, '../../..');
const BACKEND_SRC = path.join(RAIZ, 'backend/src/main/java/sgc');
const BACKEND_TEST = path.join(RAIZ, 'backend/src/test/java/sgc');
const FRONTEND_SRC = path.join(RAIZ, 'frontend/src');
const FRONTEND_TEST = path.join(RAIZ, 'frontend/src');
const E2E_DIR = path.join(RAIZ, 'e2e');
const OUTPUT_FILE = path.join(RAIZ, 'mensagens-extraidas.json');

// ── Utilitários ─────────────────────────────────────────────────────────────

/**
 * Retorna recursivamente todos os arquivos com a extensão dada em um diretório.
 */
function listarArquivos(dirPath, extensao) {
    const resultado = [];
    if (!fs.existsSync(dirPath)) return resultado;
    const entradas = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const entrada of entradas) {
        const fullPath = path.join(dirPath, entrada.name);
        if (entrada.isDirectory()) {
            resultado.push(...listarArquivos(fullPath, extensao));
        } else if (entrada.isFile() && fullPath.endsWith(extensao)) {
            resultado.push(fullPath);
        }
    }
    return resultado;
}

/**
 * Retorna o caminho relativo à raiz do projeto.
 */
function relativo(fullPath) {
    return path.relative(RAIZ, fullPath);
}

// ── Extratores de Backend (Java) ─────────────────────────────────────────────

/**
 * Extrai mensagens de anotações Jakarta Validation (@NotBlank, @NotNull, etc.)
 * dos arquivos Java de DTOs.
 */
function extrairMensagensValidacaoDto(arquivos) {
    const mensagens = [];
    const ANOTACOES = ['@NotBlank', '@NotNull', '@NotEmpty', '@Size', '@Future', '@Pattern',
        '@Min', '@Max', '@Email', '@Past', '@PastOrPresent', '@FutureOrPresent'];

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            const temAnotacao = ANOTACOES.some(a => linha.includes(a));
            if (!temAnotacao) continue;

            // Extrai o valor de message = "..."
            // Pode estar na mesma linha ou nas próximas
            let bloco = linha;
            // Captura até o fechamento da anotação se multiline
            let j = i;
            while (!bloco.includes(')') && j < linhas.length - 1) {
                j++;
                bloco += ' ' + linhas[j];
            }

            const match = bloco.match(/message\s*=\s*"([^"]+)"/);
            if (match) {
                const anotacao = ANOTACOES.find(a => bloco.includes(a)) || 'unknown';
                mensagens.push({
                    texto: match[1],
                    arquivo: relativo(arquivo),
                    linha: i + 1,
                    tipo: 'validacao_dto',
                    anotacao: anotacao.replace('@', ''),
                });
            }
        }
    }
    return mensagens;
}

/**
 * Extrai mensagens de throw new ErroValidacao, ErroEntidadeNaoEncontrada, etc.
 */
function extrairMensagensExcecoes(arquivos) {
    const mensagens = [];
    const PATTERN = /throw\s+new\s+Erro\w+\(\s*"([^"]+)"/g;

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            const match = linha.match(/throw\s+new\s+(Erro\w+)\(\s*"([^"]+)"/);
            if (match) {
                mensagens.push({
                    texto: match[2],
                    arquivo: relativo(arquivo),
                    linha: i + 1,
                    tipo: 'excecao_negocio',
                    classe: match[1],
                });
            }
        }
    }
    return mensagens;
}

/**
 * Extrai strings de mensagem de arquivos Java de teste.
 */
function extrairMensagensTeste(arquivos) {
    const mensagens = [];
    // Padrões comuns em testes: isEqualTo("..."), contains("..."), hasMessage("...")
    const PATTERNS = [
        /\.isEqualTo\("([^"]+)"\)/,
        /\.contains\("([^"]+)"\)/,
        /\.hasMessage\("([^"]+)"\)/,
        /\.hasMessageContaining\("([^"]+)"\)/,
        /assertThat.*"([^"]+)"/,
        /assertEquals\(\s*"([^"]+)"/,
        /\.message\(\).*"([^"]+)"/,
    ];

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            for (const pattern of PATTERNS) {
                const match = linha.match(pattern);
                if (match && match[1].length > 5) { // filtrar strings muito curtas
                    mensagens.push({
                        texto: match[1],
                        arquivo: relativo(arquivo),
                        linha: i + 1,
                        tipo: 'teste_backend',
                    });
                    break;
                }
            }
        }
    }
    return mensagens;
}

// ── Extratores de Frontend (TypeScript/Vue) ──────────────────────────────────

/**
 * Extrai mensagens de toastStore.setPending() nos arquivos Vue/TS.
 */
function extrairMensagensToast(arquivos) {
    const mensagens = [];
    const PATTERN = /setPending\(\s*["'`]([^"'`]+)["'`]\s*\)/g;

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            const match = linha.match(/setPending\(\s*["'`]([^"'`]+)["'`]\s*\)/);
            if (match) {
                mensagens.push({
                    texto: match[1],
                    arquivo: relativo(arquivo),
                    linha: i + 1,
                    tipo: 'toast_sucesso',
                });
            }
        }
    }
    return mensagens;
}

/**
 * Extrai mensagens de notify() e notifyStructured() nos arquivos Vue/TS.
 */
function extrairMensagensNotificacao(arquivos) {
    const mensagens = [];
    const PATTERNS = [
        /notify\(\s*["'`]([^"'`]{5,})["'`]/,
        /notifyStructured\(\s*["'`]([^"'`]{5,})["'`]/,
        /message:\s*["'`]([^"'`]{5,})["'`]/,
    ];

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            for (const pattern of PATTERNS) {
                const match = linha.match(pattern);
                if (match) {
                    mensagens.push({
                        texto: match[1],
                        arquivo: relativo(arquivo),
                        linha: i + 1,
                        tipo: 'notificacao_frontend',
                    });
                    break;
                }
            }
        }
    }
    return mensagens;
}

/**
 * Extrai constantes de texto dos arquivos TS de constants.
 */
function extrairConstantesTexto(arquivos) {
    const mensagens = [];

    for (const arquivo of arquivos) {
        if (!arquivo.includes('constants') && !arquivo.includes('situacoes') && !arquivo.includes('textos')) {
            continue;
        }
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            // Padrão: CHAVE: "valor" ou CHAVE = "valor"
            const match = linha.match(/[A-Z_]{3,}\s*[=:]\s*["'`]([^"'`]{3,})["'`]/);
            if (match) {
                mensagens.push({
                    texto: match[1],
                    arquivo: relativo(arquivo),
                    linha: i + 1,
                    tipo: 'constante_frontend',
                });
            }
        }
    }
    return mensagens;
}

// ── Extratores de Testes E2E (Playwright/TypeScript) ─────────────────────────

/**
 * Extrai asserções de texto dos testes E2E (getByText, toContainText, etc.)
 */
function extrairMensagensE2e(arquivos) {
    const mensagens = [];
    const PATTERNS = [
        /getByText\(\s*["'`]([^"'`]{3,})["'`]/,
        /toContainText\(\s*["'`]([^"'`]{3,})["'`]/,
        /toHaveText\(\s*["'`]([^"'`]{3,})["'`]/,
        /toContain\(\s*["'`]([^"'`]{3,})["'`]/,
        /getByRole\([^)]+,\s*\{\s*name:\s*["'`]([^"'`]{3,})["'`]/,
        /getByLabel\(\s*["'`]([^"'`]{3,})["'`]/,
        /getByPlaceholder\(\s*["'`]([^"'`]{3,})["'`]/,
    ];

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const linha = linhas[i];
            for (const pattern of PATTERNS) {
                const match = linha.match(pattern);
                if (match) {
                    const texto = match[1].trim();
                    // Filtrar seletores CSS, IDs e strings técnicas
                    if (texto.startsWith('[') || texto.startsWith('.') || texto.startsWith('#')) continue;
                    if (texto.length < 4) continue;
                    mensagens.push({
                        texto,
                        arquivo: relativo(arquivo),
                        linha: i + 1,
                        tipo: 'assertiva_e2e',
                    });
                    break;
                }
            }
        }
    }
    return mensagens;
}

/**
 * Extrai chamadas de toast nos testes E2E.
 */
function extrairToastE2e(arquivos) {
    const mensagens = [];
    const PATTERN = /toHaveBeenCalledWith\(\s*["'`]([^"'`]{3,})["'`]/;

    for (const arquivo of arquivos) {
        const conteudo = fs.readFileSync(arquivo, 'utf-8');
        const linhas = conteudo.split(/\r?\n/);

        for (let i = 0; i < linhas.length; i++) {
            const match = linhas[i].match(PATTERN);
            if (match) {
                mensagens.push({
                    texto: match[1],
                    arquivo: relativo(arquivo),
                    linha: i + 1,
                    tipo: 'toast_e2e',
                });
            }
        }
    }
    return mensagens;
}

// ── Função Principal ──────────────────────────────────────────────────────────

function extrair() {
    console.log('🔍 Iniciando extração de mensagens do SGC...\n');

    const resultado = {
        meta: {
            geradoEm: new Date().toISOString(),
            versao: '1.0',
            descricao: 'Inventário de mensagens/strings do projeto SGC',
        },
        backend: {
            validacao_dto: [],
            excecao_negocio: [],
        },
        frontend: {
            toast_sucesso: [],
            notificacao_frontend: [],
            constante_frontend: [],
        },
        testes: {
            teste_backend: [],
            assertiva_e2e: [],
            toast_e2e: [],
        },
    };

    // ── Backend: arquivos de produção ──
    console.log('📂 Analisando backend (produção)...');
    const arquivosProdJava = listarArquivos(BACKEND_SRC, '.java');
    const arquivosDtos = arquivosProdJava.filter(f =>
        f.includes('Dto') || f.includes('Request') || f.includes('dto')
    );
    const arquivosServices = arquivosProdJava.filter(f =>
        !f.includes('Dto') && !f.includes('Request')
    );

    resultado.backend.validacao_dto = extrairMensagensValidacaoDto(arquivosDtos);
    resultado.backend.excecao_negocio = extrairMensagensExcecoes(arquivosServices);

    console.log(`  ✓ DTOs: ${resultado.backend.validacao_dto.length} mensagens encontradas`);
    console.log(`  ✓ Serviços: ${resultado.backend.excecao_negocio.length} mensagens encontradas`);

    // ── Backend: testes ──
    console.log('📂 Analisando testes do backend...');
    const arquivosTestJava = listarArquivos(BACKEND_TEST, '.java');
    resultado.testes.teste_backend = extrairMensagensTeste(arquivosTestJava);
    console.log(`  ✓ Testes Java: ${resultado.testes.teste_backend.length} asserções encontradas`);

    // ── Frontend ──
    console.log('📂 Analisando frontend...');
    const arquivosVue = listarArquivos(FRONTEND_SRC, '.vue');
    const arquivosTs = listarArquivos(FRONTEND_SRC, '.ts');
    const todosArquivosFrontend = [...arquivosVue, ...arquivosTs];

    resultado.frontend.toast_sucesso = extrairMensagensToast(todosArquivosFrontend);
    resultado.frontend.notificacao_frontend = extrairMensagensNotificacao(todosArquivosFrontend);
    resultado.frontend.constante_frontend = extrairConstantesTexto(arquivosTs);

    console.log(`  ✓ Toast: ${resultado.frontend.toast_sucesso.length} mensagens encontradas`);
    console.log(`  ✓ Notificações: ${resultado.frontend.notificacao_frontend.length} mensagens encontradas`);
    console.log(`  ✓ Constantes: ${resultado.frontend.constante_frontend.length} constantes encontradas`);

    // ── E2E ──
    console.log('📂 Analisando testes E2E...');
    const arquivosE2e = listarArquivos(E2E_DIR, '.ts');
    resultado.testes.assertiva_e2e = extrairMensagensE2e(arquivosE2e);
    resultado.testes.toast_e2e = extrairToastE2e([...listarArquivos(FRONTEND_SRC, '.spec.ts')]);
    console.log(`  ✓ Asserções E2E: ${resultado.testes.assertiva_e2e.length} encontradas`);
    console.log(`  ✓ Toast em testes: ${resultado.testes.toast_e2e.length} encontradas`);

    // ── Totais ──
    const total = Object.values(resultado.backend).flat().length
        + Object.values(resultado.frontend).flat().length
        + Object.values(resultado.testes).flat().length;

    console.log(`\n📊 Total: ${total} mensagens extraídas`);

    // ── Gravar resultado ──
    fs.writeFileSync(OUTPUT_FILE, JSON.stringify(resultado, null, 2), 'utf-8');
    console.log(`\n✅ Resultado gravado em: ${path.relative(process.cwd(), OUTPUT_FILE)}`);
    console.log(`\n💡 Próximo passo: node etc/scripts/analisar-mensagens.cjs`);

    return resultado;
}

extrair();
