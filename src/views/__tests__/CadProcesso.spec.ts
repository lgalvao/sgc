import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import CadProcesso from '../CadProcesso.vue';
import {useProcessosStore} from '@/stores/processos';
import {useUnidadesStore} from '@/stores/unidades';
import {useRouter} from 'vue-router';
import {ProcessoTipo} from '@/types/tipos';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock do useRouter
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
    RouterLink: {
        template: '<a :href="to"><slot /></a>',
        props: ['to'],
    },
}));

describe('CadProcesso.vue', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let routerPushMock: ReturnType<typeof useRouter>['push'];

    beforeEach(() => {
        setActivePinia(createPinia());
        processosStore = useProcessosStore();
        unidadesStore = useUnidadesStore();
        routerPushMock = useRouter().push;

        vi.clearAllMocks();

        // Mock de dados iniciais para as stores
        unidadesStore.unidades = [
            {
                id: 101,
                sigla: 'UN1',
                nome: 'Unidade Teste 1',
                tipo: 'OPERACIONAL',
                titular: 1,
                responsavel: null,
                filhas: [
                    {
                        id: 102,
                        sigla: 'SUB1',
                        nome: 'Subunidade 1',
                        tipo: 'OPERACIONAL',
                        titular: 1,
                        responsavel: null,
                        filhas: []
                    },
                    {
                        id: 102,
                        sigla: 'SUB2',
                        nome: 'Subunidade 2',
                        tipo: 'OPERACIONAL',
                        titular: 1,
                        responsavel: null,
                        filhas: []
                    },
                ]
            },
            {
                id: 103,
                sigla: 'UN2',
                nome: 'Unidade Teste 2',
                tipo: 'INTERMEDIARIA',
                titular: 1,
                responsavel: null,
                filhas: []
            },
        ];
        processosStore.processos = [];
        processosStore.processosUnidade = [];

        // Espionar métodos das stores
        vi.spyOn(processosStore, 'adicionarProcesso');
        vi.spyOn(processosStore, 'adicionarProcessosUnidade');

        // Resetar o estado das stores para garantir isolamento entre os testes
        processosStore.$reset();
        unidadesStore.$reset();
    });

    it('deve renderizar corretamente os campos do formulário', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        expect(wrapper.find('h2').text()).toBe('Cadastro de processo');
        expect(wrapper.find('#descricao').exists()).toBe(true);
        expect(wrapper.find('#tipo').exists()).toBe(true);
        expect(wrapper.find('#dataLimite').exists()).toBe(true);
        expect(wrapper.find('button').text()).toContain('Salvar');
        expect(wrapper.find('button').text()).toContain('Iniciar processo');
        expect(wrapper.find('a').text()).toContain('Cancelar');
    });

    it('deve selecionar e deselecionar unidades', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        // Selecionar UN1 (pai)
        await wrapper.find('#chk-UN1').trigger('change');
        expect((wrapper.find('#chk-UN1').element as HTMLInputElement).checked).toBe(true);
        expect((wrapper.find('#chk-SUB1').element as HTMLInputElement).checked).toBe(true);
        expect((wrapper.find('#chk-SUB2').element as HTMLInputElement).checked).toBe(true);

        // Desselecionar SUB1
        await wrapper.find('#chk-SUB1').trigger('change');
        expect((wrapper.find('#chk-SUB1').element as HTMLInputElement).checked).toBe(false);
        expect((wrapper.find('#chk-UN1').element as HTMLInputElement).indeterminate).toBe(true);

        // Selecionar SUB1 novamente
        await wrapper.find('#chk-SUB1').trigger('change');
        expect((wrapper.find('#chk-SUB1').element as HTMLInputElement).checked).toBe(true);
        expect((wrapper.find('#chk-UN1').element as HTMLInputElement).checked).toBe(true);
        expect((wrapper.find('#chk-UN1').element as HTMLInputElement).indeterminate).toBe(false);
    });

    it('deve salvar um novo processo e redirecionar', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('#descricao').setValue('Processo de Teste');
        await wrapper.find('#tipo').setValue(ProcessoTipo.MAPEAMENTO);
        await wrapper.find('#dataLimite').setValue('2025-12-31');
        await wrapper.find('#chk-UN1').trigger('change'); // Seleciona UN1 e suas filhas

        await wrapper.find('button.btn-primary').trigger('click'); // Botão Salvar

        expect(processosStore.adicionarProcesso).toHaveBeenCalledWith(
            expect.objectContaining({
                descricao: 'Processo de Teste',
                tipo: ProcessoTipo.MAPEAMENTO,
                situacao: 'Não iniciado',
            })
        );
        expect(processosStore.adicionarProcessosUnidade).toHaveBeenCalledWith(
            expect.arrayContaining([
                expect.objectContaining({unidade: 'UN1'}),
                expect.objectContaining({unidade: 'SUB1'}),
                expect.objectContaining({unidade: 'SUB2'}),
            ])
        );
        expect(wrapper.find('.alert-info').text()).toBe('Processo salvo com sucesso!');
        await new Promise(resolve => setTimeout(resolve, 1000));
        expect(routerPushMock).toHaveBeenCalledWith('/painel');
    });

    it('não deve salvar processo com campos vazios', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button.btn-primary').trigger('click'); // Botão Salvar

        expect(processosStore.adicionarProcesso).not.toHaveBeenCalled();
        expect(processosStore.adicionarProcessosUnidade).not.toHaveBeenCalled();
        expect(wrapper.find('.alert-info').text()).toBe('Preencha todos os campos e selecione ao menos uma unidade.');
    });

    it('deve iniciar um novo processo e redirecionar', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('#descricao').setValue('Processo de Início');
        await wrapper.find('#tipo').setValue(ProcessoTipo.REVISAO);
        await wrapper.find('#dataLimite').setValue('2025-11-30');
        await wrapper.find('#chk-UN1').trigger('change'); // Seleciona UN1 e suas filhas

        await wrapper.find('button.btn-success').trigger('click'); // Botão Iniciar processo

        expect(processosStore.adicionarProcesso).toHaveBeenCalledWith(
            expect.objectContaining({
                descricao: 'Processo de Início',
                tipo: ProcessoTipo.REVISAO,
                situacao: 'Iniciado',
            })
        );
        expect(processosStore.adicionarProcessosUnidade).toHaveBeenCalledWith(
            expect.arrayContaining([
                expect.objectContaining({unidade: 'UN1', situacao: 'Aguardando preenchimento do mapa'}),
                expect.objectContaining({unidade: 'SUB1', situacao: 'Aguardando preenchimento do mapa'}),
                expect.objectContaining({unidade: 'SUB2', situacao: 'Aguardando preenchimento do mapa'}),
            ])
        );
        expect(wrapper.find('.alert-info').text()).toBe('Processo iniciado! Notificações enviadas às unidades.');
        await new Promise(resolve => setTimeout(resolve, 1200));
        expect(routerPushMock).toHaveBeenCalledWith('/painel');
    });

    it('deve redirecionar para o painel ao clicar em cancelar', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('a[to="/painel"]').trigger('click');
        expect(routerPushMock).toHaveBeenCalledWith('/painel');
    });

    it('não deve incluir unidades intermediárias nos processosUnidade', async () => {
        const wrapper = mount(CadProcesso, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('#descricao').setValue('Processo com Intermediaria');
        await wrapper.find('#tipo').setValue(ProcessoTipo.MAPEAMENTO);
        await wrapper.find('#dataLimite').setValue('2025-12-31');
        await wrapper.find('#chk-UN2').trigger('change'); // Seleciona UN2 (intermediária)

        await wrapper.find('button.btn-primary').trigger('click'); // Botão Salvar

        expect(processosStore.adicionarProcessosUnidade).toHaveBeenCalledWith([]); // UN2 não deve ser incluída
    });
});
