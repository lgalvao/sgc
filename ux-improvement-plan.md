# Plano de Melhorias de UX - SGC
## Documento Orientado para Agentes de IA

**Data de atualização:** 2026-02-15
**Versão:** 1.1 (Refinado)
**Baseado em:** `ux-improvement-report.md`  
**Escopo:** Autenticação, Painel, Processos, Subprocessos, Mapa, Navegação, Responsividade e Relatórios

---

## 📋 Índice

1. [Visão Geral](#1-visão-geral)
2. [Contexto Arquitetural](#2-contexto-arquitetural)
3. [Melhorias Priorizadas](#3-melhorias-priorizadas)
4. [Especificações Técnicas Detalhadas](#4-especificações-técnicas-detalhadas)
5. [Checklist de Implementação](#5-checklist-de-implementação)
6. [Testes e Validação](#6-testes-e-validação)
7. [Referências Técnicas](#7-referências-técnicas)

---

## 1. Visão Geral

### 1.1 Objetivo
Padronizar e melhorar a experiência do usuário (UX) do sistema SGC através de melhorias incrementais, baseadas em evidências visuais concretas, mantendo aderência aos padrões arquiteturais do projeto.

### 1.2 Princípios Orientadores
- **Minimalismo nas mudanças:** Alterações cirúrgicas e precisas
- **Evidência visual:** Cada melhoria baseada em capturas de tela específicas
- **Consistência:** Padrões uniformes entre módulos
- **Acessibilidade:** WCAG 2.1 nível AA como mínimo
- **Responsividade:** Mobile-first com breakpoints bem definidos

### 1.3 Stack Tecnológico
- **Frontend:** Vue 3.5 + TypeScript + BootstrapVueNext
- **Padrão de componentes:** `<script setup lang="ts">`
- **Estado:** Pinia (Setup Stores: `usePerfilStore`, `useProcessosStore`, etc.)
- **Arquitetura:** View → Store → Service → API
- **Testes E2E:** Playwright (`e2e/captura-telas.spec.ts`)

---

## 2. Contexto Arquitetural

### 2.1 Estrutura de Diretórios Frontend (Atual vs Alvo)

#### Situação Atual (Realidade)
O projeto apresenta uma estrutura mista, com muitos arquivos na raiz de `components` e `views`.

```
frontend/src/
├── components/           
│   ├── atividades/      # (CadAtividadeForm, VisAtividadeItem)
│   ├── common/          # (ErrorAlert, InlineEditor)
│   ├── layout/          # (PageHeader)
│   ├── mapa/            # (CompetenciasListSection)
│   ├── processo/        # (ProcessoFormFields)
│   ├── relatorios/      # (Modais específicos)
│   ├── ui/              # (LoadingButton)
│   ├── unidade/         # (UnidadeInfoCard)
│   └── *.vue            # (26+ arquivos soltos: Modais, Tabelas, Navbars, Cards)
├── views/               
│   └── *.vue            # (20+ arquivos soltos: Cad*, Vis*, Detalhes, Painel)
```

#### Estrutura Alvo (Após UX-010)
Objetivo: Organizar views por domínio e padronizar nomes.

```
frontend/src/
├── components/           
│   ├── comum/           # Componentes genéricos (ModalPadrao, BadgeSituacao)
│   ├── layout/          # (LayoutPadrao, PageHeader, Sidebar)
│   └── [Dominio]/       # (processo, mapa, atividade...)
├── views/               
│   ├── processo/        # (ProcessoListaView, ProcessoDetalheView, ProcessoCadastroView)
│   ├── mapa/            # (MapaVisualizacaoView, MapaCadastroView)
│   ├── unidade/         # (UnidadeListaView, UnidadeDetalheView)
│   └── admin/           # (AdminView, ConfiguracoesView)
└── assets/              
    └── css/             # (tokens.css)
```

### 2.2 Convenções de Nomenclatura
- **Componentes Vue:** `PascalCase` (ex: `ProcessoInfo.vue`)
- **Arquivos TS:** `camelCase` (ex: `usuarioService.ts`)
- **Stores:** `use{Nome}Store` (ex: `useProcessosStore`)
- **Idioma:** Português Brasileiro (código, comentários, mensagens)

### 2.3 Padrões de Código Existentes
- **Props/Emits:** Componentes apresentacionais recebem Props e emitem Events
- **Erro Handling:** `useErrorHandler` composable; componentes usam `ErrorAlert` ou `FormErrorAlert`
- **Logging:** `logger.info()`, `logger.warn()`, `logger.error()`
- **Validação:** Bean Validation no backend; alguns formulários usam `fieldErrors` do `useProcessoForm`

---

## 3. Melhorias Priorizadas

### 3.1 Prioridade Alta (Impacto Imediato)

#### UX-001: Padronizar Rodapé e Semântica de Botões em Modais e Ações
**Evidências:** 
- `03-processo--02-modal-iniciar-processo.png` (via `ModalConfirmacao` em `CadProcesso.vue`)
- `03-processo--04-modal-finalizar-processo.png` (via `ModalConfirmacao` em `Processo.vue`)
- `05-mapa--07-modal-disponibilizar-mapa.png` (via `DisponibilizarMapaModal.vue`)
- `14-relatorios--02-modal-relatorio-andamento.png` (via `ModalAndamentoGeral.vue`)

**Problema:** Inconsistência na ordem e estilo de botões. Alguns usam `ModalConfirmacao` genérico, outros têm implementações próprias.

**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  
**Arquivos afetados:** 
- Criar: `components/comum/ModalPadrao.vue` 
- Refatorar: `CadProcesso.vue`, `Processo.vue`, `DisponibilizarMapaModal.vue`, `ModalAndamentoGeral.vue`.

#### UX-002: Unificar Padrão de Validação Inline
**Evidências:**
- `03-processo--10-botoes-desativados-form-vazio.png`
- `04-subprocesso--23-validacao-inline-primeira-atividade.png`

**Problema:** Validação inconsistente. Alguns usam `FormErrorAlert`, outros validação via backend apenas. Falta feedback visual imediato nos campos em alguns casos.

**Complexidade:** Alta  
**Esforço estimado:** 5-6 horas  
**Arquivos afetados:**
- `components/processo/ProcessoFormFields.vue`
- `components/atividades/CadAtividadeForm.vue`
- `components/CriarCompetenciaModal.vue`

#### UX-003: Melhorar Legibilidade de Tabelas
**Evidências:**
- `02-painel--06a-tabela-processos.png`
- `07-estados--03-tabela-com-multiplos-estados.png`

**Problema:** Layout denso e pouco contraste nos status.

**Complexidade:** Média  
**Esforço estimado:** 4-5 horas  
**Arquivos afetados:**
- `components/TabelaProcessos.vue`
- `components/TabelaMovimentacoes.vue`
- `views/HistoricoView.vue` (ou componente interno)

### 3.2 Prioridade Média

#### UX-004: Adicionar Cabeçalho Contextual por Etapa/Perfil
**Evidências:**
- `02-painel--10-painel-gestor.png`
- `04-subprocesso--01-dashboard-subprocesso.png`

**Problema:** Falta de clareza sobre "onde estou" e "o que devo fazer" para diferentes perfis.

**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  
**Arquivos afetados:** `PageHeader.vue` (existente) pode ser  evoluído ou encapsulado, e aplicado em `PainelView.vue`, `Processo.vue`, `Subprocesso.vue`.

#### UX-005: Padronizar Layout Base das Páginas
**Evidências:**
- `06-navegacao--01-menu-principal.png`

**Problema:** Estrutura das views varia ligeiramente.

**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  
**Arquivos afetado:** `App.vue`, `MainNavbar.vue`, e views principais.

#### UX-006: Fortalecer Estado Vazio com CTA Orientado
**Evidências:**
- `02-painel--06a-tabela-processos.png` (quando vazio, verificar comportamento atual)

**Problema:** Tabelas vazias não orientam o usuário.

**Complexidade:** Baixa  
**Esforço estimado:** 2-3 horas  
**Arquivos afetados:** `EmptyState.vue` (já existe, verificar uso) e componentes de tabela.

### 3.3 Prioridade Estrutural (Fundação)

#### UX-007: Criar Design Tokens
**Complexidade:** Alta  
**Esforço estimado:** 8-10 horas  
**Impacto:** Centralizar cores, espaçamentos e tipografia. Criar `frontend/src/assets/styles/_tokens.scss`.

#### UX-008: Definir Regras de Responsividade
**Evidências:**
- `08-responsividade--01-desktop-1920x1080.png` -> `04-mobile-375x667.png`

**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  
**Impacto:** Crítico para mobile. Ajustar tabelas e modais.

#### UX-009: Manter Suíte de Captura como Auditoria Visual
**Complexidade:** Baixa  
**Esforço estimado:** 2 horas  
**Arquivo:** `e2e/captura-telas.spec.ts` (já existe e está bom).

#### UX-010: Padronização de Nomenclatura e Estrutura (Refatoração)
**Complexidade:** Média
**Esforço estimado:** 6-8 horas
**Impacto:** Manutenibilidade, escalabilidade e consistência cognitiva
**Alvo:** Reorganizar views e componentes em pastas de domínio (`views/processo/`, `views/mapa/`) e padronizar nomes.

---

## 4. Especificações Técnicas Detalhadas

### 4.1 UX-001: Padronização de Botões em Modais

#### Contexto Técnico
- **Framework:** BootstrapVueNext `<BModal>`
- **Situação Atual:**
  - `ModalConfirmacao.vue`: Usado em vários lugares (`CadProcesso`, `Processo`). Precisa garantir que permite customização suficiente ou criar um wrapper padrão.
  - Modais específicos (`DisponibilizarMapaModal.vue`, `ModalAndamentoGeral.vue`) implementam seus próprios footers.
- **Padrão Desejado:** Sempre usar botões semânticos (Primary p/ ação principal, Secondary p/ cancelar) na ordem correta (Cancelar à esquerda, Ação à direita).

#### Componente Proposto: `ModalPadrao.vue` (Wrapper)
Pode encapsular `BModal` e impor o slot de `footer`.

```vue
<!-- components/comum/ModalPadrao.vue -->
<template>
  <BModal v-model="model" :title="titulo" hide-footer>
    <slot />
    <template #footer>
      <div class="d-flex justify-content-between w-100">
        <BButton variant="secondary" @click="cancelar">Cancelar</BButton>
        <BButton :variant="variantAcao" @click="confirmar" :disabled="loading">
            <BSpinner small v-if="loading" />
            <span v-else>{{ textoAcao }}</span>
        </BButton>
      </div>
    </template>
  </BModal>
</template>
```

#### Arquivos a Modificar
1.  **Criar:** `frontend/src/components/comum/ModalPadrao.vue`
2.  **Refatorar:**
    - `frontend/src/components/DisponibilizarMapaModal.vue`
    - `frontend/src/components/relatorios/ModalAndamentoGeral.vue`
    - `frontend/src/components/ModalConfirmacao.vue` (para alinhar com o padrão visual se necessário, ou fazer `ModalPadrao` usar `ModalConfirmacao` internamente se a lógica for igual).

### 4.2 UX-002: Padrão de Validação Inline

#### Contexto Técnico
- **Atual:** `ProcessoFormFields.vue` usa `props.fieldErrors` que vem do backend.
- **Desejado:** Adicionar validação client-side imediata (visual) onde possível e padronizar a exibição dos erros do backend.

#### Arquivos a Modificar
1.  `frontend/src/components/processo/ProcessoFormFields.vue`: Garantir que `BFormInput` receba o estado `:state` (booleano) corretamente baseado em `fieldErrors`.
2.  `frontend/src/components/atividades/CadAtividadeForm.vue`: Implementar visualização de erro similar.
3.  Criar `frontend/src/components/comum/CampoTexto.vue` (Opcional, mas recomendado para reduzir duplicação).

### 4.3 UX-003: Melhorar Legibilidade de Tabelas

#### Componente de Badge de Situação
Criar `components/comum/BadgeSituacao.vue` que receba a situação (string) e mapeie para cores/ícones.

```vue
<!-- components/comum/BadgeSituacao.vue -->
<script setup lang="ts">
// Mapeamento de situação do backend para cores
const mapaCores = {
  'CRIADO': 'secondary',
  'EM_ANDAMENTO': 'primary',
  'FINALIZADO': 'success',
  // ...
}
</script>
```

#### Arquivos a Modificar
1.  `frontend/src/components/TabelaProcessos.vue`: Usar `BadgeSituacao`.
2.  `frontend/src/components/TabelaMovimentacoes.vue`.

### 4.4 UX-004: Cabeçalho Contextual

#### Contexto Técnico
- O `PageHeader.vue` já existe. Pode ser estendido para aceitar breadcrumbs e sugestões de ação.
- Store: `usePerfilStore` (arquivo `frontend/src/stores/perfil.ts`) contém `perfilSelecionado` e logica `isAdmin`, `isGestor`.

#### Lógica
Criar composable `useProximaAcao.ts` que consome `usePerfilStore` e o estado do processo para sugerir ação.

### 4.5 UX-007: Design Tokens (Fundação)

Criar estrutura de CSS/SCSS em `frontend/src/assets/styles/`.
- `_variables.scss` ou `_tokens.scss`.
- Importar no `style.css` ou `main.ts`.

### 4.7 UX-010: Padronização de Nomenclatura e Estrutura

#### Regras de Organização (Domain-Driven)

Além de renomear, os arquivos devem ser movidos para diretórios contextualizados.

1.  **Views (`frontend/src/views/`)**:
    *   **Regra:** Agrupar por módulo/entidade. Evitar raiz plana.
    *   **Estrutura Alvo:**
        *   `auth/` (LoginView)
        *   `painel/` (PainelView, HistoricoView)
        *   `processo/` (ProcessoCadastroView, ProcessoDetalheView, ProcessoListaView)
        *   `subprocesso/` (SubprocessoDetalheView, AtividadeCadastroView)
        *   `mapa/` (MapaVisualizacaoView, MapaCadastroView)
        *   `unidade/` (UnidadeListaView, UnidadeDetalheView)
        *   `relatorios/` (RelatorioListaView)
        *   `admin/` (ConfiguracoesView)

2.  **Componentes (`frontend/src/components/`)**:
    *   **Regra:** Separar componentes genéricos (reutilizáveis em todo app) de componentes de domínio.
    *   **Estrutura Alvo:**
        *   `comum/` (Universal: ModalPadrao, BadgeSituacao, LoadingButton, ErrorAlert)
        *   `layout/` (Navbar, Sidebar, PageHeader)
        *   `processo/` (Específico: TabelaProcessos, FormularioProcesso)
        *   `mapa/` (Específico: CompetenciaCard, ModalMapaDisponibilizar)
        *   `unidade/` (Específico: ArvoreUnidades)

3.  **Nomenclatura (Arquivos)**:
    *   **Views:** `[Entidade][Acao]View.vue`
    *   **Modais:** `Modal[Contexto][Acao].vue`
    *   **Forms:** `[Contexto][Acao]Form.vue`

---

## 5. Checklist de Implementação

### 5.1 UX-001 (Modais)
- [ ] Criar `frontend/src/components/comum/ModalPadrao.vue`.
- [ ] Refatorar `DisponibilizarMapaModal.vue` para usar `ModalPadrao`.
- [ ] Refatorar `ModalAndamentoGeral.vue` para usar `ModalPadrao`.
- [ ] Verificar e ajustar `ModalConfirmacao.vue`.

### 5.2 UX-002 (Validação)
- [ ] Analisar `ProcessoFormFields.vue` e melhorar feedback visual de erro.
- [ ] Analisar `CadAtividadeForm.vue` e padronizar.

### 5.3 UX-003 (Tabelas)
- [ ] Criar `BadgeSituacao.vue`.
- [ ] Atualizar `TabelaProcessos.vue`.

### 5.4 UX-004 (Contexto)
- [ ] Melhorar `PageHeader.vue`.
- [ ] Criar `useProximaAcao.ts` usando `usePerfilStore`.

#### UX-007: Design Tokens (CSS Nativo)
- [ ] Criar diretório `frontend/src/assets/css`
- [ ] Criar arquivo `tokens.css` com variáveis `:root`
- [ ] Definir cores semânticas mapeadas do Bootstrap
- [ ] Definir escala de espaçamento
- [ ] Importar `tokens.css` no `style.css` ou `main.ts`
- [ ] Documentar uso das variáveis CSS

### 5.6 UX-010 (Nomenclatura e Estrutura)
- [ ] Criar script de migração `scripts/refactor-structure.ts`
- [ ] Mapear `origem -> destino` de todos os arquivos
- [ ] Rodar script para mover arquivos e atualizar imports automaticamente
- [ ] Rodar `eslint --fix` após migração
- [ ] Validar build e E2E após renomeação
- [ ] Validar build e E2E após renomeação

---

## 6. Testes e Validação

### 6.1 Testes E2E
Manter e expandir `e2e/captura-telas.spec.ts`.

### 6.2 Testes Unitários
Garantir testes para os novos componentes comuns (`ModalPadrao`, `BadgeStatus`).

---

## 7. Referências Técnicas

### 7.1 Documentação do Projeto
- **Stores:** `frontend/src/stores/perfil.ts` (Auth/Perfil), `frontend/src/stores/processos.ts`.
- **Componentes:** `frontend/src/components/`.

### 7.2 Frameworks
- **Vue 3:** https://vuejs.org/guide/
- **BootstrapVueNext:** https://bootstrap-vue-next.github.io/bootstrap-vue-next/
- **Pinia:** https://pinia.vuejs.org/
