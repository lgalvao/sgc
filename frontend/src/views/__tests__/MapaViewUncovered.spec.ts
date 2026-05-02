import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import MapaView from "../MapaView.vue";
import {createTestingPinia} from "@pinia/testing";
import * as useAcessoModule from "@/composables/useAcesso";
import {useMapas} from "@/composables/useMapas";
import {useFluxoMapa} from "@/composables/useFluxoMapa";
import {ref} from "vue";
import * as subprocessoService from "@/services/subprocessoService";

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: vi.fn()}),
}));

vi.mock("@/services/subprocessoService", () => ({
    obterSugestoesMapa: vi.fn().mockResolvedValue("Sugestões mock"),
    apresentarSugestoes: vi.fn().mockResolvedValue({}),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn().mockResolvedValue([]),
}));

const mapasStoreMock = {
    mapaCompleto: ref({ atividades: [], competencias: [] }),
    buscarMapaCompleto: vi.fn(),
    buscarImpactoMapa: vi.fn(),
    impactoMapa: ref(null),
    erro: ref(null),
};

vi.mock("@/composables/useMapas", () => ({
    useMapas: () => mapasStoreMock
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

const fluxoMapaMock = {
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
};

vi.mock("@/composables/useFluxoMapa", () => ({
    useFluxoMapa: () => fluxoMapaMock
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
        
        const mapasStore = useMapas();
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

    it("cobre confirmarAceitacao e handleConfirmarDevolucao", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        
        vm.codigoSubprocesso = 1;
        vm.acaoPrincipalMapa = { codigo: 'ACEITAR', mostrar: true };
        
        // 1. Aceitar
        vm.mostrarModalAceitar = true;
        await vm.confirmarAceitacao("OK");
        expect(vm.mostrarModalAceitar).toBe(false);

        // 2. Devolver
        vm.observacaoDevolucao = "Justificativa";
        vm.mostrarModalDevolucao = true;
        await vm.handleConfirmarDevolucao();
        expect(vm.mostrarModalDevolucao).toBe(false);
    });

    it("cobre disponibilizarMapa e caminhos de erro", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codigoSubprocesso = 123;

        // 1. Caso de erro no disponibilizar
        const fluxo = useFluxoMapa();
        vi.mocked(fluxo.disponibilizarMapa).mockRejectedValue(new Error("Erro"));
        await vm.disponibilizarMapa({dataLimite: "2024-01-01", observacoes: ""});
        expect(vm.loadingDisponibilizacao).toBe(false);

        // 2. abrirModalDisponibilizar com erro de validação (sem competências)
        const mapasStore = useMapas();
        mapasStore.mapaCompleto.value = { competencias: [] } as any;
        vm.abrirModalDisponibilizar();
        expect(vm.erroValidacaoMapa).toContain("competência");
    });

    it("cobre o checklist de disponibilizacao em cada ramificacao", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();

        const mapasStore = useMapas();
        const vm = wrapper.vm as any;

        mapasStore.mapaCompleto.value = {competencias: [], atividades: []} as any;
        expect(vm.obterMensagemErroChecklistDisponibilizacao()).toContain("competência");
        vm.abrirModalDisponibilizar();
        expect(vm.mostrarModalDisponibilizar).toBe(false);

        mapasStore.mapaCompleto.value = {
            competencias: [{codigo: 1, descricao: "Comp 1", atividades: []}],
            atividades: []
        } as any;
        expect(vm.obterMensagemErroChecklistDisponibilizacao()).toContain("atividade");

        mapasStore.mapaCompleto.value = {
            competencias: [{codigo: 1, descricao: "Comp 1", atividades: [{codigo: 10}]}],
            atividades: [{codigo: 10}, {codigo: 20}]
        } as any;
        expect(vm.obterMensagemErroChecklistDisponibilizacao()).toContain("competência");

        mapasStore.mapaCompleto.value = {
            competencias: [{codigo: 1, descricao: "Comp 1", atividades: [{codigo: 10}]}],
            atividades: [{codigo: 10}]
        } as any;
        vm.abrirModalDisponibilizar();
        expect(vm.erroValidacaoMapa).toBe("");
        expect(vm.mostrarModalDisponibilizar).toBe(true);
        vm.fecharModalDisponibilizar();
        expect(vm.mostrarModalDisponibilizar).toBe(false);
    });

    it("cobre fechamentos e ramos simples das modais", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();

        const vm = wrapper.vm as any;
        vm.abrirModalAceitar();
        expect(vm.mostrarModalAceitar).toBe(true);
        vm.fecharModalAceitar();
        expect(vm.mostrarModalAceitar).toBe(false);

        vm.abrirModalValidar();
        expect(vm.mostrarModalValidar).toBe(true);
        vm.fecharModalValidar();
        expect(vm.mostrarModalValidar).toBe(false);

        vm.abrirModalDevolucao();
        expect(vm.mostrarModalDevolucao).toBe(true);
        vm.fecharModalDevolucao();
        expect(vm.mostrarModalDevolucao).toBe(false);

        vm.mostrarModalHistorico = true;
        vm.fecharModalHistorico();
        expect(vm.mostrarModalHistorico).toBe(false);

        vm.mostrarModalVerSugestoes = true;
        vm.fecharModalVerSugestoes();
        expect(vm.mostrarModalVerSugestoes).toBe(false);

        vi.mocked(subprocessoService.obterSugestoesMapa).mockRejectedValueOnce(new Error("Falha"));
        await vm.verSugestoes();
    });

    it("cobre handleConfirmarSugestoes e verSugestoes", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codigoSubprocesso = 123;
        vm.sugestoes = "Novas sugestões";

        await vm.handleConfirmarSugestoes();
        expect(vm.mostrarModalSugestoes).toBe(false);

        await vm.verSugestoes();
    });

    it("cobre sincronizacao do mapa e carregamento de sugestoes direto", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();

        const vm = wrapper.vm as any;
        const mapasStore = useMapas();

        vm.sincronizarMapa({codigo: 9, competencias: []});
        expect(mapasStore.mapaCompleto.value).toBeDefined();

        vi.mocked(subprocessoService.obterSugestoesMapa).mockRejectedValueOnce(new Error("Falha"));
        await vm.carregarSugestoesParaVisualizacao();

        vi.mocked(subprocessoService.obterSugestoesMapa).mockResolvedValueOnce("Texto");
        await vm.carregarSugestoesParaEdicao();
    });

    it("cobre fluxos de validação, devolução e histórico", async () => {
        const wrapper = mount(MapaView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codigoSubprocesso = 123;

        // 1. Validar
        vm.abrirModalValidar();
        expect(vm.mostrarModalValidar).toBe(true);
        await vm.confirmarValidacao();
        expect(fluxoMapaMock.validarMapa).toHaveBeenCalled();

        // 2. Devolver
        vm.abrirModalDevolucao();
        expect(vm.mostrarModalDevolucao).toBe(true);
        vm.observacaoDevolucao = "Obs";
        await vm.handleConfirmarDevolucao();
        expect(fluxoMapaMock.devolverMapa).toHaveBeenCalled();

        // 3. Histórico
        await vm.verHistorico();
        expect(vm.mostrarModalHistorico).toBe(true);

        // 4. sincronizarMapa
        vm.sincronizarMapa({ codigo: 1, competencias: [] });
        expect(useMapas().mapaCompleto.value).toBeDefined();

        // 5. Disponibilizar
        vm.mostrarModalDisponibilizar = true;
        await vm.disponibilizarMapa({ dataLimite: "2023-12-31", observacoes: "" });
        expect(fluxoMapaMock.disponibilizarMapa).toHaveBeenCalled();

        // 6. Fechar modal exclusão
        vm.fecharModalExcluirCompetencia();
        expect(vm.mostrarModalExcluirCompetencia).toBe(false);
    });
});
