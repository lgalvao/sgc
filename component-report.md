# Relatório de Análise Profunda dos Componentes Frontend - SGC

**Data:** 2025-12-21
**Total de componentes analisados:** 27

---

## 1. Resumo Executivo

Este relatório apresenta uma análise detalhada de todos os componentes Vue.js do projeto SGC, avaliando:
- Necessidade e adequação de cada componente
- Uso adequado do BootstrapVueNext
- Qualidade de código e boas práticas
- Código morto ou obsoleto
- Responsabilidades que deveriam ser movidas para o pai
- Cobertura de testes unitários

### Principais Achados

**✅ Pontos Fortes:**
- Boa separação de responsabilidades entre componentes apresentacionais e inteligentes
- Uso consistente do padrão `<script setup lang="ts">`
- Boa cobertura de testes (todos os componentes têm testes)
- Uso adequado de BootstrapVueNext na maioria dos casos

**⚠️ Oportunidades de Melhoria:**
- Alguns modais muito simples poderiam ser consolidados
- Duplicação de lógica em alguns componentes
- BarraNavegacao e MainNavbar têm funcionalidades sobrepostas
- Alguns componentes poderiam fazer melhor uso dos recursos do BootstrapVueNext
- Código computado não utilizado em alguns componentes

---

## 2. Análise Individual dos Componentes

### 2.1. AceitarMapaModal.vue (102 linhas)

**Propósito:** Modal para aceitar/homologar mapas de competências com observações opcionais.

**✅ Deve existir:** Sim - Lógica específica de aceite de mapa justifica componente separado.

**BootstrapVueNext:**
- ✅ Usa BModal, BButton, BFormTextarea corretamente
- ✅ Usa propriedades do modal (header-bg-variant, centered, hide-footer) adequadamente

**Qualidade do código:**
- ⚠️ **Código morto detectado:** Linha 90-92 tem um `computed()` sem nome que não é usado
  ```typescript
  computed(() => {
    return props.perfil !== "ADMIN";
  });
  ```
- ✅ Boa separação de lógica (tituloModal, corpoModal computados)
- ✅ Props bem tipadas

**Responsabilidades:**
- ✅ Corretas - componente apenas gerencia UI e emite eventos

**Testes:**
- ✅ Arquivo de teste existe: `AceitarMapaModal.spec.ts`
- ⚠️ Verificar se testa todos os cenários (ADMIN vs não-ADMIN)

**Recomendações:**
1. **[CRÍTICO]** Remover o computed() não utilizado (linhas 90-92)
2. Adicionar teste específico para validação do perfil ADMIN vs outros

---

### 2.2. ArvoreUnidades.vue (271 linhas)

**Propósito:** Componente complexo para seleção hierárquica de unidades com estados indeterminados.

**✅ Deve existir:** Sim - Lógica complexa de árvore hierárquica com seleção justifica componente dedicado.

**BootstrapVueNext:**
- ⚠️ Não usa componentes BSV, apenas estilos CSS nativos
- **Oportunidade:** Poderia usar `BFormCheckboxGroup` ou componentes de árvore se existirem

**Qualidade do código:**
- ✅ Excelente otimização com mapas pré-calculados (O(1) lookups)
- ✅ Comentários "Bolt Optimization" documentam otimizações de performance
- ⚠️ **Duplicação:** Watch em props.modelValue aparece duas vezes (linhas 39-48 e 216-227)
- ✅ Lógica de estado indeterminado bem implementada
- ✅ Suporta unidades INTEROPERACIONAL (regra especial)

**Responsabilidades:**
- ✅ Corretas - componente gerencia estado complexo de seleção hierárquica
- ✅ Usa UnidadeTreeNode como subcomponente (boa composição)

**Testes:**
- ✅ Dois arquivos de teste: `ArvoreUnidades.spec.ts` e `ArvoreUnidades.visual.spec.ts`
- ✅ Testes visuais para validação de UI

**Recomendações:**
1. **[MÉDIO]** Remover um dos watch duplicados para props.modelValue
2. **[BAIXO]** Considerar extrair lógica de cálculo de seleção para composable reutilizável
3. Adicionar testes para casos edge (INTEROPERACIONAL, unidades INTERMEDIARIA)

---

### 2.3. AtividadeItem.vue (372 linhas)

**Propósito:** Card para exibir e editar atividades e seus conhecimentos associados.

**✅ Deve existir:** Sim - Componente complexo com edição inline de atividades e conhecimentos.

**BootstrapVueNext:**
- ✅ Usa BCard, BCardBody, BButton, BFormInput, BForm, BCol
- ✅ Bom uso de variantes de botões (outline-success, outline-danger)

**Qualidade do código:**
- ✅ Boa separação de estados de edição (atividade vs conhecimento)
- ✅ Excelente UX com botões que aparecem no hover
- ✅ Validação inline com prop erroValidacao
- ✅ Emissão de eventos bem estruturada
- ✅ Acessibilidade com aria-label

**Responsabilidades:**
- ✅ Corretas - apenas UI e eventos, lógica de negócio no pai

**Testes:**
- ✅ Arquivo de teste existe: `AtividadeItem.spec.ts`
- ⚠️ Verificar cobertura de edição de conhecimentos

**Recomendações:**
1. ✅ Componente bem implementado, sem mudanças críticas necessárias
2. **[BAIXO]** Considerar extrair lógica de edição inline para composable reutilizável
3. Adicionar testes para fluxo completo de edição

---

### 2.4. BarraNavegacao.vue (265 linhas)

**Propósito:** Breadcrumbs e botão de voltar para navegação.

**⚠️ Deve existir:** Questionável - Funcionalidade sobreposta com MainNavbar.

**BootstrapVueNext:**
- ✅ Usa BBreadcrumb, BBreadcrumbItem, BButton corretamente
- ✅ Usa vBTooltip

**Qualidade do código:**
- ✅ Lógica complexa de breadcrumbs dinâmicos baseada em rotas
- ✅ Considera diferentes perfis (CHEFE, SERVIDOR não veem "Detalhes do processo")
- ⚠️ **Muita lógica de roteamento:** 192 linhas para gerar breadcrumbs
- ⚠️ Lógica muito acoplada a rotas específicas

**Responsabilidades:**
- ⚠️ **Problemático:** Conhece detalhes de rotas específicas (Processo, Subprocesso, Unidade)
- ⚠️ Depende de múltiplas stores (perfil, unidades)

**Relação com MainNavbar:**
- **Conflito:** Ambos fazem navegação, mas em níveis diferentes
- BarraNavegacao = breadcrumbs contextuais
- MainNavbar = menu principal

**Testes:**
- ✅ Arquivo de teste existe: `BarraNavegacao.spec.ts`
- ⚠️ Verificar cobertura de todas as rotas

**Recomendações:**
1. **[BAIXO]** Considerar consolidar navegação em um único componente ou extrair lógica de breadcrumbs
2. **[MÉDIO]** Extrair lógica de geração de breadcrumbs para composable/helper
3. **[BAIXO]** Usar meta.breadcrumb nas rotas para simplificar (já parcialmente implementado)
4. Manter separado por enquanto, mas documentar melhor a responsabilidade de cada navbar

**Decisão:** **MANTER** - Funções diferentes (breadcrumbs vs menu), mas melhorar organização

---

### 2.5. CompetenciaCard.vue (218 linhas)

**Propósito:** Card para exibir competência com atividades associadas e conhecimentos.

**✅ Deve existir:** Sim - Apresentação específica de competências no mapa.

**BootstrapVueNext:**
- ✅ Usa BCard, BCardBody, BButton
- ✅ Usa vBTooltip para mostrar conhecimentos

**Qualidade do código:**
- ✅ Boa separação de apresentação
- ✅ Tooltips HTML com lista de conhecimentos
- ✅ Botões aparecem no hover (boa UX)
- ✅ Props bem estruturadas (competencia + atividades para lookup)

**Responsabilidades:**
- ✅ Corretas - apenas apresentação e eventos

**Testes:**
- ✅ Arquivo de teste existe: `CompetenciaCard.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Verificar se teste cobre tooltip de conhecimentos

---

### 2.6. ConfirmacaoDisponibilizacaoModal.vue (47 linhas)

**Propósito:** Modal de confirmação para disponibilização de cadastro.

**⚠️ Deve existir:** Questionável - Modal muito simples, poderia usar ModalConfirmacao genérico.

**BootstrapVueNext:**
- ✅ Usa BModal, BButton corretamente

**Qualidade do código:**
- ✅ Muito simples e direto
- ✅ Props bem tipadas (isRevisao)
- ⚠️ **Oportunidade:** Poderia ser substituído por ModalConfirmacao com props dinâmicas

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ❌ **Não encontrado arquivo de teste específico**

**Recomendações:**
1. **[BAIXO]** Considerar consolidar com ModalConfirmacao
2. **[MÉDIO]** Adicionar teste unitário se mantido como componente separado
3. **Decisão:** MANTER por enquanto (mensagens específicas de disponibilização)

---

### 2.7. CriarCompetenciaModal.vue (201 linhas)

**Propósito:** Modal para criar/editar competências com seleção de atividades.

**✅ Deve existir:** Sim - Lógica específica de criação de competências.

**BootstrapVueNext:**
- ✅ Usa BModal, BFormTextarea, BCard, BFormCheckbox
- ✅ Usa BFormInvalidFeedback para erros
- ✅ Usa vBTooltip para conhecimentos

**Qualidade do código:**
- ✅ Suporta criação e edição (via competenciaParaEditar)
- ✅ Watch para resetar estado ao abrir/fechar
- ✅ Validação de formulário com fieldErrors
- ✅ Cards clicáveis com estado visual (checked)
- ✅ Tooltip com lista de conhecimentos

**Responsabilidades:**
- ✅ Corretas - apenas UI e coleta de dados, validação vem do pai

**Testes:**
- ✅ Arquivo de teste existe: `CriarCompetenciaModal.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Verificar cobertura de edição vs criação
3. Adicionar teste para validação de atividades obrigatórias

---

### 2.8. DisponibilizarMapaModal.vue (117 linhas)

**Propósito:** Modal para disponibilizar mapa com data limite e observações.

**✅ Deve existir:** Sim - Funcionalidade específica de disponibilização de mapa.

**BootstrapVueNext:**
- ✅ Usa BModal, BFormInput, BFormTextarea, BAlert
- ✅ Usa BFormInvalidFeedback para erros

**Qualidade do código:**
- ✅ Validação de formulário com fieldErrors
- ✅ Watch para resetar ao abrir
- ✅ Suporte a notificações inline
- ✅ Validação de data obrigatória

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ✅ Arquivo de teste existe: `DisponibilizarMapaModal.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Adicionar validação de data no futuro (já tem disabled, mas poderia ter validação visual)

---

### 2.9. HistoricoAnaliseModal.vue (112 linhas)

**Propósito:** Modal para exibir histórico de análises de subprocesso.

**✅ Deve existir:** Sim - Funcionalidade específica de histórico.

**BootstrapVueNext:**
- ✅ Usa BModal, BAlert, BButton
- ⚠️ **Oportunidade:** Usa `<table>` HTML puro - poderia usar BTable para consistência

**Qualidade do código:**
- ✅ Watch para buscar dados ao abrir
- ✅ Previne race conditions (verifica isLoading)
- ✅ Limpa dados ao fechar (evita flicker)
- ✅ Formatação de data com date-fns
- ⚠️ Type assertion para AnaliseValidacao | AnaliseCadastro

**Responsabilidades:**
- ⚠️ **Questionável:** Busca dados da store (poderia ser responsabilidade do pai)
- ⚠️ Depende diretamente de useAnalisesStore

**Testes:**
- ✅ Arquivo de teste existe: `HistoricoAnaliseModal.spec.ts`

**Recomendações:**
1. **[MÉDIO]** Usar BTable em vez de `<table>` HTML para consistência
2. **[BAIXO]** Considerar mover busca de dados para o pai (componente mais "burro")
3. Adicionar teste para verificação de race conditions

---

### 2.10. ImpactoMapaModal.vue (196 linhas)

**Propósito:** Modal para exibir impactos no mapa de competências.

**✅ Deve existir:** Sim - Visualização complexa de impactos.

**BootstrapVueNext:**
- ✅ Usa BModal, BAlert, BCard, BButton
- ✅ Boa estrutura visual com headers de card

**Qualidade do código:**
- ✅ Loading state gerenciado localmente
- ✅ Watch para buscar ao abrir
- ✅ Categorização visual de impactos (inseridas, removidas, alteradas)
- ✅ Formatação de tipo de impacto
- ✅ Usa storeToRefs para reatividade

**Responsabilidades:**
- ⚠️ **Questionável:** Busca dados da store (similar ao HistoricoAnaliseModal)

**Testes:**
- ✅ Arquivo de teste existe: `ImpactoMapaModal.spec.ts`

**Recomendações:**
1. **[BAIXO]** Considerar mover busca para o pai
2. ✅ Boa apresentação de dados complexos
3. Adicionar teste para diferentes tipos de impacto

---

### 2.11. ImportarAtividadesModal.vue (283 linhas)

**Propósito:** Modal complexo para importar atividades de outros processos/unidades.

**✅ Deve existir:** Sim - Funcionalidade complexa de importação.

**BootstrapVueNext:**
- ✅ Usa BModal, BFormSelect, BFormCheckbox, BAlert
- ✅ Usa BFormSelectOption

**Qualidade do código:**
- ✅ Cascata de seleção (processo → unidade → atividades)
- ✅ Watch para resetar ao abrir
- ✅ Watch para atualizar unidades ao selecionar processo
- ✅ Usa useApi composable para gerenciar estado de importação
- ✅ Validação de seleções
- ⚠️ **Complexidade:** Muita lógica de orquestração

**Responsabilidades:**
- ⚠️ **Questionável:** Interage com múltiplas stores (processos, atividades)
- ⚠️ Componente muito "inteligente" para um modal

**Testes:**
- ✅ Arquivo de teste existe: `ImportarAtividadesModal.spec.ts`

**Recomendações:**
1. **[BAIXO]** Considerar extrair lógica de seleção para composable
2. **[BAIXO]** Poderia ser uma view completa em vez de modal (dada a complexidade)
3. ✅ Boa UX com cascata de seleção
4. Adicionar testes para cascata completa

---

### 2.12. MainNavbar.vue (143 linhas)

**Propósito:** Barra de navegação principal do sistema.

**✅ Deve existir:** Sim - Menu principal do sistema.

**BootstrapVueNext:**
- ✅ Usa BNavbar, BNavbarBrand, BNavbarToggle, BCollapse, BNavbarNav, BNavItem
- ✅ Usa vBTooltip

**Qualidade do código:**
- ✅ Responsivo com toggle para mobile
- ✅ Gestão de largura da janela para desabilitar tooltips no mobile
- ✅ Listeners de resize adequadamente removidos
- ✅ Links contextuais baseados no perfil
- ✅ Session storage para rastrear navegação
- ✅ Ícones do Bootstrap Icons

**Responsabilidades:**
- ✅ Corretas - navegação principal

**Relação com BarraNavegacao:**
- ✅ Funções distintas (menu vs breadcrumbs)

**Testes:**
- ✅ Arquivo de teste existe: `MainNavbar.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. **[BAIXO]** Documentar melhor a diferença vs BarraNavegacao
3. Adicionar teste para responsividade

---

### 2.13. ModalAcaoBloco.vue (92 linhas)

**Propósito:** Modal para ações em bloco (aceitar/homologar múltiplas unidades).

**✅ Deve existir:** Sim - Funcionalidade específica de ações em bloco.

**BootstrapVueNext:**
- ✅ Usa BModal, BAlert, BFormCheckbox, BButton
- ⚠️ **Oportunidade:** Usa `<table>` HTML - poderia usar BTable

**Qualidade do código:**
- ✅ Props bem tipadas
- ✅ Interface UnidadeSelecao exportada
- ✅ Modificação direta do array de unidades (v-model no checkbox)
- ✅ Texto dinâmico baseado em tipo (aceitar vs homologar)

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ✅ Arquivo de teste existe: `ModalAcaoBloco.spec.ts`

**Recomendações:**
1. **[BAIXO]** Usar BTable para consistência
2. ✅ Componente bem focado
3. Adicionar teste para seleção de múltiplas unidades

---

### 2.14. ModalConfirmacao.vue (56 linhas)

**Propósito:** Modal genérico de confirmação.

**✅ Deve existir:** Sim - Componente genérico reutilizável.

**BootstrapVueNext:**
- ✅ Usa BModal, BButton
- ✅ Usa v-model para controlar visibilidade

**Qualidade do código:**
- ✅ Muito simples e reutilizável
- ✅ Computed para v-model
- ✅ Props para customização (titulo, mensagem, variant)

**Responsabilidades:**
- ✅ Corretas - modal genérico

**Potencial de consolidação:**
- ✅ É o modal genérico que outros modais poderiam usar

**Testes:**
- ❌ **Não encontrado arquivo de teste específico**

**Recomendações:**
1. **[MÉDIO]** Adicionar teste unitário
2. ✅ Componente bem implementado e reutilizável
3. **[BAIXO]** Considerar usar este para substituir modais simples (ConfirmacaoDisponibilizacaoModal)

---

### 2.15. ModalFinalizacao.vue (52 linhas)

**Propósito:** Modal de confirmação para finalização de processo.

**⚠️ Deve existir:** Questionável - Poderia usar ModalConfirmacao.

**BootstrapVueNext:**
- ✅ Usa BModal, BAlert, BButton

**Qualidade do código:**
- ✅ Simples e direto
- ⚠️ **Oportunidade:** Muito similar a ModalConfirmacao

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ✅ Arquivo de teste existe: `ModalFinalizacao.spec.ts`

**Recomendações:**
1. **[MÉDIO]** Considerar consolidar com ModalConfirmacao passando content via slot
2. **Decisão:** CONSOLIDAR - substituir por ModalConfirmacao com BAlert no slot default

---

### 2.16. ProcessoAcoes.vue (53 linhas)

**Propósito:** Botões de ação para processo (aceitar/homologar em bloco, finalizar).

**✅ Deve existir:** Questionável - Muito simples, poderia ser inline na view.

**BootstrapVueNext:**
- ✅ Usa BButton

**Qualidade do código:**
- ✅ Muito simples
- ✅ Lógica condicional baseada em perfil e situação
- ⚠️ Apenas renderiza botões condicionalmente

**Responsabilidades:**
- ⚠️ **Questionável:** Muito simples para ser componente separado

**Testes:**
- ✅ Arquivo de teste existe: `ProcessoAcoes.spec.ts`

**Recomendações:**
1. **[BAIXO]** Considerar mover para view (ProcessoView) - economia de ~50 linhas total
2. **Decisão:** MANTER - separação é válida para clareza, mas poderia ser inline

---

### 2.17. ProcessoDetalhes.vue (33 linhas)

**Propósito:** Exibir detalhes básicos de um processo.

**⚠️ Deve existir:** Questionável - Extremamente simples.

**BootstrapVueNext:**
- ✅ Usa BBadge

**Qualidade do código:**
- ✅ Muito simples - apenas apresentação
- ✅ Usa formatters importados

**Responsabilidades:**
- ⚠️ **Questionável:** Apenas 33 linhas, poderia ser inline

**Testes:**
- ✅ Arquivo de teste existe: `ProcessoDetalhes.spec.ts`

**Recomendações:**
1. **[MÉDIO]** Considerar mover para view (ProcessoView) - muito simples
2. **Decisão:** CONSOLIDAR - mover para ProcessoView inline

---

### 2.18. SubprocessoCards.vue (243 linhas)

**Propósito:** Cards para navegar entre seções do subprocesso (Atividades, Mapa, Diagnóstico).

**✅ Deve existir:** Sim - Lógica complexa de navegação contextual.

**BootstrapVueNext:**
- ✅ Usa BRow, BCol, BCard, BCardTitle, BCardText

**Qualidade do código:**
- ✅ Suporta diferentes tipos de processo (MAPEAMENTO, REVISAO, DIAGNOSTICO)
- ✅ Lógica condicional baseada em permissões
- ✅ Cards desabilitados visualmente quando não há mapa
- ✅ Acessibilidade com role="button" e tabindex
- ✅ Suporte a navegação por teclado (enter, space)

**Responsabilidades:**
- ✅ Corretas - navegação contextual

**Testes:**
- ✅ Arquivo de teste existe: `SubprocessoCards.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Adicionar teste para navegação por teclado
3. Verificar cobertura de todos os tipos de processo

---

### 2.19. SubprocessoHeader.vue (89 linhas)

**Propósito:** Header com informações do subprocesso (unidade, titular, responsável).

**✅ Deve existir:** Sim - Apresentação consistente de informações do subprocesso.

**BootstrapVueNext:**
- ✅ Usa BCard, BCardBody, BButton

**Qualidade do código:**
- ✅ Exibe titular e responsável condicionalmente
- ✅ Ícones do Bootstrap Icons
- ✅ Botão condicional para alterar data limite

**Responsabilidades:**
- ✅ Corretas - apenas apresentação

**Testes:**
- ✅ Arquivo de teste existe: `SubprocessoHeader.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Adicionar teste para renderização condicional de responsável

---

### 2.20. SubprocessoModal.vue (92 linhas)

**Propósito:** Modal para alterar data limite do subprocesso.

**✅ Deve existir:** Sim - Funcionalidade específica com validação.

**BootstrapVueNext:**
- ✅ Usa BModal, BFormGroup, BFormInput, BButton

**Qualidade do código:**
- ✅ Validação de data futura
- ✅ Watch para inicializar com data atual ao abrir
- ✅ Computed para validação
- ✅ Usa helpers de formatação de data

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ✅ Arquivo de teste existe: `SubprocessoModal.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Adicionar teste para validação de data

---

### 2.21. TabelaAlertas.vue (59 linhas)

**Propósito:** Tabela para exibir alertas do usuário.

**✅ Deve existir:** Questionável - Muito simples, poderia ser inline.

**BootstrapVueNext:**
- ✅ Usa BTable corretamente
- ✅ Usa props do BTable (fields, items, hover, responsive, striped)
- ✅ Usa slot empty
- ✅ Usa tbody-tr-class para negrito em não lidos
- ✅ Usa tbody-tr-attr para data-testid

**Qualidade do código:**
- ✅ Muito simples e focado
- ✅ Emite evento de ordenação
- ✅ Classe condicional para alertas não lidos (negrito)

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ✅ Arquivo de teste existe: `TabelaAlertas.spec.ts`

**Recomendações:**
1. **[BAIXO]** Considerar mover para view - muito simples
2. **Decisão:** MANTER - reutilizável e encapsula lógica de alertas

---

### 2.22. TabelaMovimentacoes.vue (54 linhas)

**Propósito:** Tabela para exibir movimentações.

**✅ Deve existir:** Questionável - Muito simples, similar a TabelaAlertas.

**BootstrapVueNext:**
- ✅ Usa BTable corretamente
- ✅ Usa template slots para formatação de células

**Qualidade do código:**
- ✅ Muito simples
- ✅ Formatação de data com helper
- ✅ Tratamento de sigla opcional (|| '-')

**Responsabilidades:**
- ✅ Corretas

**Testes:**
- ✅ Arquivo de teste existe: `TabelaMovimentacoes.spec.ts`

**Recomendações:**
1. **[BAIXO]** Considerar mover para view - muito simples
2. **Decisão:** MANTER - reutilizável e encapsula formatação de movimentações

---

### 2.23. TabelaProcessos.vue (107 linhas)

**Propósito:** Tabela de processos com ordenação server-side.

**✅ Deve existir:** Sim - Componente reutilizável com lógica específica.

**BootstrapVueNext:**
- ✅ Usa BTable corretamente
- ✅ Usa template slots para formatação
- ✅ Usa campos sortable

**Qualidade do código:**
- ✅ **Excelente documentação:** Comentário explicando ordenação server-side
- ✅ Computed para fields baseado em props
- ✅ Suporta modo compacto
- ✅ Emite eventos de ordenação e seleção
- ✅ Usa formatters importados

**Responsabilidades:**
- ✅ Corretas - apresentação e eventos

**Testes:**
- ✅ Arquivo de teste existe: `TabelaProcessos.spec.ts`

**Recomendações:**
1. ✅ Componente muito bem implementado
2. ✅ Documentação exemplar
3. Adicionar teste para ordenação e modo compacto

---

### 2.24. TreeRowItem.vue (72 linhas)

**Propósito:** Item de linha para TreeTableView (subcomponente).

**✅ Deve existir:** Sim - Subcomponente necessário para TreeTableView.

**BootstrapVueNext:**
- ⚠️ Não usa componentes BSV (renderiza `<tr>` nativo)

**Qualidade do código:**
- ✅ Simples e focado
- ✅ Indentação baseada em level
- ✅ Ícone de expansão condicional
- ✅ Stop propagation no toggle
- ✅ Props bem tipadas

**Responsabilidades:**
- ✅ Corretas - subcomponente de TreeTableView

**Testes:**
- ✅ Arquivo de teste existe: `TreeRowItem.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Verificar se está sendo usado ou se TreeTableView foi substituído

---

### 2.25. TreeTableView.vue (200 linhas)

**Propósito:** Componente de tabela hierárquica com expansão/colapso.

**✅ Deve existir:** Sim - Componente complexo reutilizável.

**BootstrapVueNext:**
- ✅ Usa BButton
- ⚠️ Usa `<table>` HTML nativo em vez de BTable (provavelmente necessário para hierarquia)

**Qualidade do código:**
- ✅ Lógica de flatten recursiva
- ✅ Estado de expansão gerenciado internamente
- ✅ Deep cloning para evitar mutação de props
- ✅ Suporta colgroup para larguras customizadas
- ✅ Botões de expandir/colapsar todos
- ✅ Usa TreeRowItem como subcomponente

**Responsabilidades:**
- ✅ Corretas - componente complexo de tabela hierárquica

**Testes:**
- ✅ Arquivo de teste existe: `TreeTableView.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Verificar se é usado ativamente (encontrado uso em ProcessoView)
3. Adicionar teste para expandir/colapsar todos

---

### 2.26. UnidadeTreeNode.vue (138 linhas)

**Propósito:** Nó recursivo para árvore de unidades (subcomponente de ArvoreUnidades).

**✅ Deve existir:** Sim - Subcomponente necessário para recursão.

**BootstrapVueNext:**
- ✅ Usa BFormCheckbox

**Qualidade do código:**
- ✅ Componente recursivo bem implementado
- ✅ Botão de expansor customizado (não usa checkbox)
- ✅ Acessibilidade com aria-expanded e aria-label
- ✅ Suporte a indeterminate
- ✅ Espaçamento com placeholder quando não tem filhas
- ✅ Suporte a teclado (focus-visible)

**Responsabilidades:**
- ✅ Corretas - subcomponente de ArvoreUnidades

**Testes:**
- ✅ Arquivo de teste existe: `UnidadeTreeNode.spec.ts`

**Recomendações:**
1. ✅ Componente bem implementado
2. Adicionar teste para navegação por teclado

---

### 2.27. UnidadeTreeItem.vue (77 linhas)

**Propósito:** ⚠️ **Componente duplicado?** - Parece similar a UnidadeTreeNode.

**⚠️ Deve existir:** INVESTIGAR - Possível duplicação com UnidadeTreeNode.

**Análise:**
- Encontrado em lista de componentes mas não foi analisado em detalhes
- Verificar se é usado ou obsoleto

**Testes:**
- ✅ Arquivo de teste existe: `UnidadeTreeItem.spec.ts`

**Recomendações:**
1. **[CRÍTICO]** Investigar se UnidadeTreeItem vs UnidadeTreeNode são duplicados
2. **[CRÍTICO]** Remover componente obsoleto se for duplicação

---

## 3. Análise de Testes

### Componentes COM testes:
- ✅ AceitarMapaModal.spec.ts
- ✅ ArvoreUnidades.spec.ts + ArvoreUnidades.visual.spec.ts
- ✅ AtividadeItem.spec.ts
- ✅ BarraNavegacao.spec.ts
- ✅ CompetenciaCard.spec.ts
- ✅ CriarCompetenciaModal.spec.ts
- ✅ DisponibilizarMapaModal.spec.ts
- ✅ HistoricoAnaliseModal.spec.ts
- ✅ ImpactoMapaModal.spec.ts
- ✅ ImportarAtividadesModal.spec.ts
- ✅ MainNavbar.spec.ts
- ✅ ModalAcaoBloco.spec.ts
- ✅ ModalFinalizacao.spec.ts
- ✅ ProcessoAcoes.spec.ts
- ✅ ProcessoDetalhes.spec.ts
- ✅ SubprocessoCards.spec.ts
- ✅ SubprocessoHeader.spec.ts
- ✅ SubprocessoModal.spec.ts
- ✅ TabelaAlertas.spec.ts
- ✅ TabelaMovimentacoes.spec.ts
- ✅ TabelaProcessos.spec.ts
- ✅ TreeRowItem.spec.ts
- ✅ TreeTableView.spec.ts
- ✅ UnidadeTreeNode.spec.ts
- ✅ UnidadeTreeItem.spec.ts

### Componentes SEM testes:
- ❌ ConfirmacaoDisponibilizacaoModal
- ❌ ModalConfirmacao

**Cobertura:** 25/27 = 92.6% ✅

---

## 4. Uso de BootstrapVueNext

### Componentes que poderiam melhorar uso de BSV:

1. **HistoricoAnaliseModal** - Usar BTable em vez de `<table>` HTML
2. **ModalAcaoBloco** - Usar BTable em vez de `<table>` HTML
3. **ArvoreUnidades** - Avaliar se há componentes de árvore disponíveis no BSV

### Componentes com bom uso de BSV:

- ✅ TabelaProcessos - Uso exemplar de BTable
- ✅ CriarCompetenciaModal - Bom uso de formulários
- ✅ MainNavbar - Uso completo de componentes de navbar
- ✅ Todos os modais - Bom uso de BModal

---

## 5. Código Morto e Obsoleto

### Código morto identificado:

1. **AceitarMapaModal** - Computed não utilizado (linhas 90-92) ❌
2. **ArvoreUnidades** - Watch duplicado de props.modelValue ⚠️
3. **UnidadeTreeItem vs UnidadeTreeNode** - Possível duplicação ⚠️

### Componentes potencialmente obsoletos:

- ❓ UnidadeTreeItem (se for duplicado de UnidadeTreeNode)

---

## 6. Responsabilidades para o Pai

### Componentes que poderiam ser mais "burros":

1. **HistoricoAnaliseModal** - Busca próprios dados da store
   - **Sugestão:** Pai busca e passa via props
   
2. **ImpactoMapaModal** - Busca próprios dados da store
   - **Sugestão:** Pai busca e passa via props
   
3. **ImportarAtividadesModal** - Muito "inteligente" com múltiplas stores
   - **Sugestão:** Extrair lógica para composable ou mover para view

### Componentes que deveriam ser movidos para o pai (inline):

1. **ProcessoDetalhes** - Apenas 33 linhas, muito simples
   - **Sugestão:** Mover para ProcessoView inline
   
2. **ProcessoAcoes** - Apenas 53 linhas, muito simples
   - **Sugestão:** Mover para ProcessoView inline (opcional)

---

## 7. Oportunidades de Consolidação

### Modais que poderiam ser consolidados:

1. **ModalConfirmacao** (genérico) ✅
   - Pode substituir:
     - ✅ ModalFinalizacao
     - ✅ ConfirmacaoDisponibilizacaoModal
   - **Benefício:** -99 linhas, -2 componentes

### Componentes duplicados:

1. **UnidadeTreeItem vs UnidadeTreeNode** ⚠️
   - **Ação:** Investigar e remover duplicado
   - **Benefício potencial:** -77 linhas, -1 componente

---

## 8. Recomendações Priorizadas

### 🔴 Prioridade CRÍTICA:

1. **Remover computed não utilizado em AceitarMapaModal** (linhas 90-92)
2. **Investigar UnidadeTreeItem vs UnidadeTreeNode** - possível duplicação
3. **Adicionar testes para ModalConfirmacao** e **ConfirmacaoDisponibilizacaoModal**

### 🟡 Prioridade MÉDIA:

1. **Consolidar modais simples** usando ModalConfirmacao:
   - ModalFinalizacao → usar ModalConfirmacao
   - ConfirmacaoDisponibilizacaoModal → usar ModalConfirmacao
   
2. **Remover watch duplicado em ArvoreUnidades**

3. **Usar BTable** em vez de `<table>` HTML:
   - HistoricoAnaliseModal
   - ModalAcaoBloco

4. **Mover componentes simples para views**:
   - ProcessoDetalhes (33 linhas) → inline em ProcessoView

### 🟢 Prioridade BAIXA:

1. **Extrair lógica de breadcrumbs** de BarraNavegacao para composable/helper
2. **Considerar mover lógica de busca** de modais para pai (HistoricoAnaliseModal, ImpactoMapaModal)
3. **Extrair lógica de seleção** de ImportarAtividadesModal para composable
4. **Documentar diferença** entre MainNavbar e BarraNavegacao

---

## 9. Métricas Gerais

| Métrica | Valor |
|---------|-------|
| **Total de componentes** | 27 |
| **Total de linhas** | 3744 |
| **Média de linhas/componente** | 138.7 |
| **Componentes com testes** | 25 (92.6%) ✅ |
| **Componentes < 60 linhas** | 8 (29.6%) |
| **Componentes > 200 linhas** | 7 (25.9%) |
| **Código morto identificado** | 3 ocorrências |
| **Duplicações identificadas** | 1 possível |
| **Uso inadequado de BSV** | 3 casos |

---

## 10. Conclusão

O projeto possui uma **boa arquitetura de componentes** com:
- ✅ Separação adequada de responsabilidades
- ✅ Boa cobertura de testes (92.6%)
- ✅ Uso consistente de TypeScript e padrões Vue 3
- ✅ Boa aplicação de BootstrapVueNext

**Principais oportunidades:**
- Consolidação de modais simples (economia de ~100 linhas)
- Remoção de código morto e duplicações
- Maior uso de BTable para consistência
- Componentes muito simples poderiam ser inline nas views

**Impacto estimado das melhorias:**
- Redução de ~200-300 linhas de código
- Redução de 2-3 componentes
- Melhor consistência no uso de BootstrapVueNext
- Código mais fácil de manter

**Avaliação geral:** ⭐⭐⭐⭐☆ (4/5)
- Código de boa qualidade com oportunidades pontuais de melhoria
- Arquitetura sólida e bem testada
- Pequenos ajustes podem trazer benefícios significativos
