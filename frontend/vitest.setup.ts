import {config, RouterLinkStub} from "@vue/test-utils";
import {createBootstrap} from "bootstrap-vue-next";
import {vi} from "vitest";

// Mock HTMLCanvasElement.prototype.getContext
HTMLCanvasElement.prototype.getContext = vi.fn();


vi.mock("@/utils/logger", () => ({
    default: {
        error: vi.fn(),
        warn: vi.fn(),
        info: vi.fn(),
        debug: vi.fn(),
        success: vi.fn(),
        trace: vi.fn(),
        log: vi.fn(),
    }
}));

vi.mock("bootstrap", () => ({
    Tooltip: class Tooltip {
        dispose() {
            // Mock dispose method
        }
    },

}));

config.global.stubs["b-modal"] = {
    props: ["modelValue"],
    template: `
    <div v-if="modelValue">
      <slot />
      <slot name="footer" />
    </div>
  `,
};

config.global.stubs["RouterLink"] = RouterLinkStub;
config.global.stubs["router-link"] = RouterLinkStub;

config.global.directives = {
    "b-tooltip": {},
};

config.global.plugins.push(createBootstrap());

// Mock localStorage
const localStorageMock = (function () {
    let store: { [key: string]: string } = {};
    return {
        getItem: function (key: string) {
            return store[key] || null;
        },
        setItem: function (key: string, value: string) {
            store[key] = value.toString();
        },
        removeItem: function (key: string) {
            delete store[key];
        },
        clear: function () {
            store = {};
        },
    };
})();

Object.defineProperty(globalThis, "localStorage", {
    value: localStorageMock,
});


// Mock window.location
const locationMock = {
    href: "http://localhost/",
    assign: vi.fn(),
    replace: vi.fn(),
    reload: vi.fn(),
};

Object.defineProperty(globalThis, "location", {
    value: locationMock,
});

