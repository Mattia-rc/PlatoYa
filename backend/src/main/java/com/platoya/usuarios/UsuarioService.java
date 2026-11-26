package com.platoya.usuarios;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platoya.common.error.NegocioException;
import com.platoya.common.error.RecursoNoEncontradoException;

@Service
@Transactional
public class UsuarioService {

    static final String RESTRICCION_NOMBRE_USUARIO = "uq_usuarios_nombre_usuario";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByNombreUsuarioAsc().stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse crear(UsuarioAltaRequest request) {
        Usuario usuario = new Usuario(request.nombre(), request.usuario(),
                passwordEncoder.encode(request.contrasena()), request.rol());
        // Sin consulta previa: la restricción única también cubre dos altas simultáneas.
        try {
            return UsuarioResponse.de(usuarios.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException violacion
                    && RESTRICCION_NOMBRE_USUARIO.equals(violacion.getConstraintName())) {
                throw new NegocioException(null,
                        "Ya existe un usuario con ese nombre de usuario. Si está dado de baja, podés reactivarlo.");
            }
            throw ex;
        }
    }

    public UsuarioResponse modificar(Long id, UsuarioModificacionRequest request, Long idActual) {
        Usuario usuario = buscarParaActualizar(id);
        Rol rolAnterior = usuario.getRol();
        // La entidad rechaza primero al usuario dado de baja (RN-23). Si después falla el control
        // del rol propio, la excepción descarta el cambio con el rollback.
        usuario.modificar(request.nombre(), request.rol());
        if (id.equals(idActual) && request.rol() != rolAnterior) {
            throw new NegocioException(null, "No podés cambiar tu propio rol.");
        }
        if (request.contrasena() != null) {
            usuario.asignarPasswordHash(passwordEncoder.encode(request.contrasena()));
        }
        return UsuarioResponse.de(usuario);
    }

    public void darDeBaja(Long id, Long idActual) {
        Usuario usuario = buscarParaActualizar(id);
        if (id.equals(idActual)) {
            throw new NegocioException(null, "No podés darte de baja a vos mismo.");
        }
        usuario.darDeBaja(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public UsuarioResponse reactivar(Long id) {
        Usuario usuario = buscarParaActualizar(id);
        usuario.reactivar();
        return UsuarioResponse.de(usuario);
    }

    private Usuario buscarParaActualizar(Long id) {
        return usuarios.findByIdParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
    }
}
