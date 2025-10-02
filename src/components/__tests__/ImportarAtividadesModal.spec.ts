import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createPinia} from 'pinia';
import {initPinia} from '@/test/helpers';
import {
    assertUnidadeOptions,
    expectImportButtonDisabled,
    expectImportButtonEnabled,
    selecionarProcessoEUnidade,
    selectFirstCheckbox
} from '@/test/uiHelpers';
import ImportarAtividadesModal from '../ImportarAtividadesModal.vue';
import {Atividade, Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos';

// Mock dos stores
const mockProcessosStore = {
    processos: [] as Processo[],
    getUnidadesDoProcesso: vi.fn(),
};

const mockAtividadesStore = {
    fetchAtividadesPorSubprocesso: vi.fn(),
    getAtividadesPorSubprocesso: vi.fn(),
};

vi.mock('@/stores/processos', () => ({
    useProcessosStore: () => mockProcessosStore,
}));

vi.mock('@/stores/atividades', () => ({
    useAtividadesStore: () => mockAtividadesStore,
}));

// Helpers para reduzir repetição nos testes
const criarProcesso = (overrides = {}) => ({
    id: 1,
    descricao: 'Processo Teste',
    tipo: TipoProcesso.MAPEAMENTO,
    situacao: SituacaoProcesso.FINALIZADO,
    dataLimite: new Date(),
    dataFinalizacao: new Date(),
    ...overrides
});

function aplicarMocks({
                          processos = [],
                          unidades = [],
                          atividades = []
                      }: {
    processos?: Processo[];
    unidades?: Subprocesso[];
    atividades?: Atividade[];
} = {}) {
    mockProcessosStore.processos = processos;
    mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue(unidades);
    mockAtividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(atividades);
    mockAtividadesStore.fetchAtividadesPorSubprocesso = vi.fn();
}

describe('ImportarAtividadesModal.vue', () => {
    let pinia: ReturnType<typeof createPinia>;

    beforeEach(() => {
        pinia = initPinia();

        // Reset mocks
        vi.clearAllMocks();
    });

    const mountComponent = (props: { mostrar: boolean } = {mostrar: true}) => {
        return mount(ImportarAtividadesModal, {
            props,
            global: {
                plugins: [pinia]
            }
        });
    };

    describe('Renderização', () => {
        it('deve renderizar o modal quando mostrar=true', () => {
            const wrapper = mountComponent({mostrar: true});

            expect(wrapper.find('.modal').exists()).toBe(true);
            expect(wrapper.find('.modal-dialog').exists()).toBe(true);
            expect(wrapper.find('.modal-content').exists()).toBe(true);
            expect(wrapper.find('.modal-header').exists()).toBe(true);
            expect(wrapper.find('.modal-body').exists()).toBe(true);
            expect(wrapper.find('.modal-footer').exists()).toBe(true);
        });

        it('não deve renderizar o modal quando mostrar=false', () => {
            const wrapper = mountComponent({mostrar: false});

            expect(wrapper.find('.modal').exists()).toBe(false);
            expect(wrapper.find('.modal-backdrop').exists()).toBe(false);
        });

        it('deve renderizar o título correto', () => {
            const wrapper = mountComponent();

            expect(wrapper.find('.modal-title').text()).toBe('Importação de atividades');
        });

        it('deve renderizar os botões de fechar e importar', () => {
            const wrapper = mountComponent();

            const buttons = wrapper.findAll('.modal-footer button');
            expect(buttons).toHaveLength(2);
            expect(buttons[0].text()).toBe('Cancelar');
            expect(buttons[1].text()).toBe('Importar');
        });
    });

    describe('Seleção de Processo', () => {
        beforeEach(() => {
            aplicarMocks({
                processos: [
                    {
                        id: 1,
                        descricao: 'Processo de Mapeamento 1',
                        tipo: TipoProcesso.MAPEAMENTO,
                        situacao: SituacaoProcesso.FINALIZADO,
                        dataLimite: new Date(),
                        dataFinalizacao: new Date()
                    },
                    {
                        id: 2,
                        descricao: 'Processo de Revisão 1',
                        tipo: TipoProcesso.REVISAO,
                        situacao: SituacaoProcesso.FINALIZADO,
                        dataLimite: new Date(),
                        dataFinalizacao: new Date()
                    },
                    {
                        id: 3,
                        descricao: 'Processo em Andamento',
                        tipo: TipoProcesso.MAPEAMENTO,
                        situacao: SituacaoProcesso.EM_ANDAMENTO,
                        dataLimite: new Date(),
                        dataFinalizacao: null
                    },
                ]
            });
        });

        it('deve exibir apenas processos finalizados de mapeamento/revisão', async () => {
            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            const options = wrapper.findAll('select#processo-select option');
            expect(options).toHaveLength(3); // 2 finalizados + 1 disabled
            expect(options[1].text()).toBe('Processo de Mapeamento 1');
            expect(options[2].text()).toBe('Processo de Revisão 1');
        });

        it('deve mostrar mensagem quando não há processos disponíveis', async () => {
            mockProcessosStore.processos = [
                {
                    id: 3,
                    descricao: 'Processo em Andamento',
                    tipo: TipoProcesso.MAPEAMENTO,
                    situacao: SituacaoProcesso.EM_ANDAMENTO,
                    dataLimite: new Date(),
                    dataFinalizacao: null,
                },
            ];

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            expect(wrapper.text()).toContain('Nenhum processo disponível para importação.');
        });

        it('deve desabilitar seleção de unidade quando nenhum processo selecionado', () => {
            const wrapper = mountComponent();

            const unidadeSelect = wrapper.find('select#unidade-select');
            expect(unidadeSelect.attributes('disabled')).toBeDefined();
        });
    });

    describe('Seleção de Unidade', () => {
        beforeEach(() => {
            aplicarMocks({
                processos: [criarProcesso()],
                unidades: [
                    {id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado'} as Subprocesso,
                    {id: 2, unidade: 'UNID2', idProcesso: 1, situacao: 'Finalizado'} as Subprocesso,
                ]
            });
        });

        it('deve carregar unidades quando processo selecionado', async () => {
            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();
            await selecionarProcessoEUnidade(wrapper);
            assertUnidadeOptions(wrapper, ['UNID1', 'UNID2']);
        });

        it('deve mostrar mensagem quando não há atividades', async () => {
            mockAtividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue([]);

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            await selecionarProcessoEUnidade(wrapper);

            expect(wrapper.text()).toContain('Nenhuma atividade encontrada para esta unidade/processo.');
        });

    });

    describe('Botão Importar', () => {
        beforeEach(() => {
            aplicarMocks({
                processos: [criarProcesso()],
                unidades: [
                    {id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado'} as Subprocesso,
                ],
                atividades: [
                    {id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: []} as Atividade,
                    {id: 2, descricao: 'Atividade 2', idSubprocesso: 1, conhecimentos: []} as Atividade,
                ]
            });
        });

        it('deve estar desabilitado quando nenhuma atividade selecionada', async () => {
            const wrapper = mountComponent();
    
            await wrapper.vm.$nextTick();
    
            await selecionarProcessoEUnidade(wrapper);
    
            expectImportButtonDisabled(wrapper);
        });

        it('deve estar habilitado quando atividades selecionadas', async () => {
            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            await selecionarProcessoEUnidade(wrapper);
            await selectFirstCheckbox(wrapper);
            expectImportButtonEnabled(wrapper);
        });

        // duplicate test removed: cobertura mantida por the other "deve estar desabilitado..." test above
    });

    describe('Eventos', () => {
        beforeEach(() => {
            aplicarMocks({
                processos: [criarProcesso()],
                unidades: [
                    {id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado'} as Subprocesso,
                ],
                atividades: [
                    {id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: []} as Atividade,
                    {id: 2, descricao: 'Atividade 2', idSubprocesso: 1, conhecimentos: []} as Atividade,
                ]
            });
        });

        it('deve emitir evento fechar ao clicar no botão fechar', async () => {
            const wrapper = mountComponent();

            const closeButton = wrapper.find('.btn-close');
            await closeButton.trigger('click');

            expect(wrapper.emitted().fechar).toBeTruthy();
        });

        it('deve emitir evento fechar ao clicar no botão cancelar', async () => {
            const wrapper = mountComponent();

            const cancelButton = wrapper.find('.btn-outline-secondary');
            await cancelButton.trigger('click');

            expect(wrapper.emitted().fechar).toBeTruthy();
        });

        it('deve emitir evento importar com atividades selecionadas', async () => {
            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            await selecionarProcessoEUnidade(wrapper);
            await selectFirstCheckbox(wrapper);

            // Clicar importar
            const importarButton = wrapper.find('.btn-outline-primary');
            await importarButton.trigger('click');

            expect(wrapper.emitted().importar).toBeTruthy();
            expect(wrapper.emitted().importar[0]).toEqual([
                [{id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: []}]
            ]);
        });

    });

    describe('Reset do Modal', () => {
        it('deve resetar o modal quando mostrar muda para true', async () => {
            const wrapper = mountComponent({mostrar: false});

            // Simular que o modal foi mostrado antes
            await wrapper.setProps({mostrar: true});

            await wrapper.vm.$nextTick();

            // Verificar se os elementos do modal estão presentes
            expect(wrapper.find('.modal').exists()).toBe(true);
        });
    });

    describe('Funcionalidade de Async', () => {
        it('deve chamar fetchAtividadesPorSubprocesso quando unidade selecionada', async () => {
            aplicarMocks({
                processos: [criarProcesso()],
                unidades: [
                    {id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado'} as Subprocesso,
                ]
            });

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            await selecionarProcessoEUnidade(wrapper);

            expect(mockAtividadesStore.fetchAtividadesPorSubprocesso).toHaveBeenCalledWith(1);
        });
    });

    describe('Edge Cases e Validações', () => {
        it('deve lidar com processo sem unidades', async () => {
            aplicarMocks({
                processos: [
                    {
                        id: 1,
                        descricao: 'Processo sem Unidades',
                        tipo: TipoProcesso.MAPEAMENTO,
                        situacao: SituacaoProcesso.FINALIZADO,
                        dataLimite: new Date(),
                        dataFinalizacao: new Date()
                    },
                ],
                unidades: []
            });

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            const processoSelect = wrapper.find('select#processo-select');
            await processoSelect.setValue(1);

            await wrapper.vm.$nextTick();

            // Simular que processoSelecionadoId se torna null

            const vm = wrapper.vm as any;
            await vm.selecionarProcesso(null);

            // Verificar que o estado foi resetado
            expect(vm.processoSelecionado).toBeNull();
            expect(vm.unidadesParticipantes).toEqual([]);
            expect(vm.unidadeSelecionada).toBeNull();
            expect(vm.unidadeSelecionadaId).toBeNull();
        });

        it('deve chamar selecionarUnidade(null) quando unidadeSelecionadaId é null', async () => {
            mockProcessosStore.processos = [
                {
                    id: 1,
                    descricao: 'Processo Teste',
                    tipo: TipoProcesso.MAPEAMENTO,
                    situacao: SituacaoProcesso.FINALIZADO,
                    dataLimite: new Date(),
                    dataFinalizacao: new Date(),
                },
            ];

            mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([
                {id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado'} as Subprocesso,
            ]);

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            await selecionarProcessoEUnidade(wrapper);

            // Verificar que botão importar está desabilitado
            const importarButton = wrapper.find('.btn-outline-primary');
            expect(importarButton.attributes('disabled')).toBeDefined();

            // Verificar que não emitiu evento de importar
            expect(wrapper.emitted().importar).toBeFalsy();
        });
    });
});
