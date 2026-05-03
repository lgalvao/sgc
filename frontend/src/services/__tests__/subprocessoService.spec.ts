import {beforeEach, describe, expect, it, vi} from 'vitest';
import * as subprocessoService from '../subprocessoService';
import apiClient from '@/axios-setup';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';

vi.mock('@/axios-setup', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

vi.mock('@/utils/apiError', () => ({
  normalizarErro: vi.fn((err) => ({ kind: 'inesperado', message: 'Erro' }))
}));

describe('subprocessoService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const getMock = vi.mocked(apiClient.get);
  const postMock = vi.mocked(apiClient.post);

  describe('importarAtividades', () => {
    it('deve chamar a API corretamente com codigos', async () => {
      postMock.mockResolvedValueOnce({ data: { message: 'Atividades importadas.' } } as never);
      await subprocessoService.importarAtividades(1, 2, [3, 4]);
      expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/importar-atividades', {
        codSubprocessoOrigem: 2,
        codigosAtividades: [3, 4]
      });
    });

    it('deve chamar a API corretamente sem codigos', async () => {
      postMock.mockResolvedValueOnce({ data: { message: 'Atividades importadas.' } } as never);
      await subprocessoService.importarAtividades(1, 2);
      expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/importar-atividades', {
        codSubprocessoOrigem: 2
      });
    });

    it('deve retornar aviso quando há duplicatas', async () => {
      const aviso = 'Uma ou mais atividades selecionadas já existem no cadastro e não foram importadas.';
      postMock.mockResolvedValueOnce({ data: { message: 'Atividades importadas.', aviso } } as never);
      const resultado = await subprocessoService.importarAtividades(1, 2, [3]);
      expect(resultado.aviso).toBe(aviso);
    });
  });

  it('listarAtividadesParaImportacao', async () => {
    getMock.mockResolvedValueOnce({ data: [] } as never);
    await subprocessoService.listarAtividadesParaImportacao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/atividades-importacao');
  });

  it('validarCadastro', async () => {
    getMock.mockResolvedValueOnce({ data: { valido: true, erros: [] } } as never);
    await subprocessoService.validarCadastro(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/validar-cadastro');
  });

  it('alterarDataLimiteSubprocesso', async () => {
    await subprocessoService.alterarDataLimiteSubprocesso(1, { novaData: '2024-12-31' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/data-limite', {
      data: '2024-12-31',
    });
  });

  it('reabrirCadastro', async () => {
    await subprocessoService.reabrirCadastro(1, 'Erro');
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/reabrir-cadastro', { justificativa: 'Erro' });
  });

  it('reabrirRevisaoCadastro', async () => {
    await subprocessoService.reabrirRevisaoCadastro(1, 'Erro');
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/reabrir-revisao-cadastro', { justificativa: 'Erro' });
  });

  it('buscarContextoEdicao', async () => {
    getMock.mockResolvedValueOnce({ data: {
      unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
      subprocesso: {
        codigo: 1,
        unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
        situacao: SituacaoSubprocesso.NAO_INICIADO,
        dataLimite: '2025-01-01T00:00:00',
        dataFimEtapa1: '',
        dataLimiteEtapa2: '',
        atividades: [],
        codUnidade: 1,
      },
      detalhes: {
        subprocesso: {
          codigo: 1,
          unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
          situacao: SituacaoSubprocesso.NAO_INICIADO,
          dataLimiteEtapa1: '2025-01-01T00:00:00',
          dataFimEtapa1: null,
          dataLimiteEtapa2: '2025-02-01T00:00:00',
          dataFimEtapa2: null,
          ultimaDataLimite: '2025-02-01T00:00:00',
          processoDescricao: 'Processo',
          dataCriacaoProcesso: '2024-01-01T00:00:00',
          tipoProcesso: TipoProcesso.MAPEAMENTO,
          isEmAndamento: true,
          etapaAtual: null,
        },
        titular: null,
        responsavel: null,
        movimentacoes: [],
        localizacaoAtual: 'UND',
        permissoes: null,
      },
      mapa: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], atividades: [], situacao: '' },
    } } as never);
    const resultado = await subprocessoService.buscarContextoEdicao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/contexto-edicao');
    expect(resultado.detalhes).toMatchObject({
      codigo: 1,
      processoDescricao: 'Processo',
      situacao: SituacaoSubprocesso.NAO_INICIADO,
      ultimaDataLimiteSubprocesso: '2025-02-01T00:00:00',
      etapaAtual: null,
      localizacaoAtual: 'UND',
    });
  });

  it('buscarContextoEdicaoPorProcessoEUnidade', async () => {
    getMock.mockResolvedValueOnce({ data: {
      unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
      subprocesso: {
        codigo: 1,
        unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
        situacao: SituacaoSubprocesso.NAO_INICIADO,
        dataLimite: '2025-01-01T00:00:00',
        dataFimEtapa1: '',
        dataLimiteEtapa2: '',
        atividades: [],
        codUnidade: 1,
      },
      detalhes: {
        subprocesso: {
          codigo: 1,
          unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
          situacao: SituacaoSubprocesso.NAO_INICIADO,
          dataLimiteEtapa1: '2025-01-01T00:00:00',
          dataFimEtapa1: null,
          dataLimiteEtapa2: null,
          dataFimEtapa2: null,
          ultimaDataLimite: '2025-01-01T00:00:00',
          processoDescricao: 'Processo',
          dataCriacaoProcesso: '2024-01-01T00:00:00',
          tipoProcesso: TipoProcesso.MAPEAMENTO,
          isEmAndamento: true,
          etapaAtual: 1,
        },
        titular: null,
        responsavel: null,
        movimentacoes: [],
        localizacaoAtual: 'UND',
        permissoes: null,
      },
      mapa: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' },
      atividadesDisponiveis: [],
    } } as never);
    const resultado = await subprocessoService.buscarContextoEdicaoPorProcessoEUnidade(1, 'SIGLA');
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/contexto-edicao/buscar', {
      params: { codProcesso: 1, siglaUnidade: 'SIGLA' }
    });
    expect(resultado.detalhes).toMatchObject({
      codigo: 1,
      processoDescricao: 'Processo',
      situacao: SituacaoSubprocesso.NAO_INICIADO,
      localizacaoAtual: 'UND',
    });
  });

  it('obterSugestoesMapa', async () => {
    getMock.mockResolvedValueOnce({ data: { sugestoes: 'Texto salvo' } } as never);
    const resultado = await subprocessoService.obterSugestoesMapa(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/sugestoes');
    expect(resultado).toBe('Texto salvo');
  });

  it('verificarImpactosMapa', async () => {
    getMock.mockResolvedValueOnce({ 
      data: { 
        temImpactos: true,
        inseridas: [],
        removidas: [],
        alteradas: [],
        competenciasImpactadas: [],
        totalInseridas: 1,
        totalRemovidas: 0,
        totalAlteradas: 0,
        totalCompetenciasImpactadas: 0,
      } 
    } as never);
    const result = await subprocessoService.verificarImpactosMapa(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/impactos-mapa');
    expect(result.temImpactos).toBe(true);
    expect(result.atividadesInseridas).toEqual([]);
    expect(result.atividadesRemovidas).toEqual([]);
    expect(result.totalAtividadesInseridas).toBe(1);
  });

  it('obterMapaCompleto', async () => {
    getMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.obterMapaCompleto(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/mapa-completo');
  });

  it('salvarMapaCompleto', async () => {
    const payload = { competencias: [] };
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.salvarMapaCompleto(1, payload);
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/mapa-completo', payload);
  });

  it('salvarMapaAjuste', async () => {
    const payload = { competencias: [], atividades: [], sugestoes: '' };
    postMock.mockResolvedValueOnce({ data: {} } as never);
    await subprocessoService.salvarMapaAjuste(1, payload);
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/mapa-ajuste/atualizar', payload);
  });

  it('disponibilizarMapa', async () => {
    await subprocessoService.disponibilizarMapa(1, { dataLimite: '2025-01-01', observacoes: 'teste' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/disponibilizar-mapa', { dataLimite: '2025-01-01', observacoes: 'teste' });
  });

  it('validarMapa', async () => {
    await subprocessoService.validarMapa(1);
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/validar-mapa');
  });

  it('homologarValidacao', async () => {
    await subprocessoService.homologarValidacao(1, { texto: 'Obs' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/homologar-validacao', { texto: 'Obs' });
  });

  it('aceitarValidacao', async () => {
    await subprocessoService.aceitarValidacao(1, { texto: 'Obs' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/aceitar-validacao', { texto: 'Obs' });
  });

  it('devolverValidacao', async () => {
    await subprocessoService.devolverValidacao(1, { justificativa: 'Erro' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/devolver-validacao', { justificativa: 'Erro' });
  });

  it('adicionarCompetencia', async () => {
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.adicionarCompetencia(1, { descricao: 'desc', atividadesCodigos: [1] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia', { descricao: 'desc', atividadesCodigos: [1] });
  });

  it('atualizarCompetencia', async () => {
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.atualizarCompetencia(1, 2, { descricao: 'desc', atividadesCodigos: [1] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia/2', { descricao: 'desc', atividadesCodigos: [1] });
  });

  it('removerCompetencia', async () => {
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.removerCompetencia(1, 2);
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia/2/remover');
  });

  it('apresentarSugestoes', async () => {
    await subprocessoService.apresentarSugestoes(1, { sugestoes: 'Texto' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/apresentar-sugestoes', {
      texto: 'Texto',
    });
  });
});
