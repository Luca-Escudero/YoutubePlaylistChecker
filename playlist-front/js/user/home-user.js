import { requireAuth, getSession, logout } from "../modules/auth.js";
import { showError, hideError, formatDate, extractPlaylistId } from "../modules/utils.js";
import { verificarPlaylistUrl } from "../services/verificacionService.js";
import { getMisConsultas, getConsultaPorId } from "../services/consultasService.js";
import { getTodasPlaylists, eliminarPlaylist } from "../services/playlistService.js";

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;

  const session = getSession();
  if (session) {
    document.getElementById("user-display-name").textContent = session.nombre || session.email;
    document.getElementById("navbar-user-email").textContent = session.email;
    document.getElementById("dropdown-user-name").textContent = session.nombre || "Usuario";
    document.getElementById("dropdown-user-email").textContent = session.email;
  }

  document.getElementById("logout-button").addEventListener("click", () => {
    logout();
  });

  // Sidebar Tab Navigation
  const navLinks = document.querySelectorAll("#user-nav-menu .nav-link");
  const sections = document.querySelectorAll(".content-section");
  const sectionTitle = document.getElementById("section-title");

  const TITLES = {
    "section-verificar": `<i class="bi bi-search text-danger me-2"></i>Verificar Playlist de YouTube`,
    "section-consultas": `<i class="bi bi-journal-text text-warning me-2"></i>Mis Consultas Realizadas`,
    "section-playlists": `<i class="bi bi-collection-play text-primary me-2"></i>Playlists Registradas`,
  };

  navLinks.forEach((link) => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      const targetSectionId = link.getAttribute("data-section");

      navLinks.forEach((l) => {
        l.classList.remove("active");
        l.classList.add("text-white-50");
      });
      link.classList.add("active");
      link.classList.remove("text-white-50");

      sections.forEach((sec) => {
        sec.style.display = sec.id === targetSectionId ? "block" : "none";
      });

      if (TITLES[targetSectionId]) {
        sectionTitle.innerHTML = TITLES[targetSectionId];
      }

      if (targetSectionId === "section-consultas") {
        cargarConsultas();
      } else if (targetSectionId === "section-playlists") {
        cargarPlaylists();
      }
    });
  });

  // Verification Form
  const formVerificar = document.getElementById("form-verificar");
  const playlistInput = document.getElementById("playlist-input");
  const btnVerificar = document.getElementById("btn-verificar");
  const errorBox = document.getElementById("verificar-error");
  const resultsBox = document.getElementById("verification-results");

  formVerificar.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideError(errorBox);

    const rawInput = playlistInput.value.trim();
    if (!rawInput) return;

    btnVerificar.disabled = true;
    btnVerificar.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Verificando...`;

    try {
      const data = await verificarPlaylistUrl(rawInput);
      mostrarResultadosVerificacion(data);
    } catch (err) {
      console.error(err);
      showError(errorBox, err.message || "Error al verificar la playlist. Asegúrate de que la URL o ID sea correcta y pública.");
      resultsBox.style.display = "none";
    } finally {
      btnVerificar.disabled = false;
      btnVerificar.innerHTML = `<i class="bi bi-play-circle me-1"></i> Verificar Estado`;
    }
  });

  function mostrarResultadosVerificacion(data) {
    const { playlist, videos, cantidadVideos, disponibles, noDisponibles } = data;

    document.getElementById("stat-total").textContent = cantidadVideos || 0;
    document.getElementById("stat-disponibles").textContent = disponibles || 0;
    document.getElementById("stat-no-disponibles").textContent = noDisponibles || 0;

    document.getElementById("playlist-title-header").textContent = playlist?.titulo || "Playlist de YouTube";
    document.getElementById("playlist-id-badge").textContent = playlist?.youtubeId || "";

    const tbody = document.getElementById("videos-table-body");
    tbody.innerHTML = "";

    if (!videos || videos.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No se encontraron videos en la playlist.</td></tr>`;
    } else {
      videos.forEach((vid, index) => {
        const tr = document.createElement("tr");

        const isDisponible = vid.disponible !== false;
        const statusBadge = isDisponible
          ? `<span class="badge badge-disponible"><i class="bi bi-check-circle-fill me-1"></i>Disponible</span>`
          : `<span class="badge badge-no-disponible"><i class="bi bi-x-circle-fill me-1"></i>No disponible</span>`;

        const thumbUrl = vid.youtubeId
          ? `https://i.ytimg.com/vi/${vid.youtubeId}/hqdefault.jpg`
          : "/assets/img/user1-128x128.jpg";

        const videoLink = vid.youtubeId
          ? `<a href="https://www.youtube.com/watch?v=${vid.youtubeId}" target="_blank" class="btn btn-sm btn-outline-danger"><i class="bi bi-box-arrow-up-right"></i> Ver</a>`
          : "-";

        tr.innerHTML = `
          <td>${index + 1}</td>
          <td>
            <img src="${thumbUrl}" class="video-thumb" alt="Thumbnail" onerror="this.src='https://via.placeholder.com/120x68?text=No+Thumb'"/>
          </td>
          <td>
            <div class="fw-bold">${vid.titulo || "Video de YouTube"}</div>
            <small class="text-muted">ID: ${vid.youtubeId || "-"}</small>
          </td>
          <td>${vid.canal || "Canal Desconocido"}</td>
          <td>${statusBadge}</td>
          <td>${videoLink}</td>
        `;
        tbody.appendChild(tr);
      });
    }

    resultsBox.style.display = "block";
    resultsBox.scrollIntoView({ behavior: "smooth" });
  }

  // Load Mis Consultas
  const btnRefreshConsultas = document.getElementById("btn-refresh-consultas");
  btnRefreshConsultas.addEventListener("click", cargarConsultas);

  async function cargarConsultas() {
    const loading = document.getElementById("consultas-loading");
    const empty = document.getElementById("consultas-empty");
    const tbody = document.getElementById("consultas-table-body");

    loading.style.display = "flex";
    empty.style.display = "none";
    tbody.innerHTML = "";

    try {
      const consultas = await getMisConsultas();
      loading.style.display = "none";

      if (!consultas || consultas.length === 0) {
        empty.style.display = "block";
        return;
      }

      consultas.forEach((c) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>#${c.id}</td>
          <td>${formatDate(c.fechaConsulta)}</td>
          <td>
            <div class="fw-bold">${c.playlist?.titulo || c.playlistTitulo || "Playlist"}</div>
            <small class="text-muted">ID: ${c.playlist?.youtubeId || "-"}</small>
          </td>
          <td><span class="badge bg-secondary">${c.cantidadVideos || 0}</span></td>
          <td><span class="badge bg-success">${c.disponibles || 0}</span></td>
          <td><span class="badge bg-danger">${c.noDisponibles || 0}</span></td>
          <td>
            <button class="btn btn-sm btn-primary btn-ver-detalle" data-id="${c.id}">
              <i class="bi bi-eye"></i> Detalle
            </button>
          </td>
        `;
        tbody.appendChild(tr);
      });

      document.querySelectorAll(".btn-ver-detalle").forEach((btn) => {
        btn.addEventListener("click", () => {
          const id = btn.getAttribute("data-id");
          mostrarModalConsulta(id);
        });
      });
    } catch (err) {
      console.error(err);
      loading.style.display = "none";
      tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">Error al cargar las consultas.</td></tr>`;
    }
  }

  // Detail Modal
  async function mostrarModalConsulta(id) {
    try {
      const consulta = await getConsultaPorId(id);
      if (!consulta) return;

      document.getElementById("modal-title").textContent = `Consulta #${consulta.id}`;
      document.getElementById("modal-playlist-name").textContent = consulta.playlist?.titulo || "Playlist";
      document.getElementById("modal-fecha").textContent = formatDate(consulta.fechaConsulta);
      document.getElementById("modal-disponibles").textContent = `${consulta.disponibles || 0} disponibles`;
      document.getElementById("modal-nodisponibles").textContent = `${consulta.noDisponibles || 0} no disponibles`;

      const tbody = document.getElementById("modal-videos-body");
      tbody.innerHTML = "";

      const videos = consulta.videos || [];
      if (videos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Sin detalles de videos.</td></tr>`;
      } else {
        videos.forEach((v, index) => {
          const isDisp = v.disponible !== false;
          tbody.innerHTML += `
            <tr>
              <td>${index + 1}</td>
              <td>${v.titulo || "-"}</td>
              <td>${v.canal || "-"}</td>
              <td>
                <span class="badge ${isDisp ? "bg-success" : "bg-danger"}">
                  ${isDisp ? "Disponible" : "No disponible"}
                </span>
              </td>
            </tr>
          `;
        });
      }

      const modalEl = new bootstrap.Modal(document.getElementById("consultaDetailModal"));
      modalEl.show();
    } catch (err) {
      console.error(err);
      alert("No se pudo cargar el detalle de la consulta.");
    }
  }

  // Load Playlists
  const btnRefreshPlaylists = document.getElementById("btn-refresh-playlists");
  btnRefreshPlaylists.addEventListener("click", cargarPlaylists);

  async function cargarPlaylists() {
    const loading = document.getElementById("playlists-loading");
    const empty = document.getElementById("playlists-empty");
    const grid = document.getElementById("playlists-grid");

    loading.style.display = "flex";
    empty.style.display = "none";
    grid.innerHTML = "";

    try {
      const playlists = await getTodasPlaylists();
      loading.style.display = "none";

      if (!playlists || playlists.length === 0) {
        empty.style.display = "block";
        return;
      }

      playlists.forEach((p) => {
        const col = document.createElement("div");
        col.className = "col-md-6 col-lg-4";
        col.innerHTML = `
          <div class="card h-100 border-0 shadow-sm">
            <div class="card-body">
              <div class="d-flex align-items-center gap-2 mb-2">
                <i class="bi bi-youtube text-danger fs-3"></i>
                <h6 class="fw-bold mb-0 text-truncate">${p.titulo || "Playlist de YouTube"}</h6>
              </div>
              <p class="small text-muted mb-2">ID: <code>${p.youtubeId}</code></p>
              <div class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
                <button class="btn btn-sm btn-outline-danger btn-verificar-rapido" data-youtubeid="${p.youtubeId}">
                  <i class="bi bi-arrow-repeat"></i> Verificar
                </button>
                <button class="btn btn-sm btn-outline-secondary btn-eliminar-playlist" data-id="${p.id}">
                  <i class="bi bi-trash"></i> Eliminar
                </button>
              </div>
            </div>
          </div>
        `;
        grid.appendChild(col);
      });

      document.querySelectorAll(".btn-verificar-rapido").forEach((btn) => {
        btn.addEventListener("click", () => {
          const ytid = btn.getAttribute("data-youtubeid");
          playlistInput.value = ytid;
          document.querySelector(`[data-section="section-verificar"]`).click();
          formVerificar.dispatchEvent(new Event("submit"));
        });
      });

      document.querySelectorAll(".btn-eliminar-playlist").forEach((btn) => {
        btn.addEventListener("click", async () => {
          const id = btn.getAttribute("data-id");
          if (confirm("¿Estás seguro de eliminar esta playlist registrada?")) {
            try {
              await eliminarPlaylist(id);
              cargarPlaylists();
            } catch (err) {
              alert("Error al eliminar la playlist.");
            }
          }
        });
      });

    } catch (err) {
      console.error(err);
      loading.style.display = "none";
      grid.innerHTML = `<div class="col-12 text-center text-danger py-4">Error al cargar las playlists.</div>`;
    }
  }
});
