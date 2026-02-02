package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoValidacaoService")
class SubprocessoValidacaoServiceTest {

    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private SubprocessoCrudService crudService;

    @InjectMocks
    private SubprocessoValidacaoService service;

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna lista vazia se mapa null")
    void obterAtividadesSemConhecimentoMapaNull() {
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        assertThat(service.obterAtividadesSemConhecimento(1L)).isEmpty();
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
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());
        assertThat(service.obterAtividadesSemConhecimento(mapa)).isEmpty();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna atividades")
    void obterAtividadesSemConhecimentoRetornaLista() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        Atividade a1 = new Atividade();
        a1.setConhecimentos(List.of(new Conhecimento()));
        Atividade a2 = new Atividade();
        a2.setConhecimentos(List.of()); // Sem
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1, a2));

        assertThat(service.obterAtividadesSemConhecimento(mapa)).containsExactly(a2);
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro sem atividades")
    void validarExistenciaAtividadesErroSemAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa();
        m.setCodigo(1L);
        sp.setMapa(m);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ao menos uma atividade");
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro atividade sem conhecimento")
    void validarExistenciaAtividadesErroSemConhecimento() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa();
        m.setCodigo(1L);
        sp.setMapa(m);
        Atividade a = new Atividade();
        a.setConhecimentos(List.of());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades devem possuir conhecimentos");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: erro competencias sem associacao")
    void validarAssociacoesMapaErroCompetencias() {
        Competencia c = new Competencia();
        c.setAtividades(Collections.emptySet());
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(1L)).thenReturn(List.of(c));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("competências que não foram associadas");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: erro atividades sem associacao")
    void validarAssociacoesMapaErroAtividades() {
        Competencia c = new Competencia();
        c.setAtividades(Set.of(new Atividade()));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(1L)).thenReturn(List.of(c));

        Atividade a = new Atividade();
        a.setCompetencias(Collections.emptySet());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(1L)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades que não foram associadas");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: sucesso quando todas associações estão válidas")
    void validarAssociacoesMapaSucesso() {
        Atividade a = new Atividade();
        Competencia c = new Competencia();
        c.setAtividades(Set.of(a));
        a.setCompetencias(Set.of(c));

        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(1L)).thenReturn(List.of(c));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(1L)).thenReturn(List.of(a));

        service.validarAssociacoesMapa(1L);

        verify(mapaManutencaoService).buscarCompetenciasPorCodMapa(1L);
        verify(mapaManutencaoService).buscarAtividadesPorMapaCodigo(1L);
    }

    @Test
    @DisplayName("validarCadastro: sem atividades")
    void validarCadastroSemAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa();
        m.setCodigo(1L);
        sp.setMapa(m);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.valido()).isFalse();
        assertThat(res.erros().getFirst().tipo()).isEqualTo("SEM_ATIVIDADES");
    }

    @Test
    @DisplayName("validarCadastro: atividade sem conhecimento")
    void validarCadastroAtividadeSemConhecimento() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa();
        m.setCodigo(1L);
        sp.setMapa(m);
        Atividade a = new Atividade();
        a.setCodigo(10L);
        a.setConhecimentos(List.of());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.valido()).isFalse();
        assertThat(res.erros().getFirst().tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
    }

    @Test
    @DisplayName("validarCadastro: sucesso")
    void validarCadastroSucesso() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa();
        m.setCodigo(1L);
        sp.setMapa(m);
        Atividade a = new Atividade();
        a.setConhecimentos(List.of(new Conhecimento()));

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.valido()).isTrue();
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com Set - sucesso quando situação está no conjunto")
    void validarSituacaoPermitidaSetSucesso() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        
        service.validarSituacaoPermitida(sp, Set.of(
            MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        ));
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com Set - erro quando situação não está no conjunto")
    void validarSituacaoPermitidaSetErro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(NAO_INICIADO);
        
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
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        
        service.validarSituacaoPermitida(sp,
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO
        );
    }

    @Test
    @DisplayName("validarSituacaoPermitida: varargs - erro quando situação não está entre as permitidas")
    void validarSituacaoPermitidaVarargsErro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(NAO_INICIADO);
        
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
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        
        service.validarSituacaoPermitida(sp, "Mensagem customizada",
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        );
    }

    @Test
    @DisplayName("validarSituacaoPermitida: com mensagem customizada - erro")
    void validarSituacaoPermitidaMensagemErro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(NAO_INICIADO);
        
        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, "Mensagem customizada de teste",
            MAPEAMENTO_CADASTRO_HOMOLOGADO
        ))
            .isInstanceOf(ErroValidacao.class)
            .hasMessage("Mensagem customizada de teste");
    }

    @Test
    @DisplayName("validarSituacaoMinima: sucesso quando situação é igual à mínima")
    void validarSituacaoMinimaSucessoIgual() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        
        service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_HOMOLOGADO);
    }

    @Test
    @DisplayName("validarSituacaoMinima: sucesso quando situação é maior que a mínima")
    void validarSituacaoMinimaSucessoMaior() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(MAPEAMENTO_MAPA_CRIADO);
        
        service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_HOMOLOGADO);
    }

    @Test
    @DisplayName("validarSituacaoMinima: erro quando situação é menor que a mínima")
    void validarSituacaoMinimaErro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        
        assertThatThrownBy(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_HOMOLOGADO))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("não atingiu a situação mínima necessária");
    }

    @Test
    @DisplayName("validarSituacaoMinima: com mensagem customizada - sucesso")
    void validarSituacaoMinimaMensagemSucesso() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
        
        service.validarSituacaoMinima(sp, REVISAO_CADASTRO_HOMOLOGADA, "Mensagem customizada");
    }

    @Test
    @DisplayName("validarSituacaoMinima: com mensagem customizada - erro")
    void validarSituacaoMinimaMensagemErro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        
        assertThatThrownBy(() -> service.validarSituacaoMinima(sp,
            REVISAO_CADASTRO_HOMOLOGADA,
            "Subprocesso ainda está em fase de revisão."
        ))
            .isInstanceOf(ErroValidacao.class)
            .hasMessage("Subprocesso ainda está em fase de revisão.");
    }
}