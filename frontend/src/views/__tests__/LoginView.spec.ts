import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import LoginView from "../LoginView.vue";
import {createTestingPinia} from "@pinia/testing";
import {usePerfilStore} from "@/stores/perfil";
import {ref} from "vue";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRouter: () => ({ push: pushMock }),
}));

vi.mock("@/utils", () => ({
    logger: { error: vi.fn(), warn: vi.fn(), info: vi.fn() }
}));

vi.mock("@/utils/apiError", () => ({
    normalizeError: vi.fn((err) => {
        if (err?.message === "Erro interno") return { kind: 'unexpected', message: "Erro interno" };
        return { kind: 'unexpected', message: "Erro" };
    })
}));

vi.mock("@/composables/useNotification", () => ({
    useNotification: vi.fn(() => ({
        notificacao: ref(null),
        notify: vi.fn(),
        clear: vi.fn()
    }))
}));

vi.mock("@/composables/useValidacaoFormulario", () => ({
    useValidacaoFormulario: vi.fn(() => ({
        validarSubmissao: vi.fn(() => true),
        resetarValidacao: vi.fn(),
        deveExibirErro: vi.fn(() => false),
        focarPrimeiroErroInvalido: vi.fn()
    }))
}));

const stubs = {
    BContainer: { template: '<div><slot/></div>' },
    BRow: { template: '<div><slot/></div>' },
    BCol: { template: '<div><slot/></div>' },
    BCard: { template: '<div><slot/></div>' },
    BForm: { template: '<form @submit.prevent="$emit(\'submit\')"><slot/></form>' },
    BFormGroup: { template: '<div><slot name="label"/><slot/></div>' },
    BFormInput: {
        props: ['modelValue', 'state'],
        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
    },
    BFormInvalidFeedback: { template: '<div><slot/></div>', props: ['state'] },
    BInputGroup: { template: '<div><slot/><slot name="append"/></div>' },
    BButton: { template: '<button @click="$emit(\'click\')"><slot/></button>' },
    BAlert: { template: '<div><slot/></div>', props: ['modelValue'] },
    BFormSelect: {
        props: ['modelValue', 'options'],
        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', options[$event.target.selectedIndex-1].value)"><slot/><option v-for="o in options" :key="o.text">{{o.text}}</option></select>'
    },
    BFormSelectOption: { template: '<option><slot/></option>', props: ['value'] },
    LoadingButton: { template: '<button type="submit"><slot/></button>', props: ['loading'] },
    AppAlert: { template: '<div><slot/></div>' },
};

describe("LoginView", () => {
    let pinia: any;

    beforeEach(() => {
        vi.clearAllMocks();
        pinia = createTestingPinia({ stubActions: false });
    });

    it("deve realizar login inicial com sucesso (perfil único)", async () => {
        const store = usePerfilStore();
        vi.spyOn(store, 'iniciarLogin').mockResolvedValue({
            autenticado: true,
            requerSelecaoPerfil: false,
            perfisUnidades: [{ perfil: 'GESTOR', unidade: { codigo: 1, sigla: 'U1' } }],
            sessao: { perfil: 'GESTOR', unidadeCodigo: 1 }
        } as any);
        store.perfisUnidades = [{ perfil: 'GESTOR', unidade: { codigo: 1, sigla: 'U1' } }] as any;

        const wrapper = mount(LoginView, {
            global: { plugins: [pinia], stubs }
        });

        const inputs = wrapper.findAll('input');
        await inputs[0].setValue("123");
        await inputs[1].setValue("senha");
        
        await wrapper.find('form').trigger('submit');
        await flushPromises();

        expect(pushMock).toHaveBeenCalledWith("/painel");
    });
});
