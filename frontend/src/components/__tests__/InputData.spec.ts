import {mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import InputData from "@/components/comum/InputData.vue";
import {logger, obterHojeFormatado} from "@/utils";
import {flattenTree} from "@/utils/treeUtils";

const BFormInputStub = {
    template: `
        <input
            :id="id"
            :value="modelValue"
            :data-testid="dataTestid"
            :aria-required="ariaRequired"
            :max="max"
            :min="min"
            :class="customClass"
            @input="$emit('update:model-value', $event.target.value)"
        />
    `,
    props: ["id", "modelValue", "dataTestid", "ariaRequired", "max", "min", "customClass", "state"],
    emits: ["update:model-value"],
};

describe("InputData.vue", () => {
    const criarWrapper = (props = {}) => mount(InputData, {
        props: {
            modelValue: "2026-03-27",
            ...props,
        },
        global: {
            stubs: {
                BInputGroup: {template: "<div><slot /></div>"},
                BInputGroupText: {
                    template: `
                        <button
                            type="button"
                            :aria-label="ariaLabel"
                            :role="role"
                            :tabindex="tabindex"
                            @click="$emit('click')"
                            @keydown.enter="$emit('keydown.enter')"
                            @keydown.space="$emit('keydown.space')"
                        >
                            <slot />
                        </button>
                    `,
                    props: ["ariaLabel", "role", "tabindex"],
                    emits: ["click", "keydown.enter", "keydown.space"]
                },
                BFormInput: BFormInputStub,
            },
        },
    });

    it("deve possuir atributos de acessibilidade no ícone do calendário", () => {
        const wrapper = criarWrapper();
        const trigger = wrapper.find("button");

        expect(trigger.attributes("aria-label")).toBe("Abrir calendário");
        expect(trigger.attributes("role")).toBe("button");
        expect(trigger.attributes("tabindex")).toBe("0");
    });

    it("deve abrir o seletor nativo ao pressionar Enter ou Espaço", async () => {
        const wrapper = criarWrapper({dataTestid: "input-data"});
        const input = wrapper.get('[data-testid="input-data"]').element as HTMLInputElement;
        const showPicker = vi.fn();

        Object.defineProperty(input, "showPicker", {
            value: showPicker,
            configurable: true,
        });

        const trigger = wrapper.find("button");

        await trigger.trigger("keydown.enter");
        expect(showPicker).toHaveBeenCalledTimes(1);

        await trigger.trigger("keydown.space");
        expect(showPicker).toHaveBeenCalledTimes(2);
    });

    it("deve repassar props para o input", () => {
        const wrapper = criarWrapper({
            id: "data-inicio",
            required: true,
            max: "2026-12-31",
            min: "2026-01-01",
            dataTestid: "input-data",
            inputClass: "campo-data",
        });

        const input = wrapper.get('[data-testid="input-data"]');

        expect(input.attributes("id")).toBe("data-inicio");
        expect(input.attributes("aria-required")).toBe("true");
        expect(input.attributes("max")).toBe("2026-12-31");
        expect(input.attributes("min")).toBe("2026-01-01");
        expect(input.classes()).toContain("campo-data");
    });

    it("deve emitir update:modelValue ao alterar o campo", async () => {
        const wrapper = criarWrapper({dataTestid: "input-data"});

        await wrapper.get('[data-testid="input-data"]').setValue("2026-04-01");

        expect(wrapper.emitted("update:modelValue")).toEqual([["2026-04-01"]]);
    });

    it("deve abrir o seletor nativo ao clicar no ícone quando showPicker existir", async () => {
        const wrapper = criarWrapper({dataTestid: "input-data"});
        const input = wrapper.get('[data-testid="input-data"]').element as HTMLInputElement;
        const showPicker = vi.fn();

        Object.defineProperty(input, "showPicker", {
            value: showPicker,
            configurable: true,
        });

        await wrapper.get("button").trigger("click");

        expect(showPicker).toHaveBeenCalledTimes(1);
    });

    it("deve focar o input quando showPicker não existir", async () => {
        const wrapper = criarWrapper({dataTestid: "input-data"});
        const input = wrapper.get('[data-testid="input-data"]').element as HTMLInputElement;
        const focusSpy = vi.spyOn(input, "focus");

        await wrapper.get("button").trigger("click");

        expect(focusSpy).toHaveBeenCalledTimes(1);
    });

    it("deve expor método focus para uso externo", () => {
        const wrapper = criarWrapper({dataTestid: "input-data"});
        const input = wrapper.get('[data-testid="input-data"]').element as HTMLInputElement;
        const focusSpy = vi.spyOn(input, "focus");

        (wrapper.vm as unknown as { focus: () => void }).focus();

        expect(focusSpy).toHaveBeenCalledTimes(1);
    });

    it("deve manter os reexports do barrel de utils disponíveis", () => {
        expect(typeof logger.info).toBe("function");
        expect(obterHojeFormatado()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
        expect(flattenTree([{codigo: 1, subordinadas: [{codigo: 2}]}]).map((item: { codigo: number }) => item.codigo))
            .toEqual([1, 2]);
    });
});
