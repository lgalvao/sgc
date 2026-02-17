import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import VisAtividades from "@/views/processo/AtividadesVisualizacaoView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useAnalisesStore} from "@/stores/analises";
import {useMapasStore} from "@/stores/mapas";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {useRouter} from "vue-router";
import {obterDetalhesProcesso} from "@/services/processoService";
import {buscarSubprocessoDetalhe} from "@/services/subprocessoService";

// Hoist mocks to avoid ReferenceError
const { mockApiClient } = vi.hoisted(() => {
    const client = {
        get: vi.fn().mockResolvedValue({ data: {} }),
        post: vi.fn().mockResolvedValue({ data: {} }),
        put: vi.fn().mockResolvedValue({ data: {} }),
        delete: vi.fn().mockResolvedValue({ data: {} }),
    };
    return { mockApiClient: client };
});

// Mock router
vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    useRoute: vi.fn(),
    createRouter: vi.fn(() => ({
        push: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Mock axios with default export
vi.mock("@/axios-setup", () => ({
    apiClient: mockApiClient,
    default: mockApiClient,
}));

// Mock services
vi.mock("@/services/cadastroService", () => ({
    aceitarRevisaoCadastro: vi.fn().mockResolvedValue(true),
    homologarRevisaoCadastro: vi.fn().mockResolvedValue(true),
    devolverRevisaoCadastro: vi.fn().mockResolvedValue(true),
    aceitarCadastro: vi.fn().mockResolvedValue(true),
    homologarCadastro: vi.fn().mockResolvedValue(true),
    devolverCadastro: vi.fn().mockResolvedValue(true),
}));

// Fix: Mock obterDetalhesProcesso to return minimal valid data matching the test scenario
vi.mock("@/services/processoService", () => ({
    buscarProcessoDetalhe: vi.fn(),
    obterDetalhesProcesso: vi.fn().mockResolvedValue({
        codigo: 1,
        tipo: 'REVISAO',
        unidades: [{
            sigla: "U1",
            codSubprocesso: 10,
            situacaoSubprocesso: "REVISAO_CADASTRO_DISPONIBILIZADA"
        }]
    }),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoDetalhe: vi.fn().mockImplementation((cod) => Promise.resolve({
        codigo: cod,
        permissoes: {
            podeVerPagina: true,
            podeEditarMapa: true,
            podeVisualizarMapa: true,
            podeVisualizarImpacto: true,
            podeHomologarCadastro: true,
            podeAceitarCadastro: true,
            podeDevolverCadastro: true,
        }
    })),
}));

vi.mock("@/services/mapaService", () => ({
    verificarImpactosMapa: vi.fn().mockResolvedValue({ temImpactos: false, impactos: [] }),
}));

vi.mock("@/stores/atividades", () => ({
    useAtividadesStore: vi.fn(() => ({
        buscarAtividadesParaSubprocesso: vi.fn(),
        obterAtividadesPorSubprocesso: vi.fn().mockReturnValue([]),
    }))
}));

// Mock child components
const BModalStub = {
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['modelValue', 'title'],
    emits: ['update:modelValue']
};
const BButtonStub = {
    template: '<button @click="$emit(\'click\')"><slot></slot></button>',
    props: ['variant']
};

describe("VisAtividades.vue", () => {
    let subprocessosStore: any;
    let pushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        pushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: pushMock,
        });
    });

    const mountOptions = (initialState: any = {}) => ({
        props: {
            codProcesso: "1",
            sigla: "U1"
        },
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        processos: {
                            processoDetalhe: {
                                codigo: 1,
                                tipo: TipoProcesso.REVISAO,
                                unidades: [
                                    {
                                        sigla: "U1",
                                        nome: "Unidade 1",
                                        codSubprocesso: 10,
                                        situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                                        permissoes: { podeVisualizarImpacto: true }
                                    }
                                ]
                            }
                        },
                        perfil: {
                            perfilSelecionado: Perfil.ADMIN,
                        },
                        atividades: {
                            // ... activities mock if needed
                        },
                        subprocessos: {
                            subprocessoDetalhe: {
                                codigo: 10,
                                permissoes: { podeHomologarCadastro: true, podeVisualizarImpacto: true }
                            }
                        },
                        ...initialState,
                    },
                    stubActions: false, // Allow actions to call services
                }),
            ],
            stubs: {
                BModal: BModalStub,
                BButton: BButtonStub,
                BFormTextarea: { template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>', props: ['modelValue'] },
                ImpactoMapaModal: { template: '<div></div>' },
                HistoricoAnaliseModal: { template: '<div></div>' },
                BContainer: { template: '<div><slot/></div>' },
                BCard: { template: '<div><slot/></div>' },
                BCardBody: { template: '<div><slot/></div>' }
            },
        },
    });

    it("deve validar cadastro (Homologar) e redirecionar", async () => {
        const wrapper = mount(VisAtividades, mountOptions({
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    tipo: TipoProcesso.REVISAO,
                    unidades: [
                        {
                            sigla: "U1",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                            permissoes: { podeHomologarCadastro: true, podeVisualizarImpacto: true }
                        }
                    ]
                }
            }
        }));
        subprocessosStore = useSubprocessosStore();

        // Mock success response
        vi.spyOn(subprocessosStore, "homologarRevisaoCadastro").mockResolvedValue(true);

        // Open modal
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");

        // Confirm
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();

        expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalledWith(
            10, // codSubprocesso
            { observacoes: "" }
        );
        expect(pushMock).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1", // props are string
                siglaUnidade: "U1"
            }
        });
    });

    it("deve validar cadastro (Aceitar) e redirecionar", async () => {
        const wrapper = mount(VisAtividades, mountOptions({
            perfil: { perfilSelecionado: Perfil.GESTOR },
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    tipo: TipoProcesso.REVISAO,
                    unidades: [
                        {
                            sigla: "U1",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                            permissoes: { podeAceitarCadastro: true }
                        }
                    ]
                }
            },
            subprocessos: {
                subprocessoDetalhe: {
                    codigo: 10,
                    permissoes: { podeAceitarCadastro: true, podeHomologarCadastro: false }
                }
            }
        }));
        subprocessosStore = useSubprocessosStore();
        vi.spyOn(subprocessosStore, "aceitarRevisaoCadastro").mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();

        expect(subprocessosStore.aceitarRevisaoCadastro).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("deve devolver cadastro e redirecionar", async () => {
        const wrapper = mount(VisAtividades, mountOptions());
        subprocessosStore = useSubprocessosStore();
        vi.spyOn(subprocessosStore, "devolverRevisaoCadastro").mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");

        // Fill observation
        const textarea = wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]');
        await textarea.setValue("Devolvendo");

        await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
        await flushPromises();

        expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(
            10,
            { observacoes: "Devolvendo" }
        );
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve abrir modal de impacto ao clicar no botão", async () => {
        const wrapper = mount(VisAtividades, mountOptions());
        // Force button visibility
        const btn = wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa"]');
        await btn.trigger("click");

        await flushPromises();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);
    });

    it("deve abrir modal de histórico de análise", async () => {
        const wrapper = mount(VisAtividades, mountOptions());
        await flushPromises(); // Ensure initial load

        const analisesStore = useAnalisesStore();
        // Since createTestingPinia already mocks actions, we can just assert or configure the mock
        // access the existing spy
        (analisesStore.buscarAnalisesCadastro as any).mockResolvedValue([]);

        await wrapper.find('[data-testid="btn-vis-atividades-historico"]').trigger("click");
        await flushPromises();

        expect(analisesStore.buscarAnalisesCadastro).toHaveBeenCalledWith(10);
        expect((wrapper.vm as any).mostrarModalHistoricoAnalise).toBe(true);
    });

    describe("Fluxo Mapeamento (Não Revisão)", () => {
        const mountOptionsMapeamento = () => mountOptions({
            processos: {
                processoDetalhe: {
                    codigo: 2,
                    tipo: TipoProcesso.MAPEAMENTO,
                    unidades: [
                        {
                            sigla: "U1",
                            codSubprocesso: 20,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        }
                    ]
                }
            },
            subprocessos: {
                subprocessoDetalhe: {
                    codigo: 20,
                    permissoes: { podeVisualizarImpacto: true, podeHomologarCadastro: true }
                }
            }
        });

        beforeEach(() => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 2,
                tipo: 'MAPEAMENTO',
                unidades: [{
                    sigla: "U1",
                    codSubprocesso: 20,
                    situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                }]
            });
        });

        it("deve homologar cadastro de mapeamento", async () => {
            const wrapper = mount(VisAtividades, mountOptionsMapeamento());
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "homologarCadastro").mockResolvedValue(true);

            await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
            await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
            await flushPromises();

            expect(subprocessosStore.homologarCadastro).toHaveBeenCalledWith(20, { observacoes: "" });
            expect(pushMock).toHaveBeenCalledWith({
                name: "Subprocesso",
                params: { codProcesso: "1", siglaUnidade: "U1" }
            });
        });

        it("deve aceitar cadastro de mapeamento", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 2,
                tipo: 'MAPEAMENTO',
                unidades: [{
                    sigla: "U1",
                    codSubprocesso: 20,
                    situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                }]
            });

             const wrapper = mount(VisAtividades, mountOptions({
                perfil: { perfilSelecionado: Perfil.GESTOR },
                processos: {
                    processoDetalhe: {
                        codigo: 2,
                        tipo: 'MAPEAMENTO',
                        unidades: [{
                            sigla: "U1",
                            codSubprocesso: 20,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                            permissoes: { podeAceitarCadastro: true }
                        }]
                    }
                },
                subprocessos: {
                    subprocessoDetalhe: {
                        codigo: 20,
                        permissoes: { podeAceitarCadastro: true, podeHomologarCadastro: false }
                    }
                }
            }));
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "aceitarCadastro").mockResolvedValue(true);

            await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
            await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
            await flushPromises();

            expect(subprocessosStore.aceitarCadastro).toHaveBeenCalled();
            expect(pushMock).toHaveBeenCalledWith({ name: "Painel" });
        });

        it("deve devolver cadastro de mapeamento", async () => {
            const wrapper = mount(VisAtividades, mountOptionsMapeamento());
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "devolverCadastro").mockResolvedValue(true);

            await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");
            await wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]').setValue("Devolvendo map");
            await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
            await flushPromises();

            expect(subprocessosStore.devolverCadastro).toHaveBeenCalledWith(20, { observacoes: "Devolvendo map" });
            expect(pushMock).toHaveBeenCalledWith("/painel");
        });

        it("deve mostrar texto correto no botão de impacto", async () => {
            const wrapper = mount(VisAtividades, mountOptionsMapeamento());
            const btn = wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa"]');
            expect(btn.text()).toContain("Impacto no mapa");
        });

        it("não deve redirecionar se devolução de mapeamento falhar", async () => {
            const wrapper = mount(VisAtividades, mountOptionsMapeamento());
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "devolverCadastro").mockResolvedValue(false);

            await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");
            await wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]').setValue("Obs");
            await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
            await flushPromises();

            expect(subprocessosStore.devolverCadastro).toHaveBeenCalled();
            expect(pushMock).not.toHaveBeenCalled();
        });
    });

    describe("Tratamento de Erros", () => {
        beforeEach(() => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 1,
                tipo: 'REVISAO',
                unidades: [{
                    sigla: "U1",
                    codSubprocesso: 10,
                    situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                    permissoes: { podeHomologarCadastro: true }
                }]
            });
        });

        it("não deve redirecionar se validação falhar", async () => {
            const wrapper = mount(VisAtividades, mountOptions());
            subprocessosStore = useSubprocessosStore();
            // Spy on both possible actions
            const homologarSpy = vi.spyOn(subprocessosStore, "homologarRevisaoCadastro").mockResolvedValue(false);
            const aceitarSpy = vi.spyOn(subprocessosStore, "aceitarRevisaoCadastro").mockResolvedValue(false);

            await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
            await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
            await flushPromises();

            // Ensure one of them was called
            expect(homologarSpy.mock.calls.length + aceitarSpy.mock.calls.length).toBeGreaterThan(0);
            expect(pushMock).not.toHaveBeenCalled();
        });

        it("não deve redirecionar se devolução falhar", async () => {
             const wrapper = mount(VisAtividades, mountOptions());
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "devolverRevisaoCadastro").mockResolvedValue(false);

            await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");
            await wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]').setValue("Obs");
            await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
            await flushPromises();

            expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalled();
            expect(pushMock).not.toHaveBeenCalled();
        });
    });

    describe("Cobertura de Casos de Borda", () => {
        it("não deve buscar impacto se codSubprocesso não existir", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 1,
                tipo: TipoProcesso.REVISAO,
                unidades: []
            });
             const wrapper = mount(VisAtividades, mountOptions({
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.REVISAO,
                        unidades: [] // Sem unidade correspondente
                    }
                }
            }));
            const mapasStore = useMapasStore();

            // Chama o método diretamente pois o botão estaria oculto
            await (wrapper.vm as any).abrirModalImpacto();

            expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);
            expect(mapasStore.buscarImpactoMapa).not.toHaveBeenCalled();
        });

        it("não deve confirmar validação se codSubprocesso não existir", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 1,
                tipo: TipoProcesso.REVISAO,
                unidades: []
            });
            const wrapper = mount(VisAtividades, mountOptions({
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.REVISAO,
                        unidades: []
                    }
                }
            }));
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "homologarRevisaoCadastro");

            // Abre modal diretamente
            (wrapper.vm as any).mostrarModalValidar = true;
            await wrapper.vm.$nextTick();

            // Chama confirmar
            await (wrapper.vm as any).confirmarValidacao();

            expect(subprocessosStore.homologarRevisaoCadastro).not.toHaveBeenCalled();
        });

        it("não deve confirmar devolução se codSubprocesso não existir", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 1,
                tipo: TipoProcesso.REVISAO,
                unidades: []
            });
            const wrapper = mount(VisAtividades, mountOptions({
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.REVISAO,
                        unidades: []
                    }
                }
            }));
            subprocessosStore = useSubprocessosStore();
            vi.spyOn(subprocessosStore, "devolverRevisaoCadastro");

            (wrapper.vm as any).mostrarModalDevolver = true;
            await wrapper.vm.$nextTick();

            await (wrapper.vm as any).confirmarDevolucao();

            expect(subprocessosStore.devolverRevisaoCadastro).not.toHaveBeenCalled();
        });

        it("deve cobrir branches de isHomologacao (ADMIN e Mapeamento Homologado)", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 2,
                tipo: TipoProcesso.MAPEAMENTO,
                unidades: [{
                    sigla: "U1",
                    codSubprocesso: 20,
                    situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                    permissoes: { podeHomologarCadastro: true }
                }]
            });
            const wrapper = mount(VisAtividades, mountOptions({
                perfil: { perfilSelecionado: Perfil.ADMIN },
                processos: {
                    processoDetalhe: {
                        codigo: 2,
                        tipo: TipoProcesso.MAPEAMENTO,
                        unidades: [{
                            sigla: "U1",
                            codSubprocesso: 20,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                            permissoes: { podeHomologarCadastro: true }
                        }]
                    }
                }
            }));
            await flushPromises();
            // isHomologacao deve ser true
            expect((wrapper.vm as any).isHomologacao).toBe(true);
        });

         it("deve cobrir branches de isHomologacao (ADMIN e Revisao Homologada)", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 1,
                tipo: TipoProcesso.REVISAO,
                unidades: [{
                    sigla: "U1",
                    codSubprocesso: 10,
                    situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                    permissoes: { podeHomologarCadastro: true }
                }]
            });
            const wrapper = mount(VisAtividades, mountOptions({
                perfil: { perfilSelecionado: Perfil.ADMIN },
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.REVISAO,
                        unidades: [{
                            sigla: "U1",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                            permissoes: { podeHomologarCadastro: true }
                        }]
                    }
                }
            }));
            await flushPromises();
            expect((wrapper.vm as any).isHomologacao).toBe(true);
        });

        it("isHomologacao deve ser false se permissão for false", async () => {
            (buscarSubprocessoDetalhe as any).mockImplementation((cod: number) => Promise.resolve({
                codigo: cod,
                permissoes: { podeHomologarCadastro: false }
            }));

            const wrapper = mount(VisAtividades, mountOptions({
                perfil: { perfilSelecionado: Perfil.GESTOR },
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.REVISAO,
                        unidades: [{
                            sigla: "U1",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                            permissoes: { podeAceitarCadastro: true }
                        }]
                    }
                }
            }));
            await flushPromises();
            expect((wrapper.vm as any).isHomologacao).toBe(false);
        });

        it("podeVerImpacto deve ser false se subprocesso undefined", async () => {
            (obterDetalhesProcesso as any).mockResolvedValue({
                codigo: 1,
                tipo: TipoProcesso.REVISAO,
                unidades: []
            });
             const wrapper = mount(VisAtividades, mountOptions({
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.REVISAO,
                        unidades: []
                    }
                },
                subprocessos: { subprocessoDetalhe: null }
            }));
            expect((wrapper.vm as any).podeVerImpacto).toBe(false);
        });
    });
});
