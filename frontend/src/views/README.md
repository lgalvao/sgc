# Diretório de Views (Páginas)

Este diretório contém os componentes de mais alto nível da aplicação, conhecidos como "Views" ou "Pages". Cada arquivo `.vue` neste diretório representa uma página completa que um usuário pode visitar.

## Objetivo

As _views_ são os componentes que são diretamente mapeados para as rotas da aplicação no arquivo `router.ts`. Elas servem como o ponto de entrada para cada tela do sistema.

## Características

- **Componentes "Smart"**: As _views_ são tipicamente componentes "inteligentes" (_smart components_). Elas contêm a lógica de negócio da página, orquestram a busca de dados e gerenciam o estado específico daquela tela.
- **Composição de UI**: Uma _view_ é responsável por montar a interface da página, compondo e organizando múltiplos componentes reutilizáveis que vêm do diretório `src/components`. Por exemplo, uma `DashboardView.vue` pode importar e usar componentes como `GraficoVendas`, `TabelaAtividadesRecentes` e `CartaoResumo`.
- **Interação com o Estado Global**: As _views_ frequentemente interagem com as _stores_ do Pinia (`src/stores`) para obter dados globais ou para disparar ações que afetam toda a aplicação (como fazer logout).
- **Busca de Dados**: A lógica para buscar os dados necessários para a página (e.g., fazer chamadas de API) geralmente é iniciada aqui, muitas vezes utilizando funções dos `composables` ou ações das `stores`.

## Estrutura

Para uma melhor organização, as _views_ podem ser agrupadas em subdiretórios que correspondem às principais funcionalidades ou módulos da aplicação.

Exemplo:
- `views/`
  - `auth/`
    - `LoginView.vue`
    - `RecuperarSenhaView.vue`
  - `subprocesso/`
    - `ListagemSubprocessosView.vue`
    - `DetalhesSubprocessoView.vue`
  - `HomeView.vue`
  - `NotFoundView.vue`

Essa estrutura ajuda a manter o código organizado à medida que a aplicação cresce em complexidade.