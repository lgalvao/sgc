import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import LoginView from "@/views/LoginView.vue";
import {createTestingPinia} from "@pinia/testing";
import {usePerfilStore} from "@/stores/perfil";
import {useFeedbackStore} from "@/stores/feedback";
import {useRouter} from "vue-router";
import {Perfil} from "@/types/tipos";

// Mocks
vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    createRouter: vi.fn(() => ({
        push: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe("LoginView.vue", () => {
    let routerPushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
    });

    const mountOptions = (initialState = {}) => ({
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: initialState,
                    stubActions: false, // We want to call actions to test logic inside them or mock them explicitly
                }),
            ],
            // Stubs for bootstrap components to simplify testing
            stubs: {
                BCard: {template: "<div><slot /></div>"},
                BForm: {template: '<form @submit.prevent><slot /></form>'},
                // Simple input stubs that support v-model
                BFormInput: {
                    props: ['modelValue'],
                    emits: ['update:modelValue'],
                    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
                },
                BFormSelect: {
                    props: ['modelValue', 'options', 'value-field', 'text-field'],
                    emits: ['update:modelValue'],
                    template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot /></select>'
                },
                BFormSelectOption: {
                    props: ['value'],
                    template: '<option :value="value"><slot /></option>'
                },
                BButton: {template: '<button type="submit"><slot /></button>'},
            },
        },
    });

    it("deve renderizar o formulário de login corretamente", () => {
        const wrapper = mount(LoginView, mountOptions());

        expect(wrapper.find('[data-testid="txt-login-titulo"]').text()).toBe("SGC");
        expect(wrapper.find('[data-testid="inp-login-usuario"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="inp-login-senha"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-login-entrar"]').exists()).toBe(true);
        // Passo 2 deve estar oculto
        expect(wrapper.find('[data-testid="sec-login-perfil"]').exists()).toBe(false);
    });

    it("deve mostrar erro se campos estiverem vazios", async () => {
        const wrapper = mount(LoginView, mountOptions());
        const feedbackStore = useFeedbackStore();

        // Limpar campos (já que no DEV mode vêm preenchidos)
        await wrapper.find('[data-testid="inp-login-usuario"]').setValue("");
        await wrapper.find('[data-testid="inp-login-senha"]').setValue("");

        await wrapper.find('form').trigger('submit');

        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Dados incompletos",
            "Por favor, preencha título e senha.",
            "danger"
        );
    });

    it("deve realizar login com sucesso e redirecionar para painel (perfil único)", async () => {
        const wrapper = mount(LoginView, mountOptions());
        const perfilStore = usePerfilStore();

        // Mock do loginCompleto retornando sucesso
        perfilStore.loginCompleto = vi.fn().mockResolvedValue(true);
        // Mock de um único perfil
        perfilStore.perfisUnidades = [{perfil: Perfil.ADMIN, unidade: {sigla: "SEDE", codigo: 1, nome: "Sede"}, siglaUnidade: "SEDE"}];

        await wrapper.find('[data-testid="inp-login-usuario"]').setValue("123");
        await wrapper.find('[data-testid="inp-login-senha"]').setValue("pass");
        await wrapper.find('form').trigger('submit');

        expect(perfilStore.loginCompleto).toHaveBeenCalledWith("123", "pass");
        expect(routerPushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve exibir erro se login falhar", async () => {
        const wrapper = mount(LoginView, mountOptions());
        const perfilStore = usePerfilStore();
        const feedbackStore = useFeedbackStore();

        perfilStore.loginCompleto = vi.fn().mockResolvedValue(false);

        await wrapper.find('[data-testid="inp-login-usuario"]').setValue("123");
        await wrapper.find('[data-testid="inp-login-senha"]').setValue("wrong");
        await wrapper.find('form').trigger('submit');

        expect(perfilStore.loginCompleto).toHaveBeenCalled();
        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Erro no login",
            "Título ou senha inválidos.",
            "danger"
        );
    });

    it("deve avançar para passo 2 se houver múltiplos perfis", async () => {
        const wrapper = mount(LoginView, mountOptions());
        const perfilStore = usePerfilStore();

        perfilStore.loginCompleto = vi.fn().mockResolvedValue(true);
        perfilStore.perfisUnidades = [
            {perfil: Perfil.ADMIN, unidade: {sigla: "SEDE", codigo: 1, nome: "Sede"}, siglaUnidade: "SEDE"},
            {perfil: Perfil.SERVIDOR, unidade: {sigla: "FILIAL", codigo: 2, nome: "Filial"}, siglaUnidade: "FILIAL"}
        ];

        await wrapper.find('[data-testid="inp-login-usuario"]').setValue("123");
        await wrapper.find('[data-testid="inp-login-senha"]').setValue("pass");
        await wrapper.find('form').trigger('submit');

        expect(routerPushMock).not.toHaveBeenCalled();
        // Verifica se mudou o passo (aparece o select)
        expect(wrapper.find('[data-testid="sec-login-perfil"]').exists()).toBe(true);
    });

    it("deve selecionar perfil e logar no passo 2", async () => {
        const wrapper = mount(LoginView, mountOptions());
        const perfilStore = usePerfilStore();

        // Configura estado inicial como se já tivesse passado do passo 1
        // Para testar o passo 2, precisamos simular o fluxo ou manipular o estado interno
        // Como loginStep é ref interna, vamos fazer o fluxo completo

        perfilStore.loginCompleto = vi.fn().mockResolvedValue(true);
        perfilStore.selecionarPerfilUnidade = vi.fn().mockResolvedValue(true);
        const mockPerfis = [
            {perfil: Perfil.ADMIN, unidade: {sigla: "SEDE", codigo: 1, nome: "Sede"}, siglaUnidade: "SEDE"},
            {perfil: Perfil.SERVIDOR, unidade: {sigla: "FILIAL", codigo: 2, nome: "Filial"}, siglaUnidade: "FILIAL"}
        ];
        perfilStore.perfisUnidades = mockPerfis;

        // Passo 1
        await wrapper.find('[data-testid="inp-login-usuario"]').setValue("123");
        await wrapper.find('[data-testid="inp-login-senha"]').setValue("pass");
        await wrapper.find('form').trigger('submit');

        // Passo 2
        expect(wrapper.find('[data-testid="sec-login-perfil"]').exists()).toBe(true);

        // Selecionar opção. O select stub emite change.
        // O watcher no componente seleciona o primeiro automaticamente.
        // Vamos garantir que o valor está setado.

        // Trigger submit novamente para confirmar seleção
        await wrapper.find('form').trigger('submit');

        expect(perfilStore.selecionarPerfilUnidade).toHaveBeenCalledWith(123, mockPerfis[0]);
        expect(routerPushMock).toHaveBeenCalledWith("/painel");
    });
});
