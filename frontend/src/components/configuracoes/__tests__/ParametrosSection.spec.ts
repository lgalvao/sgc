import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {axe} from 'vitest-axe';
import ParametrosSection from '@/components/configuracoes/ParametrosSection.vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';
import {useNotificacoesStore} from '@/stores/feedback';

describe('ParametrosSection', () => {
    let wrapper: any;
    let configuracoesStore: any;
    let notificacoesStore: any;

    const setupWrapper = (initialState = {}, storeParams = null) => {
        const pinia = createTestingPinia({
            stubActions: false,
            initialState
        });

        configuracoesStore = useConfiguracoesStore(pinia);
        notificacoesStore = useNotificacoesStore(pinia);

        configuracoesStore.parametros = storeParams !== null ? storeParams : [
            { codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', valor: '30', descricao: 'Desc 1' },
            { codigo: 2, chave: 'DIAS_ALERTA_NOVO', valor: '5', descricao: 'Desc 2' }
        ];
        configuracoesStore.getDiasInativacaoProcesso = vi.fn().mockReturnValue(30);
        configuracoesStore.getDiasAlertaNovo = vi.fn().mockReturnValue(5);
        configuracoesStore.carregarConfiguracoes = vi.fn().mockResolvedValue([]);
        configuracoesStore.salvarConfiguracoes = vi.fn().mockResolvedValue(true);

        const App = {
            template: '<main><ParametrosSection /></main>',
            components: { ParametrosSection }
        };

        wrapper = mount(App, {
            global: {
                plugins: [pinia],
                stubs: {
                    LoadingButton: {
                        template: '<button :disabled="loading" class="loading-button-stub" @click="$emit(\'click\')">{{ text }}<slot /></button>',
                        props: ['loading', 'variant', 'size', 'icon', 'text']
                    },
                    BAlert: {
                        template: '<div class="alert-stub"><slot /></div>'
                    }
                }
            }
        });
    };

    beforeEach(async () => {
        vi.clearAllMocks();
    });

    it('deve renderizar o formulário corretamente', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        expect(wrapper.find('form').exists()).toBe(true);
        expect(wrapper.find('#diasInativacao').exists()).toBe(true);
        expect(wrapper.find('#diasAlertaNovo').exists()).toBe(true);
    });

    it('deve passar nos testes de acessibilidade', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        const results = await axe(wrapper.element);
        expect(results).toHaveNoViolations();
    });

    it('deve chamar recarregar ao clicar no botão recarregar', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        const btnRecarregar = wrapper.find('.loading-button-stub');
        await btnRecarregar.trigger('click');
        expect(configuracoesStore.carregarConfiguracoes).toHaveBeenCalled();
    });

    it('deve chamar salvar ao submeter o formulário com sucesso', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        await wrapper.find('form').trigger('submit.prevent');
        expect(configuracoesStore.salvarConfiguracoes).toHaveBeenCalled();
        expect(notificacoesStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
    });

    it('deve mostrar erro ao falhar ao salvar', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        configuracoesStore.salvarConfiguracoes.mockResolvedValue(false);
        await wrapper.find('form').trigger('submit.prevent');
        expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', expect.any(String), 'danger');
    });

    it('deve mostrar loading state', async () => {
        setupWrapper({
            configuracoes: { loading: true }
        });
        await wrapper.vm.$nextTick();
        expect(wrapper.find('.spinner-border').exists()).toBe(true);
    });

    it('deve mostrar erro state da store', async () => {
        setupWrapper({
            configuracoes: { error: 'Erro de teste', loading: false }
        });
        await wrapper.vm.$nextTick();
        expect(wrapper.find('.alert-stub').text()).toContain('Erro de teste');
    });

    it('deve carregar configuracoes no onMounted se estiverem vazias', async () => {
        setupWrapper({}, []); // empty params
        await wrapper.vm.$nextTick();
        expect(configuracoesStore.carregarConfiguracoes).toHaveBeenCalled();
    });

    it('deve lidar com parametros nao encontrados no salvar', async () => {
        setupWrapper({}, []); // empty params, findCodigo will return undefined
        await wrapper.vm.$nextTick();
        await wrapper.find('form').trigger('submit.prevent');
        expect(configuracoesStore.salvarConfiguracoes).toHaveBeenCalledWith([
            { codigo: undefined, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: expect.any(String), valor: '30' },
            { codigo: undefined, chave: 'DIAS_ALERTA_NOVO', descricao: expect.any(String), valor: '5' }
        ]);
    });
});
