package com.Gabriel.API_Banco.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.Gabriel.API_Banco.dto.AlterarSenhaDTO;

import com.Gabriel.API_Banco.dto.EditarUsuarioDTO;
import com.Gabriel.API_Banco.dto.ListarUsuariosDTO;
import com.Gabriel.API_Banco.dto.recuperaSenhaDTO;
import com.Gabriel.API_Banco.exceptions.UsuarioExceptions;
import com.Gabriel.API_Banco.repository.LojaRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.Gabriel.API_Banco.model.Usuario;
import com.Gabriel.API_Banco.repository.UsuarioRepositorio;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;



@Service
public class UsuarioService {

    private final UsuarioRepositorio r;
    private final LojaRepositorio lr;
    private final ImageService imageService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final EmailHttpService emailHttpService;
    private final TurnstileService turnstileService;

    @Value("${front.login.url}")
    private String frontLoginUrl;


    public UsuarioService(
            UsuarioRepositorio r,
            PasswordEncoder passwordEncoder,
            LojaRepositorio lr,
            ImageService imageService,
            EmailService emailService,
            EmailHttpService emailHttpService,
            TurnstileService turnstileService
    ) {
        this.r = r;
        this.lr = lr;
        this.imageService = imageService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.emailHttpService = emailHttpService;
        this.turnstileService = turnstileService;
    }




    public Usuario salvar(Usuario usuario) {

        if (!turnstileService.validarToken(usuario.getCaptchaToken())) {
            throw new RuntimeException("Falha na verificação anti-bot. Tente novamente.");
        }

        if (r.existsByEmail(usuario.getEmail())) {
            throw new UsuarioExceptions("Email já cadastrado");
        }

        if (r.existsByNome(usuario.getNome())) {
            throw new UsuarioExceptions("Nome de usuário já cadastrado");
        }

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new RuntimeException("Senha é obrigatória.");
        }



        if (usuario.getAceitouTermos() == null || !usuario.getAceitouTermos()) {
            throw new RuntimeException("É necessário aceitar os Termos de Uso e a Política de Privacidade.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        usuario.setVersaoTermos("v2");
        usuario.setDataAceiteTermos(LocalDateTime.now());

        return r.save(usuario);
    }

    public Optional<Usuario> consultarPorEmail(String email) {
        return r.findByEmail(email);
    }
    public Optional<Usuario> consultarPorNome(String nome){return r.findByNome(nome);}

    public Usuario editarUsuario(EditarUsuarioDTO dto) {

        Usuario usuario = r.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());

        return r.save(usuario);
    }

    public List<ListarUsuariosDTO> listarUsuarios() {
        return r.findAll().stream()
                .map(usuario -> new ListarUsuariosDTO(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getTipoUsuario()

                ))
                .toList();
    }

    public void alterarSenha(AlterarSenhaDTO dto) {
        if (dto.getIdUsuario() == null) {
            throw new RuntimeException("Usuário inválido.");
        }

        if (dto.getSenhaAtual() == null || dto.getSenhaAtual().isBlank()) {
            throw new RuntimeException("Senha atual é obrigatória.");
        }

        if (dto.getNovaSenha() == null || dto.getNovaSenha().isBlank()) {
            throw new RuntimeException("Nova senha é obrigatória.");
        }

        Usuario usuario = r.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String senhaNoBanco = usuario.getSenha();

        boolean senhaAtualCorreta;

        if (senhaNoBanco != null &&
                (senhaNoBanco.startsWith("$2a$") ||
                        senhaNoBanco.startsWith("$2b$") ||
                        senhaNoBanco.startsWith("$2y$"))) {

            senhaAtualCorreta = passwordEncoder.matches(dto.getSenhaAtual(), senhaNoBanco);

        } else {
            senhaAtualCorreta = dto.getSenhaAtual().equals(senhaNoBanco);
        }

        if (!senhaAtualCorreta) {
            throw new RuntimeException("Senha atual incorreta.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        r.save(usuario);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = r.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        r.delete(usuario);
    }

    //IMAGENSSSSSSS FINALMENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE//

    public String atualizarFoto(Long usuarioId, MultipartFile file) throws IOException {

        Usuario usuario = r.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String urlAntiga = usuario.getFotoUrl();
        String urlNova = imageService.uploadProfileImage(file);

        usuario.setFotoUrl(urlNova);
        r.save(usuario);

        if(urlAntiga != null &&  !urlAntiga.isBlank()) {
            imageService.deleteImage(urlAntiga);
        }

        return urlNova;
    }

    public String getFotoPerfil(Long id){
        Usuario usuario = r.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return usuario.getFotoUrl();
    }

    public ResponseEntity<?> recuperaSenha(recuperaSenhaDTO dto) {

        if (!turnstileService.validarToken(dto.getCaptchaToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Falha na verificação anti-bot. Tente novamente.");
        }

        try {
            if (dto.getEmail() == null || dto.getEmail().isBlank()
                    || dto.getNome() == null || dto.getNome().isBlank()) {
                return ResponseEntity.badRequest().body("Nome e e-mail são obrigatórios.");
            }

            Optional<Usuario> usuarioOpt = r.findByEmail(dto.getEmail());

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Email não encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            if (usuario.getNome() == null || !usuario.getNome().equalsIgnoreCase(dto.getNome())) {
                return ResponseEntity.badRequest().body("Nome de usuário não corresponde ao email informado");
            }

            String senhaTemporaria = gerarSenhaTemporaria();

            try {
                emailHttpService.enviarRecuperacaoSenha(
                        dto.getEmail(),
                        frontLoginUrl,
                        senhaTemporaria
                );

                usuario.setSenha(passwordEncoder.encode(senhaTemporaria));
                r.save(usuario);

                return ResponseEntity.ok("Email de recuperação enviado com sucesso!");

            } catch (Exception e) {
                e.printStackTrace();

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao enviar email de recuperação via Resend: "
                                + e.getClass().getSimpleName()
                                + " - "
                                + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno na recuperação: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage());
        }
    }

    private String gerarSenhaTemporaria() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        return "Vp@" + uuid.substring(0, 8);
    }


}
