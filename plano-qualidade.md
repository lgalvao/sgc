# Plano de Qualidade Estrutural do SGC

## Objetivo

Reduzir a dívida técnica estrutural do SGC sem perder a proteção que a suíte atual de testes já oferece.

O foco deste plano não é aumentar contagem de testes nem cobertura nominal. O foco é corrigir os problemas que continuam caros mesmo com muitos testes passando:

- baixa coesão em services/controllers/facades grandes;
- contratos HTTP frouxos ou inconsistentes entre backend e frontend;
- duplicações de fluxo e regra espalhadas;
- tratamento de erro ruidoso e pouco coerente;
- nomenclatura e modelagem com sinais de consolidação incompleta;
- gates de qualidade que validam muita coisa, mas não necessariamente o que mais importa.

---

## Estado atual resumido

### Sinais positivos

- `./gradlew :backend:qualityCheckFast --console=plain` passou com `2125` testes.
- `npm run typecheck` na raiz passou.
- O frontend já recebeu simplificação relevante recente e a auditoria arquitetural dele está controlada.

### Sinais de dívida ainda ativa

- `npm --prefix frontend run quality:lint` ainda falha.
- O backend concentra os maiores hubs de complexidade do sistema.
- A borda backend/frontend ainda vaza detalhes de modelo interno.
- Parte dos gates de cobertura/mutação está configurada com thresholds neutros.
- O tratamento de erro mistura preocupação de infraestrutura, navegação e UX.

---

## Diagnóstico principal

## 1. O problema central hoje é estrutural, não funcional

O projeto já tem boa proteção contra regressão, mas isso convive com desenho desigual.

Há arquivos muito grandes e muito centrais que acumulam responsabilidades demais:

- [backend/src/main/java/sgc/processo/service/ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java)
- [backend/src/main/java/sgc/relatorio/RelatorioFacade.java](/Users/leonardo/sgc/backend/src/main/java/sgc/relatorio/RelatorioFacade.java)
- [backend/src/main/java/sgc/subprocesso/SubprocessoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoController.java)
- [backend/src/main/java/sgc/e2e/E2eController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/e2e/E2eController.java)
- [frontend/src/composables/useCadastroTela.ts](/Users/leonardo/sgc/frontend/src/composables/useCadastroTela.ts)
- [frontend/src/axios-setup.ts](/Users/leonardo/sgc/frontend/src/axios-setup.ts)

Isso gera um efeito típico de sistemas montados por múltiplas rodadas e múltiplos agentes:

- o sistema continua “passando”;
- mas cada alteração relevante exige leitura excessiva;
- contratos ficam defensivos demais;
- responsabilidades se sobrepõem;
- a chance de duplicar lógica aumenta a cada nova funcionalidade.

## 2. O backend é hoje o principal concentrador de dívida

O frontend ainda tem hotspots, mas a dívida mais arriscada migrou para o backend e para a integração entre camadas.

Os sinais mais claros são:

- services com papel de orquestração, consulta, validação, workflow e notificação ao mesmo tempo;
- facades que misturam domínio, montagem de DTO e formatação de saída;
- controllers “unificados” que concentram muitos casos de uso distintos;
- DTOs e respostas HTTP ainda acoplados a entidades/modelos internos.

## 3. A integração backend/frontend continua permissiva demais

Hoje existem pontos em que a integração ainda depende de:

- campos opcionais demais;
- fallbacks silenciosos;
- coerção com `!`, `as`, `??`, strings e enums default;
- normalização defensiva no frontend para tolerar contratos imperfeitos.

Isso é ruim porque desloca o erro para mais longe da origem.

Em vez de o contrato quebrar cedo, o sistema “se ajusta” e deixa o defeito aparecer depois como:

- UI incoerente;
- ação desabilitada sem explicação real;
- estado impossível mascarado;
- bug intermitente difícil de reproduzir.

---

## Frentes de trabalho

## Frente 1. Endurecer a borda HTTP

### Problema

A API ainda expõe modelos internos em respostas que deveriam ser DTOs puros.

Exemplos:

- [backend/src/main/java/sgc/subprocesso/dto/ContextoEdicaoResponse.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/ContextoEdicaoResponse.java)
- [backend/src/main/java/sgc/subprocesso/dto/ContextoCadastroAtividadesResponse.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/ContextoCadastroAtividadesResponse.java)
- [backend/src/main/java/sgc/mapa/dto/MapaVisualizacaoResponse.java](/Users/leonardo/sgc/backend/src/main/java/sgc/mapa/dto/MapaVisualizacaoResponse.java)

Essas respostas ainda carregam `Unidade`, `Competencia`, `Atividade` ou outros tipos de domínio.

### Objetivo

Garantir que a camada HTTP exponha apenas contratos explícitos de API, sem dependência do grafo JPA/dominio.

### O que fazer

- Criar DTOs específicos de borda para contextos de subprocesso, mapa e relatórios.
- Eliminar retornos HTTP com entidades JPA ou modelos de domínio serializados diretamente.
- Remover dependência implícita de `@JsonView` como mecanismo principal de “controle de contrato”.
- Padronizar mapeadores backend para separar:
  - montagem de contexto de domínio;
  - projeção para API;
  - serialização final.

### Sinais concretos de trabalho

- `ContextoEdicaoResponse.unidade` deve deixar de ser `Unidade`.
- `ContextoCadastroAtividadesResponse.unidade` deve deixar de ser `Unidade`.
- `MapaVisualizacaoResponse` deve deixar de expor `Competencia` e `Atividade` do domínio.
- DTOs que importam `*.model.*` precisam ser revisitados caso estejam sendo retornados pela API.

### Critério de saída

- Nenhum endpoint público da aplicação depende de entidade/modelo de domínio no JSON de resposta.
- O frontend consome tipos explícitos e estáveis, sem inferência indireta do modelo interno.

---

## Frente 2. Reduzir god objects do backend

### Problema

Há classes centrais que agregam responsabilidades demais.

Isso reduz coesão, dificulta navegação, aumenta acoplamento e torna refatorações locais mais arriscadas.

### Alvos prioritários

- [backend/src/main/java/sgc/processo/service/ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java)
- [backend/src/main/java/sgc/relatorio/RelatorioFacade.java](/Users/leonardo/sgc/backend/src/main/java/sgc/relatorio/RelatorioFacade.java)
- [backend/src/main/java/sgc/subprocesso/SubprocessoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoController.java)
- [backend/src/main/java/sgc/e2e/E2eController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/e2e/E2eController.java)

### Objetivo

Reorganizar por caso de uso real e por responsabilidade real, sem criar abstrações cosméticas.

### O que fazer

- Fatiar `ProcessoService` por subdomínios reais:
  - consulta e listagem;
  - manutenção;
  - workflow;
  - ações em bloco;
  - lembretes/notificações.
- Extrair de `RelatorioFacade` as partes de:
  - coleta de dados;
  - transformação para modelo de relatório;
  - renderização PDF.
- Quebrar `SubprocessoController` por áreas de caso de uso se a rota já estiver servindo domínios distintos:
  - consulta/contexto;
  - cadastro;
  - mapa/validação;
  - ações administrativas.
- Reduzir `E2eController` a uma superfície mínima de suporte de testes, com helpers de infraestrutura fora do controller.

### Regra importante

Não fragmentar por “linhas”. Só extrair quando a fronteira representar um conceito real.

Extrações ruins a evitar:

- helper genérico que só recebe 8 dependências;
- classe “util” sem papel semântico;
- fachada intermediária que só delega;
- splitter artificial para satisfazer lint.

### Critério de saída

- Os grandes hubs passam a ter papéis nítidos.
- Um desenvolvedor consegue localizar a regra principal sem percorrer arquivos de 500+ linhas para mudanças pequenas.

---

## Frente 3. Corrigir frouxidão de contrato no frontend

### Problema

O frontend ainda compensa inconsistência de contrato com tipos permissivos e defaults silenciosos.

Exemplos:

- [frontend/src/services/processo/types.ts](/Users/leonardo/sgc/frontend/src/services/processo/types.ts)
- [frontend/src/services/processo/mapeadores.ts](/Users/leonardo/sgc/frontend/src/services/processo/mapeadores.ts)
- [frontend/src/types/subprocesso-contexto.ts](/Users/leonardo/sgc/frontend/src/types/subprocesso-contexto.ts)

### Objetivo

Fazer o frontend falhar cedo em contrato inválido e parar de “inventar completude” local.

### O que fazer

- Remover `!` e defaults silenciosos quando o dado deveria ser obrigatório pelo contrato.
- Reduzir uso de `[key: string]: unknown` em tipos de payload/resposta.
- Trocar `Partial<>` em respostas centrais por contratos fechados quando a resposta deveria ser completa.
- Substituir coerções com fallback por validação explícita ou ajuste no backend.
- Manter fallbacks apenas quando houver degradação intencional e aceitável pelo requisito.

### Exemplos de sinais a tratar

- `nome: dto.nome!`
- `sigla: dto.sigla!`
- `codSubprocesso ?? 0`
- `situacaoSubprocesso` forçada para `NAO_INICIADO`

### Critério de saída

- O frontend deixa de mascarar payload inconsistente.
- Os contratos principais ficam legíveis sem comentário explicando “campos adicionais do spread”.

---

## Frente 4. Reorganizar tratamento de erro

### Problema

O sistema hoje mistura tratamento de erro de infraestrutura, sessão, cancelamento, monitoramento, redirecionamento e notificação em múltiplas camadas.

Pontos centrais:

- [frontend/src/axios-setup.ts](/Users/leonardo/sgc/frontend/src/axios-setup.ts)
- [frontend/src/utils/apiError/normalizer.ts](/Users/leonardo/sgc/frontend/src/utils/apiError/normalizer.ts)
- composables/telas que também normalizam, notificam e preservam estado local.

### Objetivo

Definir uma política simples e coerente de erro:

- o que é erro de rede;
- o que é cancelamento legítimo;
- o que é erro de autenticação;
- o que é erro de domínio;
- onde cada tipo é tratado;
- quando a UI notifica globalmente e quando trata localmente.

### O que fazer

- Manter cancelamento e transição de sessão como infraestrutura.
- Manter normalização em um único ponto de verdade.
- Separar erro global de erro de fluxo local.
- Eliminar `try/catch` que apenas transforma ou repassa ruído sem decisão real.
- Revisar composables grandes que duplicam lógica de tratamento de erro, loading e notificação.

### Alvos prioritários

- [frontend/src/axios-setup.ts](/Users/leonardo/sgc/frontend/src/axios-setup.ts)
- [frontend/src/composables/useCadastroTela.ts](/Users/leonardo/sgc/frontend/src/composables/useCadastroTela.ts)
- composables de mutação e orquestração relacionados a cadastro, mapa, processo e importação.

### Critério de saída

- Fica claro onde cada erro nasce, onde é normalizado e onde é exibido.
- O comportamento diante de cancelamento e erro de rede é previsível.

---

## Frente 5. Reduzir duplicação e sobreposição de fluxo

### Problema

Há duplicação não necessariamente textual, mas conceitual:

- mesma regra em backend e frontend;
- mesma decisão de permissão espalhada;
- mesma ideia de contexto montada por caminhos paralelos;
- mesma orquestração reaparecendo com nomes diferentes.

### Objetivo

Ter uma única camada dona de cada decisão importante.

### O que fazer

- Revisar fluxos em que o frontend recompõe regra que já deveria vir pronta do backend.
- Revisar campos derivados que o backend já poderia entregar resolvidos.
- Revisar construção de contexto completo em processo/subprocesso/mapa.
- Consolidar mapeadores e serviços duplicados quando o contrato for o mesmo.

### Perguntas-guia

- Essa regra pertence ao backend?
- O frontend está inferindo algo que poderia vir explícito?
- Existem duas formas de obter o mesmo contexto?
- Estamos mantendo compatibilidade antiga sem consumidor real?

### Critério de saída

- Cada fluxo central tem um dono claro para regra, estado e permissão.

---

## Frente 6. Limpar nomenclatura e coerência semântica

### Problema

Há sinais de nomenclatura que refletem evolução incompleta do desenho:

- “Facade” com papel de service de domínio;
- “Controller unificado” com responsabilidades heterogêneas;
- tipos `Response`, `Dto`, `Resumo`, `Contexto`, `View` usados com fronteiras pouco consistentes;
- nomes que escondem se algo é contrato HTTP, modelo interno, projeção de tela ou comando de domínio.

### Objetivo

Fazer o nome do artefato explicar o seu papel sem precisar abrir a implementação.

### O que fazer

- Revisar nomenclatura das classes centrais extraídas nas frentes 1 e 2.
- Padronizar distinção entre:
  - `Request`;
  - `Command`;
  - `Dto`;
  - `Response`;
  - `Mapper`;
  - `Service`;
  - `Facade`.
- Eliminar aliases e compatibilidades que só preservam nomes antigos.

### Critério de saída

- O nome do arquivo/classe indica claramente se ele é borda HTTP, domínio, consulta, mutação, renderização ou suporte de teste.

---

## Frente 7. Tornar os gates de qualidade mais honestos

### Problema

Hoje existe bastante validação, mas parte dela está desalinhada com o que o projeto quer proteger.

Exemplos:

- `pitest` com thresholds `0` em [backend/build.gradle.kts](/Users/leonardo/sgc/backend/build.gradle.kts:308)
- `coverage:unit:collect` do frontend com thresholds `0` em [frontend/package.json](/Users/leonardo/sgc/frontend/package.json:22)
- `quality:lint` do frontend combinando `oxlint` e `eslint` com limites efetivos diferentes de [frontend/eslint.config.js](/Users/leonardo/sgc/frontend/eslint.config.js:40)

### Objetivo

Fazer os gates refletirem a política de qualidade desejada, não apenas produzir sinal.

### O que fazer

- Definir o que é gate de regressão, o que é gate estrutural e o que é auditoria.
- Decidir quais thresholds devem ser reais e quais devem ser apenas coleta.
- Alinhar `oxlint` e `eslint` para evitar dupla interpretação do mesmo hotspot.
- Manter gates que ajudam a priorizar e remover os que apenas acrescentam ruído.

### Critério de saída

- Quando o gate falha, a falha significa algo útil e coerente com a política do projeto.

---

## Frente 8. Evoluir o toolkit `sgc.js` para backend e integração

### Problema

O toolkit atual já é rico e útil, mas a distribuição das capacidades ainda é desigual.

Hoje ele está mais maduro em:

- frontend estrutural;
- QA consolidado;
- cheiros e nomenclatura transversais;
- cobertura e higiene de testes.

Isso aparece diretamente em [etc/scripts/sgc.js](/Users/leonardo/sgc/etc/scripts/sgc.js):

- `frontend cruft`
- `frontend arquitetura`
- `frontend validacoes`
- `frontend a11y`
- `codigo smells`
- `codigo nomes`
- `qa snapshot`

Já no backend e na integração frontend/backend, o toolkit ainda está mais concentrado em:

- cobertura;
- priorização de testes;
- auditoria de `null`;
- higiene Java pontual.

Isso é pouco para o estágio atual do projeto, em que os problemas mais caros estão em:

- coesão do backend;
- borda HTTP;
- coerência de contrato;
- duplicação de regras entre backend e frontend;
- ruído de erro e cancelamento;
- drift entre payload real, tipos do frontend e regras de acesso.

### Objetivo

Transformar o toolkit em mecanismo ativo de governança da qualidade estrutural, especialmente no backend e na integração, e não apenas em apoio para frontend e cobertura.

### Diretriz

Antes de criar novos gates duros, criar auditorias de leitura e priorização.

Ou seja:

- primeiro gerar bom sinal;
- depois estabilizar critérios;
- só então transformar parte desse sinal em validação obrigatória.

### Expansões prioritárias do toolkit

## 8.1 Backend estrutural

Criar uma família `backend arquitetura` e `backend coesao` para diagnosticar desenho, não só cobertura.

### Comandos sugeridos

- `node etc/scripts/sgc.js backend arquitetura auditar`
- `node etc/scripts/sgc.js backend arquitetura validar`
- `node etc/scripts/sgc.js backend coesao auditar`
- `node etc/scripts/sgc.js backend dto auditar`
- `node etc/scripts/sgc.js backend controllers auditar`
- `node etc/scripts/sgc.js backend workflow auditar`

### O que essas auditorias devem medir

- classes service/facade/controller com linhas, métodos e dependências acima do saudável;
- mistura de responsabilidades no mesmo arquivo;
- DTOs que importam `model.*` e vazam domínio para a borda;
- controllers que concentram áreas demais do domínio;
- services que misturam consulta, mutação, validação, permissão e notificação;
- sobreposição entre services/facades;
- uso excessivo de `@Nullable`, `Optional` e checks defensivos em fluxos centrais;
- dependência cruzada entre módulos de domínio.

### Hotspots iniciais óbvios para calibrar

- `ProcessoService`
- `RelatorioFacade`
- `SubprocessoController`
- `E2eController`
- services de transição/notificação/cadastro de subprocesso

## 8.2 Integração frontend/backend

Criar uma família `integracao` ou `contratos` para tornar visível o drift entre backend e frontend.

### Comandos sugeridos

- `node etc/scripts/sgc.js integracao contratos auditar`
- `node etc/scripts/sgc.js integracao contratos validar`
- `node etc/scripts/sgc.js integracao erros auditar`
- `node etc/scripts/sgc.js integracao permissoes auditar`
- `node etc/scripts/sgc.js integracao contextos auditar`

### O que essas auditorias devem medir

- respostas HTTP cujo DTO ainda carrega entidade/modelo do domínio;
- campos opcionais demais no frontend para contratos que deveriam ser completos;
- mapeadores frontend que usam `!`, `as`, `??`, defaults silenciosos ou estados inventados;
- divergência entre nomes/formatos de campos backend e tipos frontend;
- duplicação de regra de permissão ou elegibilidade entre backend e frontend;
- múltiplas rotas/serviços produzindo “o mesmo contexto” com formatos diferentes;
- ruído em cancelamento, 401, erro de rede e erro de domínio.

### Casos iniciais para calibrar

- contexto de processo;
- contexto de subprocesso;
- contexto de cadastro/edição;
- mapa visualização/ajuste;
- relatórios JSON usados pela UI.

## 8.3 QA snapshot orientado a estrutura

O `qa snapshot` já é um bom ponto de consolidação e deve incorporar essas novas famílias.

### Extensões sugeridas

- perfil `backend-estrutura`
- perfil `integracao`
- perfil `qualidade-estrutural`

### O snapshot deve consolidar

- hotspots de coesão do backend;
- vazamentos de contrato backend/frontend;
- drift entre tipos frontend e payload backend;
- ruído de tratamento de erro;
- indicadores de duplicação de fluxo;
- histórico comparável entre rodadas.

## 8.4 Técnicas de implementação no toolkit

As novas auditorias devem seguir o padrão útil que o toolkit já usa no frontend:

- saída Markdown para leitura humana;
- saída JSON estável para automação;
- snapshot `latest`;
- score total + breakdown por regra;
- top hotspots com arquivo e motivo;
- budgets calibráveis;
- waivers explícitos quando fizer sentido;
- comparação com snapshot anterior;
- foco em sinal explicável, não em numerologia.

### Técnicas recomendadas

- heurísticas leves por AST e regex combinadas;
- inventário de símbolos e imports reaproveitando a infraestrutura de `codigo nomes`;
- leitura comparativa de DTO backend versus tipos/mapeadores frontend;
- classificação por severidade e não só por contagem;
- relatórios com “por que isso importa” e “primeiro corte sugerido”.

## 8.5 O que não fazer no toolkit

- gate duro cedo demais para heurística ainda instável;
- score único opaco sem breakdown;
- auditoria que só conta linhas sem contexto;
- sugerir abstração genérica como correção padrão;
- misturar sinal de regressão funcional com sinal estrutural em um único número.

### Critério de saída

- O toolkit passa a cobrir backend estrutural e integração com profundidade parecida à que já existe no frontend.
- O time consegue rodar auditorias úteis antes de abrir arquivos grandes ou discutir refatoração.
- O `qa snapshot` passa a mostrar não só saúde funcional, mas também saúde de desenho.

---

## Sequência recomendada

## Etapa 1. Borda HTTP e integração

Fazer primeiro:

- Frente 1
- Frente 3

Motivo:

- reduz acoplamento estrutural;
- limpa a relação backend/frontend;
- evita consolidar lógica em cima de contratos frouxos.

## Etapa 2. Coesão do backend

Fazer depois:

- Frente 2
- Frente 5
- Frente 6

Motivo:

- depois de estabilizar contrato, fica mais seguro quebrar hubs centrais;
- evita extrair serviços em cima de payload ruim ou ambíguo.

## Etapa 3. Ruído operacional e gates

Fazer em paralelo controlado:

- Frente 4
- Frente 7
- Frente 8

Motivo:

- essas frentes melhoram a legibilidade operacional do sistema;
- não devem liderar a rodada, para não maquiar dívida de desenho com infraestrutura;
- a evolução do toolkit deve acompanhar a estratégia, não substituir a estratégia.

---

## Backlog inicial sugerido

## Alta prioridade

- Substituir entidades/modelos expostos em respostas HTTP por DTOs explícitos.
- Fechar contratos de processo/subprocesso/mapa consumidos pelo frontend.
- Quebrar `ProcessoService` pelos casos de uso reais.
- Extrair de `RelatorioFacade` a camada de renderização PDF.
- Criar auditorias do toolkit para backend estrutural e contratos de integração.

## Média prioridade

- Reorganizar `SubprocessoController`.
- Reduzir sobreposição entre composables de cadastro/mapa/processo.
- Centralizar melhor a política de erro/cancelamento.
- Alinhar `oxlint` e `eslint`.
- Estender `qa snapshot` com perfis focados em backend estrutural e integração.

## Baixa prioridade

- Renomeações de artefatos que não estejam bloqueando entendimento.
- Refino fino de métricas de lint depois dos cortes estruturais principais.
- Transformar auditorias novas em gates duros antes de estabilizar as heurísticas.

---

## Como validar cada rodada

## Validação mínima

```bash
./gradlew :backend:test
npm run typecheck
npm --prefix frontend run test:unit
```

## Validação estrutural

```bash
npm --prefix frontend run quality:lint
node etc/scripts/sgc.js frontend arquitetura auditar
node etc/scripts/sgc.js frontend cruft auditar
node etc/scripts/sgc.js codigo smells auditar
```

## Validação dirigida por mudança

- Se mexer em contrato HTTP, validar backend + typecheck raiz + consumidores frontend.
- Se mexer em service central, validar testes focados do módulo e fluxo dependente.
- Se mexer em tratamento de erro, validar cenários de cancelamento, 401, rede e erro de domínio.

---

## O que evitar durante a execução

- criar wrappers genéricos para “loading/erro/toast” que escondem o fluxo;
- quebrar arquivos grandes apenas para satisfazer budget;
- mover regra de domínio para frontend para “simplificar” backend;
- inventar fallback para estados impossíveis;
- manter compatibilidades silenciosas por tempo indeterminado;
- confundir DTO de API com entidade serializada;
- usar cobertura alta como argumento para adiar refatoração estrutural.

---

## Resultado esperado

Ao final desta trilha, o SGC deve continuar com boa proteção de regressão, mas com uma base mais coerente para evoluir:

- contratos explícitos e estáveis entre backend e frontend;
- backend mais coeso e menos concentrado em hubs gigantes;
- menos duplicação conceitual;
- tratamento de erro previsível;
- nomenclatura mais honesta;
- gates de qualidade que sinalizam problemas reais.
