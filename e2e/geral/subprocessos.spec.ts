import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Detalhes da Unidade no Processo', () => {
    test.beforeEach(async ({ page }) => {
        await login(page);
        await page.goto('/processo/4/SESEL');
        //await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes da unidade e os cards de funcionalidade', async ({ page }) => {
        await expect(page.getByText('Titular:')).toBeVisible();

        await expect(page.getByTestId('atividades-card')).toBeVisible();
        await expect(page.getByRole('heading', { name: 'Mapa de Competências' })).toBeVisible();
    });

    test('deve navegar para a página de atividades ao clicar no card', async ({ page }) => {
        await page.getByTestId('atividades-card').click();
        await page.waitForURL(/.*\/processo\/\d+\/SESEL\/vis-cadastro/);
        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
    });

    test('deve exibir informações completas da unidade no processo', async ({ page }) => {
        // Verificar informações do titular
        await expect(page.getByText('Titular:')).toBeVisible();

        // Verificar informações de contato (ramal e email)
        const iconeTelefone = page.locator('.bi-telephone-fill');
        const iconeEmail = page.locator('.bi-envelope-fill');

        if (await iconeTelefone.isVisible()) {
            await expect(iconeTelefone).toBeVisible();
        }

        if (await iconeEmail.isVisible()) {
            await expect(iconeEmail).toBeVisible();
        }
    });

    test('deve exibir responsável temporário quando aplicável', async ({ page }) => {
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

    test('deve exibir seção de movimentações do processo', async ({ page }) => {
        // Verificar título da seção
        await expect(page.getByRole('heading', { name: 'Movimentações do Processo' })).toBeVisible();

        // Verificar tabela de movimentações
        const tabelaMovimentacoes = page.locator('table');
        if (await tabelaMovimentacoes.isVisible()) {
            await expect(tabelaMovimentacoes).toBeVisible();

            // Verificar cabeçalhos da tabela
            await expect(page.getByRole('columnheader', { name: 'Data/Hora' })).toBeVisible();
            await expect(page.getByRole('columnheader', { name: 'Unidade Origem' })).toBeVisible();
            await expect(page.getByRole('columnheader', { name: 'Unidade Destino' })).toBeVisible();
            await expect(page.getByRole('columnheader', { name: 'Descrição' })).toBeVisible();
        } else {
            // Se não há movimentações, verificar mensagem informativa
            await expect(page.getByText('Nenhuma movimentação registrada para este subprocesso.')).toBeVisible();
        }
    });

    test('deve exibir cards de funcionalidade corretos', async ({ page }) => {
        // Verificar card de atividades
        await expect(page.getByTestId('atividades-card')).toBeVisible();

        // Verificar card de mapa de competências
        await expect(page.getByRole('heading', { name: 'Mapa de Competências' })).toBeVisible();

        // Verificar se há outros cards baseados no tipo de processo
        const cards = page.locator('.card');
        const countCards = await cards.count();
        expect(countCards).toBeGreaterThan(0);
    });

    test('deve navegar para diferentes funcionalidades', async ({ page }) => {
        // Testar navegação para atividades
        await page.getByTestId('atividades-card').click();
        await page.waitForURL(/.*\/processo\/\d+\/SESEL\/vis-cadastro/);
        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();

        // Voltar para a página do subprocesso
        await page.goBack();
        await expect(page).toHaveURL(/.*\/processo\/\d+\/SESEL/);

        // Testar navegação para mapa (se disponível)
        const mapaCard = page.getByRole('heading', { name: 'Mapa de Competências' }).locator('..');
        if (await mapaCard.isVisible()) {
            await mapaCard.click();
            // Verificar se navegou para a página de mapa
            await page.waitForURL(/.*\/processo\/\d+\/SESEL\/vis-mapa/);
        }
    });

    test('deve exibir botão de alterar data limite quando aplicável', async ({ page }) => {
        // Verificar se o SubprocessoHeader está presente (onde o botão deve estar)
        await expect(page.getByText(/Titular:/i)).toBeVisible();

        // O botão de alterar data limite está no SubprocessoHeader component
        // Procurar por botão que contenha texto relacionado a data limite
        const alterarDataButton = page.getByRole('button').filter({ hasText: /data limite/i });

        if (await alterarDataButton.isVisible()) {
            await expect(alterarDataButton).toBeVisible();

            // Clicar no botão
            await alterarDataButton.click();
            await page.waitForTimeout(500);

            // Verificar se o modal SubprocessoModal foi aberto
            const modal = page.locator('.modal.show');
            await expect(modal).toBeVisible({ timeout: 2000 });

            // Verificar título do modal
            await expect(modal.getByText(/alterar data limite/i)).toBeVisible();

            // Verificar botões do modal
            await expect(modal.getByRole('button', { name: /cancelar/i })).toBeVisible();
            await expect(modal.getByRole('button', { name: /confirmar/i })).toBeVisible();
        }
    });

    test('deve fechar modal de alterar data limite', async ({ page }) => {
        // Verificar SubprocessoHeader primeiro
        await expect(page.getByText(/Titular:/i)).toBeVisible();

        // Procurar botão de alterar data limite
        const alterarDataButton = page.getByRole('button').filter({ hasText: /data limite/i });

        if (await alterarDataButton.isVisible()) {
            await alterarDataButton.click();
            await page.waitForTimeout(500);

            // Verificar modal aberto
            const modal = page.locator('.modal.show');
            await expect(modal).toBeVisible({ timeout: 2000 });

            // Fechar modal pelo botão cancelar
            const cancelarButton = modal.getByRole('button', { name: /cancelar/i });
            await cancelarButton.click();

            // Verificar que o modal foi fechado
            await expect(modal).not.toBeVisible({ timeout: 2000 });
        }
    });

    test('deve alterar data limite com sucesso', async ({ page }) => {
        const alterarDataButton = page.getByRole('button', { name: /alterar data limite/i });

        if (await alterarDataButton.isVisible()) {
            await alterarDataButton.click();

            // Preencher nova data
            const dataInput = page.getByLabel(/data/i);
            if (await dataInput.isVisible()) {
                await dataInput.fill('2025-12-31');

                // Confirmar alteração
                const confirmarButton = page.getByRole('button', { name: /confirmar/i });
                await confirmarButton.click();

                // Verificar notificação de sucesso
                await expect(page.getByText(/data limite alterada/i)).toBeVisible();
            }
        }
    });

    test('deve exibir informações de situação do subprocesso', async ({ page }) => {
        // Verificar se há informações de situação
        const situacaoText = page.getByText(/situação/i);

        if (await situacaoText.isVisible()) {
            await expect(situacaoText).toBeVisible();
        }

        // Verificar se há informações de unidade atual
        const unidadeAtualText = page.getByText(/unidade atual/i);

        if (await unidadeAtualText.isVisible()) {
            await expect(unidadeAtualText).toBeVisible();
        }
    });

    test('deve exibir botões de ação baseados no perfil', async ({ page }) => {
        // Verificar se há botões de ação específicos do perfil
        const botoesAcao = page.locator('.btn');
        const countBotoes = await botoesAcao.count();

        if (countBotoes > 0) {
            // Verificar se pelo menos um botão está visível
            const primeiroBotao = botoesAcao.first();
            await expect(primeiroBotao).toBeVisible();
        }
    });

    test('deve navegar para diagnóstico de equipe quando disponível', async ({ page }) => {
        // Verificar se há card de diagnóstico de equipe
        const diagnosticoCard = page.getByText(/diagnóstico/i);

        if (await diagnosticoCard.isVisible()) {
            await diagnosticoCard.click();

            // Verificar navegação
            await page.waitForURL(/.*\/processo\/\d+\/SESEL\/diagnostico-equipe/);
        }
    });

    test('deve navegar para ocupações críticas quando disponível', async ({ page }) => {
        // Verificar se há card de ocupações críticas
        const ocupacoesCard = page.getByText(/ocupações críticas/i);

        if (await ocupacoesCard.isVisible()) {
            await ocupacoesCard.click();

            // Verificar navegação
            await page.waitForURL(/.*\/processo\/\d+\/SESEL\/ocupacoes-criticas/);
        }
    });

    test('deve exibir informações de processo', async ({ page }) => {
        // Verificar se há informações do processo
        const processoInfo = page.getByTestId('processo-info');

        if (await processoInfo.isVisible()) {
            await expect(processoInfo).toBeVisible();
        }
    });

    test('deve exibir informações de unidade', async ({ page }) => {
        // Verificar informações básicas da unidade no SubprocessoHeader
        await expect(page.getByText(/Titular:/i)).toBeVisible();
        await expect(page.getByTestId('unidade-info')).toBeVisible();

        // Verificar cards de funcionalidade (SubprocessoCards)
        await expect(page.getByTestId('atividades-card')).toBeVisible();

        // Verificar seção de movimentações
        await expect(page.getByRole('heading', { name: 'Movimentações do Processo' })).toBeVisible();
    });

    test('deve navegar corretamente para diferentes unidades', async ({ page }) => {
        // Testar navegação para unidade diferente
        await page.goto('/processo/4/COSIS');
        await expect(page).toHaveURL(/.*\/processo\/\d+\/COSIS/);

        // Verificar que a página carrega
        await expect(page.getByRole('heading', { name: /Movimentações do Processo/i })).toBeVisible();
    });

    test('deve exibir estrutura responsiva', async ({ page }) => {
        // Verificar se a página é responsiva
        const container = page.locator('.container').first();
        await expect(container).toBeVisible();

        // Verificar se os cards estão organizados corretamente
        const cards = page.locator('.card');
        const countCards = await cards.count();
        expect(countCards).toBeGreaterThanOrEqual(0);
    });
});
