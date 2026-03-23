package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.AcaoPermissao;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Reprodução do Bug de Hierarquia - TDD")
class ReproducaoBugHierarquiaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("Ações em bloco marcadas como LEITURA burlam a Regra de Ouro (Localização)")
    void acoesEmBlocoBurlamLocalizacaoPorSeremLeitura() {
        // Usuário GESTOR da Unidade 6 (COSIS)
        // Subprocesso 60000 da Unidade 8 (SEDESENV)
        
        Subprocesso sp = subprocessoRepo.findById(60000L).orElseThrow();
        
        // Simulando Usuario GESTOR logado na Unidade 6
        Usuario gestor = Usuario.builder()
                .tituloEleitoral("GESTOR_TESTE")
                .perfilAtivo(Perfil.GESTOR)
                .unidadeAtivaCodigo(6L)
                .build();
        
        // Ação ACEITAR_CADASTRO_EM_BLOCO está marcada como LEITURA no AcaoPermissao.java
        // Isso faz com que o SgcPermissionEvaluator use verificarHierarquia em vez de verificarLocalizacao.
        
        // Como 8 é subordinada de 6, verificarHierarquia retorna TRUE.
        // O esperado pela Regra de Ouro era FALSE, pois a localização (8) é diferente da unidade ativa (6).
        
        boolean permitido = permissionEvaluator.verificarPermissao(gestor, sp, AcaoPermissao.ACEITAR_CADASTRO_EM_BLOCO);
        
        assertThat(permitido)
                .as("A ação em bloco deve ser barrada pela localização (Regra de Ouro), pois o gestor está na unidade 6 e o subprocesso na 8")
                .isFalse();
    }
}
