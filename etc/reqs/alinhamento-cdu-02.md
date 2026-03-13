# Alinhamento CDU-02 - Visualizar painel

## Cobertura atual do teste
O teste `cdu-02.spec.ts` cobre os seguintes cenários:

**Como ADMIN:**
- ✅ Exibição de seções principais: "Processos" e "Alertas"
- ✅ Exibição do botão "Criar processo"
- ✅ Ordenação da tabela de processos (clique em cabeçalho alterna sorting)
- ✅ Criação de processo e visualização na tabela
- ✅ Ocultação de processos "Criado" para perfis não-ADMIN (apenas ADMIN vê)
- ✅ Validação de que unidades intermediárias são filtradas no envio (comportamento de seleção em cascata)
- ✅ Validação de que unidades intermediárias são **habilitadas** na árvore (novo comportamento mencionado no teste)

**Como GESTOR:**
- ✅ Ocultação do botão "Criar processo"
- ✅ Exibição da tabela de processos vazia
- ✅ Exibição da tabela de alertas vazia
- ✅ Ordenação da tabela de alertas (coluna "Processo")

## Lacunas em relação ao requisito
1. **Filtro de unidades participantes não completamente validado**: O requisito (passo 2) especifica que devem ser mostrados apenas processos que incluam "entre as unidades participantes a unidade do usuário e/ou suas unidades subordinadas". O teste não valida este filtro para um GESTOR (um GESTOR deveria ver apenas processos de sua unidade e subordinadas).

2. **Campos da tabela de Processos (passo 2.1)**: O teste não valida explicitamente:
   - Presença e conteúdo do campo "Descrição"
   - Presença e conteúdo do campo "Tipo"
   - Campo "Unidades participantes" não é validado completamente (teste apenas busca por "COORD_11" como unidade representativa)
   - Campo "Situação" não é explicitamente validado

3. **Regra de ordenação de alertas (passo 2.2, linha 59)**: O requisito especifica que a ordenação deve ser "primeiro critério Processo (asc/desc) e em seguida Data/hora (desc)". O teste apenas clica no cabeçalho "Processo" e valida que `aria-sort` muda, mas não valida:
   - Que a ordenação secundária por Data/hora é aplicada
   - Que a ordenação de Data/hora está em descending

4. **Alertas não visualizados em negrito (passo 3.2)**: O requisito especifica que alertas não visualizados devem aparecer em negrito. O teste não valida isto.

5. **Marca de "visualizado pelo usuário" (passo 3.2)**: O requisito menciona que "Na primeira visualização de um ou mais alertas, estes devem ser marcados como visualizado **pelo usuário**". O teste não valida que ao ver a página de Alertas, os alertas são marcados como visualizados e deixam de aparecer em negrito em próximas visualizações.

6. **Teste de GESTOR incompleto**: O teste com GESTOR é bem superficial:
   - Não valida se há processos que devem ser mostrados (participantes da sua unidade)
   - Não valida que processos fora de sua unidade/subordinadas são ocultados
   - Não testa se os botões de ação sobre processos (clique para abrir detalhes) funcionam para GESTOR

7. **Testes para CHEFE e SERVIDOR ausentes**: O requisito (passo 2.2) menciona regras específicas de clique para CHEFE/SERVIDOR ("Detalhes do subprocesso" vs "Detalhes do processo" para ADMIN/GESTOR). Não há testes para estes perfis.

8. **Comportamento de clique em processos não testado**: O requisito (passo 2.2) descreve que clicar em processos em diferentes situações abre diferentes telas. O teste não valida:
   - Clicar em processo "Criado" abre "Cadastro de processo" (ADMIN)
   - Clicar em "Em andamento"/"Finalizado" abre "Detalhes do processo" (ADMIN/GESTOR) ou "Detalhes do subprocesso" (CHEFE/SERVIDOR)

9. **Pré-condição não validada**: O requisito menciona "Usuário ter feito login (qualquer perfil)". O teste utiliza fixtures de autenticação, o que é correto, mas não há um teste explícito de comportamento sem login.

## Alterações necessárias no teste E2E
- Adicionar testes para perfis CHEFE e SERVIDOR com painel
- Adicionar testes que validam o filtro de unidades participantes (criar processo com ADMIN e verificar se GESTOR de unidade participante vê, e de unidade não-participante não vê)
- Adicionar validação explícita dos campos: Descrição, Tipo, Unidades participantes, Situação na tabela
- Adicionar teste que valida a ordenação secundária de alertas (Processo primário, Data/hora secundário)
- Adicionar teste que valida que alertas não visualizados aparecem em negrito (requer dados de teste com alertas)
- Adicionar teste que valida marca de "visualizado" (carregar página, marcar como visualizado, recarregar, verificar não está mais em negrito)
- Adicionar teste que valida clique em processo "Criado" abre "Cadastro de processo" para ADMIN
- Adicionar teste que valida clique em processo "Em andamento" abre "Detalhes do processo" para ADMIN
- Adicionar teste que valida clique em processo "Em andamento" abre "Detalhes do subprocesso" para CHEFE
- Expandir teste de GESTOR para incluir validações de visibilidade de processos baseado em unidades participantes

## Notas e inconsistências do requisito
- O requisito (passo 2.2, linha 31-36) descreve comportamento diferente para CHEFE/SERVIDOR vs ADMIN/GESTOR ao clicar em processos, sugerindo que há casos de uso interdependentes (CDU-06 para Detalhes do processo, CDU não mencionado para Detalhes do subprocesso)
- A ordenação de alertas (passo 3.2, linha 59) é clara mas complexa: ordenação primária por Processo e secundária por Data/hora. Pode ser confusa implementar isto sem especificação da direção de ordenação inicial
- Não há clareza sobre o que são "Alertas registrados pelo sistema" e como são populados (referência aos CDU-04/05 onde alertas são criados)
