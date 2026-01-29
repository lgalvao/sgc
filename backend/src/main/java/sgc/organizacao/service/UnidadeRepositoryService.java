package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnidadeRepositoryService {
    private final UnidadeRepo unidadeRepo;

    @Transactional(readOnly = true)
    public Optional<Unidade> findById(Long codigo) {
        return unidadeRepo.findById(codigo);
    }

    @Transactional(readOnly = true)
    public Unidade buscarPorId(Long codigo) {
        return unidadeRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigo));
    }

    @Transactional(readOnly = true)
    public Optional<Unidade> findBySigla(String sigla) {
        return unidadeRepo.findBySigla(sigla);
    }

    @Transactional(readOnly = true)
    public List<Unidade> findAllById(List<Long> codigos) {
        return unidadeRepo.findAllById(codigos);
    }

    @Transactional(readOnly = true)
    public List<Unidade> findAllWithHierarquia() {
        return unidadeRepo.findAllWithHierarquia();
    }

    @Transactional(readOnly = true)
    public List<String> findSiglasByCodigos(List<Long> codigos) {
        return unidadeRepo.findSiglasByCodigos(codigos);
    }

    @Transactional
    public Unidade salvar(Unidade unidade) {
        return unidadeRepo.save(unidade);
    }
}
