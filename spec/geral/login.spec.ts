import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';

test.describe('Login', () => {
    test('deve permitir o login com credenciais válidas e navegar para o painel', async ({page}) => {
        // Navegar para a página de login
        await page.goto('/');

        // Preencher o formulário de login (usa o servidor ID 1, que tem múltiplos perfis)
        await page.getByLabel('Título eleitoral').fill('7');
        await page.getByLabel('Senha').fill('123');

        // Clicar no botão de login (primeira vez)
        await page.getByRole('button', {name: 'Entrar'}).click();

        // Verificar se o seletor de perfil/unidade apareceu
        const seletorDePerfil = page.getByLabel('Selecione o Perfil e a Unidade');
        await expect(seletorDePerfil).toBeVisible();

        // Selecionar um perfil específico para garantir a consistência do teste
        await seletorDePerfil.selectOption({label: 'ADMIN - SEDOC'});

        // Clicar no botão de login (segunda vez)
        await page.getByRole('button', {name: 'Entrar'}).click();

        // Verificar se a navegação para a página /painel ocorreu
        await expect(page).toHaveURL(`/painel`);
        await expect(page.getByTestId('titulo-processos')).toBeVisible(); // Verifica se um elemento da página do painel está visível
    });
});