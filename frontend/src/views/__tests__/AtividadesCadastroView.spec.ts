import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {reactive, ref} from "vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import {useMapas} from "@/composables/useMapas";
import * as useProcessosModule from "@/composables/useProcessos";
import * as subprocessoService from "@/services/subprocessoService";
import * as analiseService from "@/services/analiseService";
import * as atividadeService from "@/services/atividadeService";
import CadastroView from "@/views/CadastroView.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import * as useAcessoModule from '@/composables/useAcesso';
import {Perfil} from "@/types/tipos";

vi.mock("@/utils/logger", () => ({
    default: {
        error: vi.fn(),
        warn: vi.fn(),
        info: vi.fn(),
        debug: vi.fn(),
    }
}));

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));
const subprocessosMock = reactive({
    subprocessoDetalhe: null as any,
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null as any,
    clearError: vi.fn(),
});

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/composables/usePerfil", () => ({usePerfil: vi.fn()}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    validarCadastro: vi.fn(),
    disponibilizarCadastro: vi.fn(),
    listarAtividades: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/atividadeService", () => ({
    excluirAtividade: vi.fn(),
    atualizarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    atualizarConhecimento: vi.fn(),
    excluirConhecimento: vi.fn(),
}));

vi.mock("@/composables/useSubprocessos", () => ({useSubprocessos: () => subprocessosMock}));
vi.mock("@/composables/useFluxoSubprocesso", () => ({useFluxoSubprocesso: vi.fn()}));
vi.mock("@/composables/useProcessos", () => ({useProcessos: vi.fn()}));
const mockAtividadeForm = {
    novaAtividade: ref(""),
    loadingAdicionar: ref(false),
    adicionarAtividade: vi.fn(),
};

vi.mock("@/composables/useAtividadeForm", () => ({
    useAtividadeForm: vi.fn(() => mockAtividadeForm)
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    LoadingButton: {
        props: {
            disabled: {
                type: Boolean,
                default: false,
            }
        },
        template: '<button :data-testid="$attrs[\'data-testid\']" v-bind="disabled ? { disabled: true } : {}" @click="$emit(\'click\')"><slot /></button>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
    BFormCheckbox: {
        props: ['modelValue'],
        template: '<input type="checkbox" :data-testid="$attrs[\'data-testid\']" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />'
    },
    BDropdown: {template: '<div><slot /></div>'},
    BDropdownItem: {template: '<div :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></div>'},
    BAlert: {template: '<div><slot /><button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>', props: ['modelValue']},
    AppAlert: {template: '<div><button data-testid="btn-dismiss-app-alert" @click="$emit(\'dismissed\')">x</button></div>', props: ['message', 'variant', 'dismissible']},
    ErrorAlert: {template: '<div></div>'},
    CadAtividadeForm: {
        name: 'CadAtividadeForm',
        template: '<div data-testid="cad-atividade-form"></div>', 
        props: ['modelValue'],
        expose: ['inputRef'],
        setup(_: any, {emit: __}: any) {
            return {
                inputRef: {
                    $el: {
                        focus: vi.fn()
                    }
                }
            }
        }
    },
    EmptyState: {template: '<div><slot /></div>'},
    AtividadeItem: {
        name: 'AtividadeItem',
        template: '<div data-testid="atividade-item"></div>', 
        props: ['atividade', 'pode-editar', 'erro-validacao']
    },
    ImportarAtividadesModal: {template: '<div></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div v-if="mostrar" data-testid="modal-impacto"></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {
        template: '<div v-if="mostrar" data-testid="modal-confirmacao">Confirmacao <button data-testid="btn-confirmar-disponibilizacao" @click="$emit(\'confirmar\')">Confirmar</button></div>',
        props: ['mostrar'],
        emits: ['confirmar', 'fechar']
    },
    HistoricoAnaliseModal: {template: '<div v-if="mostrar" data-testid="modal-historico"></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"></div>', props: ['modelValue']},
};

function createWrapper(customState = {}, accessOverrides = {}) {
    vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
        podeEditarCadastro: ref(true),
        podeDisponibilizarCadastro: ref(true),
        podeVisualizarImpacto: ref(true),
        habilitarEditarCadastro: ref(true),
        habilitarDisponibilizarCadastro: ref(true),
        mesmaUnidade: ref(true),
        habilitarAcessoCadastro: ref(true),
        ...accessOverrides
    } as any);

    vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
        perfilSelecionado: ref(Perfil.CHEFE),
        isChefe: ref(true),
    } as any);

    const wrapper = mount(CadastroView, {
        global: {
            plugins: [createTestingPinia({
                createSpy: vi.fn,
                stubActions: false,
                initialState: {
                    perfil: {
                        perfilSelecionado: Perfil.CHEFE,
                    },
                    subprocessos: {
                        subprocessoDetalhe: {
                            codigo: 123,
                            unidade: {sigla: "TESTE"},
                            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                            tipoProcesso: "MAPEAMENTO"
                        }
                    },
                    mapas: {
                        mapaCompleto: {codigo: 100}
                    },
                    ...customState,
                }
            })],
            stubs
        },
        props: {
            codProcesso: "1",
            sigla: "TESTE"
        }
    });

    const mapas = useMapas();
    mapas.mapaCompleto.value = {codigo: 100} as any;
    mapas.impactoMapa.value = null;
    mapas.erro.value = null;

    (wrapper.vm as any).codSubprocesso = 123;
    (wrapper.vm as any).unidade = {sigla: "TESTE", nome: "Teste"};

    return wrapper;
}

describe("CadastroView.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        subprocessosMock.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: {}
        };
        subprocessosMock.lastError = null;
        subprocessosMock.buscarContextoEdicao = vi.fn();
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn();
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            validarCadastro: vi.fn().mockResolvedValue({valido: true}),
            disponibilizarCadastro: vi.fn().mockResolvedValue(true),
            disponibilizarRevisaoCadastro: vi.fn().mockResolvedValue(true),
        } as any);
        vi.mocked(useProcessosModule.useProcessos).mockReturnValue({
            processoDetalhe: ref({
                codigo: 1,
                unidades: [
                    {sigla: "TESTE", codSubprocesso: 123, filhas: []}
                ]
            }),
            buscarProcessoDetalhe: vi.fn().mockResolvedValue(undefined),
        } as any);
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            detalhes: {
                codigo: 123,
                situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                unidade: {sigla: "TESTE"}
            },
            mapa: {codigo: 100},
            atividadesDisponiveis: [{codigo: 1, descricao: "Ativ 1", conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]}],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    it("renderiza corretamente", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        expect(wrapper.text()).toContain("Atividades e conhecimentos");
    });

    it("chama validação antes de disponibilizar", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (wrapper.vm as any).atividades = [{codigo: 1, conhecimentos: [{codigo: 1}]}];
        await wrapper.vm.$nextTick();
        const subprocessosStore = subprocessosMock as any;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as any;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: {}
        } as any;

        await (wrapper.vm as any).disponibilizarCadastro();

        expect(fluxoSubprocesso.validarCadastro).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).mostrarModalConfirmacao).toBe(true);
    });

    it("confirma disponibilização e redireciona", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        (wrapper.vm as any).atividades = [{codigo: 1, conhecimentos: [{codigo: 1}]}];
        await wrapper.vm.$nextTick();
        const subprocessosStore = subprocessosMock as any;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as any;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: {}
        } as any;

        await (wrapper.vm as any).disponibilizarCadastro();
        await flushPromises();

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModal);
        modal.vm.$emit('confirmar');
        await flushPromises();

        expect(fluxoSubprocesso.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("carrega histórico ao abrir modal", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);

        await wrapper.find('[data-testid="btn-cad-atividades-historico"]').trigger("click");
        await flushPromises();

        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);
        expect(wrapper.findComponent(HistoricoAnaliseModal).exists()).toBe(true);
    });

    it("carrega impacto ao abrir modal", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const mapas = useMapas();
        mapas.buscarImpactoMapa = vi.fn().mockResolvedValue(null);

        await wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa-edicao"]').trigger("click");
        await flushPromises();

        expect(mapas.buscarImpactoMapa).toHaveBeenCalledWith(123);
        expect(wrapper.findComponent(ImpactoMapaModal).exists()).toBe(true);
    });

    it("desabilita botão disponibilizar se houver atividades sem conhecimentos", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        // Atividade sem conhecimentos
        (wrapper.vm as any).atividades = [{
            codigo: 1,
            descricao: "Atividade 1",
            conhecimentos: []
        }];

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeDefined();
    });

    it("habilita botão disponibilizar se todas atividades tiverem conhecimentos", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        // Atividade com conhecimentos
        (wrapper.vm as any).atividades = [{
            codigo: 1,
            descricao: "Atividade 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("em revisão, mantém botão desabilitado sem mudanças e checkbox desmarcada", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        const subprocessosStore = subprocessosMock as any;
        subprocessosStore.subprocessoDetalhe = {
            ...subprocessosStore.subprocessoDetalhe,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
        await flushPromises();

        const checkbox = wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]');
        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(checkbox.exists()).toBe(true);
        expect(btn.attributes('disabled')).toBeDefined();
    });

    it("em revisão, habilita botão ao marcar checkbox sem mudanças", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        const subprocessosStore = subprocessosMock as any;
        subprocessosStore.subprocessoDetalhe = {
            ...subprocessosStore.subprocessoDetalhe,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
        vm.disponibilizacaoSemMudancas = true;
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("em revisão, habilita botão quando houver alteração no cadastro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        const subprocessosStore = subprocessosMock as any;
        subprocessosStore.subprocessoDetalhe = {
            ...subprocessosStore.subprocessoDetalhe,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
        };
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1 alterada",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("mantem botão visivel e desabilitado quando chefe pode editar mas ainda nao pode disponibilizar", async () => {
        const wrapper = createWrapper({}, {
            podeEditarCadastro: ref(true),
            podeDisponibilizarCadastro: ref(false),
            podeVisualizarImpacto: ref(true),
        });
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.attributes('disabled')).toBeDefined();
    });

    it("recarrega contexto completo apos importar atividades", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const subprocessosStore = subprocessosMock as any;
        subprocessosStore.buscarContextoEdicao = vi.fn().mockResolvedValue({
            detalhes: {
                subprocesso: {
                    codigo: 123,
                    situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                    unidade: {sigla: "TESTE"},
                },
                permissoes: {
                    podeEditarCadastro: true,
                    podeDisponibilizarCadastro: true,
                },
            },
            atividadesDisponiveis: [{codigo: 2, descricao: "Atividade importada", conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]}],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);

        await (wrapper.vm as any).handleImportAtividades();

        expect(subprocessosStore.buscarContextoEdicao).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).atividades).toEqual([
            {codigo: 2, descricao: "Atividade importada", conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]}
        ]);
    });

    it("deve gerenciar interações com formulários de atividades, modais de importação e alertas de erro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.atividades = [{codigo: 1, descricao: "A1", conhecimentos: [{codigo: 1}]}];
        await vm.$nextTick();

        // Abertura do modal de importação
        const btnImportar = wrapper.find('[data-testid="btn-cad-atividades-importar"]');
        await btnImportar.trigger('click');
        expect(vm.mostrarModalImportar).toBe(true);

        // Descarte de alerta de erro global
        vm.erroGlobal = "Erro";
        await vm.$nextTick();
        const btnDismissAlert = wrapper.find('[data-testid="btn-dismiss-alert"]');
        await btnDismissAlert.trigger('click');
        expect(vm.erroGlobal).toBeNull();

        // Descarte de notificação do sistema
        vm.notify("Msg", "info");
        await vm.$nextTick();
        const btnDismissAppAlert = wrapper.find('[data-testid="btn-dismiss-app-alert"]');
        await btnDismissAppAlert.trigger('click');
        expect(vm.notificacao).toBeNull();

        // Atualização de v-model no formulário de atividade
        const form = wrapper.findComponent({name: 'CadAtividadeForm'});
        await form.vm.$emit('update:modelValue', 'Nova');
        expect(vm.novaAtividade).toBe('Nova');

        // Eventos disparados pelo item de atividade
        const item = wrapper.findComponent({name: 'AtividadeItem'});
        await item.vm.$emit('atualizar-atividade', 'desc');
        await item.vm.$emit('remover-atividade');
        await item.vm.$emit('adicionar-conhecimento', 'con');
        await item.vm.$emit('atualizar-conhecimento', 1, 'con desc');
        await item.vm.$emit('remover-conhecimento', 1);

        // Atualização de v-model no modal de confirmação
        vm.mostrarModalConfirmacaoRemocao = false;
        const modalConfirmacao = wrapper.findAllComponents({name: 'ModalConfirmacao'}).find(c => c.props('modelValue') !== undefined);
        if (modalConfirmacao) {
            await modalConfirmacao.vm.$emit('update:modelValue', true);
            expect(vm.mostrarModalConfirmacaoRemocao).toBe(true);
        }

        // @fechar events (114, 130, 136)
        const importModal = wrapper.findComponent({name: 'ImportarAtividadesModal'});
        if (importModal.exists()) await importModal.vm.$emit('fechar');
        
        const confirmModal = wrapper.findComponent({name: 'ConfirmacaoDisponibilizacaoModal'});
        if (confirmModal.exists()) await confirmModal.vm.$emit('fechar');
        
        const histModal = wrapper.findComponent({name: 'HistoricoAnaliseModal'});
        if (histModal.exists()) await histModal.vm.$emit('fechar');

        // Branches de timeout e outros

        vm.timeoutLimparErros = setTimeout(() => {}, 100);
        vm.timeoutLimpezaErros();
        
        // Covering 351-352
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(null);
        vi.mocked(useProcessosModule.useProcessos().buscarProcessoDetalhe).mockResolvedValue(undefined);
        await vm.carregarContextoInicial();

        // 375-377 (adicionarAtividade success branch)
        vm.codSubprocesso = 123;
        const mockAtiv = {atividadesAtualizadas: []};
        mockAtividadeForm.adicionarAtividade.mockResolvedValue(mockAtiv);
        await vm.adicionarAtividade();

        // 381-385 (adicionarAtividade fail branch)
        mockAtividadeForm.adicionarAtividade.mockRejectedValue(new Error("Erro"));
        await vm.adicionarAtividade();

        // 404 (confirmarRemocao success branch)
        vi.mocked(atividadeService.excluirAtividade).mockResolvedValue({atividadesAtualizadas: []} as any);
        vm.dadosRemocao = {tipo: "atividade", index: 0};
        await vm.confirmarRemocao();
        
        // 291 (processarRespostaLocal branch)
        vm.processarRespostaLocal({atividadesAtualizadas: [{codigo: 1}]});
    });
});
