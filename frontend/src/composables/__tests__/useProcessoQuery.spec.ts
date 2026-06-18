import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {CHAVE_QUERY_PROCESSO, useInvalidacaoProcesso, useProcessoQuery} from "../useProcessoQuery";
import * as processoService from "@/services/processo";

let queryOptions: any = null;
const invalidateQueriesMock = vi.fn();

vi.mock("@pinia/colada", () => ({
    useQuery: vi.fn((options: any) => {
        queryOptions = options;
        return {
            data: ref(null),
            status: ref("success"),
        };
    }),
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
    }),
}));

vi.mock("@/services/processo", () => ({
    buscarContextoCompleto: vi.fn(),
}));

describe("useProcessoQuery", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        queryOptions = null;
    });

    it("deve inicializar useQuery com as opções corretas", async () => {
        useProcessoQuery(42);

        expect(queryOptions).toBeDefined();

        // 1. key
        expect(queryOptions.key()).toEqual([...CHAVE_QUERY_PROCESSO, 42]);

        // 2. query
        const mockProcesso = {codigo: 42, nome: "P42"};
        vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue(mockProcesso as any);
        const res = await queryOptions.query();
        expect(res).toEqual(mockProcesso);
        expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(42);

        // 3. enabled
        expect(queryOptions.enabled).toBe(false);
    });

    it("useInvalidacaoProcesso deve invalidar a query do processo", () => {
        const {invalidarProcesso} = useInvalidacaoProcesso();
        invalidarProcesso();

        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: CHAVE_QUERY_PROCESSO});
    });
});
