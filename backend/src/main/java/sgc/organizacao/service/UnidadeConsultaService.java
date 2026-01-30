package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadeConsultaService {
    private final UnidadeRepo unidadeRepo;
    private static final String ENTIDADE_UNIDADE = "Unidade";

    public Unidade buscarPorId(Long codigo) {
        Unidade unidade = unidadeRepo.findById(codigo)
                .orElseThrow(ErroEntidadeNaoEncontrada.naoEncontrada(ENTIDADE_UNIDADE, codigo));
        if (unidade.getSituacao() != SituacaoUnidade.ATIVA) {
            throw new ErroEntidadeNaoEncontrada(ENTIDADE_UNIDADE, codigo);
        }
        return unidade;
    }

    public Unidade buscarPorSigla(String sigla) {
        return unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(ErroEntidadeNaoEncontrada.naoEncontrada("Unidade com sigla " + sigla + " não encontrada"));
    }

    public List<Unidade> buscarEntidadesPorIds(List<Long> codigos) {
        return unidadeRepo.findAllById(codigos);
    }

    public List<Unidade> buscarTodasEntidadesComHierarquia() {
        return unidadeRepo.findAllWithHierarquia();
    }

    public List<String> buscarSiglasPorIds(List<Long> codigos) {
        return unidadeRepo.findSiglasByCodigos(codigos);
    }
}
