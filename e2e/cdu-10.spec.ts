import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';
import {
    navegarParaAtividades,
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    verificarSituacaoSubprocesso
} from './helpers/atividade-helpers';

test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro', () => {
    const timestamp = Date.now();
    const nomeProcesso = `Processo CDU-10 ${timestamp}`;
    const unidadeSigla = 'SECAO_111';
    const gestorUsuario = '222222'; // GESTOR_COORD_11
    const chefeUsuario = '333333'; // CHEFE_SECAO_111
    const senhaPadrao = 'senha';

    test.beforeAll(async ({ browser }) => {
        const context = await browser.newContext();
        const page = await context.newPage();
        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);

        await criarProcesso(page, {
            descricao: nomeProcesso,
            tipo: 'REVISAO',
            diasLimite: 5,
            unidade: unidadeSigla,
            expandir: ['COORD_11']
        });
        await context.close();
    });

    test('Deve validar atividades sem conhecimento e permitir disponibilizar após correção', async ({ page }) => {
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);

        // 1. Acessar o processo
        await page.getByText(nomeProcesso).click();
        await expect(page.getByTestId('txt-header-unidade')).toContainText(unidadeSigla);

        // 2. Ir para Atividades
        await navegarParaAtividades(page);

        // 3. Adicionar atividade sem conhecimento
        const atvSemConhecimento = `Atividade Sem Conhecimento ${timestamp}`;
        await adicionarAtividade(page, atvSemConhecimento);

        // 4. Tentar disponibilizar (deve falhar)
        // Clica no botão de disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Verifica que o modal de confirmação NÃO aparece (pois deve haver erro de validação)
        await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeHidden();

        // 5. Corrigir adicionando conhecimento
        const conhecimento = `Conhecimento ${timestamp}`;
        await adicionarConhecimento(page, atvSemConhecimento, conhecimento);

        // 6. Disponibilizar com sucesso
        await disponibilizarCadastro(page);

        // 7. Verificar sucesso (redirecionamento para painel e status atualizado)
        await expect(page).toHaveURL(/\/painel/);

        // Verifica o status do processo na visão do chefe
        await page.getByText(nomeProcesso).click();
        await verificarSituacaoSubprocesso(page, 'Revisão do cadastro disponibilizada');
    });
});
