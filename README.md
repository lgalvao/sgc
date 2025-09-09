# Sobre o projeto

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com
Vue Router, Bootstrap 5 e Pinia. O objetivo é simular os fluxos de mapeamento, revisão e diagnóstico de competências das
unidades do TRE-PE, centralizando todos os dados no front-end via mocks em JSON. O sistema está em desenvolvimento
ativo, com várias
funcionalidades já implementadas e outras em andamento.

## Antes de qualquer coisa

- Este é um protótipo. Não vamos nos preocupar com desempenho ou reuso; o foco é no funcionamento da UX/UI do sistema;
  não antecipe otimizações e
  abstrações; faça só o necessário para o momento.
- O código deve ser simples e direto, seguindo as convenções do Vue e do Bootstrap, mas sem complexidade desnecessária.
- O código, comentários e dados devem estar sempre em **português do brasil**.

## Ambiente e Comandos

Para configurar o ambiente de desenvolvimento, siga os passos abaixo:

1. **Instalar dependências**:
   ```bash
   npm install
   ```
2. **Iniciar o servidor de desenvolvimento**:
   ```bash
   npm run dev
   ```

## Testes e Qualidade de Código

O projeto utiliza Vitest para testes unitários, Playwright para testes end-to-end e ESLint para garantir a consistência
do código.

- **Executar testes unitários**:
  ```bash
  npm run test:unit
  ```
- **Executar testes end-to-end**:
  ```bash
  npm run test:e2e
  ```
- **Verificar o estilo do código (Lint)**:
  ```bash
  npm run lint
  ```

## Visão geral do projeto

- **Dados Centralizados**:
    - Todos os dados (processos, unidades, atividades, conhecimentos, mapas de competências, atribuições temporárias,
      subprocessos etc.) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há backend;
      toda manipulação é local e reativa.
    - A funcionalidade de algumas telas pode depender da existência de dados específicos nos mocks (ex: atividades para
      uma unidade para habilitar o botão "Disponibilizar" em mapas de competências).
- **Perfis de Usuário**:
    - O perfil de usuário (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`), definido no enum `Perfil` em `src/types/tipos.ts`, é
      determinado dinamicamente com base na lotação do servidor logado, através do composable `usePerfil`.
    - O `idServidor` do usuário logado é gerenciado pela store `perfil.ts` e persistido no localStorage.
- **Login**:
    - A tela de login permite ao usuário "logar" como qualquer servidor cadastrado
    - Essa tela apresenta um seletor para pares "perfil - unidade" quando múltiplas opções estão disponíveis.

## Estrutura de Diretórios:

- `/src/components/`: Componentes Vue reutilizáveis
- `/src/views/`: Páginas/rotas da aplicação
- `/src/stores/`: Gerenciamento de estado com Pinia
- `/src/mocks/`: Dados simulados em JSON
- `/src/composables/`: Lógica reutilizável, como o `usePerfil.ts` que determina o perfil do usuário
- `/src/constants/`: Constantes e enums centralizados para situações, tipos e configurações
- `/src/utils/`: Utilitários auxiliares (dateUtils, idGenerator) para funcionalidades comuns
- `/src/types/`: Definições de tipos usados em todo o projeto

## Telas e Componentes Principais

- **Painel**: Tela inicial que exibe uma lista de processos e uma seção de alertas. A lista de processos é alimentada
  pela store `processos.ts`; alertas são dados mockados da store `alertas.ts`.
- **Processo**: Exibe árvore de unidades participantes de um processo.
- **Atividades e conhecimentos**: Cadastro de atividades e conhecimentos para uma unidade do tipo operacional ou
  interoperacional.
- **Mapa**: Edição/criação de mapa de competências. Para unidades com cadastro de atividades/conhecimentos finalizado,
  permite criar, editar, visualizar e disponibilizar mapas, que associam atividades a competências. Edição de mapas
  disponível apenas para o perfil ADMIN.
- **Atribuição temporária**: Criação/edição de atribuições temporárias de servidores a unidades.
- **Histórico de processos**: Consulta de processos finalizados.
- **Navbar**: Barra de navegação principal com links para todas as áreas, incluindo um seletor de perfil/unidade para
  simular o login de diferentes usuários. Exibe o perfil e a unidade selecionados.

## Arquitetura e Componentização

- **Dados**: Mockados em JSON, importados apenas nos stores do Pinia.
- **Utilitários Centralizados**: Funções comuns em `/src/utils/` para evitar duplicação de código.
- **Constantes**: Valores constantes centralizados em `/src/constants/` para facilitar manutenção.
- **Sistema de Notificações**: Substituição de `alert()` nativo com UX personalizado.

- **Stores**: Cada domínio (processo, mapa, unidade, perfil etc.) possui um store dedicado.
    - `processos.ts`: Gerencia o estado dos processos, incluindo a relação com as unidades através de `Subprocesso` e o
      acesso a `subprocessos.json`.
    - `mapas.ts`: Gerencia os mapas de competência, incluindo a busca por unidade e processo (`getMapaByUnidadeId`) e a
      busca por mapa vigente (`getMapaVigentePorUnidade`).
    - `atividades.ts`: Gerencia atividades e conhecimentos.
    - `atribuicaoTemporaria.ts`: Gerencia atribuições temporárias.
    - `perfil.ts`: Gerencia o `idServidor`, o perfil e a unidade selecionados do usuário logado, persistindo-os no
      localStorage.
    - `servidores.ts`: Gerencia os dados dos servidores.
    - `unidades.ts`: Gerencia as unidades organizacionais e inclui a função `pesquisarUnidade` para busca hierárquica.
    - `revisao.ts`: Gerencia o estado das mudanças realizadas durante a revisão de um mapa de competências, registrando
      todas as alterações em atividades e conhecimentos.
    - `alertas.ts`: Fornece dados mockados de alertass para o painel.
    - `notificacoes.ts`: Gerencia o sistema de notificações da aplicação.

- **Utilitários**:
    - `dateUtils.ts`: Funções para parsing, formatação e validação de datas em português brasileiro.
    - `idGenerator.ts`: Geração de IDs únicos para novas entidades do sistema.

- **Constantes**:
    - `situacoes.ts`: Constantes para situações de processos, mapas e atividades, incluindo labels e classes CSS.

- **Componentes**:
    - `AceitarMapaModal.vue`: Modal para que GESTORES/CHEFES aceitem um mapa ou para que o ADMIN o homologue.
    - `AcoesEmBlocoModal.vue`: Modal para aceitar ou homologar, em lote, os cadastros de atividades de múltiplas
      unidades.
    - `BarraNavegacao.vue`: Componente que agrupa o botão "Voltar" e breadcrumbs. O botão "Voltar" retorna ao histórico
      de navegação ou para o Painel. A trilha de navegação é dinâmica, sensível ao contexto (processos/unidades) e
      gerenciada por stores Pinia.
    - `CriarCompetenciaModal.vue`: Modal para criar ou editar uma competência, associando uma descrição a um conjunto de
      atividades selecionadas.
    - `DisponibilizarMapaModal.vue`: Modal para o ADMIN disponibilizar um mapa para validação, exigindo a definição de
      uma data limite.
    - `ImpactoMapaModal.vue`: Exibe o impacto das alterações (adições, remoções, modificações) de atividades e
      conhecimentos sobre as competências de um mapa durante um processo de revisão.
    - `ImportarAtividadesModal.vue`: Permite a importação de atividades de outros processos já finalizados para o
      cadastro de atividades atual.
    - `Navbar.vue`: Barra de navegação superior principal. Contém links de navegação, um seletor de perfil/unidade para
      simular a troca de usuário, e o botão de logout.
    - `NotificacaoContainer.vue`: Container global para exibição de notificações (toasts) no canto da tela. Notificações
      de sucesso desaparecem automaticamente.
    - `SistemaNotificacoesModal.vue`: Um modal que funciona como uma central de notificações, exibindo o histórico de
      todas as notificações do sistema.
    - `SubprocessoCards.vue`: Apresenta os cards de navegação na tela de subprocesso, que mudam de acordo com o tipo de
      processo (Mapeamento, Revisão ou Diagnóstico).
    - `SubprocessoHeader.vue`: Cabeçalho da página de subprocesso, exibindo informações da unidade, do processo,
      situação e responsáveis.
    - `SubprocessoModal.vue`: Modal exclusivo para o ADMIN alterar a data limite de uma etapa do subprocesso.
    - `TabelaProcessos.vue`: Componente reutilizável para exibir uma lista de processos, com suporte a ordenação.
    - `TreeRow.vue`: Linha de uma tabela hierárquica (`TreeTable`) que renderiza suas células dinamicamente e controla o
      aninhamento visual. Propaga eventos de clique corretamente.
    - `TreeTable.vue`: Tabela hierárquica que renderiza cabeçalhos e linhas com base nas colunas fornecidas. Suporta
      ocultar cabeçalhos, larguras de coluna dinâmicas e expandir/recolher todos os nós.
    - `UnidadeTreeItem.vue`: Componente recursivo para renderizar uma árvore de unidades com checkboxes, usado para
      seleção hierárquica.

- **Views**:
    - `Login.vue`: Tela de login que permite ao autenticar o usuário (simulado) como qualquer servidor cadastrado. Com
      seletor para pares "perfil/unidade" quando múltiplas opções destes estiverem disponíveis.
    - `Painel.vue`: Painel que exibe processos e alertas, utilizando os stores `processos.ts` e `alertas.ts`. O botão "
      Criar processo" é exibido para perfil ADMIN.
    - `Processo.vue`: Detalhes de um processo, incluindo situação do processo e a árvore de unidades participantes.
    - `Subprocesso.vue`: Detalhes de um subprocesso de uma unidade. Exibe informações do responsável e cards dinâmicos e
      clicáveis baseados no tipo de processo 'pai'.
    - `CadProcesso.vue`: Formulário para cadastro e edição de processos (CRUD).
    - `CadAtividades.vue`: Tela para cadastro e edição de atividades e conhecimentos de uma unidade.
    - `CadAtribuicao.vue`: Formulário para criação de atribuições temporárias de servidores a uma unidade.
    - `CadMapa.vue`: Tela para criação e edição de um mapa de competências, associando atividades a competências.
    - `Configuracoes.vue`: Página para o ADMIN ajustar configurações globais do sistema e carregar cenários de
      demonstração.
    - `DiagnosticoEquipe.vue`: Tela para a equipe avaliar a importância e o domínio das competências da unidade.
    - `Historico.vue`: Exibe uma lista de todos os processos já finalizados.
    - `OcupacoesCriticas.vue`: Tela para identificar e cadastrar as ocupações críticas da unidade, associando-as a
      competências.
    - `Relatorios.vue`: Painel de relatórios que permite visualizar e exportar dados sobre mapas vigentes, gaps de
      diagnóstico e andamento geral dos processos.
    - `Unidade.vue`: Detalhes de uma unidade organizacional fora do contexto de um processo, exibindo informações do
      responsável e unidades subordinadas.
    - `VisAtividades.vue`: Tela de visualização do cadastro de atividades e conhecimentos, usada para validação pela
      hierarquia de unidades.
    - `VisMapa.vue`: Tela de visualização de um mapa de competências, usada para validação.

## Regras Importantes ao codificar

- Sempre centralize dados e lógica de acesso a esses dados nos stores do Pinia.
- Nunca acesse arquivos JSON diretamente nos componentes.
- Mantenha todo o código, comentários e dados em português.
- Siga o padrão de navegação, componentização e UI/UX já estabelecido.
- Mantenha o padrão de rotas definido (Vue Router) — veja descrições em `endpoints.md`:
- Breadcrumbs globais: mostram trilha especial para processos/unidades: (home) > Processo > SIGLA > Página.
- Botão "Voltar" global volta no histórico quando possível; caso contrário volta para Painel.
- Breadcrumbs são ocultados no Painel e quando a navegação é iniciada pela navbar.
- Para o contexto do usuário logado (perfil e unidade), utilize sempre `perfilStore.perfilSelecionado` e
  `perfilStore.unidadeSelecionada`.
- Unidades do tipo `INTERMEDIARIA` (como COSIS) não devem ter `subprocessos` associados a elas.

## Utilitários e Constantes

- **Datas e horas**: Use `dateUtils.ts` para todas as operações com datas (parsing, formatação, validação);
  crie nova funções aqui, se necessário.
- **Constantes e modelos de mensagens**: Utilize as constantes em `/src/constants/` para situações, tipos e
  configurações em vez de strings
  "mágicas".
- **Notificações**: O sistema de notificações é gerenciado pela store `src/stores/notificacoes.ts`, acessível via
  `useNotificacoesStore()`. Ele oferece métodos convenientes como `sucesso()`, `erro()`, `aviso()`, `info()` e `email()`
  para disparar diferentes tipos de mensagens. As notificações são exibidas globalmente pelo `NotificacaoContainer.vue`
  e são amplamente utilizadas em telas como `src/views/CadAtividades.vue` para feedback de operações (adição, remoção,
  edição, importação de atividades e conhecimentos).
- **Geração de IDs**: Use `idGenerator.ts` para criar novos IDs únicos no sistema.