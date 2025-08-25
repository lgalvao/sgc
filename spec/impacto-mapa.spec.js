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
function editarAtividade(page, atividadeOriginal, atividadeEditada) {
    return __awaiter(this, void 0, void 0, function () {
        var atividadeCard;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeOriginal });
                    return [4 /*yield*/, atividadeCard.hover()];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 2:
                    _a.sent();
                    return [4 /*yield*/, atividadeCard.getByTestId('btn-editar-atividade').click({ force: true })];
                case 3:
                    _a.sent();
                    return [4 /*yield*/, page.getByTestId('input-editar-atividade').fill(atividadeEditada)];
                case 4:
                    _a.sent();
                    return [4 /*yield*/, page.getByTestId('btn-salvar-edicao-atividade').click()];
                case 5:
                    _a.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(atividadeEditada)).toBeVisible()];
                case 6:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
function removerAtividade(page, atividadeParaRemover) {
    return __awaiter(this, void 0, void 0, function () {
        var atividadeCard;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeParaRemover });
                    return [4 /*yield*/, atividadeCard.hover()];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 2:
                    _a.sent();
                    return [4 /*yield*/, atividadeCard.getByTestId('btn-remover-atividade').click({ force: true })];
                case 3:
                    _a.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(atividadeParaRemover)).not.toBeAttached()];
                case 4:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
function editarConhecimento(page, atividadeNome, conhecimentoOriginal, conhecimentoEditado) {
    return __awaiter(this, void 0, void 0, function () {
        var atividadeCard, conhecimentoRow;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeNome });
                    conhecimentoRow = atividadeCard.locator('.group-conhecimento', { hasText: conhecimentoOriginal });
                    return [4 /*yield*/, conhecimentoRow.hover()];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 2:
                    _a.sent();
                    return [4 /*yield*/, conhecimentoRow.getByTestId('btn-editar-conhecimento').click()];
                case 3:
                    _a.sent();
                    return [4 /*yield*/, page.getByTestId('input-editar-conhecimento').fill(conhecimentoEditado)];
                case 4:
                    _a.sent();
                    return [4 /*yield*/, page.getByTestId('btn-salvar-edicao-conhecimento').click()];
                case 5:
                    _a.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(conhecimentoEditado)).toBeVisible()];
                case 6:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
function removerConhecimento(page, atividadeNome, conhecimentoParaRemover) {
    return __awaiter(this, void 0, void 0, function () {
        var atividadeCard, conhecimentoRow;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeNome });
                    conhecimentoRow = atividadeCard.locator('.group-conhecimento', { hasText: conhecimentoParaRemover });
                    return [4 /*yield*/, conhecimentoRow.hover()];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, page.waitForTimeout(100)];
                case 2:
                    _a.sent();
                    return [4 /*yield*/, conhecimentoRow.getByTestId('btn-remover-conhecimento').click()];
                case 3:
                    _a.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText(conhecimentoParaRemover)).not.toBeAttached()];
                case 4:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
test_1.test.describe('Impacto no Mapa de Competências', function () {
    test_1.test.beforeEach(function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve exibir tela vazia quando não há mudanças', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, page.goto("/processo/1/SESEL/cadastro")];
                case 1:
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _c.sent();
                    // Abrir modal de impacto sem fazer mudanças
                    return [4 /*yield*/, page.getByText('Impacto no mapa').click()];
                case 3:
                    // Abrir modal de impacto sem fazer mudanças
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Impacto no Mapa de Competências')).toBeVisible()];
                case 4:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas')).not.toBeVisible()];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('msg-nenhuma-competencia')).toBeVisible()];
                case 6:
                    _c.sent();
                    // Fechar modal
                    return [4 /*yield*/, page.getByText('Fechar').click()];
                case 7:
                    // Fechar modal
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve exibir apenas atividades inseridas quando não há competências', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, page.goto("/processo/1/STIC/cadastro")];
                case 1:
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _c.sent();
                    // Adicionar apenas uma atividade
                    return [4 /*yield*/, adicionarAtividade(page, 'Atividade Teste')];
                case 3:
                    // Adicionar apenas uma atividade
                    _c.sent();
                    return [4 /*yield*/, page.getByText('Impacto no mapa').click()];
                case 4:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('titulo-atividades-inseridas')).toBeVisible()];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Atividade Teste')).toBeVisible()];
                case 6:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('msg-nenhuma-competencia')).toBeVisible()];
                case 7:
                    _c.sent();
                    // Fechar modal
                    return [4 /*yield*/, page.getByText('Fechar').click()];
                case 8:
                    // Fechar modal
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve exibir conhecimentos de atividades inseridas', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, page.goto("/processo/1/STIC/cadastro")];
                case 1:
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _c.sent();
                    // Criar atividade com conhecimentos
                    return [4 /*yield*/, adicionarAtividade(page, 'Atividade Nova')];
                case 3:
                    // Criar atividade com conhecimentos
                    _c.sent();
                    atividadeCard = page.locator('.atividade-card', { hasText: 'Atividade Nova' });
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, 'Conhecimento A')];
                case 4:
                    _c.sent();
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, 'Conhecimento B')];
                case 5:
                    _c.sent();
                    return [4 /*yield*/, page.getByText('Impacto no mapa').click()];
                case 6:
                    _c.sent();
                    // Verificar se a atividade e seus conhecimentos aparecem
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('titulo-atividades-inseridas')).toBeVisible()];
                case 7:
                    // Verificar se a atividade e seus conhecimentos aparecem
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Atividade Nova')).toBeVisible()];
                case 8:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByTestId('label-conhecimentos-adicionados')).toBeVisible()];
                case 9:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento A')).toBeVisible()];
                case 10:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento B')).toBeVisible()];
                case 11:
                    _c.sent();
                    // Fechar modal
                    return [4 /*yield*/, page.getByText('Fechar').click()];
                case 12:
                    // Fechar modal
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve exibir todas as mudanças na tela de impacto', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var atividadeExistente, conhecimentoExistente, atividadeParaRemover, atividadeCard, conhecimentoAlterado, conhecimentoParaRemover, novoConhecimento, atividadeAlterada, atividadeXCard, hasCompetenciasImpactadas;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: return [4 /*yield*/, page.goto("/processo/1/SESEL/cadastro")];
                case 1:
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _c.sent();
                    atividadeExistente = "Atividade Existente ".concat(Date.now());
                    conhecimentoExistente = "Conhecimento Existente ".concat(Date.now());
                    atividadeParaRemover = "Atividade Para Remover ".concat(Date.now());
                    // Criar atividades e conhecimentos iniciais
                    return [4 /*yield*/, adicionarAtividade(page, atividadeExistente)];
                case 3:
                    // Criar atividades e conhecimentos iniciais
                    _c.sent();
                    atividadeCard = page.locator('.atividade-card', { hasText: atividadeExistente });
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, conhecimentoExistente)];
                case 4:
                    _c.sent();
                    return [4 /*yield*/, adicionarAtividade(page, atividadeParaRemover)];
                case 5:
                    _c.sent();
                    conhecimentoAlterado = "Conhecimento Alterado ".concat(Date.now());
                    return [4 /*yield*/, editarConhecimento(page, atividadeExistente, conhecimentoExistente, conhecimentoAlterado)];
                case 6:
                    _c.sent();
                    conhecimentoParaRemover = "Conhecimento Para Remover ".concat(Date.now());
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, conhecimentoParaRemover)];
                case 7:
                    _c.sent();
                    return [4 /*yield*/, removerConhecimento(page, atividadeExistente, conhecimentoParaRemover)];
                case 8:
                    _c.sent();
                    novoConhecimento = "Novo Conhecimento ".concat(Date.now());
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeCard, novoConhecimento)];
                case 9:
                    _c.sent();
                    atividadeAlterada = "Atividade Alterada ".concat(Date.now());
                    return [4 /*yield*/, editarAtividade(page, atividadeExistente, atividadeAlterada)];
                case 10:
                    _c.sent();
                    // 5. Remover uma atividade
                    return [4 /*yield*/, removerAtividade(page, atividadeParaRemover)];
                case 11:
                    // 5. Remover uma atividade
                    _c.sent();
                    // 6. Criar nova atividade 'Atividade X'
                    return [4 /*yield*/, adicionarAtividade(page, 'Atividade X')];
                case 12:
                    // 6. Criar nova atividade 'Atividade X'
                    _c.sent();
                    atividadeXCard = page.locator('.atividade-card', { hasText: 'Atividade X' });
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeXCard, 'Conhecimento A de X')];
                case 13:
                    _c.sent();
                    return [4 /*yield*/, adicionarConhecimento(page, atividadeXCard, 'Conhecimento B de X')];
                case 14:
                    _c.sent();
                    // 8. Clicar em 'Impacto no mapa'
                    return [4 /*yield*/, page.getByText('Impacto no mapa').click()];
                case 15:
                    // 8. Clicar em 'Impacto no mapa'
                    _c.sent();
                    // 9. Verificar que todas as mudanças aparecem
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Impacto no Mapa de Competências')).toBeVisible()];
                case 16:
                    // 9. Verificar que todas as mudanças aparecem
                    _c.sent();
                    // Verificar seção de atividades inseridas
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('titulo-atividades-inseridas')).toBeVisible()];
                case 17:
                    // Verificar seção de atividades inseridas
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Atividade X')).toBeVisible()];
                case 18:
                    _c.sent();
                    // Verificar seção de competências impactadas
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('titulo-competencias-impactadas')).toBeVisible()];
                case 19:
                    // Verificar seção de competências impactadas
                    _c.sent();
                    // Verificar atividades inseridas e seus conhecimentos
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Atividade X')).toBeVisible()];
                case 20:
                    // Verificar atividades inseridas e seus conhecimentos
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByTestId('label-conhecimentos-adicionados').first()).toBeVisible()];
                case 21:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento A de X')).toBeVisible()];
                case 22:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('secao-atividades-inseridas').getByText('Conhecimento B de X')).toBeVisible()];
                case 23:
                    _c.sent();
                    return [4 /*yield*/, page.getByTestId('msg-nenhuma-competencia').isVisible()];
                case 24:
                    hasCompetenciasImpactadas = _c.sent();
                    if (!!hasCompetenciasImpactadas) return [3 /*break*/, 30];
                    // Verificar mudanças específicas nas competências impactadas
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Conhecimento alterado')).toBeVisible()];
                case 25:
                    // Verificar mudanças específicas nas competências impactadas
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Conhecimento removido')).toBeVisible()];
                case 26:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Conhecimento adicionado')).toBeVisible()];
                case 27:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Atividade alterada')).toBeVisible()];
                case 28:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Atividade removida')).toBeVisible()];
                case 29:
                    _c.sent();
                    _c.label = 30;
                case 30: 
                // Fechar modal
                return [4 /*yield*/, page.getByText('Fechar').click()];
                case 31:
                    // Fechar modal
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
});
