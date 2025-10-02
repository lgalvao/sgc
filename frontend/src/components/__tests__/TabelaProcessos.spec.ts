import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import TabelaProcessos from '../TabelaProcessos.vue';
import {Processo, SituacaoProcesso, TipoProcesso} from '@/types/tipos';

// Mock de dados de processo
const mockProcessos: (Processo & { unidadesFormatadas: string, dataFinalizacaoFormatada?: string | null })[] = [
  {
    id: 1,
    descricao: 'Processo Alpha',
      tipo: TipoProcesso.MAPEAMENTO,
    unidadesFormatadas: 'UNID1, UNID2',
      situacao: SituacaoProcesso.EM_ANDAMENTO,
    dataLimite: new Date(),
    dataFinalizacao: null,
  },
  {
    id: 2,
    descricao: 'Processo Beta',
      tipo: TipoProcesso.REVISAO,
    unidadesFormatadas: 'UNID3',
      situacao: SituacaoProcesso.FINALIZADO,
    dataLimite: new Date(),
    dataFinalizacao: new Date('2024-08-26'),
    dataFinalizacaoFormatada: '26/08/2024',
  },
];

describe('TabelaProcessos.vue', () => {
  it('deve renderizar a tabela e os cabeçalhos corretamente', () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    expect(wrapper.find('table').exists()).toBe(true);
    expect(wrapper.find('[data-testid="coluna-descricao"]').text()).toContain('Descrição');
    expect(wrapper.find('[data-testid="coluna-tipo"]').text()).toContain('Tipo');
    expect(wrapper.find('[data-testid="coluna-unidades"]').text()).toContain('Unidades participantes');
    expect(wrapper.find('[data-testid="coluna-situacao"]').text()).toContain('Situação');
    expect(wrapper.find('[data-testid="coluna-data-finalizacao"]').exists()).toBe(false); // Não deve existir por padrão
  });

  it('deve exibir os processos passados via prop', () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(mockProcessos.length);

    expect(rows[0].text()).toContain('Processo Alpha');
    expect(rows[0].text()).toContain('Mapeamento');
    expect(rows[0].text()).toContain('UNID1, UNID2');
    expect(rows[0].text()).toContain('Em andamento');

    expect(rows[1].text()).toContain('Processo Beta');
    expect(rows[1].text()).toContain('Revisão');
    expect(rows[1].text()).toContain('UNID3');
    expect(rows[1].text()).toContain('Finalizado');
  });

  it('deve emitir o evento ordenar ao clicar nos cabeçalhos', async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.find('[data-testid="coluna-descricao"]').trigger('click');
    expect(wrapper.emitted().ordenar).toEqual([['descricao']]);

    await wrapper.find('[data-testid="coluna-tipo"]').trigger('click');
    expect(wrapper.emitted().ordenar).toEqual([['descricao'], ['tipo']]);
  });

  it('deve exibir os indicadores de ordenação corretamente', () => {
    const wrapperAsc = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });
    expect(wrapperAsc.find('[data-testid="coluna-descricao"] span').text()).toBe('↑');

    const wrapperDesc = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: false,
      },
    });
    expect(wrapperDesc.find('[data-testid="coluna-descricao"] span').text()).toBe('↓');
  });

  it('deve emitir o evento selecionarProcesso ao clicar em uma linha', async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.findAll('tbody tr')[0].trigger('click');
    expect(wrapper.emitted().selecionarProcesso).toEqual([[mockProcessos[0]]]);

    await wrapper.findAll('tbody tr')[1].trigger('click');
    expect(wrapper.emitted().selecionarProcesso).toEqual([[mockProcessos[0]], [mockProcessos[1]]]);
  });

  it('deve exibir a coluna Finalizado em quando showDataFinalizacao é true', () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: true,
      },
    });

    expect(wrapper.find('[data-testid="coluna-data-finalizacao"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="coluna-data-finalizacao"]').text()).toContain('Finalizado em');

    const rows = wrapper.findAll('tbody tr');
    expect(rows[1].text()).toContain('26/08/2024'); // Processo Beta tem dataFinalizacaoFormatada
  });

  it('não deve exibir a coluna Finalizado em quando showDataFinalizacao é false ou não fornecido', () => {
    const wrapperFalse = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: false,
      },
    });
    expect(wrapperFalse.find('[data-testid="coluna-data-finalizacao"]').exists()).toBe(false);

    const wrapperUndefined = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
      },
    });
    expect(wrapperUndefined.find('[data-testid="coluna-data-finalizacao"]').exists()).toBe(false);
  });

  it('deve exibir indicadores de ordenação para diferentes critérios', () => {
    // Teste para critério 'tipo'
    const wrapperTipo = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'tipo',
        direcaoOrdenacaoAsc: true,
      },
    });
    expect(wrapperTipo.find('[data-testid="coluna-tipo"] span').text()).toBe('↑');

    // Teste para critério 'unidades'
    const wrapperUnidades = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'unidades',
        direcaoOrdenacaoAsc: false,
      },
    });
    expect(wrapperUnidades.find('[data-testid="coluna-unidades"] span').text()).toBe('↓');

    // Teste para critério 'situacao'
    const wrapperSituacao = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'situacao',
        direcaoOrdenacaoAsc: true,
      },
    });
    expect(wrapperSituacao.find('[data-testid="coluna-situacao"] span').text()).toBe('↑');
  });

  it('deve exibir indicadores de ordenação para dataFinalizacao quando showDataFinalizacao é true', () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'dataFinalizacao',
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: true,
      },
    });

    expect(wrapper.find('[data-testid="coluna-data-finalizacao"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="coluna-data-finalizacao"] span').text()).toBe('↑');
  });

  it('deve exibir data de finalização formatada quando showDataFinalizacao é true', () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: true,
      },
    });

    const rows = wrapper.findAll('tbody tr');
    // O segundo processo tem dataFinalizacaoFormatada
    expect(rows[1].text()).toContain('26/08/2024');
  });

  it('deve emitir ordenar com dataFinalizacao quando showDataFinalizacao é true', async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: 'descricao',
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: true,
      },
    });

    await wrapper.find('[data-testid="coluna-data-finalizacao"]').trigger('click');
    expect(wrapper.emitted().ordenar).toEqual([['dataFinalizacao']]);
  });
});