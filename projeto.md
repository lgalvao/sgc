# Projeto: SGC — Análise da Estrutura Frontend/Backend

## Visão Geral
- Projeto multi-módulo Gradle com frontend (Vue 3 + Vite) e backend (Spring Boot).
- Documentação principal: [`README.md`](README.md:1)

## Estrutura de diretórios
- [`backend/`](backend:1): Módulo do Spring Boot — arquivos principais:
  - [`backend/src/main/java/sgc/SgcApplication.java`](backend/src/main/java/sgc/SgcApplication.java:1)
  - [`backend/src/main/java/sgc/WebConfig.java`](backend/src/main/java/sgc/WebConfig.java:1)
  - [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml:1)
  - build: [`backend/build.gradle.kts`](backend/build.gradle.kts:1)
- [`frontend/`](frontend:1): SPA Vue 3 — arquivos principais:
  - [`frontend/package.json`](frontend/package.json:1)
  - [`frontend/vite.config.js`](frontend/vite.config.js:1)
  - [`frontend/index.html`](frontend/index.html:1)
  - [`frontend/src/main.ts`](frontend/src/main.ts:1)
  - [`frontend/src/router.ts`](frontend/src/router.ts:1)
  - [`frontend/src/App.vue`](frontend/src/App.vue:1)

## Como o desenvolvimento roda localmente
- Backend: porta 8080 — ver [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml:1)
- Frontend: Vite em 5173 — ver [`README.md`](README.md:29)
- Proxy: [`frontend/vite.config.js`](frontend/vite.config.js:11) redireciona `/api` para backend
- CORS: [`backend/src/main/java/sgc/WebConfig.java`](backend/src/main/java/sgc/WebConfig.java:1)

## O que foi implementado nesta sessão
- Modelo JPA completo criado a partir de [`reqs/modelo-dados.md`](reqs/modelo-dados.md:1), usando Lombok (`@Getter/@Setter`).
- Exemplos de entidades (arquivos):
  - [`backend/src/main/java/sgc/model/Usuario.java`](backend/src/main/java/sgc/model/Usuario.java:1)
  - [`backend/src/main/java/sgc/model/Unidade.java`](backend/src/main/java/sgc/model/Unidade.java:1)
  - [`backend/src/main/java/sgc/model/Processo.java`](backend/src/main/java/sgc/model/Processo.java:1)
  - [`backend/src/main/java/sgc/model/Subprocesso.java`](backend/src/main/java/sgc/model/Subprocesso.java:1)
  - [`backend/src/main/java/sgc/model/Mapa.java`](backend/src/main/java/sgc/model/Mapa.java:1)
  - [`backend/src/main/java/sgc/model/Atividade.java`](backend/src/main/java/sgc/model/Atividade.java:1)
  - [`backend/src/main/java/sgc/model/Competencia.java`](backend/src/main/java/sgc/model/Competencia.java:1)
  - [`backend/src/main/java/sgc/model/Conhecimento.java`](backend/src/main/java/sgc/model/Conhecimento.java:1)
- Repositórios Spring Data JPA criados (ex.: [`backend/src/main/java/sgc/repository/CompetenciaRepository.java`](backend/src/main/java/sgc/repository/CompetenciaRepository.java:1))
- Controladores REST (CRUD) implementados e expostos em `/api` (refatorados para usar DTOs):
  - Competência: [`backend/src/main/java/sgc/controller/CompetenciaController.java`](backend/src/main/java/sgc/controller/CompetenciaController.java:1)
  - Atividade: [`backend/src/main/java/sgc/controller/AtividadeController.java`](backend/src/main/java/sgc/controller/AtividadeController.java:1)
  - Conhecimento: [`backend/src/main/java/sgc/controller/ConhecimentoController.java`](backend/src/main/java/sgc/controller/ConhecimentoController.java:1)
  - Mapa: [`backend/src/main/java/sgc/controller/MapaController.java`](backend/src/main/java/sgc/controller/MapaController.java:1)
  - Processo: [`backend/src/main/java/sgc/controller/ProcessoController.java`](backend/src/main/java/sgc/controller/ProcessoController.java:1)
  - Subprocesso: [`backend/src/main/java/sgc/controller/SubprocessoController.java`](backend/src/main/java/sgc/controller/SubprocessoController.java:1)
- DTOs e mappers implementados para evitar expor entidades JPA diretamente:
  - DTOs criados:
    - [`backend/src/main/java/sgc/dto/CompetenciaDTO.java`](backend/src/main/java/sgc/dto/CompetenciaDTO.java:1)
    - [`backend/src/main/java/sgc/dto/AtividadeDTO.java`](backend/src/main/java/sgc/dto/AtividadeDTO.java:1)
    - [`backend/src/main/java/sgc/dto/ConhecimentoDTO.java`](backend/src/main/java/sgc/dto/ConhecimentoDTO.java:1)
    - [`backend/src/main/java/sgc/dto/MapaDTO.java`](backend/src/main/java/sgc/dto/MapaDTO.java:1)
    - [`backend/src/main/java/sgc/dto/ProcessoDTO.java`](backend/src/main/java/sgc/dto/ProcessoDTO.java:1)
    - [`backend/src/main/java/sgc/dto/SubprocessoDTO.java`](backend/src/main/java/sgc/dto/SubprocessoDTO.java:1)
  - Mappers criados:
    - [`backend/src/main/java/sgc/mapper/CompetenciaMapper.java`](backend/src/main/java/sgc/mapper/CompetenciaMapper.java:1)
    - [`backend/src/main/java/sgc/mapper/AtividadeMapper.java`](backend/src/main/java/sgc/mapper/AtividadeMapper.java:1)
    - [`backend/src/main/java/sgc/mapper/ConhecimentoMapper.java`](backend/src/main/java/sgc/mapper/ConhecimentoMapper.java:1)
    - [`backend/src/main/java/sgc/mapper/MapaMapper.java`](backend/src/main/java/sgc/mapper/MapaMapper.java:1)
    - [`backend/src/main/java/sgc/mapper/ProcessoMapper.java`](backend/src/main/java/sgc/mapper/ProcessoMapper.java:1)
    - [`backend/src/main/java/sgc/mapper/SubprocessoMapper.java`](backend/src/main/java/sgc/mapper/SubprocessoMapper.java:1)
- Associação N‑N Competência<->Atividade:
  - Entidade: [`backend/src/main/java/sgc/model/CompetenciaAtividade.java`](backend/src/main/java/sgc/model/CompetenciaAtividade.java:1)
  - Repositório: [`backend/src/main/java/sgc/repository/CompetenciaAtividadeRepository.java`](backend/src/main/java/sgc/repository/CompetenciaAtividadeRepository.java:1)
  - Controlador: [`backend/src/main/java/sgc/controller/CompetenciaAtividadeController.java`](backend/src/main/java/sgc/controller/CompetenciaAtividadeController.java:1)
- Ajustes em controllers para aceitar/retornar DTOs e manter compatibilidade com payloads existentes.
- Implementação de equals()/hashCode() em Ids embutidos críticos (ex.: [`backend/src/main/java/sgc/model/CompetenciaAtividade.java`](backend/src/main/java/sgc/model/CompetenciaAtividade.java:1)) para eliminar fragilidade em operações com chaves compostas.
- Testes:
  - Testes unitários de controladores: [`backend/src/test/java/sgc/controller/ControllerUnitTests.java`](backend/src/test/java/sgc/controller/ControllerUnitTests.java:1) — adaptados para compatibilidade com DTOs.
  - Testes de integração (MockMvc + BD): [`backend/src/test/java/sgc/IntegrationTests.java`](backend/src/test/java/sgc/IntegrationTests.java:1) — cobrindo fluxos principais (criar mapa, competência, atividade, conhecimento).
  - A suíte de testes backend foi executada localmente: `./gradlew :backend:test` (BUILD SUCCESSFUL).

## Problemas encontrados e resoluções
- `data.sql` executava antes do esquema JPA ser criado — erro resolvido com `spring.jpa.defer-datasource-initialization: true` em [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml:1).
- Avisos do Hibernate sobre chaves compostas sem equals/hashCode; recomenda-se implementar equals/hashCode nas classes Id embutidas para evitar fragilidade.
- Ajustei logs para SLF4J nas classes que faziam println (ex.: [`backend/src/main/java/sgc/config/CarregadorDadosIniciais.java`](backend/src/main/java/sgc/config/CarregadorDadosIniciais.java:1)).
- Refatorei controllers para `@RequiredArgsConstructor`; removi construtores manuais.

## Próximos passos recomendados
1. Implementar DTOs e validação para não expor entidades diretamente nas APIs.
3. Implementar equals/hashCode nas classes Id compostas.
4. Adicionar OpenAPI/Swagger e documentação dos endpoints.
5. Ajustar segurança/CORS e adicionar autenticação/roles.
6. Rodar a suíte de testes e corrigir regressões: `./gradlew :backend:test`

## Como rodar localmente (com testes)
1. Iniciar backend (vai compilar frontend, copiar assets e subir Spring Boot): ./gradlew :backend:bootRun
2. Frontend: cd frontend && npm install && npm run dev
4. Rodar testes unitários backend: ./gradlew :backend:test
5. Rodar build completo: ./gradlew build

## Arquivos criados / alterados nesta sessão (resumo)
- Entidades: `backend/src/main/java/sgc/model/*` (várias)
- Repositórios: `backend/src/main/java/sgc/repository/*`
- Controllers: `backend/src/main/java/sgc/controller/*` (vários, refatorados para @RequiredArgsConstructor)
- Inicialização e dados: [`backend/src/main/java/sgc/config/CarregadorDadosIniciais.java`](backend/src/main/java/sgc/config/CarregadorDadosIniciais.java:1), [`backend/src/main/resources/data.sql`](backend/src/main/resources/data.sql:1)
- Testes: [`backend/src/test/java/sgc/controller/ControllerUnitTests.java`](backend/src/test/java/sgc/controller/ControllerUnitTests.java:1)
- Build: root `build.gradle.kts` e [`backend/build.gradle.kts`](backend/build.gradle.kts:1) (tarefa :copyFrontend)

## Observações finais
- A base do backend está funcional para desenvolvimento; o frontend já está buildado e serve assets estáticos via backend.
- Posso executar a suíte de testes agora, expandir os testes, adicionar DTOs/validações, configurar OpenAPI/Swagger ou implementar autenticação — diga qual prioridade prefere.

## Referências (arquivos mais relevantes)
- [`README.md`](README.md:1)
- [`projeto.md`](projeto.md:1)
- [`backend/build.gradle.kts`](backend/build.gradle.kts:1)
- [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml:1)
- [`backend/src/main/java/sgc/config/CarregadorDadosIniciais.java`](backend/src/main/java/sgc/config/CarregadorDadosIniciais.java:1)
- [`backend/src/main/resources/data.sql`](backend/src/main/resources/data.sql:1)
- [`backend/src/main/java/sgc/controller/CompetenciaAtividadeController.java`](backend/src/main/java/sgc/controller/CompetenciaAtividadeController.java:1)
- [`backend/src/test/java/sgc/controller/ControllerUnitTests.java`](backend/src/test/java/sgc/controller/ControllerUnitTests.java:1)