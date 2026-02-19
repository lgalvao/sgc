package sgc.subprocesso.service.workflow;

import net.jqwik.api.*;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("PBT")
class SubprocessoBulkOperationsPbtTest {

    @Property
    void aceitarCadastroEmBloco_deveSerAtomico(@ForAll("listaSubprocessosUnicos") List<Subprocesso> subprocessos, 
                                              @ForAll int indiceFalha) {
        // Mock dependencies
        SubprocessoRepo subprocessoRepo = mock(SubprocessoRepo.class);
        SubprocessoCrudService crudService = mock(SubprocessoCrudService.class);
        AlertaFacade alertaService = mock(AlertaFacade.class);
        UnidadeFacade unidadeService = mock(UnidadeFacade.class);
        SubprocessoTransicaoService transicaoService = mock(SubprocessoTransicaoService.class);
        AnaliseFacade analiseFacade = mock(AnaliseFacade.class);
        UsuarioFacade usuarioServiceFacade = mock(UsuarioFacade.class);
        SubprocessoValidacaoService validacaoService = mock(SubprocessoValidacaoService.class);
        ImpactoMapaService impactoMapaService = mock(ImpactoMapaService.class);
        AccessControlService accessControlService = mock(AccessControlService.class);

        SubprocessoCadastroWorkflowService service = new SubprocessoCadastroWorkflowService(
                subprocessoRepo, crudService, alertaService, unidadeService, 
                transicaoService, analiseFacade, usuarioServiceFacade, 
                validacaoService, impactoMapaService, accessControlService
        );

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("ADMIN");

        List<Long> ids = new ArrayList<>();
        for (Subprocesso sp : subprocessos) {
            ids.add(sp.getCodigo());
            when(crudService.buscarSubprocesso(sp.getCodigo())).thenReturn(sp);
        }

        // Simular falha em um dos itens se o índice estiver no range
        if (indiceFalha >= 0 && indiceFalha < subprocessos.size()) {
            Subprocesso spFalha = subprocessos.get(indiceFalha);
            doThrow(new ErroValidacao("Erro provocado no item " + indiceFalha))
                    .when(accessControlService).verificarPermissao(eq(usuario), any(), eq(spFalha));

            assertThatThrownBy(() -> service.aceitarCadastroEmBloco(ids, usuario))
                    .isInstanceOf(ErroValidacao.class);

            // Para itens DEPOIS do erro, eles NÃO devem ser chamados.
            for (int i = indiceFalha + 1; i < subprocessos.size(); i++) {
                final Long targetId = subprocessos.get(i).getCodigo();
                verify(transicaoService, never()).registrarAnaliseETransicao(argThat(cmd -> 
                    cmd != null && cmd.sp().getCodigo().equals(targetId)));
            }
        } else {
            // Sucesso geral
            service.aceitarCadastroEmBloco(ids, usuario);
            
            for (Subprocesso sp : subprocessos) {
                final Long targetId = sp.getCodigo();
                verify(transicaoService, times(1)).registrarAnaliseETransicao(argThat(cmd -> 
                    cmd != null && cmd.sp().getCodigo().equals(targetId)));
            }
        }
    }

    @Provide
    Arbitrary<List<Subprocesso>> listaSubprocessosUnicos() {
        return Arbitraries.longs().between(1, 1000).set().ofSize(5).map(ids -> {
            List<Subprocesso> list = new ArrayList<>();
            for (Long id : ids) {
                Unidade u = new Unidade();
                u.setCodigo(id + 10000);
                u.setSigla("U" + id);
                list.add(Subprocesso.builder()
                        .codigo(id)
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                        .unidade(u)
                        .build());
            }
            return list;
        });
    }

    @Provide
    Arbitrary<Integer> indiceFalha() {
        return Arbitraries.integers().between(-1, 6);
    }
}
