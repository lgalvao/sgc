package sgc.mapa.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ImpactoMapaServiceCoverageTest {
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;

    @InjectMocks
    private ImpactoMapaService service;

    @Test
    @DisplayName("Deve detectar alteração quando conhecimentos diferem (um vazio, outro cheio)")
    void deveDetectarAlteracaoListaVazia() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        subprocesso.setUnidade(unidade);

        Usuario usuario = new Usuario();

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(10L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

        // Atividade A: No vigente tem 1 conhecimento, no atual tem 0
        Atividade aVigente = new Atividade();
        aVigente.setCodigo(1L);
        aVigente.setDescricao("A");
        aVigente.setConhecimentos(List.of(Conhecimento.builder().descricao("C1").atividade(aVigente).build()));

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(1L);
        aAtual.setDescricao("A");
        aAtual.setConhecimentos(Collections.emptyList());

        when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of(aVigente));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(200L)).thenReturn(List.of(aAtual));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto impacto = service.verificarImpactos(subprocesso, usuario);

        assertThat(impacto.isTemImpactos()).isTrue();
        assertThat(impacto.getAtividadesAlteradas()).hasSize(1);
    }

    @Test
    @DisplayName("Não deve detectar alteração quando ambos vazios")
    void naoDeveDetectarAlteracaoAmbosVazios() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        subprocesso.setUnidade(unidade);

        Usuario usuario = new Usuario();

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(10L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

        Atividade aVigente = new Atividade();
        aVigente.setCodigo(1L);
        aVigente.setDescricao("A");
        aVigente.setConhecimentos(Collections.emptyList());

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(1L);
        aAtual.setDescricao("A");
        aAtual.setConhecimentos(Collections.emptyList());

        when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of(aVigente));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(200L)).thenReturn(List.of(aAtual));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto impacto = service.verificarImpactos(subprocesso, usuario);

        assertThat(impacto.isTemImpactos()).isFalse();
    }

    @Test
    @DisplayName("Deve detectar alteração quando descrições diferem")
    void deveDetectarAlteracaoDescricoesDiferentes() {
        // Cobre branch 229 (sets diferentes)
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        subprocesso.setUnidade(unidade);

        Usuario usuario = new Usuario();

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(10L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

        Atividade aVigente = new Atividade();
        aVigente.setCodigo(1L);
        aVigente.setDescricao("A");
        aVigente.setConhecimentos(List.of(Conhecimento.builder().descricao("C1").atividade(aVigente).build()));

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(1L);
        aAtual.setDescricao("A");
        aAtual.setConhecimentos(List.of(Conhecimento.builder().descricao("C2").atividade(aAtual).build()));

        when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of(aVigente));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(200L)).thenReturn(List.of(aAtual));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto impacto = service.verificarImpactos(subprocesso, usuario);

        assertThat(impacto.isTemImpactos()).isTrue();
        assertThat(impacto.getAtividadesAlteradas()).hasSize(1);
    }
}
