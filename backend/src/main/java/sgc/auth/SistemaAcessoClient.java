package sgc.auth;

import sgc.dto.PerfilUnidadeDTO;
import java.util.List;

public interface SistemaAcessoClient {

    /**
     * Verifica as credenciais do usuário no Sistema Acesso.
     * @param titulo título (identificador)
     * @param senha senha
     * @return true se credenciais válidas; false caso contrário
     */
    boolean authenticate(String titulo, String senha);

    /**
     * Recupera os perfis/unidades associados ao título informado.
     * Deve retornar lista vazia se não houver registros.
     * @param titulo título (identificador)
     * @return lista de perfis/unidade do usuário
     */
    List<PerfilUnidadeDTO> fetchPerfis(String titulo);
}