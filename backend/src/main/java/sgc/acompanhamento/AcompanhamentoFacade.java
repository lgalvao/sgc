package sgc.acompanhamento;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Perfil;
import sgc.painel.PainelFacade;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcompanhamentoFacade {
    private final AnaliseFacade analiseFacade;
    private final AlertaFacade alertaFacade;
    private final PainelFacade painelFacade;

    public List<Analise> listarAnalisesPorSubprocesso(Long codigoSubprocesso, TipoAnalise tipoAnalise) {
        return analiseFacade.listarPorSubprocesso(codigoSubprocesso, tipoAnalise);
    }

    public Analise criarAnalise(Subprocesso subprocesso, CriarAnaliseCommand comando) {
        return analiseFacade.criarAnalise(subprocesso, comando);
    }

    public List<AlertaDto> listarAlertasPorUsuario(String usuarioTitulo) {
        return alertaFacade.listarAlertasPorUsuario(usuarioTitulo);
    }

    public List<AlertaDto> listarAlertasNaoLidos(String usuarioTitulo) {
        return alertaFacade.listarAlertasNaoLidos(usuarioTitulo);
    }

    public void marcarAlertasComoLidos(String usuarioTitulo, List<Long> codigos) {
        alertaFacade.marcarComoLidos(usuarioTitulo, codigos);
    }

    public Page<ProcessoResumoDto> listarProcessosPainel(Perfil perfil, Long codigoUnidade, Pageable pageable) {
        return painelFacade.listarProcessos(perfil, codigoUnidade, pageable);
    }

    public Page<AlertaDto> listarAlertasPainel(String usuarioTitulo, Long codigoUnidade, Pageable pageable) {
        return painelFacade.listarAlertas(usuarioTitulo, codigoUnidade, pageable);
    }
}
