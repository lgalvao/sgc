# Recomendações de Arquitetura e Boas Práticas para o Frontend do SGC

## 1. Uso Mínimo de JavaScript

- Utilize JavaScript apenas quando for absolutamente inevitável para a experiência do usuário.
- Prefira soluções baseadas em HTML e Thymeleaf sempre que possível.
- Quando necessário, mantenha o código JS simples, focado e bem documentado.
- Justificativa: Facilita a manutenção, reduz bugs e melhora a previsibilidade do sistema.

## 2. Priorizar Thymeleaf e HTML Puro

- Toda a lógica de interface, exibição de dados e navegação deve ser feita com Thymeleaf e HTML puro.
- Evite lógica de manipulação de DOM ou dados no frontend; centralize a lógica no backend.
- Justificativa: Garante maior integração com o backend Spring, facilita testes e manutenção.

## 3. Não Utilizar AJAX nem REST para Atualização de Dados na Tela

- Todas as interações devem ser feitas via formulários tradicionais, com recarregamento completo da página.
- Não utilize chamadas assíncronas (AJAX) nem APIs REST para atualizar partes da interface.
- Justificativa: Simplifica o fluxo, reduz dependências e evita inconsistências de estado.

## 4. Aproveitar ao Máximo o Bootstrap

- Utilize as classes utilitárias e componentes do Bootstrap para layout, espaçamento, feedback visual e responsividade.
- Evite criar estilos CSS customizados, exceto quando absolutamente necessário.
- Justificativa: Garante padronização visual, responsividade e reduz o esforço de manutenção de CSS.

## 5. Garantir Acessibilidade e Clareza

- Sempre utilize labels, placeholders e atributos de acessibilidade nos elementos de formulário.
- Forneça feedback visual claro para campos obrigatórios e mensagens de erro.
- Justificativa: Melhora a experiência do usuário e garante conformidade com boas práticas de acessibilidade.

## 6. Justificativas Gerais

- Essas recomendações visam manter o sistema simples, previsível, fácil de manter e seguro.
- Reduzem a complexidade do frontend, centralizando a lógica no backend, onde há maior controle e testabilidade.
- Facilitam a integração contínua, testes automatizados e onboarding de novos desenvolvedores.

---

Este documento deve ser seguido por todos os desenvolvedores do SGC ao implementar ou evoluir funcionalidades no
frontend.
