import { expect, test } from '@playwright/test';
import { autenticar, login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-07 - Detalhar subprocesso', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-07 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';

    test.beforeAll(async ({ browser }) => {
        const page = await browser.newPage();
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descricaoProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        await page.getByRole('row', { name: descricaoProcesso }).click();
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await page.close();
    });

    test('Deve exibir detalhes do subprocesso', async ({ page }) => {
        await page.goto('/login');
        await autenticar(page, USUARIO_CHEFE, 'senha');

        await page.getByText(descricaoProcesso).click();

        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        // 2.1. Seção Dados da Unidade
        await expect(page.getByRole('heading', { name: UNIDADE_ALVO })).toBeVisible();
        await expect(page.getByText('Aguardando cadastro')).toBeVisible();

        // 2.2. Seção Movimentações
        await expect(page.getByText('Movimentações')).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();

        // 2.3. Seção Elementos do processo
        const cardAtividades = page.getByTestId('atividades-card');
        await expect(cardAtividades).toBeVisible();
        await expect(cardAtividades).toBeEnabled();

        const cardMapa = page.getByTestId('mapa-card-vis');
        await expect(cardMapa).toBeVisible();
    });
});
