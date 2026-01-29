package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.UnidadeMapaRepo;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnidadeMapaRepositoryService {
    private final UnidadeMapaRepo unidadeMapaRepo;

    @Transactional(readOnly = true)
    public List<Long> findAllUnidadeCodigos() {
        return unidadeMapaRepo.findAllUnidadeCodigos();
    }
}
