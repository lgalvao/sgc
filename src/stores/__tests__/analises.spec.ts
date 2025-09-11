import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useAnalisesStore} from '../analises';
import {ResultadoAnalise} from '@/types/tipos';

describe('useAnalisesStore', () => {
    let analisesStore: ReturnType<typeof useAnalisesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        analisesStore = useAnalisesStore();
    });

    it('should initialize with mock analises', () => {
        expect(analisesStore.analises.length).toBeGreaterThan(0);
        expect(analisesStore.analises[0].dataHora).toBeInstanceOf(Date);
    });

    describe('getters', () => {
        it('getAnalisesPorSubprocesso should filter and sort analyses by idSubprocesso', () => {
            const analises = analisesStore.getAnalisesPorSubprocesso(1);
            expect(Array.isArray(analises)).toBe(true);
            analises.forEach(analise => {
                expect(analise.idSubprocesso).toBe(1);
            });
        });

        it('getAnalisesPorSubprocesso should return empty array for non-existent idSubprocesso', () => {
            const analises = analisesStore.getAnalisesPorSubprocesso(999);
            expect(analises).toEqual([]);
        });
    });

    describe('actions', () => {
        it('registrarAnalise should add new analysis', () => {
            const initialLength = analisesStore.analises.length;
            const novaAnalise = analisesStore.registrarAnalise({
                idSubprocesso: 1,
                dataHora: new Date(),
                unidade: 'TEST',
                resultado: ResultadoAnalise.ACEITE,
                observacao: 'Teste'
            });

            expect(analisesStore.analises.length).toBe(initialLength + 1);
            expect(novaAnalise.id).toBeDefined();
            expect(novaAnalise.unidade).toBe('TEST');
        });

        it('removerAnalisesPorSubprocesso should remove analyses by idSubprocesso', () => {
            analisesStore.registrarAnalise({
                idSubprocesso: 999,
                dataHora: new Date(),
                unidade: 'TEST',
                resultado: ResultadoAnalise.ACEITE
            });

            const initialLength = analisesStore.analises.length;
            analisesStore.removerAnalisesPorSubprocesso(999);
            
            expect(analisesStore.analises.length).toBe(initialLength - 1);
            expect(analisesStore.getAnalisesPorSubprocesso(999)).toEqual([]);
        });
    });
});