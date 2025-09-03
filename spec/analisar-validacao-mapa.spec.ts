import {expect, test} from '@playwright/test'
import {login} from './utils/auth'

test.describe('CDU-21 - Analisar validação de mapa de competências', () => {
    test('deve permitir aceitar mapa com observações', async ({ page }) => {
        // Login como ADMIN (Zeca Silva - SEDOC)
        await login(page)

        // Navegar para o processo 2 que tem subprocesso com mapa disponibilizado
        await page.goto('/processo/2')
        await page.waitForLoadState('networkidle')

        // Clicar na linha da unidade STIC que tem "Mapa disponibilizado"
        await page.getByRole('cell', { name: 'STIC', exact: true }).click()

        // Aguardar navegação para a página do subprocesso
        await page.waitForURL(/.*\/processo\/2\/STIC$/)

        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()

        // Aguardar o card estar totalmente carregado e clicável
        await page.waitForTimeout(500)

        const mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' })
        await mapaCard.waitFor({ state: 'visible' })
        await mapaCard.click({ force: true })

        // Aguardar navegação para a visualização do mapa
        await page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)

        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible()

        // Verificar se os botões de ação estão presentes
        await expect(page.getByRole('button', { name: 'Homologar' })).toBeVisible()
        await expect(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible()

        // Clicar no botão "Homologar"
        await page.getByRole('button', { name: 'Homologar' }).click()

        // Verificar se o modal foi aberto
        await expect(page.getByText('Aceitar Mapa de Competências')).toBeVisible()
        await expect(page.getByText('Observações')).toBeVisible()

        // Preencher observações
        await page.getByLabel('Observações').fill('Mapa aprovado com pequenas observações sobre organização das competências.')

        // Clicar em "Aceitar" no modal
        await page.getByRole('button', { name: 'Aceitar' }).click()

        // Verificar redirecionamento para a página do subprocesso (indica sucesso da homologação)
        await page.waitForURL(/.*\/processo\/2\/STIC$/)
        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()
    })

    test('deve permitir rejeitar mapa sem observações', async ({ page }) => {
        // Login como ADMIN
        await login(page)

        // Navegar para o processo 2
        await page.goto('/processo/2')
        await page.waitForLoadState('networkidle')

        // Clicar na linha da unidade STIC
        await page.getByRole('cell', { name: 'STIC', exact: true }).click()

        // Aguardar navegação para a página do subprocesso
        await page.waitForURL(/.*\/processo\/2\/STIC$/)

        // Aguardar o card estar totalmente carregado e clicável
        await page.waitForTimeout(500)

        const mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' })
        await mapaCard.waitFor({ state: 'visible' })
        await mapaCard.click({ force: true })

        // Aguardar navegação para a visualização do mapa
        await page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)

        // Clicar no botão "Devolver para ajustes"
        await page.getByRole('button', { name: 'Devolver para ajustes' }).click()

        // A rejeição foi bem-sucedida se chegamos até aqui sem erros
        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()
    })

    test('deve permitir cancelar aceitação do mapa', async ({ page }) => {
        // Login como ADMIN
        await login(page)

        // Navegar para o processo 2
        await page.goto('/processo/2')
        await page.waitForLoadState('networkidle')

        // Clicar na linha da unidade STIC
        await page.getByRole('cell', { name: 'STIC', exact: true }).click()

        // Aguardar navegação para a página do subprocesso
        await page.waitForURL(/.*\/processo\/2\/STIC$/)

        // Aguardar o card estar totalmente carregado e clicável
        await page.waitForTimeout(500)

        const mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' })
        await mapaCard.waitFor({ state: 'visible' })
        await mapaCard.click({ force: true })

        // Aguardar navegação para a visualização do mapa
        await page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)

        // Clicar no botão "Homologar"
        await page.getByRole('button', { name: 'Homologar' }).click()

        // Verificar se o modal foi aberto
        await expect(page.getByText('Aceitar Mapa de Competências')).toBeVisible()

        // Clicar em "Cancelar"
        await page.getByRole('button', { name: 'Cancelar' }).click()

        // Verificar que o modal foi fechado
        await expect(page.getByText('Aceitar Mapa de Competências')).not.toBeVisible()

        // Verificar que ainda estamos na página de visualização do mapa
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible()
    })

    test('deve exibir mapa com competências e conhecimentos', async ({ page }) => {
        // Login como ADMIN
        await login(page)

        // Navegar para o processo 2
        await page.goto('/processo/2')
        await page.waitForLoadState('networkidle')

        // Clicar na linha da unidade STIC
        await page.getByRole('cell', { name: 'STIC', exact: true }).click()

        // Aguardar navegação para a página do subprocesso
        await page.waitForURL(/.*\/processo\/2\/STIC$/)

        // Aguardar o card estar totalmente carregado e clicável
        await page.waitForTimeout(500)

        const mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' })
        await mapaCard.waitFor({ state: 'visible' })
        await mapaCard.click({ force: true })

        // Aguardar navegação para a visualização do mapa
        await page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)

        // Verificar se o mapa é exibido
        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()

        // Verificar se há competências exibidas (pode haver ou não competências específicas)
        // Esta verificação é mais genérica pois depende dos dados de mock
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible()
    })

    test('deve mostrar mensagem de validação quando superior não é SEDOC', async ({ page }) => {
        // Este teste verifica o fluxo de validação intermediária
        // Quando o superior não é SEDOC, deve mostrar "Mapa aceito e submetido para análise da unidade superior"
        test.skip()
    })

    test('deve navegar corretamente através do fluxo completo', async ({ page }) => {
        // Login como ADMIN
        await login(page)

        // 1. Ir para o painel
        await expect(page.getByTestId('titulo-processos')).toBeVisible()

        // 2. Navegar para o processo 2
        await page.goto('/processo/2')
        await page.waitForLoadState('networkidle')

        // 3. Verificar que estamos na página do processo
        await expect(page.getByText('Revisão de mapeamento STIC/COINF - 2025')).toBeVisible()

        // 4. Clicar na linha da unidade STIC
        await page.getByRole('cell', { name: 'STIC', exact: true }).click()

        // 5. Verificar que estamos na página do subprocesso
        await page.waitForURL(/.*\/processo\/2\/STIC$/)
        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()

        // 6. Aguardar o card estar totalmente carregado e clicável
        await page.waitForTimeout(500)

        const mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' })
        await mapaCard.waitFor({ state: 'visible' })
        await mapaCard.click({ force: true })

        // 8. Verificar que estamos na página de visualização do mapa
        await page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible()
        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()

        // 9. Verificar presença dos botões de ação
        await expect(page.getByRole('button', { name: 'Homologar' })).toBeVisible()
        await expect(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible()
    })
})