import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount, VueWrapper} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import ImportarAtividadesModal from '../ImportarAtividadesModal.vue';
import {useProcessosStore} from '@/stores/processos';
import {useAtividadesStore} from '@/stores/atividades';
import {mockProcessoDetalhe, mockProcessosPainel} from "@/test-utils/mocks";

// Mock das stores
vi.mock('@/stores/processos', () => ({
    useProcessosStore: vi.fn(() => ({
        processosPainel: [],
        processoDetalhe: null,
        fetchProcessosPainel: vi.fn(),
        fetchProcessoDetalhe: vi.fn(),
    })),
}));

vi.mock('@/stores/atividades', () => ({
    useAtividadesStore: vi.fn(() => ({
        atividadesPorSubprocesso: new Map(),
        getAtividadesPorSubprocesso: vi.fn().mockReturnValue([]),
        fetchAtividadesPorSubprocesso: vi.fn(),
    })),
}));

describe('ImportarAtividadesModal.vue', () => {
    let wrapper: VueWrapper<any>;
    let processosStore: ReturnType<typeof useProcessosStore>;
    let atividadesStore: ReturnType<typeof useAtividadesStore>;

    const mountComponent = (props = {mostrar: true}) => {
        return mount(ImportarAtividadesModal, {
            props,
            global: {
                plugins: [createPinia()],
            },
        });
    };

    beforeEach(() => {
        setActivePinia(createPinia());
        processosStore = useProcessosStore();
        atividadesStore = useAtividadesStore();
        vi.clearAllMocks();

        // Setup default mocks
        vi.mocked(processosStore.fetchProcessosPainel).mockResolvedValue(undefined);
        vi.mocked(processosStore.fetchProcessoDetalhe).mockResolvedValue(undefined);
        vi.mocked(atividadesStore.fetchAtividadesPorSubprocesso).mockResolvedValue(undefined);
        processosStore.processosPainel = mockProcessosPainel;
    });

    it('should render the modal when "mostrar" is true', () => {
        wrapper = mountComponent();
        expect(wrapper.find('.modal.show').exists()).toBe(true);
        expect(wrapper.find('.modal-title').text()).toBe('Importação de atividades');
    });

    it('should not render the modal when "mostrar" is false', () => {
        wrapper = mountComponent({mostrar: false});
        expect(wrapper.find('.modal.show').exists()).toBe(false);
    });

    it('should fetch available processes on mount', () => {
        wrapper = mountComponent();
        expect(processosStore.fetchProcessosPainel).toHaveBeenCalled();
    });

    it('should fetch process details when a process is selected', async () => {
        processosStore.processoDetalhe = mockProcessoDetalhe;
        wrapper = mountComponent();
        const select = wrapper.find<HTMLSelectElement>('select#processo-select');
        await select.setValue('1');
        expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
    });

    it('should fetch activities when a unit is selected', async () => {
        processosStore.processoDetalhe = mockProcessoDetalhe;
        wrapper = mountComponent();
        // first select a process to enable the unit select
        const processoSelect = wrapper.find<HTMLSelectElement>('select#processo-select');
        await processoSelect.setValue('1');
        await wrapper.vm.$nextTick(); // wait for units to be populated

        const unidadeSelect = wrapper.find<HTMLSelectElement>('select#unidade-select');
        await unidadeSelect.setValue('101'); // codUnidade from mock
        expect(atividadesStore.fetchAtividadesPorSubprocesso).toHaveBeenCalledWith(101);
    });

    it('should enable import button only when activities are selected', async () => {
        const mockAtividades = [{codigo: 1, descricao: 'Atividade 1', conhecimentos: []}];
        vi.mocked(atividadesStore.getAtividadesPorSubprocesso).mockReturnValue(mockAtividades);
        processosStore.processoDetalhe = mockProcessoDetalhe;
        wrapper = mountComponent();

        const importButton = wrapper.find<HTMLButtonElement>('[data-testid="btn-importar"]');
        expect(importButton.element.disabled).toBe(true);

        // Select a process and unit
        await wrapper.find<HTMLSelectElement>('select#processo-select').setValue('1');
        await wrapper.vm.$nextTick();
        await wrapper.find<HTMLSelectElement>('select#unidade-select').setValue('101');
        await wrapper.vm.$nextTick();

        // Select an activity
        const checkbox = wrapper.find<HTMLInputElement>('input[type="checkbox"]');
        await checkbox.setValue(true);
        await wrapper.vm.$nextTick();

        expect(importButton.element.disabled).toBe(false);
    });

    it('should emit "importar" with selected subprocesso ID on import click', async () => {
        const mockAtividades = [{codigo: 1, descricao: 'Atividade 1', conhecimentos: []}];
        vi.mocked(atividadesStore.getAtividadesPorSubprocesso).mockReturnValue(mockAtividades);
        processosStore.processoDetalhe = mockProcessoDetalhe;
        wrapper = mountComponent();

        // Select a process and unit
        await wrapper.find<HTMLSelectElement>('select#processo-select').setValue('1');
        await wrapper.vm.$nextTick();
        await wrapper.find<HTMLSelectElement>('select#unidade-select').setValue('101');
        await wrapper.vm.$nextTick();

        const importButton = wrapper.find('[data-testid="btn-importar"]');
        await importButton.trigger('click');

        expect(wrapper.emitted('importar')).toBeTruthy();
        expect(wrapper.emitted('importar')?.[0]).toEqual([101]);
    });

    it('should emit "fechar" on cancel click', async () => {
        wrapper = mountComponent();
        await wrapper.find('[data-testid="btn-cancelar"]').trigger('click');
        expect(wrapper.emitted('fechar')).toBeTruthy();
    });
});