# Alinhamento CDU-13 - Analisar cadastro de atividades e conhecimentos

## Cobertura atual do teste
O teste E2E cobre:
- **Setup com devoluções prévias**: Simula GESTOR fazendo aceite, depois GESTOR superior devolvendo 2 vezes até retornar para CHEFE ajustar (linhas 21-57).
- **Fluxo de aceite com homologação**: GESTOR_COORD_21 aceita → GESTOR_SECRETARIA_2 aceita → ADMIN clica `btn-acao-analisar-principal` e confirma (linhas 59-79).
- **Validações básicas**: Verifica mudança de situação para "Cadastro homologado" e redirecionamento para detalhes do subprocesso.
- **Histórico de análise**: Testa visualização de histórico com campo de resultado e observações (linhas 49-52).

## Lacunas em relação ao requisito
**Fluxo principal não coberto:**
- **Passo 1-2**: Não há assertion explícita de que o usuário "clica no processo no Painel" ou que a tela "Detalhes do processo" é exibida.
- **Passo 3-4**: Não verifica explicitamente a navegação para "Detalhes do subprocesso" ou que apresenta os dados da unidade selecionada.
- **Passo 5**: Não testa o clique no card "Atividades e conhecimentos".
- **Passo 6**: Não valida a presença dos botões específicos (`Histórico de análise`, `Devolver para ajustes`, `Registrar aceite`/`Homologar`).

**Fluxo de devolução (passo 9) não coberto:**
- **9.1-9.4**: Teste devolver, mas não cobre cancelamento da devolução (passo 9.3 - "Cancelar interrompe").
- **9.5**: Não valida que a análise foi registrada com Data/hora, Unidade (SIGLA_UNIDADE_ANALISE), Resultado='Devolução', Observação.
- **9.6-9.8**: Não verifica a unidade de devolução ou mudança de situação para "Cadastro em andamento".
- **9.9-9.11**: Não valida e-mail, alerta interno ou redirecionamento com mensagem "Devolução realizada".

**Fluxo de aceite/homologação:**
- **10.2**: Não valida o diálogo modal com pergunta específica "Confirma o aceite do cadastro de atividades?".
- **10.3**: Não testa cancelamento (permanece na tela).
- **10.5-10.8**: Não valida registros de análise, movimentação, e-mail ou alerta criado com dados específicos.
- **10.9**: Não valida mensagem "Aceite registrado".
- **11.2-11.7**: Não testa homologação com modal "Homologação do cadastro", registros específicos ou mensagem "Homologação efetivada".

**Modal de histórico:**
- Teste abre histórico mas não valida que exibe tabela com **data/hora**, **sigla da unidade**, **resultado** ('Devolução' ou 'Aceite'), **observações**.

## Alterações necessárias no teste E2E
1. **Adicionar cobertura de botões obrigatórios**: Validar presença de `Histórico de análise`, `Devolver para ajustes`, `Registrar aceite` ou `Homologar` em passo 6.

2. **Testar cancelamento de devolução**: Adicionar cenário que clica em `Devolver para ajustes`, abre modal, clica `Cancelar`, e verifica que permanece em `Atividades e conhecimentos`.

3. **Validar diálogos modais**: 
   - Para devolução: Verificar título "Devolução", pergunta exata, campo de observação, botões `Confirmar` e `Cancelar`.
   - Para aceite: Verificar título "Aceite", pergunta exata "Confirma o aceite do cadastro de atividades?".
   - Para homologação: Verificar título "Homologação do cadastro".

4. **Validar registros de análise e movimentação**: Após devolução ou aceite, consultar estado da análise registrada (Data/hora, Unidade, Resultado, Observação).

5. **Testar notificações e alertas**: Validar que e-mail foi enviado com assunto e corpo corretos; que alerta foi criado com descrição, processo, data/hora, unidades de origem/destino.

6. **Validar mudanças de situação**: 
   - Devolução para a própria unidade → "Cadastro em andamento".
   - Aceite → movimentação para unidade superior.
   - Homologação → "Cadastro homologado".

7. **Validar mensagens de conclusão**: Verificar "Devolução realizada", "Aceite registrado", "Homologação efetivada".

## Notas e inconsistências do requisito
- **Linha 74, passo 11**: Numeração incorreta (continua de 10, deveria ser 11 e está marcado como "1"). O fluxo de homologação está fora de sequência.
- **Especificação de perfil em passo 6**: Requisito não deixa claro se o botão "Homologar" aparece **sempre** quando ADMIN acessa ou apenas em certas situações.
- **Ambiguidade em passo 9.6**: Diz "identifica a unidade de devolução como sendo a unidade de origem da última movimentação" - não é 100% claro o algoritmo se há múltiplas movimentações.
- **Falta de cenário alternativo**: Requisito não especifica o que acontece se o ADMIN visualiza este cadastro em estado "Devolução" (ele pode apenas homologar ou também devolver?).
