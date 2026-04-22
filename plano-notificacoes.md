# Plano de notificações, emails e alertas

## Diretrizes gerais

- No SGC, "notificação" significa notificação por email.
- "Alerta" é uma mensagem interna do sistema, representada por `Alerta.java`, relacionada à notificação em vários casos de uso, mas com ciclo próprio.
- Todo envio de email deve passar pelo outbox `NOTIFICACAO_EMAIL`; não criar novos envios SMTP diretos em services de negócio.
- Alertas devem continuar sendo registrados na tabela `ALERTA`.
- O script incremental para DBA deve ficar separado dos DDLs oficiais, em `backend/etc/sql`.
- `backend/src/main/resources/db/schema.sql` deve continuar alinhado ao modelo usado nos testes.
- Não alterar `backend/etc/sql/ddl_tabelas.sql` nem `backend/etc/sql/ddl_views.sql` neste trabalho.
- O frontend fica para o final e deve expor estado técnico de email apenas onde isso ajudar o usuário ou o suporte.

## Estado atual

- O backend já possui outbox de email com `NotificacaoEmailService`, `NotificacaoEmailWorker`, entidade `NotificacaoEmail` e endpoint de consulta por subprocesso.
- `EmailService` permanece como adaptador baixo nível de SMTP.
- Fluxos já migrados para o outbox:
  - alteração de data limite de subprocesso;
  - lembrete de prazo;
  - atribuição temporária do CDU-28.
- CDU-27 e CDU-28 possuem cobertura integrada confirmando uso do outbox.
- Testes focados de alerta/email/outbox e CDU-27/CDU-28 passaram em 2026-04-22.

## Pendências

### 1. Revisar processamento do outbox

- Confirmar que o worker processa apenas notificações `PENDENTE` e `FALHA_TEMPORARIA` com `proxima_tentativa_em` vencida.
- Garantir que a notificação seja marcada como `ENVIANDO` antes da chamada SMTP.
- Marcar `ENVIADO` somente após envio real.
- Registrar erro resumido quando houver falha.
- Manter retry com backoff e encerrar em `FALHA_DEFINITIVA` após o limite de tentativas.
- Revisar concorrência para evitar que execuções simultâneas processem a mesma linha.
- Definir política operacional:
  - intervalo do worker;
  - tamanho máximo do lote;
  - retenção ou limpeza histórica;
  - ação manual esperada para `FALHA_DEFINITIVA`.

### 2. Fechar suporte operacional

- Manter consulta de emails por subprocesso para casos ligados ao fluxo de subprocesso.
- Criar consulta por alerta ou por usuário apenas se houver necessidade real do frontend ou do suporte.
- Garantir que as consultas exponham:
  - situação;
  - tentativas;
  - data de criação;
  - data de envio;
  - próximo reprocessamento;
  - último erro resumido.
- Avaliar endpoint administrativo para reprocessar falhas definitivas somente se a operação pedir isso.

### 3. Revisar modelo incremental com DBA

- Validar o script `backend/etc/sql/notificacao-email-outbox.sql` antes de aplicar em ambiente compartilhado.
- Confirmar:
  - criação de `NOTIFICACAO_EMAIL`;
  - vínculos opcionais com `ALERTA` e `SUBPROCESSO`;
  - destino pessoal por `usuario_destino_titulo`;
  - ajuste de `ALERTA` para alerta pessoal sem processo e sem unidade de destino;
  - `CHECK` que exige unidade de destino ou usuário de destino;
  - índices necessários para worker e consultas.

### 4. Implementar frontend somente depois

- Para telas de subprocesso, exibir histórico de emails relacionados ao subprocesso se isso for útil no fluxo.
- Para alertas pessoais, decidir se a situação do email aparece no detalhe do alerta ou em área administrativa.
- Evitar misturar alertas normais com estado técnico de envio.
- Usar mensagens simples:
  - pendente;
  - enviando;
  - enviado;
  - falha temporária;
  - falha definitiva.

### 5. Validação final

- Rodar testes focados de alerta/email/outbox.
- Rodar integrações de CDU-27 e CDU-28.
- Rodar testes dos fluxos migrados em `ProcessoService` e `SubprocessoTransicaoService`.
- Fazer dogfooding com monitoramento ligado em um fluxo real.
- Confirmar que requests críticas não dependem mais de SMTP direto nos casos migrados.

## Próxima etapa

Fechar a revisão do processamento do outbox, principalmente concorrência do worker e política operacional. Depois disso, completar apenas as APIs de suporte que forem necessárias e iniciar o frontend.
