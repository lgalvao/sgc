import {expect, test} from './fixtures/auth-fixtures';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import {Page} from '@playwright/test';

async function acessarSubprocessoChefe(page: Page, descProcesso: string) {
    await page.getByText(descProcesso).click();
    await page.getByTestId('card-subprocesso-mapa').click();
}

test.describe.serial('CDU-20 - Analisar validação de mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-20 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    // Atividades e competências para os testes
    const atividade1 = `Atividade 1 ${timestamp}`;
    const atividade2 = `Atividade 2 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar mapa validado para GESTOR/ADMIN analisar
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, autenticadoComoGestorCoord22, autenticadoComoChefeSecao221}) => {
        

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento 1A');

        await adicionarAtividade(page, atividade2);
        await adicionarConhecimento(page, atividade2, 'Conhecimento 2A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    });

    test('Preparacao 4: Admin cria competências e disponibiliza mapa', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await navegarParaMapa(page);

        await criarCompetencia(page, competencia1, [atividade1]);
        await criarCompetencia(page, competencia2, [atividade2]);

        await disponibilizarMapa(page, '2030-12-31');

        await verificarPaginaPainel(page);
        await expect(page.getByRole('heading', {name: /Mapa disponibilizado/i})).toBeVisible();
    });

    test('Preparacao 5: Chefe valida o mapa', async ({page, autenticadoComoAdmin}) => {
        

        await acessarSubprocessoChefe(page, descProcesso);

        // Validar mapa
        await page.getByTestId('btn-mapa-validar').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByRole('heading', {name: /Mapa validado/i})).toBeVisible();
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-20
    // ========================================================================

    test('Cenario 1: GESTOR navega para visualização do mapa validado', async ({page}) => {
        // CDU-20: Passos 1-4
        

        // Passo 1: No Painel, escolhe o processo
        await expect(page.getByText(descProcesso)).toBeVisible();
        await page.getByText(descProcesso).click();

        // Passo 1: Clica na unidade com situação 'Mapa validado'
        await navegarParaSubprocesso(page, 'SECAO_221');
        // Passo 2: Tela Detalhes do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Mapa validado/i);

        // Passo 3: Clica no card Mapa de Competências
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 4: Tela Visualização de mapa com botões
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-historico-gestor')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
    });

    test('Cenario 2: GESTOR cancela aceite - permanece na tela', async ({page, autenticadoComoGestorCoord22}) => {
        // CDU-20: Passo 9.3
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 9.1: Clicar em Registrar aceite
        await page.getByTestId('btn-mapa-homologar-aceite').click();

        // Passo 9.2: Modal de confirmação
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // Passo 9.3: Cancelar
        await page.getByTestId('btn-aceite-mapa-cancelar').click();

        // Permanece na tela de visualização
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
    });

    test('Cenario 3: GESTOR registra aceite do mapa', async ({page, autenticadoComoGestorCoord22}) => {
        // CDU-20: Passos 9.4-9.9
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 9.1: Clicar em Registrar aceite
        await page.getByTestId('btn-mapa-homologar-aceite').click();

        // Passo 9.4: Confirmar
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Passo 9.9: Redirecionamento para Painel
        await verificarPaginaPainel(page);

        // Verificar que o mapa avançou para ADMIN analisar
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        // Após aceite do GESTOR, o mapa ainda está validado mas agora no nível do ADMIN
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Mapa validado/i);
    });

    test('Cenario 4: ADMIN homologa o mapa', async ({page}) => {
        // CDU-20: Passos 10.1-10.6
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 10.1: Clicar em Homologar
        await page.getByTestId('btn-mapa-homologar-aceite').click();

        // Passo 10.2: Modal de confirmação
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // Passo 10.4: Confirmar
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Passo 10.6: Redirecionamento para Painel
        await verificarPaginaPainel(page);

        // Passo 10.5: Verificar situação alterada para 'Mapa homologado'
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Mapa homologado/i);
    });
});
