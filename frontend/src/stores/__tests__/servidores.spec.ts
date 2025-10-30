import {beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useServidoresStore} from '../servidores';
import {ServidoresService} from "@/services/servidoresService";
import type {Servidor} from "@/types/tipos";

const mockServidores: Servidor[] = [
    {
        "codigo": 1,
        "nome": "Ana Paula Souza",
        "unidade": { "codigo": 1, "nome": "Seção de Seleção", "sigla": "SESEL" },
        "email": "ana.souza@tre-pe.jus.br",
        "ramal": "1234",
        "tituloEleitoral": "123456789"
    },
    {
        "codigo": 2,
        "nome": "Carlos Henrique Lima",
        "unidade": { "codigo": 2, "nome": "Seção de Gestão de Pessoas", "sigla": "SGP" },
        "email": "carlos.lima@tre-pe.jus.br",
        "ramal": "2345",
        "tituloEleitoral": "987654321"
    }
];

vi.mock('@/services/servidoresService', () => ({
    ServidoresService: {
        buscarTodosServidores: vi.fn(() => Promise.resolve({ data: mockServidores }))
    }
}));

describe('useServidoresStore', () => {
    let servidoresStore: ReturnType<typeof useServidoresStore>;

    beforeEach(() => {
        initPinia();
        servidoresStore = useServidoresStore();
        servidoresStore.servidores = mockServidores;
        vi.clearAllMocks();
    });

    it('should initialize with mock servidores', () => {
        expect(servidoresStore.servidores.length).toBe(2);
        expect(servidoresStore.servidores[0].codigo).toBe(1);
    });

    describe('actions', () => {
        it('fetchServidores should fetch and set servidores', async () => {
            servidoresStore.servidores = [];
            await servidoresStore.fetchServidores();
            expect(ServidoresService.buscarTodosServidores).toHaveBeenCalledTimes(1);
            expect(servidoresStore.servidores.length).toBe(2);
        });

        it('fetchServidores should handle errors', async () => {
            (ServidoresService.buscarTodosServidores as any).mockRejectedValue(new Error('Failed'));
            await servidoresStore.fetchServidores();
            expect(servidoresStore.error).toContain('Falha ao carregar servidores');
        });
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
