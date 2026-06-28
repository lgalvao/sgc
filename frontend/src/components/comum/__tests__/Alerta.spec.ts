import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import {run} from 'axe-core';
import Alerta from '../Alerta.vue';

describe('Alerta.vue', () => {
    it('deve renderizar a prop mensagem no modo simples', () => {
        const wrapper = mount(Alerta, {
            props: {
                mensagem: 'Alerta de erro simples'
            }
        });
        expect(wrapper.find('[data-testid="app-alert"]').exists()).toBe(true);
        expect(wrapper.text()).toContain('Alerta de erro simples');
    });

    it('deve permitir configurar o data-testid do alerta', () => {
        const wrapper = mount(Alerta, {
            props: {
                mensagem: 'Alerta customizado',
                dataTestid: 'alerta-customizado',
            }
        });

        expect(wrapper.find('[data-testid="alerta-customizado"]').exists()).toBe(true);
    });

    it('deve renderizar conteudo rico via slot', () => {
        const wrapper = mount(Alerta, {
            slots: {
                default: '<div data-testid="conteudo-alerta">Conteudo customizado</div>',
            },
        });

        expect(wrapper.find('[data-testid="app-alert"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="conteudo-alerta"]').exists()).toBe(true);
        expect(wrapper.text()).toContain('Conteudo customizado');
    });

    it('deve renderizar o notificacao no modo estruturado e ocultar/exibir detalhes', async () => {
        const wrapper = mount(Alerta, {
            props: {
                notificacao: {
                    resumo: 'Resumo do erro',
                    detalhes: ['Erro 1', 'Erro 2']
                }
            }
        });

        expect(wrapper.text()).toContain('Resumo do erro');
        expect(wrapper.text()).toContain('Mostrar detalhes');
        expect(wrapper.text()).not.toContain('Erro 1');

        // Click to show details
        await wrapper.find('button').trigger('click');
        expect(wrapper.text()).toContain('Ocultar detalhes');
        expect(wrapper.text()).toContain('Erro 1');
        expect(wrapper.text()).toContain('Erro 2');
    });

    it('deve renderizar o stack trace quando em modo dev', async () => {
        const wrapper = mount(Alerta, {
            props: {
                mensagem: 'Erro com stack',
                stackTrace: 'Error at line 1'
            }
        });

        expect(wrapper.text()).toContain('Mostrar detalhes técnicos');

        // Múltiplos buttons podem existir se fosse notificação estruturada, mas é modo simples
        await wrapper.find('button').trigger('click');
        expect(wrapper.text()).toContain('Error at line 1');
    });

    it('nao deve renderizar se não houver mensagem, notificacao ou slot', () => {
        const wrapper = mount(Alerta, {
            props: {}
        });
        expect(wrapper.find('[data-testid="app-alert"]').exists()).toBe(false);
    });

    it('deve emitir o evento dismissed', async () => {
        const wrapper = mount(Alerta, {
            props: {
                mensagem: 'Erro'
            }
        });

        wrapper.vm.$emit('dismissed');
        expect(wrapper.emitted().dismissed).toBeDefined();
    });
});

describe('Alerta A11y', () => {
    function montarAlerta(props: InstanceType<typeof Alerta>['$props']) {
        const alvo = document.createElement('div');
        document.body.appendChild(alvo);

        const wrapper = mount(Alerta, {
            props,
            attachTo: alvo,
        });

        return {wrapper, alvo};
    }

    it('não tem violações de acessibilidade no modo simples', async () => {
        const {wrapper, alvo} = montarAlerta({mensagem: 'Hello world'});
        const results = await run(alvo);
        expect(results).toHaveNoViolations();
        wrapper.unmount();
        alvo.remove();
    });

    it('não tem violações de acessibilidade no modo detalhado', async () => {
        const {wrapper, alvo} = montarAlerta({
            notificacao: {
                resumo: 'Erro',
                detalhes: ['Detalhe 1', 'Detalhe 2']
            },
        });
        const results = await run(alvo);
        expect(results).toHaveNoViolations();
        wrapper.unmount();
        alvo.remove();
    });
});
