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

### Configuração e reuso

- `configuracao-toolkit.json` é o contrato versionado de configuração por projeto.
- `--base` representa a raiz auditada e deve ter precedência sobre defaults implícitos.
- Diretórios, globs, tarefas e políticas variáveis pertencem à configuração ou a adaptadores.
- Defaults do SGC são válidos, mas devem estar identificados como perfil SGC e não confundidos com regras universais.
- O local físico de instalação do pacote não pode determinar a raiz do projeto consumidor.
- APIs programáticas públicas devem ser deliberadas e cobertas por instalação isolada; módulos internos permanecem privados.

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

## Próximos passos

### 1. Horizontalizar o núcleo CDU

Prioridade imediata.

- adicionar à configuração o diretório ou glob do corpus CDU;
- extrair um contrato de análise estrutural independente do SGC;
- mover vocabulário, situações, tipos e convenções do SGC para uma política explícita;
- tornar estilo e placeholders configuráveis sem criar uma linguagem de regras excessiva;
- separar o comparador de mensagens dos extratores Java/TypeScript específicos;
- criar fixture de um segundo projeto usando o mesmo formato CDU com caminho e vocabulário próprios;
- preservar os resultados atuais do SGC por testes de regressão;
- avaliar um subpath público CDU somente depois do consumidor externo passar pelo pacote instalado.

### 2. Explicitar defaults e adaptadores de projeto

- identificar URL OpenAPI, tarefas Gradle, convenções Vue e caminhos de políticas ainda embutidos;
- mover defaults para módulos de perfil sem condicionais `projeto === "sgc"` espalhadas;
- fazer motores receberem dependências por composição;
- manter configuração pequena e orientada a conceitos, evitando dezenas de flags específicas.

### 3. Padronizar CLI e resultados

- revisar opções, mensagens de ajuda e códigos de saída por família;
- manter nomes canônicos em português: `--base`, `--arquivo`, `--diretorio`, `--entrada`, `--saida`, `--gravar` e
  `--confirmar`;
- documentar quando achados geram código não zero e quando JSON continua disponível em falha;
- versionar e validar apenas resultados consumidos por CI, outro comando ou API pública;
- reduzir callbacks e helpers redundantes somente quando a fronteira resultante ficar mais clara.

### 4. Consolidar reuso e distribuição

- ampliar APIs públicas apenas para famílias comprovadamente horizontais;
- manter o pacote no modelo fonte + `tsx`;
- comprovar cada API nova em consumidor TypeScript instalado por tarball;
- garantir que assets de política sejam resolvidos a partir da instalação e overrides a partir da base auditada;
- revisar metadados de escopo no catálogo para distinguir `núcleo`, `adaptavel` e `perfil-sgc` de forma verificável.

### 5. Revisar artefatos e limpeza

- confirmar que todo artefato gerado fica fora da fonte empacotada e é ignorado corretamente;
- uniformizar `mais-recente`, diretórios de execuções e caminhos relativos à base;
- manter `projeto artefatos limpar` em prévia por padrão;
- remover políticas, fixtures ou saídas que tenham perdido finalidade.

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
