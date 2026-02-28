# Diretório de Roteamento

Configuração centralizada do Vue Router.

## Estrutura de Arquivos

* **`index.ts`**: Ponto de entrada. Cria a instância do router e configura os Navigation Guards (proteção de rotas).
* **`main.routes.ts`**: Rotas principais de nível superior (Login, Dashboard, Erro).
* **`processo.routes.ts`**: Rotas relacionadas ao fluxo de processos e subprocessos.
* **`unidade.routes.ts`**: Rotas para gestão e visualização de unidades.

## Navigation Guards

O sistema utiliza o guard `beforeEach` para validar:

1. **Autenticação**: Verifica se o usuário possui um token válido para rotas marcadas com `requiresAuth`.
2. **Autorização**: Valida se o perfil selecionado do usuário permite o acesso à rota (`roles`).
