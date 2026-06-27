import {useToast as useToastBootstrap} from "bootstrap-vue-next";
import {useToastStore, type ToastPendente} from "@/stores/toast";

type VarianteToast = NonNullable<ToastPendente["variante"]>;

const DURACAO_PADRAO: Record<VarianteToast, number> = {
    success: 4000,
    danger: 5000,
    warning: 4500,
    info: 4000,
};

export function useToast() {
    const {create: criarToast} = useToastBootstrap();
    const toastStore = useToastStore();

    function exibirToast(
        mensagem: string,
        variante: VarianteToast = "success",
    ) {
        void criarToast({
            props: {
                body: mensagem,
                variant: variante,
                modelValue: DURACAO_PADRAO[variante],
                pos: "bottom-end",
                noProgress: true,
            },
        });
    }

    function exibirSucesso(mensagem: string) {
        exibirToast(mensagem, "success");
    }

    function exibirErro(mensagem: string) {
        exibirToast(mensagem, "danger");
    }

    function registrarPendente(
        mensagem: string,
        variante: VarianteToast = "success",
    ) {
        toastStore.setPending(mensagem, variante);
    }

    function exibirPendente() {
        const pendente = toastStore.consumePending();
        if (!pendente) {
            return false;
        }
        exibirToast(pendente.mensagem, pendente.variante ?? "success");
        return true;
    }

    return {
        exibirToast,
        exibirSucesso,
        exibirErro,
        registrarPendente,
        exibirPendente,
    };
}
