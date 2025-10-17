import {beforeEach, describe, expect, it} from 'vitest';
import {useSubprocessosStore} from '../subprocessos';
import {initPinia} from '@/test-utils/helpers';

describe('useSubprocessosStore', () => {
    let subprocessosStore: ReturnType<typeof useSubprocessosStore>;

  
    beforeEach(() => {
          initPinia();
          subprocessosStore = useSubprocessosStore();
      });

    it('should initialize with mock subprocessos', () => {
        expect(subprocessosStore.subprocessos.length).toBeGreaterThan(0);
        expect(subprocessosStore.subprocessos[0].dataLimiteEtapa1).toBeInstanceOf(Date);
    });

    describe('getters', () => {
        it('getUnidadesDoProcesso should filter subprocessos by idProcesso', () => {
            const subprocessos = subprocessosStore.getUnidadesDoProcesso(1);
            expect(Array.isArray(subprocessos)).toBe(true);
            subprocessos.forEach(sp => {
                expect(sp.idProcesso).toBe(1);
            });
        });

        it('getUnidadesDoProcesso should return empty array for non-existent idProcesso', () => {
            const subprocessos = subprocessosStore.getUnidadesDoProcesso(999);
            expect(subprocessos).toEqual([]);
        });

        it('getMovementsForSubprocesso should return movements for existing subprocesso', () => {
            const movements = subprocessosStore.getMovementsForSubprocesso(1);
            expect(Array.isArray(movements)).toBe(true);
        });

        it('getMovementsForSubprocesso should return empty array for non-existent subprocesso', () => {
            const movements = subprocessosStore.getMovementsForSubprocesso(999);
            expect(movements).toEqual([]);
        });

        it('getSubprocessosElegiveisHomologacaoBloco should filter by idProcesso and situacao', () => {
            const elegiveis = subprocessosStore.getSubprocessosElegiveisHomologacaoBloco(1);
            expect(Array.isArray(elegiveis)).toBe(true);
            elegiveis.forEach(sp => {
                expect(sp.idProcesso).toBe(1);
                expect(['Cadastro disponibilizado', 'Revisão do cadastro disponibilizada']).toContain(sp.situacao);
            });
        });
    });

    describe('actions', () => {
        it('reset should clear all subprocessos', () => {
            subprocessosStore.reset();
            expect(subprocessosStore.subprocessos).toEqual([]);
        });
    });
});