import {config} from "@vue/test-utils";
import {createBootstrap} from "bootstrap-vue-next";
import {vi} from "vitest";

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


