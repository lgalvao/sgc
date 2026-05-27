# Plano de Criação de Tabelas e Entidades JPA - Módulo de Diagnóstico

Este documento apresenta o plano detalhado para a criação das tabelas e o mapeamento das entidades JPA do módulo de **Diagnóstico** do Sistema de Gestão de Competências (SGC), respeitando as convenções estabelecidas e as regras arquiteturais do projeto.

---

## 1. Visão Geral da Arquitetura de Dados

O módulo de Diagnóstico é integrado ao fluxo macro do sistema através da associação direta com a tabela `SUBPROCESSO`. As competências técnicas utilizadas como base para as avaliações são originadas da cópia do `MAPA` de competências vigente da unidade participante.

A modelagem de dados proposta é composta por três tabelas principais no schema `sgc`:
1. **`DIAGNOSTICO`**: Entidade agregadora do diagnóstico da unidade operacional/interoperacional, vinculada unicamente a um subprocesso.
2. **`AVALIACAO_SERVIDOR`**: Registra as autoavaliações dos servidores e a posterior consolidação (avaliação de consenso) realizada pela chefia da unidade.
3. **`OCUPACAO_CRITICA`**: Registra o diagnóstico das lacunas de capacitação da equipe frente às competências vigentes (matriz de situação de capacitação).

---

## 2. Estrutura de Tabelas (DDL)

O script DDL correspondente foi criado na pasta `etc/planos/diagnostico_ddl.sql`. Ele define a criação física das tabelas com suas respectivas restrições (chaves estrangeiras, chaves primárias autoincrementais, chaves únicas e restrições de validação de valor - `CHECK constraints`), além de índices otimizados para evitar buscas completas em tabelas (`Full Table Scans`).

### Principais Restrições e Padrões de Banco:
* **Chaves Primárias**: Colunas com nome `codigo`, tipo `BIGINT` gerado por padrão como `IDENTITY`.
* **Chaves Estrangeiras**: Nomes de chaves estrangeiras iniciados com `fk_` apontando para as tabelas relacionadas.
* **Índices**: Índices criados especificamente para as colunas que servem de chave estrangeira (`diagnostico_codigo`, `servidor_titulo`, `competencia_codigo` e `subprocesso_codigo`).
* **Idioma/Convenções**: Colunas em `snake_case`, tabelas físicas em `UPPER_CASE` (`DIAGNOSTICO`, `AVALIACAO_SERVIDOR`, `OCUPACAO_CRITICA`).

---

## 3. Mapeamento de Enums JPA

Para garantir a tipagem estática e segurança do domínio no backend Java, serão criados os seguintes Enums no pacote `sgc.diagnostico.model`:

### 3.1. `SituacaoDiagnostico`
Representa o estado do diagnóstico global da unidade.
```java
package sgc.diagnostico.model;

public enum SituacaoDiagnostico {
    EM_ANDAMENTO,
    CONCLUIDO,
    VALIDADO,
    HOMOLOGADO
}
```

### 3.2. `SituacaoAvaliacaoServidor`
Representa o estado da avaliação individual de um servidor no ciclo atual.
```java
package sgc.diagnostico.model;

public enum SituacaoAvaliacaoServidor {
    AUTOAVALIACAO_NAO_REALIZADA,
    AUTOAVALIACAO_CONCLUIDA,
    CONSENSO_CRIADO,
    CONSENSO_APROVADO,
    AVALIACAO_IMPOSSIBILITADA
}
```

### 3.3. `NivelAvaliacao`
Representa a escala de Importância e Domínio das competências avaliadas. Os valores são mapeados a partir da escala descrita na especificação preliminar (NA, N1 a N6).
```java
package sgc.diagnostico.model;

public enum NivelAvaliacao {
    NA, // Não se aplica
    N1, // Nível 1
    N2, // Nível 2
    N3, // Nível 3
    N4, // Nível 4
    N5, // Nível 5
    N6  // Nível 6
}
```

### 3.4. `SituacaoCapacitacao`
Representa as situações da matriz de ocupações críticas por servidor e competência.
```java
package sgc.diagnostico.model;

public enum SituacaoCapacitacao {
    NA, // Não se aplica
    AC, // A capacitar
    EC, // Em capacitação
    C,  // Capacitado
    I   // Instrutor
}
```

---

## 4. Mapeamento das Entidades JPA

As classes serão criadas sob o pacote `sgc.diagnostico.model` e estenderão a classe base comum `sgc.comum.model.EntidadeBase`.

### 4.1. Entidade `Diagnostico`
```java
package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DIAGNOSTICO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class Diagnostico extends EntidadeBase {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subprocesso_codigo", nullable = false, unique = true)
    private Subprocesso subprocesso;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 50, nullable = false)
    private SituacaoDiagnostico situacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "justificativa_conclusao")
    private String justificativaConclusao;

    @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvaliacaoServidor> avaliacoesServidores = new ArrayList<>();

    @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OcupacaoCritica> ocupacoesCriticas = new ArrayList<>();
}
```

### 4.2. Entidade `AvaliacaoServidor`
```java
package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Competencia;
import sgc.organizacao.model.Usuario;

@Entity
@Table(name = "AVALIACAO_SERVIDOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class AvaliacaoServidor extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_codigo", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_titulo", referencedColumnName = "titulo", nullable = false)
    private Usuario servidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_codigo", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "importancia", length = 10)
    private NivelAvaliacao importancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominio", length = 10)
    private NivelAvaliacao dominio;

    @Column(name = "gap")
    private Integer gap;

    @Column(name = "observacoes")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_servidor", length = 50, nullable = false)
    private SituacaoAvaliacaoServidor situacaoServidor;

    /**
     * Calcula e atualiza o valor do GAP (Importância - Domínio)
     * desconsiderando casos onde Importância ou Domínio sejam NA.
     */
    public void calcularGap() {
        if (importancia == null || dominio == null || 
            importancia == NivelAvaliacao.NA || dominio == NivelAvaliacao.NA) {
            this.gap = null;
        } else {
            // Converte o nome do enum (ex: N1 -> 1, N6 -> 6) em inteiro para o cálculo
            int valorImportancia = Integer.parseInt(importancia.name().substring(1));
            int valorDominio = Integer.parseInt(dominio.name().substring(1));
            this.gap = valorImportancia - valorDominio;
        }
    }
}
```

### 4.3. Entidade `OcupacaoCritica`
```java
package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Competencia;
import sgc.organizacao.model.Usuario;

@Entity
@Table(name = "OCUPACAO_CRITICA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class OcupacaoCritica extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_codigo", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_titulo", referencedColumnName = "titulo", nullable = false)
    private Usuario servidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_codigo", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_capacitacao", length = 10, nullable = false)
    private SituacaoCapacitacao situacaoCapacitacao;
}
```

---

## 5. Interfaces de Repositório (Spring Data JPA)

Os repositórios estenderão `JpaRepository` e conterão consultas específicas para apoiar os Casos de Uso (CDUs).

### 5.1. `DiagnosticoRepo`
```java
package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticoRepo extends JpaRepository<Diagnostico, Long> {

    @Query("""
        SELECT d FROM Diagnostico d 
        LEFT JOIN FETCH d.avaliacoesServidores 
        LEFT JOIN FETCH d.ocupacoesCriticas 
        WHERE d.subprocesso.codigo = :subprocessoCodigo
    """)
    Optional<Diagnostico> buscarPorSubprocessoComRelacionamentos(@Param("subprocessoCodigo") Long subprocessoCodigo);

    Optional<Diagnostico> findBySubprocessoCodigo(Long subprocessoCodigo);
}
```

### 5.2. `AvaliacaoServidorRepo`
```java
package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoServidorRepo extends JpaRepository<AvaliacaoServidor, Long> {

    @Query("""
        SELECT a FROM AvaliacaoServidor a 
        JOIN FETCH a.servidor 
        JOIN FETCH a.competencia 
        WHERE a.diagnostico.codigo = :diagnosticoCodigo
    """)
    List<AvaliacaoServidor> listarPorDiagnostico(@Param("diagnosticoCodigo") Long diagnosticoCodigo);

    @Query("""
        SELECT a FROM AvaliacaoServidor a 
        JOIN FETCH a.competencia 
        WHERE a.diagnostico.codigo = :diagnosticoCodigo 
        AND a.servidor.tituloEleitoral = :servidorTitulo
    """)
    List<AvaliacaoServidor> buscarAvaliacoesDoServidor(
            @Param("diagnosticoCodigo") Long diagnosticoCodigo,
            @Param("servidorTitulo") String servidorTitulo);
}
```

### 5.3. `OcupacaoCriticaRepo`
```java
package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcupacaoCriticaRepo extends JpaRepository<OcupacaoCritica, Long> {

    @Query("""
        SELECT o FROM OcupacaoCritica o 
        JOIN FETCH o.servidor 
        JOIN FETCH o.competencia 
        WHERE o.diagnostico.codigo = :diagnosticoCodigo
    """)
    List<OcupacaoCritica> listarPorDiagnostico(@Param("diagnosticoCodigo") Long diagnosticoCodigo);
}
```

---

## 6. Controle de Serialização (Jackson / JsonView)

Para manter a compatibilidade e o padrão REST adotado no SGC, usaremos anotações do Jackson (`@JsonView` e `@JsonIgnoreProperties`) para controle de visibilidade das coleções bidirecionais e relacionamentos LAZY nas respostas das APIs de Diagnóstico. 

Novos views específicos para diagnóstico serão definidos na classe `sgc.diagnostico.model.DiagnosticoViews`:
```java
package sgc.diagnostico.model;

public class DiagnosticoViews {
    public interface Publica {}
    public interface Detalhada extends Publica {}
}
```

E as propriedades das entidades serão anotadas adequadamente para expor os dados necessários e impedir a serialização infinita de dependências circulares.
