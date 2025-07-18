# Prompt para Continuidade do Projeto SGC

Este projeto é um protótipo de um Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com Vue Router, Bootstrap 5 e Pinia para gerenciamento de estado global. O objetivo é apoiar a SEDOC no mapeamento de competências das unidades organizacionais do Tribunal.

## Regras e Fluxo do Sistema

- **Processo**: O fluxo principal é o de mapeamento de competências. Cada processo tem tipo (Mapeamento, Revisão, Diagnóstico), unidades participantes (em hierarquia) e situação (Não iniciado, Em andamento, Finalizado), sendo esta situação determinada pelo sistema
- **Unidades Organizacionais**: Possuem hierarquia (ex: STIC > COSIS > SEDESENV/SESEL). Apenas unidades "folha" (sem filhas) podem cadastrar atividades/conhecimentos.
- **Atividades e Conhecimentos**: Cada unidade folha cadastra atividades (descrição) e, para cada atividade, conhecimentos (descrição). Tudo é feito em uma única tela dinâmica.
- **Mapa de Competências**: Para cada unidade finalizada, é possível criar, editar, visualizar e disponibilizar um Mapa de Competências, associando atividades a competências. O fluxo é simulado no front-end, com dados centralizados em store Pinia e mockados em JSON.
- **Situação**: Situação dos processos, unidades e mapas é padronizada: Não iniciado, Em andamento, Finalizado, Disponível para validação. Situação dos procesos, unidades e mapas é calculada pelo sistema.
- **Sem backend**: Todos os dados são fictícios e manipulados apenas no front-end.
- **Centralização dos dados**: Todos os dados de processos, unidades, atividades, conhecimentos e mapas de competências são centralizados em stores Pinia. Os componentes acessam e manipulam os dados exclusivamente via os stores, garantindo reatividade e fonte única de verdade.
- **Dados de amostra**: Os dados mockados ficam em arquivos JSON na pasta `src/mocks` e são importados apenas nos stores Pinia, nunca diretamente nos componentes. O arquivo `src/mocks/mapas.json` armazena os mapas de competências.

## Telas e Componentes

- **Login**: Simples, sem autenticação real.
- **Painel**: Acesso rápido às áreas principais. O conteúdo do painel é parametrizado por perfil, usando arquivos JSON em português localizados em `src/mocks/painel`. Esses arquivos definem os cartões, listas e ações rápidas de cada perfil, usando apenas dados puros e chaves em português.
- **Processos**: Lista de processos. Clique em um processo mostra as unidades participantes em árvore colapsável.
- **Formulário de Processo**: Cadastro de novo processo, seleção de unidades participantes via checkboxes hierárquicos.
- **Unidades do Processo**: Árvore colapsável de unidades. Clique em qualquer nó expande/recolhe. Unidades folha são destacadas em azul ao hover e são totalmente clicáveis (linha inteira), levando à tela de atividades/conhecimentos.
- **Atividades/Conhecimentos**: Tela única para cada unidade folha, onde o usuário cadastra atividades e conhecimentos. Para a unidade SESEL, já existem atividades/conhecimentos predefinidos em JSON.
- **Mapa de Competências**: Para cada unidade finalizada, é possível criar, editar, visualizar e disponibilizar um mapa de competências. O fluxo inclui:
  - **Edição de Mapa de Competências** (`src/views/MapaCompetencias.vue`): Seleção de atividades, associação a competências, descrição detalhada, visualização das competências cadastradas e geração do mapa.
  - **Finalização de Mapa de Competências** (`src/views/FinalizacaoMapa.vue`): Resumo das competências, opção de incluir atividades, definição de data limite para validação e simulação de notificação de disponibilização.
- **Navbar**: Links para todas as áreas principais, incluindo acesso direto à tela de atividades/conhecimentos e mapas. Inclui seletor de perfil global.
- **TreeNode.vue**: Componente recursivo para árvore de unidades.

## UI/UX

- Os formulários são simples, sem backend ou validação real (protótipo).
- **Bootstrap 5** para layout responsivo e padronizado.
- **Árvore de unidades**: Todos os nós começam expandidos. Clique em qualquer parte do nó (exceto folha) expande/recolhe. Folhas têm hover azul (bg-primary, texto branco) e são totalmente clicáveis.
- **Formulários**: Simples, sem validação real.
- **Seletor de perfil global**: Sempre visível na Navbar, permite alternar entre SEDOC, CHEFE e GESTOR. Exibe aviso de que a alternância é apenas simulação, sem controle real de permissões.
- **Fluxo do Mapa de Competências**: O usuário SEDOC pode criar, editar e disponibilizar mapas para unidades finalizadas. O fluxo é totalmente simulado, com status e notificações fictícias.

## Gerenciamento de Perfil (Pinia + localStorage)

- O perfil do usuário é gerenciado globalmente usando Pinia (src/stores/perfil.js), com persistência automática em localStorage.
- O store exporta `usePerfilStore`, que deve ser acessado via um composable (`src/composables/usePerfil.js`).
- O valor inicial do perfil é 'SEDOC' ou o último valor salvo no localStorage.
- Para alterar o perfil, use o método `setPerfil` do store. Exemplo:

```js
import { usePerfil } from "../composables/usePerfil";
const perfil = usePerfil();
perfil.setPerfil("GESTOR"); // muda o perfil globalmente e persiste
```

- Para acessar o valor atual do perfil:

```js
const perfil = usePerfil();
console.log(perfil.value); // 'SEDOC', 'CHEFE' ou 'GESTOR'
```

- Todos os componentes que dependem do perfil (Navbar, Painel, etc.) devem usar o store para garantir reatividade e persistência.
- O perfil selecionado permanece após recarregar a página.
- O Pinia está registrado em `main.js`.

## Parametrização do Painel por Perfil

- O Painel é totalmente dinâmico e parametrizado por arquivos JSON em português, localizados em `src/mocks/painel`.
- Esses arquivos definem os cartões, listas e ações rápidas de cada perfil, usando apenas dados puros e chaves em português (ex: `cartoes`, `titulo`, `tipo`, `campos`, `rotulo`, `chaveDado`, `chaveLista`, `acoes`).
- O Painel.vue consome esses JSONs e renderiza dinamicamente os elementos conforme o perfil selecionado.
- A tabela de processos é exibida para os perfis SEDOC e GESTOR, acima dos cartões, com navegação igual à da tela de Processos.
- O mapeamento de status/tipo para classes de estilo (ex: Bootstrap) é feito apenas no componente Vue, nunca nos JSONs.
- Todo o sistema, inclusive dados, configuração e comentários, deve ser mantido em português.

## Observações

- Todo o código, comentários e arquivos de configuração estão em português.
- O sistema é um protótipo, focado em simular o fluxo e experiência de uso.
- Sempre seguir o padrão de componentização, navegação, UI e organização de dados em JSON já estabelecidos.
- Comentar o código explicando as simulações e limitações do protótipo.
- Atualizar README e documentação ao evoluir o sistema.

## Importante

- Para continuar, siga o padrão de componentização, navegação, UI e organização de dados em JSON já estabelecidos.
- Sempre use o store Pinia para o perfil e para os dados globais, nunca refs globais ou variáveis locais.
- Se adicionar novos perfis, fluxos ou cartões, centralize a lógica no store e mantenha a persistência em localStorage e a configuração em JSONs em português.

## Estrutura de Componentes

- `src/components/TreeNode.vue`: Componente recursivo para árvore de unidades.
- `src/views/UnidadesProcesso.vue`: Tela de unidades participantes do processo.
- `src/views/AtividadesConhecimentos.vue`: Tela de cadastro de atividades/conhecimentos por unidade.
- `src/views/FormProcesso.vue`: Formulário de novo processo.
- `src/views/Processos.vue`: Lista de processos.
- `src/components/Navbar.vue`: Barra de navegação principal.
- `src/views/Painel.vue`: Painel dinâmico parametrizado por perfil e JSON.
- `src/views/MapaCompetencias.vue`: Edição e criação de mapa de competências por unidade.
- `src/views/FinalizacaoMapa.vue`: Finalização e disponibilização do mapa de competências.
- `src/mocks/mapas.json`: Mock de dados dos mapas de competências.
