import {describe, expect, it, vi, beforeEach} from 'vitest';
import {useCadAtividades} from '../useCadAtividades';
import {createTestingPinia} from '@pinia/testing';
import {setActivePinia} from 'pinia';
import {defineComponent, h} from 'vue';
import {mount, flushPromises} from '@vue/test-utils';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useAnalisesStore} from '@/stores/analises';
import {useFeedbackStore} from '@/stores/feedback';
import {SituacaoSubprocesso} from '@/types/tipos';

function withSetup(composable: (props: any) => any, props: any = {}) {
  let result: any;
  const setupComponent = defineComponent({
    setup() {
      result = composable(props);
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
        currentRoute: { value: { params: { codProcesso: '1', siglaUnidade: 'ABC' }, query: {} } }
    })),
    useRoute: vi.fn(() => ({
        params: { codProcesso: '1', siglaUnidade: 'ABC' },
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

vi.mock('@/composables/useAtividadeForm', () => ({
  useAtividadeForm: vi.fn(() => ({
    novaAtividade: { value: '' },
    loadingAdicionar: { value: false },
    adicionarAtividade: vi.fn().mockResolvedValue(true)
  }))
}));

describe('useCadAtividades', () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia({ stubActions: true }));
  });

  it('disponibilizarCadastro impede ação se situação inválida', async () => {
      const subStore = useSubprocessosStore();
      const feedbackStore = useFeedbackStore();
      // @ts-expect-error - mocking store
      subStore.subprocessoDetalhe = { situacao: 'OUTRA' };

      const cad = withSetup((p) => useCadAtividades(p), { codProcesso: '1', sigla: 'ABC' });
      await cad.disponibilizarCadastro();

      expect(feedbackStore.show).toHaveBeenCalledWith("Ação não permitida", expect.any(String), "danger");
  });

  it('disponibilizarCadastro trata erros de validação', async () => {
    const subStore = useSubprocessosStore();
    // @ts-expect-error - mocking store
    subStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);
    // @ts-expect-error - mocking store
    subStore.subprocessoDetalhe = { situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO };
    // @ts-expect-error - mocking store
    subStore.validarCadastro.mockResolvedValue({
        valido: false,
        erros: [{ atividadeCodigo: 1, mensagem: 'Erro 1' }, { mensagem: 'Erro Global' }]
    });

    const cad = withSetup((p) => useCadAtividades(p), { codProcesso: '1', sigla: 'ABC' });
    await flushPromises();

    await cad.disponibilizarCadastro();
    expect(cad.errosValidacao.value).toHaveLength(2);
    expect(cad.erroGlobal.value).toBe('Erro Global');
    expect(cad.obterErroParaAtividade(1)).toBe('Erro 1');
  });

  it('confirmarDisponibilizacao funciona para mapeamento e revisao', async () => {
      const subStore = useSubprocessosStore();
      // @ts-expect-error - mocking store
      subStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);

      const cad = withSetup((p) => useCadAtividades(p), { codProcesso: '1', sigla: 'ABC' });
      await flushPromises();

      // Mapeamento
      await cad.confirmarDisponibilizacao();
      expect(subStore.disponibilizarCadastro).toHaveBeenCalled();

      // Revisao
      // @ts-expect-error - mocking store
      subStore.subprocessoDetalhe = { tipoProcesso: 'REVISAO' };
      await cad.confirmarDisponibilizacao();
  });

  it('abre modais corretamente e busca dados', async () => {
      const subStore = useSubprocessosStore();
      // @ts-expect-error - mocking store
      subStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);
      const mapasStore = useMapasStore();
      const analisesStore = useAnalisesStore();

      // @ts-expect-error - mocking store
      mapasStore.buscarImpactoMapa.mockResolvedValue({});

      const cad = withSetup((p) => useCadAtividades(p), { codProcesso: '1', sigla: 'ABC' });
      await flushPromises();

      cad.abrirModalImpacto();
      expect(cad.mostrarModalImpacto.value).toBe(true);
      expect(mapasStore.buscarImpactoMapa).toHaveBeenCalledWith(123);

      await cad.abrirModalHistorico();
      expect(cad.mostrarModalHistorico.value).toBe(true);
      expect(analisesStore.buscarAnalisesCadastro).toHaveBeenCalledWith(123);
  });
});
