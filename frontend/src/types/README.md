# Diretório de Tipos (TypeScript)

Este diretório centraliza as definições de tipos, interfaces e enums compartilhados por toda a aplicação frontend.

## Organização

- **`index.ts`**: (Opcional) Pode exportar todos os tipos para facilitar imports, mas prefira imports explícitos de arquivos específicos se o volume crescer.
- **`DomainTypes.ts`**: Interfaces que espelham as entidades do backend (Processo, Unidade, Competencia).
- **`ComponentProps.ts`**: Interfaces para props complexas de componentes reutilizáveis.
- **`ApiTypes.ts`**: DTOs de requisição e resposta da API (se diferirem muito das entidades de domínio).

## Convenção de Nomenclatura

- **Interfaces:** PascalCase (ex: `Processo`, `Usuario`). Evite prefixo `I` (ex: `IProcesso` - não use).
- **Enums:** PascalCase para o nome e chaves.
- **Types:** PascalCase.

## Exemplo

```typescript
// DomainTypes.ts
export interface Processo {
  codigo: number; // Note que usamos 'codigo' e não 'id'
  titulo: string;
  dataInicio: string; // ISO Date String
  situacao: SituacaoProcesso;
}

export enum SituacaoProcesso {
  EM_ANDAMENTO = 'EM_ANDAMENTO',
  FINALIZADO = 'FINALIZADO'
}
```