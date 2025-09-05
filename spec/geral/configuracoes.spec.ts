import {expect, test} from "@playwright/test";
import {login} from "~/utils/auth";

test.describe('Configurações', () => {
    test.beforeEach(async ({page}) => {
        await login(page);
        await page.goto(`/configuracoes`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título e os campos de configuração com valores padrão', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Configurações do Sistema'})).toBeVisible();

        // Verificar campo Dias para inativação de processos
        const diasInativacaoProcessoInput = page.getByLabel('Dias para inativação de processos');
        await expect(diasInativacaoProcessoInput).toBeVisible();
        await expect(diasInativacaoProcessoInput).toHaveValue('10'); // Valor padrão do store

        // Verificar campo Dias para indicação de alerta como novo
        const diasAlertaNovoInput = page.getByLabel('Dias para indicação de alerta como novo');
        await expect(diasAlertaNovoInput).toBeVisible();
        await expect(diasAlertaNovoInput).toHaveValue('7'); // Valor padrão do store
    });

    test('deve permitir alterar as configurações e salvá-las', async ({page}) => {
        const diasInativacaoProcessoInput = page.getByLabel('Dias para inativação de processos');
        const diasAlertaNovoInput = page.getByLabel('Dias para indicação de alerta como novo');
        const salvarButton = page.getByRole('button', {name: 'Salvar'});

        // Alterar valores
        await diasInativacaoProcessoInput.fill('45');
        await diasAlertaNovoInput.fill('10');

        // Clicar em salvar
        await salvarButton.click();

        // Verificar mensagem de sucesso
        await expect(page.getByText('Configurações salvas com sucesso!')).toBeVisible();

        // Recarregar a página para verificar persistência
        await page.reload();
        await page.waitForLoadState('networkidle');

        // Verificar se os novos valores persistem
        await expect(diasInativacaoProcessoInput).toHaveValue('45');
        await expect(diasAlertaNovoInput).toHaveValue('10');
    });
});