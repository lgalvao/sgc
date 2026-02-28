package sgc.processo.service;

import net.jqwik.api.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.erros.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("PBT")
class ProcessoManutencaoServicePbtTest {

    @Property
    void criar_rejeitaProcessoRevisaoSemMapa(@ForAll("requisicaoRevisaoInvalida") CriarProcessoRequest req) {
        // Mock dependencies
        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        OrganizacaoFacade unidadeService = mock(OrganizacaoFacade.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);
        ProcessoConsultaService processoConsultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(
                processoRepo, unidadeService, processoValidador, processoConsultaService
        );

        when(unidadeService.unidadePorCodigo(any())).thenAnswer(inv -> {
            Unidade u = new Unidade();
            u.setCodigo(inv.getArgument(0));
            u.setSituacao(SituacaoUnidade.ATIVA);
            u.setTipo(TipoUnidade.OPERACIONAL);
            return u;
        });
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.of("Erro: Unidade sem mapa"));

        // Act & Assert
        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ErroProcesso.class)
                .hasMessage("Erro: Unidade sem mapa");
    }

    @Property
    void criar_aceitaProcessoValido(@ForAll("requisicaoValida") CriarProcessoRequest req) {
        // Mock dependencies
        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        OrganizacaoFacade unidadeService = mock(OrganizacaoFacade.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);
        ProcessoConsultaService processoConsultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(
                processoRepo, unidadeService, processoValidador, processoConsultaService
        );

        when(unidadeService.unidadePorCodigo(any())).thenAnswer(inv -> {
            Unidade u = new Unidade();
            u.setCodigo(inv.getArgument(0));
            u.setSituacao(SituacaoUnidade.ATIVA);
            u.setTipo(TipoUnidade.OPERACIONAL);
            return u;
        });
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());
        when(processoRepo.saveAndFlush(any(Processo.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        service.criar(req);

        // Assert
        verify(processoRepo).saveAndFlush(any(Processo.class));
    }

    @Property
    void atualizar_rejeitaSeNaoForCriado(@ForAll("situacaoNaoCriada") SituacaoProcesso situacao,
                                         @ForAll("requisicaoValidaAtualizar") AtualizarProcessoRequest req) {
        // Mock dependencies
        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        OrganizacaoFacade unidadeService = mock(OrganizacaoFacade.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);
        ProcessoConsultaService processoConsultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(
                processoRepo, unidadeService, processoValidador, processoConsultaService
        );

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(situacao);

        when(processoConsultaService.buscarProcessoCodigo(anyLong())).thenReturn(processo);

        // Act & Assert
        assertThatThrownBy(() -> service.atualizar(1L, req))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                .hasMessage("Apenas processos na situação 'CRIADO' podem ser editados.");
    }

    @Property
    void apagar_rejeitaSeNaoForCriado(@ForAll("situacaoNaoCriada") SituacaoProcesso situacao) {
        // Mock dependencies
        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        OrganizacaoFacade unidadeService = mock(OrganizacaoFacade.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);
        ProcessoConsultaService processoConsultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(
                processoRepo, unidadeService, processoValidador, processoConsultaService
        );

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(situacao);

        when(processoConsultaService.buscarProcessoCodigo(anyLong())).thenReturn(processo);

        // Act & Assert
        assertThatThrownBy(() -> service.apagar(1L))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                .hasMessage("Apenas processos na situação 'CRIADO' podem ser removidos.");
    }

    @Provide
    Arbitrary<SituacaoProcesso> situacaoNaoCriada() {
        return Arbitraries.of(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.FINALIZADO);
    }

    @Provide
    Arbitrary<AtualizarProcessoRequest> requisicaoValidaAtualizar() {
        return Arbitraries.strings().alpha().ofMinLength(5).flatMap(descricao ->
                Arbitraries.of(TipoProcesso.values()).flatMap(tipo ->
                        Arbitraries.longs().between(1, 100).list().ofMinSize(1).map(unidades ->
                                AtualizarProcessoRequest.builder()
                                        .descricao(descricao)
                                        .tipo(tipo)
                                        .dataLimiteEtapa1(LocalDateTime.now().plusDays(1))
                                        .unidades(unidades)
                                        .build()
                        )
                )
        );
    }

    @Provide
    Arbitrary<CriarProcessoRequest> requisicaoRevisaoInvalida() {
        return Arbitraries.strings().alpha().ofMinLength(5).flatMap(descricao ->
                Arbitraries.of(TipoProcesso.REVISAO, TipoProcesso.DIAGNOSTICO).flatMap(tipo ->
                        Arbitraries.longs().between(1, 100).list().ofMinSize(1).map(unidades ->
                                CriarProcessoRequest.builder()
                                        .descricao(descricao)
                                        .tipo(tipo)
                                        .dataLimiteEtapa1(LocalDateTime.now().plusDays(1))
                                        .unidades(unidades)
                                        .build()
                        )
                )
        );
    }

    @Provide
    Arbitrary<CriarProcessoRequest> requisicaoValida() {
        return Arbitraries.strings().alpha().ofMinLength(5).flatMap(descricao ->
                Arbitraries.of(TipoProcesso.values()).flatMap(tipo ->
                        Arbitraries.longs().between(1, 100).list().ofMinSize(1).map(unidades ->
                                CriarProcessoRequest.builder()
                                        .descricao(descricao)
                                        .tipo(tipo)
                                        .dataLimiteEtapa1(LocalDateTime.now().plusDays(1))
                                        .unidades(unidades)
                                        .build()
                        )
                )
        );
    }
}
