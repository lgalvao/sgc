import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import AcoesEmBlocoModal from '../AcoesEmBlocoModal.vue';
import { BFormCheckbox } from 'bootstrap-vue-next';

const unidadesDisponiveis = [
  { sigla: 'UND1', nome: 'Unidade 1', situacao: 'Pendente' },
  { sigla: 'UND2', nome: 'Unidade 2', situacao: 'Pendente' },
];

const BModalStub = {
  template: `
    <div v-if="modelValue">
      <h5 class="modal-title">{{ title }}</h5>
      <slot />
      <slot name="footer" />
    </div>
  `,
  props: ['modelValue', 'title'],
};

const BFormCheckboxStub = {
  template: '<input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
  props: ['modelValue'],
};

describe('AcoesEmBlocoModal', () => {
  const globalConfig = {
    global: {
      stubs: {
        'b-modal': BModalStub,
        'b-form-checkbox': BFormCheckboxStub,
      },
    },
  };

  const createWrapper = (props: any) => {
    return mount(AcoesEmBlocoModal, {
      props: {
        mostrar: false,
        unidadesDisponiveis,
        ...props,
      },
      ...globalConfig,
    });
  };

  it("deve renderizar o título e o botão corretamente para a ação 'aceitar'", async () => {
    const wrapper = createWrapper({ tipoAcao: 'aceitar' });
    await wrapper.setProps({ mostrar: true });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('.modal-title').text()).toContain('Aceitar cadastros em bloco');
    expect(wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').text()).toContain('Aceitar');
  });

  it("deve renderizar o título e o botão corretamente para a ação 'homologar'", async () => {
    const wrapper = createWrapper({ tipoAcao: 'homologar' });
    await wrapper.setProps({ mostrar: true });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('.modal-title').text()).toContain('Homologar cadastros em bloco');
    expect(wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').text()).toContain('Homologar');
  });

  it('deve pré-selecionar todas as unidades quando o modal é exibido', async () => {
    const wrapper = createWrapper({ tipoAcao: 'aceitar' });
    await wrapper.setProps({ mostrar: true });
    await wrapper.vm.$nextTick();

    const checkboxes = wrapper.findAll('input[type="checkbox"]');
    expect(checkboxes.every(c => (c.element as HTMLInputElement).checked)).toBe(true);
  });

  it("deve emitir o evento 'fechar' ao clicar no botão de cancelar", async () => {
    const wrapper = createWrapper({ tipoAcao: 'aceitar' });
    await wrapper.setProps({ mostrar: true });
    await wrapper.vm.$nextTick();

    await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click');
    expect(wrapper.emitted()).toHaveProperty('fechar');
  });

  it("deve emitir o evento 'confirmar' com as unidades selecionadas", async () => {
    const wrapper = createWrapper({ tipoAcao: 'aceitar' });
    await wrapper.setProps({ mostrar: true });
    await wrapper.vm.$nextTick();

    const checkbox = wrapper.find('[data-testid="chk-unidade-UND2"]');
    await checkbox.setValue(false);

    await wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').trigger('click');

    expect(wrapper.emitted()).toHaveProperty('confirmar');
    const payload = wrapper.emitted('confirmar')![0] as any[];
    expect(payload[0]).toEqual(['UND1']);
  });

  it('deve exibir um alerta se nenhuma unidade for selecionada', async () => {
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
    const wrapper = createWrapper({ tipoAcao: 'aceitar' });
    await wrapper.setProps({ mostrar: true });
    await wrapper.vm.$nextTick();

    const checkbox1 = wrapper.find('[data-testid="chk-unidade-UND1"]');
    await checkbox1.setValue(false);
    const checkbox2 = wrapper.find('[data-testid="chk-unidade-UND2"]');
    await checkbox2.setValue(false);

    await wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').trigger('click');

    expect(alertSpy).toHaveBeenCalledWith('Selecione ao menos uma unidade para processar.');
    expect(wrapper.emitted()).not.toHaveProperty('confirmar');

    alertSpy.mockRestore();
  });
});
