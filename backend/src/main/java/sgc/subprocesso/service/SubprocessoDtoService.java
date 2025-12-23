package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.internal.model.Analise;
import sgc.analise.internal.model.TipoAnalise;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.dto.ConhecimentoMapper;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoDtoService {
        private final SubprocessoRepo repositorioSubprocesso;
        private final MovimentacaoRepo repositorioMovimentacao;
        private final AtividadeRepo atividadeRepo;
        private final ConhecimentoRepo repositorioConhecimento;
        private final CompetenciaRepo competenciaRepo;
        private final AnaliseService analiseService;
        private final ConhecimentoMapper conhecimentoMapper;
        private final SubprocessoMapper subprocessoMapper;
        private final SubprocessoPermissoesService subprocessoPermissoesService;
        private final SgrhService sgrhService;
        private final SubprocessoDetalheMapper subprocessoDetalheMapper;
        private final MapaAjusteMapper mapaAjusteMapper;

        @Transactional(readOnly = true)
        public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil, Long codUnidadeUsuario) {
                log.debug("Obtendo detalhes para o subprocesso {}", codigo);
                if (perfil == null) {
                        log.warn("Perfil inválido para acesso aos detalhes do subprocesso.");
                        throw new ErroAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
                }

                Subprocesso sp = repositorioSubprocesso
                                .findById(codigo)
                                .orElseThrow(
                                                () -> new ErroEntidadeNaoEncontrada(
                                                                "Subprocesso não encontrado: %d"
                                                                                .formatted(codigo)));
                log.debug("Subprocesso encontrado: {}", sp.getCodigo());

                var authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                Usuario usuario = sgrhService.buscarUsuarioPorLogin(username);

                verificarPermissaoVisualizacao(sp, perfil, usuario);
                Usuario responsavel = sgrhService.buscarResponsavelVigente(sp.getUnidade().getSigla());

                Usuario titular = null;
                if (sp.getUnidade() != null && sp.getUnidade().getTituloTitular() != null) {
                        try {
                                titular = sgrhService.buscarUsuarioPorLogin(sp.getUnidade().getTituloTitular());
                        } catch (Exception e) {
                                log.warn("Erro ao buscar titular da unidade: {}", e.getMessage());
                        }
                }

                List<Movimentacao> movimentacoes = repositorioMovimentacao
                                .findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

                SubprocessoPermissoesDto permissoes = subprocessoPermissoesService.calcularPermissoes(sp, usuario);
                log.debug("Permissões calculadas: {}", permissoes);

                return subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes);
        }

        private void verificarPermissaoVisualizacao(Subprocesso sp, Perfil perfil, Usuario usuario) {
                boolean hasPerfil = usuario.getTodasAtribuicoes().stream().anyMatch(a -> a.getPerfil() == perfil);

                if (!hasPerfil) {
                        log.warn("Usuário não possui o perfil sSolicitado.");
                        throw new ErroAccessoNegado("Perfil inválido para o usuário.");
                }
                if (perfil == Perfil.ADMIN) return;

                Unidade unidadeAlvo = sp.getUnidade();
                if (unidadeAlvo == null) throw new ErroAccessoNegado("Unidade não identificada.");

                boolean hasPermission = usuario.getTodasAtribuicoes().stream()
                                .filter(a -> a.getPerfil() == perfil)
                                .anyMatch(a -> {
                                        Unidade unidadeUsuario = a.getUnidade();
                                        if (perfil == Perfil.GESTOR) {
                                                return isMesmaUnidadeOuSubordinada(unidadeAlvo, unidadeUsuario);
                                        } else if (perfil == Perfil.CHEFE || perfil == Perfil.SERVIDOR) {
                                                return unidadeAlvo.getCodigo().equals(unidadeUsuario.getCodigo());
                                        }
                                        return false;
                                });

                if (!hasPermission) throw new ErroAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
        }

        private boolean isMesmaUnidadeOuSubordinada(Unidade alvo, Unidade superior) {
                sgc.unidade.internal.model.Unidade atual = alvo;
                while (atual != null) {
                        if (atual.getCodigo().equals(superior.getCodigo())) {
                                return true;
                        }
                        atual = atual.getUnidadeSuperior();
                }
                return false;
        }

        @Transactional(readOnly = true)
        public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
                Subprocesso sp = repositorioSubprocesso
                                .findById(codSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

                List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
                if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
                        List<Atividade> atividades = atividadeRepo.findByMapaCodigoWithConhecimentos(sp.getMapa().getCodigo());
                        if (atividades == null) atividades = emptyList();

                        for (Atividade a : atividades) {
                                List<Conhecimento> ks = a.getConhecimentos();
                                List<ConhecimentoDto> ksDto = ks == null ? emptyList() : ks.stream().map(conhecimentoMapper::toDto).toList();

                                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                                                .codigo(a.getCodigo())
                                                .descricao(a.getDescricao())
                                                .conhecimentos(ksDto)
                                                .build());
                        }
                }

                return SubprocessoCadastroDto.builder()
                                .subprocessoCodigo(sp.getCodigo())
                                .unidadeSigla(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null)
                                .atividades(atividadesComConhecimentos)
                                .build();
        }

        @Transactional(readOnly = true)
        public SugestoesDto obterSugestoes(Long codSubprocesso) {
                Subprocesso sp = repositorioSubprocesso
                                .findById(codSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: %d"
                                                                .formatted(codSubprocesso)));

                return SugestoesDto.of(sp);
        }

        @Transactional(readOnly = true)
        public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
                Subprocesso sp = repositorioSubprocesso
                                .findById(codSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codSubprocesso));

                if (sp.getMapa() == null) {
                        throw new IllegalStateException("Subprocesso sem mapa associado.");
                }

                Long codMapa = sp.getMapa().getCodigo();

                Analise analise = analiseService.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO).stream()
                                .findFirst()
                                .orElse(null);

                List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);
                List<Atividade> atividades = atividadeRepo.findByMapaCodigo(codMapa);
                List<Conhecimento> conhecimentos = repositorioConhecimento.findByMapaCodigo(codMapa);

                return mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos);
        }

        @Transactional(readOnly = true)
        public List<SubprocessoDto> listar() {
                return repositorioSubprocesso.findAll().stream().map(subprocessoMapper::toDTO).toList();
        }

        @Transactional(readOnly = true)
        public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
                Subprocesso sp = repositorioSubprocesso
                                .findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o processo %d e unidade %d".formatted(codProcesso, codUnidade)));
                return subprocessoMapper.toDTO(sp);
        }

        @Transactional(readOnly = true)
        public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso) {
                Subprocesso sp = repositorioSubprocesso
                                .findById(codSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

                Usuario usuario = obterUsuarioAutenticado();
                return subprocessoPermissoesService.calcularPermissoes(sp, usuario);
        }

        @Transactional(readOnly = true)
        public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
                log.debug("Validando cadastro para disponibilização. Subprocesso: {}", codSubprocesso);
                Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

                List<ErroValidacaoDto> erros = new ArrayList<>();

                if (sp.getMapa() == null) {
                        return ValidacaoCadastroDto.builder()
                        .valido(false)
                        .erros(List.of(ErroValidacaoDto.builder()
                                                        .tipo("MAPA_INEXISTENTE")
                                                        .mensagem("O subprocesso não possui um mapa associado.")
                                                        .build()))
                        .build();
                }

                List<Atividade> atividades = atividadeRepo.findByMapaCodigoWithConhecimentos(sp.getMapa().getCodigo());

                if (atividades == null || atividades.isEmpty()) {
                        erros.add(ErroValidacaoDto.builder()
                                        .tipo("SEM_ATIVIDADES")
                                        .mensagem("O mapa não possui atividades cadastradas.")
                                        .build());
                } else {
                        for (Atividade atividade : atividades) {
                                // Valida se tem conhecimentos
                                long qtdConhecimentos = atividade.getConhecimentos() == null ? 0 : atividade.getConhecimentos().size();
                                if (qtdConhecimentos == 0) {
                                        erros.add(ErroValidacaoDto.builder()
                                                        .tipo("ATIVIDADE_SEM_CONHECIMENTO")
                                                        .atividadeCodigo(atividade.getCodigo())
                                                        .descricaoAtividade(atividade.getDescricao())
                                                        .mensagem("A atividade '" + atividade.getDescricao()
                                                                        + "' não possui conhecimentos associados.")
                                                        .build());
                                }
                        }
                }

                return ValidacaoCadastroDto.builder()
                                .valido(erros.isEmpty())
                                .erros(erros)
                                .build();
        }

        @Transactional(readOnly = true)
        public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
                log.debug("Obtendo status para o subprocesso {}", codSubprocesso);
                Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

                return SubprocessoSituacaoDto.builder()
                                .codigo(sp.getCodigo())
                                .situacao(sp.getSituacao())
                                .situacaoLabel(sp.getSituacao().getDescricao())
                                .build();
        }

        private Usuario obterUsuarioAutenticado() {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || authentication.getName() == null) {
                        throw new ErroAccessoNegado("Usuário não autenticado.");
                }
                String username = authentication.getName();
                return sgrhService.buscarUsuarioPorLogin(username);
        }
}
