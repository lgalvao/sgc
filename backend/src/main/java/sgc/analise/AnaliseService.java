package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.AnaliseRepo;
import sgc.analise.modelo.TipoAnalise;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.analise.dto.CriarAnaliseRequestDto; // Added import

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnaliseService {
    private final AnaliseRepo analiseRepo;
    private final SubprocessoRepo subprocessoRepo;

    @Transactional(readOnly = true)
    public List<Analise> listarPorSubprocesso(Long codSubprocesso, TipoAnalise tipo) {
        if (subprocessoRepo.findById(codSubprocesso).isEmpty()) {
            throw new ErroDominioNaoEncontrado("Subprocesso", codSubprocesso);
        }
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso)
            .stream()
            .filter(a -> a.getTipo() == tipo)
            .toList();
    }

    @Transactional
    public Analise criarAnalise(CriarAnaliseRequestDto request) {

        Subprocesso sp = subprocessoRepo.findById(request.subprocessoCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso", request.subprocessoCodigo()));

        Analise a = new Analise();
        a.setSubprocesso(sp);
        a.setDataHora(LocalDateTime.now());
        a.setObservacoes(request.observacoes());
        a.setTipo(request.tipo());
        a.setAcao(request.acao());
        a.setUnidadeSigla(request.unidadeSigla());
        a.setAnalistaUsuarioTitulo(request.analistaUsuarioTitulo());
        a.setMotivo(request.motivo());

        return analiseRepo.save(a);
    }

    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseRepo.deleteBySubprocessoCodigo(subprocessoCodigo);
    }
}
