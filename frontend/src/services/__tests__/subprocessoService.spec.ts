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
  getOrNull: vi.fn((fn) => fn().catch(() => null)),
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

  it('listarAtividades', async () => {
    getMock.mockResolvedValueOnce({ data: { atividadesDisponiveis: [] } } as never);
    await subprocessoService.listarAtividades(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/contexto-edicao');
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

  it('obterStatus', async () => {
    getMock.mockResolvedValueOnce({ data: { codigo: 1, situacao: SituacaoSubprocesso.NAO_INICIADO } } as never);
    await subprocessoService.obterStatus(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/status');
  });

  it('buscarSubprocessoDetalhe', async () => {
    getMock.mockResolvedValueOnce({ data: {
      subprocesso: {
        codigo: 1,
        unidade: { codigo: 1, nome: 'Unidade', sigla: 'UND' },
        situacao: SituacaoSubprocesso.NAO_INICIADO,
        dataLimiteEtapa1: '2025-01-01T00:00:00',
        dataFimEtapa1: null,
        dataLimiteEtapa2: null,
        dataFimEtapa2: null,
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
      permissoes: {},
    } } as never);
    await subprocessoService.buscarSubprocessoDetalhe(1, 'ADMIN', 2);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1', {
      params: { perfil: 'ADMIN', unidadeUsuario: 2 }
    });
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
        permissoes: {},
      },
      mapa: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' },
      atividadesDisponiveis: [],
    } } as never);
    const resultado = await subprocessoService.buscarContextoEdicao(1, 'ADMIN', 2);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/contexto-edicao', {
      params: { perfil: 'ADMIN', unidadeUsuario: 2 }
    });
    expect(resultado.detalhes).toMatchObject({
      codigo: 1,
      processoDescricao: 'Processo',
      situacao: SituacaoSubprocesso.NAO_INICIADO,
      ultimaDataLimiteSubprocesso: '2025-02-01T00:00:00',
      etapaAtual: 1,
      localizacaoAtual: 'UND',
    });
  });

  it('buscarSubprocessoPorProcessoEUnidade', async () => {
    getMock.mockResolvedValueOnce({ data: { codigo: 1 } } as never);
    await subprocessoService.buscarSubprocessoPorProcessoEUnidade(1, 'SIGLA');
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/buscar', {
      params: { codProcesso: 1, siglaUnidade: 'SIGLA' }
    });
  });

  it('obterMapaVisualizacao', async () => {
    getMock.mockResolvedValueOnce({ data: { codigo: 1, descricao: '', competencias: [] } } as never);
    await subprocessoService.obterMapaVisualizacao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/mapa-visualizacao');
  });

  it('verificarImpactosMapa', async () => {
    getMock.mockResolvedValueOnce({ 
      data: { 
        temImpactos: true,
        inseridas: [],
        totalInseridas: 1
      } 
    } as never);
    const result = await subprocessoService.verificarImpactosMapa(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/impactos-mapa');
    expect(result.temImpactos).toBe(true);
    expect(result.atividadesInseridas).toEqual([]);
    expect(result.atividadesRemovidas).toEqual([]); // fallback
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

  it('obterMapaAjuste', async () => {
    getMock.mockResolvedValueOnce({ data: { codigo: 1, descricao: '', competencias: [] } } as never);
    await subprocessoService.obterMapaAjuste(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/mapa-ajuste');
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

  it('adicionarCompetencia', async () => {
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.adicionarCompetencia(1, { descricao: 'desc', atividadesIds: [1] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia', { descricao: 'desc', atividadesIds: [1] });
  });

  it('atualizarCompetencia', async () => {
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.atualizarCompetencia(1, 2, { descricao: 'desc', atividadesIds: [1] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia/2', { descricao: 'desc', atividadesIds: [1] });
  });

  it('removerCompetencia', async () => {
    postMock.mockResolvedValueOnce({ data: { codigo: 1, subprocessoCodigo: 1, observacoes: '', competencias: [], situacao: '' } } as never);
    await subprocessoService.removerCompetencia(1, 2);
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia/2/remover');
  });

  it('aceitarCadastroEmBloco', async () => {
    await subprocessoService.aceitarCadastroEmBloco(1, { unidadeCodigos: [2] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/aceitar-cadastro-bloco', { acao: 'ACEITAR', subprocessos: [2] });
  });

  it('homologarCadastroEmBloco', async () => {
    await subprocessoService.homologarCadastroEmBloco(1, { unidadeCodigos: [2] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/homologar-cadastro-bloco', { acao: 'HOMOLOGAR', subprocessos: [2] });
  });

  it('aceitarValidacaoEmBloco', async () => {
    await subprocessoService.aceitarValidacaoEmBloco(1, { unidadeCodigos: [2] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/aceitar-validacao-bloco', { acao: 'ACEITAR_VALIDACAO', subprocessos: [2] });
  });

  it('homologarValidacaoEmBloco', async () => {
    await subprocessoService.homologarValidacaoEmBloco(1, { unidadeCodigos: [2] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/homologar-validacao-bloco', { acao: 'HOMOLOGAR_VALIDACAO', subprocessos: [2] });
  });

  it('disponibilizarMapaEmBloco', async () => {
    await subprocessoService.disponibilizarMapaEmBloco(1, { unidadeCodigos: [2], dataLimite: '2024-01-01' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/disponibilizar-mapa-bloco', { acao: 'DISPONIBILIZAR', subprocessos: [2], dataLimite: '2024-01-01' });
  });

  it('listarAnalisesCadastro', async () => {
    getMock.mockResolvedValueOnce({ data: [] } as never);
    await subprocessoService.listarAnalisesCadastro(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/historico-cadastro');
  });

  it('listarAnalisesValidacao', async () => {
    getMock.mockResolvedValueOnce({ data: [] } as never);
    await subprocessoService.listarAnalisesValidacao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/historico-validacao');
  });
});
