## **UC-002 \- Monitorar Processos**

**Ator**: SEDOC

**Pré-condições**: Ao menos um processo de mapeamento criado.

**Fluxo principal:**

1. SEDOC acessa o sistema.  
2. Sistema mostra a página **Painel**, incluindo:  
   1. Lista de processos de em andamento, com a situação consolidada, com base na situação das unidades participantes do processo.  
   2. Estatísticas agregadas (número de unidades em andamento, iniciadas e finalizadas).   
   3. Filtragem/ordenação por situação, prazo ou unidade   
3. SEDOC clica em um processo.  
4. Sistema mostra a página **Detalhes de processo**, com uma visualização da hierarquia de todas as unidades participantes do processo, com a situação de cada uma, calculada com base na situação das unidades subordinadas. Esta hierarquia deve permitir expandir/recolher as unidades e navegar para uma página com os detalhes de uma unidade.  
5. SEDOC clica em uma unidade  
6. Sistema mostra página de **Detalhes de unidade**, com as seguintes informações sobre a unidade:   
   1. Sigla e nome;   
   2. Nome do Servidor Responsável, com informações de contato;   
   3. Situação do processo de cadastro de atividades e conhecimentos  
   4. Se unidade tiver atribuição temporária definida, sistema mostra detalhes da atribuição temporária e botões 'Editar' e 'Remover' para a atribuição. Se unidade não tiver atribuição temporária, sistema mostra botão 'Nova atribuição temporária'. **VER: UC-007: Manter Atribuição Temporária**  
7. Se unidade estiver com situação 'Finalizada', sistema mostra um botão para manutenção do mapa, segundo essas regras (**VER: UC-005: Manter Mapa de Competências)**:   
   1. Se já houver um mapa em andamento, 'Editar mapa';   
   2. Se não houver mapa, 'Criar mapa';   
   3. Se mapa já estiver disponibilizado para validação, 'Visualizar mapa'.  
8. SEDOC clica em Painel para voltar à página inicial do sistema.