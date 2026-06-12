import apiClient from "@/axios-setup";
import type {Analise} from "@/types/tipos";

export const listarAnalisesCadastro = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get<Analise[]>(
        `/subprocessos/${codSubprocesso}/historico-cadastro`,
    );
    return response.data;
};

export const listarAnalisesValidacao = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get<Analise[]>(
        `/subprocessos/${codSubprocesso}/historico-validacao`,
    );
    return response.data;
};

export const listarAnalisesDiagnostico = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get<Analise[]>(
        `/diagnosticos/subprocessos/${codSubprocesso}/historico`,
    );
    return response.data;
};
