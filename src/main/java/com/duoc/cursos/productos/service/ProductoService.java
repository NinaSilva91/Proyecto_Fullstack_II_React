package com.duoc.cursos.productos.service;

import com.duoc.cursos.common.exception.BusinessException;
import com.duoc.cursos.common.exception.ResourceNotFoundException;
import com.duoc.cursos.productos.dto.ProductoRequest;
import com.duoc.cursos.productos.dto.ProductoResponse;
import com.duoc.cursos.productos.model.Producto;
import com.duoc.cursos.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(ProductoService::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .map(ProductoService::aResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "codigo", codigo));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("CODIGO_DUPLICADO",
                    "Ya existe un producto con el código: " + request.getCodigo());
        }
        Producto producto = new Producto();
        return aResponse(productoRepository.save(aplicar(producto, request)));
    }

    @Transactional
    public ProductoResponse actualizar(String codigo, ProductoRequest request) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "codigo", codigo));
        // si cambia el código, verifico que el nuevo no esté ocupado por otro
        if (!codigo.equals(request.getCodigo()) && productoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("CODIGO_DUPLICADO",
                    "Ya existe un producto con el código: " + request.getCodigo());
        }
        return aResponse(productoRepository.save(aplicar(producto, request)));
    }

    @Transactional
    public void eliminar(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "codigo", codigo));
        productoRepository.delete(producto);
    }

    private static Producto aplicar(Producto producto, ProductoRequest request) {
        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setStockCritico(request.getStockCritico());
        producto.setCategoria(request.getCategoria());
        producto.setTipo(request.getTipo());
        producto.setImagen(request.getImagen());
        producto.setDestacado(Boolean.TRUE.equals(request.getDestacado()));
        return producto;
    }

    private static ProductoResponse aResponse(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getStockCritico(), p.getCategoria(), p.getTipo(),
                p.getImagen(), p.getDestacado());
    }
}
