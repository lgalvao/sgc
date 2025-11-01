# Plano de Testes E2E com Preparação de Dados

Este documento descreve o plano para a criação de uma nova suíte de testes end-to-end (E2E) que utiliza preparação de dados via API, em vez de depender de um estado inicial do banco de dados.

## Objetivo

O objetivo principal é refatorar os testes E2E existentes para que cada arquivo de teste seja auto-suficiente, criando todos os dados necessários para sua execução e realizando a limpeza ao final. Isso tornará os testes mais robustos, independentes e permitirá a execução paralela.

## Acompanhamento

| CDU | Arquivo Original | Novo Arquivo | Situação | Observações |
| --- | --- | --- | --- | --- |
| 01 | `cdu-01.spec.ts` | `cdu-01-prep.spec.ts` | Analisado | **Cobertura Parcial.** Faltam testes para falha de login e verificação detalhada da UI. |
| 02 | `cdu-02.spec.ts` | `cdu-02-prep.spec.ts` | Analisado | **Cobertura Parcial.** Faltam testes de ordenação, navegação para a maioria dos perfis e funcionalidade de alertas. |
| 03 | `cdu-03.spec.ts` | `cdu-03-prep.spec.ts` | Analisado | **Cobertura Parcial.** Faltam testes para a lógica de seleção em árvore e validações específicas de 'Revisão'/'Diagnóstico'. |
| 04 | `cdu-04.spec.ts` | `cdu-04-prep.spec.ts` | Analisado | **Cobertura Baixa.** Teste crítico que não verifica nenhum resultado da ação no backend (criação de subprocessos, alertas, etc.). |
| 05 | `cdu-05.spec.ts` | `cdu-05-prep.spec.ts` | Analisado | **Cobertura Parcial.** Falha em verificar a cópia do mapa de competências, que é o requisito principal deste CDU. |
| 06 | `cdu-06.spec.ts` | `cdu-06-prep.spec.ts` | Analisado | **Cobertura Baixa.** Não verifica a visibilidade condicional de botões e funcionalidades por perfil (`ADMIN` vs. `GESTOR`). |
| 07 | `cdu-07.spec.ts` | `cdu-07-prep.spec.ts` | Analisado | **Cobertura Média.** Boa cobertura de fluxo, mas faltam testes para o perfil `SERVIDOR` e o tipo de processo 'Diagnóstico'. |
| 08 | `cdu-08.spec.ts` | `cdu-08-prep.spec.ts` | Analisado | **Cobertura Média.** Faltam testes para a funcionalidade de "Importar atividades" e a mudança de status do subprocesso. |
| 09 | `cdu-09.spec.ts` | `cdu-09-prep.spec.ts` | Analisado | **Cobertura Média.** Não verifica o modal de confirmação nem os efeitos da ação no backend (status, `Movimentacao`, etc.). |
| 10 | `cdu-10.spec.ts` | `cdu-10-prep.spec.ts` | Analisado | **Cobertura Média.** Similar ao CDU-09, não verifica o modal de confirmação nem os efeitos da ação no backend. |
| 11 | `cdu-11.spec.ts` | `cdu-11-prep.spec.ts` | Analisado | **Cobertura Média.** Faltam testes de visualização para os perfis `CHEFE` e `SERVIDOR`. |
| 12 | `cdu-12.spec.ts` | `cdu-12-prep.spec.ts` | Analisado | **Cobertura Baixa.** Falha crítica em não verificar o conteúdo do modal de impactos, que é o requisito principal. |
| 13 | `cdu-13.spec.ts` | `cdu-13-prep.spec.ts` | Analisado | **Cobertura Média.** Cobre bem a UI, mas falha em verificar qualquer um dos efeitos da ação no backend. |
| 14 | `cdu-14.spec.ts` | `cdu-14-prep.spec.ts` | Analisado | **Cobertura Média.** Cobre a UI, mas não verifica o backend nem a lógica condicional da homologação pelo `ADMIN`. |
| 15 | `cdu-15.spec.ts` | `cdu-15-prep.spec.ts` | Analisado | **Cobertura Alta.** Ótima cobertura, faltando apenas a verificação da mudança de status do subprocesso. |
| 16 | `cdu-16.spec.ts` | `cdu-16-prep.spec.ts` | Analisado | **Cobertura Alta.** Ótima cobertura, faltando apenas a verificação da funcionalidade "Impactos no mapa". |
| 17 | `cdu-17.spec.ts` | `cdu-17-prep.spec.ts` | Analisado | **Cobertura Média.** Cobre a UI, mas falha em testar as validações do backend e não verifica nenhum dos seus efeitos. |
| 18 | `cdu-18.spec.ts` | `cdu-18-prep.spec.ts` | Analisado | **Cobertura Alta.** Excelente cobertura, faltando apenas um teste específico para o perfil `GESTOR`. |
| 19 | `cdu-19.spec.ts` | `cdu-19-prep.spec.ts` | Analisado | **Cobertura Baixa.** Não verifica o modal de confirmação nem os efeitos da ação no backend para nenhum dos dois fluxos. |
| 20 | `cdu-20.spec.ts` | `cdu-20-prep.spec.ts` | Analisado | **Cobertura Média.** Cobre a UI, mas não verifica nenhum dos efeitos da ação no backend. |
| 21 | `cdu-21.spec.ts` | `cdu-21-prep.spec.ts` | Analisado | **Cobertura Baixa.** Falha crítica em não testar a regra de validação do backend (todos os subprocessos devem estar homologados). |

## Lições Aprendidas

- A suíte de testes E2E refatorada (`-prep.spec.ts`) foca muito em testar o "caminho feliz" da interface do usuário (UI).
- Há uma carência significativa na verificação dos efeitos colaterais das ações no backend, como:
  - Mudanças de status de processos e subprocessos.
  - Criação de registros de movimentação e análise.
  - Geração de alertas e envio de e-mails.
- Validações de backend críticas e lógicas condicionais complexas (especialmente para o perfil `ADMIN`) não estão sendo adequadamente testadas.
- A cobertura de perfis de usuário é inconsistente; alguns CDUs testam múltiplos perfis, enquanto outros focam em apenas um, deixando os outros de fora.
- A estratégia de usar a API para verificar o estado do backend (como visto no CDU-05) é um bom padrão que deveria ser aplicado de forma mais consistente em toda a suíte de testes.
