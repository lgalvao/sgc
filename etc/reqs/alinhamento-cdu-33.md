# Alinhamento CDU-33 - Reabrir revisão de cadastro

## Cobertura atual do teste
O teste E2E (cdu-33.spec.ts) cobre:
- Setup: Criação de processo de mapeamento, finalização e criação de processo de revisão com mapa homologado
- Navegação para subprocesso de revisão da unidade (SECAO_212)
- Verificação da situação "Mapa homologado"
- Visualização e habilitação do botão "Reabrir revisão" (via `btn-reabrir-revisao`)
- Abertura do modal com heading "Reabrir Revisão"
- Preenchimento de justificativa "Ajuste necessário"
- Confirmação de reabertura
- Validação de mudança de situação para "Revisão em andamento"
- Validação de registro de movimentação com descrição "Reabertura de revisão de cadastro"

## Lacunas em relação ao requisito
- **Falta validação de e-mails**: O requisito especifica envio de notificações por e-mail para (8.1) unidade solicitante e (8.2) unidades superiores com templates específicos. Nenhuma validação é realizada.
- **Falta validação de alertas internos**: O requisito especifica criação de alertas (seção 9.1 e 9.2) com descrições específicas. O teste não valida a existência ou conteúdo dos alertas.
- **Falta validação de campo Observação na movimentação**: O requisito especifica que a movimentação deve incluir campo `Observação: [JUSTIFICATIVA]` (linha 24). O teste valida apenas a descrição.
- **Falta validação de dados completos da movimentação**: Não há validação de Data/hora, Unidade origem (ADMIN), Unidade destino [SIGLA_UNIDADE].
- **Falta mensagem de sucesso**: O requisito especifica "O sistema exibe mensagem de sucesso 'Revisão reaberta com sucesso'" (linha 72). O teste não valida essa mensagem.
- **Falta teste de cancelamento do modal**: O teste não testa o cenário de usuário abrindo o modal e cancelando, diferente do CDU-32.

## Alterações necessárias no teste E2E
- Adicionar validação de mensagem de sucesso "Revisão reaberta com sucesso"
- Adicionar validação de envio de e-mails (tanto para unidade solicitante quanto para unidades superiores)
- Adicionar validação de alertas internos criados (descrições conforme requisito)
- Adicionar validação de campo "Observação" da movimentação com a justificativa fornecida
- Adicionar validação de Data/hora, Unidade origem, Unidade destino da movimentação
- Adicionar teste de cancelamento do modal (abrindo e clicando em Cancelar)
- Considerar adicionar teste com unidades superiores para validar escalação de notificações

## Notas e inconsistências do requisito
- Situação na linha 6 refere-se a "REVISAO_CADASTRO_EM_ANDAMENTO", mas no teste é validada como "Revisão em andamento" (texto formatado). Há correspondência?
- Requisito menciona "unidades solicitante" (linha 28), mas não fica claro se é a unidade do subprocesso ou outra entidade. Contexto anterior (CDU-32) esclarece que é a unidade do subprocesso.
- Diferença entre CDU-32 e CDU-33: CDU-32 gera movimentação com "Descrição: 'Reabertura de cadastro'" enquanto CDU-33 gera "Descrição: 'Reabertura de revisão de cadastro'" e adiciona "Observação: [JUSTIFICATIVA]". Estrutura de movimentação difere.
