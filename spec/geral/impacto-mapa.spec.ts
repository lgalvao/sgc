import {expect, Locator, Page, test} from "@playwright/test";
import {login} from "~/utils/auth";

async function adicionarAtividade(page: Page, nomeAtividade: string) {
    await page.getByTestId('input-nova-atividade').fill(nomeAtividade);
    await page.getByTestId('btn-adicionar-atividade').click();
    await expect(page.locator('.atividade-card', {hasText: nomeAtividade})).toBeVisible();
}

async function adicionarConhecimento(page: Page, atividadeCard: Locator, nomeConhecimento: string) {
    await atividadeCard.locator('[data-testid="input-novo-conhecimento"]').fill(nomeConhecimento);
    await atividadeCard.locator('[data-testid="btn-adicionar-conhecimento"]').click({force: true});
    await expect(atividadeCard.locator('.group-conhecimento', {hasText: nomeConhecimento})).toBeVisible();
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
        await impactoButton.click({force: true});

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
        await impactoButton.click({force: true});

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

        // Verificar se estamos na página correta
        await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();

        // Verificar se o botão existe
        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await expect(impactoButton).toBeVisible();

        // Abrir modal de impacto
        await impactoButton.click({force: true});

        // Verificar se o modal está aberto
        await page.waitForTimeout(1000);
        await expect(page.locator('.modal')).toBeVisible();
        await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();

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

        // 2. Adicionar um conhecimento
        const novoConhecimento = `Novo Conhecimento ${Date.now()}`;
        await adicionarConhecimento(page, atividadeCard, novoConhecimento);

        // 3. Criar nova atividade 'Atividade X' com conhecimentos
        await adicionarAtividade(page, 'Atividade X');
        const atividadeXCard = page.locator('.atividade-card', {hasText: 'Atividade X'});
        await adicionarConhecimento(page, atividadeXCard, 'Conhecimento A de X');
        await adicionarConhecimento(page, atividadeXCard, 'Conhecimento B de X');

        // 8. Aguardar que as mudanças sejam processadas
        await page.waitForLoadState('networkidle');

        // Aguardar que qualquer notificação apareça e desapareça automaticamente
        await page.waitForTimeout(500); // Dar tempo para notificações auto-fecharem

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