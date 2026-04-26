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
- **Desmembrar classes massivas**:
    - `ProcessoService.java` (~1300 linhas): Isolar responsabilidades de criação, workflow e finalização.
    - `SubprocessoConsultaService.java` (~600 linhas): Extrair cálculo de permissões e montagem de contexto.
    - `SubprocessoTransicaoService.java` (~600 linhas): Isolar lógicas de movimentação específicas.

### Código Não-Defensivo
- **Limpeza Sistemática**: Atacar os hotspots identificados (>270 `@Nullable` e >100 checks manuais de `null`).
- Remover verificações de nulidade e validações redundantes que não agregam valor em ambiente controlado.
- Confiar em tipos fortes e contratos estáveis entre frontend e backend.

### Racionalização de Contexto e Desempenho (Frontend)
- **Eliminação de Chamadas Redundantes**:
    - Implementar mecanismos de *deduplication* e *cancelamento de requisições sobrepostas* para evitar sobrecarga na rede e no backend.
    - Otimizar `useSubprocessos` e stores para garantir que a navegação intrafolha não force a reconstrução total do contexto via API desnecessariamente.
- **Gestão Eficiente de Cache**:
    - Adotar cache de curto prazo para dados estáveis do subprocesso dentro da mesma ativação de rota.
    - Garantir que a invalidação de cache ocorra apenas em eventos de mutação real (transições de workflow).
- **Quebra de Views Gigantes**:
    - `CadastroView.vue` (~960 linhas) e `MapaView.vue` (~890 linhas): Extrair lógica de orquestração de modais e manipulação de estado complexo para composables dedicados ou componentes menores.


---

## 3. Qualidade e Validação

### E2E Semântico e Confiável
- Validar capacidades operacionais (dados e ações) em vez de rotas específicas.
- **Proibido: Resiliência Artificial**: Nunca usar `if/or` em asserções para forçar a passagem de testes instáveis.
- Comunicação Direta: Alertas e notificações devem seguir estritamente a hierarquia imediata (destino + superior imediato).

### Tipagem e Cobertura
- Eliminar o uso de `any` em testes e componentes.
- Manter cobertura de branches em níveis de excelência, focando em lógica de negócio complexa.
