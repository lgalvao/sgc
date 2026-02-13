import { beforeEach, describe, expect, it, vi } from "vitest";
import { flushPromises, mount } from "@vue/test-utils";
import Relatorios from "@/views/Relatorios.vue";
import { createTestingPinia } from "@pinia/testing";
import { useProcessosStore } from "@/stores/processos";
import { useMapasStore } from "@/stores/mapas";
import { nextTick } from "vue";

// Stubs
const PageHeaderStub = {
  template: '<div data-testid="page-header">{{ title }}</div>',
  props: ['title']
};
const RelatorioFiltrosStub = {
  template: '<div data-testid="relatorio-filtros"></div>',
  props: ['tipo', 'dataInicio', 'dataFim'],
  emits: ['update:tipo', 'update:dataInicio', 'update:dataFim']
};
const RelatorioCardsStub = {
  template: '<div data-testid="relatorio-cards"></div>',
  props: ['mapasVigentesCount', 'diagnosticosGapsCount', 'processosFiltradosCount'],
  emits: ['abrir-mapas-vigentes', 'abrir-diagnosticos-gaps', 'abrir-andamento-geral']
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
          BContainer: { template: '<div><slot /></div>' },
          PageHeader: PageHeaderStub,
          RelatorioFiltros: RelatorioFiltrosStub,
          RelatorioCards: RelatorioCardsStub,
          ModalMapasVigentes: ModalMapasVigentesStub,
          ModalDiagnosticosGaps: ModalDiagnosticosGapsStub,
          ModalAndamentoGeral: ModalAndamentoGeralStub,
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
      { codigo: 1, tipo: 'MAPEAMENTO', dataCriacao: '2024-01-01T10:00:00' },
      { codigo: 2, tipo: 'REVISAO', dataCriacao: '2024-01-01T10:00:00' }
    ];

    const filtros = wrapper.findComponent(RelatorioFiltrosStub);
    await filtros.vm.$emit('update:tipo', 'REVISAO');
    await nextTick();

    const cards = wrapper.findComponent(RelatorioCardsStub);
    expect(cards.props('processosFiltradosCount')).toBe(1);
  });

  it("deve filtrar processos por data", async () => {
    wrapper = createWrapper();
    processosStore = useProcessosStore();
    processosStore.processosPainel = [
      { codigo: 1, tipo: 'MAPEAMENTO', dataCriacao: '2024-01-15T10:00:00' },
      { codigo: 2, tipo: 'MAPEAMENTO', dataCriacao: '2024-02-15T10:00:00' }
    ];

    const filtros = wrapper.findComponent(RelatorioFiltrosStub);
    await filtros.vm.$emit('update:dataInicio', '2024-02-01');
    await nextTick();

    const cards = wrapper.findComponent(RelatorioCardsStub);
    expect(cards.props('processosFiltradosCount')).toBe(1);
  });

  it("deve abrir modais corretamente", async () => {
    wrapper = createWrapper();
    const cards = wrapper.findComponent(RelatorioCardsStub);

    await cards.vm.$emit('abrir-mapas-vigentes');
    expect(wrapper.findComponent(ModalMapasVigentesStub).exists()).toBe(true);

    await cards.vm.$emit('abrir-diagnosticos-gaps');
    expect(wrapper.findComponent(ModalDiagnosticosGapsStub).exists()).toBe(true);

    await cards.vm.$emit('abrir-andamento-geral');
    expect(wrapper.findComponent(ModalAndamentoGeralStub).exists()).toBe(true);
  });

  it("deve computar mapas vigentes a partir da store de mapas", async () => {
    wrapper = createWrapper();
    mapasStore = useMapasStore();
    mapasStore.mapaCompleto = {
      codigo: 10,
      unidade: { sigla: 'TEST' },
      competencias: [{ id: 1, nome: 'Comp 1' }]
    };

    await nextTick();
    const cards = wrapper.findComponent(RelatorioCardsStub);
    expect(cards.props('mapasVigentesCount')).toBe(1);
  });

  it("deve filtrar diagnosticos de gaps por tipo", async () => {
    wrapper = createWrapper();
    const filtros = wrapper.findComponent(RelatorioFiltrosStub);
    
    // Default has 4 diagnosticos
    await filtros.vm.$emit('update:tipo', 'MAPEAMENTO'); // Not DIAGNOSTICO
    await nextTick();
    
    const cards = wrapper.findComponent(RelatorioCardsStub);
    expect(cards.props('diagnosticosGapsCount')).toBe(0);
  });

  it("deve filtrar diagnosticos de gaps por data", async () => {
    wrapper = createWrapper();
    const filtros = wrapper.findComponent(RelatorioFiltrosStub);
    
    // Mock diagnosticos in useRelatorios.ts have dates in Aug/Sep 2024
    await filtros.vm.$emit('update:dataInicio', '2024-09-01');
    await nextTick();
    
    const cards = wrapper.findComponent(RelatorioCardsStub);
    expect(cards.props('diagnosticosGapsCount')).toBe(2); // 2024-09-05 and 2024-09-10
  });
});
