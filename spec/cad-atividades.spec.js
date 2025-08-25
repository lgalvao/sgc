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
var auth_1 = require("./utils/auth");
function adicionarAtividade(page, nomeAtividade) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, page.getByTestId('input-nova-atividade').fill(nomeAtividade)];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, page.getByTestId('btn-adicionar-atividade').click()];
                case 2:
                    _a.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(nomeAtividade)).toBeVisible()];
                case 3:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
function adicionarConhecimento(page, atividadeCard, nomeConhecimento) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, atividadeCard.locator('[data-testid="input-novo-conhecimento"]').fill(nomeConhecimento)];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, atividadeCard.locator('[data-testid="btn-adicionar-conhecimento"]').click()];
                case 2:
                    _a.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(nomeConhecimento)).toBeVisible()];
                case 3:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
test_1.test.describe('Cadastro de Atividades e Conhecimentos', function () {
    test_1.test.beforeEach(function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    _c.sent();
                    // Navegar para a página de cadastro de atividades (novo padrão: /processo/:idProcesso/:siglaUnidade/cadastro)
                    return [4 /*yield*/, page.goto("/processo/1/STIC/cadastro")];
                case 2:
                    // Navegar para a página de cadastro de atividades (novo padrão: /processo/:idProcesso/:siglaUnidade/cadastro)
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 3:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve exibir o título da página e os campos de entrada', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, (0, test_1.expect)(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible()];
                case 1:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('input-nova-atividade')).toBeVisible()];
                case 2:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('btn-adicionar-atividade')).toBeVisible()];
                case 3:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir adicionar uma nova atividade', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var novaAtividade;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    novaAtividade = "Atividade de Teste ".concat(Date.now());
                    return [4 /*yield*/, adicionarAtividade(page, novaAtividade)];
                case 1:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir editar uma atividade existente', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeOriginal, atividadeCard, btnEditarAtividade, atividadeEditada;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    atividadeOriginal = "Atividade para Editar ".concat(Date.now());
                    return [4 /*yield*/, adicionarAtividade(page, atividadeOriginal)];
                case 1:
                    _c.sent();
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeOriginal });
                    return [4 /*yield*/, atividadeCard.hover()];
                case 2:
                    _c.sent(); // Simula o hover para exibir os botões
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 3:
                    _c.sent(); // Adiciona um pequeno delay
                    btnEditarAtividade = atividadeCard.getByTestId('btn-editar-atividade');
                    return [4 /*yield*/, (0, test_1.expect)(btnEditarAtividade).toBeVisible()];
                case 4:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(btnEditarAtividade).toBeEnabled()];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, btnEditarAtividade.click({ force: true })];
                case 6:
                    _c.sent();
                    atividadeEditada = "Atividade Editada ".concat(Date.now());
                    return [4 /*yield*/, page.getByTestId('input-editar-atividade').fill(atividadeEditada)];
                case 7:
                    _c.sent();
                    return [4 /*yield*/, page.getByTestId('btn-salvar-edicao-atividade').click()];
                case 8:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(atividadeEditada)).toBeVisible()];
                case 9:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir remover uma atividade', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeParaRemover, atividadeCard, btnRemoverAtividade;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    atividadeParaRemover = "Atividade para Remover ".concat(Date.now());
                    return [4 /*yield*/, adicionarAtividade(page, atividadeParaRemover)];
                case 1:
                    _c.sent();
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeParaRemover });
                    return [4 /*yield*/, atividadeCard.hover()];
                case 2:
                    _c.sent(); // Simula o hover para exibir os botões
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 3:
                    _c.sent(); // Adiciona um pequeno delay
                    btnRemoverAtividade = atividadeCard.getByTestId('btn-remover-atividade');
                    return [4 /*yield*/, (0, test_1.expect)(btnRemoverAtividade).toBeVisible()];
                case 4:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(btnRemoverAtividade).toBeEnabled()];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, btnRemoverAtividade.click({ force: true })];
                case 6:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(atividadeParaRemover)).not.toBeAttached()];
                case 7:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir adicionar um novo conhecimento a uma atividade', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeComConhecimento, novoConhecimento, atividadeCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    atividadeComConhecimento = "Atividade com Conhecimento ".concat(Date.now());
                    return [4 /*yield*/, adicionarAtividade(page, atividadeComConhecimento)];
                case 1:
                    _c.sent();
                    novoConhecimento = "Conhecimento de Teste ".concat(Date.now());
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeComConhecimento });
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, novoConhecimento)];
                case 2:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir editar um conhecimento existente', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeParaEditarConhecimento, conhecimentoOriginal, atividadeCard, conhecimentoRow, btnEditarConhecimento, conhecimentoEditado;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    atividadeParaEditarConhecimento = "Atividade para Editar Conhecimento ".concat(Date.now());
                    return [4 /*yield*/, adicionarAtividade(page, atividadeParaEditarConhecimento)];
                case 1:
                    _c.sent();
                    conhecimentoOriginal = "Conhecimento Original ".concat(Date.now());
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeParaEditarConhecimento });
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, conhecimentoOriginal)];
                case 2:
                    _c.sent();
                    conhecimentoRow = atividadeCard.locator('.group-conhecimento', { hasText: conhecimentoOriginal });
                    return [4 /*yield*/, conhecimentoRow.hover()];
                case 3:
                    _c.sent();
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 4:
                    _c.sent(); // Adiciona um pequeno delay
                    btnEditarConhecimento = conhecimentoRow.getByTestId('btn-editar-conhecimento');
                    return [4 /*yield*/, (0, test_1.expect)(btnEditarConhecimento).toBeVisible()];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(btnEditarConhecimento).toBeEnabled()];
                case 6:
                    _c.sent();
                    return [4 /*yield*/, btnEditarConhecimento.click()];
                case 7:
                    _c.sent();
                    conhecimentoEditado = "Conhecimento Editado ".concat(Date.now());
                    return [4 /*yield*/, page.getByTestId('input-editar-conhecimento').fill(conhecimentoEditado)];
                case 8:
                    _c.sent();
                    return [4 /*yield*/, page.getByTestId('btn-salvar-edicao-conhecimento').click()];
                case 9:
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 10:
                    _c.sent(); // Adicionado para sincronização
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(conhecimentoEditado)).toBeVisible()];
                case 11:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir remover um conhecimento', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeParaRemoverConhecimento, conhecimentoParaRemover, atividadeCard, conhecimentoRow, btnRemoverConhecimento;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    atividadeParaRemoverConhecimento = "Atividade para Remover Conhecimento ".concat(Date.now());
                    return [4 /*yield*/, adicionarAtividade(page, atividadeParaRemoverConhecimento)];
                case 1:
                    _c.sent();
                    conhecimentoParaRemover = "Conhecimento para Remover ".concat(Date.now());
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeParaRemoverConhecimento });
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, conhecimentoParaRemover)];
                case 2:
                    _c.sent();
                    conhecimentoRow = atividadeCard.locator('.group-conhecimento', { hasText: conhecimentoParaRemover });
                    return [4 /*yield*/, conhecimentoRow.hover()];
                case 3:
                    _c.sent();
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 4:
                    _c.sent(); // Adiciona um pequeno delay
                    btnRemoverConhecimento = conhecimentoRow.getByTestId('btn-remover-conhecimento');
                    return [4 /*yield*/, (0, test_1.expect)(btnRemoverConhecimento).toBeVisible()];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(btnRemoverConhecimento).toBeEnabled()];
                case 6:
                    _c.sent();
                    return [4 /*yield*/, btnRemoverConhecimento.click()];
                case 7:
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 8:
                    _c.sent(); // Adicionado para sincronização
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(conhecimentoParaRemover)).not.toBeAttached()];
                case 9:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
});
