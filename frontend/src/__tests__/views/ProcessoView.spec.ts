import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ProcessoView from '@/views/ProcessoView.vue';
import { createTestingPinia } from '@pinia/testing';
import { useProcessosStore } from '@/stores/processos';

import { useFeedbackStore } from '@/stores/feedback';
import { useRoute, useRouter } from 'vue-router';
import { Perfil } from '@/types/tipos';
import * as processoService from '@/services/processoService';

// Mocks
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>();
  return {
    ...actual,
    useRoute: vi.fn(),
    useRouter: vi.fn(),
  };
});

vi.mock('@/services/processoService', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@/services/processoService')>();
    return {
        ...actual,
        obterDetalhesProcesso: vi.fn(),
        buscarSubprocessosElegiveis: vi.fn(),
        finalizarProcesso: vi.fn(),
        processarAcaoEmBloco: vi.fn(),
        buscarContextoCompleto: vi.fn(),
    };
});

// Component Stubs
const BAlertStub = {
    template: '<div v-if="modelValue"><slot></slot></div>',
    props: ['modelValue', 'variant', 'dismissible']
};
const BContainerStub = {
    template: '<div><slot></slot></div>'
};
const ProcessoDetalhesStub = {
    template: '<div>Detalhes</div>',
    props: ['descricao', 'situacao', 'tipo']
};
const TreeTableStub = {
    template: '<div>TreeTable</div>',
    props: ['columns', 'data', 'title'],
    emits: ['row-click']
};
const ProcessoAcoesStub = {
    template: '<div>Acoes</div>',
    props: ['mostrar-botoes-bloco', 'perfil', 'situacao-processo'],
    emits: ['finalizar', 'aceitar-bloco', 'homologar-bloco']
};
const ModalAcaoBlocoStub = {
    template: '<div>ModalAcaoBloco</div>',
    props: ['mostrar', 'tipo', 'unidades'],
    emits: ['confirmar', 'fechar']
};
const ModalFinalizacaoStub = {
    template: '<div>ModalFinalizacao</div>',
    props: ['mostrar', 'processo-descricao'],
    emits: ['confirmar', 'fechar']
};

describe('ProcessoView.vue', () => {
    let mockRoute: any;
    let mockRouter: any;

    beforeEach(() => {
        mockRoute = {
            params: { codProcesso: '1' }
        };
        mockRouter = {
            push: vi.fn()
        };
        (useRoute as any).mockReturnValue(mockRoute);
        (useRouter as any).mockReturnValue(mockRouter);

        // Mock window.scrollTo
        global.window.scrollTo = vi.fn();

        // Setup default service responses
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            descricao: 'Processo Teste',
            unidades: [],
            resumoSubprocessos: []
        } as any);
        vi.mocked(processoService.buscarSubprocessosElegiveis).mockResolvedValue([]);
        vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue({
            processo: {
                codigo: 1,
                descricao: 'Processo Teste',
                unidades: [],
                resumoSubprocessos: []
            },
            elegiveis: []
        } as any);
    });

    const mountComponent = (piniaConfig = {}) => {
        return mount(ProcessoView, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: true, // Stub actions by default
                    ...piniaConfig
                })],
                stubs: {
                    BAlert: BAlertStub,
                    BContainer: BContainerStub,
                    ProcessoDetalhes: ProcessoDetalhesStub,
                    TreeTable: TreeTableStub,
                    ProcessoAcoes: ProcessoAcoesStub,
                    ModalAcaoBloco: ModalAcaoBlocoStub,
                    ModalFinalizacao: ModalFinalizacaoStub
                }
            }
        });
    };

    it('deve exibir alerta de erro se houver erro na store', async () => {
        const wrapper = mountComponent();
        const processosStore = useProcessosStore();

        processosStore.lastError = { message: 'Erro teste', details: 'Detalhes erro' } as any;
        await wrapper.vm.$nextTick();

        const alert = wrapper.findComponent(BAlertStub);
        expect(alert.exists()).toBe(true);
        expect(alert.text()).toContain('Erro teste');
        expect(alert.text()).toContain('Detalhes erro');
    });

    it('deve navegar para subprocesso quando ADMIN clica em unidade', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1 }
                 },
                 perfil: {
                     perfilSelecionado: Perfil.ADMIN
                 }
             }
        });

        const treeTable = wrapper.findComponent(TreeTableStub);
        const item = { clickable: true, unidadeAtual: 'UNIDADE', id: 10 };

        await treeTable.vm.$emit('row-click', item);

        expect(mockRouter.push).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1",
                siglaUnidade: "UNIDADE",
            },
        });
    });

    it('deve navegar para subprocesso quando GESTOR clica em unidade', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1 }
                 },
                 perfil: {
                     perfilSelecionado: Perfil.GESTOR
                 }
             }
        });

        const treeTable = wrapper.findComponent(TreeTableStub);
        const item = { clickable: true, unidadeAtual: 'UNIDADE', id: 10 };

        await treeTable.vm.$emit('row-click', item);

        expect(mockRouter.push).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1",
                siglaUnidade: "UNIDADE",
            },
        });
    });

    it('deve navegar para subprocesso quando CHEFE clica em SUA unidade', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1 }
                 },
                 perfil: {
                     perfilSelecionado: Perfil.CHEFE,
                     unidadeSelecionada: 10
                 }
             }
        });

        const treeTable = wrapper.findComponent(TreeTableStub);
        const item = { clickable: true, unidadeAtual: 'UNIDADE', id: 10 };

        await treeTable.vm.$emit('row-click', item);

        expect(mockRouter.push).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1",
                siglaUnidade: "UNIDADE",
            },
        });
    });

    it('NÃO deve navegar quando CHEFE clica em OUTRA unidade', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1 }
                 },
                 perfil: {
                     perfilSelecionado: Perfil.CHEFE,
                     unidadeSelecionada: 99
                 }
             }
        });

        const treeTable = wrapper.findComponent(TreeTableStub);
        const item = { clickable: true, unidadeAtual: 'UNIDADE', id: 10 };

        await treeTable.vm.$emit('row-click', item);

        expect(mockRouter.push).not.toHaveBeenCalled();
    });

    it('deve formatar dados para a árvore corretamente', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: {
                         codigo: 1,
                         unidades: [
                             { codUnidade: 1, sigla: 'PAI', filhos: [ { codUnidade: 2, sigla: 'FILHO' } ] }
                         ]
                     }
                 }
             }
        });

        const treeTable = wrapper.findComponent(TreeTableStub);
        const data = treeTable.props('data');

        expect(data).toHaveLength(1);
        expect(data[0].nome).toBe('PAI');
        expect(data[0].children).toHaveLength(1);
        expect(data[0].children[0].nome).toBe('FILHO');
    });

    it('deve finalizar processo corretamente', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1, descricao: 'Teste' }
                 }
             },
             stubActions: false // Allow action to be called
        });
        vi.mocked(processoService.finalizarProcesso).mockResolvedValue(undefined);
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({} as any);

        const modalFinalizacao = wrapper.findComponent(ModalFinalizacaoStub);

        // Open modal
        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit('finalizar');
        expect((wrapper.vm as any).mostrarModalFinalizacao).toBe(true);

        // Confirm
        await modalFinalizacao.vm.$emit('confirmar');
        // Await promises to settle
        await new Promise(resolve => setTimeout(resolve, 0));

        expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(mockRouter.push).toHaveBeenCalledWith('/painel');
    });

    it('deve lidar com erro ao finalizar processo', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1, descricao: 'Teste' }
                 }
             },
             stubActions: false
        });
        vi.mocked(processoService.finalizarProcesso).mockRejectedValue(new Error('Fail'));

        const modalFinalizacao = wrapper.findComponent(ModalFinalizacaoStub);

        // Ensure modal is present
        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit('finalizar');

        await modalFinalizacao.vm.$emit('confirmar');

        expect(processoService.finalizarProcesso).toHaveBeenCalled();
        expect(mockRouter.push).not.toHaveBeenCalled();
    });

    it('deve processar ação em bloco', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1 },
                     subprocessosElegiveis: [{ unidadeSigla: 'A' }, { unidadeSigla: 'B' }]
                 },
                 perfil: {
                     unidadeSelecionada: 5
                 }
             },
             stubActions: false
        });
        vi.mocked(processoService.processarAcaoEmBloco).mockResolvedValue(undefined);
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({} as any);

        const modalBloco = wrapper.findComponent(ModalAcaoBlocoStub);

        // Trigger modal open via ProcessoAcoes
        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit('aceitar-bloco');

        expect((wrapper.vm as any).mostrarModalBloco).toBe(true);
        expect((wrapper.vm as any).tipoAcaoBloco).toBe('aceitar');

        // Confirm action
        await modalBloco.vm.$emit('confirmar', [
            { sigla: 'A', selecionada: true },
            { sigla: 'B', selecionada: false }
        ]);
        // Await promises to settle
        await new Promise(resolve => setTimeout(resolve, 0));

        expect(processoService.processarAcaoEmBloco).toHaveBeenCalledWith({
            codProcesso: 1,
            unidades: ['A'],
            tipoAcao: 'aceitar',
            unidadeUsuario: '5'
        });
        expect(mockRouter.push).toHaveBeenCalledWith('/painel');
    });

    it('deve validar seleção vazia na ação em bloco', async () => {
        const wrapper = mountComponent({
             initialState: {
                 processos: {
                     processoDetalhe: { codigo: 1 },
                     subprocessosElegiveis: [{ unidadeSigla: 'A' }]
                 }
             }
        });
        const feedbackStore = useFeedbackStore();
        const modalBloco = wrapper.findComponent(ModalAcaoBlocoStub);

        // Open modal first to ensure it's rendered/active
        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit('aceitar-bloco');

        await modalBloco.vm.$emit('confirmar', [
            { sigla: 'A', selecionada: false }
        ]);

        expect(feedbackStore.show).toHaveBeenCalledWith(
            expect.stringContaining('Nenhuma unidade'),
            expect.any(String),
            'danger'
        );
    });
});
