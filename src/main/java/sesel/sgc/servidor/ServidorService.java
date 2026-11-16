package sesel.sgc.servidor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServidorService {
    private final ServidorRepository servidorRepository;

    public ServidorService(ServidorRepository servidorRepository) {
        this.servidorRepository = servidorRepository;
    }

    public List<Servidor> listarPorUnidade(UUID unidadeId) {
        return servidorRepository.findByUnidadeId(unidadeId);
    }
}