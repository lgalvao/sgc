# Alinhamento CDU-16 - Ajustar mapa de competências

## Cobertura atual do teste
O teste E2E cobre:
- **Preparação extensiva** (6 passos): Cria processo de mapeamento, CHEFE adiciona 3 atividades, gestores aceitam, ADMIN homologa e cria 3 competências, CHEFE valida mapa, gestores aceitam, ADMIN homologa e finaliza mapeamento, cria e inicia processo de revisão (linhas 41-179).
- **Preparação 8**: CHEFE revisa atividades - adiciona nova atividade, edita existente, remove outra, disponibiliza (linhas 181-202).
- **Preparação 9**: Gestores e ADMIN aceitam revisão (linhas 204-222).
- **Navegação para Mapa em contexto de Revisão** (passo 1-6): Acessa processo de revisão, navega para mapa (linhas 228-237).
- **Botão "Impactos no mapa"**: Clica, valida presença do modal com seções "Atividades Inseridas" e "Competências Impactadas", fecha modal (linhas 239-253).
- **Edição de competência**: Abre modal de edição, valida preenchimento, cancela (linhas 255-267).
- **Associação de atividade não vinculada**: Cria nova competência associada a atividade nova, valida presença (linhas 269-275).

## Lacunas em relação ao requisito
**Fluxo principal (passos 1-6) não completamente validado:**
- **Passo 1-2**: Não há validação explícita de que ADMIN está no Painel e clica em processo de revisão.
- **Passo 3-4**: Não valida que clica em unidade com subprocesso nas situações 'Revisão do cadastro homologada' ou 'Mapa ajustado', ou que tela "Detalhes do subprocesso" é exibida.
- **Passo 5**: Não há validação de clique no card "Mapa de Competências" (implícito no teste ao chamar `navegarParaMapa`).
- **Passo 6**: Não valida que tela exibe "Edição de mapa" com os elementos descritos em CDU-15 (blocos de competência, atividades, conhecimentos, botões).

**Fluxo de análise de impactos (passos 7-8):**
- **Passo 8**: Teste abre modal de impactos mas não valida se a lógica de "impactos detectados" é a mesma de CDU-14 (requisito remete a CDU-19 "Verificar impactos no mapa de competências").
- Teste valida seções "Atividades Inseridas" e "Competências Impactadas" mas não valida:
  - Que "Atividades Inseridas" mostra TODAS as novas atividades adicionadas no cadastro.
  - Que "Competências Impactadas" mostra TODAS as competências que teriam conhecimentos alterados.

**Fluxo de ajuste do mapa (passos 9-9.1):**
- **Passo 9**: Requisito diz ADMIN pode "alterar descrições de competências, de atividades e de conhecimentos; remover ou criar novas competências; ajustar a associação das atividades às competências".
  - Teste cria nova competência (✓) mas **não testa**:
    - Alterar descrição de competência já existente (passo 12 de CDU-15, aplicável aqui).
    - Alterar descrição de atividade (não é escopo direto de CDU-16, é feito na atividades).
    - Alterar descrição de conhecimento (não é escopo direto de CDU-16, é feito em atividades).
    - Remover uma competência existente (passo 13 de CDU-15, aplicável aqui).
    - Ajustar associação de atividades a competências (seria editar competência).

- **Passo 9.1**: Requisito diz "ADMIN deve associar a uma competência todas as atividades ainda não associadas" - 
  - Teste cria competência para atividade não associada (✓) mas não valida:
    - Que **antes** de disponibilizar, ADMIN verifica se há atividades não associadas.
    - Que o sistema **força** essa associação (se é validação obrigatória em CDU-17).

**Passo 10 não coberto:**
- Teste não valida que ao clicar "Disponibilizar", o sistema segue o fluxo de CDU-17.
- Não valida redirecionamento ou mensagens de sucesso/erro.

**Validações genéricas não feitas:**
- Teste não valida que a tela de "Edição de mapa" em contexto de revisão é **idêntica** à de mapeamento (exceto por presença do botão "Impactos no mapa").
- Teste não valida que mudanças são **salvas automaticamente** ou que há um botão de salvar (implícito em CDU-15).

## Alterações necessárias no teste E2E
1. **Validar estrutura de "Edição de Mapa" em contexto de revisão**:
   - Mesmos blocos de competência, atividades, conhecimentos que em CDU-15.
   - Validar presença de botões `Impactos no mapa` e `Disponibilizar`.

2. **Validar navegação e pré-condições (passos 1-6)**:
   - Confirmar que ADMIN está em Painel.
   - Confirma que clica em processo de revisão.
   - Confirma que clica em unidade com situação 'Revisão do cadastro homologada' ou 'Mapa ajustado'.
   - Confirma que tela "Detalhes do subprocesso" é exibida.

3. **Expandir cobertura de "Impactos no mapa"**:
   - Validar que modal exibe "Atividades Inseridas" com atividades específicas adicionadas na revisão.
   - Validar que modal exibe "Competências Impactadas" com competências que têm atividades removidas/alteradas.
   - Validar que pode fechar modal e retorna à edição de mapa.

4. **Testar múltiplos cenários de ajuste (passo 9)**:
   - **Criar nova competência** para atividade não associada ✓ (já cobre).
   - **Editar competência existente**: Alterar descrição ou atividades associadas.
   - **Remover competência**: Validar exclusão (reutilizar lógica de CDU-15).
   - **Ajustar associação de atividades**: Editar competência para associar/desassociar atividades.

5. **Testar validação de "atividades não associadas" (passo 9.1)**:
   - Criar estado onde há atividade não associada a nenhuma competência.
   - Validar que sistema não permite disponibilizar sem associar todas.
   - Ou, se a validação é apenas em CDU-17, validar que teste de CDU-17 a cobre.

6. **Validar navegação para disponibilização (passo 10)**:
   - Após ajustes finalizados, clicar "Disponibilizar".
   - Confirmar que redireciona para fluxo de CDU-17 (ou validar presença do modal de disponibilização).

7. **Adicionar cenário de "Mapa ajustado"**:
   - Requisito menciona que subprocesso pode estar em situação 'Mapa ajustado' (pré-condição).
   - Teste deveria cobrir que ADMIN consegue acessar e ajustar novamente um mapa já ajustado.

## Notas e inconsistências do requisito
- **Passo 8**: Requisito diz "Ver caso de uso `Verificar impactos no mapa de competências`" mas não detalha se ADMIN **precisa** usar essa função ou apenas **pode** usar como subsídio.
- **Passo 9**: Ambiguidade sobre escopo - diz que ADMIN pode "alterar descrições de atividades e conhecimentos" mas essas alterações seriam feitas na tela de atividades (CDU-?), não de mapa. Não fica claro se há edição in-line aqui.
- **Passo 9.1**: Requisito diz "ADMIN deve associar" - Usar "deve" deixa ambíguo se é obrigatório **antes** de disponibilizar (pré-validação) ou se é um lembrete/boa prática.
- **Pré-condição**: Diz "processo de Revisão com ao menos uma unidade com subprocesso nas situações 'Revisão do cadastro homologada' ou 'Mapa ajustado'" - Não está claro como subprocesso chega a 'Mapa ajustado' (é estado intermediário de revisão anterior? Passo anterior de CDU-16?).
