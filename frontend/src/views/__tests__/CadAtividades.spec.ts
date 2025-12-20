import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {BFormInput} from "bootstrap-vue-next";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {computed} from "vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as analiseService from "@/services/analiseService";
import * as atividadeService from "@/services/atividadeService";
import * as cadastroService from "@/services/cadastroService";
import * as mapaService from "@/services/mapaService";
import * as processoService from "@/services/processoService";
import * as subprocessoService from "@/services/subprocessoService";
import * as unidadesService from "@/services/unidadesService";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useFeedbackStore} from "@/stores/feedback";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadAtividades from "@/views/CadAtividades.vue";
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

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: vi.fn(),
}));

vi.mock("@/services/atividadeService", () => ({
    criarAtividade: vi.fn(),
    excluirAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    excluirConhecimento: vi.fn(),
    atualizarAtividade: vi.fn(),
    atualizarConhecimento: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
    obterMapaVisualizacao: vi.fn(),
}));

vi.mock("@/services/cadastroService", () => ({
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    importarAtividades: vi.fn(),
    adicionarCompetencia: vi.fn(),
    atualizarCompetencia: vi.fn(),
    removerCompetencia: vi.fn(),
    listarAtividades: vi.fn(),
    obterPermissoes: vi.fn(),
    validarCadastro: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
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

const mockAtividades = [
    {
        codigo: 1,
        descricao: "Atividade 1",
        conhecimentos: [
            {id: 101, descricao: "Conhecimento 1.1"},
            {id: 102, descricao: "Conhecimento 1.2"},
        ],
    },
    {
        codigo: 2,
        descricao: "Atividade 2",
        conhecimentos: [],
    },
];

const mockMapaVisualizacao = (atividades = []) => ({
    subprocessoCodigo: 123,
    competencias: [
        {
            codigo: 10,
            descricao: "Competencia Geral",
            atividades: atividades,
        },
    ],
});

describe("CadAtividades.vue", () => {
    const ctx = setupComponentTest();

    function createWrapper(isRevisao = false, customState = {}) {
        vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
            perfilSelecionado: computed(() => Perfil.CHEFE),
            servidorLogado: computed(() => null),
            unidadeSelecionada: computed(() => null),
        } as any);

        const wrapper = mount(CadAtividades, {
            props: {
                codProcesso: 1,
                sigla: "TESTE",
            },
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: false,
                        initialState: {
                            processos: {
                                processoDetalhe: {
                                    codigo: 1,
                                    tipo: isRevisao
                                        ? TipoProcesso.REVISAO
                                        : TipoProcesso.MAPEAMENTO,
                                    unidades: [
                                        {
                                            codUnidade: 123,
                                            codSubprocesso: 123,
                                            mapaCodigo: 456,
                                            sigla: "TESTE",
                                            situacaoSubprocesso: isRevisao
                                                ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                                                : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
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
                            atividades: {
                                atividadesPorSubprocesso: new Map(),
                            },
                            analises: {
                                analisesPorSubprocesso: new Map(),
                            },
                            ...customState,
                        },
                    }),
                ],
                stubs: {
                    BForm: {template: '<form @submit.prevent><slot /></form>'},
                    ImportarAtividadesModal: true,
                    BModal: {
                        name: "BModal",
                        template: `
                       <div v-if="modelValue" class="b-modal-stub" :aria-label="title">
                         <div class="stub-title">{{ title }}</div>
                         <slot />
                         <slot name="footer" />
                       </div>
                    `,
                        props: ["modelValue", "title"],
                        emits: ["update:modelValue"],
                    },
                },
            },
            attachTo: document.body,
        });

        const atividadesStore = useAtividadesStore();
        const processosStore = useProcessosStore();
        const subprocessosStore = useSubprocessosStore();
        const analisesStore = useAnalisesStore();
        const feedbackStore = useFeedbackStore();

        return {
            wrapper,
            atividadesStore,
            processosStore,
            subprocessosStore,
            analisesStore,
            feedbackStore,
        };
    }

    beforeEach(async () => {
        vi.clearAllMocks();
        window.confirm = vi.fn(() => true);

        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({
            codigo: 123
        } as any);

        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                situacaoLabel: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                permissoes: {
                    podeVerPagina: true,
                    podeEditarMapa: true,
                    podeVisualizarMapa: true,
                    podeDisponibilizarCadastro: true,
                    podeDevolverCadastro: false,
                    podeAceitarCadastro: false,
                    podeVisualizarDiagnostico: false,
                    podeAlterarDataLimite: false,
                    podeVisualizarImpacto: true,
                    podeRealizarAutoavaliacao: false,
                }
            },
            mapa: { codigo: 456, subprocessoCodigo: 123, competencias: [], situacao: "EM_ANDAMENTO" },
            atividadesDisponiveis: [],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            tipo: TipoProcesso.MAPEAMENTO,
            unidades: [
                {
                    codUnidade: 123,
                    codSubprocesso: 123,
                    mapaCodigo: 456,
                    sigla: "TESTE",
                    situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                },
            ],
        } as any);
        vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue({
            codigo: 1,
            sigla: "TESTE",
            nome: "Teste",
        } as any);
        vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
            mockMapaVisualizacao([]) as any,
        );
        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);
        vi.mocked(subprocessoService.listarAtividades).mockResolvedValue([]);
        vi.mocked(subprocessoService.obterPermissoes).mockResolvedValue({
            podeVerPagina: true,
            podeEditarMapa: true,
            podeVisualizarMapa: true,
            podeDisponibilizarCadastro: true,
            podeDevolverCadastro: false,
            podeAceitarCadastro: false,
            podeVisualizarDiagnostico: false,
            podeAlterarDataLimite: false,
            podeVisualizarImpacto: true,
            podeRealizarAutoavaliacao: false,
        });
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({
            valido: true,
            erros: []
        });
    });

    afterEach(() => {
        ctx.wrapper?.unmount();
    });

    it("deve carregar atividades no mount", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        expect(subprocessoService.buscarContextoEdicao).toHaveBeenCalledWith(123, expect.anything(), expect.anything());
    });

    it("deve adicionar uma atividade", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeEditarMapa: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue("Nova Atividade");

        vi.mocked(atividadeService.criarAtividade).mockResolvedValue({
            codigo: 99,
            descricao: "Nova Atividade",
            conhecimentos: [],
        } as any);

        await wrapper
            .find('[data-testid="form-nova-atividade"]')
            .trigger("submit.prevent");

        expect(atividadeService.criarAtividade).toHaveBeenCalledWith(
            {descricao: "Nova Atividade"},
            456,
        );
    });

    it("deve remover uma atividade", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeEditarMapa: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        vi.mocked(atividadeService.excluirAtividade).mockResolvedValue({
            atividade: null,
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                situacaoLabel: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO"
            }
        } as any);

        await wrapper
            .find('[data-testid="btn-remover-atividade"]')
            .trigger("click");
        expect(window.confirm).toHaveBeenCalled();
        expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
    });

    it("deve adicionar um conhecimento", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeEditarMapa: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        const form = wrapper.find('[data-testid="form-novo-conhecimento"]');
        const inputWrapper = form.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue("Novo Conhecimento");

        vi.mocked(atividadeService.criarConhecimento).mockResolvedValue({
            id: 99,
            descricao: "Novo Conhecimento",
        } as any);

        await form.trigger("submit.prevent");

        expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(1, {
            descricao: "Novo Conhecimento",
        });
    });

    it("deve remover um conhecimento", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeEditarMapa: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        vi.mocked(atividadeService.excluirConhecimento).mockResolvedValue({
            atividade: {codigo: 1, descricao: "Atividade 1", conhecimentos: [{id: 102, descricao: "Conhecimento 1.2"}]},
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                situacaoLabel: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO"
            }
        } as any);

        await wrapper
            .find('[data-testid="btn-remover-conhecimento"]')
            .trigger("click");
        expect(window.confirm).toHaveBeenCalled();
        expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 101);
    });

    it("deve disponibilizar o cadastro", async () => {
        const atividadesComConhecimento = mockAtividades.filter(
            (a) => a.conhecimentos.length > 0,
        );
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeDisponibilizarCadastro: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...atividadesComConhecimento],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        vi.mocked(cadastroService.disponibilizarCadastro).mockResolvedValue();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");

        const confirmBtn = wrapper.find(
            '[data-testid="btn-confirmar-disponibilizacao"]',
        );
        await confirmBtn.trigger("click");
        await flushPromises();

        expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve abrir modal de importar atividades", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[title="Importar"]').trigger("click");

        const modal = wrapper.findComponent(ImportarAtividadesModal);
        expect(modal.props("mostrar")).toBe(true);
    });

    it("deve permitir edição inline de atividade", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeEditarMapa: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[data-testid="btn-editar-atividade"]').trigger("click");

        expect(
            wrapper.find('[data-testid="inp-editar-atividade"]').exists(),
        ).toBe(true);

        await wrapper
            .find('[data-testid="inp-editar-atividade"]')
            .setValue("Atividade Editada");

        vi.mocked(atividadeService.atualizarAtividade).mockResolvedValue({
            codigo: 1,
            descricao: "Atividade Editada",
        } as any);

        await wrapper
            .find('[data-testid="btn-salvar-edicao-atividade"]')
            .trigger("click");

        expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(
            1,
            expect.objectContaining({descricao: "Atividade Editada"}),
        );
    });

    it("deve permitir edição inline de conhecimento", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeEditarMapa: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper
            .find('[data-testid="btn-editar-conhecimento"]')
            .trigger("click");

        expect(
            wrapper.find('[data-testid="inp-editar-conhecimento"]').exists(),
        ).toBe(true);

        await wrapper
            .find('[data-testid="inp-editar-conhecimento"]')
            .setValue("Conhecimento Editado");

        vi.mocked(atividadeService.atualizarConhecimento).mockResolvedValue({
            id: 101,
            descricao: "Conhecimento Editado",
        } as any);

        await wrapper
            .find('[data-testid="btn-salvar-edicao-conhecimento"]')
            .trigger("click");

        expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(
            1,
            101,
            expect.objectContaining({descricao: "Conhecimento Editado"}),
        );
    });

    it("deve tratar disponibilização de revisão", async () => {
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            tipo: TipoProcesso.REVISAO,
            unidades: [
                {
                    codUnidade: 123,
                    codSubprocesso: 123,
                    mapaCodigo: 456,
                    sigla: "TESTE",
                    situacaoSubprocesso:
                    SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                },
            ],
        } as any);
        const atividadesComConhecimento = mockAtividades.filter(
            (a) => a.conhecimentos.length > 0,
        );
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeDisponibilizarCadastro: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...atividadesComConhecimento],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const {wrapper} = createWrapper(true);
        ctx.wrapper = wrapper;
        await flushPromises();

        vi.mocked(
            cadastroService.disponibilizarRevisaoCadastro,
        ).mockResolvedValue();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");

        const confirmBtn = wrapper.find(
            '[data-testid="btn-confirmar-disponibilizacao"]',
        );
        await confirmBtn.trigger("click");
        await flushPromises();

        expect(cadastroService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(
            123,
        );
    });

    it("deve abrir modal de histórico de análise se houver análises", async () => {
        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([
            {
                codigo: 1,
                dataHora: "2023-10-10T10:00:00",
                unidadeSigla: "TESTE",
                resultado: "REJEITADO",
                observacoes: "Obs",
            },
        ] as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        const buttons = wrapper.findAll("button");
        const btn = buttons.find((b: any) =>
            b.text().includes("Histórico de análise"),
        );
        expect(btn.exists()).toBe(true);

        await btn.trigger("click");
        await flushPromises();

        expect(wrapper.text()).toContain("Data/Hora");
        expect(wrapper.text()).toContain("REJEITADO");
    });

    it("deve mostrar erros de validação ao tentar disponibilizar se cadastro inválido", async () => {
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: { podeDisponibilizarCadastro: true }
            },
            mapa: { codigo: 456 },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);
        
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({
            valido: false,
            erros: [
                {tipo: 'ATIVIDADE_SEM_CONHECIMENTO', mensagem: 'Atividade sem conhecimento', atividadeId: 1}
            ]
        });

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        expect(wrapper.text()).toContain("Atividade sem conhecimento");

        expect(wrapper.find('[data-testid="btn-confirmar-disponibilizacao"]').exists()).toBe(false);
    });
});
