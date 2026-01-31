import { ref } from "vue";
import { useRouter } from "vue-router";
import { useSubprocessosStore } from "@/stores/subprocessos";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
} from "@/types/tipos";

export function useVisAtividadesCrud() {
    const router = useRouter();
    const subprocessosStore = useSubprocessosStore();

    const loadingValidacao = ref(false);
    const loadingDevolucao = ref(false);

    async function confirmarValidacao(
        codSubprocesso: number | undefined,
        isHomologacao: boolean,
        isRevisao: boolean,
        observacaoValidacao: string,
        props: { codProcesso: number | string; sigla: string },
        fecharModal: () => void
    ) {
        if (!codSubprocesso) return;

        loadingValidacao.value = true;
        try {
            let sucesso: boolean;

            if (isHomologacao) {
                const req: HomologarCadastroRequest = {
                    observacoes: observacaoValidacao,
                };
                if (isRevisao) {
                    sucesso = await subprocessosStore.homologarRevisaoCadastro(codSubprocesso, req);
                } else {
                    sucesso = await subprocessosStore.homologarCadastro(codSubprocesso, req);
                }

                if (sucesso) {
                    fecharModal();
                    await router.push({
                        name: "Subprocesso",
                        params: {
                            codProcesso: props.codProcesso,
                            siglaUnidade: props.sigla,
                        },
                    });
                }
            } else {
                const req: AceitarCadastroRequest = {
                    observacoes: observacaoValidacao,
                };
                if (isRevisao) {
                    sucesso = await subprocessosStore.aceitarRevisaoCadastro(codSubprocesso, req);
                } else {
                    sucesso = await subprocessosStore.aceitarCadastro(codSubprocesso, req);
                }

                if (sucesso) {
                    fecharModal();
                    await router.push({ name: "Painel" });
                }
            }
        } finally {
            loadingValidacao.value = false;
        }
    }

    async function confirmarDevolucao(
        codSubprocesso: number | undefined,
        isRevisao: boolean,
        observacaoDevolucao: string,
        fecharModal: () => void
    ) {
        if (!codSubprocesso) return;
        loadingDevolucao.value = true;

        try {
            const req: DevolverCadastroRequest = {
                observacoes: observacaoDevolucao,
            };

            let sucesso: boolean;
            if (isRevisao) {
                sucesso = await subprocessosStore.devolverRevisaoCadastro(codSubprocesso, req);
            } else {
                sucesso = await subprocessosStore.devolverCadastro(codSubprocesso, req);
            }

            if (sucesso) {
                fecharModal();
                await router.push("/painel");
            }
        } finally {
            loadingDevolucao.value = false;
        }
    }

    return {
        loadingValidacao,
        loadingDevolucao,
        confirmarValidacao,
        confirmarDevolucao,
    };
}
