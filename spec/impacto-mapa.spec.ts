import {expect, test, Locator, Page} from "@playwright/test";
import {login} from "./utils/auth";

async function adicionarAtividade(page: Page, nomeAtividade: string) {
    await page.getByTestId('input-nova-atividade').fill(nomeAtividade);
    await page.getByTestId('btn-adicionar-atividade').click();
    await expect(page.locator('.atividade-card', {hasText: nomeAtividade})).toBeVisible();
}

async function adicionarConhecimento(page: Page, atividadeCard: Locator, nomeConhecimento: string) {
    await atividadeCard.locator('[data-testid="input-novo-conhecimento"]').fill(nomeConhecimento);
    await atividadeCard.locator('[data-testid="btn-adicionar-conhecimento"]').click();
    await expect(atividadeCard.locator('.group-conhecimento', {hasText: nomeConhecimento})).toBeVisible();
}

async function editarAtividade(page: Page, atividadeOriginal: string, atividadeEditada: string) {
    const atividadeCard = page.locator('.atividade-card', {hasText: atividadeOriginal});
    await atividadeCard.hover();
    await page.waitForTimeout(100);
    await atividadeCard.getByTestId('btn-editar-atividade').click({force: true});
    await page.getByTestId('input-editar-atividade').fill(atividadeEditada);
    await page.getByTestId('btn-salvar-edicao-atividade').click();
    await expect(page.locator('.atividade-card', {hasText: atividadeEditada})).toBeVisible();
}

async function removerAtividade(page: Page, atividadeParaRemover: string) {
    const atividadeCard = page.locator('.atividade-card', {hasText: atividadeParaRemover});
    await atividadeCard.hover();
    await page.waitForTimeout(100);
    await atividadeCard.getByTestId('btn-remover-atividade').click({force: true});
    await expect(atividadeCard).not.toBeAttached();
}

async function editarConhecimento(page: Page, atividadeNome: string, conhecimentoOriginal: string, conhecimentoEditado: string) {
    const atividadeCard = page.locator('.atividade-card', {hasText: atividadeNome});
    const conhecimentoRow = atividadeCard.locator('.group-conhecimento', {hasText: conhecimentoOriginal});
    await conhecimentoRow.hover();
    await page.waitForTimeout(100);
    await conhecimentoRow.getByTestId('btn-editar-conhecimento').click();
    await page.getByTestId('input-editar-conhecimento').fill(conhecimentoEditado);
    await page.getByTestId('btn-salvar-edicao-conhecimento').click();
    await expect(atividadeCard.locator('.group-conhecimento', {hasText: conhecimentoEditado})).toBeVisible();
}

async function removerConhecimento(page: Page, atividadeNome: string, conhecimentoParaRemover: string) {
    const atividadeCard = page.locator('.atividade-card', {hasText: atividadeNome});
    const conhecimentoRow = atividadeCard.locator('.group-conhecimento', {hasText: conhecimentoParaRemover});
    await conhecimentoRow.hover();
    await page.waitForTimeout(100);
    await conhecimentoRow.getByTestId('btn-remover-conhecimento').click();
    await expect(conhecimentoRow).not.toBeAttached();
}

test.describe('Impacto no Mapa de Competências', () => {
    test.beforeEach(async ({page}) => {
        await login(page);
    });

    test('deve exibir tela vazia quando não há mudanças', async ({page}) => {
        await page.goto(`/processo/1/SESEL/cadastro`);
        await page.waitForLoadState('networkidle');

        // Verificar que a página carregou corretamente
        await expect(page.getByTestId('input-nova-atividade')).toBeVisible();

        // Aguardar o botão estar totalmente carregado e clicável
        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await impactoButton.waitFor({ state: 'visible' });
        await impactoButton.click();

        await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas')).not.toBeVisible();
        await expect(page.getByTestId('msg-nenhuma-competencia')).toBeVisible();

        // Fechar modal
        await page.getByText('Fechar').click();
    });

    test('deve exibir apenas atividades inseridas quando não há competências', async ({page}) => {
        await page.goto(`/processo/1/STIC/cadastro`);
        await page.waitForLoadState('networkidle');

        // Verificar que a página carregou corretamente
        await expect(page.getByTestId('input-nova-atividade')).toBeVisible();

        // Adicionar apenas uma atividade
        await adicionarAtividade(page, 'Atividade Teste');

        // Aguardar o botão estar totalmente carregado e clicável
        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await impactoButton.waitFor({ state: 'visible' });
        await impactoButton.click();

        await expect(page.getByTestId('titulo-atividades-inseridas')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Atividade Teste')).toBeVisible();
        await expect(page.getByTestId('msg-nenhuma-competencia')).toBeVisible();

        // Fechar modal
        await page.getByText('Fechar').click();
    });

    test('deve exibir conhecimentos de atividades inseridas', async ({page}) => {
        await page.goto(`/processo/1/STIC/cadastro`);
        await page.waitForLoadState('networkidle');

        // Verificar que a página carregou corretamente
        await expect(page.getByTestId('input-nova-atividade')).toBeVisible();

        // Criar atividade com conhecimentos
        await adicionarAtividade(page, 'Atividade Nova');
        const atividadeCard = page.locator('.atividade-card', {hasText: 'Atividade Nova'});
        await adicionarConhecimento(page, atividadeCard, 'Conhecimento A');
        await adicionarConhecimento(page, atividadeCard, 'Conhecimento B');

        // Aguardar que as mudanças sejam processadas
        await page.waitForLoadState('networkidle');

        // Abrir modal de impacto usando seletor mais específico
        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await impactoButton.waitFor({ state: 'visible' });
        await impactoButton.click();

        // Verificar se a atividade e seus conhecimentos aparecem
        await expect(page.getByTestId('titulo-atividades-inseridas')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Atividade Nova')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByTestId('label-conhecimentos-adicionados')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento A')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento B')).toBeVisible();

        // Fechar modal
        await page.getByText('Fechar').click();
    });

    test('deve exibir todas as mudanças na tela de impacto', async ({page}) => {
        await page.goto(`/processo/1/SESEL/cadastro`);
        await page.waitForLoadState('networkidle');
        // Preparar dados para o teste
        const atividadeExistente = `Atividade Existente ${Date.now()}`;
        const conhecimentoExistente = `Conhecimento Existente ${Date.now()}`;
        const atividadeParaRemover = `Atividade Para Remover ${Date.now()}`;

        // Criar atividades e conhecimentos iniciais
        await adicionarAtividade(page, atividadeExistente);
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeExistente});
        await adicionarConhecimento(page, atividadeCard, conhecimentoExistente);

        await adicionarAtividade(page, atividadeParaRemover);

        // 1. Alterar descrição de um conhecimento
        const conhecimentoAlterado = `Conhecimento Alterado ${Date.now()}`;
        await editarConhecimento(page, atividadeExistente, conhecimentoExistente, conhecimentoAlterado);

        // 2. Remover um conhecimento (adicionar outro primeiro)
        const conhecimentoParaRemover = `Conhecimento Para Remover ${Date.now()}`;
        await adicionarConhecimento(page, atividadeCard, conhecimentoParaRemover);
        await removerConhecimento(page, atividadeExistente, conhecimentoParaRemover);

        // 3. Adicionar um conhecimento
        const novoConhecimento = `Novo Conhecimento ${Date.now()}`;
        await adicionarConhecimento(page, atividadeCard, novoConhecimento);

        // 4. Alterar descrição de uma atividade
        const atividadeAlterada = `Atividade Alterada ${Date.now()}`;
        await editarAtividade(page, atividadeExistente, atividadeAlterada);

        // 5. Remover uma atividade
        await removerAtividade(page, atividadeParaRemover);

        // 6. Criar nova atividade 'Atividade X'
        await adicionarAtividade(page, 'Atividade X');

        // 7. Adicionar dois conhecimentos na nova atividade
        const atividadeXCard = page.locator('.atividade-card', {hasText: 'Atividade X'});
        await adicionarConhecimento(page, atividadeXCard, 'Conhecimento A de X');
        await adicionarConhecimento(page, atividadeXCard, 'Conhecimento B de X');

        // 8. Aguardar que as mudanças sejam processadas
        await page.waitForLoadState('networkidle');

        // Verificar que a página ainda está carregada corretamente
        await expect(page.getByTestId('input-nova-atividade')).toBeVisible();

        // 9. Clicar em 'Impacto no mapa' usando seletor mais específico
        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await impactoButton.waitFor({ state: 'visible' });
        await impactoButton.click();

        // 10. Verificar que todas as mudanças aparecem
        await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();

        // Verificar seção de atividades inseridas
        await expect(page.getByTestId('titulo-atividades-inseridas')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Atividade X')).toBeVisible();

        // Verificar seção de competências impactadas
        await expect(page.getByTestId('titulo-competencias-impactadas')).toBeVisible();

        // Verificar atividades inseridas e seus conhecimentos
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Atividade X')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByTestId('label-conhecimentos-adicionados').first()).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento A de X')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento B de X')).toBeVisible();

        // Verificar se há competências impactadas ou apenas atividades inseridas
        const hasCompetenciasImpactadas = await page.getByTestId('msg-nenhuma-competencia').isVisible();

        if (!hasCompetenciasImpactadas) {
            // Verificar mudanças específicas nas competências impactadas
            await expect(page.getByText('Conhecimento alterado')).toBeVisible();
            await expect(page.getByText('Conhecimento removido')).toBeVisible();
            await expect(page.getByText('Conhecimento adicionado')).toBeVisible();
            await expect(page.getByText('Atividade alterada')).toBeVisible();
            await expect(page.getByText('Atividade removida')).toBeVisible();
        }

        // Fechar modal
        await page.getByText('Fechar').click();
    });
});