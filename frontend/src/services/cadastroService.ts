import apiClient from "../axios-setup";

export async function disponibilizarCadastro(codSubprocesso: number): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/cadastro/disponibilizar`);
}

export async function disponibilizarRevisaoCadastro(codSubprocesso: number): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar-revisao`);
}

export async function devolverCadastro(
    codSubprocesso: number,
    dados: { observacoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/devolver-cadastro`, dados);
}

export async function aceitarCadastro(
    codSubprocesso: number,
    dados: { observacoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-cadastro`, dados);
}

export async function homologarCadastro(
    codSubprocesso: number,
    dados: { observacoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-cadastro`, dados);
}

export async function devolverRevisaoCadastro(
    codSubprocesso: number,
    dados: { observacoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/devolver-revisao-cadastro`, dados);
}

export async function aceitarRevisaoCadastro(
    codSubprocesso: number,
    dados: { observacoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-revisao-cadastro`, dados);
}

export async function homologarRevisaoCadastro(
    codSubprocesso: number,
    dados: { observacoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-revisao-cadastro`, dados);
}
