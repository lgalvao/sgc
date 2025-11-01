import { vueTest as test } from './support/vue-specific-setup';
import { expect } from '@playwright/test';
import {
    loginComoAdmin,
    limparProcessos,
    criarProcesso,
    SELETORES
} from './helpers';
import {
    getProcessoById,
    getSubprocessosByProcessoId,
    getAlertas
} from './helpers/verificacoes/api-verifications';

test.describe('CDU-04: Iniciar processo (com preparação e verificação de backend)', () => {

    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    // afterEach is removed to avoid redundant cleanup, as beforeEach handles it.

    test('deve iniciar processo e verificar efeitos no backend', async ({ page }) => {
        const descricao = `Processo Iniciar ${Date.now()}`;
        const unidadesParticipantes = ['SGP', 'STIC'];
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, unidadesParticipantes);

        await page.goto(`/processo/cadastro?idProcesso=${processoId}`);

        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await modal.getByRole('button', { name: /confirmar/i }).click();
        await page.waitForURL(/\/painel/);

        // --- Verificações de Backend ---

        // 1. Verificar se o status do processo mudou para "EM_ANDAMENTO"
        const processo = await getProcessoById(page, processoId);
        expect(processo.situacao).toBe('EM_ANDAMENTO');

        // 2. Verificar se os subprocessos foram criados corretamente
        const subprocessos = await getSubprocessosByProcessoId(page, processoId);
        expect(subprocessos).toHaveLength(unidadesParticipantes.length);
        expect(subprocessos.map(s => s.unidade.sigla)).toEqual(expect.arrayContaining(unidadesParticipantes));
        subprocessos.forEach(sub => {
            expect(sub.situacao).toBe('CRIADO');
        });

        // 3. Verificar se um alerta foi gerado
        await page.goto('/painel'); // Refresh to ensure alerts are loaded
        const alertas = await getAlertas(page);
        const alertaDoProcesso = alertas.content.find(a => a.mensagem.includes(descricao));
        expect(alertaDoProcesso).toBeDefined();
        expect(alertaDoProcesso.mensagem).toContain('Um novo processo de mapeamento de competências foi iniciado');

        // --- Verificação de UI (mantida e corrigida) ---
        await page.getByText(descricao).first().click();
        await page.waitForURL(new RegExp(`/processo/${processoId}`));
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
    });

    test('deve cancelar iniciação e permanecer na tela com estado inalterado', async ({ page }) => {
        const descricao = `Processo Cancelar ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, ['STIC']);

        await page.goto(`/processo/cadastro?idProcesso=${processoId}`);

        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();

        await modal.getByRole('button', { name: /cancelar/i }).click();
        await expect(modal).not.toBeVisible();

        // Verificar que o processo ainda está no estado "CRIADO" via API
        const processo = await getProcessoById(page, processoId);
        expect(processo.situacao).toBe('CRIADO');
    });
});
