# Plano do toolkit independente

## Objetivo

Extrair o toolkit atualmente mantido no SGC para um produto independente, modular e versionado, capaz de estabelecer e
auditar um baseline canônico para projetos Spring, Vue, Thymeleaf e requisitos em casos de uso.

O toolkit deve ser instalado nos projetos. Seu código não deve ser copiado para cada repositório. Cada projeto mantém
somente sua configuração, políticas, orçamentos, exceções e demais artefatos locais de integração.

O objetivo não é adaptar automaticamente qualquer organização histórica. O toolkit deve:

1. propor uma arquitetura canônica mínima;
2. detectar a aderência do projeto a essa arquitetura;
3. recomendar ajustes no projeto quando forem mais simples que parametrizar diferenças acidentais;
4. configurar variações legítimas;
5. evoluir seus módulos quando novos projetos revelarem necessidades horizontais reais;
6. manter políticas de domínio no projeto que as possui.

## Princípios

- Pessoas e agentes são consumidores legítimos do toolkit.
- Projetos auditados podem e devem ser reorganizados quando isso os aproxima de um padrão melhor.
- Detecção automática sugere; configuração versionada é a fonte da verdade.
- Resultado vazio não significa aprovação.
- Inventário, auditoria, gate, achado, falha operacional e ausência de configuração são estados diferentes.
- Um módulo não instalado não registra comandos, tarefas ou resultados.
- Módulos públicos representam escolhas reais de instalação, não toda divisão técnica interna.
- Baselines são versionados e suas mudanças devem produzir recomendações de migração compreensíveis.
- O bootstrap pode aplicar mudanças mecânicas seguras, mas não inventa políticas arquiteturais ou de domínio.
- A ampliação do toolkit exige evidência em projeto real e teste de regressão correspondente.
- Não há requisito de compatibilidade retroativa durante a extração inicial.

## Arquitetura modular

### Unidades públicas iniciais

| Módulo | Responsabilidade |
|---|---|
| `nucleo` | CLI, descoberta de módulos, configuração, baseline, execução, schemas e relatórios |
| `spring` | Backend Java/Spring, Gradle, JUnit, JaCoCo, SpotBugs, dependências, arquitetura e contratos |
| `vue` | Vue moderno com TypeScript, npm, typecheck, Vitest, V8, arquitetura e resíduos frontend |
| `thymeleaf` | Templates, fragments, formulários, recursos estáticos e referências entre controllers e views |
| `requisitos` | Organização, expressão, inventário, auditoria e validação de casos de uso em Markdown |

Spring inclui Java e Gradle porque essa é a unidade efetivamente adotada nos projetos previstos. Vue inclui TypeScript e
npm pelo mesmo motivo. Separações internas continuam permitidas quando melhorarem o código, mas não precisam criar
pacotes públicos sem uma escolha de instalação correspondente.

Thymeleaf permanece separado de Spring porque nem todo backend Spring o utiliza. Requisitos é ortogonal à stack e pode
ser instalado com qualquer combinação tecnológica.

### Composições esperadas

```text
Spring + Vue + Requisitos
Spring + Thymeleaf + Requisitos
Spring + Requisitos
Vue + Requisitos
```

Um projeto que não instala Vue não possui comandos ou gates Vue. `nao_aplicavel` não deve ser usado para representar
módulo ausente. Dentro de um módulo instalado, os estados relevantes são:

- `ok`: verificação executada e aprovada;
- `achados`: verificação executada e encontrou problemas;
- `nao_configurado`: falta uma política necessária;
- `indisponivel`: falta ferramenta, dependência ou relatório;
- `falha`: a execução não pôde ser concluída corretamente.

### Organização física proposta

O desenvolvimento pode começar em um monorepo com pacotes independentes:

```text
pacotes/
├── nucleo/
├── spring/
├── vue/
├── thymeleaf/
└── requisitos/
```

Metapacotes de conveniência só devem ser criados se reduzirem trabalho real de instalação. A descoberta do CLI deve
registrar apenas módulos instalados, sem dependências opcionais ocultas.

## Baseline canônico

Cada módulo publica um baseline versionado. A composição instalada determina o baseline total do projeto.

### Baseline do núcleo

- versões mínimas do ambiente;
- arquivos de configuração e diretórios de artefatos;
- scripts de auditoria previsíveis;
- separação entre tarefas estáticas, build e E2E;
- comportamento somente leitura por padrão;
- saídas JSON completas e resumidas;
- documentação operacional mínima para pessoas e agentes.

### Baseline Spring

- Java e Gradle nas versões suportadas;
- Gradle Wrapper presente;
- Spring Boot e plugins esperados;
- plugin de atualização de dependências configurado somente para dependências declaradas pelo projeto;
- SpotBugs;
- JaCoCo e caminho previsível para o relatório;
- JUnit e convenções de testes;
- tarefas estáticas independentes do build completo quando tecnicamente possível;
- arquitetura, contratos, segurança e nomenclatura Spring auditáveis.

### Baseline Vue

- Vue moderno com TypeScript;
- scripts `typecheck`, `lint` e `test:unit` estáveis;
- Vitest e cobertura V8;
- auditoria npm de dependências, atualizações e vulnerabilidades;
- Knip ou capacidade equivalente;
- organização conhecida de componentes, views, stores e rotas;
- orçamento de resíduos configurável;
- E2E mantido no projeto e não incorporado ao toolkit.

### Baseline Thymeleaf

- diretórios previsíveis de templates e recursos estáticos;
- convenções de fragments e layouts;
- referências verificáveis entre controllers e templates;
- validação de formulários, mensagens e recursos;
- auditorias específicas somente quando puderem ser comprovadas semanticamente.

### Baseline de requisitos

O formato oficial inicial é Markdown. Organização canônica sugerida:

```text
documentacao/
└── requisitos/
    ├── casos-de-uso/
    ├── atores.md
    ├── glossario.md
    ├── estados.md
    └── configuracao-requisitos.json
```

O baseline define estrutura e campos dos casos de uso, atores, pré-condições, pós-condições, fluxos, regras de negócio,
referências e rastreabilidade. Vocabulário de domínio, perfis válidos, estados e exigências locais permanecem no projeto.

Os módulos tecnológicos podem produzir evidências de controllers, rotas, templates e testes. O módulo de requisitos cruza
essas evidências por um contrato neutro, sem importar Spring, Vue ou Thymeleaf.

## Auditor de conformidade e bootstrap

### Comandos pretendidos

```bash
ferramentas projeto baseline auditar
ferramentas projeto baseline planejar
ferramentas projeto baseline aplicar --confirmar
ferramentas projeto integrar detectar
ferramentas projeto integrar inicializar --gravar
ferramentas projeto integrar verificar
```

A nomenclatura final deve ser revisada para evitar sobreposição entre baseline e integração. A superfície pública deve
permanecer pequena; comandos podem ser fundidos se os contratos ficarem mais claros.

### Detecção

O detector deve localizar, com evidência e nível de confiança:

- módulos Gradle e projetos npm;
- fontes e testes Java;
- frontend Vue e templates Thymeleaf;
- ferramentas e plugins instalados;
- scripts npm e tarefas Gradle;
- relatórios JaCoCo e V8;
- casos de uso e documentação;
- regras Semgrep, orçamentos e políticas existentes.

Detecção não deve aplicar políticas nem considerar ausência como aprovação.

### Auditoria do baseline

Cada requisito é classificado como obrigatório, recomendado, opcional ou obsoleto. O relatório deve informar:

- código estável do requisito;
- módulo e versão do baseline;
- status;
- evidência encontrada;
- recomendação concreta;
- possibilidade de correção automática;
- arquivos potencialmente afetados.

### Planejamento e aplicação

O planejamento converte divergências em mudanças propostas: arquivos, scripts, plugins, dependências, diretórios e decisões
humanas. A aplicação exige confirmação e se limita a transformações determinísticas e reversíveis.

Não devem ser inferidos automaticamente:

- regras arquiteturais locais;
- orçamento definitivo de resíduos;
- política Semgrep de domínio;
- vocabulário de requisitos;
- tarefas de build consideradas seguras;
- interpretação de módulos ambíguos.

O bootstrap pode gerar rascunhos explícitos para esses itens, sempre marcados como não calibrados.

## Configuração por projeto

Cada projeto deve versionar, conforme os módulos instalados:

- configuração principal do toolkit;
- seleção e versões dos baselines;
- módulos e diretórios;
- tarefas de qualidade;
- regras Semgrep locais;
- orçamento e exceções de resíduos;
- vocabulário e política de requisitos;
- caminhos de relatórios;
- exceções justificadas e, quando apropriado, prazo de revisão.

A configuração não deve servir para preservar desorganização sem justificativa. Quando a divergência for acidental e a
mudança tiver baixo risco, o relatório deve recomendar adequação ao baseline.

## Skill de adoção e evolução

Uma skill específica deve orquestrar o processo que exige julgamento:

1. analisar o projeto e as instruções locais;
2. executar a detecção e a auditoria de baseline;
3. separar divergências acidentais de variações legítimas;
4. propor e realizar adequações no projeto;
5. gerar e revisar a configuração;
6. executar uma amostra representativa do toolkit;
7. procurar falsos positivos, falsos negativos e sucessos vazios;
8. calibrar políticas e orçamentos locais;
9. decidir se uma lacuna pertence ao projeto, ao perfil ou ao toolkit;
10. adicionar regressões quando o toolkit for modificado;
11. validar e documentar o estado final.

A skill pode modificar o projeto auditado e o toolkit, mas uma mudança no toolkit é uma decisão explícita, nunca efeito
colateral do bootstrap. Ela não duplica algoritmos: consome comandos e contratos públicos do toolkit.

## Ciclo de aprendizado entre projetos

Cada novo projeto deve alimentar uma destas categorias:

| Achado | Destino |
|---|---|
| Prática melhor para todos | baseline canônico |
| Variação legítima e recorrente | opção ou adaptador do módulo |
| Falso positivo ou falso negativo | motor e fixture de regressão |
| Regra de domínio | configuração do projeto |
| Caso único ou temporário | não ampliar o toolkit |
| Organização acidentalmente divergente | adequar o projeto |

Uma política local só se torna compartilhada quando houver contrato claro e evidência em mais de um corpus real.

## Estratégia de extração

### Fase 1 — contrato

1. inventariar comandos, motores, políticas, dependências e arquivos atuais;
2. classificar cada item entre núcleo, Spring, Vue, Thymeleaf, requisitos ou política SGC;
3. definir contratos de registro de comandos, evidências, resultados e baselines;
4. identificar imports, caminhos e fixtures que ainda dependem do workspace SGC.

### Fase 2 — modularização interna

1. tornar explícita a seleção de perfil SGC;
2. remover detecção por presença de arquivos do SGC;
3. separar políticas SGC dos motores reutilizáveis;
4. fazer o CLI compor apenas módulos registrados;
5. preservar testes semânticos com SGC e SAPE como corpora externos.

### Fase 3 — baseline e bootstrap

1. criar os baselines iniciais de núcleo, Spring, Vue e requisitos;
2. criar o esqueleto Thymeleaf sem inventar auditorias ainda não comprovadas;
3. implementar detecção, auditoria, planejamento e geração de configuração;
4. produzir JSON completo e resumido para uso por agentes;
5. testar projetos conformes, divergentes, incompletos e parcialmente indisponíveis.

### Fase 4 — extração física

1. criar o repositório independente;
2. mover o código sem manter implementação duplicada no SGC;
3. configurar build, testes, pacote e publicação;
4. instalar versões do toolkit no SGC e no SAPE;
5. mover políticas locais para os respectivos projetos;
6. executar a prova real sem depender do checkout do toolkit.

### Fase 5 — adoção

1. versionar as configurações do SGC e SAPE;
2. criar a skill de adoção e evolução a partir do fluxo comprovado;
3. integrar gradualmente projetos Spring/Thymeleaf;
4. revisar o baseline com os aprendizados desses projetos;
5. publicar versões e guias de migração do baseline.

## Decisões em aberto

- nome definitivo do repositório, escopo npm e pacotes;
- mecanismo de descoberta e registro dos módulos instalados;
- granularidade entre comandos de baseline e integração;
- formato do contrato neutro de evidências entre requisitos e módulos tecnológicos;
- política de versões dos baselines e compatibilidade com versões do pacote;
- localização da skill e forma de distribuir sua versão compatível com o toolkit;
- momento em que Thymeleaf terá funcionalidade suficiente para publicação.

Essas decisões devem ser tomadas com protótipos mínimos e validação nos projetos reais, não apenas por desenho abstrato.

## Critérios de término

A iniciativa estará concluída quando:

- o toolkit estiver em repositório independente e instalado como dependência;
- módulos ausentes não registrarem comandos ou gates;
- SGC e SAPE usarem o mesmo pacote sem carregar políticas um do outro;
- ao menos um projeto Spring/Thymeleaf validar a composição correspondente;
- baselines forem versionados e produzirem relatórios acionáveis;
- bootstrap gerar configuração inicial sem fabricar aprovações;
- mudanças automáticas exigirem confirmação e tiverem testes;
- requisitos em Markdown funcionarem independentemente da stack;
- cada gate provar aprovação, reprovação e pré-condição ausente;
- package tests, typechecks, lint, análise de dependências e build passarem no repositório independente;
- a skill executar o ciclo completo de adoção e devolver aprendizados ao baseline, aos módulos ou ao projeto correto.
