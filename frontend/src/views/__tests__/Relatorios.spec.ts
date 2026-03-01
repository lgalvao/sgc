import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import Relatorios from "@/views/RelatoriosView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useProcessosStore} from "@/stores/processos";
import {useMapasStore} from "@/stores/mapas";
import {nextTick} from "vue";

// Stubs
const PageHeaderStub = {
    template: '<div data-testid="page-header">{{ title }}</div>',
    props: ['title']
};
const ModalMapasVigentesStub = {
    template: '<div data-testid="modal-mapas-vigentes" v-if="modelValue"></div>',
    props: ['modelValue', 'mapas'],
    emits: ['update:modelValue']
};
const ModalDiagnosticosGapsStub = {
    template: '<div data-testid="modal-diagnosticos-gaps" v-if="modelValue"></div>',
    props: ['modelValue', 'diagnosticos'],
    emits: ['update:modelValue']
};
const ModalAndamentoGeralStub = {
    template: '<div data-testid="modal-andamento-geral" v-if="modelValue"></div>',
    props: ['modelValue', 'processos'],
    emits: ['update:modelValue']
};

describe("Relatorios.vue", () => {
    let wrapper: any;
    let processosStore: any;
    let mapasStore: any;

    const createWrapper = (initialState = {}) => {
        return mount(Relatorios, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: true,
                        initialState
                    }),
                ],
                stubs: {
                    BContainer: {template: '<div><slot /></div>'},
                    PageHeader: PageHeaderStub,
                    ModalMapasVigentes: ModalMapasVigentesStub,
                    ModalDiagnosticosGaps: ModalDiagnosticosGapsStub,
                    ModalRelatorioAndamento: ModalAndamentoGeralStub,
                },
            },
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve carregar processos ao montar se necessário", async () => {
        const initialState = {
            perfil: {
                perfilSelecionado: 'GESTOR',
                unidadeSelecionada: 1
            },
            processos: {
                processosPainel: []
            }
        };
        wrapper = createWrapper(initialState);
        processosStore = useProcessosStore();

        // Trigger onMounted
        await flushPromises();

        expect(processosStore.buscarProcessosPainel).toHaveBeenCalled();
    });

    it("deve filtrar processos por tipo", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.processosPainel = [
            {codigo: 1, tipo: 'MAPEAMENTO', dataCriacao: '2024-01-01T10:00:00'},
            {codigo: 2, tipo: 'REVISAO', dataCriacao: '2024-01-01T10:00:00'}
        ];

        wrapper.vm.filtroTipo = 'REVISAO';
        await nextTick();

        expect(wrapper.vm.processosFiltrados).toHaveLength(1);
    });

    it("deve filtrar processos por data", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.processosPainel = [
            {codigo: 1, tipo: 'MAPEAMENTO', dataCriacao: '2024-01-15T10:00:00'},
            {codigo: 2, tipo: 'MAPEAMENTO', dataCriacao: '2024-02-15T10:00:00'}
        ];

        wrapper.vm.filtroDataInicio = '2024-02-01';
        await nextTick();

        expect(wrapper.vm.processosFiltrados).toHaveLength(1);
    });

    it("deve abrir modais corretamente", async () => {
        wrapper = createWrapper();

        wrapper.vm.mostrarModalMapasVigentes = true;
        await nextTick();
        expect(wrapper.findComponent(ModalMapasVigentesStub).exists()).toBe(true);

        wrapper.vm.mostrarModalDiagnosticosGaps = true;
        await nextTick();
        expect(wrapper.findComponent(ModalDiagnosticosGapsStub).exists()).toBe(true);

        wrapper.vm.mostrarModalAndamentoGeral = true;
        await nextTick();
        expect(wrapper.findComponent(ModalAndamentoGeralStub).exists()).toBe(true);
    });

    it("deve computar mapas vigentes a partir da store de mapas", async () => {
        wrapper = createWrapper();
        mapasStore = useMapasStore();
        mapasStore.mapaCompleto = {
            codigo: 10,
            unidade: {sigla: 'TEST'},
            competencias: [{id: 1, nome: 'Comp 1'}]
        };

        await nextTick();
        expect(wrapper.vm.mapasVigentes).toHaveLength(1);
    });

    it("deve filtrar diagnosticos de gaps por tipo", async () => {
        wrapper = createWrapper();

        // Default has 4 diagnosticos
        wrapper.vm.filtroTipo = 'MAPEAMENTO'; // Not DIAGNOSTICO
        await nextTick();

        expect(wrapper.vm.diagnosticosGapsFiltrados).toHaveLength(0);
    });

    it("deve filtrar diagnosticos de gaps por data", async () => {
        wrapper = createWrapper();

        // Mock diagnosticos in RelatoriosView.vue have dates in Aug/Sep 2024
        wrapper.vm.filtroDataInicio = '2024-09-01';
        await nextTick();

        expect(wrapper.vm.diagnosticosGapsFiltrados).toHaveLength(2); // 2024-09-05 and 2024-09-10
    });
});
