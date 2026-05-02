import {describe, expect, it, vi} from 'vitest';

vi.unmock("@/utils/logger");

import {getLogLevel} from '../logger';

describe('logger', () => {
  describe('getLogLevel', () => {
    it('deve retornar 1 para o modo test', () => {
      expect(getLogLevel('test')).toBe(1);
    });

    it('deve retornar 3 para o modo production', () => {
      expect(getLogLevel('production')).toBe(3);
    });

    it('deve retornar 4 para o modo development ou outros', () => {
      expect(getLogLevel('development')).toBe(4);
      expect(getLogLevel('other')).toBe(4);
    });

    it('deve detectar o modo ambiente quando não informado', () => {
      // Como estamos rodando em VITEST, ele deve retornar 1 (test)
      expect(getLogLevel()).toBe(1);
    });
  });
});
