import {describe, it, expect} from 'vitest';
import {mount} from '@vue/test-utils';
import RelatorioFiltros from '../RelatorioFiltros.vue';
import {defineComponent} from 'vue';

const BFormSelectStub = defineComponent({
  name: 'BFormSelect',
  inheritAttrs: false,
  props: {
    options: { type: Array, default: () => [] },
    modelValue: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  computed: {
    normalizedOptions() {
      return (this.options || []).map((opt: any) => typeof opt === 'object' ? opt : { value: opt, text: opt });
    }
  },
  template: `
    <select :value="modelValue" @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)" v-bind="$attrs">
      <option v-for="opt in normalizedOptions" :key="String(opt.value)" :value="opt.value">
        {{ opt.text }}
      </option>
    </select>
  `
});

const BFormInputStub = defineComponent({
  name: 'BFormInput',
  inheritAttrs: false,
  props: {
    modelValue: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', ($event.target as HTMLInputElement).value)" v-bind="$attrs">'
});

describe('RelatorioFiltros.vue', () => {
  it('renderiza todos os filtros e emite atualizações', async () => {
    const wrapper = mount(RelatorioFiltros, {
      props: {
        tipo: '',
        dataInicio: '',
        dataFim: ''
      },
      global: {
        stubs: {
          'b-row': { template: '<div><slot /></div>' },
          'b-col': { template: '<div><slot /></div>' },
          'b-form-group': { template: '<div><slot /></div>' },
          'b-form-select': BFormSelectStub,
          'b-form-input': BFormInputStub
        }
      }
    });

    const select = wrapper.find('#filtro-tipo');
    await select.setValue('MAPEAMENTO');
    expect(wrapper.emitted('update:tipo')).toBeTruthy();
    expect(wrapper.emitted('update:tipo')![0]).toEqual(['MAPEAMENTO']);

    const inputInicio = wrapper.find('#filtro-data-inicio');
    await inputInicio.setValue('2023-01-01');
    expect(wrapper.emitted('update:dataInicio')).toBeTruthy();
    expect(wrapper.emitted('update:dataInicio')![0]).toEqual(['2023-01-01']);

    const inputFim = wrapper.find('#filtro-data-fim');
    await inputFim.setValue('2023-12-31');
    expect(wrapper.emitted('update:dataFim')).toBeTruthy();
    expect(wrapper.emitted('update:dataFim')![0]).toEqual(['2023-12-31']);
  });
});
