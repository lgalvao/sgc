package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoValidacaoServiceCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @InjectMocks
    private SubprocessoValidacaoService validacaoService;

    @Nested
    @DisplayName("validarExistenciaAtividades")
    class ValidarExistenciaAtividades {

        @Test
        @DisplayName("deve lançar erro se mapa for nulo")
        void erroMapaNulo() {
            Subprocesso sp = new Subprocesso();
            sp.setMapa(null);

            assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.SUBPROCESSO_SEM_MAPA);
        }

        @Test
        @DisplayName("deve lançar erro se codigo do mapa for nulo")
        void erroCodigoMapaNulo() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(null);
            sp.setMapa(mapa);

            assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.SUBPROCESSO_SEM_MAPA);
        }

        @Test
        @DisplayName("deve lançar erro se mapa não possui atividades cadastradas")
        void erroSemAtividades() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

            assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.MAPA_SEM_ATIVIDADES);
        }

        @Test
        @DisplayName("deve lançar erro se houver atividades sem conhecimento")
        void erroAtividadeSemConhecimento() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a1 = new Atividade();
            a1.setConhecimentos(Set.of()); // Atividade sem conhecimento

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1));

            assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.ATIVIDADES_SEM_CONHECIMENTOS);
        }
    }

    @Nested
    @DisplayName("validarAssociacoesMapa")
    class ValidarAssociacoesMapa {

        @Test
        @DisplayName("deve lançar erro se existe competencia sem atividade")
        void erroCompetenciaSemAtividade() {
            Competencia comp = new Competencia();
            comp.setDescricao("Comp1");
            comp.setAtividades(Set.of());

            when(mapaManutencaoService.competenciasCodMapa(1L)).thenReturn(List.of(comp));

            assertThatThrownBy(() -> validacaoService.validarAssociacoesMapa(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.COMPETENCIAS_SEM_ATIVIDADE);
        }

        @Test
        @DisplayName("deve lançar erro se existe atividade sem competencia")
        void erroAtividadeSemCompetencia() {
            Competencia comp = new Competencia();
            comp.setDescricao("Comp1");
            Atividade a1 = new Atividade();
            a1.setDescricao("A1");
            comp.setAtividades(Set.of(a1));

            Atividade aSemComp = new Atividade();
            aSemComp.setDescricao("A2");
            aSemComp.setCompetencias(Set.of());

            when(mapaManutencaoService.competenciasCodMapa(1L)).thenReturn(List.of(comp));
            when(mapaManutencaoService.atividadesMapaCodigo(1L)).thenReturn(List.of(a1, aSemComp));

            assertThatThrownBy(() -> validacaoService.validarAssociacoesMapa(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.ATIVIDADES_SEM_COMPETENCIA);
        }

        @Test
        @DisplayName("nao deve lançar erro se todas as competencias tem atividades e vice-versa")
        void sucesso() {
            Competencia comp = new Competencia();
            comp.setDescricao("Comp1");
            Atividade a1 = new Atividade();
            a1.setDescricao("A1");
            comp.setAtividades(Set.of(a1));
            a1.setCompetencias(Set.of(comp));

            when(mapaManutencaoService.competenciasCodMapa(1L)).thenReturn(List.of(comp));
            when(mapaManutencaoService.atividadesMapaCodigo(1L)).thenReturn(List.of(a1));

            validacaoService.validarAssociacoesMapa(1L);
        }
    }

    @Nested
    @DisplayName("validarMapaParaDisponibilizacao")
    class ValidarMapaParaDisponibilizacao {

        @Test
        @DisplayName("deve lançar erro se competencia sem atividade")
        void erroCompetenciaSemAtividade() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Competencia comp = new Competencia();
            comp.setAtividades(Set.of());

            when(mapaManutencaoService.competenciasCodMapa(1L)).thenReturn(List.of(comp));

            assertThatThrownBy(() -> validacaoService.validarMapaParaDisponibilizacao(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.TODAS_COMPETENCIAS_DEVEM_TER_ATIVIDADE);
        }

        @Test
        @DisplayName("deve lançar erro se atividade nao estiver associada a competencia")
        void erroAtividadeNaoAssociada() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("A1");

            Atividade a2 = new Atividade();
            a2.setCodigo(20L);
            a2.setDescricao("A2");

            Competencia comp = new Competencia();
            comp.setAtividades(Set.of(a1));

            when(mapaManutencaoService.competenciasCodMapa(1L)).thenReturn(List.of(comp));
            when(mapaManutencaoService.atividadesMapaCodigo(1L)).thenReturn(List.of(a1, a2));

            assertThatThrownBy(() -> validacaoService.validarMapaParaDisponibilizacao(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.ATIVIDADES_DEVEM_TER_COMPETENCIA);
        }

        @Test
        @DisplayName("sucesso quando todas estao associadas")
        void sucesso() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a1 = new Atividade();
            a1.setCodigo(10L);

            Competencia comp = new Competencia();
            comp.setAtividades(Set.of(a1));

            when(mapaManutencaoService.competenciasCodMapa(1L)).thenReturn(List.of(comp));
            when(mapaManutencaoService.atividadesMapaCodigo(1L)).thenReturn(List.of(a1));

            validacaoService.validarMapaParaDisponibilizacao(sp);
        }
    }

    @Nested
    @DisplayName("validarSituacaoMinima")
    class ValidarSituacaoMinima {

        @Test
        @DisplayName("deve lançar IllegalArgumentException se situação for nula")
        void erroSituacaoNula() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(null);

            assertThatThrownBy(() -> validacaoService.validarSituacaoMinima(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "msg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar ErroValidacao se situação atual for menor que a mínima")
        void erroSituacaoMenor() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

            assertThatThrownBy(() -> validacaoService.validarSituacaoMinima(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Msg custom"))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("Msg custom");
        }

        @Test
        @DisplayName("sucesso se situação atual for igual a mínima")
        void sucessoSituacaoIgual() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            validacaoService.validarSituacaoMinima(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Msg custom");
        }

        @Test
        @DisplayName("sucesso se situação atual for maior que a mínima")
        void sucessoSituacaoMaior() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            validacaoService.validarSituacaoMinima(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Msg custom");
        }
    }

    @Nested
    @DisplayName("validarSituacaoPermitida")
    class ValidarSituacaoPermitida {

        @Test
        @DisplayName("deve lançar IllegalArgumentException se situação for nula")
        void erroSituacaoNula() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(null);

            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, "msg", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Situação do subprocesso não pode ser nula");
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException se não houver situações permitidas")
        void erroSemPermitidas() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, "msg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Pelo menos uma situação permitida deve ser fornecida");
        }
    }

    @Nested
    @DisplayName("validarRequisitosNegocioParaDisponibilizacao")
    class ValidarRequisitosNegocioParaDisponibilizacao {

        @Test
        @DisplayName("deve lançar erro se houver atividades sem conhecimento")
        void erroAtividadeSemConhecimento() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("A1");
            Conhecimento c1 = new Conhecimento();
            a1.setConhecimentos(Set.of(c1));

            // Para passar na validarExistenciaAtividades, o mapaManutencaoService deve retornar a1
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1));

            // Atividade sem conhecimento passada no argumento
            Atividade a2 = new Atividade();
            a2.setCodigo(20L);
            a2.setDescricao("A2");

            assertThatThrownBy(() -> validacaoService.validarRequisitosNegocioParaDisponibilizacao(sp, List.of(a2)))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.ATIVIDADES_SEM_CONHECIMENTO_ASSOCIADO);
        }
    }
}
