import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {nextTick} from 'vue';
import ProcessoCadastroView from '@/views/ProcessoCadastroView.vue';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as unidadeService from '@/services/unidadeService';
import * as processoService from '@/services/processoService';
import {obterAmanhaFormatado} from "@/utils/dateUtils";

vi.mock('@/services/processoService', () => ({
    obterDetalhesProcesso: vi.fn(),
    criarProcesso: vi.fn(),
    atualizarProcesso: vi.fn(),
    iniciarProcesso: vi.fn(),
    excluirProcesso: vi.fn(),
}));

vi.mock('@/services/unidadeService', () => ({
    buscarArvoreComElegibilidade: vi.fn().mockResolvedValue([]),
    buscarDiagnosticoOrganizacional: vi.fn().mockResolvedValue({
        possuiViolacoes: false,
        resumo: '',
        quantidadeTiposViolacao: 0,
        quantidadeOcorrencias: 0,
        grupos: [],
    }),
    mapUnidadesArray: vi.fn((arr) => arr || []),
}));

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

function criarErroApi(message: string, subErrors: Array<{field?: string | null; message?: string}> = []) {
    return {
        isAxiosError: true,
        response: {
            status: 400,
            data: {message, subErrors}
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
            ...getCommonMountOptions(
                {
                    ...initialState
                },
                {
                    BContainer: {template: '<div><slot /></div>'},
                    BAlert: {template: '<div class="alert"><slot /></div>', props: ['modelValue', 'variant']},
                    BForm: {template: '<form @submit.prevent><slot /></form>'},
                    BFormGroup: {
                        template: '<div><slot name="label">{{ label }}</slot><slot /></div>',
                        props: ['label']
                    },
                    BFormInput: {
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                        props: ['modelValue']
                    },
                    BFormSelect: {
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot name="first" /><option value="MAPEAMENTO">MAPEAMENTO</option></select>',
                        props: ['modelValue', 'options'],
                        inheritAttrs: false
                    },
                    BFormSelectOption: {
                        template: '<option :value="value" :disabled="disabled"><slot /></option>',
                        props: ['value', 'disabled']
                    },
                    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                    BModal: {
                        template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
                        props: ['modelValue']
                    },
                    BSpinner: {template: '<span>Loading...</span>'},
                    ArvoreUnidades: ArvoreUnidadesStub,
                    BFormInvalidFeedback: {template: '<div><slot /></div>'}
                }
            )
        });

        return {wrapper: context.wrapper};
    };

    beforeEach(async () => {
        vi.clearAllMocks();
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue(null as any);
        mockRoute.query = {};
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockResolvedValue([]);
        vi.mocked(unidadeStoreMock.garantirArvoreElegibilidade).mockResolvedValue([]);
        vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        } as any);
        vi.mocked(unidadeService.mapUnidadesArray).mockImplementation((arr) => arr || []);

        await import('@/services/processoService');

        window.scrollTo = vi.fn();

        vi.spyOn(console, 'error').mockImplementation(() => {
        });
    });

    it('renders correctly in creation mode', async () => {
        const {wrapper} = createWrapper();
        expect(wrapper.find('h2').text()).toBe('Cadastro de processo');
        expect(unidadeStoreMock.garantirArvoreElegibilidade).not.toHaveBeenCalled();
        expect(wrapper.find('[data-testid="btn-processo-salvar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-salvar-rodape"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-remover"]').exists()).toBe(false);
    });

    it('exibe alerta fixo de pendencias organizacionais para ADMIN', async () => {
        // Pré-popula a organizacaoStore via initialState (evita dependência da chamada HTTP)
        const {wrapper} = createWrapper({
            organizacao: {
                diagnostico: {
                    possuiViolacoes: true,
                    resumo: 'Foram encontradas 2 inconsistencias.',
                    quantidadeTiposViolacao: 1,
                    quantidadeOcorrencias: 2,
                    grupos: [
                        {
                            tipo: 'Unidade sem responsável',
                            quantidadeOcorrencias: 2,
                            ocorrencias: ['sigla=Z1', 'sigla=Z2'],
                        },
                    ],
                },
                erroDiagnostico: null,
                carregado: true,
            },
            perfil: {
                perfilSelecionado: 'ADMIN',
                permissoesSessao: permissoesAdmin,
            }
        });
        await flushPromises();

        expect(wrapper.text()).toContain('Pendências organizacionais identificadas.');
        expect(wrapper.text()).toContain('Unidade sem responsável: 2 ocorrência(s)');
    });

    it('disables action buttons when form is incomplete and enables when complete', async () => {
        const {wrapper} = createWrapper();
        const salvarBtn = wrapper.find('[data-testid="btn-processo-salvar"]');
        const iniciarBtn = wrapper.find('[data-testid="btn-processo-iniciar"]');

        // Initially disabled (empty fields)
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);
        expect((iniciarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Fill description
        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste');
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Fill date
        await wrapper.find('[data-testid="inp-processo-data-limite"]').setValue(obterAmanhaFormatado());
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Select units (modelValue for ArvoreUnidades)
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Select type
        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();

        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(false);
        expect((iniciarBtn.element as HTMLButtonElement).disabled).toBe(false);

        // Clear description again
        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('');
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);
    });

    it('loads process data for editing', async () => {
        mockRoute.query = {codProcesso: '123'};
        createWrapper();
        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(123);
    });

    it('redirects if process is not editable', async () => {
        mockRoute.query = {codProcesso: '123'};
        createWrapper({
            processos: {
                processoDetalhe: {codigo: 123, situacao: 'EM_ANDAMENTO', unidades: []},
            }
        });
        await flushPromises();
        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/processo/123');
    });

    it('creates a new process', async () => {
        const {wrapper} = createWrapper();

        const descricaoInput = wrapper.find('[data-testid="inp-processo-descricao"]');
        await descricaoInput.setValue('Novo processo');

        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();

        const dataInput = wrapper.find('[data-testid="inp-processo-data-limite"]');
        await dataInput.setValue(obterAmanhaFormatado());

        wrapper.vm.unidadesSelecionadas = [1, 2];
        await nextTick();

        const salvarBtn = wrapper.find('[data-testid="btn-processo-salvar"]');
        await salvarBtn.trigger('click');

        expect(processoService.criarProcesso).toHaveBeenCalledWith({
            descricao: 'Novo processo',
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: `${obterAmanhaFormatado()}T00:00:00`,
            unidades: [1, 2]
        });

        await flushPromises();
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('handles creation error', async () => {
        const {wrapper} = createWrapper();

        vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi('Erro validacao'));

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.summary).toContain('Erro validacao');
    });

    it('updates an existing process', async () => {
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {codigo: 1, sigla: 'U1', nome: 'Unidade 1', isElegivel: true, filhas: []}
        ] as any);

        const {wrapper} = createWrapper();

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.descricao = 'Editado';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');

        expect(processoService.atualizarProcesso).toHaveBeenCalledWith(123, {
            codigo: 123,
            descricao: 'Editado',
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: `${obterAmanhaFormatado()}T00:00:00`,
            unidades: [1]
        });
        await flushPromises();
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('initiates a process (confirmation flow)', async () => {
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {codigo: 1, sigla: 'U1', nome: 'Unidade 1', isElegivel: true, filhas: []}
        ] as any);

        const {wrapper} = createWrapper();

        wrapper.vm.descricao = 'Iniciar teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);

        vi.mocked(processoService.criarProcesso).mockResolvedValue({codigo: 999} as any);

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');

        expect(processoService.criarProcesso).toHaveBeenCalled();
        await flushPromises();
        expect(processoService.iniciarProcesso).toHaveBeenCalledWith(999, 'MAPEAMENTO', [1]);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('initiates an existing process', async () => {
        unidadeStoreMock.garantirArvoreElegibilidade.mockResolvedValue([
            {codigo: 1, sigla: 'U1', nome: 'Unidade 1', isElegivel: true, filhas: []}
        ] as any);

        const {wrapper} = createWrapper();

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.descricao = 'Existente';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);
        await nextTick();

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');

        expect(processoService.criarProcesso).not.toHaveBeenCalled();
        expect(processoService.iniciarProcesso).toHaveBeenCalledWith(123, 'MAPEAMENTO', [1]);
    });

    it('removes a process', async () => {
        const {wrapper} = createWrapper();
        vi.mocked(processoService.excluirProcesso).mockResolvedValue(undefined);

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.mostrarModalRemocao = true;

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processoService.excluirProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('updates unit list when type changes', async () => {
        const {wrapper} = createWrapper();
        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        expect(unidadeStoreMock.garantirArvoreElegibilidade).toHaveBeenCalledWith('REVISAO', undefined);
    });

    it('remove unidades selecionadas inelegíveis ao trocar tipo para revisão', async () => {
        unidadeStoreMock.garantirArvoreElegibilidade.mockImplementation(async (tipoProcesso: string) => {
            if (tipoProcesso === 'MAPEAMENTO') {
                return [
                    {codigo: 1, sigla: 'ASSESSORIA_11', nome: 'A11', isElegivel: true, filhas: []},
                    {codigo: 2, sigla: 'ASSESSORIA_12', nome: 'A12', isElegivel: true, filhas: []}
                ] as any;
            }
            return [
                {codigo: 1, sigla: 'ASSESSORIA_11', nome: 'A11', isElegivel: false, filhas: []},
                {codigo: 2, sigla: 'ASSESSORIA_12', nome: 'A12', isElegivel: true, filhas: []}
            ] as any;
        });

        const {wrapper} = createWrapper();

        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();
        await flushPromises();

        wrapper.vm.unidadesSelecionadas = [1, 2];
        await nextTick();

        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        await flushPromises();

        expect(wrapper.vm.unidadesSelecionadas).toEqual([2]);
    });

    it('handles error when starting a new process (creation fail)', async () => {
        const {wrapper} = createWrapper();

        wrapper.vm.processoEditando = null;
        wrapper.vm.descricao = 'Novo processo fail';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.unidadesSelecionadas = [1];

        wrapper.vm.mostrarModalConfirmacao = true;
        await nextTick();

        vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi('Erro ao criar'));

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.summary).toContain('Erro ao criar');
    });

    it('handles error when starting a process (initiation fail)', async () => {
        const {wrapper} = createWrapper();

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.unidadesSelecionadas = [1];
        wrapper.vm.mostrarModalConfirmacao = true;
        await nextTick();

        vi.mocked(processoService.iniciarProcesso).mockRejectedValue(criarErroApi('Erro ao iniciar'));

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.summary).toContain('Erro ao iniciar');
    });

    it('handles error when removing a process', async () => {
        const {wrapper} = createWrapper();
        vi.mocked(processoService.excluirProcesso).mockRejectedValue(new Error('Erro remocao'));

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.mostrarModalRemocao = true;

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.message).toContain('Não foi possível remover o processo');
    });

    it('closes confirmation modal when cancel button is clicked', async () => {
        const {wrapper} = createWrapper();

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);

        await wrapper.find('[data-testid="btn-iniciar-processo-cancelar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(false);
    });

    it('maps field-specific validation errors from subErrors', async () => {
        const {wrapper} = createWrapper();

        vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi(
            'Erro de validação',
            [
                {field: 'descricao', message: 'Descrição é obrigatória'},
                {field: 'tipo', message: 'Tipo inválido'},
                {field: 'dataLimiteEtapa1', message: 'Data inválida'},
                {field: 'unidades', message: 'Selecione ao menos uma unidade'},
                {field: null, message: 'Erro genérico'}
            ],
        ));

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
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

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
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

    it('clears field errors when fields are updated', async () => {
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
        expect(wrapper.text()).toContain('Carregando unidades...');
    });

    it('shows alert with multiple errors', async () => {
        const {wrapper} = createWrapper();
        (wrapper.vm).notifyStructured('Corpo', ['Erro 1', 'Erro 2'], {variant: 'danger'});
        await nextTick();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.details).toContain('Erro 1');
        expect(wrapper.vm.notificacao?.notification?.details).toContain('Erro 2');
    });

    it('shows saving spinner on button', async () => {
        const {wrapper} = createWrapper();
        vi.mocked(processoService.criarProcesso).mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)) as any);

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        const btn = wrapper.find('[data-testid="btn-processo-salvar"]');
        await btn.trigger('click');

        expect(btn.text()).toContain('Salvando...'); // Button content might be replaced by "Salvando..."
    });

    it('handles error when loading process details on mount', async () => {
        mockRoute.query = {codProcesso: '123'};
        vi.mocked(processoService.obterDetalhesProcesso).mockRejectedValue(new Error('Load error'));

        const wrapper = mount(ProcessoCadastroView, {
            ...getCommonMountOptions({}, {
                BContainer: {template: '<div><slot /></div>'},
                BAlert: {template: '<div class="alert"><slot /></div>', props: ['modelValue', 'variant']},
                BForm: {template: '<form @submit.prevent><slot /></form>'},
                BFormGroup: {template: '<div><slot name="label">{{ label }}</slot><slot /></div>', props: ['label']},
                BFormInput: {template: '<input />', props: ['modelValue']},
                BFormSelect: {template: '<select></select>', props: ['modelValue', 'options'], inheritAttrs: false},
                BButton: {template: '<button></button>'},
                BModal: {template: '<div><slot /></div>'},
                BSpinner: {template: '<span></span>'},
                ArvoreUnidades: {template: '<div></div>', props: ['unidades', 'modelValue']},
                BFormInvalidFeedback: {template: '<div></div>'},
                LoadingButton: {template: '<button><slot /></button>'}
            })
        });

        await flushPromises();

        expect((wrapper.vm as any).notificacao?.message).toContain('Não foi possível carregar');
    });

    it('focuses on the first invalid field when validation errors occur', async () => {
        const {wrapper} = createWrapper();

        const focusMock = vi.fn();
        const mockElement = {focus: focusMock} as unknown as HTMLElement;
        const querySelectorSpy = vi.spyOn(document, 'querySelector').mockReturnValue(mockElement);

        vi.mocked(processoService.criarProcesso).mockRejectedValue(criarErroApi(
            'Erro de validação',
            [{field: 'descricao', message: 'Descrição é obrigatória'}],
        ));

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = obterAmanhaFormatado();
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');

        await flushPromises();
        await nextTick();
        await nextTick();

        expect(querySelectorSpy).toHaveBeenCalledWith('.is-invalid');
        expect(focusMock).toHaveBeenCalled();

        querySelectorSpy.mockRestore();
    });

});
