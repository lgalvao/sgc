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

vi.mock('@/services/ServidoresService', () => ({
    ServidoresService: {
        buscarTodosServidores: vi.fn(() => Promise.resolve({ data: mockServidores }))
    }
}));

describe('useServidoresStore', () => {
    let servidoresStore: ReturnType<typeof useServidoresStore>;

    beforeEach(async () => {
        initPinia();
        servidoresStore = useServidoresStore();
        vi.clearAllMocks();
        await servidoresStore.fetchServidores();
    });

    it('should initialize with mock servidores', () => {
        expect(servidoresStore.servidores.length).toBe(2);
        expect(servidoresStore.servidores[0].codigo).toBe(1);
        expect(ServidoresService.buscarTodosServidores).toHaveBeenCalledTimes(1);
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
