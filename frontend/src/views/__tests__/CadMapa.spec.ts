import {mount, flushPromises} from '@vue/test-utils'
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import CadMapa from '@/views/CadMapa.vue'
import {createTestingPinia} from '@pinia/testing'
import {useMapasStore} from '@/stores/mapas'
import {useAtividadesStore} from '@/stores/atividades'
import {useSubprocessosStore} from '@/stores/subprocessos'
import {useUnidadesStore} from '@/stores/unidades'
import * as usePerfilModule from '@/composables/usePerfil'
import {Perfil} from '@/types/tipos'

const { pushMock } = vi.hoisted(() => {
  return { pushMock: vi.fn() }
});

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: {
      codProcesso: '1',
      siglaUnidade: 'TESTE'
    }
  }),
  useRouter: () => ({
    push: pushMock,
    currentRoute: { value: { path: '/' } }
  }),
  createRouter: () => ({
      push: pushMock,
      afterEach: vi.fn(),
      beforeEach: vi.fn()
  }),
  createWebHistory: vi.fn(),
  createMemoryHistory: vi.fn(),
}));

vi.mock('@/composables/usePerfil', () => ({
    usePerfil: vi.fn()
}));

// Mock bootstrap-vue-next components minimally to avoid rendering issues
vi.mock('bootstrap-vue-next', async () => {
  return {
    BModal: { template: '<div><slot /></div>' },
    BButton: { template: '<button><slot /></button>' },
    BContainer: { template: '<div><slot /></div>' },
    BCard: { template: '<div><slot /></div>' },
    BCardBody: { template: '<div><slot /></div>' },
    BFormInput: { template: '<input />' },
    BFormTextarea: { template: '<textarea />' },
    BFormCheckbox: { template: '<input type="checkbox" />' },
    BAlert: { template: '<div><slot /></div>' }
  }
});

describe('CadMapa.vue', () => {
  let wrapper: any;

  function createWrapper() {
    vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
        perfilSelecionado: { value: Perfil.CHEFE },
        servidorLogado: { value: null },
        unidadeSelecionada: { value: null },
        getPerfisDoServidor: vi.fn()
    } as any);

    const wrapper = mount(CadMapa, {
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              mapas: {
                mapaCompleto: {
                   codigo: 1,
                   competencias: [],
                   subprocessoCodigo: 123
                }
              },
              atividades: {
                  atividadesPorSubprocesso: new Map()
              },
              unidades: {
                unidade: {codigo: 1, nome: 'Unidade Teste', sigla: 'TESTE'}
              },
              subprocessos: {
              }
            },
            stubActions: true,
          }),
        ],
        stubs: {
            ImpactoMapaModal: true,
        },
        directives: {
            'b-tooltip': {}
        }
      },
      attachTo: document.body,
    });

    const mapasStore = useMapasStore();
    const atividadesStore = useAtividadesStore();
    const subprocessosStore = useSubprocessosStore();
    const unidadesStore = useUnidadesStore();

    return { wrapper, mapasStore, atividadesStore, subprocessosStore, unidadesStore };
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it('deve carregar dados no mount', async () => {
    const { wrapper: w, subprocessosStore, mapasStore, atividadesStore } = createWrapper();
    wrapper = w;

    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);

    await flushPromises();

    expect(subprocessosStore.fetchSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TESTE');
    expect(mapasStore.fetchMapaCompleto).toHaveBeenCalledWith(123);
    expect(atividadesStore.fetchAtividadesParaSubprocesso).toHaveBeenCalledWith(123);
  });
});
