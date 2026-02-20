package sgc.seguranca.acesso;

import net.jqwik.api.*;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Tag("PBT")
class SubprocessoAccessPolicyPbtTest {
    @Property
    void canExecute_adminPodeQuaseTudo(@ForAll Acao acao, @ForAll SituacaoSubprocesso situacao) {
        // Mock dependencies
        UsuarioPerfilRepo repo = mock(UsuarioPerfilRepo.class);
        HierarquiaService hierarquiaService = mock(HierarquiaService.class);
        
        SubprocessoAccessPolicy policy = new SubprocessoAccessPolicy(repo, hierarquiaService);
        
        Usuario admin = new Usuario();
        admin.setTituloEleitoral("123");
        admin.setPerfilAtivo(Perfil.ADMIN);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        sgc.processo.model.Processo processo = new sgc.processo.model.Processo();
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso sp = Subprocesso.builder().situacao(situacao).unidade(unidade).processo(processo).build();
        
        // canExecute doesn't call repo.findPerfisByUsuario(admin) directly, it uses getPerfilAtivo()
        
        boolean can = policy.canExecute(admin, acao, sp);
        
        if (EnumSet.of(Acao.EDITAR_CADASTRO, Acao.DISPONIBILIZAR_CADASTRO, Acao.EDITAR_REVISAO_CADASTRO, 
                       Acao.DISPONIBILIZAR_REVISAO_CADASTRO, Acao.APRESENTAR_SUGESTOES, Acao.VALIDAR_MAPA
                       ).contains(acao)) {
            assertThat(can).as("Admin não deve ter permissão para ação operacional: " + acao).isFalse();
        } else if (acao == Acao.VERIFICAR_IMPACTOS) {
            boolean sitAdmin = EnumSet.of(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 
                                        SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, 
                                        SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO).contains(situacao);
            assertThat(can).isEqualTo(sitAdmin);
        }
    }

    @Property
    void canExecute_chefeRespeitaHierarquia(@ForAll("acoesChefe") Acao acao, @ForAll SituacaoSubprocesso situacao) {
        UsuarioPerfilRepo repo = mock(UsuarioPerfilRepo.class);
        HierarquiaService hierarquiaService = mock(HierarquiaService.class);
        SubprocessoAccessPolicy policy = new SubprocessoAccessPolicy(repo, hierarquiaService);

        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral("CHEFE");
        chefe.setPerfilAtivo(Perfil.CHEFE);
        chefe.setUnidadeAtivaCodigo(100L);
        
        Unidade lotacao = new Unidade();
        lotacao.setCodigo(100L);
        chefe.setUnidadeLotacao(lotacao);

        Unidade unidadeSp = new Unidade();
        unidadeSp.setCodigo(200L);
        unidadeSp.setSigla("U200");
        unidadeSp.setTituloTitular("TITULAR_QUALQUER");

        sgc.processo.model.Processo processo = new sgc.processo.model.Processo();
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso sp = Subprocesso.builder().situacao(situacao).unidade(unidadeSp).processo(processo).build();

        // No canExecute, p.ex. para EDITAR_CADASTRO (CHEFE, [NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO], MESMA_UNIDADE)
        // verificaHierarquia(chefe, unidadeSp, MESMA_UNIDADE) deve retornar false pois 100 != 200
        
        boolean can = policy.canExecute(chefe, acao, sp);
        
        if (EnumSet.of(Acao.EDITAR_CADASTRO, Acao.DISPONIBILIZAR_CADASTRO, Acao.APRESENTAR_SUGESTOES, Acao.VALIDAR_MAPA).contains(acao)) {
             assertThat(can).as("Chefe fora da unidade não deve poder executar ação: " + acao).isFalse();
        }
    }

    @Provide
    Arbitrary<Acao> acoesChefe() {
        return Arbitraries.of(Acao.EDITAR_CADASTRO, Acao.DISPONIBILIZAR_CADASTRO, Acao.APRESENTAR_SUGESTOES, Acao.VALIDAR_MAPA);
    }
}
