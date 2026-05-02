import {beforeEach, describe, expect, it, vi} from 'vitest';
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarReferenciaMapaVigente,
    buscarSubordinadas,
    buscarSuperior,
    buscarTodasUnidades,
    buscarUnidadePorCodigo,
    buscarUnidadePorSigla,
    mapUnidade,
    mapUnidadesArray,
    mapUnidadeSnapshot
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
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
      await buscarTodasUnidades();
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades');
    });

    it('deve chamar buscarUnidadePorSigla corretamente', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({});
      await buscarUnidadePorSigla('TESTE');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sigla/TESTE');
    });

    it('deve chamar buscarUnidadePorCodigo corretamente', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({});
      await buscarUnidadePorCodigo(123);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123');
    });

    it('deve chamar buscarArvoreComElegibilidade com processo', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
      await buscarArvoreComElegibilidade('MAPEAMENTO', 10);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO&codProcesso=10');
    });

    it('deve chamar buscarArvoreComElegibilidade sem processo', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
      await buscarArvoreComElegibilidade('MAPEAMENTO');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO');
    });

    it('deve chamar buscarArvoreUnidade corretamente', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
      await buscarArvoreUnidade(123);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123/arvore');
    });

    it('deve chamar buscarReferenciaMapaVigente corretamente', async () => {
      await buscarReferenciaMapaVigente(123);
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123/mapa-vigente/referencia');
    });

    it('deve chamar buscarSubordinadas corretamente', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
      await buscarSubordinadas('TESTE');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sigla/TESTE/subordinadas');
    });

    it('deve chamar buscarSuperior corretamente', async () => {
      vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({ codigo: 1, subunidades: [] });
      const res = await buscarSuperior('TESTE');
      expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sigla/TESTE/superior');
      expect(res).toMatchObject({ codigo: 1 });
    });
  });

  describe('mappers', () => {
    it('deve mapear mapUnidadeSnapshot corretamente', () => {
      const obj = {
        codigo: 1,
        nome: 'Teste',
        sigla: 'TST',
        subunidades: [{ codigo: 2, nome: 'Filha', sigla: 'FIL', subunidades: [] }]
      };
      const result = mapUnidadeSnapshot(obj);
      expect(result.codigo).toBe(1);
      expect(result.nome).toBe('Teste');
      expect(result.sigla).toBe('TST');
      expect(result.filhas).toHaveLength(1);
    });

    it('deve mapear mapUnidade corretamente', () => {
      const obj = {
        codigo: 1,
        sigla: 'TST',
        tipo: 'Setor',
        nome: 'Teste',
        isElegivel: true,
        usuarioCodigo: 2,
        responsavel: null,
        subunidades: []
      };
      const result = mapUnidade(obj);
      expect(result.codigo).toBe(1);
      expect(result.sigla).toBe('TST');
      expect(result.tipo).toBe('Setor');
      expect(result.nome).toBe('Teste');
      expect(result.isElegivel).toBe(true);
      expect(result.responsavel).toBeNull();
      expect(result.filhas).toEqual([]);
    });

    it('deve mapear mapUnidade com responsavel e titular', () => {
      const obj = {
        responsavel: {
          codigo: 1,
          nome: 'Resp',
          tituloEleitoral: '123',
          matricula: 'M1',
          email: 'r@r',
          ramal: '1'
        },
        titular: {
          codigo: 2,
          nome: 'Tit',
          tituloEleitoral: '456',
          matricula: 'M2',
          email: 't@t',
          ramal: '2'
        },
        subunidades: []
      };
      const result = mapUnidade(obj);
      expect(result.responsavel?.nome).toBe('Resp');
      expect(result.titular?.nome).toBe('Tit');
    });

    it('deve mapear mapUnidadesArray', () => {
      const result = mapUnidadesArray([{ codigo: 1, nome: 'Unidade 1', sigla: 'U1', subunidades: [] }]);
      expect(result).toHaveLength(1);
      expect(result[0].codigo).toBe(1);
    });
  });
});
