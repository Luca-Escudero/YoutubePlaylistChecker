import { registerUser, redirectToDashboard } from "./modules/auth.js";
import { showError, hideError, showSuccess } from "./modules/utils.js";

const form = document.getElementById("form-register");
const errorBox = document.getElementById("error");
const successBox = document.getElementById("success");
const registerButton = document.getElementById("register-button");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideError(errorBox);
  hideError(successBox);

  const nombre = document.getElementById("nombre").value.trim();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();

  registerButton.disabled = true;
  registerButton.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Registrando...`;

  try {
    const session = await registerUser({ nombre, email, password });
    showSuccess(successBox, "¡Cuenta creada con éxito! Redirigiendo...");
    setTimeout(() => {
      redirectToDashboard(session);
    }, 1200);
  } catch (error) {
    console.error(error);
    showError(errorBox, error.message || "Error al registrar el usuario. Intentá nuevamente.");
    registerButton.disabled = false;
    registerButton.innerHTML = `<i class="bi bi-person-plus me-1"></i> Registrarme`;
  }
});
