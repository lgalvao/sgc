import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import ParametrosView from '@/views/ParametrosView.vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';

describe('ParametrosView', () => {
    let wrapper: any;
    let configuracoesStore: any;

    const setupWrapper = (initialState: any = {}, storeParams: any[] | null = null) => {
        const pinia = createTestingPinia({
            stubActions: false,
            initialState
        });

        configuracoesStore = useConfiguracoesStore(pinia);

        configuracoesStore.configuracoes = storeParams !== null ? storeParams : [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', valor: '30', descricao: 'Desc 1'},
            {codigo: 2, chave: 'DIAS_ALERTA_NOVO', valor: '5', descricao: 'Desc 2'}
        ];
        configuracoesStore.getDiasInativacaoProcesso = vi.fn().mockReturnValue(30);
        configuracoesStore.getDiasAlertaNovo = vi.fn().mockReturnValue(5);
        configuracoesStore.carregarConfiguracoes = vi.fn().mockResolvedValue([]);
        configuracoesStore.salvarConfiguracoes = vi.fn().mockResolvedValue(true);

        wrapper = mount(ParametrosView, {
            global: {
                plugins: [pinia],
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    PageHeader: {
                        props: ['title'],
                        template: '<div><h1>{{ title }}</h1><slot name="actions" /></div>'
                    },
                    BAlert: {
                        template: '<div class="alert-stub"><slot /></div>'
                    },
                    BFormInput: {
                        name: 'BFormInput',
                        props: ['modelValue'],
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
                    },
                    LoadingButton: {
                        template: '<button :disabled="loading" class="loading-button-stub" @click="$emit(\'click\')">{{ text }}<slot /></button>',
                        props: ['loading', 'variant', 'size', 'icon', 'text']
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
        expect(wrapper.find('h1').text()).toBe('Configurações');
        expect(wrapper.find('form').exists()).toBe(true);
        expect(wrapper.find('#diasInativacao').exists()).toBe(true);
        expect(wrapper.find('#diasAlertaNovo').exists()).toBe(true);
    });

    it('deve chamar salvar ao submeter o formulário com sucesso', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        await wrapper.find('form').trigger('submit.prevent');
        expect(configuracoesStore.salvarConfiguracoes).toHaveBeenCalled();
        expect(wrapper.text()).toContain('Configurações salvas.');
    });

    it('deve mostrar erro ao falhar ao salvar', async () => {
        setupWrapper();
        await wrapper.vm.$nextTick();
        configuracoesStore.salvarConfiguracoes.mockResolvedValue(false);
        await wrapper.find('form').trigger('submit.prevent');
        expect(wrapper.text()).toContain('Erro ao salvar configurações.');
    });

    it('deve mostrar loading state', async () => {
        setupWrapper({
            configuracoes: {loading: true}
        });
        await wrapper.vm.$nextTick();
        expect(wrapper.find('.spinner-border').exists()).toBe(true);
    });

    it('deve mostrar erro state da store', async () => {
        setupWrapper({
            configuracoes: {error: 'Erro de teste', loading: false}
        });
        await wrapper.vm.$nextTick();
        expect(wrapper.find('.alert-stub').text()).toContain('Erro de teste');
    });

    it('deve gerenciar o carregamento de configurações e validações de formulário de parâmetros', async () => {
        // Carregamento de configurações quando a lista está vazia
        setupWrapper({}, []);
        await wrapper.vm.$nextTick();
        expect(configuracoesStore.carregarConfiguracoes).toHaveBeenCalled();

        // Atualização de v-model nos campos do formulário
        const inputs = wrapper.findAllComponents({name: 'BFormInput'});
        if (inputs.length > 0) await inputs[0].vm.$emit('update:modelValue', 40);
        if (inputs.length > 1) await inputs[1].vm.$emit('update:modelValue', 10);
        expect(wrapper.vm.form.diasInativacao).toBe(40);
        expect(wrapper.vm.form.diasAlertaNovo).toBe(10);

        // Bloqueio de submissão do formulário com dados inválidos
        wrapper.vm.form.diasInativacao = 0;
        await wrapper.find('form').trigger('submit.prevent');
        expect(configuracoesStore.salvarConfiguracoes).not.toHaveBeenCalled();
    });
});
