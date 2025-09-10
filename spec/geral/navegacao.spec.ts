import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login} from "~/utils/auth";

async function getBreadcrumbItemsText(page: Page) {
    const items = page.locator('[data-testid="breadcrumbs"] [data-testid="breadcrumb-item"]')
    const count = await items.count()
    const texts: string[] = []

    for (let i = 0; i < count; i++) {
        const el = items.nth(i)
        const text = (await el.innerText()).trim()
        texts.push(text.replace(/\s+/g, ' '))
    }
    return texts
}

async function lastBreadcrumbHasLink(page: Page) {
    const lastItem = page.locator('[data-testid="breadcrumbs"] [data-testid="breadcrumb-item"]').last()
    return await lastItem.locator('a').count() > 0
}

async function breadcrumbLinkHrefAt(page: Page, index: number) {
    const item = page.locator('[data-testid="breadcrumbs"] [data-testid="breadcrumb-item"]').nth(index)
    const link = item.locator('a').first()
    if (await link.count() === 0) return null
    return await link.getAttribute('href')
}

test.describe('Breadcrumbs - cobertura de cenários', () => {
    test.beforeEach(async ({page}) => await login(page))

    test('painel não exibe breadcrumbs', async ({page}) => {
        // Após login estamos no /painel
        await expect(page.locator('[data-testid="breadcrumbs"]')).toHaveCount(0)
    })

    test('navegação via navbar: breadcrumbs oculto na primeira renderização', async ({page}) => {
        // Clicar em Relatórios pela navbar (usa flag transitória)
        await page.getByRole('link', {name: /Relatórios/}).click()

        // Deve ocultar breadcrumbs nesta primeira renderização
        await expect(page.locator('[data-testid="breadcrumbs"]')).toHaveCount(0)
    })

    test('processo: mostra (home) > Processo e último não é link', async ({page}) => {
        await page.goto('/processo/1')
        await page.waitForLoadState('networkidle')

        const items = await getBreadcrumbItemsText(page)
        expect(items.length).toBeGreaterThanOrEqual(2)
        // Primeiro item é o ícone de home (texto pode estar vazio), então validamos a presença do container e do segundo item
        await expect(page.locator('[data-testid="breadcrumb-home-icon"]').first()).toBeVisible()
        expect(items[1]).toContain('Processo')

        // Último breadcrumb não é link
        expect(await lastBreadcrumbHasLink(page)).toBeFalsy()
    })

    test('processo > unidade: inclui SIGLA e links intermediários corretos', async ({page}) => {
        await page.goto('/processo/1/SESEL')
        await page.waitForLoadState('networkidle')

        const items = await getBreadcrumbItemsText(page)
        // Esperado: [home], Processo, SESEL
        expect(items.length).toBe(3)
        expect(items[1]).toContain('Processo')
        expect(items[2]).toContain('SESEL')

        // Link do Processo deve estar correto
        const hrefProcesso = await breadcrumbLinkHrefAt(page, 1)
        expect(hrefProcesso).toMatch(/\/processo\/1$/)

        // Último breadcrumb (SESEL) não é link
        expect(await lastBreadcrumbHasLink(page)).toBeFalsy()
    })

    test('processo > unidade > mapa: adiciona página final e mantém links corretos', async ({page}) => {
        await page.goto('/processo/1/SESEL/mapa')
        await page.waitForLoadState('networkidle')

        const items = await getBreadcrumbItemsText(page)

        // Esperado: [home], Processo, SESEL, Mapa
        expect(items.length).toBe(4)
        expect(items[1]).toContain('Processo')
        expect(items[2]).toContain('SESEL')
        expect(items[3]).toContain('Mapa')

        // Links intermediários atualizados
        const hrefProcesso = await breadcrumbLinkHrefAt(page, 1)
        expect(hrefProcesso).toMatch(/\/processo\/1$/)
        const hrefSigla = await breadcrumbLinkHrefAt(page, 2)
        expect(hrefSigla).toMatch(/\/processo\/1\/SESEL$/)

        // Último breadcrumb (Mapa) não é link
        expect(await lastBreadcrumbHasLink(page)).toBeFalsy()
    })

    test('unidade: mostra apenas (home) > SIGLA; último não é link', async ({page}) => {
        await page.goto('/unidade/SESEL')
        await page.waitForLoadState('networkidle')

        const items = await getBreadcrumbItemsText(page)
        // Esperado: [home], SESEL
        await expect(page.locator('[data-testid="breadcrumb-home-icon"]').first()).toBeVisible()
        expect(items[1]).toContain('SESEL')

        // Último breadcrumb não é link
        expect(await lastBreadcrumbHasLink(page)).toBeFalsy()
    })
})
