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
 * 📁 utils/         - Utilitários gerais
 */

// ===== AÇÕES =====
export * from './acoes';

// ===== VERIFICAÇÕES =====
export * from './verificacoes';

// ===== NAVEGAÇÃO E LOGIN =====
export * from './navegacao';

// ===== DADOS E CONSTANTES =====
export * from './dados';

// ===== UTILITÁRIOS GERAIS =====
export * from './utils';

// ===== PAGES =====
export * from './pages/painel-page';
export * from './pages/processo-page';
