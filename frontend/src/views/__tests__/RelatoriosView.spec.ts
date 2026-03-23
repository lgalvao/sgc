import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {ref} from 'vue';
import {flushPromises, mount} from '@vue/test-utils';
import Relatorios from '@/views/RelatoriosView.vue';
import {TipoProcesso} from '@/types/tipos';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';
import * as useRelatoriosModule from '@/composables/api/useRelatorios';
import * as useProcessosModule from '@/composables/useProcessos';

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: vi.fn(() => ({
        perfilSelecionado: ref('CHEFE')
    }))
}));

describe('Relatorios.vue', () => {
    const ctx = setupComponentTest();

    const mockProcessos = [
        {
            codigo: 1,
            descricao: 'Processo mapeamento 2024',
            tipo: TipoProcesso.MAPEAMENTO,
            dataCriacao: '2024-01-01',
            dataLimite: '2024-12-31'
        },
        {
            codigo: 2,
            descricao: 'Processo revisao 2024',
            tipo: TipoProcesso.REVISAO,
            dataCriacao: '2024-01-01',
            dataLimite: '2024-12-31'
        }
    ];

    const mockObterRelatorioAndamento = vi.fn().mockResolvedValue([]);
    const mockDownloadRelatorioAndamentoPdf = vi.fn().mockResolvedValue(true);
    const mockDownloadRelatorioMapasPdf = vi.fn().mockResolvedValue(true);

    const stubs = {
        LayoutPadrao: {template: '<div><slot /></div>'},
        BCard: {template: '<div class="card"><slot /></div>'},
        BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
        BTabs: {template: '<div><slot /></div>'},
        BTab: {template: '<div><slot /></div>'},
        BFormSelect: {
            name: 'BFormSelect',
            template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', Number($event.target.value))"></select>',
            props: ['modelValue', 'options']
        },
        BTable: {template: '<table class="b-table"></table>', props: ['items']},
        EmptyState: {template: '<div class="empty-state">Vazio</div>'},
        BRow: {template: '<div><slot /></div>'},
        BCol: {template: '<div><slot /></div>'},
        BFormGroup: {template: '<div><slot /></div>'},
        BSpinner: {template: '<div></div>'},
    };

    beforeEach(() => {
        vi.spyOn(useRelatoriosModule, 'useRelatorios').mockReturnValue({
            obterRelatorioAndamento: mockObterRelatorioAndamento,
            downloadRelatorioAndamentoPdf: mockDownloadRelatorioAndamentoPdf,
            downloadRelatorioMapasPdf: mockDownloadRelatorioMapasPdf,
            loading: ref(false),
            error: ref(null)
        } as any);

        vi.spyOn(useProcessosModule, 'useProcessos').mockReturnValue({
            processosPainel: ref(mockProcessos),
            buscarProcessosPainel: vi.fn().mockResolvedValue([])
        } as any);

        const mountOptions = getCommonMountOptions({}, stubs);

        ctx.wrapper = mount(Relatorios, mountOptions);
    });

    afterEach(() => {
        ctx.wrapper?.unmount();
    });

    it('deve renderizar o componente', () => {
        expect(ctx.wrapper!.exists()).toBe(true);
    });

    it('deve carregar processos ao montar', async () => {
        expect(ctx.wrapper!.vm.opcoesProcessos.length).toBeGreaterThan(1);
    });

    it('deve chamar a geracao de relatorio de andamento', async () => {
        ctx.wrapper!.vm.processoIdSelecionado = 1;
        await ctx.wrapper!.vm.$nextTick();
        
        const btns = ctx.wrapper!.findAll('button');
        await btns[0].trigger('click');

        expect(mockObterRelatorioAndamento).toHaveBeenCalledWith(1);
    });

    it('deve chamar a exportacao de pdf de andamento', async () => {
        ctx.wrapper!.vm.processoIdSelecionado = 1;
        await ctx.wrapper!.vm.$nextTick();
        
        // Simula que ja buscou dados (para aparecer o botao de exportar)
        ctx.wrapper!.vm.relatorioAndamento = [{}];
        await ctx.wrapper!.vm.$nextTick();

        // Agora temos 2 botoes no andamento: [0] = Gerar, [1] = Exportar PDF
        const btns = ctx.wrapper!.findAll('button');
        await btns[1].trigger('click');

        expect(mockDownloadRelatorioAndamentoPdf).toHaveBeenCalledWith(1);
    });

    it('deve chamar a exportacao de pdf de mapas', async () => {
        ctx.wrapper!.vm.processoIdSelecionadoMapas = 2;
        await ctx.wrapper!.vm.$nextTick();
        
        // O botao de mapas é o ultimo (index 1 antes de ter dados no andamento, ou index 2 se tiver)
        // Como o andamento ta vazio, temos 2 botoes disponiveis no DOM: [0] = Gerar andamento, [1] = Gerar PDF Mapas
        const btns = ctx.wrapper!.findAll('button');
        await btns[1].trigger('click');

        // Passa undefined como unidade pois nao foi selecionada
        expect(mockDownloadRelatorioMapasPdf).toHaveBeenCalledWith(2, undefined);
    });

    it('deve gerenciar estados de seleção e erros de geração de relatórios', async () => {
        const vm = ctx.wrapper!.vm;
        
        // Configuração de processos simulados
        vm.processosPainel = mockProcessos;
        await vm.$nextTick();

        // Atualização de v-model nos seletores de processo e unidade
        const selects = ctx.wrapper!.findAllComponents({name: 'BFormSelect'});
        if (selects.length > 0) {
            await selects[0].vm.$emit('update:modelValue', 1);
            expect(vm.processoIdSelecionado).toBe(1);
        }
        if (selects.length > 1) {
            await selects[1].vm.$emit('update:modelValue', 2);
            expect(vm.processoIdSelecionadoMapas).toBe(2);
        }
        if (selects.length > 2) {
            await selects[2].vm.$emit('update:modelValue', 3);
            expect(vm.unidadeIdSelecionadaMapas).toBe(3);
        }

        // Verificação de opções de processos disponíveis
        expect(vm.opcoesProcessos.length).toBeGreaterThan(1);

        // Tratamento de erros nas chamadas de serviço de relatórios
        mockObterRelatorioAndamento.mockRejectedValue(new Error('API Error'));
        vm.processoIdSelecionado = 1;
        await vm.gerarRelatorioAndamento();

        mockDownloadRelatorioAndamentoPdf.mockRejectedValue(new Error('PDF Error'));
        await vm.exportarPdfAndamento();

        mockDownloadRelatorioMapasPdf.mockRejectedValue(new Error('Map API Error'));
        vm.processoIdSelecionadoMapas = 1;
        await vm.gerarRelatorioMapas();

        // Retornos antecipados em funções de geração sem seleção de ID
        vm.processoIdSelecionado = null;
        await vm.gerarRelatorioAndamento();
        await vm.exportarPdfAndamento();
        vm.processoIdSelecionadoMapas = null;
        await vm.gerarRelatorioMapas();
    });

    it('deve carregar processos no onMounted se unidade estiver selecionada', async () => {
        const {usePerfilStore} = await import("@/stores/perfil");
        const perfilStore = usePerfilStore();
        perfilStore.unidadeSelecionada = {codigo: 5} as any;
        
        const _ = mount(Relatorios, getCommonMountOptions({
            processos: {
                buscarProcessosPainel: vi.fn().mockResolvedValue([])
            }
        }, stubs));
        await flushPromises();
    });
});
