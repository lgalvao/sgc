package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoValidacaoService")
class SubprocessoValidacaoServiceTest {

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
        void erroSemMapa() {
            Subprocesso sp = new Subprocesso();
            assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(sp))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.SUBPROCESSO_SEM_MAPA);
        }

        @Test
        @DisplayName("não deve lançar erro quando todas as atividades possuem conhecimento")
        void sucessoQuandoTodasAsAtividadesPossuemConhecimento() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade atividade = new Atividade();
            atividade.setCodigo(10L);
            atividade.setDescricao("Atividade");
            atividade.setConhecimentos(Set.of(new Conhecimento()));

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(atividade));

            validacaoService.validarExistenciaAtividades(sp);

            verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(1L);
        }
    }

    @Nested
    @DisplayName("validarCadastro")
    class ValidarCadastro {

        @Test
        @DisplayName("deve retornar inválido se não tiver mapa")
        void invalidoSemMapa() {
            Subprocesso sp = new Subprocesso();
            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(1);
            assertThat(dto.erros().getFirst().tipo()).isEqualTo("SEM_MAPA");
        }

        @Test
        @DisplayName("deve retornar inválido se mapa não tiver atividades")
        void invalidoSemAtividades() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(1);
            assertThat(dto.erros().getFirst().tipo()).isEqualTo("SEM_ATIVIDADES");
        }

        @Test
        @DisplayName("deve retornar inválido se atividade não tiver conhecimentos")
        void invalidoAtividadeSemConhecimento() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a = new Atividade();
            a.setCodigo(10L);
            a.setDescricao("A1");
            a.setConhecimentos(Set.of());

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(1);
            assertThat(dto.erros().getFirst().tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
        }


        @Test
        @DisplayName("deve acumular múltiplas atividades sem conhecimento no retorno")
        void invalidoComMultiplasAtividadesSemConhecimento() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("Atividade 1");
            a1.setConhecimentos(Set.of());

            Atividade a2 = new Atividade();
            a2.setCodigo(11L);
            a2.setDescricao("Atividade 2");
            a2.setConhecimentos(Set.of());

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1, a2));

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);

            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(2);
            assertThat(dto.erros()).allSatisfy(erro -> assertThat(erro.tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO"));
            assertThat(dto.erros().stream().map(ValidacaoCadastroDto.Erro::atividadeCodigo))
                    .containsExactlyInAnyOrder(10L, 11L);
        }

        @Test
        @DisplayName("deve incluir detalhes da atividade no erro de conhecimento ausente")
        void invalidoComDetalhesDaAtividade() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a = new Atividade();
            a.setCodigo(77L);
            a.setDescricao("Atividade detalhada");
            a.setConhecimentos(Set.of());

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);

            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).singleElement().satisfies(erro -> {
                assertThat(erro.atividadeCodigo()).isEqualTo(77L);
                assertThat(erro.descricaoAtividade()).isEqualTo("Atividade detalhada");
            });
        }

        @Test
        @DisplayName("deve retornar válido se tudo estiver correto")
        void validoTudoCorreto() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a = new Atividade();
            a.setCodigo(10L);
            a.setDescricao("A1");
            Conhecimento c = new Conhecimento();
            c.setDescricao("C1");
            a.setConhecimentos(Set.of(c));

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isTrue();
            assertThat(dto.erros()).isEmpty();
        }
    }

    @Nested
    @DisplayName("validarMapaParaDisponibilizacao")
    class ValidarMapaParaDisponibilizacao {

        @Test
        @DisplayName("deve lançar erro quando subprocesso não possui mapa")
        void deveLancarErroQuandoSubprocessoSemMapa() {
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setMapa(null);

            assertThatThrownBy(() -> validacaoService.validarMapaParaDisponibilizacao(subprocesso))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.SUBPROCESSO_SEM_MAPA);
        }
    }

    @Nested
    @DisplayName("validarSituacaoPermitida")
    class ValidarSituacaoPermitida {

        @Test
        @DisplayName("deve lançar erro se entrada de API não informar situacao")
        void erroSituacaoNula() {
            Subprocesso sp = mock(Subprocesso.class);
            when(sp.getSituacao()).thenReturn(null);

            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, Set.of(SituacaoSubprocesso.NAO_INICIADO)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Situação do subprocesso não pode ser nula");
        }

        @Test
        @DisplayName("deve lançar erro se permitidas for vazio")
        void erroPermitidasVazia() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, Set.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar erro de validacao se situacao nao permitida")
        void erroSituacaoNaoPermitida() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("não deve lançar erro se situacao permitida")
        void sucessoSituacaoPermitida() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            validacaoService.validarSituacaoPermitida(sp, Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
        }

        @Test
        @DisplayName("deve lançar erro se varargs vazio")
        void erroVarargsVazio() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve usar a mensagem fornecida no erro")
        void usaMensagemFornecida() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, "Msg custom", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("Msg custom");
        }
    }

    @Nested
    @DisplayName("verificarAcessoUnidadeAoProcesso")
    class VerificarAcessoUnidadeAoProcesso {

        @Test
        @DisplayName("deve retornar true quando unidade participa do processo")
        void deveRetornarTrueQuandoUnidadeParticipaDoProcesso() {

            Long codProcesso = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos))
                    .thenReturn(true);

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(codProcesso, unidadeCodigos);

            assertThat(resultado).isTrue();
            verify(subprocessoRepo).existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos);
        }

        @Test
        @DisplayName("deve retornar false quando unidade não participa do processo")
        void deveRetornarFalseQuandoUnidadeNaoParticipaDoProcesso() {

            Long codProcesso = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos))
                    .thenReturn(false);

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(codProcesso, unidadeCodigos);

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando lista de unidades está vazia")
        void deveRetornarFalseQuandoListaUnidadesVazia() {

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(1L, List.of());

            assertThat(resultado).isFalse();
            verifyNoInteractions(subprocessoRepo);
        }
    }

    @Nested
    @DisplayName("validarSubprocessosParaFinalizacao")
    class ValidarSubprocessosParaFinalizacao {

        @Test
        @DisplayName("deve retornar válido quando todos subprocessos estão homologados")
        void deveRetornarValidoQuandoTodosHomologados() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(3L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(
                    eq(codProcesso),
                    argThat(situacoes -> situacoes.contains(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                            && situacoes.contains(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO))
            )).thenReturn(3L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("deve retornar inválido quando nem todos subprocessos estão homologados")
        void deveRetornarInvalidoQuandoNemTodosHomologados() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(5L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(3L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("Apenas 3 de 5 subprocessos foram homologados")
                    .contains("Todos os subprocessos devem estar homologados");
        }

        @Test
        @DisplayName("deve retornar inválido quando processo não possui subprocessos")
        void deveRetornarInvalidoQuandoProcessoSemSubprocessos() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(0L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("processo não possui subprocessos para finalizar");
            verify(subprocessoRepo, never()).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
        }

        @Test
        @DisplayName("deve otimizar query usando count ao invés de carregar entidades")
        void deveOtimizarQueryUsandoCount() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(100L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(100L);

            validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            // Verifica que apenas queries de contagem foram chamadas, não findAll
            verify(subprocessoRepo).countByProcessoCodigo(codProcesso);
            verify(subprocessoRepo).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
            verify(subprocessoRepo, never()).listarPorProcessoComUnidade(anyLong());
        }
    }

    @Nested
    @DisplayName("ValidationResult")
    class ValidationResultTest {

        @Test
        @DisplayName("valido() deve criar resultado válido sem mensagem")
        void validoDeveCriarResultadoValidoSemMensagem() {
            var resultado = SubprocessoValidacaoService.ResultadoValidacao.ofValido();
            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("invalido() deve criar resultado inválido com mensagem")
        void invalidoDeveCriarResultadoInvalidoComMensagem() {
            String mensagem = "Erro de validação";
            var resultado = SubprocessoValidacaoService.ResultadoValidacao.ofInvalido(mensagem);
            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem()).isEqualTo(mensagem);
        }
    }

    @Nested
    @DisplayName("Cobertura Adicional")
    class CoberturaAdicional {

        @Nested
        @DisplayName("validarExistenciaAtividades")
        class ValidarExistenciaAtividades {

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
                a1.setConhecimentos(Set.of());

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
        @DisplayName("validarMapaParaDisponibilizacao - adicional")
        class ValidarMapaParaDisponibilizacaoExtra {

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
        @DisplayName("validarSituacaoPermitida - varargs com mensagem")
        class ValidarSituacaoPermitidaExtra {

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

                Atividade a2 = new Atividade();
                a2.setCodigo(20L);
                a2.setDescricao("A2");

                when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1, a2));

                assertThatThrownBy(() -> validacaoService.validarRequisitosNegocioParaDisponibilizacao(sp))
                        .isInstanceOf(ErroValidacao.class)
                        .hasMessageContaining(Mensagens.ATIVIDADES_SEM_CONHECIMENTO_ASSOCIADO);
            }
        }
    }
}
