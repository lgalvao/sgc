import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import SubprocessoCards from "../SubprocessoCards.vue";
import {createTestingPinia} from "@pinia/testing";
import type {PermissoesSubprocesso, SubprocessoDetalhe} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";

const {pushMock} = vi.hoisted(() => ({ pushMock: vi.fn() }));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: pushMock,
    }),
}));

vi.mock("@/composables/useSubprocessos", () => ({
    useSubprocessos: () => ({
        subprocessoDetalhe: null
    })
}));

describe("SubprocessoCards.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function criarPermissoes(parciais: Partial<PermissoesSubprocesso> = {}): PermissoesSubprocesso {
        return {
            podeEditarCadastro: false,
            podeDisponibilizarCadastro: false,
            podeDevolverCadastro: false,
            podeAceitarCadastro: false,
            podeHomologarCadastro: false,
            podeEditarMapa: false,
            podeDisponibilizarMapa: false,
            podeValidarMapa: false,
            podeApresentarSugestoes: false,
            podeVerSugestoes: false,
            podeDevolverMapa: false,
            podeAceitarMapa: false,
            podeHomologarMapa: false,
            podeVisualizarImpacto: false,
            podeAlterarDataLimite: false,
            podeReabrirCadastro: false,
            podeReabrirRevisao: false,
            podeEnviarLembrete: false,
            mesmaUnidade: false,
            habilitarAcessoCadastro: false,
            habilitarAcessoMapa: false,
            habilitarEditarCadastro: false,
            habilitarDisponibilizarCadastro: false,
            habilitarDevolverCadastro: false,
            habilitarAceitarCadastro: false,
            habilitarHomologarCadastro: false,
            habilitarEditarMapa: false,
            habilitarDisponibilizarMapa: false,
            habilitarValidarMapa: false,
            habilitarApresentarSugestoes: false,
            habilitarDevolverMapa: false,
            habilitarAceitarMapa: false,
            habilitarHomologarMapa: false,
            ...parciais,
        };
    }

    function criarSubprocesso(parciais: Partial<SubprocessoDetalhe> = {}): SubprocessoDetalhe {
        return {
            codigo: 1,
            unidade: {codigo: 1, sigla: "ASSESSORIA_22", nome: "Assessoria 22"},
            titular: null,
            responsavel: null,
            situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
            localizacaoAtual: "ASSESSORIA_22",
            processoDescricao: "Processo",
            dataCriacaoProcesso: "2025-01-01T00:00:00",
            tipoProcesso: TipoProcesso.REVISAO,
            prazoEtapaAtual: "2025-01-01T00:00:00",
            ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
            isEmAndamento: true,
            etapaAtual: 1,
            movimentacoes: [],
            elementosProcesso: [],
            permissoes: criarPermissoes(),
            ...parciais,
        };
    }

    function createWrapper(props: InstanceType<typeof SubprocessoCards>["$props"], access: Partial<PermissoesSubprocesso> = {}) {
        const pinia = createTestingPinia();

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarCadastro: ref(access.podeEditarCadastro ?? false),
            podeEditarMapa: ref(access.podeEditarMapa ?? false),
            habilitarAcessoCadastro: ref(access.habilitarAcessoCadastro ?? false),
            habilitarAcessoMapa: ref(access.habilitarAcessoMapa ?? false),
        } as ReturnType<typeof useAcessoModule.useAcesso>);

        return mount(SubprocessoCards, {
            global: {
                plugins: [pinia],
                stubs: {
                    BCard: { template: '<div @click="$emit(\'click\')" @keydown="$emit(\'keydown\', $event)"><slot /></div>' },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' },
                }
            },
            props
        });
    }

    it("renderiza cards de Mapeamento", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        });

        expect(wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-mapa-desabilitado"]').exists()).toBe(true);
    });

    it("navega para cadastro ao clicar no card se habilitado", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        }, {
            habilitarAcessoCadastro: true,
            podeEditarCadastro: true
        });

        const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            path: "/processo/1/U1/cadastro",
            state: {codSubprocesso: 1}
        }));
    });

    it("navega para visualização de cadastro se não pode editar", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        }, {
            habilitarAcessoCadastro: true,
            podeEditarCadastro: false
        });

        const card = wrapper.find('[data-testid="card-subprocesso-atividades-vis"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            path: "/processo/1/U1/vis-cadastro",
            state: {codSubprocesso: 1}
        }));
    });

    it("prioriza o subprocesso recebido por prop para evitar rota stale na primeira renderização", async () => {
        vi.restoreAllMocks();

        const wrapper = mount(SubprocessoCards, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                subprocessos: {
                            subprocessoDetalhe: criarSubprocesso({
                                codigo: 999,
                                permissoes: criarPermissoes({
                                    podeEditarCadastro: false,
                                    habilitarAcessoCadastro: true,
                                    podeEditarMapa: false,
                                    habilitarAcessoMapa: false,
                                }),
                            })
                        }
                    }
                })],
                stubs: {
                    BCard: { template: '<div @click="$emit(\'click\')" @keydown="$emit(\'keydown\', $event)"><slot /></div>' },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' },
                }
            },
            props: {
                tipoProcesso: TipoProcesso.REVISAO,
                mapa: null,
                codSubprocesso: 1,
                codProcesso: 401,
                siglaUnidade: "ASSESSORIA_22",
                subprocesso: criarSubprocesso({
                    permissoes: criarPermissoes({
                        podeEditarCadastro: true,
                        habilitarAcessoCadastro: true,
                        podeEditarMapa: false,
                        habilitarAcessoMapa: false,
                    })
                })
            }
        });

        expect(wrapper.find('[data-testid="card-subprocesso-atividades"]').exists()).toBe(true);

        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            path: "/processo/401/ASSESSORIA_22/cadastro",
            state: {codSubprocesso: 1}
        }));
    });

    it("navega para mapa ao clicar no card se habilitado", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        }, {
            podeEditarMapa: true,
            habilitarAcessoMapa: true
        });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            path: "/processo/1/U1/mapa",
            state: {codSubprocesso: 1}
        }));
    });

    it("navega para visualização de mapa se habilitado mas não pode editar", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        }, {
            podeEditarMapa: false,
            habilitarAcessoMapa: true
        });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            path: "/processo/1/U1/vis-mapa",
            state: {codSubprocesso: 1}
        }));
    });

    it("renderiza cards de Diagnóstico", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        });

        expect(wrapper.find('[data-testid="card-subprocesso-diagnostico"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-monitoramento"]').exists()).toBe(true);
    });

    it("navega para diagnóstico", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        });

        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger("click");
        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({ name: "AutoavaliacaoDiagnostico" }));
    });

    it("trata tecla Enter/Space nos cards", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        }, {
            habilitarAcessoCadastro: true
        });

        const card = wrapper.find('[data-testid="card-subprocesso-atividades-vis"]');
        await card.trigger("keydown", { key: "Enter" });
        expect(pushMock).toHaveBeenCalled();

        pushMock.mockClear();
        await card.trigger("keydown", { key: " " });
        expect(pushMock).toHaveBeenCalled();

        pushMock.mockClear();
        await card.trigger("keydown", { key: "Escape" });
        expect(pushMock).not.toHaveBeenCalled();
    });

});
