import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import HistoricoAnaliseModal from '../HistoricoAnaliseModal.vue';
import { createPinia, setActivePinia } from 'pinia';

const BModalStub = {
  template: `
    <div v-if="modelValue">
      <h5 class="modal-title">{{ title }}</h5>
      <slot />
      <slot name="footer" />
    </div>
  `,
  props: ['modelValue', 'title'],
};

const mockAnalises = [
  {
    dataHora: '2024-01-01T12:00:00Z',
    unidadeSigla: 'TEST',
    resultado: 'APROVADO',
    observacoes: 'Tudo certo.',
  },
  {
    dataHora: '2024-01-02T14:30:00Z',
    unidadeSigla: 'TEST2',
    resultado: 'REPROVADO',
    observacoes: 'Faltou informação.',
  },
];

vi.mock('@/stores/analises', () => ({
  useAnalisesStore: vi.fn(() => ({
    getAnalisesPorSubprocesso: (codSubprocesso: number) => {
      return codSubprocesso === 1 ? mockAnalises : [];
    },
  })),
}));

describe('HistoricoAnaliseModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  const globalConfig = {
    global: {
      stubs: {
        'b-modal': BModalStub,
      },
    },
  };

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(HistoricoAnaliseModal, {
      props: {
        mostrar: false,
        codSubrocesso: 1,
      },
      ...globalConfig,
    });
    expect(wrapper.find('[data-testid="modal-historico-body"]').exists()).toBe(false);
  });

  it('deve renderizar a mensagem de "nenhuma análise" quando não houver análises', () => {
    const wrapper = mount(HistoricoAnaliseModal, {
      props: {
        mostrar: true,
        codSubrocesso: 2,
      },
      ...globalConfig,
    });

    expect(wrapper.find('.alert-info').text()).toContain('Nenhuma análise registrada');
  });

  it('deve renderizar a tabela com as análises', () => {
    const wrapper = mount(HistoricoAnaliseModal, {
      props: {
        mostrar: true,
        codSubrocesso: 1,
      },
      ...globalConfig,
    });

    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(mockAnalises.length);
    const firstRowText = rows[0].text();
    expect(firstRowText).toContain('01/01/2024');
    expect(firstRowText).toContain('12:00:00');
    expect(firstRowText).toContain('TEST');
    expect(firstRowText).toContain('APROVADO');
    expect(firstRowText).toContain('Tudo certo.');
  });

  it('deve emitir o evento fechar ao clicar no botão de fechar', async () => {
    const wrapper = mount(HistoricoAnaliseModal, {
      props: {
        mostrar: true,
        codSubrocesso: 1,
      },
      ...globalConfig,
    });

    await wrapper.find('[data-testid="btn-modal-fechar"]').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });
});
