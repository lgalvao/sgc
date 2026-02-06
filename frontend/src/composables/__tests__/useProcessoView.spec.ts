import {describe, expect, it, vi, beforeEach} from 'vitest';
import {useProcessoView} from '../useProcessoView';
import {createTestingPinia} from '@pinia/testing';
import {setActivePinia} from 'pinia';
import {defineComponent, h} from 'vue';
import {mount} from '@vue/test-utils';
import {usePerfilStore} from '@/stores/perfil';

const mockPush = vi.fn();

vi.mock('vue-router', async () => {
  return {
    useRouter: vi.fn(() => ({
        push: mockPush,
        currentRoute: { value: { params: {}, query: {} } }
    })),
    useRoute: vi.fn(() => ({
        params: { codProcesso: '1' },
        query: {}
    })),
    createRouter: vi.fn(() => ({
      beforeEach: vi.fn(),
      afterEach: vi.fn(),
      resolve: vi.fn().mockReturnValue({ href: '#' }),
      push: vi.fn(),
    })),
    createMemoryHistory: vi.fn(() => ({})),
    createWebHistory: vi.fn(() => ({}))
  };
});

function withSetup(composable: () => any) {
  let result: any;
  const setupComponent = defineComponent({
    setup() {
      result = composable();
      return () => h('div');
    },
  });
  mount(setupComponent);
  return result;
}

describe('useProcessoView', () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia({
        stubActions: true,
    }));
    mockPush.mockClear();
  });

  it('abrirDetalhesUnidade verifica permissões', async () => {
      const perfilStore = usePerfilStore();
      // @ts-expect-error - mocking store
      perfilStore.isAdmin = true;

      const pv = withSetup(() => useProcessoView());

      await pv.abrirDetalhesUnidade({ clickable: true, codigo: 999, sigla: 'X' });
      expect(mockPush).toHaveBeenCalled();
  });
});
