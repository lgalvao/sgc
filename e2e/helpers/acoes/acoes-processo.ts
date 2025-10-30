import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {clicarElemento, preencherCampo} from '../utils';
import {navegarParaCriacaoProcesso} from 'e2e/helpers/index';

/**
 * Seleciona unidades na árvore usando suas SIGLAS
 * @param page - A instância da página do Playwright.
 * @param siglas - Um array de SIGLAS das unidades a serem selecionadas (ex: ['STIC', 'SGP']).
 */
export async function selecionarUnidadesPorSigla(page: Page, siglas: string[]): Promise<void> {
    // Aguardar a árvore de unidades carregar
    await page.waitForSelector('.form-check-input[type="checkbox"]', {state: 'visible', timeout: 15000});
    
    for (const sigla of siglas) {
        const seletorCheckbox = `#chk-${sigla}`;
        await page.waitForSelector(seletorCheckbox, {state: 'visible', timeout: 15000});
        await page.check(seletorCheckbox);
    }
}

/**
 * Seleciona unidades na árvore de hierarquia com base em seus IDs.
 * NOTA: Este helper converte IDs para SIGLAS conhecidas
 * @param page - A instância da página do Playwright.
 * @param unidades - Um array de IDs (números) das unidades a serem selecionadas.
 */
export async function selecionarUnidadesPorId(page: Page, unidades: number[]): Promise<void> {
    // Mapeamento conhecido de ID -> SIGLA (baseado no data.sql)
    const idParaSigla: Record<number, string> = {
        1: 'TRE-PE',
        2: 'STIC',
        3: 'SGP',
        4: 'COEDE',
        5: 'COJUR',
        6: 'COSIS',
        7: 'COSINF',
        8: 'SEDESENV',
        // Adicione mais conforme necessário
    };
    
    const siglas = unidades.map(id => idParaSigla[id]).filter(Boolean);
    if (siglas.length !== unidades.length) {
        console.warn('Algumas unidades não têm SIGLA mapeada:', unidades);
    }
    
    await selecionarUnidadesPorSigla(page, siglas);
}


/**
 * Preencher formulário básico de processo
 */
export async function preencherFormularioProcesso(
    page: Page,
    descricao: string,
    tipo: string,
    dataLimite?: string,
    sticChecked: boolean = false
): Promise<void> {
    await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
    await page.selectOption(SELETORES.CAMPO_TIPO, tipo);

    if (dataLimite) {
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, dataLimite);
    }
    if (sticChecked) {
        await page.check(SELETORES.CHECKBOX_STIC);
    }
}

/**
 * Selecionar primeira unidade disponível
 */
export async function selecionarPrimeiraUnidade(page: Page): Promise<void> {
    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.click();
}

/**
 * Clicar no primeiro processo da tabela
 */
export async function clicarPrimeiroProcessoTabela(page: Page): Promise<void> {
    const primeiraLinha = page.locator('table tbody tr').first();
    await primeiraLinha.click();
}

/**
 * Iniciar processo de mapeamento (CDU-04)
 * Clica no botão "Iniciar processo" e aguarda modal de confirmação
 */
export async function iniciarProcessoMapeamento(page: Page): Promise<void> {
    console.log(`[DEBUG] iniciarProcessoMapeamento: Procurando botão Iniciar processo`);
    
    // DEBUG: Verificar chamadas de rede
    const responses: string[] = [];
    page.on('response', async (response) => {
        if (response.url().includes('/api/')) {
            const status = response.status();
            const url = response.url();
            try {
                const body = await response.text();
                responses.push(`${status} ${url}: ${body.substring(0, 200)}`);
                console.log(`[DEBUG] API Response: ${status} ${url}`);
            } catch (e) {
                responses.push(`${status} ${url}: (couldn't read body)`);
            }
        }
    });
    
    // DEBUG: Verificar se checkboxes de unidades estão marcados
    const checkboxesMarcados = await page.locator('input[type="checkbox"]:checked').count();
    console.log(`[DEBUG] Checkboxes marcados: ${checkboxesMarcados}`);
    
    // Se não há checkboxes marcados, há um bug no frontend!
    // O processo foi criado com unidades, mas ao abrir para edição elas não são carregadas
    if (checkboxesMarcados === 0) {
        console.warn(`[AVISO] BUG DO FRONTEND: Unidades não foram carregadas ao abrir processo para edição!`);
        console.warn(`[AVISO] Últimas ${responses.length} respostas da API:`);
        responses.forEach(r => console.warn(`  ${r}`));
    }
    
    await clicarElemento([
        page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO),
        page.getByRole('button', {name: /iniciar processo/i})
    ]);
    console.log(`[DEBUG] iniciarProcessoMapeamento: Clicou em Iniciar processo`);
}

/**
 * Confirmar iniciação de processo no modal (CDU-04)
 */
export async function confirmarIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.waitFor({state: 'visible', timeout: 15000});
    
    await clicarElemento([
        modal.getByTestId(SELETORES.BTN_MODAL_CONFIRMAR),
        modal.getByRole('button', {name: /confirmar/i})
    ]);
}

/**
 * Cancelar iniciação de processo no modal (CDU-04)
 */
export async function cancelarIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.waitFor({state: 'visible', timeout: 15000});
    
    await clicarElemento([
        modal.getByTestId(SELETORES.BTN_MODAL_CANCELAR),
        modal.getByRole('button', {name: /cancelar/i})
    ]);
}

/**
 * Criar processo básico (usado em vários CDUs)
 * Preenche formulário e salva
 */
export async function criarProcessoBasico(
    page: Page,
    descricao: string,
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO',
    siglas: string[],
    dataLimite: string = '2025-12-31'
): Promise<void> {
    console.log(`[DEBUG] criarProcessoBasico: Iniciando criação de processo "${descricao}"`);
    
    await navegarParaCriacaoProcesso(page);
    console.log(`[DEBUG] criarProcessoBasico: Navegou para criação`);
    
    await preencherCampo([page.locator(SELETORES.CAMPO_DESCRICAO)], descricao);
    console.log(`[DEBUG] criarProcessoBasico: Preencheu descrição`);
    
    await page.selectOption(SELETORES.CAMPO_TIPO, tipo);
    console.log(`[DEBUG] criarProcessoBasico: Selecionou tipo ${tipo}`);
    
    await preencherCampo([page.locator(SELETORES.CAMPO_DATA_LIMITE)], dataLimite);
    console.log(`[DEBUG] criarProcessoBasico: Preencheu data limite`);
    
    await selecionarUnidadesPorSigla(page, siglas);
    console.log(`[DEBUG] criarProcessoBasico: Selecionou unidades ${siglas.join(', ')}`);
    
    await clicarElemento([
        page.getByRole('button', {name: /salvar/i}),
        page.getByTestId('btn-salvar')
    ]);
    console.log(`[DEBUG] criarProcessoBasico: Clicou em Salvar`);
    
    // Aguardar redirecionamento ao painel
    await page.waitForURL(/\/painel/, {timeout: 15000});
    console.log(`[DEBUG] criarProcessoBasico: Redirecionado ao painel`);
}

/**
 * Abrir processo específico da tabela pelo nome
 */
export async function abrirProcessoPorNome(page: Page, descricao: string): Promise<void> {
    console.log(`[DEBUG] abrirProcessoPorNome: Procurando processo "${descricao}"`);
    const row = page.locator(`[data-testid="${SELETORES.TABELA_PROCESSOS}"] tr:has-text("${descricao}")`);
    await row.waitFor({state: 'visible', timeout: 15000});
    console.log(`[DEBUG] abrirProcessoPorNome: Processo encontrado, clicando`);
    await row.click();
    console.log(`[DEBUG] abrirProcessoPorNome: Clicou no processo`);
    
    // Aguardar navegação para página de cadastro
    await page.waitForURL(/\/processo\/cadastro\?idProcesso=\d+/, {timeout: 15000});
    console.log(`[DEBUG] abrirProcessoPorNome: Navegou para página de cadastro`);
    
    // Aguardar formulário carregar com os dados do processo
    await page.waitForSelector(SELETORES.CAMPO_DESCRICAO, {state: 'visible', timeout: 15000});
    // Aguardar um pouco mais para garantir que todos os dados foram carregados (unidades, etc)
    await page.waitForTimeout(2000); // Aumentado de 1s para 2s
    console.log(`[DEBUG] abrirProcessoPorNome: Formulário carregado`);
    
    // DEBUG: Verificar dados carregados usando page.evaluate
    const dadosCarregados = await page.evaluate(() => {
        // Acessar variáveis Vue através do DOM
        const checkboxes = Array.from(document.querySelectorAll('input[type="checkbox"]'));
        const checkboxesChecked = checkboxes.filter((cb: any) => cb.checked);
        return {
            totalCheckboxes: checkboxes.length,
            checkboxesMarcados: checkboxesChecked.length,
            idsCheckboxes: checkboxes.map((cb: any) => cb.id),
            idsCheckboxesMarcados: checkboxesChecked.map((cb: any) => cb.id)
        };
    });
    console.log(`[DEBUG] abrirProcessoPorNome: Dados carregados:`, JSON.stringify(dadosCarregados));
}

/**
 * Criar processo completo com dados básicos, interagindo com a UI.
 * Esta função agora retorna o ID do processo criado para uso em testes subsequentes.
 */
export async function criarProcessoCompleto(
    page: Page,
    descricao: string,
    tipo: string,
    dataLimite: string,
    unidades: number[]
): Promise<{ processo: { codigo: number; descricao: string; } }> {
    await navegarParaCriacaoProcesso(page);
    await preencherFormularioProcesso(page, descricao, tipo, dataLimite);
    await selecionarUnidadesPorId(page, unidades);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
    await page.waitForURL(/\/processo\/\d+$/);
    const url = page.url();
    const id = Number(url.split('/').pop());
    return {processo: {codigo: id, descricao}};
}

/**
 * Tentar salvar processo sem preencher dados
 */
export async function tentarSalvarProcessoVazio(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Criar processo apenas com descrição e tipo (sem unidades)
 */
export async function criarProcessoSemUnidades(page: Page, descricao: string, tipo: string): Promise<void> {
    await preencherFormularioProcesso(page, descricao, tipo);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Navegar para processo específico na tabela
 */
export async function navegarParaProcessoNaTabela(page: Page, descricaoProcesso: string): Promise<void> {
    await page.getByText(descricaoProcesso).click();
}

/**
 * Editar descrição de processo existente
 */
export async function editarDescricaoProcesso(page: Page, novaDescricao: string): Promise<void> {
    await page.fill(SELETORES.CAMPO_DESCRICAO, novaDescricao);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Criar processo de mapeamento completo (versão específica para CDU-04)
 */
export async function criarProcessoMapeamentoCompleto(page: Page, descricao: string, dataLimite: string): Promise<void> {
    await page.getByLabel('Descrição').fill(descricao);
    await page.getByLabel('Tipo').selectOption('Mapeamento');
    await page.getByLabel('Data limite').fill(dataLimite);

    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    primeiroCheckbox.check();
}

/**
 * Clica no botão de finalização do processo.
 */
export async function clicarBotaoFinalizarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO}).click();
}

/**
 * Abre o modal de finalização do processo.
 */
export async function abrirModalFinalizacaoProcesso(page: Page): Promise<void> {
    await clicarBotaoFinalizarProcesso(page);
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Confirma a finalização no modal aberto.
 */
export async function confirmarFinalizacaoNoModal(page: Page): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Finaliza um processo com confirmação
 */
export async function finalizarProcesso(page: Page): Promise<void> {
    await abrirModalFinalizacaoProcesso(page);
    await confirmarFinalizacaoNoModal(page);
}

/**
 * Disponibiliza cadastro com confirmação
 */
export async function disponibilizarCadastro(page: Page): Promise<void> {
    await page.click(`button:has-text("${TEXTOS.DISPONIBILIZAR}")`);
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
    await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Abre o modal para devolver um cadastro para ajustes.
 */
export async function abrirModalDevolucao(page: Page): Promise<void> {
    await page.getByRole('button', {name: 'Devolver para ajustes'}).click();
}

/**
 * Devolve um cadastro para ajustes, preenchendo a observação se fornecida.
 */
export async function devolverParaAjustes(page: Page, observacao?: string): Promise<void> {
    await abrirModalDevolucao(page);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await preencherCampo([modal.getByLabel('Observação')], observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Orquestra o fluxo de devolução de um cadastro por um GESTOR.
 */
export async function devolverCadastro(page: Page, processo: {
    codigo: any;
    descricao: string
}, nomeUnidade: string, observacao: string): Promise<void> {
    const {loginComoGestor, navegarParaProcessoPorId} = await import("../navegacao");
    await loginComoGestor(page);
    await navegarParaProcessoPorId(page, processo.codigo);
    const {acessarAnaliseRevisaoComoGestor} = await import("../navegacao");
    await acessarAnaliseRevisaoComoGestor(page, processo.codigo, nomeUnidade);
    await devolverParaAjustes(page, observacao);
}


/**
 * Aceita um cadastro, preenchendo a observação se fornecida.
 */
export async function aceitarCadastro(page: Page, observacao?: string): Promise<void> {
    await clicarElemento([page.getByRole('button', {name: 'Registrar aceite'}), page.getByRole('button', {name: TEXTOS.VALIDAR})]);

    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await preencherCampo([modal.getByLabel('Observação')], observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Registra o aceite da revisão do cadastro, preenchendo a observação se fornecida.
 */
export async function registrarAceiteRevisao(page: Page, observacao?: string): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);

    await page.getByRole('button', {name: TEXTOS.REGISTRAR_ACEITE}).click();
    await expect(modal).toBeVisible();

    // Tentar localizar título específico, caso não exista aceitamos qualquer heading que contenha 'aceit'
    if ((await modal.getByRole('heading', {name: TEXTOS.ACEITE_REVISAO_TITULO}).count()) > 0) {
        await expect(modal.getByRole('heading', {name: TEXTOS.ACEITE_REVISAO_TITULO})).toBeVisible();
    } else {
        await expect(modal.getByRole('heading', {name: /aceit(ar|e)?/i}).first()).toBeVisible();
    }

    if (observacao) {
        await preencherCampo([modal.getByLabel(/observa/i), modal.locator('textarea, input').first()], observacao);
    }

    await clicarElemento([
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
        modal.getByRole('button', {name: /aceitar|confirmar/i}),
    ]);
}

/**
 * Homologa um cadastro, preenchendo a observação se fornecida.
 */
export async function homologarCadastro(page: Page, observacao?: string): Promise<void> {
    await clicarElemento([page.getByRole('button', {name: TEXTOS.HOMOLOGAR}), page.getByRole('button', {name: TEXTOS.VALIDAR})]);

    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await preencherCampo([modal.getByLabel(/observa/i)], observacao);
    }

    await clicarElemento([
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
        modal.getByRole('button', {name: /aceitar|confirmar/i}),
    ]);
}

/**
 * Clica no botão de iniciar processo
 */
export async function clicarBotaoIniciarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
}

/**
 * Clica em um processo específico na tabela do painel
 */
export async function clicarProcessoNaTabela(page: Page, nomeProcesso: string): Promise<void> {
    await page.waitForSelector(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"]`); // Espera a tabela carregar
    const processo = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: nomeProcesso});
    await processo.click();
}

/**
 * Inicia um processo com confirmação
 */
export async function iniciarProcesso(page: Page): Promise<void> {
    await clicarBotaoIniciarProcesso(page);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remover processo com confirmação
 */
export async function removerProcessoComConfirmacao(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Cancelar remoção de processo
 */
export async function cancelarRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.getByRole('button', {name: TEXTOS.CANCELAR}).click();
}

/**
 * Confirmar inicialização de processo
 */
export async function confirmarInicializacaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remover processo com confirmação usando botão de danger no modal
 */
export async function removerProcessoConfirmandoNoModal(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.locator('.modal.show .btn-danger').click();
}

/**
 * Clica em uma unidade específica na tabela de detalhes do processo.
 */
export async function clicarUnidadeNaTabelaDetalhes(page: Page, nomeUnidade: string): Promise<void> {
    const unidadeRow = page.locator(SELETORES.LINHA_TABELA_ARVORE).filter({hasText: nomeUnidade}).first();
    await unidadeRow.click();
}

// noinspection JSUnusedGlobalSymbols
/**
 * Clica no botão de histórico de análise.
 */
export async function clicarBotaoHistoricoAnalise(page: Page): Promise<void> {
    await page.getByTestId(SELETORES.BTN_HISTORICO_ANALISE).click();
}

/**
 * Homologa a revisão de um cadastro, preenchendo a observação se fornecida.
 */
export async function homologarRevisaoCadastro(page: Page, observacao?: string): Promise<void> {
    await homologarCadastro(page, observacao);
}
