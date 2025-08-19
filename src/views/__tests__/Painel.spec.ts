import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Painel from '../Painel.vue';
import {usePerfilStore} from '@/stores/perfil';
import {useProcessosStore} from '@/stores/processos';
import {useAlertasStore} from '@/stores/alertas';
import {useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ProcessoTipo} from '@/types/tipos';

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

describe('Painel.vue', () => {
    let perfilStore: ReturnType<typeof usePerfilStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;
    let alertasStore: ReturnType<typeof useAlertasStore>;
    let routerPushMock: ReturnType<typeof useRouter>['push'];

    beforeEach(() => {
        setActivePinia(createPinia());
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        alertasStore = useAlertasStore();
        routerPushMock = useRouter().push;

        vi.clearAllMocks();

        // Mock de dados iniciais para as stores
        perfilStore.perfilSelecionado = 'ADMIN';
        processosStore.processos = [
            {
                id: 1,
                descricao: 'Processo A',
                tipo: ProcessoTipo.MAPEAMENTO,
                dataLimite: new Date(),
                situacao: 'Em andamento',
                dataFinalizacao: null
            },
            {
                id: 2,
                descricao: 'Processo B',
                tipo: ProcessoTipo.REVISAO,
                dataLimite: new Date(),
                situacao: 'Finalizado',
                dataFinalizacao: null
            },
        ];
        processosStore.processosUnidade = [
            {
                id: 101,
                idProcesso: 1,
                unidade: 'UN1',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Em andamento',
                unidadeAtual: 'UN1',
                unidadeAnterior: null
            },
            {
                id: 102,
                idProcesso: 1,
                unidade: 'UN2',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN2',
                unidadeAnterior: null
            },
            {
                id: 103,
                idProcesso: 2,
                unidade: 'UN3',
                situacao: 'Finalizado',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN3',
                unidadeAnterior: null
            },
        ];
        alertasStore.alertas = [
            {
                id: 1,
                unidadeOrigem: 'COSIS',
                unidadeDestino: 'SESEL',
                dataHora: new Date('2025-07-02T10:00:00'),
                idProcesso: 1,
                descricao: 'Cadastro devolvido para ajustes'
            },
            {
                id: 2,
                unidadeOrigem: 'SEDOC',
                unidadeDestino: 'SEDESENV',
                dataHora: new Date('2025-07-03T14:30:00'),
                idProcesso: 2,
                descricao: 'Prazo próximo para validação do mapa de competências'
            },
        ];

        // Resetar o estado das stores
        perfilStore.$reset();
        processosStore.$reset();
        alertasStore.$reset();
    });

    it('deve renderizar corretamente os títulos e tabelas', () => {
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        expect(wrapper.find('[data-testid="titulo-processos"]').text()).toBe('Processos');
        expect(wrapper.find('[data-testid="titulo-alertas"]').text()).toBe('Alertas');
        expect(wrapper.find('[data-testid="tabela-processos"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="tabela-alertas"]').exists()).toBe(true);
    });

    it('deve exibir o botão "Criar processo" para o perfil ADMIN', async () => {
        perfilStore.perfilSelecionado = 'ADMIN';
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });
        expect(wrapper.find('[data-testid="btn-criar-processo"]').exists()).toBe(true);
    });

    it('não deve exibir o botão "Criar processo" para outros perfis', async () => {
        perfilStore.perfilSelecionado = 'GESTOR';
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });
        expect(wrapper.find('[data-testid="btn-criar-processo"]').exists()).toBe(false);
    });

    it('deve exibir os processos na tabela', async () => {
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        const rows = wrapper.findAll('[data-testid="tabela-processos"] tbody tr');
        expect(rows.length).toBe(2);
        expect(rows[0].text()).toContain('Processo A');
        expect(rows[0].text()).toContain('Mapeamento');
        expect(rows[0].text()).toContain('UN1, UN2');
        expect(rows[0].text()).toContain('Em andamento');
    });

    it('deve ordenar os processos por descrição (ascendente e descendente)', async () => {
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        // Padrão: ascendente por descrição
        let rows = wrapper.findAll('[data-testid="tabela-processos"] tbody tr');
        expect(rows[0].text()).toContain('Processo A');
        expect(rows[1].text()).toContain('Processo B');

        // Clicar para ordenar descendente
        await wrapper.find('[data-testid="coluna-descricao"]').trigger('click');
        rows = wrapper.findAll('[data-testid="tabela-processos"] tbody tr');
        expect(rows[0].text()).toContain('Processo B');
        expect(rows[1].text()).toContain('Processo A');
    });

    it('deve redirecionar para os detalhes do processo ao clicar na linha', async () => {
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        const firstRow = wrapper.findAll('[data-testid="tabela-processos"] tbody tr')[0];
        await firstRow.trigger('click');

        expect(routerPushMock).toHaveBeenCalledWith({name: 'Processo', params: {idProcesso: 1}});
    });

    it('deve exibir os alertas na tabela', async () => {
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        const rows = wrapper.findAll('[data-testid="tabela-alertas"] tbody tr');
        expect(rows.length).toBe(2);
        expect(rows[0].text()).toContain('Cadastro devolvido para ajustes');
        expect(rows[0].text()).toContain('COSIS');
        expect(rows[0].text()).toContain('Processo A');
    });

    it('deve exibir "Nenhum alerta no momento." quando não há alertas', async () => {
        alertasStore.alertas = [];
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        expect(wrapper.find('[data-testid="tabela-alertas"]').text()).toContain('Nenhum alerta no momento.');
    });

    it('deve redirecionar para a página de criação de processo ao clicar no botão', async () => {
        perfilStore.perfilSelecionado = 'ADMIN';
        const wrapper = mount(Painel, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('[data-testid="btn-criar-processo"]').trigger('click');
        expect(routerPushMock).toHaveBeenCalledWith({name: 'CadProcesso'});
    });
});