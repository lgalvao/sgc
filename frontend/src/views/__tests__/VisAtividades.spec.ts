import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {nextTick} from "vue";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import VisAtividades from "@/views/VisAtividades.vue";
import {usePerfilStore} from "@/stores/perfil";
import {useAtividadesStore} from "@/stores/atividades";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

const pushMock = vi.fn();

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: pushMock,
    }),
    createRouter: () => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    }),
    createWebHistory: () => ({}),
    createMemoryHistory: () => ({}),
}));

vi.mock("@/services/mapaService", () => ({
    obterMapaVisualizacao: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
    obterDetalhesProcesso: vi.fn(),
}));

vi.mock("@/services/unidadesService", () => ({
    buscarUnidadePorSigla: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

describe("VisAtividades.vue", () => {
    const ctx = setupComponentTest();

    function createWrapper(
        perfil: Perfil,
        situacao: SituacaoSubprocesso,
        tipoProcesso: TipoProcesso = TipoProcesso.REVISAO,
    ) {
        const wrapper = mount(VisAtividades, {
            props: {
                codProcesso: 1,
                sigla: "TESTE",
            },
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: true,
                        initialState: {
                            processos: {
                                processoDetalhe: {
                                    codigo: 1,
                                    tipo: tipoProcesso,
                                    unidades: [
                                        {
                                            codUnidade: 123,
                                            codSubprocesso: 123,
                                            sigla: "TESTE",
                                            situacaoSubprocesso: situacao,
                                        },
                                    ],
                                },
                            },
                            unidades: {
                                unidade: {
                                    codigo: 1,
                                    nome: "Unidade de Teste",
                                    sigla: "TESTE",
                                },
                            },
                        },
                    }),
                ],
            },
        });

        const perfilStore = usePerfilStore();
        perfilStore.perfilSelecionado = perfil;

        return {wrapper};
    }

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        ctx.wrapper?.unmount();
    });

    it('deve mostrar o botão "Impacto no mapa" para GESTOR em CADASTRO_DISPONIBILIZADO', async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        );
        ctx.wrapper = wrapper;
        await flushPromises();
        await nextTick();

        expect(wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa"]').exists()).toBe(true);
    });

    it('deve mostrar o botão "Impacto no mapa" para ADMIN em CADASTRO_DISPONIBILIZADO', async () => {
        const {wrapper} = createWrapper(
            Perfil.ADMIN,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        );
        ctx.wrapper = wrapper;
        await flushPromises();

        expect(wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa"]').exists()).toBe(true);
    });

    it('não deve mostrar o botão "Impacto no mapa" para GESTOR em outra situação', async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
        );
        ctx.wrapper = wrapper;
        await flushPromises();

        expect(wrapper.find('[data-testid="vis-atividades__btn-impactos-mapa"]').exists()).toBe(false);
    });

    it("deve listar atividades e conhecimentos", async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        );
        ctx.wrapper = wrapper;
        const atividadesStore = useAtividadesStore();
        atividadesStore.atividadesPorSubprocesso.set(123, [
            {
                codigo: 1,
                descricao: "Atividade 1",
                conhecimentos: [{id: 10, descricao: "Conhecimento 1"}],
            },
        ]);

        await flushPromises();
        wrapper.vm.$forceUpdate();
        await nextTick();

        expect(wrapper.text()).toContain("Atividade 1");
        expect(wrapper.text()).toContain("Conhecimento 1");
    });

    it("deve abrir e fechar modal de histórico", async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        );
        ctx.wrapper = wrapper;
        await flushPromises();

        const btn = wrapper.findAll("button").find((b: any) => b.text() === "Histórico de análise");
        await btn.trigger("click");
        expect(wrapper.vm.mostrarModalHistoricoAnalise).toBe(true);
    });

    it("deve validar cadastro (Homologar) e redirecionar", async () => {
        const {wrapper} = createWrapper(
            Perfil.ADMIN,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        );
        ctx.wrapper = wrapper;
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.homologarRevisaoCadastro = vi.fn();

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-acao-analisar-principal"]');
        await btn.trigger("click");
        expect(wrapper.vm.mostrarModalValidar).toBe(true);

        const btnConfirm = wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]');
        await btnConfirm.trigger("click");

        expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: 1,
                siglaUnidade: "TESTE",
            },
        });
    });

    it("deve validar cadastro (Aceitar) e redirecionar", async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        );
        ctx.wrapper = wrapper;
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.aceitarRevisaoCadastro = vi.fn();

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-acao-analisar-principal"]');
        await btn.trigger("click");

        const btnConfirm = wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]');
        await btnConfirm.trigger("click");

        expect(subprocessosStore.aceitarRevisaoCadastro).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith({name: "Painel"});
    });

    it("deve devolver cadastro e redirecionar", async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        );
        ctx.wrapper = wrapper;
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.devolverRevisaoCadastro = vi.fn();

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-acao-devolver"]');
        await btn.trigger("click");
        expect(wrapper.vm.mostrarModalDevolver).toBe(true);

        const textarea = wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]');
        await textarea.setValue("Devolvendo");

        const btnConfirm = wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]');
        await btnConfirm.trigger("click");

        expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(123, {
            observacoes: "Devolvendo",
        });
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve chamar aceitarCadastro se não for revisão", async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
            TipoProcesso.MAPEAMENTO,
        );
        ctx.wrapper = wrapper;
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.aceitarCadastro = vi.fn();

        await flushPromises();

        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");

        expect(subprocessosStore.aceitarCadastro).toHaveBeenCalled();
    });

    it("deve encontrar unidade em hierarquia complexa", async () => {
        const {wrapper} = createWrapper(Perfil.GESTOR, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        ctx.wrapper = wrapper;
        const unidadesStore = useUnidadesStore();
        unidadesStore.unidades = [{
            codigo: 99,
            sigla: "ROOT",
            nome: "Root",
            filhas: [{
                codigo: 1,
                sigla: "TESTE",
                nome: "Unidade de Teste",
                filhas: []
            }]
        }] as any;

        await flushPromises();
        await nextTick();
        expect(wrapper.text()).toContain("Unidade de Teste");
    });

    it("deve validar cadastro (Homologar Mapeamento)", async () => {
        const {wrapper} = createWrapper(
            Perfil.ADMIN,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
            TipoProcesso.MAPEAMENTO
        );
        ctx.wrapper = wrapper;
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.homologarCadastro = vi.fn();

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-acao-analisar-principal"]');
        await btn.trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");

        expect(subprocessosStore.homologarCadastro).toHaveBeenCalled();
    });

    it("deve devolver cadastro (Mapeamento)", async () => {
        const {wrapper} = createWrapper(
            Perfil.GESTOR,
            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
            TipoProcesso.MAPEAMENTO
        );
        ctx.wrapper = wrapper;
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.devolverCadastro = vi.fn();

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-acao-devolver"]');
        await btn.trigger("click");
        await wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]').setValue("Obs");
        await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");

        expect(subprocessosStore.devolverCadastro).toHaveBeenCalled();
    });
});
