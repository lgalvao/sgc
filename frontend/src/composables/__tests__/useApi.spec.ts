import {describe, expect, it, vi} from "vitest";
import {useApi} from "../useApi";

describe("useApi", () => {
    it("deve definir isLoading como verdadeiro enquanto a chamada da API estiver em andamento", async () => {
        const apiCall = vi.fn(
            () => new Promise((resolve) => setTimeout(() => resolve("data"), 10)),
        );
        const {execute, isLoading} = useApi(apiCall);

        const promise = execute();

        expect(isLoading.value).toBe(true);

        await promise;

        expect(isLoading.value).toBe(false);
    });

    it("deve definir dados na chamada da API bem-sucedida", async () => {
        const apiCall = vi.fn(() => Promise.resolve("data"));
        const {execute, data} = useApi(apiCall);

        await execute();

        expect(data.value).toBe("data");
    });

    it("deve definir erro na chamada da API com falha", async () => {
        const apiCall = vi.fn(() =>
            Promise.reject({isAxiosError: true, response: {data: {message: "error"}}}),
        );
        const {execute, error} = useApi(apiCall);

        try {
            await execute();
        } catch {
            // Ignora erro esperado
        }

        expect(error.value).toBe("error");
    });

    it("deve limpar o erro quando clearError for chamado", async () => {
        const apiCall = vi.fn(() =>
            Promise.reject({isAxiosError: true, response: {data: {message: "error"}}}),
        );
        const {execute, error, clearError} = useApi(apiCall);

        try {
            await execute();
        } catch {
            // Ignora erro esperado
        }

        expect(error.value).toBe("error");

        clearError();

        expect(error.value).toBe(null);
    });
});
