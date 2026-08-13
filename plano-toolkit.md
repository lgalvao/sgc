# Plano de modernização do toolkit

## Objetivos

O trabalho abrange exclusivamente `toolkit/`. Backend, frontend, E2E e especificações do SGC são considerados apenas
como consumidores, fixtures e fontes de políticas do toolkit.

Os objetivos são:

1. manter uma CLI moderna, coerente e inteiramente em TypeScript;
2. remover código temporário, obsoleto, redundante ou sem finalidade atual comprovável;
3. separar capacidades horizontais de políticas e integrações específicas do SGC;
4. permitir o uso do toolkit em outros projetos Java/Spring Boot/Vue por configuração e composição;
5. preservar as funcionalidades específicas do SGC que continuam úteis;
6. manter auditorias seguras, previsíveis e somente leitura por padrão;
7. oferecer uma distribuição que funcione sem depender do layout ou do `node_modules` do SGC.

### Escopo funcional desejado

O toolkit deve ser organizado conceitualmente em três camadas:

| Camada | Responsabilidade | Exemplos |
|---|---|---|
| Núcleo | Funções independentes de regras de negócio | CLI, configuração, execução, saída, cobertura JaCoCo/V8, parser CDU |
| Adaptadores | Integração com stacks, layouts e ferramentas | Gradle, npm, Vue, OpenAPI, Semgrep, extratores Java/TypeScript |
| Perfil SGC | Defaults e políticas deliberadamente locais | vocabulário CDU, coesão, notificações, views, modais, caminhos e tarefas SGC |

Não é objetivo transformar toda regra local em abstração genérica. Uma capacidade só deve ser promovida ao núcleo
quando houver contrato claro e uso horizontal plausível ou comprovado.

## Aprendizados e diretrizes

### Código e linguagem

- Implementação, testes, símbolos, mensagens, comentários e documentação devem usar TypeScript e português brasileiro.
- O entrypoint fonte é `toolkit/sgc.ts`, executado diretamente com `tsx`.
- `dist/` é produto de verificação do build, não o caminho normal de execução.
- Não manter implementação JavaScript paralela, wrappers de transição ou aliases legados sem finalidade atual, uso por
  humanos/agentes ou contrato público deliberado.
- Node 26.7 ou superior é o ambiente mínimo; não há necessidade de compatibilidade com clientes antigos.

### Evidência de utilidade

- Ausência de importação não prova ausência de consumidor: comandos manuais podem ter consumidores humanos legítimos.
- A decisão de manter uma funcionalidade deve considerar CLI, scripts, CI, documentação, testes, artefatos e uso manual.
- Código temporário deve ser removido quando o problema original desapareceu e não existe finalidade permanente.
- Testes que apenas reproduzem uma implementação sem finalidade atual, uso por humanos/agentes ou contrato público não
  justificam sua preservação.
- O histórico de mudanças pertence ao Git, não a este plano nem ao README.

### Segurança e efeitos colaterais

- Auditorias e inventários devem ser somente leitura por padrão.
- Persistência exige `--gravar`; remoção exige `--confirmar`.
- Ações cujo próprio nome expressa geração ou promoção, como exportar OpenAPI ou fixar baseline, podem gravar diretamente.
- Saída JSON deve permanecer limpa em stdout; diagnóstico operacional e falhas devem ir para stderr.
- Comandos mutáveis precisam de teste de prévia, efeito e idempotência quando aplicável.
- O catálogo distingue a finalidade do comando de seus efeitos observáveis: inventariar não significa necessariamente
  gravar, e orquestrar não informa por si só se há rede ou subprocessos.
- Metadados declaram separadamente persistência direta, remoção, rede e execução de subprocessos; efeitos internos de
  ferramentas externas não são atribuídos ao toolkit sem evidência.
- Funções de domínio devem retornar resultados ou lançar erros; somente a borda CLI pode alterar `process.exitCode`.

### Disciplina da CLI

- Opções desconhecidas e argumentos excedentes devem falhar, inclusive nos comandos encaminhados pelo roteador.
- A CLI não deve aceitar silenciosamente erros de digitação sob `allowUnknownOption`; cada módulo precisa validar que
  todos os argumentos foram reconhecidos.
- A ajuda, o parser e o catálogo não podem manter três definições divergentes do mesmo contrato.
- A gramática dos comandos deve seguir uma ordem estável, preferencialmente `<dominio> <recurso> <acao>` com a ação em
  infinitivo; nomes fora do padrão devem ser corrigidos diretamente, pois não há clientes legados.
- Opções comuns devem compartilhar parser, validação, mensagens e semântica, sem obrigar todos os comandos a usar o mesmo
  framework internamente.

### Configuração e reuso

- `configuracao-toolkit.json` é o contrato versionado de configuração por projeto.
- `--base` representa a raiz auditada e deve ter precedência sobre defaults implícitos.
- Diretórios, globs, tarefas e políticas variáveis pertencem à configuração ou a adaptadores.
- Defaults do SGC são válidos, mas devem estar identificados como perfil SGC e não confundidos com regras universais.
- O local físico de instalação do pacote não pode determinar a raiz do projeto consumidor.
- APIs programáticas públicas devem ser deliberadas e cobertas por instalação isolada; essa validação comprova o contrato,
  não exige consumidor prévio no código; módulos internos permanecem privados.
- A identidade pública do pacote e do binário deve ser neutra antes de o toolkit ser oferecido para reutilização em outro
  projeto; o perfil SGC pode continuar sendo o default sem obrigar outros projetos a adotar os nomes `sgc-scripts` e `sgc`.

### Contratos de resultados

- Campos produzidos pelo toolkit devem usar português e uma única convenção de caixa; nomes externos de JaCoCo, V8,
  Semgrep e OpenAPI são preservados somente na borda de leitura.
- Formatos usados por outro comando, CI ou API pública precisam de tipo exportado, versão e validação ao carregar.
- Campos históricos em inglês ou `snake_case` próprios do toolkit devem ser migrados diretamente, sem aliases de
  compatibilidade.
- JSON humano ou de agente ocasional não precisa receber um envelope universal; versionamento deve acompanhar a finalidade
  e a forma de uso do resultado.
- Códigos de saída precisam distinguir execução inválida, falha de infraestrutura e achados de auditoria quando essa
  diferença for relevante ao consumidor.

### Fronteiras já esclarecidas

- `qualidade coletar` produz uma fotografia consolidada; `qualidade tarefas executar` apenas orquestra tarefas configuradas.
- A auditoria de dependências combina Knip, versões desatualizadas, vulnerabilidades npm e atualizações Gradle.
- Acessibilidade Playwright/Axe pertence ao workspace `e2e/`, não ao toolkit.
- OpenAPI mantém exportação, comparação e baseline; geração de tipos não deve voltar sem finalidade atual e compatibilidade
  com a cadeia moderna do toolkit.
- Cheiros internos e Semgrep são análises complementares, não duplicadas.
- Auditorias consolidadas de cobertura e listagens focadas de ramificações têm resultados diferentes e podem coexistir.
- O corretor FQN é um utilitário ocasional mutável, não um gate permanente.
- Views, modais, notificações, coesão e heurísticas de erro permanecem políticas do perfil SGC enquanto forem desejadas.

### Casos de uso CDU

O formato CDU será usado em vários projetos e deve ser tratado como capacidade horizontal:

- parser, auditoria estrutural, densidade e duplicações pertencem ao núcleo reutilizável;
- inventários são diagnósticos ocasionais, não gates automáticos;
- estilo e vocabulário recebem políticas do projeto;
- comparação de mensagens com código recebe adaptadores por stack;
- caminho do corpus, perfis, situações, tipos de processo, mensagens e extratores atuais do SGC pertencem ao perfil SGC.

O padrão de arquivos do corpus já pode ser substituído por `requisitos.cdus.padraoArquivos` em
`configuracao-toolkit.json`; o default continua sendo `specs/cdu/cdu-*.md`. As fontes da comparação com mensagens do código
também podem ser declaradas por caminho e tipo de adaptador; prefixos, grupos e convenções de mensagens ainda são política
do perfil.

O SGC deve continuar funcionando com `specs/cdu/cdu-*.md`, mas esse caminho não pode permanecer uma regra rígida do
motor horizontal.

### Qualidade e validação

- Testes devem caracterizar comportamento observável, não detalhes internos sem valor contratual.
- O smoke do pacote deve instalar o tarball em consumidor isolado para detectar dependências implícitas do workspace.
- Knip deve analisar entrypoints reais e impedir exports ou módulos órfãos.
- Cobertura é evidência auxiliar; não substitui análise de utilidade nem justifica thresholds arbitrários.
- Mudanças no toolkit devem ser validadas em série para evitar contenção e falsos timeouts.
- Cada recorte validado deve terminar em commit e push para `main`.
- Arquivos grandes só devem ser divididos por responsabilidades coesas e contratos testáveis; contagem de linhas isolada
  não justifica fragmentação.
- Performance deve ser trabalhada a partir de medição, especialmente em AST, varreduras de corpus e subprocessos.

### Diagnóstico estrutural atual

A implementação já é integralmente TypeScript, a superfície CDU foi reduzida a dois comandos agregadores e o pacote
isolado funciona, mas as fronteiras ainda não estão concluídas:

- `requisitos cdus inventariar` e `requisitos cdus auditar` agora são os únicos comandos CDU públicos; as regras menores
  continuam como módulos internos compostos por eles;
- os dois agregadores aceitam `--secoes` e produzem JSON versionado; o caminho do corpus já é configurável por
  `requisitos.cdus.padraoArquivos`, com default `specs/cdu/cdu-*.md`;
- a configuração já representa corpus, fontes de mensagens, vocabulário, situações, tipos e perfis tipográficos; estilo
  textual, placeholders e heurísticas de contexto ainda não são políticas configuráveis;
- a comparação com mensagens do código usa fontes configuráveis por caminho e tipo de adaptador; o default SGC mantém sete
  fontes e as heurísticas de prefixos e grupos ainda estão dentro do adaptador;
- `backend testes analisar` e `backend testes priorizar` são somente leitura por padrão, persistem apenas com `--gravar` e
  já aparecem no mesmo catálogo de opções da CLI principal;
- o roteador e os entrypoints diretos publicados validam opções, valores e posicionais pelo mesmo preflight, inclusive a
  forma `--opcao=valor`; os scripts internos não publicados continuam fora desse contrato deliberado;
- o catálogo já separa finalidade (`auditar`, `inventariar`, `gerar`, `transformar`, `orquestrar`) dos efeitos diretos de
  persistência, remoção, subprocessos e rede;
- os entrypoints diretos ainda atribuem `process.exitCode` ao converter erros ou achados em status do processo; isso é
  deliberado na borda CLI, enquanto as funções reutilizáveis retornam resultados ou lançam erros;
- a análise e a priorização de testes já usam contratos versionados, campos em português/camelCase e categorias em
  português; os demais formatos próprios ainda precisam de revisão semelhante;
- o inventário de símbolos e as auditorias de nomenclatura/idioma agora usam contratos versionados e uma validação única;
  arquivo ausente pode ser coletado sob demanda, mas arquivo existente incompatível falha explicitamente;
- apenas cobertura Java e web está publicada como API programática horizontal;
- defaults de Gradle, Vue, OpenAPI, Semgrep e políticas SGC ainda aparecem dentro de módulos adaptáveis;
- os maiores módulos concentram análise, política, formatação, persistência e CLI, dificultando reuso seletivo.

## Próximos passos

### 1. Fechar contratos inseguros da CLI e dos efeitos

Prioridade imediata, porque corrige comportamento surpreendente antes de ampliar a superfície reutilizável.

- preservar o modo somente leitura de `backend testes analisar` e `backend testes priorizar`, com resumo humano ou JSON no
  stdout e persistência explícita por `--gravar`;
- manter o esquema de argumentos no catálogo e rejeitar opções desconhecidas, valores ausentes e argumentos excedentes nos
  comandos encaminhados pelo roteador;
- avaliar apenas entrypoints internos não publicados que continuarem sendo chamados diretamente por agentes; comandos
  públicos de arquivo e `qualidade/coleta.ts` já compartilham o preflight da CLI;
- refinar metadados de efeitos quando novas integrações forem adicionadas, mantendo a distinção entre escrita direta e
  efeitos de subprocessos externos;
- manter `process.exitCode` somente na borda dos entrypoints; não criar uma taxonomia de códigos além do contrato atual até
  existir consumidor que precise distinguir invocação inválida, falha operacional e achados;
- adicionar testes de contrato para erro de digitação, modo somente leitura, persistência explícita, stdout JSON e stderr.

Critério de saída: nenhum comando de auditoria grava sem solicitação, opções inválidas nunca são ignoradas e o catálogo
permite determinar efeitos sem ler a implementação.

### 2. Horizontalizar a família CDU

- manter a superfície pública reduzida a `requisitos cdus inventariar` e `requisitos cdus auditar`;
- manter `inventariar` reunindo formatos, vocabulário, mensagens, densidade e duplicações em seções de um resultado único;
- manter `auditar` reunindo estrutura, estilo, vocabulário, mensagens mecânicas e comparação com mensagens do código;
- manter seleção de seções com uma opção explícita, sem transformar cada regra em subcomando público;
- conservar detalhes e capacidades atuais no JSON agregado, identificados por seção, para não perder informação do SGC;
- ampliar a configuração do corpus com políticas conceituais somente quando houver necessidade real de composição; o
  padrão de glob já está em `requisitos.cdus.padraoArquivos`;
- extrair parser e análise estrutural para contratos independentes do SGC;
- manter perfis, tipos de processo, arquivo de situações e perfis tipográficos em política explícita; avaliar placeholders e
  demais heurísticas somente quando houver contrato horizontal claro;
- separar a comparação de mensagens das heurísticas de prefixos e grupos e permitir composição por stack;
- reclassificar cada comando CDU individualmente: análise estrutural no núcleo, integração no adaptador e convenções no
  perfil SGC;
- criar fixture de um segundo projeto com caminho e vocabulário próprios e preservar o SGC por regressão;
- publicar um subpath CDU quando o modelo e as análises agregadas formarem uma fronteira programática estável, validada por
  fixture isolada e útil para composição por scripts, humanos ou agentes.

Critério de saída: um segundo projeto executa os dois comandos e seleciona as capacidades horizontais sem copiar arquivos,
editar o toolkit ou receber políticas do SGC acidentalmente.

### 3. Separar adaptadores e perfil SGC

- inventariar e tornar explícitos URL OpenAPI, tarefas Gradle, regras Semgrep, convenções Vue e caminhos ainda embutidos;
- mover defaults locais para módulos coesos de perfil, sem condicionais `projeto === "sgc"` espalhadas;
- fazer motores receberem políticas, coletores e executores por composição;
- manter a configuração orientada a conceitos do domínio, evitando espelhar cada detalhe interno como uma opção;
- preservar defaults do SGC apenas onde eles tornam a experiência local direta e não contaminam o motor horizontal.

Critério de saída: é possível apontar, por arquivo e contrato, o que é motor, integração de stack e política SGC.

### 4. Normalizar resultados e fronteiras programáticas

- manter a análise de testes com relatório JSON `versao: 1` e a priorização com contrato JSON próprio `versao: 1`; o
  priorizador deve rejeitar versões ausentes ou incompatíveis antes de interpretar categorias;
- converter campos próprios para português em `camelCase`, mantendo nomes externos apenas nos adaptadores de leitura;
- exportar tipo, versão e validação para formatos persistidos, consumidos por outro comando/CI ou deliberadamente expostos
  a scripts e agentes;
- revisar os demais relatórios persistidos e saídas JSON, priorizando os que atravessam comandos ou são usados por agentes;
- separar análise, formatação e persistência para que a API de domínio não dependa de arquivo ou console;
- evitar um envelope universal para saídas sem consumidor automático;
- ampliar exports públicos apenas após teste por tarball em um projeto TypeScript isolado, mesmo que esse projeto seja uma
  fixture criada para validar o contrato;

Critério de saída: formatos entre comandos falham cedo quando incompatíveis e APIs públicas não expõem detalhes de CLI,
filesystem ou workspace.

### 5. Unificar gramática, ajuda e identidade da CLI

- revisar comandos fora de `<dominio> <recurso> <acao>`, incluindo formas nominais e ações compostas inconsistentes;
- manter opções canônicas em português: `--base`, `--arquivo`, `--diretorio`, `--entrada`, `--saida`, `--gravar` e
  `--confirmar`;
- gerar ou verificar ajuda, parser e catálogo a partir de um contrato comum por comando;
- escolher nome neutro para pacote e binário antes de publicar o reuso horizontal, mantendo o perfil SGC como conveniência;
- atualizar todos os consumidores do repositório diretamente, sem aliases ou período de compatibilidade.

Critério de saída: nomes, ajuda, validação e catálogo descrevem a mesma interface, e a identidade pública não sugere que o
toolkit horizontal pertence exclusivamente ao SGC.

### 6. Reduzir concentração de responsabilidades

Tratar primeiro `frontend/arquitetura-lib.ts`, `backend/testes-analisar.ts`,
`codigo/nomes-simbolos-coletar.ts` e `frontend/residuos-lib.ts`.

- separar somente fronteiras coesas, como coleta, regras, agregação, formatação e persistência;
- manter juntas regras que mudam pelo mesmo motivo, mesmo em arquivos extensos;
- exigir testes diretamente sobre cada contrato extraído;
- remover helpers, exports e fixtures que se tornarem sem uso, finalidade ou contrato durante a extração;
- medir AST, varreduras e subprocessos antes de introduzir cache, paralelismo ou atalhos.

Critério de saída: motores podem ser usados sem carregar CLI ou persistência, e a divisão reduz acoplamento observável em
vez de apenas diminuir arquivos.

### 7. Consolidar distribuição, artefatos e limpeza final

- manter o modelo fonte + `tsx` e usar `dist/` apenas para validar compilação;
- comprovar binário, assets e APIs públicas pela instalação do tarball fora do workspace;
- resolver assets empacotados a partir da instalação e overrides a partir da base auditada;
- uniformizar `mais-recente`, diretórios de execuções e caminhos relativos à base;
- manter `projeto artefatos limpar` em prévia e exigir `--confirmar` para remoção;
- repetir a auditoria de utilidade ao fim: remover políticas, fixtures, comandos e saídas que continuarem sem finalidade
  permanente ou uso documentável por humanos/agentes.

Critério de saída: o pacote instalado é autônomo, não suja a fonte e não conserva código apenas por ter testes.

### Validação de cada recorte

Executar em série:

```bash
npm --prefix toolkit run test -- --maxWorkers=1
npm --prefix toolkit run test:coverage -- --maxWorkers=1
npm --prefix toolkit run test:pacote
npm --prefix toolkit run typecheck
npm --prefix toolkit run typecheck:testes
npm --prefix toolkit run lint
npm --prefix toolkit run deps:audit
npm --prefix toolkit run build
git diff --check
```

Acrescentar testes focados conforme o risco: fixture externa para reuso, smoke da CLI para roteamento, teste de efeito
para mutação e execução frontend/E2E quando o contrato correspondente for alterado.

### Definição de conclusão

A modernização estará concluída quando:

- núcleo, adaptadores e perfil SGC estiverem separados por contratos claros;
- outro projeto puder configurar e executar as capacidades horizontais sem editar o toolkit;
- o SGC preservar suas políticas e resultados intencionais;
- CLI, configuração, APIs públicas, códigos de saída e efeitos colaterais estiverem documentados e testados;
- não houver código obsoleto, compatibilidade artificial ou dependência implícita do workspace;
- o pacote instalado isoladamente executar o binário e todas as APIs públicas suportadas.
