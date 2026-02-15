import {mount} from "@vue/test-utils";
import {afterEach, describe, expect, it, vi} from "vitest";
import * as utils from "@/utils";
import SubprocessoModal from "../processo/SubprocessoModal.vue";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("SubprocessoModal", () => {
    const context = setupComponentTest();

    afterEach(() => {
        vi.restoreAllMocks();
    });

    const dataLimiteAtual = new Date("2024-10-10T00:00:00");

    it("não deve renderizar o modal quando mostrarModal for falso", () => {
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: false, dataLimiteAtual, etapaAtual: 1},
        });
        expect(
            context.wrapper.find('[data-testid="input-nova-data-limite"]').exists(),
        ).toBe(false);
    });

    it("deve inicializar o campo de data com a data limite atual", () => {
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, etapaAtual: 1},
        });
        const input = context.wrapper.find('[data-testid="input-nova-data-limite"]');
        expect((input.element as HTMLInputElement).value).toBe("2024-10-10");
    });

    it("deve desabilitar o botão de confirmar se a data for inválida", async () => {
        vi.spyOn(utils, "isDateValidAndFuture").mockReturnValue(false);
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, etapaAtual: 1},
        });

        const confirmButton = context.wrapper.find('[data-testid="btn-modal-confirmar"]');
        expect(confirmButton.attributes("disabled")).toBeDefined();
    });

    it("deve habilitar o botão de confirmar se a data for válida", async () => {
        vi.spyOn(utils, "isDateValidAndFuture").mockReturnValue(true);
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, etapaAtual: 1},
        });

        await context.wrapper
            .find('[data-testid="input-nova-data-limite"]')
            .setValue("2025-01-01");

        const confirmButton = context.wrapper.find('[data-testid="btn-modal-confirmar"]');
        expect(confirmButton.attributes("disabled")).toBeUndefined();
    });

    it('deve emitir "fecharModal" ao clicar no botão de cancelar', async () => {
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, etapaAtual: 1},
        });
        await context.wrapper.find('[data-testid="subprocesso-modal__btn-modal-cancelar"]').trigger("click");
        expect(context.wrapper.emitted("fecharModal")).toBeTruthy();
    });

    it('deve emitir "confirmarAlteracao" com a nova data', async () => {
        vi.spyOn(utils, "isDateValidAndFuture").mockReturnValue(true);
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, etapaAtual: 1},
        });

        const novaData = "2025-01-01";
        await context.wrapper
            .find('[data-testid="input-nova-data-limite"]')
            .setValue(novaData);
        await context.wrapper.find('[data-testid="btn-modal-confirmar"]').trigger("click");

        expect(context.wrapper.emitted("confirmarAlteracao")).toBeTruthy();
        expect(context.wrapper.emitted("confirmarAlteracao")?.[0]).toEqual([novaData]);
    });
});
