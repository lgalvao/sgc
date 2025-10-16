# Diretório de Mappers

Este diretório contém funções _mapper_ (mapeadoras), cuja responsabilidade é transformar (mapear) objetos de uma estrutura para outra.

## Objetivo

O principal objetivo dos mappers é desacoplar as estruturas de dados do _backend_ (DTOs - _Data Transfer Objects_) das estruturas de dados utilizadas internamente no _frontend_ (modelos de visão, ou _view models_).

Essa camada de transformação é crucial para a manutenibilidade e resiliência da aplicação:

1.  **Isolamento de Mudanças**: Se o _backend_ alterar a estrutura de um DTO (e.g., renomear um campo), a mudança só precisa ser tratada no mapper correspondente, sem a necessidade de refatorar todos os componentes que consomem aquela informação.
2.  **Otimização para a Visão**: Os mappers podem preparar os dados para a exibição, por exemplo, formatando datas, calculando campos derivados, ou estruturando os dados de uma forma que seja mais conveniente para os componentes Vue.
3.  **Tipagem Forte**: Eles garantem que os dados que fluem pela aplicação _frontend_ estejam em conformidade com as interfaces TypeScript (`types`) definidas no projeto.

## Estrutura e Convenções

- Cada arquivo geralmente corresponde a um domínio de negócio (e.g., `mapaMapper.ts`, `subprocessoMapper.ts`).
- As funções de mapeamento devem ser puras (sem efeitos colaterais) sempre que possível.
- Os nomes das funções devem indicar claramente a transformação que realizam, por exemplo: `fromSubprocessoDtoToViewModel` ou `fromViewModelToCreateSubprocessoRequest`.

### Exemplo

```typescript
// Em /mappers/usuarioMapper.ts
import type { UsuarioDto } from '@/types/dto/UsuarioDto';
import type { UsuarioViewModel } from '@/types/UsuarioViewModel';

export function fromUsuarioDtoToViewModel(dto: UsuarioDto): UsuarioViewModel {
  return {
    id: dto.codigo,
    nomeCompleto: `${dto.nome} ${dto.sobrenome}`,
    email: dto.email,
    ativo: dto.status === 'ATIVO',
  };
}
```