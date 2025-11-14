import {vi} from 'vitest';
import {config} from '@vue/test-utils';

vi.stubGlobal('vi', vi);

// Configure global stubs for Vue Test Utils
config.global.stubs = {
    RouterLink: {
        template: '<a><slot /></a>'
    },
    RouterView: {
        template: '<div><slot /></div>'
    }
};

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
