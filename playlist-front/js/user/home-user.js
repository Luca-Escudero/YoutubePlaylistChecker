import { requireAuth, getSession, logout } from "../modules/auth.js";
import { showError, hideError, formatDate, extractPlaylistId } from "../modules/utils.js";
import { verificarPlaylistUrl } from "../services/verificacionService.js";
import { getMisConsultas, getConsultaPorId, compararConsultas } from "../services/consultasService.js";
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

  let chartInstance = null;

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

  function getMotivoBadge(motivo, estado) {
    if (estado === "DISPONIBLE" || !motivo) {
      return `<span class="badge bg-success"><i class="bi bi-check-circle-fill me-1"></i>Disponible</span>`;
    }
    const mot = String(motivo).toUpperCase();
    if (mot.includes("PRIVADO")) {
      return `<span class="badge bg-warning text-dark"><i class="bi bi-lock-fill me-1"></i>Privado</span>`;
    }
    if (mot.includes("COPYRIGHT")) {
      return `<span class="badge bg-purple text-white" style="background-color: #6f42c1;"><i class="bi bi-shield-slash-fill me-1"></i>Copyright</span>`;
    }
    if (mot.includes("RESTRICCION_REGIONAL") || mot.includes("REGIONAL")) {
      return `<span class="badge bg-info text-dark"><i class="bi bi-globe-americas me-1"></i>Restricción Regional</span>`;
    }
    return `<span class="badge bg-danger"><i class="bi bi-x-circle-fill me-1"></i>Eliminado / Indisponible</span>`;
  }

  function mostrarResultadosVerificacion(data) {
    const { playlist, videos, consulta, cantidadVideos, disponibles, noDisponibles } = data;

    const total = cantidadVideos || 0;
    const disp = disponibles || 0;
    const noDisp = noDisponibles || 0;
    const duracionMs = consulta?.duracionMs || 0;

    const pctDisp = total > 0 ? ((disp / total) * 100).toFixed(1) : 0;
    const pctNoDisp = total > 0 ? ((noDisp / total) * 100).toFixed(1) : 0;

    document.getElementById("stat-total").textContent = total;
    document.getElementById("stat-disponibles").textContent = disp;
    document.getElementById("stat-no-disponibles").textContent = noDisp;
    document.getElementById("stat-disponibles-pct").textContent = `${pctDisp}%`;
    document.getElementById("stat-no-disponibles-pct").textContent = `${pctNoDisp}%`;

    const formattedTime = duracionMs > 1000 ? `${(duracionMs / 1000).toFixed(2)}s` : `${duracionMs}ms`;
    document.getElementById("stat-duracion").innerHTML = `<i class="bi bi-clock me-1"></i>${formattedTime}`;

    document.getElementById("playlist-title-header").textContent = playlist?.titulo || "Playlist de YouTube";
    document.getElementById("playlist-id-badge").textContent = playlist?.youtubeId || "";

    // Visual Chart
    const ctx = document.getElementById("chart-disponibilidad");
    if (ctx && window.Chart) {
      if (chartInstance) chartInstance.destroy();
      chartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
          labels: ["Disponibles", "No Disponibles"],
          datasets: [
            {
              data: [disp, noDisp],
              backgroundColor: ["#198754", "#dc3545"],
              borderWidth: 2,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: "bottom", labels: { boxWidth: 12, font: { size: 11 } } },
          },
        },
      });
    }

    const tbody = document.getElementById("videos-table-body");
    tbody.innerHTML = "";

    if (!videos || videos.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No se encontraron videos en la playlist.</td></tr>`;
    } else {
      videos.forEach((vid, index) => {
        const tr = document.createElement("tr");

        const statusBadge = getMotivoBadge(vid.motivo, vid.estado);

        const thumbUrl = vid.youtubeId
          ? `https://i.ytimg.com/vi/${vid.youtubeId}/hqdefault.jpg`
          : "/assets/img/user1-128x128.jpg";

        const videoLink = vid.youtubeId
          ? `<a href="https://www.youtube.com/watch?v=${vid.youtubeId}" target="_blank" class="btn btn-sm btn-outline-danger"><i class="bi bi-box-arrow-up-right"></i> Ver</a>`
          : "-";

        tr.innerHTML = `
          <td>${index + 1}</td>
          <td>
            <img src="${thumbUrl}" class="video-thumb" style="width: 80px; height: 45px; object-fit: cover; border-radius: 4px;" alt="Thumbnail" onerror="this.src='https://via.placeholder.com/80x45?text=No+Thumb'"/>
          </td>
          <td>
            <div class="fw-bold text-truncate" style="max-width: 300px;">${vid.titulo || "Video de YouTube"}</div>
            <small class="text-muted">ID: <code>${vid.youtubeId || "-"}</code></small>
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

    // Auto update background tabs
    cargarConsultas();
    cargarPlaylists();
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

      // Group consultations by playlist to allow timeline comparison
      const consultasPorPlaylist = {};
      consultas.forEach((c) => {
        const plId = c.playlistId || c.playlist?.id || "unknown";
        if (!consultasPorPlaylist[plId]) consultasPorPlaylist[plId] = [];
        consultasPorPlaylist[plId].push(c);
      });

      consultas.forEach((c) => {
        const plId = c.playlistId || c.playlist?.id || "unknown";
        const plConsultas = consultasPorPlaylist[plId] || [];
        
        // Find an older consultation for the same playlist to compare against
        const consultaAnterior = plConsultas.find((other) => other.id < c.id);

        const tr = document.createElement("tr");

        let compareBtnHtml = "";
        if (consultaAnterior) {
          compareBtnHtml = `
            <button class="btn btn-sm btn-outline-warning btn-comparar-consulta ms-1" data-anterior="${consultaAnterior.id}" data-nueva="${c.id}">
              <i class="bi bi-clock-history"></i> Comparar
            </button>
          `;
        }

        tr.innerHTML = `
          <td>#${c.id}</td>
          <td>${formatDate(c.fechaConsulta)}</td>
          <td>
            <div class="fw-bold">${c.playlist?.titulo || c.playlistTitulo || "Playlist"}</div>
            <small class="text-muted">ID: ${c.playlist?.youtubeId || "-"}</small>
          </td>
          <td><span class="badge bg-secondary">${c.cantidadVideos || 0}</span></td>
          <td><span class="badge bg-success">${c.disponibles || 0} (${c.porcentajeDisponibles || 0}%)</span></td>
          <td><span class="badge bg-danger">${c.noDisponibles || 0} (${c.porcentajeNoDisponibles || 0}%)</span></td>
          <td>
            <button class="btn btn-sm btn-primary btn-ver-detalle" data-id="${c.id}">
              <i class="bi bi-eye"></i> Detalle
            </button>
            ${compareBtnHtml}
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

      document.querySelectorAll(".btn-comparar-consulta").forEach((btn) => {
        btn.addEventListener("click", () => {
          const idAnt = btn.getAttribute("data-anterior");
          const idNue = btn.getAttribute("data-nueva");
          mostrarModalComparacion(idAnt, idNue);
        });
      });

    } catch (err) {
      console.error(err);
      loading.style.display = "none";
      tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">Error al cargar las consultas.</td></tr>`;
    }
  }

  // Comparison Modal
  async function mostrarModalComparacion(idAnterior, idNueva) {
    try {
      const comp = await compararConsultas(idAnterior, idNueva);
      if (!comp) return;

      const c1 = comp.consultaAnterior;
      const c2 = comp.consultaNueva;

      document.getElementById("comp-fecha-1").textContent = formatDate(c1?.fechaConsulta);
      document.getElementById("comp-info-1").textContent = `${c1?.cantidadVideos || 0} videos | ${c1?.disponibles || 0} disp | ${c1?.noDisponibles || 0} no disp`;

      document.getElementById("comp-fecha-2").textContent = formatDate(c2?.fechaConsulta);
      document.getElementById("comp-info-2").textContent = `${c2?.cantidadVideos || 0} videos | ${c2?.disponibles || 0} disp | ${c2?.noDisponibles || 0} no disp`;

      const diffDispStr = comp.diferenciaDisponibles >= 0 ? `+${comp.diferenciaDisponibles}` : `${comp.diferenciaDisponibles}`;
      const diffNoDispStr = comp.diferenciaNoDisponibles >= 0 ? `+${comp.diferenciaNoDisponibles}` : `${comp.diferenciaNoDisponibles}`;

      document.getElementById("comp-diff-disp").textContent = diffDispStr;
      document.getElementById("comp-diff-nodisp").textContent = diffNoDispStr;

      const tbody = document.getElementById("comp-videos-body");
      tbody.innerHTML = "";

      const videosNoDisp = comp.videosAhoraNoDisponibles || [];
      const videosDisp = comp.videosRecuperados || [];
      const todosVideos = [...videosNoDisp, ...videosDisp];

      if (todosVideos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">No hay diferencias registradas de estado.</td></tr>`;
      } else {
        todosVideos.forEach((v, index) => {
          const isDisp = v.estado === "DISPONIBLE" || v.disponible === true;
          const badge = getMotivoBadge(v.motivo, v.estado);
          tbody.innerHTML += `
            <tr>
              <td>${index + 1}</td>
              <td><div class="fw-bold">${v.titulo || "Video"}</div><small class="text-muted">ID: ${v.youtubeId}</small></td>
              <td>${v.canal || "Canal Desconocido"}</td>
              <td>${badge}</td>
              <td><small class="text-muted">${v.motivo || (isDisp ? "Video público y activo" : "Indisponible")}</small></td>
            </tr>
          `;
        });
      }

      const modalEl = new bootstrap.Modal(document.getElementById("modalComparacion"));
      modalEl.show();
    } catch (err) {
      console.error(err);
      alert("Error al obtener la comparación histórica.");
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
      document.getElementById("modal-disponibles").textContent = `${consulta.disponibles || 0} disponibles (${consulta.porcentajeDisponibles || 0}%)`;
      document.getElementById("modal-nodisponibles").textContent = `${consulta.noDisponibles || 0} no disponibles (${consulta.porcentajeNoDisponibles || 0}%)`;

      const tbody = document.getElementById("modal-videos-body");
      tbody.innerHTML = "";

      const videos = consulta.videos || [];
      if (videos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Sin detalles de videos.</td></tr>`;
      } else {
        videos.forEach((v, index) => {
          const isDisp = v.estado === "DISPONIBLE" || v.disponible === true;
          const badge = getMotivoBadge(v.motivo, v.estado);
          tbody.innerHTML += `
            <tr>
              <td>${index + 1}</td>
              <td>${v.titulo || "-"}</td>
              <td>${v.canal || "Canal Desconocido"}</td>
              <td>${badge}</td>
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

  // Load initial data for tabs
  cargarConsultas();
  cargarPlaylists();
});
