import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
} from './helpers/helpers-atividades.js';
import {
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    abrirHistoricoAnalise,
    fecharHistoricoAnalise,
} from './helpers/helpers-analise.js';
import {
    limparNotificacoes,
    verificarPaginaPainel,
} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}(?:/)?$`));
}

test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const timestamp = Date.now();
    const descProcessoRevisao = `Rev 10 ${timestamp}`;

    test('1. Setup: Preparar processo de revisão e atividades iniciais', async ({_resetAutomatico, request, page}) => {
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
        expect(processo.codigo).toBeGreaterThan(0);

        // Chefe revisa atividades (muda situação para EM_ANDAMENTO)
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade revisão nova ${timestamp}`);
        await adicionarConhecimento(page, `Atividade revisão nova ${timestamp}`, 'Conhecimento revisão');
        await page.getByTestId('btn-nav-voltar').click();
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
    });

    test('1.1 Cenário adicional: primeiro acesso direto ao cadastro carrega o subprocesso', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();
        await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible();
    });

    test('2. Cenário 1: Validação - Atividade sem conhecimento', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        const atividadeIncompleta = `Atividade incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeIncompleta);
        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();

        await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento corretivo');
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
    });

    test('3. Cenário 2: Caminho feliz - Disponibilizar revisão', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const modalConfirmacao = page.getByRole('dialog');
        await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO)).toBeVisible();
        await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO)).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/disponibilizada?|Disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar alerta para o gestor superior
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await expect(page.getByTestId('tbl-alertas')).toContainText(TEXTOS.alerta.SUCESSO_REVISAO_DISPONIBILIZADA(UNIDADE_ALVO));

        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.REVISAO_CADASTRO_DISPONIBILIZADA);
    });

    test('4. Cenário 3: Devolução e Histórico', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-devolver').click();
        const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar movimentação de devolução
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.REVISAO_CADASTRO_DEVOLVIDA);

        // Verificar alerta para o chefe da unidade
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await limparNotificacoes(page);
        await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: TEXTOS.alerta.REVISAO_DEVOLVIDA(UNIDADE_ALVO) })).toBeVisible();

        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);
        await fecharHistoricoAnalise(page);

        await disponibilizarCadastro(page);
        await verificarPaginaPainel(page);
    });

    test('5. Cenário 4: Histórico retém as análises após nova disponibilização', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Segunda devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar movimentação de devolução (2ª vez)
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('tbl-movimentacoes').locator('tr', { hasText: TEXTOS.movimentacao.REVISAO_CADASTRO_DEVOLVIDA }).first()).toBeVisible();

        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await disponibilizarCadastro(page);
        await verificarPaginaPainel(page);

        // Gestor devolve novamente
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Terceira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar alerta da 3ª devolução
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await limparNotificacoes(page);
        await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: TEXTOS.alerta.REVISAO_DEVOLVIDA(UNIDADE_ALVO) }).first()).toBeVisible();

        // Chefe verifica que histórico tem TODAS as devoluções (a última primeiro)
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Terceira devolução');
        await expect(modal.getByTestId('cell-resultado-1')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-1')).toHaveText('Segunda devolução');
        await fecharHistoricoAnalise(page);
    });

    test('6. Cenário 5: Cancelar disponibilização', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
        await expect(page.getByRole('dialog')).toBeHidden();
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });
});
