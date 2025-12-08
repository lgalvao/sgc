import {describe, expect, it, vi} from "vitest";
import {useApi} from "../useApi";

describe("useApi", () => {
    it("should set isLoading to true while the api call is in progress", async () => {
        const apiCall = vi.fn(
            () => new Promise((resolve) => setTimeout(() => resolve("data"), 10)),
        );
    const { execute, isLoading } = useApi(apiCall);

    const promise = execute();

    expect(isLoading.value).toBe(true);

    await promise;

    expect(isLoading.value).toBe(false);
  });

    it("should set data on successful api call", async () => {
        const apiCall = vi.fn(() => Promise.resolve("data"));
    const { execute, data } = useApi(apiCall);

    await execute();

        expect(data.value).toBe("data");
  });

    it("should set error on failed api call", async () => {
        const apiCall = vi.fn(() =>
            Promise.reject({response: {data: {message: "error"}}}),
        );
    const { execute, error } = useApi(apiCall);

    try {
      await execute();
    } catch {
      // a
    }

        expect(error.value).toBe("error");
  });

    it("should clear error when clearError is called", async () => {
        const apiCall = vi.fn(() =>
            Promise.reject({response: {data: {message: "error"}}}),
        );
    const { execute, error, clearError } = useApi(apiCall);

    try {
      await execute();
    } catch {
      // a
    }

        expect(error.value).toBe("error");

    clearError();

    expect(error.value).toBe(null);
  });
});
