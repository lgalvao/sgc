import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, VueWrapper } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ImportarAtividadesModal from '../ImportarAtividadesModal.vue';
import { Atividade, ProcessoResumo, TipoProcesso, UnidadeParticipante, SituacaoProcesso } from '@/types/tipos';
import { nextTick, ref } from 'vue';
import { useAtividadesStore } from '@/stores/atividades';

type ImportarAtividadesModalVM = InstanceType<typeof ImportarAtividadesModal>;

const mockProcessos: ProcessoResumo[] = [
  { codigo: 1, descricao: 'Processo 1', tipo: TipoProcesso.MAPEAMENTO, situacao: SituacaoProcesso.FINALIZADO, dataCriacao: '2021-01-01', dataLimite: '2021-01-01', unidadeCodigo: 1, unidadeNome: 'test' },
];
const mockUnidades: UnidadeParticipante[] = [{ codUnidade: 10, sigla: 'U1', codSubprocesso: 100 }];
const mockAtividades: Atividade[] = [{ codigo: 1, descricao: 'Atividade A', conhecimentos: [] }];

const BModalStub = {
  template: `<div v-if="modelValue"><slot /><slot name="footer" /></div>`,
  props: ['modelValue'],
};

const BFormSelectStub = {
  template: `
    <select :value="modelValue" @change="$emit('update:modelValue', $event.target.value)">
      <slot name="first" />
      <option v-for="option in options" :key="option[valueField]" :value="option[valueField]">
        {{ option[textField] }}
      </option>
    </select>
  `,
  props: ['modelValue', 'options', 'valueField', 'textField'],
};

const BFormCheckboxStub = {
  template: `<input type="checkbox" :checked="modelValue.some(item => item.codigo === value.codigo)" @change="onChange" />`,
  props: ['modelValue', 'value'],
  emits: ['update:modelValue'],
  setup(props: any, { emit }: any) {
    const onChange = (event: any) => {
      if (event.target.checked) {
        emit('update:modelValue', [...props.modelValue, props.value]);
      } else {
        emit('update:modelValue', props.modelValue.filter((item: any) => item.codigo !== props.value.codigo));
      }
    };
    return { onChange };
  },
};

const BFormSelectOptionStub = {
  template: `<option :value="value" :disabled="disabled"><slot /></option>`,
  props: ['value', 'disabled'],
};

const mockImportar = vi.fn();
vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ execute: mockImportar, error: ref(null), isLoading: ref(false), clearError: vi.fn() }),
}));
vi.mock('@/stores/processos', () => ({
  useProcessosStore: () => ({
    processosPainel: mockProcessos,
    processoDetalhe: { unidades: mockUnidades },
    fetchProcessosPainel: vi.fn(),
    fetchProcessoDetalhe: vi.fn(),
  }),
}));
vi.mock('@/stores/atividades');

describe('ImportarAtividadesModal', () => {
  let wrapper: VueWrapper<ImportarAtividadesModalVM>;

  beforeEach(() => {
    setActivePinia(createPinia());
    const atividadesStore = useAtividadesStore();
    vi.mocked(atividadesStore.getAtividadesPorSubprocesso).mockReturnValue(mockAtividades);
    vi.clearAllMocks();

    wrapper = mount(ImportarAtividadesModal, {
      props: { mostrar: true, codSubrocessoDestino: 999 },
      global: {
        stubs: {
          'b-modal': BModalStub,
          'b-form-select': BFormSelectStub,
          'b-form-checkbox': BFormCheckboxStub,
          'b-form-select-option': BFormSelectOptionStub,
        },
      },
    });
  });

  it('deve emitir "fechar" ao clicar em Cancelar', async () => {
    await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });

  it('deve habilitar o botão de importação e chamar a API ao importar', async () => {
    const importButton = wrapper.find('[data-testid="btn-importar"]');
    expect((importButton.element as HTMLButtonElement).disabled).toBe(true);

    await wrapper.find('[data-testid="select-processo"]').setValue('1');
    await nextTick();
    await wrapper.find('[data-testid="select-unidade"]').setValue('10');
    await nextTick();

    const checkbox = wrapper.find('[data-testid^="checkbox-atividade-"]');
    await checkbox.trigger('change');
    await nextTick();

    expect((importButton.element as HTMLButtonElement).disabled).toBe(false);

    mockImportar.mockResolvedValue(true);
    await importButton.trigger('click');

    expect(mockImportar).toHaveBeenCalledWith(999, 100, [1]);
    expect(wrapper.emitted('importar')).toBeTruthy();
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });
});
