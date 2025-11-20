import {mount, flushPromises} from '@vue/test-utils'
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import CadAtividades from '@/views/CadAtividades.vue'
import {createTestingPinia} from '@pinia/testing'
import {useAtividadesStore} from '@/stores/atividades'
import {useProcessosStore} from '@/stores/processos'
import {useSubprocessosStore} from '@/stores/subprocessos'
import {SituacaoSubprocesso, TipoProcesso, Perfil} from '@/types/tipos'
import { BFormInput, BButton, BModal } from 'bootstrap-vue-next';
import ImportarAtividadesModal from '@/components/ImportarAtividadesModal.vue';
import EditarConhecimentoModal from '@/components/EditarConhecimentoModal.vue';
import * as usePerfilModule from '@/composables/usePerfil';

const pushMock = vi.fn();

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
  createRouter: () => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
  }),
  createWebHistory: () => ({}),
}));

// Mock usePerfil
vi.mock('@/composables/usePerfil', () => ({
    usePerfil: vi.fn()
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
  let wrapper: any;

  function createWrapper(isRevisao = false) {
    // Setup usePerfil mock per test
    vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
        perfilSelecionado: { value: Perfil.CHEFE },
        servidorLogado: { value: null },
        unidadeSelecionada: { value: null },
        getPerfisDoServidor: vi.fn()
    } as any);

    const wrapper = mount(CadAtividades, {
      props: {
        codProcesso: 1,
        sigla: 'TESTE',
      },
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
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
              },
              atividades: {
                  atividadesPorSubprocesso: new Map()
              }
            }
          }),
        ],
        stubs: {
             ImportarAtividadesModal: true,
             EditarConhecimentoModal: true,
             BModal: {
                template: `
                   <div class="b-modal-stub">
                     <slot />
                     <slot name="footer" />
                   </div>
                `,
                props: ['modelValue']
             }
        },
      },
      attachTo: document.body,
    });

    const atividadesStore = useAtividadesStore();
    const processosStore = useProcessosStore();
    const subprocessosStore = useSubprocessosStore();

    return { wrapper, atividadesStore, processosStore, subprocessosStore };
  }

  beforeEach(async () => {
    vi.clearAllMocks();
    window.confirm = vi.fn(() => true);
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it('deve carregar atividades no mount', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    await flushPromises();

    expect(atividadesStore.fetchAtividadesParaSubprocesso).toHaveBeenCalledWith(123);
  });

  it('deve adicionar uma atividade', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    const inputWrapper = wrapper.findComponent(BFormInput);
    const nativeInput = inputWrapper.find('input');
    await nativeInput.setValue('Nova Atividade');
    await wrapper.find('[data-testid="form-nova-atividade"]').trigger('submit.prevent');

    expect(atividadesStore.adicionarAtividade).toHaveBeenCalledWith(
      123,
      {descricao: 'Nova Atividade'}
    );
  });

  it('deve remover uma atividade', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    await wrapper.find('[data-testid="btn-remover-atividade"]').trigger('click');
    expect(window.confirm).toHaveBeenCalled();
    expect(atividadesStore.removerAtividade).toHaveBeenCalledWith(123, 1);
  });

  it('deve adicionar um conhecimento', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    const form = wrapper.find('[data-testid="form-novo-conhecimento"]');
    const inputWrapper = form.findComponent(BFormInput);
    const nativeInput = inputWrapper.find('input');
    await nativeInput.setValue('Novo Conhecimento');
    await form.trigger('submit.prevent');

    expect(atividadesStore.adicionarConhecimento).toHaveBeenCalledWith(
      123,
      1,
      {descricao: 'Novo Conhecimento'}
    );
  });

  it('deve remover um conhecimento', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    await wrapper.find('[data-testid="btn-remover-conhecimento"]').trigger('click');
    expect(window.confirm).toHaveBeenCalled();
    expect(atividadesStore.removerConhecimento).toHaveBeenCalledWith(123, 1, 101);
  });

  it('deve disponibilizar o cadastro', async () => {
    const { wrapper: w, atividadesStore, subprocessosStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    await wrapper.find('[data-testid="btn-disponibilizar"]').trigger('click');

    const confirmBtn = wrapper.find('[data-testid="btn-confirmar-disponibilizacao"]');
    await confirmBtn.trigger('click');

    expect(subprocessosStore.disponibilizarCadastro).toHaveBeenCalledWith(123);
    expect(pushMock).toHaveBeenCalledWith('/painel');
  });

  it('deve abrir modal de importar atividades', async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper.find('[title="Importar"]').trigger('click'); // Use title selector if data-testid missing

    const modal = wrapper.findComponent(ImportarAtividadesModal);
    expect(modal.props('mostrar')).toBe(true);
  });

  it('deve permitir edição inline de atividade', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    await wrapper.find('[data-testid="btn-editar-atividade"]').trigger('click');

    // Check if input appears
    expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(true);

    await wrapper.find('[data-testid="input-editar-atividade"]').setValue('Atividade Editada');
    await wrapper.find('[data-testid="btn-salvar-edicao-atividade"]').trigger('click');

    expect(atividadesStore.atualizarAtividade).toHaveBeenCalled();
  });

  it('deve abrir modal de editar conhecimento', async () => {
    const { wrapper: w, atividadesStore } = createWrapper();
    wrapper = w;
    atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
    await flushPromises();

    await wrapper.find('[data-testid="btn-editar-conhecimento"]').trigger('click');

    const modal = wrapper.findComponent(EditarConhecimentoModal);
    expect(modal.props('mostrar')).toBe(true);
    expect(modal.props('conhecimento')).toBeTruthy();
  });

  it('deve tratar disponibilizacao de revisao', async () => {
      const { wrapper: w, atividadesStore, subprocessosStore } = createWrapper(true); // isRevisao = true
      wrapper = w;
      atividadesStore.atividadesPorSubprocesso.set(123, [...mockAtividades]);
      await flushPromises();

      await wrapper.find('[data-testid="btn-disponibilizar"]').trigger('click');

      const confirmBtn = wrapper.find('[data-testid="btn-confirmar-disponibilizacao"]');
      await confirmBtn.trigger('click');

      expect(subprocessosStore.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(123);
  });
});
