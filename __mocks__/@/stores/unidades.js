"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useUnidadesStore = exports.mockPesquisarUnidade = void 0;
var vitest_1 = require("vitest");
exports.mockPesquisarUnidade = vitest_1.vi.fn(function (sigla) { return ({ sigla: sigla, nome: "Unidade ".concat(sigla) }); });
exports.useUnidadesStore = vitest_1.vi.fn(function () { return ({
    pesquisarUnidade: exports.mockPesquisarUnidade,
}); });
