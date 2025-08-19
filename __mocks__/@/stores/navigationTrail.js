"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useNavigationTrail = exports.mockTrailPush = exports.mockTrailEnsureBase = exports.mockTrailPopTo = exports.mockTrailReset = exports.mockTrailCrumbs = void 0;
var vitest_1 = require("vitest");
exports.mockTrailCrumbs = []; // Variável para o estado dos crumbs
exports.mockTrailReset = vitest_1.vi.fn();
exports.mockTrailPopTo = vitest_1.vi.fn();
exports.mockTrailEnsureBase = vitest_1.vi.fn();
exports.mockTrailPush = vitest_1.vi.fn(function (crumb) { return exports.mockTrailCrumbs.push(crumb); });
exports.useNavigationTrail = vitest_1.vi.fn(function () { return ({
    get crumbs() { return exports.mockTrailCrumbs; },
    set crumbs(newCrumbs) { exports.mockTrailCrumbs = newCrumbs; },
    reset: exports.mockTrailReset,
    popTo: exports.mockTrailPopTo,
    ensureBase: exports.mockTrailEnsureBase,
    push: exports.mockTrailPush,
}); });
