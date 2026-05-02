import {describe, expect, it} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import NotificacoesAdminView from "../NotificacoesAdminView.vue";
import ProcessoCadastroView from "../ProcessoCadastroView.vue";
import {createTestingPinia} from "@pinia/testing";
import {createMemoryHistory, createRouter} from "vue-router";

const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/', component: {} }]
});

const stubs = {
    LayoutPadrao: { template: '<div><slot/></div>' },
    PageHeader: { template: '<div><slot/></div>', props: ['title'] },
    BCard: { template: '<div><slot/></div>' },
    BCardBody: { template: '<div><slot/></div>' },
    BTable: { template: '<table><slot name="cell(acoes)"/></table>', props: ['items', 'fields'] },
    BButton: { template: '<button @click="$emit(\'click\')"><slot/></button>' },
    BModal: { template: '<div><slot/></div>', props: ['modelValue'] },
    BForm: { template: '<form><slot/></form>' },
    BFormGroup: { template: '<div><slot/></div>' },
    BFormInput: { template: '<input/>' },
    BFormSelect: { template: '<select/>' },
    BFormTextarea: { template: '<textarea/>' },
    BSpinner: { template: '<div></div>' },
    AppAlert: { template: '<div></div>' },
};

describe("Admin Views Basic Coverage", () => {
    it("deve montar NotificacoesAdminView", async () => {
        const wrapper = mount(NotificacoesAdminView, {
            global: {
                plugins: [createTestingPinia(), router],
                stubs
            }
        });
        await flushPromises();
        expect(wrapper.exists()).toBe(true);
    });

    it("deve montar ProcessoCadastroView", async () => {
        const wrapper = mount(ProcessoCadastroView, {
            global: {
                plugins: [createTestingPinia(), router],
                stubs
            }
        });
        await flushPromises();
        expect(wrapper.exists()).toBe(true);
    });
});
