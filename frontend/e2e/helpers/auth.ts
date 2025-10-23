import { Page } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

// --- Leitura dos Dados de Teste ---
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const testData = JSON.parse(fs.readFileSync(path.join(__dirname, '..', '.test-data.json'), 'utf-8'));

// --- Funções Internas de Baixo Nível ---

/**
 * Realiza a autenticação programática no backend para criar uma sessão.
 */
async function autenticarComo(page: Page, username: string) {
    await page.request.post('http://localhost:10000/api/test/login', {
        data: { username: username },
    });
}

/**
 * Injeta o estado do perfil do usuário no localStorage do navegador.
 */
async function injetarPerfil(page: Page, username: string, perfil: string, siglaUnidade: string) {
    await page.evaluate(([username, perfil, siglaUnidade]) => {
        localStorage.setItem('idServidor', username);
        localStorage.setItem('perfilSelecionado', perfil);
        localStorage.setItem('unidadeSelecionada', siglaUnidade);
    }, [username, perfil, siglaUnidade]);
}

/**
 * Orquestra o login programático completo: autentica, navega, injeta e recarrega.
 */
async function loginProgramatico(page: Page, username: string, perfil: string, siglaUnidade: string) {
    await autenticarComo(page, username);
    await page.goto('/painel');
    await injetarPerfil(page, username, perfil, siglaUnidade);
    await page.reload();
}

// --- Funções de Abstração Semântica (DSL) ---

export async function loginComoAdmin(page: Page) {
    await loginProgramatico(page, testData.adminUsername, 'ADMIN', 'SEDOC');
}

export async function loginComoGestor(page: Page) {
    await loginProgramatico(page, testData.gestorUsername, 'GESTOR', 'SGP');
}

export async function loginComoChefe(page: Page) {
    await loginProgramatico(page, testData.chefeUsername, 'CHEFE', 'STIC');
}

export async function loginComoServidor(page: Page) {
    await loginProgramatico(page, testData.servidorUsername, 'SERVIDOR', 'STIC');
}
