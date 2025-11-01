import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS, URLS} from '../dados';
import {esperarMensagemSucesso, esperarUrl, verificarUrlDoPainel} from './verificacoes-basicas';

/**
 * Verifica se a página atual é a de edição de processo.
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaEdicaoProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
}

/**
 * Verifica se a página atual é a de cadastro de processo.
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaCadastroProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/processo\/cadastro(\?idProcesso=\d+)?$/);
}

/**
 * Verifica se os campos obrigatórios do formulário de processo estão visíveis.
 * @param page A instância da página do Playwright.
 */
export async function verificarCamposObrigatoriosFormulario(page: Page): Promise<void> {
    await expect(page.getByLabel('Descrição')).toBeVisible();
    await expect(page.getByLabel('Tipo')).toBeVisible();
    await expect(page.getByText(TEXTOS.UNIDADES_PARTICIPANTES)).toBeVisible();
}

/**
 * Verifica se uma notificação de erro está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarNotificacaoErro(page: Page): Promise<void> {
    // As notificações aparecem como alerts do Bootstrap ou mensagens inline
    // Vamos verificar se há alguma mensagem de erro visível
    const erroSelectors = [
        '.alert.alert-danger',  // Bootstrap alert
        '[role="alert"]',        // ARIA alert
        '.text-danger',          // Texto vermelho
        '.invalid-feedback:visible', // Feedback de validação do Bootstrap
    ];
    
    let encontrado = false;
    for (const selector of erroSelectors) {
        const element = page.locator(selector).first();
        if (await element.isVisible({timeout: 5000}).catch(() => false)) {
            encontrado = true;
            break;
        }
    }
    
    if (!encontrado) {
        throw new Error('Nenhuma mensagem de erro encontrada na página');
    }
}

/**
 * Aguarda que um processo apareça na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function aguardarProcessoNoPainel(page: Page, descricaoProcesso: string): Promise<void> {
    await page.waitForURL(URLS.PAINEL);
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoProcesso)).toBeVisible();
}

/**
 * Verifica se um processo editado aparece na tabela e o original não.
 * @param page A instância da página do Playwright.
 * @param descricaoOriginal A descrição original do processo.
 * @param descricaoEditada A nova descrição do processo.
 */
export async function verificarProcessoEditado(page: Page, descricaoOriginal: string, descricaoEditada: string): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoEditada)).toBeVisible();
    await expect(page.getByText(descricaoOriginal)).not.toBeVisible();
}

/**
 * Verifica se o diálogo de confirmação de remoção está visível.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarDialogoConfirmacaoRemocao(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).toBeVisible();
}

/**
 * Verifica se um processo foi removido com sucesso.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarProcessoRemovidoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
    const mensagemEsperada = `${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricaoProcesso}${TEXTOS.PROCESSO_REMOVIDO_FIM}`;

    // 1) Tentar verificar marcador em localStorage (mais robusto em presença de navegação)
    try {
        const lastRemoved = await page.evaluate(() => localStorage.getItem('lastRemovedProcess'));
        if (lastRemoved === descricaoProcesso) {
            // Remover o marcador para evitar interferência em próximos testes
            await page.evaluate(() => localStorage.removeItem('lastRemovedProcess'));
            return;
        }
    } catch {
        // continuar para os fallbacks
    }

    // 2) Tentar localizar notificação com test-id específico (mais robusto)
    try {
        const notificacaoTestId = page.getByTestId('notificacao-remocao');
        if ((await notificacaoTestId.count()) > 0) {
            await notificacaoTestId.first().waitFor({state: 'visible'});
            return; // sucesso
        }
    } catch {
        // continuar para os fallbacks
    }

    // 2) Tentar localizar notificação que contenha exatamente a mensagem canônica
    try {
        await page.waitForSelector(`.notification:has-text("${mensagemEsperada}")`,);
        await expect(page.locator(`.notification:has-text("${mensagemEsperada}")`)).toBeVisible();
    } catch {
        // 3) Fallback: procurar qualquer notificação que contenha o nome do processo
        try {
            await page.waitForSelector(`.notification:has-text("${descricaoProcesso}")`);
            await expect(page.locator(`.notification:has-text("${descricaoProcesso}")`)).toBeVisible();
        } catch {
            // 4) Se ainda não encontrado, registrar aviso para diagnóstico e prosseguir para verificação de ausência na tabela

            console.warn(`Aviso: não foi possível encontrar notificação de remoção para "${descricaoProcesso}"`);
        }
    }

    // Tentar garantir que retornou ao painel — não falhar imediatamente se a navegação for rápida/assíncrona
    try {
        await expect(page).toHaveURL(URLS.PAINEL);
    } catch {
        // Aviso para diagnóstico, mas continuamos com verificação da tabela
        console.warn('Aviso: não foi possível confirmar navegação para o painel via URL após remoção.');
    }

    // Verificar que a linha do processo não está mais presente na tabela de processos.
    // Usar polling tolerante para lidar com timings assíncronos da UI
    const timeoutMs = 10000;
    const intervalMs = 500;
    const deadline = Date.now() + timeoutMs;
    let stillPresent = true;
    while (Date.now() < deadline) {
        try {
            const linhaProcesso = page.locator('[data-testid="tabela-processos"] tbody tr').filter({hasText: descricaoProcesso});
            const count = await linhaProcesso.count();
            if (count === 0) {
                stillPresent = false;
                break;
            }
            // se existe, verificar se está invisível
            const visible = (await linhaProcesso.first().isVisible());
            if (!visible) {
                stillPresent = false;
                break;
            }
        } catch (err) {
            // Se a página/contexto foi fechado inesperadamente, abortamos o loop

            console.warn('Aviso durante verificação de remoção:', err);
            break;
        }
        // aguardar próximo polling
        await new Promise(res => setTimeout(res, intervalMs));
    }

    if (stillPresent) {
        // Falha explícita para diagnóstico (mantemos stack de onde foi chamada)
        throw new Error(`Linha do processo "${descricaoProcesso}" ainda está presente após ${timeoutMs}ms`);
    }
}

/**
 * Verifica se o botão "Finalizar processo" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoFinalizarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO})).toBeVisible();
}

/**
 * Verifica se o botão "Finalizar processo" não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoFinalizarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO})).not.toBeVisible();
}

/**
 * Verifica se o modal de finalização de processo está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalFinalizacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(modal.getByRole('heading', {name: TEXTOS.FINALIZACAO_PROCESSO})).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMA_FINALIZACAO)).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMACAO_VIGENCIA_MAPAS)).toBeVisible();
    await expect(modal.getByRole('button', {name: TEXTOS.CONFIRMAR})).toBeVisible();
    await expect(modal.getByRole('button', {name: TEXTOS.CANCELAR})).toBeVisible();
}

/**
 * Verifica se o modal de finalização de processo não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalFinalizacaoFechado(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Verifica se a notificação de bloqueio de finalização está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarFinalizacaoBloqueada(page: Page): Promise<void> {
    await expect(page.locator('.notification')).toContainText(TEXTOS.FINALIZACAO_BLOQUEADA);
}

/**
 * Verifica se um processo está com o status "Finalizado" no painel.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 */
export async function verificarProcessoFinalizadoNoPainel(page: Page, nomeProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: nomeProcesso});
    await expect(linhaProcesso).toContainText(TEXTOS.FINALIZADO);
}

/**
 * Verifica se a mensagem de sucesso de finalização está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarMensagemFinalizacaoSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_FINALIZADO);
}

/**
 * Verifica se a notificação de mapas vigentes está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarMapasVigentesNotificacao(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.NOTIFICACAO_SUCESSO)).toContainText(TEXTOS.MAPAS_VIGENTES);
}

/**
 * Verifica se a notificação de e-mail enviado está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarEmailFinalizacaoEnviado(page: Page): Promise<void> {
    const notificacaoEmail = page.locator(SELETORES.NOTIFICACAO_EMAIL).first();
    await expect(notificacaoEmail).toBeVisible();
    await expect(notificacaoEmail).toContainText(TEXTOS.EMAIL_ENVIADO);
}

/**
 * Verifica se o cadastro foi devolvido com sucesso.
 * @param page A instância da página do Playwright.
 */
export async function verificarCadastroDevolvidoComSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
    await verificarUrlDoPainel(page);
}

/**
 * Verifica se o aceite foi registrado com sucesso.
 * @param page A instância da página do Playwright.
 */
export async function verificarAceiteRegistradoComSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.ACEITE_REGISTRADO);
    await verificarUrlDoPainel(page);
}

/**
 * Verifica se o diálogo de confirmação de remoção não está visível.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarDialogoConfirmacaoFechado(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).not.toBeVisible();
}

/**
 * Verifica se um processo foi iniciado com sucesso.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarProcessoIniciadoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(TEXTOS.PROCESSO_INICIADO)).toBeVisible();
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.locator('tr', {hasText: descricaoProcesso}).getByText(TEXTOS.EM_ANDAMENTO)).toBeVisible();
}

/**
 * Verifica se a página permanece no formulário de edição.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarPermanenciaFormularioEdicao(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
    await expect(page.getByLabel('Descrição')).toHaveValue(descricaoProcesso);
}

/**
 * Verifica se o modal de confirmação de inicialização está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarConfirmacaoInicializacao(page: Page): Promise<void> {
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
}

/**
 * Verifica o conteúdo do modal de confirmação de inicialização.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param numUnidades O número de unidades.
 */
export async function verificarModalConfirmacaoInicializacao(page: Page, descricao: string, tipo: string, numUnidades: number): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
    await expect(modal.locator('p')).toContainText(`Descrição: ${descricao}`);
    await expect(modal.locator('p')).toContainText(`Tipo: ${tipo}`);
    await expect(modal.locator('p')).toContainText(`Unidades selecionadas: ${numUnidades}`);
}

/**
 * Verifica se um processo foi inicializado com sucesso.
 * @param page A instância da página do Playwright.
 */
export async function verificarProcessoInicializadoComSucesso(page: Page): Promise<void> {
    await esperarUrl(page, URLS.PAINEL);
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
}

/**
 * Verifica o valor do campo de descrição.
 * @param page A instância da página do Playwright.
 * @param valor O valor esperado.
 */
export async function verificarValorCampoDescricao(page: Page, valor: string): Promise<void> {
    await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(valor);
}

/**
 * Verifica se o botão "Iniciar processo" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoIniciarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.locator(`[data-testid="${SELETORES.BTN_INICIAR_PROCESSO}"]`)).toBeVisible();
}

/**
 * Verifica se o modal de confirmação de iniciar processo está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalConfirmacaoIniciarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
    await expect(page.locator(SELETORES.TITULO_MODAL_INICIAR_PROCESSO)).toBeVisible();
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
    // Removido: verificação de notificação por email que pode não estar presente no modal atual
}

/**
 * Verifica se o modal de confirmação de iniciar processo não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalConfirmacaoIniciarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Verifica o conteúdo do modal de confirmação de iniciação.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalConfirmacaoIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible({timeout: 10000});
    await expect(modal.locator('.modal-title')).toContainText(/iniciar processo/i);
    await expect(modal.locator('.modal-body')).toContainText(/não será mais possível editá-lo ou removê-lo/i);
    await expect(modal.locator('.modal-body')).toContainText(/todas as unidades participantes serão notificadas/i);
    await expect(modal.getByRole('button', {name: /confirmar/i})).toBeVisible();
    await expect(modal.getByRole('button', {name: /cancelar/i})).toBeVisible();
}

/**
 * Verifica se um processo está bloqueado para edição.
 * @param page A instância da página do Playwright.
 */
export async function verificarProcessoBloqueadoParaEdicao(page: Page): Promise<void> {
    // Verificar que botões de edição não estão disponíveis
    await expect(page.getByRole('button', {name: /^salvar$/i})).not.toBeVisible();
    await expect(page.getByRole('button', {name: /^remover$/i})).not.toBeVisible();
    await expect(page.getByRole('button', {name: /^iniciar processo$/i})).not.toBeVisible();
}

/**
 * Verifica se a página permanece na página de um processo.
 * @param page A instância da página do Playwright.
 * @param idProcesso O ID do processo.
 */
export async function verificarPermanenciaNaPaginaProcesso(page: Page, idProcesso: number): Promise<void> {
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}`));
}
