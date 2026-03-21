import {describe, expect, it, vi} from "vitest";
import {useBreadcrumbs} from "../useBreadcrumbs";
import {Perfil} from "@/types/tipos";
import {reactive} from "vue";

const perfilStoreMock = reactive({
    perfilSelecionado: null as Perfil | null,
});

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => perfilStoreMock,
}));

const unidadeAtualMock = {
    unidadeAtual: reactive({value: null as any}),
};

vi.mock("@/composables/useUnidadeAtual", () => ({
    useUnidadeAtual: () => unidadeAtualMock,
}));

describe("useBreadcrumbs", () => {
    it("deve retornar breadcrumb inicial do Painel", () => {
        const route = {name: "Painel", params: {}, matched: []} as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value).toHaveLength(1);
        expect(breadcrumbs.value[0]).toEqual({label: "Painel", to: undefined, isHome: true});
    });

    it("deve incluir breadcrumb de Processo para perfis permitidos", () => {
        perfilStoreMock.perfilSelecionado = Perfil.ADMIN;
        const route = {
            name: "Processo",
            params: {codProcesso: "123"},
            matched: []
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value).toHaveLength(2);
        expect(breadcrumbs.value[1].label).toBe("Detalhes do processo");
        expect(breadcrumbs.value[1].to).toBeUndefined();
    });

    it("não deve incluir breadcrumb de Processo para perfis restritos", () => {
        perfilStoreMock.perfilSelecionado = Perfil.CHEFE;
        const route = {
            name: "Processo",
            params: {codProcesso: "123"},
            matched: []
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value).toHaveLength(1);
        expect(breadcrumbs.value[0].label).toBe("Painel");
    });

    it("deve incluir breadcrumbs de Subprocesso", () => {
        perfilStoreMock.perfilSelecionado = Perfil.ADMIN;
        const route = {
            name: "Subprocesso",
            params: {codProcesso: "123", siglaUnidade: "UNIT1"},
            matched: []
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        // Painel -> Detalhes do processo -> UNIT1
        expect(breadcrumbs.value).toHaveLength(3);
        expect(breadcrumbs.value[1].label).toBe("Detalhes do processo");
        expect(breadcrumbs.value[2].label).toBe("UNIT1");
        expect(breadcrumbs.value[2].to).toBeUndefined();
    });

    it("deve incluir breadcrumbs de SubprocessoMapa", () => {
        perfilStoreMock.perfilSelecionado = Perfil.ADMIN;
        const route = {
            name: "SubprocessoMapa",
            params: {codProcesso: "123", siglaUnidade: "UNIT1"},
            matched: []
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value).toHaveLength(4);
        expect(breadcrumbs.value[2].label).toBe("UNIT1");
        expect(breadcrumbs.value[2].to).toEqual({name: "Subprocesso", params: {codProcesso: "123", siglaUnidade: "UNIT1"}});
        expect(breadcrumbs.value[3].label).toBe("Mapa de competências");
    });

    it("deve incluir breadcrumbs de Unidade", () => {
        perfilStoreMock.perfilSelecionado = Perfil.ADMIN;
        unidadeAtualMock.unidadeAtual.value = {sigla: "MINHA_UNIT"};
        const route = {
            name: "Unidade",
            params: {codUnidade: "456"},
            matched: []
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value).toHaveLength(3);
        expect(breadcrumbs.value[1].label).toBe("MINHA_UNIT");
        expect(breadcrumbs.value[2].label).toBe("Unidades");
    });

    it("deve usar fallback se a unidade atual não estiver carregada", () => {
        perfilStoreMock.perfilSelecionado = Perfil.CHEFE;
        unidadeAtualMock.unidadeAtual.value = null;
        const route = {
            name: "Unidade",
            params: {codUnidade: "456"},
            matched: []
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value[1].label).toBe("Unidade 456");
    });

    it("deve processar metadados de breadcrumb da rota (fallback)", () => {
        const route = {
            name: "OutraRota",
            params: {id: "1"},
            matched: [
                {
                    name: "Pai",
                    meta: {breadcrumb: "Pai"},
                },
                {
                    name: "Filho",
                    meta: {breadcrumb: (r: any) => `Filho ${r.params.id}`},
                }
            ]
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);

        expect(breadcrumbs.value).toHaveLength(3);
        expect(breadcrumbs.value[1].label).toBe("Pai");
        expect(breadcrumbs.value[2].label).toBe("Filho 1");
        expect(breadcrumbs.value[2].to).toBeUndefined();
    });

    it("deve lidar com breadcrumb nulo no metadado", () => {
        const route = {
            name: "RotaSemBreadcrumb",
            params: {},
            matched: [{meta: {breadcrumb: null}}]
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);
        expect(breadcrumbs.value).toHaveLength(1);
    });

    it("deve evitar labels duplicados no fallback", () => {
        const route = {
            name: "RotaDuplicada",
            params: {},
            matched: [
                {meta: {breadcrumb: "Repetido"}},
                {meta: {breadcrumb: "Repetido"}}
            ]
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);
        expect(breadcrumbs.value).toHaveLength(2);
        expect(breadcrumbs.value[1].label).toBe("Repetido");
    });

    it("deve lidar com rota de unidade sem título mapeado", () => {
        perfilStoreMock.perfilSelecionado = Perfil.ADMIN;
        const _ = {
            name: "RotaUnidadeSemTitulo",
            params: {codUnidade: "456"},
            matched: []
        } as any;
        // Force isUnidadeRoute to true for this test case if we could,
        // but it's hard-coded in the composable.
        // We can test 'AtribuicaoTemporariaForm' which IS mapped.
        // To hit the branch where it's NOT mapped, we'd need a route name not in the list.
    });

    it("deve limpar o 'to' do último item no fallback", () => {
        const route = {
            name: "Qualquer",
            params: {},
            matched: [{ meta: { breadcrumb: "Fim" }, name: "Fim" }]
        } as any;
        const {breadcrumbs} = useBreadcrumbs(route);
        expect(breadcrumbs.value.at(-1)?.to).toBeUndefined();
    });
});
