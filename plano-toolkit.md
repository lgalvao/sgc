# Plano de refatoração e modernização do toolkit

## 1. Objetivo

Transformar `toolkit/` em uma ferramenta de auditoria e automação:

- executável diretamente a partir do código-fonte com `tsx`;
- escrita integralmente em TypeScript, sem manter duas implementações da mesma regra;
- modular o suficiente para ser reutilizada em outros projetos Java/Spring Boot/Vue;
- configurável por projeto, sem copiar regras do SGC para cada novo repositório;
- previsível para uso humano, CI, scripts npm e automações externas;
- conservadora sobre o código auditado: auditores devem diagnosticar por padrão e só gravar quando a ação for explícita.

O objetivo não é transformar cada regra específica do SGC em uma abstração artificial. A separação correta é entre um
núcleo horizontal, perfis/adaptadores de projeto e regras que pertencem exclusivamente ao SGC.

## 2. Diretrizes permanentes

### 2.1 Linguagem e nomenclatura

- Código, comentários, mensagens e documentação em português brasileiro.
- Identificadores novos devem usar `codigo` em vez de `id` para chaves e referências.
- Diretórios e arquivos novos devem seguir a nomenclatura portuguesa já adotada pelo toolkit.
- Nomes externos inevitáveis — `OpenAPI`, `Vue`, `Gradle`, `tsx`, `JSON`, `Java` — permanecem como nomes técnicos.
- Não fazer uma renomeação massiva de uma vez. Quando um identificador público mudar, atualizar consumidores, testes,
  documentação e eventuais aliases na mesma rodada.

### 2.2 Fonte única e runtime

- TypeScript deve ser a fonte única da implementação.
- O alvo final do fluxo normal é `npx tsx toolkit/sgc.ts` — ou o script npm equivalente — diretamente na árvore-fonte;
  durante a transição, a entrada ainda é `toolkit/sgc.js`.
- `toolkit/dist/` é somente artefato opcional para verificar compilação, distribuição ou smoke test de pacote; não é o
  fluxo de desenvolvimento.
- Não criar novos arquivos `.js` como cópias, wrappers permanentes ou shims para uma implementação `.ts`.
- O fallback transitório de `garantirArquivo` que tenta `.ts` quando o despachador pede `.js` deve desaparecer quando o
  roteador e todos os comandos tiverem sido convertidos para caminhos TypeScript consistentes.
- Node padrão do projeto: `26.7.0`, registrado em `.nvmrc` e nos `engines`. TypeScript 6 permanece a linha adotada;
  TS7 fica explicitamente fora do escopo até que o ecossistema usado pelo projeto seja compatível.
- ESM continua sendo o formato do toolkit.

### 2.3 Fronteiras de módulo

Todo comando reutilizável deve:

1. exportar uma função pública, normalmente `principal(argumentos)` ou uma função de domínio nomeada;
2. não iniciar auditorias, rede, Gradle, npm, Playwright ou escrita de arquivos durante o `import`;
3. detectar execução direta somente na borda `ehEntradaPrincipal`;
4. receber base, entrada, saída e opções por argumento ou configuração, não por caminho global escondido;
5. retornar um resultado estruturado quando houver consumidor programático;
6. reservar `process.exitCode` para a borda CLI;
7. manter mensagens humanas e saída JSON estável e mutuamente compreensíveis.

O roteador deve apenas registrar comandos, transportar argumentos e delegar. Regra de auditoria e acesso ao sistema de
arquivos pertencem ao comando ou a uma biblioteca de domínio, não ao roteador.

### 2.4 Auditorias e efeitos colaterais

- Auditorias são read-only por padrão.
- Gravação de fotografia, baseline, relatório ou correção exige uma opção explícita e um nome de ação claro.
- Não normalizar, renumerar ou reescrever documentos apenas durante a leitura.
- Saída JSON deve ir para stdout sem texto decorativo; logs operacionais e diagnóstico de falha devem ir para stderr.
- Falhas devem ter código de saída diferente de zero, mas não podem destruir o relatório estruturado que o CI precisa ler.
- Toda exceção de compatibilidade deve ser temporária, documentada e coberta por teste; não mascarar contrato incorreto
  com fallback silencioso.

### 2.5 Configuração

`configuracao-toolkit.json` continua sendo a configuração externa simples e editável. A configuração deve evoluir para
um contrato versionado, com validação e defaults explícitos. Os caminhos atuais são:

- `backend`;
- `frontend`;
- `backendCodigo`;
- `frontendCodigo`;
- `testesIntegracao`;
- `artefatosQualidade`;
- `coberturaBackend`;
- `coberturaFrontend`;
- `orcamentoResiduosFrontend`;
- `excecoesResiduosFrontend`;
- `regrasSemgrep`;
- `contratosOpenapi`.

Novos comandos devem preferir esses pontos de configuração ou opções explícitas. Caminhos fixos só são aceitáveis dentro
de um adaptador de perfil, nunca no núcleo horizontal.

### 2.6 Compatibilidade e dependências

- Uma mudança de pacote não é suficiente: executar testes unitários, typecheck, lint, Knip e os smoke tests do toolkit.
- `tsx` é runtime, não apenas ferramenta de desenvolvimento, enquanto a execução de fonte for o caminho oficial. A
  posição dele em `dependencies`/`devDependencies` deve ser corrigida quando o pacote do toolkit for tornado instalável
  fora do workspace.
- O binário declarado em `toolkit/package.json` e o `shebang` do entrypoint precisam executar o mesmo caminho suportado
  pelos scripts npm. Hoje o binário ainda aponta para `sgc.js` com `node`, enquanto o caminho documentado usa `tsx`.
- Não atualizar dependências major sem uma matriz mínima de validação. A linha de TypeScript fica em TS6 por decisão
  explícita do projeto.

## 3. Situação atual — 12 de agosto de 2026

### 3.1 O que já foi feito

- Node atualizado para `26.7.0` com `.nvmrc` e `engines` no projeto raiz e no toolkit.
- Execução de fonte padronizada em `tsx`; scripts npm, documentação, `release-it` e ADR relevante já não usam `node`
  puro para comandos fonte.
- Build TypeScript criado e mantido como verificação opcional em `toolkit/dist/`.
- `knip` passou a enxergar arquivos TypeScript do toolkit e os exports dos módulos migrados.
- O núcleo compartilhado foi convertido para TypeScript:
  - `lib/caminhos.ts`;
  - `lib/cli-ajuda.ts`;
  - `lib/cli-opcoes.ts`;
  - `lib/configuracao.ts`;
  - `lib/execucao.ts`;
  - `lib/logger.ts`;
  - `lib/qualidade.ts`;
  - `lib/saida.ts`.
- Dois comandos de projeto já foram convertidos:
  - `projeto/arvore-linhas.ts`;
  - `projeto/versao-sincronizar.ts`.
- `garantirArquivo` já encontra `.ts` na fonte e `.js` no build, permitindo migração incremental sem duplicar módulos.
- Exports de `toolkit/package.json` já distinguem módulos migrados e módulos ainda JavaScript.
- A configuração já aceita caminhos de backend/frontend diferentes do layout do SGC; auditores de cobertura, arquitetura,
  coesão, contratos e resíduos já possuem partes parametrizadas por `--base`, `--arquivo`, `--saida` ou configuração.
- O gerador de tipos OpenAPI foi removido. O toolkit mantém somente exportação, comparação e fixação de fotografias de
  contrato; o Springdoc permanece no backend porque o ciclo E2E usa Swagger/OpenAPI para aguardar a aplicação.
- O histórico recente relevante está publicado na `main`, culminando em `3a6cbea24 Atualiza consumidores para
  execucao com tsx`.

### 3.2 Evidência de validação atual

No estado publicado, sob Node `26.7.0`:

- `npm --prefix toolkit run test`: 75 testes aprovados em 2 arquivos;
- `npm --prefix toolkit run build`: aprovado;
- `npm --prefix toolkit run typecheck`: aprovado;
- `npm --prefix toolkit run lint`: aprovado;
- `npm --prefix toolkit run deps:audit`: aprovado;
- `git diff --check`: aprovado;
- importação dos comandos sem execução acidental: coberta pelos testes;
- execução fonte com `tsx` e smoke do artefato compilado: aprovados nas rodadas de migração.

O número caiu de 76 para 75 porque o teste do wrapper experimental `sgc-ts.js` foi removido junto com o wrapper. Isso é
uma remoção de contrato obsoleto, não uma perda de cenário funcional.

### 3.3 Tamanho e composição atual

Inventário dos arquivos rastreados do toolkit, excluindo `dist`, cobertura e artefatos ignorados:

- 10 arquivos TypeScript de implementação;
- 62 arquivos JavaScript de implementação ainda pendentes;
- 2 arquivos JavaScript de teste (`test/sgc.test.js` e `test/cdus.test.js`);
- 2 arquivos de teste concentrando 75 cenários;
- maior módulo atual: `frontend/arquitetura-lib.js`, com aproximadamente 1.000 linhas;
- outros hotspots: `codigo/nomes-simbolos-coletar.js`, `backend/testes-analisar.js`,
  `frontend/residuos-lib.js`, `backend/contratos-auditar.js` e `qualidade/coleta-execucao.js`.

O núcleo TypeScript está adiantado, mas a migração do toolkit como um todo ainda está no início: aproximadamente 14%
dos arquivos de implementação rastreados são TypeScript.

## 4. Classificação para reuso externo

### 4.1 Horizontal com pouca adaptação

Estes componentes podem formar o pacote reutilizável:

- resolução de raiz, configuração, parsing de opções e saída;
- execução de comandos, logging e detecção de entrada principal;
- fotografia/resumo de qualidade quando o schema for formalizado;
- contagem de linhas e operações sobre um diretório Git;
- leitura de JaCoCo e de cobertura V8, desde que caminhos e schema de entrada sejam parâmetros;
- comparação de fotografias JSON e geração de relatórios determinísticos;
- exportação/diff/baseline de OpenAPI, desde que URL e diretórios de artefatos sejam configuráveis;
- primitivas para detectar resíduos, dependências, convenções e violações estruturais.

### 4.2 Horizontal com adaptador de projeto

Estas capacidades são úteis em projetos Spring Boot/Vue, mas hoje carregam decisões do SGC:

- auditoria de arquitetura Vue: nomes de stores/composables/services, views e estratégias de cache;
- auditoria de resíduos e modais BootstrapVueNext;
- auditorias de cobertura e priorização de testes Java/Gradle;
- coleta de qualidade que coordena Gradle, npm, Playwright e auditores;
- Semgrep, regras de nomenclatura e contratos OpenAPI;
- diagnóstico de ambiente e preparação do projeto.

Elas devem receber um perfil/adaptador com:

- diretórios e globs;
- tarefas de build/teste;
- convenções de camadas;
- nomes de componentes permitidos;
- schema de fotografias;
- comandos opcionais disponíveis;
- políticas de exceção.

### 4.3 Específico do SGC

Não promover diretamente ao núcleo horizontal:

- `backend/src/main/java/sgc` e o pacote Java `sgc`;
- `AssuntosNotificacao`, `Mensagens.java` e a lista de constantes de mensagens do SGC;
- regras de nomenclatura `codigo` e auditoria de inglês próprias do SGC;
- estrutura `specs/cdu-*.md`, vocabulário e convenções de CDU;
- arquivos concretos de constantes e stores do frontend do SGC;
- `ModalPadrao`, `LayoutPadrao`, `PageHeader` e exceções específicas de views do SGC;
- tarefas Gradle e perfis E2E fixos do SGC;
- portas padrão, `swagger-ui.html`, `/api-docs` e diretórios de artefatos sem configuração;
- arquivos de política e exceção mantidos em `toolkit/qualidade/`.

Essas regras continuam valiosas no perfil SGC. O trabalho correto é mover a regra para uma política/adaptador explícito,
não apagá-la nem espalhá-la em condicionais genéricas.

## 5. Lacunas e riscos conhecidos

### Prioridade alta

1. **Entry point e pacote**: `sgc.js` ainda é JavaScript, tem `shebang` para Node puro e é o alvo do `bin`; o caminho
   documentado usa `tsx`. Migrar o entrypoint e corrigir `bin`, `exports`, scripts, referências e smoke tests de instalação.
2. **TypeScript sem rigor uniforme**: `tsconfig.nucleo.json` cobre o núcleo, mas o `tsconfig.json` geral mantém
   `checkJs: false` e não impõe `strict` aos próximos comandos TS. Criar uma configuração estrita por etapas, sem tentar
   tipar os 62 módulos JavaScript de uma vez.
3. **Dependência de runtime**: `tsx` ainda está em `devDependencies` do toolkit, embora seja necessário para executar a
   fonte. Definir o modelo de instalação externo e mover a dependência ou fornecer um launcher de pacote coerente.
4. **Contrato `.js`/`.ts` temporário**: o fallback do despachador é útil durante a transição, mas aumenta a superfície e
   pode esconder caminhos inválidos. Medir os consumidores e removê-lo ao fim da conversão.
5. **Hard-coding de perfil**: várias regras continuam presas ao layout e ao vocabulário SGC. Antes de declarar o toolkit
   reutilizável, separar engine, política e adaptador.

### Prioridade média

6. **Testes concentrados**: dois arquivos de teste grandes em JavaScript dificultam localizar contratos e impedem que os
   tipos dos testes ajudem na migração. Dividir por domínio e converter para TypeScript depois de estabilizar os comandos.
7. **Schema de resultados**: fotografias, auditorias, cobertura, diagnósticos e relatórios usam objetos sem schema
   versionado. Formalizar tipos e versões de saída antes de extrair o pacote externo.
8. **Opções inconsistentes**: há mistura de `--input`, `--output`, `--dir`, `--arquivo`, `--saida` e defaults locais.
   Definir opções canônicas em português, aliases de transição e mensagens uniformes sem quebrar automações existentes.
9. **Documentação derivada**: ainda há referências de `sgc.js`, execução direta e exemplos que precisam ser regenerados ou
   centralizados. O inventário de comandos não deve divergir do roteador.
10. **Orquestração pesada**: `qualidade/coleta-execucao.js` mistura subprocessos, Gradle, npm, Playwright, parsing de
    relatórios e schema da fotografia. Separar executor, adaptadores de ferramenta e agregador.

### Prioridade baixa

11. **Distribuição**: decidir se o toolkit será copiado como diretório, instalado como pacote privado ou publicado como
    pacote interno. Só então fechar `files`, exports compilados, licença, versão e documentação de instalação.
12. **Artefatos e limpeza**: revisar políticas, arquivos ignorados e nomes de `mais-recente`/`execucoes` para evitar que
    saídas locais sejam confundidas com recursos do pacote.
13. **Performance**: medir antes de otimizar. A coleta e os auditores só devem ser otimizados por gargalo observado, com
    comparação antes/depois e sem sacrificar a legibilidade do relatório.

## 6. Próximos passos ordenados

Cada item abaixo deve ser uma rodada pequena, validada e publicada antes do próximo. A ordem privilegia redução de risco,
reuso externo e preservação de contratos.

### Fase A — fechar a fronteira TypeScript do runtime

1. Migrar `toolkit/sgc.js` para `toolkit/sgc.ts`.
2. Tipar o registro de comandos do Commander, opções e resultado de `principal`.
3. Atualizar scripts raiz, script do toolkit, README, ADRs, `release-it`, testes e referências internas para o novo
   entrypoint.
4. Corrigir o `bin` do pacote e testar a execução por `npm exec`, `npx tsx` e `npm --prefix toolkit run sgc`.
5. Decidir e implementar o modelo de instalação externo: `tsx` como dependência de runtime ou distribuição compilada.
6. Remover o fallback `.js` → `.ts` do despachador somente depois de todos os comandos registrados terem extensões e
   imports consistentes.

Critério de aceite: nenhum comando fonte depender de `node` puro, nenhum binário apontar para um caminho quebrado e o
roteador fonte/compilado possuir testes de smoke equivalentes.

### Fase B — converter bibliotecas puras e contratos de dados

1. Migrar `lib/dominios/cobertura-java.js` e `lib/dominios/cobertura-web.js`.
2. Migrar `backend/lib/testes-analisar-regras.js` e bibliotecas puras de frontend/requisitos.
3. Introduzir tipos para JaCoCo, V8, fotografia de qualidade, achados de auditoria e diagnóstico.
4. Substituir `any` implícito por `unknown` na entrada JSON e validar apenas o que o consumidor realmente exige.
5. Criar `tsconfig.toolkit-estrito.json` ou equivalente com `strict`, aplicando-o aos módulos já convertidos e aos
   próximos lotes.

Critério de aceite: as bibliotecas convertidas não fazem I/O durante import, têm tipos públicos documentados e mantêm os
mesmos fixtures e resultados JSON.

### Fase C — converter comandos por reuso, não por diretório

Lotes sugeridos:

1. **Projeto**: diagnóstico, limpeza, preparação e perfil de qualidade; separar o que é genérico do que coordena o SGC.
2. **Backend**: cobertura, análise/priorização de testes, contratos e FQN; parametrizar raiz Java, tarefas Gradle e
   categorias.
3. **Frontend**: cobertura V8, resíduos, acessibilidade e identificadores de teste; parametrizar raiz Vue, globs e
   convenções de componentes.
4. **Integração**: exportação, diff e baseline OpenAPI; manter o módulo independente do gerador de tipos removido.
5. **Requisitos**: converter o motor de Markdown e depois isolar o perfil CDU do SGC.
6. **Código transversal**: converter cheiros, Semgrep e inventários de nomes/idioma por último, pois concentram mais
   políticas locais e maior volume de parsing.

Para cada comando convertido:

- definir tipos das opções e do resultado;
- preservar `principal(argumentos)` e importação sem efeito colateral;
- trocar mensagens e exemplos para `npx tsx`;
- atualizar o registro do roteador e o teste de importação;
- mover caminhos específicos para configuração/política, quando a regra tiver potencial horizontal;
- remover o `.js` somente quando todos os consumidores forem atualizados.

### Fase D — separar núcleo horizontal e perfil SGC

1. Definir uma configuração de projeto versionada, mantendo JSON como formato oficial de entrada.
2. Criar uma camada de adaptadores para:
   - layout de backend Java/Spring/Gradle;
   - layout de frontend Vue;
   - contratos OpenAPI;
   - coleta de qualidade;
   - políticas de nomenclatura, CDU, modais e arquitetura.
3. Fazer o núcleo receber adaptadores por composição, sem `if (projeto === "sgc")` espalhado.
4. Criar um projeto fixture externo mínimo com backend/frontend fictícios e executar os comandos horizontais contra ele.
5. Documentar claramente quais comandos são `núcleo`, `perfil-sgc` ou `opcionais`.

Critério de aceite: um segundo projeto consegue configurar raiz, globs, tarefas e políticas sem editar o código do
núcleo; as regras CDU e `AssuntosNotificacao` não aparecem nesse projeto fictício.

### Fase E — padronizar CLI e resultados

1. Inventariar todas as opções, defaults, mensagens e códigos de saída.
2. Definir opções canônicas em português (`--entrada`, `--saida`, `--diretorio`, `--arquivo`, `--base`) e aliases de
   transição para as formas antigas quando houver consumidores reais.
3. Definir um envelope comum de resultado: versão do schema, status, resumo, violações, métricas, artefatos e avisos.
4. Separar stdout estruturado, stdout humano e stderr operacional.
5. Definir quando um comando retorna falha por violação encontrada versus erro de execução.
6. Adicionar `--json`/`--sem-gravar` de forma consistente, sem inventar opções para comandos que não precisam delas.

### Fase F — testes, documentação e distribuição

1. Converter `test/sgc.test.js` e `test/cdus.test.js` para TypeScript após a estabilização das interfaces.
2. Dividir testes por domínio: runtime, configuração, saída, projeto, backend, frontend, integração e requisitos.
3. Manter testes comportamentais sobre a API pública; não testar métodos privados por reflexão ou acoplamento à
   implementação.
4. Adicionar smoke test de instalação em diretório externo, incluindo `npx tsx` e o binário do pacote.
5. Adicionar matriz de validação para Node `26.7+`, TypeScript 6 e as versões de Vitest/tsx usadas no workspace.
6. Executar periodicamente as validações de integração do repositório:
   - `npm --prefix frontend run typecheck`;
   - `npm --prefix frontend run lint`;
   - `npm --prefix frontend run test:unit`;
   - testes E2E conforme `e2e/regras-e2e.md`, somente quando a mudança atravessar esse contrato.
7. Atualizar `toolkit/README.md`, README raiz, ADRs e exemplos a partir de uma fonte única de comandos.
8. Fechar o modelo de distribuição e retirar arquivos JS, aliases e fallbacks de transição.

## 7. Validação obrigatória por rodada

### Rodada focada

```bash
source "$HOME/.nvm/nvm.sh"
nvm use 26.7.0
npm --prefix toolkit run typecheck:nucleo
npm --prefix toolkit exec vitest run test/sgc.test.js test/cdus.test.js --reporter=dot --no-color
npm --prefix toolkit run build
git diff --check
```

### Rodada completa do toolkit

```bash
npm --prefix toolkit run test
npm --prefix toolkit run typecheck
npm --prefix toolkit run lint
npm --prefix toolkit run deps:audit
npm --prefix toolkit run build
```

### Verificações de integração de execução

```bash
npx tsx toolkit/sgc.js --help
npx tsx toolkit/sgc.js projeto arvore-linhas --help
npm --prefix toolkit run sgc -- --help
npm exec --workspace toolkit sgc -- --help
```

Depois de qualquer renomeação, procurar referências antigas fora de `dist` e `node_modules`:

```bash
rg -n 'node toolkit|sgc-ts|node --import=tsx|arquivo\.js removido' \
  --glob '!node_modules/**' --glob '!toolkit/dist/**'
```

Cada recorte deve terminar com um commit pequeno e push para `main`, conforme o fluxo adotado nesta modernização.

## 8. Decisões que não devem ser reabertas sem evidência

- Não reintroduzir o gerador de tipos OpenAPI enquanto ele exigir uma versão antiga de TypeScript e não houver consumidor
  real dos tipos gerados.
- Não tornar `dist` o caminho principal apenas porque o build existe.
- Não manter uma implementação JavaScript paralela para facilitar uma migração parcial.
- Não generalizar regras do SGC com dezenas de flags antes de existir um segundo consumidor real.
- Não transformar auditores read-only em formatadores ou corretores automáticos silenciosos.
- Não trocar TS6 por TS7 antes da compatibilidade do conjunto Node/Vitest/Vue/tsx/ESLint estar comprovada.
- Não otimizar coleta e auditorias sem medição de um cenário monitorado e comparação antes/depois.

## 9. Definição de concluído

A modernização do toolkit estará concluída quando:

- todo o código de implementação e testes estiver em TypeScript;
- o entrypoint, o binário, o `npx tsx`, os scripts npm e o build tiverem o mesmo contrato;
- não houver fallback de extensão ou wrapper de transição sem justificativa;
- o núcleo horizontal estiver separado do perfil SGC;
- um segundo projeto fixture executar os comandos horizontais apenas por configuração;
- schemas de saída e códigos de retorno estiverem documentados e testados;
- os auditores mantiverem comportamento read-only por padrão;
- as suítes do toolkit e as validações de frontend/E2E aplicáveis passarem sob Node 26.7+;
- o pacote puder ser instalado ou copiado para outro projeto sem depender de caminhos ou `node_modules` implícitos do SGC.
