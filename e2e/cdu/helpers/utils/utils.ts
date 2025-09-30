import {Locator, Page} from '@playwright/test';

export function gerarNomeUnico(prefixo: string): string {
  return `${prefixo} ${Date.now()}`;
}

/**
 * Localiza um elemento preferindo test-id e mantendo fallback por role/text/locator.
 * - testId: string | undefined — se fornecido, tenta `page.getByTestId(testId)`
 * - role: Playwright role string (ex: 'button') e name: texto para getByRole fallback
 * - locatorFallback: css selector ou locator string para fallback adicional
 */
export async function localizarPorTestIdOuRole(pageOrLocator: Page | Locator, testId?: string, role?: string, name?: string, locatorFallback?: string): Promise<Locator> {
  const page = pageOrLocator as Page;
  if (testId) {
    const byTestId = page.getByTestId(testId);
    if ((await byTestId.count()) > 0) return byTestId;
  }

  if (role && name) {
    const byRole = page.getByRole(role as any, { name });
    if ((await byRole.count()) > 0) return byRole;
  }

  if (name) {
    const byText = page.getByText(name);
    if ((await byText.count()) > 0) return byText;
  }

  if (locatorFallback) {
    const byLocator = page.locator(locatorFallback);
    if ((await byLocator.count()) > 0) return byLocator;
  }

  // Último recurso: retornar locator do testId (mesmo que vazio) para que o chamador lide com erro.
  return page.getByTestId(testId || '');
}

/**
 * Clica em um elemento preferindo test-id, com fallback para role/text/locator.
 * Retorna true se clicou com sucesso, false caso contrário.
 */
export async function clicarPorTestIdOuRole(page: Page, testId?: string, role?: string, name?: string, locatorFallback?: string): Promise<boolean> {
  const el = await localizarPorTestIdOuRole(page, testId, role, name, locatorFallback);
  if ((await el.count()) === 0) return false;
  const first = el.first();

  // Tenta o click normal primeiro; se falhar por backdrop/animation, aplica fallbacks seguros.
  try {
    await first.click({ timeout: 5000 });
    return true;
  } catch (err) {
    // 1) Tentar elemento via elementHandle + evaluate (click via DOM)
    try {
      const handle = await first.elementHandle();
      if (handle) {
        await handle.evaluate((node: HTMLElement) => (node as HTMLElement).click());
        return true;
      }
    } catch (e) {
      // continua para próximo fallback
    }

    // 2) Tentar click forçado (force: true)
    try {
      await first.click({ force: true });
      return true;
    } catch (e) {
      // continua para próximo fallback
    }

    // 3) Se houver backdrop que intercepta pointer events, desabilitar pointerEvents via evaluateAll e tentar novamente
    try {
      await page.locator('.modal-backdrop, .modal.fade.show').evaluateAll((nodes: Element[]) => {
        nodes.forEach(n => {
          (n as HTMLElement).style.pointerEvents = 'none';
        });
      });
      await first.click({ timeout: 3000 });
      return true;
    } catch (e) {
      // falha final — retornar false para que o chamador trate/report
      return false;
    }
  }
}
