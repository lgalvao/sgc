package sgc.fixture;

import sgc.unidade.model.SituacaoUnidade;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;

public class UnidadeFixture {

    public static Unidade unidadePadrao() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade Padrão");
        unidade.setSigla("UPD");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        return unidade;
    }

    public static Unidade unidadeComSigla(String sigla) {
        Unidade unidade = unidadePadrao();
        unidade.setSigla(sigla);
        return unidade;
    }

    public static Unidade novaUnidade(Long codigo) {
        Unidade unidade = unidadePadrao();
        unidade.setCodigo(codigo);
        return unidade;
    }

    public static Unidade unidadeComId(Long id) {
        Unidade unidade = unidadePadrao();
        unidade.setCodigo(id);
        return unidade;
    }
}
