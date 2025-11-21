import apiClient from "@/axios-setup";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

export const listarAnalisesCadastro = async (
    subprocessoId: number,
): Promise<AnaliseCadastro[]> => {
    const response = await apiClient.get(
        `/subprocessos/${subprocessoId}/historico-cadastro`,
    );
    return response.data;
};

export const listarAnalisesValidacao = async (
    subprocessoId: number,
): Promise<AnaliseValidacao[]> => {
    const response = await apiClient.get(
        `/subprocessos/${subprocessoId}/historico-validacao`,
    );
    return response.data;
};
