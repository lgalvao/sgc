# Tracking de Refatoração e Estabilização

## Status Geral
Fase 1 (Unificação Visual/Acessibilidade) concluída. Iniciando Fase 2 (Racionalização Estrutural e Limpeza de Código Defensivo).

---

## P0: Estabilização e Validação Crítica

### Suíte E2E
- [ ] **Regressão Ampla**: Executar todos os 219+ testes (`npx playwright test`) para garantir que as refatorações de layout e backend não introduziram regressões.
- [ ] **Limpeza de Alertas**: Validar se as mudanças na RN-09.07 (comunicação direta) estabilizaram os testes de alerta (CDU-32/33).

---

## P1: Racionalização Estrutural (Hotspots)

### Backend: Quebra de God Classes
- [ ] **`SubprocessoConsultaService`**: Isolar cálculo de permissões (`PermissoesFluxoDto`) e extrair consultas de Mapa/Cadastro.
- [ ] **`ProcessoService`**: Mapear responsabilidades de criação/workflow e isolar lógica de finalização/bloqueio.

### Remoção de Código Defensivo
- [ ] **Varredura de DTOs**: Reduzir `@Nullable` e checks manuais de `null` em `UnidadeDto` e `ProcessoDetalheDto`.
- [ ] **Limpeza no Frontend**: Eliminar verificações redundantes de existência de dados já garantidos pelo contrato de carga de contexto.

---

## P2: Dívida Técnica e UX

### Qualidade de Código
- [ ] **Acessibilidade Mobile**: Validar via capturas de tela (375px) a legibilidade das novas listas inline de conhecimentos.
- [ ] **Tipagem em Testes**: Eliminar `any` remanescentes em hotspots de cobertura.

### QA Dashboard
- [ ] **Recuperação do Verde**: Atingir os thresholds de branch coverage no frontend sem relaxar as regras.
