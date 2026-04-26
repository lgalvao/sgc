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
- Desmembrar classes com múltiplas responsabilidades (Hotspots: `ProcessoService` e `SubprocessoConsultaService`).
- Isolar lógica de permissões, consultas de domínio e persistência em serviços coesos.

### Código Não-Defensivo
- Remover verificações de nulidade e validações redundantes que não agregam valor em ambiente controlado.
- Confiar em tipos fortes e contratos estáveis entre frontend e backend.

### Racionalização de Contexto (Frontend)
- Gerenciar o estado do subprocesso ativamente para evitar recomposições redundantes de contexto em transições de tela.
- Minimizar round-trips à API sem ganho funcional material.

---

## 3. Qualidade e Validação

### E2E Semântico e Confiável
- Validar capacidades operacionais (dados e ações) em vez de rotas específicas.
- **Proibido: Resiliência Artificial**: Nunca usar `if/or` em asserções para forçar a passagem de testes instáveis.
- Comunicação Direta: Alertas e notificações devem seguir estritamente a hierarquia imediata (destino + superior imediato).

### Tipagem e Cobertura
- Eliminar o uso de `any` em testes e componentes.
- Manter cobertura de branches em níveis de excelência, focando em lógica de negócio complexa.
