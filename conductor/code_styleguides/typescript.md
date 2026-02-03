# Guia de Estilo: TypeScript & Vue (Frontend SGC)

## Princípios Gerais
- **Idioma:** Português Brasileiro (pt-BR) para toda a lógica de negócio e interface.
- **Composition API:** Utilize obrigatoriamente `<script setup>` com TypeScript.
- **Reatividade:** Priorize o uso de `ref` e `computed`.

## Nomenclatura
- **Componentes:** PascalCase (ex: `ListaAtividades.vue`).
- **Views:** PascalCase com sufixo `View` (ex: `MapeamentoView.vue`).
- **Stores:** camelCase (ex: `useProcessosStore`).
- **Types/Interfaces:** PascalCase (ex: `Competencia`).

## Estrutura e Organização
- **Views vs Components:** Views gerenciam dados (inteligentes); Components são puramente visuais (burros).
- **Mappers:** Sempre utilize mappers para isolar o frontend dos DTOs do backend.
- **Services:** Isole as chamadas Axios em arquivos de serviço por domínio.

## Testes
- **Vitest:** Utilize Vitest para testes unitários.
- **Acessibilidade:** Garanta conformidade com WCAG 2.2 em todos os componentes.
