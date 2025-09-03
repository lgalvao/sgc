"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
var test_1 = require("@playwright/test");
var auth_1 = require("../utils/auth");
// Constants for repeated strings
var MODAL_DEVOLUCAO_TITLE = 'Devolução';
var MODAL_DEVOLUCAO_BODY = 'Confirma a devolução da validação do mapa para ajustes?';
var MODAL_ACEITE_TITLE = ' Aceitar Mapa de Competências ';
var MODAL_ACEITE_BODY = 'Confirma o aceite da validação do mapa de competências?';
var MODAL_HOMOLOGACAO_TITLE = 'Homologação';
var MODAL_HOMOLOGACAO_BODY = 'Confirma a homologação do mapa de competências?';
var MODAL_SUGESTOES_TITLE = 'Sugestões';
var MSG_DEVOLUCAO_REALIZADA = 'Devolução realizada';
var MSG_ACEITE_REGISTRADO = 'Aceite registrado';
var MSG_HOMOLOGACAO_EFETIVADA = 'Homologação efetivada';
var TEXTO_MAPA_COMPETENCIAS = 'Mapa de competências técnicas';
var MODAL_HISTORICO_TITLE = 'Histórico de Análise';
function navegarParaUnidade(page_1, siglaUnidade_1) {
    return __awaiter(this, arguments, void 0, function (page, siglaUnidade, indiceProcesso) {
        var processoRows, unidadeRow;
        if (indiceProcesso === void 0) { indiceProcesso = 0; }
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    processoRows = page.locator('table tbody tr');
                    return [4 /*yield*/, processoRows.nth(indiceProcesso).click()];
                case 1:
                    _a.sent();
                    // Step 2: Wait for TreeTable to load completely
                    return [4 /*yield*/, page.waitForSelector('[data-testid="tree-table-row"]')];
                case 2:
                    // Step 2: Wait for TreeTable to load completely
                    _a.sent();
                    // Step 3: Expand all nodes to ensure visibility of units
                    return [4 /*yield*/, page.getByTestId('expand-all-btn').click()];
                case 3:
                    // Step 3: Expand all nodes to ensure visibility of units
                    _a.sent();
                    unidadeRow = page.locator('[data-testid="tree-table-row"]').filter({ hasText: siglaUnidade }).first();
                    return [4 /*yield*/, unidadeRow.waitFor({ state: 'visible' })];
                case 4:
                    _a.sent();
                    return [4 /*yield*/, unidadeRow.click()];
                case 5:
                    _a.sent();
                    // Step 5: Click on the "Mapa de competências" card to enter analysis view
                    return [4 /*yield*/, page.locator('[data-testid="mapa-card"]').click()];
                case 6:
                    // Step 5: Click on the "Mapa de competências" card to enter analysis view
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
test_1.test.describe('CDU-20: Analisar validação de mapa de competências', function () {
    test_1.test.describe('como GESTOR', function () {
        test_1.test.beforeEach(function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, (0, auth_1.loginAsGestor)(page)];
                    case 1: return [2 /*return*/, _c.sent()];
                }
            });
        }); });
        (0, test_1.test)('deve mostrar botões de análise', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('historico-analise-btn')).toBeVisible()];
                    case 2:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('devolver-ajustes-btn')).toBeVisible()];
                    case 3:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('registrar-aceite-btn')).toBeVisible()];
                    case 4:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve mostrar botão ver sugestões quando há sugestões', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var verSugestoesButton;
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        verSugestoesButton = page.getByTestId('ver-sugestoes-btn');
                        return [4 /*yield*/, verSugestoesButton.isVisible()];
                    case 2:
                        if (!_c.sent()) return [3 /*break*/, 7];
                        return [4 /*yield*/, verSugestoesButton.click()];
                    case 3:
                        _c.sent();
                        // Validate modal title and content
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-sugestoes-title')).toHaveText(MODAL_SUGESTOES_TITLE)];
                    case 4:
                        // Validate modal title and content
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-sugestoes-body')).toBeVisible()];
                    case 5:
                        _c.sent(); // Assuming body exists
                        return [4 /*yield*/, page.getByTestId('modal-sugestoes-close').click()];
                    case 6:
                        _c.sent();
                        return [3 /*break*/, 9];
                    case 7: 
                    // Edge case: ensure no button when no suggestions
                    return [4 /*yield*/, (0, test_1.expect)(verSugestoesButton).not.toBeVisible()];
                    case 8:
                        // Edge case: ensure no button when no suggestions
                        _c.sent();
                        _c.label = 9;
                    case 9: return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve devolver validação para ajustes', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        // Click devolver button
                        return [4 /*yield*/, page.getByTestId('devolver-ajustes-btn').click()];
                    case 2:
                        // Click devolver button
                        _c.sent();
                        // Validate modal opens with correct content
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-devolucao-title')).toHaveText(MODAL_DEVOLUCAO_TITLE)];
                    case 3:
                        // Validate modal opens with correct content
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-devolucao-body')).toContainText(MODAL_DEVOLUCAO_BODY)];
                    case 4:
                        _c.sent();
                        // Fill observation and confirm
                        return [4 /*yield*/, page.getByTestId('observacao-devolucao-textarea').fill('Observação de teste')];
                    case 5:
                        // Fill observation and confirm
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('modal-devolucao-confirmar').click()];
                    case 6:
                        _c.sent();
                        // Validate success message and redirect
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText(MSG_DEVOLUCAO_REALIZADA)).toBeVisible()];
                    case 7:
                        // Validate success message and redirect
                        _c.sent();
                        return [4 /*yield*/, page.waitForURL('**/painel')];
                    case 8:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve registrar aceite da validação', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SESEL')];
                    case 1:
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('registrar-aceite-btn').click()];
                    case 2:
                        _c.sent();
                        // Validate modal content
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_ACEITE_TITLE)];
                    case 3:
                        // Validate modal content
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-body')).toContainText(MODAL_ACEITE_BODY)];
                    case 4:
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('modal-aceite-confirmar').click()];
                    case 5:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText(MSG_ACEITE_REGISTRADO)).toBeVisible()];
                    case 6:
                        _c.sent();
                        return [4 /*yield*/, page.waitForURL('**/painel')];
                    case 7:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve cancelar registro de aceite da validação', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SESEL')];
                    case 1:
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('registrar-aceite-btn').click()];
                    case 2:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_ACEITE_TITLE)];
                    case 3:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-body')).toContainText(MODAL_ACEITE_BODY)];
                    case 4:
                        _c.sent();
                        // Cancel the action
                        return [4 /*yield*/, page.getByTestId('modal-aceite-cancelar').click()];
                    case 5:
                        // Cancel the action
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText(TEXTO_MAPA_COMPETENCIAS)).toBeVisible()];
                    case 6:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve mostrar histórico de análise da validação', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var tabela;
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        // Click history button
                        return [4 /*yield*/, page.getByTestId('historico-analise-btn').click()];
                    case 2:
                        // Click history button
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-historico-title')).toHaveText(MODAL_HISTORICO_TITLE)];
                    case 3:
                        _c.sent();
                        tabela = page.getByTestId('tabela-historico');
                        return [4 /*yield*/, tabela.isVisible()];
                    case 4:
                        if (!_c.sent()) return [3 /*break*/, 7];
                        return [4 /*yield*/, (0, test_1.expect)(tabela.locator('thead')).toBeVisible()];
                    case 5:
                        _c.sent();
                        // Optionally check for table body if data exists
                        return [4 /*yield*/, (0, test_1.expect)(tabela.locator('tbody')).toBeVisible()];
                    case 6:
                        // Optionally check for table body if data exists
                        _c.sent();
                        _c.label = 7;
                    case 7: return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve cancelar devolução da validação', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        // Open devolver modal
                        return [4 /*yield*/, page.getByTestId('devolver-ajustes-btn').click()];
                    case 2:
                        // Open devolver modal
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-devolucao-title')).toHaveText(MODAL_DEVOLUCAO_TITLE)];
                    case 3:
                        _c.sent();
                        // Fill observation and cancel
                        return [4 /*yield*/, page.getByTestId('observacao-devolucao-textarea').fill('Observação de teste para cancelamento')];
                    case 4:
                        // Fill observation and cancel
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('modal-devolucao-cancelar').click()];
                    case 5:
                        _c.sent();
                        // Ensure back to main view
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText(TEXTO_MAPA_COMPETENCIAS)).toBeVisible()];
                    case 6:
                        // Ensure back to main view
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
    });
    test_1.test.describe('como ADMIN', function () {
        test_1.test.beforeEach(function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, (0, auth_1.loginAsAdmin)(page)];
                    case 1: return [2 /*return*/, _c.sent()];
                }
            });
        }); });
        (0, test_1.test)('deve mostrar botões de análise', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV', 0)];
                    case 1:
                        _c.sent(); // Primeiro processo
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('historico-analise-btn')).toBeVisible()];
                    case 2:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('devolver-ajustes-btn')).toBeVisible()];
                    case 3:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('registrar-aceite-btn')).toBeVisible()];
                    case 4:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve homologar validação', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        // O botão de "Registrar aceite" vira "Homologar" para o ADMIN
                        return [4 /*yield*/, page.getByTestId('registrar-aceite-btn').click()];
                    case 2:
                        // O botão de "Registrar aceite" vira "Homologar" para o ADMIN
                        _c.sent();
                        // Validate modal content for homologation
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_HOMOLOGACAO_TITLE)];
                    case 3:
                        // Validate modal content for homologation
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-body')).toHaveText(MODAL_HOMOLOGACAO_BODY)];
                    case 4:
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('modal-aceite-confirmar').click()];
                    case 5:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText(MSG_HOMOLOGACAO_EFETIVADA)).toBeVisible()];
                    case 6:
                        _c.sent();
                        return [4 /*yield*/, page.waitForURL('**/painel')];
                    case 7:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
        (0, test_1.test)('deve cancelar homologação da validação', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
            var page = _b.page;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, navegarParaUnidade(page, 'SEDESENV')];
                    case 1:
                        _c.sent();
                        return [4 /*yield*/, page.getByTestId('registrar-aceite-btn').click()];
                    case 2:
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_HOMOLOGACAO_TITLE)];
                    case 3:
                        _c.sent();
                        // Cancel the action
                        return [4 /*yield*/, page.getByTestId('modal-aceite-cancelar').click()];
                    case 4:
                        // Cancel the action
                        _c.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText(TEXTO_MAPA_COMPETENCIAS)).toBeVisible()];
                    case 5:
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        }); });
    });
});
