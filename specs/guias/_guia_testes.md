# Arquitetura e Padrões de Testes

Este documento centraliza as convenções de testes e2e e de integração (backend), os quais são baseados nas
especificações de casos de uso.

## Objetivo

As regras gerais de arquitetura dos testes, nomenclatura e expectativa de cobertura são espefificadas neste arquivo. Os
arquivos `cdu-xx.md` permanecem como referência funcional.

## Camadas de teste

- **Testes de integração** - validam o comportamento de backend associado ao caso de uso, incluindo regras de negócio,
  permissões, persistência e efeitos observáveis na API ou nos fluxos internos expostos.
- **Testes E2E** - validam o comportamento observável do sistema a partir da interface gráfica, cobrindo as atividades
  reais dos usuários.
- Testes unitários complementam a cobertura quando houver lógica local relevante, mas não substituem os testes de integração
  nem os de E2E, quando o objetivo é validar as especificações completas dos CDUs.

## Alinhamento com os CDUs

- Os testes de integração e E2E devem permanecer alinhados aos casos de uso documentados.

## Convenções de nomenclatura

- Testes E2E seguem o formato `cdu-xx.spec.ts` e são localizados no diretório `e2e`, na raiz do projeto.
- Testes de integração seguem o formato `CDUXXIntegrationTest.java` e são localizados no pacote de testes
  `sgc.integration`.