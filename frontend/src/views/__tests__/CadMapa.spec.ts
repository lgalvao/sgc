/* eslint-disable vue/one-component-per-file */
import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {computed, defineComponent} from "vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import * as unidadesService from "@/services/unidadesService";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {Perfil} from "@/types/tipos";
import CadMapa from "@/views/CadMapa.vue";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

const {pushMock} = vi.hoisted(() => {
    return {pushMock: vi.fn()};
});

vi.mock("vue-router", () => ({
    useRoute: () => ({
        params: {
            codProcesso: "1",
            siglaUnidade: "TESTE",
        },
    }),
    useRouter: () => ({
        push: pushMock,
        currentRoute: {value: {path: "/"}},
    }),
    createRouter: () => ({
        push: pushMock,
        afterEach: vi.fn(),
        beforeEach: vi.fn(),
    }),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
    obterMapaCompleto: vi.fn(),
    obterMapaVisualizacao: vi.fn(),
    disponibilizarMapa: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    adicionarCompetencia: vi.fn(),
    atualizarCompetencia: vi.fn(),
    removerCompetencia: vi.fn(),
    listarAtividades: vi.fn(),
}));

vi.mock("@/services/unidadesService", () => ({
    buscarUnidadePorSigla: vi.fn(),
}));

// Mocks for Async Components
vi.mock("@/components/CriarCompetenciaModal.vue", () => {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const { defineComponent, ref } = require('vue');
    return {
        __esModule: true,
        default: defineComponent({
            props: ['mostrar'],
            emits: ['salvar', 'fechar'],
            setup(props, { emit }) {
                const descricao = ref("");
                return { descricao, emit };
            },
            template: `
                <div v-if="mostrar" data-testid="mdl-criar-competencia">
                    <textarea data-testid="inp-criar-competencia-descricao" v-model="descricao"></textarea>
                    <input type="checkbox" value="101" checked />
                    <button data-testid="btn-criar-competencia-salvar" @click="emit('salvar', { descricao: descricao, atividadesSelecionadas: [101] })"></button>
                    <button data-testid="btn-criar-competencia-cancelar" @click="emit('fechar')"></button>
                </div>
            `
        })
    };
});

vi.mock("@/components/DisponibilizarMapaModal.vue", () => {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const { defineComponent, ref } = require('vue');
    return {
        __esModule: true,
        default: defineComponent({
            props: ['mostrar'],
            emits: ['disponibilizar', 'fechar'],
             setup(props, { emit }) {
                const data = ref("");
                const obs = ref("");
                return { data, obs, emit };
            },
            template: `
                <div v-if="mostrar" data-testid="mdl-disponibilizar-mapa">
                    <input data-testid="inp-disponibilizar-mapa-data" v-model="data" />
                    <input data-testid="inp-disponibilizar-mapa-obs" v-model="obs" />
                    <button data-testid="btn-disponibilizar-mapa-confirmar" @click="emit('disponibilizar', { dataLimite: data, observacoes: obs })"></button>
                </div>
            `
        })
    };
});

vi.mock("@/components/ImpactoMapaModal.vue", () => {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const { defineComponent } = require('vue');
    return {
        __esModule: true,
        default: defineComponent({
            name: "ImpactoMapaModal",
            props: ['mostrar'],
            template: `<div v-if="mostrar">Impacto</div>`
        })
    };
});

const BFormCheckbox = defineComponent({
    name: "BFormCheckbox",
    props: {
        modelValue: {type: [Boolean, Array], default: false},
        value: {type: [String, Number, Boolean, Object], default: null},
    },
    emits: ["update:modelValue"],
    setup(props, {emit}) {
        const isChecked = computed(() => {
            if (Array.isArray(props.modelValue)) {
                return props.modelValue.includes(props.value);
            }
            return props.modelValue;
        });
        const handleChange = (e: any) => {
            let newValue = props.modelValue;
            if (Array.isArray(props.modelValue)) {
                if (e.target.checked) {
                    newValue = [...props.modelValue, props.value];
                } else {
                    newValue = props.modelValue.filter((v: any) => v !== props.value);
                }
            } else {
                newValue = e.target.checked;
            }
            emit("update:modelValue", newValue);
        };
        return {isChecked, handleChange};
    },
    template: `
    <div class="form-check">
      <input type="checkbox" class="form-check-input" :checked="isChecked" @change="handleChange" data-testid="atividade-checkbox" />
      <label class="form-check-label"><slot /></label>
    </div>
  `,
});

const BModalStub = {
    name: "BModal",
    props: ["modelValue", "title"],
    template: `
        <div v-if="modelValue" class="modal-stub" :aria-label="title" data-testid="modal-container">
            <div class="modal-title">{{ title }}</div>
            <slot />
            <div class="modal-footer">
                <slot name="footer" />
            </div>
        </div>
    `,
    emits: ["update:modelValue", "ok", "hidden"],
};

describe("CadMapa.vue", () => {
    const ctx = setupComponentTest();

    const mockAtividades = [
        {codigo: 101, descricao: "Atividade 1", conhecimentos: []},
        {
            codigo: 102,
            descricao: "Atividade 2",
            conhecimentos: [{descricao: "Java"}],
        },
    ];

    const mockCompetencias = [
        {codigo: 10, descricao: "Competencia A", atividadesAssociadas: [101]},
    ];

    const mockMapaCompleto = {
        codigo: 1,
        subprocessoCodigo: 123,
        competencias: [...mockCompetencias],
        situacao: "EM_ANDAMENTO",
    };

    const stubs = {
        BModal: BModalStub,
        BButton: {
            name: "BButton",
            template: '<button type="button"><slot /></button>',
        },
        BContainer: {name: "BContainer", template: "<div><slot /></div>"},
        BCard: {
            name: "BCard",
            template: '<div class="card"><slot /></div>',
        },
        BCardBody: {
            name: "BCardBody",
            template: '<div class="card-body"><slot /></div>',
        },
        BFormInput: {
            name: "BFormInput",
            props: ["modelValue"],
            template:
                '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            emits: ["update:modelValue"],
        },
        BFormTextarea: {
            name: "BFormTextarea",
            props: ["modelValue"],
            template:
                '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            emits: ["update:modelValue"],
        },
        BFormCheckbox: BFormCheckbox,
        BAlert: {
            name: "BAlert",
            template: '<div role="alert"><slot /></div>',
        },
    };

    function createWrapper(customState = {}) {
        vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
            perfilSelecionado: {value: Perfil.CHEFE},
            servidorLogado: {value: null},
            unidadeSelecionada: {value: null},
        } as any);

        const wrapper = mount(CadMapa, {
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: false,
                        initialState: {
                            perfil: {
                                perfilSelecionado: Perfil.CHEFE,
                                unidadeSelecionada: 1,
                                perfisUnidades: [
                                    {
                                        perfil: Perfil.CHEFE,
                                        unidade: {codigo: 1, sigla: "TESTE"},
                                    },
                                ],
                            },
                            unidades: {
                                unidade: {codigo: 1, sigla: "TESTE", nome: "Teste"},
                            },
                            ...customState,
                        },
                    }),
                ],
                stubs,
                directives: {
                    "b-tooltip": {},
                },
            },
            attachTo: document.body,
        });

        const mapasStore = useMapasStore();
        const atividadesStore = useAtividadesStore();
        const subprocessosStore = useSubprocessosStore();
        const unidadesStore = useUnidadesStore();
        unidadesStore.unidade = {codigo: 1, sigla: "TESTE", nome: "Teste"} as any;

        return {
            wrapper,
            mapasStore,
            atividadesStore,
            subprocessosStore,
            unidadesStore,
        };
    }

    beforeEach(() => {
        vi.clearAllMocks();

        vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue({
            codigo: 1,
            sigla: "TESTE",
            nome: "Teste",
        } as any);
        vi.mocked(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).mockResolvedValue({codigo: 123} as any);
        vi.mocked(subprocessoService.buscarSubprocessoDetalhe).mockResolvedValue({
            permissoes: {podeVisualizarImpacto: true},
        } as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                situacao: 'EM_ANDAMENTO',
                situacaoLabel: 'Em Andamento',
                permissoes: { podeVisualizarImpacto: true }
            },
            mapa: mockMapaCompleto,
            atividades: mockAtividades,
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);
        vi.mocked(mapaService.obterMapaCompleto).mockResolvedValue(
            mockMapaCompleto as any,
        );
        vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue({
            competencias: [{atividades: mockAtividades}],
        } as any);
        vi.mocked(subprocessoService.listarAtividades).mockResolvedValue(
            mockAtividades as any,
        );
    });

    afterEach(() => {
        ctx.wrapper?.unmount();
    });

    it("deve carregar dados no mount", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        expect(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).toHaveBeenCalledWith(1, "TESTE");

        expect(wrapper.text()).toContain("TESTE - Teste");
        expect(wrapper.text()).toContain("Competencia A");
    });

    it("deve abrir modal e criar nova competência", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper
            .find('[data-testid="btn-abrir-criar-competencia"]')
            .trigger("click");
        expect(
            wrapper.find('[data-testid="mdl-criar-competencia"]').exists(),
        ).toBe(true);

        const textarea = wrapper.find('[data-testid="inp-criar-competencia-descricao"]');
        await textarea.setValue("Nova Competencia Teste");

        const inputs = wrapper.findAll('input[type="checkbox"]');
        if (inputs.length > 0) {
            await inputs[0].setValue(true);
        }

        vi.mocked(subprocessoService.adicionarCompetencia).mockResolvedValue({
            ...mockMapaCompleto,
        } as any);

        await wrapper
            .find('[data-testid="btn-criar-competencia-salvar"]')
            .trigger("click");

        expect(subprocessoService.adicionarCompetencia).toHaveBeenCalledWith(
            123,
            expect.objectContaining({
                descricao: "Nova Competencia Teste",
                atividadesAssociadas: [101],
            }),
        );
    });

    it("deve editar uma competência existente", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper
            .find('[data-testid="btn-editar-competencia"]')
            .trigger("click");
        expect(
            wrapper.find('[data-testid="mdl-criar-competencia"]').exists(),
        ).toBe(true);

        const textarea = wrapper.find('[data-testid="inp-criar-competencia-descricao"]');
        await textarea.setValue("Competencia A Editada");

        vi.mocked(subprocessoService.atualizarCompetencia).mockResolvedValue({
            ...mockMapaCompleto,
        } as any);

        await wrapper
            .find('[data-testid="btn-criar-competencia-salvar"]')
            .trigger("click");

        expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(
            123,
            expect.objectContaining({
                codigo: 10,
                descricao: "Competencia A Editada",
            }),
        );
    });

    it("deve excluir uma competência", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper
            .find('[data-testid="btn-excluir-competencia"]')
            .trigger("click");

        const deleteModal = wrapper.findComponent(
            '[data-testid="mdl-excluir-competencia"]',
        ) as any;

        expect(deleteModal.exists()).toBe(true);
        expect(deleteModal.props("modelValue")).toBe(true);

        vi.mocked(subprocessoService.removerCompetencia).mockResolvedValue({
            ...mockMapaCompleto,
        } as any);

        await deleteModal.vm.$emit("ok");

        expect(subprocessoService.removerCompetencia).toHaveBeenCalledWith(123, 10);
    });

    it("deve remover atividade associada", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        const removeBtn = wrapper.find(".botao-acao-inline");
        await removeBtn.trigger("click");

        vi.mocked(subprocessoService.atualizarCompetencia).mockResolvedValue({
            ...mockMapaCompleto,
        } as any);

        expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(
            123,
            expect.objectContaining({
                codigo: 10,
                atividadesAssociadas: [],
            }),
        );
    });

    it("deve abrir modal de disponibilizar e enviar", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper
            .find('[data-testid="btn-cad-mapa-disponibilizar"]')
            .trigger("click");

        const modal = wrapper.find('[data-testid="mdl-disponibilizar-mapa"]');
        expect(modal.exists()).toBe(true);

        await wrapper
            .find('[data-testid="inp-disponibilizar-mapa-data"]')
            .setValue("2023-12-31");
        await wrapper
            .find('[data-testid="inp-disponibilizar-mapa-obs"]')
            .setValue("Obs");

        vi.mocked(mapaService.disponibilizarMapa).mockResolvedValue();

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]').trigger("click");
        await flushPromises();

        expect(mapaService.disponibilizarMapa).toHaveBeenCalledWith(123, {
            dataLimite: "2023-12-31",
            observacoes: "Obs",
        });
        expect(pushMock).toHaveBeenCalledWith({name: "Painel"});
    });

    it("deve abrir modal de impacto", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[data-testid="cad-mapa__btn-impactos-mapa"]').trigger("click");

        const impactoModal = wrapper.findComponent({name: "ImpactoMapaModal"});
        expect(impactoModal.props("mostrar")).toBe(true);
    });

    it('deve mostrar o botão "Impacto no mapa" se tiver permissão', async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        expect(wrapper.find('[data-testid="cad-mapa__btn-impactos-mapa"]').exists()).toBe(true);
    });

    it('não deve mostrar o botão "Impacto no mapa" se não tiver permissão', async () => {
        vi.mocked(subprocessoService.buscarSubprocessoDetalhe).mockResolvedValue({
            permissoes: {podeVisualizarImpacto: false},
        } as any);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-impactos-mapa"]').exists()).toBe(false);
    });

    it("não deve buscar dados se subprocesso não encontrado", async () => {
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue(null);
        createWrapper();
        await flushPromises();

        expect(mapaService.obterMapaCompleto).not.toHaveBeenCalled();
    });

    it("deve tratar erro ao criar competência", async () => {
        // Mock o service para rejeitar com formato de AxiosError
        const axiosError = {
            isAxiosError: true,
            response: {
                status: 400,
                data: {
                    message: "Erro API",
                    subErrors: [{message: "Erro API", field: null}],
                }
            }
        };
        vi.mocked(subprocessoService.adicionarCompetencia).mockRejectedValueOnce(axiosError);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger("click");
        await wrapper.find('[data-testid="inp-criar-competencia-descricao"]').setValue("Nova Competencia");

        // Seleciona uma atividade para habilitar o botão de salvar
        const inputs = wrapper.findAll('input[type="checkbox"]');
        if (inputs.length > 0) {
            await inputs[0].setValue(true);
        }

        await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger("click");
        await flushPromises();

        expect((wrapper.vm as any).fieldErrors.generic).toBe("Erro API");
    });

    it("deve tratar erro ao excluir competência", async () => {
        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[data-testid="btn-excluir-competencia"]').trigger("click");

        // Mock o service para rejeitar com formato de AxiosError
        const axiosError = {
            isAxiosError: true,
            response: {
                status: 400,
                data: {
                    message: "Erro API",
                    subErrors: [{message: "Erro API", field: null}],
                }
            }
        };
        vi.mocked(subprocessoService.removerCompetencia).mockRejectedValueOnce(axiosError);

        const deleteModal = wrapper.findComponent('[data-testid="mdl-excluir-competencia"]') as any;
        await deleteModal.vm.$emit("ok");
        await flushPromises();

        expect((wrapper.vm as any).fieldErrors.generic).toBe("Erro API");
    });

    it("deve tratar erro ao disponibilizar mapa", async () => {
        // Mock o service para rejeitar com formato de AxiosError
        const axiosError = {
            isAxiosError: true,
            response: {
                status: 400,
                data: {
                    message: "Erro API",
                    subErrors: [{message: "Erro API", field: null}],
                }
            }
        };
        vi.mocked(mapaService.disponibilizarMapa).mockRejectedValueOnce(axiosError);

        const {wrapper} = createWrapper();
        ctx.wrapper = wrapper;
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-mapa-disponibilizar"]').trigger("click");

        // Preenche a data limite para habilitar o botão
        await wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]').setValue("2024-12-31");

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]').trigger("click");
        await flushPromises();

        expect((wrapper.vm as any).fieldErrors.generic).toBe("Erro API");
    });
});
