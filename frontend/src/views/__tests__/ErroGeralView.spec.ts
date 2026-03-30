import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ErroGeralView from "../ErroGeralView.vue";
import {TEXTOS} from "@/constants/textos";

describe("ErroGeralView.vue", () => {
    it("deve renderizar o título, descrição e botão de voltar corretamente", () => {
        const wrapper = mount(ErroGeralView);

        expect(wrapper.find('[data-testid="txt-erro-geral-titulo"]').text()).toBe(TEXTOS.erroGeral.TITULO);
        expect(wrapper.find('[data-testid="txt-erro-geral-descricao"]').text()).toBe(TEXTOS.erroGeral.DESCRICAO);

        const btnVoltar = wrapper.find('[data-testid="btn-erro-geral-voltar"]');
        expect(btnVoltar.exists()).toBe(true);
        expect(btnVoltar.text()).toBe(TEXTOS.erroGeral.ACAO);
    });
});
