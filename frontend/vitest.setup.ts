import {config, RouterLinkStub} from "@vue/test-utils";
import {createBootstrap} from "bootstrap-vue-next";
import {vi} from "vitest";
import apiClient from "@/axios-setup";

HTMLCanvasElement.prototype.getContext = vi.fn();

apiClient.defaults.adapter = async (config) => {
    const metodo = config.method?.toUpperCase() ?? "GET";
    const url = config.url ?? "(url-desconhecida)";
    throw new Error(`Requisicao HTTP nao mockada em teste: ${metodo} ${url}`);
};

const xmlHttpRequestOpenOriginal = XMLHttpRequest.prototype.open;
const xmlHttpRequestSendOriginal = XMLHttpRequest.prototype.send;

Object.defineProperty(XMLHttpRequest.prototype, "open", {
    configurable: true,
    writable: true,
    value: function (
        this: XMLHttpRequest & {_sgcMetodo?: string; _sgcUrl?: string},
        method: string,
        url: string | URL,
        ...args: unknown[]
    ) {
        this._sgcMetodo = method;
        this._sgcUrl = String(url);
        return xmlHttpRequestOpenOriginal.call(this, method, url, ...args as []);
    },
});

Object.defineProperty(XMLHttpRequest.prototype, "send", {
    configurable: true,
    writable: true,
    value: function (
        this: XMLHttpRequest & {_sgcMetodo?: string; _sgcUrl?: string},
        ...args: unknown[]
    ) {
        const metodo = this._sgcMetodo ?? "GET";
        const url = this._sgcUrl ?? "(url-desconhecida)";
        throw new Error(`XMLHttpRequest nao mockado em teste: ${metodo} ${url}`);
        return xmlHttpRequestSendOriginal.call(this, ...args as []);
    },
});


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


const locationMock = {
    href: "http://localhost/",
    assign: vi.fn(),
    replace: vi.fn(),
    reload: vi.fn(),
};

Object.defineProperty(globalThis, "location", {
    value: locationMock,
});

