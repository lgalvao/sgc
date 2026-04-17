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
        localizacao: 'Local 1',
        dataLimiteEtapa1: '2026-04-15T10:00:00',
        dataLimiteEtapa2: '2026-04-15T10:00:00',
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
            localizacao: 'Local 1',
            dataLimiteEtapa1: '2026-04-15T10:00:00',
            dataLimiteEtapa2: '2026-04-15T10:00:00',
            dataFimEtapa1: '2026-04-16T11:30:00',
            dataFimEtapa2: null,
            dataUltimaMovimentacao: '2026-04-10T08:00:00',
            responsavel: 'Responsavel 1',
            titular: 'Responsavel 1',
        }];
    });

    it('deve exibir cards com informações formatadas do relatório', async () => {
        const wrapper = mount(RelatorioAndamentoView, {
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    PageHeader: {template: '<div><slot /><slot name="actions" /></div>', props: ['title']},
                    EmptyState: {template: '<div />'},
                    BCard: {template: '<div class="card"><slot /></div>'},
                    BCardBody: {template: '<div class="card-body"><slot /></div>'},
                    BCardTitle: {template: '<h4 class="card-title"><slot /></h4>'},
                    BRow: {template: '<div class="row"><slot /></div>'},
                    BCol: {template: '<div class="col"><slot /></div>'},
                    BFormGroup: {template: '<div><slot /></div>', props: ['label', 'labelFor']},
                    BFormSelect: {template: '<select />', props: ['modelValue', 'options']},
                    BButton: {template: '<button><slot /></button>', props: ['variant', 'disabled', 'to']},
                    BSpinner: {template: '<div />'},
                }
            }
        });

        await flushPromises();

        const cards = wrapper.findAll('[data-testid="card-resultado-andamento"]');
        expect(cards.length).toBe(1);

        const textoCard = cards[0].text();
        expect(textoCard).toContain('U1 - Unidade 1');
        expect(textoCard).toContain('MAPEAMENTO_CADASTRO_EM_ANDAMENTO');
        expect(textoCard).toContain('Local 1');
        expect(textoCard).toContain('10/04/2026 08:00');
        expect(textoCard).toContain('15/04/2026');
        expect(textoCard).toContain('16/04/2026');
        expect(textoCard).toContain('Responsavel 1');
    });
});
