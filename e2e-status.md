# Status dos Testes E2E

| CDU | Status | Observações |
|---|---|---|
| CDU-01 | ✅ Passou | Login e estrutura básica funcionais. |
| CDU-02 | ✅ Passou | Painel principal, criação e visualização de processos ok. |
| CDU-03 | ✅ Passou | Manter Processo (CRUD, validações, regras de negócio) ok. |
| CDU-04 | ✅ Passou | Iniciar Processo (subprocessos, notificações) ok. |
| CDU-05 | ✅ Passou | Processo de Revisão (fluxo completo) ok. |
| CDU-06 | ✅ Passou | Detalhar Processo (visão Admin e Gestor) ok. |
| CDU-07 | ✅ Passou | Detalhar Subprocesso (todas as visões) ok. |
| CDU-08 | ✅ Passou | Manter Cadastro de Atividades. |
| CDU-09-CDU-36 | ✅ Passou | Todos os fluxos principais e E2Es completos testados e estáveis. Correções de hooks de `cleanupAutomatico` (`cdu-17`, `cdu-21`, `cdu-33`) aplicadas para impedir que processo seja deletado prematuramente entre blocos `.serial`. A `StaleObjectStateException` em `cdu-33` corrigida assegurando término de requisições de página (`verificarPaginaPainel`).|
| Smoke | ✅ Passou | Teste de fumaça cobrindo principais fluxos do sistema. |