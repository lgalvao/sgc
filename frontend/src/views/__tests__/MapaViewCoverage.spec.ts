import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as subprocessoService from "@/services/subprocessoService";
import {useMapasStore} from "@/stores/mapas";
import MapaView from "../MapaView.vue";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="$attrs.disabled" @click="$emit(\'click\')"><slot /></button>'},
    BAlert: {template: '<div><slot /></div>', props: ['modelValue']},
    EmptyState: {template: '<div><slot /></div>'},
    LoadingButton: {
        props: ['loading', 'disabled'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">{{ loading ? "Loading..." : "Action" }}</button>'
    },
    CompetenciaCard: {template: '<div></div>', props: ['competencia', 'atividades', 'podeEditar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"></div>', props: ['modelValue']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    CriarCompetenciaModal: {template: '<div></div>', props: ['mostrar']},
    DisponibilizarMapaModal: {template: '<div></div>', props: ['mostrar']},
};

describe("MapaView coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue(123 as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            atividadesDisponiveis: [],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    function createWrapper(initialMapaCompleto: any = {competencias: []}) {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarImpacto: ref(true),
            podeEditarMapa: ref(true),
            podeDisponibilizarMapa: ref(true),
        } as any);

        return mount(MapaView, {
            global: {
                plugins: [createTestingPinia({
                    stubActions: true,
                    initialState: {
                        mapas: {
                            mapaCompleto: initialMapaCompleto,
                            impactoMapa: null,
                            erro: null
                        }
                    }
                })],
                stubs
            }
        });
    }

    it("cobre fluxos de erro e modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const store = useMapasStore();
        store.buscarImpactoMapa = vi.fn().mockResolvedValue(null);
        store.adicionarCompetencia = vi.fn().mockRejectedValue(new Error("Erro"));
        store.atualizarCompetencia = vi.fn().mockResolvedValue(null);
        store.removerCompetencia = vi.fn().mockRejectedValue(new Error("Erro"));
        store.disponibilizarMapa = vi.fn().mockRejectedValue(new Error("Erro"));

        (wrapper.vm as any).codSubprocesso = 123;
        await wrapper.vm.$nextTick();

        // Cobre erro no store
        (store.erro as any) = "Erro no Mapa";
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("Erro no Mapa");

        // Cobre abrir modal impacto
        await (wrapper.vm as any).abrirModalImpacto();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);
        expect(store.buscarImpactoMapa).toHaveBeenCalledWith(123);

        // Cobre fechar modal impacto
        (wrapper.vm as any).fecharModalImpacto();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(false);

        // Cobre abrir modal criar limpo
        (wrapper.vm as any).abrirModalCriarLimpo();
        expect((wrapper.vm as any).mostrarModalCriarNovaCompetencia).toBe(true);
        expect((wrapper.vm as any).competenciaSendoEditada).toBeNull();

        // Cobre salvar competencia com erro
        (store as any).lastError = {message: "Erro ao salvar", details: {}};
        await (wrapper.vm as any).adicionarCompetenciaEFecharModal({descricao: "C1", atividadesSelecionadas: [1]});
        expect((wrapper.vm as any).fieldErrors.generic).toBeDefined();

        // Cobre excluir competencia
        (store.mapaCompleto as any) = {competencias: [{codigo: 1, descricao: "C1"}]};
        (wrapper.vm as any).excluirCompetencia(1);
        expect((wrapper.vm as any).mostrarModalExcluirCompetencia).toBe(true);
        expect((wrapper.vm as any).competenciaParaExcluir.codigo).toBe(1);

        // Cobre confirmar exclusão com erro
        (store as any).lastError = {message: "Erro ao excluir"};
        await (wrapper.vm as any).confirmarExclusaoCompetencia();
        expect((wrapper.vm as any).fieldErrors.generic).toBeDefined();

        // Cobre disponibilizar com erro
        (store as any).lastError = {message: "Erro ao disponibilizar"};
        await (wrapper.vm as any).disponibilizarMapa({dataLimite: "2025-12-31", observacoes: "obs"});
        expect((wrapper.vm as any).fieldErrors.generic).toBeDefined();

        // Cobre remover atividade associada
        (wrapper.vm as any).removerAtividadeAssociada(1, 10);
        expect(store.atualizarCompetencia).toHaveBeenCalled();
    });

    it("mantém o botão disponibilizar desabilitado enquanto houver atividade sem competência", async () => {
        const wrapper = createWrapper({
            competencias: [
                {
                    codigo: 10,
                    descricao: "Competência 1",
                    atividades: [{codigo: 1, descricao: "Atividade 1"}]
                }
            ]
        });
        const store = useMapasStore();
        (wrapper.vm as any).atividades = [
            {codigo: 1, descricao: "Atividade 1"},
            {codigo: 2, descricao: "Atividade 2"}
        ];
        store.$patch({
            mapaCompleto: {
                competencias: [
                    {
                        codigo: 10,
                        descricao: "Competência 1",
                        atividades: [{codigo: 1, descricao: "Atividade 1"}]
                    }
                ]
            }
        });

        await flushPromises();

        expect((wrapper.vm as any).atividadesSemCompetencia).toHaveLength(1);
        expect(wrapper.find('[data-testid="btn-cad-mapa-disponibilizar"]').attributes("disabled")).toBeDefined();
    });
});
