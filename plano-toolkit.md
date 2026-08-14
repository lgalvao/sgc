# Plano de modernização do toolkit

## Objetivos

O trabalho abrange exclusivamente `toolkit/`. O SGC e outros projetos são corpus de validação, não consumidores nem
partes da implementação.

1. remover código temporário, obsoleto, redundante ou sem finalidade permanente;
2. separar núcleo horizontal, adaptadores de stack e políticas específicas de projeto;
3. simplificar e uniformizar comandos, opções, nomes, resultados e documentação;
4. manter TypeScript como fonte única e português brasileiro no vocabulário próprio;
5. preservar capacidades específicas do SGC que continuem úteis, sem aplicá-las silenciosamente a outros projetos;
6. oferecer um pacote utilizável diretamente por pessoas e agentes em projetos Java/Spring/Vue semelhantes;
7. comprovar utilidade e correção com projetos reais, além de testes sintéticos.

Não há requisito de compatibilidade retroativa.

## Diretrizes

### Utilidade e escopo

- Uso humano ou por agentes é consumo legítimo; ausência de imports não é critério de remoção.
- Todo comando mantido deve responder a uma pergunta permanente e explicável em uma frase.
- Inventário, auditoria e gate são contratos diferentes e devem ser nomeados e sinalizados como tal.
- Resultado vazio, código zero ou teste verde não comprovam utilidade; fixtures devem conter violações conhecidas.
- O histórico pertence ao Git. Este documento contém somente objetivos, aprendizados vigentes e próximos passos.

### Fronteiras

| Camada | Responsabilidade |
|---|---|
| Núcleo | CLI, configuração, schemas e algoritmos independentes de projeto |
| Adaptador | Integração parametrizável com Java, Spring, Vue, npm, Gradle, Semgrep, JaCoCo, V8 ou OpenAPI |
| Perfil de projeto | caminhos, orçamentos, vocabulário e políticas locais, inclusive as do SGC |

- Motores horizontais recebem caminhos e políticas explicitamente.
- Defaults genéricos podem existir quando não produzem aprovação enganosa.
- Política ausente deve gerar `nao_configurado` ou erro claro quando for necessária para interpretar o resultado.
- Uma política SGC só é aplicada ao SGC e deve permanecer identificável como perfil local.
- Pequenos ajustes nos projetos auditados são aceitáveis: configuração, orçamento, regras e scripts fazem parte da
  integração saudável com o toolkit.

### Implementação e execução

- TypeScript é a única implementação; `binarios/ferramentas.cjs` é apenas o lançador npm.
- A execução usa `tsx` diretamente; `dist/` serve somente para validar o build do pacote.
- Node 26.7 ou superior é o ambiente mínimo.
- Código, símbolos, mensagens e documentação próprios usam português brasileiro.
- Formatos externos preservam seu vocabulário somente na borda.
- Não criar aliases, wrappers de transição ou implementações paralelas.
- Não introduzir abstrações, cache ou concorrência sem responsabilidade independente e evidência real.

### Segurança e contratos

- Auditorias e inventários são somente leitura por padrão.
- Persistência exige `--gravar`; remoção exige `--confirmar`.
- JSON limpo vai para stdout e diagnóstico operacional para stderr.
- Erros de execução são diferentes de achados de auditoria.
- Opções desconhecidas, valores ausentes e entradas incompatíveis falham explicitamente.
- Formatos persistidos ou encadeados possuem tipo, versão e validação.
- Respostas potencialmente grandes oferecem resumo limitado para agentes.

## Aprendizados atuais

### Estado do toolkit

- A superfície pública está consolidada em 38 comandos com gramática `<dominio> <recurso> <acao>`.
- Casos de uso foram reduzidos a `requisitos cdus inventariar` e `requisitos cdus auditar`.
- Acessibilidade Playwright/Axe pertence aos diretórios `e2e` dos projetos, não ao toolkit.
- O pacote isolado e o binário não dependem do layout nem do `node_modules` do SGC.
- Cobertura Java/web, análise de testes, arquitetura, contratos, resíduos, nomenclatura e CDU possuem saídas estruturadas;
  os produtores volumosos oferecem `--json-resumido`.
- A política de idioma do SGC, inclusive a rejeição de `id`, permanece em `codigo nomes auditar-idioma`; a consistência
  horizontal verifica somente convenções compartilháveis.
- O perfil de qualidade do SGC coleta resíduos como inventário. Um gate só existe quando há orçamento configurado.

### Prova real no SAPE

O SAPE em `/Users/leonardo/hyphenation/sape` possui Spring Boot, Java e Vue/TypeScript, com módulos `servidor`, `admin` e
`etl`. A dependência Java privada indisponível impede build e testes do backend; por isso a prova atual cobre somente
análises estáticas e tarefas frontend independentes do Gradle.

Achados que já produziram correções horizontais:

- arquitetura e contratos precisavam reconhecer anotações Spring, nomes e pacotes em português e inglês;
- os três auditores Java precisavam aceitar `--diretorio <modulo>`;
- a análise de testes precisava classificar `Servico`, `Controlador`, `Fachada`, `modelo` e equivalentes em inglês;
- fora do SGC, a análise de testes agora usa política genérica, nunca a política SGC implícita;
- resíduos precisava reconhecer `componentes`, `visoes` e `stores` como camadas equivalentes;
- Semgrep externo sem regra própria agora falha com orientação, em vez de usar regras SGC;
- validação de resíduos sem orçamento retorna `nao_configurado` e código não zero;
- auditoria de dependências separa vulnerabilidade ou pacote desatualizado de falha operacional da ferramenta;
- consistência de nomes não trata componentes Vue lazy em PascalCase como funções inválidas e aceita nomes descritivos
  com `_` em métodos de teste, mantendo a regra camelCase em produção;
- cópia sem `.git` recebe erro de pré-condição claro em `projeto arvore-linhas`.

Resultados representativos atuais:

- arquitetura: 82 alvos no `servidor`, 28 no `admin` e 17 no `etl`;
- contratos: 3 controladores no `servidor`, 12 controladores e 3 DTOs no `admin`;
- testes do `servidor`: 261 classes, 99 com teste dedicado e 89 pendências não classificadas como ruído;
- nomenclatura: 882 arquivos, sem violações estruturais após eliminar dois falsos positivos sistemáticos;
- resíduos: inventário com pontuação de ordenação 611 e status `nao_configurado`, portanto sem aprovação falsa;
- frontend: typecheck e 253 testes unitários passaram no perfil estático;
- dependências: a prova anterior encontrou achados reais de Knip e atualização de pacote, sem usar Gradle.

### Limitações conhecidas

- A identificação automática do perfil SGC ainda usa a presença de `toolkit/ferramentas.ts`; é eficaz no workspace atual,
  mas um perfil explícito na configuração seria uma fronteira mais limpa.
- Projetos multimódulo exigem repetir `--diretorio`; a configuração ainda não declara uma coleção de módulos Java para
  execução agregada.
- Semgrep não oferece valor em um projeto externo até que esse projeto forneça ou escolha regras próprias.
- Resíduos é inventário, não gate, até existir orçamento calibrado no projeto auditado.
- Métricas JaCoCo e verificações que dependem do classpath Java ficam indisponíveis enquanto o backend do SAPE não
  resolver sua dependência privada.
- `projeto arvore-linhas` depende de metadados Git e não analisa uma cópia sem repositório.
- `projeto dependencias auditar` distingue status corretamente, mas ainda não oferece JSON; agentes precisam interpretar
  a saída humana para identificar escopos com achados ou falhas.
- Auditores heurísticos podem ter novos falsos positivos em vocabulários ou estruturas ainda não representados pelos
  corpora SGC e SAPE; cada correção deve vir acompanhada de regressão sem enfraquecer produção.

### Oportunidades de integração nos projetos

- Versionar `configuracao-toolkit.json` no SAPE com diretórios, módulos, perfis de qualidade e escopos de dependências.
- Criar regras Semgrep compartilhadas para a stack e manter regras realmente locais em perfis separados.
- Calibrar e versionar um orçamento de resíduos por projeto após revisar o inventário inicial.
- Expor relatórios JaCoCo/V8 nos caminhos configurados quando os builds estiverem disponíveis.
- Ajustar scripts npm dos projetos para oferecer tarefas estáticas estáveis (`typecheck`, testes unitários, lint sem
  mutação e auditoria de dependências).
- Manter uma fixture mínima por projeto com violações conhecidas para provar que os auditores não apenas executam.

## Próximos passos

### Prioridade 1 — fechar a prova SAPE atual

1. executar a auditoria de dependências após a nova separação entre achado e falha operacional;
2. executar regressão representativa no SGC para confirmar que políticas locais continuam ativas somente ali;
3. executar a validação completa do toolkit: testes, pacote isolado, typechecks, lint, Knip, build e `git diff --check`;
4. registrar somente resultados atuais no README e encerrar o recorte.

### Prioridade 2 — tornar a integração externa deliberada

1. substituir detecção implícita do SGC por seleção explícita de perfil na configuração;
2. decidir um schema de módulos Java e agregar arquitetura, contratos e análise de testes sem multiplicar comandos;
3. adicionar saída JSON completa e resumida à auditoria de dependências;
4. versionar a configuração do SAPE e fornecer uma regra Semgrep e um orçamento de resíduos mínimos;
5. criar um comando de diagnóstico da integração que informe capacidades disponíveis, indisponíveis e não configuradas,
   sem executar builds.

### Prioridade 3 — ampliar comprovação semântica

1. manter fixtures positivas, negativas e de erro operacional para cada gate;
2. adicionar corpus externo sempre que um falso positivo ou falso negativo real for encontrado;
3. quando a dependência privada do SAPE estiver disponível, validar JaCoCo e tarefas backend sem transformar o build em
   pré-condição das auditorias estáticas;
4. só promover uma política local a compartilhada quando SGC e SAPE demonstrarem o mesmo contrato.

## Critérios de término

O trabalho pode ser declarado concluído quando:

- cada comando tiver finalidade permanente, camada e contrato de saída claros;
- nenhuma política ou caminho SGC for aplicado silenciosamente fora do perfil SGC;
- inventário, auditoria, gate, achado, falha operacional e não configurado forem distinguíveis;
- SGC e SAPE produzirem resultados acionáveis em amostras representativas;
- cada gate tiver testes que provem aprovação, reprovação e pré-condição ausente;
- pacote isolado, testes, typechecks, lint, Knip e build passarem;
- README refletir apenas o estado atual e este plano contiver somente objetivos, aprendizados e próximos passos;
- limitações restantes forem integrações opcionais ou dependências externas documentadas, não defeitos silenciosos.
