// noinspection JSUnusedGlobalSymbols

import {createBootstrap, vBTooltip} from "bootstrap-vue-next"; // Importar createBootstrap e vBTooltip
import {createPinia} from "pinia";
import {createApp} from "vue";
import App from "./App.vue";
import router from "./router/index";

import "./style.css";
import "bootstrap-vue-next/dist/bootstrap-vue-next.css";
import "bootstrap/dist/css/bootstrap.min.css"; // Manter se houver estilos customizados
import "bootstrap-icons/font/bootstrap-icons.css";

declare global {
    interface Window {
        pinia: ReturnType<typeof createPinia>;
        __pinia__: ReturnType<typeof createPinia>;
    }
}

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);

window.pinia = pinia;

app.use(router);
app.use(createBootstrap());
app.directive('b-tooltip', vBTooltip);
app.mount("#app");

window.__pinia__ = pinia;
