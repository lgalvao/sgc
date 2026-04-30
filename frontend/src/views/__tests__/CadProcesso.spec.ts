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
                    LayoutPadrao: true,
                    ArvoreUnidadesElegiveis: ArvoreUnidadesStub,
                    BContainer: true,
                    BRow: true,
                    BCol: true,
                    BCard: true,
                    BCardBody: true,
                    BForm: true,
                    BFormGroup: true,
                    BFormInput: true,
                    BFormSelect: true,
                    BFormCheckbox: true,
                    BButton: true,
                    BAlert: true,
                    BSpinner: true,
                    PageHeader: true,
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

        expect(wrapper.find('h1').text()).toBe('Cadastro de processo');
        expect(unidadeStoreMock.garantirArvoreElegibilidade).toHaveBeenCalled();
    });

    it('loads process details when codProcesso is present in query', async () => {
        mockRoute.query = {codProcesso: '123'};
        const mockProcesso = {codigo: 123, descricao: 'P1', tipo: 'MAPEAMENTO', dataLimite: '2024-12-31', unidades: []};
        
        const {wrapper} = createWrapper({
            perfil: {permissoes: permissoesAdmin},
            processos: {processoDetalhe: mockProcesso}
        });
        await flushPromises();

        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(123);
        expect(wrapper.vm.descricao).toBe('P1');
    });

    it('validates form before saving', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        await wrapper.find('[data-testid="btn-processo-salvar-rodape"]').trigger('click');
        
        expect(wrapper.vm.fieldErrors.descricao).toBe('Informe a descrição do processo.');
        expect(wrapper.vm.fieldErrors.tipo).toBe('Selecione o tipo do processo.');
        expect(wrapper.vm.fieldErrors.dataLimite).toBe('Informe a data limite.');
        expect(wrapper.vm.fieldErrors.unidades).toBe('Selecione ao menos uma unidade participante.');
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
        const mockProcesso = {codigo: 123, descricao: 'P1', tipo: 'MAPEAMENTO', dataLimite: '2024-12-31', unidades: [1]};
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
        const mockProcesso = {codigo: 123, descricao: 'P1'};
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();
        
        vi.mocked(processoService.excluirProcesso).mockResolvedValue({} as any);

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processoService.excluirProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('handles startProcess correctly', async () => {
        const mockProcesso = {codigo: 123, descricao: 'P1'};
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();
        
        vi.mocked(processoService.iniciarProcesso).mockResolvedValue({} as any);

        await wrapper.vm.confirmarInicio();
        await flushPromises();

        expect(processoService.iniciarProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('resets units when type changes to REVISAO and no units were previously selected', async () => {
        const {wrapper} = createWrapper();
        wrapper.vm.unidadesSelecionadas = [1];
        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        // Since it was a new process, the snapshot was empty
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);
    });

    it('restores units from snapshot when type changes back to original', async () => {
        const mockProcesso = {codigo: 123, tipo: 'MAPEAMENTO', unidades: [1]};
        const {wrapper} = createWrapper({processos: {processoDetalhe: mockProcesso}});
        await flushPromises();

        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([]);

        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();
        expect(wrapper.vm.unidadesSelecionadas).toEqual([1]);
    });
});
