import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ModalMapasVigentes from '../ModalMapasVigentes.vue';
import { BModal } from 'bootstrap-vue-next';
import * as csvUtils from '@/utils/csv';

// Mock dependencies
vi.mock('@/utils/csv', () => ({
  gerarCSV: vi.fn(),
  downloadCSV: vi.fn(),
}));

describe('ModalMapasVigentes.vue', () => {
  const mockMapas = [
    { id: 1, unidade: 'Unidade A', competencias: [1, 2, 3] },
    { id: 2, unidade: 'Unidade B', competencias: [] },
    { id: 3, unidade: 'Unidade C' }, // undefined competencias
  ];

  const mountOptions = {
    props: {
      modelValue: true,
      mapas: mockMapas,
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
    const wrapper = mount(ModalMapasVigentes, mountOptions);
    expect(wrapper.findAll('tbody tr')).toHaveLength(3);
    expect(wrapper.text()).toContain('Unidade A');
    expect(wrapper.text()).toContain('3'); // 3 competencias
    expect(wrapper.text()).toContain('Unidade B');
    expect(wrapper.text()).toContain('0'); // 0 competencias
    expect(wrapper.text()).toContain('Unidade C');
  });

  it('exports to CSV when button is clicked', async () => {
    const wrapper = mount(ModalMapasVigentes, mountOptions);
    const exportBtn = wrapper.find('[data-testid="export-csv-mapas"]');

    await exportBtn.trigger('click');

    expect(csvUtils.gerarCSV).toHaveBeenCalledTimes(1);
    expect(csvUtils.downloadCSV).toHaveBeenCalledTimes(1);
    expect(csvUtils.downloadCSV).toHaveBeenCalledWith(undefined, 'mapas-vigentes.csv');
  });

  it('emits update:modelValue when modal is closed', async () => {
    const wrapper = mount(ModalMapasVigentes, mountOptions);
    const modal = wrapper.findComponent(BModal);

    await modal.vm.$emit('update:modelValue', false);

    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false]);
  });
});
