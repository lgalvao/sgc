import {describe, expect, it} from "vitest";
import {mapVWUsuariosArray, mapVWUsuarioToServidor,} from "@/mappers/servidores";
import {mapUnidade, mapUnidadesArray, mapUnidadeSnapshot,} from "@/mappers/unidades";
import type {Alerta, Mapa, MapaAjuste, MapaVisualizacao} from "@/types/tipos";
import {mapAlertaDtoToFrontend} from "../alertas";
import {
    mapAtividadeDtoToModel,
    mapConhecimentoDtoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto,
} from "../atividades";
import {
    mapImpactoMapaDtoToModel,
    mapMapaAjusteDtoToModel,
    mapMapaCompletoDtoToModel,
    mapMapaDtoToModel,
    mapMapaVisualizacaoToAtividades,
} from "../mapas";
import {mapProcessoDetalheDtoToFrontend, mapProcessoDtoToFrontend, mapProcessoResumoDtoToFrontend,} from "../processos";
import {
    LoginResponseToFrontend,
    mapPerfilUnidadeToFrontend,
    mapUsuarioToFrontend,
    perfisUnidadesParaDominio
} from "../sgrh";

describe("mappers/alertas", () => {
    it("mapAlertaDtoToFrontend should map all fields correctly", () => {
        const dto = {
            codigo: 1,
            codProcesso: 10,
            descricao: "Test Alert",
            dataHora: "2025-10-18T10:00:00",
            unidadeOrigem: "UO",
            unidadeDestino: "UD",
            dataHoraLeitura: null,
            linkDestino: "/test",
            mensagem: "Test Message",
            dataHoraFormatada: "18/10/2025 10:00",
            origem: "UO",
            processo: "Test Process",
        };

        const mapped: Alerta = mapAlertaDtoToFrontend(dto);

        expect(mapped.codigo).toBe(1);
        expect(mapped.codProcesso).toBe(10);
        expect(mapped.descricao).toBe("Test Alert");
        expect(mapped.dataHora).toBe("2025-10-18T10:00:00");
        expect(mapped.unidadeOrigem).toBe("UO");
        expect(mapped.unidadeDestino).toBe("UD");
        expect(mapped.linkDestino).toBe("/test");
        expect(mapped.mensagem).toBe("Test Message");
        expect(mapped.dataHoraFormatada).toBe("18/10/2025 10:00");
        expect(mapped.origem).toBe("UO");
        expect(mapped.processo).toBe("Test Process");
    });
});

describe("mappers/atividades", () => {
    it("mapAtividadeDtoToModel should map correctly", () => {
        const dto = {
            codigo: 1,
            descricao: "Atividade Teste",
            conhecimentos: [{id: 101, descricao: "Conhecimento Teste"}],
        };
        const model = mapAtividadeDtoToModel(dto);
        expect(model.codigo).toBe(1);
        expect(model.descricao).toBe("Atividade Teste");
        expect(model.conhecimentos).toHaveLength(1);
        expect(model.conhecimentos[0].id).toBe(101);
    });

    it("mapConhecimentoDtoToModel should map correctly", () => {
        const dto = {id: 101, descricao: "Conhecimento Teste"};
        const model = mapConhecimentoDtoToModel(dto);
        expect(model.id).toBe(101);
        expect(model.descricao).toBe("Conhecimento Teste");
    });

    it("mapCriarAtividadeRequestToDto should add codSubrocesso", () => {
        const request = {descricao: "Nova Atividade"};
        const dto = mapCriarAtividadeRequestToDto(request, 123);
        expect(dto.descricao).toBe("Nova Atividade");
        expect(dto.mapaCodigo).toBe(123);
    });

    it("mapCriarConhecimentoRequestToDto should map correctly", () => {
        const request = {descricao: "Novo Conhecimento"};
        const dto = mapCriarConhecimentoRequestToDto(request, 1);
        expect(dto.descricao).toBe("Novo Conhecimento");
    });
});

describe("mappers/mapas", () => {
    it("mapMapaVisualizacaoToAtividades should extract and map all activities including knowledge", () => {
        const dto: MapaVisualizacao = {
            codigo: 1,
            descricao: "mapa",
            competencias: [
                {
                    codigo: 1,
                    descricao: "c1",
                    atividades: [
                        {
                            codigo: 1,
                            descricao: "A1",
                            conhecimentos: [{id: 10, descricao: "K1"}],
                        },
                    ],
                },
                {
                    codigo: 2,
                    descricao: "c2",
                    atividades: [
                        {codigo: 2, descricao: "A2", conhecimentos: []},
                        {codigo: 3, descricao: "A3", conhecimentos: []},
                    ],
                },
            ],
        };
        const atividades = mapMapaVisualizacaoToAtividades(dto);
        expect(atividades).toHaveLength(3);
        expect(atividades[0].codigo).toBe(1);
        expect(atividades[0].conhecimentos).toHaveLength(1);
        expect(atividades[0].conhecimentos[0].id).toBe(10);
        expect(atividades[2].codigo).toBe(3);
    });

    it("mapMapaVisualizacaoToAtividades should handle null or missing competencias", () => {
        expect(mapMapaVisualizacaoToAtividades(null as any)).toEqual([]);
        expect(mapMapaVisualizacaoToAtividades({} as any)).toEqual([]);
        expect(
            mapMapaVisualizacaoToAtividades({competencias: null} as any),
        ).toEqual([]);
    });

    it("mapMapaDtoToModel should map fields and handle dates", () => {
        const dto = {
            codigo: 1,
            dataCriacao: "2025-01-01T00:00:00Z",
            dataDisponibilizacao: "2025-01-02T00:00:00Z",
            competencias: [],
        };
        const model: Mapa = mapMapaDtoToModel(dto);
        expect(model.codigo).toBe(1);
        expect(model.dataCriacao).toBe("2025-01-01T00:00:00Z");
        expect(model.dataDisponibilizacao).toBe("2025-01-02T00:00:00Z");
        expect(model.dataFinalizacao).toBeUndefined();
    });

    it("mapMapaCompletoDtoToModel should map nested structures", () => {
        const dto = {
            codigo: 1,
            competencias: [
                {
                    codigo: 10,
                    atividades: [{codigo: 100, conhecimentos: [{id: 1000}]}],
                },
            ],
        };
        const model = mapMapaCompletoDtoToModel(dto);
        expect(
            (model.competencias[0] as any).atividades[0].conhecimentos[0].id,
        ).toBe(1000);
    });

    it("mapImpactoMapaDtoToModel should map impact fields including activity changes", () => {
        const dto = {
            temImpactos: true,
            competenciasImpactadas: [{codigo: 1, atividadesAfetadas: ["A1"]}],
            atividadesInseridas: [{codigo: 1, descricao: "New"}],
            atividadesRemovidas: [{codigo: 2, descricao: "Old"}],
            atividadesAlteradas: [{codigo: 3, descricao: "Changed"}],
            totalAtividadesInseridas: 1,
            totalAtividadesRemovidas: 1,
            totalAtividadesAlteradas: 1,
            totalCompetenciasImpactadas: 1,
        };
        const model = mapImpactoMapaDtoToModel(dto);
        expect(model.temImpactos).toBe(true);
        expect(model.competenciasImpactadas[0].atividadesAfetadas).toContain("A1");
        expect(model.atividadesInseridas).toHaveLength(1);
        expect(model.atividadesInseridas[0].descricao).toBe("New");
        expect(model.atividadesRemovidas).toHaveLength(1);
        expect(model.atividadesRemovidas[0].codigo).toBe(2);
        expect(model.atividadesAlteradas).toHaveLength(1);
        expect(model.atividadesAlteradas[0].descricao).toBe("Changed");
    });

    it("mapMapaAjusteDtoToModel should map ajuste fields", () => {
        const dto = {codigo: 1, descricao: "mapa", competencias: [1, 2]};
        const model: MapaAjuste = mapMapaAjusteDtoToModel(dto);
        expect(model.competencias).toHaveLength(2);
    });
});

describe("mappers/processos", () => {
    it("mapProcessoResumoDtoToFrontend should copy all properties", () => {
        const dto = {codigo: 1, tipo: "MAPEAMENTO", situacao: "EM_ANDAMENTO"};
        const model = mapProcessoResumoDtoToFrontend(dto);
        expect(model).toEqual(dto);
    });

    it("mapProcessoDtoToFrontend should copy all properties", () => {
        const dto = {
            codigo: 1,
            tipo: "REVISAO",
            situacao: "INICIADO",
            responsavel: "user",
        };
        const model = mapProcessoDtoToFrontend(dto);
        expect(model).toEqual(dto);
    });

    it("mapProcessoDetalheDtoToFrontend should map nested structures", () => {
        const dto = {
            codigo: 1,
            unidades: [{codigo: 10, filhos: [{codigo: 11}]}],
            resumoSubprocessos: [{codigo: 100}],
        };
        const model = mapProcessoDetalheDtoToFrontend(dto);
        expect(model.unidades[0].filhos[0].codUnidade).toBe(11);
        expect(model.resumoSubprocessos[0].codigo).toBe(100);
    });
});

describe("mappers/sgrh", () => {
    it("mapPerfilUnidadeToFrontend should map correctly", () => {
        const dto = {
            perfil: "CHEFE",
            unidade: {codigo: 1, nome: "Unidade Teste", sigla: "UT"},
            siglaUnidade: "UT",
        };
        const model = mapPerfilUnidadeToFrontend(dto);
        expect(model.perfil).toBe("CHEFE");
        expect(model.unidade.codigo).toBe(1);
        expect(model.siglaUnidade).toBe("UT");
    });

    it("mapUsuarioToFrontend should map correctly", () => {
        const dto = {
            tituloEleitoral: 123,
            nome: "Usuário Teste",
            email: "test@test.com",
            ramal: "1234",
            unidade: {codigo: 1, nome: "Unidade Teste", sigla: "UT"},
            perfis: ["CHEFE"],
        };
        const model = mapUsuarioToFrontend(dto);
        expect(model.tituloEleitoral).toBe(123);
        expect(model.nome).toBe("Usuário Teste");
        expect(model.unidade.sigla).toBe("UT");
        expect(model.perfis).toContain("CHEFE");
    });

    it("LoginResponseToFrontend should map correctly", () => {
        const dto = {
            tituloEleitoral: 123456,
            perfil: "GESTOR",
            unidadeCodigo: 10,
            token: "abc.def.ghi",
        };
        const model = LoginResponseToFrontend(dto);
        expect(model.tituloEleitoral).toBe(123456);
        expect(model.perfil).toBe("GESTOR");
        expect(model.unidadeCodigo).toBe(10);
        expect(model.token).toBe("abc.def.ghi");
    });

    it("perfisUnidadesParaDominio should map array correctly", () => {
        const backendData = [
            {
                perfil: "GESTOR",
                unidade: {codigo: 1, nome: "Unidade 1", sigla: "U1"},
            },
            {
                perfil: "SERVIDOR",
                unidade: {codigo: 2, nome: "Unidade 2", sigla: "U2"},
            },
        ];
        const result = perfisUnidadesParaDominio(backendData);
        expect(result).toHaveLength(2);
        expect(result[0].perfil).toBe("GESTOR");
        expect(result[0].unidade.sigla).toBe("U1");
        expect(result[0].siglaUnidade).toBe("U1");
        expect(result[1].perfil).toBe("SERVIDOR");
    });
});

describe("mappers/servidores", () => {
    it("mapVWUsuarioToServidor maps numeric titulo to codigo and fields", () => {
        const vw = {
            titulo: "42",
            nome: "Fulano",
            unidade: "SESEL",
            email: "f@t.br",
            ramal: "123",
        };
    const s = mapVWUsuarioToServidor(vw);
    expect(s.codigo).toBe(42);
        expect(s.nome).toBe("Fulano");
        expect(s.unidade).toBe("SESEL");
        expect(s.email).toBe("f@t.br");
        expect(s.ramal).toBe("123");
  });

    it("mapVWUsuarioToServidor uses codigo when provided and defaults missing fields", () => {
        const vw = {codigo: 7, nome: "Beltrano"};
    const s = mapVWUsuarioToServidor(vw);
    expect(s.codigo).toBe(7);
        expect(s.nome).toBe("Beltrano");
        expect(s.unidade).toBe("");
  });

    it("mapVWUsuariosArray maps array", () => {
        const arr = [
            {codigo: 1, nome: "A"},
            {titulo: "2", nome: "B"},
        ];
    const res = mapVWUsuariosArray(arr);
    expect(res.length).toBe(2);
    expect(res[0].codigo).toBe(1);
    expect(res[1].codigo).toBe(2);
  });
});

describe("mappers/unidades", () => {
    it("mapUnidade maps fields and responsavel, and recursive filhas", () => {
    const u = {
      codigo: 10,
        sigla: "SETEST",
        nome: "Seção Teste",
        tipo: "OPERACIONAL",
      idServidorTitular: 99,
      responsavel: {
        idServidorResponsavel: 100,
          tipo: "Substituição",
          dataInicio: "2025-01-01",
          dataFim: null,
      },
      filhas: [
          {
              codigo: 11,
              sigla: "FILHA",
              nome: "Filha",
              tipo: "OPERACIONAL",
              filhas: [],
          },
      ],
    };

    const mapped = mapUnidade(u);
    expect(mapped.codigo).toBe(10);
        expect(mapped.sigla).toBe("SETEST");
        expect(mapped.nome).toBe("Seção Teste");
    expect(mapped.idServidorTitular).toBe(99);
    expect(mapped.responsavel).not.toBeNull();
    expect((mapped.responsavel as any)!.idServidor).toBe(100);
    expect(Array.isArray(mapped.filhas)).toBe(true);
    expect(mapped.filhas[0].codigo).toBe(11);
  });

    it("mapUnidadeSnapshot maps simple snapshot structure recursively", () => {
        const s = {
            sigla: "ROOT",
            tipo: "OPERACIONAL",
            filhas: [{sigla: "C1", tipo: "OPERACIONAL", filhas: []}],
        };
    const snap = mapUnidadeSnapshot(s);
        expect(snap.sigla).toBe("ROOT");
    expect(snap.filhas.length).toBe(1);
        expect(snap.filhas[0].sigla).toBe("C1");
  });

    it("mapUnidadesArray maps arrays", () => {
        const arr = [
            {codigo: 1, sigla: "A"},
            {codigo: 2, sigla: "B"},
        ];
    const res = mapUnidadesArray(arr);
    expect(res.length).toBe(2);
        expect(res[0].sigla).toBe("A");
  });
});
