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
function getBreadcrumbItemsText(page) {
    return __awaiter(this, void 0, void 0, function () {
        var items, count, texts, i, el, text;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    items = page.locator('[data-testid="breadcrumbs"] [data-testid="breadcrumb-item"]');
                    return [4 /*yield*/, items.count()];
                case 1:
                    count = _a.sent();
                    texts = [];
                    i = 0;
                    _a.label = 2;
                case 2:
                    if (!(i < count)) return [3 /*break*/, 5];
                    el = items.nth(i);
                    return [4 /*yield*/, el.innerText()];
                case 3:
                    text = (_a.sent()).trim();
                    texts.push(text.replace(/\s+/g, ' '));
                    _a.label = 4;
                case 4:
                    i++;
                    return [3 /*break*/, 2];
                case 5: return [2 /*return*/, texts];
            }
        });
    });
}
function lastBreadcrumbHasLink(page) {
    return __awaiter(this, void 0, void 0, function () {
        var lastItem;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    lastItem = page.locator('[data-testid="breadcrumbs"] [data-testid="breadcrumb-item"]').last();
                    return [4 /*yield*/, lastItem.locator('a').count()];
                case 1: return [2 /*return*/, (_a.sent()) > 0];
            }
        });
    });
}
function breadcrumbLinkHrefAt(page, index) {
    return __awaiter(this, void 0, void 0, function () {
        var item, link;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    item = page.locator('[data-testid="breadcrumbs"] [data-testid="breadcrumb-item"]').nth(index);
                    link = item.locator('a').first();
                    return [4 /*yield*/, link.count()];
                case 1:
                    if ((_a.sent()) === 0)
                        return [2 /*return*/, null];
                    return [4 /*yield*/, link.getAttribute('href')];
                case 2: return [2 /*return*/, _a.sent()];
            }
        });
    });
}
// Importante: o servidor é iniciado pelo webServer do playwright (playwright.config.ts)
test_1.test.describe('Breadcrumbs - cobertura de cenários', function () {
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
    (0, test_1.test)('painel não exibe breadcrumbs', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Após login estamos no /painel
                return [4 /*yield*/, (0, test_1.expect)(page.locator('[data-testid="breadcrumbs"]')).toHaveCount(0)];
                case 1:
                    // Após login estamos no /painel
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('navegação via navbar: breadcrumbs oculto na primeira renderização', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Clicar em Relatórios pela navbar (usa flag transitória)
                return [4 /*yield*/, page.getByRole('link', { name: /Relatórios/ }).click()
                    // Deve ocultar breadcrumbs nesta primeira renderização
                ];
                case 1:
                    // Clicar em Relatórios pela navbar (usa flag transitória)
                    _c.sent();
                    // Deve ocultar breadcrumbs nesta primeira renderização
                    return [4 /*yield*/, (0, test_1.expect)(page.locator('[data-testid="breadcrumbs"]')).toHaveCount(0)];
                case 2:
                    // Deve ocultar breadcrumbs nesta primeira renderização
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('processo: mostra (home) > Processo e último não é link', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var items, _c;
        var page = _b.page;
        return __generator(this, function (_d) {
            switch (_d.label) {
                case 0: return [4 /*yield*/, page.goto('/processo/1')];
                case 1:
                    _d.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _d.sent();
                    return [4 /*yield*/, getBreadcrumbItemsText(page)];
                case 3:
                    items = _d.sent();
                    (0, test_1.expect)(items.length).toBeGreaterThanOrEqual(2);
                    // Primeiro item é o ícone de home (texto pode estar vazio), então validamos a presença do container e do segundo item
                    return [4 /*yield*/, (0, test_1.expect)(page.locator('[data-testid="breadcrumb-home-icon"]').first()).toBeVisible()];
                case 4:
                    // Primeiro item é o ícone de home (texto pode estar vazio), então validamos a presença do container e do segundo item
                    _d.sent();
                    (0, test_1.expect)(items[1]).toContain('Processo');
                    // Último breadcrumb não é link
                    _c = test_1.expect;
                    return [4 /*yield*/, lastBreadcrumbHasLink(page)];
                case 5:
                    // Último breadcrumb não é link
                    _c.apply(void 0, [_d.sent()]).toBeFalsy();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('processo > unidade: inclui SIGLA e links intermediários corretos', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var items, hrefProcesso, _c;
        var page = _b.page;
        return __generator(this, function (_d) {
            switch (_d.label) {
                case 0: return [4 /*yield*/, page.goto('/processo/1/SESEL')];
                case 1:
                    _d.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _d.sent();
                    return [4 /*yield*/, getBreadcrumbItemsText(page)
                        // Esperado: [home], Processo, SESEL
                    ];
                case 3:
                    items = _d.sent();
                    // Esperado: [home], Processo, SESEL
                    (0, test_1.expect)(items[1]).toContain('Processo');
                    (0, test_1.expect)(items[2]).toContain('SESEL');
                    return [4 /*yield*/, breadcrumbLinkHrefAt(page, 1)];
                case 4:
                    hrefProcesso = _d.sent();
                    (0, test_1.expect)(hrefProcesso).toMatch(/\/processo\/1$/);
                    // Último breadcrumb (SESEL) não é link
                    _c = test_1.expect;
                    return [4 /*yield*/, lastBreadcrumbHasLink(page)];
                case 5:
                    // Último breadcrumb (SESEL) não é link
                    _c.apply(void 0, [_d.sent()]).toBeFalsy();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('processo > unidade > mapa: adiciona página final e mantém links corretos', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var items, hrefProcesso, hrefSigla, _c;
        var page = _b.page;
        return __generator(this, function (_d) {
            switch (_d.label) {
                case 0: return [4 /*yield*/, page.goto('/processo/1/SESEL/mapa')];
                case 1:
                    _d.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _d.sent();
                    return [4 /*yield*/, getBreadcrumbItemsText(page)
                        // Esperado: [home], Processo, SESEL, Mapa
                    ];
                case 3:
                    items = _d.sent();
                    // Esperado: [home], Processo, SESEL, Mapa
                    (0, test_1.expect)(items[1]).toContain('Processo');
                    (0, test_1.expect)(items[2]).toContain('SESEL');
                    (0, test_1.expect)(items[3]).toContain('Mapa');
                    return [4 /*yield*/, breadcrumbLinkHrefAt(page, 1)];
                case 4:
                    hrefProcesso = _d.sent();
                    return [4 /*yield*/, breadcrumbLinkHrefAt(page, 2)];
                case 5:
                    hrefSigla = _d.sent();
                    (0, test_1.expect)(hrefProcesso).toMatch(/\/processo\/1$/);
                    (0, test_1.expect)(hrefSigla).toMatch(/\/processo\/1\/SESEL$/);
                    // Último breadcrumb (Mapa) não é link
                    _c = test_1.expect;
                    return [4 /*yield*/, lastBreadcrumbHasLink(page)];
                case 6:
                    // Último breadcrumb (Mapa) não é link
                    _c.apply(void 0, [_d.sent()]).toBeFalsy();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('unidade: mostra apenas (home) > SIGLA; último não é link', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var items, _c;
        var page = _b.page;
        return __generator(this, function (_d) {
            switch (_d.label) {
                case 0: return [4 /*yield*/, page.goto('/unidade/SESEL')];
                case 1:
                    _d.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 2:
                    _d.sent();
                    return [4 /*yield*/, getBreadcrumbItemsText(page)
                        // Esperado: [home], SESEL
                    ];
                case 3:
                    items = _d.sent();
                    // Esperado: [home], SESEL
                    return [4 /*yield*/, (0, test_1.expect)(page.locator('[data-testid="breadcrumb-home-icon"]').first()).toBeVisible()];
                case 4:
                    // Esperado: [home], SESEL
                    _d.sent();
                    (0, test_1.expect)(items[1]).toContain('SESEL');
                    // Último breadcrumb não é link
                    _c = test_1.expect;
                    return [4 /*yield*/, lastBreadcrumbHasLink(page)];
                case 5:
                    // Último breadcrumb não é link
                    _c.apply(void 0, [_d.sent()]).toBeFalsy();
                    return [2 /*return*/];
            }
        });
    }); });
});
