export function cleanErrorMessage(rawMessage) {
  if (!rawMessage) return "Ocurrió un error inesperado.";
  const str = String(rawMessage);

  if (str.includes("API_KEY_INVALID") || str.includes("API key not valid") || str.includes("YOUTUBE_API_KEY")) {
    return "🔑 <strong>API Key no válida:</strong> La variable <code>YOUTUBE_API_KEY</code> configurada en el archivo <code>.env</code> no es válida o está usando el valor por defecto. Por favor, asigná una API Key activa de <em>YouTube Data API v3</em> en tu archivo <code>.env</code>.";
  }

  if (str.includes("404") || str.includes("notFound") || str.includes("No se encontró")) {
    return "🔍 <strong>Playlist no encontrada:</strong> Comprobá que la URL o el ID ingresado sea correcto y que la playlist sea pública en YouTube.";
  }

  if (str.includes("Failed to fetch") || str.includes("NetworkError") || str.includes("ERR_CONNECTION_REFUSED")) {
    return "🔌 <strong>Error de Conexión:</strong> No se pudo conectar con el servidor backend. Verificá que esté ejecutándose en <code>http://localhost:8080</code>.";
  }

  // Si contiene trazas de JSON o etiquetas <EOL>, limpiamos el mensaje
  let cleaned = str.split(": 400 Bad Request:")[0].replace(/\{<EOL>[\s\S]*/g, "").replace(/<EOL>/g, " ").trim();
  return cleaned;
}

export function showError(container, message) {
  if (!container) return;
  container.innerHTML = `<i class="bi bi-exclamation-triangle-fill me-2"></i>${cleanErrorMessage(message)}`;
  container.style.display = "block";
}

export function hideError(container) {
  if (!container) return;
  container.textContent = "";
  container.style.display = "none";
}

export function showSuccess(container, message) {
  if (!container) return;
  container.innerHTML = `<i class="bi bi-check-circle-fill me-2"></i>${message}`;
  container.style.display = "block";
}

export function formatDate(dateString) {
  if (!dateString) return "-";
  try {
    const date = new Date(dateString);
    return date.toLocaleString("es-AR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  } catch (e) {
    return dateString;
  }
}

export function extractPlaylistId(urlOrId) {
  if (!urlOrId) return "";
  const trimmed = urlOrId.trim();
  if (trimmed.includes("list=")) {
    const urlParams = new URLSearchParams(trimmed.split("?")[1]);
    return urlParams.get("list") || trimmed;
  }
  return trimmed;
}
