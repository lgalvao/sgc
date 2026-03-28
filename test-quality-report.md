# Pendências remanescentes — Test Quality Check (backend/src/test/)

## Atualização deste ciclo (2026-03-28)

### Melhorias executadas
1. **`SubprocessoControllerCoverageTest#validarCadastro`** agora cobre sucesso, inconsistências reais de cadastro (ex.: `SEM_MAPA`, `SEM_ATIVIDADES`) e falha inesperada de serviço.
2. **`PainelSecurityReproductionTest`** deixou de depender de código fixo de seed (`70002`) e passou a criar fixture própria de alerta em unidade alheia para validar isolamento de acesso.
3. **`SubprocessoValidacaoServiceTest#erroSituacaoNula`** foi ajustado para simular entrada inconsistente de API (via mock) em vez de cenário de entidade persistida inválida.
4. Cobertura de controller foi ampliada com cenários de **falha de validação**, **erro de negócio** e **verificação de payload/interações** em endpoints críticos.
5. Foram feitas melhorias em **20+ testes** no conjunto de suítes alvo deste checklist.
6. Foram reforçadas asserções e verificações de interação em **30+ testes adicionais** de controller, com foco em:
   - confirmação explícita das chamadas de serviço em cenários de sucesso;
   - validação de ausência de efeitos colaterais com `verifyNoMoreInteractions`;
   - endurecimento de cenários de erro com inspeção de payload de falha.
7. Em `SubprocessoControllerCoverageExtraTest`, cenários de transição e análise passaram a validar também o payload de resposta (ex.: tipo de análise retornado), reduzindo chance de falso positivo por apenas status HTTP.
8. Foi ampliada a suíte `SubprocessoControllerTest` com **20+ melhorias adicionais** (novos cenários e endurecimento de asserções) cobrindo:
   - transições completas de cadastro e revisão (devolver/aceitar/homologar/disponibilizar);
   - fluxo de sugestões e propagação de erro de negócio;
   - consultas de sugestões, histórico de validação e impactos;
   - criação de análise de validação/cadastro com casos de sucesso, erro de negócio e payload inválido.

### Investigação guiada pelo checklist
- **Dependência de seed fixa:** não foram identificadas novas ocorrências nas suítes ajustadas; os cenários permanecem com fixtures/mocks locais.
- **Paridade sucesso/falha por endpoint crítico:** ampliada nas rotas de importação, validação de cadastro, disponibilização e montagem de mapa.
- **Verificação de payload + interação:** as suítes de cobertura do `SubprocessoController` agora cobrem de forma mais consistente ambos os aspectos.
- **Rastreabilidade RN/CDU:** ainda há lacuna de marcação explícita por método de teste em parte das suítes de integração e cobertura.

## Pendências priorizadas

### Média prioridade
1. **Consolidação de suítes de controller**: `SubprocessoControllerCoverageTest` e `SubprocessoControllerCoverageExtraTest` ainda possuem sobreposição e podem ser reorganizados por endpoint/fluxo.
2. **Reorganização de suíte de serviço de processo**: `ProcessoServiceTest`, `ProcessoServiceCoverageTest` e `ProcessoServiceExtraCoverageTest` ainda podem ser unificados por domínio de comportamento (criação, atualização, transições, validação).
3. **Rastreabilidade RN/CDU por método**: embora melhorada em pontos críticos, ainda falta padronização completa de referência explícita em todos os testes de manutenção de mapa e validação.
4. **Padronização de profundidade de asserção**: replicar o padrão (status + payload + interação + ausência de interação indevida) nas demais suítes de controller fora do módulo de subprocesso.

### Baixa prioridade
4. Revisar testes de baixo valor restante (ex.: asserções muito triviais) para reduzir padding de cobertura.
5. Padronizar `@DisplayName` legados para formato comportamental consistente (Dado/Quando/Então).

---

## Checklist de verificação para próximos ciclos

- [x] Todo teste novo evita dependência de seed fixa (`data.sql`) quando o objetivo é regra de negócio/autorização.
- [x] Cada endpoint crítico possui cenário de sucesso **e** cenário de falha de regra de negócio.
- [x] Suítes de controller verificam payload relevante e interação com serviço (não apenas status HTTP).
- [ ] Toda suíte de integração possui rastreabilidade explícita para CDU/RN em `etc/reqs/` (pendência permanece fora do escopo deste ciclo).
- [x] Arquivos de teste mantêm descrições em português e focadas em comportamento observável.
- [ ] Evitar testes de getters/setters/toString sem requisito funcional explícito.
- [x] Rodar `./gradlew :backend:test` (ou recorte equivalente) após mudanças e atualizar este arquivo com apenas pendências ativas.
