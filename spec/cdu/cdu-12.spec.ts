import {expect, test} from '@playwright/test';
import {expectTextVisible, loginAsAdmin} from './test-helpers';

test.describe('CDU-12 - Verificar impactos no mapa de competências', () => {

    test('Deve exibir mensagem de "Nenhum impacto" quando não houver divergências (ADMIN)', async ({page}) => {
        await loginAsAdmin(page);

        // Navegar diretamente para a tela de edição de mapa (CadMapa.vue)
        await page.goto('/processo/2/SESEL/mapa');
        await expect(page).toHaveURL(/\/processo\/2\/SESEL\/mapa/);
        await page.waitForLoadState('networkidle');

        // Clicar no botão 'Impactos no mapa'
        await page.getByTestId('impactos-mapa-button').waitFor({state: 'visible'});
        await page.getByTestId('impactos-mapa-button').click();

        // Verificar se o modal de impacto NÃO aparece e se a notificação de nenhum impacto é exibida
        await expect(page.getByTestId('impacto-mapa-modal')).not.toBeVisible();
        await expectTextVisible(page, 'Nenhum impacto no mapa da unidade.');
    });

});
