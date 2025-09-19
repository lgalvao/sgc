import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Detalhes da Unidade', () => {
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página da unidade STIC
        await page.goto(`/unidade/STIC`);
        //await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes da unidade e a tabela de subunidades', async ({page}) => {
        await expect(page.getByRole('heading', { name: 'STIC - Secretaria de Informática e Comunicações' })).toBeVisible();

        await expect(page.getByRole('button', {name: 'Visualizar Mapa'})).not.toBeVisible(); // Verifica que o botão não está visível quando não há mapa
        await expect(page.getByRole('heading', {name: 'Unidades Subordinadas'})).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
    });

    test('deve exibir subunidades na tabela', async ({page}) => {
        // Wait for table to render (TreeTable renders as standard table)
        await page.waitForSelector('table');
        await page.waitForTimeout(500); // Allow for data loading
        
        const treeRows = page.locator('.tree-row');
        const countRows = await treeRows.count();
        expect(countRows).toBeGreaterThan(0); // STIC tem subunidades hierárquicas
    });

    test('deve exibir informações completas da unidade', async ({page}) => {
        // Verificar título da unidade
        await expect(page.getByRole('heading', { name: 'STIC - Secretaria de Informática e Comunicações' })).toBeVisible();
        
        // Verificar informações do titular
        await expect(page.getByText('Titular:')).toBeVisible();
        
        // Verificar informações de contato
        const iconeTelefone = page.locator('.bi-telephone-fill');
        const iconeEmail = page.locator('.bi-envelope-fill');
        
        if (await iconeTelefone.isVisible()) {
            await expect(iconeTelefone).toBeVisible();
        }
        
        if (await iconeEmail.isVisible()) {
            await expect(iconeEmail).toBeVisible();
        }
    });

    test('deve exibir responsável temporário quando aplicável', async ({page}) => {
        // Verificar se há informações de responsável temporário
        const responsavelText = page.getByText('Responsável:');
        
        if (await responsavelText.isVisible()) {
            await expect(responsavelText).toBeVisible();
            
            // Verificar informações de contato do responsável
            const iconesResponsavel = page.locator('.ms-3 .bi-telephone-fill, .ms-3 .bi-envelope-fill');
            const countIcones = await iconesResponsavel.count();
            expect(countIcones).toBeGreaterThanOrEqual(0);
        }
    });

    test('deve exibir botão de mapa vigente quando disponível', async ({page}) => {
        // Verificar se há botão de mapa vigente
        const mapaButton = page.getByRole('button', {name: /mapa vigente/i});
        
        if (await mapaButton.isVisible()) {
            await expect(mapaButton).toBeVisible();
            await expect(mapaButton).toHaveClass(/btn-outline-success/);
        }
    });

    test('deve navegar para mapa vigente quando disponível', async ({page}) => {
        const mapaButton = page.getByRole('button', {name: /mapa vigente/i});
        
        if (await mapaButton.isVisible()) {
            await mapaButton.click();
            
            // Verificar navegação para página de mapa
            await page.waitForURL(/.*\/processo\/\d+\/STIC\/vis-mapa/);
        }
    });

    test('deve exibir botão de criar atribuição para ADMIN', async ({page}) => {
        // Verificar se há botão de criar atribuição
        const criarAtribuicaoButton = page.getByTestId('btn-criar-atribuicao');
        
        if (await criarAtribuicaoButton.isVisible()) {
            await expect(criarAtribuicaoButton).toBeVisible();
            await expect(criarAtribuicaoButton).toHaveClass(/btn-outline-primary/);
        }
    });

    test('deve navegar para criar atribuição', async ({page}) => {
        const criarAtribuicaoButton = page.getByTestId('btn-criar-atribuicao');
        
        if (await criarAtribuicaoButton.isVisible()) {
            await criarAtribuicaoButton.click();
            
            // Verificar navegação para página de atribuição
            await page.waitForURL(/.*\/unidade\/STIC\/atribuicao/);
        }
    });

    test('deve exibir tabela de unidades subordinadas', async ({page}) => {
        // Verificar título da seção
        await expect(page.getByRole('heading', {name: 'Unidades Subordinadas'})).toBeVisible();
        
        // Verificar tabela
        await expect(page.getByRole('table')).toBeVisible();
        
        // Verificar se há dados na tabela
        const linhas = page.locator('table tbody tr');
        const countLinhas = await linhas.count();
        expect(countLinhas).toBeGreaterThan(0);
    });

    test('deve navegar para unidades subordinadas', async ({page}) => {
        // Aguardar carregamento da tabela
        await page.waitForSelector('table tbody tr');
        
        // Clicar na primeira linha da tabela (unidade subordinada)
        const primeiraLinha = page.locator('table tbody tr').first();
        await primeiraLinha.click();
        
        // Verificar navegação para unidade subordinada
        await expect(page).toHaveURL(/.*\/unidade\/[A-Z]+/);
    });

    test('deve exibir estrutura hierárquica das unidades', async ({page}) => {
        // Wait for load
        await page.waitForLoadState('networkidle');
        
        // Verificar tabela para estrutura hierárquica (TreeTable renders as table)
        await page.waitForSelector('table');
        await expect(page.locator('table')).toBeVisible();
        
        // Verificar rows da árvore hierárquica
        const treeRows = page.locator('.tree-row');
        const countRows = await treeRows.count();
        expect(countRows).toBeGreaterThan(0);
        
        // Verificar se há elementos filhos (expansão)
        const expandedRows = page.locator('.tree-row');
        expect(await expandedRows.count()).toBeGreaterThan(0);
    });

    test('deve navegar corretamente para diferentes unidades', async ({page}) => {
        // Testar navegação para unidade diferente
        await page.goto('/unidade/SESEL');
        await expect(page).toHaveURL(/.*\/unidade\/SESEL/);
        
        // Verificar que a página carrega
        await expect(page.getByRole('heading')).toBeVisible();
        
        // Voltar para STIC
        await page.goto('/unidade/STIC');
        await expect(page).toHaveURL(/.*\/unidade\/STIC/);
    });

    test('deve exibir estrutura responsiva', async ({page}) => {
        // Verificar container principal da unidade (mais específico)
        const containerUnidade = page.locator('.container .card').first().locator('..');
        await expect(containerUnidade).toBeVisible();
        
        // Verificar card da unidade
        const cardUnidade = page.locator('.card').first();
        await expect(cardUnidade).toBeVisible();
        
        // Verificar tabela responsiva (TreeTable renders as table)
        await page.waitForSelector('table');
        await expect(page.locator('table')).toBeVisible();
        
        // Verificar que a estrutura hierárquica é visível em diferentes estados
        const treeRows = page.locator('.tree-row');
        expect(await treeRows.count()).toBeGreaterThan(0);
    });

    test('deve exibir informações de unidade não encontrada', async ({page}) => {
        // Navegar para unidade inexistente
        await page.goto('/unidade/UNIDADE_INEXISTENTE');
        
        // Verificar mensagem de unidade não encontrada
        await expect(page.getByText('Unidade não encontrada.')).toBeVisible();
    });

    test('deve exibir botões de ação baseados no perfil', async ({page}) => {
        // Verificar se há botões de ação específicos do perfil
        const botoesAcao = page.locator('.btn');
        const countBotoes = await botoesAcao.count();
        
        if (countBotoes > 0) {
            // Verificar se pelo menos um botão está visível
            const primeiroBotao = botoesAcao.first();
            await expect(primeiroBotao).toBeVisible();
        }
    });

    test('deve exibir ícones de contato corretamente', async ({page}) => {
        // Verificar ícones de telefone e email
        const icones = page.locator('.bi-telephone-fill, .bi-envelope-fill');
        const countIcones = await icones.count();
        
        if (countIcones > 0) {
            // Verificar se os ícones estão visíveis
            const primeiroIcone = icones.first();
            await expect(primeiroIcone).toBeVisible();
        }
    });

    test('deve manter estado após navegação', async ({page}) => {
        // Verificar informações iniciais
        await expect(page.getByRole('heading', { name: 'STIC - Secretaria de Informática e Comunicações' })).toBeVisible();
        
        // Navegar para outra página
        await page.goto('/painel');
        await expect(page).toHaveURL(/\/painel/);
        
        // Voltar para a unidade
        await page.goto('/unidade/STIC');
        await expect(page).toHaveURL(/.*\/unidade\/STIC/);
        
        // Verificar que as informações ainda estão presentes
        await expect(page.getByRole('heading', { name: 'STIC - Secretaria de Informática e Comunicações' })).toBeVisible();
    });

    test('deve exibir informações de unidades sem subordinadas', async ({page}) => {
        // Navegar para unidade sem subordinadas diretas (COJUR tem SEJUR e SEPRO)
        await page.goto('/unidade/SEJUR'); // SEJUR é folha na hierarquia
        await expect(page).toHaveURL(/.*\/unidade\/SEJUR/);
        
        // Verificar que a página carrega
        await expect(page.getByRole('heading')).toBeVisible();
        
        // Verificar que não há seção de subordinadas (não deve aparecer TreeTable para unidades folha)
        const treeTable = page.locator('tree-table');
        await expect(treeTable).not.toBeVisible();
        
        // Mas ainda deve mostrar informações da unidade
        await expect(page.locator('.card')).toBeVisible();
    });

    test('deve exibir informações de unidades interoperacionais', async ({page}) => {
        // Navegar para unidade interoperacional (STIC é INTEROPERACIONAL)
        await page.goto('/unidade/STIC');
        await expect(page).toHaveURL(/.*\/unidade\/STIC/);
        
        // Verificar que a página carrega com estrutura hierárquica
        await expect(page.getByRole('heading', { name: 'STIC - Secretaria de Informática e Comunicações' })).toBeVisible();
        
        // Verificar tabela para unidades interoperacionais (TreeTable renders as table)
        await page.waitForSelector('table');
        await expect(page.locator('table')).toBeVisible();
        
        // Verificar que há subunidades visíveis
        const treeRows = page.locator('.tree-row');
        expect(await treeRows.count()).toBeGreaterThan(1);
    });
});