# Alinhamento CDU-06 - Detalhar processo

## Cobertura atual do teste
O teste `cdu-06.spec.ts` cobre os seguintes cenários:

**Fase 1 - Detalhes para ADMIN:**
- ✅ Exibição de detalhes do processo (Descrição, Tipo, Situação)
- ✅ Unidade participante listada com situação e data limite
- ✅ ADMIN pode navegar para subprocesso (clique na unidade)
- ✅ Botão "Alterar data limite" visível para ADMIN em subprocesso
- ✅ Botão "Reabrir cadastro" oculto quando situação é "Não iniciado" (requer situação >= MAPEAMENTO_MAPA_HOMOLOGADO)
- ✅ Botão "Enviar lembrete" visível para ADMIN em subprocesso

**Fase 1b - Detalhes para GESTOR:**
- ✅ GESTOR vê detalhes do processo
- ✅ Botão "Finalizar processo" oculto para GESTOR
- ✅ Botões "Alterar data limite" e "Reabrir cadastro" ocultos para GESTOR em subprocesso

**Fase 2 - Ações em bloco (passo 2.2.2):**
- ✅ Botão "Homologar em bloco" exibido para ADMIN quando há subprocessos em "Cadastro disponibilizado"
- ✅ Botão "Aceitar cadastro em bloco" exibido para GESTOR quando há subprocessos em "Cadastro disponibilizado"

## Lacunas em relação ao requisito
1. **Seção "Dados do processo" não completamente validada (passo 2.1.1)**: O requisito menciona exibição de "descrição, tipo e situação dos processos". O teste valida isto, mas não valida:
   - Referência a "_situacoes.md" (não testável diretamente, mas sugere haver definição de situações possíveis)
   - Formato de exibição (ordem, layout)

2. **Botão "Finalizar processo" (passo 2.1.2)**: O teste valida que botão é oculto para GESTOR, mas não valida:
   - Que botão está visível para ADMIN
   - Comportamento de clique (modal de confirmação, resultado de finalização)
   - Mensagens de feedback

3. **Seção "Unidades participantes" - Subárvore (passo 2.2.1)**: O requisito especifica "Subárvore das unidades hierarquicamente inferiores". O teste não valida:
   - Estrutura hierárquica (relação pai-filho exibida)
   - Que apenas unidades hierarquicamente inferiores são exibidas

4. **Exibição de unidades na tabela (passo 2.2.1)**: O requisito menciona exibição de "informações da situação do subprocesso e da data limite". O teste valida isto mas não valida:
   - Todos os campos exibidos (Descrição da unidade? Sigla?)
   - Formato de data limite (dd/mm/yyyy vs outro)

5. **Clique em unidades operacionais/interoperacionais (passo 2.2.1)**: O teste navega para subprocesso ao clicar em unidade, mas não valida:
   - Comportamento com unidades intermediárias (se existem, não devem ser clicáveis)
   - Que apenas operacionais/interoperacionais são clicáveis

6. **Elementos de alteração no subprocesso (passo 2.2.1)**: O requisito menciona "elementos para possibilitar a alteração da data limite da etapa atual da unidade assim como da situação atual do subprocesso (ex. Reabertura do cadastro de atividades)". O teste valida presença/ausência de botões, mas não valida:
   - Comportamento de clique no botão "Alterar data limite" (modal, edição, feedback)
   - Comportamento de clique no botão "Reabrir cadastro" (modal, edição, feedback)
   - Que estes botões aparecem apenas para ADMIN

7. **Botões de ação em bloco (passo 2.2.2)**: O requisito descreve condições para exibição de botões:
   - "Aceitar/Homologar em bloco" se existirem unidades subordinadas com subprocesso em "Cadastro disponibilizado" (MAPEAMENTO) ou "Revisão do cadastro disponibilizada" (REVISAO)
   - "Aceitar/Homologar mapa em bloco" se existirem subprocessos em "Mapa validado" ou "Mapa com sugestões"

   O teste valida apenas o primeiro tipo (cadastro em bloco). Não testa:
   - Botão para "Mapa em bloco"
   - Diferentes situações de subprocesso (REVISAO vs MAPEAMENTO)

8. **GESTOR de unidade intermediária**: O teste testa GESTOR da SECRETARIA_1 (George Harrison para SECRETARIA_2), mas pode haver confusão sobre qual GESTOR testa qual unidade. Isto é implementado corretamente no teste, mas pode ser mais claro.

9. **Botão "Enviar lembrete" (passo não especificado)**: O teste valida presença deste botão, mas não está descrito no requisito (passo 2.2.1). Isto pode ser um recurso adicional não documentado no requisito.

## Alterações necessárias no teste E2E
- Adicionar teste que valida comportamento de clique em botão "Finalizar processo" para ADMIN
- Adicionar teste que valida modal de confirmação e feedback de finalização
- Adicionar teste que valida estrutura hierárquica de unidades (árvore)
- Adicionar teste que valida que apenas unidades operacionais/interoperacionais são clicáveis
- Adicionar teste que valida comportamento de clique em "Alterar data limite" (modal, edição, feedback)
- Adicionar teste que valida comportamento de clique em "Reabrir cadastro" (modal, edição, feedback)
- Adicionar teste que valida botão "Aceitar/Homologar mapa em bloco" quando há subprocessos em situações apropriadas
- Adicionar teste com unidade intermediária para validar que não é clicável
- Adicionar teste que valida botão "Enviar lembrete" (se é recurso oficial) ou remover do teste (se não documentado)
- Expandir testes de ações em bloco para cobrir REVISAO vs MAPEAMENTO
- Adicionar validação de formato de data limite exibida

## Notas e inconsistências do requisito
- O requisito menciona referência a "_situacoes.md" (passo 2.1.1), sugerindo haver documento separado definindo situações de processos e subprocessos. Isto não está incluído nos arquivos analisados.
- Passo 2.2.1 menciona "elementos para possibilitar a alteração" e dá exemplo "Reabertura do cadastro de atividades". Isto sugere que há múltiplas ações possíveis dependendo da situação do subprocesso, não apenas data limite. O requisito não é claro sobre quais ações estão disponíveis em quais situações.
- Botão "Enviar lembrete" é testado mas não aparece no requisito. Pode ser recurso adicional ou omissão no documento de requisitos.
- O requisito menciona "Se o perfil do usuário seja ADMIN, serão exibidos..." na seção 2.2.1, mas não há menção de diferenciar comportamento para GESTOR (além de ocultar "Finalizar processo"). O teste testa isto, mas o requisito pode ser mais claro.
- Referência a "caso de uso Aceitar/Homologar mapa em bloco" no requisito, mas este caso de uso não está entre os 6 analisados.
