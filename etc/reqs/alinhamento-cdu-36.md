# Alinhamento CDU-36 - Gerar relatório de mapas

## Cobertura atual do teste
O teste E2E (cdu-36.spec.ts) abrange:
- Setup: Criação de processo de mapeamento com mapa homologado, 30 dias de limite para unidade ASSESSORIA_12
- Verificação de visualização do processo na tabela do painel
- Navegação para página de Relatórios
- Validação de URL `/relatorios` e heading "Relatórios"
- Clique na aba "Mapas"
- Localização de select "Selecione o Processo" e "Selecione a unidade"
- Validação inicial: botão "Gerar PDF" desabilitado
- Seleção do processo no primeiro select
- Validação: botão "Gerar PDF" habilitado após seleção do processo (unidade ainda não selecionada)
- Clique em "Gerar PDF"
- Validação de evento de download
- Validação de nome do arquivo: `relatorio-mapas-{processo.codigo}.pdf`

## Lacunas em relação ao requisito
- **Falta validação de estrutura do PDF**: O requisito (linha 22-28) especifica que PDF deve conter estrutura: Unidade (Sigla e Nome), para cada competência: Descrição da competência, Atividades da competência, Conhecimentos da atividade. O teste não valida conteúdo do PDF.
- **Falta teste com filtro de unidade**: O requisito (linha 17-18) especifica "Unidade (Opcional - se vazio, considera todas as unidades do processo)". O teste não valida (a) comportamento com unidade não selecionada, (b) comportamento com unidade selecionada especificamente.
- **Falta validação de diferenças no relatório**: Não há teste comparando PDF gerado com unidade específica vs. sem filtro de unidade.
- **Falta validação de casos extremos**: Não há teste de processo sem mapas homologados, sem unidades, etc.
- **Falta teste de múltiplas seleções**: Não há validação de que seleções diferentes produzem PDFs diferentes.

## Alterações necessárias no teste E2E
- Adicionar validação de conteúdo do PDF gerado (estrutura, seções, competências, atividades, conhecimentos)
- Adicionar cenário com seleção de unidade específica e validação de que apenas aquela unidade aparece no PDF
- Adicionar cenário SEM seleção de unidade para validar que todas as unidades do processo aparecem no PDF
- Adicionar teste comparativo: gerar com unidade específica vs. sem unidade específica e validar diferenças de conteúdo
- Considerar adicionar validação de metadados do PDF (título, data de criação, etc.)
- Considerar adicionar cenário com processo sem mapas para validar mensagem de erro ou comportamento esperado

## Notas e inconsistências do requisito
- Requisito menciona filtro "Unidade (Opcional)", mas lógica em CDU-36 parece exigir que Processo seja selecionado primeiro (linha 17 especifica "Obrigatório"). Teste valida que processo habilita botão, mas não testa se unidade é realmente opcional (gerar sem selecionar).
- Estrutura do PDF no requisito é hierárquica: Unidade → Competência → Atividades → Conhecimentos. Teste não valida essa hierarquia, apenas download.
- Requisito não especifica se "Mapas" refere-se a Mapas Homologados ou Mapas em qualquer situação. Setup do teste usa `criarProcessoMapaHomologadoFixture`, sugerindo apenas mapas homologados são relevantes.
- Linha 20 do teste seleciona `.last()` para select de processo, sugerindo que há múltiplos selects na página (um na aba Andamento CDU-35, outro na aba Mapas CDU-36). Requisito não esclarece a estrutura da página de Relatórios.
