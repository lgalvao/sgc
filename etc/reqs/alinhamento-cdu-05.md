# Alinhamento CDU-05 - Iniciar processo de revisão

## Cobertura atual do teste
O teste `cdu-05.spec.ts` é um teste serial (interdependente) com múltiplas fases:

**Fase 1 - Preparação (Processo de Mapeamento):**
- ✅ Fase 1.1: ADMIN cria e inicia processo de MAPEAMENTO
- ✅ Fase 1.2: CHEFE adiciona atividades e conhecimentos
- ✅ Fase 1.3: CHEFE disponibiliza cadastro
- ✅ Fase 1.3b: GESTOR da unidade intermediária registra aceite
- ✅ Fase 1.4: ADMIN homologa cadastro
- ✅ Fase 1.5: ADMIN cria competências e disponibiliza mapa
- ✅ Fase 1.6: CHEFE valida mapa
- ✅ Fase 1.6b: GESTOR aceita validação do mapa
- ✅ Fase 1.7: ADMIN homologa e finaliza processo de MAPEAMENTO

**Fase 2 - Inicialização de Revisão:**
- ✅ Criação de processo de REVISAO
- ✅ Navegação até tela "Cadastro de processo"
- ✅ Exibição de modal de confirmação com texto apropriado
- ✅ Confirmação de inicialização
- ✅ Redirecionamento para Painel
- ✅ Processo muda para situação "Em andamento"
- ✅ Subprocesso criado com situação "Não iniciado" e Data limite copiada (passo 9)
- ✅ Movimentação "Processo iniciado" registrada (passo 11)

**Fase 2.1 - Alertas de Revisão:**
- ✅ Alerta para CHEFE da unidade alvo (operacional): "Início do processo" (sem "subordinada")
- ✅ Alerta para GESTOR da unidade intermediária: "Início do processo em unidade(s) subordinada(s)"

**Fase 3 - Cópia de Mapa (passo 10):**
- ✅ CHEFE verifica que atividades do mapeamento foram copiadas para revisão
- ✅ Conhecimentos foram copiados junto com atividades

## Lacunas em relação ao requisito
1. **Cópia de mapa de competências não validada (passo 10)**: O requisito especifica "O sistema cria internamente uma cópia do mapa de competências vigente, juntamente com as suas respectivas atividades e conhecimentos". O teste valida que atividades/conhecimentos aparecem (Fase 3), o que implicitamente valida a cópia, mas não testa explicitamente o mapa de competências em si.

2. **E-mail para unidades (passo 12)**: Analogamente ao CDU-04, e-mails não são testados. O requisito descreve modelos específicos para:
   - Unidades operacionais/interoperacionais (modelo de revisão do mapa)
   - Unidades intermediárias (modelo com lista de unidades subordinadas)

3. **Unidades intermediárias não recebem subprocessos (passo 9)**: O teste cria processo apenas com unidade operacional (ASSESSORIA_21). Não testa se unidade intermediária pura não recebe subprocesso.

4. **Campos de subprocesso Observações e Sugestões (passo 9)**: O teste não valida explicitamente estes campos no subprocesso de revisão.

5. **Comparação entre e-mail de REVISAO vs MAPEAMENTO**: O requisito (passo 12.1) descreve e-mail diferente para REVISAO ("Já é possível realizar a revisão...") vs MAPEAMENTO ("Já é possível realizar o cadastro..."). O teste não valida diferença no conteúdo de e-mail.

6. **Alertas para unidade interoperacional (passo 13.3)**: O requisito especifica dois alertas para unidade interoperacional (operacional e intermediária). O teste não cobre este cenário de forma explícita (cria processo apenas com operacional ASSESSORIA_21 e intermediária SECRETARIA_2).

7. **Pré-condição de mapa vigente**: O requisito menciona "cópia do mapa de competências vigente". O teste satisfaz isto ao preparar mapeamento na Fase 1, mas não valida explicitamente se o mapa é "vigente" (válido) vs expirado.

8. **Modalidade de diálogo**: O requisito (passo 4) menciona "diálogo de confirmação". O teste valida que modal é exibido com texto apropriado, o que está correto.

## Alterações necessárias no teste E2E
- Adicionar validação explícita do mapa de competências copiado (não apenas atividades/conhecimentos)
- Adicionar validação de campos de subprocesso Observações e Sugestões
- Adicionar teste com unidade interoperacional para validar dois alertas (operacional + intermediária)
- Adicionar teste que valida que unidade intermediária pura não recebe subprocesso
- Considerar adicionar teste de e-mail (pode estar em testes de integração)
- Validar que a data limite foi copiada corretamente (teste já faz isto)
- Adicionar teste de cancelamento de confirmação

## Notas e inconsistências do requisito
- A estrutura de Fase 1 (preparação de MAPEAMENTO) é muito longa e depende de múltiplos CDUs (Atividades, Mapas, Análise). Isto torna o teste frágil se qualquer um destes falhar.
- O requisito diferencia e-mail para REVISAO vs MAPEAMENTO, mas o teste não valida isto (esperado, pois E2E não testa e-mail).
- O requisito (passo 10) menciona "mapa de competências vigente", sugerindo que pode haver mapas expirados. Não há clareza se isto deve ser testado.
- A seção 12.2 menciona novamente "Início de processo de mapeamento de competências em unidades subordinadas" (copy-paste do CDU-04?), quando deveria mencionar "revisão". Possível erro no requisito.
