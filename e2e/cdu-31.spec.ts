import {expect, test} from './fixtures/complete-fixtures.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

/**
 * CDU-31 - Configurar sistema
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-31 - Configurar sistema', () => {

    test('Cenários CDU-31: ADMIN navega, valida entradas e persiste alterações de configurações', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);
        await expect(page.getByRole('heading', {name: TEXTOS.configuracoes.TITULO, exact: true})).toBeVisible();

        const campoDiasInativacao = page.getByLabel(TEXTOS.configuracoes.LABEL_DIAS_INATIVACAO);
        const campoDiasAlertaNovo = page.getByLabel(TEXTOS.configuracoes.LABEL_DIAS_ALERTA_NOVO);
        const botaoSalvar = page.getByRole('button', {name: TEXTOS.configuracoes.BOTAO_SALVAR});

        await expect(campoDiasInativacao).toBeVisible();
        await expect(campoDiasAlertaNovo).toBeVisible();
        await expect(botaoSalvar).toBeVisible();
        await expect(campoDiasInativacao).toHaveAttribute('min', '1');
        await expect(campoDiasInativacao).toHaveValue(/\d+/);
        await expect(campoDiasAlertaNovo).toHaveValue(/\d+/);

        const valorInicialInativacao = Number(await campoDiasInativacao.inputValue());
        const valorInicialAlerta = Number(await campoDiasAlertaNovo.inputValue());

        await campoDiasInativacao.fill('0');
        await botaoSalvar.click();
        await expect(page).toHaveURL(/\/configuracoes/);
        await expect(page.getByText(TEXTOS.configuracoes.SUCESSO_SALVAR)).toBeHidden();
        await page.reload();
        await expect(campoDiasInativacao).toHaveValue(String(valorInicialInativacao));
        await expect(campoDiasAlertaNovo).toHaveValue(String(valorInicialAlerta));

        const novoValorInativacao = String(valorInicialInativacao + 1);
        const novoValorAlerta = String(valorInicialAlerta + 1);
        await campoDiasInativacao.fill(novoValorInativacao);
        await campoDiasAlertaNovo.fill(novoValorAlerta);
        await botaoSalvar.click();
        await expect(page.getByText(TEXTOS.configuracoes.SUCESSO_SALVAR)).toBeVisible();

        await page.reload();
        await expect(campoDiasInativacao).toHaveValue(novoValorInativacao);
        await expect(campoDiasAlertaNovo).toHaveValue(novoValorAlerta);
    });
});
