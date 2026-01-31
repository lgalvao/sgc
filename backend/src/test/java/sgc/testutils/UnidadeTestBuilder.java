package sgc.testutils;

import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;

/**
 * Builder para criação de objetos Unidade em testes.
 * 
 * Elimina a necessidade de mocks complexos e setup repetitivo.
 * 
 * @example
 * ```java
 * // Ao invés de:
 * Unidade unidade = mock(Unidade.class);
 * when(unidade.getCodigo()).thenReturn("ASSESSORIA_11");
 * when(unidade.getSigla()).thenReturn("ASSESSORIA_11");
 * when(unidade.getTipo()).thenReturn(TipoUnidade.OPERACIONAL);
 * 
 * // Use:
 * Unidade unidade = UnidadeTestBuilder.operacional().build();
 * ```
 */
public class UnidadeTestBuilder {
    private String codigo = "UNIDADE_TEST"; // may be non-numeric sigla-like code
    private String sigla = "UNIDADE_TEST";
    private String nome = "Unidade de Teste";
    private TipoUnidade tipo = TipoUnidade.OPERACIONAL;
    private String tituloTitular = "191919";
    private Unidade unidadeSuperior = null;

    public static UnidadeTestBuilder umaDe() {
        return new UnidadeTestBuilder();
    }

    /**
     * Alias/fixture para a assessoria frequentemente usada nos testes
     */
    public static UnidadeTestBuilder assessoria() {
        return operacional();
    }

    /**
     * Cria uma Unidade Operacional padrão (ASSESSORIA_11)
     */
    public static UnidadeTestBuilder operacional() {
        return new UnidadeTestBuilder()
            .comCodigo("ASSESSORIA_11")
            .comSigla("ASSESSORIA_11")
            .comNome("Assessoria 11")
            .comTipo(TipoUnidade.OPERACIONAL)
            .comTituloTitular("555555");
    }

    /**
     * Cria uma Unidade Intermediária padrão (COORD_11)
     */
    public static UnidadeTestBuilder intermediaria() {
        return new UnidadeTestBuilder()
            .comCodigo("COORD_11")
            .comSigla("COORD_11")
            .comNome("Coordenadoria 11")
            .comTipo(TipoUnidade.INTERMEDIARIA)
            .comTituloTitular("222222");
    }

    /**
     * Cria uma Unidade Raiz padrão (SEDOC)
     */
    public static UnidadeTestBuilder raiz() {
        return new UnidadeTestBuilder()
            .comCodigo("SEDOC")
            .comSigla("SEDOC")
            .comNome("SEDOC")
            .comTipo(TipoUnidade.RAIZ)
            .comTituloTitular("191919");
    }

    public UnidadeTestBuilder comCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public UnidadeTestBuilder comSigla(String sigla) {
        this.sigla = sigla;
        return this;
    }

    public UnidadeTestBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public UnidadeTestBuilder comTipo(TipoUnidade tipo) {
        this.tipo = tipo;
        return this;
    }

    public UnidadeTestBuilder comTituloTitular(String tituloTitular) {
        this.tituloTitular = tituloTitular;
        return this;
    }

    public UnidadeTestBuilder comSuperior(Unidade unidadeSuperior) {
        this.unidadeSuperior = unidadeSuperior;
        return this;
    }

    public Unidade build() {
        Unidade unidade = new Unidade();
        // Try to set numeric id if codigo is numeric; otherwise prefer leaving id null
        // and rely on sigla to identify the unit in tests that use human codes like "SEDOC".
        if (codigo != null) {
            try {
                Long codigoLong = Long.parseLong(codigo);
                unidade.setCodigo(codigoLong);
            } catch (NumberFormatException ex) {
                // not a numeric id, don't set codigo (it's generated). Use sigla instead.
            }
        }
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        unidade.setTipo(tipo);
        unidade.setTituloTitular(tituloTitular);
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        return unidade;
    }
}
