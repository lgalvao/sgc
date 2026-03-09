# Alinhamento CDU-17 - Disponibilizar mapa de competências

## Cobertura atual do teste
O teste E2E cobre:
- **Preparação 1-4**: Cria processo de mapeamento, CHEFE adiciona 3 atividades com conhecimentos, gestores aceitam, ADMIN homologa e cria 2 competências (linhas 31-105).
- **Navegação para Edição de Mapa** (passos 1-6): ADMIN acessa processo, navega para subprocesso, navega para mapa (linhas 114-121).
- **Validação de disponibilidade do botão "Disponibilizar"** (passo 7): Botão está visível (linha 121).
- **Abertura de modal** (passo 10): Clica no botão, valida presença do modal com teste ID `mdl-disponibilizar-mapa` (linhas 123-126).
- **Cancelamento** (passo 11): Clica Cancelar, valida que modal fecha e permanece na tela de edição (linhas 128-131).
- **Disponibilização com sucesso** (passos 12-20): Chama helper `disponibilizarMapa(page, '2030-12-31')`, valida redirecionamento para Painel e mudança de situação para "Mapa disponibilizado" (linhas 134-138).

## Lacunas em relação ao requisito
**Validação de pré-condições (pré-requisitos) não coberta:**
- **Pré-condição 1**: Não há validação explícita que usuário está logado como ADMIN.
- **Pré-condição 2**: Não há validação que subprocesso está em situação 'Mapa criado' ou 'Mapa ajustado' **antes** de acessar tela de disponibilização.
- **Pré-condição 3**: Não há validação explícita que "Tela `Painel` está sendo exibida" no início.

**Validação de pré-validações (passos 8-9) não coberta:**
- **Passo 8**: "O sistema verifica se todas as competências criadas estão associadas a pelo menos uma atividade do cadastro da unidade."
  - Teste não cria cenário onde uma competência NÃO tem atividades associadas.
  - Teste não valida mensagem de erro se validação falha.

- **Passo 9**: "O sistema verifica se todas as atividades foram associadas a pelo menos uma competência."
  - Teste cria 3 atividades mas apenas 2 competências (competencia1 com atividade1+2, competencia2 com atividade3).
  - **Na verdade, TODAS as atividades ESTÃO associadas**, então teste não cobre o cenário de erro.
  - Teste deveria incluir cenário onde uma atividade fica não associada e sistema retorna erro.

**Validação de modal (passo 10) incompleta:**
- **Título**: Não valida que título é 'Disponibilização do mapa de competências'.
- **Campo Data**: Não valida presença de campo de data, label, validação (obrigatório), formato esperado.
- **Campo Observações**: Não valida presença de campo de observações, label, validação (opcional).
- **Botões**: Valida que modal está visível e tem botão Cancelar, mas não valida explicitamente botão "Disponibilizar" no modal.

**Campos do modal não validados:**
- Teste chama helper `disponibilizarMapa(page, '2030-12-31')` que provavelmente preenche data, mas teste não valida:
  - Que campo data é preenchido corretamente.
  - Que campo observações pode ser preenchido (teste não passa observação).
  - Que valores são salvos conforme esperado.

**Registros de dados (passos 13-18) não validados:**
- **Passo 13**: "O sistema registra a informação do campo `Observações` no mapa do subprocesso e a informação do campo `Data limite`..."
  - Teste não valida que dados foram persistidos no backend.
  - Teste não consulta endpoint ou UI para verificar que "Data limite da etapa 2" foi salva.

- **Passo 14**: "O sistema altera a situação do subprocesso da unidade para 'Mapa disponibilizado'."
  - Teste valida essa mudança ✓ (linha 138).

- **Passo 15**: "O sistema registra uma movimentação para o subprocesso..."
  - Teste não valida que movimentação foi registrada (Data/hora, Unidade origem ADMIN, Unidade destino SIGLA_UNIDADE_SUBPROCESSO, Descrição).

- **Passo 16**: "O sistema notifica a unidade do subprocesso quanto à disponibilização, com e-mail..."
  - Teste não valida que e-mail foi enviado com assunto/corpo corretos.

- **Passo 17**: "O sistema notifica as unidades superiores da unidade do subprocesso..."
  - Teste não valida que e-mail foi enviado para unidades superiores.

- **Passo 18**: "O sistema cria internamente um alerta..."
  - Teste não valida que alerta foi criado com descrição, processo, data/hora, unidades.

- **Passo 19**: "O sistema exclui as sugestões apresentadas do mapa de competência do subprocesso da unidade."
  - Teste não valida esse comportamento.

**Validação de redirecionamento e mensagem (passo 20):**
- **Redirecionamento**: Teste valida que URL é `/painel` ✓.
- **Mensagem de confirmação**: Teste não valida que mensagem "Disponibilização do mapa de competências efetuada" é exibida.

**Cenários de erro não cobertos:**
- Teste não cobre cenário onde competência não tem atividades associadas.
- Teste não cobre cenário onde atividade não está associada a competência.
- Teste não cobre cenário onde campo data não é preenchido (obrigatório).

## Alterações necessárias no teste E2E
1. **Validar pré-condições**:
   - Confirmar que usuário logado é ADMIN.
   - Confirmar que subprocesso está em situação 'Mapa criado' ou 'Mapa ajustado' antes de abrir tela.
   - Confirmar que está no Painel antes de começar fluxo.

2. **Adicionar cenário de erro (competência sem atividades)**:
   - Criar competência sem selecionar atividades.
   - Tentar disponibilizar.
   - Validar mensagem de erro informando qual competência não tem atividades.
   - Confirmar que disponibilização é interrompida.

3. **Adicionar cenário de erro (atividade não associada)**:
   - Criar 2 competências para 3 atividades (deixar 1 atividade não associada).
   - Tentar disponibilizar.
   - Validar mensagem de erro informando qual atividade não está associada.
   - Confirmar que disponibilização é interrompida.

4. **Validar conteúdo e elementos do modal**:
   - Validar título: "Disponibilização do mapa de competências".
   - Validar rótulo e obrigatoriedade do campo data.
   - Validar rótulo e opcionalidade do campo observações.
   - Validar presença e rótulos dos botões "Disponibilizar" e "Cancelar".

5. **Validar preenchimento de campos**:
   - Preencher campo data com data válida (teste já faz via helper).
   - Preencher campo observações com texto (teste não cobre, adicionar).
   - Validar que dados são preenchidos corretamente no modal antes de clicar.

6. **Validar persistência de dados**:
   - Após disponibilização, navegar de volta para subprocesso.
   - Validar que "Data limite da etapa 2" foi salva (pode estar visível em UI ou acessar endpoint).
   - Validar que observações foram salvas no mapa.

7. **Validar registros de movimentação**:
   - Consultar histórico de movimentações do subprocesso.
   - Validar que movimentação foi registrada com campos corretos.

8. **Validar notificações**:
   - Validar que e-mail foi enviado para unidade do subprocesso (asunto e corpo).
   - Validar que e-mail foi enviado para unidades superiores (assunto e corpo).

9. **Validar criação de alerta**:
   - Consultar lista de alertas.
   - Validar que alerta foi criado com descrição, processo, data/hora, unidades.

10. **Validar exclusão de sugestões**:
    - Se houver sugestões no mapa, validar que foram excluídas após disponibilização.

11. **Validar mensagem de sucesso**:
    - Após redirecionamento para Painel, validar que mensagem "Disponibilização do mapa de competências efetuada" é exibida.

## Notas e inconsistências do requisito
- **Passo 8.1 e 9.1**: Requisito diz "informa em mensagem de erro quais competências..." e "quais atividades..." - Não especifica se é toast, modal, alert ou inline message. Teste deveria validar um tipo específico.
- **Passo 15**: Campo "Unidade origem" é 'ADMIN' (string literal), não uma sigla de unidade. Isso pode causar confusão com o padrão de "SIGLA_UNIDADE" usado em outros passos.
- **Passo 19**: Diz "exclui as sugestões apresentadas do mapa" - Não há contexto sobre o que são "sugestões" ou quando aparecem. Requisito de CDU-? que apresenta sugestões não é referenciado.
- **Campo obrigatório vs opcional**: Passo 10 diz "Data": de preenchimento obrigatório" e "Observações": de preenchimento opcional" mas teste não valida essa obrigatoriedade em cenário onde data não é preenchida.
