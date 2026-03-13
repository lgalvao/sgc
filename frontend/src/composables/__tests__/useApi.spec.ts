import {describe, expect, it, vi} from "vitest";
import {useApi} from "@/composables/useApi";

describe("useApi", () => {
    it("deve executar chamada com sucesso", async () => {
        const mockCall = vi.fn().mockResolvedValue("resultado");
        const {data, isLoading, error, execute} = useApi(mockCall);

        expect(isLoading.value).toBe(false);
        const promise = execute("arg1");
        expect(isLoading.value).toBe(true);

        await promise;

        expect(isLoading.value).toBe(false);
        expect(data.value).toBe("resultado");
        expect(error.value).toBe(null);
        expect(mockCall).toHaveBeenCalledWith("arg1");
    });

    it("deve lidar com erro na chamada", async () => {
        const mockCall = vi.fn().mockRejectedValue(new Error("falha"));
        const {data, isLoading, error, normalizedError, execute} = useApi(mockCall);

        try {
            await execute();
        } catch (e) {
            // expected
        }

        expect(isLoading.value).toBe(false);
        expect(data.value).toBe(null);
        expect(error.value).toBe("falha");
        expect(normalizedError.value?.message).toBe("falha");
    });

    it("deve limpar erro", async () => {
        const mockCall = vi.fn().mockRejectedValue(new Error("erro"));
        const {error, execute, clearError} = useApi(mockCall);

        try {
            await execute();
        } catch (e) {}

        expect(error.value).toBe("erro");
        clearError();
        expect(error.value).toBe(null);
    });
});
