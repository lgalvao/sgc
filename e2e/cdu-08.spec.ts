import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';
import {
    navegarParaAtividades,
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    removerAtividade
} from './helpers/atividade-helpers';

test.describe.serial('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    const timestamp = Date.now();
    const nomeProcesso = `Processo CDU-08 ${timestamp}`;
    const unidadeSigla = 'SECAO_111'; // Unidade gerida pelo CHEFE (333333)
    const gestorUsuario = '222222'; // GESTOR_COORD_11
    const chefeUsuario = '333333'; // CHEFE_SECAO_111
    const senhaPadrao = 'senha';

    test.beforeAll(async ({ browser }) => {
        // Setup: Criar processo como Gestor
        const context = await browser.newContext();
        const page = await context.newPage();

        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);

        // Expandir a árvore se necessário. COORD_11 -> SECAO_111.
        await criarProcesso(page, {
            descricao: nomeProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 5,
            unidade: unidadeSigla,
            iniciar: true
        });

        await context.close();
    });

    test('Deve permitir cadastrar, editar e remover atividades e conhecimentos', async ({ page }) => {
        // 1. Login como Chefe
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);

        // 2. Acessar o processo e subprocesso
        await page.getByText(nomeProcesso).click();

        await expect(page.getByTestId('txt-header-unidade')).toContainText(unidadeSigla);

        // 3. Acessar cadastro de atividades
        await navegarParaAtividades(page);

        // 4. Adicionar Atividade
        const atividadeDesc = `Atividade Teste ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);

        // 5. Adicionar Conhecimento
        const conhecimentoDesc = `Conhecimento Teste ${timestamp}`;
        await adicionarConhecimento(page, atividadeDesc, conhecimentoDesc);

        // 6. Editar Atividade
        const novaAtividadeDesc = `Atividade Editada ${timestamp}`;
        await editarAtividade(page, atividadeDesc, novaAtividadeDesc);

        // 7. Remover Atividade
        await removerAtividade(page, novaAtividadeDesc);
    });
});
