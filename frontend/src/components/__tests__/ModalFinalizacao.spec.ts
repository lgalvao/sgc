import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import ModalFinalizacao from "../ModalFinalizacao.vue";
import { setupComponentTest } from "@/test-utils/componentTestHelpers";

describe("ModalFinalizacao", () => {
    const context = setupComponentTest();
    const processoDescricao = "Processo de Teste";

    it("não deve renderizar o modal quando mostrar for falso", () => {
        context.wrapper = mount(ModalFinalizacao, {
            props: {mostrar: false, processoDescricao},
        });
        expect(context.wrapper.find(".alert").exists()).toBe(false);
    });

    it("deve renderizar o modal com a descrição do processo", () => {
        context.wrapper = mount(ModalFinalizacao, {
            props: {mostrar: true, processoDescricao},
        });
        expect(context.wrapper.text()).toContain(processoDescricao);
    });

    it('deve emitir "fechar" ao clicar no botão de cancelar', async () => {
        context.wrapper = mount(ModalFinalizacao, {
            props: {mostrar: true, processoDescricao},
        });
        await context.wrapper
            .find('[data-testid="btn-finalizar-processo-cancelar"]')
            .trigger("click");
        expect(context.wrapper.emitted("fechar")).toBeTruthy();
    });

    it('deve emitir "confirmar" ao clicar no botão de confirmar', async () => {
        context.wrapper = mount(ModalFinalizacao, {
            props: {mostrar: true, processoDescricao},
        });
        await context.wrapper
            .find('[data-testid="btn-finalizar-processo-confirmar"]')
            .trigger("click");
        expect(context.wrapper.emitted("confirmar")).toBeTruthy();
    });
});
