import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import BarraNavegacao from '@/components/BarraNavegacao.vue';
import {useRoute, useRouter} from 'vue-router';

import {createTestingPinia} from '@pinia/testing';
import {Perfil} from '@/types/tipos';

// Mocks
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>();
  return {
    ...actual,
    useRoute: vi.fn(),
    useRouter: vi.fn(),
  };
});

const BButtonStub = {
  template: '<button @click="$emit(\'click\')"><slot></slot></button>',
};

const BBreadcrumbStub = {
  template: '<nav><slot></slot></nav>',
};

const BBreadcrumbItemStub = {
  template: '<li><slot></slot></li>',
  props: ['active', 'to'],
};

describe('BarraNavegacao.vue', () => {
  let mockRoute: any;
  let mockRouter: any;

  beforeEach(() => {
    mockRoute = {
      path: '/some/path',
      name: 'SomeRoute',
      params: {},
      matched: [],
    };
    mockRouter = {
      back: vi.fn(),
    };
    (useRoute as any).mockReturnValue(mockRoute);
    (useRouter as any).mockReturnValue(mockRouter);
  });

  const mountComponent = (piniaState = {}) => {
    return mount(BarraNavegacao, {
      global: {
        plugins: [createTestingPinia({
          initialState: piniaState,
          createSpy: vi.fn,
        })],
        stubs: {
          BButton: BButtonStub,
          BBreadcrumb: BBreadcrumbStub,
          BBreadcrumbItem: BBreadcrumbItemStub,
        },
        directives: {
            'b-tooltip': {},
        }
      },
    });
  };

  it('deve renderizar botão voltar e breadcrumbs quando não for login ou painel', () => {
    mockRoute.path = '/outra-rota';
    const wrapper = mountComponent();
    expect(wrapper.find('.btn-voltar').exists()).toBe(true);
    expect(wrapper.find('.breadcrumb-compacto').exists()).toBe(true);
  });

  it('não deve renderizar botão voltar e breadcrumbs na rota /login', () => {
    mockRoute.path = '/login';
    const wrapper = mountComponent();
    expect(wrapper.find('.btn-voltar').exists()).toBe(false);
    expect(wrapper.find('.breadcrumb-compacto').exists()).toBe(false);
  });

  it('não deve renderizar botão voltar e breadcrumbs na rota /painel', () => {
    mockRoute.path = '/painel';
    const wrapper = mountComponent();
    expect(wrapper.find('.btn-voltar').exists()).toBe(false);
    expect(wrapper.find('.breadcrumb-compacto').exists()).toBe(false);
  });

  it('deve chamar router.back() ao clicar no botão voltar', async () => {
    const wrapper = mountComponent();
    await wrapper.find('.btn-voltar').trigger('click');
    expect(mockRouter.back).toHaveBeenCalled();
  });

  describe('Breadcrumbs Logic', () => {
    it('deve sempre mostrar Home/Painel como primeiro item', () => {
       const wrapper = mountComponent();
       const items = wrapper.findAllComponents(BBreadcrumbItemStub);
       expect(items[0].find('i.bi-house-door').exists()).toBe(true);
    });

    it('deve mostrar "Detalhes do processo" se for rota de processo e perfil permitir', () => {
      mockRoute.name = 'Processo';
      mockRoute.params = { codProcesso: '123' };
      const wrapper = mountComponent({
          perfil: { perfilSelecionado: Perfil.GESTOR }
      });
      const items = wrapper.findAllComponents(BBreadcrumbItemStub);
      expect(items.length).toBe(2);
      expect(items[1].text()).toBe('Detalhes do processo');
    });

    it('NÃO deve mostrar "Detalhes do processo" se perfil for CHEFE ou SERVIDOR', () => {
        mockRoute.name = 'Subprocesso';
        mockRoute.params = { codProcesso: '123', siglaUnidade: 'UNIDADE' };
        const wrapper = mountComponent({
            perfil: { perfilSelecionado: Perfil.CHEFE }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        // Home -> UNIDADE (Subprocesso skipped)
        expect(items.length).toBe(2);
        expect(items[1].text()).toBe('UNIDADE');
    });

    it('deve mostrar breadcrumbs hierárquicos para SubprocessoMapa', () => {
        mockRoute.name = 'SubprocessoMapa';
        mockRoute.params = { codProcesso: '123', siglaUnidade: 'UNIDADE' };
        const wrapper = mountComponent({
            perfil: { perfilSelecionado: Perfil.GESTOR }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        // Home -> Detalhes do processo -> UNIDADE -> Mapa de competências
        expect(items.length).toBe(4);
        expect(items[1].text()).toBe('Detalhes do processo');
        expect(items[2].text()).toBe('UNIDADE');
        expect(items[3].text()).toBe('Mapa de competências');
    });

    it('deve mostrar breadcrumbs para rotas de Unidade (não-ADMIN vê Minha unidade)', () => {
        mockRoute.name = 'Unidade';
        mockRoute.params = { codUnidade: '10' };
        const wrapper = mountComponent({
             perfil: { perfilSelecionado: Perfil.GESTOR },
             unidades: { unidade: { codigo: 10, sigla: 'MINHA_UNIDADE' } }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        // Home -> MINHA_UNIDADE -> Minha unidade (Título da pagina)
        expect(items.length).toBe(3);
        expect(items[1].text()).toBe('MINHA_UNIDADE');
        expect(items[2].text()).toBe('Minha unidade');
    });

    it('deve mostrar breadcrumbs para rotas de Unidade (ADMIN vê Unidades)', () => {
        mockRoute.name = 'Unidade';
        mockRoute.params = { codUnidade: '1' };
        const wrapper = mountComponent({
             perfil: { perfilSelecionado: Perfil.ADMIN },
             unidades: { unidade: { codigo: 1, sigla: 'SEDOC' } }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        // Home -> SEDOC -> Unidades
        expect(items.length).toBe(3);
        expect(items[1].text()).toBe('SEDOC');
        expect(items[2].text()).toBe('Unidades');
    });

    it('deve usar ID da unidade se sigla não estiver na store', () => {
        mockRoute.name = 'Unidade';
        mockRoute.params = { codUnidade: '99' };
        const wrapper = mountComponent({
             perfil: { perfilSelecionado: Perfil.GESTOR },
             unidades: { unidade: null }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        // Home -> Unidade 99 -> Minha unidade
        expect(items[1].text()).toBe('Unidade 99');
    });

    it('deve usar meta.breadcrumb para rotas genéricas', () => {
        mockRoute.name = 'OutraRota';
        mockRoute.matched = [
            { name: 'OutraRota', meta: { breadcrumb: 'Minha Rota' } }
        ];
        const wrapper = mountComponent();
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        expect(items.length).toBe(2);
        expect(items[1].text()).toBe('Minha Rota');
    });

    it('deve resolver meta.breadcrumb function', () => {
        mockRoute.name = 'RotaFunc';
        mockRoute.matched = [
            { name: 'RotaFunc', meta: { breadcrumb: () => 'Rota Dinâmica' } }
        ];
        const wrapper = mountComponent();
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        expect(items[1].text()).toBe('Rota Dinâmica');
    });

     it('deve adicionar breadcrumb final para SubprocessoVisMapa', () => {
        mockRoute.name = 'SubprocessoVisMapa';
        mockRoute.params = { codProcesso: '123', siglaUnidade: 'UNIDADE' };
        const wrapper = mountComponent({
             perfil: { perfilSelecionado: Perfil.GESTOR }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        expect(items[3].text()).toBe('Visualizar mapa');
    });

     it('deve adicionar breadcrumb final para SubprocessoCadastro', () => {
        mockRoute.name = 'SubprocessoCadastro';
        mockRoute.params = { codProcesso: '123', siglaUnidade: 'UNIDADE' };
        const wrapper = mountComponent({
             perfil: { perfilSelecionado: Perfil.GESTOR }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        expect(items[3].text()).toBe('Atividades e conhecimentos');
    });

    it('deve adicionar breadcrumb final para Mapa (Rota de Unidade)', () => {
        mockRoute.name = 'Mapa';
        mockRoute.params = { codUnidade: '10' };
        const wrapper = mountComponent({
             unidades: { unidade: { codigo: 10, sigla: 'TESTE' } }
        });
        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        // Home -> TESTE -> Mapa de competências
        expect(items[2].text()).toBe('Mapa de competências');
    });
  });
});
