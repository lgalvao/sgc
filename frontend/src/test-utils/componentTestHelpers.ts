import { mount, type VueWrapper, RouterLinkStub } from "@vue/test-utils";
import { createTestingPinia } from "@pinia/testing";
import { vi, afterEach } from "vitest";

export interface ComponentTestContext {
    wrapper?: VueWrapper<any>;
}

/**
 * Utilitário para limpar o wrapper após cada teste
 */
export function setupComponentTest(): ComponentTestContext {
    const context: ComponentTestContext = { wrapper: undefined };

    afterEach(() => {
        if (context.wrapper) {
            context.wrapper.unmount();
        }
    });

    return context;
}

/**
 * Retorna opções comuns de montagem com Pinia e Router
 */
export function getCommonMountOptions(initialState = {}, additionalStubs = {}, piniaOptions = {}) {
    return {
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState,
                    ...piniaOptions,
                }),
            ],
            stubs: {
                RouterLink: RouterLinkStub,
                RouterView: true,
                ...additionalStubs,
            },
            components: {} as Record<string, any>,
        },
    };
}
