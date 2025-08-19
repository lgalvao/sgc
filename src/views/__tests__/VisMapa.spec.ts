import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import VisMapa from '../VisMapa.vue';
import {useMapasStore} from '@/stores/mapas';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtividadesStore} from '@/stores/atividades';
import {useProcessosStore} from '@/stores/processos';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock do useRoute
vi.mock('vue-router', () => ({
    useRoute: vi.fn(() => ({
        params: {idProcesso: 1, sigla: 'SESEL'},
    })),
}));

describe('VisMapa.vue', () => {
    let mapasStore: ReturnType<typeof useMapasStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let atividadesStore: ReturnType<typeof useAtividadesStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;
    let consoleSpy: ReturnType<typeof vi.spyOn>;

    beforeEach(() => {
        setActivePinia(createPinia());
        mapasStore = useMapasStore();
        unidadesStore = useUnidadesStore();
        atividadesStore = useAtividadesStore();
        processosStore = useProcessosStore();

        vi.clearAllMocks();
        consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {
        });

        // Mock de dados iniciais para as stores
        unidadesStore.unidades = [
            {
                id: 1,
                sigla: 'SESEL',
                nome: 'Seção de Sistemas Eleitorais',
                titular: 1,
                responsavel: null,
                tipo: 'OPERACIONAL',
                filhas: []
            },
        ];
        processosStore.processosUnidade = [
            {
                id: 101,
                idProcesso: 1,
                unidade: 'SESEL',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'SESEL',
                unidadeAnterior: null
            },
        ];
        atividadesStore.atividades = [
            {
                id: 6,
                descricao: 'Atividade 6',
                idSubprocesso: 101,
                conhecimentos: [{id: 60, descricao: 'Conhecimento 60'}]
            },
            {
                id: 1,
                descricao: 'Atividade 1',
                idSubprocesso: 101,
                conhecimentos: [{id: 10, descricao: 'Conhecimento 10'}]
            },
            {
                id: 4,
                descricao: 'Atividade 4',
                idSubprocesso: 101,
                conhecimentos: [{id: 40, descricao: 'Conhecimento 40'}]
            },
            {
                id: 5,
                descricao: 'Atividade 5',
                idSubprocesso: 101,
                conhecimentos: [{id: 50, descricao: 'Conhecimento 50'}]
            },
        ];
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'SESEL',
                idProcesso: 1,
                situacao: 'em_andamento',
                competencias: [
                    {id: 1, descricao: 'Competência A', atividadesAssociadas: [6, 1]},
                    {id: 2, descricao: 'Competência B', atividadesAssociadas: [4, 5]},
                ],
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        // Resetar o estado das stores
        mapasStore.$reset();
        unidadesStore.$reset();
        atividadesStore.$reset();
        processosStore.$reset();
    });

    it('deve renderizar corretamente o título e os detalhes da unidade', () => {
        const wrapper = mount(VisMapa, {
            global: {plugins: [createPinia()]},
        });

        expect(wrapper.find('.display-6').text()).toBe('Mapa de competências técnicas');
        expect(wrapper.find('.fs-5').text()).toBe('SESEL - Seção de Sistemas Eleitorais');
        expect(wrapper.find('button[title="Devolver para ajustes"]').exists()).toBe(true);
        expect(wrapper.find('button[title="Validar"]').exists()).toBe(true);
    });

    it('deve exibir as competências, atividades e conhecimentos', async () => {
        const wrapper = mount(VisMapa, {
            global: {plugins: [createPinia()]},
        });

        await wrapper.vm.$nextTick();
        const competenciasCards = wrapper.findAll('[data-testid="competencia-item"]');
        expect(competenciasCards.length).toBe(2);

        // Competência A
        expect(competenciasCards[0].find('[data-testid="competencia-descricao"]').text()).toBe('Competência A');
        const atividadesCompA = competenciasCards[0].findAll('.atividade-associada-card-item');
        expect(atividadesCompA.length).toBe(2);
        expect(atividadesCompA[0].find('.atividade-associada-descricao').text()).toBe('Atividade 6');
        expect(atividadesCompA[0].find('.conhecimentos-atividade').text()).toContain('Conhecimento 60');
        expect(atividadesCompA[1].find('.atividade-associada-descricao').text()).toBe('Atividade 1');
        expect(atividadesCompA[1].find('.conhecimentos-atividade').text()).toContain('Conhecimento 10');

        // Competência B
        expect(competenciasCards[1].find('[data-testid="competencia-descricao"]').text()).toBe('Competência B');
        const atividadesCompB = competenciasCards[1].findAll('.atividade-associada-card-item');
        expect(atividadesCompB.length).toBe(2);
        expect(atividadesCompB[0].find('.atividade-associada-descricao').text()).toBe('Atividade 4');
        expect(atividadesCompB[0].find('.conhecimentos-atividade').text()).toContain('Conhecimento 40');
        expect(atividadesCompB[1].find('.atividade-associada-descricao').text()).toBe('Atividade 5');
        expect(atividadesCompB[1].find('.conhecimentos-atividade').text()).toContain('Conhecimento 50');
    });

    it('deve exibir "Nenhuma competência cadastrada." quando não há competências', async () => {
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'SESEL',
                idProcesso: 1,
                situacao: 'em_andamento',
                competencias: [],
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        const wrapper = mount(VisMapa, {
            global: {plugins: [createPinia()]},
        });

        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain('Nenhuma competência cadastrada.');
    });

    it('deve chamar validarCadastro ao clicar no botão "Validar"', async () => {
        const wrapper = mount(VisMapa, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Validar"]').trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Validar cadastro de mapa');
    });

    it('deve chamar devolverCadastro ao clicar no botão "Devolver para ajustes"', async () => {
        const wrapper = mount(VisMapa, {
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Devolver para ajustes"]').trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Devolver cadastro de mapa');
    });
});