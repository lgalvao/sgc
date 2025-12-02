import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-03 - Manter Processo', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
    });

    test('Deve validar campos obrigatórios', async ({ page }) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Tenta salvar vazio -> Erro de descrição
        await page.getByTestId('btn-processo-salvar').click();
        await expect(page.getByText('Preencha a descrição')).toBeVisible();

        // Preenche descrição e tenta salvar -> Erro de unidade
        await page.getByTestId('inp-processo-descricao').fill('Descrição Teste');
        await page.getByTestId('btn-processo-salvar').click();
        await expect(page.getByText('Pelo menos uma unidade participante elegível deve ser incluída.')).toBeVisible();
    });

    test('Deve editar um processo existente', async ({ page }) => {
        const descricaoOriginal = `Processo para Edição - ${Date.now()}`;
        // Cria um processo inicial
        await criarProcesso(page, {
            descricao: descricaoOriginal,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11'
        });

        // Clica na linha do processo criado para editar
        await page.getByText(descricaoOriginal).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Verifica que os dados foram carregados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoOriginal);
        await expect(page.getByTestId('chk-arvore-unidade-ASSESSORIA_11')).toBeChecked();

        // Modifica o processo
        const novaDescricao = descricaoOriginal + ' (Editado)';
        await page.getByTestId('inp-processo-descricao').fill(novaDescricao);

        await page.getByTestId('btn-processo-salvar').click();

        // Verifica redirecionamento e mensagem (se houver toast, mas aqui verificamos a tabela)
        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(novaDescricao)).toBeVisible();
        // Use exact: true to avoid matching the new description which contains the old one
        await expect(page.getByText(descricaoOriginal, { exact: true })).not.toBeVisible();
    });

    test('Deve remover um processo', async ({ page }) => {
        const descricao = `Processo para Remoção - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11'
        });

        await page.getByText(descricao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Clica em Remover
        await page.getByTestId('btn-processo-remover').click();

        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeVisible();

        const btnsRemover = page.getByRole('button', { name: 'Remover' });
        await expect(btnsRemover).toHaveCount(2);
        await btnsRemover.last().click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByTestId('tbl-processos').getByText(descricao)).not.toBeVisible();
    });
});
