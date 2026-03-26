import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import SubprocessoCards from "../SubprocessoCards.vue";
import {createTestingPinia} from "@pinia/testing";
import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";
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

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => ({
        processoDetalhe: { value: { situacao: SituacaoProcesso.EM_ANDAMENTO } }
    })
}));

describe("SubprocessoCards.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function createWrapper(props: any, access: any = {}) {
        const pinia = createTestingPinia();

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarCadastro: ref(access.podeEditarCadastro ?? false),
            podeEditarMapa: ref(access.podeEditarMapa ?? false),
            habilitarAcessoCadastro: ref(access.habilitarAcessoCadastro ?? false),
            habilitarAcessoMapa: ref(access.habilitarAcessoMapa ?? false),
        } as any);

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

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({ name: "SubprocessoCadastro" }));
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

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({ name: "SubprocessoVisCadastro" }));
    });

    it("prioriza o subprocesso recebido por prop para evitar rota stale na primeira renderização", async () => {
        vi.restoreAllMocks();

        const wrapper = mount(SubprocessoCards, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        subprocessos: {
                            subprocessoDetalhe: {
                                codigo: 999,
                                permissoes: {
                                    podeEditarCadastro: false,
                                    habilitarAcessoCadastro: true,
                                    podeEditarMapa: false,
                                    habilitarAcessoMapa: false,
                                }
                            }
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
                subprocesso: {
                    codigo: 1,
                    unidade: {codigo: 1, sigla: "ASSESSORIA_22", nome: "Assessoria 22"},
                    titular: {codigo: 1, nome: "Titular"} as any,
                    responsavel: {codigo: 1, usuario: {nome: "Responsavel"}} as any,
                    situacao: "REVISAO_CADASTRO_EM_ANDAMENTO" as any,
                    localizacaoAtual: "ASSESSORIA_22",
                    processoDescricao: "Processo",
                    dataCriacaoProcesso: "2025-01-01T00:00:00",
                    tipoProcesso: TipoProcesso.REVISAO,
                    prazoEtapaAtual: "2025-01-01T00:00:00",
                    isEmAndamento: true,
                    etapaAtual: 1,
                    movimentacoes: [],
                    elementosProcesso: [],
                    permissoes: {
                        podeEditarCadastro: true,
                        habilitarAcessoCadastro: true,
                        podeEditarMapa: false,
                        habilitarAcessoMapa: false,
                    }
                } as any
            }
        });

        expect(wrapper.find('[data-testid="card-subprocesso-atividades"]').exists()).toBe(true);

        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            name: "SubprocessoCadastro",
            params: {codProcesso: 401, siglaUnidade: "ASSESSORIA_22"}
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

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({ name: "SubprocessoMapa" }));
    });

    it("navega para visualização de mapa se habilitado mas não pode editar", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            mapa: { codigo: 1 } as any,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1"
        }, {
            podeEditarMapa: false,
            habilitarAcessoMapa: true
        });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({ name: "SubprocessoVisMapa" }));
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
