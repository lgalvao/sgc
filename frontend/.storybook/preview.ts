import {setup} from '@storybook/vue3-vite';
import {createPinia} from 'pinia';
import {createBootstrap} from 'bootstrap-vue-next';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue-next/dist/bootstrap-vue-next.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import '../src/assets/css/tokens.css';
import '../src/assets/css/responsividade.css';
import '../src/style.css';

const pinia = createPinia();

setup((app) => {
    app.use(pinia);
    app.use(createBootstrap());
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
