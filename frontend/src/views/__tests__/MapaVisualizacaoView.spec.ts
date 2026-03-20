import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as usePerfilModule from "@/composables/usePerfil";
import * as analiseService from "@/services/analiseService";
import * as mapaService from "@/services/mapaService";
import * as unidadeService from "@/services/unidadeService";
import MapaVisualizacaoView from "../MapaVisualizacaoView.vue";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
    obterMapaVisualizacao: vi.fn(),
}));

vi.mock("@/services/unidadeService", () => ({
    buscarUnidadePorSigla: vi.fn(),
}));

const processosMock = {
    processoDetalhe: ref<any>(null),
    buscarProcessoDetalhe: vi.fn(),
    apresentarSugestoes: vi.fn(),
    validarMapa: vi.fn(),
    aceitarValidacao: vi.fn(),
    homologarValidacao: vi.fn(),
    devolverValidacao: vi.fn(),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => processosMock
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {
        props: ['disabled'],
        template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
    },
    BCard: {template: '<div><slot /></div>'},
    BBadge: {template: '<span><slot /></span>'},
    BFormGroup: {template: '<div><label><slot name="label" /></label><slot /></div>'},
    BFormTextarea: {
        props: ['modelValue'],
        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
    },
    EmptyState: {template: '<div></div>'},
    ModalConfirmacao: {
        props: ['modelValue', 'okDisabled'],
        template: '<div v-if="modelValue"><slot /> <button data-testid="btn-confirmar" :disabled="okDisabled" @click="$emit(\'confirmar\')">Confirmar</button></div>'
    },
    ModalPadrao: {
        props: ['modelValue', 'testIdCancelar'],
        template: '<div v-if="modelValue"><slot /> <button :data-testid="testIdCancelar" @click="$emit(\'fechar\')">Fechar</button></div>'
    },
    AceitarMapaModal: {
        template: '<div v-if="mostrarModal"><button data-testid="btn-confirmar-aceite" @click="$emit(\'confirmar-aceitacao\', \'Obs aceite\')">Confirmar</button></div>',
        props: ['mostrarModal']
    },
    HistoricoAnaliseModal: {template: '<div v-if="mostrar"></div>', props: ['mostrar']},
};

describe("MapaVisualizacaoView.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        processosMock.processoDetalhe.value = {
            codigo: 1,
            unidades: [{sigla: "TESTE", codSubprocesso: 123}]
        };
        vi.mocked(unidadeService.buscarUnidadePorSigla).mockResolvedValue({sigla: "TESTE", nome: "Unidade Teste"} as any);
        vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue({
            codigo: 100,
            competencias: [
                {
                    codigo: 1,
                    descricao: "Competencia 1",
                    atividades: [{codigo: 1, descricao: "Ativ 1", conhecimentos: [{codigo: 1, descricao: "Conhec 1"}]}]
                }
            ]
        } as any);
    });

    function createWrapper(accessOverrides = {}) {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeValidarMapa: ref(true),
            podeAceitarMapa: ref(true),
            podeDevolverMapa: ref(true),
            podeHomologarMapa: ref(true),
            podeVerSugestoes: ref(true),
            habilitarValidarMapa: ref(true),
            habilitarAceitarMapa: ref(true),
            habilitarDevolverMapa: ref(true),
            habilitarHomologarMapa: ref(true),
            ...accessOverrides
        } as any);

        vi.spyOn(usePerfilModule, 'usePerfil').mockReturnValue({
            perfilSelecionado: ref('CHEFE'),
            isAdmin: ref(false),
        } as any);

        return mount(MapaVisualizacaoView, {
            global: {
                plugins: [createTestingPinia({
                    stubActions: true,
                    initialState: {}
                })],
                stubs
            }
        });
    }

    it("renderiza corretamente os dados do mapa", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        expect(wrapper.text()).toContain("TESTE");
        expect(wrapper.text()).toContain("Competencia 1");
        expect(wrapper.text()).toContain("Ativ 1");
        expect(wrapper.text()).toContain("Conhec 1");
    });

    it("abre modal e confirma sugestões", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (processosMock.apresentarSugestoes as any).mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-mapa-sugestoes"]').trigger("click");
        await wrapper.find('[data-testid="inp-sugestoes-mapa-texto"]').setValue("Minhas sugestões");
        await wrapper.find('[data-testid="btn-confirmar"]').trigger("click");
        await flushPromises();

        expect(processosMock.apresentarSugestoes).toHaveBeenCalledWith(123, {sugestoes: "Minhas sugestões"});
        expect(pushMock).toHaveBeenCalledWith({name: "Painel"});
    });

    it("deve manter confirmar desabilitado enquanto as sugestões estiverem vazias", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (processosMock.apresentarSugestoes as any).mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-mapa-sugestoes"]').trigger("click");
        expect(wrapper.find('[data-testid="btn-confirmar"]').attributes('disabled')).toBeDefined();

        await wrapper.find('[data-testid="btn-confirmar"]').trigger("click");
        await flushPromises();

        expect(processosMock.apresentarSugestoes).not.toHaveBeenCalled();
    });

    it("abre modal e confirma validação", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (processosMock.validarMapa as any).mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-mapa-validar"]').trigger("click");
        await wrapper.find('[data-testid="btn-confirmar"]').trigger("click");
        await flushPromises();

        expect(processosMock.validarMapa).toHaveBeenCalledWith(123);
        expect(pushMock).toHaveBeenCalledWith({name: "Painel"});
    });

    it("abre modal e confirma aceite", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (processosMock.aceitarValidacao as any).mockResolvedValue(true);
        // Garante que não é homologação para testar aceitarValidacao
        (wrapper.vm as any).podeHomologarMapa = false;

        await wrapper.find('[data-testid="btn-mapa-homologar-aceite"]').trigger("click");
        await wrapper.find('[data-testid="btn-confirmar-aceite"]').trigger("click");
        await flushPromises();

        expect(processosMock.aceitarValidacao).toHaveBeenCalledWith(123, {texto: 'Obs aceite'});
    });

    it("abre modal e confirma devolução", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (processosMock.devolverValidacao as any).mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-mapa-devolver"]').trigger("click");
        await wrapper.find('[data-testid="inp-devolucao-mapa-obs"]').setValue("Justificativa");
        await wrapper.find('[data-testid="btn-confirmar"]').trigger("click");
        await flushPromises();

        expect(processosMock.devolverValidacao).toHaveBeenCalledWith(123, {justificativa: "Justificativa"});
    });

    it("carrega histórico ao clicar no botão", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);

        await wrapper.find('[data-testid="btn-mapa-historico-gestor"]').trigger("click");
        await flushPromises();

        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);
    });

    it("deve manter devolucao, aceite e validacao visiveis, porem desabilitadas, fora da localizacao permitida", async () => {
        const wrapper = createWrapper({
            habilitarValidarMapa: ref(false),
            habilitarAceitarMapa: ref(false),
            habilitarDevolverMapa: ref(false),
            habilitarHomologarMapa: ref(false),
        });
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-mapa-devolver"]').attributes('disabled')).toBeDefined();
        expect(wrapper.find('[data-testid="btn-mapa-homologar-aceite"]').attributes('disabled')).toBeDefined();
        expect(wrapper.find('[data-testid="btn-mapa-validar"]').attributes('disabled')).toBeDefined();
    });
});
