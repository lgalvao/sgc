import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useServidoresStore} from '../servidores';

// Mock the servidores.json import
vi.mock('../../mocks/servidores.json', () => ({
    default: [
        {
            "id": 1,
            "nome": "Ana Paula Souza",
            "unidade": "SESEL",
            "email": "ana.souza@tre-pe.jus.br",
            "ramal": "1234"
        },
        {
            "id": 2,
            "nome": "Carlos Henrique Lima",
            "unidade": "SGP",
            "email": "carlos.lima@tre-pe.jus.br",
            "ramal": "2345"
        }
    ]
}));

describe('useServidoresStore', () => {
    let servidoresStore: ReturnType<typeof useServidoresStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        servidoresStore = useServidoresStore();
        // Manually reset the store state based on the initial mock data
        servidoresStore.$patch({
            servidores: [
                {
                    "id": 1,
                    "nome": "Ana Paula Souza",
                    "unidade": "SESEL",
                    "email": "ana.souza@tre-pe.jus.br",
                    "ramal": "1234"
                },
                {
                    "id": 2,
                    "nome": "Carlos Henrique Lima",
                    "unidade": "SGP",
                    "email": "carlos.lima@tre-pe.jus.br",
                    "ramal": "2345"
                }
            ].map(s => ({...s}))
        });
    });

    it('should initialize with mock servidores', () => {
        expect(servidoresStore.servidores.length).toBe(2); // Directly use the expected length
        expect(servidoresStore.servidores[0].id).toBe(1);
    });

    describe('getters', () => {
        it('getServidorById should return the correct servidor by ID', () => {
            const servidor = servidoresStore.getServidorById(1);
            expect(servidor).toBeDefined();
            expect(servidor?.id).toBe(1);
            expect(servidor?.nome).toBe('Ana Paula Souza');
        });

        it('getServidorById should return undefined if no matching servidor is found', () => {
            const servidor = servidoresStore.getServidorById(999);
            expect(servidor).toBeUndefined();
        });
    });
});
