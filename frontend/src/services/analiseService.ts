import apiClient from "@/axios-setup";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

export const listarAnalisesCadastro = async (
    codSubprocesso: number,
): Promise<AnaliseCadastro[]> => {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-cadastro`,
    );
    return response.data;
};

export const listarAnalisesValidacao = async (
    codSubprocesso: number,
): Promise<AnaliseValidacao[]> => {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-validacao`,
    );
    return response.data;
};
