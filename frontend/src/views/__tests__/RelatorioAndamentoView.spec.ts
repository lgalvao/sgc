import {describe, expect, it, vi, beforeEach} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {ref} from 'vue';
import RelatorioAndamentoView from '@/views/RelatorioAndamentoView.vue';

const buscarRelatorioAndamento = vi.fn();
const exportarAndamentoPdf = vi.fn();
const limparRelatorio = vi.fn();
const clearError = vi.fn();
const notify = vi.fn();

const relatorioAndamento = ref([
    {
        siglaUnidade: 'U1',
        nomeUnidade: 'Unidade 1',
        situacaoAtual: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO',
        dataLimite: '2026-04-15T10:00:00',
        dataFimEtapa1: '2026-04-16T11:30:00',
        dataFimEtapa2: null,
        dataUltimaMovimentacao: '2026-04-10T08:00:00',
        responsavel: 'Responsavel 1',
        titular: 'Responsavel 1',
    }
]);

vi.mock('@/stores/relatorios', () => ({
    useRelatoriosStore: () => ({
        get relatorioAndamento() {
            return relatorioAndamento.value;
        },
        get lastError() {
            return null;
        },
        buscarRelatorioAndamento,
        exportarAndamentoPdf,
        limparRelatorio,
        clearError,
    })
}));

vi.mock('@/services/painelService', () => ({
    listarProcessos: vi.fn().mockResolvedValue({
        content: [{codigo: 1, descricao: 'Processo 1'}]
    })
}));

vi.mock('@/composables/useNotification', () => ({
    useNotification: () => ({
        notify,
    })
}));

describe('RelatorioAndamentoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        relatorioAndamento.value = [{
            siglaUnidade: 'U1',
            nomeUnidade: 'Unidade 1',
            situacaoAtual: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO',
            dataLimite: '2026-04-15T10:00:00',
            dataFimEtapa1: '2026-04-16T11:30:00',
            dataFimEtapa2: null,
            dataUltimaMovimentacao: '2026-04-10T08:00:00',
            responsavel: 'Responsavel 1',
            titular: 'Responsavel 1',
        }];
    });

    it('deve configurar colunas e entregar datas formatadas para a tabela', async () => {
        const wrapper = mount(RelatorioAndamentoView, {
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    PageHeader: {template: '<div><slot /><slot name="actions" /></div>', props: ['title']},
                    EmptyState: {template: '<div />'},
                    BCard: {template: '<div><slot /></div>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BFormGroup: {template: '<div><slot /></div>', props: ['label', 'labelFor']},
                    BFormSelect: {template: '<select />', props: ['modelValue', 'options']},
                    BButton: {template: '<button><slot /></button>', props: ['variant', 'disabled', 'to']},
                    BSpinner: {template: '<div />'},
                    BTable: {name: 'BTable', template: '<div data-testid="tbl-relatorio-andamento"></div>', props: ['items', 'fields']},
                }
            }
        });

        await flushPromises();

        const campos = wrapper.getComponent({name: 'BTable'}).props('fields') as Array<{ key: string }>;
        const itens = wrapper.getComponent({name: 'BTable'}).props('items') as Array<Record<string, string>>;
        expect(campos.map(campo => campo.key)).toEqual([
            'siglaUnidade',
            'situacaoAtual',
            'dataLimite',
            'dataFimEtapa1',
            'dataFimEtapa2',
            'responsavel',
        ]);

        expect(itens[0].dataLimite).toBe('15/04/2026');
        expect(itens[0].dataFimEtapa1).toBe('16/04/2026');
        expect(itens[0].dataFimEtapa2).toBe('Não informado');
    });
});
