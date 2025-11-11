import { describe, it, expect } from 'vitest';
import { criarProcesso, submeterProcesso, fetchProcessoDetalhe } from './processoService';
import { TipoProcesso } from '../types/tipos';

describe('Processos Service Integration', () => {
    let processoId: number;

    it.skip('should create a new process and return a valid ID', async () => {
        const processo = {
            tipo: TipoProcesso.MAPEAMENTO,
            unidades: [2],
            descricao: 'Processo de Teste',
            dataLimiteEtapa1: '2025-12-31',
        };
        const response = await criarProcesso(processo);
        expect(typeof response).toBe('number');
        processoId = response;
    });

    it.skip('should submit the process successfully', async () => {
        expect(processoId).toBeDefined();
        await expect(submeterProcesso(processoId)).resolves.not.toThrow();
    });

    it.skip('should fetch the process details and return the correct data', async () => {
        expect(processoId).toBeDefined();
        const detalhes = await fetchProcessoDetalhe(processoId);
        expect(detalhes).toHaveProperty('id', processoId);
        expect(detalhes).toHaveProperty('tipo', TipoProcesso.MAPEAMENTO);
        expect(detalhes).toHaveProperty('situacao');
    });
});
