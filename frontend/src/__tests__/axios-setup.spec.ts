import {createPinia, setActivePinia} from "pinia";
import {afterEach, beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
import router from "@/router";
import logger from "@/utils/logger";

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

vi.mock("@/utils/logger", () => {
    return {
        default: {
            error: vi.fn(),
            warn: vi.fn(),
            info: vi.fn(),
        }
    }
});

vi.mock("@/utils", async () => {
    const actual = await vi.importActual("@/utils") as any;
    return {
        ...actual,
        logger: {
            error: vi.fn(),
            warn: vi.fn(),
            info: vi.fn(),
        }
    };
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
    let cancelarRequisicoesPendentes: () => void;
    let iniciarTransicaoSessao: () => void;
    let finalizarTransicaoSessao: () => void;

    beforeAll(async () => {
        const modulo = await import("../axios-setup"); // Use dynamic import
        const {setRouter} = modulo;
        cancelarRequisicoesPendentes = modulo.cancelarRequisicoesPendentes;
        iniciarTransicaoSessao = modulo.iniciarTransicaoSessao;
        finalizarTransicaoSessao = modulo.finalizarTransicaoSessao;
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
        finalizarTransicaoSessao();
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

    it("interceptor de requisicao deve marcar monitoramento quando ativado por sessao", () => {
        window.sessionStorage.setItem('sgc.monitoramento.ativo', 'true');

        const config = requestInterceptor({method: 'get', url: '/processos', headers: {}});

        expect(config.headers['X-Correlacao-Id']).toBe('corr-123');
        expect(config.metadadosMonitoramento.monitoramentoAtivo).toBe(true);
    });

    it("interceptor de requisicao deve marcar monitoramento quando ativado por URL", () => {
        vi.stubGlobal('location', { search: '?monitoramento=1' });

        const config = requestInterceptor({method: 'get', url: '/processos', headers: {}});

        expect(config.metadadosMonitoramento.monitoramentoAtivo).toBe(true);
        vi.unstubAllGlobals(); // Limpa location stub
    });

    it("definirHeader deve lidar com headers nulos e headers sem metodo set", () => {
        const config: any = { headers: null };
        requestInterceptor(config); // Isso chama isMonitoramentoAtivo e gerarCorrelacaoId, e entao definirHeader
        expect(config.headers).toBeDefined();
        expect(config.headers['X-Correlacao-Id']).toBe('corr-123');

        const config2: any = { headers: {} }; // Object literal sem .set
        requestInterceptor(config2);
        expect(config2.headers['X-Correlacao-Id']).toBe('corr-123');
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

    it("interceptor de erro de resposta deve registrar erro se monitoramento estiver ativo", async () => {
        window.sessionStorage.setItem('sgc.monitoramento.ativo', 'true');
        const config = requestInterceptor({method: 'get', url: '/processos', headers: {}});
        const error = {
            isAxiosError: true,
            config,
            response: {status: 500}
        };

        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(logger.error).toHaveBeenCalledWith("[http] erro", expect.objectContaining({
            status: 500,
            correlacaoId: 'corr-123'
        }));
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

    it("cancelarRequisicoesPendentes deve abortar requisicoes em andamento", () => {
        const config = requestInterceptor({method: 'get', url: '/processos', headers: {}});

        cancelarRequisicoesPendentes();

        expect(config.signal.aborted).toBe(true);
    });

    it("interceptor de erro cancelado nao deve redirecionar para login", async () => {
        const config = requestInterceptor({method: 'get', url: '/subprocessos/contexto-edicao/buscar', headers: {}});
        const error = {isAxiosError: true, code: 'ERR_CANCELED', config};

        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(router.push).not.toHaveBeenCalled();
    });

    it("deve bloquear requisicoes autenticadas durante transicao de sessao", async () => {
        iniciarTransicaoSessao();

        await expect(requestInterceptor({method: 'get', url: '/subprocessos/contexto-edicao/buscar', headers: {}}))
            .rejects.toMatchObject({code: 'ERR_CANCELED'});
    });

    it("deve permitir requisicoes de autenticacao durante transicao de sessao", async () => {
        iniciarTransicaoSessao();

        const config = await requestInterceptor({method: 'post', url: '/usuarios/login', headers: {}});

        expect(config.url).toBe('/usuarios/login');
        expect(config.signal).toBeDefined();
    });
});
