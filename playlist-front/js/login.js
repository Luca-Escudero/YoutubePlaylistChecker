import { loginUser, redirectToDashboard, getSession } from "./modules/auth.js";
import { showError, hideError } from "./modules/utils.js";

const form = document.getElementById("form-login");
const errorBox = document.getElementById("error");
const logButton = document.getElementById("log-button");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideError(errorBox);

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();

  logButton.disabled = true;
  logButton.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Ingresando...`;

  try {
    const session = await loginUser(email, password);
    redirectToDashboard(session);
  } catch (error) {
    console.error(error);
    showError(errorBox, error.message || "Credenciales incorrectas. Verificá tu email y contraseña.");
  } finally {
    logButton.disabled = false;
    logButton.innerHTML = `<i class="bi bi-box-arrow-in-right me-1"></i> Iniciar Sesión`;
  }
});
