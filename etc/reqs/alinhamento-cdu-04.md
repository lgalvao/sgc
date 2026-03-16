# Alinhamento CDU-04 - Iniciar processo de mapeamento

## Cobertura atual do teste
O teste `cdu-04.spec.ts` cobre os seguintes cenários:

**Fluxo principal de inicialização:**
- ✅ Criação de processo com unidades participantes (Interoperacional + Operacional)
- ✅ Navegação até tela de "Cadastro de processo"
- ✅ Exibição de modal de confirmação com texto apropriado
- ✅ Cancelamento de inicialização mantém na mesma tela
- ✅ Confirmação de inicialização
- ✅ Redirecionamento para Painel após sucesso
- ✅ Feedback de toast "iniciado"
- ✅ Processo muda para situação "Em andamento" na tabela

**Snapshot de unidades (passo 7):**
- ✅ Após iniciar, processo mostra detalhes com unidades participantes
- ✅ Dados iniciais de subprocessos validados (passo 9):
  - Situação: "Não iniciado"
  - Data limite copiada da data limite do processo

**Movimentações (passo 11):**
- ✅ Timeline de movimentações exibida
- ✅ Movimentação "Processo iniciado" presente

**Alertas (passo 13):**
- ✅ Alerta para unidade operacional (ASSESSORIA_11): "Início do processo" (sem "subordinada")
- ✅ Alerta para unidade interoperacional como CHEFE: "Início do processo" (operacional)
- ✅ Alerta para unidade interoperacional como GESTOR: "Início do processo em unidade(s) subordinada(s)" (intermediária)

## Lacunas em relação ao requisito
1. **Mapa de competências vazio não validado (passo 10)**: O requisito especifica "O sistema cria internamente um mapa de competências vazio (sem competências) e o vincula ao subprocesso da unidade." O teste não valida a existência deste mapa vazio. Isto pode ser implícito (testado em CDU-05 quando copia o mapa), mas não explicitamente.

2. **Notificações por e-mail não testadas (passo 12)**: O requisito descreve modelos específicos de e-mail para:
   - Unidades operacionais/interoperacionais (modelo com URL_SISTEMA)
   - Unidades intermediárias (modelo com lista de unidades subordinadas)
   O teste não valida envio de e-mail ou conteúdo. Isto é esperado para E2E, mas pode haver testes de contrato ou mocks não presentes.

3. **E-mail para unidades intermediárias e interoperacionais (passo 12.2)**: O teste não valida alertas/e-mails para unidade intermediária que é parte de uma unidade interoperacional. A test valida para SECRETARIA_1 como GESTOR, mas não testa uma unidade puramente intermediária.

4. **Campos de subprocesso não completamente validados (passo 9)**:
   - "Observações": Campo de texto formatado para ADMIN. Teste não valida.
   - "Sugestões": Campo de texto formatado para unidades. Teste não valida.
   O teste apenas valida Situação e Data limite.

5. **Unidades intermediárias não recebem subprocessos (passo 9)**: O requisito especifica "(unidade do tipo Intermediária nunca participam diretamente)". O teste cria processo apenas com Operacional e Interoperacional (ASSESSORIA_11 + SECRETARIA_1). Não testa caso com Intermediária pura que não deve receber subprocesso direto.

6. **Origens de movimentação (passo 11)**: O teste valida movimento "Processo iniciado", mas não valida:
   - "Unidade origem": ADMIN
   - "Unidade destino": [SIGLA_UNIDADE_SUBPROCESSO]
   Apenas verifica a descrição.

7. **Data/hora da movimentação**: O teste não valida que a data/hora foi registrada corretamente.

8. **Alertas para unidade interoperacional (passo 13.3)**: O requisito especifica "Para cada unidade **interoperacional** serão criados dois alertas: um de unidade operacional e outro de unidade intermediária". O teste verifica:
   - Alerta como CHEFE (operacional) ✅
   - Alerta como GESTOR (intermediária) ✅
   Mas não testa em um contexto claro de que são dois alertas distintos para a mesma unidade interoperacional, apenas diferentes logins.

9. **Pré-condição não validada**: O requisito menciona "Existência de ao menos um processo de mapeamento na situação 'Criado'". O teste cria o processo, o que satisfaz a pré-condição.

10. **Teste não cobre processo de REVISAO ou DIAGNOSTICO**: O requisito é específico para "processo de mapeamento". O teste apenas testa MAPEAMENTO, o que está correto, mas não há teste analogamente para REVISAO ou DIAGNOSTICO (CDU-05 cobre REVISAO).

## Alterações necessárias no teste E2E
- Adicionar validação de que mapa de competências vazio foi criado (pode ser implícito ao verificar subprocesso)
- Adicionar validação explícita dos campos de subprocesso: Observações e Sugestões (pelo menos verificar presença)
- Adicionar validação de movimentação completa: "Unidade origem" (ADMIN), "Unidade destino", Data/hora
- Adicionar teste com unidade intermediária pura (não interoperacional) para validar que não recebe subprocesso direto
- Adicionar teste que valida dados iniciais de Data limite copiada corretamente (teste já faz isto, mas pode ser mais robusto)
- Considerar adicionar teste de e-mail (pode estar em testes de integração/contrato, não E2E)
- Validar que cancelamento da confirmação não cria subprocessos

## Notas e inconsistências do requisito
- O requisito é muito detalhado quanto à estrutura de alertas (13.1, 13.2, 13.3) e unidades intermediárias vs operacionais. A implementação de "duas categorias de alertas" para unidade interoperacional é clara no requisito mas complexa de testar em E2E.
- Modelos de e-mail (passo 12.1 e 12.2) contêm placeholders como [SIGLA_UNIDADE], [DESCRICAO_PROCESSO], [DATA_LIMITE], [URL_SISTEMA]. Não há clareza se estes devem ser testados em E2E ou apenas em testes de integração.
- Referência a "O sistema de Gestão de Competências" no corpo do e-mail (modelo 12.1) parece incompleta ou com redação redundante.
