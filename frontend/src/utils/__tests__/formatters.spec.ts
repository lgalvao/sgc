import {describe, expect, it} from 'vitest';
import {
    formatDate,
    formatNumeroCsvBR,
    formatSituacaoProcesso,
    formatSituacaoSubprocesso,
    formatTipoProcesso
} from '../formatters';
import {SituacaoProcesso, SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';

describe('formatters', () => {
  describe('formatDate', () => {
    it('deve retornar string vazia para data nula ou indefinida', () => {
      expect(formatDate(null)).toBe('');
      expect(formatDate(undefined)).toBe('');
      expect(formatDate('')).toBe('');
    });

    it('deve formatar data valida sem hora', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      expect(formatDate(date, false)).toMatch(/15\/01\/2024/);
    });

    it('deve formatar data invalida como string vazia', () => {
      expect(formatDate('data-invalida')).toBe('');
    });
  });

  describe('formatSituacaoProcesso', () => {
    it('deve retornar string vazia para valor nulo/indefinido', () => {
      expect(formatSituacaoProcesso(null)).toBe('');
      expect(formatSituacaoProcesso(undefined)).toBe('');
    });

    it('deve retornar a label correta', () => {
      expect(formatSituacaoProcesso(SituacaoProcesso.CRIADO)).toBe('Criado');
      expect(formatSituacaoProcesso(SituacaoProcesso.EM_ANDAMENTO)).toBe('Em andamento');
      expect(formatSituacaoProcesso(SituacaoProcesso.FINALIZADO)).toBe('Finalizado');
    });

    it('deve retornar o proprio valor se nao houver label', () => {
      expect(formatSituacaoProcesso('OUTRA_SITUACAO')).toBe('OUTRA_SITUACAO');
    });
  });

  describe('formatTipoProcesso', () => {
    it('deve retornar string vazia para valor nulo/indefinido', () => {
      expect(formatTipoProcesso(null)).toBe('');
      expect(formatTipoProcesso(undefined)).toBe('');
    });

    it('deve retornar a label correta', () => {
      expect(formatTipoProcesso(TipoProcesso.MAPEAMENTO)).toBe('Mapeamento');
      expect(formatTipoProcesso(TipoProcesso.REVISAO)).toBe('Revisão');
      expect(formatTipoProcesso(TipoProcesso.DIAGNOSTICO)).toBe('Diagnóstico');
    });

    it('deve retornar o proprio valor se nao houver label', () => {
      expect(formatTipoProcesso('OUTRO_TIPO')).toBe('OUTRO_TIPO');
    });
  });

  describe('formatSituacaoSubprocesso', () => {
    it('deve retornar string vazia para valor nulo/indefinido', () => {
      expect(formatSituacaoSubprocesso(null)).toBe('');
      expect(formatSituacaoSubprocesso(undefined)).toBe('');
    });

    it('deve retornar a label correta', () => {
      expect(formatSituacaoSubprocesso(SituacaoSubprocesso.NAO_INICIADO)).toBe('Não iniciado');
      expect(formatSituacaoSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)).toBe('Cadastro em andamento');
      expect(formatSituacaoSubprocesso(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO)).toBe('Mapa homologado');
    });

    it('deve retornar o proprio valor se nao houver label', () => {
      expect(formatSituacaoSubprocesso('OUTRA_SITUACAO_SUB')).toBe('OUTRA_SITUACAO_SUB');
    });
  });

  describe('formatNumeroCsvBR', () => {
    it('deve retornar string vazia para valor nulo/indefinido', () => {
      expect(formatNumeroCsvBR(null)).toBe('');
      expect(formatNumeroCsvBR(undefined)).toBe('');
    });

    it('deve formatar numero com a quantidade de casas decimais esperada', () => {
      expect(formatNumeroCsvBR(1000.5)).toBe('1.000,5');
      expect(formatNumeroCsvBR(1000.555, 2)).toBe('1.000,56'); // pt-BR rounding pode variar, mas testando o funcionamento padrao
    });
  });
});
