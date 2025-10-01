import {describe, expect, it} from 'vitest';
import atividades from '../../mocks/atividades.json';
import mapas from '../../mocks/mapas.json';
import analises from '../../mocks/analises.json';
import subprocessos from '../../mocks/subprocessos.json';
import unidades from '../../mocks/unidades.json';
import type {Unidade} from '@/types/tipos';
import {ResultadoAnalise, TipoResponsabilidade} from '@/types/tipos';

describe('Validação de Mocks', () => {
  describe('Atividades', () => {
    it('deve incluir conhecimentos em todas as atividades', () => {
      atividades.forEach((atividade) => {
        expect(atividade.conhecimentos).toBeDefined();
        expect(Array.isArray(atividade.conhecimentos)).toBe(true);
        expect(atividade.conhecimentos.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Mapas', () => {
    it('deve conter pelo menos três atividades em todos os mapas', () => {
      mapas.forEach((mapa) => {
        const totalAtividades = mapa.competencias.reduce(
          (soma, competencia) => soma + competencia.atividadesAssociadas.length,
          0
        );
        expect(totalAtividades).toBeGreaterThanOrEqual(3);
      });
    });
  });

  describe('Análises', () => {
    it("deve ter o resultado como 'Aceite' ou 'Devolução'", () => {
      const valoresValidos = Object.values(ResultadoAnalise);
      analises.forEach((analise) => {
        expect(valoresValidos).toContain(analise.resultado);
      });
    });
  });

  describe('Subprocessos', () => {
    it("deve ter os campos obrigatórios 'unidade' e 'dataLimiteEtapa1'", () => {
      subprocessos.forEach((subprocesso) => {
        expect(subprocesso.unidade).toBeDefined();
        expect(typeof subprocesso.unidade).toBe('string');
        expect(subprocesso.unidade.length).toBeGreaterThan(0);
        expect(subprocesso.dataLimiteEtapa1).toBeDefined();
      });
    });
  });

  describe('Unidades', () => {
    it("deve ter o tipo de responsabilidade como 'Substituição' ou 'Atribuição temporária' se houver responsável", () => {
      const valoresValidos = Object.values(TipoResponsabilidade);
      const checarUnidades = (unidadesParaChecar: Unidade[]) => {
        unidadesParaChecar.forEach((unidade) => {
          if (unidade.responsavel) {
            expect(valoresValidos).toContain(unidade.responsavel.tipo);
          }
          if (unidade.filhas && unidade.filhas.length > 0) {
            checarUnidades(unidade.filhas);
          }
        });
      };
       
      checarUnidades(unidades as any as Unidade[]);
    });
  });
});