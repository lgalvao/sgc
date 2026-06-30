import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {CHAVE_QUERY_UNIDADES, useInvalidacaoUnidades, useUnidadesQuery} from "../useUnidadesQuery";
import * as unidadeService from "@/services/unidadeService";

let queryOptions: any = null;
const invalidateQueriesMock = vi.fn();

vi.mock("@pinia/colada", () => ({
    useQuery: vi.fn((options: any) => {
        queryOptions = options;
        return {
            data: ref([]),
            status: ref("success"),
        };
    }),
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
    }),
}));

const mockPerfilSelecionado = ref<string | undefined>("SERVIDOR");

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => ({
        get perfilSelecionado() {
            return mockPerfilSelecionado.value;
        },
    }),
}));

vi.mock("@/services/unidadeService", () => ({
    buscarTodasUnidades: vi.fn(),
}));

describe("useUnidadesQuery", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        queryOptions = null;
        mockPerfilSelecionado.value = "SERVIDOR";
    });

    it("deve inicializar useQuery com as opções corretas", async () => {
        useUnidadesQuery();

        expect(queryOptions).toBeDefined();

        // 1. key
        expect(queryOptions.key).toEqual(CHAVE_QUERY_UNIDADES);

        // 2. query
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue([{codigo: 1, nome: "U1"}] as any);
        const res = await queryOptions.query();
        expect(res).toEqual([{codigo: 1, nome: "U1"}]);
        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();

        // 3. enabled
        expect(queryOptions.enabled()).toBe(true);
    });

    it("deve retornar enabled false se perfilSelecionado for ausente", () => {
        mockPerfilSelecionado.value = undefined;
        useUnidadesQuery();

        expect(queryOptions.enabled()).toBe(false);
    });

    it("useInvalidacaoUnidades deve chamar invalidateQueries corretamente", () => {
        const {invalidarUnidades} = useInvalidacaoUnidades();
        invalidarUnidades();

        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: CHAVE_QUERY_UNIDADES});
    });
});
