import {createTestingPinia} from "@pinia/testing";
import {mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {type Mapa, type SubprocessoPermissoes, TipoProcesso, type Unidade,} from "@/types/tipos";
import SubprocessoCards from "../SubprocessoCards.vue";

const pushMock = vi.fn();

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: pushMock,
    }),
}));

describe("SubprocessoCards.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const defaultPermissoes: SubprocessoPermissoes = {
        podeVerPagina: true,
        podeEditarMapa: true,
        podeVisualizarMapa: true,
        podeDisponibilizarCadastro: true,
        podeDevolverCadastro: true,
        podeAceitarCadastro: true,
        podeVisualizarDiagnostico: true,
        podeAlterarDataLimite: true,
        podeVisualizarImpacto: true,
        podeRealizarAutoavaliacao: true,
    };

    const createWrapper = (propsOverride: any = {}) => {
        return mount(SubprocessoCards, {
            props: {
                permissoes: defaultPermissoes,
                codProcesso: 1,
                siglaUnidade: "TEST",
                codSubprocesso: 10,
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null,
                ...propsOverride,
            },
            global: {
                plugins: [createTestingPinia({stubActions: false})],
            },
        });
    };

    const mockMapa: Mapa = {
        codigo: 1,
        descricao: "mapa de teste",
        unidade: {sigla: "UNID_TESTE"} as Unidade,
        situacao: "em_andamento",
        codProcesso: 1,
        competencias: [],
        dataCriacao: new Date().toISOString(),
        dataDisponibilizacao: null,
        dataFinalizacao: null,
    };

    describe("Lógica de Navegação", () => {
        it("deve navegar para SubprocessoCadastro ao clicar no card de atividades (edição)", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null,
                situacao: "Mapa disponibilizado",
                permissoes: {...defaultPermissoes, podeEditarMapa: true},
            });

            const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "SubprocessoCadastro",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
        });

        it("deve navegar para SubprocessoVisCadastro ao clicar no card de atividades (visualização)", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null,
                situacao: "Mapa disponibilizado",
                permissoes: {
                    ...defaultPermissoes,
                    podeEditarMapa: false,
                    podeVisualizarMapa: true,
                },
            });

            const card = wrapper.find('[data-testid="card-subprocesso-atividades-vis"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "SubprocessoVisCadastro",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
        });

        it("deve navegar para SubprocessoMapa ao clicar no card de mapa", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: mockMapa,
                situacao: "Mapa disponibilizado",
                permissoes: {...defaultPermissoes, podeVisualizarMapa: true},
            });

            const card = wrapper.find('[data-testid="card-subprocesso-mapa"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "SubprocessoMapa",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
        });

        it("deve navegar para AutoavaliacaoDiagnostico ao clicar no card de diagnostico", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                mapa: null,
                situacao: "Em andamento",
                permissoes: {...defaultPermissoes, podeVisualizarDiagnostico: true},
                codSubprocesso: 10,
            });

            const card = wrapper.find('[data-testid="card-subprocesso-diagnostico"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "AutoavaliacaoDiagnostico",
                params: {
                    codSubprocesso: 10,
                    siglaUnidade: "TEST",
                },
            });
        });

        it("deve navegar para OcupacoesCriticasDiagnostico ao clicar no card de ocupações", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                mapa: null,
                situacao: "Em andamento",
                codSubprocesso: 10,
            });

            const card = wrapper.find('[data-testid="card-subprocesso-ocupacoes"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "OcupacoesCriticasDiagnostico",
                params: {
                    codSubprocesso: 10,
                    siglaUnidade: "TEST",
                },
            });
        });
    });

    describe("Lógica de Renderização", () => {
        it("não deve renderizar card de mapa se não puder visualizar", () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: mockMapa,
                situacao: "Mapa disponibilizado",
                permissoes: {
                    ...defaultPermissoes,
                    podeVisualizarMapa: false,
                    podeEditarMapa: false,
                },
            });

            expect(wrapper.find('[data-testid="card-subprocesso-mapa"]').exists()).toBe(false);
        });
    });

    describe("Acessibilidade", () => {
        it("cards devem ter role='button' e tabindex='0'", () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null,
                situacao: "Mapa disponibilizado",
                permissoes: {...defaultPermissoes, podeEditarMapa: true},
            });
            const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');

            expect(card.attributes("role")).toBe("button");
            expect(card.attributes("tabindex")).toBe("0");
        });

        it("deve navegar ao pressionar Enter", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null,
                situacao: "Mapa disponibilizado",
                permissoes: {...defaultPermissoes, podeEditarMapa: true},
            });
            const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');

            await card.trigger("keydown.enter");

            expect(pushMock).toHaveBeenCalledWith({
                name: "SubprocessoCadastro",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
        });

        it("deve navegar ao pressionar Espaço", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null,
                situacao: "Mapa disponibilizado",
                permissoes: {...defaultPermissoes, podeEditarMapa: true},
            });
            const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');

            await card.trigger("keydown.space");

            expect(pushMock).toHaveBeenCalledWith({
                name: "SubprocessoCadastro",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
        });

        it("deve desabilitar navegação e foco se mapa não existir (card desabilitado)", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: null, // Sem mapa
                situacao: "Mapa disponibilizado",
                permissoes: {...defaultPermissoes, podeEditarMapa: true},
            });
            const card = wrapper.find('[data-testid="card-subprocesso-mapa"]');

            expect(card.attributes("aria-disabled")).toBe("true");
            expect(card.attributes("tabindex")).toBe("-1");

            await card.trigger("click");
            expect(pushMock).not.toHaveBeenCalled();
        });
    });
});
