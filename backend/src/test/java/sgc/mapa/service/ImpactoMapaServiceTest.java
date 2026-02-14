package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Nested;
import sgc.comum.repo.ComumRepo;

@ExtendWith(MockitoExtension.class)
class ImpactoMapaServiceTest {
    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private AccessControlService accessControlService;
    @Mock private ComumRepo repo;

    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Test
    @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
    void semMapaVigente() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertNotNull(result);
        assertTrue(result.atividadesInseridas().isEmpty());
        assertTrue(result.atividadesRemovidas().isEmpty());
        assertTrue(result.atividadesAlteradas().isEmpty());
        
        verify(accessControlService).verificarPermissao(usuario, Acao.VERIFICAR_IMPACTOS, subprocesso);
    }

    @Test
    @DisplayName("Deve detectar atividade inserida")
    void deveDetectarInserida() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repo.buscar(Mapa.class, "subprocesso.codigo", 10L)).thenReturn(mapaSub);

        // Vigente: vazio
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());
        
        // Atual: 1 atividade
        Atividade nova = new Atividade();
        nova.setCodigo(1L);
        nova.setDescricao("Nova");
        nova.setConhecimentos(Collections.emptyList());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(nova));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.atividadesInseridas().size());
        assertEquals("Nova", result.atividadesInseridas().getFirst().descricao());
    }

    @Test
    @DisplayName("Deve detectar atividade removida e impacto em competência")
    void deveDetectarRemovida() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repo.buscar(Mapa.class, "subprocesso.codigo", 10L)).thenReturn(mapaSub);

        // Vigente: 1 atividade "Antiga"
        Atividade antiga = new Atividade();
        antiga.setCodigo(1L);
        antiga.setDescricao("Antiga");
        antiga.setConhecimentos(Collections.emptyList());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(antiga));
        
        // Atual: vazio (foi removida)
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.emptyList());

        // Competencia ligada à atividade antiga
        Competencia comp = new Competencia();
        comp.setCodigo(50L);
        comp.setDescricao("Comp A");
        comp.setAtividades(Set.of(antiga));
        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.singletonList(comp));

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.atividadesRemovidas().size());
        assertEquals("Antiga", result.atividadesRemovidas().getFirst().descricao());
        
        assertEquals(1, result.competenciasImpactadas().size());
        assertEquals("Comp A", result.competenciasImpactadas().getFirst().descricao());
    }

    @Test
    @DisplayName("Deve detectar atividade alterada (conhecimentos diferentes)")
    void deveDetectarAlterada() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repo.buscar(Mapa.class, "subprocesso.codigo", 10L)).thenReturn(mapaSub);

        // Vigente: Ativ A com Conhecimento C1
        Atividade ativVigente = new Atividade();
        ativVigente.setCodigo(1L);
        ativVigente.setDescricao("Ativ A");
        Conhecimento c1 = new Conhecimento();
        c1.setDescricao("C1");
        ativVigente.setConhecimentos(Collections.singletonList(c1));
        
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(ativVigente));
        
        // Atual: Ativ A com Conhecimento C2 (alterado)
        Atividade ativAtual = new Atividade();
        ativAtual.setCodigo(2L); // ID pode ser diferente se for recriado, mas descrição igual
        ativAtual.setDescricao("Ativ A");
        Conhecimento c2 = new Conhecimento();
        c2.setDescricao("C2");
        ativAtual.setConhecimentos(Collections.singletonList(c2));
        
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(ativAtual));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.atividadesAlteradas().size());
        assertEquals("Ativ A", result.atividadesAlteradas().getFirst().descricao());
    }

    @Test
    void deveDetectarConhecimentosDiferentesComAmbosVazios() {
        Mapa mapaVigente = Mapa.builder().codigo(100L).build();
        Mapa mapaAtual = Mapa.builder().codigo(200L).build();
        Subprocesso subprocesso = criarSubprocesso(1L, mapaAtual);

        Usuario usuario = new Usuario();

        // Atividade vigente com conhecimentos vazios
        Atividade ativVigente = Atividade.builder()
                .codigo(1L)
                .descricao("Ativ A")
                .conhecimentos(List.of()) // vazio
                .build();

        // Atividade atual com conhecimentos vazios
        Atividade ativAtual = Atividade.builder()
                .codigo(2L)
                .descricao("Ativ A")
                .conhecimentos(List.of()) // vazio
                .build();

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repo.buscar(Mapa.class, "subprocesso.codigo", 1L)).thenReturn(mapaAtual);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L))
                .thenReturn(Collections.singletonList(ativVigente));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L))
                .thenReturn(Collections.singletonList(ativAtual));
        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        // Não deve marcar como alterada quando ambos têm conhecimentos vazios
        assertEquals(0, result.atividadesAlteradas().size());
    }

    @Test
    void deveDetectarAtividadesComConhecimentosDiferentes() {
        Mapa mapaVigente = Mapa.builder().codigo(100L).build();
        Mapa mapaAtual = Mapa.builder().codigo(200L).build();
        Subprocesso subprocesso = criarSubprocesso(1L, mapaAtual);

        Usuario usuario = new Usuario();

        Conhecimento c1 = Conhecimento.builder().codigo(1L).descricao("C1").build();
        Conhecimento c2 = Conhecimento.builder().codigo(2L).descricao("C2").build();
        Conhecimento c3 = Conhecimento.builder().codigo(3L).descricao("C3").build();

        // Atividade vigente com conhecimento C1
        Atividade ativVigente = Atividade.builder()
                .codigo(1L)
                .descricao("Ativ Teste")
                .conhecimentos(List.of(c1))
                .build();

        // Atividade atual com conhecimentos C2 e C3 (diferente da vigente)
        Atividade ativAtual = Atividade.builder()
                .codigo(2L)
                .descricao("Ativ Teste")
                .conhecimentos(List.of(c2, c3)) // Diferentes
                .build();

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repo.buscar(Mapa.class, "subprocesso.codigo", 1L)).thenReturn(mapaAtual);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L))
                .thenReturn(Collections.singletonList(ativVigente));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L))
                .thenReturn(Collections.singletonList(ativAtual));
        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        // Deve detectar a atividade como alterada porque os conhecimentos são diferentes
        assertEquals(1, result.atividadesAlteradas().size());
        assertEquals("Ativ Teste", result.atividadesAlteradas().getFirst().descricao());
    }

    private Subprocesso criarSubprocesso(Long unidadeCodigo, Mapa mapa) {
        Unidade unidade = Unidade.builder()
                .codigo(unidadeCodigo)
                .sigla("UNID")
                .nome("Unidade Teste")
                .build();
        return Subprocesso.builder()
                .codigo(1L)
                .unidade(unidade)
                .mapa(mapa)
                .build();
    }

    @Nested
    @DisplayName("Cobertura Extra")
    class Coverage {

        @Test
        @DisplayName("verificarImpactos: Falha quando mapa do subprocesso não existe")
        void verificarImpactos_MapaSubprocessoInexistente() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);
            Usuario usuario = new Usuario();

            when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(new Mapa()));
            when(repo.buscar(Mapa.class, "subprocesso.codigo", 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 1L));

            assertThrows(ErroEntidadeNaoEncontrada.class, () -> impactoMapaService.verificarImpactos(sp, usuario));
        }

        @Test
        @DisplayName("verificarImpactos: Atividades duplicadas (mesma descrição) usa handler de colisão")
        void verificarImpactos_AtividadesDuplicadas() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);
            
            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);
            
            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(repo.buscar(Mapa.class, "subprocesso.codigo", 1L)).thenReturn(mapaSub);

            // Duplicatas na lista de atividades do mapa vigente
            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("Mesma");
            
            Atividade a2 = new Atividade();
            a2.setCodigo(11L);
            a2.setDescricao("Mesma");
            
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(a1, a2));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of());
            
            // Necessário mockar competenciaRepo se as atividades forem removidas
            when(competenciaRepo.findByMapaCodigo(20L)).thenReturn(Collections.emptyList());

            ImpactoMapaDto dto = impactoMapaService.verificarImpactos(sp, new Usuario());
            
            // Deve ter processado sem erro (handler de colisão keep-first)
            // Se tinha 2, removeu 1 (porque mapa atual vazio).
            // Na verdade, mapaVigentes map vai ter apenas 1 entrada ("Mesma" -> a1).
            // Mas detectarRemovidas itera sobre a LISTA de vigentes, entao remove as duas.
            assertEquals(2, dto.atividadesRemovidas().size());
        }

        @Test
        @DisplayName("verificarImpactos: Atividade Alterada vinculada a Competencia gera impacto")
        void verificarImpactos_AtividadeAlteradaComCompetencia() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);
            
            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);
            
            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(repo.buscar(Mapa.class, "subprocesso.codigo", 1L)).thenReturn(mapaSub);

            // Atividade vigente
            Atividade aVigente = new Atividade();
            aVigente.setCodigo(10L);
            aVigente.setDescricao("Ativ 1");
            aVigente.setConhecimentos(List.of(new Conhecimento())); // Tem conhecimento

            // Atividade atual (mesma descricao, conhecimentos difs)
            Atividade aAtual = new Atividade();
            aAtual.setCodigo(10L); // mesmo codigo ou outro, importa descricao
            aAtual.setDescricao("Ativ 1");
            aAtual.setConhecimentos(List.of()); // Vazio -> alterada

            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(aVigente));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of(aAtual));

            // Competencia vinculada a atividade vigente (ID 10L)
            Competencia comp = new Competencia();
            comp.setCodigo(50L);
            comp.setDescricao("Comp A");
            comp.setAtividades(Set.of(aVigente));
            
            when(competenciaRepo.findByMapaCodigo(20L)).thenReturn(List.of(comp));

            ImpactoMapaDto dto = impactoMapaService.verificarImpactos(sp, new Usuario());
            
            assertEquals(1, dto.atividadesAlteradas().size());
            assertEquals(1, dto.competenciasImpactadas().size());
            assertTrue(dto.competenciasImpactadas().get(0).tiposImpacto()
                .contains(TipoImpactoCompetencia.ATIVIDADE_ALTERADA));
        }
    }
}

