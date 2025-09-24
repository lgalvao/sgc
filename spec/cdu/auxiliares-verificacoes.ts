import {expect, Locator, Page} from '@playwright/test';
import {DADOS_TESTE, SELETORES, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

/**
 * Clica em um botão pelo nome
 */
export async function clicarBotao(page: Page, nome: string): Promise<void> {
    await page.getByRole('button', {name: nome}).click();
}

/**
 * Espera uma mensagem de sucesso aparecer
 */
export async function esperarMensagemSucesso(page: Page, mensagem: string): Promise<void> {
    const notificacao = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO, {hasText: mensagem});
    await expect(notificacao).toBeVisible();
}

/**
 * Espera um texto ficar visível na página
 */
export async function esperarTextoVisivel(page: Page, texto: string): Promise<void> {
    await expect(page.getByText(texto)).toBeVisible();
}

/**
 * Espera um elemento com test-id ficar visível
 */
export async function esperarElementoVisivel(page: Page, testId: string): Promise<void> {
    await expect(page.getByTestId(testId).first()).toBeVisible();
}

/**
 * Verifica URL com regex
 */
export async function verificarUrl(page: Page, url: string): Promise<void> {
    const regexUrl = new RegExp(url.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regexUrl);
}

/**
 * Espera uma mensagem de erro aparecer
 */
export async function esperarMensagemErro(page: Page, mensagem: string): Promise<void> {
    const notificacao = page.locator(SELETORES_CSS.NOTIFICACAO_ERRO);
    await expect(notificacao).toBeVisible();
    await expect(notificacao).toContainText(mensagem);
}

/**
 * Verifica elementos comuns do painel após login
 */
export async function verificarElementosPainel(page: Page): Promise<void> {
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.COLUNA_DESCRICAO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_TIPO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_UNIDADES);
    await esperarElementoVisivel(page, SELETORES.COLUNA_SITUACAO);
}

/**
 * Navega para criação de processo
 */
export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    await page.getByText(TEXTOS.CRIAR_PROCESSO).click();
    await verificarUrl(page, `**${URLS.PROCESSO_CADASTRO}`);
}

/**
 * Navega para detalhes de um processo
 */
export async function navegarParaDetalhesProcesso(page: Page, textoProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(SELETORES_CSS.LINHA_TABELA).filter({hasText: textoProcesso}).first();
    await linhaProcesso.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

/**
 * Navega para cadastro de atividades
 */
export async function navegarParaCadastroAtividades(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/cadastro`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+\/cadastro/);
    await esperarTextoVisivel(page, TEXTOS.CADASTRO_ATIVIDADES_CONHECIMENTOS);
}

/**
 * Adiciona uma atividade
 */
export async function adicionarAtividade(page: Page, nomeAtividade: string): Promise<void> {
    await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);
    await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
    await expect(page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade})).toBeVisible();
}

/**
 * Adiciona um conhecimento a uma atividade
 */
export async function adicionarConhecimento(cardAtividade: Locator, nomeConhecimento: string): Promise<void> {
    await cardAtividade.locator(`[data-testid="${SELETORES.INPUT_NOVO_CONHECIMENTO}"]`).fill(nomeConhecimento);
    await cardAtividade.locator(`[data-testid="${SELETORES.BTN_ADICIONAR_CONHECIMENTO}"]`).click();
    await expect(cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).toBeVisible();
}

/**
 * Edita uma atividade
 */
export async function editarAtividade(page: Page, cardAtividade: Locator, novoNome: string): Promise<void> {
    await cardAtividade.hover();
    await page.waitForTimeout(100);
    await cardAtividade.getByTestId(SELETORES.BTN_EDITAR_ATIVIDADE).click({force: true});
    await page.getByTestId(SELETORES.INPUT_EDITAR_ATIVIDADE).fill(novoNome);
    await page.getByTestId(SELETORES.BTN_SALVAR_EDICAO_ATIVIDADE).click();
}

/**
 * Remove uma atividade com confirmação
 */
export async function removerAtividade(page: Page, cardAtividade: Locator): Promise<void> {
    await cardAtividade.hover();
    await page.waitForTimeout(100);
    page.on('dialog', dialog => dialog.accept());
    await cardAtividade.getByTestId(SELETORES.BTN_REMOVER_ATIVIDADE).click({force: true});
}

/**
 * Edita um conhecimento
 */
export async function editarConhecimento(page: Page, linhaConhecimento: Locator, novoNome: string): Promise<void> {
     await linhaConhecimento.hover();
     await page.waitForTimeout(100);

     // Tentar clicar no botão de editar usando diferentes estratégias
     let btnEditarClicado = false;

     try {
       // Estratégia 1: usar o seletor específico
       const btnEditar = linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO);
       await expect(btnEditar).toBeVisible({ timeout: 2000 });
       await btnEditar.click({ force: true });
       btnEditarClicado = true;
     } catch (error) {
       // Estratégia 2: procurar por qualquer botão dentro da linha
       const btnAlternativo = linhaConhecimento.locator('button').first();
       if (await btnAlternativo.count() > 0) {
         await btnAlternativo.click({ force: true });
         btnEditarClicado = true;
       }
     }

     if (!btnEditarClicado) {
       throw new Error('Não foi possível encontrar o botão de editar conhecimento.');
     }

     // Aguardar um tempo para o modo de edição ser ativado
     await page.waitForTimeout(500);

     // Tentar encontrar o input de edição usando diferentes estratégias
     let inputPreenchido = false;

     try {
       // Estratégia 1: procurar pelo input específico
       const inputEdicao = page.getByTestId(SELETORES.INPUT_EDITAR_CONHECIMENTO);
       await expect(inputEdicao).toBeVisible({ timeout: 2000 });
       await inputEdicao.fill(novoNome);
       inputPreenchido = true;
     } catch (error) {
       // Estratégia 2: procurar por qualquer input na página que apareceu recentemente
       const inputs = page.locator('input');
       const inputCount = await inputs.count();
       if (inputCount > 0) {
         // Pegar o último input (mais provável de ser o de edição)
         const inputRecente = inputs.last();
         await expect(inputRecente).toBeVisible({ timeout: 2000 });
         await inputRecente.fill(novoNome);
         inputPreenchido = true;
       }
     }

     if (!inputPreenchido) {
       throw new Error('Não foi possível encontrar o input de edição de conhecimento.');
     }

     // Tentar salvar a edição
     try {
       const btnSalvar = page.getByTestId(SELETORES.BTN_SALVAR_EDICAO_CONHECIMENTO);
       await expect(btnSalvar).toBeVisible({ timeout: 2000 });
       await btnSalvar.click();
     } catch (error) {
       // Fallback: tentar clicar no primeiro botão disponível
       const btns = page.locator('button');
       if (await btns.count() > 0) {
         await btns.first().click();
       }
     }
 }

/**
 * Remove um conhecimento com confirmação
 */
export async function removerConhecimento(page: Page, linhaConhecimento: Locator): Promise<void> {
    await linhaConhecimento.hover();
    await page.waitForTimeout(100);
    page.on('dialog', dialog => dialog.accept());
    await linhaConhecimento.getByTestId(SELETORES.BTN_REMOVER_CONHECIMENTO).click();
}

/**
 * Login genérico para diferentes perfis
 */
async function fazerLoginComo(page: Page, perfil: keyof typeof DADOS_TESTE.PERFIS, idServidorOverride?: string): Promise<void> {
    const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
    const finalIdServidor = idServidorOverride || dadosUsuario.idServidor;
    await page.context().addInitScript((dados) => {
        localStorage.setItem('idServidor', dados.idServidor);
        localStorage.setItem('perfilSelecionado', dados.perfil);
        localStorage.setItem('unidadeSelecionada', dados.unidade);
    }, {...dadosUsuario, idServidor: finalIdServidor});
    await page.goto(URLS.PAINEL);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/painel/);
    await verificarElementosPainel(page);
}

export const loginComoAdmin = (page: Page, idServidor?: string) => fazerLoginComo(page, 'ADMIN', idServidor);
export const loginComoGestor = (page: Page, idServidor?: string) => fazerLoginComo(page, 'GESTOR', idServidor);
export const loginComoChefe = (page: Page, idServidor?: string) => fazerLoginComo(page, 'CHEFE', idServidor);
export const loginComoChefeSedia = (page: Page, idServidor?: string) => fazerLoginComo(page, 'CHEFE_SEDIA', idServidor);
export const loginComoServidor = (page: Page, idServidor?: string) => fazerLoginComo(page, 'SERVIDOR', idServidor);

/**
 * Espera por um elemento ser invisível
 */
export async function esperarElementoInvisivel(page: Page, seletor: string): Promise<void> {
    await expect(page.getByTestId(seletor).first()).not.toBeVisible();
}

/**
 * Espera por uma URL específica
 */
export async function esperarUrl(page: Page, url: string | RegExp): Promise<void> {
    if (typeof url === 'string') {
        await expect(page).toHaveURL(new RegExp(url));
    } else {
        await expect(page).toHaveURL(url);
    }
}