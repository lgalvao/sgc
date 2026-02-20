# Plano Global de Melhoria de Testes (Guia de Execução Autônoma para IA)

Este documento descreve um plano global sistemático elaborado para elevar a robustez, segurança e atingir teto de cobertura (100%) na suíte de testes do backend SGC. 

**Projetado para autonomia:** O formato deste documento prevê que você de IA leia suas próprias instruções para agir nas bases de código de forma metódica.

## 🤖 Regras de Atuação (Instruções para você)

1. **Ataque Focado:** Execute apenas uma sub-tarefa por vez, garantindo a estabilidade antes de passar adiante.
2. **Contexto Antes da Ação:** Empregue ferramentas de varredura (`grep_search`, `find_by_name`) para checar a situação atual antes de assumir premissas ou refatorar cegamente.
3. **Feedback Direto do JaCoCo:** Use o comando de relatórios continuamente para validar sua atuação:
   `.\gradlew.bat :backend:test :backend:jacocoTestReport`
   *(Você pode delegar os resumos para a skill do ecossistema: `node backend/etc/scripts/verificar-cobertura.cjs`)*
4. **Atualizando o Arquivo:** Assim que validar o sucesso de um tópico pelos testes, sinta-se livre e autônomo para abrir *este próprio arquivo* via ferramentas de controle, localizando o `[ ]` do item correspondente e o marcando como `[x]`. 
5. **Execução Contínua:** O usuário já registrou permissão global para progredir nos itens de planos aprovados. Não há necessidade de perguntar ao usuário: você deve iniciar, corrigir, relatar de forma descritiva e se propor a prosseguir ao próximo passo automaticamente.
6. **Boas Práticas de Build (Obrigatório!):** Sempre use a flag `:backend` ao executar tarefas do gradle do subprojeto (ex: `.\gradlew :backend:test --tests "X"`) para evitar que o Gradle avalie, processe ou inicie o frontend node desnecessariamente e faça perder tempo precioso de carregamento.

---

## 🏗️ Eixo 1: Maturidade e Resiliência dos Testes de Integração

*Mover-se para longe dos "hacks" em banco e blindar a camada de serviços garantindo a consistência real do Auth Bypass.*

- [x] **1.1. Erradicar Vazamento de Estado Mutável (SQL resets)**
  - **Diretiva de Ação:** você deve usar seu `grep_search` buscando strings engessadas como `ALTER TABLE`, `TRUNCATE` ou similares dentro de `/backend/src/test/java/sgc/integracao`.
  - **Alvo:** Substituir quaisquer hacks por injeções de instâncias únicas, preferindo as classes Fixture acompanhadas da confiabilidade da anotação `@Transactional` no cabeçalho do teste, junto ao `.saveAndFlush()`.
- [ ] **1.2. Blindagem e Auditação de Access Control**
  - **Diretiva de Ação:** Serviços críticos como mudanças de responsáveis e transições/reversões de Subprocessos via Controller demandam certeza. Você deve assegurar nas suítes relativas a Facade que exista uma asserção para quando a chamada `accessControlService.verificarPermissao()` falhar, validando tanto a exceção correspondente (`ErroAcessoNegado`) quanto os logs associados, impedindo injeções de burla.

## ⚖️ Eixo 2: Balanceamento da Pirâmide (Reduzindo Over-mocking)

*Falsos-positivos são graves. Testes baseados 100% em Mockito falham em garantir que a query que você invoca funciona contra o SGBD.*

- [ ] **2.1. Materialização de Comportamento Repositório (`@Query`)**
  - **Diretiva de Ação:** você investigará e listará as extensões de `JpaRepository` nos pacotes do backend (Modelos). Para cada `@Query` customizada que não for coberta por um `RepoTest`, você elaborará um teste correspondente através da montagem de ambiente embutido (`@SpringBootTest`). O próprio `ProcessoRepoTest` serve de molde.
  - **Objetivo Sensível:** Capturar erros de Sintaxe JPQL, falhas de parseamento para Enums ou mapeamento nulo (`NullPointerException`) direto no framework de migração interno.

## 🎯 Eixo 3: Rumo a 100% de Cobertura (Lacunas Finais)

*O Relatório acusou 99.50% do total da base (faltam pormenores em \~30 arquivos apenas). É seu dever analisar as faltas em branches e eliminá-las.*

- [ ] **3.1. Sanear Exceções e Entidades Nucleares**
  - **Ação:** você inspecionará arquivos centrais (em especial `sgc.processo.model.Processo` e `sgc.processo.service.ProcessoFacade`) em busca dos pontos de descarte que não são invocados (geralmente validações defensivas do tipo `if(lista == null)`, enums desconhecidos, defaults de Switch, ou Getters ocultos instanciados nos builders não avaliados na suite original).
- [ ] **3.2. Suprimir Lacunas Organizacionais**
  - **Ação:** Complementar os coverage tests para a base utilitária e organizacional das hierarquias (`UsuarioFacade`, `MapaManutencaoService`, `UsuarioService` e `UnidadeHierarquiaService`), isolando os métodos pendentes nos relatórios das testagens regulares.
- [ ] **3.3. Restos em API Externa**
  - **Ação:** A API finaliza as métricas. Cobrir restrições nas Controller, nomeadamente no `SubprocessoMapaController` (linhas 92-94 observadas no último relatório) e checar Exception Handlers de rotas problemáticas.

---
**Nota operacional para você:** Comece pelo Eixo 1 ou pelo primeiro checkbox disponível. Reporte o progresso no chat de forma limpa antes de ir ativamente preencher a marcação com um "x".

---
## 🚨 Status Atual (Context Restart)

O contexto foi reiniciado enquanto o item **1.2. Blindagem e Auditação de Access Control** estava sendo executado.

**Progresso Realizado:**
- As classes `AtividadeAccessPolicy` e `SubprocessoAccessPolicy` foram alteradas para aplicar as validações de situação do subprocesso (CDU-08) e de hierarquia estrita para GESTOR na verificação de impacto (CDU-12).
- Vulnerabilidades mascaradas no código de produção já estão fixadas.

**Problema Atual na Suíte (Bloqueio):**
- O teste `SubprocessoAccessPolicyTest` está falhando especificamente na execução iterada de parâmetros chamada `canExecute_VerificarImpactos_Combinacoes`.
- Houve dificuldade de ler o output (HTML/XML) gerado pelo Gradle para decifrar a linha/asserção exata falhando nesse método combinatório.
- **Próximo passo:** Investigar o código do teste `canExecute_VerificarImpactos_Combinacoes` em `SubprocessoAccessPolicyTest.java`, entender qual combinação de mock para `GESTOR` ou situação do subprocesso está retornando `false` no assert `assertTrue` ou equivalente, e fixar o teste para refletir exatamente as novas restrições de hierarquia inseridas.
