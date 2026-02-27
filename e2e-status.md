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
| CDU-08 | ✅ Passou | Manter Cadastro de Atividades. **FIX**: Corrigido erro de `IllegalArgumentException` no backend ao acessar propriedades aninhadas (`atividade.codigo`) em critérios de busca. |
| Smoke | ✅ Passou | Teste de fumaça cobrindo principais fluxos do sistema. |
