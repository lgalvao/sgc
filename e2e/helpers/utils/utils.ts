import { Page } from '@playwright/test';
import { logger } from './logger';

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
    try {
        // Mapeamento de siglas para códigos (STIC = 2)
        const codigosUnidade: Record<string, number> = {
            'STIC': 2,
            'SGP': 3,
            'COEDE': 4,
            'SEMARE': 5,
        };
        
        const codigo = codigosUnidade[siglaUnidade];
        if (!codigo) {
            logger.warn(`Unidade ${siglaUnidade} não mapeada para cleanup`);
            return;
        }
        
        // Usar endpoint E2E (POST em vez de DELETE por questões de segurança)
        await page.request.post(`http://localhost:10000/api/e2e/processos/unidade/${codigo}/limpar`);
    } catch (error) {
        // Falha silenciosa - se não conseguir limpar, teste tentará rodar mesmo assim
        logger.warn('Aviso: Não foi possível limpar processos:', error);
    }
}
