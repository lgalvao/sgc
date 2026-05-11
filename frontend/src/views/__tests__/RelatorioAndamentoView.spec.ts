import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {ref} from 'vue';
import RelatorioAndamentoView from '@/views/RelatorioAndamentoView.vue';
import * as painelService from '@/services/painelService';
import {TEXTOS} from '@/constants/textos';

const buscarRelatorioAndamento = vi.fn();
const exportarAndamentoPdf = vi.fn();
const limparRelatorio = vi.fn();
const clearError = vi.fn();
const notify = vi.fn();

const relatorioAndamento = ref<any[]>([]);
const lastError = ref<Error | null>(null);

vi.mock('@/stores/relatorios', () => ({
    useRelatoriosStore: () => ({
        get relatorioAndamento() {
            return relatorioAndamento.value;
        },
        get lastError() {
            return lastError.value;
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

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {template: '<div><slot /><slot name="actions" /></div>', props: ['title']},
    BCard: {template: '<div class="card"><slot /></div>'},
    BCardBody: {template: '<div class="card-body"><slot /></div>'},
    BCardTitle: {template: '<h4 class="card-title"><slot /></h4>'},
    BRow: {template: '<div class="row"><slot /></div>'},
    BCol: {template: '<div class="col"><slot /></div>'},
    BFormGroup: {template: '<div><slot /></div>', props: ['label', 'labelFor']},
    BFormSelect: {
        template: '<select v-bind="$attrs"><slot /></select>',
        props: ['modelValue', 'options'],
        inheritAttrs: false
    },
    BButton: {template: '<button><slot /></button>', props: ['variant', 'disabled', 'to']},
    BSpinner: {template: '<div />'},
};

describe('RelatorioAndamentoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        lastError.value = null;
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
        vi.mocked(painelService.listarProcessos).mockResolvedValue({
            content: [{codigo: 1, descricao: 'Processo 1'}]
        } as any);
    });

    it('deve exibir cards com informações formatadas do relatório', async () => {
        const wrapper = mount(RelatorioAndamentoView, {
            global: {stubs}
        });

        await flushPromises();

        const cards = wrapper.findAll('[data-testid="card-resultado-andamento"]');
        expect(cards.length).toBe(1);

        const textoCard = cards[0].text();
        expect(textoCard).toContain('U1');
        expect(textoCard).toContain('Unidade 1');
        expect(textoCard).toContain('MAPEAMENTO_CADASTRO_EM_ANDAMENTO');
        expect(textoCard).toContain('Local 1');
        expect(textoCard).toContain('10/04/2026 08:00');
        expect(textoCard).toContain('15/04/2026');
        expect(textoCard).toContain('16/04/2026');
        expect(textoCard).toContain('Responsavel 1');
    });

    it('cobre falha em carregarProcessos', async () => {
        vi.mocked(painelService.listarProcessos).mockRejectedValue(new Error("Erro simulado"));
        mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        expect(notify).toHaveBeenCalledWith("Erro ao carregar processos", "danger");
    });

    it('cobre gerarRelatorio com e sem erro', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const vm = wrapper.vm as any;

        // return early
        await vm.gerarRelatorio();
        expect(buscarRelatorioAndamento).not.toHaveBeenCalled();

        vm.codProcessoSelecionado = 1;
        await vm.gerarRelatorio();
        expect(buscarRelatorioAndamento).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();

        // cover error
        lastError.value = new Error("Erro");
        await vm.gerarRelatorio();
        expect(notify).toHaveBeenCalledWith(TEXTOS.relatorios.ERRO_BUSCA, "danger");
    });

    it('cobre exportarPdf com e sem erro', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const vm = wrapper.vm as any;

        // return early
        await vm.exportarPdf();
        expect(exportarAndamentoPdf).not.toHaveBeenCalled();

        vm.codProcessoSelecionado = 1;
        await vm.exportarPdf();
        expect(exportarAndamentoPdf).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();

        // cover error
        lastError.value = new Error("Erro");
        await vm.exportarPdf();
        expect(notify).toHaveBeenCalledWith(TEXTOS.relatorios.ERRO_EXPORTAR, "danger");
    });

    it('encerra o carregamento quando gerar relatório falha', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const vm = wrapper.vm as any;

        vm.codProcessoSelecionado = 1;
        buscarRelatorioAndamento.mockRejectedValueOnce(new Error("403"));

        await vm.gerarRelatorio();

        expect(vm.carregando).toBe(false);
    });

    it('encerra o carregamento quando exportar pdf falha', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const vm = wrapper.vm as any;

        vm.codProcessoSelecionado = 1;
        exportarAndamentoPdf.mockRejectedValueOnce(new Error("403"));

        await vm.exportarPdf();

        expect(vm.carregando).toBe(false);
    });
});
