import {beforeEach, describe, expect, it, vi} from "vitest";
import {useFeedbackStore} from "@/stores/feedback";
import {createPinia, setActivePinia} from "pinia";

describe("Feedback Store", () => {
    let store: ReturnType<typeof useFeedbackStore>;
    const mockToast = {
        create: vi.fn().mockReturnValue({
            destroy: vi.fn()
        }),
    };

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useFeedbackStore();
        vi.clearAllMocks();
    });

    it("deve enfileirar mensagens se não inicializado", () => {
        store.show("Teste", "Mensagem", "info");
        
        expect(mockToast.create).not.toHaveBeenCalled();
    });

    it("deve disparar toast se já inicializado", () => {
        store.init(mockToast);
        store.show("Sucesso", "Operação realizada", "success", 2000);

        expect(mockToast.create).toHaveBeenCalledTimes(1);
        expect(mockToast.create).toHaveBeenCalledWith(
            expect.objectContaining({
                props: expect.objectContaining({
                    title: "Sucesso",
                    body: "Operação realizada",
                    variant: "success",
                    value: 2000,
                    pos: "top-right",
                    noProgress: true,
                })
            })
        );
    });

    it("deve processar fila ao inicializar", () => {
        // Chamada antes do init
        store.show("Fila 1", "Msg 1");
        store.show("Fila 2", "Msg 2");
        
        expect(mockToast.create).not.toHaveBeenCalled();

        // Inicializa
        store.init(mockToast);

        // Only the first queued message should be created due to debounce
        expect(mockToast.create).toHaveBeenCalledTimes(1);
        
        // Verifica argumentos da primeira chamada
        expect(mockToast.create).toHaveBeenNthCalledWith(1, expect.objectContaining({
            props: expect.objectContaining({
                title: "Fila 1"
            })
        }));
    });
    
    it("deve usar defaults corretamente", () => {
        store.init(mockToast);
        store.show("Título", "Mensagem"); // Sem variant e delay
        
        expect(mockToast.create).toHaveBeenCalledWith(expect.objectContaining({
            props: expect.objectContaining({
                variant: 'info',
                value: 3000
            })
        }));
    });

    it("método close deve ser seguro para chamar e não fazer nada (ou o que for definido)", () => {
        expect(() => store.close()).not.toThrow();
    });

    it("deve ignorar chamadas subsequentes enquanto um toast está ativo (debounce)", () => {
        store.init(mockToast);
        
        // Primeiro toast
        store.show("Toast 1", "Mensagem 1");
        
        // Segundo toast (should be ignored due to debounce)
        store.show("Toast 2", "Mensagem 2");
        
        // Only the first toast should be created
        expect(mockToast.create).toHaveBeenCalledTimes(1);
        expect(mockToast.create).toHaveBeenCalledWith(
            expect.objectContaining({
                props: expect.objectContaining({
                    title: "Toast 1"
                })
            })
        );
    });
});
