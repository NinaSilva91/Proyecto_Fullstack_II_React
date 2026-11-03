package com.duoc.cursos.pedidos.service;

import com.duoc.cursos.common.exception.BusinessException;
import com.duoc.cursos.common.exception.ResourceNotFoundException;
import com.duoc.cursos.pedidos.dto.PedidoRequest;
import com.duoc.cursos.pedidos.dto.PedidoResponse;
import com.duoc.cursos.pedidos.model.ItemPedido;
import com.duoc.cursos.pedidos.model.Pedido;
import com.duoc.cursos.pedidos.repository.PedidoRepository;
import com.duoc.cursos.productos.model.Producto;
import com.duoc.cursos.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    /**
     * Crea el pedido del carrito: valida stock, descuenta unidades y calcula el
     * total con los precios guardados en la base (nunca los que manda el cliente).
     */
    @Transactional
    public PedidoResponse crear(String usuarioId, String usuarioEmail, PedidoRequest request) {
        // junto cantidades por producto (por si el mismo código viene repetido)
        var cantidades = new LinkedHashMap<String, Integer>();
        for (var item : request.items()) {
            if (item.cantidad() == null || item.cantidad() <= 0) {
                throw new BusinessException("CANTIDAD_INVALIDA", "Cantidad inválida para el producto: " + item.codigo());
            }
            cantidades.merge(item.codigo(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setUsuarioEmail(usuarioEmail);
        int total = 0;

        for (var entrada : cantidades.entrySet()) {
            String codigo = entrada.getKey();
            int cantidad = entrada.getValue();

            Producto producto = productoRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "codigo", codigo));

            if (producto.getStock() < cantidad) {
                throw new BusinessException("STOCK_INSUFICIENTE",
                        "No hay stock suficiente de " + producto.getNombre() + " (disponible: " + producto.getStock() + ")");
            }
            producto.setStock(producto.getStock() - cantidad);

            ItemPedido linea = new ItemPedido();
            linea.setPedido(pedido);
            linea.setCodigoProducto(producto.getCodigo());
            linea.setNombre(producto.getNombre());
            linea.setPrecioUnitario(producto.getPrecio());
            linea.setCantidad(cantidad);
            pedido.getLineas().add(linea);
            total += producto.getPrecio() * cantidad;
        }

        pedido.setTotal(total);
        return aResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDelUsuario(String usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(PedidoService::aResponse)
                .toList();
    }

    private static PedidoResponse aResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getLineas().stream()
                        .map(l -> new PedidoResponse.Linea(l.getCodigoProducto(), l.getNombre(),
                                l.getPrecioUnitario(), l.getCantidad()))
                        .toList()
        );
    }
}
