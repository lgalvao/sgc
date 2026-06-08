# Diretrizes de Tratamento de Erros — SGC

> **Escopo:** backend Java 25/Spring Boot 4 · frontend Vue 3.5/TypeScript  
> **Última revisão:** 2026-06-07

---

## Objetivo

Maximizar a centralização do tratamento de erros sem degradar robustez,
semântica nem arquitetura.

A regra central do projeto passa a ser:

- erro de usuário ou de fluxo legítimo deve ser classificado como erro de negócio;
- estado impossível, inconsistência ou falha interna deve morrer como erro interno;
- o sistema não deve tentar “recuperar” bug localmente;
- a centralização deve acontecer por camada, não por um único ponto mágico.

---

## Diretrizes Atuais

### Backend

- **Zero exceções Java padrão lançadas explicitamente** no código de produção.
- Toda exceção lançada pelo SGC deve ser uma classe própria da hierarquia do projeto.
- Estado impossível deve usar **uma família semântica única de erro interno irrecoverável**.
- O backend deve falhar rápido, logar com contexto e responder com **500 uniforme** para falhas internas.
- Erros de validação, acesso, autenticação e não encontrado devem continuar semanticamente separados.
- O enforcement dessa regra deve ser automático; não depender de convenção verbal.

### Frontend

- `normalizarErro()` é o ponto único de normalização estrutural do erro.
- Interceptadores/infra HTTP tratam concern técnicos transversais.
- Composables e views decidem apenas a reação local necessária de UX.
- `app.config.errorHandler` é última linha de defesa, não mecanismo principal para fluxos assíncronos.
- Evitar estado paralelo de erro:
  um fluxo deve preferir um único objeto/estado de retorno em vez de vários `erroX`, `erroY`, `alertaZ`.
- Evitar `catch` espalhado que só repete `notify("Erro ...")`.
- Fallback genérico só é aceitável quando a mensagem normalizada realmente não agrega contexto útil.

### Observabilidade

- Toda falha interna relevante deve sair com contexto suficiente para diagnóstico.
- O usuário não deve receber stacktrace nem mensagem técnica crua.
- O frontend não deve duplicar log/notificação global para o mesmo erro já registrado.

---

## O Que Já Foi Consolidado

- Regra de **zero `throw` explícito de exceções Java padrão** no backend de produção.
- Enforcement arquitetural no backend para impedir regressão dessa regra.
- `RestExceptionHandler` alinhado para não tratar exceções Java padrão remanescentes como erro semântico de negócio.
- Contrato de erro do frontend padronizado em torno de `ErroNormalizado`.
- Redução de compatibilidades artificiais de erro em string.
- Deduplicação de log global HTTP/Vue no frontend.
- Simplificação relevante dos fluxos de erro nas telas de diagnóstico e em partes do cadastro, mapa, painel e relatórios.

---

## O Que Ainda Falta Fazer

### P-1 · Fechar o hotspot principal do frontend

**Arquivo-alvo:** `frontend/src/composables/useCadastroTela.ts`

Pendências:

- reduzir mais o acúmulo de branches de validação e erro local;
- separar ainda melhor validação local, erro de fluxo e notificação de importação;
- diminuir a superfície pública exposta para a view quando houver acoplamento acidental.

### P-2 · Limpar fluxos secundários ainda com branches suspeitos

Arquivos prioritários:

- `frontend/src/composables/usePainelTela.ts`
- `frontend/src/composables/useMapaTela.ts`
- `frontend/src/views/NotificacoesAdminView.vue`

Pendências:

- revisar `catch` de background e fallback de mensagem;
- confirmar se todos os branches restantes são reais ou apenas defensividade sobrando;
- remover tratamento redundante quando a infraestrutura já cobre o caso.

### P-3 · Ratchet por cobertura de branches de erro

- continuar usando cobertura de branches como detector de tratamento morto ou redundante;
- priorizar branches de erro não cobertos em composables/views antes de expandir para refactors cosméticos;
- quando um branch de erro for legítimo, adicionar teste;
- quando for impossível ou redundante, remover ou consolidar.

### P-4 · Smoke de comportamento geral após refatorações grandes

- rodar captura E2E ou suíte equivalente sempre que houver rodada estrutural grande em tratamento de erro;
- verificar não só quebra funcional, mas também se estão aparecendo alertas, toasts, warnings ou erros inesperados em fluxo nominal.

---

## Critério de Encerramento

O plano pode ser considerado maduro quando:

- backend estiver protegido por regra automática contra regressão para exceções Java padrão;
- hotspots restantes do frontend estiverem reduzidos a branches justificáveis e cobertos;
- o fluxo nominal do sistema não apresentar “pipoco” de erro/warning após rodadas grandes;
- o relatório deixar de ser inventário de débito e passar a ser apenas política arquitetural curta.
