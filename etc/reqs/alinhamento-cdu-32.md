# Alinhamento CDU-32 - Reabrir cadastro

## Cobertura atual do teste
O teste E2E (cdu-32.spec.ts) abrange:
- Criação de fixture com processo de mapeamento e subprocesso em estado "Mapa homologado"
- Navegação para o subprocesso da unidade (SECAO_221)
- Verificação da situação "Mapa homologado" via badge `subprocesso-header__txt-situacao`
- Visualização e habilitação do botão "Reabrir cadastro"
- Teste de cancelamento do modal de reabertura
- Teste de validação: botão confirmar desabilitado sem justificativa, habilitado com justificativa
- Confirmação de reabertura com justificativa "Justificativa de teste"
- Validação de alert de sucesso "Cadastro reaberto com sucesso"
- Verificação de mudança de situação para "Cadastro em andamento"
- Verificação de registro de movimentação com descrição "Reabertura de cadastro"

## Lacunas em relação ao requisito
- **Falta validação de e-mails**: O requisito especifica envio de notificações por e-mail para (8.1) unidade do subprocesso e (8.2) unidades superiores com templates específicos. Nenhuma validação de e-mail é realizada no teste.
- **Falta validação de alertas internos**: O requisito especifica criação de alertas (seção 9.1 e 9.2) com descrições específicas ("Cadastro de atividades reaberto" e "Cadastro da unidade [SIGLA] reaberto"). O teste não valida a existência desses alertas.
- **Falta validação de campos da movimentação**: O requisito especifica campos da movimentação (9.1): data/hora, unidade origem (ADMIN), unidade destino (SIGLA_UNIDADE), descrição. O teste valida apenas a descrição; não valida os outros campos.
- **Falta teste com unidades superiores**: O fluxo mencionado no requisito inclui notificação e alerta para "unidades superiores na hierarquia", mas o teste não valida isso (pode estar limitado a uma unidade sem hierarquia superior).
- **Falta validação de cenários com processo de Revisão**: O requisito menciona "Processo do tipo Mapeamento ou Revisão", mas o teste apenas valida Mapeamento (evidência: fixture `criarProcessoMapaHomologadoFixture`).

## Alterações necessárias no teste E2E
- Adicionar validação de envio de e-mails (via mock/verificação de chamadas de API de e-mail ou log de envio)
- Adicionar verificação de existência e conteúdo dos alertas internos criados
- Adicionar validação de todos os campos da movimentação registrada (data/hora, origem, destino, além de descrição)
- Adicionar cenário adicional testando processo de Revisão (conforme pré-condição do requisito)
- Adicionar teste com unidade que possua hierarquia superior para validar notificações escalonadas
- Considerar adicionar cenários de erro: tentar reabrir já aberto, tentar sem permissão, justificativa muito longa/vazia

## Notas e inconsistências do requisito
- Requisito menciona "Processo do tipo Mapeamento ou Revisão" mas pré-condição especifica "Processo do tipo Mapeamento ou Revisão" e "Ao menos um subprocesso que tenha passado da situação 'Mapa homologado'". Para Revisão, a situação equivalente seria explícita?
- Campo "Unidade origem" na movimentação é listado como "ADMIN" (linha 26), mas não está claro se é o login ADMIN ou se é uma representação literal da string "ADMIN".
- Testes de e-mail dependem de infraestrutura de envio; não fica claro se E2E deve validar efetivamente ou apenas UI.
