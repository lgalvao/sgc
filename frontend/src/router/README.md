# Diretório de Roteamento

Este diretório contém a configuração do Vue Router.

## Estrutura

- **`index.ts`**: Ponto de entrada que cria a instância do router e define os guards globais (autenticação).
- **`*.routes.ts`**: Definições de rotas modulares. Cada domínio funcional deve ter seu próprio arquivo de rotas para
  evitar um `index.ts` gigante.

## Modularização

Exemplo de importação no `index.ts`:

```typescript
import processoRoutes from './processo.routes';
import adminRoutes from './admin.routes';

const routes = [
  ...processoRoutes,
  ...adminRoutes,
  { path: '/login', component: Login }
];
```

## Navigation Guards

O `index.ts` implementa o `beforeEach` para verificar:

1. Se a rota requer autenticação (`meta: { requiresAuth: true }`).
2. Se o usuário tem o perfil necessário (`meta: { roles: ['ADMIN'] }`).