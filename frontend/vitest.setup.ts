import { config } from "@vue/test-utils";
import { createBootstrap } from "bootstrap-vue-next";
import { vi, expect } from "vitest";
import * as matchers from "vitest-axe/matchers";
import "vitest-axe/extend-expect";

expect.extend(matchers);

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
        constructor() {
        }

        dispose() {
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

Object.defineProperty(window, "localStorage", {
    value: localStorageMock,
});

// Mock window.location
const locationMock = {
    href: "http://localhost/",
    assign: vi.fn(),
    replace: vi.fn(),
    reload: vi.fn(),
};

Object.defineProperty(window, "location", {
    value: locationMock,
});


