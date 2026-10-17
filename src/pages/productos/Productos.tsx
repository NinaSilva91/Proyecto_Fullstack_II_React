// PÁGINA: PRODUCTOS (ruta "/productos")  <- mi productos.html de antes
// Acá muestro todo el catálogo y lo puedo filtrar por maid con la lista desplegable.
import { useState } from "react";
import PageTitle from "../../components/page-title/PageTitle";
import ProductoCard from "../../components/producto-card/ProductoCard";
import { categorias, productos, type Producto } from "../../data/productos";

interface ProductosProps {
  onAgregar: (producto: Producto) => void;
}

const Productos = ({ onAgregar }: ProductosProps) => {
  // acá guardo qué categoría eligió la persona (empieza en "todas")
  const [categoria, setCategoria] = useState("todas");

  // filtro la lista cada vez que dibujo; si es "todas" muestro todo
  const visibles = categoria === "todas" ? productos : productos.filter((p) => p.categoria === categoria);

  return (
    <div className="mx-auto max-w-6xl">
      <PageTitle subtitulo="Encuentra productos inspirados en nuestras maids y en Secret Sweet Maid.">
        Nuestra Merch
      </PageTitle>

      {/* filtro por maid */}
      <div className="mb-5 flex flex-wrap items-center gap-3">
        <label htmlFor="filtro-categoria" className="font-bold">
          Ver merch de:
        </label>
        <select
          id="filtro-categoria"
          value={categoria}
          onChange={(e) => setCategoria(e.target.value)}
          className="entrada w-auto"
        >
          {categorias.map((c) => (
            <option key={c.valor} value={c.valor}>
              {c.etiqueta}
            </option>
          ))}
        </select>
        <span className="rounded-full bg-white/80 px-3 py-1 text-sm font-semibold">
          {visibles.length} {visibles.length === 1 ? "producto" : "productos"}
        </span>
      </div>

      {/* acá dibujo una tarjeta por cada producto; 1 columna en celular, 2 en tablet y 3 en computador */}
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {visibles.map((producto) => (
          <ProductoCard key={producto.codigo} producto={producto} onAgregar={onAgregar} />
        ))}
      </div>
    </div>
  );
};

export default Productos;
