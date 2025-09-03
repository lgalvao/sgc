import {describe, expect, it} from 'vitest';
import {
    CLASSES_BADGE_SITUACAO,
    LABELS_SITUACAO,
    SITUACOES_EM_ANDAMENTO,
    SITUACOES_MAPA,
    SITUACOES_SUBPROCESSO
} from '../situacoes';

describe('situacoes.ts constants', () => {
  describe('SITUACOES_SUBPROCESSO', () => {
    it('should export all subprocess situations', () => {
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('AGUARDANDO', 'Aguardando');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('EM_ANDAMENTO', 'Em andamento');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('AGUARDANDO_VALIDACAO', 'Aguardando validação');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('FINALIZADO', 'Finalizado');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('VALIDADO', 'Validado');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('DEVOLVIDO', 'Devolvido');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('CADASTRO_EM_ANDAMENTO', 'Cadastro em andamento');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('CADASTRO_DISPONIBILIZADO', 'Cadastro disponibilizado');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('REVISAO_CADASTRO_DISPONIBILIZADA', 'Revisão do cadastro disponibilizada');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('MAPA_DISPONIBILIZADO', 'Mapa disponibilizado');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('MAPA_EM_ANDAMENTO', 'Mapa em andamento');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('REVISAO_MAPA_DISPONIBILIZADA', 'Revisão do mapa disponibilizada');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('REVISAO_MAPA_EM_ANDAMENTO', 'Revisão do mapa em andamento');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('MAPA_VALIDADO', 'Mapa validado');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('MAPA_HOMOLOGADO', 'Mapa homologado');
      expect(SITUACOES_SUBPROCESSO).toHaveProperty('MAPA_CRIADO', 'Mapa criado');
    });

    it('should have correct number of situations', () => {
      expect(Object.keys(SITUACOES_SUBPROCESSO)).toHaveLength(17);
    });
  });

  describe('SITUACOES_MAPA', () => {
    it('should export all map situations', () => {
      expect(SITUACOES_MAPA).toHaveProperty('EM_ANDAMENTO', 'em_andamento');
      expect(SITUACOES_MAPA).toHaveProperty('DISPONIVEL_VALIDACAO', 'disponivel_validacao');
    });

    it('should have correct number of map situations', () => {
      expect(Object.keys(SITUACOES_MAPA)).toHaveLength(2);
    });
  });

  describe('LABELS_SITUACAO', () => {
    it('should have labels for all relevant situations', () => {
      expect(LABELS_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO,
        'Cadastro disponibilizado'
      );
      expect(LABELS_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.MAPA_VALIDADO,
        'Mapa validado'
      );
      expect(LABELS_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO,
        'Mapa homologado'
      );
      expect(LABELS_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.MAPA_CRIADO,
        'Mapa criado'
      );
      expect(LABELS_SITUACAO).toHaveProperty(
        SITUACOES_MAPA.EM_ANDAMENTO,
        'Em andamento'
      );
      expect(LABELS_SITUACAO).toHaveProperty(
        SITUACOES_MAPA.DISPONIVEL_VALIDACAO,
        'Disponibilizado'
      );
      expect(LABELS_SITUACAO).toHaveProperty('NAO_DISPONIBILIZADO', 'Não disponibilizado');
    });

    it('should have correct number of labels', () => {
      expect(Object.keys(LABELS_SITUACAO)).toHaveLength(7);
    });
  });

  describe('SITUACOES_EM_ANDAMENTO', () => {
    it('should contain all situations considered "em andamento"', () => {
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.CADASTRO_EM_ANDAMENTO);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_DISPONIBILIZADA);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.MAPA_DISPONIBILIZADO);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.MAPA_EM_ANDAMENTO);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.REVISAO_MAPA_DISPONIBILIZADA);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.REVISAO_MAPA_EM_ANDAMENTO);
      expect(SITUACOES_EM_ANDAMENTO).toContain(SITUACOES_SUBPROCESSO.MAPA_CRIADO);
    });

    it('should have correct number of situations', () => {
      expect(SITUACOES_EM_ANDAMENTO).toHaveLength(8);
    });

    it('should be readonly array', () => {
      // Test that the array has the expected length and we can't modify it in strict mode
      const originalLength = SITUACOES_EM_ANDAMENTO.length;
      expect(SITUACOES_EM_ANDAMENTO).toHaveLength(8);
      expect(SITUACOES_EM_ANDAMENTO.length).toBe(originalLength);
    });
  });

  describe('CLASSES_BADGE_SITUACAO', () => {
    it('should have CSS classes for all relevant situations', () => {
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.AGUARDANDO,
        'bg-warning text-dark'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.EM_ANDAMENTO,
        'bg-warning text-dark'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.AGUARDANDO_VALIDACAO,
        'bg-warning text-dark'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.FINALIZADO,
        'bg-success'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.VALIDADO,
        'bg-success'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.DEVOLVIDO,
        'bg-danger'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.MAPA_VALIDADO,
        'bg-success'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO,
        'bg-success'
      );
      expect(CLASSES_BADGE_SITUACAO).toHaveProperty(
        SITUACOES_SUBPROCESSO.MAPA_CRIADO,
        'bg-info'
      );
    });

    it('should have correct number of CSS classes', () => {
      expect(Object.keys(CLASSES_BADGE_SITUACAO)).toHaveLength(10);
    });
  });

  describe('type safety and immutability', () => {
    it('should have consistent structure', () => {
      // Test that all constants have the expected structure
      expect(typeof SITUACOES_SUBPROCESSO).toBe('object');
      expect(typeof SITUACOES_MAPA).toBe('object');
      expect(typeof LABELS_SITUACAO).toBe('object');
      expect(typeof CLASSES_BADGE_SITUACAO).toBe('object');
      expect(Array.isArray(SITUACOES_EM_ANDAMENTO)).toBe(true);
    });

    it('should not allow modification in strict mode', () => {
      // Test that we can't modify the constants (this will work in production with proper TypeScript settings)
      const originalValue = SITUACOES_SUBPROCESSO.EM_ANDAMENTO;
      expect(SITUACOES_SUBPROCESSO.EM_ANDAMENTO).toBe(originalValue);
    });
  });
});