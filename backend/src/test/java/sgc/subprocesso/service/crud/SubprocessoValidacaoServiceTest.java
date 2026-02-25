package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoWorkflowService (Validação)")
class SubprocessoValidacaoServiceTest {

    @Mock
    private MapaManutencaoService mapaManutencaoService;
    
    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private SubprocessoWorkflowService service;

    @BeforeEach
    void setup() {
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMapaManutencaoService(mapaManutencaoService);
    }

    private Subprocesso criarSubprocessoComDadosMinimos() {
        Processo proc = Processo.builder().codigo(1L).tipo(sgc.processo.model.TipoProcesso.MAPEAMENTO).build();
        Unidade uni = Unidade.builder().codigo(1L).sigla("TESTE").build();
        return Subprocesso.builder()
                .codigo(1L)
                .processo(proc)
                .unidade(uni)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .build();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna lista vazia se mapa null")
    void obterAtividadesSemConhecimentoMapaNull() {
        Long id = 1L;
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setMapa(null);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        assertThat(service.obterAtividadesSemConhecimento(id)).isEmpty();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna lista vazia se mapa código é null")
    void obterAtividadesSemConhecimentoMapaCodigoNull() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(null);
        assertThat(service.obterAtividadesSemConhecimento(mapa)).isEmpty();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna lista vazia se atividades retornam lista vazia")
    void obterAtividadesSemConhecimentoAtividadesVazia() {
        Mapa mapa = Mapa.builder().codigo(1L).build();
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());
        assertThat(service.obterAtividadesSemConhecimento(mapa)).isEmpty();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna atividades")
    void obterAtividadesSemConhecimentoRetornaLista() {
        Mapa mapa = Mapa.builder().codigo(1L).build();
        Atividade a1 = Atividade.builder().codigo(1L).descricao("A1").conhecimentos(Set.of(new Conhecimento())).build();
        Atividade a2 = Atividade.builder().codigo(2L).descricao("A2").conhecimentos(Set.of()).build(); 
        
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1, a2));

        assertThat(service.obterAtividadesSemConhecimento(mapa)).containsExactly(a2);
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro sem atividades")
    void validarExistenciaAtividadesErroSemAtividades() {
        Long id = 1L;
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        Mapa m = Mapa.builder().codigo(id).build();
        sp.setMapa(m);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(id)).thenReturn(List.of());

        assertThatThrownBy(() -> service.validarExistenciaAtividades(id))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ao menos uma atividade");
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro atividade sem conhecimento")
    void validarExistenciaAtividadesErroSemConhecimento() {
        Long id = 1L;
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        Mapa m = Mapa.builder().codigo(id).build();
        sp.setMapa(m);
        Atividade a = Atividade.builder().codigo(10L).conhecimentos(Set.of()).build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(id)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarExistenciaAtividades(id))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades devem possuir conhecimentos");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: erro competencias sem associacao")
    void validarAssociacoesMapaErroCompetencias() {
        Long id = 1L;
        Competencia c = new Competencia();
        c.setAtividades(Collections.emptySet());
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(id)).thenReturn(List.of(c));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(id))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("competências que não foram associadas");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: erro atividades sem associacao")
    void validarAssociacoesMapaErroAtividades() {
        Long id = 1L;
        Competencia c = new Competencia();
        c.setAtividades(Set.of(new Atividade()));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(id)).thenReturn(List.of(c));

        Atividade a = new Atividade();
        a.setCompetencias(Collections.emptySet());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(id)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(id))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades que não foram associadas");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: sucesso quando todas associações estão válidas")
    void validarAssociacoesMapaSucesso() {
        Long id = 1L;
        Atividade a = new Atividade();
        Competencia c = new Competencia();
        c.setAtividades(Set.of(a));
        a.setCompetencias(Set.of(c));

        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(id)).thenReturn(List.of(c));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(id)).thenReturn(List.of(a));

        assertThatCode(() -> service.validarAssociacoesMapa(id)).doesNotThrowAnyException();

        verify(mapaManutencaoService).buscarCompetenciasPorCodMapa(id);
        verify(mapaManutencaoService).buscarAtividadesPorMapaCodigo(id);
    }

    @Test
    @DisplayName("validarCadastro: sem atividades")
    void validarCadastroSemAtividades() {
        Long id = 1L;
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        Mapa m = Mapa.builder().codigo(id).build();
        sp.setMapa(m);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(id)).thenReturn(List.of());

        ValidacaoCadastroDto res = service.validarCadastro(id);
        assertThat(res.valido()).isFalse();
        assertThat(res.erros().getFirst().tipo()).isEqualTo("SEM_ATIVIDADES");
    }

    @Test
    @DisplayName("validarCadastro: atividade sem conhecimento")
    void validarCadastroAtividadeSemConhecimento() {
        Long id = 1L;
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        Mapa m = Mapa.builder().codigo(id).build();
        sp.setMapa(m);
        Atividade a = Atividade.builder().codigo(10L).conhecimentos(Set.of()).build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(id)).thenReturn(List.of(a));

        ValidacaoCadastroDto res = service.validarCadastro(id);
        assertThat(res.valido()).isFalse();
        assertThat(res.erros().getFirst().tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
    }

    @Test
    @DisplayName("validarCadastro: sucesso")
    void validarCadastroSucesso() {
        Long id = 1L;
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        Mapa m = Mapa.builder().codigo(id).build();
        sp.setMapa(m);
        Atividade a = Atividade.builder().codigo(50L).conhecimentos(Set.of(new Conhecimento())).build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(id)).thenReturn(List.of(a));

        ValidacaoCadastroDto res = service.validarCadastro(id);
        assertThat(res.valido()).isTrue();
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com Set - sucesso quando situação está no conjunto")
    void validarSituacaoPermitidaSetSucesso() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        
        assertThatCode(() -> service.validarSituacaoPermitida(sp, Set.of(
            MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com Set - erro quando situação não está no conjunto")
    void validarSituacaoPermitidaSetErro() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(NAO_INICIADO);
        
        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, Set.of(
            MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        )))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Situação do subprocesso não permite esta operação");
    }

    @Test
    @DisplayName("validarSituacaoPermitida: varargs - sucesso quando situação está entre as permitidas")
    void validarSituacaoPermitidaVarargsSucesso() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        
        assertThatCode(() -> service.validarSituacaoPermitida(sp,
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSituacaoPermitida: varargs - erro quando situação não está entre as permitidas")
    void validarSituacaoPermitidaVarargsErro() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(NAO_INICIADO);
        
        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp,
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO
        ))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Situação do subprocesso não permite esta operação");
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com mensagem customizada - sucesso")
    void validarSituacaoPermitidaMensagemSucesso() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        
        assertThatCode(() -> service.validarSituacaoPermitida(sp, "Mensagem customizada",
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com mensagem customizada - erro")
    void validarSituacaoPermitidaMensagemErro() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(NAO_INICIADO);
        
        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, "Mensagem customizada de teste",
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        ))
            .isInstanceOf(ErroValidacao.class)
            .hasMessage("Mensagem customizada de teste");
    }

    @Test
    @DisplayName("validarSituacaoMinima: sucesso quando situação é igual à mínima")
    void validarSituacaoMinimaSucessoIgual() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        
        assertThatCode(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_HOMOLOGADO)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSituacaoMinima: sucesso quando situação é maior que a mínima")
    void validarSituacaoMinimaSucessoMaior() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_CRIADO);
        
        assertThatCode(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_HOMOLOGADO)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSituacaoMinima: erro quando situação é menor que a mínima")
    void validarSituacaoMinimaErro() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        
        assertThatThrownBy(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_HOMOLOGADO))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("não atingiu a situação mínima necessária");
    }

    @Test
    @DisplayName("validarSituacaoMinima: com mensagem customizada - sucesso")
    void validarSituacaoMinimaMensagemSucesso() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(REVISAO_CADASTRO_HOMOLOGADA);
        
        assertThatCode(() -> service.validarSituacaoMinima(sp, REVISAO_CADASTRO_HOMOLOGADA, "Mensagem customizada")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSituacaoMinima: com mensagem customizada - erro")
    void validarSituacaoMinimaMensagemErro() {
        Subprocesso sp = criarSubprocessoComDadosMinimos();
        sp.setSituacaoForcada(REVISAO_CADASTRO_EM_ANDAMENTO);
        
        assertThatThrownBy(() -> service.validarSituacaoMinima(sp,
            REVISAO_CADASTRO_HOMOLOGADA,
            "Subprocesso ainda está em fase de revisão."
        ))
            .isInstanceOf(ErroValidacao.class)
            .hasMessage("Subprocesso ainda está em fase de revisão.");
    }

    @Nested
    @DisplayName("Validação de Argumentos")
    class ArgumentosTests {
        @Test
        @DisplayName("validarSituacaoPermitida(Set): deve lançar IllegalArgumentException se situacao for null")
        void validarSituacaoPermitidaSet_DeveLancarErroSeSituacaoNull() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            sp.setSituacaoForcada(null);
            Set<SituacaoSubprocesso> permitidas = Set.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, permitidas))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Situação do subprocesso não pode ser nula");
        }

        @Test
        @DisplayName("validarSituacaoPermitida(Set): deve lançar IllegalArgumentException se conjunto permitidas for vazio")
        void validarSituacaoPermitidaSet_DeveLancarErroSePermitidasVazio() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Set<SituacaoSubprocesso> permitidas = Collections.emptySet();

            assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, permitidas))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Conjunto de situações permitidas não pode ser vazio");
        }

        @Test
        @DisplayName("validarSituacaoPermitida(Varargs): deve lançar IllegalArgumentException se permitidas for vazio")
        void validarSituacaoPermitidaVarargs_DeveLancarErroSePermitidasVazio() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            assertThatThrownBy(() -> service.validarSituacaoPermitida(sp))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Pelo menos uma situação permitida deve ser fornecida");
        }

        @Test
        @DisplayName("validarSituacaoPermitida(Varargs + Message): deve lançar IllegalArgumentException se situacao for null")
        void validarSituacaoPermitidaVarargsMsg_DeveLancarErroSeSituacaoNull() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            sp.setSituacaoForcada(null);
            assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, "msg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Situação do subprocesso não pode ser nula");
        }

        @Test
        @DisplayName("validarSituacaoPermitida(Varargs + Message): deve lançar IllegalArgumentException se permitidas for vazio")
        void validarSituacaoPermitidaVarargsMsg_DeveLancarErroSePermitidasVazio() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, "msg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Pelo menos uma situação permitida deve ser fornecida");
        }

        @Test
        @DisplayName("validarSituacaoMinima: deve lançar IllegalArgumentException se situacao for null")
        void validarSituacaoMinima_DeveLancarErroSeSituacaoNull() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            sp.setSituacaoForcada(null);
            assertThatThrownBy(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Situação do subprocesso não pode ser nula");
        }

        @Test
        @DisplayName("validarSituacaoMinima(Message): deve lançar IllegalArgumentException se situacao for null")
        void validarSituacaoMinimaMsg_DeveLancarErroSeSituacaoNull() {
            Subprocesso sp = criarSubprocessoComDadosMinimos();
            sp.setSituacaoForcada(null);
            assertThatThrownBy(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "msg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Situação do subprocesso não pode ser nula");
        }
    }
}