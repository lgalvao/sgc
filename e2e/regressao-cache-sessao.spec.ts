import {expect, test} from './fixtures/complete-fixtures.js';
import {autenticar, USUARIOS} from './helpers/helpers-auth.js';
import {fazerLogout, limparNotificacoes} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso, criarProcesso, extrairProcessoCodigo} from './helpers/helpers-processos.js';

test.describe('Regressão - cache de sessão no subprocesso', () => {
    test('deve habilitar atividades para CHEFE após logout de ADMIN sem limpar caches da SPA', async ({
        _resetAutomatico,
        page,
        _autenticadoComoAdmin
    }) => {
        const descricao = `Regressao cache subprocesso ${Date.now()}`;
        const unidade = 'ASSESSORIA_12';

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade,
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        await acessarDetalhesProcesso(page, descricao);
        const codProcesso = await extrairProcessoCodigo(page);

        // Primeiro acesso como ADMIN para deixar o contexto do subprocesso carregado na SPA.
        await page.goto(`/processo/${codProcesso}/${unidade}`);
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${unidade}(?:\\?.*)?$`));
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();

        // Logout e novo login na mesma aba, sem limpar storage nem reiniciar a aplicação.
        await fazerLogout(page);
        await autenticar(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await page.waitForURL(/\/painel(?:\?|$)/);
        await limparNotificacoes(page);

        // Acessa o mesmo subprocesso imediatamente após o login do CHEFE.
        await page.goto(`/processo/${codProcesso}/${unidade}`);
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${unidade}(?:\\?.*)?$`));

        const cardCadastro = page.getByTestId('card-subprocesso-atividades');
        await expect(cardCadastro).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeHidden();

        await cardCadastro.click();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${unidade}/cadastro(?:\\?.*)?$`));
    });
});
