# Prompt para Continuidade do Projeto SGC

Este projeto é um protótipo de um Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com Vue Router, Bootstrap 5 e Pinia para gerenciamento de estado global. O objetivo é apoiar a SEDOC no mapeamento de competências das unidades organizacionais do Tribunal.

## Regras e Fluxo do Sistema
- **Processo**: O fluxo principal é o de mapeamento de competências. Cada processo tem tipo (Mapeamento, Revisão, Diagnóstico), unidades participantes (em hierarquia) e situação (Não iniciado, Em andamento, Finalizado), sendo esta situação determinada pelo sistema.
- **Unidades Organizacionais**: Possuem hierarquia (ex: STIC > COSIS > SEDESENV/SESEL). Apenas unidades "folha" (sem filhas) podem cadastrar atividades/conhecimentos.
- **Atividades e Conhecimentos**: Cada unidade folha cadastra atividades (descrição) e, para cada atividade, conhecimentos (descrição). Tudo é feito em uma única tela dinâmica.
- **Situação**: Situação dos processos e unidades é padronizada: Não iniciado, Em andamento, Finalizado. Situação dos procesos e unidades é calculada pelo sistema.
- **Sem backend**: Todos os dados são fictícios e manipulados apenas no front-end.
- **Dados de amostra**: Os dados de processos, unidades, atividades e conhecimentos estão organizados em arquivos JSON na pasta `src/mocks` e são importados nos componentes. Isso facilita o ajuste fino e a simulação de dados reais sem alterar o código Vue.

## Telas e Componentes
- **Login**: Simples, sem autenticação real.
- **Painel**: Acesso rápido às áreas principais.
- **Processos**: Lista de processos. Clique em um processo mostra as unidades participantes em árvore colapsável.
- **Formulário de Processo**: Cadastro de novo processo, seleção de unidades participantes via checkboxes hierárquicos.
- **Unidades do Processo**: Árvore colapsável de unidades. Clique em qualquer nó expande/recolhe. Unidades folha são destacadas em azul ao hover e são clicáveis (linha inteira), levando à tela de atividades/conhecimentos.
- **Atividades/Conhecimentos**: Tela única para cada unidade folha, onde o usuário cadastra atividades e conhecimentos. Para a unidade SESEL, já existem atividades/conhecimentos predefinidos em JSON.
- **Navbar**: Links para todas as áreas principais, incluindo acesso direto à tela de atividades/conhecimentos. Inclui seletor de perfil global.
- **TreeNode.vue**: Componente recursivo para árvore de unidades.

## UI/UX
- **Bootstrap 5** para layout responsivo.
- **Árvore de unidades**: Todos os nós começam expandidos. Clique em qualquer parte do nó (exceto folha) expande/recolhe. Folhas têm hover azul (bg-primary, texto branco) e são totalmente clicáveis.
- **Formulários**: Simples, sem validação real.
- **Seletor de perfil global**: Sempre visível na Navbar, permite alternar entre SEDOC, CHEFE e GESTOR. Exibe aviso de que a alternância é apenas simulação, sem controle real de permissões.

## Gerenciamento de Perfil (Pinia + localStorage)
- O perfil do usuário é gerenciado globalmente usando Pinia (src/stores/perfil.js), com persistência automática em localStorage.
- O store exporta `usePerfilStore`, que deve ser acessado via um composable (`src/composables/usePerfil.js`).
- O valor inicial do perfil é 'SEDOC' ou o último valor salvo no localStorage.
- Para alterar o perfil, use o método `setPerfil` do store. Exemplo:

```js
import { usePerfil } from '../composables/usePerfil'
const perfil = usePerfil()
perfil.setPerfil('GESTOR') // muda o perfil globalmente e persiste
```
- Para acessar o valor atual do perfil:
```js
const perfil = usePerfil()
console.log(perfil.value) // 'SEDOC', 'CHEFE' ou 'GESTOR'
```
- Todos os componentes que dependem do perfil (Navbar, Painel, etc.) devem usar o store para garantir reatividade e persistência.
- O perfil selecionado permanece após recarregar a página.
- O Pinia está registrado em `main.js`.

## Observações
- Todo o código e comentários estão em português.
- O sistema é um protótipo, focado em simular o fluxo e experiência de uso.
- Sempre seguir o padrão de componentização, navegação, UI e organização de dados em JSON já estabelecidos.
- Comentar o código explicando as simulações e limitações do protótipo.
- Atualizar README e documentação ao evoluir o sistema.

## Importante
- Para continuar, siga o padrão de componentização, navegação, UI e organização de dados em JSON já estabelecidos.
- Sempre use o store Pinia para o perfil, nunca refs globais ou variáveis locais.
- Se adicionar novos perfis ou fluxos, centralize a lógica no store e mantenha a persistência em localStorage.

----

Aqui está o todo list detalhado para adaptação do protótipo SGC, seguindo as especificações e priorizando a simulação do fluxo:

Simular fluxo de cadastro de atividades/conhecimentos pelas unidades folha (CHEFE), incluindo finalização do cadastro (bloqueando edição).

Simular validação/subida (GESTOR) e devolução/descida dos cadastros, com interfaces para GESTOR validar/devolver e SEDOC reabrir etapas.

Criar tela para SEDOC analisar atividades/conhecimentos e criar competências, permitindo associar competências a múltiplas atividades.

Simular etapa de disponibilização do mapa de competências para validação pelas unidades, com interfaces para CHEFE sugerir melhorias e GESTOR ratificar/devolver.

Exibir status de validação do mapa (Aguardando, Com sugestões, Validado) e simular notificações para prazos, devoluções e validações pendentes.

Garantir que todos os dados (processos, unidades, atividades, conhecimentos, competências) estejam em arquivos JSON na pasta src/mocks, facilitando ajustes sem alterar código Vue.

Revisar e ajustar UI/UX: manter Bootstrap 5, árvore de unidades interativa, formulários simples e padrão de componentização.

Comentar o código em português, explicando as simulações e limitações do protótipo. Atualizar README e documentação.

## Estrutura de Componentes
- `src/components/TreeNode.vue`: Componente recursivo para árvore de unidades.
- `src/views/UnidadesProcesso.vue`: Tela de unidades participantes do processo.
- `src/views/AtividadesConhecimentos.vue`: Tela de cadastro de atividades/conhecimentos por unidade.
- `src/views/FormProcesso.vue`: Formulário de novo processo.
- `src/views/Processos.vue`: Lista de processos.
- `src/components/Navbar.vue`: Barra de navegação principal.

## Sugestões para Expansão Futura
- Implementar autenticação real e perfis de usuário.
- Persistência de dados (backend ou localStorage).
- Fluxo de aprovação/finalização para SEDOC.
- Exportação dos mapas de competências.
- Permitir edição/remoção de atividades e conhecimentos.

## Observações Adicionais
- Os formulários são simples, sem backend ou validação real (protótipo).
- O layout e a navegação são facilmente adaptáveis para necessidades reais.
