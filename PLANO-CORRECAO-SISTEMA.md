Problema
Corrigir falhas nos testes E2E com base no guia de correção e learnings do projeto, priorizando causa raiz e mudanças mínimas.

Abordagem
1) Executar os testes E2E com saída capturada em arquivo.
2) Analisar os cenários com falha e seus error-context.md.
3) Aplicar correções cirúrgicas (preferencialmente nos testes/helpers; no backend/frontend apenas se necessário).
4) Reexecutar os testes afetados e depois validar a suíte necessária.

Notas
- Não usar aumento indiscriminado de timeout.
- Não executar cenário isolado quando houver dependência serial.
- Preferir helpers centralizados e seletores robustos.

Progresso Atual
- Diagnóstico concluído com log consolidado (`test_output_run.txt` e `test_output_fix.txt`).
- Correções aplicadas em helpers e múltiplos cenários (login multi-perfil, strict mode/toasts, seleção de filtros, autenticação explícita por cenário, navegação de subprocesso).
- Próximo passo: reexecutar a suíte alvo de arquivos corrigidos e ajustar os cenários remanescentes de preparo (principalmente fluxos de CHEFE e GESTOR por hierarquia).
- Estado atual: remanescem falhas com forte indício de bug de sistema (subprocesso não encontrado, painel vazio para ator elegível e carregamento infinito em detalhes do processo).
- Correção de sistema iniciada no backend:
  - `PainelController`: validação rígida para exigir `unidade` em perfis não-ADMIN (sem fallback).
  - `SubprocessoCrudController`: busca estrita por processo+unidade solicitada (sem fallback para descendentes).
  - `PainelFacade`: removido fallback silencioso de link de destino (erros agora propagam explicitamente).
  - Testes backend de controller atualizados e passando.
- Premissa confirmada: dados de `Usuario`, `Unidade` e perfis vêm completos das views pre-populadas; tratar ausência desses dados como erro grave, nunca com fallback.
- Correção aplicada em `CadAtribuicao.vue`: removido comportamento silencioso no submit; agora exibe erro explícito quando faltam usuário/campos obrigatórios ou unidade não carregada.
- Ajuste adicional: para unidade ausente, comportamento agora é **fail-fast** (`Invariante violada`) em vez de mensagem de fallback, alinhado com a premissa de dados obrigatórios vindos das views.
- Próximo passo imediato: consolidar triagem de erros manuais do PDF e fechar causa raiz dos cenários de visibilidade do GESTOR no painel.
- Observação de triagem: parte dos erros manuais pode já estar resolvida ou ser falso positivo; validar cada item contra a base atual (`e2e/setup/seed.sql`) antes de implementar novas correções.
