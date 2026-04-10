import axios, {AxiosHeaders} from "axios";
import type {AxiosResponse, InternalAxiosRequestConfig} from "axios";
import type {Router} from "vue-router";
import {normalizeError, shouldNotifyGlobally} from '@/utils/apiError';
import {logger} from "@/utils";

let routerInstance: Router | null = null;
const CHAVE_MONITORAMENTO_SESSAO = 'sgc.monitoramento.ativo';
const MODO_MONITORAMENTO = import.meta.env?.VITE_MONITORAMENTO_MODO || 'off';

type MetadadosMonitoramento = {
    correlacaoId: string;
    inicioMs: number;
    monitoramentoAtivo: boolean;
};

type ConfiguracaoMonitorada = InternalAxiosRequestConfig & {
    metadadosMonitoramento?: MetadadosMonitoramento;
};

export function setRouter(router: Router) {
    routerInstance = router;
}

export function ativarMonitoramentoHttp() {
    if (typeof window !== 'undefined') {
        window.sessionStorage.setItem(CHAVE_MONITORAMENTO_SESSAO, 'true');
    }
}

export function desativarMonitoramentoHttp() {
    if (typeof window !== 'undefined') {
        window.sessionStorage.removeItem(CHAVE_MONITORAMENTO_SESSAO);
    }
}

function isMonitoramentoSolicitadoPorSessao(): boolean {
    if (typeof window === 'undefined') {
        return false;
    }
    return window.sessionStorage.getItem(CHAVE_MONITORAMENTO_SESSAO) === 'true';
}

function isMonitoramentoSolicitadoPorUrl(): boolean {
    if (typeof window === 'undefined') {
        return false;
    }
    return new URLSearchParams(window.location.search).get('monitoramento') === '1';
}

function isMonitoramentoAtivo(): boolean {
    return MODO_MONITORAMENTO === 'full'
        || isMonitoramentoSolicitadoPorSessao()
        || isMonitoramentoSolicitadoPorUrl();
}

function gerarCorrelacaoId(): string {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
        return crypto.randomUUID();
    }
    return `corr-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function definirHeader(config: ConfiguracaoMonitorada, nome: string, valor: string) {
    if (!config.headers) {
        config.headers = new AxiosHeaders();
    }

    if (typeof config.headers.set === 'function') {
        config.headers.set(nome, valor);
        return;
    }

    config.headers = AxiosHeaders.from(config.headers);
    config.headers.set(nome, valor);
}

function calcularDuracao(inicioMs?: number): number {
    if (inicioMs === undefined) {
        return 0;
    }
    return Math.round(performance.now() - inicioMs);
}

function registrarConclusaoResposta(response: AxiosResponse) {
    const config = (response.config || {}) as ConfiguracaoMonitorada;
    const metadados = config.metadadosMonitoramento;

    if (!metadados?.monitoramentoAtivo) {
        return response;
    }

    logger.info("[http] fim", {
        metodo: config.method?.toUpperCase(),
        url: config.url,
        status: response.status,
        duracaoMs: calcularDuracao(metadados.inicioMs),
        correlacaoId: metadados.correlacaoId,
        tempoServidorMs: response.headers["x-tempo-servidor-ms"],
        serverTiming: response.headers["server-timing"],
    });

    return response;
}

export const apiClient = axios.create({
    baseURL: import.meta.env?.VITE_API_BASE_URL || "/api",
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {
        "Content-type": "application/json",
    },
});

apiClient.interceptors.request.use((config: ConfiguracaoMonitorada) => {
    const monitoramentoAtivo = isMonitoramentoAtivo();
    const correlacaoId = gerarCorrelacaoId();

    config.metadadosMonitoramento = {
        correlacaoId,
        inicioMs: performance.now(),
        monitoramentoAtivo,
    };

    definirHeader(config, "X-Correlacao-Id", correlacaoId);

    if (monitoramentoAtivo) {
        definirHeader(config, "X-Monitoramento-Ativo", "true");
        logger.info("[http] inicio", {
            metodo: config.method?.toUpperCase(),
            url: config.url,
            correlacaoId,
        });
    }

    return config;
});

const handleResponseError = (error: unknown) => {
    const config = (error as {config?: ConfiguracaoMonitorada})?.config;
    const metadados = config?.metadadosMonitoramento;

    if (metadados?.monitoramentoAtivo) {
        logger.error("[http] erro", {
            metodo: config?.method?.toUpperCase(),
            url: config?.url,
            status: (error as {response?: {status?: number}})?.response?.status,
            duracaoMs: calcularDuracao(metadados.inicioMs),
            correlacaoId: metadados.correlacaoId,
        });
    }

    const normalized = normalizeError(error);

    // Caso especial: 401 - redirecionar para login
    if (normalized.kind === 'unauthorized') {
        const currentPath = routerInstance?.currentRoute?.value?.path;
        if (currentPath !== '/login') {
            routerInstance?.push('/login').catch(e => logger.error("Erro ao redirecionar:", e));
        }
        return Promise.reject(error);
    }

    // Loga globalmente erros de rede e inesperados para diagnóstico
    if (shouldNotifyGlobally(normalized)) {
        logger.error("[axios] Erro global:", normalized.message);
    }

    // Sempre rejeitar para permitir tratamento local
    return Promise.reject(error);
};

apiClient.interceptors.response.use(
    registrarConclusaoResposta,
    handleResponseError,
);

export default apiClient;
