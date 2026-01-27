import { defineStore } from "pinia";
import { ref } from "vue";
import {
    aceitarCadastro,
    aceitarRevisaoCadastro,
    devolverCadastro,
    devolverRevisaoCadastro,
    disponibilizarCadastro,
    disponibilizarRevisaoCadastro,
    homologarCadastro,
    homologarRevisaoCadastro,
} from "@/services/cadastroService";
import { apiClient } from "@/axios-setup";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarSubprocessoDetalhe as serviceFetchSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import { usePerfilStore } from "@/stores/perfil";
import { useProcessosStore } from "@/stores/processos";
import { useFeedbackStore } from "@/stores/feedback";
import { useUnidadesStore } from "@/stores/unidades";
import { useMapasStore } from "@/stores/mapas";
import { useAtividadesStore } from "@/stores/atividades";
import { mapMapaCompletoDtoToModel } from "@/mappers/mapas";
import { mapAtividadeVisualizacaoToModel } from "@/mappers/atividades";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    SubprocessoDetalhe,
} from "@/types/tipos";
import { useErrorHandler } from "@/composables/useErrorHandler";
import { normalizeError } from "@/utils/apiError";

export const useSubprocessosStore = defineStore("subprocessos", () => {
    const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const feedbackStore = useFeedbackStore();

    async function _executarAcao(acao: () => Promise<any>, sucessoMsg: string, _: string): Promise<boolean> {
        return withErrorHandling(async () => {
            await acao();
            feedbackStore.show(sucessoMsg, `${sucessoMsg}.`, 'success');

            const processosStore = useProcessosStore();
            if (processosStore.processoDetalhe) {
                await processosStore.buscarProcessoDetalhe(processosStore.processoDetalhe.codigo);
            }
            return true;
        }).catch(() => {
            // Propagating error is important if the component relies on it,
            // but the original code was catching it and returning false.
            // The plan says: "Deixar componente/view decidir UX"
            // However, _executarAcao is a helper. Let's populate lastError and return false,
            // but we might want to throw if components need to handle it.
            // The original code was returning false, so let's stick to that for now
            // but ensure lastError is set so components CAN display it if they want.
            // AND we remove the feedbackStore.show call for errors.
            return false;
        });
    }

    async function alterarDataLimiteSubprocesso(
        id: number,
        dados: { novaData: string },
    ) {
        return withErrorHandling(async () => {
            await apiClient.post(`/subprocessos/${id}/data-limite`, {
                novaDataLimite: dados.novaData
            });
            // Recarregar os detalhes para refletir a nova data
            await buscarSubprocessoDetalhe(id);
        });
    }

    async function buscarSubprocessoDetalhe(id: number) {
        subprocessoDetalhe.value = null; // Limpa estado anterior
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;

        // Perfis ADMIN e GESTOR são globais e podem acessar qualquer subprocesso
        // sem necessidade de codUnidade específico
        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            subprocessoDetalhe.value = null;
            return;
        }

        await withErrorHandling(async () => {
            subprocessoDetalhe.value = await serviceFetchSubprocessoDetalhe(
                id,
                perfil,
                codUnidade as number,
            );
        }, () => {
            subprocessoDetalhe.value = null;
        }).catch(() => {
            // Silenciar erro - lastError já foi populado pelo withErrorHandling
            subprocessoDetalhe.value = null;
        });
    }

    async function buscarSubprocessoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
    ): Promise<number | null> {
        return withErrorHandling(async () => {
            const dto = await serviceBuscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);
            return dto.codigo;
        }).catch(() => {
            return null;
        });
    }

    async function buscarContextoEdicao(id: number) {
        subprocessoDetalhe.value = null; // Limpa estado anterior
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;

        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            return;
        }

        return withErrorHandling(async () => {
            const data = await serviceBuscarContextoEdicao(id, perfil, codUnidade as number);

            subprocessoDetalhe.value = data.subprocesso;

            const unidadesStore = useUnidadesStore();
            unidadesStore.unidade = data.unidade;

            const mapasStore = useMapasStore();
            mapasStore.mapaCompleto = data.mapa ? mapMapaCompletoDtoToModel(data.mapa) : null;

            const atividadesStore = useAtividadesStore();
            const atividadesMapped = (data.atividadesDisponiveis || []).map(mapAtividadeVisualizacaoToModel);
            atividadesStore.setAtividadesParaSubprocesso(id, atividadesMapped);
        });
    }

    return {
        subprocessoDetalhe,
        lastError,
        clearError,
        alterarDataLimiteSubprocesso,
        buscarSubprocessoDetalhe,
        buscarContextoEdicao,
        buscarSubprocessoPorProcessoEUnidade,
        disponibilizarCadastro: (codSubrocesso: number) =>
            _executarAcao(
                () => disponibilizarCadastro(codSubrocesso),
                "Cadastro de atividades disponibilizado",
                "Erro ao disponibilizar",
            ),
        disponibilizarRevisaoCadastro: (codSubrocesso: number) =>
            _executarAcao(
                () => disponibilizarRevisaoCadastro(codSubrocesso),
                "Revisão disponibilizada",
                "Erro ao disponibilizar",
            ),
        devolverCadastro: (codSubrocesso: number, req: DevolverCadastroRequest) =>
            _executarAcao(
                () => devolverCadastro(codSubrocesso, req),
                "Cadastro devolvido",
                "Erro ao devolver",
            ),
        aceitarCadastro: (codSubrocesso: number, req: AceitarCadastroRequest) =>
            _executarAcao(
                () => aceitarCadastro(codSubrocesso, req),
                "Cadastro aceito",
                "Erro ao aceitar",
            ),
        homologarCadastro: async (codSubrocesso: number, req: HomologarCadastroRequest) => {
            const ok = await _executarAcao(
                () => homologarCadastro(codSubrocesso, req),
                "Cadastro homologado",
                "Erro ao homologar",
            );
            if (ok) await buscarSubprocessoDetalhe(codSubrocesso);
            return ok;
        },
        devolverRevisaoCadastro: (codSubrocesso: number, req: DevolverCadastroRequest) =>
            _executarAcao(
                () => devolverRevisaoCadastro(codSubrocesso, req),
                "Revisão devolvida",
                "Erro ao devolver",
            ),
        aceitarRevisaoCadastro: (codSubrocesso: number, req: AceitarCadastroRequest) =>
            _executarAcao(
                () => aceitarRevisaoCadastro(codSubrocesso, req),
                "Revisão aceita",
                "Erro ao aceitar",
            ),
        homologarRevisaoCadastro: async (codSubrocesso: number, req: HomologarCadastroRequest) => {
            const ok = await _executarAcao(
                () => homologarRevisaoCadastro(codSubrocesso, req),
                "Revisão homologada",
                "Erro ao homologar",
            );
            if (ok) await buscarSubprocessoDetalhe(codSubrocesso);
            return ok;
        },
    };
});
