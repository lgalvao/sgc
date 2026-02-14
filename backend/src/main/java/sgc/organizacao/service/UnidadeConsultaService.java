package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.List;
import java.util.Map;
import sgc.comum.repo.ComumRepo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadeConsultaService {
    private final UnidadeRepo unidadeRepo;
    private final ComumRepo repo;

    public Unidade buscarPorId(Long codigo) {
        return repo.buscar(Unidade.class, Map.of("codigo", codigo, "situacao", SituacaoUnidade.ATIVA));
    }

    public Unidade buscarPorSigla(String sigla) {
        return repo.buscarPorSigla(Unidade.class, sigla);
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
