package sgc.processo.service;

import net.jqwik.api.*;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessoManutencaoServicePbtTest {

    @Property
    void criar_rejeitaProcessoRevisaoSemMapa(@ForAll("requisicaoRevisaoInvalida") CriarProcessoRequest req) {
        // Mock dependencies
        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        UnidadeFacade unidadeService = mock(UnidadeFacade.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);
        ProcessoConsultaService processoConsultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(
            processoRepo, unidadeService, processoValidador, processoConsultaService
        );

        when(unidadeService.buscarEntidadePorId(any())).thenAnswer(inv -> {
             Unidade u = new Unidade();
             u.setCodigo((Long) inv.getArgument(0));
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
        UnidadeFacade unidadeService = mock(UnidadeFacade.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);
        ProcessoConsultaService processoConsultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(
            processoRepo, unidadeService, processoValidador, processoConsultaService
        );

        when(unidadeService.buscarEntidadePorId(any())).thenAnswer(inv -> {
             Unidade u = new Unidade();
             u.setCodigo((Long) inv.getArgument(0));
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
