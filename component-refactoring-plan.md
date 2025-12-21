# Plano de Refatoração de Componentes Frontend - SGC

**Data de criação:** 2025-12-21  
**Baseado em:** component-report.md  
**Objetivo:** Refatoração guiada por agentes de IA para melhorar qualidade, consistência e manutenibilidade

---

## 📋 Visão Geral

Este plano detalha as tarefas de refatoração dos componentes Vue.js do SGC, organizadas por prioridade e estruturadas para execução por agentes de IA. O foco está em:

- ✅ Remoção de código morto e duplicações
- ✅ Consolidação de componentes simples
- ✅ Melhoria no uso de BootstrapVueNext
- ✅ Simplificação de componentes complexos
- ✅ Aumento de cobertura de testes

**Impacto estimado:**
- Redução de 200-300 linhas de código
- Redução de 2-3 componentes
- Melhoria na consistência e manutenibilidade
- Cobertura de testes de 92.6% → 100%

---

## 🎯 Princípios para Agentes de IA

### Regras Fundamentais

1. **Idioma:** Todo código, comentários e mensagens em **Português Brasileiro**
2. **Mudanças Mínimas:** Fazer apenas as alterações necessárias para cada tarefa
3. **Testes Primeiro:** Sempre executar testes antes e depois das mudanças
4. **Validação:** Executar `npm run typecheck` e `npm run lint` após cada mudança
5. **Commits Incrementais:** Usar `report_progress` após cada tarefa concluída

### Comandos de Validação

```bash
# Typecheck
cd /home/runner/work/sgc/sgc/frontend && npm run typecheck

# Lint
cd /home/runner/work/sgc/sgc/frontend && npm run lint

# Testes unitários
cd /home/runner/work/sgc/sgc/frontend && npm run test:unit

# Teste específico
cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ComponentName.spec.ts
```

### Estrutura de Diretórios

```
frontend/src/
├── components/          # Componentes reutilizáveis
│   ├── __tests__/      # Testes unitários
│   └── *.vue           # Componentes
├── views/              # Views (componentes de página)
└── stores/             # Pinia stores
```

---

## 🔴 FASE 1: Correções Críticas (Prioridade ALTA)

### Tarefa 1.1: Remover Computed Não Utilizado em AceitarMapaModal

**Arquivo:** `frontend/src/components/AceitarMapaModal.vue`

**Problema:** Linhas 90-92 contêm um `computed()` sem nome que não é usado.

**Código a remover:**
```typescript
computed(() => {
  return props.perfil !== "ADMIN";
});
```

**Passos para o Agente:**

1. **Ler** o arquivo `frontend/src/components/AceitarMapaModal.vue`
2. **Localizar** as linhas 90-92 com o computed não utilizado
3. **Verificar** que não há referências a esse computed no código
4. **Remover** as linhas 90-92
5. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- AceitarMapaModal.spec.ts
   ```
6. **Executar validações:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run typecheck && npm run lint
   ```
7. **Usar report_progress** com commit message: "Remove computed não utilizado de AceitarMapaModal"

**Critério de Sucesso:**
- ✅ Computed removido
- ✅ Testes passam
- ✅ Typecheck e lint sem erros

---

### Tarefa 1.2: Investigar e Resolver Duplicação UnidadeTreeItem vs UnidadeTreeNode

**Arquivos:**
- `frontend/src/components/UnidadeTreeItem.vue`
- `frontend/src/components/UnidadeTreeNode.vue`

**Problema:** Componentes aparentemente duplicados com funcionalidades similares.

**Passos para o Agente:**

1. **Ler ambos os arquivos** completamente:
   - `UnidadeTreeItem.vue`
   - `UnidadeTreeNode.vue`
   
2. **Buscar usos** de cada componente:
   ```bash
   cd /home/runner/work/sgc/sgc && grep -r "UnidadeTreeItem" frontend/src --include="*.vue" --include="*.ts"
   cd /home/runner/work/sgc/sgc && grep -r "UnidadeTreeNode" frontend/src --include="*.vue" --include="*.ts"
   ```

3. **Analisar diferenças:**
   - Comparar props, emits, estrutura
   - Identificar qual é mais completo/atualizado
   - Verificar se há recursão própria

4. **Decisão:**
   - Se forem duplicados: manter **UnidadeTreeNode** (mais completo, usa BFormCheckbox)
   - Se forem diferentes: documentar as diferenças

5. **Se forem duplicados, remover UnidadeTreeItem:**
   - Atualizar imports em `ArvoreUnidades.vue` se necessário
   - Remover `UnidadeTreeItem.vue`
   - Remover `frontend/src/components/__tests__/UnidadeTreeItem.spec.ts`
   
6. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ArvoreUnidades.spec.ts
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- UnidadeTreeNode.spec.ts
   ```

7. **Usar report_progress** com commit message apropriado

**Critério de Sucesso:**
- ✅ Apenas um componente de nó de árvore existe
- ✅ Todos os usos funcionam corretamente
- ✅ Testes passam

---

### Tarefa 1.3: Adicionar Testes para ModalConfirmacao

**Arquivo de teste:** `frontend/src/components/__tests__/ModalConfirmacao.spec.ts` (criar)

**Problema:** Componente genérico importante sem testes unitários.

**Passos para o Agente:**

1. **Ler** o componente `ModalConfirmacao.vue` para entender props e comportamento

2. **Criar** arquivo de teste seguindo padrão dos outros testes:
   ```typescript
   import { describe, it, expect } from 'vitest'
   import { mount } from '@vue/test-utils'
   import ModalConfirmacao from '../ModalConfirmacao.vue'
   ```

3. **Casos de teste a cobrir:**
   - Renderização com props padrão
   - Customização de título e mensagem
   - Customização de variant (danger, warning, etc)
   - Emissão do evento `confirmar` ao clicar em confirmar
   - Emissão do evento `cancelar` ao clicar em cancelar
   - Comportamento do v-model (modelValue)

4. **Exemplo de estrutura:**
   ```typescript
   describe('ModalConfirmacao', () => {
     it('renderiza título e mensagem', () => { ... })
     it('emite evento confirmar ao clicar no botão', async () => { ... })
     it('emite evento cancelar ao clicar no botão', async () => { ... })
     it('aplica variant corretamente', () => { ... })
   })
   ```

5. **Executar teste:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ModalConfirmacao.spec.ts
   ```

6. **Usar report_progress** com commit: "Adiciona testes unitários para ModalConfirmacao"

**Critério de Sucesso:**
- ✅ Arquivo de teste criado
- ✅ Mínimo de 4 casos de teste
- ✅ Todos os testes passam
- ✅ Cobertura adequada do componente

---

### Tarefa 1.4: Adicionar Testes para ConfirmacaoDisponibilizacaoModal

**Arquivo de teste:** `frontend/src/components/__tests__/ConfirmacaoDisponibilizacaoModal.spec.ts` (criar)

**Problema:** Componente sem testes unitários.

**Passos para o Agente:**

1. **Ler** `ConfirmacaoDisponibilizacaoModal.vue`

2. **Criar** arquivo de teste seguindo padrão

3. **Casos de teste a cobrir:**
   - Renderização em modo revisão (isRevisao=true)
   - Renderização em modo normal (isRevisao=false)
   - Texto dinâmico baseado em isRevisao
   - Emissão de evento ao confirmar
   - Emissão de evento ao cancelar

4. **Executar teste:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ConfirmacaoDisponibilizacaoModal.spec.ts
   ```

5. **Usar report_progress** com commit: "Adiciona testes unitários para ConfirmacaoDisponibilizacaoModal"

**Critério de Sucesso:**
- ✅ Arquivo de teste criado
- ✅ Testes para ambos os modos (revisão e normal)
- ✅ Todos os testes passam

---

## 🟡 FASE 2: Consolidações e Melhorias (Prioridade MÉDIA)

### Tarefa 2.1: Remover Watch Duplicado em ArvoreUnidades

**Arquivo:** `frontend/src/components/ArvoreUnidades.vue`

**Problema:** Watch em `props.modelValue` aparece duas vezes (linhas ~39-48 e ~216-227).

**Passos para o Agente:**

1. **Ler** `ArvoreUnidades.vue` completo

2. **Localizar** ambos os watch de `props.modelValue`

3. **Comparar** a lógica de ambos:
   - Verificar se são idênticos
   - Verificar se um é condicional
   - Identificar qual deve ser mantido

4. **Remover** o watch duplicado (manter o mais completo)

5. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ArvoreUnidades.spec.ts
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ArvoreUnidades.visual.spec.ts
   ```

6. **Usar report_progress** com commit: "Remove watch duplicado de props.modelValue em ArvoreUnidades"

**Critério de Sucesso:**
- ✅ Apenas um watch para props.modelValue
- ✅ Funcionalidade preservada
- ✅ Testes passam

---

### Tarefa 2.2: Consolidar ModalFinalizacao em ModalConfirmacao

**Arquivos:**
- `frontend/src/components/ModalFinalizacao.vue` (remover)
- `frontend/src/components/__tests__/ModalFinalizacao.spec.ts` (atualizar)

**Problema:** ModalFinalizacao é muito simples e pode usar ModalConfirmacao genérico.

**Passos para o Agente:**

1. **Ler** ambos os componentes:
   - `ModalFinalizacao.vue`
   - `ModalConfirmacao.vue`

2. **Buscar usos** de ModalFinalizacao:
   ```bash
   cd /home/runner/work/sgc/sgc && grep -r "ModalFinalizacao" frontend/src --include="*.vue" --include="*.ts"
   ```

3. **Para cada uso encontrado**, substituir:
   ```vue
   <!-- ANTES -->
   <ModalFinalizacao
     v-model="mostrarModalFinalizacao"
     @confirmar="handleFinalizar"
   />
   
   <!-- DEPOIS -->
   <ModalConfirmacao
     v-model="mostrarModalFinalizacao"
     titulo="Finalizar Processo"
     variant="danger"
     @confirmar="handleFinalizar"
   >
     <template #default>
       <BAlert show variant="info">
         <i class="bi bi-info-circle-fill me-2"></i>
         Atenção: Esta ação não poderá ser desfeita.
       </BAlert>
     </template>
   </ModalConfirmacao>
   ```

4. **Atualizar imports** nas views que usam ModalFinalizacao

5. **Mover testes** de ModalFinalizacao.spec.ts para testar o uso com ModalConfirmacao

6. **Remover arquivos:**
   - `ModalFinalizacao.vue`
   - `__tests__/ModalFinalizacao.spec.ts` (após mover testes relevantes)

7. **Executar todos os testes afetados**

8. **Usar report_progress** com commit: "Consolida ModalFinalizacao em ModalConfirmacao"

**Critério de Sucesso:**
- ✅ ModalFinalizacao removido
- ✅ Funcionalidade preservada usando ModalConfirmacao
- ✅ Todos os testes passam
- ✅ Redução de ~52 linhas de código

---

### Tarefa 2.3: Migrar HistoricoAnaliseModal para BTable

**Arquivo:** `frontend/src/components/HistoricoAnaliseModal.vue`

**Problema:** Usa `<table>` HTML puro em vez de BTable do BootstrapVueNext.

**Passos para o Agente:**

1. **Ler** `HistoricoAnaliseModal.vue`

2. **Identificar** a estrutura atual da tabela HTML

3. **Definir** campos para BTable:
   ```typescript
   const fields = [
     { key: 'data', label: 'Data', sortable: false },
     { key: 'analista', label: 'Analista', sortable: false },
     { key: 'tipo', label: 'Tipo', sortable: false },
     { key: 'situacao', label: 'Situação', sortable: false }
   ]
   ```

4. **Substituir** `<table>` por `<BTable>`:
   ```vue
   <BTable
     :fields="fields"
     :items="historico"
     striped
     hover
     responsive
   >
     <template #cell(data)="{ item }">
       {{ format(new Date(item.dataAnalise), 'dd/MM/yyyy HH:mm') }}
     </template>
     <!-- outros slots conforme necessário -->
   </BTable>
   ```

5. **Importar** BTable:
   ```typescript
   import { BTable } from 'bootstrap-vue-next'
   ```

6. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- HistoricoAnaliseModal.spec.ts
   ```

7. **Usar report_progress** com commit: "Migra HistoricoAnaliseModal para usar BTable"

**Critério de Sucesso:**
- ✅ BTable implementado
- ✅ Formatação preservada
- ✅ Testes passam
- ✅ Consistência com outros componentes

---

### Tarefa 2.4: Migrar ModalAcaoBloco para BTable

**Arquivo:** `frontend/src/components/ModalAcaoBloco.vue`

**Problema:** Usa `<table>` HTML puro em vez de BTable.

**Passos para o Agente:**

1. **Ler** `ModalAcaoBloco.vue`

2. **Definir** campos para BTable:
   ```typescript
   const fields = [
     { key: 'selecionado', label: '', sortable: false },
     { key: 'sigla', label: 'Sigla', sortable: false },
     { key: 'nome', label: 'Nome', sortable: false }
   ]
   ```

3. **Substituir** por BTable com checkbox:
   ```vue
   <BTable
     :fields="fields"
     :items="unidades"
     striped
     hover
   >
     <template #cell(selecionado)="{ item }">
       <BFormCheckbox v-model="item.selecionado" />
     </template>
   </BTable>
   ```

4. **Testar** seleção de múltiplas unidades

5. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ModalAcaoBloco.spec.ts
   ```

6. **Usar report_progress** com commit: "Migra ModalAcaoBloco para usar BTable"

**Critério de Sucesso:**
- ✅ BTable implementado
- ✅ Checkboxes funcionando
- ✅ Testes passam

---

### Tarefa 2.5: Mover ProcessoDetalhes Inline para ProcessoView

**Arquivos:**
- `frontend/src/components/ProcessoDetalhes.vue` (remover)
- `frontend/src/views/ProcessoView.vue` (atualizar)

**Problema:** Componente muito simples (33 linhas), pode ser inline na view.

**Passos para o Agente:**

1. **Ler** ambos os arquivos:
   - `ProcessoDetalhes.vue`
   - `ProcessoView.vue`

2. **Localizar** uso de `<ProcessoDetalhes>` em ProcessoView

3. **Copiar** o template de ProcessoDetalhes diretamente para ProcessoView:
   ```vue
   <!-- Em ProcessoView.vue, substituir: -->
   <ProcessoDetalhes :processo="processo" />
   
   <!-- Por: -->
   <div>
     <p>
       <strong>Situação:</strong>
       <BBadge :variant="formatarSituacaoCor(processo.situacao)">
         {{ formatarSituacao(processo.situacao) }}
       </BBadge>
     </p>
     <!-- resto do template -->
   </div>
   ```

4. **Importar** funções de formatação em ProcessoView se necessário:
   ```typescript
   import { formatarSituacao, formatarSituacaoCor } from '@/utils/formatters'
   ```

5. **Remover** import de ProcessoDetalhes

6. **Atualizar/remover** testes:
   - Mover testes relevantes para ProcessoView.spec.ts
   - Remover `ProcessoDetalhes.spec.ts`

7. **Remover** arquivo `ProcessoDetalhes.vue`

8. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- ProcessoView.spec.ts
   ```

9. **Usar report_progress** com commit: "Move ProcessoDetalhes inline para ProcessoView"

**Critério de Sucesso:**
- ✅ ProcessoDetalhes removido
- ✅ Funcionalidade preservada em ProcessoView
- ✅ Testes atualizados e passando
- ✅ Redução de ~33 linhas + arquivo de teste

---

## 🟢 FASE 3: Otimizações e Melhorias de Arquitetura (Prioridade BAIXA)

### Tarefa 3.1: Extrair Lógica de Breadcrumbs de BarraNavegacao

**Objetivo:** Simplificar BarraNavegacao extraindo lógica complexa para composable.

**Arquivo:** `frontend/src/components/BarraNavegacao.vue` (192 linhas de lógica de breadcrumbs)

**Passos para o Agente:**

1. **Criar** composable `useBreadcrumbs.ts`:
   ```typescript
   // frontend/src/composables/useBreadcrumbs.ts
   import { computed } from 'vue'
   import type { RouteLocationNormalizedLoaded } from 'vue-router'
   
   export function useBreadcrumbs(route: RouteLocationNormalizedLoaded) {
     const breadcrumbs = computed(() => {
       // Mover lógica de geração de breadcrumbs aqui
     })
     
     return { breadcrumbs }
   }
   ```

2. **Mover** lógica de geração de breadcrumbs do componente para o composable

3. **Atualizar** BarraNavegacao para usar o composable:
   ```typescript
   import { useBreadcrumbs } from '@/composables/useBreadcrumbs'
   
   const { breadcrumbs } = useBreadcrumbs(route)
   ```

4. **Criar** testes para o composable:
   ```typescript
   // frontend/src/composables/__tests__/useBreadcrumbs.spec.ts
   ```

5. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit
   ```

6. **Usar report_progress** com commit: "Extrai lógica de breadcrumbs para composable reutilizável"

**Critério de Sucesso:**
- ✅ Composable criado e testado
- ✅ BarraNavegacao simplificado
- ✅ Funcionalidade preservada
- ✅ Código mais reutilizável

**NOTA:** Esta tarefa é opcional e pode ser feita em uma fase posterior.

---

### Tarefa 3.2: Tornar HistoricoAnaliseModal Mais Apresentacional

**Objetivo:** Mover responsabilidade de busca de dados para o componente pai.

**Arquivo:** `frontend/src/components/HistoricoAnaliseModal.vue`

**Passos para o Agente:**

1. **Analisar** o componente atual que busca dados via store

2. **Modificar** para receber dados via props:
   ```typescript
   // ANTES
   const store = useAnalisesStore()
   watch(() => props.modelValue, async (show) => {
     if (show) {
       await store.carregarHistorico(props.codigoSubprocesso)
     }
   })
   
   // DEPOIS
   interface Props {
     modelValue: boolean
     historico: AnaliseHistorico[]  // novo prop
     loading?: boolean              // novo prop
   }
   ```

3. **Atualizar** views que usam o componente para buscar dados:
   ```vue
   <script setup lang="ts">
   const store = useAnalisesStore()
   const { historico, loading } = storeToRefs(store)
   
   watch(() => mostrarHistorico.value, async (show) => {
     if (show) {
       await store.carregarHistorico(codigoSubprocesso)
     }
   })
   </script>
   
   <template>
     <HistoricoAnaliseModal
       v-model="mostrarHistorico"
       :historico="historico"
       :loading="loading"
     />
   </template>
   ```

4. **Atualizar** testes do componente

5. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc/frontend && npm run test:unit -- HistoricoAnaliseModal.spec.ts
   ```

6. **Usar report_progress** com commit: "Torna HistoricoAnaliseModal mais apresentacional"

**Critério de Sucesso:**
- ✅ Componente não acessa store diretamente
- ✅ Dados vêm via props
- ✅ Funcionalidade preservada
- ✅ Testes atualizados

**NOTA:** Esta tarefa é opcional e requer mudanças nas views.

---

### Tarefa 3.3: Tornar ImpactoMapaModal Mais Apresentacional

**Objetivo:** Similar à Tarefa 3.2, mover responsabilidade de busca para o pai.

**Arquivo:** `frontend/src/components/ImpactoMapaModal.vue`

**Passos:** Similares à Tarefa 3.2, adaptados para ImpactoMapaModal.

**NOTA:** Esta tarefa é opcional e requer mudanças nas views.

---

### Tarefa 3.4: Documentar Diferença entre MainNavbar e BarraNavegacao

**Objetivo:** Adicionar documentação clara sobre responsabilidades de cada componente.

**Arquivos:**
- `frontend/src/components/MainNavbar.vue`
- `frontend/src/components/BarraNavegacao.vue`
- `frontend/src/components/README.md`

**Passos para o Agente:**

1. **Atualizar** README.md dos componentes:
   ```markdown
   ## Componentes de Navegação
   
   ### MainNavbar
   **Responsabilidade:** Menu principal do sistema (topo da aplicação)
   - Links para páginas principais (Home, Alertas, Movimentações)
   - Links contextuais baseados em perfil do usuário
   - Responsivo com toggle para mobile
   - Posição: Fixa no topo
   
   ### BarraNavegacao
   **Responsabilidade:** Breadcrumbs contextuais e navegação hierárquica
   - Mostra caminho atual na hierarquia (Processo → Subprocesso → Seção)
   - Botão de voltar
   - Breadcrumbs dinâmicos baseados na rota atual
   - Posição: Abaixo do MainNavbar, dentro do conteúdo
   ```

2. **Adicionar** comentários nos componentes:
   ```vue
   <!--
   MainNavbar - Menu principal do sistema
   Responsável por: navegação entre páginas principais, perfil do usuário
   -->
   ```

3. **Usar report_progress** com commit: "Documenta diferença entre MainNavbar e BarraNavegacao"

**Critério de Sucesso:**
- ✅ README atualizado
- ✅ Comentários adicionados nos componentes
- ✅ Diferenças claramente documentadas

---

## 📊 Checklist de Validação Final

Após completar todas as tarefas, executar validação completa:

### Validação de Código

```bash
# TypeScript
cd /home/runner/work/sgc/sgc/frontend && npm run typecheck

# Linting
cd /home/runner/work/sgc/sgc/frontend && npm run lint

# Todos os testes unitários
cd /home/runner/work/sgc/sgc/frontend && npm run test:unit

# Build
cd /home/runner/work/sgc/sgc/frontend && npm run build
```

### Métricas de Sucesso

- [x] **Código morto removido:** 3 ocorrências eliminadas
- [x] **Duplicações resolvidas:** UnidadeTreeItem/UnidadeTreeNode consolidado
- [x] **Cobertura de testes:** 100% (27/27 componentes)
- [x] **Componentes consolidados:** ModalFinalizacao → ModalConfirmacao
- [x] **Uso de BTable:** HistoricoAnaliseModal e ModalAcaoBloco migrados
- [x] **Componentes simplificados:** ProcessoDetalhes movido inline
- [x] **TypeCheck:** ✅ Sem erros
- [x] **Lint:** ✅ Sem erros
- [x] **Testes:** ✅ Todos passando
- [x] **Build:** ✅ Sucesso

---

## 🎯 Ordem de Execução Recomendada

### Sprint 1: Correções Críticas (1-2 dias)
1. Tarefa 1.1: Remover computed não utilizado
2. Tarefa 1.2: Resolver duplicação UnidadeTreeItem
3. Tarefa 1.3: Testes para ModalConfirmacao
4. Tarefa 1.4: Testes para ConfirmacaoDisponibilizacaoModal

### Sprint 2: Consolidações (2-3 dias)
5. Tarefa 2.1: Remover watch duplicado
6. Tarefa 2.2: Consolidar ModalFinalizacao
7. Tarefa 2.3: Migrar HistoricoAnaliseModal para BTable
8. Tarefa 2.4: Migrar ModalAcaoBloco para BTable
9. Tarefa 2.5: Mover ProcessoDetalhes inline

### Sprint 3: Otimizações (Opcional, 2-3 dias)
10. Tarefa 3.1: Extrair lógica de breadcrumbs
11. Tarefa 3.2: Tornar HistoricoAnaliseModal apresentacional
12. Tarefa 3.3: Tornar ImpactoMapaModal apresentacional
13. Tarefa 3.4: Documentar navegação

---

## 📝 Notas para Agentes de IA

### Contexto Importante

- **BootstrapVueNext:** Biblioteca de componentes UI usada no projeto
- **Pinia:** Store management (sintaxe Setup Stores com `ref` e `computed`)
- **Composition API:** Todos os componentes usam `<script setup lang="ts">`
- **Convenções:** Ver `/home/runner/work/sgc/sgc/regras/frontend-padroes.md`

### Dicas de Execução

1. **Sempre ler o código antes de modificar** - Use `view` para ler arquivos
2. **Buscar padrões existentes** - Use `grep` para encontrar exemplos no código
3. **Testar incrementalmente** - Executar testes após cada mudança
4. **Fazer commits pequenos** - Usar `report_progress` frequentemente
5. **Validar antes e depois** - Executar typecheck e lint sempre

### Comandos Úteis de Busca

```bash
# Buscar uso de componente
cd /home/runner/work/sgc/sgc && grep -r "ComponentName" frontend/src --include="*.vue" --include="*.ts"

# Buscar padrão em testes
cd /home/runner/work/sgc/sgc && grep -r "describe.*Modal" frontend/src/components/__tests__ --include="*.spec.ts"

# Listar todos os componentes
cd /home/runner/work/sgc/sgc && ls -la frontend/src/components/*.vue

# Verificar imports
cd /home/runner/work/sgc/sgc && grep -r "from.*BootstrapVueNext" frontend/src --include="*.vue"
```

---

## 🔍 Referências

- **Relatório base:** `/home/runner/work/sgc/sgc/component-report.md`
- **Padrões frontend:** `/home/runner/work/sgc/sgc/regras/frontend-padroes.md`
- **Padrões backend:** `/home/runner/work/sgc/sgc/regras/backend-padroes.md`
- **README do projeto:** `/home/runner/work/sgc/sgc/README.md`

---

## ✅ Conclusão

Este plano fornece uma roadmap completa e estruturada para refatoração dos componentes frontend do SGC. Cada tarefa é independente e pode ser executada por agentes de IA com instruções claras, critérios de sucesso e comandos de validação.

**Resultado esperado:**
- Código mais limpo e manutenível
- Melhor consistência no uso de BootstrapVueNext
- 100% de cobertura de testes
- Redução de código duplicado e morto
- Componentes mais focados e reutilizáveis

**Próximos passos:**
1. Revisar e aprovar este plano
2. Executar tarefas da Fase 1 (críticas)
3. Validar resultados
4. Prosseguir com Fases 2 e 3 conforme prioridade
