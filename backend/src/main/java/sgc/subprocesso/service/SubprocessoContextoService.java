package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.service.UnidadeService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoContextoService {

    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoDtoService subprocessoDtoService;
    private final SgrhService sgrhService;
    private final UnidadeService unidadeService;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final MapaCompletoMapper mapaCompletoMapper;
    private final AtividadeMapper atividadeMapper;

    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codSubprocesso) {
        log.debug("Montando contexto de edição para subprocesso {}", codSubprocesso);

        // 1. Obter Subprocesso Entity para acesso a relacionamentos básicos
        Subprocesso subprocesso = subprocessoRepo.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        // 2. Resolver usuário logado para permissões
        Usuario usuarioLogado = obterUsuarioAutenticado();
        Perfil perfilAtivo = determinarPerfilAtivo(usuarioLogado, subprocesso.getUnidade());

        // 3. Obter Subprocesso Detalhe DTO
        // Passa o código da unidade do usuário logado se ele tiver atribuição
        Long unidadeUsuarioCod = usuarioLogado.getUnidadeLotacao() != null ? usuarioLogado.getUnidadeLotacao().getCodigo() : null;

        // Se o perfil ativo for associado a uma unidade específica (Gestor/Chefe), usa essa unidade
        // para garantir que a validação de permissão no DtoService funcione corretamente.
        if (perfilAtivo != Perfil.ADMIN) {
             unidadeUsuarioCod = usuarioLogado.getTodasAtribuicoes().stream()
                    .filter(a -> a.getPerfil() == perfilAtivo)
                    .map(a -> a.getUnidade().getCodigo())
                    .findFirst()
                    .orElse(unidadeUsuarioCod);
        }

        SubprocessoDetalheDto subprocessoDto = subprocessoDtoService.obterDetalhes(
                codSubprocesso,
                perfilAtivo,
                unidadeUsuarioCod
        );

        // 4. Obter Unidade DTO
        UnidadeDto unidadeDto = unidadeService.buscarPorSigla(subprocesso.getUnidade().getSigla());

        // 5. Obter Mapa Completo (se existir)
        MapaCompletoDto mapaDto = null;
        if (subprocesso.getMapa() != null) {
            List<Competencia> competencias = competenciaRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo());
            mapaDto = mapaCompletoMapper.toDto(subprocesso.getMapa(), codSubprocesso, competencias);
        }

        // 6. Obter Atividades Disponíveis (se mapa existir)
        List<AtividadeDto> atividadesDto = List.of();
        if (subprocesso.getMapa() != null) {
            List<Atividade> atividades = atividadeRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo());
            atividadesDto = atividades.stream().map(atividadeMapper::toDto).toList();
        }

        return ContextoEdicaoDto.builder()
                .unidade(unidadeDto)
                .subprocesso(subprocessoDto)
                .mapa(mapaDto)
                .atividadesDisponiveis(atividadesDto)
                .build();
    }

    private Usuario obterUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ErroAccessoNegado("Usuário não autenticado.");
        }
        return sgrhService.buscarUsuarioPorLogin(authentication.getName());
    }

    private Perfil determinarPerfilAtivo(Usuario usuario, Unidade unidadeSubprocesso) {
        if (usuario.getTodasAtribuicoes().stream().anyMatch(a -> a.getPerfil() == Perfil.ADMIN)) {
            return Perfil.ADMIN;
        }

        boolean isGestor = usuario.getTodasAtribuicoes().stream()
                .anyMatch(a -> a.getPerfil() == Perfil.GESTOR
                        && isMesmaUnidadeOuSubordinada(unidadeSubprocesso, a.getUnidade()));
        if (isGestor) return Perfil.GESTOR;

        boolean isChefeOuServidor = usuario.getTodasAtribuicoes().stream()
                .anyMatch(a -> (a.getPerfil() == Perfil.CHEFE || a.getPerfil() == Perfil.SERVIDOR)
                        && a.getUnidade().getCodigo().equals(unidadeSubprocesso.getCodigo()));
        if (isChefeOuServidor) return Perfil.CHEFE;

        return usuario.getTodasAtribuicoes().stream()
                .findFirst()
                .map(sgc.sgrh.model.UsuarioPerfil::getPerfil)
                .orElseThrow(() -> new ErroAccessoNegado("Usuário sem perfil válido para acessar este subprocesso."));
    }

    private boolean isMesmaUnidadeOuSubordinada(Unidade alvo, Unidade superior) {
        Unidade atual = alvo;
        while (atual != null) {
            if (atual.getCodigo().equals(superior.getCodigo())) {
                return true;
            }
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }
}
