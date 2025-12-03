import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_21'; // Unidade do Chefe (Janis Joplin)
    const CHEFE_UNIDADE = '777777';
    const SENHA_CHEFE = 'senha';

    test('Fluxo principal: adicionar, editar e remover atividades e conhecimentos', async ({ page }) => {
        // Aumentar timeout pois esse teste envolve criação de processo e varias interações
        test.setTimeout(60000);

        const timestamp = Date.now();
        const descricao = `Processo CDU-08 ${timestamp}`;

        // 1. ADMIN cria processo
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2'],
            iniciar: true
        });
        await page.getByTestId('btn-logout').click();

        // 2. CHEFE loga e acessa
        await login(page, CHEFE_UNIDADE, SENHA_CHEFE);
        await page.waitForLoadState('networkidle');

        await page.getByText(descricao).click();

        // Entrar em atividades
        await page.getByTestId('card-subprocesso-atividades').click();
        await expect(page.getByTestId('form-nova-atividade')).toBeVisible();

        // 3. Adicionar Atividade
        const atividade1 = `Atividade 1 ${timestamp}`;
        await page.getByTestId('inp-nova-atividade').fill(atividade1);
        await page.getByTestId('btn-adicionar-atividade').click();
        await expect(page.getByText(atividade1)).toBeVisible();

        // 4. Adicionar Conhecimento
        // Usar filter para pegar o card correto
        const cardAtiv = page.locator('.atividade-card').first();

        const conhecimento1 = `Conhecimento 1 ${timestamp}`;
        await cardAtiv.getByTestId('inp-novo-conhecimento').fill(conhecimento1);
        await cardAtiv.getByTestId('btn-adicionar-conhecimento').click();
        await expect(cardAtiv.getByText(conhecimento1)).toBeVisible();

        // 5. Editar Atividade
        const atividade1Editada = `${atividade1} EDITADA`;
        await cardAtiv.hover();
        await cardAtiv.getByTestId('btn-editar-atividade').click({ force: true });
        await cardAtiv.getByTestId('inp-editar-atividade').fill(atividade1Editada);
        await cardAtiv.getByTestId('btn-salvar-edicao-atividade').click();

        // Atualizar referencia do card pois o texto mudou
        const cardAtivEditada = page.locator('.atividade-card').first();
        await expect(cardAtivEditada).toContainText(atividade1Editada);

        // 6. Editar Conhecimento
        const conhecimento1Editado = `${conhecimento1} EDITADO`;
        // Encontrar a linha do conhecimento para fazer hover
        const linhaConhecimento = cardAtivEditada.locator('.group-conhecimento').first();
        await linhaConhecimento.hover();
        await linhaConhecimento.getByTestId('btn-editar-conhecimento').click({ force: true });
        await linhaConhecimento.getByTestId('inp-editar-conhecimento').fill(conhecimento1Editado);
        await linhaConhecimento.getByTestId('btn-salvar-edicao-conhecimento').click();
        await expect(cardAtivEditada.getByText(conhecimento1Editado)).toBeVisible();

        // 7. Remover Conhecimento
        const linhaConhecimentoEditado = cardAtivEditada.locator('.group-conhecimento').first();
        page.once('dialog', dialog => dialog.accept()); // Aceitar confirmação
        await linhaConhecimentoEditado.hover();
        await linhaConhecimentoEditado.getByTestId('btn-remover-conhecimento').click({ force: true });
        await expect(cardAtivEditada.getByText(conhecimento1Editado)).toBeHidden();

        // 8. Remover Atividade
        page.once('dialog', dialog => dialog.accept()); // Aceitar confirmação
        await cardAtivEditada.hover();
        await cardAtivEditada.getByTestId('btn-remover-atividade').click({ force: true });
        await expect(page.getByText(atividade1Editada)).toBeHidden();
    });
});
