# SGC - Backlog de Limpezas e Qualidade (Estado Atual)

Este documento reflete o estado atual do sistema após rodadas de refatoração do frontend.

## 📊 Métricas Atuais 

| Métrica | Valor Atual | Meta / Limite | Status |
| :--- | :--- | :--- | :--- |
| **Complexidade (Smells)** | 1636 | < 1000 | 🟠 Crítico |
| **Cruft Frontend (Score)** | 2341.5 | < 2000 | 🟠 Crítico |
| **Cobertura (Funções)** | 85.51% | 90.00% | 🟡 Alerta |
| **Cobertura (Branches)** | 84.11% | 85.00% | 🟡 Alerta |
| **Testes Unitários** | 1257 Passando | 100% | 🟢 OK |
| **E2E (Captura)** | 17 Passando | 100% | 🟢 OK |

### 🛠️ Hotspots Identificados (Top Prioridade)

1.  **frontend/src/constants/textos.ts**: 438 pts (452 linhas). Centraliza strings mas cresceu demais.
2.  **frontend/src/views/MapaView.vue**: 194 pts (442 linhas). Concentra fluxo de mapa, mutações e exibição.
3.  **frontend/src/components/comum/TreeTable.vue**: 190 pts (360 linhas). Componente complexo de visualização.
4.  **frontend/src/views/CadastroView.vue**: 165.5 pts (423 linhas). Lógica densa de formulário.
5.  **frontend/src/axios-setup.ts**: 147 pts (248 linhas). Interceptores e tratamentos globais.

---

## 🚀 Próximas Ações

### P2 - Próximos Passos
- [ ] **Ajuste de Cobertura (Gaps)**: Atacar arquivos com baixa cobertura de funções (ex: `UnidadeView.vue` e `analiseFluxo.ts`) para atingir os 90%.

### P2 - Qualidade e Ratcheting
- [x] **Ratcheting de Waivers**: Reduzir os limites em `frontend-cruft-waivers.json`.
    - [x] `ArvoreUnidades.vue`: Refatorado (de 471 para 194 linhas). Waiver reduzido para 200.
    - [x] `useArvoreSelecao.ts`: Limpeza estrutural realizada (de 214 para 166 linhas). Waiver removido (dentro do budget).
    - [x] `axios-setup.ts`: Reduzido para 250 (atual: 248).
    - [x] `ProcessoCadastroView.vue`: Reduzido para 460 (atual: 456).
- [ ] **Simplificação de Erros**: Unificar redundância no `useErrorHandler.ts` entre erros de rede e de negócio via `normalizarErro`.