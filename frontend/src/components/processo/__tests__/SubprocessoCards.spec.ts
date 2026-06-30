import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import SubprocessoCards from "../SubprocessoCards.vue";
import {createTestingPinia} from "@pinia/testing";
import type {PermissoesSubprocesso, SubprocessoDetalhe} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/acesso";
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));
vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: pushMock,
    }),
}));

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '242426',
    }),
}));

describe("SubprocessoCards.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function criarPermissoes(parciais: Partial<PermissoesSubprocesso> = {}): PermissoesSubprocesso {
        return {
            ...PERMISSOES_SUBPROCESSO_VAZIAS,
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
            dataFimEtapa1: null,
            ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
            isEmAndamento: true,
            etapaAtual: 1,
            movimentacoes: [],
            elementosProcesso: [],
            permissoes: criarPermissoes(),
            ...parciais,
        };
    }

    function createWrapper(
        props: InstanceType<typeof SubprocessoCards>["$props"],
        access: Partial<PermissoesSubprocesso> = {},
        estadoInicial: Record<string, unknown> = {}
    ) {
        const pinia = createTestingPinia({
            initialState: estadoInicial,
        });

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarCadastro: ref(access.podeEditarCadastro ?? false),
            podeEditarMapa: ref(access.podeEditarMapa ?? false),
            habilitarAcessoCadastro: ref(access.habilitarAcessoCadastro ?? false),
            habilitarAcessoMapa: ref(access.habilitarAcessoMapa ?? false),
            habilitarCardConsenso: ref(access.habilitarCardConsenso ?? false),
            habilitarCardSituacaoCapacitacao: ref(access.habilitarCardSituacaoCapacitacao ?? false),
        } as ReturnType<typeof useAcessoModule.useAcesso>);

        return mount(SubprocessoCards, {
            global: {
                plugins: [pinia],
                stubs: {
                    BCard: {template: '<div v-bind="$attrs" @click="$emit(\'click\')" @keydown="$emit(\'keydown\', $event)"><slot /></div>'},
                    BCardTitle: {template: '<div v-bind="$attrs"><slot /></div>'},
                    BCardText: {template: '<div v-bind="$attrs"><slot /></div>'},
                    BRow: {template: '<div v-bind="$attrs"><slot /></div>'},
                    BCol: {template: '<div v-bind="$attrs"><slot /></div>'},
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

        expect(wrapper.find('[data-testid="card-subprocesso-atividades"]').exists()).toBe(true);
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
            name: "SubprocessoCadastro",
            params: {codProcesso: 1, siglaUnidade: "U1"},
            query: {codSubprocesso: "1"},
        }));
    });

    it("navega para cadastro se não pode editar", async () => {
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

        const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            name: "SubprocessoCadastro",
            params: {codProcesso: 1, siglaUnidade: "U1"},
            query: {codSubprocesso: "1"},
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
                    BCard: {template: '<div v-bind="$attrs" @click="$emit(\'click\')" @keydown="$emit(\'keydown\', $event)"><slot /></div>'},
                    BCardTitle: {template: '<div v-bind="$attrs"><slot /></div>'},
                    BCardText: {template: '<div v-bind="$attrs"><slot /></div>'},
                    BRow: {template: '<div v-bind="$attrs"><slot /></div>'},
                    BCol: {template: '<div v-bind="$attrs"><slot /></div>'},
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
            name: "SubprocessoCadastro",
            params: {codProcesso: 401, siglaUnidade: "ASSESSORIA_22"},
            query: {codSubprocesso: "1"},
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

        const card = wrapper.find('[data-testid="card-subprocesso-mapa"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            name: "SubprocessoMapa",
            params: {codProcesso: 1, siglaUnidade: "U1"},
            query: {codSubprocesso: "1"},
        }));
    });

    it("navega para mapa se habilitado mas não pode editar", async () => {
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

        const card = wrapper.find('[data-testid="card-subprocesso-mapa"]');
        await card.trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            name: "SubprocessoMapa",
            params: {codProcesso: 1, siglaUnidade: "U1"},
            query: {codSubprocesso: "1"},
        }));
    });

    it("renderiza cards de Diagnóstico para servidor", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podePreencherAutoavaliacao: true,
                    podeCriarConsenso: false,
                }),
            }),
        });

        expect(wrapper.find('[data-testid="card-subprocesso-diagnostico"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-consenso"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-situacoes-capacitacao"]').exists()).toBe(false);
    });

    it("renderiza card de situação de capacitação para chefia", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podePreencherAutoavaliacao: false,
                    podeCriarConsenso: true,
                }),
            }),
        }, {
            habilitarCardSituacaoCapacitacao: true,
        });

        expect(wrapper.find('[data-testid="card-subprocesso-diagnostico"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="card-subprocesso-consenso"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="card-subprocesso-situacoes-capacitacao"]').exists()).toBe(true);
    });

    it("navega para diagnóstico", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podePreencherAutoavaliacao: true,
                }),
            }),
        });

        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger("click");
        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({name: "AutoavaliacaoDiagnostico"}));
    });

    it("navega para diagnóstico com Enter e espaço", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podePreencherAutoavaliacao: true,
                }),
            }),
        });

        const cardDiagnostico = wrapper.find('[data-testid="card-subprocesso-diagnostico"]');

        await cardDiagnostico.trigger("keydown", {key: "Enter"});
        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({name: "AutoavaliacaoDiagnostico"}));

        pushMock.mockClear();
        await cardDiagnostico.trigger("keydown", {key: " "});
        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({name: "AutoavaliacaoDiagnostico"}));
    });

    it("não renderiza card de autoavaliação sem permissão", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podePreencherAutoavaliacao: false,
                }),
            }),
        });

        expect(wrapper.find('[data-testid="card-subprocesso-diagnostico"]').exists()).toBe(false);
    });

    it("abre situação de capacitação ao clicar no card da chefia se habilitado", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "ASSESSORIA_12",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: {
                    ...criarPermissoes(),
                    podePreencherAutoavaliacao: false,
                    podeCriarConsenso: true,
                } as any,
            }),
        }, {
            habilitarCardSituacaoCapacitacao: true,
        });

        await wrapper.find('[data-testid="card-subprocesso-situacoes-capacitacao"]').trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            name: "SituacaoCapacitacaoDiagnostico",
            params: {codSubprocesso: 1, siglaUnidade: "ASSESSORIA_12"},
        }));
    });

    it("não abre situação de capacitação ao clicar no card da chefia se desabilitado", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "ASSESSORIA_12",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: {
                    ...criarPermissoes(),
                    podePreencherAutoavaliacao: false,
                    podeCriarConsenso: true,
                } as any,
            }),
        });

        await wrapper.find('[data-testid="card-subprocesso-situacoes-capacitacao-desabilitado"]').trigger("click");

        expect(pushMock).not.toHaveBeenCalled();
    });

    it("desabilita card de situação de capacitação se não houver consenso aprovado", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podeCriarConsenso: true,
                }),
            }),
        });

        expect(wrapper.find('[data-testid="card-subprocesso-situacoes-capacitacao-desabilitado"]').exists()).toBe(true);
    });

    it("habilita card de situação de capacitação se houver consenso aprovado", () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "U1",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: criarPermissoes({
                    podeCriarConsenso: true,
                }),
            }),
        }, {
            habilitarCardSituacaoCapacitacao: true,
        });

        expect(wrapper.find('[data-testid="card-subprocesso-situacoes-capacitacao"]').exists()).toBe(true);
    });

    it("abre consenso do servidor logado quando o usuário não pode criar consenso", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "ASSESSORIA_12",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                titular: {tituloEleitoral: "242426", nome: "Duff"} as any,
                permissoes: {
                    ...criarPermissoes(),
                    podePreencherAutoavaliacao: true,
                    podeCriarConsenso: false,
                } as any,
            }),
        }, {
            habilitarCardConsenso: true,
        }, {
            perfil: {
                usuarioCodigo: '242426',
                usuarioNome: 'Duff',
            }
        });

        await wrapper.find('[data-testid="card-subprocesso-consenso"]').trigger("click");

        expect(pushMock).toHaveBeenCalledWith(expect.objectContaining({
            name: "ConsensoDiagnostico",
            params: expect.objectContaining({
                codSubprocesso: 1,
                siglaUnidade: "ASSESSORIA_12",
                servidorTitulo: '242426',
            }),
        }));
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

        const card = wrapper.find('[data-testid="card-subprocesso-atividades"]');
        await card.trigger("keydown", {key: "Enter"});
        expect(pushMock).toHaveBeenCalled();

        pushMock.mockClear();
        await card.trigger("keydown", {key: " "});
        expect(pushMock).toHaveBeenCalled();

        pushMock.mockClear();
        await card.trigger("keydown", {key: "Escape"});
        expect(pushMock).not.toHaveBeenCalled();
    });

    it("trata tecla Enter/Space e ignora outras no card de consenso", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "ASSESSORIA_12",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                titular: {tituloEleitoral: "242426", nome: "Duff"} as any,
                permissoes: {
                    ...criarPermissoes(),
                    podePreencherAutoavaliacao: true,
                    podeCriarConsenso: false,
                } as any,
            }),
        }, {
            habilitarCardConsenso: true,
        }, {
            perfil: {
                usuarioCodigo: '242426',
                usuarioNome: 'Duff',
            }
        });

        const card = wrapper.find('[data-testid="card-subprocesso-consenso"]');

        await card.trigger("keydown", {key: "Escape"});
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger("keydown", {key: "Enter"});
        expect(pushMock).toHaveBeenCalled();

        pushMock.mockClear();
        await card.trigger("keydown", {key: " "});
        expect(pushMock).toHaveBeenCalled();
    });

    it("não navega para consenso se titulo do servidor não for encontrado", async () => {
        const wrapper = createWrapper({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            codSubprocesso: 1,
            codProcesso: 1,
            siglaUnidade: "ASSESSORIA_12",
            subprocesso: criarSubprocesso({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: {
                    ...criarPermissoes(),
                    podePreencherAutoavaliacao: true,
                    podeCriarConsenso: false,
                } as any,
                titular: null,
            }),
        }, {}, {
            perfil: {
                usuarioCodigo: null,
            }
        });

        await wrapper.find('[data-testid="card-subprocesso-consenso"]').trigger("click");

        expect(pushMock).not.toHaveBeenCalled();
    });

});
