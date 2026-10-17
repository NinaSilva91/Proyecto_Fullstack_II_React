// App.tsx: acá armé el "centro" de la tienda.
//  1) Guardo el carrito y la sesión (con useState) y se los paso por props a las páginas que los necesitan.
//  2) Dibujo el menú (siempre visible) y la página que corresponde a la dirección.
//  3) <Switch> mira la dirección (/productos, /carrito...) y muestra solo la <Route> que coincide.
import { useEffect, useState } from "react";
import { Route, Router, Switch } from "wouter";
import Footer from "./components/footer/Footer";
import Menu from "./components/menu/Menu";
import { productos, type Producto } from "./data/productos";
import Carrito from "./pages/carrito/Carrito";
import Contacto from "./pages/contacto/Contacto";
import Home from "./pages/home/Home";
import Login from "./pages/login/Login";
import NotFound from "./pages/not-found/NotFound";
import ProductoDetalle from "./pages/producto-detalle/ProductoDetalle";
import Productos from "./pages/productos/Productos";
import Register from "./pages/register/Register";
import { agregarItem, cambiarCantidad, contarUnidades, quitarItem, type ItemCarrito } from "./utils/carrito";
import { borrarSesion, guardarCarrito, leerCarrito, leerSesion, type Sesion } from "./utils/storage";

function App() {
  // acá arranco con lo que haya guardado en el navegador, así no se pierde al recargar
  const [carrito, setCarrito] = useState<ItemCarrito[]>(leerCarrito);
  const [sesion, setSesion] = useState<Sesion | null>(leerSesion);

  // cada vez que el carrito cambia, lo guardo en LocalStorage
  useEffect(() => {
    guardarCarrito(carrito);
  }, [carrito]);

  const handleAgregar = (producto: Producto, cantidad = 1) =>
    setCarrito((actual) => agregarItem(actual, producto, cantidad));

  // para cambiar la cantidad necesito saber el stock del producto, por eso lo busco en la lista
  const handleCambiarCantidad = (codigo: string, cantidad: number) => {
    const stock = productos.find((p) => p.codigo === codigo)?.stock ?? 1;
    setCarrito((actual) => cambiarCantidad(actual, codigo, cantidad, stock));
  };

  const handleQuitar = (codigo: string) => setCarrito((actual) => quitarItem(actual, codigo));
  const handleVaciar = () => setCarrito([]);

  const handleLogout = () => {
    borrarSesion();
    setSesion(null);
  };

  return (
    <Router>
      {/* acá dejé mi grilla de 12 columnas: en computador el menú usa 3 (2 en pantallas muy grandes)
          y el contenido usa el resto; en celular todo va en una sola columna */}
      <div className="min-h-screen lg:grid lg:grid-cols-12">
        <Menu sesion={sesion} cantidadCarrito={contarUnidades(carrito)} onLogout={handleLogout} />

        <main className="p-4 sm:p-6 lg:col-span-9 lg:p-10 xl:col-span-10">
          <Switch>
            <Route path="/">
              <Home onAgregar={handleAgregar} />
            </Route>
            <Route path="/productos">
              <Productos onAgregar={handleAgregar} />
            </Route>
            <Route path="/productos/:codigo">
              <ProductoDetalle onAgregar={handleAgregar} />
            </Route>
            <Route path="/carrito">
              <Carrito
                carrito={carrito}
                sesion={sesion}
                onCambiarCantidad={handleCambiarCantidad}
                onQuitar={handleQuitar}
                onVaciar={handleVaciar}
              />
            </Route>
            <Route path="/login">
              <Login onLogin={setSesion} />
            </Route>
            <Route path="/register" component={Register} />
            <Route path="/contacto" component={Contacto} />
            {/* esta Route sin "path" atrapa cualquier dirección que no exista (página 404) */}
            <Route component={NotFound} />
          </Switch>
          <Footer />
        </main>
      </div>
    </Router>
  );
}

export default App;
