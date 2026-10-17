// main.tsx: acá conecto React con el <div id="root"> del index.html
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "animate.css"; // la misma librería de animaciones que usaba antes (animate__fadeIn)
import "./index.css";
import App from "./App.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
