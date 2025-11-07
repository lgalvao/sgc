import {mount} from '@vue/test-utils'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import CadAtividades from '@/views/CadAtividades.vue'
import {createTestingPinia} from '@pinia/testing'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useSubprocessosStore} from '@/stores/subprocessos'
import {useAnalisesStore} from '@/stores/analises'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
  createRouter: () => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
  }),
  createWebHistory: () => ({}),
}));

const mockAtividades = [
  {
    codigo: 1,
    descricao: 'Atividade 1',
    conhecimentos: [
      {id: 101, descricao: 'Conhecimento 1.1'},
      {id: 102, descricao: 'Conhecimento 1.2'},
    ],
  },
  {
    codigo: 2,
    descricao: 'Atividade 2',
    conhecimentos: [],
  },
];

describe('CadAtividades.vue', () => {
  let wrapper;
  let atividadesStore;
  let unidadesStore;
  let processosStore;
  let subprocessosStore;
  let analisesStore;
  let notificacoesStore;

  function createWrapper(isRevisao = false) {
    return mount(CadAtividades, {
      props: {
        codProcesso: 1,
        sigla: 'TESTE',
      },
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              perfil: {
                perfilSelecionado: 'CHEFE',
              },
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  tipo: isRevisao ? TipoProcesso.REVISAO : TipoProcesso.MAPEAMENTO,
                  unidades: [
                    {
                      codUnidade: 123,
                      sigla: 'TESTE',
                      situacaoSubprocesso: isRevisao ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO : SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO,
                    },
                  ],
                },
              },
              unidades: {
                unidade: {codigo: 1, nome: 'Unidade de Teste', sigla: 'TESTE'}
              }
            },
            stubActions: false,
          }),
        ],
      },
    });
  }

  beforeEach(() => {
    wrapper = createWrapper();
    atividadesStore = useAtividadesStore();
    unidadesStore = useUnidadesStore();
    processosStore = useProcessosStore();
    subprocessosStore = useSubprocessosStore();
    analisesStore = useAnalisesStore();
    notificacoesStore = useNotificacoesStore();

    atividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);
    window.confirm = vi.fn(() => true);
  });

  it('deve adicionar uma atividade', async () => {
    const input = wrapper.find('[data-testid="input-nova-atividade"]');
    await input.setValue('Nova Atividade');
    await wrapper.find('[data-testid="form-nova-atividade"]').trigger('submit.prevent');

    expect(atividadesStore.adicionarAtividade).toHaveBeenCalledWith(
      123,
      {descricao: 'Nova Atividade'}
    );
  });

  it('deve remover uma atividade', async () => {
    await wrapper.find('[data-testid="btn-remover-atividade"]').trigger('click');
    expect(window.confirm).toHaveBeenCalled();
    expect(atividadesStore.removerAtividade).toHaveBeenCalledWith(123, 1);
  });

  it('deve adicionar um conhecimento', async () => {
    const form = wrapper.find('[data-testid="form-novo-conhecimento"]');
    const input = form.find('[data-testid="input-novo-conhecimento"]');
    await input.setValue('Novo Conhecimento');
    await form.trigger('submit.prevent');

    expect(atividadesStore.adicionarConhecimento).toHaveBeenCalledWith(
      123,
      1,
      {descricao: 'Novo Conhecimento'}
    );
  });

  it('deve remover um conhecimento', async () => {
    await wrapper.find('[data-testid="btn-remover-conhecimento"]').trigger('click');
    expect(window.confirm).toHaveBeenCalled();
    expect(atividadesStore.removerConhecimento).toHaveBeenCalledWith(123, 1, 101);
  });

  it('deve disponibilizar o cadastro', async () => {
    await wrapper.find('[data-testid="btn-disponibilizar"]').trigger('click');
    await wrapper.find('.modal-footer .btn-success').trigger('click');
    expect(subprocessosStore.disponibilizarCadastro).toHaveBeenCalledWith(123);
  });
});
