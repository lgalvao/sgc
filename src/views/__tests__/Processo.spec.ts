import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Processo from '../Processo.vue';
import {useProcessosStore} from '@/stores/processos';
import {useUnidadesStore} from '@/stores/unidades';
import {usePerfilStore} from '@/stores/perfil';
import {useRoute, useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ProcessoTipo} from '@/types/tipos';

// Mock do useRouter e useRoute
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
    useRoute: vi.fn(() => ({
        params: {idProcesso: 1}, // Valor padrão para idProcesso
    })),
}));

// Mock do componente TreeTable
const mockTreeTable = {
    template: '<div><slot></slot></div>',
    props: ['data', 'columns', 'title', 'hideHeaders'],
    emits: ['row-click'],
};

describe('Processo.vue', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let perfilStore: ReturnType<typeof usePerfilStore>;
    let routerPushMock: ReturnType<typeof useRouter>['push'];
    let useRouteMock: ReturnType<typeof useRoute>;

    beforeEach(() => {
        setActivePinia(createPinia());
        processosStore = useProcessosStore();
        unidadesStore = useUnidadesStore();
        perfilStore = usePerfilStore();
        routerPushMock = useRouter().push;
        useRouteMock = useRoute();

        vi.clearAllMocks();

        // Mock de dados iniciais para as stores
        processosStore.processos = [
            {
                id: 1,
                descricao: 'Processo Teste',
                tipo: ProcessoTipo.MAPEAMENTO,
                dataLimite: new Date('2025-12-31'),
                situacao: 'Em andamento',
                dataFinalizacao: null as Date | null
            },
            {
                id: 2,
                descricao: 'Processo Finalizado',
                tipo: ProcessoTipo.REVISAO,
                dataLimite: new Date('2025-12-31'),
                situacao: 'Finalizado',
                dataFinalizacao: null as Date | null
            },
        ];
        processosStore.processosUnidade = [
            {
                id: 101,
                idProcesso: 1,
                unidade: 'SESEL',
                dataLimiteEtapa1: new Date('2025-12-20'),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Em andamento',
                unidadeAtual: 'SESEL',
                unidadeAnterior: null
            },
            {
                id: 102,
                idProcesso: 1,
                unidade: 'SEDESENV',
                dataLimiteEtapa1: new Date('2025-12-25'),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Não iniciado',
                unidadeAtual: 'SEDESENV',
                unidadeAnterior: null
            },
            {
                id: 103,
                idProcesso: 1,
                unidade: 'COSIS',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Não iniciado',
                unidadeAtual: 'COSIS',
                unidadeAnterior: null
            },
        ];
        unidadesStore.unidades = [
            {
                id: 100, sigla: 'SEDOC', nome: 'SEDOC', tipo: 'ADMINISTRATIVA', titular: 1, responsavel: null, filhas: [
                    {
                        id: 201,
                        sigla: 'STIC',
                        nome: 'STIC',
                        tipo: 'INTEROPERACIONAL',
                        titular: 1,
                        responsavel: null,
                        filhas: [
                            {
                                id: 103,
                                sigla: 'COSIS',
                                nome: 'COSIS',
                                tipo: 'INTERMEDIARIA',
                                titular: 1,
                                responsavel: null,
                                filhas: [
                                    {
                                        id: 101,
                                        sigla: 'SESEL',
                                        nome: 'SESEL',
                                        tipo: 'OPERACIONAL',
                                        titular: 1,
                                        responsavel: null,
                                        filhas: []
                                    },
                                    {
                                        id: 102,
                                        sigla: 'SEDESENV',
                                        nome: 'SEDESENV',
                                        tipo: 'OPERACIONAL',
                                        titular: 1,
                                        responsavel: null,
                                        filhas: []
                                    },
                                ]
                            },
                        ]
                    },
                ]
            },
        ];
        perfilStore.perfilSelecionado = 'ADMIN';

        // Espionar métodos
        vi.spyOn(processosStore, 'finalizarProcesso');
        // @ts-ignore
        vi.spyOn(window, 'confirm').mockReturnValue(true);

        // Resetar o estado das stores
        processosStore.$reset();
        unidadesStore.$reset();
        perfilStore.$reset();
    });

    it('deve renderizar corretamente os detalhes do processo', async () => {
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });

        expect(wrapper.find('h2').text()).toBe('Processo Teste');
        expect(wrapper.text()).toContain('Tipo: Mapeamento');
        expect(wrapper.text()).toContain('Situação: Em andamento');
        expect(wrapper.findComponent(mockTreeTable).exists()).toBe(true);
    });

    it('deve exibir o botão "Finalizar processo" para o perfil ADMIN', async () => {
        perfilStore.perfilSelecionado = 'ADMIN';
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });
        expect(wrapper.find('button.btn-danger').exists()).toBe(true);
    });

    it('não deve exibir o botão "Finalizar processo" para outros perfis', async () => {
        perfilStore.perfilSelecionado = 'GESTOR';
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });
        expect(wrapper.find('button.btn-danger').exists()).toBe(false);
    });

    it('deve chamar finalizarProcesso e redirecionar ao clicar no botão', async () => {
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });

        await wrapper.find('button.btn-danger').trigger('click');

        expect(window.confirm).toHaveBeenCalledWith('Tem certeza que deseja finalizar este processo?');
        expect(processosStore.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(routerPushMock).toHaveBeenCalledWith('/painel');
    });

    it('não deve finalizar o processo se o usuário cancelar o confirm', async () => {
        // @ts-ignore
        (window.confirm as vi.Mock).mockReturnValue(false);

        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });

        await wrapper.find('button.btn-danger').trigger('click');

        expect(window.confirm).toHaveBeenCalledWith('Tem certeza que deseja finalizar este processo?');
        expect(processosStore.finalizarProcesso).not.toHaveBeenCalled();
        expect(routerPushMock).not.toHaveBeenCalled();
    });

    it('deve formatar corretamente os dados para a TreeTable', async () => {
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });

        const treeTableProps = wrapper.findComponent(mockTreeTable).props();
        const data = treeTableProps.data;

        expect(data.length).toBe(1);
        expect(data[0].nome).toBe('SEDOC - SEDOC');
        expect(data[0].children.length).toBe(1);
        expect(data[0].children[0].nome).toBe('STIC - STIC');
        expect(data[0].children[0].children.length).toBe(1);
        expect(data[0].children[0].children[0].nome).toBe('COSIS - COSIS');

        const cosis = data[0].children[0].children[0];
        expect(cosis.nome).toBe('COSIS - COSIS');
        expect(cosis.situacao).toBe('');
        expect(cosis.dataLimite).toBe('');
        expect(cosis.unidadeAtual).toBe('');
        expect(cosis.clickable).toBe(false);

        const sesel = cosis.children.find((c: any) => c.id === 'SESEL');
        expect(sesel.nome).toBe('SESEL - SESEL');
        expect(sesel.situacao).toBe('Em andamento');
        expect(sesel.dataLimite).toBe('20/12/2025');
        expect(sesel.unidadeAtual).toBe('SESEL');
        expect(sesel.clickable).toBe(true);
    });

    it('deve navegar para subprocesso ao clicar em unidade participante', async () => {
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });

        const seselItem = {
            id: 'SESEL',
            nome: 'SESEL - SESEL',
            situacao: 'Em andamento',
            dataLimite: '20/12/2025',
            unidadeAtual: 'SESEL',
            clickable: true,
            children: [],
        };
        wrapper.findComponent(mockTreeTable).vm.$emit('row-click', seselItem);

        expect(routerPushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {idProcesso: 1, siglaUnidade: 'SESEL'},
        });
    });

    it('não deve navegar ao clicar em unidade intermediária', async () => {
        const wrapper = mount(Processo, {
            global: {
                plugins: [createPinia()],
                stubs: {TreeTable: mockTreeTable},
            },
        });

        const cosisItem = {
            id: 'COSIS',
            nome: 'COSIS - COSIS',
            situacao: '',
            dataLimite: '',
            unidadeAtual: '',
            clickable: false,
            children: [],
        };
        wrapper.findComponent(mockTreeTable).vm.$emit('row-click', cosisItem);

        expect(routerPushMock).not.toHaveBeenCalled();
    });
});