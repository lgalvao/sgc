import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import ConfiguracoesView from "@/views/ConfiguracoesView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useConfiguracoesStore} from "@/stores/configuracoes";

describe("ConfiguracoesView.vue", () => {
    const mountOptions = () => ({
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        configuracoes: {
                            diasInativacaoProcesso: 30,
                            diasAlertaNovo: 7
                        }
                    },
                    stubActions: true,
                }),
            ],
            stubs: {
                BContainer: {template: '<div><slot /></div>'},
                BForm: {template: '<form><slot /></form>'},
                BFormInput: {
                    props: ['modelValue'],
                    emits: ['update:modelValue'],
                    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', Number($event.target.value))" />'
                },
                BButton: {template: '<button type="submit"><slot /></button>'},
                BAlert: {
                    template: '<div class="alert-success" v-if="modelValue"><slot /></div>',
                    props: ['modelValue']
                },
            },
        },
    });

    it("deve renderizar o formulário com valores iniciais", () => {
        const wrapper = mount(ConfiguracoesView, mountOptions());
        const store = useConfiguracoesStore();

        expect(wrapper.find('[data-testid="inp-config-dias-inativacao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="inp-config-dias-alerta"]').exists()).toBe(true);

        // Check if store values are bound (v-model)
        // Accessing component instance or dom value if implemented in stub
        // The stub binds :value.
        const inputInativacao = wrapper.find('[data-testid="inp-config-dias-inativacao"]');
        expect(inputInativacao.element.value).toBe("30");

        expect(store.carregarConfiguracoes).toHaveBeenCalled();
    });

    it("deve salvar configurações e exibir mensagem de sucesso", async () => {
        const wrapper = mount(ConfiguracoesView, mountOptions());
        const store = useConfiguracoesStore();

        // Mock return true for save
        (store.salvarConfiguracoes as any).mockReturnValue(true);

        // Update values
        await wrapper.find('[data-testid="inp-config-dias-inativacao"]').setValue(60);
        await wrapper.find('[data-testid="inp-config-dias-alerta"]').setValue(14);

        // Submit
        await wrapper.find('form').trigger('submit');

        expect(store.diasInativacaoProcesso).toBe(60);
        expect(store.diasAlertaNovo).toBe(14);
        expect(store.salvarConfiguracoes).toHaveBeenCalled();

        // Message
        expect(wrapper.find('.alert-success').exists()).toBe(true);
        expect(wrapper.find('.alert-success').text()).toBe("Configurações salvas!");
    });

    it("mensagem de sucesso deve desaparecer após 3 segundos", async () => {
        vi.useFakeTimers();
        const wrapper = mount(ConfiguracoesView, mountOptions());
        const store = useConfiguracoesStore();
        (store.salvarConfiguracoes as any).mockReturnValue(true);

        await wrapper.find('form').trigger('submit');
        expect(wrapper.find('.alert-success').exists()).toBe(true);

        vi.advanceTimersByTime(3000);
        await wrapper.vm.$nextTick();

        expect(wrapper.find('.alert-success').exists()).toBe(false);
        vi.useRealTimers();
    });
});
