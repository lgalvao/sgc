export type AcaoPrincipalCadastro = {
    codigo: 'ACEITAR' | 'HOMOLOGAR';
    mostrar: boolean;
    habilitar: boolean;
    tituloModal: string;
    textoModal: string;
    rotuloBotao: string;
    rotuloConfirmacao: string;
    mensagemSucesso: string;
    redirecionarParaPainel: boolean;
};

export type AcaoPrincipalMapa = {
    codigo: 'ACEITAR' | 'HOMOLOGAR';
    mostrar: boolean;
    habilitar: boolean;
    rotuloBotao: string;
    mensagemSucesso: string;
};
