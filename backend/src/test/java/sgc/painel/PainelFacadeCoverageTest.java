package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.Alerta;
import sgc.alerta.AlertaRepo;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.seguranca.acesso.AcessoContexto;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para PainelFacade")
class PainelFacadeCoverageTest {

    @InjectMocks
    private PainelFacade painelFacade;

    @Mock private AlertaRepo alertaRepo;
    @Mock private ProcessoFacade processoFacade;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioFacade usuarioFacade;

    @Test
    @DisplayName("Deve calcular link de destino para CHEFE sem unidade informada")
    void deveCalcularLinkChefeSemUnidade() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        
        when(processoFacade.listarAtivos()).thenReturn(List.of(p));
        when(usuarioFacade.obterUsuarioAutenticadoOuNull()).thenReturn(null);
        
        AcessoContexto ctx = AcessoContexto.builder()
                .perfil(Perfil.CHEFE)
                .unidadesPossiveis(Collections.emptySet())
                .build();
        
        List<PainelGlobalDto.ProcessoAtivoDto> result = painelFacade.obterDadosPainelGlobal(ctx).processos();
        
        assertThat(result).hasSize(1);
        // Sem unidade e perfil CHEFE -> cai na linha 231
        assertThat(result.get(0).link()).isEqualTo("/processo/1");
    }

    @Test
    @DisplayName("Deve lidar com erro ao buscar unidade no calculo do link")
    void deveLidarComErroBuscaUnidadeNoLink() {
       Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        
        when(processoFacade.listarAtivos()).thenReturn(List.of(p));
        when(unidadeService.buscarPorCodigo(99L)).thenThrow(new RuntimeException("Erro"));
        
        AcessoContexto ctx = AcessoContexto.builder()
                .perfil(Perfil.CHEFE)
                .codigoUnidadeNoContexto(99L)
                .unidadesPossiveis(Collections.singleton(99L))
                .build();
        
        List<PainelGlobalDto.ProcessoAtivoDto> result = painelFacade.obterDadosPainelGlobal(ctx).processos();
        
        assertThat(result.get(0).link()).isNull(); // Linha 228
    }

    @Test
    @DisplayName("Deve cobrir nulos no mapeamento de AlertaDto")
    void deveCobrirNulosNoAlertaDto() {
        Alerta a = new Alerta();
        a.setCodigo(100L);
        a.setDescricao("D");
        a.setUnidadeOrigem(null);
        a.setUnidadeDestino(null);
        a.setProcesso(null);

        when(alertaRepo.buscarAlertasNaoLidos(anyString())).thenReturn(List.of(a));
        
        AcessoContexto ctx = AcessoContexto.builder().tituloUsuario("T").build();
        List<AlertaDto> result = painelFacade.obterAlertasNaoLidos(ctx);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).unidadeOrigem()).isNull();
        assertThat(result.get(0).unidadeDestino()).isNull();
        assertThat(result.get(0).codProcesso()).isNull();
    }
}
