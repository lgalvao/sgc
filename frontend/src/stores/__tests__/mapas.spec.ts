import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useMapasStore } from '../mapas';
import { useApi } from '@/composables/useApi';

// Mock the useApi composable
vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);

describe('useMapasStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        // Reset mocks before each test
        vi.clearAllMocks();
    });

    describe('fetchMapas action', () => {
        it('should fetch mapas successfully and update the state', async () => {
            const mockMapas = [
                { id: 1, unidade: 'SESEL', idProcesso: 1, situacao: 'em_andamento' },
                { id: 2, unidade: 'COSIS', idProcesso: 2, situacao: 'finalizado' },
            ];
            // Mock the return value of useApi().get
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockMapas }),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useMapasStore();

            expect(store.loading).toBe(false);
            expect(store.mapas).toEqual([]);
            expect(store.error).toBe(null);

            const promise = store.fetchMapas();

            expect(store.loading).toBe(true);

            await promise;

            expect(store.loading).toBe(false);
            expect(store.mapas).toEqual(mockMapas);
            expect(store.error).toBe(null);
            // Ensure the mocked get function was called
            expect(mockedUseApi().get).toHaveBeenCalledWith('/api/mapas');
        });

        it('should handle errors during fetch', async () => {
            const errorMessage = 'Network Error';
            // Mock the rejected value of useApi().get
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockRejectedValue(new Error(errorMessage)),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useMapasStore();

            expect(store.loading).toBe(false);
            expect(store.error).toBe(null);

            await store.fetchMapas();

            expect(store.loading).toBe(false);
            expect(store.error).toContain(errorMessage);
            expect(store.mapas).toEqual([]);
        });
    });

    describe('getters', () => {
        it('should return the correct mapa with getMapaByUnidadeId', () => {
            const store = useMapasStore();
            store.mapas = [
                { id: 1, unidade: 'SESEL', idProcesso: 1, situacao: 'em_andamento' },
                { id: 2, unidade: 'COSIS', idProcesso: 2, situacao: 'finalizado' },
            ] as any;

            const found = store.getMapaByUnidadeId('SESEL', 1);
            expect(found).toBeDefined();
            expect(found?.id).toBe(1);

            const notFound = store.getMapaByUnidadeId('XYZ', 99);
            expect(notFound).toBeUndefined();
        });

        // The logic for this getter is noted as needing review in the store itself.
        // The test will reflect the current implementation.
        it('should return a mapa for a given unit with getMapaVigentePorUnidade', () => {
            const store = useMapasStore();
            store.mapas = [
                { id: 1, unidade: 'SESEL', idProcesso: 1, situacao: 'em_andamento' },
                { id: 2, unidade: 'COSIS', idProcesso: 2, situacao: 'finalizado' },
            ] as any;

            const found = store.getMapaVigentePorUnidade('SESEL');
            expect(found).toBeDefined();
            expect(found?.id).toBe(1);

            const notFound = store.getMapaVigentePorUnidade('XYZ');
            expect(notFound).toBeUndefined();
        });
    });
});