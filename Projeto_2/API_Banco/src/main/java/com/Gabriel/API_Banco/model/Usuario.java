package com.Gabriel.API_Banco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Entity
@Data
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @OneToOne(mappedBy = "dono", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private Loja loja;

    @Column(name = "nome", unique = true)
    private String nome;

    @Column(name = "email", unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 100, nullable = false)
    private String senha;

    @Column(name = "tipo_usuario")
    private String tipoUsuario;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Produto> produtos;

    @Column(name = "url_foto")
    private String fotoUrl;

    private Boolean aceitouTermos = false;

    private String versaoTermos;

    private LocalDateTime dataAceiteTermos;

    @JsonIgnore
    private String tokenRecuperacao;

    @JsonIgnore
    private LocalDateTime expiracaoTokenRecuperacao;

    @Transient
    private String captchaToken;

}
