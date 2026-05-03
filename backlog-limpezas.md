# SGC - Backlog de Limpezas e Qualidade (Estado Atual)

Este documento reflete o estado atual do sistema após rodadas de refatoração do frontend.

## 📊 Métricas Atuais 

| Métrica | Valor Atual | Meta / Limite | Status |
| :--- | :--- | :--- | :--- |
| **Complexidade (Smells)** | 1641 | < 1000 | 🟠 Crítico |
| **Cruft Frontend (Score)** | 2977.5 | < 2000 | 🟠 Crítico |
| **Cobertura (Funções)** | 86.43% | 90.00% | 🟡 Alerta |
| **Cobertura (Branches)** | 84.90% | 85.00% | 🟢 Quase lá |
| **Testes Unitários** | 1257 Passando | 100% | 🟢 OK |
| **E2E (Captura)** | 17 Passando | 100% | 🟢 OK |

### 🛠️ Hotspots Identificados (Top Prioridade)

1.  **frontend/src/components/unidade/ArvoreUnidades.vue**: 365.5 pts (471 linhas). Mistura renderização recursiva com lógica de busca.
2.  **frontend/src/views/ProcessoCadastroView.vue**: 234.5 pts (456 linhas). Ainda centraliza muita orquestração apesar das melhorias no form.
3.  **frontend/src/views/NotificacoesAdminView.vue**: 220.5 pts (461 linhas). Combina filtros complexos com uma tabela de logs extensa.
4.  **frontend/src/views/MapaView.vue**: 194 pts (442 linhas). Concentra fluxo de mapa, mutações e exibição.

---

## 🚀 Próximas Ações

### P2 - Próximos Passos
- [ ] **Ajuste de Cobertura (Gaps)**: Atacar arquivos com baixa cobertura de funções (ex: `UnidadeView.vue` e `analiseFluxo.ts`) para atingir os 90%.

### P2 - Qualidade e Ratcheting
- [ ] **Ratcheting de Waivers**: Reduzir os limites em `frontend-cruft-waivers.json`.
    - [ ] `ProcessoFormFields.vue`: Remover waiver (atual: 95 linhas, limite normal: 200).
    - [ ] `axios-setup.ts`: Reduzir para 250 (atual: 247).
    - [ ] `ProcessoCadastroView.vue`: Reduzir para 460 (atual: 456).
- [ ] **Simplificação de Erros**: Unificar redundância no `useErrorHandler.ts` entre erros de rede e de negócio via `normalizarErro`.