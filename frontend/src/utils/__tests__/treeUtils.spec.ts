import {describe, expect, it} from 'vitest';
import {flattenTree, organizarArvoreUnidades, TITULO_GRUPO_ZONAS_ELEITORAIS} from '@/utils/treeUtils';

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

    describe('organizarArvoreUnidades', () => {
        const config = {
            obterCodigo: (item: any) => item.codigo,
            obterRotulo: (item: any) => item.nome,
            obterSigla: (item: any) => item.sigla,
            obterTipo: (item: any) => item.tipo,
            obterFilhos: (item: any) => item.filhas,
            clonarComFilhos: (item: any, filhas: any[]) => ({...item, filhas}),
            criarGrupoZonas: (identificadorGrupo: any, filhas: any[]) => ({
                codigo: -999,
                nome: TITULO_GRUPO_ZONAS_ELEITORAIS,
                tipo: 'AGRUPADOR_VISUAL',
                filhas
            }),
            criarIdentificadorGrupoFilhos: (item: any) => item.codigo,
        };

        it('deve ordenar as Zonas Eleitorais numericamente', () => {
            const items = [
                {codigo: 1, sigla: "Z.E. 10", nome: "ZE 10", tipo: "ZONA ELEITORAL"},
                {codigo: 2, sigla: "Z.E. 001", nome: "ZE 001", tipo: "ZONA ELEITORAL"},
                {codigo: 3, sigla: "Z.E. 2", nome: "ZE 2", tipo: "ZONA ELEITORAL"},
            ];

            const resultado = organizarArvoreUnidades(items, 0, config);

            expect(resultado).toHaveLength(1);
            const grupoZonas = resultado[0];
            expect(grupoZonas.nome).toBe(TITULO_GRUPO_ZONAS_ELEITORAIS);

            const siglasOrdenadas = grupoZonas.filhas.map((i: any) => i.sigla);
            expect(siglasOrdenadas).toEqual(["Z.E. 001", "Z.E. 2", "Z.E. 10"]);
        });
    });
});
