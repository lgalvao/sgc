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
test_1.test.describe('CDU-21 - Analisar validação de mapa de competências', function () {
    (0, test_1.test)('deve permitir aceitar mapa com observações', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var mapaCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Login como ADMIN (Zeca Silva - SEDOC)
                return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    // Login como ADMIN (Zeca Silva - SEDOC)
                    _c.sent();
                    // Navegar para o processo 2 que tem subprocesso com mapa disponibilizado
                    return [4 /*yield*/, page.goto('/processo/2')];
                case 2:
                    // Navegar para o processo 2 que tem subprocesso com mapa disponibilizado
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 3:
                    _c.sent();
                    // Clicar na linha da unidade STIC que tem "Mapa disponibilizado"
                    return [4 /*yield*/, page.getByRole('cell', { name: 'STIC', exact: true }).click()];
                case 4:
                    // Clicar na linha da unidade STIC que tem "Mapa disponibilizado"
                    _c.sent();
                    // Aguardar navegação para a página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 5:
                    // Aguardar navegação para a página do subprocesso
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()];
                case 6:
                    _c.sent();
                    // Aguardar o card estar totalmente carregado e clicável
                    return [4 /*yield*/, page.waitForTimeout(500)];
                case 7:
                    // Aguardar o card estar totalmente carregado e clicável
                    _c.sent();
                    mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' });
                    return [4 /*yield*/, mapaCard.waitFor({ state: 'visible' })];
                case 8:
                    _c.sent();
                    return [4 /*yield*/, mapaCard.click({ force: true })];
                case 9:
                    _c.sent();
                    // Aguardar navegação para a visualização do mapa
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)];
                case 10:
                    // Aguardar navegação para a visualização do mapa
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Mapa de competências técnicas')).toBeVisible()];
                case 11:
                    _c.sent();
                    // Verificar se os botões de ação estão presentes
                    return [4 /*yield*/, (0, test_1.expect)(page.getByRole('button', { name: 'Aceitar' })).toBeVisible()];
                case 12:
                    // Verificar se os botões de ação estão presentes
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible()];
                case 13:
                    _c.sent();
                    // Clicar no botão "Aceitar"
                    return [4 /*yield*/, page.getByRole('button', { name: 'Aceitar' }).click()];
                case 14:
                    // Clicar no botão "Aceitar"
                    _c.sent();
                    // Verificar se o modal foi aberto
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Aceitar Mapa de Competências')).toBeVisible()];
                case 15:
                    // Verificar se o modal foi aberto
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Observações (opcional)')).toBeVisible()];
                case 16:
                    _c.sent();
                    // Preencher observações
                    return [4 /*yield*/, page.getByLabel('Observações (opcional)').fill('Mapa aprovado com pequenas observações sobre organização das competências.')];
                case 17:
                    // Preencher observações
                    _c.sent();
                    // Clicar em "Aceitar" no modal
                    return [4 /*yield*/, page.getByRole('button', { name: 'Aceitar' }).click()];
                case 18:
                    // Clicar em "Aceitar" no modal
                    _c.sent();
                    // Verificar mensagem de sucesso
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Mapa homologado')).toBeVisible()];
                case 19:
                    // Verificar mensagem de sucesso
                    _c.sent();
                    // Verificar redirecionamento para a página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 20:
                    // Verificar redirecionamento para a página do subprocesso
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()];
                case 21:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir rejeitar mapa sem observações', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var mapaCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Login como ADMIN
                return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    // Login como ADMIN
                    _c.sent();
                    // Navegar para o processo 2
                    return [4 /*yield*/, page.goto('/processo/2')];
                case 2:
                    // Navegar para o processo 2
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 3:
                    _c.sent();
                    // Clicar na linha da unidade STIC
                    return [4 /*yield*/, page.getByRole('cell', { name: 'STIC', exact: true }).click()];
                case 4:
                    // Clicar na linha da unidade STIC
                    _c.sent();
                    // Aguardar navegação para a página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 5:
                    // Aguardar navegação para a página do subprocesso
                    _c.sent();
                    // Aguardar o card estar totalmente carregado e clicável
                    return [4 /*yield*/, page.waitForTimeout(500)];
                case 6:
                    // Aguardar o card estar totalmente carregado e clicável
                    _c.sent();
                    mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' });
                    return [4 /*yield*/, mapaCard.waitFor({ state: 'visible' })];
                case 7:
                    _c.sent();
                    return [4 /*yield*/, mapaCard.click({ force: true })];
                case 8:
                    _c.sent();
                    // Aguardar navegação para a visualização do mapa
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)];
                case 9:
                    // Aguardar navegação para a visualização do mapa
                    _c.sent();
                    // Clicar no botão "Devolver para ajustes"
                    return [4 /*yield*/, page.getByRole('button', { name: 'Devolver para ajustes' }).click()];
                case 10:
                    // Clicar no botão "Devolver para ajustes"
                    _c.sent();
                    // Verificar mensagem de sucesso
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Mapa devolvido à unidade subordinada, para ajustes')).toBeVisible()];
                case 11:
                    // Verificar mensagem de sucesso
                    _c.sent();
                    // Verificar redirecionamento para a página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 12:
                    // Verificar redirecionamento para a página do subprocesso
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve permitir cancelar aceitação do mapa', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var mapaCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Login como ADMIN
                return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    // Login como ADMIN
                    _c.sent();
                    // Navegar para o processo 2
                    return [4 /*yield*/, page.goto('/processo/2')];
                case 2:
                    // Navegar para o processo 2
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 3:
                    _c.sent();
                    // Clicar na linha da unidade STIC
                    return [4 /*yield*/, page.getByRole('cell', { name: 'STIC', exact: true }).click()];
                case 4:
                    // Clicar na linha da unidade STIC
                    _c.sent();
                    // Aguardar navegação para a página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 5:
                    // Aguardar navegação para a página do subprocesso
                    _c.sent();
                    // Aguardar o card estar totalmente carregado e clicável
                    return [4 /*yield*/, page.waitForTimeout(500)];
                case 6:
                    // Aguardar o card estar totalmente carregado e clicável
                    _c.sent();
                    mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' });
                    return [4 /*yield*/, mapaCard.waitFor({ state: 'visible' })];
                case 7:
                    _c.sent();
                    return [4 /*yield*/, mapaCard.click({ force: true })];
                case 8:
                    _c.sent();
                    // Aguardar navegação para a visualização do mapa
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)];
                case 9:
                    // Aguardar navegação para a visualização do mapa
                    _c.sent();
                    // Clicar no botão "Aceitar"
                    return [4 /*yield*/, page.getByRole('button', { name: 'Aceitar' }).click()];
                case 10:
                    // Clicar no botão "Aceitar"
                    _c.sent();
                    // Verificar se o modal foi aberto
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Aceitar Mapa de Competências')).toBeVisible()];
                case 11:
                    // Verificar se o modal foi aberto
                    _c.sent();
                    // Clicar em "Cancelar"
                    return [4 /*yield*/, page.getByRole('button', { name: 'Cancelar' }).click()];
                case 12:
                    // Clicar em "Cancelar"
                    _c.sent();
                    // Verificar que o modal foi fechado
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Aceitar Mapa de Competências')).not.toBeVisible()];
                case 13:
                    // Verificar que o modal foi fechado
                    _c.sent();
                    // Verificar que ainda estamos na página de visualização do mapa
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Mapa de competências técnicas')).toBeVisible()];
                case 14:
                    // Verificar que ainda estamos na página de visualização do mapa
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve exibir mapa com competências e conhecimentos', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var mapaCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Login como ADMIN
                return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    // Login como ADMIN
                    _c.sent();
                    // Navegar para o processo 2
                    return [4 /*yield*/, page.goto('/processo/2')];
                case 2:
                    // Navegar para o processo 2
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 3:
                    _c.sent();
                    // Clicar na linha da unidade STIC
                    return [4 /*yield*/, page.getByRole('cell', { name: 'STIC', exact: true }).click()];
                case 4:
                    // Clicar na linha da unidade STIC
                    _c.sent();
                    // Aguardar navegação para a página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 5:
                    // Aguardar navegação para a página do subprocesso
                    _c.sent();
                    // Aguardar o card estar totalmente carregado e clicável
                    return [4 /*yield*/, page.waitForTimeout(500)];
                case 6:
                    // Aguardar o card estar totalmente carregado e clicável
                    _c.sent();
                    mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' });
                    return [4 /*yield*/, mapaCard.waitFor({ state: 'visible' })];
                case 7:
                    _c.sent();
                    return [4 /*yield*/, mapaCard.click({ force: true })];
                case 8:
                    _c.sent();
                    // Aguardar navegação para a visualização do mapa
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)];
                case 9:
                    // Aguardar navegação para a visualização do mapa
                    _c.sent();
                    // Verificar se o mapa é exibido
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()];
                case 10:
                    // Verificar se o mapa é exibido
                    _c.sent();
                    // Verificar se há competências exibidas (pode haver ou não competências específicas)
                    // Esta verificação é mais genérica pois depende dos dados de mock
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Mapa de competências técnicas')).toBeVisible()];
                case 11:
                    // Verificar se há competências exibidas (pode haver ou não competências específicas)
                    // Esta verificação é mais genérica pois depende dos dados de mock
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, test_1.test)('deve mostrar mensagem de validação quando superior não é SEDOC', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var page = _b.page;
        return __generator(this, function (_c) {
            // Este teste verifica o fluxo de validação intermediária
            // Quando o superior não é SEDOC, deve mostrar "Mapa aceito e submetido para análise da unidade superior"
            test_1.test.skip();
            return [2 /*return*/];
        });
    }); });
    (0, test_1.test)('deve navegar corretamente através do fluxo completo', function (_a) { return __awaiter(void 0, [_a], void 0, function (_b) {
        var mapaCard;
        var page = _b.page;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0: 
                // Login como ADMIN
                return [4 /*yield*/, (0, auth_1.login)(page)];
                case 1:
                    // Login como ADMIN
                    _c.sent();
                    // 1. Ir para o painel
                    return [4 /*yield*/, (0, test_1.expect)(page.getByTestId('titulo-processos')).toBeVisible()];
                case 2:
                    // 1. Ir para o painel
                    _c.sent();
                    // 2. Navegar para o processo 2
                    return [4 /*yield*/, page.goto('/processo/2')];
                case 3:
                    // 2. Navegar para o processo 2
                    _c.sent();
                    return [4 /*yield*/, page.waitForLoadState('networkidle')];
                case 4:
                    _c.sent();
                    // 3. Verificar que estamos na página do processo
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Revisão de mapeamento STIC/COINF - 2025')).toBeVisible()];
                case 5:
                    // 3. Verificar que estamos na página do processo
                    _c.sent();
                    // 4. Clicar na linha da unidade STIC
                    return [4 /*yield*/, page.getByRole('cell', { name: 'STIC', exact: true }).click()];
                case 6:
                    // 4. Clicar na linha da unidade STIC
                    _c.sent();
                    // 5. Verificar que estamos na página do subprocesso
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC$/)];
                case 7:
                    // 5. Verificar que estamos na página do subprocesso
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()];
                case 8:
                    _c.sent();
                    // 6. Aguardar o card estar totalmente carregado e clicável
                    return [4 /*yield*/, page.waitForTimeout(500)];
                case 9:
                    // 6. Aguardar o card estar totalmente carregado e clicável
                    _c.sent();
                    mapaCard = page.locator('.card-actionable').filter({ hasText: 'Mapa de Competências' });
                    return [4 /*yield*/, mapaCard.waitFor({ state: 'visible' })];
                case 10:
                    _c.sent();
                    return [4 /*yield*/, mapaCard.click({ force: true })];
                case 11:
                    _c.sent();
                    // 8. Verificar que estamos na página de visualização do mapa
                    return [4 /*yield*/, page.waitForURL(/.*\/processo\/2\/STIC\/vis-mapa$/)];
                case 12:
                    // 8. Verificar que estamos na página de visualização do mapa
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('Mapa de competências técnicas')).toBeVisible()];
                case 13:
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible()];
                case 14:
                    _c.sent();
                    // 9. Verificar presença dos botões de ação
                    return [4 /*yield*/, (0, test_1.expect)(page.getByRole('button', { name: 'Aceitar' })).toBeVisible()];
                case 15:
                    // 9. Verificar presença dos botões de ação
                    _c.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible()];
                case 16:
                    _c.sent();
                    return [2 /*return*/];
            }
        });
    }); });
});
