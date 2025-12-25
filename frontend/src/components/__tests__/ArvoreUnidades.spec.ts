import {describe, expect, it} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import ArvoreUnidades from '@/components/ArvoreUnidades.vue';
import type {Unidade} from '@/types/tipos';
import {setupComponentTest} from '@/test-utils/componentTestHelpers';

describe('ArvoreUnidades.vue', () => {
    const context = setupComponentTest();

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
                    filhas: [
                        {
                            codigo: 141,
                            sigla: 'SECAO_121',
                            nome: 'Seção 121',
                            tipo: 'OPERACIONAL',
                            isElegivel: true,
                            usuarioCodigo: 0,
                            responsavel: null,
                            filhas: []
                        }
                    ]
                }
            ]
        }
    ];

    describe('Regras Básicas de Seleção', () => {
        it('deve selecionar todas as filhas ao marcar um pai (COORD_11)', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2]; // COORD_11

            // Simula toggle no pai
            vm.toggle(coord11, true);
            await flushPromises();

            const emitted = wrapper.emitted('update:modelValue');
            const lastEmit = emitted![emitted!.length - 1][0] as number[];

            // Todas as filhas elegíveis de COORD_11 (131, 132, 133) devem estar selecionadas
            expect(lastEmit).toContain(131);
            expect(lastEmit).toContain(132);
            expect(lastEmit).toContain(133);
            expect(lastEmit).toHaveLength(3); // COORD_11 não entra pois é INTERMEDIARIA (isElegivel: false)
        });

        it('deve desmarcar todas as filhas ao desmarcar um pai (COORD_11)', async () => {
             const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [131, 132, 133] // Começa selecionado
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2];

            vm.toggle(coord11, false);
            await flushPromises();

            const emitted = wrapper.emitted('update:modelValue');
            const lastEmit = emitted![emitted!.length - 1][0] as number[];

            expect(lastEmit).not.toContain(131);
            expect(lastEmit).not.toContain(132);
            expect(lastEmit).not.toContain(133);
        });
    });

    describe('Estado dos Checkboxes (Visual)', () => {
        it('Checkbox deve ter props model-value e indeterminate separadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [132] // 1 de 3 filhas de COORD_11
                }
            });
            context.wrapper = wrapper;

            const treeNodes = wrapper.findAllComponents({ name: 'UnidadeTreeNode' });
            const coord11Node = treeNodes.find(node =>
                (node.props('unidade') as Unidade).sigla === 'COORD_11'
            );

            expect(coord11Node).toBeTruthy();

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2];
            expect(vm.getEstadoSelecao(coord11)).toBe('indeterminate');
        });

        it('Checkbox marcado quando todas filhas selecionadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [131, 132, 133] // Todas filhas de COORD_11
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2];

            expect(vm.getEstadoSelecao(coord11)).toBe(true);
        });

        it('Checkbox desmarcado quando nenhuma filha selecionada', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2];

            expect(vm.getEstadoSelecao(coord11)).toBe(false);
        });
    });

    describe('Reatividade e Sincronização', () => {
        it('unidadesSelecionadasLocal sincroniza com props.modelValue', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            expect(vm.unidadesSelecionadasLocal).toEqual([]);

            await wrapper.setProps({ modelValue: [131, 132, 133] });
            await flushPromises();

            expect(vm.unidadesSelecionadasLocal).toEqual([131, 132, 133]);
        });

        it('Mudanças locais emitem update:modelValue', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            vm.unidadesSelecionadasLocal = [131];
            await flushPromises();

            const emitted = wrapper.emitted('update:modelValue');
            expect(emitted).toBeTruthy();
            expect(emitted![emitted!.length - 1]).toEqual([[131]]);
        });

        it('Não deve causar loop infinito ao atualizar modelValue', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            await wrapper.setProps({ modelValue: [131] });
            await wrapper.setProps({ modelValue: [131, 132] });
            await wrapper.setProps({ modelValue: [131, 132, 133] });
            await flushPromises();

            const vm = wrapper.vm as any;
            expect(vm.unidadesSelecionadasLocal).toEqual([131, 132, 133]);
        });
    });

    describe('Expansão', () => {
        it('Raízes devem inicializar expandidas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            expect(vm.expandedUnits.has(1)).toBe(true);
        });

        it('Raízes devem poder ser contraídas', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            vm.toggleExpand(criarUnidades()[0]);
            await flushPromises();

            expect(vm.expandedUnits.has(1)).toBe(false);
        });
    });

    describe('Lógica de Seleção Complexa (Bugs Anteriores)', () => {
        it('SECRETARIA_1 indeterminada com algumas filhas selecionadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [11, 12] // Apenas ASSESSORIA_11 e ASSESSORIA_12
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const secretaria = criarUnidades()[0];

            const estado = vm.getEstadoSelecao(secretaria);
            expect(estado).toBe('indeterminate');
        });

        it('SECRETARIA_1 marcada com todas filhas selecionadas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [1, 11, 12, 131, 132, 133, 141] // Todas elegíveis
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const secretaria = criarUnidades()[0];

            const estado = vm.getEstadoSelecao(secretaria);
            expect(estado).toBe(true);
        });

        it('COORD_11 indeterminada com 2 de 3 filhas', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: [132, 133] // 2 de 3 filhas
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2];

            const estado = vm.getEstadoSelecao(coord11);
            expect(estado).toBe('indeterminate');
        });
    });

    describe('Habilitação', () => {
        it('INTERMEDIARIA com filhas elegíveis deve estar habilitada', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;
            const coord11 = criarUnidades()[0].filhas![2];

            expect(coord11.isElegivel).toBe(false);
            expect(vm.isHabilitado(coord11)).toBe(true);
        });

        it('Checkbox de INTERMEDIARIA habilitada deve estar enabled', () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const coord11Checkbox = wrapper.find('[data-testid="chk-arvore-unidade-COORD_11"]');
            expect(coord11Checkbox.attributes('disabled')).toBeUndefined();
        });
    });

    describe('Cenário Completo: Fluxo de Seleção', () => {
        it('Fluxo completo de seleção hierárquica', async () => {
            const wrapper = mount(ArvoreUnidades, {
                props: {
                    unidades: criarUnidades(),
                    modelValue: []
                }
            });
            context.wrapper = wrapper;

            const vm = wrapper.vm as any;

            // 1. Seleciona SECAO_112
            vm.toggle(criarUnidades()[0].filhas![2].filhas![1], true);
            await flushPromises();

            // COORD_11 deve estar indeterminada (1 de 3)
            expect(vm.getEstadoSelecao(criarUnidades()[0].filhas![2])).toBe('indeterminate');

            // 2. Seleciona SECAO_113
            vm.toggle(criarUnidades()[0].filhas![2].filhas![2], true);
            await flushPromises();

            // COORD_11 ainda indeterminada (2 de 3)
            expect(vm.getEstadoSelecao(criarUnidades()[0].filhas![2])).toBe('indeterminate');

            // 3. Seleciona SECAO_111
            vm.toggle(criarUnidades()[0].filhas![2].filhas![0], true);
            await flushPromises();

            // COORD_11 agora marcada (3 de 3)
            expect(vm.getEstadoSelecao(criarUnidades()[0].filhas![2])).toBe(true);

            // 4. Verifica que INTERMEDIARIA não está no modelValue
            const emitted = wrapper.emitted('update:modelValue');
            const lastEmit = emitted![emitted!.length - 1][0] as number[];
            expect(lastEmit).toContain(131);
            expect(lastEmit).toContain(132);
            expect(lastEmit).toContain(133);
            expect(lastEmit).not.toContain(13); // COORD_11 não deve estar
        });
    });
});
