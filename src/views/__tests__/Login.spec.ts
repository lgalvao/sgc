import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Login from '../Login.vue';
import {useServidoresStore} from '@/stores/servidores';
import {usePerfilStore} from '@/stores/perfil';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria';
import {usePerfil} from '@/composables/usePerfil';
import {ref, Ref} from 'vue';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useRouter} from "vue-router";

// Mock do useRouter
vi.mock('vue-router', async () => {
    const actual = await vi.importActual('vue-router');
    return {
        ...actual,
        useRouter: vi.fn(() => ({
            push: vi.fn(),
        })),
    };
});

// Mock do usePerfil composable
vi.mock('@/composables/usePerfil', () => ({
    usePerfil: vi.fn(() => ({
        servidoresComPerfil: ref([
            {id: 1, nome: 'Ana Paula Souza', unidade: 'SESEL', perfil: 'GESTOR'},
            {id: 4, nome: 'João Batista Silva', unidade: 'SESEL', perfil: 'SERVIDOR'},
            {id: 9, nome: 'Giuseppe Corleone', unidade: 'SEDESENV', perfil: 'CHEFE'},
            {id: 10, nome: 'Paula Gonçalves', unidade: 'SEDIA', perfil: 'ADMIN'},
        ]) as Ref<Array<{ id: number; nome: string; unidade: string; perfil: string }>>,
    })),
}));

describe('Login.vue', () => {
    let servidoresStore: ReturnType<typeof useServidoresStore>;
    let perfilStore: ReturnType<typeof usePerfilStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let atribuicaoTemporariaStore: ReturnType<typeof useAtribuicaoTemporariaStore>;
    let routerPushMock: ReturnType<typeof import('vue-router').useRouter>['push']; // Corrigido: tipagem do useRouter

    beforeEach(() => {
        setActivePinia(createPinia());
        servidoresStore = useServidoresStore();
        perfilStore = usePerfilStore();
        unidadesStore = useUnidadesStore();
        atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
        routerPushMock = useRouter().push;

        vi.clearAllMocks();

        // Mock de dados iniciais para as stores
        servidoresStore.servidores = [
            {id: 1, nome: 'Ana Paula Souza', unidade: 'SESEL', email: null, ramal: null},
            {id: 4, nome: 'João Batista Silva', unidade: 'SESEL', email: null, ramal: null},
            {id: 9, nome: 'Giuseppe Corleone', unidade: 'SEDESENV', email: null, ramal: null},
            {id: 10, nome: 'Paula Gonçalves', unidade: 'SEDIA', email: null, ramal: null},
        ];
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
            {
                id: 2,
                sigla: 'SEDESENV',
                nome: 'Seção de Desenvolvimento de Sistemas',
                titular: 9,
                responsavel: null,
                tipo: 'OPERACIONAL',
                filhas: []
            },
            {
                id: 3,
                sigla: 'SEDIA',
                nome: 'Seção de Dados de Inteligência Artificial',
                titular: 10,
                responsavel: null,
                tipo: 'OPERACIONAL',
                filhas: []
            },
        ];
        atribuicaoTemporariaStore.atribuicoes = [];

        // Espionar métodos das stores
        vi.spyOn(perfilStore, 'setServidorId');
        vi.spyOn(perfilStore, 'setPerfilUnidade');

        // Mock do alert
        vi.spyOn(window, 'alert').mockImplementation(() => {
        });

        // Resetar o estado das stores
        servidoresStore.$reset();
        perfilStore.$reset();
        unidadesStore.$reset();
        atribuicaoTemporariaStore.$reset();
    });

    it('deve renderizar corretamente os campos de login', () => {
        const wrapper = mount(Login);

        expect(wrapper.find('h2').text()).toBe('SGC');
        expect(wrapper.find('h5').text()).toBe('Sistema de Gestão de Competências');
        expect(wrapper.find('#titulo').exists()).toBe(true);
        expect(wrapper.find('#senha').exists()).toBe(true);
        expect(wrapper.find('button[type="submit"]').text()).toBe('Entrar');
        expect(wrapper.find('#par').exists()).toBe(false);
    });

    it('deve fazer login com sucesso para um usuário com um único perfil/unidade', async () => {
        const wrapper = mount(Login);

        await wrapper.find('#titulo').setValue('9');
        await wrapper.find('#senha').setValue('123');
        await wrapper.find('button[type="submit"]').trigger('submit');

        expect(perfilStore.setServidorId).toHaveBeenCalledWith(9);
        expect(perfilStore.setPerfilUnidade).toHaveBeenCalledWith('CHEFE', 'SEDESENV');
        expect(routerPushMock).toHaveBeenCalledWith('/painel');
        expect(window.alert).not.toHaveBeenCalled();
    });

    it('deve exibir seletor de perfil/unidade para usuário com múltiplos perfis/unidades', async () => {
        // Adicionar uma atribuição temporária para o servidor 1 (Ana Paula Souza)
        atribuicaoTemporariaStore.atribuicoes = [
            {unidade: 'SEDIA', servidorId: 1, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'Teste'}, // Corrigido: removido 'id'
        ];

        const wrapper = mount(Login);

        await wrapper.find('#titulo').setValue('1');
        await wrapper.find('#senha').setValue('123');
        await wrapper.find('button[type="submit"]').trigger('submit');

        expect(wrapper.find('#par').exists()).toBe(true);
        const options = wrapper.findAll('#par option');
        expect(options.length).toBe(2);
        expect(options[0].text()).toBe('GESTOR - SESEL');
        expect(options[1].text()).toBe('SERVIDOR - SEDIA');

        // Selecionar o segundo par e finalizar login
        await wrapper.find('#par').setValue(options[1].attributes().value);
        await wrapper.find('button[type="submit"]').trigger('submit');

        expect(perfilStore.setServidorId).toHaveBeenCalledWith(1);
        expect(perfilStore.setPerfilUnidade).toHaveBeenCalledWith('SERVIDOR', 'SEDIA');
        expect(routerPushMock).toHaveBeenCalledWith('/painel');
        expect(window.alert).not.toHaveBeenCalled();
    });

    it('deve exibir alerta se campos de login estiverem vazios', async () => {
        const wrapper = mount(Login);

        await wrapper.find('#titulo').setValue('');
        await wrapper.find('#senha').setValue('');
        await wrapper.find('button[type="submit"]').trigger('submit');

        expect(window.alert).toHaveBeenCalledWith('Por favor, preencha título e senha.');
        expect(routerPushMock).not.toHaveBeenCalled();
    });

    it('deve exibir alerta se usuário não for encontrado', async () => {
        const wrapper = mount(Login);

        await wrapper.find('#titulo').setValue('999');
        await wrapper.find('#senha').setValue('123');
        await wrapper.find('button[type="submit"]').trigger('submit');

        expect(window.alert).toHaveBeenCalledWith('Usuário não encontrado.');
        expect(routerPushMock).not.toHaveBeenCalled();
    });

    it('deve exibir alerta se nenhum perfil/unidade estiver disponível para o usuário', async () => {
        // @ts-ignore
        (usePerfil as vi.Mock).mockReturnValue({
            servidoresComPerfil: ref([
                {id: 998, nome: 'Servidor Sem Perfil', unidade: 'UN_INEXISTENTE', perfil: 'SERVIDOR'},
            ]) as Ref<Array<{ id: number; nome: string; unidade: string; perfil: string }>>,
        });
        servidoresStore.servidores = [
            {id: 998, nome: 'Servidor Sem Perfil', unidade: 'UN_INEXISTENTE', email: null, ramal: null},
        ];
        unidadesStore.unidades = [];

        const wrapper = mount(Login);

        await wrapper.find('#titulo').setValue('998');
        await wrapper.find('#senha').setValue('123');
        await wrapper.find('button[type="submit"]').trigger('submit');

        expect(window.alert).toHaveBeenCalledWith('Nenhum perfil/unidade disponível para este usuário.');
        expect(routerPushMock).not.toHaveBeenCalled();
    });
});