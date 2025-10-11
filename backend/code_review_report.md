# Relatório de Revisão de Código - SGC

**Data da Revisão:** 2025-10-11

## Sumário Executivo
A revisão de código do SGC revelou uma base de código bem documentada e com uma arquitetura desacoplada, especialmente no que diz respeito ao uso de eventos. No entanto, foram identificados problemas críticos de segurança e performance que precisam de atenção imediata. Também foram observadas inconsistências nas convenções do projeto e oportunidades para aplicar melhores práticas de desenvolvimento.

---

## `Questões Críticas`

### 1. (Segurança) CORS Permite Métodos HTTP Perigosos
- **Arquivo:** `sgc/comum/config/ConfigWeb.java`
- **Descrição:** A configuração do CORS (`addCorsMappings`) permite os métodos HTTP `TRACE` e `CONNECT`. O método `TRACE` pode ser usado em ataques de Cross-Site Tracing (XST) para roubar cookies de sessão e informações de autenticação. O método `CONNECT` é projetado para uso com proxies e não deve ser permitido em uma configuração CORS padrão.
- **Impacto:** Alto. Exposição a vulnerabilidades de segurança conhecidas.
- **Recomendação:** Remover `TRACE` e `CONNECT` da lista de `allowedMethods`. Manter apenas os métodos estritamente necessários para a API (ex: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`).

---

## `Alta Prioridade`

### 1. (Performance) Risco de Múltiplas Consultas (N+1) na Validação de Processos
- **Arquivo:** `sgc/processo/ProcessoService.java`
- **Método:** `validarUnidadesComMapasVigentes`
- **Descrição:** O método itera sobre uma lista de códigos de unidade e, dentro do loop, executa duas consultas ao banco de dados (`unidadeRepo.findById` e `unidadeMapaRepo.findByUnidadeCodigo`). Se um processo for iniciado com 100 unidades, isso resultará em 200 consultas individuais ao banco de dados, causando um grande gargalo de performance.
- **Impacto:** Alto. Degradação severa da performance em operações de início de processo com muitas unidades.
- **Recomendação:** Refatorar o método para usar uma única consulta que verifique todas as unidades de uma vez. Por exemplo, buscando todos os `UnidadeMapa` para a lista de unidades e depois processando em memória.

### 2. (Performance) Risco de Múltiplas Consultas (N+1) no Envio de Notificações
- **Arquivo:** `sgc/processo/ProcessoService.java`
- **Método:** `enviarNotificacoesDeFinalizacao`
- **Descrição:** Este método itera sobre os subprocessos e, dentro do loop, faz múltiplas chamadas ao `sgrhService` (`buscarResponsavelUnidade`, `buscarUsuarioPorTitulo`). Embora o `sgrhService` esteja atualmente mockado e com cache, a estrutura do código levará a um problema de N+1 quando conectado a uma fonte de dados real, especialmente se o cache expirar ou for ineficaz para o padrão de uso.
- **Impacto:** Alto. Risco de degradação de performance na finalização de processos.
- **Recomendação:** Implementar métodos no `sgrhService` que aceitem uma lista de IDs (unidades ou títulos) e retornem todos os dados necessários em uma única consulta (padrão "bulk"). Refatorar o `enviarNotificacoesDeFinalizacao` para usar esses novos métodos.

---

## `Média Prioridade`

### 1. (Melhores Práticas) Exposição de Detalhes Internos em Handler de Exceção Genérico
- **Arquivo:** `sgc/comum/erros/RestExceptionHandler.java`
- **Método:** `handleGenericException`
- **Descrição:** O handler de exceção genérico para `Exception.class` formata a mensagem de erro incluindo o nome da classe da exceção (`ex.getClass().getSimpleName()`). Isso expõe detalhes da implementação interna, o que pode ser uma pequena vulnerabilidade de segurança (information leakage).
- **Impacto:** Baixo. Fornece informações que poderiam ser úteis a um atacante.
- **Recomendação:** Remover o nome da classe da exceção da mensagem de erro e usar uma mensagem genérica, como "Ocorreu um erro inesperado. Contate o suporte.". O erro real já é logado no servidor para depuração.

### 2. (Convenções) Inconsistência na Nomenclatura e Idioma
- **Arquivo:** `sgc/processo/ProcessoService.java`
- **Descrição:** O projeto adota o português brasileiro como padrão, mas há inconsistências. Variáveis de dependência são nomeadas em português (`publicadorDeEventos`, `servicoNotificacaoEmail`), mas as classes que elas representam às vezes não seguem o padrão (ex: `NotificacaoEmailService` em vez de `NotificacaoEmailServico`). Além disso, alguns métodos no `ProcessoService` estão em inglês, como `startRevisionProcess`, embora a maioria esteja em português.
- **Impacto:** Baixo. Afeta a legibilidade e a consistência do código.
- **Recomendação:** Renomear classes, métodos e variáveis para seguir estritamente a convenção de usar o português brasileiro. Por exemplo, `NotificacaoEmailService` -> `NotificacaoEmailServico`.

### 3. (Melhores Práticas) Uso de Strings Literais para Perfis de Acesso
- **Arquivo:** `sgc/processo/ProcessoService.java`
- **Método:** `obterDetalhes`
- **Descrição:** O método usa strings literais (`"GESTOR"`, `"ADMIN"`) para verificar perfis de segurança. Isso é propenso a erros de digitação e dificulta a manutenção.
- **Impacto:** Baixo. Reduz a manutenibilidade do código.
- **Recomendação:** Criar um `enum` para os perfis (`PerfilEnum`) e usá-lo nas comparações para garantir consistência e segurança de tipos.

### 4. (Arquitetura) Uso de Cache em Serviço Mockado
- **Arquivo:** `sgc/sgrh/SgrhService.java`
- **Descrição:** A anotação `@Cacheable("sgrh")` foi aplicada em nível de classe a um serviço que retorna dados inteiramente mockados. Isso não tem efeito prático e pode ser enganoso para desenvolvedores que assumam que o cache está funcionando com uma fonte de dados real.
- **Impacto:** Baixo. Código enganoso que não segue a intenção da funcionalidade.
- **Recomendação:** Remover as anotações `@Cacheable` do `SgrhService` enquanto ele estiver em modo mock. As anotações devem ser adicionadas novamente quando o serviço for integrado com a fonte de dados real.

### 5. (Arquitetura) Métodos Transacionais Excessivamente Complexos
- **Arquivo:** `sgc/processo/ProcessoService.java`
- **Métodos:** `iniciarProcessoMapeamento`, `iniciarProcessoRevisao`, `finalizar`
- **Descrição:** Esses métodos são longos e contêm múltiplas responsabilidades (validação, criação de entidades, publicação de eventos). Embora a transacionalidade garanta a atomicidade, a complexidade ciclômatica é alta, dificultando a leitura, o teste e a manutenção.
- **Impacto:** Baixo. Dívida técnica que pode levar a bugs no futuro.
- **Recomendação:** Refatorar esses métodos, extraindo a lógica para métodos privados menores e com responsabilidades únicas.

---

## `Observações Positivas`

- **Documentação:** O projeto possui uma excelente documentação nos arquivos `README.md` dentro dos pacotes. Isso acelera significativamente a compreensão da arquitetura e das responsabilidades de cada módulo.
- **Arquitetura Orientada a Eventos:** O uso do `ApplicationEventPublisher` para desacoplar o módulo `processo` dos módulos `alerta` e `notificacao` é um ponto forte do design, promovendo alta coesão e baixo acoplamento.
- **Tratamento de Exceções:** O uso de exceções de domínio customizadas (`ErroProcesso`, `ErroEntidadeNaoEncontrada`) e um `RestExceptionHandler` centralizado é uma ótima prática que torna o código mais limpo e a API mais robusta.
- **Design de "Snapshot":** A entidade `UnidadeProcesso` como um "snapshot" dos dados no momento da execução do processo é uma solução robusta para garantir a integridade histórica dos dados.