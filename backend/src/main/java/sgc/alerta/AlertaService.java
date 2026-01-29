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

    /**
     * Salva um alerta.
     *
     * @param alerta alerta a salvar
     * @return alerta salvo
     */
    @Transactional
    public Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }

    /**
     * Busca alertas por unidade de destino.
     *
     * @param codigoUnidade código da unidade de destino
     * @return lista de alertas
     */
    public List<Alerta> buscarPorUnidadeDestino(Long codigoUnidade) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade);
    }

    /**
     * Busca alertas por unidade de destino com paginação.
     *
     * @param codigoUnidade código da unidade de destino
     * @param pageable configuração de paginação
     * @return página de alertas
     */
    public Page<Alerta> buscarPorUnidadeDestino(Long codigoUnidade, Pageable pageable) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    /**
     * Busca um alerta por código.
     *
     * @param codigo código do alerta
     * @return alerta encontrado, se existir
     */
    public Optional<Alerta> buscarPorCodigo(Long codigo) {
        return alertaRepo.findById(codigo);
    }

    /**
     * Busca um AlertaUsuario por chave.
     *
     * @param chave chave composta do AlertaUsuario
     * @return AlertaUsuario encontrado, se existir
     */
    public Optional<AlertaUsuario> buscarAlertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    /**
     * Salva um AlertaUsuario.
     *
     * @param alertaUsuario AlertaUsuario a salvar
     * @return AlertaUsuario salvo
     */
    @Transactional
    public AlertaUsuario salvarAlertaUsuario(AlertaUsuario alertaUsuario) {
        return alertaUsuarioRepo.save(alertaUsuario);
    }

    /**
     * Busca AlertaUsuario por usuário e lista de alertas.
     * Usado para buscar status de leitura em lote, evitando N+1.
     *
     * @param usuarioTitulo título do usuário
     * @param alertaCodigos lista de códigos de alertas
     * @return lista de AlertaUsuario
     */
    public List<AlertaUsuario> buscarPorUsuarioEAlertas(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.findByUsuarioAndAlertas(usuarioTitulo, alertaCodigos);
    }

    /**
     * Busca data/hora de leitura de um alerta para um usuário.
     *
     * @param codigoAlerta código do alerta
     * @param usuarioTitulo título do usuário
     * @return data/hora de leitura, se o alerta foi lido
     */
    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaUsuarioRepo
                .findById(AlertaUsuario.Chave.builder()
                        .alertaCodigo(codigoAlerta)
                        .usuarioTitulo(usuarioTitulo)
                        .build())
                .map(AlertaUsuario::getDataHoraLeitura);
    }
}
