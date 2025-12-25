import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {useFeedbackStore} from "@/stores/feedback";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";

describe("Feedback Store", () => {
    const context = setupStoreTest(useFeedbackStore);

    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it("deve ter o estado inicial correto", () => {
        expect(context.store.currentFeedback).toEqual({
            title: "",
            message: "",
            variant: "info",
            show: false,
        });
    });

    it("deve mostrar feedback corretamente", () => {
        context.store.show("Sucesso", "Operação realizada", "success");

        expect(context.store.currentFeedback).toEqual({
            title: "Sucesso",
            message: "Operação realizada",
            variant: "success",
            show: true,
            autoHideDelay: 5000,
        });
    });

    it("deve fechar feedback", () => {
        context.store.show("Info", "Teste");
        expect(context.store.currentFeedback.show).toBe(true);

        context.store.close();
        expect(context.store.currentFeedback.show).toBe(false);
    });

    it("deve fechar automaticamente após o delay", () => {
        context.store.show("Info", "Teste", "info", 3000);

        expect(context.store.currentFeedback.show).toBe(true);

        vi.advanceTimersByTime(3000);

        expect(context.store.currentFeedback.show).toBe(false);
    });

    it("deve limpar timer anterior ao mostrar novo feedback", () => {
        // Primeiro feedback que fecharia em 5s
        context.store.show("Primeiro", "Msg 1", "info", 5000);

        // Avança 2s
        vi.advanceTimersByTime(2000);

        // Mostra segundo feedback que fecha em 5s
        context.store.show("Segundo", "Msg 2", "warning", 5000);

        // Avança mais 3s (total 5s desde o primeiro). O primeiro DEVERIA fechar aqui se não fosse cancelado.
        vi.advanceTimersByTime(3000);

        // Como foi cancelado, não deve ter fechado ainda (pois o segundo só fecha daqui a 2s)
        expect(context.store.currentFeedback.title).toBe("Segundo");
        expect(context.store.currentFeedback.show).toBe(true);

        // Avança mais 2s (total 5s desde o segundo)
        vi.advanceTimersByTime(2000);
        expect(context.store.currentFeedback.show).toBe(false);
    });

    it("não deve setar timeout se delay for 0", () => {
        context.store.show("Fixo", "Não some", "danger", 0);

        vi.advanceTimersByTime(10000);
        expect(context.store.currentFeedback.show).toBe(true);
    });
});
