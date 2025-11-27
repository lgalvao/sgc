import {Page} from '@playwright/test';
import {logger} from './logger';

/**
 * Gera um nome único adicionando um timestamp a um prefixo.
 * @param prefixo O prefixo para o nome único.
 * @returns O nome único.
 */
export function gerarNomeUnico(prefixo: string): string {
    return `${prefixo} ${Date.now()}`;
}

/**
 * Remove todos os processos que usam a unidade especificada.
 * @param page A instância da página do Playwright.
 * @param siglaUnidade A sigla da unidade.
 */
export async function limparProcessosCriadosComUnidade(page: Page, siglaUnidade: string): Promise<void> {
    // Mapeamento de siglas para códigos (STIC = 2)
    const codigosUnidade: Record<string, number> = {
        'STIC': 2,
        'SGP': 3,
        'COEDE': 4,
        'SEMARE': 5,
        'SEDESENV': 8,
        'SESEL': 9,
        'SEDIA': 10
    };

    const codigo = codigosUnidade[siglaUnidade];
    if (!codigo) {
        throw new Error(`Unidade ${siglaUnidade} não mapeada para cleanup`);
    }

    // Usar endpoint E2E (POST em vez de DELETE por questões de segurança)
    const response = await page.request.post(`http://localhost:10000/api/e2e/processos/unidade/${codigo}/limpar`);

    if (!response.ok()) {
        logger.warn(`Aviso: Falha ao limpar processos da unidade ${siglaUnidade}. Status: ${response.status()}`);
        // Não lança erro para não falhar o teste principal por causa de limpeza,
        // mas loga de forma visível.
    }
}
