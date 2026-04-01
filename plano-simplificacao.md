# Plano de Simplificação do SGC

Este plano consolida recomendações concretas sustentadas pela leitura do código atual. O objetivo é reduzir
complexidade acidental sem perder regras de negócio, segurança, transações, contratos de API ou padronizações úteis já
existentes.

## Princípios gerais

* Simplificar primeiro a menor fronteira segura.
* Tornar dependências e fluxos explícitos.
* Reduzir superfície pública antes de criar abstração nova.
* Desconfiar de abstração genérica com um único consumidor real.
* Preservar contratos externos, DTOs e regras de acesso.
* Remover código morto logo após a simplificação.
* Validar em passos pequenos e registrar aprendizado no próprio plano.
* Usar medição como guarda da simplificação, não como objetivo separado.

## Guardrails obrigatórios

Antes de qualquer simplificação, aplicar as seguintes restrições:

* Não remover DTOs de forma mecânica.
* DTOs devem continuar sendo a fronteira padrão para respostas e cargas úteis externas.
* Qualquer proposta de remoção ou fusão de DTO precisa verificar antes:
  * contrato consumido pelo frontend;
  * proteção contra lazy loading fora da transação;
  * prevenção de serialização acidental de grafo JPA;
  * isolamento entre modelo de domínio e API.
* Em caso de dúvida, preferir manter o DTO e simplificar apenas mapeamento, nome, escopo ou duplicação.
* Simplificação arquitetural não pode aumentar acoplamento entre controller e entidade JPA.
* Novas facades só devem ser criadas quando centralizarem regra transversal clara.
* Acesso direto de controllers a repositórios: restrito a leituras triviais sem regra de negócio nem segurança
  contextual.

## Situação atual

As etapas preliminares e frentes A a D de simplificação do frontend (limpeza de wrappers, round-trips reduzidos) e a consolidação das lógicas do back-end (fixação de endpoints em um ciclo central e lazy loads) já foram concluídas. O sistema se encontra rápido, estável (testes 100% no verde) e com as diretrizes arquiteturais ativas.

### Pontos de atenção herdados
* O reaproveitamento em `SubprocessoTransicaoService` e `SubprocessoService` deixou ambas classes com alto volume (33KB e 42KB respectivamente).
* `LoadingButton.vue` continua como wrapper fino, o que deve ser evitado.

---

## Próxima fase — Refatoração de Arquitetura

### Frente E. Redução de Classes Hipertrofiadas no Backend

**Problema:**
Serviços como `SubprocessoService` (42.7 KB) e `SubprocessoTransicaoService` (33 KB) concentram responsabilidades demais e estão difíceis de manter.
Embora o reaproveitamento inicial tenha removido duplicações locais, o acoplamento entre esses componentes ainda é alto e o volume de linhas penaliza a manutenibilidade.

**Objetivo:**
* Extirpar domínios paralelos (como orquestração de leitura x transições).
* Separar fluxos de workflow num componente coeso (ex: `SubprocessoWorkflowFacade` ou segregando a orquestração).
* Distribuir consultas (leitura) isoladas para um `SubprocessoConsultaService` puro.

**Critério de execução:**
* Antes de cortar classes, mapear interdependências atuais.
* Reduzir o acoplamento entre essas duas superclasses transferindo a interface de dados puramente a componentes menores quando viável.
* Preservar o 100% de cobertura nos testes de unidade (1401 testes green).

---

## Backlog pendente de prioridade menor

### Auditar wrappers visuais finos

**Escopo:** `LoadingButton.vue` e demais componentes comuns com baixo volume de lógica própria.

**Perguntas de triagem:**

* Padroniza algo recorrente?
* Reduz duplicação material?
* Adiciona acessibilidade ou comportamento?
* Evita divergência visual entre telas?

**Critério de pronto:** cada componente auditado classificado como manter, ajustar ou remover.

---

## Critério de sucesso

O plano terá sido bem executado se:

* houver menos pontos de navegação para seguir um fluxo simples;
* não houver perda de regras de segurança, transação ou notificação;
* o frontend reduzir singletons desnecessários sem espalhar tratamento de erro;
* componentes comuns restantes tiverem justificativa clara de existência.

---

## Aprendizados consolidados

Regras empíricas extraídas das rodadas anteriores, para orientar decisões futuras:

1. **Duplicação antes de fusão.** O melhor candidato inicial não é fusão de serviço, e sim remoção de duplicação
   utilitária.
2. **Reuso com registro.** Reutilizar serviço existente é aceitável como etapa intermediária, desde que o
   acoplamento gerado fique registrado e seja reavaliado.
3. **Assinaturas públicas.** Ao simplificar serviços usados por testes, preservar assinaturas públicas ou introduzir
   sobrecargas compatíveis. Porém, quando a simplificação remove API redundante, os testes devem ser atualizados.
4. **Testes seguem o fluxo real.** Quando uma simplificação troca o collaborator principal, os testes precisam
   validar o collaborator novo e o comportamento real do branch.
5. **Singleton só quando compartilhado.** Quando a abstração encolhe para uma ou duas funções com um único
   consumidor, voltar para o componente.
6. **View como dona da sincronização.** Quando o composable de fluxo retorna o dado atualizado, a view fica mais
   previsível e os testes deixam de depender de efeito colateral.
7. **Round-trips como critério de design.** Chamadas pequenas demais em sequência têm custo perceptível em Oracle.
   Testes de orçamento de chamadas evitam regressão durante refatorações.
8. **Erro estruturado vs. genérico.** Payload de validação de API deve ser tratado diferente de `Error` genérico:
   o primeiro mapeia campos, o segundo exibe mensagem padrão.
9. **Cache de build.** `compileTestJava` pode falhar por cache; `--no-configuration-cache` antes de tratar como
   regressão.
10. **`@RequestBody(required = false)`.** Não usar `Optional.of(...)` no controller — reintroduz `500` em fluxos
    sem payload.
11. **Storybook acompanha simplificação.** Stories que simulam o desenho antigo deixam documentação executável
    desalinhada.
12. **README como débito técnico.** Documentação de módulo também entra no ciclo de simplificação; README
    desatualizado mascara responsabilidades reais.
