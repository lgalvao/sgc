import {expect, Page, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {
    abrirModalImpacto,
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    fecharModalImpacto,
    navegarParaAtividades,
    removerAtividade,
    verificarBotaoImpacto
} from './helpers/helpers-atividades';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function acessarSubprocessoChefe(page: Page, descricaoProcesso: string) {
    await page.getByText(descricaoProcesso).click();
    // Se cair na lista de unidades, clica na unidade do Chefe
    if (await page.getByRole('heading', {name: /Unidades participantes/i}).isVisible()) {
        await page.getByRole('row', {name: 'Seção 221'}).click();
    }
}

test.describe.serial('CDU-12 - Verificar impactos no mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `AAA Mapeamento CDU-12 ${timestamp}`;
    const descProcessoRevisao = `AAA Revisão CDU-12 ${timestamp}`;
    let codProcessoMapeamento: number;
    let processoRevisaoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar Mapa Vigente e Iniciar Revisão
    // ========================================================================

    test('Preparacao 1: Setup Mapeamento (Atividades, Competências, Homologação)', async ({page}) => {
        // 1. Criar Processo Mapeamento
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoMapeamento)});
        await linhaProcesso.click();
        codProcessoMapeamento = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (codProcessoMapeamento > 0) cleanup.registrar(codProcessoMapeamento);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 2. Chefe preenche atividades
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoMapeamento);
        await navegarParaAtividades(page);

        // Atividade 1
        await adicionarAtividade(page, `Atividade Base 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 1 ${timestamp}`, 'Conhecimento Base 1A');

        // Atividade 2 (será modificada na revisão)
        await adicionarAtividade(page, `Atividade Base 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 2 ${timestamp}`, 'Conhecimento Base 2A');

        // Atividade 3 (será removida na revisão)
        await adicionarAtividade(page, `Atividade Base 3 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 3 ${timestamp}`, 'Conhecimento Base 3A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Admin homologa cadastro
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: 'Seção 221'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // Aguardar conclusão e redirecionamento (comportamento padrão de homologação)
        await verificarPaginaPainel(page);

        // 4. Admin cria competências (Mapa)
        await expect(page.getByText(descProcessoMapeamento)).toBeVisible();
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: 'Seção 221'}).click();

        // Verificar se o card de mapa EDITAVEL está visível (confirma permissão/status correto)
        await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
        await page.getByTestId('card-subprocesso-mapa').click();

        // Aguardar carregamento da tela do mapa (título da unidade)
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toBeVisible();

        // Competência 1 ligada a Atividade 1
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 1 ${timestamp}`);
        await page.getByText(`Atividade Base 1 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Competência 2 ligada a Atividade 2
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 2 ${timestamp}`);
        await page.getByText(`Atividade Base 2 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Competência 3 ligada a Atividade 3
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 3 ${timestamp}`);
        await page.getByText(`Atividade Base 3 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Disponibilizar Mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        // 5. Chefe Valida e Admin Homologa (Finalizar Mapeamento)
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoMapeamento);
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: 'Seção 221'}).click();
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Finalizar Processo
        await page.goto('/painel');
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
    });

    test('Preparacao 2: Iniciar Processo de Revisão', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoRevisao)});
        await linhaProcesso.click();
        processoRevisaoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoRevisaoId > 0) cleanup.registrar(processoRevisaoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
    });

    // ========================================================================
    // TESTES CDU-12
    // ========================================================================

    test('Cenario 1: Verificar Sem Impactos (Estado Inicial)', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcessoRevisao);

        await navegarParaAtividades(page);

        // 1. Verificar presença do botão
        await verificarBotaoImpacto(page, true);

        // 2. Clicar no botão
        await page.getByTestId('cad-atividades__btn-impactos-mapa').click();
        await expect(page.getByText('Nenhum impacto detectado no mapa.')).toBeVisible();
    });

    test('Cenario 2: Verificar Impacto de Inclusão de Atividade', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoRevisao);
        await navegarParaAtividades(page);

        // Adicionar nova atividade
        const novaAtividade = `Atividade Nova Revisão ${timestamp}`;
        await adicionarAtividade(page, novaAtividade);
        await adicionarConhecimento(page, novaAtividade, 'Conhecimento Novo');

        // Verificar impacto
        await abrirModalImpacto(page);

        // Verificar seção de atividades inseridas
        const modal = page.locator('.modal-content');
        await expect(modal.getByText('Atividades inseridas')).toBeVisible();
        await expect(modal.getByText(novaAtividade)).toBeVisible();
        // A modal não lista conhecimentos, apenas competencias vinculadas

        await fecharModalImpacto(page);
    });

    test('Cenario 3: Verificar Impacto de Alteração em Atividade (Impacta Competência)', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoRevisao);
        await navegarParaAtividades(page);

        // Editar atividade existente (Atividade Base 2)
        const descOriginal = `Atividade Base 2 ${timestamp}`;
        const descNova = `Atividade Base 2 Editada ${timestamp}`;
        await editarAtividade(page, descOriginal, descNova);

        // Verificar impacto
        await abrirModalImpacto(page);

        // Verificar seção de competências impactadas
        const modal = page.locator('.modal-content');
        await expect(modal.getByText('Competências impactadas')).toBeVisible();

        // Deve mostrar a Competência 2
        await expect(modal.getByText(`Competência 2 ${timestamp}`)).toBeVisible();

        // Deve mostrar o detalhe da alteração. O texto exato pode variar na implementação, mas deve conter a descrição da atividade.
        await expect(modal.getByText(descNova)).toBeVisible();
        await fecharModalImpacto(page);
    });

    test('Cenario 4: Verificar Impacto de Remoção de Atividade (Impacta Competência)', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoRevisao);
        await navegarParaAtividades(page);

        // Remover atividade (Atividade Base 3)
        const descRemovida = `Atividade Base 3 ${timestamp}`;
        await removerAtividade(page, descRemovida);

        // Verificar impacto
        await abrirModalImpacto(page);

        const modal = page.locator('.modal-content');
        await expect(modal.getByText('Competências impactadas')).toBeVisible();

        // Deve mostrar a Competência 3
        await expect(modal.getByText(`Competência 3 ${timestamp}`)).toBeVisible();

        // Deve indicar que atividade foi removida
        await expect(modal.getByTestId('lista-atividades-removidas').getByText(descRemovida)).toBeVisible();
        // await expect(modal.getByText(/removida/i)).toBeVisible();

        await fecharModalImpacto(page);
    });

    test('Cenario 5: Verificar visualização pelo Admin (Somente Leitura)', async ({page}) => {
        // Chefe disponibiliza a revisão
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoRevisao);
        await navegarParaAtividades(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Admin acessa
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcessoRevisao).click();
        await page.getByRole('row', {name: 'Seção 221'}).click();

        // Acessar visualização
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Verificar botão de impacto
        await verificarBotaoImpacto(page, true);

        // Abrir e verificar conteúdo (deve ter os acumulados dos cenários anteriores)
        await abrirModalImpacto(page);
        const modal = page.locator('.modal-content');

        // Inserida
        await expect(modal.getByText(`Atividade Nova Revisão ${timestamp}`)).toBeVisible();
        // Alterada (Competência 2)
        await expect(modal.getByText(`Competência 2 ${timestamp}`)).toBeVisible();
        // Removida (Competência 3)
        await expect(modal.getByText(`Competência 3 ${timestamp}`)).toBeVisible();

        await fecharModalImpacto(page);
    });
});
