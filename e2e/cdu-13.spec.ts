import { test, expect, type Page } from '@playwright/test';
import { login, USUARIOS } from './helpers/helpers-auth';
import { criarProcesso } from './helpers/helpers-processos';
import { adicionarAtividade, adicionarConhecimento, navegarParaAtividades } from './helpers/helpers-atividades';
import { resetDatabase, useProcessoCleanup } from './hooks/hooks-limpeza';
import {
    acessarSubprocessoGestor,
    acessarSubprocessoChefe,
    acessarSubprocessoAdmin,
    abrirHistoricoAnalise,
    fecharHistoricoAnalise,
    devolverCadastro,
    aceitarCadastro,
    homologarCadastroMapeamento,
    verificarPaginaPainel,
    verificarPaginaSubprocesso
} from './helpers/helpers-analise';

test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-13 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({ request }) => {
        await cleanup.limpar(request);
    });

    test('Preparacao 1: ADMIN cria e inicia processo de mapeamento', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Capturar ID do processo
        const linhaProcesso = page.locator('tr').filter({ has: page.getByText(descProcesso) });
        await linhaProcesso.click();

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcesso);
        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: CHEFE preenche atividades e disponibiliza', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcesso);
        await navegarParaAtividades(page);

        const atividadeDesc = `Atividade CDU-13 ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);
        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Teste');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page.getByRole('heading', { name: /Cadastro de atividades disponibilizado/i })).toBeVisible();
    });

    test('Cenario 1: GESTOR visualiza histórico de análise (vazio inicialmente)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByText(/Nenhuma análise realizada/i).or(modal.getByText(/Não há histórico/i)).or(modal.locator('table'))).toBeVisible();

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 2: GESTOR devolve cadastro para ajustes COM observação', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await devolverCadastro(page, 'Favor incluir mais detalhes nos conhecimentos');
    });

    test('Cenario 3: CHEFE visualiza histórico após devolução e corrige', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcesso);
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Cadastro em andamento/i);

        await navegarParaAtividades(page);

        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Favor incluir mais detalhes nos conhecimentos');
        await fecharHistoricoAnalise(page);

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page.getByRole('heading', { name: /Cadastro de atividades disponibilizado/i })).toBeVisible();
    });

    test('Cenario 4: GESTOR cancela devolução', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Cancelando...');
        await page.getByRole('button', { name: 'Cancelar' }).click();

        await expect(page.getByRole('dialog')).toBeHidden();
        await expect(page.getByTestId('btn-acao-devolver')).toBeVisible();
    });

    test('Cenario 5: GESTOR registra aceite SEM observação', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await aceitarCadastro(page);
    });

    test('Cenario 6: GESTOR registra aceite COM observação', async ({ page }) => {
        // Precisa devolver e disponibilizar novamente para testar outro aceite
        // 1. ADMIN devolve (para resetar estado)
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await devolverCadastro(page, 'Reset para teste de aceite com obs');

        // 2. CHEFE disponibiliza novamente
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcesso);
        await navegarParaAtividades(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await fazerLogout(page);

        // 3. GESTOR aceita com observação
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await aceitarCadastro(page, 'Cadastro aprovado conforme análise');
    });

    test('Cenario 7: ADMIN visualiza histórico com múltiplas análises', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        const modal = await abrirHistoricoAnalise(page);
        // Verificar múltiplos registros (Devolução, Aceite, Devolução, Aceite)
        // A tabela mostra o mais recente primeiro, geralmente.
        await expect(modal.locator('tbody tr')).toHaveCount(4);
        await fecharHistoricoAnalise(page);
    });

    test('Cenario 8: ADMIN cancela homologação', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-acao-analisar-principal').click();
        await expect(page.getByText(/Confirma a homologação do cadastro/i)).toBeVisible();
        await page.getByRole('button', { name: 'Cancelar' }).click();

        await expect(page.getByRole('dialog')).toBeHidden();
        await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
    });

    test('Cenario 9: ADMIN homologa cadastro', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await homologarCadastroMapeamento(page);

        // Verificar situação final
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Cadastro homologado/i);
    });
});

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}
