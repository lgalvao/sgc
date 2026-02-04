import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import {nextTick} from 'vue';
import ArvoreUnidades from '@/components/ArvoreUnidades.vue';
import type {Unidade} from '@/types/tipos';
import {setupComponentTest} from '@/test-utils/componentTestHelpers';

describe('ArvoreUnidades - Estado Visual (getEstadoSelecao)', () => {
    setupComponentTest();

    const criarUnidades = (): Unidade[] => [
        {
            codigo: 1,
            sigla: 'SECRETARIA_1',
            nome: 'Secretaria 1',
            tipo: 'INTEROPERACIONAL',
            isElegivel: true,
            usuarioCodigo: 0,
            responsavel: null,
            filhas: [
                {
                    codigo: 11,
                    sigla: 'ASSESSORIA_11',
                    nome: 'Assessoria 11',
                    tipo: 'OPERACIONAL',
                    isElegivel: true,
                    usuarioCodigo: 0,
                    responsavel: null,
                    filhas: []
                },
                {
                    codigo: 12,
                    sigla: 'ASSESSORIA_12',
                    nome: 'Assessoria 12',
                    tipo: 'OPERACIONAL',
                    isElegivel: true,
                    usuarioCodigo: 0,
                    responsavel: null,
                    filhas: []
                },
                {
                    codigo: 13,
                    sigla: 'COORD_11',
                    nome: 'Coordenadoria 11',
                    tipo: 'INTERMEDIARIA',
                    isElegivel: false,
                    usuarioCodigo: 0,
                    responsavel: null,
                    filhas: [
                        {
                            codigo: 131,
                            sigla: 'SECAO_111',
                            nome: 'Seção 111',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            usuarioCodigo: 0,
                            responsavel: null,
                            filhas: []
                        },
                        {
                            codigo: 132,
                            sigla: 'SECAO_112',
                            nome: 'Seção 112',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            usuarioCodigo: 0,
                            responsavel: null,
                            filhas: []
                        },
                        {
                            codigo: 133,
                            sigla: 'SECAO_113',
                            nome: 'Seção 113',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            usuarioCodigo: 0,
                            responsavel: null,
                            filhas: []
                        }
                    ]
                },
                {
                    codigo: 14,
                    sigla: 'COORD_12',
                    nome: 'Coordenadoria 12',
                    tipo: 'INTERMEDIARIA',
                    isElegivel: false,
                    usuarioCodigo: 0,
                    responsavel: null,
                    filhas: []
                }
            ]
        }
    ];

    describe('Bug 1: SECRETARIA_1 marcada com filhas desmarcadas', () => {
        it('SECRETARIA_1 deve estar INDETERMINADA quando apenas ASSESSORIA_11 e ASSESSORIA_12 selecionadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [11, 12] // Apenas ASSESSORIA_11 e ASSESSORIA_12
                }
            });

            const vm = wrapper.vm as any;
            const secretaria = criarUnidades()[0];

            // SECRETARIA_1 tem 4 filhas: ASSESSORIA_11, ASSESSORIA_12, COORD_11, COORD_12
            // Apenas 2 estão marcadas (ASSESSORIA_11 e ASSESSORIA_12)
            // COORD_11 está desmarcada (nenhuma filha selecionada)
            // COORD_12 está desmarcada (sem filhas)
            // Resultado: SECRETARIA_1 deve estar INDETERMINADA
            expect(vm.getEstadoSelecao(secretaria)).toBe('indeterminate');
        });

        it('SECRETARIA_1 deve estar MARCADA quando todas filhas estão marcadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [1, 11, 12, 131, 132, 133] // Todas elegíveis
                }
            });

            const vm = wrapper.vm as any;
            const secretaria = criarUnidades()[0];

            expect(vm.getEstadoSelecao(secretaria)).toBe(true);
        });
    });

    describe('Bug 2: COORD_11 marcada sem filhas', () => {
        it('COORD_11 deve estar DESMARCADA quando nenhuma filha selecionada', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [] // Nada selecionado
                }
            });

            const vm = wrapper.vm;
            const coord11 = criarUnidades()[0].filhas[2];

            expect(vm.getEstadoSelecao(coord11)).toBe(false);
        });

        it('COORD_11 deve estar MARCADA quando todas filhas selecionadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [131, 132, 133] // Todas filhas de COORD_11
                }
            });

            const vm = wrapper.vm;
            const coord11 = criarUnidades()[0].filhas[2];

            expect(vm.getEstadoSelecao(coord11)).toBe(true);
        });

        it('COORD_11 deve estar INDETERMINADA quando algumas filhas selecionadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [131, 132] // 2 de 3 filhas
                }
            });

            const vm = wrapper.vm;
            const coord11 = criarUnidades()[0].filhas[2];

            expect(vm.getEstadoSelecao(coord11)).toBe('indeterminate');
        });
    });

    describe('Bug 3: Comportamento ao desmarcar', () => {
        it('Desmarcar SECRETARIA_1 deve desmarcar todas filhas', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [1, 11, 12, 131, 132, 133]
                }
            });

            const vm = wrapper.vm as any;
            const secretaria = criarUnidades()[0];

            // Simula desmarcar SECRETARIA_1
            vm.toggle(secretaria, false);
            await nextTick();

            const novoModelValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as number[];

            // Nenhuma unidade deve estar selecionada
            expect(novoModelValue).toEqual([]);
        });

        it('COORD_12 não deve ficar marcada ao desmarcar SECRETARIA_1', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [1, 11, 12]
                }
            });

            const vm = wrapper.vm;
            const secretaria = criarUnidades()[0];
            const coord12 = criarUnidades()[0].filhas[3];

            // Antes de desmarcar
            expect(vm.getEstadoSelecao(coord12)).toBe(false);

            // Desmarcar SECRETARIA_1
            vm.toggle(secretaria, false);

            // COORD_12 deve continuar desmarcada
            expect(vm.getEstadoSelecao(coord12)).toBe(false);
        });
    });

    describe('isHabilitado - Habilitação Recursiva', () => {
        it('INTERMEDIARIA com filhas elegíveis deve estar habilitada', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });

            const vm = wrapper.vm;
            const coord11 = criarUnidades()[0].filhas[2];

            // COORD_11 não é elegível (INTERMEDIARIA)
            expect(coord11.isElegivel).toBe(false);

            // Mas deve estar habilitada (tem filhas elegíveis)
            expect(vm.isHabilitado(coord11)).toBe(true);
        });

        it('INTERMEDIARIA sem filhas elegíveis deve estar desabilitada', () => {
            const unidadesComCoordSemFilhas: Unidade[] = [
                {
                    codigo: 99,
                    sigla: 'COORD_99',
                    nome: 'Coord sem filhas elegíveis',
                    tipo: 'INTERMEDIARIA',
                    isElegivel: false,
                    usuarioCodigo: 0,
                    responsavel: null,
                    filhas: [
                        {
                            codigo: 991,
                            sigla: 'SECAO_991',
                            nome: 'Seção não elegível',
                            tipo: 'OPERACIONAL',
                            isElegivel: false, // Não elegível
                            usuarioCodigo: 0,
                            responsavel: null,
                            filhas: []
                        }
                    ]
                }
            ];

            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: unidadesComCoordSemFilhas,
                    modelValue: []
                }
            });

            const vm = wrapper.vm;
            const coord = unidadesComCoordSemFilhas[0];

            expect(vm.isHabilitado(coord)).toBe(false);
        });

        it('Unidade elegível deve sempre estar habilitada', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });

            const vm = wrapper.vm as any;
            const assessoria = criarUnidades()[0].filhas[0];

            expect(assessoria.isElegivel).toBe(true);
            expect(vm.isHabilitado(assessoria)).toBe(true);
        });
    });

    describe('Filtro de INTERMEDIARIA no modelValue', () => {
        it('INTERMEDIARIA nunca deve estar no modelValue', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [131, 132, 133] // Todas filhas de COORD_11
                }
            });

            const modelValue = wrapper.props('modelValue');

            // COORD_11 (código 13) não deve estar no modelValue
            expect(modelValue).not.toContain(13);

            // Apenas as filhas (OPERACIONAL) devem estar
            expect(modelValue).toContain(131);
            expect(modelValue).toContain(132);
            expect(modelValue).toContain(133);
        });

        it('Selecionar COORD_11 deve adicionar apenas filhas ao modelValue', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });

            const vm = wrapper.vm;
            const coord11 = criarUnidades()[0].filhas[2];

            // Selecionar COORD_11
            vm.toggle(coord11, true);
            await nextTick();

            const novoModelValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as number[];

            // COORD_11 não deve estar
            expect(novoModelValue).not.toContain(13);

            // Filhas devem estar
            expect(novoModelValue).toContain(131);
            expect(novoModelValue).toContain(132);
            expect(novoModelValue).toContain(133);
        });
    });
});
