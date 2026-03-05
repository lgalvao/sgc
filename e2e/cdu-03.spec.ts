import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {esperarPaginaCadastroProcesso, esperarPaginaDetalhesProcesso, esperarPaginaPainel} from './helpers/helpers-navegacao.js';
import type {Page} from '@playwright/test';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

test.describe('CDU-03 - Manter Processo', () => {

    test('Deve validar campos obrigatórios e estados dos botões', async ({page, autenticadoComoAdmin}: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await esperarPaginaCadastroProcesso(page);

        const btnSalvar = page.getByTestId('btn-processo-salvar');
        const btnIniciar = page.getByTestId('btn-processo-iniciar');

        // Inicialmente desativados
        await expect(btnSalvar).toBeDisabled();
        await expect(btnIniciar).toBeDisabled();

        // Validação HTML5 / Atributos
        const descricaoInput = page.getByTestId('inp-processo-descricao');
        await expect(descricaoInput).toHaveAttribute('required', '');

        // Preenche descrição - ainda desativado
        await descricaoInput.fill('Descrição Teste');
        await expect(btnSalvar).toBeDisabled();

        // Preenche data limite - ainda desativado
        const dataLimite = new Date();
        dataLimite.setDate(dataLimite.getDate() + 30);
        await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
        await expect(btnSalvar).toBeDisabled();

        // Seleciona tipo - ainda desativado (falta unidade)
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(btnSalvar).toBeDisabled();

        // Seleciona unidade - agora deve habilitar
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
        
        await expect(btnSalvar).toBeEnabled();
        await expect(btnIniciar).toBeEnabled();

        // Se remover a descrição, deve desabilitar novamente
        await descricaoInput.fill('');
        await expect(btnSalvar).toBeDisabled();
        await expect(btnIniciar).toBeDisabled();
    });

    test('Deve permitir selecionar raiz interoperacional independentemente das subordinadas', async ({page, autenticadoComoAdmin}: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await esperarPaginaCadastroProcesso(page);
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        // SECRETARIA_1 é interoperacional
        const chkRaiz = page.getByTestId('chk-arvore-unidade-SECRETARIA_1');
        const inputRaiz = chkRaiz.locator('input').or(chkRaiz);
        
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        
        // 1. Selecionar a raiz marca a si mesma e as filhas
        await chkRaiz.click();
        await expect(inputRaiz).toBeChecked();
        const inputFilha = page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').locator('input').or(page.getByTestId('chk-arvore-unidade-ASSESSORIA_12'));
        await expect(inputFilha).toBeChecked();

        // 2. Desmarcar uma filha NÃO deve desmarcar a raiz (pois ela é Interoperacional e tem "vida própria")
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
        await expect(inputFilha).not.toBeChecked();
        await expect(inputRaiz).toBeChecked();
        
        // O estado não deve ser indeterminado, mas sim checked (true) conforme regra de interoperabilidade
        await expect(inputRaiz).not.toHaveJSProperty('indeterminate', true);
    });

    test('Deve editar um processo existente', async ({page, autenticadoComoAdmin, cleanupAutomatico}: {
        page: Page,
        autenticadoComoAdmin: void,
        cleanupAutomatico: ReturnType<typeof useProcessoCleanup>
    }) => {
        const descricaoOriginal = `Processo para Edição - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricaoOriginal,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1']
        });

        await page.getByTestId('tbl-processos').getByText(descricaoOriginal).first().click();
        await esperarPaginaCadastroProcesso(page);
        const id = await extrairProcessoId(page);
        if (id) cleanupAutomatico.registrar(id);

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoOriginal);
        await expect(page.getByTestId('sel-processo-tipo')).toBeDisabled();

        const novaDescricao = descricaoOriginal + ' (Editado)';
        await page.getByTestId('inp-processo-descricao').fill(novaDescricao);
        await page.getByTestId('btn-processo-salvar').click();

        await esperarPaginaPainel(page);
        await expect(page.getByText(/Processo alterado/i).first()).toBeVisible();
        await expect(page.getByText(novaDescricao)).toBeVisible();
    });

    test('Deve remover um processo', async ({page, autenticadoComoAdmin}: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        const descricao = `Processo para Remoção - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_21',
            expandir: ['SECRETARIA_2']
        });

        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await page.getByTestId('btn-processo-remover').click();
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeVisible();

        await page.getByRole('dialog').getByRole('button', {name: 'Remover'}).click();
        await esperarPaginaPainel(page);
        await expect(page.getByTestId('tbl-processos').getByText(descricao)).toBeHidden();
    });

    test('Deve validar regras de seleção em cascata na árvore de unidades', async ({page, autenticadoComoAdmin}: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await esperarPaginaCadastroProcesso(page);
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await page.getByTestId('btn-arvore-expand-COORD_11').click();

        // 1. Selecionar pai seleciona filhos
        const chkCoord = page.getByTestId('chk-arvore-unidade-COORD_11');
        await chkCoord.click();

        const input111 = page.getByTestId('chk-arvore-unidade-SECAO_111').locator('input').or(page.getByTestId('chk-arvore-unidade-SECAO_111'));
        const input112 = page.getByTestId('chk-arvore-unidade-SECAO_112').locator('input').or(page.getByTestId('chk-arvore-unidade-SECAO_112'));
        await expect(input111).toBeChecked();
        await expect(input112).toBeChecked();

        // 2. Desmarcar um filho deixa pai indeterminado
        await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
        const inputCoord = chkCoord.locator('input').or(chkCoord);
        await expect(inputCoord).toHaveJSProperty('indeterminate', true);

        // 3. Desmarcar todos os filhos desmarca o pai
        await page.getByTestId('chk-arvore-unidade-SECAO_112').click();
        await page.getByTestId('chk-arvore-unidade-SECAO_113').click();
        await expect(inputCoord).not.toBeChecked();

        // 4. Selecionar todos os filhos marca o pai automaticamente
        await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
        await page.getByTestId('chk-arvore-unidade-SECAO_112').click();
        await page.getByTestId('chk-arvore-unidade-SECAO_113').click();
        await expect(inputCoord).toBeChecked();
    });

    test('Deve avaliar unidades ocupadas por processos em andamento e restringi-las', async ({
                                                                                                 page,
                                                                                                 autenticadoComoAdmin,
                                                                                                 cleanupAutomatico
                                                                                             }: {
        page: Page,
        autenticadoComoAdmin: void,
        cleanupAutomatico: any
    }) => {
        const descricaoBase = `Ocupado - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricaoBase,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        await page.getByTestId('tbl-processos').getByText(descricaoBase).first().click();
        await esperarPaginaDetalhesProcesso(page);
        const id = await extrairProcessoId(page);
        cleanupAutomatico.registrar(id);
        await page.goto('/painel');

        await page.getByTestId('btn-painel-criar-processo').click();
        await esperarPaginaCadastroProcesso(page);
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();

        const chkOcupada = page.getByTestId('chk-arvore-unidade-ASSESSORIA_12');
        await expect(chkOcupada.locator('input').or(chkOcupada)).toBeDisabled();
    });

    test('Deve validar restrições de unidades sem mapa para REVISAO e DIAGNOSTICO', async ({
                                                                                               page,
                                                                                               autenticadoComoAdmin
                                                                                           }: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await esperarPaginaCadastroProcesso(page);
        await page.getByTestId('sel-processo-tipo').selectOption('REVISAO');
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();

        // ASSESSORIA_11 sem mapa -> desabilitada
        const chkInvalida = page.getByTestId('chk-arvore-unidade-ASSESSORIA_11');
        await expect(chkInvalida.locator('input').or(chkInvalida)).toBeDisabled();

        // ASSESSORIA_12 com mapa -> habilitada
        const chkValida = page.getByTestId('chk-arvore-unidade-ASSESSORIA_12');
        await expect(chkValida.locator('input').or(chkValida)).toBeEnabled();
    });

    test('Deve validar fluxos de cancelamento e mensagens de feedback', async ({
                                                                                   page,
                                                                                   autenticadoComoAdmin,
                                                                                   cleanupAutomatico
                                                                               }: {
        page: Page,
        autenticadoComoAdmin: void,
        cleanupAutomatico: any
    }) => {
        const descricao = `Feedback - ${Date.now()}`;

        // Cancelar criação
        await page.getByTestId('btn-painel-criar-processo').click();
        await page.getByTestId('inp-processo-descricao').fill(descricao);
        await page.getByRole('button', {name: 'Cancelar'}).click();
        await esperarPaginaPainel(page);
        await expect(page.getByText(descricao)).toBeHidden();

        // Criar e validar feedback
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1']
        });
        await expect(page.getByText(/Processo criado/i).first()).toBeVisible();

        // Cleanup registration
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaCadastroProcesso(page);
        const id = await extrairProcessoId(page);
        if (id) cleanupAutomatico.registrar(id);
        await page.goto('/painel');

        // Cancelar remoção
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await page.getByTestId('btn-processo-remover').click();
        await page.getByTestId('btn-modal-confirmacao-cancelar').click();
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeHidden();
    });

    test('Deve validar fluxo alternativo (Botão Iniciar invés de Salvar)', async ({
                                                                                      page,
                                                                                      autenticadoComoAdmin,
                                                                                      cleanupAutomatico
                                                                                  }: {
        page: Page,
        autenticadoComoAdmin: void,
        cleanupAutomatico: any
    }) => {
        const descricaoAlt = `Alternativo - ${Date.now()}`;

        await page.getByTestId('btn-painel-criar-processo').click();
        await page.getByTestId('inp-processo-descricao').fill(descricaoAlt);
        const dataLimite = new Date();
        dataLimite.setDate(dataLimite.getDate() + 30);
        await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await esperarPaginaPainel(page);
        await expect(page.getByText(/Processo iniciado/i).first()).toBeVisible();

        await page.getByTestId('tbl-processos').getByText(descricaoAlt).first().click();
        await esperarPaginaDetalhesProcesso(page);
        const idAlt = await extrairProcessoId(page);
        if (idAlt) cleanupAutomatico.registrar(idAlt);
        await expect(page).toHaveURL(/\/processo\/\d+$/);
    });
});
