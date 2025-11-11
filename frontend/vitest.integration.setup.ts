import axios from 'axios';
import { vi, beforeEach, beforeAll } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import waitOn from 'wait-on';

// Set the base URL for all API calls
axios.defaults.baseURL = 'http://localhost:10000/api';

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

Object.defineProperty(global, 'localStorage', {
    value: localStorageMock,
});


beforeAll(async () => {
    console.log('Waiting for backend to become available...');
    await waitOn({ resources: ['http://localhost:10000/actuator/health'], timeout: 30000 });
});

beforeEach(() => {
    setActivePinia(createPinia());
});

vi.stubGlobal('vi', vi);