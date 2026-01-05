# Diretório de Constantes

Este diretório armazena valores constantes reutilizáveis para evitar "magic numbers" e strings soltas pelo código.

## Boas Práticas

- Agrupe constantes logicamente em arquivos ou objetos.
- Use `UPPER_SNAKE_CASE` para constantes primitivas.
- Use `PascalCase` para Objetos congelados que funcionam como Enums.

## Exemplos

**`AppConstants.ts`**
```typescript
export const APP_NAME = 'SGC - Sistema de Gestão de Competências';
export const API_TIMEOUT = 30000;
```

**`BusinessConstants.ts`**
```typescript
export const PERFIS_ACESSO = {
  ADMIN: 'ADMIN',
  GESTOR: 'GESTOR',
  SERVIDOR: 'SERVIDOR'
} as const;
```