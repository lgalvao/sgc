import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {nextTick, ref} from 'vue';
import {createTestingPinia} from '@pinia/testing';
import ProcessoCadastroView from '@/views/ProcessoCadastroView.vue';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {TEXTOS} from "@/constants/textos";
import * as processoService from '@/services/processo';
import {obterAmanhaFormatado} from "@/utils/date";
import {useSubprocessoStore} from '@/stores/subprocesso';
import {useProcessoForm} from '@/composables/useProcessoForm';
import {logger} from '@/utils';

vi.mock('@/services/processo', () => ({
    obterDetalhesProcesso: vi.fn(),
    criarProcesso: vi.fn(),
    atualizarProcesso: vi.fn(),
    iniciarProcesso: vi.fn(),
    excluirProcesso: vi.fn(),
}));

vi.mock('@/services/unidadeService', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@/services/unidadeService')>();
    return {
        ...actual,
        buscarArvoreComElegibilidade: vi.fn().mockResolvedValue([]),
        buscarTodasUnidades: vi.fn().mockResolvedValue([]),
        buscarDiagnosticoOrganizacional: vi.fn().mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        }),
    };
});

const diagnosticoQueryMock = {
    data: ref<any>(null),
    isLoading: ref(false),
    error: ref<Error | null>(null),
};

vi.mock('@/composables/useDiagnosticoOrganizacionalQuery', () => ({
    useDiagnosticoOrganizacionalQuery: () => diagnosticoQueryMock,
    useInvalidacaoDiagnosticoOrganizacional: () => ({
        invalidarDiagnostico: vi.fn(),
    }),
}));

const unidadeStoreMock = {
    garantirArvoreElegibilidade: vi.fn().mockResolvedValue([]),
    invalidarCache: vi.fn(),
    invalidar: vi.fn(),
};

const useUnidadeStore = (_?: any) => unidadeStoreMock;

vi.mock('@/composables/useUnidadeQuery', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@/composables/useUnidadeQuery')>();
    return {
        ...actual,
        useArvoreElegibilidadeQuery: (tipoRef: any, codigoRef: any) => ({
            refetch: async () => {
                const data = await unidadeStoreMock.garantirArvoreElegibilidade(tipoRef.value, codigoRef.value);
                return { data };
            }
        }),
    };
});

const {mockPush, mockRoute} = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockRoute: {query: {} as Record<string, string>}
    }
});

vi.mock('vue-router', () => {
    return {
        useRouter: () => ({push: mockPush}),
        useRoute: () => mockRoute,
        RouterLink: {
            name: "RouterLink",
            props: ["to"],
            template: "<a :href=\"typeof to === 'string' ? to : to.path\"><slot /></a>"
        },
        createRouter: vi.fn(() => ({
            beforeEach: vi.fn(),
            afterEach: vi.fn(),
            push: mockPush,
            replace: vi.fn(),
            resolve: vi.fn(),
            currentRoute: {value: mockRoute},
        })),
        createWebHistory: vi.fn(),
        createMemoryHistory: vi.fn(),
    };
});

vi.mock('@/composables/useProcessoForm', async (importOriginal) => {
    const actual = await importOriginal() as any;
    return {
        ...actual,
        useProcessoForm: vi.fn(actual.useProcessoForm),
    };
});

const ArvoreUnidadesStub = {
    template: '<div><slot /></div>',
    props: ['unidades', 'modelValue'],
    emits: ['update:modelValue']
};

const ProcessoFormFieldsStub = {
    template: `<div>
        <div v-if="carregandoUnidades">{{ CARREGANDO_UNIDADES }}</div>
        <input data-testid="inp-processo-descricao" :value="modelValue.descricao" @input="$emit('update:modelValue', {...modelValue, descricao: $event.target.value})" />
    </div>`,
    props: ['modelValue', 'errosCampos', 'unidades', 'carregandoUnidades', 'modoEdicao'],
    emits: ['update:modelValue'],
    data: () => ({CARREGANDO_UNIDADES: TEXTOS.unidades.CARREGANDO}),
    methods: {
        focarDescricao: vi.fn(),
        focarPrimeiroErro: vi.fn(),
    }
};

const ModalAcaoBlocoStub = {
    name: 'ModalAcaoBloco',
    template: '<div class="modal-acao-bloco-stub"></div>',
    props: ['unidades', 'unidadesPreSelecionadas', 'titulo', 'texto', 'rotuloBotao', 'mostrarSituacao', 'id'],
    emits: ['confirmar'],
    data() {
        return {
            aberto: false,
            processando: false,
            erro: null as string | null,
        };
    },
    methods: {
        abrir(this: any) {
            this.aberto = true;
        },
        fechar(this: any) {
            this.aberto = false;
        },
        setProcessando(this: any, valor: boolean) {
            this.processando = valor;
        },
        setErro(this: any, mensagem: string | null) {
            this.erro = mensagem;
        },
    }
};

function criarErroApi(mensagem: string, erros: Array<{ campo?: string | null; mensagem?: string }> = []) {
    return {
        isAxiosError: true,
        response: {
            status: 400,
            data: {message: mensagem, erros}
        }
    } as any;
}

describe('ProcessoCadastroView.vue', () => {
    const context = setupComponentTest();
    const permissoesAdmin = {
        mostrarCriarProcesso: true,
        mostrarArvoreCompletaUnidades: true,
        mostrarCtaPainelVazio: true,
        mostrarRelatorios: true,
        mostrarDiagnosticoOrganizacional: true,
        mostrarMenuConfiguracoes: true,
        mostrarMenuAdministradores: true,
        mostrarCriarAtribuicaoTemporaria: true,
    };

    const createWrapper = (initialState = {}) => {
        const processoInicial = (initialState as any).processos?.processoDetalhe ?? null;
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue(processoInicial);

        context.wrapper = mount(ProcessoCadastroView, {
            global: {
                ...getCommonMountOptions(initialState).global,
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    ArvoreUnidadesElegiveis: ArvoreUnidadesStub,
                    ArvoreUnidades: ArvoreUnidadesStub,
                    BContainer: {template: '<div><slot /></div>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BCard: {template: '<div><slot /></div>'},
                    BCardBody: {template: '<div><slot /></div>'},
                    BForm: {template: '<form><slot /></form>'},
                    BFormGroup: {template: '<div><slot /></div>'},
                    BFormInput: {template: '<input />', props: ['modelValue', 'state']},
                    BFormSelect: {
                        template: '<select v-bind="$attrs"><slot name="first" /><slot /></select>',
                        props: ['modelValue', 'options', 'state'],
                        inheritAttrs: false
                    },
                    BFormCheckbox: {template: '<input type="checkbox" />', props: ['modelValue']},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BAlert: {
                        template: '<div v-if="modelValue" class="alert"><slot /></div>',
                        props: ['modelValue', 'variant', 'dismissible']
                    },
                    BSpinner: {template: '<span>Loading...</span>'},
                    PageHeader: true,
                    LoadingButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    AppAlert: {
                        template: '<div v-if="message || notification" data-testid="app-alert"><p v-if="message">{{message}}</p><template v-if="notification"><p>{{notification.resumo}}</p><ul><li v-for="d in notification.detalhes" :key="d">{{d}}</li></ul></template></div>',
                        props: ['message', 'notification', 'variant', 'dismissible', 'stackTrace'],
                    },
                    ProcessoFormFields: ProcessoFormFieldsStub,
                    ModalAcaoBloco: ModalAcaoBlocoStub,
                    InputData: {template: '<input type="date" />', props: ['modelValue', 'state']},
                }
            }
        });
        return {wrapper: context.wrapper};
    };

    beforeEach(() => {
        vi.clearAllMocks();
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([]);
        diagnosticoQueryMock.data.value = null;
        diagnosticoQueryMock.isLoading.value = false;
        diagnosticoQueryMock.error.value = null;
        mockPush.mockReset();
        mockRoute.query = {};
    });

    it('loads initial data correctly for new process', async () => {
        const {wrapper} = createWrapper({
            perfil: {permissoes: permissoesAdmin}
        });
        await flushPromises();

        expect(wrapper.find('page-header-stub').attributes('title')).toBe(TEXTOS.processo.cadastro.TITULO);
    });

    it('mantém link para unidade inconsistente mesmo fora da árvore de elegibilidade', async () => {
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {codigo: 10, sigla: 'U1', nome: 'Unidade Elegível', isElegivel: true, filhas: []}
        ]);
        const unidadeService = await import('@/services/unidadeService');
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce([
            {codigo: 43, sigla: '43ª Z.E.', nome: 'Zona 43', filhas: []}
        ] as any);

        diagnosticoQueryMock.data.value = {
            possuiViolacoes: true,
            resumo: 'Há inconsistências organizacionais.',
            quantidadeTiposViolacao: 1,
            quantidadeOcorrencias: 1,
            grupos: [
                {
                    tipo: 'Unidade sem responsável',
                    quantidadeOcorrencias: 1,
                    ocorrencias: ['sigla=43ª Z.E., tipo=OPERACIONAL']
                }
            ]
        };

        const {wrapper} = createWrapper({
            perfil: {permissoesSessao: permissoesAdmin},
        });

        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();
        await flushPromises();

        const link = wrapper.find('[data-testid="link-unidade-sem-responsavel-0"]');
        expect(link.exists()).toBe(true);
        expect(link.text()).toContain('43ª Z.E.');
        expect(wrapper.text()).not.toContain('tipo=OPERACIONAL');
    });

    it('loads process details when codProcesso is present in query', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {
            codigo: 123,
            descricao: 'P1',
            tipo: 'MAPEAMENTO',
            dataLimite: '2024-12-31',
            situacao: 'CRIADO',
            unidades: []
        };

        const {wrapper} = createWrapper({
            perfil: {permissoes: permissoesAdmin},
            processos: {processoDetalhe: mockProcesso}
        });
        await flushPromises();

        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(123);
        expect(wrapper.vm.descricao).toBe('P1');
    });

    it('disables save button when form is invalid', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-processo-salvar-rodape"]');
        expect(btn.exists()).toBe(true);
        expect(btn.attributes('disabled')).toBeDefined();
        expect(wrapper.vm.isFormInvalid).toBe(true);
    });

    it('saves new process successfully', async () => {
        const {wrapper} = createWrapper();
        vi.mocked(processoService.criarProcesso).mockResolvedValue({codigo: 1} as any);

        wrapper.vm.descricao = 'Novo Processo';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar-rodape"]').trigger('click');
        await flushPromises();

        expect(processoService.criarProcesso).toHaveBeenCalled();
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('updates existing process successfully', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {
            codigo: 123,
            descricao: 'P1',
            tipo: 'MAPEAMENTO',
            dataLimite: '2099-12-31T00:00:00',
            situacao: 'CRIADO',
            unidades: [{codUnidade: 1}]
        };
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {codigo: 1, sigla: 'U1', nome: 'Unidade 1', isElegivel: true, filhas: []}
        ]);
        const {wrapper} = createWrapper({
            processos: {processoDetalhe: mockProcesso}
        });
        await flushPromises();

        vi.mocked(processoService.atualizarProcesso).mockResolvedValue({codigo: 123} as any);

        wrapper.vm.descricao = 'P1 Alterado';
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar-rodape"]').trigger('click');
        await flushPromises();

        expect(processoService.atualizarProcesso).toHaveBeenCalledWith(123, expect.objectContaining({descricao: 'P1 Alterado'}));
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('handles api validation errors', async () => {
        const {wrapper} = createWrapper();

        vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi(
            'Erro de validação',
            [
                {campo: 'descricao', mensagem: 'Descrição é obrigatória'},
                {campo: 'tipo', mensagem: 'Tipo inválido'},
                {campo: 'dataLimiteEtapa1', mensagem: 'Data inválida'},
                {campo: 'unidades', mensagem: 'Selecione ao menos uma unidade'},
                {campo: null, mensagem: 'Erro genérico'}
            ],
        ));

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar-rodape"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.fieldErrors.descricao).toBe('Descrição é obrigatória');
        expect(wrapper.vm.fieldErrors.tipo).toBe('Tipo inválido');
        expect(wrapper.vm.fieldErrors.dataLimite).toBe('Data inválida');
        expect(wrapper.vm.fieldErrors.unidades).toBe('Selecione ao menos uma unidade');
        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notificacao?.detalhes).toContain('Erro genérico');
    });

    it('handles error without lastError (network/runtime error)', async () => {
        const {wrapper} = createWrapper();

        vi.mocked(processoService.criarProcesso).mockRejectedValue(new Error('Network error'));

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar-rodape"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.mensagem).toContain('Não foi possível salvar o processo');
    });

    it('does nothing when confirmarRemocao is called without processoEditando', async () => {
        const {wrapper} = createWrapper();
        const processoServiceModule = await import('@/services/processo');
        vi.spyOn(processoServiceModule, 'excluirProcesso');

        wrapper.vm.processoEditando = null;
        wrapper.vm.mostrarModalRemocao = true;
        await nextTick();

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processoServiceModule.excluirProcesso).not.toHaveBeenCalled();
        expect(wrapper.vm.mostrarModalRemocao).toBe(false);
    });

    it('clears campo errors when fields are updated', async () => {
        const {wrapper} = createWrapper();

        wrapper.vm.fieldErrors.descricao = 'Erro';
        wrapper.vm.fieldErrors.tipo = 'Erro';
        wrapper.vm.fieldErrors.dataLimite = 'Erro';
        wrapper.vm.fieldErrors.unidades = 'Erro';
        await nextTick();

        wrapper.vm.descricao = 'Nova descricao';
        await nextTick();
        expect(wrapper.vm.fieldErrors.descricao).toBe('');

        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        expect(wrapper.vm.fieldErrors.tipo).toBe('');

        wrapper.vm.dataLimite = obterAmanhaFormatado();
        await nextTick();
        expect(wrapper.vm.fieldErrors.dataLimite).toBe('');

        wrapper.vm.unidadesSelecionadas = [2];
        await nextTick();
        expect(wrapper.vm.fieldErrors.unidades).toBe('');
    });

    it('shows loading spinner when units are loading', async () => {
        unidadeStoreMock.garantirArvoreElegibilidade.mockReturnValueOnce(new Promise(() => {
        }));
        const {wrapper} = createWrapper();
        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();
        expect(wrapper.text()).toContain(TEXTOS.unidades.CARREGANDO);
    });

    it('shows alert with multiple errors', async () => {
        const {wrapper} = createWrapper();
        (wrapper.vm).notifyStructured('Corpo', ['Erro 1', 'Erro 2'], {variant: 'danger'});
        await nextTick();
        expect(wrapper.text()).toContain('Erro 1');
        expect(wrapper.text()).toContain('Erro 2');
    });

    it('handles confirmRemove correctly', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {
            codigo: 123,
            descricao: 'P1',
            situacao: 'CRIADO',
            tipo: 'MAPEAMENTO',
            dataLimite: '2024-12-31',
            unidades: []
        };
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();

        vi.mocked(processoService.excluirProcesso).mockResolvedValue({} as any);

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processoService.excluirProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('handles startProcess correctly when there are no unidades com equipe própria selecionadas', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {
            codigo: 123,
            descricao: 'P1',
            situacao: 'CRIADO',
            tipo: 'MAPEAMENTO',
            dataLimite: '2024-12-31',
            unidades: []
        };
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();

        vi.mocked(processoService.iniciarProcesso).mockResolvedValue({} as any);

        await wrapper.vm.confirmarIniciarProcesso();
        await flushPromises();

        expect(processoService.iniciarProcesso).toHaveBeenCalledWith(123, 'MAPEAMENTO', []);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('abre modal complementar quando houver unidade com equipe própria selecionada', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {
            codigo: 123,
            descricao: 'P1',
            situacao: 'CRIADO',
            tipo: 'MAPEAMENTO',
            dataLimite: '2024-12-31',
            unidades: []
        };
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {
                codigo: 10,
                sigla: 'STIC',
                nome: 'Secretaria',
                tipo: 'INTEROPERACIONAL',
                isElegivel: true,
                filhas: [
                    {codigo: 11, sigla: 'SEDIA', nome: 'Seção', tipo: 'OPERACIONAL', isElegivel: true, filhas: []}
                ]
            }
        ] as any);

        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();
        wrapper.vm.unidadesSelecionadas = [10, 11];
        await nextTick();

        await wrapper.vm.confirmarIniciarProcesso();

        const modal = wrapper.findComponent({name: 'ModalAcaoBloco'});
        expect((modal.vm as any).aberto).toBe(true);
        expect(processoService.iniciarProcesso).not.toHaveBeenCalled();
    });

    it('inicia com subconjunto confirmado de unidades com equipe própria', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {
            codigo: 123,
            descricao: 'P1',
            situacao: 'CRIADO',
            tipo: 'MAPEAMENTO',
            dataLimite: '2024-12-31',
            unidades: []
        };
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {
                codigo: 10,
                sigla: 'STIC',
                nome: 'Secretaria',
                tipo: 'INTEROPERACIONAL',
                isElegivel: true,
                filhas: [
                    {codigo: 11, sigla: 'SEDIA', nome: 'Seção', tipo: 'OPERACIONAL', isElegivel: true, filhas: []}
                ]
            }
        ] as any);

        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();
        vi.mocked(processoService.iniciarProcesso).mockResolvedValue({} as any);
        wrapper.vm.unidadesSelecionadas = [10, 11];
        await nextTick();

        await wrapper.vm.confirmarSelecaoUnidadesComEquipePropria({ids: []});
        await flushPromises();

        expect(processoService.iniciarProcesso).toHaveBeenCalledWith(123, 'MAPEAMENTO', [11]);
    });

    it('resets units when type changes to REVISAO and no units were previously selected', async () => {
        const {wrapper} = createWrapper();
        wrapper.vm.unidadesSelecionadas = [1];
        wrapper.vm.tipo = 'REVISAO';
        await flushPromises();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);
    });

    it('clears units when type changes', async () => {
        const mockProcesso = {
            codigo: 123,
            tipo: 'MAPEAMENTO',
            situacao: 'CRIADO',
            descricao: 'P1',
            dataLimite: '2099-12-31T00:00:00',
            unidades: [{codUnidade: 1}]
        };
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();

        wrapper.vm.tipo = 'REVISAO';
        await flushPromises();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);

        wrapper.vm.tipo = 'MAPEAMENTO';
        await flushPromises();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);
    });

    describe('ProcessoCadastroView.vue - cobertura adicional', () => {
        const contextCobertura = setupComponentTest();

        const ModalConfirmacaoStub = {
            name: 'ModalConfirmacao',
            template: '<div class="modal-confirmacao-stub"><slot /></div>',
            props: ['modelValue', 'loading'],
            emits: ['update:modelValue', 'confirmar']
        };

        const createWrapperCobertura = (initialState = {}) => {
            const processoInicial = (initialState as any).processos?.processoDetalhe ?? null;
            vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue(processoInicial);

            const pinia = createTestingPinia({createSpy: vi.fn, initialState});

            contextCobertura.wrapper = mount(ProcessoCadastroView, {
                global: {
                    plugins: [pinia],
                    stubs: {
                        LayoutPadrao: {template: '<div><slot /></div>'},
                        ArvoreUnidades: ArvoreUnidadesStub,
                        ArvoreUnidadesElegiveis: ArvoreUnidadesStub,
                        BContainer: {template: '<div><slot /></div>'},
                        BAlert: {template: '<div class="alert"><slot /></div>', props: ['modelValue', 'variant']},
                        BForm: {template: '<form @submit.prevent><slot /></form>'},
                        BFormGroup: {template: '<div><slot /></div>'},
                        BFormInput: {
                            template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                            props: ['modelValue']
                        },
                        BFormSelect: {
                            template: '<select v-bind="$attrs" :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option value="MAPEAMENTO">MAPEAMENTO</option></select>',
                            props: ['modelValue', 'options'],
                            inheritAttrs: false
                        },
                        BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                        ModalConfirmacao: ModalConfirmacaoStub,
                        BSpinner: {template: '<span>Loading...</span>'},
                        BFormInvalidFeedback: {template: '<div><slot /></div>'},
                        LoadingButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                        ProcessoFormFields: ProcessoFormFieldsStub,
                        ModalAcaoBloco: ModalAcaoBlocoStub,
                        PageHeader: true,
                        AppAlert: {
                            template: '<div v-if="message || notification" data-testid="app-alert"></div>',
                            props: ['message', 'notification', 'variant', 'dismissible', 'stackTrace'],
                        },
                        InputData: {template: '<input type="date" />', props: ['modelValue', 'state']},
                    }
                }
            });
            return {wrapper: contextCobertura.wrapper, pinia};
        };

        beforeEach(() => {
            unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([]);
            vi.spyOn(console, 'error').mockImplementation(() => {});
        });

        afterEach(() => {
            vi.restoreAllMocks();
        });

        it('handles mixed errors (campo + generic) correctly in handleApiErrors', async () => {
            const {wrapper} = createWrapperCobertura();

            vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi(
                'Erro misto',
                [
                    {campo: 'descricao', mensagem: 'Descrição inválida'},
                    {campo: null, mensagem: 'Erro genérico de regra de negócio'}
                ],
            ));

            await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste');
            wrapper.vm.tipo = 'MAPEAMENTO';
            wrapper.vm.dataLimite = obterAmanhaFormatado();
            wrapper.vm.unidadesSelecionadas = [1];
            await nextTick();

            await (wrapper.vm as any).salvarProcesso();
            await flushPromises();

            expect(wrapper.vm.notificacao).not.toBeNull();
            expect(wrapper.vm.notificacao?.notificacao?.detalhes).toContain('Erro genérico de regra de negócio');
            expect(wrapper.vm.fieldErrors.descricao).toBe('Descrição inválida');
        });

        it('handles error creating process during initiation flow (without existing process)', async () => {
            const {wrapper} = createWrapperCobertura();
 
            await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste inicio');
            wrapper.vm.tipo = 'MAPEAMENTO';
            wrapper.vm.dataLimite = obterAmanhaFormatado();
            wrapper.vm.unidadesSelecionadas = [1];
            await nextTick();
 
            wrapper.vm.mostrarModalConfirmacao = true;
            await nextTick();
            expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);
 
            vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi('Failed to create'));
 
            const modal = wrapper.findComponent({name: 'ModalConfirmacao'});
            await modal.vm.$emit('confirmar');
            await flushPromises();
 
            expect(processoService.criarProcesso).toHaveBeenCalled();
            expect(processoService.iniciarProcesso).not.toHaveBeenCalled();
            expect(wrapper.vm.notificacao).not.toBeNull();
            expect(wrapper.vm.notificacao?.notificacao?.resumo).toContain('Failed to create');
            expect(wrapper.vm.isStarting).toBe(false);
        });
 
        it('handles error starting process during initiation flow', async () => {
            const {wrapper} = createWrapperCobertura();
            unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
                {codigo: 1, sigla: 'U1', nome: 'Unidade 1', isElegivel: true, filhas: []}
            ] as any);
 
            await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste inicio');
            wrapper.vm.tipo = 'MAPEAMENTO';
            wrapper.vm.dataLimite = obterAmanhaFormatado();
            wrapper.vm.unidadesSelecionadas = [1];
            await nextTick();
 
            wrapper.vm.mostrarModalConfirmacao = true;
            await nextTick();
 
            vi.mocked(processoService.criarProcesso).mockResolvedValue({codigo: 777} as any);
            vi.mocked(processoService.iniciarProcesso).mockRejectedValue(criarErroApi('Failed to start'));
 
            const modal = wrapper.findComponent({name: 'ModalConfirmacao'});
            await modal.vm.$emit('confirmar');
            await flushPromises();
 
            expect(processoService.criarProcesso).toHaveBeenCalled();
            expect(processoService.iniciarProcesso).toHaveBeenCalledWith(777, 'MAPEAMENTO', [1]);
            expect(wrapper.vm.notificacao).not.toBeNull();
            expect(wrapper.vm.notificacao?.notificacao?.resumo).toContain('Failed to start');
            expect(wrapper.vm.isStarting).toBe(false);
        });

        it('opens and confirms removal modal', async () => {
            const {wrapper} = createWrapperCobertura();
 
            (wrapper.vm as any).processoEditando = {codigo: 123, descricao: 'Processo teste'};
            await nextTick();
 
            (wrapper.vm as any).mostrarModalRemocao = true;
            await nextTick();
            expect((wrapper.vm as any).mostrarModalRemocao).toBe(true);
 
            vi.mocked(processoService.excluirProcesso).mockResolvedValue(undefined);
 
            await (wrapper.vm as any).confirmarRemocao();
            await flushPromises();
 
            expect(processoService.excluirProcesso).toHaveBeenCalledWith(123);
            expect(mockPush).toHaveBeenCalledWith('/painel');
            expect((wrapper.vm as any).mostrarModalRemocao).toBe(false);
        });
 
        it('handles error during removal', async () => {
            const {wrapper} = createWrapperCobertura();
 
            (wrapper.vm as any).processoEditando = {codigo: 123, descricao: 'Processo teste'};
            await nextTick();
 
            vi.mocked(processoService.excluirProcesso).mockRejectedValue(criarErroApi('Failed to delete'));
 
            await (wrapper.vm as any).confirmarRemocao();
            await flushPromises();
 
            expect((wrapper.vm as any).mostrarModalRemocao).toBe(false);
            expect((wrapper.vm as any).notificacao).not.toBeNull();
            expect((wrapper.vm as any).notificacao?.notificacao?.resumo).toContain('Failed to delete');
        });
 
        it('closes the removal modal when showing status is set to false', async () => {
            const {wrapper} = createWrapperCobertura();
            (wrapper.vm as any).mostrarModalRemocao = true;
 
            (wrapper.vm as any).mostrarModalRemocao = false;
            await nextTick();
 
            expect((wrapper.vm as any).mostrarModalRemocao).toBe(false);
        });

        it('triggers search for units if type changes', async () => {
            const {wrapper} = createWrapperCobertura();

            (wrapper.vm as any).tipo = 'MAPEAMENTO';
            await nextTick();

            expect(unidadeStoreMock.garantirArvoreElegibilidade).toHaveBeenCalledWith('MAPEAMENTO', undefined);
        });

        it('populates fields when loading an existing process', async () => {
            mockRoute.query = {codProcesso: '123'};

            const mockProcesso = {
                codigo: 123,
                descricao: 'Processo existente',
                tipo: 'MAPEAMENTO',
                dataLimite: '2023-12-31T00:00:00',
                situacao: 'CRIADO',
                unidades: [{codUnidade: 1}, {codUnidade: 2}]
            };

            unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
                {codigo: 1, sigla: 'U1', nome: 'Unidade 1', isElegivel: true, filhas: []},
                {codigo: 2, sigla: 'U2', nome: 'Unidade 2', isElegivel: true, filhas: []}
            ] as any);

            const {wrapper} = createWrapperCobertura({processos: {processoDetalhe: mockProcesso}});
            await flushPromises();

            expect((wrapper.vm as any).descricao).toBe('Processo existente');
            expect((wrapper.vm as any).tipo).toBe('MAPEAMENTO');
            expect((wrapper.vm as any).dataLimite).toBe('2023-12-31');
            expect((wrapper.vm as any).unidadesSelecionadas).toEqual([1, 2]);
            expect(unidadeStoreMock.garantirArvoreElegibilidade).toHaveBeenCalledWith('MAPEAMENTO', 123);
        });

        it('invalida caches relacionados ao salvar processo', async () => {
            const {wrapper, pinia} = createWrapperCobertura();
            const subprocessoStore = useSubprocessoStore(pinia);

            vi.mocked(processoService.criarProcesso).mockResolvedValue({codigo: 321} as any);

            wrapper.vm.descricao = 'Processo novo';
            wrapper.vm.tipo = 'MAPEAMENTO';
            wrapper.vm.dataLimite = obterAmanhaFormatado();
            wrapper.vm.unidadesSelecionadas = [1];

            await (wrapper.vm as any).salvarProcesso();
            await flushPromises();

            expect(subprocessoStore.invalidar).toHaveBeenCalled();
            expect(mockPush).toHaveBeenCalledWith('/painel');
        });
    });

    describe('ProcessoCadastroView.vue - ramos não cobertos', () => {
        const stubsNaoCobertos = {
            LayoutPadrao: {template: '<div><slot></slot></div>'},
            PageHeader: {template: '<div><slot name="actions"></slot></div>'},
            BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
            LoadingButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
            BAlert: {template: '<div class="b-alert"><slot /></div>'},
            AppAlert: {
                name: 'AppAlert',
                template: '<div class="app-alert"></div>',
                emits: ['dismissed'],
            },
            ModalConfirmacao: {template: '<div><slot /></div>'},
                        ProcessoFormFields: {
                            template: '<div></div>',
                            methods: {
                                focarDescricao: vi.fn(),
                                focarPrimeiroErro: vi.fn(),
                            }
                        },
                        ModalAcaoBloco: ModalAcaoBlocoStub,
                        BForm: {template: '<form @submit.prevent><slot /></form>'},
        };

        let pinaRamoNaoCoberto: any;

        beforeEach(() => {
            pinaRamoNaoCoberto = createTestingPinia({
                stubActions: false,
                initialState: {
                    perfil: {perfilSelecionado: 'ADMIN'}
                }
            });
        });

        it('cobre AppAlert clear e ModalConfirmacao v-model', async () => {
            const wrapper = mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();
            const vm = wrapper.vm as any;
 
            vm.notify('Mensagem', 'success');
            await flushPromises();
 
            const appAlert = wrapper.findComponent({name: 'AppAlert'});
            if (appAlert.exists()) {
                await appAlert.vm.$emit('dismissed');
                expect(vm.notificacao).toBeNull();
            }
 
            vm.mostrarModalRemocao = true;
            await nextTick();
            expect(vm.mostrarModalRemocao).toBe(true);
        });

        it('cobre dispensarAlertaDiagnostico', async () => {
            const wrapper = mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();
            const vm = wrapper.vm as any;

            vm.dispensarAlertaDiagnostico();
            expect(vm.exibirAlertaDiagnostico).toBe(false);
        });

        it('cobre erro ao buscar unidades', async () => {
            const loggerErrorSpy = vi.spyOn(logger, 'error').mockImplementation(() => {});
            const unidadeStore = useUnidadeStore(pinaRamoNaoCoberto);
            unidadeStore.garantirArvoreElegibilidade = vi.fn().mockRejectedValue(new Error('Erro de busca'));

            const wrapper = mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();

            const vm = wrapper.vm as any;
            await vm.buscarUnidadesParaProcesso('MAPEAMENTO');

            expect(loggerErrorSpy).toHaveBeenCalledWith('Erro ao buscar unidades:', expect.any(Error));
            loggerErrorSpy.mockRestore();
        });

        it('não registra erro ao buscar unidades quando a requisição é cancelada', async () => {
            const loggerErrorSpy = vi.spyOn(logger, 'error').mockImplementation(() => {});
            const unidadeStore = useUnidadeStore(pinaRamoNaoCoberto);
            unidadeStore.garantirArvoreElegibilidade = vi.fn().mockRejectedValue({
                code: 'ERR_CANCELED',
                name: 'CanceledError',
                message: 'cancelado'
            });

            const wrapper = mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();

            const vm = wrapper.vm as any;
            await vm.buscarUnidadesParaProcesso('MAPEAMENTO');

            expect(loggerErrorSpy).not.toHaveBeenCalled();
            loggerErrorSpy.mockRestore();
        });

        it('cobre onMounted carregar unidades se tipo ja definido', async () => {
            vi.mocked(useProcessoForm).mockReturnValueOnce({
                tipo: ref('MAPEAMENTO'),
                descricao: ref(''),
                dataLimite: ref(''),
                unidadesSelecionadas: ref([]),
                errosCampos: ref({}),
                isFormInvalid: ref(false),
                setFromErroNormalizado: vi.fn(),
                clearErrors: vi.fn(),
                hasErrors: vi.fn().mockReturnValue(false),
                construirCriarRequest: vi.fn(),
                construirAtualizarRequest: vi.fn(),
                limpar: vi.fn(),
            } as any);

            const unidadeStore = useUnidadeStore(pinaRamoNaoCoberto);
            unidadeStore.garantirArvoreElegibilidade = vi.fn().mockResolvedValue([]);

            mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();

            expect(unidadeStore.garantirArvoreElegibilidade).toHaveBeenCalled();
        });

        it('cobre confirmarIniciarProcesso sem tipo', async () => {
            const wrapper = mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();
            const vm = wrapper.vm as any;
 
            vm.tipo = null;
 
            vm.mostrarModalConfirmacao = true;
            await nextTick();
            await vm.confirmarIniciarProcesso();
 
            expect(vm.mostrarModalConfirmacao).toBe(false);
            expect(vm.notificacao?.mensagem).toBe(TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR);
        });

        it('cobre confirmarRemocao limpando campos', async () => {
            const limparMock = vi.fn();
            vi.mocked(useProcessoForm).mockReturnValueOnce({
                tipo: ref('MAPEAMENTO'),
                descricao: ref(''),
                dataLimite: ref(''),
                unidadesSelecionadas: ref([]),
                errosCampos: ref({}),
                isFormInvalid: ref(false),
                setFromErroNormalizado: vi.fn(),
                clearErrors: vi.fn(),
                hasErrors: vi.fn().mockReturnValue(false),
                construirCriarRequest: vi.fn(),
                construirAtualizarRequest: vi.fn(),
                limpar: limparMock,
            } as any);

            const wrapper = mount(ProcessoCadastroView, {
                global: {plugins: [pinaRamoNaoCoberto], stubs: stubsNaoCobertos}
            });
            await flushPromises();
            const vm = wrapper.vm as any;

            vm.processoEditando = {codigo: 1, descricao: 'Teste'};

            vi.mocked(processoService.excluirProcesso).mockResolvedValue({} as any);

            await vm.confirmarRemocao();

            expect(limparMock).toHaveBeenCalled();
        });
    });
});
