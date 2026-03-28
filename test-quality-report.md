# Pendências remanescentes — Test Quality Check (backend/src/test/)

## Atualização deste ciclo (2026-03-28)

### Melhorias executadas
1. **`SubprocessoControllerCoverageTest#validarCadastro`** agora cobre sucesso, inconsistências reais de cadastro (ex.: `SEM_MAPA`, `SEM_ATIVIDADES`) e falha inesperada de serviço.
2. **`PainelSecurityReproductionTest`** deixou de depender de código fixo de seed (`70002`) e passou a criar fixture própria de alerta em unidade alheia para validar isolamento de acesso.
3. **`SubprocessoValidacaoServiceTest#erroSituacaoNula`** foi ajustado para simular entrada inconsistente de API (via mock) em vez de cenário de entidade persistida inválida.
4. Cobertura de controller foi ampliada com cenários de **falha de validação**, **erro de negócio** e **verificação de payload/interações** em endpoints críticos.
5. Foram feitas melhorias em **20+ testes** no conjunto de suítes alvo deste checklist.

## Pendências priorizadas

### Média prioridade
1. **Consolidação de suítes de controller**: `SubprocessoControllerCoverageTest` e `SubprocessoControllerCoverageExtraTest` ainda possuem sobreposição e podem ser reorganizados por endpoint/fluxo.
2. **Reorganização de suíte de serviço de processo**: `ProcessoServiceTest`, `ProcessoServiceCoverageTest` e `ProcessoServiceExtraCoverageTest` ainda podem ser unificados por domínio de comportamento (criação, atualização, transições, validação).
3. **Rastreabilidade RN/CDU por método**: embora melhorada em pontos críticos, ainda falta padronização completa de referência explícita em todos os testes de manutenção de mapa e validação.

### Baixa prioridade
4. Revisar testes de baixo valor restante (ex.: asserções muito triviais) para reduzir padding de cobertura.
5. Padronizar `@DisplayName` legados para formato comportamental consistente (Dado/Quando/Então).

---

## Checklist de verificação para próximos ciclos

- [x] Todo teste novo evita dependência de seed fixa (`data.sql`) quando o objetivo é regra de negócio/autorização.
- [x] Cada endpoint crítico possui cenário de sucesso **e** cenário de falha de regra de negócio.
- [x] Suítes de controller verificam payload relevante e interação com serviço (não apenas status HTTP).
- [ ] Toda suíte de integração possui rastreabilidade explícita para CDU/RN em `etc/reqs/`.
- [x] Arquivos de teste mantêm descrições em português e focadas em comportamento observável.
- [ ] Evitar testes de getters/setters/toString sem requisito funcional explícito.
- [x] Rodar `./gradlew :backend:test` (ou recorte equivalente) após mudanças e atualizar este arquivo com apenas pendências ativas.
