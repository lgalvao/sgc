/**
 * Tipos utilizados pelo widget de feedback.
 *
 * Este módulo é carregado apenas em builds com VITE_FEEDBACK_WIDGET=true
 * e é excluído pelo tree-shaker em builds de produção.
 */

export type FeedbackTipo = 'bug' | 'sugestao' | 'questao'

export interface MetadadosFeedback {
    /** Código/título eleitoral do usuário autenticado */
    usuarioCodigo: string
    /** Nome de exibição do usuário */
    usuarioNome: string

    /** Nome da rota Vue Router atual */
    rotaNome: string
    /** Caminho completo da rota, incluindo parâmetros */
    rotaCaminho: string
    /** Query string serializada como JSON */
    rotaQuery: string
    /** Título da página (document.title) */
    tituloPagina: string

    /** Perfil/unidade ativo na sessão SGC */
    perfilAtivo: string | null
    /** Unidade ativa (sigla) */
    unidadeAtiva: string | null

    /** Carimbo de data/hora ISO 8601 */
    dataHora: string
    /** Deslocamento do fuso horário em minutos */
    fusoHorario: number
    /** Navigator.userAgent */
    userAgent: string
    /** Largura da janela em pixels */
    larguraTela: number
    /** Altura da janela em pixels */
    alturaTela: number
    /** Navigator.language */
    idioma: string
}

export interface PayloadFeedback {
    tipo: FeedbackTipo
    nota: string
    metadados: MetadadosFeedback
}
