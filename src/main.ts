import {createApp} from "vue";
import App from "./App.vue";
import router from "./router";
import {createPinia} from "pinia";
import "./style.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);

// @ts-ignore
window.pinia = pinia;

app.use(router);
// @ts-ignore
app.mount('#app')

// Expose Pinia instance globally for Playwright testing (optional, for dev/test environments)
(window as any).__pinia__ = pinia;