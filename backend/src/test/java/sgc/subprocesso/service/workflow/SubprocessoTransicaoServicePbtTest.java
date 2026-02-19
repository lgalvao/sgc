package sgc.subprocesso.service.workflow;

import net.jqwik.api.*;
import org.springframework.context.ApplicationEventPublisher;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.notificacao.SubprocessoEmailService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("PBT")
class SubprocessoTransicaoServicePbtTest {
    @Property
    void registrar_disparaNotificacoesCorretamente(@ForAll TipoTransicao tipo, 
                                                  @ForAll("unidadesDiferentes") Unidades unidades) {
        // Mock dependencies
        MovimentacaoRepo movimentacaoRepo = mock(MovimentacaoRepo.class);
        AlertaFacade alertaService = mock(AlertaFacade.class);
        SubprocessoEmailService emailService = mock(SubprocessoEmailService.class);
        AnaliseFacade analiseFacade = mock(AnaliseFacade.class);
        UsuarioFacade usuarioFacade = mock(UsuarioFacade.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        SubprocessoTransicaoService service = new SubprocessoTransicaoService(
                movimentacaoRepo, alertaService, emailService, analiseFacade, usuarioFacade, eventPublisher
        );

        Unidade unidadeSp = new Unidade();
        unidadeSp.setSigla("USP");
        
        Subprocesso sp = Subprocesso.builder()
                .codigo(1L)
                .processo(new Processo())
                .unidade(unidadeSp)
                .build();
        
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipo)
                .origem(unidades.origem)
                .destino(unidades.destino)
                .usuario(usuario)
                .observacoes("Obs")
                .build();

        // Act
        service.registrar(cmd);

        // Assert
        if (tipo.geraAlerta()) {
            verify(alertaService, times(1)).criarAlertaTransicao(any(), anyString(), eq(unidades.origem), eq(unidades.destino));
        } else {
            verify(alertaService, never()).criarAlertaTransicao(any(), anyString(), any(), any());
        }

        if (tipo.enviaEmail()) {
            verify(emailService, times(1)).enviarEmailTransicaoDireta(sp, tipo, unidades.origem, unidades.destino, "Obs");
        } else {
            verify(emailService, never()).enviarEmailTransicaoDireta(any(), any(), any(), any(), any());
        }
    }

    static class Unidades {
        Unidade origem;
        Unidade destino;
        Unidades(Unidade o, Unidade d) { this.origem = o; this.destino = d; }
    }

    @Provide
    Arbitrary<Unidades> unidadesDiferentes() {
        return Arbitraries.longs().between(1, 100).flatMap(oId ->
            Arbitraries.longs().between(101, 200).map(dId -> {
                Unidade o = new Unidade(); o.setCodigo(oId); o.setSigla("O"+oId);
                Unidade d = new Unidade(); d.setCodigo(dId); d.setSigla("D"+dId);
                return new Unidades(o, d);
            })
        );
    }
}
