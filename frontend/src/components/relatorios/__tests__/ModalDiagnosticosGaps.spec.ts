import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ModalDiagnosticosGaps from '../ModalDiagnosticosGaps.vue';
import { BModal, BButton } from 'bootstrap-vue-next';
import * as csvUtils from '@/utils/csv';

// Mock dependencies
vi.mock('@/utils/csv', () => ({
  gerarCSV: vi.fn(),
  downloadCSV: vi.fn(),
}));

describe('ModalDiagnosticosGaps.vue', () => {
  const mockDiagnosticos = [
    {
      id: 1,
      processo: 'Processo A',
      unidade: 'Unidade X',
      gaps: 2,
      importanciaMedia: 4.5,
      dominioMedio: 3.0,
      competenciasCriticas: ['Comp A', 'Comp B'],
      data: new Date('2023-10-15'),
      status: 'Finalizado',
    },
    {
      id: 2,
      processo: 'Processo B',
      unidade: 'Unidade Y',
      gaps: 5,
      importanciaMedia: 3.0,
      dominioMedio: 2.0,
      competenciasCriticas: ['Comp C'],
      data: new Date('2023-11-20'),
      status: 'Em análise',
    },
    {
      id: 3,
      processo: 'Processo C',
      unidade: 'Unidade Z',
      gaps: 0,
      importanciaMedia: 0,
      dominioMedio: 0,
      competenciasCriticas: [],
      data: new Date('2023-12-01'),
      status: 'Pendente',
    },
     {
      id: 4,
      processo: 'Processo D',
      unidade: 'Unidade W',
      gaps: 1,
      importanciaMedia: 1,
      dominioMedio: 1,
      competenciasCriticas: [],
      data: new Date('2023-12-01'),
      status: 'Outro',
    },
  ];

  const mountOptions = {
    props: {
      modelValue: true,
      diagnosticos: mockDiagnosticos,
    },
    global: {
      stubs: {
        BModal: {
            template: '<div><slot /></div>',
            props: ['modelValue']
        },
        BButton: true,
      },
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders correctly', () => {
    const wrapper = mount(ModalDiagnosticosGaps, mountOptions);
    expect(wrapper.findAll('tbody tr')).toHaveLength(4);
    expect(wrapper.text()).toContain('Processo A');
    expect(wrapper.text()).toContain('Unidade X');
    expect(wrapper.text()).toContain('Finalizado');
  });

  it('applies correct status classes', () => {
    const wrapper = mount(ModalDiagnosticosGaps, mountOptions);
    const badges = wrapper.findAll('.badge');

    // Finalizado -> bg-success
    expect(badges[0].classes()).toContain('bg-success');
    // Em análise -> bg-warning
    expect(badges[1].classes()).toContain('bg-warning');
    // Pendente -> bg-danger
    expect(badges[2].classes()).toContain('bg-danger');
    // Default -> bg-secondary
    expect(badges[3].classes()).toContain('bg-secondary');
  });

  it('formats date correctly', () => {
     const wrapper = mount(ModalDiagnosticosGaps, mountOptions);
     // 15/10/2023 for first item
     expect(wrapper.text()).toContain('15/10/2023');
  });

  it('exports to CSV when button is clicked', async () => {
    const wrapper = mount(ModalDiagnosticosGaps, mountOptions);
    const exportBtn = wrapper.find('[data-testid="export-csv-diagnosticos"]');

    await exportBtn.trigger('click');

    expect(csvUtils.gerarCSV).toHaveBeenCalledTimes(1);
    expect(csvUtils.downloadCSV).toHaveBeenCalledTimes(1);
    expect(csvUtils.downloadCSV).toHaveBeenCalledWith(undefined, 'diagnosticos-gaps.csv'); // undefined because gerarCSV mock returns undefined by default
  });

  it('emits update:modelValue when modal is closed', async () => {
      const wrapper = mount(ModalDiagnosticosGaps, mountOptions);

      const modal = wrapper.findComponent(BModal);
      await modal.vm.$emit('update:modelValue', false);

      expect(wrapper.emitted('update:modelValue')).toBeTruthy();
      expect(wrapper.emitted('update:modelValue')![0]).toEqual([false]);
  });
});
