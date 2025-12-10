# Router (Roteamento)

Última atualização: 2025-12-04 14:18:38Z

Este diretório contém a configuração do **Vue Router**, responsável pela navegação entre as páginas da aplicação.

## Estrutura Modular

Para evitar um arquivo de rotas gigante e incontrolável, as definições de rota foram divididas em módulos por domínio
funcional.

### Arquivos

- **`index.ts`**: Ponto de entrada principal.
    - Cria a instância do roteador (`createRouter`).
    - Define o histórico de navegação (`createWebHistory`).
    - Importa e combina as rotas dos submódulos.
    - **Guards Globais:** Implementa o `beforeEach` para verificação de autenticação (redireciona para `/login` se não
      autenticado).

- **`main.routes.ts`**: Rotas gerais da aplicação, como:
    - `/`: Redirecionamento raiz.
    - `/login`: Página de login.
    - `/painel`: Dashboard principal.
    - `/404`: Página de erro não encontrado.

- **`processo.routes.ts`**: Rotas relacionadas à gestão de processos e subprocessos.
    - `/processos`: Listagem.
    - `/processos/novo`: Criação.
    - `/subprocessos/:id`: Detalhes e execução do workflow.

- **`unidade.routes.ts`**: Rotas para visualização e gestão de unidades organizacionais.

## Convenções

- **Lazy Loading:** As views devem ser importadas dinamicamente (ex:
  `component: () => import('../views/MinhaView.vue')`) para otimizar o carregamento inicial da aplicação (Code
  Splitting).
- **Meta Fields:** O campo `meta` é usado para definir propriedades da rota, como `requiresAuth` (se exige login) e
  `breadcrumb` (para navegação).

## Detalhamento técnico (gerado em 2025-12-04T14:22:48Z)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
