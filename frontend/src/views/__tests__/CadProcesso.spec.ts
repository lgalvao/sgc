import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {nextTick} from 'vue';
import ProcessoCadastroView from '@/views/ProcessoCadastroView.vue';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {TEXTOS} from "@/constants/textos";
import * as processoService from '@/services/processoService';
import {obterAmanhaFormatado} from "@/utils/dateUtils";

vi.mock('@/services/processoService', () => ({
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
        buscarDiagnosticoOrganizacional: vi.fn().mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        }),
        mapUnidadesArray: vi.fn((arr) => arr || []),
    };
});

const unidadeStoreMock = {
    garantirArvoreElegibilidade: vi.fn().mockResolvedValue([]),
    invalidarCache: vi.fn(),
};

vi.mock('@/stores/unidade', () => ({
    useUnidadeStore: () => unidadeStoreMock,
}));

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

const ArvoreUnidadesStub = {
    template: '<div><slot /></div>',
    props: ['unidades', 'modelValue'],
    emits: ['update:modelValue']
};

const ProcessoFormFieldsStub = {
    template: `<div>
        <div v-if="isLoadingUnidades">{{ CARREGANDO_UNIDADES }}</div>
        <input data-testid="inp-processo-descricao" :value="modelValue.descricao" @input="$emit('update:modelValue', {...modelValue, descricao: $event.target.value})" />
    </div>`,
    props: ['modelValue', 'fieldErrors', 'unidades', 'isLoadingUnidades', 'isEdit'],
    emits: ['update:modelValue'],
    data: () => ({CARREGANDO_UNIDADES: TEXTOS.unidades.CARREGANDO}),
};

function criarErroApi(mensagem: string, erros: Array<{campo?: string | null; mensagem?: string}> = []) {
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
                    BFormSelect: {template: '<select />', props: ['modelValue', 'options', 'state']},
                    BFormCheckbox: {template: '<input type="checkbox" />', props: ['modelValue']},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BAlert: {template: '<div v-if="modelValue" class="alert"><slot /></div>', props: ['modelValue', 'variant', 'dismissible']},
                    BSpinner: {template: '<span>Loading...</span>'},
                    PageHeader: true,
                    LoadingButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    AppAlert: {
                        template: '<div v-if="message || notification" data-testid="app-alert"><p v-if="message">{{message}}</p><template v-if="notification"><p>{{notification.summary}}</p><ul><li v-for="d in notification.details" :key="d">{{d}}</li></ul></template></div>',
                        props: ['message', 'notification', 'variant', 'dismissible', 'stackTrace'],
                    },
                    ProcessoFormFields: ProcessoFormFieldsStub,
                    InputData: {template: '<input type="date" />', props: ['modelValue', 'state']},
                }
            }
        });
        return {wrapper: context.wrapper};
    };

    beforeEach(() => {
        vi.clearAllMocks();
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

    it('loads process details when codProcesso is present in query', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {codigo: 123, descricao: 'P1', tipo: 'MAPEAMENTO', dataLimite: '2024-12-31', situacao: 'CRIADO', unidades: []};
        
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
        const mockProcesso = {codigo: 123, descricao: 'P1', tipo: 'MAPEAMENTO', dataLimite: '2099-12-31T00:00:00', situacao: 'CRIADO', unidades: [{codUnidade: 1}]};
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
        expect(wrapper.vm.notificacao?.notification?.details).toContain('Erro genérico');
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
        expect(wrapper.vm.notificacao?.message).toContain('Não foi possível salvar o processo');
    });

    it('does nothing when confirmarRemocao is called without processoEditando', async () => {
        const {wrapper} = createWrapper();
        const processoServiceModule = await import('@/services/processoService');
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
        unidadeStoreMock.garantirArvoreElegibilidade.mockReturnValueOnce(new Promise(() => {}));
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
        const mockProcesso = {codigo: 123, descricao: 'P1', situacao: 'CRIADO', tipo: 'MAPEAMENTO', dataLimite: '2024-12-31', unidades: []};
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();
        
        vi.mocked(processoService.excluirProcesso).mockResolvedValue({} as any);

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processoService.excluirProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('handles startProcess correctly', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {codigo: 123, descricao: 'P1', situacao: 'CRIADO', tipo: 'MAPEAMENTO', dataLimite: '2024-12-31', unidades: []};
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();
        
        vi.mocked(processoService.iniciarProcesso).mockResolvedValue({} as any);

        await wrapper.vm.confirmarIniciarProcesso();
        await flushPromises();

        expect(processoService.iniciarProcesso).toHaveBeenCalledWith(123, 'MAPEAMENTO', []);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('resets units when type changes to REVISAO and no units were previously selected', async () => {
        const {wrapper} = createWrapper();
        wrapper.vm.unidadesSelecionadas = [1];
        wrapper.vm.tipo = 'REVISAO';
        await flushPromises();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);
    });

    it('clears units when type changes', async () => {
        const mockProcesso = {codigo: 123, tipo: 'MAPEAMENTO', situacao: 'CRIADO', descricao: 'P1', dataLimite: '2099-12-31T00:00:00', unidades: [{codUnidade: 1}]};
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();

        wrapper.vm.tipo = 'REVISAO';
        await flushPromises();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);

        wrapper.vm.tipo = 'MAPEAMENTO';
        await flushPromises();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);
    });
});
