// Constantes gerais da aplicação
export const URL_SISTEMA = 'http://localhost:5173'; // URL do sistema SGC

export const EMAIL_TEMPLATES = {
    FINALIZACAO_PROCESSO_OPERACIONAL: (processoDescricao: string, _unidadeSigla: string) =>
    `SGC: Conclusão do processo ${processoDescricao}`,

    FINALIZACAO_PROCESSO_INTERMEDIARIA: (processoDescricao: string, _unidadeSigla: string) =>
    `SGC: Conclusão do processo ${processoDescricao} em unidades subordinadas`,

  CORPO_EMAIL_OPERACIONAL: (processoDescricao: string, unidadeSigla: string) =>
    `Prezado(a) responsável pela ${unidadeSigla},\n\n` +
    `Comunicamos a conclusão do processo ${processoDescricao} para a sua unidade.\n\n` +
    `Já é possível visualizar o seu mapa de competências atualizado através do menu Minha Unidade do Sistema de Gestão de Competências (${URL_SISTEMA}).`,

  CORPO_EMAIL_INTERMEDIARIA: (processoDescricao: string, unidadeSigla: string, unidadesSubordinadas: string[]) =>
    `Prezado(a) responsável pela ${unidadeSigla},\n\n` +
    `Comunicamos a conclusão do processo ${processoDescricao} para as unidades ${unidadesSubordinadas.join(', ')}.\n\n` +
    `Já é possível visualizar os mapas de competências atualizados destas unidades através do menu Minha Unidade do Sistema de Gestão de Competências (${URL_SISTEMA}).`
};