import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import type {Page} from '@playwright/test';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

test.describe('CDU-03 - Manter Processo', () => {

    test('Deve validar campos obrigatórios', async ({page, autenticadoComoAdmin}: {page: Page, autenticadoComoAdmin: void}) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Verifica que botões estão desativados quando formulário está vazio
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();
        await expect(page.getByTestId('btn-processo-iniciar')).toBeDisabled();

        // Preenche descrição - botões ainda devem estar desativados (falta data e unidade)
        await page.getByTestId('inp-processo-descricao').fill('Descrição Teste');
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

        // Preenche data limite - botões ainda devem estar desativados (falta unidade)
        const dataLimite = new Date();
        dataLimite.setDate(dataLimite.getDate() + 30);
        await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

        // Seleciona tipo - botões ainda devem estar desativados (falta unidade)
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

        // Seleciona unidade - agora botões devem estar habilitados
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
        await expect(page.getByTestId('btn-processo-salvar')).toBeEnabled();
        await expect(page.getByTestId('btn-processo-iniciar')).toBeEnabled();
    });

    test('Deve editar um processo existente', async ({page, autenticadoComoAdmin, cleanupAutomatico}: {page: Page, autenticadoComoAdmin: void, cleanupAutomatico: ReturnType<typeof useProcessoCleanup>}) => {
        const descricaoOriginal = `Processo para Edição - ${Date.now()}`;
        // Cria um processo inicial
        await criarProcesso(page, {
            descricao: descricaoOriginal,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1']
        });

        // Capturar ID do processo para cleanup (caso o teste falhe antes de remover)
        await page.getByTestId('tbl-processos').getByText(descricaoOriginal).first().click();
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        const url = new URL(page.url());
        const processoId = Number.parseInt(url.searchParams.get('codProcesso') || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        // Verifica que os dados foram carregados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoOriginal);

        // Modifica o processo
        const novaDescricao = descricaoOriginal + ' (Editado)';
        await page.getByTestId('inp-processo-descricao').fill(novaDescricao);
        await page.getByTestId('btn-processo-salvar').click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(novaDescricao)).toBeVisible();

        // Para manter o banco consistente para os próximos testes sequenciais,
        // vamos remover este processo agora que a edição foi validada.
        await page.getByTestId('tbl-processos').getByText(novaDescricao).first().click();
        await page.getByTestId('btn-processo-remover').click();
        await page.getByRole('dialog').getByRole('button', {name: 'Remover'}).click();
        await expect(page).toHaveURL(/\/painel/);
    });

    test('Deve remover um processo', async ({page, autenticadoComoAdmin}: {page: Page, autenticadoComoAdmin: void}) => {
        const descricao = `Processo para Remoção - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_21',
            expandir: ['SECRETARIA_2']
        });

        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await page.getByTestId('btn-processo-remover').click();
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeVisible();

        await page.getByRole('dialog').getByRole('button', {name: 'Remover'}).click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByTestId('tbl-processos').getByText(descricao)).not.toBeVisible();
    });

    test('Deve validar regras de seleção em cascata na árvore de unidades', async ({page, autenticadoComoAdmin}: {page: Page, autenticadoComoAdmin: void}) => {
        await page.getByTestId('btn-painel-criar-processo').click();

        // Seleciona tipo para carregar a árvore
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        
        const btnExpandSecretaria = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
        await expect(btnExpandSecretaria).toBeVisible();
        await btnExpandSecretaria.click();
        
        const btnExpandCoord = page.getByTestId('btn-arvore-expand-COORD_11');
        await expect(btnExpandCoord).toBeVisible();
        await btnExpandCoord.click();

        // 1. Selecionar pai deve selecionar todos os filhos elegíveis
        const chkCoord = page.getByTestId('chk-arvore-unidade-COORD_11');
        await expect(chkCoord).toBeVisible();
        await chkCoord.click();
        
        // Locators flexíveis para o input interno
        const input111 = page.getByTestId('chk-arvore-unidade-SECAO_111').locator('input').or(page.getByTestId('chk-arvore-unidade-SECAO_111'));
        const input112 = page.getByTestId('chk-arvore-unidade-SECAO_112').locator('input').or(page.getByTestId('chk-arvore-unidade-SECAO_112'));
        const input113 = page.getByTestId('chk-arvore-unidade-SECAO_113').locator('input').or(page.getByTestId('chk-arvore-unidade-SECAO_113'));

        await expect(input111).toBeChecked();
        await expect(input112).toBeChecked();
        await expect(input113).toBeChecked();

        // 2. Desmarcar um filho deve deixar o pai em estado indeterminado
        await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
        const inputCoord = chkCoord.locator('input').or(chkCoord);
        await expect(inputCoord).toHaveJSProperty('indeterminate', true);

        // 3. Desmarcar todos os filhos deve desmarcar o pai
        await page.getByTestId('chk-arvore-unidade-SECAO_112').click();
        await page.getByTestId('chk-arvore-unidade-SECAO_113').click();
        await expect(inputCoord).not.toBeChecked();
    });

    test('Deve validar restrições de unidades sem mapa para REVISAO e DIAGNOSTICO', async ({page, autenticadoComoAdmin}: {page: Page, autenticadoComoAdmin: void}) => {
        await page.getByTestId('btn-painel-criar-processo').click();

        // Seleciona tipo REVISÃO
        await page.getByTestId('sel-processo-tipo').selectOption('REVISAO');
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        
        const btnExpandSecretaria = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
        await expect(btnExpandSecretaria).toBeVisible();
        await btnExpandSecretaria.click();

        // ASSESSORIA_11 não tem mapa vigente -> deve estar desabilitada
        const chkInvalida = page.getByTestId('chk-arvore-unidade-ASSESSORIA_11');
        await expect(chkInvalida).toBeVisible();
        // O locator do checkbox em si já deve suportar toBeDisabled
        await expect(chkInvalida.locator('input').or(chkInvalida)).toBeDisabled();

        // ASSESSORIA_12 TEM mapa vigente -> deve estar habilitada
        const chkValida = page.getByTestId('chk-arvore-unidade-ASSESSORIA_12');
        await expect(chkValida).toBeVisible();
        await expect(chkValida.locator('input').or(chkValida)).toBeEnabled();
    });

    test('Deve validar fluxos de cancelamento e mensagens de feedback', async ({page, autenticadoComoAdmin, cleanupAutomatico}: {page: Page, autenticadoComoAdmin: void, cleanupAutomatico: any}) => {
        const descricao = `Processo Feedback - ${Date.now()}`;

        // 1. Cancelar criação
        await page.getByTestId('btn-painel-criar-processo').click();
        await page.getByTestId('inp-processo-descricao').fill(descricao);
        await page.getByRole('button', {name: 'Cancelar'}).click();
        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(descricao)).not.toBeVisible();

        // 2. Criar e validar mensagem de sucesso
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1']
        });
        
        // Mensagem de sucesso deve estar em um toast
        await expect(page.getByText(/Processo criado/i).first()).toBeVisible();

        // Capturar ID para cleanup
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await page.waitForURL(/codProcesso=\d+/);
        const url = new URL(page.url());
        const id = url.searchParams.get('codProcesso');
        if (id) cleanupAutomatico.registrar(Number(id));

        // 3. Cancelar remoção
        await page.getByTestId('btn-processo-remover').click();
        await page.getByTestId('btn-modal-confirmacao-cancelar').click();
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).not.toBeVisible();

        // 4. Confirmar remoção e validar mensagem
        await page.getByTestId('btn-processo-remover').click();
        await page.getByRole('dialog').getByRole('button', {name: 'Remover'}).click();
        await expect(page.getByText(/removido/i).first()).toBeVisible();
    });
});
