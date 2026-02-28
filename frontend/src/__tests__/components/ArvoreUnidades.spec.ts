import {mount, RouterLinkStub} from '@vue/test-utils';
import {describe, expect, it} from 'vitest';
import ArvoreUnidades from '@/components/unidade/ArvoreUnidades.vue';
import {Unidade} from '@/types/tipos';

// Mock UnidadeTreeNode since it is a child component
const UnidadeTreeNodeStub = {
    template: '<div><slot></slot></div>',
    props: ['unidade', 'isChecked', 'isExpanded', 'isHabilitado', 'get-estado-selecao', 'on-toggle', 'on-toggle-expand']
};

describe('ArvoreUnidades.vue', () => {
    const mockUnidades: Unidade[] = [
        {
            codigo: 1,
            sigla: 'ROOT',
            nome: 'Raiz',
            isElegivel: false,
            tipo: 'ADMINISTRATIVA',
            filhas: [
                {
                    codigo: 2,
                    sigla: 'CHILD1',
                    nome: 'Filho 1',
                    isElegivel: true,
                    tipo: 'OPERACIONAL',
                    filhas: []
                },
                {
                    codigo: 3,
                    sigla: 'CHILD2',
                    nome: 'Filho 2',
                    isElegivel: true,
                    tipo: 'OPERACIONAL',
                    filhas: [
                        {
                            codigo: 4,
                            sigla: 'GRANDCHILD',
                            nome: 'Neto',
                            isElegivel: true,
                            tipo: 'OPERACIONAL',
                            filhas: []
                        }
                    ]
                }
            ]
        }
    ];

    it('deve renderizar corretamente', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        expect(wrapper.exists()).toBe(true);
    });

    it('deve ocultar a raiz se props.ocultarRaiz for true', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: true
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        // Se ocultar raiz, deve renderizar os filhos diretos da raiz
        const children = wrapper.findAllComponents(UnidadeTreeNodeStub);
        // Esperamos 2 filhos (CHILD1 e CHILD2) pois ROOT foi ocultado
        expect(children.length).toBe(2);
    });

    it('deve mostrar a raiz se props.ocultarRaiz for false', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const children = wrapper.findAllComponents(UnidadeTreeNodeStub);
        // Esperamos 1 raiz (ROOT)
        expect(children.length).toBe(1);
    });

    it('deve inicializar expandedUnits com base nas props.unidades', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        // Acesso indireto via props passadas para o stub
        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        expect(rootNode.props('isExpanded')(mockUnidades[0])).toBe(true);
    });

    it('deve alternar expansão (toggleExpand)', async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const toggleExpand = rootNode.props('onToggleExpand');

        // Inicialmente expandido (pelo teste anterior), toggle deve fechar
        await toggleExpand(mockUnidades[0]);
        expect(rootNode.props('isExpanded')(mockUnidades[0])).toBe(false);

        // Toggle novamente deve abrir
        await toggleExpand(mockUnidades[0]);
        expect(rootNode.props('isExpanded')(mockUnidades[0])).toBe(true);
    });

    it('deve verificar seleção (isChecked)', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [2], // CHILD1 selecionado
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const isChecked = rootNode.props('isChecked');

        expect(isChecked(2)).toBe(true);
        expect(isChecked(1)).toBe(false);
    });

    it('deve calcular estado de habilitação (isHabilitado)', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const isHabilitado = rootNode.props('isHabilitado');

        // ROOT não é elegível, mas tem filhas elegíveis -> Habilitado
        expect(isHabilitado(mockUnidades[0])).toBe(true);

        // CHILD1 é elegível -> Habilitado
        expect(isHabilitado(mockUnidades[0].filhas![0])).toBe(true);
    });

    it('deve calcular estado de seleção indeterminado (getEstadoSelecao)', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [2], // Apenas CHILD1 selecionado, mas ROOT tem CHILD1 e CHILD2
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const getEstadoSelecao = rootNode.props('getEstadoSelecao');

        // ROOT tem 2 filhas elegíveis, só 1 selecionada -> Indeterminado
        expect(getEstadoSelecao(mockUnidades[0])).toBe('indeterminate');
    });

    it('deve selecionar unidade e atualizar modelValue (toggle true)', async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const toggle = rootNode.props('onToggle');

        // Selecionar CHILD1
        await toggle(mockUnidades[0].filhas![0], true);

        // Deve emitir update:modelValue com [2]
        expect(wrapper.emitted('update:modelValue')).toBeTruthy();
        expect(wrapper.emitted('update:modelValue')![0]).toEqual([[2]]);
    });

    it('deve deselecionar unidade e atualizar modelValue (toggle false)', async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [2],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const toggle = rootNode.props('onToggle');

        // Deselecionar CHILD1
        await toggle(mockUnidades[0].filhas![0], false);

        expect(wrapper.emitted('update:modelValue')).toBeTruthy();
        expect(wrapper.emitted('update:modelValue')![0]).toEqual([[]]);
    });

    it('deve selecionar recursivamente (toggle true na raiz)', async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false
            },
            global: {
                stubs: {
                    UnidadeTreeNode: UnidadeTreeNodeStub,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const rootNode = wrapper.findComponent(UnidadeTreeNodeStub);
        const toggle = rootNode.props('onToggle');

        // Selecionar ROOT (que deve selecionar CHILD1, CHILD2 e GRANDCHILD pois são elegíveis)
        // ROOT não é elegível, então não entra.
        // CHILD1 (2), CHILD2 (3), GRANDCHILD (4) entram.
        await toggle(mockUnidades[0], true);

        const emitted = wrapper.emitted('update:modelValue')![0][0] as number[];
        expect(emitted).toContain(2);
        expect(emitted).toContain(3);
        expect(emitted).toContain(4);
        expect(emitted).not.toContain(1); // ROOT não elegível
    });
});
