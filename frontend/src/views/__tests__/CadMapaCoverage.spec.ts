import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import CadMapa from "@/views/CadMapa.vue";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useMapasStore} from "@/stores/mapas";
import * as subprocessoService from "@/services/subprocessoService";
import {createTestingPinia} from "@pinia/testing";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";

// Mocks
const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: { params: { codProcesso: "1", siglaUnidade: "TESTE" } }
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: mocks.push,
        back: vi.fn(),
    }),
    useRoute: () => mocks.mockRoute,
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mocks.push,
        resolve: vi.fn(),
        currentRoute: { value: mocks.mockRoute }
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

vi.mock("@/services/processoService");

describe("CadMapaCoverage.spec.ts", () => {
    let subprocessosStore: any;
    let mapasStore: any;

    const createWrapper = (initialState: any = {}) => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: { perfilSelecionado: "CHEFE" },
                processos: { processoDetalhe: { codigo: 1, tipo: TipoProcesso.MAPEAMENTO, unidades: [] } },
                subprocessos: {
                    subprocessoDetalhe: {
                        codigo: 123,
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                        permissoes: {
                            podeEditarMapa: true,
                            podeVisualizarImpacto: true
                        }
                    }
                },
                mapas: {
                    mapaCompleto: {
                        codigo: 456,
                        competencias: [
                            { codigo: 1, descricao: "Competencia 1", atividadesAssociadas: [10, 11] }
                        ]
                    },
                    impactoMapa: null,
                    lastError: null
                },
                unidades: { unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" } },
                atividades: { atividadesPorSubprocesso: new Map() },
                ...initialState
            },
            stubActions: true
        });

        subprocessosStore = useSubprocessosStore(pinia);
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);

        mapasStore = useMapasStore(pinia);
        mapasStore.atualizarCompetencia.mockResolvedValue({});

        return mount(CadMapa, {
            global: {
                plugins: [pinia],
                stubs: {
                    CompetenciaCard: {
                        name: 'CompetenciaCard',
                        template: '<div><button @click="$emit(\'remover-atividade\', 10)">Remover Atv</button></div>',
                        props: ['competencia', 'atividades', 'pode-editar'],
                        emits: ['remover-atividade', 'editar', 'excluir']
                    },
                    CriarCompetenciaModal: true,
                    DisponibilizarMapaModal: true,
                    ModalConfirmacao: true,
                    ImpactoMapaModal: true,
                    BContainer: { template: '<div><slot /></div>' },
                    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
                    BAlert: { template: '<div v-if="modelValue"><slot /></div>', props: ['modelValue'] },
                    EmptyState: { template: '<div><slot /></div>' },
                    LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' }
                },
            },
        });
    };

    it("deve remover atividade associada", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const card = wrapper.findComponent({ name: 'CompetenciaCard' });
        // Emit removing activity ID 10 from competence ID 1 (which is passed as prop to card)
        // Wait, the stub template emits 10. But the handler expects (competenciaId, atividadeId).
        // The parent binds @remover-atividade="removerAtividadeAssociada".
        // CompetenciaCard likely emits (competenciaCodigo, atividadeCodigo) or just atividadeCodigo if it knows the competence?
        // Let's check usage in CadMapa.vue:
        // <CompetenciaCard ... @remover-atividade="removerAtividadeAssociada" />
        // And `removerAtividadeAssociada(competenciaId: number, atividadeId: number)`

        // So CompetenciaCard must emit both arguments.
        // In my stub I emitted 1 argument.
        // I should emit 2 arguments from the stub to simulate child component behavior.

        await card.vm.$emit('remover-atividade', 1, 10);
        await flushPromises();

        expect(mapasStore.atualizarCompetencia).toHaveBeenCalledWith(
            123,
            expect.objectContaining({
                codigo: 1,
                atividadesAssociadas: [11] // 10 removed
            })
        );
    });

    it("deve mostrar detalhes do erro no alert", async () => {
        const wrapper = createWrapper({
            mapas: {
                lastError: { message: "Erro Principal", details: "Detalhes do erro" }
            }
        });
        await flushPromises();

        expect(wrapper.text()).toContain("Detalhes do erro");
    });

    it("não deve buscar contexto se subprocesso não encontrado", async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                unidades: { unidade: { sigla: "TESTE" } },
                subprocessos: { subprocessoDetalhe: null }
            },
            stubActions: true
        });
        const subprocessosStore = useSubprocessosStore(pinia);
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(null);

        mount(CadMapa, {
            global: {
                plugins: [pinia],
                stubs: {
                    CompetenciaCard: true, CriarCompetenciaModal: true, DisponibilizarMapaModal: true,
                    ModalConfirmacao: true, ImpactoMapaModal: true, BContainer: true,
                    BButton: true, BAlert: true, EmptyState: true, LoadingButton: true
                }
            }
        });
        await flushPromises();

        expect(subprocessosStore.buscarContextoEdicao).not.toHaveBeenCalled();
    });
});
