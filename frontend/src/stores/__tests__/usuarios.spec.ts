import {beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useUsuariosStore} from '../usuarios';
import {UsuariosService} from "@/services/usuariosService";
import type {Usuario} from "@/types/tipos";

const mockUsuarios: Usuario[] = [
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

vi.mock('@/services/usuariosService', () => ({
    UsuariosService: {
        buscarTodosUsuarios: vi.fn(() => Promise.resolve({ data: mockUsuarios }))
    }
}));

describe('useUsuariosStore', () => {
    let usuariosStore: ReturnType<typeof useUsuariosStore>;

    beforeEach(() => {
        initPinia();
        usuariosStore = useUsuariosStore();
        usuariosStore.usuarios = mockUsuarios;
        vi.clearAllMocks();
    });

    it('should initialize with mock usuarios', () => {
        expect(usuariosStore.usuarios.length).toBe(2);
        expect(usuariosStore.usuarios[0].codigo).toBe(1);
    });

    describe('actions', () => {
        it('fetchUsuarios should fetch and set usuarios', async () => {
            usuariosStore.usuarios = [];
            await usuariosStore.fetchUsuarios();
            expect(UsuariosService.buscarTodosUsuarios).toHaveBeenCalledTimes(1);
            expect(usuariosStore.usuarios.length).toBe(2);
        });

        it('fetchUsuarios should handle errors', async () => {
            (UsuariosService.buscarTodosUsuarios as any).mockRejectedValue(new Error('Failed'));
            await usuariosStore.fetchUsuarios();
            expect(usuariosStore.error).toContain('Falha ao carregar usuários');
        });
    });

    describe('getters', () => {
        it('getUsuarioById should return the correct usuario by ID', () => {
            const usuario = usuariosStore.getUsuarioById(1);
            expect(usuario).toBeDefined();
            expect(usuario?.codigo).toBe(1);
            expect(usuario?.nome).toBe('Ana Paula Souza');
        });

        it('getUsuarioById should return undefined if no matching usuario is found', () => {
            const usuario = usuariosStore.getUsuarioById(999);
            expect(usuario).toBeUndefined();
        });
    });
});
