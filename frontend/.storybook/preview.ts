import {setup} from '@storybook/vue3-vite';
import {createPinia} from 'pinia';
import {createBootstrap, vBTooltip} from 'bootstrap-vue-next';
import {createRouter, createWebHistory} from 'vue-router';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue-next/dist/bootstrap-vue-next.css';
import 'bootstrap-icons/font/bootstrap-icons.css';

const pinia = createPinia();
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/', component: {template: '<div></div>'}},
        {path: '/login', component: {template: '<div></div>'}},
        {path: '/painel', component: {template: '<div></div>'}},
        {path: '/unidade/:codigo', component: {template: '<div></div>'}},
        {path: '/unidades', component: {template: '<div></div>'}},
        {path: '/relatorios', component: {template: '<div></div>'}},
        {path: '/historico', component: {template: '<div></div>'}},
        {path: '/configuracoes', component: {template: '<div></div>'}},
        {path: '/administradores', component: {template: '<div></div>'}},
        {path: '/administracao/notificacoes', component: {template: '<div></div>'}},
        {path: '/administracao/feedbacks', component: {template: '<div></div>'}},
        {path: '/administracao/limpeza-processos', component: {template: '<div></div>'}},
    ],
});

setup((app) => {
    app.use(pinia);
    app.use(router);
    app.use(createBootstrap());
    app.directive('b-tooltip', vBTooltip);
});

export const parameters = {
    actions: {argTypesRegex: "^on[A-Z].*"},
    controls: {
        matchers: {
            color: /(background|color)$/i,
            date: /Date$/,
        },
    },
};
