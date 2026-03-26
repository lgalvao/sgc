import {expect, type Page} from '@playwright/test';
import {verificarPaginaPainel, verificarToast} from './helpers-navegacao.js';
import {TEXTOS} from '../../frontend/src/constants/textos.js';


export async function navegarParaAtividades(page: Page) {
    const card = page.getByTestId('card-subprocesso-atividades');
    await expect(card).toBeVisible();
    await card.click();
    await page.waitForURL(/\/cadastro$/);

    await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();
    await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
}

export async function navegarParaAtividadesVisualizacao(page: Page) {
    const card = page.getByTestId('card-subprocesso-atividades-vis');
    await expect(card).toBeVisible();
    await card.click();
    await page.waitForURL(/\/vis-cadastro$/);
    await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO})).toBeVisible();
}


export async function adicionarAtividade(page: Page, descricao: string) {
    await page.getByTestId('inp-nova-atividade').fill(descricao);
    await expect(page.getByTestId('btn-adicionar-atividade')).toBeEnabled();
    await page.getByTestId('btn-adicionar-atividade').click();
    await expect(page.getByText(descricao, {exact: true})).toBeVisible();
}

export async function adicionarConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoDescricao: string) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
    await card.getByTestId('inp-novo-conhecimento').fill(conhecimentoDescricao);

    const responsePromise = page.waitForResponse(resp => resp.url().includes('/conhecimentos') && resp.status() === 201);
    await card.getByTestId('btn-adicionar-conhecimento').click();
    await responsePromise;

    await expect(card.getByText(conhecimentoDescricao)).toBeVisible();
}

export async function editarAtividade(page: Page, descricaoAtual: string | RegExp, novaDescricao: string) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricaoAtual)});
    const editButton = card.getByTestId('btn-editar-atividade');
    await editButton.click();

    const input = page.getByTestId('inp-editar-atividade');
    await input.waitFor({ state: 'visible' });
    await input.fill(novaDescricao);
    
    await page.getByTestId('btn-salvar-edicao-atividade').click();
    await expect(page.getByText(novaDescricao)).toBeVisible();
}

export async function cancelarEdicaoAtividade(page: Page, descricaoAtual: string | RegExp, textoCancelado: string) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricaoAtual)});
    const editButton = card.getByTestId('btn-editar-atividade');

    await editButton.click();

    const input = page.getByTestId('inp-editar-atividade');
    await input.waitFor({ state: 'visible' });
    await input.fill(textoCancelado);
    
    await page.getByTestId('btn-cancelar-edicao-atividade').click();
    await expect(page.getByText(descricaoAtual)).toBeVisible();
}

export async function removerAtividade(page: Page, descricao: string | RegExp) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricao)});
    const row = card.getByTestId('cad-atividades__hover-row');
    const btnRemover = card.getByTestId('btn-remover-atividade');

    await row.hover();
    await expect(btnRemover).toBeVisible();
    await btnRemover.click();

    const dialog = page.getByRole('dialog');
    await expect(dialog.getByText(TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TEXTO)).toBeVisible();
    await page.getByTestId('btn-modal-confirmacao-confirmar').click();
    await expect(page.getByText(descricao)).toBeHidden();
}

export async function editarConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoAtual: string, novoConhecimento: string) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoAtual});
    const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');

    await linhaConhecimento.hover();
    await expect(btnEditar).toBeVisible();
    await btnEditar.click();

    const input = card.getByTestId('inp-editar-conhecimento');
    await input.waitFor({ state: 'visible' });
    await input.fill(novoConhecimento);
    await card.getByTestId('btn-salvar-edicao-conhecimento').click();

    await expect(card.getByText(novoConhecimento)).toBeVisible();
}

export async function cancelarEdicaoConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoAtual: string, textoCancelado: string) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoAtual});
    const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');

    await linhaConhecimento.hover();
    await expect(btnEditar).toBeVisible();
    await btnEditar.click();

    const input = card.getByTestId('inp-editar-conhecimento');
    await input.waitFor({ state: 'visible' });
    await input.fill(textoCancelado);
    
    await card.getByTestId('btn-cancelar-edicao-conhecimento').click();
    await expect(card.getByText(conhecimentoAtual)).toBeVisible();
}

export async function removerConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimento: string) {
    const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimento});
    const btnRemover = linhaConhecimento.getByTestId('btn-remover-conhecimento');

    await linhaConhecimento.hover();
    await expect(btnRemover).toBeVisible();
    await btnRemover.click();

    const dialog = page.getByRole('dialog');
    await expect(dialog.getByText(TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TEXTO)).toBeVisible();
    await page.getByTestId('btn-modal-confirmacao-confirmar').click();
    await expect(card.getByText(conhecimento)).toBeHidden();
}

export async function disponibilizarCadastro(page: Page) {
    const botao = page.getByTestId('btn-cad-atividades-disponibilizar');
    if (await botao.isDisabled()) {
        const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
        if (await checkboxSemMudancas.count() > 0) {
            await expect(checkboxSemMudancas).toBeVisible();
            await expect(checkboxSemMudancas).toBeEnabled();
            await checkboxSemMudancas.check();
            await expect(checkboxSemMudancas).toBeChecked();
        }
    }

    await expect(botao).toBeEnabled();
    await botao.click();

    const modal = page.getByRole('dialog');
    await expect(modal).toBeVisible();
    
    const btnConfirmar = page.getByTestId('btn-confirmar-disponibilizacao');
    await expect(btnConfirmar).toBeEnabled();
    await btnConfirmar.click();
    await verificarToast(page, /disponibilizada?|Disponibilizado/i);
    await verificarPaginaPainel(page);
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('cad-atividades__txt-badge-situacao')).toHaveText(new RegExp(situacao, 'i'));
}

export async function verificarBotaoDisponibilizar(page: Page, habilitado: boolean) {
    const botao = page.getByTestId('btn-cad-atividades-disponibilizar');
    await expect(botao).toBeVisible();
    if (habilitado) {
        await expect(botao).toBeEnabled();
    } else {
        await expect(botao).toBeDisabled();
    }
}

export async function verificarBotaoImpactoDropdown(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();
}

export async function verificarBotaoHistoricoAnalise(page: Page) {
    await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
}

export async function verificarBotaoImpactoDireto(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-visualizacao')).toBeVisible();
}

export async function verificarBotaoImpactoAusenteEdicao(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeHidden();
}

export async function verificarBotaoImpactoAusenteDireto(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-visualizacao')).toBeHidden();
}

export async function abrirModalImpactoEdicao(page: Page) {
    await page.getByTestId('cad-atividades__btn-impactos-mapa-edicao').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function abrirModalImpactoVisualizacao(page: Page) {
    await page.getByTestId('cad-atividades__btn-impactos-mapa-visualizacao').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function fecharModalImpacto(page: Page) {
    await page.getByTestId('btn-fechar-impacto').click();
    await expect(page.getByRole('dialog')).toBeHidden();
}

async function preencherFormularioImportacao(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    const modal = page.getByRole('dialog');
    await expect(modal.getByText(TEXTOS.atividades.MODAL_IMPORTAR_TITULO)).toBeVisible();

    const selectProcesso = modal.getByTestId('select-processo');
    const selectUnidade = modal.getByTestId('select-unidade');

    // 1. Aguardar o processo aparecer no select e selecionar
    await expect(selectProcesso.locator('option', { hasText: processoOrigemDescricao })).toBeAttached();
    
    const respostaUnidades = page.waitForResponse(r => 
        r.url().includes('/unidades-importacao')
    );
    await selectProcesso.selectOption({ label: processoOrigemDescricao });
    await respostaUnidades;

    // 2. Aguardar a unidade aparecer no select e selecionar
    await expect(selectUnidade).toBeEnabled();
    await expect(selectUnidade.locator('option', { hasText: unidadeOrigemSigla })).toBeAttached();

    const respostaAtividades = page.waitForResponse(r =>
        r.url().includes('/atividades-importacao')
    );
    await selectUnidade.selectOption({ label: unidadeOrigemSigla });
    await respostaAtividades;

    // 3. Aguardar as atividades aparecerem (checkboxes) ou o estado vazio
    const primeiroCheckbox = modal.locator('input[type="checkbox"]').first();
    const textoVazio = modal.getByText(TEXTOS.atividades.importacao.NENHUMA_ATIVIDADE);
    await expect(primeiroCheckbox.or(textoVazio)).toBeVisible();

    // 4. Selecionar as atividades solicitadas
    for (const desc of atividadesDescricoes) {
        const checkbox = modal.getByLabel(desc, { exact: true }).first();
        await expect(checkbox).toBeVisible();
        await checkbox.check();
    }
}

export async function selecionarAtividadesParaImportacao(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    await Promise.all([
        page.waitForResponse(r => r.url().includes('/para-importacao')),
        page.getByTestId('btn-cad-atividades-importar').click()
    ]);
    await preencherFormularioImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);
}

export async function selecionarAtividadesParaImportacaoVazia(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    await Promise.all([
        page.waitForResponse(r => r.url().includes('/para-importacao')),
        page.getByTestId('btn-empty-state-importar').click()
    ]);
    await preencherFormularioImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);
}

export async function importarAtividades(page: Page,
                                         processoOrigemDescricao: string,
                                         unidadeOrigemSigla: string,
                                         atividadesDescricoes: string[]) {

    await selecionarAtividadesParaImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);

    const modal = page.getByRole('dialog');
    await modal.getByTestId('btn-importar').click();

    await expect(modal).toBeHidden();

    for (const desc of atividadesDescricoes) {
        await expect(page.getByText(desc, { exact: true }).first()).toBeVisible();
    }
}

export async function importarAtividadesVazia(page: Page,
                                              processoOrigemDescricao: string,
                                              unidadeOrigemSigla: string,
                                              atividadesDescricoes: string[]) {

    await selecionarAtividadesParaImportacaoVazia(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);

    const modal = page.getByRole('dialog');
    await modal.getByTestId('btn-importar').click();

    await expect(modal).toBeHidden();

    for (const desc of atividadesDescricoes) {
        await expect(page.getByText(desc, { exact: true }).first()).toBeVisible();
    }
}

export async function importarAtividadesComAvisoDuplicidade(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    await selecionarAtividadesParaImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);

    const modal = page.getByRole('dialog');
    await modal.getByTestId('btn-importar').click();

    await expect(modal).toBeHidden();
}

async function realizarVerificacaoOpcoesImportacao(
    page: Page,
    opcoesEsperadas: Array<{ processo: string; unidades: string[] }>
) {
    const modal = page.getByRole('dialog');
    await expect(modal.getByText(TEXTOS.atividades.MODAL_IMPORTAR_TITULO)).toBeVisible();

    const selectProcesso = modal.getByTestId('select-processo');
    const selectUnidade = modal.getByTestId('select-unidade');

    for (const opcao of opcoesEsperadas) {
        // Aguardar cada processo aparecer como opção — auto-wait sem poll
        await expect(selectProcesso.locator('option', { hasText: opcao.processo })).toBeAttached();
        
        const respostaUnidades = page.waitForResponse(r => 
            r.url().includes('/unidades-importacao')
        );
        await selectProcesso.selectOption({ label: opcao.processo });
        await respostaUnidades;

        await expect(selectUnidade).toBeEnabled();

        for (const unidade of opcao.unidades) {
            await expect(selectUnidade.locator('option', { hasText: unidade })).toBeAttached();
        }
    }

    await modal.getByTestId('importar-atividades-modal__btn-modal-cancelar').click();
}

export async function verificarOpcoesImportacao(
    page: Page,
    opcoesEsperadas: Array<{ processo: string; unidades: string[] }>
) {
    const respostaProcessos = page.waitForResponse(r => r.url().includes('/para-importacao'));
    await page.getByTestId('btn-cad-atividades-importar').click();
    await respostaProcessos;
    await realizarVerificacaoOpcoesImportacao(page, opcoesEsperadas);
}

export async function verificarOpcoesImportacaoVazia(
    page: Page,
    opcoesEsperadas: Array<{ processo: string; unidades: string[] }>
) {
    const respostaProcessos = page.waitForResponse(r => r.url().includes('/para-importacao'));
    await page.getByTestId('btn-empty-state-importar').click();
    await respostaProcessos;
    await realizarVerificacaoOpcoesImportacao(page, opcoesEsperadas);
}

