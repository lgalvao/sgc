# Problemas e Bloqueios

## E2E - Rate Limiting e ECONNREFUSED
Os testes E2E falhavam inicialmente devido ao "Rate Limiting" do backend, bloqueando logins consecutivos.
Implementei uma correção no `LimitadorTentativasLogin.java` para ignorar o limite nos perfis `test` e `e2e`.
No entanto, tentativas subsequentes de rodar os testes E2E resultaram em `ECONNREFUSED` ou falhas na execução em background.
O comando `npm run test:e2e` deve gerenciar o ciclo de vida do backend, mas a execução no ambiente de sandbox tem sido instável ou conflituosa com processos zumbis.

## Testes de Backend - CDU16IntegrationTest
O teste `deveSalvarAjustesComSucesso` do `CDU16IntegrationTest` falha persistentemente com HTTP 404, apesar de todos os esforços para corrigir a configuração de dados (refresh de entidades, verificação de IDs). O endpoint existe e o usuário tem permissão. Suspeito de uma questão sutil de transação ou configuração do MockMvc específica para este teste.

## Memória e Ferramentas
A ferramenta `initiate_memory_recording` falhou repetidamente, impedindo o registro de aprendizados.

## Status Atual
- Backend: 27/28 testes passando (CDU16 falha).
- Frontend: 100% testes passando.
- E2E: Status incerto devido a problemas de execução no ambiente, mas logs indicaram 149/149 passando em uma execução (que retornou código de saída 1).

Submetendo PR como WIP para salvar o progresso das correções já aplicadas (testes unitários corrigidos, rate limit bypass implementado).
