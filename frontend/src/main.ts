import {createApp} from "vue";
import App from "./App.vue";
import router from "./router/index";
import {createPinia} from "pinia";
import "./style.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";

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

app.mount('#app');

// Expose Pinia instance globally for Playwright testing (optional, for dev/test environments)
window.__pinia__ = pinia;