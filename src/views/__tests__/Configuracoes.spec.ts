import {mount} from '@vue/test-utils';
import Configuracoes from '../Configuracoes.vue';
import {describe, expect, it} from 'vitest';

describe('Configuracoes.vue', () => {
    it('deve renderizar corretamente o título e a mensagem', () => {
        const wrapper = mount(Configuracoes);

        expect(wrapper.find('h2').text()).toBe('Configurações do Sistema');
        expect(wrapper.find('p').text()).toBe('Ainda não há configurações disponíveis.');
    });
});
