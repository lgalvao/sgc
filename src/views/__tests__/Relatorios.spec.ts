import {mount} from '@vue/test-utils';
import Relatorios from '../Relatorios.vue';
import {beforeEach, describe, expect, it, vi} from 'vitest';

describe('Relatorios.vue', () => {
    let consoleSpy: ReturnType<typeof vi.spyOn>;

    beforeEach(() => {
        consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {
        }); // Mock console.log
    });

    it('deve renderizar corretamente o título e os cards de relatório', () => {
        const wrapper = mount(Relatorios);

        expect(wrapper.find('h2').text()).toBe('Relatórios');
        expect(wrapper.findAll('.card').length).toBe(3);

        expect(wrapper.findAll('.card-title')[0].text()).toBe('Mapas Vigentes');
        expect(wrapper.findAll('.card-text')[0].text()).toBe('Visualize os mapas de competências atualmente vigentes em todas as unidades.');

        expect(wrapper.findAll('.card-title')[1].text()).toBe('Diagnósticos de Gaps');
        expect(wrapper.findAll('.card-text')[1].text()).toBe('Analise os gaps de competências identificados nos processos de diagnóstico.');

        expect(wrapper.findAll('.card-title')[2].text()).toBe('Andamento Geral');
        expect(wrapper.findAll('.card-text')[2].text()).toBe('Acompanhe o andamento de todos os processos de mapeamento e revisão.');
    });

    it('deve chamar navegarPara com a rota correta ao clicar no card "Mapas Vigentes"', async () => {
        const wrapper = mount(Relatorios);
        await wrapper.findAll('.card')[0].trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Navegando para /relatorios/mapas-vigentes...');
    });

    it('deve chamar navegarPara com a rota correta ao clicar no card "Diagnósticos de Gaps"', async () => {
        const wrapper = mount(Relatorios);
        await wrapper.findAll('.card')[1].trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Navegando para /relatorios/diagnostico-gaps...');
    });

    it('deve chamar navegarPara com a rota correta ao clicar no card "Andamento Geral"', async () => {
        const wrapper = mount(Relatorios);
        await wrapper.findAll('.card')[2].trigger('click');
        expect(consoleSpy).toHaveBeenCalledWith('Navegando para /relatorios/andamento-geral...');
    });
});
