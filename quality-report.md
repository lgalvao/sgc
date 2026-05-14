# Relatório de Qualidade de Código (Frontend + Backend)

## Escopo e evidências objetivas (confirmadas)

Este diagnóstico foi reconfirmado com validações executadas no repositório:

- Baseline backend: `./gradlew --no-daemon --no-configuration-cache :backend:test` (**1794 testes passando**).
- Baseline frontend: `npm run lint`, `npm run typecheck`, `npm run test:unit` (**134 arquivos de teste e 1295 testes passando**).
- Cheiros gerais: `npm run smells:auditar`.
- Cruft frontend: `npm run frontend:cruft`.
- Cobertura/riscos backend: `node etc/scripts/sgc.js backend cobertura auditoria`.
- Cobertura/riscos frontend: `node etc/scripts/sgc.js frontend cobertura auditoria`.

## Achados principais (estado atual)

### 1) Complexidade e acoplamento ainda altos no backend

- `backend/src/main/java/sgc/processo/service/ProcessoService.java` segue como hotspot com **1471 linhas**.
- Cobertura aponta esse serviço como **P1 crítico** (impacto **611.0**, com **9 linhas** e **20 branches** sem teste).
- O serviço ainda concentra fluxo de ação em bloco, montagem de detalhe e regras de elegibilidade/permissão.

### 2) Hotspots de cobertura e clareza no frontend

- `frontend/src/views/FeedbacksAdminView.vue` permanece em prioridade alta de cobertura (**P2**, impacto **83.0**, **26 statements** sem teste).
- `frontend/src/views/ProcessoCadastroView.vue` mantém tamanho elevado (**405 linhas**) e aparece entre hotspots de cruft.
- O parser/normalização de metadados de feedback era um foco de complexidade local na view.

### 3) Robustez defensiva em excesso (com melhora relevante nesta rodada)

Da auditoria de cheiros/cruft:

- Backend: **109 DTOs** com `@Nullable`, **235** checks explícitos de null, **9** usos de `Objects.isNull/nonNull`.
- Frontend produção: **28** checks de null, **70** fallbacks defensivos, **51** blocos `catch`.
- Score de cheiros caiu para **1617 (crítico)**.
- Score de cruft frontend caiu para **462 (crítico)**.

### 4) Risco de manutenção ainda concentrado

- Backend P1: `ProcessoService`, `ValidadorDadosOrganizacionais`, `SubprocessoAcessoService`, `SubprocessoService`, `ImpactoMapaService`.
- Frontend P2/P3: `FeedbacksAdminView.vue`, `CadastroView.vue`, `MapaView.vue`, `ModalDevolucaoCadastro.vue`.
- Risco segue localizado em arquivos com múltiplas responsabilidades.

## Ações aplicadas nesta rodada (simplificação e clareza)

1. **FeedbacksAdminView simplificado por extração de responsabilidade**
    - Parsing/normalização e helpers de apresentação foram movidos para `frontend/src/views/feedbacksAdminApresentacao.ts`.
    - A SFC `FeedbacksAdminView.vue` ficou menor e mais focada em renderização/orquestração.
    - Resultado direto: redução da superfície local e melhoria de legibilidade no hotspot P2.

2. **ProcessoService com fluxo de ação em bloco mais explícito**
    - `executarAcaoEmBloco` foi simplificado para early-return por tipo de comando.
    - A validação de permissão de análise em bloco foi isolada em helper dedicado (`validarPermissaoAnaliseEmBloco`).
    - Resultado direto: menos estado temporário e ramificações implícitas no trecho crítico.

3. **Rodada de redução de nulabilidade acidental e de testes inúteis**
    - `ProcessoService` teve remoção de nulabilidade implícita em parâmetros internos de elegibilidade/permissão em bloco (remoção de `@Nullable` e uso de contexto explícito).
    - `UnidadeDto` e `NotificacaoDto` tiveram limpeza de `@Nullable` redundante em propriedades opcionais de DTO, reduzindo ruído sem alterar contrato serializado.
    - `FiltroMonitoramentoHttpTest` removeu 2 testes reflexivos de método privado (`getDeclaredMethod`/`setAccessible`) por não validarem contrato público.
    - `EmailService` ganhou construtor package-private para testabilidade e `EmailServiceTest` deixou de usar `ReflectionTestUtils.setField` para modo mock.
    - Resultado direto: redução mensurável de cheiros associados a nulabilidade e remoção de acoplamento de testes a implementação interna.

## Próximos cortes recomendados (prioridade)

1. **Fatiar `ProcessoService` por responsabilidade funcional**
    - Separar progressivamente elegibilidade/permissões de ação em bloco e montagem de detalhes para reduzir acoplamento.

2. **Continuar extrações locais em views grandes**
    - Repetir o padrão adotado em `FeedbacksAdminView` para `ProcessoCadastroView` e outros hotspots.

3. **Endurecer cobertura nos hotspots P1/P2**
    - Priorizar cenários comportamentais de borda em `ProcessoService` e `FeedbacksAdminView`.

4. **Ratcheting contínuo de cruft**
    - Reduzir gradualmente checks nulos e fallbacks defensivos em produção, evitando regressão via auditorias.

## Critérios de sucesso da continuidade

- Manter lint/typecheck/testes passando.
- Reduzir superfície e responsabilidade por arquivo sem alterar contratos HTTP/DTO e regras de acesso.
- Executar cortes pequenos com validação imediata e documentação atualizada dos achados.
