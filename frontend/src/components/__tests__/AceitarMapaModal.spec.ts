import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AceitarMapaModal from '@/components/AceitarMapaModal.vue';
import { BFormTextarea, BButton, BModal } from 'bootstrap-vue-next';

// Função fábrica para criar o wrapper
const createWrapper = (propsOverride = {}) => {
  return mount(AceitarMapaModal, {
    props: {
      mostrarModal: true,
      ...propsOverride,
    },
    global: {
      components: {
        BFormTextarea,
        BButton,
        BModal
      },
      stubs: {
        // Stubbing BModal to focus on content and events
        BModal: {
            template: `
                <div v-if="modelValue" data-testid="modal-stub">
                    <slot />
                    <slot name="footer" />
                </div>
            `,
            props: ['modelValue'],
            emits: ['update:modelValue', 'hide']
        }
      }
    },
  });
};

describe('AceitarMapaModal.vue', () => {
  it('não deve renderizar o modal quando mostrarModal for falso', () => {
    const wrapper = createWrapper({ mostrarModal: false });
    expect(wrapper.find('[data-testid="modal-stub"]').exists()).toBe(false);
  });

  it('deve renderizar o modal com o perfil padrão (não ADMIN)', () => {
    const wrapper = createWrapper({ perfil: 'CHEFE' }); // Default behavior

    const corpoModal = wrapper.find('[data-testid="modal-aceite-body"]');
    expect(corpoModal.exists()).toBe(true);
    expect(corpoModal.text()).toContain('Observações');
    expect(wrapper.find('[data-testid="observacao-aceite-textarea"]').exists()).toBe(true);
  });

  it('deve renderizar o modal com o perfil ADMIN', () => {
    const wrapper = createWrapper({ perfil: 'ADMIN' });

    const corpoModal = wrapper.find('[data-testid="modal-aceite-body"]');
    expect(corpoModal.exists()).toBe(true);
    expect(corpoModal.text()).toContain('Confirma a homologação do mapa de competências?');
    // Textarea não deve existir para admin
    expect(wrapper.find('[data-testid="observacao-aceite-textarea"]').exists()).toBe(false);
  });

  it('deve emitir o evento fecharModal ao clicar no botão de cancelar', async () => {
    const wrapper = createWrapper();

    await wrapper.find('[data-testid="modal-aceite-cancelar"]').trigger('click');
    expect(wrapper.emitted('fecharModal')).toBeTruthy();
  });

  it('deve emitir o evento confirmarAceitacao com a observação', async () => {
    const wrapper = createWrapper();
    const observacao = 'Mapa de competências está de acordo com o esperado.';

    const textareaWrapper = wrapper.findComponent(BFormTextarea);
    const nativeTextarea = textareaWrapper.find('textarea');
    await nativeTextarea.setValue(observacao);

    await wrapper.find('[data-testid="modal-aceite-confirmar"]').trigger('click');

    expect(wrapper.emitted('confirmarAceitacao')).toBeTruthy();
    expect(wrapper.emitted('confirmarAceitacao')?.[0]).toEqual([observacao]);
  });

  it('deve emitir o evento confirmarAceitacao com uma observação vazia', async () => {
    const wrapper = createWrapper();

    await wrapper.find('[data-testid="modal-aceite-confirmar"]').trigger('click');

    expect(wrapper.emitted('confirmarAceitacao')).toBeTruthy();
    expect(wrapper.emitted('confirmarAceitacao')?.[0]).toEqual(['']);
  });
});
