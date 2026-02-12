import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades
} from './helpers/helpers-atividades.js';
import {abrirModalCriarCompetencia} from './helpers/helpers-mapas.js';
import {abrirHistoricoAnalise, acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';
import {fazerLogout, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcessoMapeamento = `Map 10 ${timestamp}`;
    const descProcessoRevisao = `Rev 10 ${timestamp}`;
    let processoMapeamentoId: number;
    let processoRevisaoId: number;

    // ========================================================================
    // PREPARAÇÃO - Criar processo de mapeamento finalizado e de revisão
    // ========================================================================

    test('Preparação do cenário: Mapeamento completo e Revisão iniciada', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        test.setTimeout(60000); // Setup longo precisa de mais tempo
        // 1. Admin cria e inicia processo de mapeamento
        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Iniciar processo
        const linhaProcessoMap = page.locator('tr').filter({has: page.getByText(descProcessoMapeamento)});
        await linhaProcessoMap.click();

        // Capturar ID do processo para cleanup
        processoMapeamentoId = await extrairProcessoId(page);
        if (processoMapeamentoId > 0) cleanupAutomatico.registrar(processoMapeamentoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // 2. Chefe adiciona atividades e disponibiliza cadastro
        console.log(`[DEBUG] Passo 2: Chefe disponibiliza cadastro`);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.getByText(descProcessoMapeamento).click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade Mapeamento 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Mapeamento 1 ${timestamp}`, 'Conhecimento 1');
        await adicionarAtividade(page, `Atividade Mapeamento 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Mapeamento 2 ${timestamp}`, 'Conhecimento 2');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);

        // 3. Admin homologa cadastro
        console.log(`[DEBUG] Passo 3: Admin homologa cadastro`);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByText(descProcessoMapeamento).click();
        const linhaUnidade = page.getByRole('row', {name: 'SECAO_221'});
        await expect(linhaUnidade).toContainText(/Disponibilizado/i);
        await linhaUnidade.click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // 4. Admin adiciona competências e disponibiliza mapa
        await page.goto('/painel');
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.locator('[data-testid="card-subprocesso-mapa"]').first().click();
        await abrirModalCriarCompetencia(page);
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência Mapeamento 1 ${timestamp}`);
        await page.getByText(`Atividade Mapeamento 1 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await abrirModalCriarCompetencia(page);
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência Mapeamento 2 ${timestamp}`);
        await page.getByText(`Atividade Mapeamento 2 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        await expect(page).toHaveURL(/\/painel/);

        // 5. Chefe valida mapa
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        // 6. Admin homologa mapa e finaliza processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // 7. Admin cria e inicia processo de revisão
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });
        const linhaProcessoRev = page.locator('tr', {has: page.getByText(descProcessoRevisao)});
        await linhaProcessoRev.click();
        processoRevisaoId = await extrairProcessoId(page);
        if (processoRevisaoId > 0) cleanupAutomatico.registrar(processoRevisaoId);
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // 8. Chefe revisa atividades (muda situação para EM_ANDAMENTO)
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade Revisão Nova ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Revisão Nova ${timestamp}`, 'Conhecimento Revisão');
        await page.getByTestId('btn-cad-atividades-voltar').click();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro em andamento/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-10
    // ========================================================================

    test('Cenario 1: Validação - Atividade sem conhecimento impede disponibilização', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        await navegarParaAtividades(page);

        const atividadeIncompleta = `Atividade Incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeIncompleta);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const erroInline = page.getByTestId('atividade-erro-validacao');
        await expect(erroInline).toBeVisible();
        await expect(erroInline).toContainText(/conhecimento/i);

        await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento Corretivo');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByRole('button', {name: 'Cancelar'}).click();
    });

    test('Cenario 2: Caminho feliz - Disponibilizar revisão do cadastro', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        await navegarParaAtividades(page);
        await expect(page.getByText(`Atividade Revisão Nova ${timestamp}`)).toBeVisible();

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Disponibilização da revisão do cadastro/i)).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Revisão disponibilizada/i)).toBeVisible();
        await verificarPaginaPainel(page);
        await page.locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        if (new RegExp(/\/processo\/\d+$/).exec(page.url())) {
                await page.getByRole('row', {name: 'SECAO_221'}).click();
        }
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
    });

    test('Cenario 3: Devolução e Histórico de Análise', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        await page.getByRole('cell', {name: descProcessoRevisao, exact: true}).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos adicionados.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);
        await page.getByRole('button', {name: 'Fechar'}).click();

        await disponibilizarCadastro(page);
        await expect(page.getByText(/Revisão disponibilizada/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 4: Devolução e nova disponibilização adicional', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByText(descProcessoRevisao).click();
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Primeira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await disponibilizarCadastro(page);
        await expect(page.getByText(/Revisão disponibilizada/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 5: Segunda devolução e disponibilização que limpa o histórico', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        await page.goto(`/processo/${processoRevisaoId}`);
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Segunda devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await disponibilizarCadastro(page);
        await expect(page.getByText(/Revisão disponibilizada/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 6: Verificar que histórico foi excluído após nova disponibilização', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.goto(`/processo/${processoRevisaoId}`);
        await page.getByRole('row', {name: 'SECAO_221'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Terceira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Terceira devolução');
        await expect(modal.getByTestId('cell-resultado-1')).toBeHidden();
        await page.getByRole('button', {name: 'Fechar'}).click();
    });

    test('Cenario 7: Cancelar disponibilização mantém na mesma tela', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByRole('button', {name: 'Cancelar'}).click();
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });
});
