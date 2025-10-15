# Relatório de Alinhamento: Requisitos vs. Implementação

## Introdução

Este documento detalha a análise de alinhamento entre os Requisitos de Caso de Uso (CDU) e a implementação atual do backend do Sistema de Gestão de Competências (SGC). O objetivo é identificar o status de implementação de cada requisito, destacando funcionalidades atendidas, parcialmente atendidas e não atendidas.

## Resumo dos Principais Achados

A análise revelou que a arquitetura do backend é robusta e, em sua maior parte, alinhada com os requisitos funcionais. A API REST está bem definida, com endpoints específicos para a maioria das ações descritas nos casos de uso. No entanto, foram identificadas algumas lacunas importantes:

1.  **Integração com SGRH Simulada:** A integração com o sistema de RH (`SgrhService`) é totalmente simulada (mock). Embora isso permita o desenvolvimento e teste do restante da aplicação, a conexão com o banco de dados Oracle real é uma pendência crítica para a produção.
2.  **Lógica de Perfil Incompleta:** A atribuição de perfis de usuário (`Usuario.getAuthorities`) não contempla o perfil `SERVIDOR`, tratando todos os não-ADMIN/GESTORES como `CHEFE`.

**Observação:** Análises anteriores apontavam falhas na implementação do CDU-05 (Iniciar processo de revisão) e CDU-08 (Manter cadastro de atividades e conhecimentos). Uma reavaliação do código-fonte em **15/10/2025** confirmou que essas funcionalidades foram corrigidas e agora atendem aos requisitos.

## Análise Detalhada por Caso de Uso

A seguir, uma análise detalhada para cada CDU.

---

### CDU-01: Realizar login e exibir estrutura das telas
- **Propósito:** Autenticar o usuário e definir o perfil de acesso.
- **Status Geral:** **Parcialmente Atendido**
- **Análise:** A autenticação é gerenciada pelo Spring Security, utilizando a entidade `Usuario` que implementa `UserDetails`. A lógica de atribuição de papéis está presente, mas incompleta.
  - **Requisito 7.1 (ADMIN):** Atendido. O papel `ROLE_ADMIN` é atribuído ao usuário `admin`.
  - **Requisito 7.2 (GESTOR):** Atendido. O papel `ROLE_GESTOR` é atribuído a usuários cujo título começa com `gestor`.
  - **Requisito 7.3 (CHEFE):** Atendido. O papel `ROLE_CHEFE` é atribuído como padrão.
  - **Requisito 7.4 (SERVIDOR):** **Não Atendido.** Não há lógica para atribuir um papel `ROLE_SERVIDOR`. Todos os usuários que não são `ADMIN` ou `GESTOR` recebem `ROLE_CHEFE`, o que contradiz a especificação.
  - **Requisito 6 (Consulta a perfis e unidades):** **Parcialmente Atendido.** A funcionalidade depende da integração com o SGRH, que está simulada. O `SgrhService` retorna dados mockados.

---

### CDU-02: Visualizar Painel
- **Propósito:** Exibir a tela principal com processos ativos e alertas.
- **Status Geral:** **Atendido**
- **Análise:** A funcionalidade é implementada pelo `PainelControle`, que expõe os endpoints `/api/painel/processos` e `/api/painel/alertas`. A lógica de negócio para filtrar e agregar os dados está corretamente encapsulada no `PainelService`, alinhando-se com os requisitos.

---

### CDU-03: Manter processo
- **Propósito:** Permitir que o ADMIN crie, edite e remova processos antes de serem iniciados.
- **Status Geral:** **Atendido**
- **Análise:** O `ProcessoControle` fornece os endpoints `POST /api/processos`, `PUT /api/processos/{id}` e `DELETE /api/processos/{id}`, que correspondem diretamente às ações de criar, atualizar e excluir. A lógica de negócio no `ProcessoService` impõe a restrição de que apenas processos no estado `CRIADO` podem ser modificados, conforme especificado.

---

### CDU-04: Iniciar processo de mapeamento
- **Propósito:** Iniciar o fluxo de trabalho para um processo de mapeamento.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `POST /api/processos/{id}/iniciar` no `ProcessoControle` aciona o método `iniciarProcessoMapeamento` no `ProcessoService`. Este método orquestra corretamente todas as ações necessárias: altera o status do processo, cria subprocessos, mapas vazios, registros de movimentação e publica um `ProcessoIniciadoEvento` para que os módulos de notificação e alerta possam agir.

---

### CDU-05: Iniciar processo de revisão
- **Propósito:** Iniciar o fluxo de trabalho para um processo de revisão.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `POST /api/processos/{id}/iniciar` aciona o método `iniciarProcessoRevisao` no `ProcessoService`.
  - **Requisito 10 (Copiar mapa vigente):** **Atendido.** A implementação utiliza o `CopiaMapaService` para criar uma cópia do mapa de competências vigente da unidade, vinculando-o ao novo subprocesso de revisão. A análise anterior, que indicava a criação de um mapa vazio, estava desatualizada.

---

### CDU-06: Detalhar processo
- **Propósito:** Permitir que ADMIN e GESTOR visualizem o andamento de um processo e seus subprocessos.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `GET /api/processos/{id}/detalhes` no `ProcessoControle` atende perfeitamente a este requisito, fornecendo uma visão agregada do status de todas as unidades participantes.

---

### CDU-07: Detalhar subprocesso
- **Propósito:** Exibir os detalhes, histórico e ações disponíveis para o subprocesso de uma unidade.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `GET /api/subprocessos/{id}` no `SubprocessoControle` retorna todos os dados necessários para esta tela, incluindo o status atual, o histórico de movimentações e as informações da unidade.

---

### CDU-08: Manter cadastro de atividades e conhecimentos
- **Propósito:** Permitir que o CHEFE gerencie a lista de atividades e conhecimentos de sua unidade.
- **Status Geral:** **Atendido**
- **Análise:**
  - **CRUD de Atividades:** Atendido. O `AtividadeControle` fornece endpoints para criar, editar e remover atividades (`/api/atividades`).
  - **Importação de Atividades:** Atendido. O endpoint `POST /api/subprocessos/{id}/importar-atividades` implementa a funcionalidade de importação.
  - **Gerenciamento de Conhecimentos:** **Atendido.** O `AtividadeControle` expõe uma API aninhada (`/api/atividades/{atividadeId}/conhecimentos`) com endpoints `POST`, `PUT` e `DELETE` para o gerenciamento completo dos conhecimentos associados a uma atividade, cumprindo os requisitos do caso de uso. A análise anterior, que apontava a ausência destes endpoints, estava desatualizada.

---

### CDU-09: Disponibilizar cadastro de atividades e conhecimentos
- **Propósito:** Permitir que o CHEFE submeta a lista de atividades para análise da unidade superior.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `POST /api/subprocessos/{id}/disponibilizar` no `SubprocessoControle` implementa esta funcionalidade. O `SubprocessoService` contém a lógica para a transição de estado, validação, criação de movimentação e disparo de eventos de notificação.

---

### CDU-10: Disponibilizar revisão do cadastro de atividades e conhecimentos
- **Propósito:** Permitir que o CHEFE submeta a lista de atividades revisada para análise.
- **Status Geral:** **Atendido**
- **Análise:** Similar ao CDU-09, esta funcionalidade é implementada pelo endpoint `POST /api/subprocessos/{id}/disponibilizar-revisao`, que corretamente encapsula a lógica para o fluxo de revisão.

---

### CDU-11: Visualizar cadastro de atividades e conhecimentos
- **Propósito:** Permitir a visualização (somente leitura) da lista de atividades e conhecimentos.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `GET /api/subprocessos/{id}/cadastro` retorna o DTO `SubprocessoCadastroDto`, que agrega as atividades e conhecimentos de um subprocesso, atendendo perfeitamente a este requisito.

---

### CDU-12: Verificar impactos no mapa de competências
- **Propósito:** Analisar o impacto das alterações em atividades/conhecimentos no mapa de competências existente durante um processo de revisão.
- **Status Geral:** **Atendido**
- **Análise:** O endpoint `GET /api/subprocessos/{id}/impactos-mapa` no `SubprocessoControle` invoca o `ImpactoMapaService` para realizar a análise de impacto, conforme especificado.

---

### CDU-13: Analisar cadastro de atividades e conhecimentos
- **Propósito:** Permitir que GESTOR/ADMIN aprovem ou devolvam um cadastro submetido.
- **Status Geral:** **Atendido**
- **Análise:** O `SubprocessoControle` possui os endpoints `devolver-cadastro`, `aceitar-cadastro` e `homologar-cadastro`, que cobrem todo o fluxo de análise e aprovação descrito.

---

### CDU-14: Analisar revisão de cadastro de atividades e conhecimentos
- **Propósito:** Permitir que GESTOR/ADMIN aprovem ou devolvam uma revisão de cadastro submetida.
- **Status Geral:** **Atendido**
- **Análise:** Assim como no CDU-13, o `SubprocessoControle` possui endpoints dedicados (`devolver-revisao-cadastro`, `aceitar-revisao-cadastro`, `homologar-revisao-cadastro`) que implementam o fluxo de análise para o processo de revisão.

---

### CDU-15: Manter mapa de competências
- **Propósito:** Permitir que o ADMIN crie competências e as associe às atividades homologadas.
- **Status Geral:** **Atendido**
- **Análise:** O `CompetenciaControle` fornece um conjunto completo de endpoints para o CRUD de competências e para o gerenciamento de suas associações com atividades (`/api/competencias/{id}/atividades`), cobrindo todos os requisitos.

---

### CDU-16 a CDU-21: Fluxos de Trabalho Finais
- **Propósito:** Cobrem os fluxos de ajuste, disponibilização, visualização, validação e finalização do mapa e do processo.
- **Status Geral:** **Atendido**
- **Análise:** Todos os casos de uso do CDU-16 ao CDU-21 estão mapeados para endpoints específicos e bem definidos nos controladores `SubprocessoControle` e `ProcessoControle`. A lógica de negócio, as transições de estado e a orquestração de eventos de notificação estão corretamente implementadas na camada de serviço, refletindo fielmente os requisitos. A separação de endpoints para cada ação específica (ex: `devolver-validacao` vs `devolver-cadastro`) demonstra um design de API robusto e alinhado às especificações.
