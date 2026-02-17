package sgc.alerta;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsável pelas operações de persistência de alertas.
 *
 * <p>Este service encapsula o acesso aos repositórios {@link AlertaRepo} e {@link AlertaUsuarioRepo},
 * permitindo que {@link AlertaFacade} delegue operações de dados sem acessar repositórios diretamente.
 *
 * @see AlertaFacade
 * @see AlertaRepo
 * @see AlertaUsuarioRepo
 */
@Service
@RequiredArgsConstructor
public class AlertaService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    @Transactional
    public Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }

    public List<Alerta> buscarPorUnidadeDestino(Long codigoUnidade) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade);
    }

    public Page<Alerta> buscarPorUnidadeDestino(Long codigoUnidade, Pageable pageable) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    public Optional<Alerta> buscarPorCodigo(Long codigo) {
        return alertaRepo.findById(codigo);
    }

    public Optional<AlertaUsuario> buscarAlertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    @Transactional
    public AlertaUsuario salvarAlertaUsuario(AlertaUsuario alertaUsuario) {
        return alertaUsuarioRepo.save(alertaUsuario);
    }

    public List<AlertaUsuario> buscarPorUsuarioEAlertas(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.findByUsuarioAndAlertas(usuarioTitulo, alertaCodigos);
    }

    /**
     * Busca data/hora de leitura de um alerta para um usuário.
     *
     * @param codigoAlerta  código do alerta
     * @param usuarioTitulo título do usuário
     * @return data/hora de leitura, se o alerta foi lido
     */
    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(codigoAlerta)
                .usuarioTitulo(usuarioTitulo)
                .build();

        return alertaUsuarioRepo.findById(chave).map(AlertaUsuario::getDataHoraLeitura);
    }
}
