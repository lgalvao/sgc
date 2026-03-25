# Alinhamento CDU-03 - Manter processo

## Cobertura atual do teste
O teste `cdu-03.spec.ts` cobre os seguintes cenários:

**Criação:**
- ✅ Validação de campos obrigatórios (descrição)
- ✅ Habilitação/desabilitação de botões "Salvar" e "Iniciar" conforme preenchimento de campos obrigatórios
- ✅ Atributo `required` no campo de descrição
- ✅ Criação bem-sucedida de processo com feedback de sucesso
- ✅ Redirecionamento para Painel após criação

**Seleção em cascata (Unidades participantes):**
- ✅ Seleção de unidade intermediária seleciona filhas automaticamente
- ✅ Desselecção de um filho deixa pai em estado indeterminado
- ✅ Desselecção de todos os filhos desseleciona pai
- ✅ Seleção de todos os filhos marca pai automaticamente
- ✅ Regra especial de unidade interoperacional: pode ser selecionada independentemente das filhas

**Edição:**
- ✅ Clique em processo na tabela abre formulário preenchido
- ✅ Campo tipo é desabilitado em edição
- ✅ Edição de descrição e feedback de sucesso
- ✅ Redirecionamento para Painel após edição

**Remoção:**
- ✅ Exibição de diálogo de confirmação com descrição do processo
- ✅ Cancelamento do diálogo permanece na tela de cadastro
- ✅ Confirmação remove processo e mostra feedback de sucesso
- ✅ Redirecionamento para Painel após remoção

**Restrições de unidades:**
- ✅ Unidades ocupadas por processos em andamento são desabilitadas
- ✅ Unidades sem mapa de competências são desabilitadas para REVISAO/DIAGNOSTICO

**Fluxo alternativo:**
- ✅ Botão "Iniciar processo" valida dados, cria processo e navega para inicialização

## Lacunas em relação ao requisito
1. **Validação de campo Data limite não completamente testada (passo 4.x)**: O requisito menciona "Campo `Data limite etapa 1`" como obrigatório. O teste não valida explicitamente:
   - Se o campo é obrigatório
   - Mensagem de erro se não preenchido
   - Tipo de campo (data) e formato esperado

2. **Mensagem de validação "Descrição deve estar preenchida" (passo 4.1)**: O teste valida que botão fica desabilitado sem descrição, mas não valida a mensagem de erro "Preencha a descrição" ao submeter formulário vazio.

3. **Mensagem de validação "Pelo menos uma unidade" (passo 4.2)**: Idem anterior. Teste não busca pela mensagem específica.

4. **Validação de unidades para processos de REVISAO/DIAGNOSTICO (passo 4.3)**: O teste valida que unidades SEM mapa são desabilitadas, mas não valida:
   - Mensagem de erro exata: "Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento."
   - Se ao tentar selecionar tais unidades via API (contorno de desabilitar), a mensagem aparece
   - Teste apenas valida a desabilitação do checkbox

5. **Tipo de processo imutável (passo 3, edição)**: O teste valida que o campo é desabilitado, mas não valida que o requisito menciona "O tipo do processo não pode ser alterado" - isto é implementado como "desabilitado", o que é apropriado.

6. **Teste de cancelamento não completo**: O teste valida cancelamento de remoção, mas não valida cancelamento de criação com dados preenchidos (requisito não especifica, mas bom ter).

7. **Validação de formulário vazio não trata Data limite**: O teste começa preenchendo descrição, tipo e data limite, e então valida desabilitação de botão se remover descrição. Não há um teste começando com formulário completamente vazio e preenchendo passo a passo.

8. **Campo observações e Sugestões não validados para criação**: O requisito (passo 2) menciona que o formulário de criação contém também campos para "Observações" e "Sugestões" (no passo 9 mencionado como campos dos subprocessos, não do processo). Pode haver confusão: o processo em "Criado" tem apenas Descrição, Tipo, Data limite e Unidades?

9. **Botões do formulário não completamente testados**: O teste não valida explicitamente os botões "Cancelar", "Salvar" e "Iniciar processo" quando aparecem (exceto parcialmente).

## Alterações necessárias no teste E2E
- Adicionar teste que valida mensagens de erro exatas (ajustar regex para ser mais específica):
  - "Preencha a descrição" quando vazio
  - "Pelo menos uma unidade participante deve ser incluída." quando nenhuma selecionada
  - "Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento." para unidades sem mapa
- Adicionar teste que valida mensagem "Processo criado." (sem "sucesso")
- Adicionar teste que valida mensagem "Processo alterado." (sem "sucesso")
- Adicionar teste que valida mensagem "Processo [Descrição do Processo] removido"
- Adicionar teste de campo Data limite obrigatório e mensagem de erro
- Adicionar teste de cancelamento de criação com dados preenchidos
- Adicionar teste que valida todos os botões (Cancelar, Salvar, Iniciar, Remover quando em edição)
- Refinar testes de validação para formulário completamente vazio e preenchimento passo a passo

## Notas e inconsistências do requisito
- O requisito menciona campos "Observações" e "Sugestões" na seção de formulário (passo 2), mas parece que estes são campos dos subprocessos, não do processo em criação. Pode haver confusão de estrutura.
- A regra de unidade interoperacional (passo 2, no comportamento de seleção) é clara: pode ser selecionada independentemente das filhas, diferente de outras intermediárias. O teste valida isto corretamente.
- Requisito é claro sobre validações específicas (passo 4), mas não descreve quando estas validações são disparadas (ao salvar vs ao submitir vs em tempo real).
