# Tracking de Refatoração e Estabilização

## Status Geral
Fase 1 (Unificação Visual/Acessibilidade) concluída. Iniciando Fase 2 (Racionalização Estrutural, Desempenho e Limpeza).

---

## P0: Estabilização e Desempenho Crítico (Frontend)

### Gerenciamento de Requisições e Cache
- [ ] **Auditoria de Requests**: Mapear chamadas duplicadas no carregamento de `SubprocessoView`, `CadastroView` e `MapaView`.
- [ ] **Deduplication e Concorrência**: Garantir que requisições idênticas disparadas em paralelo sejam unificadas (via stores ou composables).
- [ ] **Invalidação Seletiva**: Substituir recargas totais por atualizações incrementais ou uso inteligente de cache local após ações de workflow.

### Suíte E2E e Alertas
- [ ] **Regressão Ampla**: Executar todos os 219+ testes para garantir integridade.
- [ ] **Limpeza de Alertas**: Confirmar estabilidade dos testes CDU-32/33 após alinhamento da RN-09.07.

---

## P1: Racionalização Estrutural (Hotspots)

### Backend: Quebra de God Classes
- [ ] **`SubprocessoConsultaService`**: Isolar cálculo de permissões (`PermissoesFluxoDto`) e extrair consultas de Mapa/Cadastro.
- [ ] **`ProcessoService`**: Isolar responsabilidades de criação e lógica de finalização/bloqueio.
- [ ] **`SubprocessoTransicaoService`**: Isolar lógicas de movimentação específicas.

### Frontend: Modularização
- [ ] **Modularização de Views**: Fatiar `CadastroView.vue` e `MapaView.vue`, movendo orquestração de modais para componentes dedicados.

### Remoção de Código Defensivo
- [ ] **Limpeza de DTOs**: Reduzir `@Nullable` e checks de `null` em `UnidadeDto` e `ProcessoDetalheDto`.
- [ ] **Simplificação Frontend**: Eliminar guards redundantes de dados garantidos pelo contrato.

---

## P2: Dívida Técnica e UX

### Qualidade e Tipagem
- [ ] **Validação Mobile**: Conferir capturas de tela (375px) para resiliência visual.
- [ ] **Tipagem**: Eliminar `any` remanescentes em hotspots de cobertura.
- [ ] **QA Dashboard**: Recuperar status verde para cobertura de branches.
