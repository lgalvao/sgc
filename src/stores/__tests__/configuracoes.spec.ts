import {createPinia, setActivePinia} from 'pinia';
import {useConfiguracoesStore} from '../configuracoes';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock localStorage
const localStorageMock = (() => {
    let store: { [key: string]: string } = {};
    return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => {
            store[key] = value.toString();
        },
        clear: () => {
            store = {};
        },
        removeItem: (key: string) => {
            delete store[key];
        }
    };
})();

Object.defineProperty(window, 'localStorage', {value: localStorageMock});

describe('Configuracoes Store', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        localStorageMock.clear(); // Limpa o localStorage antes de cada teste
        // Reset mocks for localStorage methods
        vi.spyOn(localStorageMock, 'getItem').mockRestore();
        vi.spyOn(localStorageMock, 'setItem').mockRestore();
    });

    it('should have default values', () => {
        const store = useConfiguracoesStore();
        expect(store.diasInativacaoProcesso).toBe(10);
        expect(store.diasAlertaNovo).toBe(7);
    });

    it('should load configurations from localStorage', () => {
        localStorageMock.setItem('appConfiguracoes', JSON.stringify({
            diasInativacaoProcesso: 45,
            diasAlertaNovo: 10,
        }));
        const store = useConfiguracoesStore();
        store.loadConfiguracoes();
        expect(store.diasInativacaoProcesso).toBe(45);
        expect(store.diasAlertaNovo).toBe(10);
    });

    it('should save configurations to localStorage', () => {
        const store = useConfiguracoesStore();
        store.setDiasInativacaoProcesso(60);
        store.setDiasAlertaNovo(15);
        store.saveConfiguracoes();

        const savedConfig = JSON.parse(localStorageMock.getItem('appConfiguracoes') || '{}');
        expect(savedConfig.diasInativacaoProcesso).toBe(60);
        expect(savedConfig.diasAlertaNovo).toBe(15);
    });

    it('should not set diasInativacaoProcesso less than 1', () => {
        const store = useConfiguracoesStore();
        const initialValue = store.diasInativacaoProcesso;
        store.setDiasInativacaoProcesso(0);
        expect(store.diasInativacaoProcesso).toBe(initialValue);
        store.setDiasInativacaoProcesso(-5);
        expect(store.diasInativacaoProcesso).toBe(initialValue);
    });

    it('should not set diasAlertaNovo less than 1', () => {
        const store = useConfiguracoesStore();
        const initialValue = store.diasAlertaNovo;
        store.setDiasAlertaNovo(0);
        expect(store.diasAlertaNovo).toBe(initialValue);
        store.setDiasAlertaNovo(-5);
        expect(store.diasAlertaNovo).toBe(initialValue);
    });

    it('should handle localStorage errors gracefully during load', () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {
        });
        vi.spyOn(localStorageMock, 'getItem').mockImplementation(() => {
            throw new Error('localStorage read error');
        });
        const store = useConfiguracoesStore();
        store.loadConfiguracoes();
        // expect(consoleErrorSpy).toHaveBeenCalledWith('Erro ao carregar configurações do localStorage:', expect.any(Error));
        expect(store.diasInativacaoProcesso).toBe(10);
        consoleErrorSpy.mockRestore();
    });

    it('should handle localStorage errors gracefully during save', () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {
        });
        vi.spyOn(localStorageMock, 'setItem').mockImplementation(() => {
            throw new Error('localStorage write error');
        });
        const store = useConfiguracoesStore();
        const result = store.saveConfiguracoes();
        // expect(consoleErrorSpy).toHaveBeenCalledWith('Erro ao salvar configurações no localStorage:', expect.any(Error));
        expect(result).toBe(false);
        consoleErrorSpy.mockRestore();
    });
});