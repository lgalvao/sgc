import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import CadastroVisualizacaoView from '../CadastroVisualizacaoView.vue';
import { useRouter } from 'vue-router';
import { useAcesso } from '@/composables/useAcesso';
import { useProcessosStore } from '@/stores/processos';
import { useSubprocessosStore } from '@/stores/subprocessos';
import { useAtividadesStore } from '@/stores/atividades';
import { useMapasStore } from '@/stores/mapas';
import { TipoProcesso } from '@/types/tipos';

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}));

vi.mock('@/composables/useAcesso', () => ({
  useAcesso: vi.fn()
}));

const LayoutPadraoStub = { template: '<div><slot /></div>' };
const PageHeaderStub = { template: '<div data-testid="page-header"><slot name="default"/><slot name="actions"/></div>' };
const HistoricoAnaliseModalStub = { template: '<div data-testid="historico-modal" v-if="mostrar"></div>', props: ['mostrar'] };
const ImpactoMapaModalStub = { template: '<div data-testid="impacto-modal" v-if="mostrar"></div>', props: ['mostrar'] };
const ModalConfirmacaoStub = { 
  template: `
    <div data-testid="modal-confirmacao" v-if="modelValue">
      <slot/>
      <button @click="$emit('confirmar')">Confirmar</button>
    </div>
  `, 
  props: ['modelValue'], 
  emits: ['update:modelValue', 'confirmar'] 
};
const BCardStub = { template: '<div class="card"><slot/></div>' };
const BCardBodyStub = { template: '<div class="card-body"><slot/></div>' };
const BFormGroupStub = { template: '<div><slot name="label"/><slot/></div>' };
const BFormTextareaStub = { 
  template: '<textarea @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>', 
  props: ['modelValue'] 
};
const BFormInvalidFeedbackStub = { template: '<div><slot/></div>' };
const BButtonStub = { template: '<button @click="$emit(\'click\')"><slot/></button>' };

describe('CadastroVisualizacaoView.vue', () => {
  let wrapper: any;
  let mockRouter: any;
  
  beforeEach(() => {
    vi.clearAllMocks();
    mockRouter = { push: vi.fn() };
    (useRouter as any).mockReturnValue(mockRouter);
    
    (useAcesso as any).mockReturnValue({
      podeHomologarCadastro: { value: false },
      podeAceitarCadastro: { value: true },
      podeDevolverCadastro: { value: true },
      podeVisualizarImpacto: { value: true }
    });
  });

  const createWrapper = (state = {}) => {
    return mount(CadastroVisualizacaoView, {
      props: {
        codProcesso: 1,
        sigla: 'TESTE'
      },
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            stubActions: true,
            initialState: {
              unidades: {
                unidades: [{ codigo: 1, sigla: 'TESTE', nome: 'Unidade Teste' }]
              },
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  tipo: TipoProcesso.MAPEAMENTO,
                  unidades: [{ sigla: 'TESTE', codSubprocesso: 10 }]
                }
              },
              atividades: {
                atividadesCache: {
                  '10': [
                    { codigo: 100, descricao: 'Atividade 1', conhecimentos: [{ codigo: 1000, descricao: 'Conhecimento 1' }] }
                  ]
                }
              },
              ...state
            }
          })
        ],
        stubs: {
          LayoutPadrao: LayoutPadraoStub,
          PageHeader: PageHeaderStub,
          HistoricoAnaliseModal: HistoricoAnaliseModalStub,
          ImpactoMapaModal: ImpactoMapaModalStub,
          ModalConfirmacao: ModalConfirmacaoStub,
          BCard: BCardStub,
          BCardBody: BCardBodyStub,
          BFormGroup: BFormGroupStub,
          BFormTextarea: BFormTextareaStub,
          BFormInvalidFeedback: BFormInvalidFeedbackStub,
          BButton: BButtonStub
        }
      }
    });
  };

  it('deve montar corretamente e buscar dados iniciais', async () => {
    wrapper = createWrapper();
    const processosStore = useProcessosStore();
    const subprocessosStore = useSubprocessosStore();
    const atividadesStore = useAtividadesStore();
    
    // Mock the getter
    atividadesStore.obterAtividadesPorSubprocesso = vi.fn().mockReturnValue([
      { codigo: 100, descricao: 'Atividade 1', conhecimentos: [{ codigo: 1000, descricao: 'Conhecimento 1' }] }
    ]);
    
    await flushPromises();

    expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(1);
    expect(atividadesStore.buscarAtividadesParaSubprocesso).toHaveBeenCalledWith(10);
    expect(subprocessosStore.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);

    expect(wrapper.text()).toContain('Atividade 1');
    expect(wrapper.text()).toContain('Conhecimento 1');
  });

  it('deve abrir modal de historico de analise', async () => {
    wrapper = createWrapper();
    await flushPromises();
    
    const btn = wrapper.find('[data-testid="btn-vis-atividades-historico"]');
    await btn.trigger('click');
    
    expect(wrapper.vm.mostrarModalHistoricoAnalise).toBe(true);
    expect(wrapper.find('[data-testid="historico-modal"]').exists()).toBe(true);
  });

  it('deve abrir modal de impacto', async () => {
    wrapper = createWrapper();
    const mapasStore = useMapasStore();
    await flushPromises();
    
    const btn = wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa-visualizacao"]');
    await btn.trigger('click');
    
    expect(wrapper.vm.mostrarModalImpacto).toBe(true);
    expect(mapasStore.buscarImpactoMapa).toHaveBeenCalledWith(10);
  });

  describe('Fluxo de Validação/Aceite', () => {
    it('deve confirmar aceite do cadastro (nao homologacao)', async () => {
      wrapper = createWrapper();
      const subprocessosStore = useSubprocessosStore();
      subprocessosStore.aceitarCadastro.mockResolvedValueOnce(true);
      
      await flushPromises();
      
      // Abrir modal
      await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger('click');
      expect(wrapper.vm.mostrarModalValidar).toBe(true);
      
      // Escrever obs e confirmar
      wrapper.vm.observacaoValidacao = 'Tudo certo';
      
      // O evento emitido pelo modal stub
      const modals = wrapper.findAll('[data-testid="modal-confirmacao"]');
      await modals[0].find('button').trigger('click'); // confirm button in stub
      
      expect(subprocessosStore.aceitarCadastro).toHaveBeenCalledWith(10, { observacoes: 'Tudo certo' });
      await flushPromises();
      
      expect(wrapper.vm.mostrarModalValidar).toBe(false);
      expect(mockRouter.push).toHaveBeenCalledWith({ name: 'Painel' });
    });

    it('deve confirmar homologacao do cadastro (mapeamento)', async () => {
      (useAcesso as any).mockReturnValue({
        podeHomologarCadastro: { value: true }, // IS_HOMOLOGACAO
        podeAceitarCadastro: { value: false },
        podeDevolverCadastro: { value: false },
        podeVisualizarImpacto: { value: false }
      });
      
      wrapper = createWrapper();
      const subprocessosStore = useSubprocessosStore();
      subprocessosStore.homologarCadastro.mockResolvedValueOnce(true);
      
      await flushPromises();
      
      await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger('click');
      
      wrapper.vm.observacaoValidacao = 'Homologado';
      
      const modals = wrapper.findAll('[data-testid="modal-confirmacao"]');
      await modals[0].find('button').trigger('click');
      
      expect(subprocessosStore.homologarCadastro).toHaveBeenCalledWith(10, { observacoes: 'Homologado' });
      await flushPromises();
      
      expect(mockRouter.push).toHaveBeenCalledWith({
        name: "Subprocesso",
        params: { codProcesso: 1, siglaUnidade: 'TESTE' },
      });
    });

    it('deve confirmar aceite da revisao do cadastro', async () => {
      (useAcesso as any).mockReturnValue({
        podeHomologarCadastro: { value: false },
        podeAceitarCadastro: { value: true },
        podeDevolverCadastro: { value: false },
        podeVisualizarImpacto: { value: false }
      });
      
      wrapper = createWrapper({
        processos: {
          processoDetalhe: { codigo: 1, tipo: TipoProcesso.REVISAO, unidades: [{ sigla: 'TESTE', codSubprocesso: 10 }] }
        }
      });
      
      const subprocessosStore = useSubprocessosStore();
      subprocessosStore.aceitarRevisaoCadastro.mockResolvedValueOnce(true);
      
      await flushPromises();
      
      await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger('click');
      await wrapper.findAll('[data-testid="modal-confirmacao"]')[0].find('button').trigger('click');
      
      expect(subprocessosStore.aceitarRevisaoCadastro).toHaveBeenCalled();
    });

    it('deve confirmar homologacao da revisao do cadastro', async () => {
      (useAcesso as any).mockReturnValue({
        podeHomologarCadastro: { value: true },
        podeAceitarCadastro: { value: false },
        podeDevolverCadastro: { value: false },
        podeVisualizarImpacto: { value: false }
      });
      
      wrapper = createWrapper({
        processos: {
          processoDetalhe: { codigo: 1, tipo: TipoProcesso.REVISAO, unidades: [{ sigla: 'TESTE', codSubprocesso: 10 }] }
        }
      });
      
      const subprocessosStore = useSubprocessosStore();
      subprocessosStore.homologarRevisaoCadastro.mockResolvedValueOnce(true);
      
      await flushPromises();
      
      await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger('click');
      await wrapper.findAll('[data-testid="modal-confirmacao"]')[0].find('button').trigger('click');
      
      expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalled();
    });
  });

  describe('Fluxo de Devolução', () => {
    it('deve não permitir devolver se observacao estiver vazia', async () => {
      (useAcesso as any).mockReturnValue({
        podeHomologarCadastro: { value: false },
        podeAceitarCadastro: { value: false },
        podeDevolverCadastro: { value: true },
        podeVisualizarImpacto: { value: false }
      });

      wrapper = createWrapper();
      const subprocessosStore = useSubprocessosStore();
      
      await flushPromises();
      
      await wrapper.find('[data-testid="btn-acao-devolver"]').trigger('click');
      
      wrapper.vm.observacaoDevolucao = '   '; // Espaços vazios
      
      const modals = wrapper.findAll('[data-testid="modal-confirmacao"]');
      await modals[0].find('button').trigger('click');
      
      // Não deve chamar a store
      expect(subprocessosStore.devolverCadastro).not.toHaveBeenCalled();
      expect(wrapper.vm.estadoObservacaoDevolucao).toBe(false); // Fica inválido
    });

    it('deve confirmar devolucao do cadastro (mapeamento)', async () => {
      (useAcesso as any).mockReturnValue({
        podeHomologarCadastro: { value: false },
        podeAceitarCadastro: { value: false },
        podeDevolverCadastro: { value: true },
        podeVisualizarImpacto: { value: false }
      });

      wrapper = createWrapper();
      const subprocessosStore = useSubprocessosStore();
      subprocessosStore.devolverCadastro.mockResolvedValueOnce(true);
      
      await flushPromises();
      
      await wrapper.find('[data-testid="btn-acao-devolver"]').trigger('click');
      
      wrapper.vm.observacaoDevolucao = 'Precisa melhorar';
      
      const modals = wrapper.findAll('[data-testid="modal-confirmacao"]');
      await modals[0].find('button').trigger('click');
      
      expect(subprocessosStore.devolverCadastro).toHaveBeenCalledWith(10, { observacoes: 'Precisa melhorar' });
      await flushPromises();
      
      expect(wrapper.vm.mostrarModalDevolver).toBe(false);
      expect(mockRouter.push).toHaveBeenCalledWith('/painel');
    });

    it('deve confirmar devolucao da revisao do cadastro', async () => {
      (useAcesso as any).mockReturnValue({
        podeHomologarCadastro: { value: false },
        podeAceitarCadastro: { value: false },
        podeDevolverCadastro: { value: true },
        podeVisualizarImpacto: { value: false }
      });

      wrapper = createWrapper({
        processos: {
          processoDetalhe: { codigo: 1, tipo: TipoProcesso.REVISAO, unidades: [{ sigla: 'TESTE', codSubprocesso: 10 }] }
        }
      });
      
      const subprocessosStore = useSubprocessosStore();
      subprocessosStore.devolverRevisaoCadastro.mockResolvedValueOnce(true);
      
      await flushPromises();
      
      await wrapper.find('[data-testid="btn-acao-devolver"]').trigger('click');
      
      wrapper.vm.observacaoDevolucao = 'Revisar novamente';
      
      const modals = wrapper.findAll('[data-testid="modal-confirmacao"]');
      await modals[0].find('button').trigger('click');
      
      expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(10, { observacoes: 'Revisar novamente' });
    });
  });
});
