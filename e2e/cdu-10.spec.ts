import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import { Page } from '@playwright/test';

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `Map 10 ${timestamp}`;
    const descProcessoRevisao = `Rev 10 ${timestamp}`;
    let processoMapeamentoId: number;
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
    // PREPARAÇÃO - Criar processo de mapeamento finalizado
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Iniciar processo
        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcessoMapeamento)});
        await linhaProcesso.click();

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcessoMapeamento);
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        // Capturar ID do processo para cleanup
        processoMapeamentoId = Number.parseInt(new RegExp(/codProcesso=(\d+)/).exec(page.url())?.[1] || '0');
        if (processoMapeamentoId > 0) cleanup.registrar(processoMapeamentoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });


    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // CHEFE vai direto para o subprocesso
        await page.getByText(descProcessoMapeamento).click();
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Adicionar atividades e conhecimentos
        await adicionarAtividade(page, `Atividade Mapeamento 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Mapeamento 1 ${timestamp}`, 'Conhecimento 1');

        await adicionarAtividade(page, `Atividade Mapeamento 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Mapeamento 2 ${timestamp}`, 'Conhecimento 2');

        // Disponibilizar cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await expect(page.getByText(descProcessoMapeamento)).toBeVisible();
        await page.getByText(descProcessoMapeamento).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // Após homologação, redireciona para Detalhes do subprocesso (CDU-13 passo 11.7)
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    });

    test('Preparacao 4: Admin adiciona competências e disponibiliza mapa', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // ADMIN após login está no Painel
        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoMapeamento)});
        await expect(linhaProcesso).toBeVisible();
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/\d+/);

        // Navegar para a unidade
        await page.getByRole('row', {name: 'SECAO_221'}).click();

        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa"]').first().click();

        // Adicionar primeira competência
        await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência Mapeamento 1 ${timestamp}`);
        await page.getByText(`Atividade Mapeamento 1 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();
        await expect(page.getByText(`Competência Mapeamento 1 ${timestamp}`)).toBeVisible();

        // Adicionar segunda competência para a segunda atividade
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência Mapeamento 2 ${timestamp}`);
        await page.getByText(`Atividade Mapeamento 2 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();
        await expect(page.getByText(`Competência Mapeamento 2 ${timestamp}`)).toBeVisible();

        // Disponibilizar mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();
        
        // Aguardar redirecionamento para o painel e verificar mensagem de sucesso
        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByRole('heading', {name: /Mapa disponibilizado/i})).toBeVisible();
    });


    test('Preparacao 5: Chefe valida mapa', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('card-subprocesso-mapa').click();

        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Validação: confirmar redirecionamento para Painel (CDU-19 passo 8)
        await verificarPaginaPainel(page);
    });

    test('Preparacao 6: Admin homologa mapa e finaliza processo de mapeamento', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // ADMIN após login está no Painel
        await expect(page.getByText(descProcessoMapeamento)).toBeVisible();
        await page.getByText(descProcessoMapeamento).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);

        // Navegar para a unidade
        await page.getByRole('row', {name: 'SECAO_221'}).click();

        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Validação: confirmar redirecionamento para Painel (CDU-20 passo 10.6)
        await verificarPaginaPainel(page);
        
        // Finalizar o processo de mapeamento para liberar a unidade para novos processos
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        
        await verificarPaginaPainel(page);
    });


    test('Preparacao 7: Admin cria e inicia processo de revisão', async ({page}) => {
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

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcessoRevisao);

        // Capturar ID do processo para cleanup
        processoRevisaoId = Number.parseInt(new RegExp(/codProcesso=(\d+)/).exec(page.url())?.[1] || '0');
        if (processoRevisaoId > 0) cleanup.registrar(processoRevisaoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 8: Chefe revisa atividades', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar pelo painel para o processo de revisão (Chefe vai direto)
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();

        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Verificar situação inicial - deve ser "Não Iniciado" (antes de qualquer alteração)
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Não Iniciado/i);

        await navegarParaAtividades(page);

        // Verificar que as atividades do mapeamento foram copiadas
        await expect(page.getByText(`Atividade Mapeamento 1 ${timestamp}`)).toBeVisible();
        await expect(page.getByText(`Atividade Mapeamento 2 ${timestamp}`)).toBeVisible();

        // Adicionar uma nova atividade na revisão - isso deve mudar a situação
        await adicionarAtividade(page, `Atividade Revisão Nova ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Revisão Nova ${timestamp}`, 'Conhecimento Revisão');

        // Voltar para a tela do subprocesso para verificar mudança de situação
        await page.getByTestId('btn-cad-atividades-voltar').click();

        // Agora a situação deve ser "Revisão do cadastro em andamento"
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão d[oe] cadastro em andamento/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-10
    // ========================================================================

    test('Cenario 1: Validação - Atividade sem conhecimento impede disponibilização', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        // Chefe vai direto

        await navegarParaAtividades(page);

        // Adicionar atividade SEM conhecimento
        const atividadeIncompleta = `Atividade Incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeIncompleta);

        // Tentar disponibilizar - deve mostrar erro inline (não há mais modal de pendências)
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Verificar que erro inline aparece na atividade
        const erroInline = page.getByTestId('atividade-erro-validacao');
        await expect(erroInline).toBeVisible();
        await expect(erroInline).toContainText(/conhecimento/i);

        // Corrigir adicionando conhecimento
        await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento Corretivo');

        // Agora deve permitir disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();

        // Cancelar para continuar testes
        await page.getByRole('button', {name: 'Cancelar'}).click();
    });

    test('Cenario 2: Caminho feliz - Disponibilizar revisão do cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        // Chefe vai direto

        await navegarParaAtividades(page);

        // Verificar que todas as atividades têm conhecimentos
        await expect(page.getByText(`Atividade Revisão Nova ${timestamp}`)).toBeVisible();

        // Disponibilizar revisão
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Verificar modal de confirmação com mensagem específica
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Disponibilização da revisão do cadastro/i)).toBeVisible();
        await expect(modal.getByText(/Confirma a finalização da revisão e a disponibilização do cadastro/i)).toBeVisible();
        await expect(modal.getByText(/Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores/i)).toBeVisible();

        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validar sucesso
        await expect(page.getByRole('heading', {name: /Revisão disponibilizada/i})).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar status no subprocesso
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        if (new RegExp(/\/processo\/\d+$/).exec(page.url())) {
                await page.getByRole('row', {name: 'SECAO_221'}).click();
        }
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
    });

    test('Cenario 3: Devolução e Histórico de Análise', async ({page}) => {
        // 1. Admin devolve a revisão do cadastro
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        await page.getByRole('cell', {name: descProcessoRevisao, exact: true}).click();

        // CDU-14 Passo 3: Admin clica na unidade subordinada
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await expect(page.getByRole('row', {name: 'SECAO_221'})).toBeVisible();
        await page.getByRole('row', {name: 'SECAO_221'}).click();

        // Entrar na visualização de atividades
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Devolver
        await page.getByTestId('btn-acao-devolver').click();

        const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos adicionados.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        // 2. Chefe verifica histórico de análise
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Chefe vai direto
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();

        // Verificar situação voltou para 'em andamento'
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão d[oe] cadastro em andamento/i);

        await navegarParaAtividades(page);

        // Verificar botão Histórico de Análise está visível (CDU-10 passo 5)
        await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
        await page.getByTestId('btn-cad-atividades-historico').click();

        // Verificar conteúdo do modal de histórico
        const modal = page.locator('.modal-content').filter({hasText: 'Histórico de Análise'});
        await expect(modal).toBeVisible();

        // Verificar dados da análise (CDU-10 passo 5.1)
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);

        // Verificar que tem data/hora e sigla da unidade
        await expect(modal.getByTestId('cell-unidade-0')).toBeVisible();
        await expect(modal.getByTestId('cell-data-0')).toBeVisible();

        // Fechar modal
        await page.getByRole('button', {name: 'Fechar'}).click();

        // 3. Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', {name: /Revisão disponibilizada/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 4: Verificar que histórico foi excluído após nova disponibilização', async ({page}) => {
        // Admin devolve a revisão (primeira devolução)
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        await page.getByRole('cell', {name: descProcessoRevisao, exact: true}).click();

        // CDU-14 Passo 3: Admin clica na unidade subordinada
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await expect(page.getByRole('row', {name: 'SECAO_221'})).toBeVisible();
        await page.getByRole('row', {name: 'SECAO_221'}).click();

        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Primeira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        // Chefe disponibiliza novamente para permitir segunda devolução
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        await navegarParaAtividades(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page.getByRole('heading', {name: /Revisão disponibilizada/i})).toBeVisible();
        await verificarPaginaPainel(page);

        // Admin faz segunda devolução
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // Verificar que o processo de revisão está visível no painel (deveria estar na tabela de processos)
        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        
        // Navegar diretamente para evitar erros de seleção na tabela
        await page.goto(`/processo/${processoRevisaoId}`);
        await expect(page.getByRole('table')).toBeVisible();

        // CDU-14 Passo 3: Admin clica na unidade subordinada
        // CDU-14 Passo 3: Admin clica na unidade subordinada
        await expect(page.getByRole('row', {name: 'SECAO_221'})).toBeVisible();
        await page.getByRole('row', {name: 'SECAO_221'}).click();

        // CDU-14 Passo 5: Usuário clica no card Atividades e conhecimentos
        await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();

        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Segunda devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByRole('cell', {name: descProcessoRevisao, exact: true}).click();
        await navegarParaAtividades(page);

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', {name: /Revisão disponibilizada/i})).toBeVisible();
        await verificarPaginaPainel(page);

        // Agora Admin devolve mais uma vez (terceira devolução)
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        await page.goto(`/processo/${processoRevisaoId}`);
        await expect(page.getByRole('table')).toBeVisible();

        // CDU-14 Passo 3: Admin clica na unidade subordinada
        // CDU-14 Passo 3: Admin clica na unidade subordinada
        await expect(page.getByRole('row', {name: 'SECAO_221'})).toBeVisible();
        await page.getByRole('row', {name: 'SECAO_221'}).click();

        // CDU-14 Passo 5: Usuário clica no card Atividades e conhecimentos
        await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();

        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Terceira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Chefe verifica que histórico só tem a última análise (CDU-10 passo 15)
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Chefe vai direto
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();

        await navegarParaAtividades(page);
        await page.getByTestId('btn-cad-atividades-historico').click();

        const modal = page.locator('.modal-content').filter({hasText: 'Histórico de Análise'});
        await expect(modal).toBeVisible();

        // Deve ter apenas uma linha (a última devolução, após a disponibilização que excluiu o histórico anterior)
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Terceira devolução');

        // Não deve ter segunda linha (histórico anterior foi excluído pela disponibilização)
        await expect(modal.getByTestId('cell-resultado-1')).toBeHidden();
    });

    test('Cenario 5: Cancelar disponibilização mantém na mesma tela', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Chefe vai direto
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();

        await navegarParaAtividades(page);

        // Tentar disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Cancelar (CDU-10 passo 8.1)
        await page.getByRole('button', {name: 'Cancelar'}).click();

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible();
    });
});
