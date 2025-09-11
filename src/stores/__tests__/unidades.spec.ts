import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useUnidadesStore} from '../unidades';

describe('useUnidadesStore', () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
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
    });
});