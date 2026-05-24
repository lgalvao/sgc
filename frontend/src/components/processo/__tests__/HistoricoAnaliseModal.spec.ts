import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import HistoricoAnaliseModal from '../HistoricoAnaliseModal.vue';

describe('HistoricoAnaliseModal.vue', () => {
    const historicoMock = [
        {
            dataHora: '2026-01-01T10:00:00',
            unidadeSigla: 'U1',
            unidadeNome: 'Unidade 1',
            acao: 'ACEITE_MAPEAMENTO',
            usuarioNome: 'User 1',
            observacoes: '<p>Obs 1</p>'
        },
        {
            dataHora: '2026-01-02T11:00:00',
            unidadeSigla: 'U2',
            unidadeNome: 'Unidade 2',
            acao: 'DEVOLUCAO_REVISAO',
            usuarioNome: '',
            analistaUsuarioTitulo: 'Analista 2',
            observacoes: 'Texto muito longo que deve ser resumido para não poluir demais a tabela e facilitar a leitura rápida pelo usuário'
        },
        {
            dataHora: '2026-01-03T12:00:00',
            unidadeSigla: 'U3',
            unidadeNome: 'Unidade 3',
            acao: 'OUTRA_ACAO',
            usuarioNome: null,
            analistaUsuarioTitulo: null,
            observacoes: null
        }
    ];

    const stubs = {
        BModal: {
            props: ['modelValue'],
            template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>'
        },
        BTable: {
            props: ['items'],
            template: `
                <table>
                    <tbody>
                        <tr v-for="(item, index) in items" :key="index">
                            <td><slot name="cell(dataHora)" :item="item" :index="index" /></td>
                            <td><slot name="cell(unidadeSigla)" :item="item" :index="index" /></td>
                            <td><slot name="cell(acao)" :item="item" :index="index" /></td>
                            <td><slot name="cell(usuarioNome)" :item="item" :index="index" /></td>
                            <td><slot name="cell(observacoesResumo)" :item="item" :index="index" /></td>
                            <td><slot name="cell(observacoesAcoes)" :item="item" :index="index" /></td>
                        </tr>
                    </tbody>
                </table>
            `
        },
        BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
        BSpinner: {template: '<div>Loading...</div>'},
        ModalVisualizacaoTextoFormatado: {template: '<div></div>'}
    };

    it('renders loading state', () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {mostrar: true, historico: [], loading: true},
            global: {stubs}
        });
        expect(wrapper.text()).toContain('Loading...');
    });

    it('renders empty state', () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {mostrar: true, historico: [], loading: false},
            global: {stubs}
        });
        expect(wrapper.find('[data-testid="alert-historico-vazio"]').exists()).toBe(true);
    });

    it('renders historico correctly and handles branches', async () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {mostrar: true, historico: historicoMock, loading: false},
            global: {stubs}
        });

        const vm = wrapper.vm as any;

        // Data 1
        expect(wrapper.find('[data-testid="cell-dataHora-0"]').text()).toContain('01/01/2026');
        expect(wrapper.find('[data-testid="cell-resultado-0"]').text()).toBe('Aceite');
        expect(wrapper.find('[data-testid="cell-usuario-0"]').text()).toBe('User 1');
        expect(wrapper.find('[data-testid="cell-observacao-0"]').text()).toBe('Obs 1');

        // Data 2 (long observation and analista title)
        expect(wrapper.find('[data-testid="cell-resultado-1"]').text()).toBe('Devolução');
        expect(wrapper.find('[data-testid="cell-usuario-1"]').text()).toBe('Analista 2');
        const obsResumo = wrapper.find('[data-testid="cell-observacao-1"]').text();
        expect(obsResumo).toContain('...');
        expect(obsResumo.length).toBeLessThanOrEqual(80);

        // Data 3 (missing user and observation)
        expect(wrapper.find('[data-testid="cell-resultado-2"]').text()).toBe('Outra Acao');
        expect(wrapper.find('[data-testid="cell-usuario-2"]').text()).toBe('-');
        expect(wrapper.find('[data-testid="cell-observacao-2"]').text()).toBe('-');

        // formatarAcaoAnalise other cases
        expect(vm.formatarAcaoAnalise(null)).toBe('-');
        
        // Interaction
        await wrapper.find('[data-testid="btn-ver-observacao-0"]').trigger('click');
        expect(vm.mostrarObservacao).toBe(true);
        expect(vm.observacaoSelecionada).toBe('<p>Obs 1</p>');

        await wrapper.find('[data-testid="btn-modal-fechar"]').trigger('click');
        expect(wrapper.emitted('fechar')).toBeTruthy();
    });
});