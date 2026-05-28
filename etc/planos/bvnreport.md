# Relatório BVN (BootstrapVueNext) — Oportunidades de Reuso no SGC

## Objetivo
Mapear pontos em que o SGC pode substituir implementações manuais por recursos nativos do BootstrapVueNext (BVN), reduzindo código de infraestrutura de UI e centralizando comportamento em componentes/composables do framework.

## Fontes consultadas
- Documentação oficial do BVN: Components (índice), Breadcrumb, Autocomplete, Table e Composables.
- Código atual do frontend SGC (breadcrumbs, tabelas e buscador de usuários).

---

## 1) Breadcrumb: reduzir lógica própria com `items` e `useBreadcrumb`

### Situação atual no SGC
- O componente `BarraNavegacao.vue` renderiza `BBreadcrumb` + `BBreadcrumbItem` manualmente, com `v-for` e regras de item ativo no template.
- A construção da trilha está concentrada em um composable próprio (`useBreadcrumbs.ts`) com regras por rota/perfil e fallback em `meta.breadcrumb`/`meta.title`.

### O que o BVN já oferece
- `BBreadcrumb` aceita `items` tipado (`BreadcrumbItem[]`), incluindo `href`/`to` e estado ativo.
- O BVN também possui `useBreadcrumb` composable para trilha global por `id` (útil para páginas compostas e atualizações declarativas).

### Oportunidades práticas
1. **Trocar renderização manual por `:items` em `BBreadcrumb`**
   - Manter as regras de negócio atuais de visibilidade/perfil, mas converter a saída para `BreadcrumbItem[]`.
   - Benefício: simplificação de template, menos detalhes de marcação/estado ativo para manter.

2. **Avaliar adoção gradual de `useBreadcrumb`**
   - Começar por rotas com trilha mais dinâmica (ex.: páginas de subprocesso com etapas).
   - Benefício: diminuir acoplamento entre componentes de layout e regras de composição de trilha.

3. **Padronizar contrato de metadados de rota**
   - Preservar `meta.breadcrumb`, mas consolidar convenção para strings/funções e eliminar duplicações de fallback.

### Risco / atenção
- Há regras de autorização e contexto de rota no `useBreadcrumbs.ts`; migração deve preservar integralmente essas regras.

---

## 2) Tabelas: aproveitar melhor provider assíncrono, contexto de ordenação e estado vazio/loading do `BTable`

### Situação atual no SGC
- O projeto já usa `BTable` amplamente, porém com bastante lógica auxiliar fora da tabela:
  - Ordenação local manual em `PainelView.vue` para processos.
  - Variação de comportamentos em vários componentes (campos, formatters, clique de linha, estado vazio separado em `EmptyState` etc.).
- Há padrões repetidos de tabela em diferentes telas (painel, administração, processo, histórico).

### O que o BVN já oferece
- `BTable` suporta **provider assíncrono** com contexto (paginação, ordenação e filtro) para fluxo server-side.
- Recursos nativos de feedback e estado da tabela (`busy`, mensagens de vazio/filtrado, slots dedicados).
- Eventos/contexto de ordenação já padronizados no próprio componente.

### Oportunidades práticas
1. **Centralizar padrão de tabela em um wrapper interno (`TabelaBaseBvn`)**
   - Encapsular configuração comum: estilo, `responsive`, `hover`, mensagens padrão, estado de carregamento, comportamento de linha clicável e atributos de teste.
   - Benefício: reduzir duplicação e manter consistência visual/funcional.

2. **Migrar listagens candidatas para provider assíncrono do `BTable`**
   - Candidatas prioritárias:
     - Tabela de notificações administrativas (potencial para paginação/filtro/sort server-side).
     - Tabelas de processos/alertas com crescimento de volume.
   - Benefício: evitar lógica de ordenação/paginação manual em cada tela e facilitar escalabilidade.

3. **Usar estados nativos da tabela para vazio/filtrado/carregamento quando viável**
   - Hoje parte dessas mensagens é tratada por componente externo em paralelo à tabela.
   - Benefício: reduzir ramificações no template e simplificar teste.

4. **Padronizar contrato de `fields` e `formatter`**
   - Criar utilitários/fábricas para colunas recorrentes (datas, badges de status, colunas de ações).
   - Benefício: menos código repetido e menor chance de divergência entre telas.

### Risco / atenção
- Mudanças em tabelas exigem revisão de testes unitários (stubs de `BTable`) e E2E nas páginas críticas.

---

## 3) “Autocomplete”: substituir dropdown manual por padrão mais nativo de formulário

### Situação atual no SGC
- `BuscadorUsuarios.vue` implementa comportamento completo de autocomplete manualmente:
  - controle de abertura/fechamento da lista,
  - destaque por teclado,
  - navegação com setas/enter/esc,
  - renderização de lista e loading.

### O que o BVN já oferece
- O BVN **possui** componente dedicado de autocomplete (`BAutocomplete`) com lista de sugestões em dropdown.
- O componente oferece modos de seleção única e múltipla, suporte a tags e fluxo assíncrono para carregamento de opções.
- Também há suporte nativo para filtragem/controle de entrada, reduzindo a necessidade de implementar manualmente teclado, foco e seleção.

### Oportunidades práticas
1. **Migrar `BuscadorUsuarios.vue` para `BAutocomplete` (piloto)**
   - Aplicar primeiro em uma tela administrativa e validar equivalência funcional (busca, seleção, destaque, navegação por teclado).
   - Benefício: redução direta de código customizado de interação.

2. **Padronizar contrato de busca assíncrona**
   - Definir um adaptador único (ex.: `{label, value}` + função async de busca) para conectar serviços atuais ao `BAutocomplete`.
   - Benefício: reaproveitamento em múltiplos formulários sem duplicação.

3. **Usar `BTable` apenas quando a UX exigir densidade de dados**
   - Para cenários simples de seleção rápida, priorizar `BAutocomplete`; manter modal+tabela para buscas avançadas.
   - Benefício: separar claramente quando usar seleção leve vs. consulta rica.

### Risco / atenção
- A migração deve preservar os atributos de teste (`data-testid`) e os contratos de emissão/model (`v-model`) já utilizados nas telas e testes.

---

## 4) Plano de ação sugerido (incremental)

1. **Fase 1 — Quick wins (baixo risco)**
   - Breadcrumb com `:items` no `BBreadcrumb`.
   - Wrapper `TabelaBaseBvn` para padronizar props e slots comuns.

2. **Fase 2 — Ganhos estruturais**
   - Provider assíncrono em tabelas administrativas de maior volume.
   - Migração gradual de ordenações/filtros manuais para contexto nativo do `BTable`.

3. **Fase 3 — Busca/autocomplete**
   - Introduzir componente base de busca assíncrona de usuário.
   - Aplicar em telas atuais e impedir novas implementações customizadas fora do padrão.

---

## Priorização recomendada

- **Alta prioridade:** Tabelas (maior potencial de redução de código duplicado e melhoria de escalabilidade).
- **Média prioridade:** Breadcrumb (ganho rápido de simplificação).
- **Média/baixa prioridade:** Autocomplete (depende de decisão UX e padronização interna).

---

## Resultado esperado
Com essas mudanças, o SGC tende a:
- reduzir código de infraestrutura de UI;
- concentrar comportamento em recursos nativos do BVN;
- diminuir custo de manutenção e divergência entre telas;
- facilitar testes e evolução de funcionalidades de listagem/navegação.
