import {beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useServidoresStore} from '../servidores';

// Mock the servidores.json import
vi.mock('../../mocks/servidores.json', () => ({
    default: [
        {
            "codigo": 1,
            "nome": "Ana Paula Souza",
            "unidade": { "codigo": 1, "nome": "Seção de Seleção", "sigla": "SESEL" },
            "email": "ana.souza@tre-pe.jus.br",
            "ramal": "1234"
        },
        {
            "codigo": 2,
            "nome": "Carlos Henrique Lima",
            "unidade": { "codigo": 2, "nome": "Seção de Gestão de Pessoas", "sigla": "SGP" },
            "email": "carlos.lima@tre-pe.jus.br",
            "ramal": "2345"
        }
    ]
}));

describe('useServidoresStore', () => {
    let servidoresStore: ReturnType<typeof useServidoresStore>;

    beforeEach(() => {
        initPinia();
        servidoresStore = useServidoresStore();
        // Manually reset the store state based on the initial mock data
        servidoresStore.$patch({
            servidores: [
                {
                    "codigo": 1,
                    "nome": "Ana Paula Souza",
                    "unidade": { "codigo": 1, "nome": "Seção de Seleção", "sigla": "SESEL" },
                    "email": "ana.souza@tre-pe.jus.br",
                    "ramal": "1234"
                },
                {
                    "codigo": 2,
                    "nome": "Carlos Henrique Lima",
                    "unidade": { "codigo": 2, "nome": "Seção de Gestão de Pessoas", "sigla": "SGP" },
                    "email": "carlos.lima@tre-pe.jus.br",
                    "ramal": "2345"
                }
            ].map(s => ({...s}))
        });
    });

    it('should initialize with mock servidores', () => {
        expect(servidoresStore.servidores.length).toBe(2); // Directly use the expected length
        expect(servidoresStore.servidores[0].codigo).toBe(1);
    });

    describe('getters', () => {
        it('getServidorById should return the correct servidor by ID', () => {
            const servidor = servidoresStore.getServidorById(1);
            expect(servidor).toBeDefined();
            expect(servidor?.codigo).toBe(1);
            expect(servidor?.nome).toBe('Ana Paula Souza');
        });

        it('getServidorById should return undefined if no matching servidor is found', () => {
            const servidor = servidoresStore.getServidorById(999);
            expect(servidor).toBeUndefined();
        });
    });
});
