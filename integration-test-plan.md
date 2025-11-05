# Plano de Refatoração para Dados de Testes de Integração

## 1. Análise da Situação Atual

Atualmente, os testes de integração do backend dependem de um único e extenso script SQL, `data-h2.sql`, para inicializar o banco de dados em memória (H2). Este script contém dados para uma variedade de cenários de teste, abrangendo múltiplas unidades, usuários, processos e configurações específicas para diferentes CDUs (Casos de Uso).

Embora essa abordagem tenha permitido o desenvolvimento inicial, ela apresenta desvantagens significativas que comprometem a manutenibilidade, a clareza e a robustez da suíte de testes:

- **Alta Acoplamento:** Todos os testes estão acoplados a um único conjunto de dados global. Uma alteração feita para satisfazer um novo teste pode inadvertidamente quebrar testes existentes.
- **Baixa Coesão:** O script mistura dados de diferentes domínios e para diferentes finalidades (dados base, dados para CDU-02, dados para CDU-14, etc.), dificultando a compreensão do contexto de um teste específico.
- **Dificuldade de Manutenção:** Para entender o que um teste faz, o desenvolvedor precisa constantemente cruzar referências entre o código do teste e o arquivo `data-h2.sql` de quase 400 linhas. Isso torna a depuração e a extensão dos testes uma tarefa lenta e propensa a erros.
- **Falta de Isolamento:** Embora o `@Transactional` reverta as alterações ao final de cada método de teste, o estado inicial é sempre o mesmo. Isso impede a execução de testes com configurações de dados conflitantes ou cenários de "banco de dados limpo".

## 2. Proposta de Refatoração

Propõe-se uma refatoração em fases para migrar da abordagem de script único para uma estratégia baseada em **dados por teste (per-test data)**, utilizando a anotação `@Sql` do Spring Test. O objetivo é tornar os testes mais **autocontidos, legíveis e resilientes a mudanças**.

---

### Fase 1: Modularização do Script de Dados Essenciais

O primeiro passo é desmembrar o `data-h2.sql` em arquivos menores e mais coesos, focados em um domínio específico. Isso melhora a organização sem alterar o comportamento dos testes existentes.

**Ações:**

1.  Criar uma nova estrutura de diretórios em `backend/src/test/resources/sql/`.
2.  Dividir `data-h2.sql` nos seguintes arquivos dentro de `sql/`:
    -   `01-unidades.sql`: Conterá apenas os `INSERTs` da tabela `UNIDADE`.
    -   `02-usuarios.sql`: Conterá os `INSERTs` da tabela `USUARIO`.
    -   `03-perfis-titulares.sql`: Conterá os `INSERTs` de `USUARIO_PERFIL` e os `UPDATEs` que definem os titulares das unidades.
    -   `04-mapas-competencias-base.sql`: Conterá os dados essenciais de mapas, competências, atividades e conhecimentos que são usados de forma ampla.
3.  Manter um arquivo `data-h2.sql` na raiz que importe esses scripts modulares (ou configurar o Spring para carregar múltiplos scripts) para garantir que os testes atuais continuem a passar sem modificações.

**Benefício Imediato:** Maior organização e facilidade para localizar e gerenciar os dados base.

---

### Fase 2: Extração de Dados Específicos por CDU

Identificar e extrair blocos de dados que são claramente destinados a um ou poucos testes específicos.

**Ações:**

1.  Criar subdiretórios em `backend/src/test/resources/sql/cdu/`.
2.  Mover os blocos de dados específicos do `data-h2.sql` para seus próprios arquivos. Por exemplo:
    -   `sql/cdu/cdu02-painel.sql`: Conterá os processos e alertas criados especificamente para os testes do `CDU02IntegrationTest`.
    -   `sql/cdu/cdu05-revisao.sql`: Conterá o processo de revisão e as unidades `ADMIN-UNIT` usadas no `CDU05IntegrationTest`.
    -   `sql/cdu/cdu14-impacto-mapa.sql`: Conterá os mapas e competências de revisão para o `CDU14IntegrationTest`.
3.  Remover esses blocos extraídos dos scripts da Fase 1.

**Benefício Imediato:** Redução drástica do tamanho do conjunto de dados global, separando claramente os dados de base dos dados de cenário.

---

### Fase 3: Refatoração Piloto com `@Sql`

Aplicar a nova abordagem a um ou dois testes de integração como prova de conceito. O `CDU02IntegrationTest` é um bom candidato por ter dados específicos já identificados.

**Ações:**

1.  Modificar a classe `CDU02IntegrationTest.java`.
2.  Adicionar a anotação `@Sql` na classe ou em métodos específicos para carregar os dados necessários. A anotação permite especificar scripts a serem executados antes do teste e também scripts de limpeza (`executionPhase`).

**Exemplo de Refatoração (CDU02IntegrationTest):**

**Antes:**

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
public class CDU02IntegrationTest {
    @BeforeEach
    void setup() {
        // Assume que os dados já existem no data-h2.sql
        unidadeRaiz = unidadeRepo.findById(2L).orElseThrow(); // STIC
        // ...
    }
    // ... testes
}
```

**Depois:**

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
// Carrega os dados essenciais e os dados específicos para este teste
@Sql(scripts = {
    "/sql/01-unidades.sql",
    "/sql/02-usuarios.sql",
    "/sql/03-perfis-titulares.sql",
    "/sql/cdu/cdu02-painel.sql"
})
// Opcional: Adicionar um script de limpeza se necessário
@Sql(scripts = "/sql/cdu/cdu02-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CDU02IntegrationTest {
    @BeforeEach
    void setup() {
        // O setup agora pode confiar que APENAS os dados declarados foram carregados
        unidadeRaiz = unidadeRepo.findBySigla("STIC").orElseThrow();
        // ...
    }
    // ... testes
}
```

**Benefícios:**

-   **Testes Autocontidos:** O teste declara explicitamente suas dependências de dados.
-   **Clareza:** Fica imediatamente claro qual conjunto de dados é relevante para o teste.
-   **Segurança:** A refatoração de dados para outros testes não afetará mais o `CDU02IntegrationTest`.

---

### Fase 4: Rollout e Limpeza Final

Após o sucesso do piloto, aplicar o padrão `@Sql` a todas as outras classes de teste de integração.

**Ações:**

1.  Refatorar iterativamente cada `CDUxxIntegrationTest.java` para usar a anotação `@Sql` e carregar apenas os dados essenciais mais os dados específicos de seu cenário.
2.  Uma vez que todos os testes tenham sido migrados, o arquivo global `data-h2.sql` (ou sua configuração de importação) pode ser removido do classpath dos testes. A configuração de teste do Spring Boot não carregará mais um script de dados por padrão.

## 3. Vantagens e Considerações

### Vantagens

-   **Robustez:** Reduz drasticamente a fragilidade dos testes.
-   **Legibilidade:** Melhora a compreensão e a velocidade de desenvolvimento.
-   **Isolamento Real:** Permite testar cenários que exigem um estado inicial limpo ou muito específico.
-   **Manutenibilidade:** Facilita a adição de novos testes e a modificação dos existentes.

### Considerações

-   **Duplicação de Dados:** Pode haver uma pequena duplicação de `INSERTs` se múltiplos testes precisarem de cenários muito parecidos, mas não idênticos. Isso é um trade-off aceitável em troca da clareza e do isolamento.
-   **Esforço Inicial:** A refatoração exigirá um esforço inicial concentrado, mas os benefícios a longo prazo para a saúde do projeto são imensos.

Este plano fornece um caminho estruturado para melhorar a qualidade e a sustentabilidade da suíte de testes de integração, alinhando-a com as melhores práticas de engenharia de software.
