import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import LimpezaProcessosView from "@/views/LimpezaProcessosView.vue";
import * as processoService from "@/services/processoService";
import {createTestingPinia} from "@pinia/testing";

vi.mock("@/services/processoService", () => ({
    excluirProcessoCompleto: vi.fn(),
}));

describe("LimpezaProcessosView.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const createWrapper = () => mount(LimpezaProcessosView, {
        global: {
            plugins: [createTestingPinia({createSpy: vi.fn})],
            stubs: {
                LayoutPadrao: {template: '<div><slot /></div>'},
                PageHeader: {
                    props: ['title'],
                    template: '<div><h1>{{ title }}</h1></div>'
                },
                AppAlert: {
                    props: ['message'],
                    template: '<div class="app-alert">{{ message }}</div>'
                },
                BAlert: {template: '<div><slot /></div>'},
                BCard: {template: '<div><slot /></div>'},
                BFormGroup: {template: '<div><slot /><slot name="label" /></div>'},
                BFormInput: {
                    props: ['modelValue'],
                    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    emits: ['update:modelValue']
                },
                LoadingButton: {
                    props: ['text'],
                    template: '<button @click="$emit(\'click\')">{{ text }}</button>',
                    emits: ['click']
                },
                ModalConfirmacao: {
                    name: 'ModalConfirmacao',
                    props: ['modelValue'],
                    template: '<div v-if="modelValue"><slot /><button class="confirm" @click="$emit(\'confirmar\')">OK</button></div>',
                    emits: ['confirmar', 'update:modelValue']
                }
            }
        }
    });

    it("deve abrir confirmacao e excluir processo completo", async () => {
        vi.mocked(processoService.excluirProcessoCompleto).mockResolvedValue();
        const wrapper = createWrapper();

        await wrapper.find('input').setValue('15');
        await wrapper.find('button').trigger('click');
        await flushPromises();

        expect(wrapper.text()).toContain('processo 15');

        await wrapper.find('button.confirm').trigger('click');
        await flushPromises();

        expect(processoService.excluirProcessoCompleto).toHaveBeenCalledWith(15);
    });

    it("nao deve excluir com codigo invalido", async () => {
        const wrapper = createWrapper();

        await wrapper.find('button').trigger('click');
        await flushPromises();

        expect(processoService.excluirProcessoCompleto).not.toHaveBeenCalled();
        expect(wrapper.text()).toContain('Informe um código de processo válido');
    });
});
