package com.Gabriel.API_Banco.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.Gabriel.API_Banco.model.enums.StatusCotacao;

import com.Gabriel.API_Banco.model.enums.TipoLente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "tabela_cotacao")
public class Cotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotacao")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_produto")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_loja")
    private Loja loja;

    @Column(name = "valor_base")
    private BigDecimal valorBase;

    @Column(name = "valor_final")
    private BigDecimal valorFinal;

    @Column(name = "prazo_entrega_confirmado")
    private Integer prazoEntregaConfirmado;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    @Column(name = "data_resposta")
    private LocalDate dataResposta;

    @Column(name = "data_aprovacao")
    private LocalDate dataAprovacao;

    @Column(name = "observacao_cliente", columnDefinition = "TEXT")
    private String observacaoCliente;

    @Column(name = "observacao_loja", columnDefinition = "TEXT")
    private String observacaoLoja;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_cotacao", length = 50)
    private StatusCotacao status;

    @Column(name = "url_foto")
    private String fotoUrl;

    @ManyToOne
    @JoinColumn(name = "id_cupom_usuario")
    private CupomUsuario cupomAplicado;

    @Column(name = "valor_original", precision = 10, scale = 2)
    private BigDecimal valorOriginal;

    @Column(name = "valor_desconto", precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "esferico_esquerdo", precision = 5, scale = 2)
    private BigDecimal esfericoEsquerdo;

    @Column(name = "esferico_direito", precision = 5, scale = 2)
    private BigDecimal esfericoDireito;

    @Column(name = "cilindrico_esquerdo", precision = 5, scale = 2)
    private BigDecimal cilindricoEsquerdo;

    @Column(name = "cilindrico_direito", precision = 5, scale = 2)
    private BigDecimal cilindricoDireito;

    @Column(name = "eixo_esquerdo")
    private Integer eixoEsquerdo;

    @Column(name = "eixo_direito")
    private Integer eixoDireito;

    @Column(name = "adicao", precision = 5, scale = 2)
    private BigDecimal adicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_lente_desejado", length = 40)
    private TipoLente tipoLenteDesejado;

    @Column(name = "tratamentos_desejados", length = 500)
    private String tratamentosDesejados;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "receita_url", length = 1000)
    private String receitaUrl;
}

