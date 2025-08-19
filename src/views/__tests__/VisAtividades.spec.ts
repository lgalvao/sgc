import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import VisAtividades from '../VisAtividades.vue';
import {useAtividadesStore} from '@/stores/atividades';
import {useUnidadesStore} from '@/stores/unidades';
import {useProcessosStore} from '@/stores/processos';
import {beforeEach, describe, expect, it, vi} from 'vitest';

describe('VisAtividades.vue', () => {
    let atividadesStore: ReturnType<typeof useAtividadesStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;
    let consoleSpy: ReturnType<typeof vi.spyOn>;

    beforeEach(() => {
        setActivePinia(createPinia());
        atividadesStore = useAtividadesStore();
        unidadesStore = useUnidadesStore();
        processosStore = useProcessosStore();

        vi.clearAllMocks();
        consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {
        });

        // Mock de dados iniciais para as stores
        unidadesStore.unidades = [
            {
                id: 1,
                sigla: 'UN1',
                nome: 'Unidade Teste 1',
                filhas: [],
                titular: 1,
                responsavel: null,
                tipo: 'OPERACIONAL'
            }
        ];
        processosStore.processosUnidade = [
            {
                id: 101,
                idProcesso: 1,
                unidade: 'UN1',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN1',
                unidadeAnterior: null
            },
        ];
        atividadesStore.atividades = [
            {
                id: 1,
                descricao: 'Atividade 1',
                idSubprocesso: 101,
                conhecimentos: [{id: 10, descricao: 'Conhecimento A'}]
            },
            {
                id: 2,
                descricao: 'Atividade 2',
                idSubprocesso: 101,
                conhecimentos: [{id: 20, descricao: 'Conhecimento B'}]
            },
        ];

        // Resetar o estado das stores
        atividadesStore.$reset();
        unidadesStore.$reset();
        processosStore.$reset();
    });

    it('deve renderizar corretamente os detalhes da unidade e o título', () => {
        const wrapper = mount(VisAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {plugins: [createPinia()]},
        });

        expect(wrapper.find('.unidade-sigla').text()).toBe('UN1');
        expect(wrapper.find('.unidade-nome').text()).toBe('Unidade Teste 1');
        expect(wrapper.find('h2').text()).toBe('Atividades e conhecimentos');
        expect(wrapper.find('button[title="Devolver para ajustes"]').exists()).toBe(true);
        expect(wrapper.find('button[title="Validar"]').exists()).toBe(true);
    });

    it('deve exibir as atividades e conhecimentos', async () => {
        const wrapper = mount(VisAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {plugins: [createPinia()]},
        });

        await wrapper.vm.$nextTick();
        const atividadesCards = wrapper.findAll('.atividade-card');
        expect(atividadesCards.length).toBe(2);

        expect(atividadesCards[0].find('[data-testid="atividade-descricao"]').text()).toBe('Atividade 1');
        expect(atividadesCards[0].find('[data-testid="conhecimento-descricao"]').text()).toBe('Conhecimento A');

        expect(atividadesCards[1].find('[data-testid="atividade-descricao"]').text()).toBe('Atividade 2');
        expect(atividadesCards[1].find('[data-testid="conhecimento-descricao"]').text()).toBe('Conhecimento B');
    });

    it('deve chamar validarCadastro ao clicar no botão "Validar"', async () => {
        const wrapper = mount(VisAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Validar"]').trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Validar cadastro');
    });

    it('deve chamar devolverCadastro ao clicar no botão "Devolver para ajustes"', async () => {
        const wrapper = mount(VisAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Devolver para ajustes"]').trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Devolver cadastro');
    });
});
