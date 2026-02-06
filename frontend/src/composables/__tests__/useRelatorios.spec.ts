import {describe, expect, it, vi, beforeEach} from 'vitest';
import {useRelatorios} from '../useRelatorios';
import {createTestingPinia} from '@pinia/testing';
import {setActivePinia} from 'pinia';
import {defineComponent, h} from 'vue';
import {mount} from '@vue/test-utils';
import {TipoProcesso} from '@/types/tipos';
import {useProcessosStore} from '@/stores/processos';

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

vi.mock('vue-router', async () => {
  return {
    useRouter: vi.fn(() => ({
        push: vi.fn(),
        currentRoute: { value: { params: {}, query: {} } }
    })),
    useRoute: vi.fn(() => ({
        params: {},
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

describe('useRelatorios', () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia({ stubActions: true }));
  });

  it('filtra processos por tipo e data', async () => {
    const store = useProcessosStore();
    store.processosPainel = [
        { codigo: 1, tipo: TipoProcesso.MAPEAMENTO, situacao: 'EM_ANDAMENTO', dataCriacao: '2023-01-01T00:00:00' } as any,
        { codigo: 2, tipo: TipoProcesso.DIAGNOSTICO, situacao: 'EM_ANDAMENTO', dataCriacao: '2023-06-01T00:00:00' } as any
    ];

    const rel = withSetup(() => useRelatorios());

    rel.filtroTipo.value = TipoProcesso.MAPEAMENTO;
    expect(rel.processosFiltrados.value).toHaveLength(1);

    rel.filtroTipo.value = '';
    rel.filtroDataInicio.value = '2023-05-01';
    expect(rel.processosFiltrados.value).toHaveLength(1);
    expect(rel.processosFiltrados.value[0].codigo).toBe(2);

    rel.filtroDataInicio.value = '';
    rel.filtroDataFim.value = '2023-02-01';
    expect(rel.processosFiltrados.value).toHaveLength(1);
    expect(rel.processosFiltrados.value[0].codigo).toBe(1);
  });

  it('filtra diagnosticosGaps por tipo e data', () => {
    const rel = withSetup(() => useRelatorios());

    rel.filtroTipo.value = TipoProcesso.MAPEAMENTO;
    expect(rel.diagnosticosGapsFiltrados.value).toHaveLength(0);

    rel.filtroTipo.value = TipoProcesso.DIAGNOSTICO;
    expect(rel.diagnosticosGapsFiltrados.value.length).toBeGreaterThan(0);

    rel.filtroDataInicio.value = '2024-09-01';
    rel.filtroDataFim.value = '';
    expect(rel.diagnosticosGapsFiltrados.value.length).toBeGreaterThan(0);

    rel.filtroDataInicio.value = '';
    rel.filtroDataFim.value = '2024-08-31';
    expect(rel.diagnosticosGapsFiltrados.value).toHaveLength(2);
  });
});
