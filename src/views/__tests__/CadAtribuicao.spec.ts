import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import CadAtribuicao from '../CadAtribuicao.vue';
import { useUnidadesStore } from '@/stores/unidades';
import { useAtribuicaoTemporariaStore } from '@/stores/atribuicaoTemporaria';
import { useRouter } from 'vue-router';
import { ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';

// Mock do useRouter
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

// Mock do servidoresMock.json
vi.mock('@/mocks/servidores.json', () => ({
  default: [
    { id: 1, nome: 'Servidor A', unidade: 'UN1', email: null, ramal: null },
    { id: 2, nome: 'Servidor B', unidade: 'UN1', email: null, ramal: null },
    { id: 3, nome: 'Servidor C', unidade: 'UN2', email: null, ramal: null },
  ],
}));

describe('CadAtribuicao.vue', () => {
  let unidadesStore: ReturnType<typeof useUnidadesStore>;
  let atribuicaoStore: ReturnType<typeof useAtribuicaoTemporariaStore>;
  let routerPushMock: ReturnType<typeof useRouter>['push'];

  beforeEach(() => {
    setActivePinia(createPinia());
    unidadesStore = useUnidadesStore();
    atribuicaoStore = useAtribuicaoTemporariaStore();
    routerPushMock = useRouter().push;

    vi.clearAllMocks();

    // Mock de dados iniciais para as stores
    unidadesStore.unidades = [
      { sigla: 'UN1', nome: 'Unidade Teste 1', titular: 1, responsavel: null, tipo: 'OPERACIONAL', filhas: [] },
      { sigla: 'UN2', nome: 'Unidade Teste 2', titular: 3, responsavel: null, tipo: 'OPERACIONAL', filhas: [] },
    ];
    atribuicaoStore.atribuicoes = [];

    // Espionar métodos das stores
    vi.spyOn(atribuicaoStore, 'criarAtribuicao');

    // Resetar o estado das stores para garantir isolamento entre os testes
    unidadesStore.$reset();
    atribuicaoStore.$reset();
  });

  it('deve renderizar corretamente com os dados iniciais da unidade', async () => {
    const wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'UN1',
      },
      global: {
        plugins: [createPinia()],
      },
    });

    expect(wrapper.text()).toContain('UN1 - Unidade Teste 1');
    expect(wrapper.find('h2').text()).toBe('Criar atribuição temporária');
    expect(wrapper.find('[data-testid="select-servidor"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="input-data-termino"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="textarea-justificativa"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="btn-criar-atribuicao"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="btn-cancelar-atribuicao"]').exists()).toBe(true);
  });

  it('deve carregar servidores elegíveis (excluir titular e já atribuídos)', async () => {
    atribuicaoStore.atribuicoes = [
      { unidade: 'UN1', servidorId: 2, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'Teste' },
    ];

    const wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'UN1',
      },
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();

    const options = wrapper.findAll('[data-testid="select-servidor"] option');
    expect(options.length).toBe(1);
    expect(options[0].text()).toBe('Selecione um servidor');
  });

  it('deve criar uma nova atribuição temporária e redirecionar', async () => {
    const wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'UN1',
      },
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.find('[data-testid="select-servidor"]').setValue(2);
    await wrapper.find('[data-testid="input-data-termino"]').setValue('2025-12-31');
    await wrapper.find('[data-testid="textarea-justificativa"]').setValue('Justificativa de teste');
    await wrapper.find('[data-testid="btn-criar-atribuicao"]').trigger('submit');

    expect(atribuicaoStore.criarAtribuicao).toHaveBeenCalledWith(
      expect.objectContaining({
        unidade: 'UN1',
        servidorId: 2,
        justificativa: 'Justificativa de teste',
      })
    );
    expect(wrapper.find('.alert-success').exists()).toBe(true);
    await new Promise(resolve => setTimeout(resolve, 1200));
    expect(routerPushMock).toHaveBeenCalledWith('/unidade/UN1');
  });

  it('não deve criar atribuição se nenhum servidor for selecionado', async () => {
    const wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'UN1',
      },
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.find('[data-testid="input-data-termino"]').setValue('2025-12-31');
    await wrapper.find('[data-testid="textarea-justificativa"]').setValue('Justificativa de teste');
    await wrapper.find('[data-testid="btn-criar-atribuicao"]').trigger('submit');

    expect(atribuicaoStore.criarAtribuicao).not.toHaveBeenCalled();
    expect(wrapper.find('.text-danger').text()).toBe('Selecione um servidor elegível.');
  });

  it('não deve criar atribuição se o servidor já tiver uma atribuição temporária', async () => {
    atribuicaoStore.atribuicoes = [
      { unidade: 'UN1', servidorId: 2, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'Teste' },
    ];

    const wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'UN1',
      },
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.find('[data-testid="select-servidor"]').setValue(2);
    await wrapper.find('[data-testid="input-data-termino"]').setValue('2025-12-31');
    await wrapper.find('[data-testid="textarea-justificativa"]').setValue('Justificativa de teste');
    await wrapper.find('[data-testid="btn-criar-atribuicao"]').trigger('submit');

    expect(atribuicaoStore.criarAtribuicao).not.toHaveBeenCalled();
    expect(wrapper.find('.text-danger').text()).toBe('Este servidor já possui atribuição temporária nesta unidade.');
  });

  it('deve redirecionar para a página da unidade ao clicar em cancelar', async () => {
    const wrapper = mount(CadAtribuicao, {
      props: {
        sigla: 'UN1',
      },
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.find('[data-testid="btn-cancelar-atribuicao"]').trigger('click');
    expect(routerPushMock).toHaveBeenCalledWith('/unidade/UN1');
  });
});
