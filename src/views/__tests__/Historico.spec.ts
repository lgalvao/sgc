import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Historico from '../Historico.vue';
import {useProcessosStore} from '@/stores/processos';
import {useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ProcessoTipo} from '@/types/tipos';

// Mock do useRouter
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
}));

describe('Historico.vue', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;
    let routerPushMock: ReturnType<typeof useRouter>['push'];

    beforeEach(() => {
        setActivePinia(createPinia());
        processosStore = useProcessosStore();
        routerPushMock = useRouter().push;

        vi.clearAllMocks();

        // Mock de dados iniciais para a store de processos
        processosStore.processos = [
            {
                id: 1,
                descricao: 'Processo A',
                tipo: ProcessoTipo.MAPEAMENTO,
                dataLimite: new Date(),
                situacao: 'Finalizado',
                dataFinalizacao: new Date('2025-01-10')
            },
            {
                id: 2,
                descricao: 'Processo B',
                tipo: ProcessoTipo.REVISAO,
                dataLimite: new Date(),
                situacao: 'Em andamento',
                dataFinalizacao: null
            },
            {
                id: 3,
                descricao: 'Processo C',
                tipo: ProcessoTipo.DIAGNOSTICO,
                dataLimite: new Date(),
                situacao: 'Finalizado',
                dataFinalizacao: new Date('2025-01-05')
            },
            {
                id: 4,
                descricao: 'Processo D',
                tipo: ProcessoTipo.MAPEAMENTO,
                dataLimite: new Date(),
                situacao: 'Finalizado',
                dataFinalizacao: null
            }, // Data finalização nula
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
                situacao: 'Finalizado',
                unidadeAtual: 'UN1',
                unidadeAnterior: null
            },
            {
                id: 102,
                idProcesso: 1,
                unidade: 'UN2',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Finalizado',
                unidadeAtual: 'UN2',
                unidadeAnterior: null
            },
            {
                id: 103,
                idProcesso: 3,
                unidade: 'UN3',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Finalizado',
                unidadeAtual: 'UN3',
                unidadeAnterior: null
            },
            {
                id: 104,
                idProcesso: 4,
                unidade: 'UN4',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                situacao: 'Finalizado',
                unidadeAtual: 'UN4',
                unidadeAnterior: null
            },
        ];

        processosStore.$reset();
    });

    it('deve renderizar corretamente o título e a tabela', () => {
        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        expect(wrapper.find('h2').text()).toBe('Histórico de processos');
        expect(wrapper.find('table').exists()).toBe(true);
        expect(wrapper.findAll('th').length).toBe(4);
    });

    it('deve exibir apenas processos finalizados', async () => {
        processosStore.processos = [
            {
                id: 2,
                descricao: 'Processo B',
                tipo: ProcessoTipo.REVISAO,
                dataLimite: new Date(),
                situacao: 'Em andamento',
                dataFinalizacao: null
            },
        ];

        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        expect(wrapper.find('.alert-info').text()).toBe('Nenhum processo finalizado.');
        expect(wrapper.find('table').exists()).toBe(false);
    });

    it('deve exibir mensagem "Nenhum processo finalizado." quando não há processos finalizados', async () => {
        processosStore.processos = [
            {
                id: 2,
                descricao: 'Processo B',
                tipo: ProcessoTipo.REVISAO,
                dataLimite: new Date(),
                situacao: 'Em andamento',
                dataFinalizacao: null
            },
        ];

        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        expect(wrapper.find('.alert-info').text()).toBe('Nenhum processo finalizado.');
        expect(wrapper.find('table').exists()).toBe(false);
    });

    it('deve ordenar por descrição (ascendente e descendente)', async () => {
        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        // Ordenar por descrição ascendente (padrão)
        let rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('Processo A');
        expect(rows[1].text()).toContain('Processo C');
        expect(rows[2].text()).toContain('Processo D');

        // Clicar para ordenar descendente
        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(1)').trigger('click');
        rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('Processo D');
        expect(rows[1].text()).toContain('Processo C');
        expect(rows[2].text()).toContain('Processo A');
    });

    it('deve ordenar por tipo (ascendente e descendente)', async () => {
        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(2)').trigger('click');
        let rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('Processo C');
        expect(rows[1].text()).toContain('Processo A');
        expect(rows[2].text()).toContain('Processo D');

        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(2)').trigger('click');
        rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('Processo A');
        expect(rows[1].text()).toContain('Processo D');
        expect(rows[2].text()).toContain('Processo C');
    });

    it('deve ordenar por unidades participantes (ascendente e descendente)', async () => {
        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(3)').trigger('click');
        let rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('UN1, UN2');
        expect(rows[1].text()).toContain('UN3');
        expect(rows[2].text()).toContain('UN4');

        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(3)').trigger('click');
        rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('UN4');
        expect(rows[1].text()).toContain('UN3');
        expect(rows[2].text()).toContain('UN1, UN2');
    });

    it('deve ordenar por data de finalização (ascendente e descendente), tratando nulos', async () => {
        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(4)').trigger('click');
        let rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('Processo D');
        expect(rows[1].text()).toContain('Processo C');
        expect(rows[2].text()).toContain('Processo A');

        await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(4)').trigger('click');
        rows = wrapper.findAll('tbody tr');
        expect(rows[0].text()).toContain('Processo A');
        expect(rows[1].text()).toContain('Processo C');
        expect(rows[2].text()).toContain('Processo D');
    });

    it('deve redirecionar para os detalhes do processo ao clicar na linha', async () => {
        const wrapper = mount(Historico, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();
        const firstRow = wrapper.findAll('tbody tr')[0];
        await firstRow.trigger('click');

        expect(routerPushMock).toHaveBeenCalledWith({name: 'Processo', params: {idProcesso: 1}});
    });
});