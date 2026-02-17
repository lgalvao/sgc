package sgc.fixture;

import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;

import java.time.LocalDateTime;

public class UnidadeFixture {

    public static final String DEFAULT_TITULO_TITULAR = "777";
    public static final String DEFAULT_MATRICULA_TITULAR = "00000777";

    public static Unidade unidadePadrao() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade Padrão");
        unidade.setSigla("UPD");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        // Campos obrigatórios para VW_UNIDADE (simula view de sistema externo)
        unidade.setTituloTitular(DEFAULT_TITULO_TITULAR);
        unidade.setMatriculaTitular(DEFAULT_MATRICULA_TITULAR);
        unidade.setDataInicioTitularidade(LocalDateTime.now().minusYears(1));
        return unidade;
    }

    public static Unidade unidadeComSigla(String sigla) {
        Unidade unidade = unidadePadrao();
        unidade.setSigla(sigla);
        return unidade;
    }

    public static Unidade unidadeComId(Long id) {
        Unidade unidade = unidadePadrao();
        unidade.setCodigo(id);
        return unidade;
    }
}
