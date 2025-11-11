import axios from 'axios';
import { vi } from 'vitest';

// Set the base URL for all API calls
axios.defaults.baseURL = 'http://localhost:10000/api';

vi.stubGlobal('vi', vi);

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
        },
    };
})();

Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
});