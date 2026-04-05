import {describe, expect, it} from 'vitest';
import {flattenTree} from '@/utils';

describe('treeUtils', () => {
    describe('flattenTree', () => {
        it('deve achatar uma arvore simples com subordinadas', () => {
            const arvore = [
                {codigo: 1, subordinadas: [{codigo: 2}]},
                {codigo: 3}
            ];
            const resultado = flattenTree(arvore, 'subordinadas');
            expect(resultado).toHaveLength(3);
            expect(resultado.map(i => i.codigo)).toEqual([1, 2, 3]);
        });

        it('deve achatar uma arvore com diferentes chaves de filhos', () => {
            const arvore = [
                {codigo: 1, filhos: [{codigo: 2}]},
                {codigo: 3}
            ];
            const resultado = flattenTree(arvore, 'filhos');
            expect(resultado).toHaveLength(3);
            expect(resultado.map(i => i.codigo)).toEqual([1, 2, 3]);
        });

        it('deve lidar com array vazio', () => {
            const resultado = flattenTree([]);
            expect(resultado).toEqual([]);
        });

        it('deve usar subordinadas como chave padrao', () => {
            const arvore = [
                {codigo: 1, subordinadas: [{codigo: 2}]}
            ];
            const resultado = flattenTree(arvore);
            expect(resultado).toHaveLength(2);
            expect(resultado.map(i => i.codigo)).toEqual([1, 2]);
        });

        it('deve lidar com itens sem a chave de filhos', () => {
            const arvore = [
                {codigo: 1}
            ];
            const resultado = flattenTree(arvore, 'subordinadas');
            expect(resultado).toHaveLength(1);
            expect(resultado[0].codigo).toBe(1);
        });

        it('deve lidar recursivamente com multiplos níveis', () => {
            const arvore = [
                {
                    codigo: 1,
                    children: [
                        {
                            codigo: 2,
                            children: [{codigo: 3}]
                        }
                    ]
                }
            ];
            const resultado = flattenTree(arvore, 'children');
            expect(resultado.map(i => i.codigo)).toEqual([1, 2, 3]);
        });
    });
});
