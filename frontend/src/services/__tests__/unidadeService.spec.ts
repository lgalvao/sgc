import { describe, it, expect, vi, beforeEach } from 'vitest';
import { 
  buscarTodasUnidades, 
  buscarUnidadePorSigla, 
  buscarUnidadePorCodigo, 
  buscarArvoreComElegibilidade,
  buscarArvoreUnidade,
  buscarSubordinadas,
  buscarSuperior,
  mapUnidadeSnapshot,
  mapUnidade,
  mapUnidadesArray
} from '../unidadeService';
import * as apiUtils from '@/utils/apiUtils';

vi.mock('@/utils/apiUtils', () => ({
  apiGet: vi.fn(),
}));

describe('unidadeService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('API calls', () => {
    it('deve chamar buscarTodasUnidades corretamente', async () => {
      await buscarTodasUnidades();
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades');
    });

    it('deve chamar buscarUnidadePorSigla corretamente', async () => {
      await buscarUnidadePorSigla('TESTE');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sigla/TESTE');
    });

    it('deve chamar buscarUnidadePorCodigo corretamente', async () => {
      await buscarUnidadePorCodigo(123);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123');
    });

    it('deve chamar buscarArvoreComElegibilidade com processo', async () => {
      await buscarArvoreComElegibilidade('MAPEAMENTO', 10);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO&codProcesso=10');
    });

    it('deve chamar buscarArvoreComElegibilidade sem processo', async () => {
      await buscarArvoreComElegibilidade('MAPEAMENTO');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO');
    });

    it('deve chamar buscarArvoreUnidade corretamente', async () => {
      await buscarArvoreUnidade(123);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123/arvore');
    });

    it('deve chamar buscarSubordinadas corretamente', async () => {
      await buscarSubordinadas('TESTE');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sigla/TESTE/subordinadas');
    });

    it('deve chamar buscarSuperior corretamente', async () => {
      (apiUtils.apiGet as any).mockResolvedValueOnce({ codigo: 1 });
      const res = await buscarSuperior('TESTE');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sigla/TESTE/superior');
      expect(res).toEqual({ codigo: 1 });
    });
  });

  describe('mappers', () => {
    it('deve mapear mapUnidadeSnapshot corretamente', () => {
      const obj = {
        codigo: 1,
        nome: 'Teste',
        sigla: 'TST',
        filhas: [{ codigo: 2, nome: 'Filha', sigla: 'FIL' }]
      };
      const result = mapUnidadeSnapshot(obj);
      expect(result.codigo).toBe(1);
      expect(result.nome).toBe('Teste');
      expect(result.sigla).toBe('TST');
      expect(result.filhas).toHaveLength(1);
    });

    it('deve mapear mapUnidadeSnapshot com defaults e fallbacks', () => {
      const obj = {
        nome_unidade: 'Teste',
        unidade: 'TST',
        subunidades: []
      };
      const result = mapUnidadeSnapshot(obj);
      expect(result.codigo).toBe(0);
      expect(result.nome).toBe('Teste');
      expect(result.sigla).toBe('TST');
      expect(result.filhas).toEqual([]);
    });

    it('deve mapear mapUnidade corretamente', () => {
      const obj = {
        codigo_unidade: 1,
        sigla_unidade: 'TST',
        tipo_unidade: 'Setor',
        nome_unidade: 'Teste',
        isElegivel: true,
        idServidorTitular: 2,
        responsavel: null,
        subunidades: null
      };
      const result = mapUnidade(obj);
      expect(result.codigo).toBe(1);
      expect(result.sigla).toBe('TST');
      expect(result.tipo).toBe('Setor');
      expect(result.nome).toBe('Teste');
      expect(result.isElegivel).toBe(true);
      expect(result.usuarioCodigo).toBe(2);
      expect(result.responsavel).toBeNull();
      expect(result.filhas).toEqual([]);
    });

    it('deve mapear mapUnidade com responsavel', () => {
      const obj = {
        responsavel: {
          codigo: 1,
          nome: 'Resp',
          tituloEleitoral: '123'
        }
      };
      const result = mapUnidade(obj);
      expect(result.responsavel?.nome).toBe('Resp');
      expect(result.responsavel?.tituloEleitoral).toBe('123');
    });

    it('deve mapear mapUnidadesArray', () => {
      const result = mapUnidadesArray([{ codigo: 1 }]);
      expect(result).toHaveLength(1);
      expect(result[0].codigo).toBe(1);
    });
    
    it('deve mapear mapUnidadesArray com valor default', () => {
      const result = mapUnidadesArray(undefined as unknown as any[]);
      expect(result).toHaveLength(0);
    });
  });
});
