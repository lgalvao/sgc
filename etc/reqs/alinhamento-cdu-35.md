# Alinhamento CDU-35 - Gerar relatório de andamento

## Cobertura atual do teste
O teste E2E (cdu-35.spec.ts) cobre:
- Setup: Criação de processo de mapeamento com 30 dias de limite para unidade ASSESSORIA_12, iniciado
- Verificação de visualização do processo na tabela de processos do painel
- Navegação para página de Relatórios (via link "Relatórios")
- Validação de URL `/relatorios` e heading "Relatórios"
- Localização de select "Selecione o Processo"
- Validação inicial: botão "Gerar relatório" desabilitado
- Seleção do processo criado no select
- Validação: botão "Gerar relatório" habilitado após seleção
- Clique em "Gerar relatório"
- Validação de visualização de tabela de relatório
- Validação de visualização de botão "PDF"

## Lacunas em relação ao requisito
- **Falta validação de colunas específicas**: O requisito (linha 14-20) especifica que o relatório deve conter colunas: "Sigla da unidade", "Nome da unidade", "Situação atual do subprocesso", "Data da última movimentação", "Responsável", "Titular (Se não for o responsável)". O teste valida apenas que uma tabela existe, não valida conteúdo ou colunas específicas.
- **Falta validação de dados do relatório**: Não há verificação de que os dados exibidos correspondem ao processo selecionado ou às unidades participantes.
- **Falta teste de exportação PDF**: O requisito especifica que usuário "pode optar por exportar os dados para PDF clicando no botão `PDF`" (linha 22-23) e "O sistema gera o arquivo selecionado e o disponibiliza para download" (linha 25). O teste não valida o download ou conteúdo do PDF.
- **Falta teste de múltiplos processos**: Não há validação de que select contém múltiplos processos ou que funciona corretamente com seleções diferentes.
- **Falta validação de situações de subprocessos**: Não há teste de como relatório exibe subprocessos em diferentes situações (em andamento, homologado, finalizado, etc.).

## Alterações necessárias no teste E2E
- Adicionar validação explícita das colunas do relatório (Sigla, Nome, Situação, Data última movimentação, Responsável, Titular)
- Adicionar validação de que dados exibidos correspondem ao processo selecionado (ex: unidade ASSESSORIA_12 deve estar na tabela)
- Adicionar teste de clique no botão "PDF" e validação de download do arquivo
- Adicionar validação de conteúdo do PDF gerado (estrutura, dados esperados)
- Adicionar cenário com múltiplos processos para validar que select funciona corretamente
- Considerar adicionar teste com subprocessos em diferentes situações (não apenas iniciados)
- Considerar adicionar validação de responsáveis e titulares no relatório

## Notas e inconsistências do requisito
- Requisito é simples e direto, mas teste executa apenas validação mínima de UI (existência de elementos) sem validação de dados reais.
- Coluna "Titular (Se não for o responsável)" sugere lógica condicional: se responsável = titular, não exibir duplicação. Teste não valida esse comportamento.
- Não está claro se "Situação atual do subprocesso" refere-se ao status textual (ex: "Em andamento") ou a um código interno. Teste não valida ambos.
- Requisito não menciona filtros ou pré-requisitos específicos (ex: mostrar apenas subprocessos ativos, passados, etc.), apenas seleção de processo.
