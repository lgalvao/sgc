# Plano de modernização do toolkit

## Objetivos

O trabalho abrange exclusivamente `toolkit/`. O SGC não consome nem depende do toolkit: seu workspace é alvo de
auditoria, fonte de políticas locais e corpus real de validação.

1. remover código temporário, obsoleto, redundante ou sem finalidade permanente;
2. separar capacidades horizontais, integrações de stack e políticas específicas do SGC;
3. simplificar e uniformizar diretórios, símbolos, comandos, opções e resultados;
4. manter a implementação em TypeScript e português brasileiro;
5. preservar as capacidades específicas do SGC que continuam úteis;
6. distribuir um pacote utilizável pelo autor, por agentes e por outros projetos sem depender do workspace do SGC.

Não existe requisito de compatibilidade retroativa. A continuidade exigida é funcional, não histórica.

## Diretrizes vigentes

### Utilidade e escopo

- Uso humano ou por agentes é consumo legítimo; ausência de imports não torna um comando inútil.
- Todo comando mantido precisa ter finalidade permanente explicável em uma frase.
- Testes não justificam uma funcionalidade cuja finalidade desapareceu.
- Utilitários ocasionais podem permanecer quando resolvem um problema recorrente e são identificados como tal.
- O histórico pertence ao Git. Este plano descreve apenas decisões vigentes e trabalho ainda necessário.
- O SGC é corpus de validação do toolkit, não parte da implementação nem consumidor dela.

### Fronteiras

| Camada | Critério | Exemplos |
|---|---|---|
| Núcleo | algoritmo independente de projeto e stack | CLI, configuração, parser CDU, agregação de cobertura |
| Adaptador | integração parametrizável com linguagem, framework ou ferramenta | Gradle, npm, Vue, OpenAPI, Semgrep, JaCoCo |
| Perfil SGC | convenção, caminho ou política usada para auditar o SGC | notificações, views, modais, coesão e defaults SGC |

Uma política SGC bem identificada é um resultado correto. Ela só deve ser promovida a núcleo ou adaptador quando houver
contrato horizontal claro e teste externo que não carregue defaults do SGC.

### Implementação e execução

- TypeScript é a única fonte de implementação.
- `binarios/ferramentas.cjs` é a única exceção: lançador mínimo necessário ao `bin` do npm, sem regra de domínio.
- Código, símbolos, mensagens e documentação próprios usam português brasileiro.
- Vocabulário de bibliotecas e formatos externos permanece apenas nas bordas de integração.
- `toolkit/ferramentas.ts` executa diretamente com `tsx`; `dist/` existe somente para validar o build.
- Node 26.7 ou superior é o ambiente mínimo.
- Não criar aliases, wrappers de transição ou implementações paralelas.
- Não dividir arquivos, introduzir cache, concorrência ou abstrações sem responsabilidade independente ou evidência real.

### Segurança e contratos

- Auditorias e inventários são somente leitura por padrão.
- Persistência exige `--gravar`; remoção exige `--confirmar`, salvo quando persistir é a finalidade explícita do comando.
- JSON limpo vai para stdout; diagnóstico operacional vai para stderr.
- Opções desconhecidas, valores ausentes e argumentos excedentes falham.
- Ajuda, parser e catálogo seguem a gramática `<dominio> <recurso> <acao>` e devem permanecer sincronizados.
- Formatos próprios persistidos ou consumidos por outro comando têm tipo, versão e validação de entrada.
- Funções de domínio retornam resultados ou lançam erros; somente a borda CLI altera `process.exitCode`.

### Reuso

- `configuracao-toolkit.json` é o contrato de configuração por projeto; `--base` identifica a raiz auditada.
- Motores horizontais recebem caminhos, políticas e executores explicitamente.
- Defaults locais ficam na borda ou em módulos identificados como perfil SGC.
- O pacote é `ferramentas-projeto` e o binário é `ferramentas`.
- A instalação isolada não depende do `node_modules`, da configuração nem do layout do SGC.
- APIs programáticas são deliberadas e validadas por fixture ou consumidor isolado; não precisam de imports prévios no
  repositório para serem legítimas.

## Situação atual

A modernização estrutural está concluída:

- CLI, catálogo e README registram finalidade, camada, decisão e efeitos dos 38 comandos públicos;
- não há comando classificado como temporário, redundante ou pendente de decisão;
- a implementação é TypeScript e executa diretamente com `tsx` em Node 26;
- o pacote e o binário têm identidade neutra e o tarball é testado em consumidor isolado;
- casos de uso foram consolidados em `requisitos cdus inventariar` e `requisitos cdus auditar`;
- CDU, cobertura Java/web e análise de testes Java possuem contratos reutilizáveis e testes com projeto externo;
- Semgrep, OpenAPI, JaCoCo, V8 e sincronização de versão recebem entradas explícitas em seus motores;
- defaults e políticas do SGC estão nas bordas ou em módulos identificados como perfil SGC;
- acessibilidade Playwright/Axe pertence a `e2e/`, fora do toolkit;
- o reality check contra o SGC corrigiu classificações contraditórias, ambiguidades sistemáticas e destaques sem violação;
- testes, pacote isolado, typechecks, lint, Knip, build e `git diff --check` passam.

Essa situação não comprova, por si só, a correção semântica uniforme de todos os auditores. A revisão final encontrou
auditores com bons testes comportamentais e outros ainda cobertos principalmente por execução, schema, persistência ou
um único exemplo positivo. Portanto, a modernização não deve ser declarada encerrada enquanto o recorte abaixo estiver
aberto.

## Recorte final — comprovação semântica

### Objetivo

Comprovar que os auditores mantidos respondem corretamente às perguntas que anunciam, sem reabrir arquitetura, catálogo
ou generalização já encerrados.

### Passos obrigatórios

1. Criar uma matriz `auditor -> regras relevantes -> evidências existentes -> lacunas`.
2. Priorizar regras que produzem gate, severidade, pontuação, violação ou recomendação acionável.
3. Completar somente as evidências necessárias para cada tipo de regra:
   - achado positivo mínimo, com arquivo e motivo corretos;
   - negativo próximo para heurísticas sujeitas a falso positivo;
   - limite inferior/superior quando houver threshold ou mudança de classificação;
   - invariantes entre resumo, severidade, lista destacada e motivos;
   - mais de um arquivo quando contagem, duplicação ou escala alterar o resultado;
   - saída humana quando ela for o principal produto do comando.
4. Não exigir mecanicamente todos os casos acima de toda regra. A matriz deve registrar `não aplicável` com justificativa
   curta quando a natureza da regra não exigir uma dimensão.
5. Executar uma amostra curta e representativa no SGC, cobrindo ao menos:
   - um auditor de servidor;
   - um auditor de cliente;
   - uma capacidade horizontal configurável;
   - CDU;
   - um orquestrador ou agregador.
6. Confrontar os principais resultados com os arquivos apontados. Todo falso positivo, falso negativo ou texto enganoso
   confirmado gera primeiro uma regressão mínima e depois a correção.

### Limites do recorte

- Não buscar cobertura total nem um percentual arbitrário.
- Não testar detalhes internos, fórmulas copiadas da implementação ou snapshots extensos.
- Não generalizar políticas SGC sem necessidade de um segundo projeto.
- Não reabrir comandos já decididos sem nova evidência de inutilidade ou incorreção.
- Inventários, transformadores e orquestradores não são auditores: recebem testes adequados ao seu contrato, não uma
  matriz artificial de falsos positivos e severidade.
- Melhorias sem relação com um critério de término vão para backlog e não ampliam este recorte.

### Critério de saída do recorte

O recorte termina quando a matriz não tiver lacuna obrigatória, as regressões descobertas estiverem corrigidas e a amostra
real não apresentar classificação contraditória, ambiguidade sistemática ou destaque sem motivo correspondente.

## Critérios de término

- [x] todos os comandos públicos têm finalidade atual, camada e efeitos documentados;
- [x] não há comando decidido como redundante, temporário ou obsoleto ainda presente;
- [x] não há JavaScript legado, alias de compatibilidade ou implementação paralela;
- [x] nomes e contratos próprios seguem TypeScript e português brasileiro;
- [x] motores classificados como núcleo ou adaptador não importam política ou caminho SGC;
- [x] políticas para auditar o SGC estão identificadas e continuam funcionando contra seu workspace;
- [x] CDU e análise de testes Java funcionam com projeto externo sem editar o toolkit;
- [x] ajuda, parser, catálogo e README concordam sobre a superfície pública;
- [x] o tarball instalado isoladamente executa o binário e as APIs públicas suportadas;
- [ ] a matriz semântica está completa e não contém lacuna obrigatória;
- [ ] a amostra final contra o SGC foi confrontada com o código e não revelou resultado enganoso sem regressão;
- [x] testes, typechecks, lint, Knip, build e `git diff --check` passam.

“Não existir qualquer melhoria possível” não é critério de término. Cobertura total, tamanho máximo de arquivo,
generalização de toda política e padronização de formatos externos também não são requisitos.

## Regra contra rabbit holes

Um novo achado só bloqueia a conclusão se violar correção ou segurança, funcionamento contra o SGC, reuso de capacidade
declarada horizontal, consistência pública ou um critério de término acima. Caso contrário, fica fora do recorte.

Cada correção deve ser pequena, semanticamente coesa, validada e seguida de commit/push. Um achado maior substitui uma
etapa futura ou vai para backlog; não cria uma cadeia ilimitada de sub-recortes.

## Validação

Ao encerrar uma rodada com código:

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

Para alteração exclusivamente documental, validar referências afetadas, consistência com catálogo/README e executar
`git diff --check`.

## Backlog após a conclusão

O backlog não bloqueia o encerramento e deve permanecer pequeno. Só registrar item com evidência concreta e condição
clara para retomada. Ideias sem caso de uso ficam fora do plano.
