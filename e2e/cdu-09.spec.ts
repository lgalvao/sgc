import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor
} from './helpers/helpers-analise.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page) {
    await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_221$/);
}

test.describe.serial('CDU-09 - Disponibilizar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;

    const timestamp = Date.now();
    const descProcesso = `Proc 9 ${timestamp}`;

    test('Fluxo completo de disponibilização e devolução', async ({page, autenticadoComoAdmin}) => {

        await test.step('Preparação: Admin cria e inicia processo', async () => {
            await criarProcesso(page, {
                descricao: descProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_22'],
                iniciar: true
            });
            await fazerLogout(page);
        });

        await test.step('Cenario 1: Validacao - Atividade sem conhecimento', async () => {
            await login(page, USUARIO_CHEFE, SENHA_CHEFE);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            const atividadeDesc = `Atividade Incompleta ${timestamp}`;
            await adicionarAtividade(page, atividadeDesc);

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            const erroInline = page.getByTestId('atividade-erro-validacao');
            await expect(erroInline).toBeVisible();
            await expect(erroInline).toContainText(/conhecimento/i);

            await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Corretivo');
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
            await page.getByRole('button', {name: 'Cancelar'}).click();
        });

        await test.step('Cenario 2: Caminho feliz - Disponibilizar Cadastro', async () => {
            const atividadeDesc = `Atividade Validada ${timestamp}`;
            await adicionarAtividade(page, atividadeDesc);
            await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Valido');

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();

            await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('Cenario 3: Devolucao e Historico de Analise', async () => {
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            await page.getByTestId('btn-acao-devolver').click();
            const motivo = 'Faltou detalhar melhor os conhecimentos técnicos.';
            await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivo);
            await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);

            await login(page, USUARIO_CHEFE, SENHA_CHEFE);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);

            await navegarParaAtividades(page);
            const modal = await abrirHistoricoAnalise(page);
            await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
            await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivo);
            await page.getByRole('button', {name: 'Fechar'}).click();

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        });
    });
});
