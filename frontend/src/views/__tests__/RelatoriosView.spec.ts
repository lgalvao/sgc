import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import Relatorios from '@/views/RelatoriosView.vue';
import {TipoProcesso} from '@/types/tipos';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';
import * as useRelatoriosModule from '@/composables/api/useRelatorios';

describe('Relatorios.vue', () => {
    const ctx = setupComponentTest();

    const mockProcessos = [
        {
            codigo: 1,
            descricao: 'Processo mapeamento 2024',
            tipo: TipoProcesso.MAPEAMENTO,
            situacao: 'EM_ANDAMENTO',
        },
        {
            codigo: 2,
            descricao: 'Processo revisao 2025',
            tipo: TipoProcesso.REVISAO,
            situacao: 'CRIADO',
        }
    ];

    const mockObterRelatorioAndamento = vi.fn();
    const mockDownloadRelatorioAndamentoPdf = vi.fn();
    const mockDownloadRelatorioMapasPdf = vi.fn();

    const stubs = {
        BContainer: {template: '<div><slot /></div>'},
        BCard: {template: '<div class="card"><slot /></div>'},
        BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
        BTabs: {template: '<div><slot /></div>'},
        BTab: {template: '<div><slot /></div>'},
        BFormSelect: {
            template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', Number($event.target.value))"></select>',
            props: ['modelValue', 'options']
        },
        BTable: {template: '<table class="b-table"></table>', props: ['items']},
        EmptyState: {template: '<div class="empty-state">Vazio</div>'},
        BSpinner: {template: '<span class="spinner"></span>'}
    };

    beforeEach(() => {
        vi.clearAllMocks();

        vi.spyOn(useRelatoriosModule, 'useRelatorios').mockReturnValue({
            obterRelatorioAndamento: mockObterRelatorioAndamento,
            downloadRelatorioAndamentoPdf: mockDownloadRelatorioAndamentoPdf,
            downloadRelatorioMapasPdf: mockDownloadRelatorioMapasPdf
        });

        const mountOptions = getCommonMountOptions({
            processos: {
                processosPainel: mockProcessos,
            }
        }, stubs);

        ctx.wrapper = mount(Relatorios, mountOptions);
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('deve chamar a api para gerar relatorio de andamento ao clicar no botao', async () => {
        mockObterRelatorioAndamento.mockResolvedValue([
            { siglaUnidade: 'U1', responsavel: 'Joao' }
        ]);

        // Simula selecao de processo (o select em Vue test utils com stub pode ser setado no vm)
        ctx.wrapper!.vm.codProcessoSelecionado = 1;
        await ctx.wrapper!.vm.$nextTick();
        
        // Pega todos os botoes. O primeiro é o de Gerar andamento.
        const btns = ctx.wrapper!.findAll('button');
        await btns[0].trigger('click');

        expect(mockObterRelatorioAndamento).toHaveBeenCalledWith(1);
        await ctx.wrapper!.vm.$nextTick();
        
        expect(ctx.wrapper!.vm.relatorioAndamento).toHaveLength(1);
    });

    it('deve chamar a exportacao de pdf de andamento', async () => {
        mockObterRelatorioAndamento.mockResolvedValue([{ siglaUnidade: 'U1' }]);

        ctx.wrapper!.vm.codProcessoSelecionado = 1;
        await ctx.wrapper!.vm.$nextTick();
        
        await ctx.wrapper!.findAll('button')[0].trigger('click'); // Gerar andamento
        await ctx.wrapper!.vm.$nextTick(); // espera a promise do obterRelatorio

        // O segundo botao so aparece depois que a tabela tem dados
        const btnsPosQuery = ctx.wrapper!.findAll('button');
        await btnsPosQuery[1].trigger('click'); // Exportar PDF andamento

        expect(mockDownloadRelatorioAndamentoPdf).toHaveBeenCalledWith(1);
    });

    it('deve chamar a exportacao de pdf de mapas', async () => {
        ctx.wrapper!.vm.codProcessoSelecionadoMapas = 2;
        await ctx.wrapper!.vm.$nextTick();
        
        // O botao de mapas é o ultimo (index 1 antes de ter dados no andamento, ou index 2 se tiver)
        // Como o andamento ta vazio, temos 2 botoes disponiveis no DOM: [0] = Gerar andamento, [1] = Gerar PDF Mapas
        const btns = ctx.wrapper!.findAll('button');
        await btns[1].trigger('click');

        // Passa undefined como unidade pois nao foi selecionada
        expect(mockDownloadRelatorioMapasPdf).toHaveBeenCalledWith(2, undefined);
    });
});
