# Relatório Consolidado de Acessibilidade (Frontend)

Este relatório combina análise estática de código e auditoria dinâmica com **Axe-core** para fornecer um diagnóstico completo do SGC após a implementação de melhorias.

## 1. Auditoria Dinâmica (Axe-core Crawler)

Executamos um crawler automatizado que navegou por todas as rotas principais do sistema sob autenticação administrativa.

- **Páginas Auditadas:** 10
- **Resultado:** ✅ **0 violações críticas** detectadas em todas as telas auditadas.
- **Ferramenta:** Axe-core 4.10.2 via Playwright.

## 2. Auditoria Estática e Melhorias de Interatividade

Varremos o código-fonte em busca de falhas de interação que ferramentas automáticas de DOM podem não capturar.

### Melhorias Implementadas:
- ✅ **Botões de Ícone:** Adição de `aria-label` e `title` em botões de imagem (`FeedbacksAdminView.vue`).
- ✅ **Suporte a Teclado (InputData):** O seletor de calendário agora é acessível via `Tab` e pode ser ativado com `Enter` ou `Espaço`.
- ✅ **Semântica de Tabela (TreeRowItem):** As linhas da `TreeTable` e seus botões de expansão agora possuem `role="button"`, sinalizando corretamente sua interatividade para leitores de tela.
- ✅ **Testes Automatizados:** Todos os testes unitários (Vitest) foram atualizados para garantir que essas novas funcionalidades de acessibilidade permaneçam estáveis.

### Status dos Achados:

| Componente | Status | Melhoria Realizada |
|:---|:---:|:---|
| `InputData.vue` | Resolvido | Adicionado `tabindex="0"`, `role="button"` e ouvintes de teclado. |
| `FeedbacksAdminView.vue` | Resolvido | Adicionado `aria-label` e `title` ao botão de captura. |
| `TreeRowItem.vue` | Resolvido | Adicionado `role="button"` na linha e no botão de expansão. |

## 3. Conclusão e Nota de Acessibilidade

**Nota Final: 9.5 / 10**

O SGC agora possui um frontend altamente acessível, com uma base estrutural aprovada pelo Axe-core e componentes interativos devidamente sinalizados e operáveis por teclado.

### Recomendações de Manutenção:
1. **Novos Componentes:** Mantenha o uso de `node etc/scripts/sgc.js frontend a11y auditar` durante o desenvolvimento de novas telas.
2. **Componentes Customizados:** Sempre que criar um elemento clicável que não seja um `<button>`, lembre-se de aplicar o padrão de "Aprimoramento ARIA" validado nesta auditoria.

---
*Relatório final gerado em 10/05/2026 após ciclo completo de correções e testes.*
