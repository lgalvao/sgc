# Plano de Melhorias - Suíte E2E SGC (Próximos Passos)

Após a estabilização inicial e limpeza técnica (remoção de variáveis órfãs e correção do isolamento do banco), os seguintes marcos arquiteturais permanecem como oportunidades de otimização:

---

## 1. Implementação de Global Setup para Login (storageState)

### Contexto e Oportunidade
Atualmente, a suíte realiza o login via API (`LoginFacade.entrar`) repetidamente para cada persona em quase todos os testes. Embora rápido, o acúmulo de centenas de autenticações gera um overhead desnecessário de tempo e requisições ao backend.

### Como Implementar
Aproveitar o recurso nativo de **`storageState` do Playwright**:
1. **Global Setup**: Criar um script que executa uma única vez antes da suíte começar, realiza o login com as personas principais (`ADMIN`, `GESTOR`, `CHEFE`) e salva os estados (cookies/tokens) em arquivos JSON individuais.
2. **Fixtures Autenticadas**: Criar fixtures customizadas (ex: `pageAsAdmin`, `pageAsGestor`) que já iniciam o contexto do navegador carregando o respectivo arquivo de estado.
3. **Resultado**: Os testes iniciam instantaneamente na página desejada já autenticados, reduzindo o tempo total da suíte em até 20%.

---

## 2. State-Jumping via API (Desacoplamento de Fluxos Seriais)

### Contexto e Oportunidade
Muitos CDUs (como `CDU-05`, `CDU-12`, `CDU-16`) executam jornadas longas e sequenciais (`.serial`), onde o Teste 3 depende do sucesso do Teste 2. Se um passo intermediário falha por instabilidade de rede ou UI, os passos seguintes nem são executados.

### Como Implementar
Migrar para uma estratégia de **Salto de Estado**:
1. **Fixtures de Backend**: Em vez de navegar pela UI para "preparar" um processo até a fase de homologação, utilizar chamadas diretas de API no `beforeEach` para injetar o processo já no estado desejado.
2. **Foco no Teste**: O teste de UI deve focar exclusivamente na funcionalidade alvo daquele passo (ex: testar apenas a tela de Homologação, assumindo que o processo já "nasceu" pronto para ser homologado).
3. **Resultado**: Testes mais curtos, independentes, paralelizáveis e muito menos suscetíveis a falhas em cascata (*Flakiness*).

---

## Histórico de Conclusões
- [X] **Estratégia de Reset Híbrido**: Implementado `resetDatabase` automático por teste (normal) e por arquivo (serial).
- [X] **Limpeza Técnica**: Removidas variáveis `processoId` e imports de `cleanupAutomatico` em toda a suíte.
- [X] **Saneamento do Linter**: Suíte com zero avisos/erros de ESLint.
