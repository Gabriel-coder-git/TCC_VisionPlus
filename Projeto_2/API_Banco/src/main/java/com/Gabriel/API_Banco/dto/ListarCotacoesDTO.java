package com.Gabriel.API_Banco.dto;

import com.Gabriel.API_Banco.model.Loja;
import com.Gabriel.API_Banco.model.enums.StatusCotacao;
import com.Gabriel.API_Banco.model.enums.TipoLente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListarCotacoesDTO {
    private Long idCotacao;
    private Long idUsuario;
    private String nomeUsuario;
    private String emailUsuario;
    private Loja loja;
    private ListarProdutosDTO produto;
    private BigDecimal valorBase;
    private BigDecimal valorFinal;
    private Integer prazoEntrega;
    private LocalDate dataCriacao;
    private LocalDate dataResposta;
    private LocalDate dataAprovacao;
    private String obsCliente;
    private String obsLoja;
    private StatusCotacao status;

    private BigDecimal esfericoEsquerdo;
    private BigDecimal esfericoDireito;
    private BigDecimal cilindricoEsquerdo;
    private BigDecimal cilindricoDireito;
    private Integer eixoEsquerdo;
    private Integer eixoDireito;
    private BigDecimal adicao;
    private TipoLente tipoLenteDesejado;
    private String tratamentosDesejados;
    private String observacoes;
    private String receitaUrl;
}
