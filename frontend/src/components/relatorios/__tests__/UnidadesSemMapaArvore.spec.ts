import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import UnidadesSemMapaArvore from '../UnidadesSemMapaArvore.vue';

describe('UnidadesSemMapaArvore.vue', () => {
    it('renders recursively and covers all branches', () => {
        const unidades = [
            {
                codigo: 1,
                sigla: 'U1',
                nome: 'Unidade 1',
                filhas: [
                    {
                        codigo: 2,
                        sigla: 'U2',
                        nome: 'Unidade 2',
                        filhas: []
                    }
                ]
            },
            {
                codigo: 3,
                sigla: 'U3',
                nome: '', // Test empty name
                filhas: null // Test null filhas
            },
            {
                codigo: 4,
                sigla: '', // Test empty sigla
                nome: 'Unidade 4',
                filhas: []
            }
        ];

        const wrapper = mount(UnidadesSemMapaArvore, {
            props: { unidades: unidades as any }
        });

        expect(wrapper.text()).toContain('U1');
        expect(wrapper.text()).toContain('Unidade 1');
        expect(wrapper.text()).toContain('U2');
        expect(wrapper.text()).toContain('Unidade 2');
        expect(wrapper.text()).toContain('U3');
        expect(wrapper.text()).toContain('Unidade 4');
        
        // Check for separator logic
        // U1 has sigla and name -> separator should exist
        const items = wrapper.findAll('li');
        expect(items[0].find('.arvore-unidades-sem-mapa__separador').exists()).toBe(true);
        
        // U3 has sigla but no name -> separator should NOT exist
        expect(items[2].find('.arvore-unidades-sem-mapa__separador').exists()).toBe(false);

        // U4 has no sigla but has name -> separator should NOT exist
        expect(items[3].find('.arvore-unidades-sem-mapa__separador').exists()).toBe(false);
    });
});