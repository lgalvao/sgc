import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {ref} from 'vue';
import RelatorioAndamentoView from '@/views/RelatorioAndamentoView.vue';
import {TEXTOS_RELATORIOS} from '@/constants/textos-relatorios';

const buscarRelatorioAndamento = vi.fn().mockResolvedValue(undefined);
const exportarAndamentoPdf = vi.fn().mockResolvedValue(undefined);
const limparRelatorio = vi.fn();
const notify = vi.fn();
const carregarProcessos = vi.fn().mockResolvedValue(undefined);

const relatorioAndamento = ref<any[]>([]);
const processosDisponiveis = ref<any[]>([{codigo: 1, descricao: 'Processo 1'}]);

vi.mock('@/stores/relatorios', () => ({
    useRelatoriosStore: () => ({
        get relatorioAndamento() {
            return relatorioAndamento.value;
        },
        buscarRelatorioAndamento,
        exportarAndamentoPdf,
        limparRelatorio,
    })
}));

vi.mock('@/composables/useRelatorioAndamentoTela', () => ({
    useRelatorioAndamentoTela: () => ({
        get processosDisponiveis() { return processosDisponiveis; },
        carregarProcessos,
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
        buscarRelatorioAndamento.mockResolvedValue(undefined);
        exportarAndamentoPdf.mockResolvedValue(undefined);
        carregarProcessos.mockResolvedValue(undefined);
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
        carregarProcessos.mockRejectedValueOnce(new Error("Erro simulado"));
        mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        // O erro é tratado dentro do composable; apenas verificamos que carregarProcessos foi chamado
        expect(carregarProcessos).toHaveBeenCalled();
    });

    it('cobre gerarRelatorio com e sem erro', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const vm = wrapper.vm as any;

        // return early quando não há processo selecionado
        await vm.gerarRelatorio();
        expect(buscarRelatorioAndamento).not.toHaveBeenCalled();

        vm.codProcessoSelecionado = 1;
        await vm.gerarRelatorio();
        expect(buscarRelatorioAndamento).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();

        // cover error: mock rejeita, view deve notificar
        buscarRelatorioAndamento.mockRejectedValueOnce(new Error("Erro"));
        await vm.gerarRelatorio();
        expect(notify).toHaveBeenCalledWith(TEXTOS_RELATORIOS.ERRO_BUSCA, "danger");
    });

    it('cobre exportarPdf com e sem erro', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const vm = wrapper.vm as any;

        // return early quando não há processo selecionado
        await vm.exportarPdf();
        expect(exportarAndamentoPdf).not.toHaveBeenCalled();

        vm.codProcessoSelecionado = 1;
        await vm.exportarPdf();
        expect(exportarAndamentoPdf).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();

        // cover error: mock rejeita, view deve notificar
        exportarAndamentoPdf.mockRejectedValueOnce(new Error("Erro"));
        await vm.exportarPdf();
        expect(notify).toHaveBeenCalledWith(TEXTOS_RELATORIOS.ERRO_EXPORTAR, "danger");
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

    it('exibe prazo ajustado quando data limite da etapa 2 é diferente da etapa 1', async () => {
        relatorioAndamento.value = [{
            dataLimiteEtapa1: '2026-04-15T10:00:00',
            dataLimiteEtapa2: '2026-04-20T10:00:00',
        }];
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        expect(wrapper.text()).toContain('20/04/2026');
    });

    it('lida com campos de data e localizacao nulos', async () => {
        relatorioAndamento.value = [{
            dataLimiteEtapa1: null,
            dataLimiteEtapa2: null,
            dataFimEtapa1: null,
            dataFimEtapa2: null,
            dataUltimaMovimentacao: null,
            localizacao: null,
        }];
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        await flushPromises();
        const card = wrapper.find('[data-testid="card-resultado-andamento"]');
        expect(card.text()).toContain('-');
    });

    it('obterMensagemErroRelatorio deve retornar mensagem padrao para erro inesperado sem status', async () => {
        const wrapper = mount(RelatorioAndamentoView, {global: {stubs}});
        const vm = wrapper.vm as any;
        vm.codProcessoSelecionado = 1;
        
        buscarRelatorioAndamento.mockRejectedValueOnce(new Error('Network Error'));
        await vm.gerarRelatorio();
        
        expect(notify).toHaveBeenCalledWith(TEXTOS_RELATORIOS.ERRO_BUSCA, 'danger');
    });
});
