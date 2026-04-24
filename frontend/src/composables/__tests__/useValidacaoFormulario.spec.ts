import {describe, expect, it} from "vitest";
import {useValidacaoFormulario} from "../useValidacaoFormulario";

describe("useValidacaoFormulario", () => {
    it("inicia sem submissão registrada", () => {
        const {validacaoSubmetida} = useValidacaoFormulario();

        expect(validacaoSubmetida.value).toBe(false);
    });

    it("registra tentativa de submissão", () => {
        const {validacaoSubmetida, registrarTentativaSubmissao} = useValidacaoFormulario();

        registrarTentativaSubmissao();

        expect(validacaoSubmetida.value).toBe(true);
    });

    it("reseta validação", () => {
        const {validacaoSubmetida, registrarTentativaSubmissao, resetarValidacao} = useValidacaoFormulario();

        registrarTentativaSubmissao();
        resetarValidacao();

        expect(validacaoSubmetida.value).toBe(false);
    });

    it("exibe erro somente depois da tentativa", () => {
        const {deveExibirErro, registrarTentativaSubmissao} = useValidacaoFormulario();

        expect(deveExibirErro(true)).toBe(false);

        registrarTentativaSubmissao();

        expect(deveExibirErro(true)).toBe(true);
        expect(deveExibirErro(false)).toBe(false);
    });

    it("valida submissão e registra tentativa", () => {
        const {validacaoSubmetida, validarSubmissao} = useValidacaoFormulario();

        expect(validarSubmissao(false)).toBe(false);
        expect(validacaoSubmetida.value).toBe(true);
        expect(validarSubmissao(true)).toBe(true);
    });
});
