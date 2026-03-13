import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    fecharHistoricoAnalise
} from './helpers/helpers-analise.js';
import {limparNotificacoes, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}(?:/)?$`));
}

test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const timestamp = Date.now();
    const descProcessoRevisao = `Rev 10 ${timestamp}`;
    let processoCodigo: number;

    test('1. Setup: Preparar processo de revisão e atividades iniciais', async ({request, page}) => {
        // Criar processo mapeamento finalizado (gera mapa vigente)
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: `Base map 10 ${timestamp}`
        });

        // Criar processo de revisão
        const processo = await criarProcessoFixture(request, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            unidade: UNIDADE_ALVO,
            iniciar: true
        });
        processoCodigo = processo.codigo;

        // Chefe revisa atividades (muda situação para EM_ANDAMENTO)
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade revisão nova ${timestamp}`);
        await adicionarConhecimento(page, `Atividade revisão nova ${timestamp}`, 'Conhecimento revisão');
        await page.getByTestId('btn-nav-voltar').click();
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
    });

    test('2. Cenário 1: Validação - Atividade sem conhecimento', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);

        const atividadeIncompleta = `Atividade incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeIncompleta);
        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();

        await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento corretivo');
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
    });

    test('3. Cenário 2: Caminho feliz - Disponibilizar revisão', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const modalConfirmacao = page.getByRole('dialog');
        await expect(modalConfirmacao.getByText('Disponibilização da revisão do cadastro')).toBeVisible();
        await expect(modalConfirmacao.getByText(/Confirma a finalização da revisão e a disponibilização do cadastro/i)).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Cadastro disponibilizado\./i).first()).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar alerta para o gestor superior
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await expect(page.getByTestId('tbl-alertas')).toContainText(`Revisão do cadastro da unidade ${UNIDADE_ALVO} disponibilizada para análise`);

        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Disponibilização da revisão do cadastro de atividades/i);
    });

    test('4. Cenário 3: Devolução e Histórico', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/vis-cadastro`);
        await page.getByTestId('btn-acao-devolver').click();
        const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar movimentação de devolução
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Devolução da revisão do cadastro para ajustes/i);

        // Verificar alerta para o chefe da unidade
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await limparNotificacoes(page);
        await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: `Revisão do cadastro da unidade ${UNIDADE_ALVO} devolvida para ajustes` })).toBeVisible();

        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);
        await fecharHistoricoAnalise(page);

        await disponibilizarCadastro(page);
        await verificarPaginaPainel(page);
    });

    test('5. Cenário 4: Histórico retém as análises após nova disponibilização', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/vis-cadastro`);
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Segunda devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar movimentação de devolução (2ª vez)
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('tbl-movimentacoes').locator('tr', { hasText: /Devolução da revisão do cadastro para ajustes/i }).first()).toBeVisible();

        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);
        await disponibilizarCadastro(page);
        await verificarPaginaPainel(page);

        // Gestor devolve novamente
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/vis-cadastro`);
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Terceira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar alerta da 3ª devolução
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await limparNotificacoes(page);
        await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: `Revisão do cadastro da unidade ${UNIDADE_ALVO} devolvida para ajustes` }).first()).toBeVisible();

        // Chefe verifica que histórico tem TODAS as devoluções (a última primeiro)
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Terceira devolução');
        await expect(modal.getByTestId('cell-resultado-1')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-1')).toHaveText('Segunda devolução');
        await fecharHistoricoAnalise(page);
    });

    test('6. Cenário 5: Cancelar disponibilização', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
        await expect(page.getByRole('dialog')).toBeHidden();
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });
});


