"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var vitest_1 = require("vitest");
// Mock global do sessionStorage
var mockSessionStorage = (function () {
    var store = {};
    return {
        getItem: vitest_1.vi.fn(function (key) { return store[key] || null; }),
        setItem: vitest_1.vi.fn(function (key, value) { store[key] = value.toString(); }),
        removeItem: vitest_1.vi.fn(function (key) { delete store[key]; }),
        clear: vitest_1.vi.fn(function () { store = {}; }),
    };
})();
Object.defineProperty(window, 'sessionStorage', {
    value: mockSessionStorage,
});
