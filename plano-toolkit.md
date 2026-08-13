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
- Não manter implementação JavaScript paralela, wrappers de transição ou aliases legados sem consumidor real.
- Node 26.7 ou superior é o ambiente mínimo; não há necessidade de compatibilidade com clientes antigos.

### Evidência de utilidade

- Ausência de importação não prova ausência de consumidor: comandos manuais podem ter consumidores humanos legítimos.
- A decisão de manter uma funcionalidade deve considerar CLI, scripts, CI, documentação, testes, artefatos e uso manual.
- Código temporário deve ser removido quando o problema original desapareceu e não existe finalidade permanente.
- Testes que apenas reproduzem uma implementação sem consumidor não justificam sua preservação.
- O histórico de mudanças pertence ao Git, não a este plano nem ao README.

### Segurança e efeitos colaterais

- Auditorias e inventários devem ser somente leitura por padrão.
- Persistência exige `--gravar`; remoção exige `--confirmar`.
- Ações cujo próprio nome expressa geração ou promoção, como exportar OpenAPI ou fixar baseline, podem gravar diretamente.
- Saída JSON deve permanecer limpa em stdout; diagnóstico operacional e falhas devem ir para stderr.
- Comandos mutáveis precisam de teste de prévia, efeito e idempotência quando aplicável.
- O catálogo deve distinguir a finalidade do comando de seu efeito no sistema: inventariar não significa necessariamente
  gravar, e orquestrar não informa se subprocessos produzem artefatos.
- Metadados de efeito devem declarar separadamente leitura, persistência opcional, persistência intrínseca, mutação,
  remoção, rede e execução de subprocessos.
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
- APIs programáticas públicas devem ser deliberadas e cobertas por instalação isolada; módulos internos permanecem privados.
- A identidade pública do pacote e do binário deve ser neutra antes do primeiro consumidor externo real; o perfil SGC
  pode continuar sendo o default sem obrigar outros projetos a adotar os nomes `sgc-scripts` e `sgc`.

### Contratos de resultados

- Campos produzidos pelo toolkit devem usar português e uma única convenção de caixa; nomes externos de JaCoCo, V8,
  Semgrep e OpenAPI são preservados somente na borda de leitura.
- Formatos usados por outro comando, CI ou API pública precisam de tipo exportado, versão e validação ao carregar.
- Campos históricos em inglês ou `snake_case` próprios do toolkit devem ser migrados diretamente, sem aliases de
  compatibilidade.
- JSON humano ocasional não precisa receber um envelope universal; versionamento deve acompanhar consumo real.
- Códigos de saída precisam distinguir execução inválida, falha de infraestrutura e achados de auditoria quando essa
  diferença for relevante ao consumidor.

### Fronteiras já esclarecidas

- `qualidade coletar` produz uma fotografia consolidada; `qualidade tarefas executar` apenas orquestra tarefas configuradas.
- A auditoria de dependências combina Knip, versões desatualizadas, vulnerabilidades npm e atualizações Gradle.
- Acessibilidade Playwright/Axe pertence ao workspace `e2e/`, não ao toolkit.
- OpenAPI mantém exportação, comparação e baseline; geração de tipos não deve voltar sem consumidor e compatibilidade atual.
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

A implementação já é integralmente TypeScript e o pacote isolado funciona, mas as fronteiras ainda não estão concluídas:

- o catálogo ainda classifica todos os comandos CDU como `perfil-sgc` e fixa o caminho do corpus nas descrições;
- a configuração não representa corpus, vocabulário, estilo nem extratores CDU;
- os comandos `backend testes analisar` e `backend testes priorizar` escrevem relatórios por padrão;
- comandos encaminhados aceitam opções inexistentes sem erro;
- `efeito` no catálogo mistura intenção funcional e efeitos reais no sistema;
- muitos módulos interpretam argumentos manualmente e controlam `process.exitCode` dentro da implementação;
- resultados próprios ainda misturam português, inglês, `camelCase` e `snake_case`, especialmente na análise de testes;
- apenas cobertura Java e web está publicada como API programática horizontal;
- defaults de Gradle, Vue, OpenAPI, Semgrep e políticas SGC ainda aparecem dentro de módulos adaptáveis;
- os maiores módulos concentram análise, política, formatação, persistência e CLI, dificultando reuso seletivo.

## Próximos passos

### 1. Fechar contratos inseguros da CLI e dos efeitos

Prioridade imediata, porque corrige comportamento surpreendente antes de ampliar a superfície reutilizável.

- fazer `backend testes analisar` e `backend testes priorizar` emitirem resultado em stdout por padrão e só persistirem
  relatórios com `--gravar`;
- rejeitar opções desconhecidas e argumentos excedentes em todos os comandos, inclusive os encaminhados pelo roteador;
- substituir o campo genérico `efeito` por metadados separados de finalidade e efeitos observáveis;
- retirar `process.exitCode` das funções de domínio e concentrar a tradução de resultados em códigos de saída na borda;
- definir códigos compartilhados para invocação inválida, falha operacional e achados, sem aplicar código não zero a
  inventários meramente informativos;
- adicionar testes de contrato para erro de digitação, modo somente leitura, persistência explícita, stdout JSON e stderr.

Critério de saída: nenhum comando de auditoria grava sem solicitação, opções inválidas nunca são ignoradas e o catálogo
permite determinar efeitos sem ler a implementação.

### 2. Horizontalizar a família CDU

- representar o corpus por configuração conceitual, com glob ou arquivos, em vez de embutir `specs/cdu/cdu-*.md`;
- extrair parser e análise estrutural para contratos independentes do SGC;
- mover vocabulário, situações, tipos, estilo e placeholders do SGC para uma política explícita;
- separar comparação de mensagens dos extratores Java/TypeScript e permitir composição por stack;
- reclassificar cada comando CDU individualmente: análise estrutural no núcleo, integração no adaptador e convenções no
  perfil SGC;
- criar fixture de um segundo projeto com caminho e vocabulário próprios e preservar o SGC por regressão;
- publicar um subpath CDU somente quando o consumidor isolado demonstrar que o contrato programático é necessário.

Critério de saída: um segundo projeto executa as capacidades horizontais sem copiar arquivos, editar o toolkit ou receber
políticas do SGC acidentalmente.

### 3. Separar adaptadores e perfil SGC

- inventariar e tornar explícitos URL OpenAPI, tarefas Gradle, regras Semgrep, convenções Vue e caminhos ainda embutidos;
- mover defaults locais para módulos coesos de perfil, sem condicionais `projeto === "sgc"` espalhadas;
- fazer motores receberem políticas, coletores e executores por composição;
- manter a configuração orientada a conceitos do domínio, evitando espelhar cada detalhe interno como uma opção;
- preservar defaults do SGC apenas onde eles tornam a experiência local direta e não contaminam o motor horizontal.

Critério de saída: é possível apontar, por arquivo e contrato, o que é motor, integração de stack e política SGC.

### 4. Normalizar resultados e fronteiras programáticas

- começar pela análise e priorização de testes, pois uma consome o resultado da outra;
- converter campos próprios para português em `camelCase`, mantendo nomes externos apenas nos adaptadores de leitura;
- exportar tipo, versão e validação para formatos realmente persistidos ou consumidos;
- separar análise, formatação e persistência para que a API de domínio não dependa de arquivo ou console;
- evitar um envelope universal para saídas sem consumidor automático;
- ampliar exports públicos apenas após teste por tarball em um consumidor TypeScript isolado.

Critério de saída: formatos entre comandos falham cedo quando incompatíveis e APIs públicas não expõem detalhes de CLI,
filesystem ou workspace.

### 5. Unificar gramática, ajuda e identidade da CLI

- revisar comandos fora de `<dominio> <recurso> <acao>`, incluindo formas nominais e ações compostas inconsistentes;
- manter opções canônicas em português: `--base`, `--arquivo`, `--diretorio`, `--entrada`, `--saida`, `--gravar` e
  `--confirmar`;
- gerar ou verificar ajuda, parser e catálogo a partir de um contrato comum por comando;
- escolher nome neutro para pacote e binário antes do primeiro consumidor externo, mantendo o perfil SGC como conveniência;
- atualizar todos os consumidores do repositório diretamente, sem aliases ou período de compatibilidade.

Critério de saída: nomes, ajuda, validação e catálogo descrevem a mesma interface, e a identidade pública não sugere que o
toolkit horizontal pertence exclusivamente ao SGC.

### 6. Reduzir concentração de responsabilidades

Tratar primeiro `frontend/arquitetura-lib.ts`, `backend/testes-analisar.ts`,
`codigo/nomes-simbolos-coletar.ts` e `frontend/residuos-lib.ts`.

- separar somente fronteiras coesas, como coleta, regras, agregação, formatação e persistência;
- manter juntas regras que mudam pelo mesmo motivo, mesmo em arquivos extensos;
- exigir testes diretamente sobre cada contrato extraído;
- remover helpers, exports e fixtures que se tornarem sem consumidor durante a extração;
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
  permanente ou consumidor humano documentável.

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
