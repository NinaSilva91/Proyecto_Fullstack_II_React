// main.tsx: acá conecto React con el <div id="root"> del index.html
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "animate.css"; // la misma librería de animaciones que usaba antes (animate__fadeIn)
import "./index.css";
import App from "./App.tsx";

import { AuthProvider, type AuthProviderProps } from 'react-oidc-context'
import './index.css'


createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);



const cognitoAuthConfig: AuthProviderProps = {
  // En local, VITE_OIDC_* (ver .env.development.local) apuntan al servidor OIDC falso.
  authority:
    import.meta.env.VITE_OIDC_AUTHORITY ??
    'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_hrlCS9C2m',
  client_id: import.meta.env.VITE_OIDC_CLIENT_ID ?? '2vlq37kfr5c7vfo6a6sve08es3',
  redirect_uri:
    import.meta.env.VITE_OIDC_REDIRECT_URI ??
    'https://d84l1y8p4kdic.cloudfront.net',
  response_type: 'code',
  scope: 'email openid phone',
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname)
  },
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...cognitoAuthConfig}>
      <App />
    </AuthProvider>
  </StrictMode>,
)