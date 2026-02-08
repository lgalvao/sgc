import {describe, expect, it} from "vitest";
import {mapVWUsuariosArray, mapVWUsuarioToUsuario} from "../usuarios";

describe("mappers/usuarios", () => {
    it("mapVWUsuarioToUsuario handles null/undefined input", () => {
        const user = mapVWUsuarioToUsuario(null);
        expect(user.codigo).toBe(0);
        expect(user.nome).toBe("");
    });

    it("mapVWUsuarioToUsuario resolves ID correctly (codigo > titulo number)", () => {
        // codigo priority
        expect(mapVWUsuarioToUsuario({codigo: 2, titulo: "3"}).codigo).toBe(2);
        // titulo numeric priority
        expect(mapVWUsuarioToUsuario({titulo: "3"}).codigo).toBe(3);
    });

    it("mapVWUsuarioToUsuario handles non-numeric titulo", () => {
        const user = mapVWUsuarioToUsuario({titulo: "non-numeric"});
        expect(user.codigo).toBe(0);
        expect(user.tituloEleitoral).toBe("non-numeric");
    });

    it("mapVWUsuarioToUsuario maps alternative fields", () => {
        const user = mapVWUsuarioToUsuario({
            nome_completo: "Full Name",
            unidade_sigla: "US",
            ramal_telefone: "5555",
            titulo_eleitoral: "12345"
        });
        expect(user.nome).toBe("Full Name");
        expect(user.unidade).toBe("US");
        expect(user.ramal).toBe("5555");
        expect(user.tituloEleitoral).toBe("12345");
    });

    it("mapVWUsuarioToUsuario maps other alternative fields set 2", () => {
        const user = mapVWUsuarioToUsuario({
            nome_usuario: "User Name",
            unidade_codigo: "100",
            titulo: "98765"
        });
        expect(user.nome).toBe("User Name");
        expect(user.unidade).toBe("100");
        expect(user.tituloEleitoral).toBe("98765");
    });

    it("mapVWUsuariosArray handles undefined input", () => {
        const users = mapVWUsuariosArray(undefined);
        expect(users).toEqual([]);
    });
});
