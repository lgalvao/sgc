import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import TabelaProcessos from '../TabelaProcessos.vue';
import {type ProcessoResumo, SituacaoProcesso, TipoProcesso} from '@/types/tipos';

// Mock BTable component
const MockBTable = {
  template: `
    <table class="table">
      <thead>
        <tr>
          <th v-for="field in fields" :key="field.key" :aria-sort="getAriaSort(field.key)" @click="handleSort(field.key)">
            {{ field.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="items.length === 0">
          <td :colspan="fields.length">
            <slot name="empty"></slot>
          </td>
        </tr>
        <tr v-for="(item, index) in items" :key="index" @click="$emit('row-clicked', item)">
          <td v-for="field in fields" :key="field.key">
            {{ getItemValue(item, field.key) }}
          </td>
        </tr>
      </tbody>
    </table>
  `,
  props: ['items', 'fields', 'sortBy', 'sortDesc'],
  emits: ['row-clicked', 'sort-changed'],
  methods: {
    getAriaSort(key) {
      if (this.sortBy === key) {
        return this.sortDesc ? 'descending' : 'ascending';
      }
      return 'none';
    },
    getItemValue(item, key) {
      // Handle nested properties if necessary
      if (key.includes('.')) {
        return key.split('.').reduce((o, i) => (o ? o[i] : ''), item);
      }
      return item[key];
    },
    handleSort(key) {
      this.$emit('sort-changed', { sortBy: key, sortDesc: !this.sortDesc });
    }
  },
};

// Mock de dados de processo
const mockProcessos: ProcessoResumo[] = [
  {
    codigo: 1,
    descricao: 'Processo Alpha',
    tipo: TipoProcesso.MAPEAMENTO,
    unidadeCodigo: 1,
    unidadeNome: 'UNID1, UNID2',
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    dataLimite: new Date().toISOString(),
    dataCriacao: new Date().toISOString(),
    dataFinalizacao: null,
  },
  {
    codigo: 2,
    descricao: 'Processo Beta',
    tipo: TipoProcesso.REVISAO,
    unidadeCodigo: 3,
    unidadeNome: 'UNID3',
    situacao: SituacaoProcesso.FINALIZADO,
    dataLimite: new Date().toISOString(),
    dataCriacao: new Date().toISOString(),
    dataFinalizacao: new Date('2024-08-26').toISOString(),
    dataFinalizacaoFormatada: '26/08/2024',
  },
];

describe('TabelaProcessos.vue', () => {
  const mountOptions = {
    global: {
      components: {
        BTable: MockBTable,
      },
    },
  };

  it('deve renderizar a tabela e os cabeçalhos corretamente', async () => {
    const wrapper = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    const table = wrapper.findComponent(MockBTable);
    expect(table.exists()).toBe(true);

    await wrapper.vm.$nextTick();

    const headers = table.findAll('th');
    expect(headers[0].text()).toContain('Descrição');
    expect(headers[1].text()).toContain('Tipo');
    expect(headers[2].text()).toContain('Situação');
  });

  it('deve exibir os processos passados via prop', async () => {
    const wrapper = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.vm.$nextTick();

    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(mockProcessos.length);

    const cells = rows[0].findAll('td');
    expect(cells[0].text()).toBe('Processo Alpha');
    expect(cells[1].text()).toBe('MAPEAMENTO');
    expect(cells[2].text()).toBe('EM_ANDAMENTO');

    const cells2 = rows[1].findAll('td');
    expect(cells2[0].text()).toBe('Processo Beta');
    expect(cells2[1].text()).toBe('REVISAO');
    expect(cells2[2].text()).toBe('FINALIZADO');
  });

  it('deve emitir o evento ordenar ao receber o evento sort-changed', async () => {
    const wrapper = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.findComponent(MockBTable).vm.$emit('sort-changed', { sortBy: 'tipo' });

    expect(wrapper.emitted('ordenar')).toBeTruthy();
    expect(wrapper.emitted('ordenar')![0]).toEqual(['tipo']);
  });

  it('deve exibir os indicadores de ordenação corretamente', async () => {
    const wrapperAsc = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapperAsc.vm.$nextTick();
    expect(wrapperAsc.find('th[aria-sort="ascending"]').exists()).toBe(true);

    const wrapperDesc = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: false,
      },
    });

    await wrapperDesc.vm.$nextTick();
    expect(wrapperDesc.find('th[aria-sort="descending"]').exists()).toBe(true);
  });

  it('deve emitir o evento selecionarProcesso ao clicar em uma linha', async () => {
    const wrapper = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.vm.$nextTick();

    const rows = wrapper.findAll('tbody tr');
    await rows[0].trigger('click');

    expect(wrapper.emitted('selecionarProcesso')).toBeTruthy();
    expect(wrapper.emitted('selecionarProcesso')![0]).toEqual([mockProcessos[0]]);
  });

  it('deve exibir a coluna Finalizado em quando showDataFinalizacao é true', async () => {
    const wrapper = mount(TabelaProcessos, {
      ...mountOptions,
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: true,
      },
    });

    await wrapper.vm.$nextTick();

    const headers = wrapper.findAll('th');
    expect(headers.some(h => h.text() === 'Finalizado em')).toBe(true);

    const rows = wrapper.findAll('tbody tr');
    expect(rows[1].text()).toContain('26/08/2024');
  });
});
