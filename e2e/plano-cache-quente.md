# Plano de Cobertura E2E para Cache Quente

## Situação atual

Hoje a suíte E2E privilegia isolamento forte e, por isso, a maior parte dos cenários começa em estado frio:

- `POST /e2e/reset-database` reseeda o H2 e limpa **todos** os caches Spring (`CacheManager`) em [backend/src/main/java/sgc/e2e/E2eController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/e2e/E2eController.java:81).
- A fixture `_resetAutomatico` roda esse reset antes de cada teste normal e uma vez por arquivo serial em [e2e/fixtures/complete-fixtures.ts](/Users/leonardo/sgc/e2e/fixtures/complete-fixtures.ts:29).
- O helper de login limpa `localStorage`, `sessionStorage`, cookies e desmonta a página atual em [e2e/helpers/helpers-auth.ts](/Users/leonardo/sgc/e2e/helpers/helpers-auth.ts:64).

Isso é coerente para manter banco e cache backend sincronizados. Sem limpar cache após reset de banco, o teste começaria num estado impossível.

## Risco de cobertura

O risco não é “os testes estão errados”. O risco é de **viés**:

- a suíte pega muito bem bugs de fluxo em cold start;
- a suíte pega menos bugs de reuso indevido de contexto, invalidação faltando e cache aquecido;
- problemas que aparecem só após navegação repetida, troca de usuário na mesma aba ou reabertura da mesma tela ficam sub-representados.

Há sinais de que esse risco já existe no domínio:

- [e2e/cdu-07.spec.ts](/Users/leonardo/sgc/e2e/cdu-07.spec.ts:383) já protege uma regressão “sem limpar caches da SPA”;
- [e2e/cdu-15.spec.ts](/Users/leonardo/sgc/e2e/cdu-15.spec.ts:66) já protege permanência de contexto ao sair e voltar ao mapa;
- a jornada semântica em [e2e/fluxo-completo-semantico.spec.ts](/Users/leonardo/sgc/e2e/fluxo-completo-semantico.spec.ts:56) e a jornada principal em [e2e/jornada.spec.ts](/Users/leonardo/sgc/e2e/jornada.spec.ts:12) já seguem o modelo “reset uma vez e navegar quente”.

## Onde a suíte está excessivamente fria

Os melhores candidatos não são telas triviais. São fluxos que:

- dependem de contexto de processo/subprocesso;
- reaproveitam stores, `KeepAlive` ou contexto de navegação;
- mudam perfil/localização e exigem invalidação correta;
- voltam para a mesma tela após mutação.

Prioridade mais alta no checkout atual:

1. `SubprocessoView` e cards de ação após troca de usuário na mesma aba.
   Evidência: [e2e/cdu-07.spec.ts](/Users/leonardo/sgc/e2e/cdu-07.spec.ts:383).

2. Reentrada no mapa após mutações locais e navegação interna.
   Evidência: [e2e/cdu-15.spec.ts](/Users/leonardo/sgc/e2e/cdu-15.spec.ts:66).

3. Histórico/análises após nova disponibilização no mesmo fluxo.
   Evidência: [e2e/cdu-10.spec.ts](/Users/leonardo/sgc/e2e/cdu-10.spec.ts:238).

4. Painel e subprocesso depois de ações de workflow que deveriam invalidar só o necessário.
   Evidência indireta: o frontend já trata cache/invalidação explicitamente em `PainelView` e `SubprocessoView`, e isso tende a falhar mais em cenário aquecido do que em cold start.

5. Fluxos administrativos com volta imediata para a listagem afetada.
   Exemplo provável: notificações, administradores, configurações e histórico, onde o usuário executa ação e reconsulta a mesma coleção sem reiniciar sessão.

## Proposta de cobertura “cache quente”

Manter o modelo atual para a maior parte da suíte e adicionar uma camada pequena, explícita e de alto valor.

### Classe 1: isolamento forte

Continuar usando:

- `_resetAutomatico`;
- novo `browser context` por teste;
- login helper atual com limpeza completa.

Essa classe continua sendo a base da suíte.

### Classe 2: jornada serial quente

Usar `test.describe.serial` com reset único no início do arquivo quando o objetivo for validar reuso entre fases.

Boas referências já existentes:

- [e2e/jornada.spec.ts](/Users/leonardo/sgc/e2e/jornada.spec.ts:11)
- [e2e/fluxo-completo-semantico.spec.ts](/Users/leonardo/sgc/e2e/fluxo-completo-semantico.spec.ts:26)

### Classe 3: regressão de cache quente

Criar poucos testes dedicados, com nome explícito de intenção, evitando limpeza de SPA entre passos relevantes.

#### Cenário 1

`SubprocessoView` deve recalcular permissões após logout/login na mesma aba sem limpar contexto local além do logout funcional.

Status:
já existe um embrião forte em [e2e/cdu-07.spec.ts](/Users/leonardo/sgc/e2e/cdu-07.spec.ts:383).

Próximo passo:
extrair esse padrão para uma mini convenção ou helper para repetir em outros módulos sensíveis.

#### Cenário 2

`PainelView` deve refletir a nova situação após workflow e retorno imediato, sem refresh manual e sem reset global.

Motivo:
é um ponto clássico para cache local válido demais ou inválido de menos.

Arquivo sugerido:
novo bloco em `jornada.spec.ts` ou `fluxo-completo-semantico.spec.ts`, porque esses fluxos já atravessam mutações relevantes.

#### Cenário 3

Mapa deve reabrir com dados coerentes após criar, editar, remover e voltar para a mesma unidade no mesmo contexto.

Status:
parcialmente coberto em [e2e/cdu-15.spec.ts](/Users/leonardo/sgc/e2e/cdu-15.spec.ts:66).

Lacuna:
expandir para cobrir remoção/edição seguida de retorno, não só permanência após criação.

#### Cenário 4

Histórico e análises devem preservar apenas o que é persistido após nova disponibilização, sem reaproveitar snapshot velho da tela anterior.

Status:
já há bom ponto de entrada em [e2e/cdu-10.spec.ts](/Users/leonardo/sgc/e2e/cdu-10.spec.ts:238).

Lacuna:
endurecer a asserção para distinguir “veio do backend” de “sobrou da memória da SPA”.

#### Cenário 5

Coleções administrativas devem refletir ação recém-executada ao voltar para a lista, sem reset de sessão.

Candidatos:

- `cdu-29.spec.ts` histórico;
- `cdu-30.spec.ts` administradores;
- telas de notificações já atravessadas pela jornada.

## Critério prático

Não transformar a suíte inteira em “quente”. O corte bom é:

- maioria dos testes continua fria e isolada;
- poucos testes estratégicos exercitam estado aquecido;
- cada teste quente deve proteger uma classe concreta de regressão, não “simular usuário real” genericamente.

## Próxima implementação sugerida

Ordem recomendada:

1. consolidar um helper de “troca de usuário sem limpeza agressiva da SPA” a partir de [e2e/cdu-07.spec.ts](/Users/leonardo/sgc/e2e/cdu-07.spec.ts:383);
2. expandir [e2e/cdu-15.spec.ts](/Users/leonardo/sgc/e2e/cdu-15.spec.ts:66) para cobrir reentrada após remoção/edição;
3. endurecer [e2e/cdu-10.spec.ts](/Users/leonardo/sgc/e2e/cdu-10.spec.ts:238) para provar invalidação correta do histórico;
4. adicionar um cenário quente de painel/lista administrativa com retorno imediato após mutação.
