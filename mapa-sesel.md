# Mapa de Competências Técnicas - Unidade SESEL (Simulado)

Este documento serve como especificação do exemplo completo que será materializado por um script SQL para
homologação. A base do cenário é a unidade **SESEL (Seção de Sistemas Eleitorais)** e o objetivo é ter um mapa
integro do começo ao fim, com os registros de apoio necessários para navegação, histórico e notificações.

## Objetivo do script

Criar um conjunto de dados consistente para demonstrar o ciclo completo de um mapa:

* processo criado e vinculado à unidade;
* subprocesso em estágio compatível com o mapa exibido;
* mapa com competências, atividades e conhecimentos;
* movimentações que representem a evolução do fluxo;
* alertas relacionados ao processo e aos destinatários corretos;
* notificações de e-mail coerentes com o estado do subprocesso;
* vínculos de responsabilidade, perfis e unidade vigente preservados.

## Fontes de referência

Os dois arquivos abaixo são a melhor base para construir o script:

* `backend/src/test/resources/data.sql`
* `e2e/setup/seed.sql`

Uso esperado de cada um:

* `data.sql` ajuda a identificar a estrutura mínima para a unidade SESEL, os usuários base, os perfis e os resets de
  sequência.
* `seed.sql` mostra o encadeamento completo de processo, subprocesso, mapa, movimentações, alertas e notificações,
  além de exemplificar estados intermediários e finais.

## Cenário alvo

O cenário deve ser montado de forma a representar um mapa completo e apresentável para o usuário, com a SESEL como
unidade principal e o restante da hierarquia já íntegra.

### Elementos que precisam existir

* Hierarquia de unidades suficiente para localizar a SESEL.
* Usuários com `CHEFE`, `GESTOR`, `SERVIDOR` e, quando necessário, `ADMIN`.
* Responsabilidades coerentes com os usuários e suas unidades.
* Um processo de mapeamento ativo ou concluído, conforme o estado escolhido para a demonstração.
* Um subprocesso da SESEL apontando para o mapa.
* Um mapa com o conteúdo completo deste documento.
* Movimentações que reflitam a progressão do fluxo.
* Alertas para os destinatários corretos.
* Registros em `NOTIFICACAO_EMAIL` para validar a caixa de saída administrativa.

## Ordem sugerida de carga

Para evitar chave estrangeira órfã e manter o histórico legível, o script deve seguir esta ordem:

1. Limpeza das tabelas dependentes do mapa, na ordem inversa das dependências.
2. Carga da hierarquia de unidades e da unidade SESEL.
3. Carga de usuários, perfis e responsabilidades.
4. Carga de administradores, se o cenário precisar mostrar a visão administrativa.
5. Criação do processo e da `UNIDADE_PROCESSO`.
6. Criação do `SUBPROCESSO` com a situação adequada ao estado final desejado.
7. Criação do `MAPA` e do vínculo em `UNIDADE_MAPA`, quando a tela ou a regra depender disso.
8. Inserção de `COMPETENCIA`, `ATIVIDADE` e `CONHECIMENTO`.
9. Inserção de `COMPETENCIA_ATIVIDADE` para ligar as competências às atividades.
10. Inserção de `MOVIMENTACAO` para registrar a trilha do processo.
11. Inserção de `ALERTA` e `ALERTA_USUARIO` com destinatários consistentes.
12. Inserção de `NOTIFICACAO_EMAIL` para simular o envio e falhas de notificação.
13. Reset das sequences, se o banco de homologação usar geração automática nas próximas operações.

## Regras de integridade

O script não deve criar apenas o mapa isolado. Ele precisa garantir que:

* o processo tenha unidade responsável;
* a unidade do mapa exista na hierarquia;
* o usuário que assina a movimentação exista e tenha perfil compatível;
* cada alerta tenha um processo, uma unidade de origem e uma unidade de destino válidos;
* cada notificação referencie um subprocesso que realmente exista;
* os relacionamentos entre competência, atividade e conhecimento não fiquem vazios;
* os códigos escolhidos não colidam com os usados em `data.sql` e `seed.sql`.

## Estratégia prática

Para manter o script seguro para homologação, o ideal é separar o trabalho em dois blocos:

* **bloco base**: dados reutilizáveis do ambiente, como unidade SESEL, usuários, perfis, responsáveis e administrador;
* **bloco do exemplo**: processo, subprocesso, mapa, competências, atividades, conhecimentos, movimentações, alertas e
  notificações.

Se o ambiente já contiver dados parecidos, o script deve usar uma abordagem idempotente ou ao menos limpar os códigos
reservados antes de inserir novamente.

## Validação esperada

Depois de executar o script, a checagem manual deveria confirmar:

* o mapa SESEL aparece para a unidade correta;
* o histórico de movimentações mostra a trajetória completa;
* os alertas aparecem para os destinatários esperados;
* a outbox de e-mails contém registros coerentes com o estado do fluxo;
* competências, atividades e conhecimentos exibidos correspondem ao texto abaixo.

---

## 1. Prestar suporte técnico às etapas do processo eleitoral

* **Atividade:** Realizar a preparação, carga e lacração das urnas eletrônicas para os pleitos.
    * **Conhecimentos:**
        * Funcionamento da Urna Eletrônica
        * Arquitetura da Urna Eletrônica
        * Sistema de Gerenciamento de Dados Aplicativos e Interface com a Urna Eletrônica
* **Atividade:** Coordenar o suporte técnico às Juntas Eleitorais e locais de votação no dia da eleição.
    * **Conhecimentos:**
        * Legislação sobre suporte técnico em eleições
        * Normas de suporte técnico em eleições
        * Fluxos de comunicação técnica
        * Protocolos de escalonamento de chamados
* **Atividade:** Executar procedimentos de contingência.
    * **Conhecimentos:**
        * Protocolos de contingência de hardware da Urna Eletrônica
        * Protocolos de contingência de software da Urna Eletrônica
        * Normas para votação manual
        * Procedimentos de recuperação de dados de votação

## 2. Gerenciar sistemas eleitorais informatizados

* **Atividade:** Monitorar o desempenho e a disponibilidade dos sistemas de votação e totalização.
    * **Conhecimentos:**
        * Arquitetura de sistemas eleitorais distribuídos
        * Ferramentas de monitoramento de infraestrutura de TI
* **Atividade:** Configurar parâmetros de segurança e acesso aos sistemas eleitorais.
    * **Conhecimentos:**
        * Gerenciamento de chaves digitais
        * Gerenciamento de certificados digitais
        * Políticas de controle de acesso do Tribunal Superior Eleitoral
        * Normas de auditoria do Tribunal Superior Eleitoral
* **Atividade:** Realizar a transmissão e o processamento de arquivos de resultado.
    * **Conhecimentos:**
        * Protocolos de transmissão de dados via rede segura JE-Connect
        * Estrutura de arquivos do Boletim de Urna
        * Estrutura de arquivos do Registro Digital de Voto

## 3. Gerenciar simulados eleitorais

* **Atividade:** Planejar e executar testes de estresse e carga nos sistemas de totalização.
    * **Conhecimentos:**
        * Metodologia de execução de testes de carga
        * Ferramentas de simulação de tráfego de dados
* **Atividade:** Simular cenários de falha de hardware e rede para validar planos de contingência.
    * **Conhecimentos:**
        * Técnicas de simulação de falhas de hardware
        * Arquitetura de rede do Tribunal
* **Atividade:** Coordenar simulados regionais envolvendo técnicos de diversas unidades.
    * **Conhecimentos:**
        * Gestão de equipes técnicas
        * Logística de eventos técnicos eleitorais

## 4. Elaborar roteiros técnicos de sistemas eleitorais

* **Atividade:** Desenvolver guias passo a passo para operação de sistemas por técnicos de urna.
    * **Conhecimentos:**
        * Técnicas de redação técnica
        * Simplificação de linguagem técnica
        * Princípios de experiência do usuário para manuais
* **Atividade:** Criar manuais de procedimentos para solução de problemas técnicos recorrentes.
    * **Conhecimentos:**
        * Metodologia de análise de causa raiz
        * Histórico de problemas técnicos de pleitos anteriores
* **Atividade:** Documentar as configurações específicas do ambiente para cada ciclo eleitoral.
    * **Conhecimentos:**
        * Padrões de documentação técnica de infraestrutura

## 5. Elaborar e ministrar treinamentos eleitorais

* **Atividade:** Capacitar técnicos de suporte e mesários na operação das urnas e sistemas.
    * **Conhecimentos:**
        * Didática para treinamentos operacionais
        * Conteúdo do Manual do Mesário
        * Legislação eleitoral aplicada ao mesário
* **Atividade:** Produzir videoaulas e materiais didáticos sobre novas funcionalidades dos sistemas.
    * **Conhecimentos:**
        * Ferramentas de edição de vídeo
        * Técnicas de design instrucional
* **Atividade:** Avaliar o nível de retenção de conhecimento dos participantes.
    * **Conhecimentos:**
        * Técnicas de avaliação de aprendizagem

## 6. Gerenciar projetos de desenvolvimento de sistemas informatizados

* **Atividade:** Definir cronogramas e marcos de entrega para novos módulos do SGC.
    * **Conhecimentos:**
        * Técnicas de estimativa de esforço de software
        * Uso de gráficos de Gantt
        * Uso de gráficos Burndown
* **Atividade:** Alocar recursos humanos e técnicos para sprints de desenvolvimento.
    * **Conhecimentos:**
        * Técnicas de planejamento de capacidade
        * Mapeamento de competências técnicas da equipe
* **Atividade:** Realizar reuniões de acompanhamento e retrospectivas.
    * **Conhecimentos:**
        * Facilitação de reuniões diárias do Scrum
        * Técnicas de retrospectiva de sprint

## 7. Gerenciar a configuração e o ambiente de desenvolvimento de sistemas

* **Atividade:** Manter a integridade do repositório central de código-fonte.
    * **Conhecimentos:**
        * Administração de servidores Git
        * Estratégias de ramificação de código GitFlow
        * Estratégias de desenvolvimento baseado em tronco
* **Atividade:** Configurar e manter pipelines de integração e entrega contínua.
    * **Conhecimentos:**
        * Sintaxe YAML para configurações de pipeline
        * Uso do Jenkins
        * Uso do GitLab CI
* **Atividade:** Gerenciar ambientes virtualizados para desenvolvimento e homologação.
    * **Conhecimentos:**
        * Uso de Docker
        * Orquestração de containers com Docker Compose
        * Princípios de Infraestrutura como Código

## 8. Realizar levantamento de requisitos de sistemas informatizados

* **Atividade:** Conduzir entrevistas com usuários finais para elicitar necessidades.
    * **Conhecimentos:**
        * Técnicas de entrevista de requisitos
        * Técnicas de brainstorming de requisitos
* **Atividade:** Mapear processos de negócio e identificar pontos de automação.
    * **Conhecimentos:**
        * Notação BPMN 2.0
        * Técnicas de análise de lacunas de processos
* **Atividade:** Documentar requisitos funcionais e não-funcionais detalhadamente.
    * **Conhecimentos:**
        * Escrita de Histórias de Usuário
        * Definição de Critérios de Aceitação

## 9. Realizar análise e projeto de sistemas informatizados

* **Atividade:** Desenhar diagramas de arquitetura de software e modelos de dados.
    * **Conhecimentos:**
        * Modelagem Entidade Relacionamento
        * Diagramas de Classe da UML
        * Diagramas de Sequência da UML
* **Atividade:** Definir interfaces de integração entre módulos.
    * **Conhecimentos:**
        * Padrões de arquitetura RESTful
        * Documentação de APIs com OpenAPI
* **Atividade:** Escolher padrões de projeto adequados para a solução.
    * **Conhecimentos:**
        * Padrões de projeto GoF
        * Princípios SOLID de programação orientada a objetos

## 10. Implementar serviços e sistemas informatizados

* **Atividade:** Desenvolver código backend.
    * **Conhecimentos:**
        * Linguagem de programação Java 17
        * Framework Spring Boot
        * Framework Hibernate
        * Otimização de consultas SQL no banco de dados
* **Atividade:** Desenvolver interfaces frontend.
    * **Conhecimentos:**
        * Framework Vue.js 3
        * Linguagem TypeScript
        * Técnicas de CSS Flexbox
        * Técnicas de CSS Grid
* **Atividade:** Realizar refatoração de código legado.
    * **Conhecimentos:**
        * Técnicas de refatoração de código
        * Identificação de códigos com cheiro ruim

## 11. Aplicar padrões de design, usabilidade, acessibilidade, privacidade e segurança

* **Atividade:** Garantir a acessibilidade das interfaces do sistema.
    * **Conhecimentos:**
        * Normas do Modelo de Acessibilidade em Governo Eletrônico
        * Diretrizes de Acessibilidade para Conteúdo Web 2.1
        * Ferramentas de validação de acessibilidade digital
* **Atividade:** Implementar proteção contra ataques cibernéticos comuns.
    * **Conhecimentos:**
        * Segurança em APIs com padrão OAuth2
        * Segurança em APIs com padrão JWT
        * Prevenção contra ataque de Injeção de SQL
        * Prevenção contra ataque de Script entre sites
* **Atividade:** Configurar o sistema para conformidade com a proteção de dados.
    * **Conhecimentos:**
        * Diretrizes da Lei Geral de Proteção de Dados
        * Técnicas de anonimização de dados pessoais

## 12. Coordenar a implantação de sistemas informatizados

* **Atividade:** Planejar janelas de manutenção e comunicação de indisponibilidade.
    * **Conhecimentos:**
        * Técnicas de gerenciamento de partes interessadas
* **Atividade:** Executar scripts de migração de banco de dados.
    * **Conhecimentos:**
        * Uso da ferramenta Flyway
        * Uso da ferramenta Liquibase
* **Atividade:** Verificar a estabilidade do sistema pós-implantação.
    * **Conhecimentos:**
        * Análise de verificações de saúde do sistema

## 13. Testar sistemas informatizados

* **Atividade:** Executar baterias de testes automatizados de ponta a ponta.
    * **Conhecimentos:**
        * Uso da ferramenta Playwright
* **Atividade:** Desenvolver testes unitários e de integração.
    * **Conhecimentos:**
        * Uso do framework JUnit
        * Uso da biblioteca Mockito
        * Uso da ferramenta Vitest
* **Atividade:** Reportar e monitorar o ciclo de vida de bugs.
    * **Conhecimentos:**
        * Técnicas de gestão de defeitos de software

## 14. Prestar suporte técnico especializado aos sistemas

* **Atividade:** Atuar na resolução de incidentes complexos escalonados.
    * **Conhecimentos:**
        * Análise de despejos de memória Heap
        * Análise de despejos de execução Thread
* **Atividade:** Analisar logs de erro para identificar falhas.
    * **Conhecimentos:**
        * Uso da pilha de ferramentas ELK
* **Atividade:** Desenvolver correções paliativas urgentes.
    * **Conhecimentos:**
        * Gestão de patches de software em produção

## 15. Atuar como integrante demandante no planejamento de contratações

* **Atividade:** Elaborar estudos técnicos preliminares.
    * **Conhecimentos:**
        * Instruções Normativas de contratação de TI da Secretaria de Governo Digital
* **Atividade:** Definir o objeto e a justificativa da contratação.
    * **Conhecimentos:**
        * Técnicas de pesquisa de mercado de tecnologia
        * Análise de soluções tecnológicas alternativas

## 16. Atuar como integrante técnico no planejamento de contratações

* **Atividade:** Escrever as especificações técnicas no Termo de Referência.
    * **Conhecimentos:**
        * Técnicas de definição de requisitos técnicos de TI
* **Atividade:** Definir níveis mínimos de serviço.
    * **Conhecimentos:**
        * Definição de indicadores de Acordo de Nível de Serviço
* **Atividade:** Analisar propostas técnicas em processos licitatórios.
    * **Conhecimentos:**
        * Técnicas de julgamento de conformidade técnica em licitações

## 17. Atuar como fiscal técnico na execução de contratações

* **Atividade:** Acompanhar a execução técnica garantindo os níveis de serviço.
    * **Conhecimentos:**
        * Técnicas de gestão de contratos administrativos de TI
* **Atividade:** Validar entregas e atestar notas fiscais tecnicamente.
    * **Conhecimentos:**
        * Normas de recebimento provisório de serviços de TI
        * Normas de recebimento definitivo de serviços de TI
* **Atividade:** Gerenciar riscos contratuais técnicos.
    * **Conhecimentos:**
        * Técnicas de análise de matriz de riscos contratuais
