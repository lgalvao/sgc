import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import TabelaProcessos from '../processo/TabelaProcessos.vue';
import {SituacaoProcesso, TipoProcesso} from '@/types/tipos';
import {createMockProcessoResumo} from '@/test-utils/mockFactories';

describe('TabelaProcessos Coverage', () => {
    const mockProcessos = [
        createMockProcessoResumo({
            codigo: 1,
            descricao: "Processo Teste",
            tipo: TipoProcesso.MAPEAMENTO,
            tipoLabel: "Mapeamento",
            unidadeCodigo: 1,
            unidadeNome: "U1",
            situacao: SituacaoProcesso.EM_ANDAMENTO,
            situacaoLabel: "Em andamento",
            dataLimite: new Date().toISOString(),
            dataLimiteFormatada: "31/12/2023",
            dataCriacao: new Date().toISOString()
        })
    ];

    it('handles Space key on row to select process', async () => {
        // Stub BTable to render a row with the bound event handler
        const BTableStub = {
            props: ['items', 'tbodyTrAttr'],
            template: `
                <table>
                    <tbody>
                        <tr v-for="item in items"
                            :key="item.codigo"
                            v-bind="tbodyTrAttr(item, 'row')">
                            <td>Row</td>
                        </tr>
                    </tbody>
                </table>
            `
        };

        const wrapper = mount(TabelaProcessos, {
            global: {
                stubs: {
                    BTable: BTableStub,
                    EmptyState: true
                }
            },
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true
            }
        });

        const row = wrapper.find('tr');
        // Trigger space key
        await row.trigger('keydown', { key: ' ' });

        expect(wrapper.emitted('selecionarProcesso')).toBeTruthy();
        expect(wrapper.emitted('selecionarProcesso')![0]).toEqual([mockProcessos[0]]);
    });

    it('returns empty class/attr for invalid inputs in row functions', async () => {
         const BTableStub = {
            props: ['items', 'tbodyTrClass', 'tbodyTrAttr'],
            template: `
                <div>
                     <!-- Trigger with null item -->
                    <div data-testid="null-item-class" :class="tbodyTrClass ? tbodyTrClass(null, 'row') : ''"></div>
                    <div data-testid="null-item-attr" v-bind="tbodyTrAttr ? tbodyTrAttr(null, 'row') : {}"></div>

                    <!-- Trigger with wrong type -->
                    <div data-testid="wrong-type-class" :class="tbodyTrClass ? tbodyTrClass(items[0], 'cell') : ''"></div>
                    <div data-testid="wrong-type-attr" v-bind="tbodyTrAttr ? tbodyTrAttr(items[0], 'cell') : {}"></div>
                </div>
            `
        };

        const wrapper = mount(TabelaProcessos, {
            global: {
                stubs: {
                    BTable: BTableStub,
                    EmptyState: true
                }
            },
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true
            }
        });

        // Check class results
        const nullItemClassDiv = wrapper.find('[data-testid="null-item-class"]');
        expect(nullItemClassDiv.classes().length).toBe(0); // Should be empty

        const wrongTypeClassDiv = wrapper.find('[data-testid="wrong-type-class"]');
        expect(wrongTypeClassDiv.classes().length).toBe(0); // Should be empty

        // Check attr results
        const nullItemAttrDiv = wrapper.find('[data-testid="null-item-attr"]');
        expect(nullItemAttrDiv.attributes('tabindex')).toBeUndefined();

        const wrongTypeAttrDiv = wrapper.find('[data-testid="wrong-type-attr"]');
        expect(wrongTypeAttrDiv.attributes('tabindex')).toBeUndefined();
    });
});
