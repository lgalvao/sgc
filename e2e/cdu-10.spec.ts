import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {
    abrirHistoricoAnalise,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    fecharHistoricoAnalise,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

test.describe('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    test('Fluxo completo de revisão de cadastro', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        // Definir um timeout maior para este teste específico pois ele engloba muitos passos sequenciais
        test.setTimeout(30000);

        const timestamp = Date.now();
        const descProcessoMapeamento = `Map 10 ${timestamp}`;
        const descProcessoRevisao = `Rev 10 ${timestamp}`;
        let processoRevisaoId: number;

        await test.step('1. Preparação: Mapeamento completo e Revisão iniciada', async () => {
            // Admin cria e inicia processo de mapeamento
            await criarProcesso(page, {
                descricao: descProcessoMapeamento,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_22']
            });

            const linhaProcessoMap = page.locator('tr').filter({has: page.getByText(descProcessoMapeamento)});
            await linhaProcessoMap.click();

            const processoMapeamentoId = await extrairProcessoId(page);
            if (processoMapeamentoId > 0) cleanupAutomatico.registrar(processoMapeamentoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();
            await verificarPaginaPainel(page);

            // Chefe adiciona atividades e disponibiliza cadastro
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            await adicionarAtividade(page, `Atividade Mapeamento 1 ${timestamp}`);
            await adicionarConhecimento(page, `Atividade Mapeamento 1 ${timestamp}`, 'Conhecimento 1');
            await adicionarAtividade(page, `Atividade Mapeamento 2 ${timestamp}`);
            await adicionarConhecimento(page, `Atividade Mapeamento 2 ${timestamp}`, 'Conhecimento 2');
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await verificarPaginaPainel(page);

            // Admin homologa cadastro
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.getByText(descProcessoMapeamento).click();
            const linhaUnidade = page.getByRole('row', {name: UNIDADE_ALVO});
            await expect(linhaUnidade).toContainText(/Disponibilizado/i);
            await linhaUnidade.click();
            
            await navegarParaAtividadesVisualizacao(page);
            await homologarCadastroMapeamento(page);

            // Admin adiciona competências e disponibiliza mapa
            await page.goto('/painel');
            await page.getByText(descProcessoMapeamento).click();
            await page.getByRole('row', {name: UNIDADE_ALVO}).click();
            await navegarParaMapa(page);
            await criarCompetencia(page, `Competência Mapeamento 1 ${timestamp}`, [`Atividade Mapeamento 1 ${timestamp}`]);
            await criarCompetencia(page, `Competência Mapeamento 2 ${timestamp}`, [`Atividade Mapeamento 2 ${timestamp}`]);
            await page.getByTestId('btn-cad-mapa-disponibilizar').click();
            await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
            await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Chefe valida mapa
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-validar').click();
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await verificarPaginaPainel(page);

            // Admin homologa mapa e finaliza processo
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.getByText(descProcessoMapeamento).click();
            await page.getByRole('row', {name: UNIDADE_ALVO}).click();
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await verificarPaginaPainel(page);
            await page.getByText(descProcessoMapeamento).click();
            await page.getByTestId('btn-processo-finalizar').click();
            await page.getByTestId('btn-finalizar-processo-confirmar').click();
            await verificarPaginaPainel(page);

            // Admin cria e inicia processo de revisão
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

            // Chefe revisa atividades (muda situação para EM_ANDAMENTO)
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            await adicionarAtividade(page, `Atividade Revisão Nova ${timestamp}`);
            await adicionarConhecimento(page, `Atividade Revisão Nova ${timestamp}`, 'Conhecimento Revisão');
            await page.getByTestId('btn-cad-atividades-voltar').click({force: true});
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro em andamento/i);
        });

        await test.step('2. Cenário 1: Validação - Atividade sem conhecimento', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
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

        await test.step('3. Cenário 2: Caminho feliz - Disponibilizar revisão', async () => {
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();

            await expect(page.getByText(/Revisão disponibilizada/i).first()).toBeVisible();
            await verificarPaginaPainel(page);
            await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
        });

        await test.step('4. Cenário 3: Devolução e Histórico', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-devolver').click();
            const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos.';
            await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
            await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            const modal = await abrirHistoricoAnalise(page);
            await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
            await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);
            await fecharHistoricoAnalise(page);

            await disponibilizarCadastro(page);
            await expect(page.getByText(/Revisão disponibilizada/i).first()).toBeVisible();
            await verificarPaginaPainel(page);
        });

        await test.step('5. Cenário 4: Limpeza de Histórico após nova disponibilização', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-devolver').click();
            await page.getByTestId('inp-devolucao-cadastro-obs').fill('Segunda devolução');
            await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            
            await disponibilizarCadastro(page);
            await expect(page.getByText(/Revisão disponibilizada/i).first()).toBeVisible();
            await verificarPaginaPainel(page);

            // Admin devolve novamente
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-devolver').click();
            await page.getByTestId('inp-devolucao-cadastro-obs').fill('Terceira devolução');
            await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            // Chefe verifica que histórico tem apenas a última devolução
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            const modal = await abrirHistoricoAnalise(page);
            await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
            await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Terceira devolução');
            await expect(modal.getByTestId('cell-resultado-1')).toBeHidden();
            await fecharHistoricoAnalise(page);
        });

        await test.step('6. Cenário 5: Cancelar disponibilização', async () => {
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByRole('button', {name: 'Cancelar'}).click();
            await expect(page.getByRole('dialog')).toBeHidden();
            await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        });
    });
});
