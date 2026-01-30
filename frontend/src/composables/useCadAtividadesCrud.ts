import type { Ref } from "vue";
import { useAtividadesStore } from "@/stores/atividades";
import { useFeedbackStore } from "@/stores/feedback";
import type { Atividade, Conhecimento, CriarConhecimentoRequest } from "@/types/tipos";

type DadosRemocao = { tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number } | null;

export interface CadAtividadesCrud {
    adicionarAtividade: (codSubprocesso: number | null, codMapa: number | null) => Promise<boolean>;
    removerAtividade: (idx: number, codSubprocesso: number | null, dadosRemocao: Ref<DadosRemocao>, mostrarModal: Ref<boolean>) => void;
    confirmarRemocao: (
        dadosRemocao: Ref<DadosRemocao>,
        codSubprocesso: number | null,
        atividades: Atividade[],
        mostrarModal: Ref<boolean>,
    ) => Promise<void>;
    adicionarConhecimento: (idx: number, descricao: string, codSubprocesso: number | null, atividades: Atividade[]) => Promise<void>;
    removerConhecimento: (idx: number, conhecimentoCodigo: number, codSubprocesso: number | null, dadosRemocao: Ref<DadosRemocao>, mostrarModal: Ref<boolean>) => void;
    salvarEdicaoConhecimento: (atividadeCodigo: number, conhecimentoCodigo: number, descricao: string, codSubprocesso: number | null) => Promise<void>;
    salvarEdicaoAtividade: (codigo: number, descricao: string, codSubprocesso: number | null, atividades: Atividade[]) => Promise<void>;
    handleImportAtividades: (codSubprocesso: number | null, mostrarModal: Ref<boolean>) => Promise<void>;
}

export function useCadAtividadesCrud(
    adicionarAtividadeAction: (codSubprocesso: number, codMapa: number) => Promise<boolean>,
): CadAtividadesCrud {
    const atividadesStore = useAtividadesStore();
    const feedbackStore = useFeedbackStore();

    async function adicionarAtividade(codSubprocesso: number | null, codMapa: number | null): Promise<boolean> {
        if (codMapa && codSubprocesso) {
            const sucesso = await adicionarAtividadeAction(codSubprocesso, codMapa);
            if (sucesso) {
                return true;
            }
        }
        return false;
    }

    function removerAtividade(idx: number, codSubprocesso: number | null, dadosRemocao: Ref<DadosRemocao>, mostrarModal: Ref<boolean>) {
        if (!codSubprocesso) return;
        dadosRemocao.value = { tipo: "atividade", index: idx };
        mostrarModal.value = true;
    }

    async function confirmarRemocao(
        dadosRemocao: Ref<DadosRemocao>,
        codSubprocesso: number | null,
        atividades: Atividade[],
        mostrarModal: Ref<boolean>,
    ) {
        if (!dadosRemocao.value || !codSubprocesso) return;

        const { tipo, index, conhecimentoCodigo } = dadosRemocao.value;

        try {
            if (tipo === "atividade") {
                const atividadeRemovida = atividades[index];
                await atividadesStore.removerAtividade(codSubprocesso, atividadeRemovida.codigo);
            } else if (tipo === "conhecimento" && conhecimentoCodigo !== undefined) {
                const atividade = atividades[index];
                await atividadesStore.removerConhecimento(codSubprocesso, atividade.codigo, conhecimentoCodigo);
            }
            mostrarModal.value = false;
            dadosRemocao.value = null;
        } catch (e: any) {
            feedbackStore.show("Erro na remoção", e.message || "Não foi possível remover o item.", "danger");
            mostrarModal.value = false;
        }
    }

    async function adicionarConhecimento(idx: number, descricao: string, codSubprocesso: number | null, atividades: Atividade[]) {
        if (!codSubprocesso) return;
        const atividade = atividades[idx];
        if (descricao.trim()) {
            const request: CriarConhecimentoRequest = {
                descricao: descricao.trim(),
            };
            await atividadesStore.adicionarConhecimento(codSubprocesso, atividade.codigo, request);
        }
    }

    function removerConhecimento(idx: number, conhecimentoCodigo: number, codSubprocesso: number | null, dadosRemocao: Ref<DadosRemocao>, mostrarModal: Ref<boolean>) {
        if (!codSubprocesso) return;
        dadosRemocao.value = { tipo: "conhecimento", index: idx, conhecimentoCodigo };
        mostrarModal.value = true;
    }

    async function salvarEdicaoConhecimento(atividadeCodigo: number, conhecimentoCodigo: number, descricao: string, codSubprocesso: number | null) {
        if (!codSubprocesso) return;

        if (descricao.trim()) {
            const conhecimentoAtualizado: Conhecimento = {
                codigo: conhecimentoCodigo,
                descricao: descricao.trim(),
            };
            await atividadesStore.atualizarConhecimento(
                codSubprocesso,
                atividadeCodigo,
                conhecimentoCodigo,
                conhecimentoAtualizado,
            );
        }
    }

    async function salvarEdicaoAtividade(codigo: number, descricao: string, codSubprocesso: number | null, atividades: Atividade[]) {
        if (descricao.trim() && codSubprocesso) {
            const atividadeOriginal = atividades.find((a) => a.codigo === codigo);
            if (atividadeOriginal) {
                const atividadeAtualizada: Atividade = {
                    ...atividadeOriginal,
                    descricao: descricao.trim(),
                };
                await atividadesStore.atualizarAtividade(codSubprocesso, codigo, atividadeAtualizada);
            }
        }
    }

    async function handleImportAtividades(codSubprocesso: number | null, mostrarModal: Ref<boolean>) {
        mostrarModal.value = false;
        if (codSubprocesso) {
            await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso);
        }
        feedbackStore.show("Importação Concluída", "As atividades foram importadas para o seu mapa.", "success");
    }

    return {
        adicionarAtividade,
        removerAtividade,
        confirmarRemocao,
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento,
        salvarEdicaoAtividade,
        handleImportAtividades,
    };
}
