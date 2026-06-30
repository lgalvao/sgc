# Arquitetura e Padrões de Testes

Este documento centraliza as convenções transversais de testes relacionadas aos casos de uso.

## Objetivo

Os arquivos `cdu-xx.md` continuam sendo a referência funcional principal de cada caso de uso. As regras gerais de
arquitetura, nomenclatura e expectativa de cobertura dos testes ficam aqui, para evitar repetição boilerplate em cada
CDU.

## Camadas de teste

- Testes de integração validam o comportamento de backend associado ao caso de uso, incluindo regras de negócio,
  permissões, persistência e efeitos observáveis na API ou nos fluxos internos expostos.
- Testes E2E validam o comportamento observável do sistema a partir da interface, cobrindo a jornada real do usuário.
- Testes unitários podem complementar a cobertura quando houver lógica local relevante, mas não substituem integração
  nem E2E quando o CDU exigir fluxo completo.

## Paralelismo com os CDUs

- A cobertura de testes deve permanecer alinhada aos casos de uso documentados.
- Quando um CDU exigir implementação relevante no backend e na interface, a expectativa padrão é haver cobertura de
  integração e E2E correspondentes.
- Exceções são permitidas, mas devem ser justificadas no contexto da tarefa ou da revisão, e não embutidas como
  boilerplate dentro de cada `cdu-xx.md`.

## Convenções nominais

- Testes E2E dedicados a um CDU seguem, por padrão, o formato `e2e/cdu-xx.spec.ts`.
- Testes de integração dedicados a um CDU seguem, por padrão, o formato
  `backend/src/test/java/sgc/integracao/CDUXXIntegrationTest.java`.
- Quando um teste cobre vários CDUs ou um recorte transversal, isso deve ser tratado como exceção consciente.

## Fronteira editorial

- O CDU descreve o comportamento do caso de uso.
- Este documento descreve a estratégia e os padrões de teste.
- Regras transversais adicionais de design, acesso e terminologia ficam em documentos centrais de `specs/design/` ou de
  apoio em `specs/`.
