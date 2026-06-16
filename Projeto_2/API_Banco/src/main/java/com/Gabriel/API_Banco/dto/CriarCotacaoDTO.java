package com.Gabriel.API_Banco.dto;

import com.Gabriel.API_Banco.model.enums.TipoLente;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CriarCotacaoDTO {

    private CriarProdutoDTO produto;
    private Long idUsuario;
    private Long idLoja;

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
