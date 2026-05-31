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
            props: {mostrarModal: false, dataLimiteAtual, dataFimEtapaAnterior: null, ultimaDataLimiteSubprocesso: dataLimiteAtual, etapaAtual: 1},
        });
        expect(
            context.wrapper.find('[data-testid="input-nova-data-limite"]').exists(),
        ).toBe(false);
    });

    it("deve inicializar o campo de data com a data limite atual", () => {
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, dataFimEtapaAnterior: null, ultimaDataLimiteSubprocesso: dataLimiteAtual, etapaAtual: 1},
        });
        const input = context.wrapper.find('[data-testid="input-nova-data-limite"]');
        expect((input.element as HTMLInputElement).value).toBe("2024-10-10");
    });

    it("deve mostrar erro ao confirmar se a data for inválida", async () => {
        vi.spyOn(utils, "ehDataEstritamenteFutura").mockReturnValue(false);
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, dataFimEtapaAnterior: null, ultimaDataLimiteSubprocesso: dataLimiteAtual, etapaAtual: 1},
        });

        const confirmButton = context.wrapper.find('[data-testid="btn-modal-confirmar"]');
        expect(confirmButton.attributes("disabled")).toBeUndefined();
        await confirmButton.trigger("click");

        expect(context.wrapper.emitted("confirmarAlteracao")).toBeUndefined();
        expect(context.wrapper.text()).toContain("A data limite para validação deve ser uma data futura.");
    });

    it("deve habilitar o botão de confirmar se a data for válida", async () => {
        vi.spyOn(utils, "ehDataEstritamenteFutura").mockReturnValue(true);
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, dataFimEtapaAnterior: null, ultimaDataLimiteSubprocesso: dataLimiteAtual, etapaAtual: 1},
        });

        await context.wrapper
            .find('[data-testid="input-nova-data-limite"]')
            .setValue(utils.obterAmanhaFormatado());

        const confirmButton = context.wrapper.find('[data-testid="btn-modal-confirmar"]');
        expect(confirmButton.attributes("disabled")).toBeUndefined();
    });

    it("deve usar a última data limite como data mínima quando ela for maior que amanhã", () => {
        context.wrapper = mount(SubprocessoModal, {
            props: {
                mostrarModal: true,
                dataLimiteAtual,
                dataFimEtapaAnterior: null,
                ultimaDataLimiteSubprocesso: new Date("2099-12-31T12:00:00"),
                etapaAtual: 1
            },
        });

        const input = context.wrapper.find('[data-testid="input-nova-data-limite"]');
        expect(input.attributes("min")).toBe("2099-12-31");
    });

    it("deve permitir confirmar uma data futura menor que a data limite atual quando respeitar a última data limite do subprocesso", async () => {
        vi.spyOn(utils, "ehDataEstritamenteFutura").mockReturnValue(true);
        context.wrapper = mount(SubprocessoModal, {
            props: {
                mostrarModal: true,
                dataLimiteAtual: new Date("2099-05-31T00:00:00"),
                dataFimEtapaAnterior: null,
                ultimaDataLimiteSubprocesso: new Date("2099-05-01T00:00:00"),
                etapaAtual: 1
            },
        });

        await context.wrapper
            .find('[data-testid="input-nova-data-limite"]')
            .setValue("2099-05-30");
        await context.wrapper.find('[data-testid="btn-modal-confirmar"]').trigger("click");

        expect(context.wrapper.text()).not.toContain("A data limite deve ser maior ou igual à última data limite do subprocesso.");
        expect(context.wrapper.emitted("confirmarAlteracao")?.[0]).toEqual(["2099-05-30"]);
    });

    it('deve emitir "fecharModal" ao clicar no botão de cancelar', async () => {
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, dataFimEtapaAnterior: null, ultimaDataLimiteSubprocesso: dataLimiteAtual, etapaAtual: 1},
        });
        await context.wrapper.find('[data-testid="subprocesso-modal__btn-modal-cancelar"]').trigger("click");
        expect(context.wrapper.emitted("fecharModal")).toBeDefined();
    });

    it('deve emitir "confirmarAlteracao" com a nova data', async () => {
        vi.spyOn(utils, "ehDataEstritamenteFutura").mockReturnValue(true);
        context.wrapper = mount(SubprocessoModal, {
            props: {mostrarModal: true, dataLimiteAtual, dataFimEtapaAnterior: null, ultimaDataLimiteSubprocesso: dataLimiteAtual, etapaAtual: 1},
        });

        const novaData = utils.obterAmanhaFormatado();
        await context.wrapper
            .find('[data-testid="input-nova-data-limite"]')
            .setValue(novaData);
        await context.wrapper.find('[data-testid="btn-modal-confirmar"]').trigger("click");

        expect(context.wrapper.emitted("confirmarAlteracao")).toBeDefined();
        expect(context.wrapper.emitted("confirmarAlteracao")?.[0]).toEqual([novaData]);
    });

    it("deve exigir data posterior ao fim da etapa anterior na etapa 2", async () => {
        vi.spyOn(utils, "ehDataEstritamenteFutura").mockReturnValue(true);
        context.wrapper = mount(SubprocessoModal, {
            props: {
                mostrarModal: true,
                dataLimiteAtual: new Date("2099-05-31T00:00:00"),
                dataFimEtapaAnterior: new Date("2099-05-30T00:00:00"),
                ultimaDataLimiteSubprocesso: new Date("2099-05-01T00:00:00"),
                etapaAtual: 2
            },
        });

        const input = context.wrapper.find('[data-testid="input-nova-data-limite"]');
        expect(input.attributes("min")).toBe("2099-05-31");

        await input.setValue("2099-05-30");
        await context.wrapper.find('[data-testid="btn-modal-confirmar"]').trigger("click");

        expect(context.wrapper.text()).toContain("A data limite deve ser maior que a data de fim da etapa anterior.");
        expect(context.wrapper.emitted("confirmarAlteracao")).toBeUndefined();
    });
});
