import {expect} from "vitest";

/**
 * Seleciona processo e unidade nos selects do modal e aguarda atualizações reativas.
 */
export async function selecionarProcessoEUnidade(
    wrapper: any,
    processoId = 1,
    unidadeId = 1,
) {
    const processoSelect = wrapper.find("select#processo-select");
    await processoSelect.setValue(processoId);
    await wrapper.vm.$nextTick();

    const unidadeSelect = wrapper.find("select#unidade-select");
    await unidadeSelect.setValue(unidadeId);
    await wrapper.vm.$nextTick();

    return wrapper;
}

/**
 * Assertions helpers para o botão Importar
 */
export function expectImportButtonDisabled(wrapper: any) {
    const importarButton = wrapper.find(".btn-outline-primary");
    expect(importarButton.attributes("disabled")).toBeDefined();
}

export function expectImportButtonEnabled(wrapper: any) {
    const importarButton = wrapper.find(".btn-outline-primary");
    expect(importarButton.attributes("disabled")).toBeUndefined();
}

/**
 * Seleciona a primeira checkbox de atividade
 */
export async function selectFirstCheckbox(wrapper: any) {
    const checkboxes = wrapper.findAll('.form-check-input[type="checkbox"]');
    if (checkboxes.length > 0) {
        await checkboxes[0].setValue(true);
        await wrapper.vm.$nextTick();
    }
}

/**
 * Verifica as opções do select de unidades (inclui opção disabled inicial).
 * labels é array de strings esperadas nas options (sem contar a opção disabled)
 */
export function assertUnidadeOptions(wrapper: any, labels: string[]) {
    const unidadeSelect = wrapper.find("select#unidade-select");
    expect(unidadeSelect.attributes("disabled")).toBeUndefined();

    const options = wrapper.findAll("select#unidade-select option");
    // 1 disabled + labels.length
    expect(options).toHaveLength(1 + labels.length);
    for (let i = 0; i < labels.length; i++) {
        expect(options[i + 1].text()).toBe(labels[i]);
    }
}

/**
 * Navega para uma rota, monta o componente com mountFn e verifica os breadcrumbs esperados.
 * expectedLabels é um array com os labels (excluindo 'Home' que sempre é o primeiro crumb).
 */
export async function navigateAndAssertBreadcrumbs(
    router: any,
    mountFn: () => Promise<any>,
    path: string,
    expectedLabels: string[],
) {
    router.push(path);
    const wrapper = await mountFn();
    const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');

    // Home + expectedLabels
    expect(breadcrumbItems.length).toBe(1 + expectedLabels.length);
    for (let i = 0; i < expectedLabels.length; i++) {
        expect(breadcrumbItems[i + 1].text()).toBe(expectedLabels[i]);
    }
}

/**
 * Helper para verificar que um array contém todos os itens esperados.
 * Útil para reduzir múltiplas chamadas repetitivas a expect(array).toContain(...)
 */
export function expectContainsAll(array: any[], items: string[]) {
    for (const item of items) {
        expect(array).toContain(item);
    }
}
