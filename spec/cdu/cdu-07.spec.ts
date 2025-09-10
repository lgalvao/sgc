import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsChefe} from '~/utils/auth';
import {loginAndClickFirstProcess} from './test-helpers';

test.describe('CDU-07: Detalhar subprocesso', () => {
    test.beforeEach(async ({page}) => {
     await loginAsChefe(page);
    });

    test('deve mostrar detalhes do subprocesso para CHEFE', async ({page}) => {
     // Clicar em processo
        await loginAndClickFirstProcess(page, loginAsChefe);
 
     // Deve mostrar subprocesso
     await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+/);
 
     // Verificar elementos básicos do subprocesso
     await expect(page.getByTestId('subprocesso-header')).toBeVisible();
     await expect(page.getByTestId('processo-info')).toBeVisible();
   });
});