import { describe, it, expect } from 'vitest';
import atividades from '../../mocks/atividades.json';
import mapas from '../../mocks/mapas.json';

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
});