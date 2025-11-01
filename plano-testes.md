# Plano de Testes E2E com Preparação de Dados

Este documento descreve o plano para a criação de uma nova suíte de testes end-to-end (E2E) que utiliza preparação de dados via API, em vez de depender de um estado inicial do banco de dados.

## Objetivo

O objetivo principal é refatorar os testes E2E existentes para que cada arquivo de teste seja auto-suficiente, criando todos os dados necessários para sua execução e realizando a limpeza ao final. Isso tornará os testes mais robustos, independentes e permitirá a execução paralela.

## Plano de Ação

O trabalho será dividido nas seguintes etapas, para cada caso de uso (CDU):

1.  **Análise do Teste Existente**: Analisar o arquivo `cdu-XX.spec.ts` para entender os fluxos de teste e os dados necessários.
2.  **Criação do Novo Arquivo de Teste**: Criar uma cópia do arquivo de teste original com o sufixo `-prep.ts` (ex: `cdu-XX-prep.spec.ts`).
3.  **Implementação da Preparação de Dados**: No início do novo arquivo de teste, adicionar a lógica para criar os dados necessários para o teste, utilizando a API da aplicação.
4.  **Implementação da Limpeza de Dados**: Ao final do novo arquivo de teste, adicionar a lógica para remover os dados criados durante a preparação.
5.  **Adaptação dos Testes**: Adaptar os testes existentes no novo arquivo para utilizar os dados criados dinamicamente.
6.  **Execução e Validação**: Executar o novo arquivo de teste para garantir que ele passa e que a preparação e limpeza de dados estão funcionando corretamente.

## Acompanhamento

| CDU | Arquivo Original | Novo Arquivo | Situação | Observações |
| --- | --- | --- | --- | --- |
| 01 | `cdu-01.spec.ts` | `cdu-01-prep.spec.ts` | A fazer | |
| 02 | `cdu-02.spec.ts` | `cdu-02-prep.spec.ts` | A fazer | |
| 03 | `cdu-03.spec.ts` | `cdu-03-prep.spec.ts` | A fazer | |
| 04 | `cdu-04.spec.ts` | `cdu-04-prep.spec.ts` | A fazer | |
| 05 | `cdu-05.spec.ts` | `cdu-05-prep.spec.ts` | A fazer | |
| 06 | `cdu-06.spec.ts` | `cdu-06-prep.spec.ts` | A fazer | |
| 07 | `cdu-07.spec.ts` | `cdu-07-prep.spec.ts` | A fazer | |
| 08 | `cdu-08.spec.ts` | `cdu-08-prep.spec.ts` | A fazer | |
| 09 | `cdu-09.spec.ts` | `cdu-09-prep.spec.ts` | A fazer | |
| 10 | `cdu-10.spec.ts` | `cdu-10-prep.spec.ts` | A fazer | |
| 11 | `cdu-11.spec.ts` | `cdu-11-prep.spec.ts` | A fazer | |
| 12 | `cdu-12.spec.ts` | `cdu-12-prep.spec.ts` | A fazer | |
| 13 | `cdu-13.spec.ts` | `cdu-13-prep.spec.ts` | A fazer | |
| 14 | `cdu-14.spec.ts` | `cdu-14-prep.spec.ts` | A fazer | |
| 15 | `cdu-15.spec.ts` | `cdu-15-prep.spec.ts` | A fazer | |
| 16 | `cdu-16.spec.ts` | `cdu-16-prep.spec.ts` | A fazer | |
| 17 | `cdu-17.spec.ts` | `cdu-17-prep.spec.ts` | A fazer | |
| 18 | `cdu-18.spec.ts` | `cdu-18-prep.spec.ts` | A fazer | |
| 19 | `cdu-19.spec.ts` | `cdu-19-prep.spec.ts` | A fazer | |
| 20 | `cdu-20.spec.ts` | `cdu-20-prep.spec.ts` | A fazer | |
| 21 | `cdu-21.spec.ts` | `cdu-21-prep.spec.ts` | A fazer | |

## Lições Aprendidas

_(Esta seção será preenchida durante o desenvolvimento)_
