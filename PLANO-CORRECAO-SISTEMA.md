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
- Correção de cenário por hierarquia no `CDU-25`: fluxo migrado para `SECAO_211`/`COORD_21` com atores compatíveis (`CHEFE_SECAO_211` e `GESTOR_COORD_21`), eliminando falso negativo de visibilidade no painel.
- Validação E2E atualizada:
  - `cdu-25.spec.ts`: 6/6 passed
  - subset `cdu-20 + cdu-25 + cdu-34`: 19/19 passed
- Revalidação progressiva com correções de fluxo/fixtures:
  - O erro 404 em `/api/subprocessos/buscar` deixou de ser dominante após ajuste de isolamento/cleanup dos testes seriais.
  - Subset em foco (`cdu-11`, `cdu-13`, `cdu-14`) evoluiu para **2 falhas remanescentes** (de 8 iniciais nesse grupo), ambas ligadas a navegação em detalhes de processo quando o card de subprocesso não está visível diretamente.
- Próximo passo imediato: padronizar helper/navegação ADMIN para sempre resolver subprocesso alvo (tabela de unidades vs detalhe direto) e concluir o fechamento desse trio antes de avançar para `cdu-12/15/16/17/18`.
- Observação de triagem: parte dos erros manuais pode já estar resolvida ou ser falso positivo; validar cada item contra a base atual (`e2e/setup/seed.sql`) antes de implementar novas correções.
