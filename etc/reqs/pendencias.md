# Pendências consolidadas da reanálise CDU x E2E (rodada 2)

Esta versão substitui a rodada anterior e consolida pendências após segunda varredura com leitura de specs e helpers.

## Andamento da execução (2026-03-25)
- ✅ **Lote iniciado** com foco no **CDU-02 (Visualizar painel)**, priorizando item P0 de campos da tabela de processos ativos.
- ✅ Adicionada cobertura E2E explícita para validar cabeçalhos obrigatórios da tabela (`Descrição`, `Tipo`, `Unidades participantes`, `Situação`) e presença dos dados recém-criados na mesma linha.
- 🔄 Próximo passo sugerido: fechar regras de exibição restantes do CDU-02 (visibilidade por perfil e regras de funcionamento da listagem) com cenários negativos adicionais.

## Novos aprendizados (rodada atual)
- A validação de cabeçalhos da tabela depende de existir ao menos um processo visível, pois a UI troca para `EmptyState` quando a lista está vazia.
- Para cobrir requisito de “campos da tabela”, é mais robusto preparar dado mínimo via fluxo de criação e depois validar cabeçalho + linha no painel.
- O helper semântico para cabeçalhos reduz duplicação e evita espalhar seletores de coluna em múltiplas specs.

## Síntese executiva
- Escopo: 36 pares requisito x teste E2E.
- Critério: matriz por item do fluxo principal com status `COBERTO`, `PARCIAL` e `NAO_COBERTO` em cada `alinhamento-cdu-xx.md`.
- Uso recomendado: priorizar primeiro os itens `NAO_COBERTO` com impacto de regra de negócio/permissão.

## Pendências prioritárias por CDU (top 3 por arquivo)
### CDU-01
- **P0** `NAO_COBERTO`: O usuário acessa o sistema
- **P1** `PARCIAL`: O sistema exibe a tela `Login`
- **P1** `PARCIAL`: O usuário informa suas credenciais: número do título de eleitor e senha

### CDU-02
- **P1** `PARCIAL`: Na seção `Processos ativos`, o sistema mostra uma tabela de processos ativos (com título 'Processos'). Devem ser
- **P0** `NAO_COBERTO`: Campos da tabela:
- **P0** `NAO_COBERTO`: Regras de exibição e funcionamento:

### CDU-03
- **P1** `PARCIAL`: Campo `Descrição`
- **P1** `PARCIAL`: A lista de unidades **deve deixar desativadas** (não selecionáveis) as unidades que já estejam participando de
- **P1** `PARCIAL`: Se todas as unidades de uma subárvore estiverem selecionadas, o nó raiz desta subárvore deve ser

### CDU-04
- **P1** `PARCIAL`: Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.

### CDU-05
- **P1** `PARCIAL`: Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.

### CDU-06
- **P1** `PARCIAL`: A tela será composta pelas seções Dados do processo e Unidades participantes.
- **P1** `PARCIAL`: Seção `Dados do processo` (sem título):
- **P1** `PARCIAL`: Informações da descrição, tipo e da situação dos processos (ver arquivo _situacoes.md).

### CDU-07
- **P0** `NAO_COBERTO`: Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.

### CDU-08
- **P1** `PARCIAL`: O sistema mostra a tela `Detalhes de subprocesso` com os dados do subprocesso da unidade.
- **P1** `PARCIAL`: O usuário fornece a descrição do conhecimento e clica no botão de adição correspondente.
- **P0** `NAO_COBERTO`: O usuário repete o fluxo de adição de atividades/conhecimentos.

### CDU-09
- **P1** `PARCIAL`: Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido, além dos botões fixos da
- **P1** `PARCIAL`: Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises do
- **P1** `PARCIAL`: As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas informações poderão ser usadas como subsídio para ajustes no cadastro pelo usuário, antes da realização de nova disponibilização.

### CDU-10
- **P0** `NAO_COBERTO`: Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.

### CDU-11
- **P1** `PARCIAL`: Nesta tela, são apresentados a sigla e o nome da unidade, e cada atividade é apresentada como uma tabela, com

### CDU-12
- **P1** `PARCIAL`: Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.

### CDU-13
- **P1** `PARCIAL`: No painel, o usuário clica no processo de mapeamento.
- **P1** `PARCIAL`: O sistema mostra a tela `Detalhes do processo`.
- **P0** `NAO_COBERTO`: `Histórico de análise`

### CDU-14
- **P1** `PARCIAL`: `Impactos no mapa`;
- **P1** `PARCIAL`: `Histórico de análise`;
- **P1** `PARCIAL`: `Devolver para ajustes`; e

### CDU-15
- **P1** `PARCIAL`: O sistema mostra a tela `Detalhes do subprocesso`.
- **P1** `PARCIAL`: O sistema mostra a tela `Edição de mapa` preenchida com os dados do mapa da unidade, com os seguintes elementos visuais:
- **P1** `PARCIAL`: Um bloco para cada competência criada, cujo título é a descrição da competência

### CDU-16
- **P1** `PARCIAL`: O sistema mostra tela `Detalhes do processo`.
- **P1** `PARCIAL`: O usuário usa como base as informações de impactos mostradas nesta tela para alterar o mapa, podendo alterar descrições de competências, de atividades e de conhecimentos; remover ou criar novas competências; e ajustar a associação das atividades às competências do mapa, conforme descrito no caso de uso `Manter mapa de competências`.
- **P1** `PARCIAL`: O usuário deve associar a uma competência todas as atividades ainda não associadas.

### CDU-17
- **P1** `PARCIAL`: ADMIN clica em uma unidade operacional ou interoperacional com subprocesso na situação 'Mapa criado' ou 'Mapa
- **P1** `PARCIAL`: O sistema mostra a tela `Detalhes de subprocesso`.
- **P1** `PARCIAL`: ADMIN clica no botão `Disponibilizar`.

### CDU-18
- **P1** `PARCIAL`: Usuário clica em uma unidade subordinada que seja operacional ou interoperacional.

### CDU-19
- **P1** `PARCIAL`: Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido também o botão `Histórico de análise`.
- **P1** `PARCIAL`: Usuário fornece as sugestões e clica em `Confirmar`.
- **P1** `PARCIAL`: O sistema notifica a unidade superior hierárquica da apresentação de sugestões para o mapa, com e-mail no modelo abaixo:

### CDU-20
- **P1** `PARCIAL`: O sistema mostra a tela `Detalhes do subprocesso`.
- **P1** `PARCIAL`: `Histórico de análise`;
- **P1** `PARCIAL`: `Devolver para ajustes`;

### CDU-21
- **P1** `PARCIAL`: O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na situação 'Mapa homologado'.
- **P1** `PARCIAL`: Caso negativo, o sistema exibe a mensagem "Não é possível finalizar o processo enquanto houver unidades com mapa ainda não homologado".
- **P1** `PARCIAL`: Caso positivo, sistema mostra diálogo de confirmação: título "Finalização de processo", mensagem "Confirma a finalização do processo [DESCRICAO_PROCESSO]? Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo." e botões `Confirmar` e `Finalizar`.

### CDU-22
- **P1** `PARCIAL`: Botões `Cancelar` e `Registrar aceite`.
- **P1** `PARCIAL`: Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela `Detalhes do processo`.
- **P1** `PARCIAL`: O usuário clica em `Registrar aceite`.

### CDU-23
- **P1** `PARCIAL`: Botões `Cancelar` e `Homologar`.
- **P1** `PARCIAL`: Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do
- **P1** `PARCIAL`: O sistema registra uma movimentação para o subprocesso:

### CDU-24
- **P1** `PARCIAL`: O sistema identifica que existem unidades com subprocessos com mapas criados ou ajustados mas ainda não
- **P1** `PARCIAL`: Na seção de unidades participantes, abaixo da árvore de unidades, o sistema mostra o botão
- **P1** `PARCIAL`: O sistema abre modal de confirmação, com os elementos a seguir:

### CDU-25
- **P1** `PARCIAL`: O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceite do mapa em bloco e se houver mostra o botão `Aceitar mapas em bloco`.
- **P1** `PARCIAL`: Botão `Cancelar` e botão `Registrar aceite`.
- **P1** `PARCIAL`: Caso o usuário escolha `Cancelar`, o sistema interrompe a operação, permanecendo na tela `Detalhes do processo`.

### CDU-26
- **P1** `PARCIAL`: Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do
- **P1** `PARCIAL`: O sistema registra uma movimentação para o subprocesso:
- **P0** `NAO_COBERTO`: `Data/hora`: [Data/hora atual]

### CDU-27
- **P1** `PARCIAL`: O sistema cria internamente um alerta com as seguintes informações:
- **P1** `PARCIAL`: `Processo`: [DESCRICAO_PROCESSO]
- **P1** `PARCIAL`: `Data/hora`: Data/hora atual

### CDU-28
- **P1** `PARCIAL`: Sistema mostra a árvore completa de unidades.
- **P1** `PARCIAL`: ADMIN clica em umas das unidades.
- **P0** `NAO_COBERTO`: Sistema apresenta um modal com estes campos:

### CDU-29
- Sem pendências críticas detectadas nesta rodada automatizada.

### CDU-30
- **P1** `PARCIAL`: O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`.
- **P0** `NAO_COBERTO`: O sistema apresenta opções para:
- **P1** `PARCIAL`: Remover administrador existente.

### CDU-31
- **P1** `PARCIAL`: O sistema mostra a tela Configurações com o valor atual das seguintes configurações, permitindo edição.
- **P1** `PARCIAL`: Dias para inativação de processos (referenciado neste documento como DIAS_INATIVACAO_PROCESSO): Dias depois da
- **P1** `PARCIAL`: O sistema mostra mensagem de confirmação e guarda as configurações internamente. O efeito das configurações deve ser

### CDU-32
- **P1** `PARCIAL`: sistema registra uma movimentação para o subprocesso com os campos:
- **P0** `NAO_COBERTO`: `Data/hora`: Data/hora atual
- **P1** `PARCIAL`: `Unidade origem`: ADMIN

### CDU-33
- **P1** `PARCIAL`: O sistema solicita uma justificativa.
- **P1** `PARCIAL`: O usuário informa a justificativa e confirma.
- **P1** `PARCIAL`: O sistema altera a situação do subprocesso para `REVISAO_CADASTRO_EM_ANDAMENTO`.

### CDU-34
- **P1** `PARCIAL`: O usuário acessa o `Painel`
- **P1** `PARCIAL`: O usuário confirma.
- **P1** `PARCIAL`: `Processo`: [DESCRICAO_PROCESSO]

### CDU-35
- **P1** `PARCIAL`: O usuário acessa Relatórios.
- **P1** `PARCIAL`: O usuário seleciona a opção "Andamento de processo".
- **P1** `PARCIAL`: O usuário seleciona o Processo desejado (ex: "Mapeamento 2027").

### CDU-36
- **P1** `PARCIAL`: O usuário acessa Relatórios na barra de navegacao.
- **P1** `PARCIAL`: O usuário seleciona a opção "Mapas".
- **P0** `NAO_COBERTO`: O usuário define os filtros:

## Temas mais recorrentes (agregação da rodada 2)
- `estruturado, numericamente`: 5 ocorrência(s).
- `histórico, análise`: 3 ocorrência(s).
- `subprocesso, mostra`: 3 ocorrência(s).
- `processo, descricao_processo`: 3 ocorrência(s).
- `subprocesso, registra`: 3 ocorrência(s).
- `data/hora, atual`: 3 ocorrência(s).
- `processo, unidades`: 2 ocorrência(s).
- `subprocesso, unidades`: 2 ocorrência(s).
- `processo, mostra`: 2 ocorrência(s).
- `devolver, ajustes`: 2 ocorrência(s).
- `botões, cancelar`: 2 ocorrência(s).
- `processo, escolha`: 2 ocorrência(s).
- `escolha, botão`: 2 ocorrência(s).
- `unidades, subprocessos`: 2 ocorrência(s).
- `relatórios, acessa`: 2 ocorrência(s).

## Plano de execução sugerido
1. Tratar primeiro os CDUs com maior concentração de itens `NAO_COBERTO` ligados a permissão/perfil/unidade.
2. Para cada CDU, converter cada pendência em cenário E2E explícito (nome de teste refletindo o item do requisito).
3. Onde o requisito exigir comportamento interno não visível na UI, complementar com testes de integração de backend.
4. Após cada lote, atualizar o alinhamento do CDU com evidência do novo teste e remover a pendência correspondente.

## Prontidão para iniciar a revisão dos testes E2E
- Critério de prontidão usado: requisito minimamente claro + pendências priorizadas + próximo escopo sugerido no alinhamento do CDU.

| CDU | Status | Observação de prontidão |
|---|---|---|
| 01 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 02 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 03 | PRONTO | base de análise e pendências objetivas definidas |
| 04 | PENDENTE_REFINAMENTO_REQUISITO | requisito sem fluxo principal estruturado |
| 05 | PENDENTE_REFINAMENTO_REQUISITO | requisito sem fluxo principal estruturado |
| 06 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 07 | PENDENTE_REFINAMENTO_REQUISITO | requisito sem fluxo principal estruturado; há itens sem cobertura E2E |
| 08 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 09 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 10 | PENDENTE_REFINAMENTO_REQUISITO | requisito sem fluxo principal estruturado; há itens sem cobertura E2E |
| 11 | PRONTO | base de análise e pendências objetivas definidas |
| 12 | PENDENTE_REFINAMENTO_REQUISITO | requisito sem fluxo principal estruturado |
| 13 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 14 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 15 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 16 | PRONTO | base de análise e pendências objetivas definidas |
| 17 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 18 | PRONTO | base de análise e pendências objetivas definidas |
| 19 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 20 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 21 | PRONTO | base de análise e pendências objetivas definidas |
| 22 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 23 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 24 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 25 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 26 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 27 | PRONTO | base de análise e pendências objetivas definidas |
| 28 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 29 | PRONTO | base de análise e pendências objetivas definidas |
| 30 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 31 | PRONTO | base de análise e pendências objetivas definidas |
| 32 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 33 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 34 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 35 | PRONTO_COM_GAPS | há itens sem cobertura E2E |
| 36 | PRONTO_COM_GAPS | há itens sem cobertura E2E |

**Leitura operacional**
- `PRONTO_COM_GAPS`: pode iniciar implementação de testes imediatamente, focando itens P0/P1 já listados.
- `PENDENTE_REFINAMENTO_REQUISITO`: recomendado refino rápido do requisito antes de codar para evitar retrabalho.
- `PRONTO`: pode seguir com lote completo conforme priorização.

## Estratégia recomendada para o próximo PR
1. Selecionar um lote de 4 a 6 CDUs `PRONTO_COM_GAPS` com maior risco funcional (permissão, homologação, devolução, alertas).
2. Para cada CDU do lote, implementar ao menos: 1 cenário positivo + 1 negativo + 1 assert de efeito colateral.
3. Fechar cada CDU com evidência objetiva no alinhamento (link para teste criado e item do requisito atendido).
4. Reexecutar apenas os arquivos E2E impactados no PR e registrar saída no corpo do PR.
