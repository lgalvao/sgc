import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {axe} from 'vitest-axe';
import ParametrosSection from '@/components/configuracoes/ParametrosSection.vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';

describe('ParametrosSection', () => {
    let wrapper: any;
    let configuracoesStore: any;

    beforeEach(async () => {
        const pinia = createTestingPinia({
            stubActions: false,
        });

        configuracoesStore = useConfiguracoesStore(pinia);
        configuracoesStore.parametros = [
            { codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', valor: '30', descricao: 'Desc 1' },
            { codigo: 2, chave: 'DIAS_ALERTA_NOVO', valor: '5', descricao: 'Desc 2' }
        ];
        configuracoesStore.getDiasInativacaoProcesso = vi.fn().mockReturnValue(30);
        configuracoesStore.getDiasAlertaNovo = vi.fn().mockReturnValue(5);
        configuracoesStore.carregarConfiguracoes = vi.fn().mockResolvedValue([]);

        // Wrap in main for accessibility landmarks
        const App = {
            template: '<main><ParametrosSection /></main>',
            components: { ParametrosSection }
        };

        wrapper = mount(App, {
            global: {
                plugins: [pinia],
                stubs: {
                    LoadingButton: {
                        template: '<button :disabled="loading" type="submit">{{ text }}<slot /></button>',
                        props: ['loading', 'variant', 'size', 'icon', 'text']
                    }
                }
            }
        });

        await wrapper.vm.$nextTick();
    });

    it('deve renderizar o formulário corretamente', () => {
        expect(wrapper.find('form').exists()).toBe(true);
        expect(wrapper.find('#diasInativacao').exists()).toBe(true);
        expect(wrapper.find('#diasAlertaNovo').exists()).toBe(true);
    });

    it('deve passar nos testes de acessibilidade', async () => {
        const results = await axe(wrapper.element);
        expect(results).toHaveNoViolations();
    });

    it('deve ter campos obrigatórios', () => {
        const inputInativacao = wrapper.find('#diasInativacao');
        const inputAlerta = wrapper.find('#diasAlertaNovo');

        expect(inputInativacao.attributes('required')).toBeDefined();
        expect(inputAlerta.attributes('required')).toBeDefined();
    });

    it('deve ter indicador visual de obrigatoriedade', () => {
        const labels = wrapper.findAll('label');
        expect(labels.length).toBeGreaterThan(0);
        for (const label of labels) {
            expect(label.find('.text-danger').exists()).toBe(true);
            expect(label.find('.text-danger').text()).toBe('*');
        }
    });
});
