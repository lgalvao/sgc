import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import RelatoriosView from '@/views/RelatoriosView.vue';
import {TipoProcesso} from '@/types/tipos';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';
import {checkA11y} from "@/test-utils/a11yTestHelpers";

// Mock URL.createObjectURL
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url');

describe('RelatoriosView.vue', () => {
  const ctx = setupComponentTest();

  const mockProcessos = [
    {
      codigo: 1,
      descricao: 'Processo 1',
      tipo: TipoProcesso.MAPEAMENTO,
      situacao: 'EM_ANDAMENTO',
      dataCriacao: '2023-01-01T00:00:00',
      dataLimite: '2023-12-31T00:00:00',
      unidadeNome: 'Unidade 1'
    },
    {
      codigo: 2,
      descricao: 'Processo 2',
      tipo: TipoProcesso.REVISAO,
      situacao: 'CRIADO',
      dataCriacao: '2023-06-01T00:00:00',
      dataLimite: '2023-12-31T00:00:00',
      unidadeNome: 'Unidade 2'
    }
  ];

  const mockMapa = {
    codigo: 1,
    unidade: { sigla: 'TEST' },
    competencias: [{}, {}]
  };

  const stubs = {
    BContainer: { template: '<div><slot /></div>' },
    BCard: { template: '<div class="card" @click="$emit(\'click\')"><slot /></div>' },
    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
    BModal: { template: '<div v-if="modelValue" data-testid="modal"><slot /></div>', props: ['modelValue'] },
    BFormSelect: {
      template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"></select>',
      props: ['modelValue', 'options']
    },
    BFormInput: {
      template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
      props: ['modelValue']
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();

    const mountOptions = getCommonMountOptions({
      processos: {
        processosPainel: mockProcessos,
      },
      mapas: {
        mapaCompleto: mockMapa,
      },
    }, stubs);

    ctx.wrapper = mount(RelatoriosView, mountOptions);
  });
  
  afterEach(() => {
      vi.restoreAllMocks();
  });

  it('exibe cards de relatórios', () => {
    expect(ctx.wrapper!.find('[data-testid="card-relatorio-mapas"]').exists()).toBe(true);
    expect(ctx.wrapper!.find('[data-testid="card-relatorio-gaps"]').exists()).toBe(true);
    expect(ctx.wrapper!.find('[data-testid="card-relatorio-andamento"]').exists()).toBe(true);
  });

  it('filtra processos', async () => {
    expect(ctx.wrapper!.vm.processosFiltrados).toHaveLength(2);

    ctx.wrapper!.vm.filtroTipo = TipoProcesso.REVISAO;
    await ctx.wrapper!.vm.$nextTick();
    
    expect(ctx.wrapper!.vm.processosFiltrados).toHaveLength(1);
    expect(ctx.wrapper!.vm.processosFiltrados[0].tipo).toBe(TipoProcesso.REVISAO);
    
    ctx.wrapper!.vm.filtroTipo = '';
    ctx.wrapper!.vm.filtroDataInicio = '2023-05-01';
    await ctx.wrapper!.vm.$nextTick();
    
    expect(ctx.wrapper!.vm.processosFiltrados).toHaveLength(1);
    expect(ctx.wrapper!.vm.processosFiltrados[0].codigo).toBe(2);
  });

  it('abre modais ao clicar nos cards', async () => {
    await ctx.wrapper!.find('[data-testid="card-relatorio-mapas"]').trigger('click');
    expect(ctx.wrapper!.vm.mostrarModalMapasVigentes).toBe(true);
    
    ctx.wrapper!.vm.mostrarModalMapasVigentes = false;
    await ctx.wrapper!.vm.$nextTick();

    await ctx.wrapper!.find('[data-testid="card-relatorio-gaps"]').trigger('click');
    expect(ctx.wrapper!.vm.mostrarModalDiagnosticosGaps).toBe(true);
    
    ctx.wrapper!.vm.mostrarModalDiagnosticosGaps = false;
    await ctx.wrapper!.vm.$nextTick();
    
    await ctx.wrapper!.find('[data-testid="card-relatorio-andamento"]').trigger('click');
    expect(ctx.wrapper!.vm.mostrarModalAndamentoGeral).toBe(true);
  });

  it('exporta CSV para Mapas Vigentes', async () => {
    expect(ctx.wrapper!.vm.mapasVigentes).toHaveLength(1);

    ctx.wrapper!.vm.mostrarModalMapasVigentes = true;
    await ctx.wrapper!.vm.$nextTick();
    
    const btn = ctx.wrapper!.find('[data-testid="export-csv-mapas"]');
    
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
    const setAttributeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'setAttribute');
    
    await btn.trigger('click');
    
    expect(global.URL.createObjectURL).toHaveBeenCalled();
    expect(setAttributeSpy).toHaveBeenCalledWith('download', 'mapas-vigentes.csv');
    expect(clickSpy).toHaveBeenCalled();
  });

  it('exporta CSV para Diagnósticos Gaps', async () => {
    ctx.wrapper!.vm.mostrarModalDiagnosticosGaps = true;
    await ctx.wrapper!.vm.$nextTick();
    
    const btn = ctx.wrapper!.find('[data-testid="export-csv-diagnosticos"]');
    
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
    const setAttributeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'setAttribute');
    
    await btn.trigger('click');
    
    expect(setAttributeSpy).toHaveBeenCalledWith('download', 'diagnosticos-gaps.csv');
    expect(clickSpy).toHaveBeenCalled();
  });

  it('exporta CSV para Andamento Geral', async () => {
    ctx.wrapper!.vm.mostrarModalAndamentoGeral = true;
    await ctx.wrapper!.vm.$nextTick();
    
    const btn = ctx.wrapper!.find('[data-testid="export-csv-andamento"]');
    
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
    const setAttributeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'setAttribute');
    
    await btn.trigger('click');
    
    expect(setAttributeSpy).toHaveBeenCalledWith('download', 'andamento-geral.csv');
    expect(clickSpy).toHaveBeenCalled();
  });

  it('deve ser acessível', async () => {
    await checkA11y(ctx.wrapper!.element as HTMLElement);
  });

  it('filtra processos por data fim', async () => {
    ctx.wrapper!.vm.filtroDataInicio = '';
    ctx.wrapper!.vm.filtroDataFim = '2023-05-01';
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.vm.processosFiltrados).toHaveLength(1);
    expect(ctx.wrapper!.vm.processosFiltrados[0].codigo).toBe(1);
  });

  it('filtra diagnosticos gaps', async () => {
    expect(ctx.wrapper!.vm.diagnosticosGapsFiltrados).toHaveLength(4);

    ctx.wrapper!.vm.filtroTipo = TipoProcesso.MAPEAMENTO;
    await ctx.wrapper!.vm.$nextTick();
    expect(ctx.wrapper!.vm.diagnosticosGapsFiltrados).toHaveLength(0);

    ctx.wrapper!.vm.filtroTipo = TipoProcesso.DIAGNOSTICO;
    await ctx.wrapper!.vm.$nextTick();
    expect(ctx.wrapper!.vm.diagnosticosGapsFiltrados).toHaveLength(4);

    ctx.wrapper!.vm.filtroTipo = '';
    ctx.wrapper!.vm.filtroDataInicio = '2024-08-01';
    ctx.wrapper!.vm.filtroDataFim = '2024-08-31';
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.vm.diagnosticosGapsFiltrados).toHaveLength(2);
    expect(ctx.wrapper!.vm.diagnosticosGapsFiltrados.map((d: any) => d.id)).toContain(1);
    expect(ctx.wrapper!.vm.diagnosticosGapsFiltrados.map((d: any) => d.id)).toContain(2);
  });

  it('mapasVigentes retorna vazio se dados incompletos', async () => {
    const stubsLocal = {
      BContainer: { template: '<div><slot /></div>' },
      BCard: { template: '<div class="card"><slot /></div>' },
      BFormSelect: { template: '<select></select>' },
      BFormInput: { template: '<input />' },
    };

    const mountOptions = getCommonMountOptions({
      mapas: {
        mapaCompleto: {},
      },
    }, stubsLocal);

    const wrapper = mount(RelatoriosView, mountOptions);
    expect(wrapper.vm.mapasVigentes).toHaveLength(0);
  });
});
