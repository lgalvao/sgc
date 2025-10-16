import type { SubprocessoCadastroDto } from '@/types/SubprocessoCadastroDto'
import type { Atividade, Conhecimento } from '@/types/tipos'

function mapConhecimento(dto: SubprocessoCadastroDto.ConhecimentoDto): Conhecimento {
  return {
    id: dto.id,
    descricao: dto.descricao
  }
}

function mapAtividade(
  dto: SubprocessoCadastroDto.AtividadeCadastroDTO,
  idSubprocesso: number
): Atividade {
  return {
    id: dto.id,
    idSubprocesso: idSubprocesso,
    descricao: dto.descricao,
    conhecimentos: dto.conhecimentos.map(mapConhecimento)
  }
}

export function mapSubprocessoCadastroToAtividades(
  dto: SubprocessoCadastroDto,
  idSubprocesso: number
): Atividade[] {
  if (!dto || !dto.atividades) return []
  return dto.atividades.map((atividadeDto) => mapAtividade(atividadeDto, idSubprocesso))
}