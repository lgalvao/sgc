import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import ImportarAtividadesModal from '../ImportarAtividadesModal.vue';
import {Atividade, Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos';

// Mock dos stores
const mockProcessosStore = {
  processos: [] as Processo[],
  getUnidadesDoProcesso: vi.fn(),
};

const mockAtividadesStore = {
  fetchAtividadesPorSubprocesso: vi.fn(),
  getAtividadesPorSubprocesso: vi.fn(),
};

vi.mock('@/stores/processos', () => ({
  useProcessosStore: () => mockProcessosStore,
}));

vi.mock('@/stores/atividades', () => ({
  useAtividadesStore: () => mockAtividadesStore,
}));

describe('ImportarAtividadesModal.vue', () => {
  let pinia: ReturnType<typeof createPinia>;

  beforeEach(() => {
    pinia = createPinia();
    setActivePinia(pinia);

    // Reset mocks
    vi.clearAllMocks();
  });

  const mountComponent = (props: { mostrar: boolean } = { mostrar: true }) => {
    return mount(ImportarAtividadesModal, {
      props,
      global: {
        plugins: [pinia]
      }
    });
  };

  describe('Renderização', () => {
    it('deve renderizar o modal quando mostrar=true', () => {
      const wrapper = mountComponent({ mostrar: true });

      expect(wrapper.find('.modal').exists()).toBe(true);
      expect(wrapper.find('.modal-dialog').exists()).toBe(true);
      expect(wrapper.find('.modal-content').exists()).toBe(true);
      expect(wrapper.find('.modal-header').exists()).toBe(true);
      expect(wrapper.find('.modal-body').exists()).toBe(true);
      expect(wrapper.find('.modal-footer').exists()).toBe(true);
    });

    it('não deve renderizar o modal quando mostrar=false', () => {
      const wrapper = mountComponent({ mostrar: false });

      expect(wrapper.find('.modal').exists()).toBe(false);
      expect(wrapper.find('.modal-backdrop').exists()).toBe(false);
    });

    it('deve renderizar o título correto', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('.modal-title').text()).toBe('Importação de atividades');
    });

    it('deve renderizar os botões de fechar e importar', () => {
      const wrapper = mountComponent();

      const buttons = wrapper.findAll('.modal-footer button');
      expect(buttons).toHaveLength(2);
      expect(buttons[0].text()).toBe('Cancelar');
      expect(buttons[1].text()).toBe('Importar');
    });
  });

  describe('Seleção de Processo', () => {
    beforeEach(() => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo de Mapeamento 1',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
        {
          id: 2,
          descricao: 'Processo de Revisão 1',
          tipo: TipoProcesso.REVISAO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
        {
          id: 3,
          descricao: 'Processo em Andamento',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.EM_ANDAMENTO,
          dataLimite: new Date(),
          dataFinalizacao: null,
        },
      ];
    });

    it('deve exibir apenas processos finalizados de mapeamento/revisão', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      const options = wrapper.findAll('select#processo-select option');
      expect(options).toHaveLength(3); // 2 finalizados + 1 disabled
      expect(options[1].text()).toBe('Processo de Mapeamento 1');
      expect(options[2].text()).toBe('Processo de Revisão 1');
    });

    it('deve mostrar mensagem quando não há processos disponíveis', async () => {
      mockProcessosStore.processos = [
        {
          id: 3,
          descricao: 'Processo em Andamento',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.EM_ANDAMENTO,
          dataLimite: new Date(),
          dataFinalizacao: null,
        },
      ];

      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      expect(wrapper.text()).toContain('Nenhum processo disponível para importação.');
    });

    it('deve desabilitar seleção de unidade quando nenhum processo selecionado', () => {
      const wrapper = mountComponent();

      const unidadeSelect = wrapper.find('select#unidade-select');
      expect(unidadeSelect.attributes('disabled')).toBeDefined();
    });
  });

  describe('Seleção de Unidade', () => {
    beforeEach(() => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([
        { id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado' } as Subprocesso,
        { id: 2, unidade: 'UNID2', idProcesso: 1, situacao: 'Finalizado' } as Subprocesso,
      ]);
    });

    it('deve carregar unidades quando processo selecionado', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const unidadeSelect = wrapper.find('select#unidade-select');
      expect(unidadeSelect.attributes('disabled')).toBeUndefined();

      const options = wrapper.findAll('select#unidade-select option');
      expect(options).toHaveLength(3); // 2 unidades + 1 disabled
      expect(options[1].text()).toBe('UNID1');
      expect(options[2].text()).toBe('UNID2');
    });

    it('deve limpar unidades quando processo retorna array vazio', async () => {
      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([]);

      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const options = wrapper.findAll('select#unidade-select option');
      expect(options).toHaveLength(1); // Apenas a opção disabled
    });
  });

  describe('Seleção de Atividades', () => {
    beforeEach(() => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([
        { id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado' } as Subprocesso,
      ]);

      mockAtividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue([
        { id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: [] } as Atividade,
        { id: 2, descricao: 'Atividade 2', idSubprocesso: 1, conhecimentos: [] } as Atividade,
      ]);
    });

    it('deve carregar atividades quando unidade selecionada', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Selecionar unidade
      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const checkboxes = wrapper.findAll('.form-check-input[type="checkbox"]');
      expect(checkboxes).toHaveLength(2);
      expect(checkboxes[0].attributes('value')).toBe('[object Object]');
      expect(checkboxes[1].attributes('value')).toBe('[object Object]');
    });

    it('deve mostrar mensagem quando não há atividades', async () => {
      mockAtividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue([]);

      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Selecionar unidade
      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      expect(wrapper.text()).toContain('Nenhuma atividade encontrada para esta unidade/processo.');
    });

    it('deve limpar atividades quando unidadeSelecionada é null', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Selecionar unidade
      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Verificar que atividades foram carregadas
      expect(wrapper.findAll('.form-check-input[type="checkbox"]')).toHaveLength(2);

      // Simular que unidadeSelecionada se torna null
      const vm = wrapper.vm as any;
      await vm.selecionarUnidade(null);

      // Verificar que atividades foram limpas
      expect(wrapper.findAll('.form-check-input[type="checkbox"]')).toHaveLength(0);
    });
  });

  describe('Botão Importar', () => {
    beforeEach(() => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([
        { id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado' } as Subprocesso,
      ]);

      mockAtividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue([
        { id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: [] } as Atividade,
        { id: 2, descricao: 'Atividade 2', idSubprocesso: 1, conhecimentos: [] } as Atividade,
      ]);
    });

    it('deve estar desabilitado quando nenhuma atividade selecionada', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo e unidade
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const importarButton = wrapper.find('.btn-outline-primary');
      expect(importarButton.attributes('disabled')).toBeDefined();
    });

    it('deve estar habilitado quando atividades selecionadas', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo e unidade
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Selecionar atividade
      const checkboxes = wrapper.findAll('.form-check-input[type="checkbox"]');
      await checkboxes[0].setValue(true);

      await wrapper.vm.$nextTick();

      const importarButton = wrapper.find('.btn-outline-primary');
      expect(importarButton.attributes('disabled')).toBeUndefined();
    });

    it('deve estar desabilitado quando nenhuma atividade selecionada', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo e unidade
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const importarButton = wrapper.find('.btn-outline-primary');
      expect(importarButton.attributes('disabled')).toBeDefined();
    });
  });

  describe('Eventos', () => {
    beforeEach(() => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([
        { id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado' } as Subprocesso,
      ]);

      mockAtividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue([
        { id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: [] } as Atividade,
        { id: 2, descricao: 'Atividade 2', idSubprocesso: 1, conhecimentos: [] } as Atividade,
      ]);
    });

    it('deve emitir evento fechar ao clicar no botão fechar', async () => {
      const wrapper = mountComponent();

      const closeButton = wrapper.find('.btn-close');
      await closeButton.trigger('click');

      expect(wrapper.emitted().fechar).toBeTruthy();
    });

    it('deve emitir evento fechar ao clicar no botão cancelar', async () => {
      const wrapper = mountComponent();

      const cancelButton = wrapper.find('.btn-outline-secondary');
      await cancelButton.trigger('click');

      expect(wrapper.emitted().fechar).toBeTruthy();
    });

    it('deve emitir evento importar com atividades selecionadas', async () => {
      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo e unidade
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Selecionar atividade
      const checkboxes = wrapper.findAll('.form-check-input[type="checkbox"]');
      await checkboxes[0].setValue(true);

      await wrapper.vm.$nextTick();

      // Clicar importar
      const importarButton = wrapper.find('.btn-outline-primary');
      await importarButton.trigger('click');

      expect(wrapper.emitted().importar).toBeTruthy();
      expect(wrapper.emitted().importar[0]).toEqual([
        [{ id: 1, descricao: 'Atividade 1', idSubprocesso: 1, conhecimentos: [] }]
      ]);
    });

  });

  describe('Reset do Modal', () => {
    it('deve resetar o modal quando mostrar muda para true', async () => {
      const wrapper = mountComponent({ mostrar: false });

      // Simular que o modal foi mostrado antes
      await wrapper.setProps({ mostrar: true });

      await wrapper.vm.$nextTick();

      // Verificar se os elementos do modal estão presentes
      expect(wrapper.find('.modal').exists()).toBe(true);
    });
  });

  describe('Funcionalidade de Async', () => {
    it('deve chamar fetchAtividadesPorSubprocesso quando unidade selecionada', async () => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([
        { id: 1, unidade: 'UNID1', idProcesso: 1, situacao: 'Finalizado' } as Subprocesso,
      ]);

      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Selecionar unidade
      const unidadeSelect = wrapper.find('select#unidade-select');
      await unidadeSelect.setValue(1);

      await wrapper.vm.$nextTick();

      expect(mockAtividadesStore.fetchAtividadesPorSubprocesso).toHaveBeenCalledWith(1);
    });
  });

  describe('Edge Cases e Validações', () => {
    it('deve lidar com processo sem unidades', async () => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo sem Unidades',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([]);

      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Verificar que não há opções de unidade
      const options = wrapper.findAll('select#unidade-select option');
      expect(options).toHaveLength(1); // Apenas a opção disabled
    });

    it('deve lidar com processo sem unidades', async () => {
      mockProcessosStore.processos = [
        {
          id: 1,
          descricao: 'Processo sem Unidades',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.FINALIZADO,
          dataLimite: new Date(),
          dataFinalizacao: new Date(),
        },
      ];

      mockProcessosStore.getUnidadesDoProcesso = vi.fn().mockReturnValue([]);

      const wrapper = mountComponent();

      await wrapper.vm.$nextTick();

      // Selecionar processo
      const processoSelect = wrapper.find('select#processo-select');
      await processoSelect.setValue(1);

      await wrapper.vm.$nextTick();

      // Verificar que não há opções de unidade
      const options = wrapper.findAll('select#unidade-select option');
      expect(options).toHaveLength(1); // Apenas a opção disabled
    });
  });
});