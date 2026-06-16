package com.Gabriel.API_Banco.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.Gabriel.API_Banco.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.Gabriel.API_Banco.model.Cotacao;
import com.Gabriel.API_Banco.model.Loja;
import com.Gabriel.API_Banco.model.Usuario;
import com.Gabriel.API_Banco.model.enums.StatusCotacao;
import com.Gabriel.API_Banco.repository.CotacaoRepositorio;
import com.Gabriel.API_Banco.repository.LojaRepositorio;
import com.Gabriel.API_Banco.repository.UsuarioRepositorio;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CotacaoService {

    private final CotacaoRepositorio cotacaoRepo;
    private final UsuarioRepositorio usuarioRepo;
    private final LojaRepositorio lojaRepo;
    private final ProdutoService produtoService;
    private final EmailService emailService;
    private final ImageService imageService;

    public CotacaoService(CotacaoRepositorio cotacaoRepo,
                          UsuarioRepositorio usuarioRepo,
                          LojaRepositorio lojaRepo,
                          ProdutoService produtoService,
                          EmailService emailService,
                          ImageService imageService) {
        this.cotacaoRepo = cotacaoRepo;
        this.usuarioRepo = usuarioRepo;
        this.lojaRepo = lojaRepo;
        this.produtoService = produtoService;
        this.emailService = emailService;
        this.imageService = imageService;
    }

    public List<ListarCotacoesDTO> listarPorUsuario(Long idUsuario) {
        return cotacaoRepo.findByUsuarioId(idUsuario)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ListarCotacoesDTO> listarPorLoja(Long idLoja) {
        return cotacaoRepo.findByLojaId(idLoja)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Cotacao criarCotacao(CriarCotacaoDTO dto) {
        if (dto.getProduto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do produto são obrigatórios.");
        }

        if (dto.getProduto().getIdLente() == null && dto.getProduto().getIdArmacao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cotação deve conter lente, armação ou ambos.");
        }

        validarDadosReceita(dto);

        Usuario usuario = usuarioRepo.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Loja loja = lojaRepo.findById(dto.getIdLoja())
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        if (!"Comum".equals(usuario.getTipoUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas consumidores podem solicitar cotações.");
        }

        var produto = produtoService.criarProduto(dto.getProduto());

        Cotacao cotacao = new Cotacao();
        cotacao.setUsuario(usuario);
        cotacao.setLoja(loja);
        cotacao.setProduto(produto);
        cotacao.setValorBase(produto.getValor());
        cotacao.setDataCriacao(LocalDate.now());
        cotacao.setStatus(StatusCotacao.SOLICITADA);
        cotacao.setDataResposta(LocalDate.now().plusDays(7));

        cotacao.setEsfericoEsquerdo(dto.getEsfericoEsquerdo());
        cotacao.setEsfericoDireito(dto.getEsfericoDireito());
        cotacao.setCilindricoEsquerdo(dto.getCilindricoEsquerdo());
        cotacao.setCilindricoDireito(dto.getCilindricoDireito());
        cotacao.setEixoEsquerdo(dto.getEixoEsquerdo());
        cotacao.setEixoDireito(dto.getEixoDireito());
        cotacao.setAdicao(dto.getAdicao());
        cotacao.setTipoLenteDesejado(dto.getTipoLenteDesejado());
        cotacao.setTratamentosDesejados(dto.getTratamentosDesejados());
        cotacao.setObservacoes(dto.getObservacoes());
        cotacao.setObservacaoCliente(dto.getObservacoes());
        cotacao.setReceitaUrl(dto.getReceitaUrl());

        Cotacao cotacaoSalva = cotacaoRepo.save(cotacao);

        try {
            emailService.criacaoDeCotacao(cotacaoSalva);
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail de criação da cotação: " + e.getMessage());
        }

        return cotacaoSalva;
    }

    private void validarDadosReceita(CriarCotacaoDTO dto) {
        validarEixo(dto.getEixoEsquerdo(), "Eixo esquerdo");
        validarEixo(dto.getEixoDireito(), "Eixo direito");

        if (dto.getAdicao() != null && dto.getAdicao().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adição deve ser positiva ou zero.");
        }
    }

    private void validarEixo(Integer eixo, String nomeCampo) {
        if (eixo != null && (eixo < 0 || eixo > 180)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nomeCampo + " deve estar entre 0 e 180.");
        }
    }

    public Cotacao anexarReceita(Long idCotacao, Long idUsuario, MultipartFile arquivo) throws IOException {
        Cotacao cotacao = buscarCotacao(idCotacao);
        validarDonoDoUsuario(cotacao, idUsuario);

        String url = imageService.uploadReceita(arquivo);
        cotacao.setReceitaUrl(url);
        return cotacaoRepo.save(cotacao);
    }

    public Cotacao enviarProposta(Long id, ResponderCotacaoDTO dto, Long idLojaLogada) {
        Cotacao cotacao = buscarCotacao(id);
        validarIdDaLoja(cotacao, idLojaLogada);

        validarTransicao(
                cotacao.getStatus(),
                StatusCotacao.PROPOSTA_ENVIADA,
                Set.of(StatusCotacao.SOLICITADA, StatusCotacao.NEGOCIANDO)
        );

        if (dto.getValorFinal() == null || dto.getPrazoEntrega() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor final e prazo são obrigatórios para enviar proposta");
        }

        cotacao.setValorFinal(dto.getValorFinal());
        cotacao.setPrazoEntregaConfirmado(dto.getPrazoEntrega());
        cotacao.setObservacaoLoja(dto.getObservacaoLoja());
        cotacao.setDataResposta(LocalDate.now());
        cotacao.setStatus(StatusCotacao.PROPOSTA_ENVIADA);

        Cotacao salva = cotacaoRepo.save(cotacao);

        try {
            emailService.respostaCotacao(salva);
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail de resposta da cotação: " + e.getMessage());
        }

        return salva;
    }

    public Cotacao transicionarStatus(Long id, StatusTransicaoDTO dto) {
        Cotacao cotacao = buscarCotacao(id);
        StatusCotacao atual = cotacao.getStatus();
        StatusCotacao novo = dto.getNovoStatus();
        Long idAtor = dto.getIdAtor();

        switch (novo) {
            case NEGOCIANDO -> {
                validarTransicao(atual, novo, Set.of(StatusCotacao.SOLICITADA, StatusCotacao.PROPOSTA_ENVIADA));
                if (atual == StatusCotacao.SOLICITADA) validarDonoDaLoja(cotacao, idAtor);
                if (atual == StatusCotacao.PROPOSTA_ENVIADA) validarDonoDoUsuario(cotacao, idAtor);
            }
            case PROPOSTA_ENVIADA -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para enviar proposta use o endpoint /responder");
            case APROVADA -> {
                validarTransicao(atual, novo, Set.of(StatusCotacao.PROPOSTA_ENVIADA));
                validarDonoDoUsuario(cotacao, idAtor);
                cotacao.setDataAprovacao(LocalDate.now());
            }
            case AGUARDANDO_SINAL -> {
                validarTransicao(atual, novo, Set.of(StatusCotacao.APROVADA));
                validarDonoDaLoja(cotacao, idAtor);
            }
            case RESERVADA -> {
                validarTransicao(atual, novo, Set.of(StatusCotacao.APROVADA, StatusCotacao.AGUARDANDO_SINAL));
                validarDonoDaLoja(cotacao, idAtor);
            }
            case FINALIZADA -> {
                validarTransicao(atual, novo, Set.of(StatusCotacao.RESERVADA));
                validarParticipanteDaCotacao(cotacao, idAtor);
            }
            case CANCELADA -> {
                validarTransicao(atual, novo, Set.of(
                        StatusCotacao.SOLICITADA,
                        StatusCotacao.NEGOCIANDO,
                        StatusCotacao.PROPOSTA_ENVIADA,
                        StatusCotacao.APROVADA,
                        StatusCotacao.AGUARDANDO_SINAL,
                        StatusCotacao.RESERVADA
                ));
                validarParticipanteDaCotacao(cotacao, idAtor);
            }
            case REJEITADA -> {
                validarTransicao(atual, novo, Set.of(StatusCotacao.PROPOSTA_ENVIADA));
                validarDonoDoUsuario(cotacao, idAtor);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transição de status inválida");
        }

        cotacao.setStatus(novo);
        return cotacaoRepo.save(cotacao);
    }

    private Cotacao buscarCotacao(Long id) {
        return cotacaoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cotação não encontrada"));
    }

    private void validarTransicao(StatusCotacao atual, StatusCotacao novo, Set<StatusCotacao> permitidos) {
        if (!permitidos.contains(atual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível ir de " + atual + " para " + novo);
        }
    }

    private void validarParticipanteDaCotacao(Cotacao cotacao, Long idUsuario) {
        boolean ehCliente = cotacao.getUsuario() != null && cotacao.getUsuario().getId().equals(idUsuario);
        boolean ehDonoDaLoja = cotacao.getLoja() != null && cotacao.getLoja().getDono() != null && cotacao.getLoja().getDono().getId().equals(idUsuario);

        if (!ehCliente && !ehDonoDaLoja) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar esta cotação");
        }
    }

    private void validarIdDaLoja(Cotacao cotacao, Long idLoja) {
        if (!cotacao.getLoja().getId().equals(idLoja)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta cotação não pertence à sua loja");
        }
    }

    private void validarDonoDaLoja(Cotacao cotacao, Long idUsuario) {
        if (cotacao.getLoja() == null || cotacao.getLoja().getDono() == null || !cotacao.getLoja().getDono().getId().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta ação é exclusiva da loja responsável pela cotação");
        }
    }

    private void validarDonoDoUsuario(Cotacao cotacao, Long idAtor) {
        if (!cotacao.getUsuario().getId().equals(idAtor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta ação é exclusiva do cliente da cotação");
        }
    }

    private ListarCotacoesDTO toDTO(Cotacao cotacao) {
        var produto = cotacao.getProduto();

        ListarProdutosDTO produtoDTO = null;
        if (produto != null) {
            produtoDTO = new ListarProdutosDTO(
                    produto.getNomeProduto(),
                    produto.getLente() != null ? produto.getLente().getId() : null,
                    produto.getArmacao() != null ? produto.getArmacao().getId() : null,
                    produto.getGrauLenteDireita(),
                    produto.getGrauLenteEsquerda(),
                    produto.getValor(),
                    produto.getPrazoEntregaDias()
            );
        }

        return new ListarCotacoesDTO(
                cotacao.getId(),
                cotacao.getUsuario() != null ? cotacao.getUsuario().getId() : null,
                cotacao.getUsuario() != null ? cotacao.getUsuario().getNome() : null,
                cotacao.getUsuario() != null ? cotacao.getUsuario().getEmail() : null,
                cotacao.getLoja(),
                produtoDTO,
                cotacao.getValorBase(),
                cotacao.getValorFinal(),
                cotacao.getPrazoEntregaConfirmado(),
                cotacao.getDataCriacao(),
                cotacao.getDataResposta(),
                cotacao.getDataAprovacao(),
                cotacao.getObservacaoCliente(),
                cotacao.getObservacaoLoja(),
                cotacao.getStatus(),
                cotacao.getEsfericoEsquerdo(),
                cotacao.getEsfericoDireito(),
                cotacao.getCilindricoEsquerdo(),
                cotacao.getCilindricoDireito(),
                cotacao.getEixoEsquerdo(),
                cotacao.getEixoDireito(),
                cotacao.getAdicao(),
                cotacao.getTipoLenteDesejado(),
                cotacao.getTratamentosDesejados(),
                cotacao.getObservacoes(),
                cotacao.getReceitaUrl()
        );
    }
}




