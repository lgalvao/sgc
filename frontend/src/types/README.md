# Types (Definições TypeScript)

Este diretório armazena as definições de tipos, interfaces e enums do TypeScript. Ele serve como o contrato de dados entre o frontend e o backend.

## Organização

- **`tipos.ts`**: Contém as interfaces principais que espelham as entidades do domínio (ex: `Processo`, `Unidade`, `Usuario`).
- **`impacto.ts`**: Define as interfaces específicas para a funcionalidade de análise de impacto de mapas (DTOs complexos de comparação).

## Boas Práticas
- **Sincronia com Backend:** As interfaces aqui definidas devem refletir fielmente os DTOs retornados pela API Java. Se o backend mudar um campo, a interface aqui deve ser atualizada.
- **Uso Global:** Evite definir interfaces dentro de componentes (`.vue`). Sempre que um tipo for usado em mais de um lugar, ele deve ser movido para este diretório.
