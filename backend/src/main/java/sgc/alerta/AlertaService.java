package sgc.alerta;

import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    public Optional<Alerta> porCodigo(Long codigo) {
        return alertaRepo.findById(codigo);
    }

    public List<Alerta> porUnidadeDestino(Long codigoUnidade) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade);
    }

    public Page<Alerta> porUnidadeDestinoPaginado(Long codigoUnidade, Pageable pageable) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    public Optional<AlertaUsuario> alertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    public Optional<LocalDateTime> dataHoraLeituraAlertaUsuario(Long codigoAlerta, String usuarioTitulo) {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(codigoAlerta)
                .usuarioTitulo(usuarioTitulo)
                .build();

        return alertaUsuarioRepo.findById(chave).map(AlertaUsuario::getDataHoraLeitura);
    }

    public List<AlertaUsuario> alertasUsuarios(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.findByUsuarioAndAlertas(usuarioTitulo, alertaCodigos);
    }

    @Transactional
    public AlertaUsuario salvarAlertaUsuario(AlertaUsuario alertaUsuario) {
        return alertaUsuarioRepo.save(alertaUsuario);
    }

    @Transactional
    public Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }
}
