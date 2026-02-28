import {describe, expect, it} from "vitest";
import {useValidacao} from "../useValidacao";

describe("useValidacao", () => {
    it("retorna primeiro campo com erro", () => {
        const {obterPrimeiroCampoComErro} = useValidacao();
        const primeiro = obterPrimeiroCampoComErro({
            descricao: "",
            tipo: "Tipo obrigatório",
            dataLimite: "Data obrigatória"
        });
        expect(primeiro).toBe("tipo");
    });

    it("identifica quando não há erros", () => {
        const {possuiErros} = useValidacao();
        expect(possuiErros({descricao: "", tipo: null})).toBe(false);
    });
});
