import {createApp} from "vue";
import App from "./App.vue";
import router from "./router/index";
import {createPinia} from "pinia";
import {createBootstrap} from 'bootstrap-vue-next'

import 'bootstrap-vue-next/dist/bootstrap-vue-next.css'
import "./style.css";
import "bootstrap/dist/css/bootstrap.min.css";
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
app.use(createBootstrap())

app.mount('#app');

// Expose Pinia instance globally for Playwright testing (optional, for dev/test environments)
window.__pinia__ = pinia;