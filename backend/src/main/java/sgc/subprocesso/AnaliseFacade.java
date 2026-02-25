package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.CriarAnaliseCommand;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;

/**
 * Facade para gerenciamento de análises de subprocessos.
 *
 * <p>Esta facade orquestra operações relacionadas a análises,
 * delegando a persistência para {@link SubprocessoService}.
 *
 * @see SubprocessoService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnaliseFacade {
    private final SubprocessoService subprocessoService;

    public List<Analise> listarPorSubprocesso(Long codSubprocesso, TipoAnalise tipoAnalise) {
        return subprocessoService.listarAnalisesPorSubprocesso(codSubprocesso, tipoAnalise);
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return subprocessoService.listarHistoricoCadastro(codSubprocesso);
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return subprocessoService.listarHistoricoValidacao(codSubprocesso);
    }

    public AnaliseHistoricoDto paraHistoricoDto(Analise analise) {
        return subprocessoService.paraHistoricoDto(analise);
    }

    @Transactional
    public Analise criarAnalise(Subprocesso subprocesso, CriarAnaliseCommand command) {
        return subprocessoService.criarAnalise(subprocesso, command);
    }

    @Transactional
    public void removerPorSubprocesso(Long codSubprocesso) {
        subprocessoService.removerAnalisesPorSubprocesso(codSubprocesso);
    }
}
