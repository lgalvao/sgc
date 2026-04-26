# Plano de Refatoração Estrutural e UX

## Objetivo
Consolidar a arquitetura do SGC em um modelo de alto desempenho, plenamente acessível e tecnicamente sustentável. O foco é eliminar complexidade desnecessária (God Classes, código defensivo) e garantir uma experiência de usuário fluida e resiliente.

---

## 1. Diretrizes de UX e Acessibilidade (Mandatário)

### Visibilidade e Proximidade
- **Ações Permanentes**: Vedado o uso de 'hover' para ocultar botões de ação (editar, remover). As ações devem estar visíveis e operáveis via teclado/touch.
- **Domínio Inline**: Informações fundamentais (conhecimentos) devem ser exibidas no corpo da página, eliminando tooltips/popovers de domínio.
- **Proximidade**: Controles de ação devem estar posicionados o mais próximo possível do item que afetam.

### Resiliência Visual
- Suporte a descrições longas sem quebra de layout (`overflow-wrap: anywhere`).
- Uso sistemático de componentes da biblioteca `BootstrapVueNext` para consistência responsiva.

---

## 2. Engenharia e Arquitetura

### Combate a God Classes (Backend)
- **Desmembrar classes massivas com base no estado atual do código**:
    - `ProcessoService.java` (~1300 linhas): segue como principal God Class ativa; priorizar isolamento de criação, ações em bloco, workflow e finalização.
    - `SubprocessoTransicaoService.java` (~580 linhas): permanece como hotspot de workflow; isolar lógicas de movimentação, validação de etapa e comunicação.
    - `SubprocessoConsultaService.java` deixou de ser o principal gargalo estrutural: o cálculo de contexto, permissões e visualização já foi extraído para `SubprocessoContextoConsultaService`, `SubprocessoAcessoService` e `SubprocessoVisualizacaoService`. A próxima etapa aqui é consolidar a fachada remanescente e remover acoplamentos legados.

### Código Não-Defensivo
- **Limpeza Sistemática**: o backend ainda concentra alto volume de nulabilidade explícita (baseline atual: ~285 usos de `@Nullable`), mas a remoção deve começar por DTOs e fronteiras com contrato já estabilizado.
- Reduzir verificações de nulidade e validações redundantes que não agregam valor em ambiente controlado, sem romper fluxos que ainda dependem de ausência explícita de dados.
- Confiar em tipos fortes e contratos estáveis entre frontend e backend apenas onde a estabilidade já estiver comprovada por uso e testes.

### Racionalização de Contexto e Desempenho (Frontend)
- **Fonte Única de Verdade do Subprocesso**:
    - A consolidação agora está centrada em `stores/subprocesso`: `SubprocessoView`, `CadastroView`, `MapaView`, `useFluxoSubprocesso` e `useInvalidacaoNavegacao` já foram migrados para `useSubprocessoStore`.
    - `useSubprocessos` deixou de ser dependência de runtime e sobrevive apenas em testes antigos; o próximo passo é removê-lo por completo, sem alias e sem camada de compatibilidade.
- **Eliminação de Chamadas Redundantes**:
    - A base de *deduplication* e cache curto já foi consolidada no store de subprocesso; a próxima etapa é auditar onde ainda há recarga forçada, recálculo duplicado de contexto e invalidação ampla após mutações.
    - Conectar o carregamento de subprocesso a cancelamento escopado por rota/fluxo. A infraestrutura global de `AbortController` já existe no `axios-setup`, mas ainda não está alinhada de ponta a ponta com os fluxos de `SubprocessoView`, `CadastroView` e `MapaView`.
- **Gestão Eficiente de Cache**:
    - Preservar o cache de curto prazo para dados estáveis do subprocesso dentro da mesma ativação de rota.
    - Trocar invalidação ampla por invalidação seletiva: hoje a navegação ainda zera stores de painel/processo/subprocesso em diversos fluxos onde bastaria atualizar o recurso afetado.
- **Quebra de Views Gigantes**:
    - `CadastroView.vue` e `MapaView.vue` continuam grandes, embora já usem composables, componentes dedicados e *lazy loading* de modais.
    - O próximo corte deve extrair o bootstrap de contexto e reduzir branches locais de erro/modais agora que o store já absorveu o estado global de subprocesso.


---

## 3. Qualidade e Validação

### E2E Semântico e Confiável
- Validar capacidades operacionais (dados e ações) em vez de rotas específicas.
- **Proibido: Resiliência Artificial**: Nunca usar `if/or` em asserções para forçar a passagem de testes instáveis.
- Comunicação Direta: Alertas e notificações devem seguir estritamente a hierarquia imediata (destino + superior imediato).

### Tipagem e Cobertura
- O uso de `any` em runtime do frontend está praticamente eliminado; os remanescentes concentram-se majoritariamente em infraestrutura de teste, roteamento testado por lazy import e helpers de E2E.
- Eliminar `any` remanescentes primeiro em helpers reutilizados de teste antes de atacar casos isolados de spec.
- A rodada atual já recolocou `npm run typecheck` e o lint dos arquivos migrados em estado verde após alinhar o contrato novo de `stores/subprocesso` com os testes.
- Manter cobertura de branches em níveis de excelência, focando em lógica de negócio complexa.
