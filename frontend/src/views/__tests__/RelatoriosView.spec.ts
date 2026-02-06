import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {mount, flushPromises} from '@vue/test-utils';
import RelatoriosView from '@/views/RelatoriosView.vue';
import {createTestingPinia} from '@pinia/testing';
import {setupComponentTest} from '@/test-utils/componentTestHelpers';

// Mock URL.createObjectURL
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url');

describe('RelatoriosView.vue', () => {
  const ctx = setupComponentTest();

  // Very minimal stubs for external library components ONLY
  const stubs = {
    BContainer: { template: '<div class="b-container"><slot /></div>' },
    BRow: { template: '<div class="b-row"><slot /></div>' },
    BCol: { template: '<div class="b-col"><slot /></div>' },
    BCard: { template: '<div class="b-card"><slot /></div>' },
    BFormGroup: { template: '<div class="b-form-group"><slot /></div>' },
    BFormSelect: { name: 'BFormSelect', props: ['options', 'modelValue'], template: '<select />' },
    BFormInput: { name: 'BFormInput', props: ['modelValue'], template: '<input />' },
    BModal: { name: 'BModal', template: '<div class="b-modal" v-if="modelValue"><slot /></div>', props: ['modelValue'] },
    BTable: { template: '<table />' },
    BSpinner: { template: '<div />' },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });
  
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('interage com filtros e cards e abre modais', async () => {
    const pinia = createTestingPinia({ stubActions: true });
    ctx.wrapper = mount(RelatoriosView, {
        global: {
            plugins: [pinia],
            stubs
        }
    });
    await flushPromises();

    // Trigger updates on filtros using correct event names
    const filtros = ctx.wrapper.findComponent({ name: 'RelatorioFiltros' });
    await filtros.vm.$emit('update:tipo', 'MAPEAMENTO');
    await filtros.vm.$emit('update:dataInicio', '2023-01-01');
    await filtros.vm.$emit('update:dataFim', '2023-12-31');

    // Trigger card actions
    const cards = ctx.wrapper.findComponent({ name: 'RelatorioCards' });
    await cards.vm.$emit('abrir-mapas-vigentes');
    await cards.vm.$emit('abrir-diagnosticos-gaps');
    await cards.vm.$emit('abrir-andamento-geral');

    await flushPromises();
    
    expect(ctx.wrapper.vm.mostrarModalMapasVigentes).toBe(true);
    expect(ctx.wrapper.vm.mostrarModalDiagnosticosGaps).toBe(true);
    expect(ctx.wrapper.vm.mostrarModalAndamentoGeral).toBe(true);
    
    // Check if modals (child components) are rendered when state is true
    expect(ctx.wrapper.findComponent({ name: 'ModalMapasVigentes' }).exists()).toBe(true);
    expect(ctx.wrapper.findComponent({ name: 'ModalDiagnosticosGaps' }).exists()).toBe(true);
    expect(ctx.wrapper.findComponent({ name: 'ModalAndamentoGeral' }).exists()).toBe(true);
  });
});
