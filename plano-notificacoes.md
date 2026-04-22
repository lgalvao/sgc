# Plano de notificações, emails e alertas

## Contexto

- No SGC, "notificação" significa notificação por email.
- O envio de email deve passar pelo outbox `NOTIFICACAO_EMAIL`.
- "Alerta" é uma mensagem interna do sistema (representada por `Alerta.java`), relacionada à notificação em vários casos de uso, mas com ciclo próprio.
- Alertas devem continuar sendo registrado na tabela `ALERTA`.
- O script incremental para DBA deve ficar separado dos DDLs oficiais (que estao em backend/etc/sql)
- O frontend fica para o final.

## 1. Fechar o modelo de dados incremental

Situação: parcialmente concluído.

- Manter um único script incremental para DBA com:
  - criação da tabela `NOTIFICACAO_EMAIL`;
  - `situacao`, tentativas, próxima tentativa e último erro;
  - vínculo opcional com `ALERTA`;
  - vínculo opcional com `SUBPROCESSO`;
  - destino pessoal por `usuario_destino_titulo`;
  - ajuste de `ALERTA` para aceitar alerta pessoal sem processo e sem unidade de destino;
  - `CHECK` que exige unidade de destino ou usuário de destino.
- Manter `backend/src/main/resources/db/schema.sql` alinhado para que os testes exercitem o modelo novo.
- Não alterar `backend/etc/sql/ddl_tabelas.sql` nem `backend/etc/sql/ddl_views.sql`.
- Revisar o script final com o DBA antes de aplicar em ambiente compartilhado.

## 2. Consolidar o backend de email

Situação: concluído.

- Mantido `EmailService` como adaptador baixo nível de SMTP.
- Mantido `NotificacaoEmailService` como entrada oficial para enfileirar, consultar e atualizar situação de emails.
- Removida dependência de `EmailService` de `SubprocessoTransicaoService` e `ProcessoService`.
- Migrado `SubprocessoTransicaoService.notificarAlteracaoDataLimite`: delegado a novo método `notificacaoService.notificarAlteracaoDataLimite()` que cria alerta, renderiza template HTML e enfileira no outbox.
- Migrado `ProcessoService.enviarLembrete`: cria alerta, gera HTML via `emailModelosService` e enfileira no outbox com `tipo_notificacao = LEMBRETE_PRAZO`.
- Cada tipo de email tem tipo, chave idempotente, assunto, template e vínculo com alerta.
- Template `data-limite-alterada.html` criado.
- Testes unitários e de integração atualizados.
  - `SubprocessoTransicaoServiceTest`: verifica `notificacaoService.notificarAlteracaoDataLimite`
  - `SubprocessoNotificacaoServiceTest`: dois novos testes para `notificarAlteracaoDataLimite`
  - `ProcessoServiceCoverageTest`, `ProcessoServiceExtraCoverageTest`, `ProcessoServiceTest`: usam `NotificacaoEmailService`
  - `CDU27IntegrationTest`: verifica outbox em vez de SMTP direto

## 3. CDU-28 e novo modelo de email

Situação: concluído no backend nesta rodada.

- Ao criar atribuição temporária, o sistema agora deve:
  - registrar a atribuição;
  - criar alerta pessoal para o usuário da atribuição;
  - enfileirar email pessoal no outbox;
  - usar `tipo_notificacao = ATRIBUICAO_TEMPORARIA`;
  - vincular a notificação ao alerta criado;
  - não vincular a processo nem subprocesso;
  - preencher `usuario_destino_titulo`;
  - usar o modelo de email especificado no CDU-28.
- O modelo de email de CDU-28 deve conter:
  - assunto `SGC: Atribuição de perfil CHEFE na unidade [SIGLA_UNIDADE]`;
  - nome do servidor;
  - sigla da unidade;
  - período da atribuição;
  - justificativa;
  - URL do sistema.
- A URL do sistema deve vir de configuração, com fallback local para testes.
- Testes esperados:
  - unitário do serviço de atribuição;
  - unitário do modelo de email;
  - integração CDU-28 confirmando atribuição, alerta pessoal e email no outbox.

## 4. Processamento do outbox

Situação: base existente, ainda precisa revisão final.

- Confirmar que o worker:
  - busca apenas `PENDENTE` e `FALHA_TEMPORARIA` vencidas;
  - marca `ENVIANDO` antes de chamar SMTP;
  - marca `ENVIADO` somente após envio real;
  - registra erro resumido em falha;
  - calcula próxima tentativa com backoff;
  - encerra em `FALHA_DEFINITIVA` após o limite.
- Avaliar concorrência:
  - para monolito e baixo volume, um worker simples é suficiente;
  - ainda assim, evitar reprocessar a mesma linha em execuções simultâneas.
- Definir política operacional:
  - frequência do worker;
  - quantidade máxima por lote;
  - limpeza ou retenção histórica.

## 5. APIs de consulta e suporte operacional

Situação: parcial.

- Manter consulta de emails por subprocesso para casos ligados ao fluxo de subprocesso.
- Criar consulta por alerta ou por usuário apenas se o frontend realmente precisar.
- Expor informação suficiente para suporte:
  - situação;
  - tentativas;
  - data de criação;
  - data de envio;
  - último erro resumido.
- Avaliar endpoint administrativo para reprocessar falhas definitivas somente se houver necessidade real.

## 6. Frontend

Situação: pendente e deixado para o final.

- Mostrar ao usuário a situação dos envios onde isso for útil, sem misturar com alertas normais.
- Para telas de subprocesso, exibir histórico de emails relacionados ao subprocesso.
- Para alertas pessoais, decidir se a situação do email aparece no detalhe do alerta ou em área administrativa.
- Evitar poluir a navegação principal com estado técnico de email.
- Usar mensagens simples:
  - pendente;
  - enviando;
  - enviado;
  - falha temporária;
  - falha definitiva.

## 7. Validação final

Situação: pendente.

- Rodar testes focados de alerta/email/outbox.
- Rodar CDU-28 integrado.
- Rodar os testes dos fluxos migrados quando `ProcessoService` e `SubprocessoTransicaoService` forem ajustados.
- Fazer um dogfooding com monitoramento ligado em um fluxo real.
- Confirmar que a request crítica não depende mais de SMTP direto nos casos migrados.

## Próxima etapa recomendada

Revisar o processamento do outbox (etapa 4), confirmar que o worker trata concorrência de forma adequada para o volume atual e definir a política de limpeza/retenção. Em seguida, expor as APIs de consulta operacional (etapa 5) e só então iniciar o frontend (etapa 6).
