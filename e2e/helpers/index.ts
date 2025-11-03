/**
 * 🎯 HELPERS - ÍNDICE PRINCIPAL
 * 
 * Ponto de entrada único para todas as funções auxiliares organizadas por domínio.
 * Esta estrutura segue o Princípio da Responsabilidade Única e facilita a manutenção.
 * 
 * ESTRUTURA:
 * 📁 acoes/         - Ações por domínio (processo, modais, atividades)
 * 📁 verificacoes/  - Verificações por domínio (básicas, processo, UI)  
 * 📁 navegacao/     - Navegação, login e rotas
 * 📁 dados/         - Constantes e dados de teste
 * _utils/         - Utilitários gerais
 */

// ===== AÇÕES =====
export * from './acoes';

// ===== VERIFICAÇÕES =====
export * from './verificacoes';

// ===== NAVEGAÇÃO E LOGIN =====
export * from './navegacao';
export * from './auth';
export * from './authHelpers';

// ===== DADOS E CONSTANTES =====
export * from './dados';

// ===== UTILITÁRIOS GERAIS =====
export * from './utils';

import {Page} from '@playwright/test';

/**
 * Clica em um botão pelo nome
 */
export async function clicarBotao(page: Page, nome: string): Promise<void> {
    await page.getByRole('button', {name: nome}).click();
}
