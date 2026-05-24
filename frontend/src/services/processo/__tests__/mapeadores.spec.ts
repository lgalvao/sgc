import {describe, expect, it} from "vitest";
import {mapearProcessoDetalhe} from "../mapeadores";
import {SituacaoSubprocesso} from "@/types/tipos";

describe("processo/mapeadores", () => {
  it("mapearProcessoDetalhe deve transformar DTO em modelo com valores padrão", () => {
    const dto: any = {
      codigo: 1,
      unidades: [
        {
          nome: "Unidade 1",
          sigla: "U1",
          codUnidade: 10,
          codSubprocesso: null,
          situacaoSubprocesso: null,
          filhos: null
        }
      ]
    };

    const model = mapearProcessoDetalhe(dto);

    expect(model.codigo).toBe(1);
    expect(model.unidades[0].codSubprocesso).toBe(0);
    expect(model.unidades[0].situacaoSubprocesso).toBe(SituacaoSubprocesso.NAO_INICIADO);
    expect(model.unidades[0].filhos).toEqual([]);
  });

  it("mapearProcessoDetalhe deve lidar com situacaoSubprocesso valida", () => {
    const dto: any = {
      unidades: [{
        nome: "U", sigla: "S",
        situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
      }]
    };
    const model = mapearProcessoDetalhe(dto);
    expect(model.unidades[0].situacaoSubprocesso).toBe(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
  });
});
