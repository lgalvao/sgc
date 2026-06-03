// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst

import {createBootstrap, vBTooltip} from "bootstrap-vue-next"; // Importar createBootstrap e vBTooltip
import {PiniaColada} from "@pinia/colada";
import {createPinia} from "pinia";
import {createApp} from "vue";
import {isErroCanceladoHttp, setRouter} from "@/axios-setup";
import {normalizarErro} from "@/utils/apiError";
import logger from "@/utils/logger";
import App from "./App.vue";
import router from "./router/index";

import "bootstrap-vue-next/dist/bootstrap-vue-next.css";
import "bootstrap/dist/css/bootstrap.min.css"; // Manter se houver estilos customizados
import "bootstrap-icons/font/bootstrap-icons.css";

declare global {
    var pinia: ReturnType<typeof createPinia>;
    var __pinia__: ReturnType<typeof createPinia>;
}

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(PiniaColada);

globalThis.pinia = pinia;

app.use(router);
setRouter(router);

// Erros não tratados em componentes Vue: redireciona para /erro se não tiver solução.
// Erros recuperáveis (validacao, conflito, proibido) são relançados para tratamento local nas views.
// Cancelamentos e naoAutorizado já são tratados pelo interceptor Axios.
const TIPOS_SEM_SOLUCAO = new Set(['rede', 'inesperado']);

app.config.errorHandler = (err) => {
    if (isErroCanceladoHttp(err)) return;
    const normalizado = normalizarErro(err);
    if (normalizado.tipo === 'naoAutorizado') return;
    if (TIPOS_SEM_SOLUCAO.has(normalizado.tipo)) {
        logger.error('[errorHandler]', normalizado.mensagem, err);
        void router.push('/erro').catch(() => {/* navegação já em /erro */});
        return;
    }
    // Erro recuperável não tratado localmente — relança para não suprimir silenciosamente
    throw err;
};

app.use(createBootstrap());
app.directive('b-tooltip', vBTooltip);

if (import.meta.env.VITE_FEEDBACK_WIDGET === 'true') {
    const {default: FeedbackWidget} = await import('@/components/feedback/FeedbackWidget.vue');
    const container = document.createElement('div');
    document.body.appendChild(container);
    const widgetApp = createApp(FeedbackWidget);
    widgetApp.use(pinia);
    widgetApp.use(router);
    widgetApp.use(createBootstrap());
    widgetApp.directive('b-tooltip', vBTooltip);
    widgetApp.mount(container);
}

app.mount("#app");

globalThis.__pinia__ = pinia;
