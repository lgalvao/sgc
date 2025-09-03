## Alinhamento de especificações com protótipo

Analise as especificações em @/reqs/cdu-21.md e verifique o que falta para a implementação atender a essas especificações. Desconsideere o teste e2e relacionado: ele ainda está em evolução. Como refs leia @regras-projeto e @regras-perfis.

## Revisão dos teste e2e

Reveja o teste @spec/cdu/cdu-21.spec.ts para que fique alinhado à especificação @reqs/cdu-21.md. Leve em conta o arquivo @licoes-aprendidas.md

## Correcao de testes e2e
Estou corrigindo os testes e2e baseados nas especificações. Eles estão em @spec/cdu e foram criados com base nos arquivos correspondentes em @reqs/, que são as especificações de casos de uso. A questão é que as especificações não batem exatamente com o protótipo. O protótipo precisa ser atualizado para que o sistema se comporte como na especificação.

Vamos ao @/reqs/cdu-21.md. Leia o arquivo e rode o teste criado com base nele @/spec/cdu/cdu-21.spec.ts e verifique porque não está passando. Faça as correções.
O servidor já está rodando. Não tente rodar outro!

Pode adicionar novos dados nos mocks e novos test-ids. Uma ressalva importante: ao alterar dados nos mocks, considere criar novos dados, em vez de alterar dados existentes, a não ser que tenha certeza de que as mudanças não vão quebrar testes existentes.

Uma ponto crucial nesses testes é a estrutura dos mocks: leia os arquivos de @mocks para entender como as unidades e processos se relacionam. @/src/types/tipos.ts também é importante.

Como refs leia @regras-projeto, @regras-playwright e @regras-perfis.