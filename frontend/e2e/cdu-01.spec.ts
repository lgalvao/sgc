import { test, expect } from '@playwright/test';
import { loginComoAdmin, loginComoGestor } from './helpers/auth';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

// --- Configuração e Leitura de Dados ---
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const testData = JSON.parse(fs.readFileSync(path.join(__dirname, '.test-data.json'), 'utf-8'));

// --- Testes ---

test.describe('CDU-01: Fluxo de Login e Seleção de Perfil', () => {

    test.describe('Login Convencional (via UI)', () => {

        test('deve fazer login e ir direto para o painel com perfil único', async ({ page }) => {
            // O usuário "CHEFE" tem apenas um perfil no seed
            await page.goto('/login');
            await page.getByTestId('input-titulo').fill(testData.chefeUsername);
            await page.getByTestId('input-senha').fill('123'); // A senha é ignorada pelo mock de autenticação
            await page.getByTestId('botao-entrar').click();

            // Verificação: Deve ir direto para o painel
            await expect(page).toHaveURL('/painel');
            await expect(page.getByTestId('titulo-processos')).toBeVisible();
        });

        test('deve mostrar seleção de perfis para usuário com múltiplos perfis', async ({ page }) => {
            // O usuário "MULTI PERFIL" tem múltiplos perfis no seed
            await page.goto('/login');
            await page.getByTestId('input-titulo').fill(testData.multiPerfilUsername);
            await page.getByTestId('input-senha').fill('123');
            await page.getByTestId('botao-entrar').click();

            // Verificação: Deve mostrar a tela de seleção de perfil
            await expect(page.getByText('Selecione o perfil e a unidade')).toBeVisible();
            await expect(page.getByTestId('select-perfil-unidade')).toBeVisible();

            // Ação: Seleciona um perfil e entra
            await page.getByTestId('select-perfil-unidade').selectOption({ label: 'ADMIN - SEDOC' });
            await page.getByTestId('botao-entrar').click();

            // Verificação Final: Vai para o painel
            await expect(page).toHaveURL('/painel');
            await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
        });
    });

    test.describe('Login Programático (Helpers de Teste)', () => {

        test('deve funcionar para loginComoAdmin', async ({ page }) => {
            await loginComoAdmin(page);
            await expect(page).toHaveURL('/painel');
            await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
        });

        test('deve funcionar para loginComoGestor', async ({ page }) => {
            await loginComoGestor(page);
            await expect(page).toHaveURL('/painel');
            await expect(page.getByTitle('Configurações do sistema')).not.toBeVisible();
        });
    });
});
