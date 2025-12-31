# CDU-34 - Enviar lembrete de prazo

**Ator:** Sistema/ADMIN

## Descrição

Se houver processos próximos ao prazo final, o Sistema apresenta indicadores de alertas de prazo e permite enviar um lembrete por e-mail para uma unidade.

## Regras de Negócio

- O lembrete deve ser enviado para o titular da unidade e seus substitutos imediatos (se houver).
- O sistema deve permitir o envio manual pelo Administrador.
- O sistema pode ter uma rotina automática (job) para envio de lembretes X dias antes do prazo.
- O envio do lembrete deve gerar um registro de notificação no sistema.

## Fluxo principal (Envio Manual)

1. O usuário (Administrador) acessa a tela de Acompanhamento de Processos.
2. O sistema exibe os processos e subprocessos, indicando com cores ou ícones aqueles que estão próximos do prazo ou atrasados.
3. O usuário seleciona uma ou mais unidades/subprocessos que possuem pendências.
4. O usuário aciona a opção "Enviar Lembrete".
5. O sistema exibe um modelo da mensagem que será enviada para confirmação.
6. O usuário confirma o envio.
7. O sistema envia o e-mail para os responsáveis pela unidade (Titular e Substitutos).
8. O sistema registra o envio do lembrete na tabela de NOTIFICACAO e no histórico do processo.
9. O sistema exibe mensagem de sucesso "Lembrete enviado".
