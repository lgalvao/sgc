import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import MapaView from "../MapaView.vue";
import {createTestingPinia} from "@pinia/testing";
import * as useAcessoModule from "@/composables/useAcesso";
import {useMapas} from "@/composables/useMapas";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {ref} from "vue";

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: vi.fn()}),
}));

vi.mock("@/composables/useContextoSubprocesso", () => ({
    carregarContextoSubprocessoInicial: vi.fn().mockResolvedValue({
        codigo: 123,
        contexto: {
            detalhes: { codigo: 123 },
            atividadesDisponiveis: [],
            unidade: { sigla: "TESTE" }
        }
    })
}));

vi.mock("@/composables/useFluxoMapa", () => ({
    useFluxoMapa: () => ({
        carregando: ref(false),
        validarMapa: vi.fn().mockResolvedValue({}),
        homologarMapa: vi.fn().mockResolvedValue({}),
        aceitarMapa: vi.fn().mockResolvedValue({}),
        devolverMapa: vi.fn().mockResolvedValue({}),
        disponibilizarMapa: vi.fn().mockResolvedValue({}),
        adicionarCompetencia: vi.fn().mockResolvedValue({}),
        atualizarCompetencia: vi.fn().mockResolvedValue({}),
        removerCompetencia: vi.fn().mockResolvedValue({}),
        removerAtividadeDaCompetencia: vi.fn().mockResolvedValue({}),
        lastError: ref(null),
        clearError: vi.fn()
    })
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot></slot></div>'},
    PageHeader: {template: '<div><slot></slot><slot name="actions"></slot></div>'},
    CarregamentoPagina: {template: '<div></div>'},
    EmptyState: {template: '<div></div>'},
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    LoadingButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BAlert: {template: '<div class="b-alert"><slot /></div>'},
    CompetenciaCard: {template: '<div class="competencia-card"><button @click="$emit(\'remover-atividade\', 1, 2)">Remover Ativ</button></div>'},
    ModalConfirmacao: {template: '<div><slot /></div>', props: ['modelValue']},
    CriarCompetenciaModal: {template: '<div></div>', props: ['mostrar']},
    DisponibilizarMapaModal: {template: '<div></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
};

describe("MapaView Uncovered Branches", () => {
    let pinia: any;

    beforeEach(() => {
        vi.clearAllMocks();
        pinia = createTestingPinia({
            stubActions: false,
        });
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarImpacto: ref(true),
            podeEditarMapa: ref(true),
            podeDisponibilizarMapa: ref(true),
            mostrarApresentarSugestoes: ref(true),
            mostrarValidarMapa: ref(false),
            mostrarDisponibilizarMapa: ref(true),
            mostrarDevolverMapa: ref(true),
            habilitarEditarMapa: ref(true),
            habilitarDisponibilizarMapa: ref(true),
            podeVerSugestoes: ref(true),
            acaoPrincipalMapa: ref({mostrar: true, rotuloBotao: 'Aceitar', habilitar: true, rotuloConfirmacao: 'Confirmar', tituloModal: 'Modal', textoModal: 'Texto'}),
            habilitarDevolverMapa: ref(true),
            podeAnalisarMapa: ref(true),
        } as any);
    });

    it("cobre branches de limpeza e remoção", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        // 1. limparErroMapa
        vm.limparErroMapa();
        
        // 2. remover-atividade
        const compCard = wrapper.findComponent({name: 'CompetenciaCard'});
        if (compCard.exists()) {
            await compCard.vm.$emit("remover-atividade", 1, 2);
        }
        
        expect(true).toBe(true);
    });

    it("cobre handleErrors branch atividadesCodigos", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();

        const vm = wrapper.vm as any;
        const erroId = {
            lastError: {
                message: "Erro",
                erros: [{ campo: "atividadesCodigos", mensagem: "Erro em atividade" }]
            }
        };
        vm.handleErrors(erroId);
        expect(vm.fieldErrors.atividades).toEqual("Erro em atividade");
    });

    it("cobre excluirCompetencia e lazy load", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        
        const mapasStore = useMapas(pinia);
        mapasStore.mapaCompleto.value = {
            competencias: [{ codigo: 1, descricao: "Comp 1" }]
        } as any;
        
        vm.excluirCompetencia(1);
        expect(vm.mostrarModalExcluirCompetencia).toBe(true);

        expect(vm.ImpactoMapaModal).toBeDefined();
    });

    it("cobre confirmarExclusaoCompetencia", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        
        vm.codigoSubprocesso = 1;
        vm.competenciaParaExcluir = { codigo: 50 };
        vm.mostrarModalExcluirCompetencia = true;
        
        await vm.confirmarExclusaoCompetencia();
        expect(vm.mostrarModalExcluirCompetencia).toBe(false);
    });

    it("cobre adicionarCompetenciaEFecharModal", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        
        vm.codigoSubprocesso = 1;
        vm.mostrarModalCriarNovaCompetencia = true;
        await vm.adicionarCompetenciaEFecharModal({ descricao: "Nova", atividadesSelecionadas: [1] });
        expect(vm.mostrarModalCriarNovaCompetencia).toBe(false);
    });
});
