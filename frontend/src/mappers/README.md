# Diretório de Mappers

Este diretório contém funções responsáveis por transformar dados entre diferentes formatos, geralmente entre a API (
Backend) e a View (Frontend).

## Por que usar Mappers?

- **Desacoplamento:** Evita que a estrutura do banco de dados/API "vaze" diretamente para os componentes visuais. Se o
  backend mudar o nome de um campo, você altera apenas o mapper, não 50 componentes.
- **Formatação:** Prepara dados para exibição (ex: formatar datas, converter códigos de status em labels legíveis,
  calcular campos derivados).

## Exemplo

```typescript
// ProcessoMapper.ts
import type { ProcessoDto, ProcessoVisualizacao } from '@/types';
import { formatarDataBrasileira } from '@/utils/formatters';

export function paraVisualizacao(dto: ProcessoDto): ProcessoVisualizacao {
  return {
    id: dto.codigo,
    tituloFormatado: dto.titulo.toUpperCase(),
    inicio: formatarDataBrasileira(dto.dataInicio),
    statusLabel: obterLabelSituacao(dto.situacao)
  };
}
```