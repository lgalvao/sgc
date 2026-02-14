import {expect, test} from './fixtures/complete-fixtures.js';
import {USUARIOS, login} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';
import {acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';

test.describe.serial('Regressão - Erros reportados (Erros.docx)', () => {
    test('ADMIN no menu Unidades deve visualizar árvore do tribunal', async ({page, autenticadoComoAdmin}) => {
        await page.getByRole('link', {name: /Unidades/i}).click();
        await expect(page).toHaveURL(/\/unidades/);
        await expect(page.getByTestId('link-arvore-unidade-SECRETARIA_1')).toBeVisible();
        await expect(page.getByTestId('link-arvore-unidade-SECRETARIA_2')).toBeVisible();
    });

    test('Importar atividades não deve retornar "Acesso Negado"', async ({page, autenticadoComoAdmin}) => {
        const descricao = `Regressao Importacao ${Date.now()}`;
        const unidadeAlvo = 'ASSESSORIA_11';

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: unidadeAlvo,
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await acessarSubprocessoChefeDireto(page, descricao, unidadeAlvo);
        await AtividadeHelpers.navegarParaAtividades(page);

        await page.getByTestId('btn-empty-state-importar').click();
        const modal = page.getByRole('dialog');
        await expect(modal.getByText('Importação de atividades')).toBeVisible();
        await expect(page.getByText(/Acesso Negado/i)).not.toBeVisible();
    });

    test('Criar atribuição temporária deve efetivar ação ao clicar em CRIAR', async ({page, autenticadoComoAdmin}) => {
        await page.getByRole('link', {name: /Unidades/i}).click();
        await page.getByTestId('link-arvore-unidade-SECRETARIA_2').click();
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();

        const selectUsuario = page.getByTestId('select-usuario');
        const usuario = await selectUsuario.locator('option:not([disabled])').first().getAttribute('value');
        await selectUsuario.selectOption(usuario ?? '');
        await page.getByTestId('input-data-inicio').fill('2030-01-01');
        await page.getByTestId('input-data-termino').fill('2030-12-31');
        await page.getByTestId('textarea-justificativa').fill('Teste de regressão do botão criar');
        await page.getByTestId('cad-atribuicao__btn-criar-atribuicao').click();

        await expect(page.getByText(/Atribuição criada com sucesso/i).first()).toBeVisible();
    });
});
