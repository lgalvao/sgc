import type {AxiosResponse, InternalAxiosRequestConfig} from "axios";
import axios, {AxiosHeaders} from "axios";
import type {Router} from "vue-router";
import {normalizeError, shouldNotifyGlobally} from '@/utils/apiError';
import logger from "@/utils/logger";

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
    controladorCancelamento?: AbortController;
};

type ErroCanceladoHttp = {
    code?: string;
    name?: string;
};

const controladoresPendentes = new Set<AbortController>();
let sessaoEmTransicao = false;

export function setRouter(router: Router) {
    routerInstance = router;
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
    return MODO_MONITORAMENTO === 'on'
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

function registrarControleCancelamento(config: ConfiguracaoMonitorada) {
    if (config.signal) {
        return;
    }

    const controlador = new AbortController();
    config.signal = controlador.signal;
    config.controladorCancelamento = controlador;
    controladoresPendentes.add(controlador);
}

function limparControleCancelamento(config?: ConfiguracaoMonitorada) {
    const controlador = config?.controladorCancelamento;
    if (!controlador) {
        return;
    }

    controladoresPendentes.delete(controlador);
    delete config.controladorCancelamento;
}

export function cancelarRequisicoesPendentes() {
    controladoresPendentes.forEach((controlador) => controlador.abort());
    controladoresPendentes.clear();
}

export function iniciarTransicaoSessao() {
    sessaoEmTransicao = true;
    cancelarRequisicoesPendentes();
}

export function finalizarTransicaoSessao() {
    sessaoEmTransicao = false;
}

export function isErroCanceladoHttp(error: unknown): boolean {
    if (typeof error !== "object" || error === null) {
        return false;
    }

    const erroCancelado = error as ErroCanceladoHttp;
    return erroCancelado.code === "ERR_CANCELED" || erroCancelado.name === "CanceledError";
}

function isRequisicaoPermitidaDuranteTransicao(url?: string): boolean {
    if (!url) {
        return false;
    }

    return url.includes("/usuarios/login")
        || url.includes("/usuarios/entrar")
        || url.includes("/usuarios/logout");
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
    limparControleCancelamento(config);

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

const apiClient = axios.create({
    baseURL: import.meta.env?.VITE_API_BASE_URL || "/api",
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {
        "Content-type": "application/json",
    },
});

apiClient.interceptors.request.use((config: ConfiguracaoMonitorada) => {
    if (sessaoEmTransicao && !isRequisicaoPermitidaDuranteTransicao(config.url)) {
        return Promise.reject({
            isAxiosError: true,
            code: "ERR_CANCELED",
            name: "CanceledError",
            config,
        });
    }

    const monitoramentoAtivo = isMonitoramentoAtivo();
    const correlacaoId = gerarCorrelacaoId();
    registrarControleCancelamento(config);

    config.metadadosMonitoramento = {
        correlacaoId,
        inicioMs: performance.now(),
        monitoramentoAtivo,
    };

    definirHeader(config, "X-Correlacao-Id", correlacaoId);

    if (monitoramentoAtivo) {
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
    limparControleCancelamento(config);

    if (isErroCanceladoHttp(error)) {
        return Promise.reject(error);
    }

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
