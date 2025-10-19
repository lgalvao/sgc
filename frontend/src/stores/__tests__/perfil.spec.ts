import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {initPinia} from '@/test-utils/helpers';
import {usePerfilStore} from '../perfil';
import {Perfil} from "@/types/tipos";
import * as usuarioService from '@/services/usuarioService';

vi.mock('@/services/usuarioService');

// Mock localStorage globally
const mockLocalStorage = (() => {
    let store: { [key: string]: string } = {};
    return {
        getItem: vi.fn((key: string) => store[key] || null),
        setItem: vi.fn((key: string, value: string) => {
            store[key] = value;
        }),
        removeItem: vi.fn((key: string) => {
            delete store[key];
        }),
        clear: vi.fn(() => {
            store = {};
        })
    };
})();

Object.defineProperty(window, 'localStorage', {value: mockLocalStorage});

describe('usePerfilStore', () => {
    let perfilStore: ReturnType<typeof usePerfilStore>;

    beforeEach(() => {
        // Clear the mock localStorage before each test
        mockLocalStorage.clear();
        mockLocalStorage.setItem('idServidor', '9'); // Set default for initial state

        initPinia();
        perfilStore = usePerfilStore();
        // Reset the store state to its initial values based on mocked localStorage
        perfilStore.$reset();
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('should initialize with default values if localStorage is empty', () => {
        expect(perfilStore.servidorId).toBe(9);
        expect(perfilStore.perfilSelecionado).toBeNull();
        expect(perfilStore.unidadeSelecionada).toBeNull();
    });

    it('should initialize with values from localStorage if available', () => {
        mockLocalStorage.setItem('idServidor', '10');
        mockLocalStorage.setItem('perfilSelecionado', 'USER');
        mockLocalStorage.setItem('unidadeSelecionada', 'XYZ');

        // Create a new Pinia instance and store to pick up new localStorage mocks
        setActivePinia(createPinia()); // Re-activate Pinia with a fresh instance
        const newPerfilStore = usePerfilStore(); // Get a fresh store instance

        expect(newPerfilStore.servidorId).toBe(10);
        expect(newPerfilStore.perfilSelecionado).toBe('USER');
        expect(newPerfilStore.unidadeSelecionada).toBe('XYZ');
    });

    describe('actions', () => {
        const mockUsuarioService = vi.mocked(usuarioService);

        it('setServidorId should update idServidor and store it in localStorage', () => {
            perfilStore.setServidorId(15);
            expect(perfilStore.servidorId).toBe(15);
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith('idServidor', '15');
        });

        it('setPerfilUnidade should update perfilSelecionado and unidadeSelecionada and store them in localStorage', () => {
            perfilStore.setPerfilUnidade(Perfil.ADMIN, 'ABC');
            expect(perfilStore.perfilSelecionado).toBe(Perfil.ADMIN);
            expect(perfilStore.unidadeSelecionada).toBe('ABC');
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith('perfilSelecionado', Perfil.ADMIN);
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith('unidadeSelecionada', 'ABC');
        });

        it('loginCompleto should authenticate, fetch profiles, and auto-select if one profile', async () => {
            const perfilUnidade = { perfil: Perfil.CHEFE, unidade: { codigo: 1, sigla: 'UT', nome: 'Unidade UT' }, siglaUnidade: 'UT' };
            mockUsuarioService.autenticar.mockResolvedValue(true);
            mockUsuarioService.autorizar.mockResolvedValue([perfilUnidade]);
            mockUsuarioService.entrar.mockResolvedValue(undefined);

            const result = await perfilStore.loginCompleto('123', 'pass');

            expect(mockUsuarioService.autenticar).toHaveBeenCalledWith({ tituloEleitoral: 123, senha: 'pass' });
            expect(mockUsuarioService.autorizar).toHaveBeenCalledWith(123);
            expect(mockUsuarioService.entrar).toHaveBeenCalled();
            expect(perfilStore.perfilSelecionado).toBe(Perfil.CHEFE);
            expect(perfilStore.unidadeSelecionada).toBe('UT');
            expect(result).toBe(true);
        });

        it('loginCompleto should not auto-select if multiple profiles', async () => {
            const perfis = [{ perfil: Perfil.CHEFE, unidade: {codigo: 1, nome: 'Unidade 1', sigla: 'U1'}, siglaUnidade: 'U1'}, { perfil: Perfil.GESTOR, unidade: {codigo: 2, nome: 'Unidade 2', sigla: 'U2'}, siglaUnidade: 'U2' }];
            mockUsuarioService.autenticar.mockResolvedValue(true);
            mockUsuarioService.autorizar.mockResolvedValue(perfis);

            await perfilStore.loginCompleto('123', 'pass');

            expect(mockUsuarioService.entrar).not.toHaveBeenCalled();
            expect(perfilStore.perfisUnidades).toEqual(perfis);
        });

        it('selecionarPerfilUnidade should call entrar and set profile', async () => {
            const perfilUnidade = { perfil: Perfil.GESTOR, unidade: { codigo: 2, sigla: 'XYZ', nome: 'Unidade XYZ' }, siglaUnidade: 'XYZ' };
            mockUsuarioService.entrar.mockResolvedValue(undefined);

            await perfilStore.selecionarPerfilUnidade(456, perfilUnidade);

            expect(mockUsuarioService.entrar).toHaveBeenCalledWith({
                tituloEleitoral: 456,
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
            });
            expect(perfilStore.perfilSelecionado).toBe(Perfil.GESTOR);
            expect(perfilStore.unidadeSelecionada).toBe('XYZ');
            expect(perfilStore.servidorId).toBe(456);
        });
    });
});
