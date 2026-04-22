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
- O worker captura notificações por update condicionado antes do envio, evitando reprocessamento simultâneo da mesma linha.
- Há uma view administrativa independente, acessível apenas para `ADMIN`, para acompanhar notificações por subprocesso ativo.
- A view administrativa permite `Reenviar` notificações em `FALHA_DEFINITIVA`, recolocando-as na fila para o worker processar.
- Política operacional inicial:
  - intervalo do worker: 30 segundos, configurável por `SGC_NOTIFICACAO_EMAIL_INTERVALO_WORKER_MS`;
  - tamanho do lote: 20 notificações por execução, configurável por `SGC_NOTIFICACAO_EMAIL_LOTE_WORKER`;
  - sem limpeza automática nesta etapa;
  - `FALHA_DEFINITIVA` deve ser investigada manualmente pelo suporte antes de eventual reprocessamento.
- Fluxos já migrados para o outbox:
  - alteração de data limite de subprocesso;
  - lembrete de prazo;
  - atribuição temporária do CDU-28.
- CDU-27 e CDU-28 possuem cobertura integrada confirmando uso do outbox.
- Testes focados de alerta/email/outbox passaram em 2026-04-22.

## Pendências

### 1. Acompanhar política operacional do outbox

- Validar com operação se intervalo de 30 segundos e lote 20 atendem ao volume real.
- Definir retenção ou limpeza histórica após observar uso em ambiente compartilhado.
- Documentar procedimento de suporte para investigar `ultimo_erro`, `tentativas` e `proxima_tentativa_em`.

### 2. Evoluir suporte operacional quando houver necessidade real

- Avaliar detalhe/modal com histórico completo por subprocesso.
- Criar consulta por alerta ou por usuário apenas se houver necessidade real futura.
- Avaliar auditoria explícita de reenvios manuais se a operação passar a usar a ação com frequência.

### 3. Revisar modelo incremental com DBA

- Validar o script `backend/etc/sql/notificacao-email-outbox.sql` antes de aplicar em ambiente compartilhado.
- Confirmar:
  - criação de `NOTIFICACAO_EMAIL`;
  - vínculos opcionais com `ALERTA` e `SUBPROCESSO`;
  - destino pessoal por `usuario_destino_titulo`;
  - ajuste de `ALERTA` para alerta pessoal sem processo e sem unidade de destino;
  - `CHECK` que exige unidade de destino ou usuário de destino;
  - índices necessários para worker e consultas.

### 4. Evoluir frontend administrativo

- Avaliar filtros por status depois do uso real da tela.
- Avaliar detalhe/modal com histórico completo das notificações do subprocesso.
- Evitar misturar alertas normais com estado técnico de envio.

### 5. Validação final

- Rodar testes focados de alerta/email/outbox.
- Rodar integrações de CDU-27 e CDU-28.
- Rodar testes dos fluxos migrados em `ProcessoService` e `SubprocessoTransicaoService`.
- Fazer dogfooding com monitoramento ligado em um fluxo real.
- Confirmar que requests críticas não dependem mais de SMTP direto nos casos migrados.

## Próxima etapa

Validar a política operacional inicial em ambiente compartilhado. Depois disso, completar apenas as APIs de suporte que forem necessárias e iniciar o frontend.
