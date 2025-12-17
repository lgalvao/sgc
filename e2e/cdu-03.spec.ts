import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

test.describe('CDU-03 - Manter Processo', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => await resetDatabase(request));

    test.beforeEach(async ({page}) => {
        cleanup = useProcessoCleanup();
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
    });

    test.afterEach(async ({request}) => {
        await cleanup.limpar(request);
    });

    test('Deve validar campos obrigatórios', async ({page}) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Tenta salvar vazio -> Erro de descrição
        await page.getByTestId('btn-processo-salvar').click();
        await expect(page.getByText('Preencha a descrição')).toBeVisible();

        // Preenche descrição e tenta salvar -> Erro de unidade
        await page.getByTestId('inp-processo-descricao').fill('Descrição Teste');
        await page.getByTestId('btn-processo-salvar').click();
        await expect(page.getByText('Pelo menos uma unidade participante deve ser incluída.')).toBeVisible();
    });

    test('Deve editar um processo existente', async ({page}) => {
        const descricaoOriginal = `Processo para Edição - ${Date.now()}`;
        // Cria um processo inicial
        await criarProcesso(page, {
            descricao: descricaoOriginal,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        // Capturar ID do processo para cleanup
        await page.getByText(descricaoOriginal).click();
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        const url = new URL(page.url());
        const processoId = parseInt(url.searchParams.get('codProcesso') || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        // Verifica que os dados foram carregados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoOriginal);

        // Expandir árvore para verificar seleção
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();

        await expect(page.getByTestId('chk-arvore-unidade-ASSESSORIA_11')).toBeChecked();

        // Modifica o processo
        const novaDescricao = descricaoOriginal + ' (Editado)';
        await page.getByTestId('inp-processo-descricao').fill(novaDescricao);
        await page.getByTestId('btn-processo-salvar').click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(novaDescricao)).toBeVisible();
        await expect(page.getByText(descricaoOriginal, {exact: true})).not.toBeVisible();
    });

    test('Deve remover um processo', async ({page}) => {
        const descricao = `Processo para Remoção - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        await page.getByText(descricao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await page.getByTestId('btn-processo-remover').click();
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeVisible();

        const btnsRemover = page.getByRole('button', {name: 'Remover'});
        await expect(btnsRemover).toHaveCount(2);
        await btnsRemover.last().click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByTestId('tbl-processos').getByText(descricao)).not.toBeVisible();
    });
});
