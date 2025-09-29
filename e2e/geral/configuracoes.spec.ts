import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {login} from "../utils/auth";

test.describe('Configurações', () => {
    test.beforeEach(async ({page}) => {
        await login(page);
        await page.goto(`/configuracoes`);
        //await page.waitForLoadState('networkidle');
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
        //await page.waitForLoadState('networkidle');

        // Verificar se os novos valores persistem
        await expect(diasInativacaoProcessoInput).toHaveValue('45');
        await expect(diasAlertaNovoInput).toHaveValue('10');
    });

    test('deve validar campos obrigatórios e valores mínimos', async ({page}) => {
        const diasInativacaoProcessoInput = page.getByLabel('Dias para inativação de processos');
        const diasAlertaNovoInput = page.getByLabel('Dias para indicação de alerta como novo');

        // Verificar que os campos têm atributo min="1"
        await expect(diasInativacaoProcessoInput).toHaveAttribute('min', '1');
        await expect(diasAlertaNovoInput).toHaveAttribute('min', '1');

        // Verificar que os campos são obrigatórios
        await expect(diasInativacaoProcessoInput).toHaveAttribute('required');
        await expect(diasAlertaNovoInput).toHaveAttribute('required');

        // Testar valor zero (abaixo do mínimo)
        await diasInativacaoProcessoInput.fill('0');
        await diasAlertaNovoInput.fill('0');

        // Verificar que os valores foram preenchidos (mesmo que inválidos)
        await expect(diasInativacaoProcessoInput).toHaveValue('0');
        await expect(diasAlertaNovoInput).toHaveValue('0');

        // Testar valores negativos
        await diasInativacaoProcessoInput.fill('-5');
        await diasAlertaNovoInput.fill('-3');

        // Verificar que os valores negativos foram preenchidos (mesmo que inválidos)
        await expect(diasInativacaoProcessoInput).toHaveValue('-5');
        await expect(diasAlertaNovoInput).toHaveValue('-3');
    });

    test('deve exibir textos explicativos dos campos', async ({page}) => {
        // Verificar texto explicativo do campo de dias para inativação
        await expect(page.getByText('Dias depois da finalização de um processo para que seja considerado inativo.')).toBeVisible();

        // Verificar texto explicativo do campo de dias para alerta
        await expect(page.getByText('Dias depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.')).toBeVisible();
    });

    test('deve manter configurações após navegação', async ({page}) => {
        const diasInativacaoProcessoInput = page.getByLabel('Dias para inativação de processos');
        const diasAlertaNovoInput = page.getByLabel('Dias para indicação de alerta como novo');
        const salvarButton = page.getByRole('button', {name: 'Salvar'});

        // Alterar valores
        await diasInativacaoProcessoInput.fill('30');
        await diasAlertaNovoInput.fill('15');

        // Salvar
        await salvarButton.click();
        await expect(page.getByText('Configurações salvas com sucesso!')).toBeVisible();

        // Navegar para outra página
        await page.goto('/painel');
        await expect(page).toHaveURL(/\/painel/);

        // Voltar para configurações
        await page.goto('/configuracoes');
        await expect(page).toHaveURL(/\/configuracoes/);

        // Verificar se os valores persistem
        await expect(diasInativacaoProcessoInput).toHaveValue('30');
        await expect(diasAlertaNovoInput).toHaveValue('15');
    });
});