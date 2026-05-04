# Plano de Limpeza Estrutural do Frontend

## 1. Objetivo

Reduzir cruft e complexidade acidental no frontend do SGC através de cortes pequenos e incrementais. O foco é aumentar a
coesão do código, estreitar a superfície de APIs internas (services, composables) e eliminar código morto/órfão, sem
quebrar contratos, requisitos ou regras de acesso.

## 2. Arquivos Chave & Contexto

- **Backlog Base:** `backlog-limpezas.md`
- **Prioridade 0 (Ganhos Rápidos):**
    - `frontend/src/composables/useLocalStorage.ts`
    - `frontend/src/constants/situacoes.ts`
    - `frontend/src/utils/styleUtils.ts`
    - `frontend/src/utils/formatters.ts`
    - `frontend/package.json` (dependência `zod`)
- **Prioridade 1 (Hotspots Críticos):**
    - `frontend/src/utils/dateUtils.ts`
    - `frontend/src/utils/apiError.ts`
    - `frontend/src/services/processoService.ts`
    - `frontend/src/composables/useAcesso.ts`
    - `frontend/src/stores/subprocesso.ts`

## 3. Etapas de Implementação

### Fase 1: Prioridade 0 - Ganhos Pequenos e Seguros

- **Remover API pública sem uso:** Eliminar exports mortos identificados em arquivos utilitários e constantes para
  reduzir a superfície do código sem abrir grandes refatorações.
- **Auditoria de Dependências:** Confirmar se `zod` está realmente sem uso no frontend (como apontado pelo `fallow`). Se
  confirmado, remover do `package.json`.

### Fase 2: Prioridade 1 - Hotspots com Melhor Custo/Benefício

Esta fase focará em arquivos que estão sob waivers de cruft ou apresentam alta complexidade:

- **`dateUtils.ts`:** Separar responsabilidades de parsing, formatação, cálculos e validações. Eliminar exports mortos.
- **`apiError.ts`:** Separar classificação HTTP de formatação para UI, removendo helpers não consumidos (
  `existsOrFalse`, `getOrNull`).
- **`processoService.ts`:** Fatiar operações de leitura vs ações. Endurecer contratos e remover APIs sem consumidor (
  `obterProcessoPorCodigo`, etc).
- **`useAcesso.ts`:** Extrair acesso específico de cadastro e mapa, isolando ações em bloco para reduzir o escopo do
  arquivo.
- **`stores/subprocesso.ts`:** Separar o cache curto e consultas derivadas, garantindo que o store fique mais enxuto.

### Fase 3: Ratchet e Manutenção de Estado

- A cada arquivo alterado, removeremos eventuais compatibilidades e fallbacks passados.
- Se a mudança fizer um arquivo voltar ao target original, removeremos seu waiver correspondente em
  `frontend-cruft-waivers.json` de forma imediata (ratchet).

## 4. Verificação e Testes

Após cada corte curto, as seguintes validações deverão ser executadas para garantir a integridade dos contratos e do
build:

- Executar os gates locais de qualidade:
  ```bash
  node etc/scripts/sgc.js frontend cruft validar
  npx fallow dead-code -r frontend
  npx fallow dupes -r frontend
  ```
- Validar se os test-ids colidem ou se quebram:
  ```bash
  node etc/scripts/frontend/test-ids-duplicados.js
  ```
- Garantir a compilação e os testes da aplicação:
  ```bash
  npm run typecheck
  npm run lint
  pnpm -C frontend exec vitest run --reporter=dot
  ```
- Rodar o Playwright nos fluxos E2E que dependem das páginas tocadas pelas lógicas alteradas.
