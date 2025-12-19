import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {fazerLogout, verificarPaginaPainel} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import {Page} from '@playwright/test';

async function verificarPaginaSubprocesso(page: Page) {
    await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_221$/);
}

async function acessarSubprocessoChefe(page: Page, descricaoProcesso: string) {
    await page.getByText(descricaoProcesso).click();
    // Se cair na lista de unidades, clica na unidade do Chefe
    if (await page.getByRole('heading', {name: /Unidades participantes/i}).isVisible()) {
                    await page.getByRole('row', {name: 'SECAO_221'}).click();    }
}

test.describe.serial('CDU-09 - Disponibilizar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;

    const timestamp = Date.now();
    const descProcesso = `Proc 9 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => await cleanup.limpar(request));

    test('Preparacao: Admin cria e inicia processo', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Iniciar processo
        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        // Wait for data to load to avoid race condition where fields are empty
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcesso);
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        // Capturar ID do processo para cleanup
        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Cenario 1: Validacao - Atividade sem conhecimento', async ({page}) => {
        // Login como Chefe
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso (CHEFE vai direto para o subprocesso)
        await acessarSubprocessoChefe(page, descProcesso);

        await verificarPaginaSubprocesso(page);

        // Entrar em Atividades
        await navegarParaAtividades(page);

        // Adicionar Atividade SEM conhecimento
        const atividadeDesc = `Atividade Incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);

        // Tentar Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Verificar que erro inline aparece na atividade (não há mais modal de pendências)
        const erroInline = page.getByTestId('atividade-erro-validacao');
        await expect(erroInline).toBeVisible();
        await expect(erroInline).toContainText(/conhecimento/i);

        // Adicionar conhecimento para corrigir
        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Corretivo');

        // Tentar Disponibilizar novamente - Agora deve abrir o modal de confirmação
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();

        // Cancelar para continuar o teste no proximo passo
        await page.getByRole('button', {name: 'Cancelar'}).click();
    });

    test('Cenario 2: Caminho feliz - Disponibilizar Cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcesso);
        await navegarParaAtividades(page);

        // Garantir que temos dados validos
        const atividadeDesc = `Atividade Validada ${timestamp}`;

        // Verificar se existe, se nao existir adicionar
        const count = await page.getByText(atividadeDesc).count();
        if (count === 0) {
            await adicionarAtividade(page, atividadeDesc);
            await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Valido');
        }

        // Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validar sucesso
        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar status no subprocesso
        await acessarSubprocessoChefe(page, descProcesso);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
    });

    test('Cenario 3: Devolucao e Historico de Analise', async ({page}) => {
        // 1. Admin devolve o cadastro
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByText(descProcesso).click();
                    await page.getByRole('row', {name: 'SECAO_221'}).click();
        // Entrar no cadastro de atividades (visualização)
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Devolver cadastro
        await page.getByTestId('btn-acao-devolver').click();

        // Preencher motivo da devolução
        const motivo = 'Faltou detalhar melhor os conhecimentos técnicos.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivo);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        // 2. Chefe verifica historico e corrige
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcesso);

        // Verificar situação 'Cadastro em andamento'
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);

        await navegarParaAtividades(page);

        // Verificar botão Histórico de Análise
        await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
        await page.getByTestId('btn-cad-atividades-historico').click();

        // Verificar conteúdo do modal
        const modal = page.locator('.modal-content').filter({hasText: 'Histórico de Análise'});
        await expect(modal).toBeVisible();

        // Assumindo que é a primeira linha ou única
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Faltou detalhar melhor os conhecimentos técnicos.');

        // Fechar modal
        await page.getByRole('button', {name: 'Fechar'}).click();

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validar sucesso
        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
    });
});
