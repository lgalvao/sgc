package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Gaps de Cobertura no ImpactoMapaService")
class ImpactoMapaGapTest {

    @InjectMocks
    private ImpactoMapaService service;

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private AccessControlService accessControlService;

    @Test
    @DisplayName("Deve falhar se mapa do subprocesso não for encontrado")
    void deveFalharSeMapaNaoEncontrado() {
        Subprocesso s = new Subprocesso();
        s.setCodigo(1L);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        s.setUnidade(u);
        Usuario user = new Usuario();

        doNothing().when(accessControlService).verificarPermissao(eq(user), any(), eq(s));
        when(mapaRepo.findMapaVigenteByUnidade(10L)).thenReturn(Optional.of(new Mapa()));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarImpactos(s, user))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lidar com duplicatas de descrição no mapa (merge function)")
    void deveLidarComDuplicatas() {
        Subprocesso s = new Subprocesso();
        s.setCodigo(1L);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        s.setUnidade(u);
        Usuario user = new Usuario();

        Mapa vig = new Mapa();
        vig.setCodigo(100L);
        Mapa atual = new Mapa();
        atual.setCodigo(200L);

        doNothing().when(accessControlService).verificarPermissao(eq(user), any(), eq(s));
        when(mapaRepo.findMapaVigenteByUnidade(10L)).thenReturn(Optional.of(vig));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(atual));

        Atividade a1 = new Atividade();
        a1.setCodigo(1L);
        a1.setDescricao("DUP");
        Atividade a2 = new Atividade();
        a2.setCodigo(2L);
        a2.setDescricao("DUP");

        // mock obterAtividadesDoMapa
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(200L)).thenReturn(List.of(a1, a2));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of());

        // Call method. Line 88 calls atividadesPorDescricao, which triggers the merge function.
        var impactos = service.verificarImpactos(s, user);

        // Atualmente verificarImpactos usa a lista raw para detectarInseridas, por isso o tamanho é 2
        assertThat(impactos.getAtividadesInseridas()).hasSize(2);
    }
}
