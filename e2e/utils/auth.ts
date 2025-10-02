import {Page} from "@playwright/test";
import {performLogin} from "./authHelpers";

export async function login(page: Page) {
    await loginAsAdmin(page);
}

export async function loginAsGestor(page: Page) {
    await performLogin(page, '2', 'GESTOR - SGP');
}

export async function loginAsAdmin(page: Page) {
    await performLogin(page, '7', 'ADMIN - SEDOC');
}