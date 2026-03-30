# Diretrizes Arquiteturais — Backend SGC

Este documento registra decisões e diretrizes concretas que regem a arquitetura do backend. Elas devem ser
consultadas antes de simplificações, refatorações ou criação de novas camadas.

## 1. DTOs

* **DTO não é boilerplate descartável por padrão.**
* DTOs são a fronteira entre a camada de domínio/persistência e a API REST.
* A presença de DTOs garante:
  * contrato estável consumido pelo frontend;
  * proteção contra lazy loading fora da transação (`LazyInitializationException`);
  * prevenção de serialização acidental de grafos JPA (referências circulares, coleções carregadas por cascata);
  * isolamento entre modelo de domínio e representação pública da API.
* **Entidades JPA nunca devem vazar para a API por conveniência.** Mesmo que o DTO pareça idêntico à entidade,
  ele protege contra acoplamento acidental.
* Antes de remover ou fundir um DTO, verificar **todos** os critérios acima. Em caso de dúvida, preservar o DTO e
  simplificar apenas mapeamento, nome, escopo ou duplicação interna.

## 2. Facades

* **Facades existentes não devem ser removidas de forma mecânica.**
* As facades atuais (`PainelFacade`, `AlertaFacade`, `UsuarioFacade`) concentram regras de visibilidade,
  autenticação, leitura, montagem de resposta e orquestração.
* **Novas facades só devem ser criadas** quando centralizarem:
  * regra transversal clara;
  * orquestração real entre múltiplos serviços;
  * política de acesso ou leitura que não cabe em um único service.
* Métodos que apenas repassam chamada devem ficar na camada já existente (service ou controller), salvo
  justificativa explícita.

## 3. Acesso direto a repositórios

* Controllers **não** devem acessar repositórios diretamente como diretriz geral.
* Acesso direto só é aceitável quando **todas** as condições forem verdadeiras:
  * leitura trivial e isolada;
  * nenhuma regra de negócio envolvida;
  * nenhuma segurança contextual;
  * nenhuma montagem complexa de resposta;
  * o dado não está encapsulado em service ou facade existente.
* Antes de criar um service novo para leitura simples, verificar se a lógica já cabe em uma camada existente.

## 4. Simplificação de serviços

* **Remoção de duplicação antes de fusão.** O melhor candidato inicial não é fusão de serviços, e sim remoção de
  duplicação utilitária (queries repetidas, cálculos redundantes, validações espalhadas).
* **Consolidação incremental.** Cada mudança deve se limitar a uma duplicação clara, evitando mover regras de
  transição, notificação e permissão no mesmo passo.
* **Assinaturas públicas.** Ao simplificar serviços usados por testes, preservar assinaturas públicas ou introduzir
  sobrecargas compatíveis. Porém, quando a simplificação de fato remove API redundante, os testes devem ser
  atualizados para o contrato novo.
* **Acoplamento registrado.** Reutilizar serviço existente é aceitável como etapa intermediária, desde que o
  acoplamento gerado fique registrado e seja reavaliado em rodada futura.
