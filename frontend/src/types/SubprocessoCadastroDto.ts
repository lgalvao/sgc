// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace SubprocessoCadastroDto {
  export interface ConhecimentoDto {
    id: number
    descricao: string
  }

  export interface AtividadeCadastroDTO {
    id: number
    descricao: string
    conhecimentos: ConhecimentoDto[]
  }

  export interface SubprocessoCadastro {
    subprocessoId: number
    unidadeSigla: string
    atividades: AtividadeCadastroDTO[]
  }
}