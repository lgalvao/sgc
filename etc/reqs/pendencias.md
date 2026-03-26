# Pendências consolidadas da reanálise CDU x E2E (rodada 2)

Esta versão substitui a rodada anterior e consolida pendências após segunda varredura com leitura de specs e helpers.

## Andamento da execução (2026-03-26 - continuidade CDU-28)
- ✅ **CDU-28 reforçado na regra de árvore completa**: cenário inicial passou a validar, de forma explícita, as três secretarias da árvore (`SECRETARIA_1`, `SECRETARIA_2`, `SECRETARIA_3`) e seus principais nós subordinados antes de seguir para o fluxo de criação de atribuição.
- ✅ A cobertura também ganhou evidência de profundidade no ramo intermediário (`COORD_11`), incluindo validação das seções filhas (`SECAO_111`, `SECAO_112`, `SECAO_113`).
- ✅ Regressão direcionada de `e2e/cdu-28.spec.ts` executada com sucesso após a ampliação das asserções.
- 🔄 Próximo passo sugerido: manter no E2E apenas a validação estrutural mínima da árvore e mover efeitos internos de notificação por e-mail para integração backend, evitando acoplamento desnecessário na UI.

## Novos aprendizados (continuidade CDU-28 em 2026-03-26)
- Para requisito de “árvore completa”, validar somente o ramo usado na ação principal pode deixar lacunas; uma verificação incremental por raiz e subárvore traz evidência mais fiel do comportamento esperado.
- Reusar um helper de validação de ramo (`sigla do nó + lista de filhas`) reduz duplicação e facilita manutenção quando a estrutura da árvore evoluir no seed.

## Andamento da execução (2026-03-26 - continuidade CDU-30)
- ✅ **CDU-30 avançado em regra de validação negativa**: incluído cenário E2E para tentativa de adicionar como administrador um título já administrador (`111111`), com asserção de erro funcional retornado pela API.
- ✅ O cenário novo também confirma permanência do modal de adição após falha, preservando o contexto de correção do usuário sem navegação inesperada.
- ✅ Regressão direcionada do arquivo `e2e/cdu-30.spec.ts` executada com sucesso no ambiente atual.
- 🔄 Próximo passo sugerido: cobrir no CDU-30 o ramo de validação de remoção inválida (auto-remoção e/ou único administrador) por combinação de E2E + integração backend, conforme viabilidade da massa.

## Novos aprendizados (continuidade CDU-30 em 2026-03-26)
- Em fluxos de modal administrativo, a dupla **`waitForResponse` com `status >= 400` + assert do payload de erro (`message`)** melhora evidência de regra de negócio quando a validação vem da API e não de bloqueio local.
- Para o CDU-30, manter o modal aberto após erro de domínio é comportamento relevante do requisito funcional (usuário corrige e tenta novamente), então vale validar explicitamente esse estado.

## Andamento da execução (2026-03-26 - rodada complementar)
- ✅ **Estabilização de execução E2E** aplicada para reduzir falsos negativos de infra em execução fria: timeout do `webServer` do Playwright ampliado para acomodar subida completa de backend/frontend no ambiente atual.
- ✅ **Novo avanço multi-CDU** além dos relatórios: **CDU-27 (Alterar data limite)** recebeu cobertura explícita de alerta no painel da unidade destino após alteração por ADMIN.
- ✅ No CDU-27, a asserção agora valida os campos críticos do alerta gerado (`Descrição`, `Processo`, `Data/Hora` e `Origem` com `ADMIN`) após troca de perfil para CHEFE da unidade afetada.
- ✅ Regressão direcionada executada em lote único dos specs alterados (`cdu-27`, `cdu-35`, `cdu-36`) para evidenciar passagem conjunta dos casos trabalhados.
- 🔄 Próximo passo sugerido: avançar para o próximo lote priorizado (`CDU-28`, `CDU-30`, `CDU-36`) e deixar `CDU-13`, `CDU-14` e `CDU-20` apenas com complementos pontuais de backend ou bordas de homologação.

## Novos aprendizados (rodada complementar 2026-03-26)
- Em ambiente com build cold de backend Java, timeout curto de `webServer` no Playwright gera falha de infraestrutura sem relação com regra de negócio; calibrar timeout evita retrabalho e ruído no ciclo.
- Para CDU-27, validar apenas mensagem de sucesso não garante requisito de notificação; incluir leitura da tabela de alertas da unidade destino aumenta a evidência de comportamento esperado.
- Alternar perfil no mesmo teste (ADMIN -> CHEFE da unidade) foi suficiente para confirmar o efeito colateral do fluxo sem depender de fixtures adicionais.

## Andamento da execução (2026-03-26)
- ✅ **Lote multi-CDU concluído** com foco em relatórios: **CDU-35 (Andamento de processo)** e **CDU-36 (Mapas)**.
- ✅ CDU-35 reforçado com validações explícitas de fluxo ponta a ponta: acesso em `Relatórios`, seleção obrigatória de processo, geração da tabela e presença dos campos-chave do relatório (`Sigla`, `Nome`, `Situação`, `Data`, `Responsável`, `Titular`).
- ✅ CDU-35 ampliado também com evidência de exportação: clique no botão `PDF` e validação do nome de arquivo baixado (`relatorio-andamento-{codigo}.pdf`).
- ✅ CDU-36 evoluído para cobrir melhor a etapa de filtros: validação dos campos `Selecione o Processo` e `Selecione a unidade`, estado inicial desabilitado de `Gerar PDF` e habilitação após seleção do processo.
- ✅ CDU-36 ganhou verificação explícita de comportamento do filtro opcional de unidade no request de geração: sem `unidadeId` quando o valor permanece em `Todas as unidades`.
- ✅ Execução de regressão direcionada validada com sucesso para os dois specs alterados (`e2e/cdu-35.spec.ts` e `e2e/cdu-36.spec.ts`).
- 🔄 Próximo passo sugerido: avançar na cobertura de conteúdo semântico do PDF de mapas (competências/atividades/conhecimentos) com estratégia híbrida (assert de backend + smoke de download no E2E).

## Novos aprendizados (rodada 2026-03-26)
- Para CDU-35, validar somente “tabela visível” é frágil; a cobertura fica mais robusta quando inclui cabeçalhos funcionais e pelo menos uma linha com padrões esperados de data/situação.
- Em telas com dois blocos de relatório, escopar seletores pelo `tabpanel` reduz ambiguidade entre campos iguais (ex.: `Selecione o Processo` em duas abas).
- No CDU-36, o requisito de unidade opcional pode ser coberto sem fixture extra ao validar o request emitido sem querystring `unidadeId` quando a opção padrão é mantida.
- Para comprovar exportação E2E sem acoplamento à implementação interna, a dupla `waitForEvent('download')` + asserção de `suggestedFilename` entrega evidência objetiva e estável.

## Andamento da execução (2026-03-25)
- ✅ **Lote iniciado** com foco no **CDU-02 (Visualizar painel)**, priorizando item P0 de campos da tabela de processos ativos.
- ✅ Adicionada cobertura E2E explícita para validar cabeçalhos obrigatórios da tabela do painel compacto (`Descrição`, `Tipo`, `Unidades`, `Situação`) e presença dos dados recém-criados na mesma linha.
- ✅ Execução do arquivo `e2e/cdu-02.spec.ts` validada com sucesso após instalação dos navegadores Playwright no ambiente.
- ✅ Iniciado também o fechamento do CDU-01 com cenário explícito da tela de login (título, subtítulo, campos e botão de entrar).
- ✅ Fechado bloco de regras de clique da tabela de processos do CDU-02 com cobertura explícita por perfil: `ADMIN` abre cadastro para processo `Criado`; `ADMIN`/`GESTOR` abrem `Detalhes do processo` para `Em andamento`; `CHEFE` abre `Detalhes do subprocesso`.
- ✅ Incluídos cenários negativos/funcionais complementares no CDU-02 para reforçar regra de exibição por perfil e comportamento de navegação da listagem.
- ✅ Correção de preparação de ambiente E2E aplicada: instalação do Playwright refeita com o comando obrigatório `npx playwright install --with-deps --only-shell` (inclui dependências de SO para execução headless estável).
- ✅ Novo cenário E2E no CDU-02 cobrindo tabela de alertas: validação explícita dos campos (`Data/Hora`, `Descrição`, `Processo`, `Origem`), comportamento de ordenação fixa por data/hora (sem reordenação por clique) e transição de alerta não lido (`fw-bold`) para lido após recarga.
- ✅ Lote expandido para outros casos de uso críticos: CDU-30 (validação de colunas/ações da lista de administradores + bloqueio de adição sem título) e CDU-32 (validação explícita dos campos da movimentação de reabertura, incluindo `Data/hora`, `Unidade origem` e `Unidade destino`).
- ✅ Novo lote multi-CDU executado em sequência pesada: CDU-26 (homologação em bloco com validação de movimentação e `Data/hora atual`) e CDU-28 (campos obrigatórios da tela de atribuição + caminho de cancelamento explícito).
- 🔄 Próximo passo sugerido: ampliar matriz de visibilidade de alertas por perfil no CDU-02 (principalmente regra específica de `SERVIDOR` não herdar alertas de unidade) e avançar em CDUs de histórico de análise pendentes (ex.: CDU-14).

## Novos aprendizados (rodada atual)
- A validação de cabeçalhos da tabela depende de existir ao menos um processo visível, pois a UI troca para `EmptyState` quando a lista está vazia.
- No painel (`PainelView`), a tabela roda em modo `compacto`; portanto o cabeçalho correto é `Unidades` (e não `Unidades participantes`).
- Para cobrir requisito de “campos da tabela”, é mais robusto preparar dado mínimo via fluxo de criação e depois validar cabeçalho + linha no painel.
- O helper semântico para cabeçalhos reduz duplicação e evita espalhar seletores de coluna em múltiplas specs.
- Para CDU-01, consolidar validações da tela de login em helper dedicado melhora legibilidade e reaproveitamento em cenários de autenticação inválida e múltiplos perfis.
- Para validar regra de clique por perfil no CDU-02 com estabilidade, usar **um único processo em andamento** e alternar login no mesmo teste reduziu custo de setup e evitou divergência entre massas.
- A rota de edição/cadastro de processo pode variar (`/processo/cadastro/{codigo}` ou `?codProcesso={codigo}`), então os asserts devem aceitar os dois formatos.
- Para CHEFE, o clique na linha do painel tende a redirecionar diretamente para rota de subprocesso (`/processo/{codigo}/{SIGLA_UNIDADE}`), o que fornece evidência objetiva da regra de navegação por perfil.
- **Padronização obrigatória para ambiente E2E headless:** usar `npx playwright install --with-deps --only-shell`; usar apenas `npx playwright install` pode deixar dependências nativas ausentes no host e quebrar a inicialização do navegador.
- Evitar usar, na descrição dinâmica de processos de teste, termos idênticos ao texto da situação (ex.: `Em andamento`), pois pode gerar ambiguidade de seletor textual na mesma linha da tabela.
- Para validar “não reordenável” na tabela de alertas, a asserção mais estável é verificar que o cabeçalho não ganha estado de sort (`aria-sort`) após clique e que a tabela mantém comportamento passivo no header.
- Em CDUs administrativos (como CDU-30), cobrir explicitamente os cabeçalhos da listagem e os rótulos dos botões de modal reduz falso positivo de navegação “ok” sem validação real de requisitos visuais/funcionais.
- Para CDUs com requisito de auditoria temporal (como CDU-32), validar `Data/hora` por regex no registro de movimentação aumenta cobertura de regra P0 sem acoplar o teste a timestamp exato.
- Em lotes pesados, agrupar specs por domínio (ex.: homologação/reabertura/atribuição) numa única execução Playwright reduz tempo total de setup de infraestrutura E2E.
- Para CDU-28, apesar do requisito mencionar modal, a implementação atual está em rota dedicada (`/unidade/{codigo}/atribuicao`); o teste deve validar os mesmos campos/ações esperados sem acoplar ao tipo de container visual.

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
- **P1** `PARCIAL`: Evidência direta da tela `Detalhes do processo` antes da seleção da unidade.
- **P1** `PARCIAL`: Campos de movimentação, alerta interno e notificações não observáveis de forma estável na UI.
- **P1** `PARCIAL`: Auditoria de `Data/hora atual` fora do histórico de análise visível.

### CDU-14
- **P1** `PARCIAL`: Homologação por `ADMIN`, incluindo os ramos com e sem impacto no mapa.
- **P1** `PARCIAL`: Mensagem final de aceite e redirecionamento ao painel com assert direto.
- **P1** `PARCIAL`: Efeitos colaterais internos (`alerta`, e-mail, auditoria temporal) sem superfície E2E estável.

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
- **P1** `PARCIAL`: Cancelamento de homologação em estado válido e estável para E2E.
- **P1** `PARCIAL`: Notificações por e-mail e alerta interno após aceite/devolução.
- **P1** `PARCIAL`: Campos internos de auditoria não expostos de forma consistente na UI.

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
