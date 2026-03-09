# Alinhamento CDU-34 - Enviar lembrete de prazo

## Cobertura atual do teste
O teste E2E (cdu-34.spec.ts) abrange:
- Setup: Criação de processo de mapeamento com 5 dias de limite para unidade ASSESSORIA_22
- Verificação de visualização do processo na tabela de processos
- Navegação para subprocesso da unidade
- Localização e clique no botão "Enviar lembrete"
- Visualização do modelo da mensagem (via `txt-modelo-lembrete`)
- Verificação de texto "Este lembrete será enviado" no modelo
- Confirmação do envio via botão `btn-confirmar-enviar-lembrete`
- Validação de registro da movimentação "Lembrete de prazo enviado"
- Teste complementar: Usuário da unidade destino (chefeAssessoria22) visualiza alerta no painel
- Validação de tabela de alertas contendo descrição do processo e menção de "Lembrete"

## Lacunas em relação ao requisito
- **Falta validação do conteúdo completo do e-mail**: O requisito especifica template de e-mail (linhas 23-30) com assunto, corpo e data limite. O teste valida apenas texto genérico "Este lembrete será enviado", não o conteúdo específico.
- **Falta validação de DATA_LIMITE no alerta**: O requisito (linha 41) especifica que o alerta deve conter "Lembrete: Prazo do processo [DESCRICAO_PROCESSO] encerra em [DATA_LIMITE]". O teste apenas valida presença de "Lembrete", não a data limite.
- **Falta validação de campos de movimentação**: O requisito especifica movimentação (linhas 34-37) com Data/hora, Unidade origem (ADMIN), Unidade destino [SIGLA_UNIDADE], Descrição. O teste valida apenas a descrição.
- **Falta validação de destinatários**: O requisito (linha 20) especifica envio para "os responsáveis pela unidade (Titular e Substitutos)". Não há validação de quem recebeu o e-mail.
- **Falta teste de múltiplas seleções**: O requisito (linha 12) menciona "O usuário seleciona uma ou mais unidades/subprocessos". Teste valida apenas uma unidade.
- **Falta validação de confirmação de modelo**: O requisito (linha 5-6) especifica que "O sistema exibe um modelo da mensagem que será enviada para confirmação". Teste valida que modelo existe, mas não valida interação de confirmação ou rejeição antes de envio.

## Alterações necessárias no teste E2E
- Adicionar validação do conteúdo completo do e-mail (assunto, corpo com DATA_LIMITE formatada)
- Adicionar validação de campos completos da movimentação (Data/hora, Unidade origem, Unidade destino)
- Adicionar validação de alerta contendo a descrição formatada com DATA_LIMITE
- Adicionar cenário de múltiplas seleções de unidades/subprocessos para envio em lote
- Considerar adicionar cenário de cancelamento antes de envio (abrir modelo, cancelar, validar que não foi enviado)
- Considerar adicionar validação de que responsáveis alternativos (Substitutos) também recebem notificação

## Notas e inconsistências do requisito
- Requisito menciona "Ator: Sistema/ADMIN" (linha 3), indicando que ação pode ser automática ou manual. Teste valida apenas fluxo manual via ADMIN. Não fica claro se há cenários automatizados por Sistema (ex: envio automático x dias antes do prazo).
- Linha 16 refere-se a "Modelo da mensagem para confirmação", mas linha 56 do teste apenas valida "Este lembrete será enviado", não mostrando template completo.
- Diferença: Requisito menciona "seleção de uma ou mais unidades" (linha 12), sugerindo operação em lote, mas teste opera apenas uma unidade.
