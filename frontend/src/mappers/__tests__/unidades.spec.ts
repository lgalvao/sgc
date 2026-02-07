import {describe, expect, it} from "vitest";
import {mapUnidade, mapUnidadesArray, mapUnidadeSnapshot} from "../unidades";

describe("mappers/unidades", () => {
    describe("mapUnidadeSnapshot", () => {
        it("handles null/undefined input", () => {
            const snap = mapUnidadeSnapshot({});
            expect(snap.codigo).toBe(0);
            expect(snap.nome).toBe("");
            expect(snap.sigla).toBe("");
            expect(snap.filhas).toEqual([]);
        });

        it("maps alternative fields", () => {
            const snap = mapUnidadeSnapshot({
                nome_unidade: "Name",
                sigla_unidade: "SIG",
                subunidades: [{sigla: "SUB"}]
            });
            expect(snap.nome).toBe("Name");
            expect(snap.sigla).toBe("SIG");
            expect(snap.filhas).toHaveLength(1);
            expect(snap.filhas![0].sigla).toBe("SUB");
        });

        it("maps another alternative for sigla", () => {
             const snap = mapUnidadeSnapshot({
                unidade: "SIG2"
            });
            expect(snap.sigla).toBe("SIG2");
        });
    });

    describe("mapUnidade", () => {
        it("handles null/undefined input object", () => {
            const u = mapUnidade({});
            expect(u.codigo).toBe(0);
            expect(u.sigla).toBe("");
            expect(u.responsavel).toBeNull();
            expect(u.filhas).toEqual([]);
        });

        it("maps alternative fields", () => {
             const u = mapUnidade({
                codigo_unidade: 10,
                sigla_unidade: "SU",
                tipo_unidade: "TIPO",
                nome_unidade: "NU",
                idServidorTitular: 99,
                subunidades: [{codigo: 11}]
            });
            expect(u.codigo).toBe(10);
            expect(u.sigla).toBe("SU");
            expect(u.tipo).toBe("TIPO");
            expect(u.nome).toBe("NU");
            expect(u.usuarioCodigo).toBe(99);
            expect(u.filhas).toHaveLength(1);
        });

        it("maps other alternative fields for user", () => {
             const u = mapUnidade({
                id_servidor_titular: 88
            });
            expect(u.usuarioCodigo).toBe(88);
             const u2 = mapUnidade({
                titular_id: 77
            });
            expect(u2.usuarioCodigo).toBe(77);
        });

        it("maps responsavel with all fallback fields", () => {
             const u = mapUnidade({
                responsavel: {
                    codigo: 1,
                    // missing name
                    // missing tituloEleitoral
                    // missing email
                    // missing ramal
                    usuarioTitulo: "123",
                    unidadeCodigo: 5,
                    usuarioCodigo: 6,
                    // missing tipo
                    dataInicio: "2023-01-01"
                    // missing dataFim
                }
            });
            expect(u.responsavel?.codigo).toBe(1);
            expect(u.responsavel?.nome).toBe("");
            expect(u.responsavel?.tituloEleitoral).toBe("");
            expect((u.responsavel as any)?.usuarioTitulo).toBe("123");
            expect((u.responsavel as any)?.unidadeCodigo).toBe(5);
            expect((u.responsavel as any)?.usuarioCodigo).toBe(6);
            expect((u.responsavel as any)?.tipo).toBe("");
            expect((u.responsavel as any)?.dataInicio).toBe("2023-01-01");
            expect((u.responsavel as any)?.dataFim).toBeNull();
            expect(u.responsavel?.unidade).toEqual({});
        });

        it("maps responsavel using alternative fields", () => {
             const u = mapUnidade({
                responsavel: {
                    idServidorResponsavel: 55
                }
            });
            expect((u.responsavel as any)?.usuarioCodigo).toBe(55);
        });
    });

    describe("mapUnidadesArray", () => {
        it("handles undefined input", () => {
            const arr = mapUnidadesArray();
            expect(arr).toEqual([]);
        });
    });
});
