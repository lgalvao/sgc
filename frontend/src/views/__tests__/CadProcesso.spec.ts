import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import CadProcesso from '@/views/CadProcesso.vue';
import { useProcessosStore } from '@/stores/processos';
import { useUnidadesStore } from '@/stores/unidades';
import { useNotificacoesStore } from '@/stores/notificacoes';
import * as mapaService from '@/services/mapaService';
import { buscarUsuariosPorUnidade } from '@/services/usuarioService';
import { h } from 'vue';

// Helper to flush pending promises
const flushPromises = () => new Promise(setImmediate);

// Mock the services
vi.mock('@/services/mapaService');
vi.mock('@/services/usuarioService');

// Mock axios-setup to prevent router issues
vi.mock('@/axios-setup', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}));

const mockRouter = {
  push: vi.fn(),
};

vi.mock('vue-router', () => ({
    useRouter: () => mockRouter,
    useRoute: () => ({ query: {} }),
    createRouter: () => mockRouter,
    createWebHistory: () => {},
    createMemoryHistory: () => {},
    RouterLink: {
      props: ['to'],
      setup(props, { slots }) {
        return () => h('a', { href: props.to }, slots.default ? slots.default() : '');
      }
    }
  }));

// Mock global fetch
global.fetch = vi.fn(() =>
  Promise.resolve({
    ok: true,
    json: () => Promise.resolve([]),
  })
);

describe('CadProcesso.vue', () => {
  let wrapper;

  beforeEach(() => {
    vi.clearAllMocks();

    vi.mocked(mapaService).verificarMapaVigente.mockResolvedValue(true);
    vi.mocked(buscarUsuariosPorUnidade).mockResolvedValue([{ nome: 'Test User' }]);

    wrapper = mount(CadProcesso, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            unidades: {
              unidades: [
                { codigo: 1, nome: 'Unidade A', tipo: 'OPERACIONAL', filhas: [] },
                { codigo: 2, nome: 'Unidade B', tipo: 'OPERACIONAL', filhas: [] },
                { codigo: 3, nome: 'Unidade C', tipo: 'INTERMEDIARIA', filhas: [] },
              ]
            }
          }
        })],
      },
    });

    const unidadesStore = useUnidadesStore();
    unidadesStore.fetchUnidades = vi.fn().mockResolvedValue(true);
  });

  it('renders the form correctly', () => {
    expect(wrapper.find('h2').text()).toBe('Cadastro de processo');
    expect(wrapper.find('[data-testid="input-descricao"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="select-tipo"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="input-dataLimite"]').exists()).toBe(true);
  });

  it('calls criarProcesso when saving a new process', async () => {
    const processosStore = useProcessosStore();
    processosStore.criarProcesso = vi.fn().mockResolvedValue({ codigo: 123 });
    const notificacoesStore = useNotificacoesStore();

    await wrapper.find('[data-testid="input-descricao"]').setValue('Novo Processo de Teste');
    await wrapper.find('[data-testid="input-dataLimite"]').setValue('2025-12-31');

    await wrapper.vm.unidadesSelecionadas.push(1);

    await wrapper.find('button[type="button"].btn-primary').trigger('click');

    expect(processosStore.criarProcesso).toHaveBeenCalled();
    expect(notificacoesStore.sucesso).toHaveBeenCalledWith('Processo salvo', 'O processo foi salvo!');
    expect(mockRouter.push).toHaveBeenCalledWith('/processo/123');
  });

  it('shows an error if required fields are missing on save', async () => {
    const notificacoesStore = useNotificacoesStore();
    await wrapper.find('button[type="button"].btn-primary').trigger('click');
    expect(notificacoesStore.erro).toHaveBeenCalledWith('Dados incompletos', 'Preencha todos os campos e selecione ao menos uma unidade.');
  });

  describe('unidadesStatus', () => {
    it('disables units that are blocked', async () => {
      wrapper.vm.unidadesBloqueadas = [2];
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.unidadesStatus.desabilitadas).toContain(2);
      expect(wrapper.vm.unidadeElegivel({ codigo: 2 })).toBe(false);
    });

    it('disables units without a map for REVISAO type', async () => {
      vi.mocked(mapaService).verificarMapaVigente.mockImplementation(async (codigo) => codigo === 1);

      await wrapper.find('[data-testid="select-tipo"]').setValue('REVISAO');
      await flushPromises();
      await wrapper.vm.$nextTick();

      expect(wrapper.vm.unidadesStatus.desabilitadas).toContain(2);
      expect(wrapper.vm.unidadesStatus.desabilitadas).toContain(3);
      expect(wrapper.vm.unidadeElegivel({ codigo: 1, nome: 'Unit 1', tipo: 'OPERACIONAL', filhas: [] })).toBe(true);
      expect(wrapper.vm.unidadeElegivel({ codigo: 2, nome: 'Unit 2', tipo: 'OPERACIONAL', filhas: [] })).toBe(false);
    });

    it('disables units without servers for DIAGNOSTICO type', async () => {
      vi.mocked(mapaService).verificarMapaVigente.mockResolvedValue(true);
      vi.mocked(buscarUsuariosPorUnidade).mockImplementation(async (codigo) => codigo === 1 ? [{ nome: 'Test' }] : []);

      await wrapper.find('[data-testid="select-tipo"]').setValue('DIAGNOSTICO');
      await flushPromises();
      await wrapper.vm.$nextTick();

      expect(wrapper.vm.unidadesStatus.desabilitadas).toContain(2);
      expect(wrapper.vm.unidadeElegivel({ codigo: 1, nome: 'Unit 1', tipo: 'OPERACIONAL', filhas: [] })).toBe(true);
      expect(wrapper.vm.unidadeElegivel({ codigo: 2, nome: 'Unit 2', tipo: 'OPERACIONAL', filhas: [] })).toBe(false);
    });
  });
});