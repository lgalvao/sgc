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
import { BModal } from 'bootstrap-vue-next' // Imported from mocked module

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

// Mock bootstrap-vue-next components
vi.mock('bootstrap-vue-next', async () => {
  return {
    BModal: {
      name: 'BModal',
      props: ['modelValue', 'title'],
      template: '<div v-if="modelValue" class="modal-stub" :aria-label="title"><slot /><slot name="footer" /></div>',
      emits: ['update:modelValue', 'ok', 'hidden']
    },
    BButton: { name: 'BButton', template: '<button type="button"><slot /></button>' },
    BContainer: { name: 'BContainer', template: '<div><slot /></div>' },
    BCard: { name: 'BCard', template: '<div><slot /></div>' },
    BCardBody: { name: 'BCardBody', template: '<div><slot /></div>' },
    BFormInput: {
      name: 'BFormInput',
      props: ['modelValue'],
      template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
      emits: ['update:modelValue']
    },
    BFormTextarea: {
      name: 'BFormTextarea',
      props: ['modelValue'],
      template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
      emits: ['update:modelValue']
    },
    BFormCheckbox: {
      name: 'BFormCheckbox',
      props: ['modelValue', 'value'],
      template: '<input type="checkbox" :checked="Array.isArray(modelValue) ? modelValue.includes(value) : modelValue" @change="handleChange" />',
      emits: ['update:modelValue'],
      methods: {
        handleChange(e: any) {
            let newValue = this.modelValue;
            if (Array.isArray(this.modelValue)) {
                if (e.target.checked) {
                    newValue = [...this.modelValue, this.value];
                } else {
                    newValue = this.modelValue.filter((v: any) => v !== this.value);
                }
            } else {
                newValue = e.target.checked;
            }
            this.$emit("update:modelValue", newValue);
        }
      }
    },
    BAlert: { name: 'BAlert', template: '<div><slot /></div>' }
  }
});

describe('CadMapa.vue', () => {
  let wrapper: any;

  const mockAtividades = [
    { codigo: 101, descricao: 'Atividade 1', conhecimentos: [] },
    { codigo: 102, descricao: 'Atividade 2', conhecimentos: [{descricao: 'Java'}] }
  ];

  const mockCompetencias = [
    { codigo: 10, descricao: 'Competencia A', atividadesAssociadas: [101] }
  ];

  function createWrapper(customState = {}) {
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
                   competencias: [...mockCompetencias],
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
                  subprocessoDetalhe: {
                      permissoes: { podeVisualizarImpacto: true }
                  }
              },
              ...customState
            },
            stubActions: true,
          }),
        ],
        stubs: {
            ImpactoMapaModal: {
                name: 'ImpactoMapaModal',
                template: '<div class="impacto-modal-stub"></div>',
                props: ['mostrar']
            },
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
    atividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);

    await flushPromises();

    expect(subprocessosStore.fetchSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TESTE');
    expect(mapasStore.fetchMapaCompleto).toHaveBeenCalledWith(123);
    expect(atividadesStore.fetchAtividadesParaSubprocesso).toHaveBeenCalledWith(123);

    expect(wrapper.text()).toContain('Unidade Teste');
    expect(wrapper.text()).toContain('Competencia A');
  });

  it('deve abrir modal e criar nova competencia', async () => {
    const { wrapper: w, subprocessosStore, atividadesStore, mapasStore } = createWrapper();
    wrapper = w;
    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
    atividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);
    await flushPromises();

    await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger('click');
    expect(wrapper.find('[data-testid="criar-competencia-modal"]').exists()).toBe(true);

    const textarea = wrapper.find('[data-testid="input-nova-competencia"]');
    await textarea.setValue('Nova Competencia Teste');

    const checkbox = wrapper.find(`#atv-101`);
    await checkbox.setValue(true);

    await wrapper.find('[data-testid="btn-criar-competencia"]').trigger('click');

    expect(mapasStore.adicionarCompetencia).toHaveBeenCalledWith(123, expect.objectContaining({
        descricao: 'Nova Competencia Teste',
        atividadesAssociadas: [101]
    }));
  });

  it('deve editar uma competencia existente', async () => {
    const { wrapper: w, subprocessosStore, atividadesStore, mapasStore } = createWrapper();
    wrapper = w;
    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
    atividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);
    await flushPromises();

    await wrapper.find('[data-testid="btn-editar-competencia"]').trigger('click');
    expect(wrapper.find('[data-testid="criar-competencia-modal"]').exists()).toBe(true);

    const textarea = wrapper.find('[data-testid="input-nova-competencia"]');
    expect(textarea.element.value).toBe('Competencia A');

    await textarea.setValue('Competencia A Editada');

    await wrapper.find('[data-testid="btn-criar-competencia"]').trigger('click');

    expect(mapasStore.atualizarCompetencia).toHaveBeenCalledWith(123, expect.objectContaining({
        codigo: 10,
        descricao: 'Competencia A Editada'
    }));
  });

  it('deve excluir uma competencia', async () => {
    const { wrapper: w, subprocessosStore, atividadesStore, mapasStore } = createWrapper();
    wrapper = w;
    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
    atividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);
    await flushPromises();

    await wrapper.find('[data-testid="btn-excluir-competencia"]').trigger('click');

    await wrapper.vm.$nextTick();

    // Use the imported mock component class/object
    const modals = wrapper.findAllComponents(BModal);
    expect(modals.length).toBeGreaterThan(0);

    const deleteModal = modals.find((c: any) => c.props('modelValue') === true);

    expect(deleteModal).toBeDefined();

    if(deleteModal) {
        await deleteModal.vm.$emit('ok');
    }

    expect(mapasStore.removerCompetencia).toHaveBeenCalledWith(123, 10);
  });

  it('deve remover atividade associada', async () => {
    const { wrapper: w, subprocessosStore, atividadesStore, mapasStore } = createWrapper();
    wrapper = w;
    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
    atividadesStore.getAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);
    await flushPromises();

    const removeBtn = wrapper.find('.botao-acao-inline');
    await removeBtn.trigger('click');

    expect(mapasStore.atualizarCompetencia).toHaveBeenCalledWith(123, expect.objectContaining({
        codigo: 10,
        atividadesAssociadas: []
    }));
  });

  it('deve abrir modal de disponibilizar e enviar', async () => {
    const { wrapper: w, subprocessosStore, atividadesStore, mapasStore } = createWrapper();
    wrapper = w;
    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
    await flushPromises();

    await wrapper.find('[data-testid="btn-disponibilizar-page"]').trigger('click');

    const modal = wrapper.find('[data-testid="disponibilizar-modal"]');
    expect(modal.exists()).toBe(true);

    await wrapper.find('[data-testid="input-data-limite"]').setValue('2023-12-31');
    await wrapper.find('[data-testid="input-observacoes-disponibilizacao"]').setValue('Obs');

    await wrapper.find('[data-testid="btn-disponibilizar"]').trigger('click');

    expect(mapasStore.disponibilizarMapa).toHaveBeenCalledWith(123, {
        dataLimite: '2023-12-31',
        observacoes: 'Obs'
    });
  });

  it('deve abrir modal de impacto', async () => {
    const { wrapper: w, subprocessosStore, atividadesStore } = createWrapper();
    wrapper = w;
    subprocessosStore.fetchSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
    await flushPromises();

    await wrapper.find('[data-testid="impactos-mapa-button"]').trigger('click');

    const impactoModal = wrapper.findComponent({ name: 'ImpactoMapaModal' });
    expect(impactoModal.props('mostrar')).toBe(true);
  });
});
