package sgc.alerta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertaService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    public Page<Alerta> listarPorUsuario(String usuarioTitulo, Pageable pageable) {
        return alertaRepo.findByUsuarioDestino_TituloEleitoral(usuarioTitulo, pageable);
    }

    public Page<Alerta> listarPorUnidades(List<Long> unidadeIds, Pageable pageable) {
        return alertaRepo.findByUnidadeDestino_CodigoIn(unidadeIds, pageable);
    }

    public Page<Alerta> listarTodos(Pageable pageable) {
        return alertaRepo.findAll(pageable);
    }

    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaUsuarioRepo
                .findById(new AlertaUsuario.Chave(codigoAlerta, usuarioTitulo))
                .map(AlertaUsuario::getDataHoraLeitura);
    }
}
