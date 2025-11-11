import { describe, it, expect } from 'vitest';
import { buscarTodasUnidades } from './unidadesService';

describe('Unidades Service Integration', () => {
    it('should return a list of units', async () => {
        const unidades = await buscarTodasUnidades();
        expect(unidades).toBeInstanceOf(Array);
        if (unidades.length > 0) {
            expect(unidades[0]).toHaveProperty('codigo');
            expect(unidades[0]).toHaveProperty('nome');
            expect(unidades[0]).toHaveProperty('sigla');
        }
    });
});
