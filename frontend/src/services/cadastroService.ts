import apiClient from "../axios-setup";

export async function disponibilizarCadastro(id: number): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/disponibilizar`);
}

export async function disponibilizarRevisaoCadastro(id: number): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/disponibilizar-revisao`);
}

export async function devolverCadastro(
    id: number,
    dados: { motivo: string; observacoes: string },
): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/devolver-cadastro`, dados);
}

export async function aceitarCadastro(
    id: number,
    dados: { observacoes: string },
): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/aceitar-cadastro`, dados);
}

export async function homologarCadastro(
    id: number,
    dados: { observacoes: string },
): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/homologar-cadastro`, dados);
}

export async function devolverRevisaoCadastro(
    id: number,
    dados: { motivo: string; observacoes: string },
): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/devolver-revisao-cadastro`, dados);
}

export async function aceitarRevisaoCadastro(
    id: number,
    dados: { observacoes: string },
): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/aceitar-revisao-cadastro`, dados);
}

export async function homologarRevisaoCadastro(
    id: number,
    dados: { observacoes: string },
): Promise<void> {
  await apiClient.post(`/subprocessos/${id}/homologar-revisao-cadastro`, dados);
}
