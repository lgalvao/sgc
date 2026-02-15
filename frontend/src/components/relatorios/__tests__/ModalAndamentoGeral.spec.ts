import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import ModalAndamentoGeral from '../ModalRelatorioAndamento.vue';
import * as csvUtils from '@/utils/csv';
import {SituacaoProcesso, TipoProcesso} from '@/types/tipos';

// Mock dependencies
vi.mock('@/utils/csv', () => ({
  gerarCSV: vi.fn(),
  downloadCSV: vi.fn(),
}));

describe('ModalAndamentoGeral.vue', () => {
  const mockProcessos = [
    {
      codigo: 1,
      descricao: 'Processo 1',
      situacao: SituacaoProcesso.EM_ANDAMENTO,
      situacaoLabel: 'Em andamento',
      tipo: TipoProcesso.MAPEAMENTO,
      tipoLabel: 'Mapeamento',
      dataLimite: '2023-10-15T00:00:00',
      dataLimiteFormatada: '15/10/2023',
      dataCriacao: '2023-01-01T00:00:00',
      unidadeCodigo: 1,
      unidadeNome: 'Unidade A',
    },
    {
      codigo: 2,
      descricao: 'Processo 2',
      situacao: SituacaoProcesso.FINALIZADO,
      situacaoLabel: 'Finalizado',
      tipo: TipoProcesso.REVISAO,
      tipoLabel: 'Revisão',
      dataLimite: '2023-11-20T00:00:00',
      dataLimiteFormatada: '20/11/2023',
      dataCriacao: '2023-06-01T00:00:00',
      unidadeCodigo: 2,
      unidadeNome: 'Unidade B',
    },
  ];

  const mountOptions = {
    props: {
      modelValue: true,
      processos: mockProcessos,
    },
    global: {
      stubs: {
        ModalPadrao: {
            template: '<div><slot /></div>',
            props: ['modelValue'],
            emits: ['update:modelValue']
        },
        BButton: true,
      },
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders correctly', () => {
    const wrapper = mount(ModalAndamentoGeral, mountOptions);
    expect(wrapper.findAll('tbody tr')).toHaveLength(2);
    expect(wrapper.text()).toContain('Processo 1');
    expect(wrapper.text()).toContain('MAPEAMENTO');
    expect(wrapper.text()).toContain('EM_ANDAMENTO');
    expect(wrapper.text()).toContain('Unidade A');
    expect(wrapper.text()).toContain('15/10/2023'); // formatted date

    expect(wrapper.text()).toContain('Processo 2');
    expect(wrapper.text()).toContain('FINALIZADO');
  });

  it('exports to CSV when button is clicked', async () => {
    const wrapper = mount(ModalAndamentoGeral, mountOptions);
    const exportBtn = wrapper.find('[data-testid="export-csv-andamento"]');

    await exportBtn.trigger('click');

    expect(csvUtils.gerarCSV).toHaveBeenCalledTimes(1);
    expect(csvUtils.downloadCSV).toHaveBeenCalledTimes(1);
    expect(csvUtils.downloadCSV).toHaveBeenCalledWith(undefined, 'andamento-geral.csv');
  });

  it('emits update:modelValue when modal is closed', async () => {
    const wrapper = mount(ModalAndamentoGeral, mountOptions);
    (wrapper.vm as any).modelValueComputed = false;

    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false]);
  });
});
