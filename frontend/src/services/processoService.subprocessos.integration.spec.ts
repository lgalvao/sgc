import { describe, it, expect } from 'vitest';
import { obterDetalhesProcesso } from './processoService';

describe('Processos Service Subprocessos Integration', () => {
    it('should return a list of subprocessos for a given process ID', async () => {
        const processoId = 100;
        const detalhes = await obterDetalhesProcesso(processoId);
        expect(detalhes).toHaveProperty('unidades');
        const subprocessos = detalhes.unidades;
        expect(subprocessos).toBeInstanceOf(Array);
        expect(subprocessos.length).toBe(4);
        if (subprocessos.length > 0) {
            expect(subprocessos[0]).toHaveProperty('codUnidade');
            expect(subprocessos[0]).toHaveProperty('nome');
            expect(subprocessos[0]).toHaveProperty('situacaoSubprocesso');
        }
    });
});
