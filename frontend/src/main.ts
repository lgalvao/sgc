import {createBootstrap, vBTooltip} from "bootstrap-vue-next"; // Importar createBootstrap e vBTooltip
import {createPinia} from "pinia";
import {createApp} from "vue";
import App from "./App.vue";
import router from "./router/index";

import "./style.css";
import "bootstrap-vue-next/dist/bootstrap-vue-next.css"; // Importar o CSS da BootstrapVueNext
import "bootstrap/dist/css/bootstrap.min.css"; // Manter se houver estilos customizados
import "bootstrap-icons/font/bootstrap-icons.css";
// Remover "bootstrap/dist/js/bootstrap.bundle.min.js"; pois BootstrapVueNext gerencia o JS

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

// Registrar o plugin do BootstrapVueNext
app.use(createBootstrap());

// Registrar a diretiva de tooltip globalmente
app.directive('b-tooltip', vBTooltip);

app.mount("#app");

window.__pinia__ = pinia;
