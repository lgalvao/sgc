import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import RelatoriosView from '@/views/RelatoriosView.vue';
import { useProcessosStore } from '@/stores/processos';
import { useMapasStore } from '@/stores/mapas';
import { TipoProcesso } from '@/types/tipos';

// Mock URL.createObjectURL
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url');

describe('RelatoriosView.vue', () => {
  let wrapper: any;
  let processosStore: any;
  let mapasStore: any;

  const mockProcessos = [
    {
      codigo: 1,
      descricao: 'Processo 1',
      tipo: TipoProcesso.MAPEAMENTO,
      situacao: 'EM_ANDAMENTO',
      dataCriacao: '2023-01-01T00:00:00',
      dataLimite: '2023-12-31T00:00:00',
      unidadeNome: 'Unidade 1'
    },
    {
      codigo: 2,
      descricao: 'Processo 2',
      tipo: TipoProcesso.REVISAO,
      situacao: 'CRIADO',
      dataCriacao: '2023-06-01T00:00:00',
      dataLimite: '2023-12-31T00:00:00',
      unidadeNome: 'Unidade 2'
    }
  ];

  const mockMapa = {
    codigo: 1,
    unidade: { sigla: 'TEST' },
    competencias: [{}, {}] // 2 items
  };

  beforeEach(() => {
    vi.clearAllMocks();

    wrapper = mount(RelatoriosView, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              processos: {
                processosPainel: mockProcessos,
              },
              mapas: {
                mapaCompleto: mockMapa,
              },
            },
          }),
        ],
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BCard: { template: '<div class="card" @click="$emit(\'click\')"><slot /></div>' },
          BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          BModal: { template: '<div v-if="modelValue" data-testid="modal"><slot /></div>', props: ['modelValue'] },
          BFormSelect: {
            template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"></select>',
            props: ['modelValue', 'options']
          },
          BFormInput: {
            template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue']
          },
        },
      },
    });

    processosStore = useProcessosStore();
    mapasStore = useMapasStore();
  });
  
  afterEach(() => {
      vi.restoreAllMocks();
  });

  it('renders report cards', () => {
    expect(wrapper.find('[data-testid="card-relatorio-mapas"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="card-relatorio-gaps"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="card-relatorio-andamento"]').exists()).toBe(true);
  });

  it('filters processes', async () => {
    // Check initial count
    expect(wrapper.vm.processosFiltrados).toHaveLength(2);

    // Filter by type
    wrapper.vm.filtroTipo = TipoProcesso.REVISAO;
    await wrapper.vm.$nextTick();
    
    expect(wrapper.vm.processosFiltrados).toHaveLength(1);
    expect(wrapper.vm.processosFiltrados[0].tipo).toBe(TipoProcesso.REVISAO);
    
    // Filter by date
    wrapper.vm.filtroTipo = ''; // Reset type
    wrapper.vm.filtroDataInicio = '2023-05-01'; // Should exclude Processo 1 (Jan)
    await wrapper.vm.$nextTick();
    
    expect(wrapper.vm.processosFiltrados).toHaveLength(1);
    expect(wrapper.vm.processosFiltrados[0].codigo).toBe(2);
  });

  it('opens modals on card click', async () => {
    await wrapper.find('[data-testid="card-relatorio-mapas"]').trigger('click');
    expect(wrapper.vm.mostrarModalMapasVigentes).toBe(true);
    
    wrapper.vm.mostrarModalMapasVigentes = false;
    await wrapper.vm.$nextTick();

    await wrapper.find('[data-testid="card-relatorio-gaps"]').trigger('click');
    expect(wrapper.vm.mostrarModalDiagnosticosGaps).toBe(true);
    
    wrapper.vm.mostrarModalDiagnosticosGaps = false;
    await wrapper.vm.$nextTick();
    
    await wrapper.find('[data-testid="card-relatorio-andamento"]').trigger('click');
    expect(wrapper.vm.mostrarModalAndamentoGeral).toBe(true);
  });

  it('exports CSV for Mapas Vigentes', async () => {
    // We need to ensure map store has data accessible via getter or state
    // Because computed property depends on it.
    // In createTestingPinia, we set initialState. 
    // Let's verify computed property value
    expect(wrapper.vm.mapasVigentes).toHaveLength(1);

    wrapper.vm.mostrarModalMapasVigentes = true;
    await wrapper.vm.$nextTick();
    
    const btn = wrapper.find('[data-testid="export-csv-mapas"]');
    
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click');
    const setAttributeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'setAttribute');
    
    await btn.trigger('click');
    
    expect(global.URL.createObjectURL).toHaveBeenCalled();
    expect(setAttributeSpy).toHaveBeenCalledWith('download', 'mapas-vigentes.csv');
    expect(clickSpy).toHaveBeenCalled();
  });

  it('exports CSV for Diagnosticos Gaps', async () => {
    wrapper.vm.mostrarModalDiagnosticosGaps = true;
    await wrapper.vm.$nextTick();
    
    const btn = wrapper.find('[data-testid="export-csv-diagnosticos"]');
    
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click');
    const setAttributeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'setAttribute');
    
    await btn.trigger('click');
    
    expect(setAttributeSpy).toHaveBeenCalledWith('download', 'diagnosticos-gaps.csv');
    expect(clickSpy).toHaveBeenCalled();
  });

  it('exports CSV for Andamento Geral', async () => {
    wrapper.vm.mostrarModalAndamentoGeral = true;
    await wrapper.vm.$nextTick();
    
    const btn = wrapper.find('[data-testid="export-csv-andamento"]');
    
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click');
    const setAttributeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'setAttribute');
    
    await btn.trigger('click');
    
    expect(setAttributeSpy).toHaveBeenCalledWith('download', 'andamento-geral.csv');
    expect(clickSpy).toHaveBeenCalled();
  });
});
