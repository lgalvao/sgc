/**
 * Utilitários para manipulação de estruturas de árvore hierárquicas.
 */

/**
 * Achata uma estrutura de árvore hierárquica em uma lista plana.
 *
 * @example
 * // Com subordinadas (unidades)
 * const arvore = [
 *   { codigo: 1, subordinadas: [{ codigo: 2 }] },
 *   { codigo: 3 }
 * ];
 * const plano = flattenTree(arvore, 'subordinadas');
 * // Resultado: [{ codigo: 1, ... }, { codigo: 2 }, { codigo: 3 }]
 *
 * @example
 * // Com filhos (processos)
 * const arvore = [
 *   { id: 1, filhos: [{ id: 2 }] },
 *   { id: 3 }
 * ];
 * const plano = flattenTree(arvore, 'filhos');
 * // Resultado: [{ id: 1, ... }, { id: 2 }, { id: 3 }]
 */
export function flattenTree<T extends Record<string, any>>(
    items: T[],
    childrenKey: string = 'subordinadas'
): T[] {
    const result: T[] = [];

    for (const item of items) {
        result.push(item);

        if (item[childrenKey] && Array.isArray(item[childrenKey])) {
            result.push(...flattenTree(item[childrenKey], childrenKey));
        }
    }

    return result;
}
