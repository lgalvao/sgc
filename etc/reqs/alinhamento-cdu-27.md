# Alinhamento CDU-27 - Alterar data limite de subprocesso

## Cobertura atual do teste
O teste cobre:
- **Setup preparatório**: Criação e iniciação de processo.
- **Cenário 1**: Navegação do ADMIN até detalhes do subprocesso e verificação de visibilidade da página.
- **Cenário 2**: Clique no botão "Alterar data limite", preenchimento com nova data (+7 dias), clique em "Confirmar", validação de mensagem de sucesso e fechamento do modal.

## Lacunas em relação ao requisito
**Lacunas críticas:**

1. **Falta validação de pré-condição de estado**: O requisito especifica "Unidade participante com subprocesso iniciado e ainda não finalizado". O teste não valida explicitamente o estado "não finalizado" (poderia ser "Mapa homologado" ou outro estado terminal).

2. **Falta validação do campo de data preenchido com data limite atual**: O requisito (passo 4) especifica que o modal deve ter o "campo de data preenchido com a data limite atual da etapa em andamento". O teste não valida se o campo vem preenchido com a data antiga/atual.

3. **Falta validação de atualização efetiva da data**: O teste valida a mensagem de sucesso, mas não valida se a data foi realmente alterada no banco de dados ou se ao navegar novamente para o subprocesso, a data exibida é a nova data.

4. **Falta validação de notificação por e-mail**: O requisito (passo 7) especifica envio de e-mail à unidade do subprocesso. O teste não valida.

5. **Falta validação de alerta interno**: O requisito (passo 8) especifica criação de alerta com campos específicos (`Descrição`, `Processo`, `Data/hora`, `Unidade de origem`, `Unidade de destino`). O teste não valida.

6. **Falta validação de número da etapa no alerta**: O requisito especifica que a descrição do alerta deve incluir `[NÚMERO_ETAPA]`, mas o teste não valida isso.

7. **Falta validação de mudança de data para passada**: O teste apenas valida mudança para data futura (+7 dias). Não há validação de comportamento ao tentar alterar para data passada ou data inválida.

8. **Falta validação de modal correto "Alterar data limite"**: O teste não valida o título do modal.

9. **Falta cenário de cancelamento**: Não há teste que valida o botão "Cancelar" do modal.

## Alterações necessárias no teste E2E
1. **Adicionar validação de data preenchida no modal**: 
   - Validar que o campo de data vem com a data limite atual preenchida
   - Comparar com a data limite exibida anteriormente na página do subprocesso

2. **Adicionar validação de persistência da alteração**: 
   - Após alterar data e confirmar, navegar novamente para o subprocesso
   - Validar que a nova data é exibida na página

3. **Adicionar validação de alerta interno**: 
   - Consultar API ou base de dados para validar criação de alerta
   - Validar campos: descrição (com número da etapa), processo, data/hora, unidade origem/destino

4. **Adicionar validação de e-mail**: 
   - Simular ou interceptar envio de e-mail
   - Validar assunto ("SGC: Data limite alterada")
   - Validar corpo com nova data no formato especificado

5. **Adicionar cenário de cancelamento**: 
   - Abrir modal, clicar "Cancelar"
   - Validar que modal fecha e nenhuma alteração é feita

6. **Adicionar validação de título do modal**: 
   - Validar que modal tem título "Alterar data limite"

7. **Adicionar teste de validação de datas inválidas**: 
   - Tentar alterar para data passada
   - Validar mensagem de erro ou validação de formulário

## Notas e inconsistências do requisito
- **Falta clareza em "data limite atual da etapa em andamento"**: O requisito não especifica claramente qual é a "data limite" - é a data limite do processo, da etapa ou do subprocesso? Presume-se ser a data limite da etapa em andamento do subprocesso.

- **Ambiguidade em "ainda não finalizado"**: O requisito usa "ainda não finalizado" para pré-condição, mas não especifica quais estados do subprocesso qualificam como "finalizado" versus "não finalizado". Presume-se que significa o subprocesso não está em situação terminal (como 'Finalizado' ou 'Mapa homologado').

- **Falta de clareza sobre múltiplos subprocessos**: O requisito é singular ("subprocesso iniciado"), mas não especifica se o ADMIN pode alterar a data de múltiplos subprocessos em um mesmo processo.
