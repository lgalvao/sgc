import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import CadAtribuicao from '@/views/CadAtribuicao.vue';
import { criarAtribuicaoTemporaria } from '@/services/atribuicaoTemporariaService';
import { buscarUnidadePorSigla } from '@/services/unidadesService';
import { buscarUsuariosPorUnidade } from '@/services/usuarioService';

// Mocks
const mockPush = vi.fn();
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

vi.mock('@/services/atribuicaoTemporariaService', () => ({
  criarAtribuicaoTemporaria: vi.fn(),
}));
vi.mock('@/services/unidadesService', () => ({
  buscarUnidadePorSigla: vi.fn(),
}));
vi.mock('@/services/usuarioService', () => ({
  buscarUsuariosPorUnidade: vi.fn(),
}));

describe('CadAtribuicao.vue', () => {
  let wrapper: any;

  const mockUnidade = {
    codigo: 1,
    sigla: 'TEST',
    nome: 'Unidade Teste'
  };

  const mockUsuarios = [
    { codigo: '111', nome: 'Servidor 1', tituloEleitoral: '111' },
    { codigo: '222', nome: 'Servidor 2', tituloEleitoral: '222' }
  ];

  beforeEach(() => {
    vi.clearAllMocks();

    (buscarUnidadePorSigla as any).mockResolvedValue(mockUnidade);
    (buscarUsuariosPorUnidade as any).mockResolvedValue(mockUsuarios);

    wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'TEST'
      },
      global: {
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BCard: { template: '<div><slot /></div>' },
          BCardBody: { template: '<div><slot /></div>' },
          BForm: { template: '<form @submit.prevent="$emit(\'submit\', { preventDefault: () => {} })"><slot /></form>' },
          BFormSelect: {
            template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot name="first" /><option v-for="opt in options" :key="opt.codigo" :value="opt.codigo">{{ opt.nome }}</option></select>',
            props: ['modelValue', 'options']
          },
          BFormSelectOption: { template: '<option><slot /></option>' },
          BFormInput: {
            template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue']
          },
          BFormTextarea: {
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
            props: ['modelValue']
          },
          BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          BAlert: { template: '<div role="alert"><slot /></div>' },
        },
      },
    });
  });

  it('fetches data on mount', async () => {
    expect(buscarUnidadePorSigla).toHaveBeenCalledWith('TEST');
    
    await flushPromises();
    
    expect(buscarUsuariosPorUnidade).toHaveBeenCalledWith(1);
    expect(wrapper.vm.servidores).toHaveLength(2);
  });

  it('submits the form successfully', async () => {
    await flushPromises();

    // Fill form
    const select = wrapper.find('[data-testid="select-servidor"]');
    await select.setValue('111');
    
    const dateInput = wrapper.find('[data-testid="input-data-termino"]');
    await dateInput.setValue('2023-12-31');
    
    const textarea = wrapper.find('[data-testid="textarea-justificativa"]');
    await textarea.setValue('Justificativa de teste');
    
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    
    expect(criarAtribuicaoTemporaria).toHaveBeenCalledWith(1, {
      tituloEleitoralServidor: '111',
      dataTermino: '2023-12-31',
      justificativa: 'Justificativa de teste'
    });
    
    expect(wrapper.text()).toContain('Atribuição criada!');
  });

  it('handles submission error', async () => {
    await flushPromises();

    (criarAtribuicaoTemporaria as any).mockRejectedValue(new Error('API Error'));

    // Fill form
    wrapper.vm.servidorSelecionado = '111';
    wrapper.vm.dataTermino = '2023-12-31';
    wrapper.vm.justificativa = 'Teste';
    
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    
    expect(wrapper.text()).toContain('Falha ao criar atribuição');
  });

  it('cancels and navigates back', async () => {
    const btn = wrapper.find('[data-testid="btn-cancelar-atribuicao"]');
    await btn.trigger('click');
    
    expect(mockPush).toHaveBeenCalledWith('/unidade/TEST');
  });
});