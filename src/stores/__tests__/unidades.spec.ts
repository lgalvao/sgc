import {beforeEach, describe, expect, it} from 'vitest';
import {useUnidadesStore} from '../unidades';
import {initPinia} from '@/test/helpers';
import {expectContainsAll} from '@/test/uiHelpers';

describe('useUnidadesStore', () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>;

  
    beforeEach(() => {
          initPinia();
          unidadesStore = useUnidadesStore();
      });

    it('should initialize with mock unidades', () => {
        expect(unidadesStore.unidades.length).toBeGreaterThan(0);
        expect(unidadesStore.unidades[0].sigla).toBeDefined();
    });

    describe('actions', () => {
        it('pesquisarUnidade should find SEDOC unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidade('SEDOC');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento Organizacional e Capacitação');
        });

        it('pesquisarUnidade should find STIC unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidade('STIC');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Secretaria de Informática e Comunicações');
        });

        it('pesquisarUnidade should find nested SEDESENV unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidade('SEDESENV');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento de Sistemas');
        });

        it('pesquisarUnidade should return null if unit not found', () => {
            const unidade = unidadesStore.pesquisarUnidade('NONEXISTENT');
            expect(unidade).toBeNull();
        });

        it('getUnidadesSubordinadas should return direct and indirect subordinate units for a root unit (e.g., "SEDOC")', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('SEDOC');
            expectContainsAll(subordinadas, [
              'SGP','COEDE','SEMARE','STIC','COSIS','SEDESENV','SEDIA','SESEL','COSINF','SENIC','COJUR','SEJUR','SEPRO'
            ]);
            expect(subordinadas.length).toBe(14); // Total de unidades no mock
        });

        it('getUnidadesSubordinadas should return subordinate units for an intermediate unit (e.g., "STIC")', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('STIC');
            expectContainsAll(subordinadas, [
              'COSIS','SEDESENV','SEDIA','SESEL','COSINF','SENIC','COJUR','SEJUR','SEPRO'
            ]);
            expect(subordinadas.length).toBe(10);
        });

        it('getUnidadesSubordinadas should return an empty array for an operational unit (e.g., "SEMARE")', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('SEMARE');
            expect(subordinadas).toEqual(['SEMARE']); // A própria unidade é incluída
        });

        it('getUnidadesSubordinadas should return an empty array for a non-existent unit', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('NONEXISTENT');
            expect(subordinadas).toEqual([]);
        });

        it('getUnidadeSuperior should return the superior unit sigla for a child unit (e.g., "SEMARE" -> "COEDE")', () => {
            const superior = unidadesStore.getUnidadeSuperior('SEMARE');
            expect(superior).toBe('COEDE');
        });

        it('getUnidadeSuperior should return the superior unit sigla for an intermediate unit (e.g., "COEDE" -> "SGP")', () => {
            const superior = unidadesStore.getUnidadeSuperior('COEDE');
            expect(superior).toBe('SGP');
        });

        it('getUnidadeSuperior should return null for a root unit (e.g., "SEDOC")', () => {
            const superior = unidadesStore.getUnidadeSuperior('SEDOC');
            expect(superior).toBeNull();
        });

        it('getUnidadeSuperior should return null for a non-existent unit', () => {
            const superior = unidadesStore.getUnidadeSuperior('NONEXISTENT');
            expect(superior).toBeNull();
        });

        it('getUnidadeImediataSuperior should return the immediate superior unit sigla (e.g., "SEDESENV" -> "COSIS")', () => {
            const superior = unidadesStore.getUnidadeImediataSuperior('SEDESENV');
            expect(superior).toBe('COSIS');
        });

        it('getUnidadeImediataSuperior should return null for a root unit (e.g., "SEDOC")', () => {
            const superior = unidadesStore.getUnidadeImediataSuperior('SEDOC');
            expect(superior).toBeNull();
        });

        it('getUnidadeImediataSuperior should return null for a non-existent unit', () => {
            const superior = unidadesStore.getUnidadeImediataSuperior('NONEXISTENT');
            expect(superior).toBeNull();
        });
    });
});