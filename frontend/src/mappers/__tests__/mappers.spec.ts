import {describe, expect, it} from "vitest";
import {mapVWUsuariosArray, mapVWUsuarioToUsuario,} from "@/mappers/usuarios";
import {mapUnidade, mapUnidadesArray, mapUnidadeSnapshot,} from "@/mappers/unidades";
import type {Alerta, Mapa, MapaAjuste} from "@/types/tipos";
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
} from "../mapas";
import {mapProcessoDetalheDtoToFrontend, mapProcessoDtoToFrontend, mapProcessoResumoDtoToFrontend,} from "../processos";
import {
    LoginResponseToFrontend,
    mapPerfilUnidadeToFrontend,
    mapUsuarioToFrontend,
    perfisUnidadesParaDominio
} from "../sgrh";

describe("mappers/alertas", () => {
    it("mapAlertaDtoToFrontend deve mapear todos os campos corretamente", () => {
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
        expect(mapped.mensagem).toBe("Test Message");
        expect(mapped.dataHoraFormatada).toBe("18/10/2025 10:00");
        expect(mapped.origem).toBe("UO");
        expect(mapped.processo).toBe("Test Process");
    });
});

describe("mappers/atividades", () => {
    it("mapAtividadeDtoToModel deve mapear corretamente", () => {
        const dto = {
            codigo: 1,
            descricao: "Atividade Teste",
            conhecimentos: [{codigo: 101, descricao: "Conhecimento Teste"}],
        };
        const model = mapAtividadeDtoToModel(dto);
        expect(model.codigo).toBe(1);
        expect(model.descricao).toBe("Atividade Teste");
        expect(model.conhecimentos).toHaveLength(1);
        expect(model.conhecimentos[0].codigo).toBe(101);
    });

    it("mapConhecimentoDtoToModel deve mapear corretamente", () => {
        const dto = {codigo: 101, descricao: "Conhecimento Teste"};
        const model = mapConhecimentoDtoToModel(dto);
        expect(model.codigo).toBe(101);
        expect(model.descricao).toBe("Conhecimento Teste");
    });

    it("mapCriarAtividadeRequestToDto deve adicionar codSubrocesso", () => {
        const request = {descricao: "Nova Atividade"};
        const dto = mapCriarAtividadeRequestToDto(request, 123);
        expect(dto.descricao).toBe("Nova Atividade");
        expect(dto.mapaCodigo).toBe(123);
    });

    it("mapCriarConhecimentoRequestToDto deve mapear corretamente", () => {
        const request = {descricao: "Novo Conhecimento"};
        const dto = mapCriarConhecimentoRequestToDto(request, 1);
        expect(dto.descricao).toBe("Novo Conhecimento");
    });
});

describe("mappers/mapas", () => {
    it("mapMapaDtoToModel deve mapear campos e tratar datas", () => {
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

    // TODO: Fix this test or the mapper. The mapper mapMapaCompletoDtoToModel currently does not map 'atividades' structure from the input DTO, causing this test to fail.
    it("mapMapaCompletoDtoToModel should map nested structures", () => {
        const dto = {
            codigo: 1,
            competencias: [
                {
                    codigo: 10,
                    atividades: [{codigo: 100, conhecimentos: [{codigo: 1000}]}],
                },
            ],
        };
        const model = mapMapaCompletoDtoToModel(dto);
        expect(
            (model.competencias[0] as any).atividades[0].conhecimentos[0].codigo,
        ).toBe(1000);
    });

    it("mapImpactoMapaDtoToModel deve mapear campos de impacto incluindo mudanças de atividade", () => {
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

    it("mapMapaAjusteDtoToModel deve mapear campos de ajuste", () => {
        const dto = {codigo: 1, descricao: "mapa", competencias: [1, 2]};
        const model: MapaAjuste = mapMapaAjusteDtoToModel(dto);
        expect(model.competencias).toHaveLength(2);
    });
});

describe("mappers/processos", () => {
    it("mapProcessoResumoDtoToFrontend deve copiar todas as propriedades", () => {
        const dto = {codigo: 1, tipo: "MAPEAMENTO", situacao: "EM_ANDAMENTO"};
        const model = mapProcessoResumoDtoToFrontend(dto);
        expect(model).toEqual(dto);
    });

    it("mapProcessoDtoToFrontend deve copiar todas as propriedades", () => {
        const dto = {
            codigo: 1,
            tipo: "REVISAO",
            situacao: "INICIADO",
            responsavel: "user",
        };
        const model = mapProcessoDtoToFrontend(dto);
        expect(model).toEqual(dto);
    });

    it("mapProcessoDetalheDtoToFrontend deve mapear estruturas aninhadas", () => {
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
    it("mapPerfilUnidadeToFrontend deve mapear corretamente", () => {
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

    it("mapUsuarioToFrontend deve mapear corretamente", () => {
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

    it("LoginResponseToFrontend deve mapear corretamente", () => {
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

    it("perfisUnidadesParaDominio deve mapear array corretamente", () => {
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

describe("mappers/usuarios", () => {
    it("mapVWUsuarioToUsuario mapeia titulo numérico para codigo e campos", () => {
        const vw = {
            titulo: "42",
            nome: "Fulano",
            unidade: "SESEL",
            email: "f@t.br",
            ramal: "123",
        };
        const s = mapVWUsuarioToUsuario(vw);
        expect(s.codigo).toBe(42);
        expect(s.nome).toBe("Fulano");
        expect(s.unidade).toBe("SESEL");
        expect(s.email).toBe("f@t.br");
        expect(s.ramal).toBe("123");
    });

    it("mapVWUsuarioToUsuario usa codigo quando fornecido e define valores padrão para campos ausentes", () => {
        const vw = {codigo: 7, nome: "Beltrano"};
        const s = mapVWUsuarioToUsuario(vw);
        expect(s.codigo).toBe(7);
        expect(s.nome).toBe("Beltrano");
        expect(s.unidade).toBe("");
    });

    it("mapVWUsuariosArray mapeia array", () => {
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
    it("mapUnidade mapeia campos, responsavel e filhas recursivas", () => {
        const u = {
            codigo: 10,
            sigla: "SETEST",
            nome: "Seção Teste",
            tipo: "OPERACIONAL",
            usuarioCodigo: 99,
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
        expect(mapped.usuarioCodigo).toBe(99);
        expect(mapped.responsavel).not.toBeNull();
        expect((mapped.responsavel as any)!.usuarioCodigo).toBe(100);
        expect(Array.isArray(mapped.filhas)).toBe(true);
        expect(mapped.filhas[0].codigo).toBe(11);
    });

    it("mapUnidadeSnapshot mapeia estrutura simples de snapshot recursivamente", () => {
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

    it("mapUnidadesArray mapeia arrays", () => {
        const arr = [
            {codigo: 1, sigla: "A"},
            {codigo: 2, sigla: "B"},
        ];
        const res = mapUnidadesArray(arr);
        expect(res.length).toBe(2);
        expect(res[0].sigla).toBe("A");
    });
});
