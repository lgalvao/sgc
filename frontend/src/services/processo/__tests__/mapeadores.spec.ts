import {describe, expect, it} from "vitest";
import {mapearProcessoDetalhe, mapearUnidadeImportacao} from "../mapeadores";
import {SituacaoSubprocesso} from "@/types/tipos";
import type {ProcessoDetalheResponseBackend, UnidadeParticipanteDto} from "../types";

describe("processo/mapeadores", () => {
  it("mapearProcessoDetalhe deve passar nulos de codSubprocesso e situacaoSubprocesso sem aplicar defaults", () => {
    const dto: ProcessoDetalheResponseBackend = {
      codigo: 1,
      descricao: "Processo 1",
      tipo: "MAPEAMENTO" as any,
      situacao: "CRIADO" as any,
      dataLimite: "2026-01-10T00:00:00",
      dataCriacao: "2026-01-01T00:00:00",
      dataFinalizacao: undefined,
      podeFinalizar: false,
      podeHomologarCadastro: false,
      podeHomologarMapa: false,
      podeAceitarCadastroBloco: false,
      podeDisponibilizarMapaBloco: false,
      resumoSubprocessos: [],
      acoesBloco: [],
      unidades: [
        {
          nome: "Unidade 1",
          sigla: "U1",
          codUnidade: 10,
          codSubprocesso: null,
          situacaoSubprocesso: null,
          codUnidadeSuperior: null,
          dataLimite: null,
          mapaCodigo: null,
          localizacaoAtualCodigo: null,
          filhos: []
        }
      ]
    };

    const model = mapearProcessoDetalhe(dto);

    expect(model.codigo).toBe(1);
    expect(model.unidades[0].codSubprocesso).toBe(null);
    expect(model.unidades[0].situacaoSubprocesso).toBe(null);
    expect(model.unidades[0].filhos).toEqual([]);
  });

  it("mapearProcessoDetalhe deve lidar com situacaoSubprocesso valida", () => {
    const dto: ProcessoDetalheResponseBackend = {
      codigo: 1,
      descricao: "Processo 1",
      tipo: "MAPEAMENTO" as any,
      situacao: "CRIADO" as any,
      dataLimite: "2026-01-10T00:00:00",
      dataCriacao: "2026-01-01T00:00:00",
      dataFinalizacao: undefined,
      podeFinalizar: false,
      podeHomologarCadastro: false,
      podeHomologarMapa: false,
      podeAceitarCadastroBloco: false,
      podeDisponibilizarMapaBloco: false,
      resumoSubprocessos: [],
      acoesBloco: [],
      unidades: [{
        nome: "U", sigla: "S", codUnidade: 99,
        codSubprocesso: 123,
        codUnidadeSuperior: null,
        situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        dataLimite: null,
        mapaCodigo: null,
        localizacaoAtualCodigo: null,
        filhos: []
      }]
    };
    const model = mapearProcessoDetalhe(dto);
    expect(model.unidades[0].situacaoSubprocesso).toBe(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
  });

  it("mapearUnidadeImportacao deve reaproveitar o mesmo contrato e normalizar nulos opcionais", () => {
    const dto: UnidadeParticipanteDto = {
      nome: "Unidade 2",
      sigla: "U2",
      codUnidade: 20,
      codSubprocesso: null,
      codUnidadeSuperior: null,
      situacaoSubprocesso: null,
      dataLimite: null,
      mapaCodigo: null,
      localizacaoAtualCodigo: null,
      filhos: []
    };

    expect(mapearUnidadeImportacao(dto)).toEqual({
      nome: "Unidade 2",
      sigla: "U2",
      codUnidade: 20,
      codSubprocesso: null,
    });
  });
});
