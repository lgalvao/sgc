# Relatório de Execução de Testes E2E - Sistema SGC

**Data de Execução:** 2025-12-22  
**Duração Total:** 14.7 minutos  
**Comando Executado:** `npm run test:e2e`

---

## 📊 Resumo Executivo

| Métrica | Quantidade |
|---------|-----------|
| **Total de Testes** | 149 |
| **Testes Executados** | 50 |
| **Testes Bem-Sucedidos** | 1 (2%) |
| **Testes Falhados** | 49 (98%) |
| **Testes Não Executados** | 99 |
| **Taxa de Sucesso** | 2% |

### Status Geral: ❌ **CRÍTICO**

---

## 🔴 Análise de Falhas

### Causa Raiz Identificada

Todos os 49 testes falharam pela **mesma causa raiz**:

**`LazyInitializationException` no backend durante autenticação de usuários**

```
org.hibernate.LazyInitializationException: Cannot lazily initialize collection 
of role 'sgc.sgrh.model.Usuario.atribuicoesTemporarias' with key 'XXXXXX' (no session)
```

### Erros HTTP Observados

1. **500 Internal Server Error** em `POST /api/usuarios/entrar`
   - Ocorre para todos os usuários de teste
   - Causado pela LazyInitializationException

2. **404 Not Found** em `POST /api/usuarios/autorizar`
   - Endpoint parece não estar mapeado ou configurado corretamente

### Padrão de Falha

Todos os testes falham na **fase de autenticação**, especificamente ao tentar fazer login:

```
Error: page.waitForURL: Test timeout of 15000ms exceeded.
=========================== logs ===========================
waiting for navigation to "/painel" until "load"
============================================================

   at helpers/helpers-auth.ts:31
```

O fluxo falha porque:
1. Frontend tenta fazer login via `POST /api/usuarios/entrar`
2. Backend lança LazyInitializationException ao tentar acessar `atribuicoesTemporarias`
3. Frontend recebe erro 500
4. Navegação para `/painel` nunca ocorre
5. Teste atinge timeout de 15000ms

---

## ✅ Teste Bem-Sucedido

Apenas **1 teste passou** de um total de 149:

- Localização: Desconhecida (não especificado na saída)
- Este teste provavelmente não depende de autenticação ou usa um caminho diferente

---

## 📋 Lista Detalhada de Testes Falhados

### Categoria: Captura de Telas (12 falhas)

1. `e2e/captura-telas.spec.ts:55:13` - Captura telas de login
2. `e2e/captura-telas.spec.ts:84:13` - Captura painel ADMIN
3. `e2e/captura-telas.spec.ts:142:13` - Captura painel GESTOR
4. `e2e/captura-telas.spec.ts:163:13` - Captura painel CHEFE
5. `e2e/captura-telas.spec.ts:186:13` - Captura criação e detalhamento de processo
6. `e2e/captura-telas.spec.ts:226:13` - Captura validações de formulário
7. `e2e/captura-telas.spec.ts:260:13` - Captura fluxo completo de atividades
8. `e2e/captura-telas.spec.ts:328:13` - Captura estados de validação inline de atividades
9. `e2e/captura-telas.spec.ts:428:13` - Captura fluxo de mapa de competências
10. `e2e/captura-telas.spec.ts:535:13` - Captura elementos de navegação
11. `e2e/captura-telas.spec.ts:571:13` - Captura diferentes estados de processo
12. `e2e/captura-telas.spec.ts:611:13` - Captura em diferentes resoluções

### Categoria: CDU-01 - Login e Estrutura (5 falhas)

13. `e2e/cdu-01.spec.ts:14:9` - Realizar login com sucesso (Perfil Único)
14. `e2e/cdu-01.spec.ts:22:9` - Exibir seleção de perfil se houver múltiplos
15. `e2e/cdu-01.spec.ts:34:9` - Exibir barra de navegação após login
16. `e2e/cdu-01.spec.ts:46:9` - Exibir informações do usuário e controles
17. `e2e/cdu-01.spec.ts:60:9` - Exibir rodapé

### Categoria: CDU-02 - Visualizar Painel (7 falhas)

18. `e2e/cdu-02.spec.ts:23:13` - Exibir seções de Processos e Alertas (ADMIN)
19. `e2e/cdu-02.spec.ts:31:13` - Exibir botão "Criar processo" (ADMIN)
20. `e2e/cdu-02.spec.ts:35:13` - Criar processo e visualizá-lo na tabela (ADMIN)
21. `e2e/cdu-02.spec.ts:60:13` - Processos "Criado" aparecem apenas para ADMIN
22. `e2e/cdu-02.spec.ts:94:13` - Não incluir unidades INTERMEDIARIAS na seleção
23. `e2e/cdu-02.spec.ts:152:13` - Não exibir botão "Criar processo" (GESTOR)
24. `e2e/cdu-02.spec.ts:156:13` - Exibir mensagem quando não há processos (GESTOR)
25. `e2e/cdu-02.spec.ts:167:13` - Exibir tabela de alertas vazia (GESTOR)

### Categoria: CDU-03 - Manter Processo (3 falhas)

26. `e2e/cdu-03.spec.ts:21:9` - Validar campos obrigatórios
27. `e2e/cdu-03.spec.ts:47:9` - Editar um processo existente
28. `e2e/cdu-03.spec.ts:84:9` - Remover um processo

### Categoria: CDU-04 a CDU-21 - Demais Casos de Uso (22 falhas)

29. `e2e/cdu-04.spec.ts:19:9` - Iniciar um processo com sucesso
30. `e2e/cdu-05.spec.ts:262:9` - Fase 1: Ciclo completo de Mapeamento
31. `e2e/cdu-06.spec.ts:16:9` - Exibir detalhes do processo para ADMIN
32. `e2e/cdu-06.spec.ts:60:9` - Exibir detalhes do processo para GESTOR
33. `e2e/cdu-07.spec.ts:18:9` - Exibir detalhes do subprocesso para CHEFE
34. `e2e/cdu-08.spec.ts:16:9` - Processo de Mapeamento (Fluxo Completo + Importação)
35. `e2e/cdu-08.spec.ts:85:9` - Processo de Revisão (Botão Impacto)
36. `e2e/cdu-09.spec.ts:37:9` - Admin cria e inicia processo (Preparação)
37. `e2e/cdu-10.spec.ts:40:9` - Admin cria e inicia processo de mapeamento (Preparação)
38. `e2e/cdu-11.spec.ts:44:9` - Admin cria e inicia processo de mapeamento (Preparação)
39. `e2e/cdu-12.spec.ts:57:9` - Setup Mapeamento (Preparação)
40. `e2e/cdu-13.spec.ts:54:9` - ADMIN cria e inicia processo de mapeamento (Preparação)
41. `e2e/cdu-14.spec.ts:57:9` - Criar mapa vigente através de processo de mapeamento (Preparação)
42. `e2e/cdu-15.spec.ts:55:9` - Criar processo e homologar cadastro de atividades (Preparação)
43. `e2e/cdu-16.spec.ts:63:9` - Admin cria e inicia processo de mapeamento (Preparação)
44. `e2e/cdu-17.spec.ts:41:9` - Admin cria e inicia processo de mapeamento (Preparação)
45. `e2e/cdu-18.spec.ts:23:9` - ADMIN visualiza mapa via detalhes do processo
46. `e2e/cdu-18.spec.ts:73:9` - CHEFE visualiza mapa da própria unidade
47. `e2e/cdu-19.spec.ts:40:9` - Admin cria e inicia processo de mapeamento (Preparação)
48. `e2e/cdu-20.spec.ts:48:9` - Admin cria e inicia processo de mapeamento (Preparação)
49. `e2e/cdu-21.spec.ts:48:9` - Admin cria e inicia processo de mapeamento (Preparação)

---

## 🔍 Arquivos de Contexto de Erro

Foram gerados **49 arquivos `error-context.md`** no diretório `test-results/`, um para cada teste falhado.

Todos os arquivos mostram a tela de login com os campos preenchidos, indicando que o teste conseguiu:
- Navegar para a página de login
- Preencher os campos de título eleitoral e senha
- Mas a requisição de autenticação falhou no backend

---

## 🛠️ Recomendações de Correção

### 1. **Correção Urgente - Backend (CRÍTICA)**

**Problema:** LazyInitializationException no campo `atribuicoesTemporarias` do modelo `Usuario`

**Localização:** `sgc.sgrh.model.Usuario`

**Soluções possíveis:**

#### Opção A: Eager Loading (mais simples)
```java
@OneToMany
@JoinColumn(name = "usuario_id")
@Fetch(FetchMode.SUBSELECT)
private List<AtribuicaoTemporaria> atribuicoesTemporarias;
```

#### Opção B: DTO Projection (melhor prática)
- Criar um DTO específico para autenticação que não inclua `atribuicoesTemporarias`
- Carregar apenas os dados necessários para o processo de login

#### Opção C: @Transactional (se aplicável)
- Garantir que o método de autenticação esteja marcado com `@Transactional`
- Carregar as atribuições dentro da sessão Hibernate ativa

### 2. **Verificar Endpoint /api/usuarios/autorizar**

O endpoint retorna 404, indicando que:
- Não está mapeado no controller
- Ou há erro no caminho da rota no frontend

**Ação:** Verificar se este endpoint ainda é necessário ou se foi removido/renomeado.

### 3. **Após Correção do Backend**

1. Executar novamente os testes E2E: `npm run test:e2e > e2e_test_results_v2.txt 2>&1`
2. Verificar se todos os testes que estavam como "did not run" agora executam
3. Validar taxa de sucesso esperada > 95%

---

## 📁 Artefatos Gerados

- **Log completo:** `e2e_test_results.txt` (11,767 linhas)
- **Screenshots de falhas:** 49 arquivos PNG em `test-results/`
- **Contextos de erro:** 49 arquivos `error-context.md` em `test-results/`

---

## 🎯 Próximos Passos

1. **Imediato:** Corrigir o problema de LazyInitializationException no backend
2. **Validação:** Re-executar testes E2E após correção
3. **Monitoramento:** Estabelecer execução regular dos testes E2E (CI/CD)
4. **Documentação:** Atualizar documentação com requisitos de configuração para ambiente de teste

---

## 📝 Notas Técnicas

- **Ambiente:** Local (localhost)
- **Backend:** Spring Boot rodando na porta 10000
- **Frontend:** Vite rodando na porta 5173
- **Browser:** Chromium (Playwright headless shell)
- **Workers:** 1 (sequencial)
- **Timeout por teste:** 15000ms (15 segundos)
- **Profile Spring ativo:** e2e

---

**Relatório gerado automaticamente pela execução de testes E2E**
