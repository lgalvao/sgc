import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import RelatorioDiagnosticoGapsResultado from '../RelatorioDiagnosticoGapsResultado.vue';

const campos = [
    {key: 'competenciaDescricao', label: 'Competência'},
    {key: 'mediaGap', label: 'Gap médio'},
    {key: 'totalAvaliacoesConsideradas', label: 'Avaliações consideradas'},
];

const itens = [
    {
        codigoUnidade: 1,
        siglaUnidade: 'ASSESSORIA_12',
        nomeUnidade: 'Assessoria 12',
        competencias: [
            {
                competenciaDescricao: 'Competência A',
                mediaGap: 1.236,
                totalAvaliacoesConsideradas: 4,
            },
            {
                competenciaDescricao: 'Competência B',
                mediaGap: null,
                totalAvaliacoesConsideradas: 2,
            },
        ],
    },
];

describe('RelatorioDiagnosticoGapsResultado', () => {
    it('renderiza cards e formata gaps da tabela', () => {
        const wrapper = mount(RelatorioDiagnosticoGapsResultado, {
            props: {campos, itens},
            global: {
                stubs: {
                    BCard: {template: '<section><slot /></section>'},
                    BCardTitle: {template: '<h2><slot /></h2>'},
                    BCardText: {template: '<p><slot /></p>'},
                    BTable: {
                        props: ['fields', 'items'],
                        template: `
                          <table>
                            <tbody>
                              <tr v-for="item in items" :key="item.competenciaDescricao">
                                <td>{{ item.competenciaDescricao }}</td>
                                <td><slot name="cell(mediaGap)" :item="item" /></td>
                                <td>{{ item.totalAvaliacoesConsideradas }}</td>
                              </tr>
                            </tbody>
                          </table>
                        `,
                    },
                },
            },
        });

        expect(wrapper.findAll('[data-testid="card-relatorio-gaps-diagnostico"]')).toHaveLength(1);
        expect(wrapper.text()).toContain('ASSESSORIA_12');
        expect(wrapper.text()).toContain('Assessoria 12');
        expect(wrapper.text()).toContain('Competência A');
        expect(wrapper.text()).toContain('1.24');
        expect(wrapper.text()).toContain('Competência B');
        expect(wrapper.text()).toContain('-');
    });
});
