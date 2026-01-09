import {describe, expect, it} from "vitest";
import {mapProcessoDetalheDtoToFrontend, mapUnidadeParticipanteDtoToFrontend,} from "../processos";
import type {Processo, UnidadeParticipante} from "@/types/tipos";

describe("mappers/processos", () => {
    it("mapUnidadeParticipanteDtoToFrontend handles missing children (filhos)", () => {
        const dto = {
            codigo: 1,
            // filhos is undefined
        };
        const model: UnidadeParticipante = mapUnidadeParticipanteDtoToFrontend(dto);
        expect(model.codUnidade).toBe(1);
        expect(model.filhos).toEqual([]);
    });

    it("mapProcessoDetalheDtoToFrontend handles missing lists", () => {
        const dto = {
            codigo: 1,
            // unidades and resumoSubprocessos are undefined
        };
        const model: Processo = mapProcessoDetalheDtoToFrontend(dto);
        expect(model.codigo).toBe(1);
        expect(model.unidades).toEqual([]);
        expect(model.resumoSubprocessos).toEqual([]);
    });
});
