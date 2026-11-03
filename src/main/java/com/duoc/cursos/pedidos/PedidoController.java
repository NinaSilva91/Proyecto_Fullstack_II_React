package com.duoc.cursos.pedidos;

import com.duoc.cursos.auth.repository.UsuarioRepository;
import com.duoc.cursos.common.dto.ApiResponse;
import com.duoc.cursos.pedidos.dto.PedidoRequest;
import com.duoc.cursos.pedidos.dto.PedidoResponse;
import com.duoc.cursos.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Pedidos de la tienda. Todo requiere sesión: el id del usuario sale del
 * token de Cognito, no del cuerpo de la petición.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> crear(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PedidoRequest request) {
        String usuarioId = jwt.getSubject();
        String email = usuarioRepository.findById(usuarioId)
                .map(u -> u.getEmail())
                .orElse(null);
        PedidoResponse pedido = pedidoService.crear(usuarioId, email, request);
        return ResponseEntity.status(201).body(ApiResponse.ok(pedido, "Pedido creado exitosamente"));
    }

    @GetMapping("/mios")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarMios(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listarDelUsuario(jwt.getSubject())));
    }
}
