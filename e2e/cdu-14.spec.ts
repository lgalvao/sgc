import { test, expect, type Page } from '@playwright/test';
import { login, USUARIOS } from './helpers/helpers-auth';
import { criarProcesso } from './helpers/helpers-processos';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    editarAtividade,
    removerAtividade,
    verificarBotaoImpacto,
    abrirModalImpacto,
    fecharModalImpacto
} from './helpers/helpers-atividades';
import { resetDatabase, useProcessoCleanup } from './hooks/hooks-limpeza';
import {
    acessarSubprocessoGestor,
    acessarSubprocessoChefe,
    acessarSubprocessoAdmin,
    abrirHistoricoAnalise,
    fecharHistoricoAnalise,
    devolverCadastro,
    aceitarCadastro,
    homologarRevisaoComImpactos,
    homologarRevisaoSemImpactos,
    verificarPaginaPainel
} from './helpers/helpers-analise';

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

test.describe.serial('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `Mapeamento CDU-14 ${timestamp}`;
    const descProcessoRevisao = `Revisão CDU-14 ${timestamp}`;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({ request }) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar Mapa Vigente
    // ========================================================================

    test('Preparacao 1: Setup Mapeamento Completo', async ({ page }) => {
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

        const linhaProcesso = page.locator('tr', { has: page.getByText(descProcessoMapeamento) });
        await linhaProcesso.click();
        const codProcesso = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (codProcesso > 0) cleanup.registrar(codProcesso);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 2. Chefe preenche atividades
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoMapeamento);
        await navegarParaAtividades(page);

        // Atividades para o mapa
        await adicionarAtividade(page, `Atividade 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade 1 ${timestamp}`, 'Conhecimento 1');

        await adicionarAtividade(page, `Atividade 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade 2 ${timestamp}`, 'Conhecimento 2');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Admin homologa cadastro
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await page.getByTestId('btn-acao-analisar-principal').click(); // Homologar
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // 4. Admin cria mapa
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-mapa').click();

        // Competência 1 -> Atividade 1
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 1 ${timestamp}`);
        await page.getByText(`Atividade 1 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Competência 2 -> Atividade 2
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 2 ${timestamp}`);
        await page.getByText(`Atividade 2 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Disponibilizar Mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        // 5. Chefe Valida e Admin Homologa
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoMapeamento);
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Finalizar Processo
        await page.goto('/painel');
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
    });

    test('Preparacao 2: Iniciar Revisão e Gerar Impactos', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', { has: page.getByText(descProcessoRevisao) });
        await linhaProcesso.click();
        const codProcesso = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (codProcesso > 0) cleanup.registrar(codProcesso);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Chefe edita para gerar impactos
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoRevisao);
        await navegarParaAtividades(page);

        // Adicionar nova atividade (Impacto: Inserção)
        await adicionarAtividade(page, `Atividade Nova Revisão ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Nova Revisão ${timestamp}`, 'Conhecimento Novo');

        // Editar atividade existente (Impacto: Alteração)
        await editarAtividade(page, `Atividade 1 ${timestamp}`, `Atividade 1 Editada ${timestamp}`);

        // Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
    });

    // ========================================================================
    // CENÁRIOS DE TESTE (COM IMPACTOS)
    // ========================================================================

    test('Cenario 1: GESTOR visualiza impactos no mapa', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await verificarBotaoImpacto(page, true);
        await abrirModalImpacto(page);

        const modal = page.locator('.modal-content');
        await expect(modal.getByText(`Atividade Nova Revisão ${timestamp}`)).toBeVisible();
        await expect(modal.getByText(`Atividade 1 Editada ${timestamp}`)).toBeVisible();

        await fecharModalImpacto(page);
    });

    test('Cenario 2: GESTOR visualiza histórico de análise (vazio)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByText(/Nenhuma análise realizada/i).or(modal.getByText(/Não há histórico/i)).or(modal.locator('table'))).toBeVisible();
        await fecharHistoricoAnalise(page);
    });

    test('Cenario 3: GESTOR devolve revisão para ajustes COM observação', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await devolverCadastro(page, 'Revisar impactos na Competência X');
    });

    test('Cenario 4: CHEFE visualiza histórico e disponibiliza novamente', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descProcessoRevisao);
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão do cadastro em andamento/i);

        await navegarParaAtividades(page);

        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Revisar impactos na Competência X');
        await fecharHistoricoAnalise(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
    });

    test('Cenario 5: GESTOR cancela aceite', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Obs de cancelamento');
        await page.getByRole('button', { name: 'Cancelar' }).click();

        await expect(page.getByRole('dialog')).toBeHidden();
    });

    test('Cenario 6: GESTOR registra aceite da revisão', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await aceitarCadastro(page, 'Revisão aprovada');
    });

    test('Cenario 7: ADMIN visualiza histórico e impactos', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Histórico
        const modalHist = await abrirHistoricoAnalise(page);
        await expect(modalHist.locator('tbody tr')).toHaveCount(2); // Devolução + Aceite
        await fecharHistoricoAnalise(page);

        // Impactos
        await abrirModalImpacto(page);
        const modalImp = page.locator('.modal-content');
        await expect(modalImp.getByText(`Atividade Nova Revisão ${timestamp}`)).toBeVisible();
        await fecharModalImpacto(page);
    });

    test('Cenario 9: ADMIN cancela homologação', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-acao-analisar-principal').click();
        await expect(page.getByText(/Confirma a homologação do cadastro/i)).toBeVisible();
        await page.getByRole('button', { name: 'Cancelar' }).click();
    });

    test('Cenario 11: ADMIN homologa revisão COM impactos', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await homologarRevisaoComImpactos(page);

        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão do cadastro homologada/i);
    });

    // ========================================================================
    // CENÁRIO EXTRA (SEM IMPACTOS)
    // ========================================================================

    test('Cenario 10: ADMIN homologa revisão SEM impactos', async ({ page }) => {
        // Criar novo processo de revisão 2
        const descRevisaoSemImpacto = `Revisão Sem Impacto ${timestamp}`;

        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await criarProcesso(page, {
            descricao: descRevisaoSemImpacto,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', { has: page.getByText(descRevisaoSemImpacto) });
        await linhaProcesso.click();
        const codProcesso = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (codProcesso > 0) cleanup.registrar(codProcesso);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Chefe disponibiliza SEM ALTERAÇÕES DE ESTRUTURA (apenas edita descrição de conhecimento, por exemplo, ou nem isso)
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefe(page, descRevisaoSemImpacto);
        await navegarParaAtividades(page);

        // Editar apenas um conhecimento (não impacta mapa)
        // Como o helper edita atividade, vamos apenas não fazer nada, apenas disponibilizar.
        // Se não houver mudanças, não deve ter impactos.
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Admin homologa
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descRevisaoSemImpacto, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Verificar que não tem impactos
        await page.getByTestId('cad-atividades__btn-impactos-mapa').click();
        await expect(page.getByText('Nenhum impacto detectado no mapa.')).toBeVisible();
        await fecharModalImpacto(page);

        // Homologar
        await homologarRevisaoSemImpactos(page);
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Mapa homologado/i);
    });
});
