import {Locator} from '@playwright/test';

// TODO esse arquivo pode ser mesclado com o 'utils.js' no mesmo diretorio...

/**
 * Localiza um elemento a partir de uma lista de possíveis locators.
 * Retorna o primeiro locator que corresponde a um elemento visível na página.
 * @param seletores - Um array de Locators a serem tentados em ordem.
 * @returns - O primeiro locator que encontrou uma correspondência.
 * @throws - Lança um erro se nenhum dos locators encontrar um elemento.
 */
export async function localizarElemento(seletores: Locator[]): Promise<Locator> {
    for (const seletor of seletores) {
        try {
            if ((await seletor.count()) > 0) {
                return seletor;
            }
        } catch {
            // Ignora erros de timeout ou outros que podem ocorrer se o seletor for inválido
            // e continua para o próximo da lista.
        }
    }
    throw new Error('Nenhum dos seletores fornecidos encontrou um elemento na página.');
}

/**
 * Preenche um campo (input, textarea) utilizando uma lista de seletores de fallback.
 * @param seletores - Array de locators para tentar encontrar o campo.
 * @param valor - O texto a ser inserido no campo.
 */
export async function preencherCampo(seletores: Locator[], valor: string): Promise<void> {
    const elemento = await localizarElemento(seletores);
    await elemento.fill(valor);
}

/**
 * Clica em um elemento utilizando uma lista de seletores de fallback.
 * @param seletores - Array de locators para tentar encontrar o elemento clicável.
 * @param options - Opções para o clique, como `force`.
 */
export async function clicarElemento(seletores: Locator[], options?: { force?: boolean }): Promise<void> {
    const elemento = await localizarElemento(seletores);
    await elemento.click(options);
}
