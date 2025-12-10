import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import ArvoreUnidades from '@/components/ArvoreUnidades.vue';
import type {Unidade} from '@/types/tipos';

describe('ArvoreUnidades - Bug do usuário: COORD_11 desmarcada com filhas marcadas', () => {
    const criarUnidades = (): Unidade[] => [
        {
            codigo: 1,
            sigla: 'SECRETARIA_1',
            nome: 'Secretaria 1',
            tipo: 'INTEROPERACIONAL',
            isElegivel: true,
            idServidorTitular: 0,
            responsavel: null,
            filhas: [
                {
                    codigo: 11,
                    sigla: 'ASSESSORIA_11',
                    nome: 'Assessoria 11',
                    tipo: 'OPERACIONAL',
                    isElegivel: true,
                    idServidorTitular: 0,
                    responsavel: null,
                    filhas: []
                },
                {
                    codigo: 12,
                    sigla: 'ASSESSORIA_12',
                    nome: 'Assessoria 12',
                    tipo: 'OPERACIONAL',
                    isElegivel: true,
                    idServidorTitular: 0,
                    responsavel: null,
                    filhas: []
                },
                {
                    codigo: 13,
                    sigla: 'COORD_11',
                    nome: 'Coordenadoria 11',
                    tipo: 'INTERMEDIARIA',
                    isElegivel: false,
                    idServidorTitular: 0,
                    responsavel: null,
                    filhas: [
                        {
                            codigo: 131,
                            sigla: 'SECAO_111',
                            nome: 'Seção 111',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            idServidorTitular: 0,
                            responsavel: null,
                            filhas: []
                        },
                        {
                            codigo: 132,
                            sigla: 'SECAO_112',
                            nome: 'Seção 112',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            idServidorTitular: 0,
                            responsavel: null,
                            filhas: []
                        },
                        {
                            codigo: 133,
                            sigla: 'SECAO_113',
                            nome: 'Seção 113',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            idServidorTitular: 0,
                            responsavel: null,
                            filhas: []
                        }
                    ]
                }
            ]
        }
    ];

    it('COORD_11 deve estar INDETERMINADA quando 2 de 3 filhas selecionadas', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: criarUnidades(),
                modelValue: [132, 133] // SECAO_112 e SECAO_113 (2 de 3)
            }
        });

        const vm = wrapper.vm as any;
        const coord11 = criarUnidades()[0].filhas![2];
        const estado = vm.getEstadoSelecao(coord11);

        // COORD_11 tem 3 filhas elegíveis: 131, 132, 133
        // Apenas 2 estão selecionadas: 132, 133
        // Resultado esperado: indeterminate
        expect(estado).toBe('indeterminate');
    });

    it('COORD_11 deve estar MARCADA quando todas as 3 filhas selecionadas', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: criarUnidades(),
                modelValue: [131, 132, 133] // Todas as 3 filhas
            }
        });

        const vm = wrapper.vm as any;
        const coord11 = criarUnidades()[0].filhas![2];

        const estado = vm.getEstadoSelecao(coord11);
        expect(estado).toBe(true);
    });
});
