import { Page, expect } from '@playwright/test';

export async function verificarNavegacaoPaginaSubprocesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/subprocesso\/\d+/);
}
