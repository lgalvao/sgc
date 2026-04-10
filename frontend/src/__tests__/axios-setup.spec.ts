import {createPinia, setActivePinia} from "pinia";
import {afterEach, beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
import router from "@/router";
import {logger} from "@/utils";

// Hoist mock instance so it's shared between module and test
const {mockInstance} = vi.hoisted(() => {
    return {
        mockInstance: {
            interceptors: {
                request: {use: vi.fn()},
                response: {use: vi.fn()},
            },
            defaults: {headers: {common: {}}},
            get: vi.fn(),
            post: vi.fn(),
            put: vi.fn(),
            delete: vi.fn(),
        },
    };
});

vi.mock("@/router", () => ({
    default: {
        push: vi.fn().mockResolvedValue(undefined),
        currentRoute: {
            value: {path: '/'}
        }
    },
}));

vi.mock("@/utils", () => {
    return {
        logger: {
            error: vi.fn(),
            warn: vi.fn(),
            info: vi.fn(),
        }
    }
});

vi.mock("axios", () => {
    class AxiosHeadersMock {
        private readonly valores: Record<string, string> = {};

        set(nome: string, valor: string) {
            this.valores[nome] = valor;
            (this as unknown as Record<string, string>)[nome] = valor;
        }

        static from(headers: Record<string, string>) {
            const instancia = new AxiosHeadersMock();
            Object.entries(headers || {}).forEach(([nome, valor]) => instancia.set(nome, valor));
            return instancia;
        }
    }

    return {
        AxiosHeaders: AxiosHeadersMock,
        default: {
            create: vi.fn(() => mockInstance),
        },
    };
});

describe("axios-setup", () => {
    let requestInterceptor: (config: any) => any;
    let responseSuccessInterceptor: (response: any) => any;
    let responseErrorInterceptor: (error: any) => any;

    beforeAll(async () => {
        const {setRouter} = await import("../axios-setup"); // Use dynamic import
        setRouter(router as any);

        const requestUseCalls = mockInstance.interceptors.request.use.mock.calls;
        const responseUseCalls = mockInstance.interceptors.response.use.mock.calls;

        if (requestUseCalls.length > 0) {
            requestInterceptor = requestUseCalls[0][0];
        }

        if (responseUseCalls.length > 0) {
            responseSuccessInterceptor = responseUseCalls[0][0];
            responseErrorInterceptor = responseUseCalls[0][1];
        }
    }, 10000); 

    beforeEach(async () => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
        window.sessionStorage.clear();
        vi.spyOn(performance, 'now')
            .mockReturnValueOnce(100)
            .mockReturnValue(250);
        vi.stubGlobal('crypto', {
            randomUUID: vi.fn(() => 'corr-123'),
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("interceptor de erro de resposta deve redirecionar para login em caso de 401", async () => {
        const error = {isAxiosError: true, response: {status: 401, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(router.push).toHaveBeenCalledWith("/login");
    });

    it("interceptor de erro de resposta não deve redirecionar para login em caso de 401 se já estiver no login", async () => {
        (router.currentRoute.value as any).path = '/login';
        const error = {isAxiosError: true, response: {status: 401, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(router.push).not.toHaveBeenCalled();
    });

    it("interceptor de resposta bem-sucedida deve retornar a resposta", () => {
        const response = {data: 'test'};
        expect(responseSuccessInterceptor(response)).toBe(response);
    });

    it("interceptor de requisicao deve incluir headers de monitoramento quando ativado por sessao", () => {
        window.sessionStorage.setItem('sgc.monitoramento.ativo', 'true');

        const config = requestInterceptor({method: 'get', url: '/processos', headers: {}});

        expect(config.headers['X-Correlacao-Id']).toBe('corr-123');
        expect(config.headers['X-Monitoramento-Ativo']).toBe('true');
        expect(config.metadadosMonitoramento.monitoramentoAtivo).toBe(true);
    });

    it("interceptor de resposta bem-sucedida deve registrar duracao quando monitoramento estiver ativo", () => {
        window.sessionStorage.setItem('sgc.monitoramento.ativo', 'true');
        const config = requestInterceptor({method: 'get', url: '/processos', headers: {}});
        const response = {
            status: 200,
            headers: {'x-tempo-servidor-ms': '90', 'server-timing': 'app;dur=90'},
            config,
        };

        expect(responseSuccessInterceptor(response)).toBe(response);
        expect(logger.info).toHaveBeenCalledWith("[http] fim", expect.objectContaining({
            correlacaoId: 'corr-123',
            duracaoMs: 150,
        }));
    });


    it("interceptor de erro de resposta não deve redirecionar para 400, 404, 409, 422", async () => {
        const error = {isAxiosError: true, response: {status: 400, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(router.push).not.toHaveBeenCalled();
    });

    it("interceptor de erro de resposta deve propagar erro 500", async () => {
        const error = {
            isAxiosError: true,
            response: {status: 500, data: {message: "Server error"}},
            stack: 'stack'
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
    });

    it("interceptor de erro de resposta deve propagar erro de rede", async () => {
        const error = {isAxiosError: true, request: {}, message: "Network error"};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
    });

    it("interceptor de erro de resposta deve propagar erro genérico", async () => {
        const error = new Error("Generic failure");
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
    });
});
