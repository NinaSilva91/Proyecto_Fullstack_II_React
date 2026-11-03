package com.duoc.cursos.productos;

import com.duoc.cursos.common.dto.ApiResponse;
import com.duoc.cursos.productos.dto.ProductoRequest;
import com.duoc.cursos.productos.dto.ProductoResponse;
import com.duoc.cursos.productos.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD del catálogo. La consulta (GET) es pública para que la tienda
 * muestre los productos sin haber iniciado sesión; el resto pide token.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listar()));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.obtenerPorCodigo(codigo)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok(productoService.crear(request), "Producto creado exitosamente"));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(@PathVariable String codigo,
                                                                    @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.actualizar(codigo, request),
                "Producto actualizado exitosamente"));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String codigo) {
        productoService.eliminar(codigo);
        return ResponseEntity.ok(ApiResponse.ok(null, "Producto eliminado exitosamente"));
    }
}
