package sgc.testutils;

import sgc.organizacao.model.*;

/**
 * Builder para criação de objetos Unidade em testes.
 */
public class UnidadeTestBuilder {
    private String codigo = "1"; 
    private String sigla = "UNIDADE_TEST";
    private String nome = "Unidade de Teste";
    private TipoUnidade tipo = TipoUnidade.OPERACIONAL;
    private String tituloTitular = "191919";
    private Unidade unidadeSuperior = null;

    public static UnidadeTestBuilder umaDe() {
        return new UnidadeTestBuilder();
    }

    /**
     * Cria uma Unidade Operacional padrão (ASSESSORIA_11)
     */
    public static UnidadeTestBuilder operacional() {
        return new UnidadeTestBuilder()
            .comCodigo("11")
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
            .comCodigo("12")
            .comSigla("COORD_11")
            .comNome("Coordenadoria 11")
            .comTipo(TipoUnidade.INTERMEDIARIA)
            .comTituloTitular("222222");
    }

    /**
     * Cria uma Unidade Raiz padrão (ADMIN)
     */
    public static UnidadeTestBuilder raiz() {
        return new UnidadeTestBuilder()
            .comCodigo("1")
            .comSigla("ADMIN")
            .comNome("Administração")
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
        Long codigoLong = Long.parseLong(codigo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(codigoLong);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        unidade.setTipo(tipo);
        unidade.setTituloTitular(tituloTitular);
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        return unidade;
    }
}
