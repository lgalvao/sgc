import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { usePerfilStore } from '../perfil';

// Mock localStorage globally
const mockLocalStorage = (() => {
  let store: { [key: string]: string } = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
    removeItem: vi.fn((key: string) => { delete store[key]; }),
    clear: vi.fn(() => { store = {}; })
  };
})();

Object.defineProperty(window, 'localStorage', { value: mockLocalStorage });

describe('usePerfilStore', () => {
  let perfilStore: ReturnType<typeof usePerfilStore>;

  beforeEach(() => {
    // Clear the mock localStorage before each test
    mockLocalStorage.clear();
    mockLocalStorage.setItem('servidorId', '9'); // Set default for initial state

    setActivePinia(createPinia());
    perfilStore = usePerfilStore();
    // Reset the store state to its initial values based on mocked localStorage
    perfilStore.$reset();
  });

  it('should initialize with default values if localStorage is empty', () => {
    expect(perfilStore.servidorId).toBe(9);
    expect(perfilStore.perfilSelecionado).toBeNull();
    expect(perfilStore.unidadeSelecionada).toBeNull();
  });

  it('should initialize with values from localStorage if available', () => {
    mockLocalStorage.setItem('servidorId', '10');
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
    it('setServidorId should update servidorId and store it in localStorage', () => {
      perfilStore.setServidorId(15);
      expect(perfilStore.servidorId).toBe(15);
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('servidorId', '15');
    });

    it('setPerfilUnidade should update perfilSelecionado and unidadeSelecionada and store them in localStorage', () => {
      perfilStore.setPerfilUnidade('ADMIN', 'ABC');
      expect(perfilStore.perfilSelecionado).toBe('ADMIN');
      expect(perfilStore.unidadeSelecionada).toBe('ABC');
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('perfilSelecionado', 'ADMIN');
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('unidadeSelecionada', 'ABC');
    });
  });
});
