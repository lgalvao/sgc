import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Subprocesso from '../Subprocesso.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria';
import {useMapasStore} from '@/stores/mapas';
import {useServidoresStore} from '@/stores/servidores';
import {useProcessosStore} from '@/stores/processos';
import {useRoute, useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock do useRouter e useRoute
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
    useRoute: vi.fn(() => ({
        params: {idProcesso: 1, siglaUnidade: 'SESEL'},
    })),
}));

// Mock de servidores.json
vi.mock('@/mocks/servidores.json', () => ({
    default: [
        {id: 1, nome: 'Servidor Teste', unidade: 'SESEL', email: 'teste@email.com', ramal: '1234'},
        {id: 2, nome: 'Servidor Teste 2', unidade: 'SEDESENV', email: 'teste2@email.com', ramal: '5678'},
    ],
}));

// Mock de processos.json
vi.mock('@/mocks/processos.json', () => ({
    default: [
        {
            id: 1,
            descricao: 'Processo Mapeamento',
            tipo: 'Mapeamento',
            dataLimite: '2025-12-31',
            situacao: 'Em andamento'
        },
        {
            id: 2,
            descricao: 'Processo Diagnostico',
            tipo: 'Diagnostico',
            dataLimite: '2025-12-31',
            situacao: 'Em andamento'
        },
    ],
}));

// Mock de subprocessos.json
vi.mock('@/mocks/subprocessos.json', () => ({
    default: [
        {
            id: 101,
            idProcesso: 1,
            unidade: 'SESEL',
            dataLimiteEtapa1: '2025-12-20',
            dataLimiteEtapa2: '2025-12-25',
            dataFimEtapa1: null,
            dataFimEtapa2: null,
            situacao: 'Aguardando',
            unidadeAtual: 'SESEL',
            unidadeAnterior: null
        },
        {
            id: 102,
            idProcesso: 2,
            unidade: 'SEDESENV',
            dataLimiteEtapa1: '2025-12-20',
            dataLimiteEtapa2: '2025-12-25',
            dataFimEtapa1: null,
            dataFimEtapa2: null,
            situacao: 'Em andamento',
            unidadeAtual: 'SEDESENV',
            unidadeAnterior: null
        },
    ],
}));

// Mock de mapas.json
vi.mock('@/mocks/mapas.json', () => ({
    default: [
        {
            id: 1,
            unidade: 'SESEL',
            idProcesso: 1,
            situacao: 'em_andamento',
            competencias: [],
            dataCriacao: '2025-01-01',
            dataDisponibilizacao: null,
            dataFinalizacao: null
        },
        {
            id: 2,
            unidade: 'SEDESENV',
            idProcesso: 2,
            situacao: 'disponivel_validacao',
            competencias: [],
            dataCriacao: '2025-01-01',
            dataDisponibilizacao: '2025-01-10',
            dataFinalizacao: null
        },
    ],
}));

describe('Subprocesso.vue', () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let atribuicaoTemporariaStore: ReturnType<typeof useAtribuicaoTemporariaStore>;
    let mapasStore: ReturnType<typeof useMapasStore>;
    let servidoresStore: ReturnType<typeof useServidoresStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;
    let routerPushMock: ReturnType<typeof useRouter>['push'];
    let useRouteMock: ReturnType<typeof useRoute>;

    beforeEach(() => {
        setActivePinia(createPinia());
        unidadesStore = useUnidadesStore();
        atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
        mapasStore = useMapasStore();
        servidoresStore = useServidoresStore();
        processosStore = useProcessosStore();
        routerPushMock = useRouter().push;
        useRouteMock = useRoute();

        vi.clearAllMocks();

        // Resetar o estado das stores
        unidadesStore.$reset();
        atribuicaoTemporariaStore.$reset();
        mapasStore.$reset();
        servidoresStore.$reset();
        processosStore.$reset();
    });

    it('deve renderizar corretamente os detalhes da unidade e responsável', async () => {
        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 1, siglaUnidade: 'SESEL'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        expect(wrapper.find('h2').text()).toBe('SESEL - Seção de Sistemas Eleitorais');
        expect(wrapper.text()).toContain('Responsável: Servidor Teste');
        expect(wrapper.text()).toContain('Ramal: 1234');
        expect(wrapper.text()).toContain('E-mail: teste@email.com');
        expect(wrapper.text()).toContain('Situação: Aguardando');
        expect(wrapper.text()).toContain('Unidade Atual: SESEL');
    });

    it('deve exibir cards de Mapeamento/Revisão para o tipo de processo correspondente', async () => {
        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 1, siglaUnidade: 'SESEL'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Atividades e conhecimentos');
        expect(wrapper.text()).toContain('Mapa de Competências');
        expect(wrapper.text()).not.toContain('Diagnóstico da Equipe');
        expect(wrapper.text()).not.toContain('Ocupações Críticas');

        expect(wrapper.find('.card-actionable h5').text()).toBe('Atividades e conhecimentos');
        expect(wrapper.find('.card-actionable .badge').text()).toBe('Não disponibilizado');
        expect(wrapper.findAll('.card-actionable h5')[1].text()).toBe('Mapa de Competências');
        expect(wrapper.findAll('.card-actionable .badge')[1].text()).toBe('Em andamento');
    });

    it('deve exibir cards de Diagnóstico para o tipo de processo correspondente', async () => {
        // Mock useRoute para retornar idProcesso 2 e siglaUnidade SEDESENV
        (useRoute as vi.Mock).mockReturnValue({
            params: {idProcesso: 2, siglaUnidade: 'SEDESENV'},
        });

        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 2, siglaUnidade: 'SEDESENV'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).not.toContain('Atividades e conhecimentos');
        expect(wrapper.text()).not.toContain('Mapa de Competências');
        expect(wrapper.text()).toContain('Diagnóstico da Equipe');
        expect(wrapper.text()).toContain('Ocupações Críticas');

        expect(wrapper.find('.card-actionable h5').text()).toBe('Diagnóstico da Equipe');
        expect(wrapper.find('.card-actionable .badge').text()).toBe('Não disponibilizado');
        expect(wrapper.findAll('.card-actionable h5')[1].text()).toBe('Ocupações Críticas');
        expect(wrapper.findAll('.card-actionable .badge')[1].text()).toBe('Não disponibilizado');
    });

    it('deve navegar para SubprocessoCadastro ao clicar em "Atividades e conhecimentos"', async () => {
        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 1, siglaUnidade: 'SESEL'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        const cardAtividades = wrapper.findAll('.card-actionable')[0];
        expect(cardAtividades.exists()).toBe(true);
        await cardAtividades.trigger('click');
        expect(routerPushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: {idProcesso: 1, siglaUnidade: 'SESEL'},
        });
    });

    it('deve navegar para SubprocessoMapa (editar) ao clicar em "Mapa de Competências" em andamento', async () => {
        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 1, siglaUnidade: 'SESEL'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        const cardMapa = wrapper.findAll('.card-actionable')[1];
        expect(cardMapa.exists()).toBe(true);
        await cardMapa.trigger('click');
        expect(routerPushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {idProcesso: 1, siglaUnidade: 'SESEL'},
        });
    });

    it('deve navegar para SubprocessoMapa (criar) ao clicar em "Mapa de Competências" sem mapa existente', async () => {
        mapasStore.mapas = [];
        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 1, siglaUnidade: 'SESEL'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        const cardMapa = wrapper.findAll('.card-actionable')[1];
        expect(cardMapa.exists()).toBe(true);
        await cardMapa.trigger('click');
        expect(routerPushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {idProcesso: 1, siglaUnidade: 'SESEL'},
        });
    });

    it('deve navegar para SubprocessoVisMapa (visualizar) ao clicar em "Mapa de Competências" disponibilizado', async () => {
        // Mock useRoute para retornar idProcesso 2 e siglaUnidade SEDESENV
        (useRoute as vi.Mock).mockReturnValue({
            params: {idProcesso: 2, siglaUnidade: 'SEDESENV'},
        });

        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 2, siglaUnidade: 'SEDESENV'},
            global: {plugins: [createPinia()]},
        });
        await wrapper.vm.$nextTick();

        const cardMapa = wrapper.findAll('.card-actionable')[1];
        expect(cardMapa.exists()).toBe(true);
        await cardMapa.trigger('click');
        expect(routerPushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {idProcesso: 2, siglaUnidade: 'SEDESENV'},
        });
    });

    it('deve retornar a classe de badge correta para diferentes situações', () => {
        const wrapper = mount(Subprocesso, {
            props: {idProcesso: 1, siglaUnidade: 'SESEL'},
            global: {plugins: [createPinia()]},
        });

        const vm = wrapper.vm as any;

        expect(vm.badgeClass('Aguardando')).toBe('bg-warning text-dark');
        expect(vm.badgeClass('Em andamento')).toBe('bg-warning text-dark');
        expect(vm.badgeClass('Aguardando validação')).toBe('bg-warning text-dark');
        expect(vm.badgeClass('Finalizado')).toBe('bg-success');
        expect(vm.badgeClass('Validado')).toBe('bg-success');
        expect(vm.badgeClass('Devolvido')).toBe('bg-danger');
        expect(vm.badgeClass('Outra situação')).toBe('bg-secondary');
    });
});
