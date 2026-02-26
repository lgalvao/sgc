# Guia para Agentes de Desenvolvimento - SGC

Este documento resume as diretrizes essenciais para o desenvolvimento no projeto SGC. O foco está nas **convenções
específicas** do projeto que diferem dos padrões genéricos.

## 1. Regras Fundamentais

* **Idioma:** Todo o código (variáveis, métodos), comentários, mensagens de erro e documentação deve ser em **Português
  Brasileiro**.
* **Identificadores:** Use sempre `codigo` em vez de `id` para chaves primárias e referências.
* **Convenções de Nomenclatura:**
    * **Backend:** Classes `PascalCase`, métodos `camelCase`. Sufixos: `Controller`, `Service`, `Repo`, `Dto`, `Mapper`.
      Exceções iniciam com `Erro` (ex: `ErroNegocio`).
    * **Frontend:** Componentes `PascalCase` (`ProcessoCard.vue`), arquivos TS `camelCase`. Stores seguem
      `use{Nome}Store`.

* **Qualidade de Código:**
    * **Limite de Parâmetros:** Métodos devem ter no máximo **3 parâmetros**. Se ultrapassar, use um objeto de
      transporte (Record ou DTO).
    * **Código Depreciado:** Código marcado como `@Deprecated` deve ser removido sumariamente assim que não houver mais
      dependências internas (especialmente após consolidações arquiteturais).

## 2. Backend (Java / Spring Boot 4)

* **Arquitetura:** Módulos de domínio. Facades orquestram múltiplos services quando há lógica de coordenação real.
  Controllers podem injetar services diretamente quando a facade é pass-through.
* **REST Não-Padrão:**
    * `GET` para consultas.
    * `POST` para criação.
    * `POST` com sufixo semanticamente claro para atualizações, ações de workflow e exclusão (ex:
      `/api/processos/{id}/iniciar`, `/api/processos/{id}/excluir`).
* **Persistence:** Tabelas em `UPPER_CASE`, colunas em `snake_case`. Enums como `STRING`.
* **Controle de Acesso (Security):**
    * Baseado nas regras documentadas em [`acesso.md`](/etc/regras-acesso.md):
        * **Leitura**: Hierarquia da Unidade Responsável
        * **Escrita**: Localização Atual do Subprocesso
    * **Implementação:** `SgcPermissionEvaluator` (implementa `PermissionEvaluator` do Spring Security)
    * **Controllers:** Use `@PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACAO')")` para verificações
    * **Services:** NÃO fazem verificações de acesso diretas
    * **Hierarquia:** `HierarquiaService` para verificações de hierarquia de unidades
    * **Perfis:** `ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR` (ver `acesso.md` para detalhes)

## 3. Frontend (Vue 3.5 / TypeScript)

* **Padrão de componentes:** Use `<script setup lang="ts">` e **BootstrapVueNext**.
* **Estado:** **Pinia** utilizando "Setup Stores" (com `ref` e `computed`).
* **Camadas:** `View -> Store -> Service -> API`. 
* **Erros:** Use `normalizeError` em services/stores. Componentes decidem como exibir (preferencialmente `BAlert` inline
  para erros de negócio).
* **Roteamento:** Modularizado (cada módulo tem seu arquivo `.routes.ts`).
* **Logging:**
    * **NAO** use `console.log`, `console.warn`, ou `console.debug` em código de produção
    * **USE** o logger estruturado: `import { logger } from '@/utils'`
    * **ESLint:** Configurado para bloquear `console.*` (exceto `console.error` para casos extremos)
    * **Exemplo:**
      ```typescript
      // ❌ ERRADO
      console.log('Usuário logado:', usuario);
      
      // ✅ CORRETO
      logger.info('Usuário logado:', usuario);
      ```

## 4. Comandos e Testes

* **Backend:** `./gradlew :backend:test` (JUnit 5 + Mockito + H2).
* **Frontend:** `npm run typecheck`, `npm run lint`, `npm run test:unit` (Vitest).
* **E2E:** Playwright (consulte `/e2e/README.md`).
* **Git Hooks:** Existe um hook de `pre-push` local que impede o envio de código se os testes do backend falharem. Agentes de IA devem garantir que os testes passem antes de sugerir ou realizar um push.

## 6. Referências e Padrões Detalhados

Para detalhes técnicos e exemplos de código, consulte:

* **Padrões de Código:**
    * [Backend Patterns](/backend/etc/regras/backend-padroes.md)
    * [Frontend Patterns](/frontend/etc/regras/frontend-padroes.md)
    * [Regras de DTOs](/backend/etc/regras/guia-dtos.md) - Taxonomia e convenções de DTOs
    * [Regras para execução de testes e2e e correção de bugs](/frontend/etc/regras/guia-correcao-e2e.md)

* **Módulo-Específico:**
    * `README.md` de cada módulo e diretório para responsabilidades específicas

## 7. Apêndice: Aprendizados do Ambiente (Local)
- **Testes E2E Seriais**: Em testes marcados como `test.describe.serial`, não se deve executar cenários individualmente (ex: usando `-g "Cenario X"`), pois cada cenário depende do estado deixado pelo anterior. Execute sempre o arquivo de teste completo.
* **Sistema Operacional:** Windows (win32).
* **Shell:** PowerShell via `powershell.exe -NoProfile -Command`.
* **Comandos de Shell:**
    * Comandos `dir` com sintaxe legada do CMD (ex: `dir /s /b`) podem falhar se usados com múltiplos argumentos de busca simultâneos ou se mal interpretados pelo wrapper do PowerShell.
    * Prefira comandos nativos do PowerShell como `Get-ChildItem` para buscas recursivas.
* **Ferramenta `glob`:** Funciona bem para padrões simples, mas pode ser sensível a maiúsculas/minúsculas dependendo da configuração.
* **Deploy:** O script `release-hom.sh` é exclusivo para Linux/Bash e não deve ser executado localmente no Windows.