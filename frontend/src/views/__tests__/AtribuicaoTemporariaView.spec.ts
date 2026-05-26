import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import AtribuicaoTemporariaView from "@/views/AtribuicaoTemporariaView.vue";
import {TEXTOS} from "@/constants/textos";

const {
    mockPush,
    mockNotify,
    mockClear,
    mockObterUnidade,
    mockBuscarAtribuicoes,
    mockCriarAtribuicao,
    mockAtualizarAtribuicao,
    mockRemoverAtribuicao,
    mockRecarregarDiagnostico,
    unidadeQueryMock,
} = vi.hoisted(() => {
    const unidade = {
        codigo: 1,
        sigla: "TESTE",
        nome: "Unidade de Teste",
        tipoResponsabilidade: "TITULAR",
        titular: null,
        responsavel: null,
        filhas: [],
    };

    return {
        mockPush: vi.fn(),
        mockNotify: vi.fn(),
        mockClear: vi.fn(),
        mockObterUnidade: vi.fn().mockResolvedValue(unidade),
        mockBuscarAtribuicoes: vi.fn().mockResolvedValue([]),
        mockCriarAtribuicao: vi.fn().mockResolvedValue(undefined),
        mockAtualizarAtribuicao: vi.fn().mockResolvedValue(undefined),
        mockRemoverAtribuicao: vi.fn().mockResolvedValue(undefined),
        mockRecarregarDiagnostico: vi.fn().mockResolvedValue(undefined),
        unidadeQueryMock: {
            refresh: vi.fn(),
        },
    };
});

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: mockPush,
    }),
}));

vi.mock("@/composables/useUnidadeQuery", () => ({
    useUnidadeQuery: () => unidadeQueryMock,
}));

vi.mock("@/stores/organizacao", () => ({
    useOrganizacaoStore: () => ({
        recarregarDiagnostico: mockRecarregarDiagnostico,
    }),
}));

vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({
        notificacao: {value: null},
        notify: mockNotify,
        clear: mockClear,
    }),
}));

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: () => ({
        mostrarDiagnosticoOrganizacional: {value: true},
    }),
}));

vi.mock("@/services/atribuicaoTemporariaService", () => ({
    criarAtribuicaoTemporaria: mockCriarAtribuicao,
    buscarAtribuicoesTemporariasPorUnidade: mockBuscarAtribuicoes,
    atualizarAtribuicaoTemporaria: mockAtualizarAtribuicao,
    removerAtribuicaoTemporaria: mockRemoverAtribuicao,
}));

describe("AtribuicaoTemporariaView", () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date("2026-05-13T12:00:00"));
        vi.clearAllMocks();
        const unidade = {
            codigo: 1,
            sigla: "TESTE",
            nome: "Unidade de Teste",
            tipoResponsabilidade: "TITULAR",
            titular: null,
            responsavel: null,
            filhas: [],
        };
        mockObterUnidade.mockResolvedValue(unidade);
        unidadeQueryMock.refresh.mockImplementation(async () => ({data: await mockObterUnidade(1)}));
        mockBuscarAtribuicoes.mockResolvedValue([]);
    });

    function mountView() {
        return mount(AtribuicaoTemporariaView, {
            props: {codUnidade: 1},
            global: {
                stubs: {
                    LayoutPadrao: {template: "<div><slot /></div>"},
                    PageHeader: {
                        template: "<div><h1>{{ title }}</h1><slot /><slot name='actions' /></div>",
                        props: ["title"],
                    },
                    BButton: {
                        template: "<button :disabled='disabled' @click=\"$emit('click')\"><slot /></button>",
                        props: ["disabled", "to", "variant"],
                    },
                    BAlert: {
                        template: "<div><slot /></div>",
                        props: ["modelValue"],
                        emits: ["dismissed"],
                    },
                    BForm: {
                        template: "<form @submit.prevent=\"$emit('submit', $event)\"><slot /></form>",
                        emits: ["submit"],
                    },
                    BFormGroup: {template: "<div><slot name='label' /><slot name='description' /><slot /></div>"},
                    BFormInvalidFeedback: {template: "<div><slot /></div>", props: ["state"]},
                    BRow: {template: "<div><slot /></div>"},
                    BCol: {template: "<div><slot /></div>"},
                    InputData: {
                        name: "InputData",
                        template: "<input :value='modelValue' @input=\"$emit('update:modelValue', $event.target.value)\" />",
                        props: ["modelValue", "min", "state", "max"],
                        emits: ["update:modelValue"],
                    },
                    BuscadorUsuarios: {
                        name: "BuscadorUsuarios",
                        template: "<input :value='termo' @input=\"$emit('update:termo', $event.target.value)\" />",
                        props: ["termo", "selecionado", "placeholder", "state"],
                        emits: ["update:termo", "update:selecionado"],
                    },
                    EditorTextoRico: {
                        template: "<div contenteditable data-testid='textarea-justificativa' @input=\"$emit('update:modelValue', $event.target.innerHTML)\" v-html='modelValue'></div>",
                        props: ["modelValue"],
                        emits: ["update:modelValue"],
                    },
                    LoadingButton: {
                        template: "<button :disabled='disabled' @click=\"$emit('click')\">{{ text }}</button>",
                        props: ["disabled", "loading", "loadingText", "text", "variant"],
                        emits: ["click"],
                    },
                    AppAlert: {template: "<div />"},
                    CarregamentoPagina: {template: "<div data-testid='loading'>Carregando...</div>"},
                    ModalConfirmacao: {
                        template: `
                          <div v-if="modelValue">
                            <slot />
                            <button :data-testid="testIdConfirmar" @click="$emit('confirmar')">{{ okTitle }}</button>
                          </div>
                        `,
                        props: ["modelValue", "testIdConfirmar", "okTitle", "loading", "titulo", "variant", "autoClose"],
                        emits: ["update:modelValue", "confirmar"],
                    },
                },
            },
        });
    }

    async function preencherFormulario(wrapper: ReturnType<typeof mount>) {
        await wrapper.findComponent({name: "BuscadorUsuarios"}).vm.$emit("update:selecionado", "999");
        await wrapper.findAll("input")[1].setValue("2026-05-13");
        await wrapper.findAll("input")[2].setValue("2026-05-30");
        const editor = wrapper.find("[data-testid='textarea-justificativa']");
        (editor.element as HTMLDivElement).innerHTML = "<p>Justificativa de teste</p>";
        await editor.trigger("input");
    }

    it("carrega unidade e atribuições ao montar", async () => {
        const wrapper = mountView();
        await flushPromises();

        expect(mockObterUnidade).toHaveBeenCalledWith(1);
        expect(mockBuscarAtribuicoes).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain(TEXTOS.atribuicaoTemporaria.TITULO);
        expect(wrapper.text()).toContain("TESTE");
    });

    it("cria atribuição quando não há AT vigente", async () => {
        const wrapper = mountView();
        await flushPromises();
        await preencherFormulario(wrapper);

        await wrapper.find("[data-testid='cad-atribuicao__btn-salvar-atribuicao']").trigger("click");
        await flushPromises();
        await flushPromises();

        expect(mockCriarAtribuicao).toHaveBeenCalledWith(1, {
            tituloEleitoralUsuario: "999",
            dataInicio: "2026-05-13",
            dataTermino: "2026-05-30",
            justificativa: "<p>Justificativa de teste</p>",
        });
        expect(mockAtualizarAtribuicao).not.toHaveBeenCalled();
        expect(mockNotify).toHaveBeenCalledWith(TEXTOS.atribuicaoTemporaria.SUCESSO, "success");
    });

    it("entra em modo edição quando há AT vigente", async () => {
        mockObterUnidade.mockResolvedValueOnce({
            codigo: 1,
            sigla: "TESTE",
            nome: "Unidade de Teste",
            tipoResponsabilidade: "ATRIBUICAO_TEMPORARIA",
            titular: null,
            responsavel: null,
            filhas: [],
        });
        mockBuscarAtribuicoes.mockResolvedValueOnce([
            {
                codigo: 10,
                unidadeCodigo: 1,
                unidadeSigla: "TESTE",
                usuario: {
                    tituloEleitoral: "999",
                    matricula: "M1",
                    nome: "Servidor Teste",
                    email: "servidor@tre-pe.jus.br",
                    ramal: "1234",
                },
                dataInicio: "2026-05-10T00:00:00",
                dataTermino: "2026-05-30T23:59:59",
                justificativa: "<p>Vigente</p>",
            },
        ]);

        const wrapper = mountView();
        await flushPromises();

        expect(wrapper.text()).toContain(TEXTOS.atribuicaoTemporaria.TITULO);
        expect(wrapper.find("[data-testid='btn-remover-atribuicao']").exists()).toBe(true);
        expect(wrapper.find("[data-testid='textarea-justificativa']").element.innerHTML).toContain("Vigente");
        expect(wrapper.text()).toContain(TEXTOS.atribuicaoTemporaria.BOTAO_REMOVER);
    });

    it("atualiza atribuição vigente", async () => {
        mockObterUnidade.mockResolvedValue({
            codigo: 1,
            sigla: "TESTE",
            nome: "Unidade de Teste",
            tipoResponsabilidade: "ATRIBUICAO_TEMPORARIA",
            titular: null,
            responsavel: null,
            filhas: [],
        });
        mockBuscarAtribuicoes.mockResolvedValue([
            {
                codigo: 10,
                unidadeCodigo: 1,
                unidadeSigla: "TESTE",
                usuario: {
                    tituloEleitoral: "999",
                    matricula: "M1",
                    nome: "Servidor Teste",
                    email: "servidor@tre-pe.jus.br",
                    ramal: "1234",
                },
                dataInicio: "2026-05-10T00:00:00",
                dataTermino: "2026-05-30T23:59:59",
                justificativa: "<p>Vigente</p>",
            },
        ]);

        const wrapper = mountView();
        await flushPromises();

        await wrapper.findAll("input")[2].setValue("2026-06-05");
        const editor = wrapper.find("[data-testid='textarea-justificativa']");
        (editor.element as HTMLDivElement).innerHTML = "<p>Atualizada</p>";
        await editor.trigger("input");

        await wrapper.find("[data-testid='cad-atribuicao__btn-salvar-atribuicao']").trigger("click");
        await flushPromises();
        await flushPromises();

        expect(mockAtualizarAtribuicao).toHaveBeenCalledWith(1, 10, {
            tituloEleitoralUsuario: "999",
            dataInicio: "2026-05-10",
            dataTermino: "2026-06-05",
            justificativa: "<p>Atualizada</p>",
        });
        expect(mockNotify).toHaveBeenCalledWith(TEXTOS.atribuicaoTemporaria.SUCESSO_ATUALIZACAO, "success");
    });

    it("remove atribuição vigente", async () => {
        mockObterUnidade.mockResolvedValue({
            codigo: 1,
            sigla: "TESTE",
            nome: "Unidade de Teste",
            tipoResponsabilidade: "ATRIBUICAO_TEMPORARIA",
            titular: null,
            responsavel: null,
            filhas: [],
        });
        mockBuscarAtribuicoes.mockResolvedValue([
            {
                codigo: 10,
                unidadeCodigo: 1,
                unidadeSigla: "TESTE",
                usuario: {
                    tituloEleitoral: "999",
                    matricula: "M1",
                    nome: "Servidor Teste",
                    email: "servidor@tre-pe.jus.br",
                    ramal: "1234",
                },
                dataInicio: "2026-05-10T00:00:00",
                dataTermino: "2026-05-30T23:59:59",
                justificativa: "<p>Vigente</p>",
            },
        ]);

        const wrapper = mountView();
        await flushPromises();

        await wrapper.find("[data-testid='btn-remover-atribuicao']").trigger("click");
        await wrapper.find("[data-testid='btn-confirmar-remover-atribuicao']").trigger("click");
        await flushPromises();

        expect(mockRemoverAtribuicao).toHaveBeenCalledWith(1, 10);
        expect(mockNotify).toHaveBeenCalledWith(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO, "success");
    });

    it("navega de volta ao cancelar", async () => {
        const wrapper = mountView();
        await flushPromises();

        await wrapper.find("[data-testid='btn-cancelar-atribuicao']").trigger("click");

        expect(mockPush).toHaveBeenCalledWith("/unidade/1");
    });

    it("exibe erro normalizado ao falhar no carregamento inicial", async () => {
        mockObterUnidade.mockRejectedValueOnce(new Error("Falha ao carregar unidade"));

        const wrapper = mountView();
        await flushPromises();

        expect(wrapper.text()).toContain("Falha ao carregar unidade");
    });

    it("exibe erro normalizado ao falhar criação de atribuição", async () => {
        mockCriarAtribuicao.mockRejectedValueOnce(new Error("Falha ao salvar atribuição"));

        const wrapper = mountView();
        await flushPromises();
        await preencherFormulario(wrapper);
        await wrapper.find("[data-testid='cad-atribuicao__btn-salvar-atribuicao']").trigger("click");
        await flushPromises();

        expect(wrapper.text()).toContain("Falha ao salvar atribuição");
    });

    it("exibe erro normalizado ao falhar remoção de atribuição", async () => {
        mockBuscarAtribuicoes.mockResolvedValueOnce([
            {
                codigo: 10,
                unidadeCodigo: 1,
                unidadeSigla: "TESTE",
                usuario: {
                    tituloEleitoral: "999",
                    matricula: "M1",
                    nome: "Servidor Teste",
                    email: "servidor@tre-pe.jus.br",
                    ramal: "1234",
                },
                dataInicio: "2026-05-10T00:00:00",
                dataTermino: "2026-05-30T23:59:59",
                justificativa: "<p>Vigente</p>",
            },
        ]);
        mockRemoverAtribuicao.mockRejectedValueOnce(new Error("Falha ao remover atribuição"));

        const wrapper = mountView();
        await flushPromises();
        await wrapper.find("[data-testid='btn-remover-atribuicao']").trigger("click");
        await wrapper.find("[data-testid='btn-confirmar-remover-atribuicao']").trigger("click");
        await flushPromises();

        expect(wrapper.text()).toContain("Falha ao remover atribuição");
    });

    it("valida formulário antes de salvar", async () => {
        const wrapper = mountView();
        await flushPromises();

        // Form is empty, should fail validation
        await wrapper.find("[data-testid='cad-atribuicao__btn-salvar-atribuicao']").trigger("click");
        await flushPromises();

        expect(mockCriarAtribuicao).not.toHaveBeenCalled();
        const vm = wrapper.vm as any;
        expect(vm.mensagemErroUsuario).toBeTruthy();
        expect(vm.mensagemErroDataInicio).toBeTruthy();
        expect(vm.mensagemErroDataTermino).toBeTruthy();
        expect(vm.mensagemErroJustificativa).toBeTruthy();
    });

    it("recarrega dados ao reativar a view", async () => {
        const wrapper = mountView();
        await flushPromises();
        mockObterUnidade.mockClear();
        mockBuscarAtribuicoes.mockClear();

        const hook = ((wrapper.vm.$ as any).a)?.[0];
        if (hook) {
            await hook.call(wrapper.vm);
        }

        expect(mockObterUnidade).toHaveBeenCalledWith(1);
        expect(mockBuscarAtribuicoes).toHaveBeenCalledWith(1);
    });

    it("previne remoção sem unidade ou atribuição vigente", async () => {
        const wrapper = mountView();
        await flushPromises();
        const vm = wrapper.vm as any;
        
        vm.unidade = null;
        await vm.removerAtribuicao();
        expect(mockRemoverAtribuicao).not.toHaveBeenCalled();

        vm.unidade = { codigo: 1 };
        vm.atribuicoes = []; // No vigente
        await vm.removerAtribuicao();
        expect(mockRemoverAtribuicao).not.toHaveBeenCalled();
    });
});
