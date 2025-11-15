import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useProcessosStore} from '@/stores/processos';
import {useAtividadesStore} from '@/stores/atividades';
import {ref} from 'vue';
import {SubprocessoDetalhe, ProcessoResumo, SituacaoProcesso, TipoProcesso, SituacaoSubprocesso} from '@/types/tipos';

const mockProcessosPainel: ProcessoResumo[] = [{
    codigo: 1,
    descricao: 'processo 1',
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    tipo: TipoProcesso.MAPEAMENTO,
    dataLimite: '2025-12-31',
    dataCriacao: '2025-01-01',
    unidadeCodigo: 1,
    unidadeNome: 'unidade 1',
}];

const mockProcessoDetalhe: SubprocessoDetalhe = {
    unidade: { codigo: 1, nome: 'Teste', sigla: 'TST' },
    titular: { codigo: 1, nome: 'Titular Teste', tituloEleitoral: '123', email: 't@t.com', ramal: '123', unidade: { codigo: 1, nome: 'Teste', sigla: 'TST' } },
    responsavel: { codigo: 1, nome: 'Responsável Teste', tituloEleitoral: '456', email: 'r@r.com', ramal: '456', unidade: { codigo: 1, nome: 'Teste', sigla: 'TST' } },
    situacao: SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO,
    situacaoLabel: 'Em Andamento',
    localizacaoAtual: 'Na unidade',
    processoDescricao: 'Processo Teste',
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    prazoEtapaAtual: '2025-12-31',
    isEmAndamento: true,
    etapaAtual: 1,
    movimentacoes: [],
    elementosProcesso: [],
    permissoes: {
        podeVerPagina: true,
        podeEditarMapa: true,
        podeVisualizarMapa: true,
        podeDisponibilizarCadastro: true,
        podeDevolverCadastro: true,
        podeAceitarCadastro: true,
        podeVisualizarDiagnostico: true,
        podeAlterarDataLimite: true,
    },
};

// Mock das stores
vi.mock('@/stores/processos', () => ({
  useProcessosStore: vi.fn(() => ({
    processosPainel: ref([]),
    processoDetalhe: ref(null),
    fetchProcessosPainel: vi.fn(),
    fetchProcessoDetalhe: vi.fn(),
  })),
}));

vi.mock('@/stores/atividades', () => ({
  useAtividadesStore: vi.fn(() => ({
    atividadesPorSubprocesso: ref(new Map()),
    getAtividadesPorSubprocesso: vi.fn().mockReturnValue([]),
    fetchAtividadesParaSubprocesso: vi.fn(),
    importarAtividades: vi.fn(),
  })),
}));

describe('ImportarAtividadesModal Store Logic', () => {
  let processosStore: ReturnType<typeof useProcessosStore>;
  let atividadesStore: ReturnType<typeof useAtividadesStore>;

  beforeEach(() => {
    setActivePinia(createPinia());
    processosStore = useProcessosStore();
    atividadesStore = useAtividadesStore();
    vi.clearAllMocks();

    // Setup default mocks
    (processosStore.fetchProcessosPainel as any).mockResolvedValue(undefined);
    (processosStore.fetchProcessoDetalhe as any).mockResolvedValue(undefined);
    (atividadesStore.fetchAtividadesParaSubprocesso as any).mockResolvedValue(undefined);
    processosStore.processosPainel = mockProcessosPainel as any;
  });

  it('should fetch available processes', async () => {
    await processosStore.fetchProcessosPainel('CHEFE', 1, 0, 10);
    expect(processosStore.fetchProcessosPainel).toHaveBeenCalled();
  });

  it('should fetch process details', async () => {
    (processosStore.processoDetalhe as any).value = mockProcessoDetalhe;
    await processosStore.fetchProcessoDetalhe(1);
    expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
  });

  it('should fetch activities', async () => {
    (processosStore.processoDetalhe as any).value = mockProcessoDetalhe;
    await atividadesStore.fetchAtividadesParaSubprocesso(101);
    expect(atividadesStore.fetchAtividadesParaSubprocesso).toHaveBeenCalledWith(101);
  });

  it('should import activities', async () => {
    await atividadesStore.importarAtividades(1, 101);
    expect(atividadesStore.importarAtividades).toHaveBeenCalledWith(1, 101);
  });
});
