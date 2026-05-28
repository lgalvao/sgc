import {useErrorHandler} from "@/composables/useErrorHandler";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";
import type {RouteLocationRaw} from "vue-router";
import {useRouter} from "vue-router";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";

export interface WorkflowSubprocesso {
    mensagemSucesso?: string;
    redirecionarParaPainel?: boolean;
    redirecionarPara?: RouteLocationRaw;
    atualizarFluxoMapa?: number;
    recarregarContexto?: {
        codigoSubprocesso: number;
        tipo: "cadastro" | "edicao";
    };
}

async function recarregarContextoSubprocesso(
    subprocessoStore: ReturnType<typeof useSubprocessoStore>,
    recarga: NonNullable<WorkflowSubprocesso["recarregarContexto"]>,
) {
    if (recarga.tipo === "cadastro") {
        await subprocessoStore.recarregarContextoCadastroAtividades(recarga.codigoSubprocesso);
        return;
    }

    await subprocessoStore.recarregarContextoEdicao(recarga.codigoSubprocesso);
}

export type ExecucaoFluxoSubprocesso = ReturnType<typeof useFluxoSubprocessoExecucao>;

export function useFluxoSubprocessoExecucao() {
    const {ultimoErro, limparErro, executarComTratamentoDeErros} = useErrorHandler();
    const subprocessoStore = useSubprocessoStore();
    const toastStore = useToastStore();
    const router = useRouter();
    const {atualizarFluxoMapa, limparEstadoSubprocessoAtual} = useInvalidacaoNavegacao();

    async function executarAcaoWorkflow(
        acao: () => Promise<unknown>,
        workflow: WorkflowSubprocesso = {},
    ): Promise<boolean> {
        try {
            await executarComTratamentoDeErros(async () => {
                await acao();

                if (workflow.mensagemSucesso) {
                    toastStore.setPending(workflow.mensagemSucesso);
                }

                if (typeof workflow.atualizarFluxoMapa === "number") {
                    atualizarFluxoMapa(workflow.atualizarFluxoMapa);
                }

                if (workflow.redirecionarParaPainel) {
                    limparEstadoSubprocessoAtual();
                    await router.push("/painel");
                    return;
                }

                if (workflow.redirecionarPara) {
                    limparEstadoSubprocessoAtual();
                    await router.push(workflow.redirecionarPara);
                    return;
                }

                if (workflow.recarregarContexto) {
                    await recarregarContextoSubprocesso(subprocessoStore, workflow.recarregarContexto);
                }
            });

            return true;
        } catch {
            return false;
        }
    }

    return {
        executarAcaoWorkflow,
        executarComTratamentoDeErros,
        limparErro,
        ultimoErro,
    };
}
