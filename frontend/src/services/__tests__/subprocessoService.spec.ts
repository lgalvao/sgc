import {beforeEach, describe, expect, it, vi} from 'vitest';
import * as subprocessoService from '../subprocessoService';
import apiClient from '@/axios-setup';

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

  describe('importarAtividades', () => {
    it('deve chamar a API corretamente com codigos', async () => {
      await subprocessoService.importarAtividades(1, 2, [3, 4]);
      expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/importar-atividades', {
        codSubprocessoOrigem: 2,
        codigosAtividades: [3, 4]
      });
    });

    it('deve chamar a API corretamente sem codigos', async () => {
      await subprocessoService.importarAtividades(1, 2);
      expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/importar-atividades', {
        codSubprocessoOrigem: 2
      });
    });
  });

  it('listarAtividades', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: { atividadesDisponiveis: [] } });
    await subprocessoService.listarAtividades(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/contexto-edicao');
  });

  it('listarAtividadesParaImportacao', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: [] });
    await subprocessoService.listarAtividadesParaImportacao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/atividades-importacao');
  });

  it('validarCadastro', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: { isValid: true } });
    await subprocessoService.validarCadastro(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/validar-cadastro');
  });

  it('obterStatus', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.obterStatus(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/status');
  });

  it('buscarSubprocessoDetalhe', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.buscarSubprocessoDetalhe(1, 'ADMIN', 2);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1', {
      params: { perfil: 'ADMIN', unidadeUsuario: 2 }
    });
  });

  it('buscarContextoEdicao', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.buscarContextoEdicao(1, 'ADMIN', 2);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/contexto-edicao', {
      params: { perfil: 'ADMIN', unidadeUsuario: 2 }
    });
  });

  it('buscarSubprocessoPorProcessoEUnidade', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.buscarSubprocessoPorProcessoEUnidade(1, 'SIGLA');
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/buscar', {
      params: { codProcesso: 1, siglaUnidade: 'SIGLA' }
    });
  });

  it('obterMapaVisualizacao', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.obterMapaVisualizacao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/mapa-visualizacao');
  });

  it('verificarImpactosMapa', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ 
      data: { 
        temImpactos: true,
        inseridas: [1],
        totalInseridas: 1
      } 
    });
    const result = await subprocessoService.verificarImpactosMapa(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/impactos-mapa');
    expect(result.temImpactos).toBe(true);
    expect(result.atividadesInseridas).toEqual([1]);
    expect(result.atividadesRemovidas).toEqual([]); // fallback
    expect(result.totalAtividadesInseridas).toBe(1);
  });

  it('obterMapaCompleto', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.obterMapaCompleto(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/mapa-completo');
  });

  it('salvarMapaCompleto', async () => {
    (apiClient.post as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.salvarMapaCompleto(1, { foo: 'bar' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/mapa-completo', { foo: 'bar' });
  });

  it('obterMapaAjuste', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.obterMapaAjuste(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/mapa-ajuste');
  });

  it('salvarMapaAjuste', async () => {
    (apiClient.post as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.salvarMapaAjuste(1, { foo: 'bar' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/mapa-ajuste/atualizar', { foo: 'bar' });
  });

  it('verificarMapaVigente', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: { temMapaVigente: true } });
    const result = await subprocessoService.verificarMapaVigente(1);
    expect(apiClient.get).toHaveBeenCalledWith('/unidades/1/mapa-vigente');
    expect(result).toBe(true);
  });

  it('verificarMapaVigente catch fallback', async () => {
    (apiClient.get as any).mockRejectedValueOnce(new Error('fail'));
    const result = await subprocessoService.verificarMapaVigente(1);
    expect(result).toBe(false);
  });

  it('disponibilizarMapa', async () => {
    await subprocessoService.disponibilizarMapa(1, { dataLimite: '2025-01-01', observacoes: 'teste' });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/disponibilizar-mapa', { dataLimite: '2025-01-01', observacoes: 'teste' });
  });

  it('adicionarCompetencia', async () => {
    (apiClient.post as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.adicionarCompetencia(1, { descricao: 'desc', atividadesIds: [1] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia', { descricao: 'desc', atividadesIds: [1] });
  });

  it('atualizarCompetencia', async () => {
    (apiClient.post as any).mockResolvedValueOnce({ data: {} });
    await subprocessoService.atualizarCompetencia(1, 2, { descricao: 'desc', atividadesIds: [1] });
    expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/1/competencia/2', { descricao: 'desc', atividadesIds: [1] });
  });

  it('removerCompetencia', async () => {
    (apiClient.post as any).mockResolvedValueOnce({ data: {} });
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
    (apiClient.get as any).mockResolvedValueOnce({ data: [] });
    await subprocessoService.listarAnalisesCadastro(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/historico-cadastro');
  });

  it('listarAnalisesValidacao', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: [] });
    await subprocessoService.listarAnalisesValidacao(1);
    expect(apiClient.get).toHaveBeenCalledWith('/subprocessos/1/historico-validacao');
  });
});
