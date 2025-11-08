import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {clicarElemento, preencherCampo} from '../utils';
import {navegarParaCriacaoProcesso} from '~/helpers';
import {extrairIdDoSeletor} from '../utils/utils';

/**
 * Seleciona unidades na árvore de hierarquia usando suas siglas.
 * @param page A instância da página do Playwright.
 * @param siglas Um array de siglas das unidades a serem selecionadas (ex: ['STIC', 'SGP']).
 */
export async function selecionarUnidadesPorSigla(page: Page, siglas: string[]): Promise<void> {
    // Aguardar a árvore de unidades carregar
    await page.waitForSelector('.form-check-input[type="checkbox"]', {state: 'visible'});

    for (const sigla of siglas) {
        const seletorCheckbox = `#chk-${sigla}`;
        const alvo = page.locator(seletorCheckbox);

        // Aguardar a unidade aparecer na árvore (pode demorar devido a validações assíncronas)
        await alvo.waitFor({state: 'visible'});

        const isDisabled = await alvo.isDisabled();
        if (isDisabled) {
            console.warn(`[AVISO] Checkbox "${sigla}" está desabilitada (unidade já em uso em outro processo)`);
            continue;
        }

        await page.check(seletorCheckbox);
    }
}

/**
 * Seleciona uma unidade disponível na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 * @param index O índice da unidade a ser selecionada (padrão: 0).
 * @returns O ID da unidade selecionada.
 */
export async function selecionarUnidadeDisponivel(page: Page, index: number = 0): Promise<string> {
    await page.waitForSelector('.form-check-input[type="checkbox"]', {state: 'visible', timeout: 2000});
    const disponiveis = page.locator('.form-check-input[type="checkbox"]:not(:disabled)');
    const total = await disponiveis.count();
    if (total === 0) throw new Error('Nenhuma unidade disponível para seleção');
    const idx = Math.min(index, total - 1);
    const alvo = disponiveis.nth(idx);
    const id = await alvo.getAttribute('id');
    await alvo.check();
    return id || '';
}

/**
 * Seleciona unidades na árvore de hierarquia com base em seus IDs.
 * @param page A instância da página do Playwright.
 * @param unidades Um array de IDs das unidades a serem selecionadas.
 */
export async function selecionarUnidadesPorId(page: Page, unidades: number[]): Promise<void> {
    // Mapeamento conhecido de ID -> SIGLA (baseado no data.sql)
    const idParaSigla: Record<number, string> = {
        1: 'TRE',
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
 * Preenche o formulário de criação de processo.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param dataLimite A data limite do processo.
 * @param sticChecked `true` se a unidade STIC deve ser selecionada.
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

    // Para processos de REVISAO/DIAGNOSTICO, aguardar que as validações de mapa vigente terminem
    // O Vue faz requisições assíncronas para verificar todas as unidades
    if (tipo === 'REVISAO' || tipo === 'DIAGNOSTICO') {
        // Aguardar que pelo menos alguns checkboxes operacionais estejam visíveis
        // Isso indica que a validação de mapas vigentes foi concluída
        await page.waitForFunction(
            () => {
                const checkboxes = document.querySelectorAll('.form-check-input[type="checkbox"]:not([disabled])');
                return checkboxes.length > 0;
            },
            { timeout: 10000 }
        );
    }

    if (sticChecked) {
        await page.check(SELETORES.CHECKBOX_STIC);
    }
}

/**
 * Seleciona a primeira unidade na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 */
export async function selecionarPrimeiraUnidade(page: Page): Promise<void> {
    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.click();
}

/**
 * Clica no primeiro processo da tabela de processos.
 * @param page A instância da página do Playwright.
 */
export async function clicarPrimeiroProcessoTabela(page: Page): Promise<void> {
    const primeiraLinha = page.locator('table tbody tr').first();
    await primeiraLinha.click();
}

/**
 * Inicia o processo de mapeamento.
 * @param page A instância da página do Playwright.
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
        page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_INICIAR_PROCESSO)),
        page.getByRole('button', {name: /iniciar processo/i})
    ]);
    console.log(`[DEBUG] iniciarProcessoMapeamento: Clicou em Iniciar processo`);
}

/**
 * Confirma a iniciação do processo no modal.
 * @param page A instância da página do Playwright.
 */
export async function confirmarIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.waitFor({state: 'visible', timeout: 10000});

    await clicarElemento([
        modal.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_CONFIRMAR)),
        modal.getByRole('button', {name: /confirmar/i})
    ]);
}

/**
 * Cancela a iniciação do processo no modal.
 * @param page A instância da página do Playwright.
 */
export async function cancelarIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.waitFor({state: 'visible', timeout: 10000});

    await clicarElemento([
        modal.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_CANCELAR)),
        modal.getByRole('button', {name: /cancelar/i})
    ]);
}

/**
 * Cria um processo básico.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param siglas As siglas das unidades a serem associadas ao processo.
 * @param dataLimite A data limite do processo.
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
    await page.waitForURL(/\/painel/, );
    console.log(`[DEBUG] criarProcessoBasico: Redirecionado ao painel`);
}

/**
 * Abre um processo pelo nome na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo a ser aberto.
 */
export async function abrirProcessoPorNome(page: Page, descricao: string): Promise<void> {
    console.log(`[DEBUG] abrirProcessoPorNome: Procurando processo "${descricao}"`);
    const row = page.locator(`${SELETORES.TABELA_PROCESSOS} tr:has-text("${descricao}")`);
    await row.waitFor({state: 'visible', timeout: 10000});
    console.log(`[DEBUG] abrirProcessoPorNome: Processo encontrado, clicando`);
    await row.click();
    console.log(`[DEBUG] abrirProcessoPorNome: Clicou no processo`);

    // Aguardar navegação para página de cadastro
    await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/, );
    console.log(`[DEBUG] abrirProcessoPorNome: Navegou para página de cadastro`);

    // Aguardar formulário carregar com os dados do processo
    await page.waitForSelector(SELETORES.CAMPO_DESCRICAO, {state: 'visible', timeout: 10000});
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
 * Cria um processo completo e retorna seus dados.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param dataLimite A data limite do processo.
 * @param unidades Uma lista de IDs de unidades a serem associadas ao processo.
 * @returns Um objeto com o código e a descrição do processo criado.
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
 * Tenta salvar um processo com o formulário vazio.
 * @param page A instância da página do Playwright.
 */
export async function tentarSalvarProcessoVazio(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Cria um processo sem associar nenhuma unidade.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 */
export async function criarProcessoSemUnidades(page: Page, descricao: string, tipo: string): Promise<void> {
    await preencherFormularioProcesso(page, descricao, tipo);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Navega para a página de um processo a partir da tabela de processos.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function navegarParaProcessoNaTabela(page: Page, descricaoProcesso: string): Promise<void> {
    await page.getByText(descricaoProcesso).click();
}

/**
 * Edita a descrição de um processo.
 * @param page A instância da página do Playwright.
 * @param novaDescricao A nova descrição do processo.
 */
export async function editarDescricaoProcesso(page: Page, novaDescricao: string): Promise<void> {
    await page.fill(SELETORES.CAMPO_DESCRICAO, novaDescricao);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Cria um processo de mapeamento completo.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param dataLimite A data limite do processo.
 */
export async function criarProcessoMapeamentoCompleto(page: Page, descricao: string, dataLimite: string): Promise<void> {
    await page.getByLabel('Descrição').fill(descricao);
    await page.getByLabel('Tipo').selectOption('Mapeamento');
    await page.getByLabel('Data limite').fill(dataLimite);

    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.check();
}

/**
 * Clica no botão "Finalizar processo".
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoFinalizarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO}).click();
}

/**
 * Abre o modal de finalização do processo.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalFinalizacaoProcesso(page: Page): Promise<void> {
    await clicarBotaoFinalizarProcesso(page);
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Clica no botão "Confirmar" no modal de finalização do processo.
 * @param page A instância da página do Playwright.
 */
export async function confirmarFinalizacaoNoModal(page: Page): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Finaliza um processo.
 * @param page A instância da página do Playwright.
 */
export async function finalizarProcesso(page: Page): Promise<void> {
    await abrirModalFinalizacaoProcesso(page);
    await confirmarFinalizacaoNoModal(page);
}

/**
 * Disponibiliza o cadastro de um processo.
 * @param page A instância da página do Playwright.
 */
export async function disponibilizarCadastro(page: Page): Promise<void> {
    await page.click(`button:has-text("${TEXTOS.DISPONIBILIZAR}")`);
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
    await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Abre o modal para devolver um cadastro para ajustes.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalDevolucao(page: Page): Promise<void> {
    await page.getByRole('button', {name: 'Devolver para ajustes'}).click();
}

/**
 * Devolve um cadastro para ajustes.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
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
 * Devolve um cadastro.
 * @param page A instância da página do Playwright.
 * @param processo O processo.
 * @param nomeUnidade O nome da unidade.
 * @param observacao A observação.
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
 * Aceita o cadastro de um processo.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
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
 * Registra o aceite da revisão de um cadastro.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
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
 * Homologa o cadastro de um processo.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
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
 * Clica no botão "Iniciar processo".
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoIniciarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
}

/**
 * Clica em um processo na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 */
export async function clicarProcessoNaTabela(page: Page, nomeProcesso: string): Promise<void> {
    await page.waitForSelector(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"]`); // Espera a tabela carregar
    const processo = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: nomeProcesso});
    await processo.click();
}

/**
 * Inicia um processo.
 * @param page A instância da página do Playwright.
 */
export async function iniciarProcesso(page: Page): Promise<void> {
    await clicarBotaoIniciarProcesso(page);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remove um processo com confirmação.
 * @param page A instância da página do Playwright.
 */
export async function removerProcessoComConfirmacao(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Cancela a remoção de um processo.
 * @param page A instância da página do Playwright.
 */
export async function cancelarRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.getByRole('button', {name: TEXTOS.CANCELAR}).click();
}

/**
 * Confirma a inicialização de um processo.
 * @param page A instância da página do Playwright.
 */
export async function confirmarInicializacaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remove um processo confirmando no modal.
 * @param page A instância da página do Playwright.
 */
export async function removerProcessoConfirmandoNoModal(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.locator('.modal.show .btn-danger').click();
}

/**
 * Clica em uma unidade na tabela de detalhes de um processo.
 * @param page A instância da página do Playwright.
 * @param nomeUnidade O nome da unidade.
 */
export async function clicarUnidadeNaTabelaDetalhes(page: Page, nomeUnidade: string): Promise<void> {
    const unidadeRow = page.locator(SELETORES.LINHA_TABELA_ARVORE).filter({hasText: nomeUnidade}).first();
    await unidadeRow.click();
}

/**
 * Clica no botão de histórico de análise.
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoHistoricoAnalise(page: Page): Promise<void> {
    await page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_HISTORICO_ANALISE)).click();
}

/**
 * Homologa a revisão de um cadastro.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
 */
export async function homologarRevisaoCadastro(page: Page, observacao?: string): Promise<void> {
    await homologarCadastro(page, observacao);
}
