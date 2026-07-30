const state = {
    currentView: "worlds",
    token: window.localStorage.getItem("survivalHubToken"),
    currentUser: null,
    worlds: [],
    selectedWorldId: null,
    members: [],
    tasks: [],
    selectedTaskId: null,
    resources: [],
    summary: null,
    dashboard: null,
    guides: [],
    taskFilter: "all",
    resourceFilter: "all",
    presetSearch: "",
    guideScopeFilter: "all",
    presetGameFilter: "all",
    presetTypeFilter: "all",
    presetSort: "popular",
    expandedGuideIds: new Set(),
    draggedTaskId: null,
    draggedResourceId: null
};

const els = {
    authLoggedOut: document.getElementById("authLoggedOut"),
    authLoggedIn: document.getElementById("authLoggedIn"),
    showLoginButton: document.getElementById("showLoginButton"),
    showRegisterButton: document.getElementById("showRegisterButton"),
    screenLoginForm: document.getElementById("screenLoginForm"),
    screenLoginUsername: document.getElementById("screenLoginUsername"),
    screenLoginPassword: document.getElementById("screenLoginPassword"),
    screenRegisterForm: document.getElementById("screenRegisterForm"),
    screenRegisterUsername: document.getElementById("screenRegisterUsername"),
    screenRegisterEmail: document.getElementById("screenRegisterEmail"),
    screenRegisterDisplayName: document.getElementById("screenRegisterDisplayName"),
    screenRegisterPassword: document.getElementById("screenRegisterPassword"),
    currentUserLabel: document.getElementById("currentUserLabel"),
    logoutButton: document.getElementById("logoutButton"),
    worldsNavButton: document.getElementById("worldsNavButton"),
    presetsNavButton: document.getElementById("presetsNavButton"),
    profileNavButton: document.getElementById("profileNavButton"),
    authView: document.getElementById("authView"),
    worldsView: document.getElementById("worldsView"),
    presetsView: document.getElementById("presetsView"),
    profileView: document.getElementById("profileView"),
    worldForm: document.getElementById("worldForm"),
    worldId: document.getElementById("worldId"),
    worldName: document.getElementById("worldName"),
    worldGame: document.getElementById("worldGame"),
    worldDescription: document.getElementById("worldDescription"),
    saveWorldButton: document.getElementById("saveWorldButton"),
    resetWorldButton: document.getElementById("resetWorldButton"),
    deleteWorldButton: document.getElementById("deleteWorldButton"),
    worldList: document.getElementById("worldList"),
    worldCount: document.getElementById("worldCount"),
    selectedWorldGame: document.getElementById("selectedWorldGame"),
    selectedWorldName: document.getElementById("selectedWorldName"),
    selectedWorldDescription: document.getElementById("selectedWorldDescription"),
    memberMetric: document.getElementById("memberMetric"),
    taskMetric: document.getElementById("taskMetric"),
    resourceMetric: document.getElementById("resourceMetric"),
    progressMetric: document.getElementById("progressMetric"),
    profileDisplayName: document.getElementById("profileDisplayName"),
    profileAccountInfo: document.getElementById("profileAccountInfo"),
    profileWorldCount: document.getElementById("profileWorldCount"),
    profileGuideCount: document.getElementById("profileGuideCount"),
    profileFavoriteCount: document.getElementById("profileFavoriteCount"),
    profileVoteCount: document.getElementById("profileVoteCount"),
    profileWorldList: document.getElementById("profileWorldList"),
    profileGuideList: document.getElementById("profileGuideList"),
    profileFavoriteList: document.getElementById("profileFavoriteList"),
    memberForm: document.getElementById("memberForm"),
    memberId: document.getElementById("memberId"),
    memberName: document.getElementById("memberName"),
    memberRole: document.getElementById("memberRole"),
    memberList: document.getElementById("memberList"),
    taskForm: document.getElementById("taskForm"),
    taskId: document.getElementById("taskId"),
    taskTitle: document.getElementById("taskTitle"),
    taskDescription: document.getElementById("taskDescription"),
    taskPriority: document.getElementById("taskPriority"),
    taskFilter: document.getElementById("taskFilter"),
    taskList: document.getElementById("taskList"),
    selectedTaskTitle: document.getElementById("selectedTaskTitle"),
    pendingTaskInsight: document.getElementById("pendingTaskInsight"),
    completedTaskInsight: document.getElementById("completedTaskInsight"),
    highPriorityInsight: document.getElementById("highPriorityInsight"),
    nextFocusInsight: document.getElementById("nextFocusInsight"),
    resourceForm: document.getElementById("resourceForm"),
    resourceId: document.getElementById("resourceId"),
    resourceName: document.getElementById("resourceName"),
    requiredQuantity: document.getElementById("requiredQuantity"),
    collectedQuantity: document.getElementById("collectedQuantity"),
    resourceList: document.getElementById("resourceList"),
    resourceFilter: document.getElementById("resourceFilter"),
    resourceImportFile: document.getElementById("resourceImportFile"),
    progressLabel: document.getElementById("progressLabel"),
    progressValue: document.getElementById("progressValue"),
    progressBar: document.getElementById("progressBar"),
    guideForm: document.getElementById("guideForm"),
    guideId: document.getElementById("guideId"),
    guideTitle: document.getElementById("guideTitle"),
    guideGame: document.getElementById("guideGame"),
    guideAuthor: document.getElementById("guideAuthor"),
    guideDifficulty: document.getElementById("guideDifficulty"),
    guideType: document.getElementById("guideType"),
    guideYoutubeUrl: document.getElementById("guideYoutubeUrl"),
    guideDescription: document.getElementById("guideDescription"),
    guideSteps: document.getElementById("guideSteps"),
    guideResources: document.getElementById("guideResources"),
    addGuideStepButton: document.getElementById("addGuideStepButton"),
    addGuideResourceButton: document.getElementById("addGuideResourceButton"),
    guideList: document.getElementById("guideList"),
    presetWorldSelect: document.getElementById("presetWorldSelect"),
    presetActiveWorldName: document.getElementById("presetActiveWorldName"),
    activeUserLabel: document.getElementById("activeUserLabel"),
    presetSearch: document.getElementById("presetSearch"),
    guideScopeFilter: document.getElementById("guideScopeFilter"),
    presetGameFilter: document.getElementById("presetGameFilter"),
    presetTypeFilter: document.getElementById("presetTypeFilter"),
    presetSort: document.getElementById("presetSort"),
    presetCount: document.getElementById("presetCount"),
    resetGuideButton: document.getElementById("resetGuideButton"),
    saveGuideButton: document.getElementById("saveGuideButton"),
    toast: document.getElementById("toast"),
    confirmOverlay: document.getElementById("confirmOverlay"),
    confirmTitle: document.getElementById("confirmTitle"),
    confirmMessage: document.getElementById("confirmMessage"),
    confirmCancelButton: document.getElementById("confirmCancelButton"),
    confirmAcceptButton: document.getElementById("confirmAcceptButton")
};

async function api(path, options = {}) {
    const isFormData = options.body instanceof FormData;
    const headers = {
        ...(!isFormData ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {})
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(`/api${path}`, {
        ...options,
        headers
    });

    if (response.status === 401 && !path.startsWith("/auth/")) {
        clearSession();
        throw new Error("Sesion no valida");
    }

    if (!response.ok) {
        const error = new Error(`Error HTTP ${response.status}`);
        error.status = response.status;
        throw error;
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function body(data) {
    return JSON.stringify(data);
}

function selectedWorld() {
    return state.worlds.find(world => world.id === state.selectedWorldId) || null;
}

function selectedTask() {
    return state.tasks.find(task => task.id === state.selectedTaskId) || null;
}

function activeUser() {
    return state.currentUser;
}

function activeUserName() {
    const user = activeUser();

    if (!user) {
        return "";
    }

    return user.displayName || user.username;
}

function normalizedText(value) {
    return String(value || "").trim().toLowerCase();
}

function isGuideFromActiveUser(guide) {
    const user = activeUser();

    if (!user) {
        return false;
    }

    const author = normalizedText(guide.author);

    return author === normalizedText(user.displayName) || author === normalizedText(user.username);
}

function setSession(token, user) {
    state.token = token;
    state.currentUser = user;
    window.localStorage.setItem("survivalHubToken", token);
    renderAuth();
}

function clearSession() {
    state.token = null;
    state.currentUser = null;
    window.localStorage.removeItem("survivalHubToken");
    renderAuth();
}

async function loadCurrentUser() {
    state.currentUser = await api("/auth/me");
    renderAuth();
}

function presetTargetWorldId() {
    if (!els.presetWorldSelect.value) {
        return null;
    }

    return Number(els.presetWorldSelect.value);
}

function presetTargetWorld() {
    const worldId = presetTargetWorldId();

    return state.worlds.find(world => world.id === worldId) || null;
}

function setView(view) {
    state.currentView = view;
    els.authView.hidden = view !== "auth";
    els.worldsView.hidden = view !== "worlds";
    els.presetsView.hidden = view !== "presets";
    els.profileView.hidden = view !== "profile";
    els.authView.classList.toggle("active-view", view === "auth");
    els.worldsView.classList.toggle("active-view", view === "worlds");
    els.presetsView.classList.toggle("active-view", view === "presets");
    els.profileView.classList.toggle("active-view", view === "profile");
    els.worldsNavButton.classList.toggle("active", view === "worlds");
    els.presetsNavButton.classList.toggle("active", view === "presets");
    els.profileNavButton.classList.toggle("active", view === "profile");
}

function showAuthView(mode = "login") {
    setView("auth");

    if (mode === "register") {
        els.screenRegisterUsername.focus();
        return;
    }

    els.screenLoginUsername.focus();
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function guideStepRowTemplate(step = {}) {
    return `
        <div class="guide-builder-row" data-guide-step-row>
            <span class="guide-builder-index">Paso</span>
            <input type="text" value="${escapeHtml(step.title || "")}" placeholder="Título del paso" data-guide-step-title>
            <textarea rows="2" placeholder="Descripción del paso" data-guide-step-description>${escapeHtml(step.description || "")}</textarea>
            <button type="button" class="small-button danger" data-remove-guide-step>Quitar</button>
        </div>
    `;
}

function guideResourceRowTemplate(resource = {}) {
    return `
        <div class="guide-builder-row resource-builder-row" data-guide-resource-row>
            <span class="guide-builder-index">Recurso</span>
            <input type="text" value="${escapeHtml(resource.name || "")}" placeholder="Nombre del recurso" data-guide-resource-name>
            <input type="number" min="0" value="${resource.requiredQuantity ?? ""}" placeholder="Cantidad" data-guide-resource-quantity>
            <button type="button" class="small-button danger" data-remove-guide-resource>Quitar</button>
        </div>
    `;
}

function updateGuideBuilderIndexes() {
    els.guideSteps.querySelectorAll("[data-guide-step-row]").forEach((row, index) => {
        row.querySelector(".guide-builder-index").textContent = `Paso ${index + 1}`;
    });

    els.guideResources.querySelectorAll("[data-guide-resource-row]").forEach((row, index) => {
        row.querySelector(".guide-builder-index").textContent = `Recurso ${index + 1}`;
    });
}

function addGuideStepRow(step = {}) {
    els.guideSteps.insertAdjacentHTML("beforeend", guideStepRowTemplate(step));
    updateGuideBuilderIndexes();
}

function addGuideResourceRow(resource = {}) {
    els.guideResources.insertAdjacentHTML("beforeend", guideResourceRowTemplate(resource));
    updateGuideBuilderIndexes();
}

function renderGuideStepRows(steps = []) {
    els.guideSteps.innerHTML = "";
    const rows = steps.length > 0
        ? [...steps].sort((first, second) => first.stepNumber - second.stepNumber)
        : [{}];

    rows.forEach(addGuideStepRow);
}

function renderGuideResourceRows(resources = []) {
    els.guideResources.innerHTML = "";
    const rows = resources.length > 0 ? resources : [{}];

    rows.forEach(addGuideResourceRow);
}

function parseGuideSteps() {
    return [...els.guideSteps.querySelectorAll("[data-guide-step-row]")]
        .map((row, index) => ({
            id: null,
            stepNumber: index + 1,
            title: row.querySelector("[data-guide-step-title]").value.trim(),
            description: row.querySelector("[data-guide-step-description]").value.trim()
        }))
        .filter(step => step.title);
}

function parseGuideResources() {
    return [...els.guideResources.querySelectorAll("[data-guide-resource-row]")]
        .map(row => {
            const requiredQuantity = Number(row.querySelector("[data-guide-resource-quantity]").value);

            return {
                id: null,
                name: row.querySelector("[data-guide-resource-name]").value.trim(),
                requiredQuantity: Number.isFinite(requiredQuantity) ? requiredQuantity : 0
            };
        })
        .filter(resource => resource.name);
}

function presetRatingText(guide) {
    const ratingAverage = Number(guide.ratingAverage || 0);
    const ratingCount = Number(guide.ratingCount || 0);

    if (ratingCount === 0) {
        return "Sin votos";
    }

    return `${ratingAverage.toFixed(1)} / 5 (${ratingCount})`;
}

function dragHandleIcon() {
    return `
        <svg viewBox="0 0 128 128" aria-hidden="true" focusable="false">
            <circle cx="82" cy="30" r="4" fill="currentColor" stroke="currentColor" stroke-width="7"></circle>
            <circle cx="82" cy="64" r="4" fill="currentColor" stroke="currentColor" stroke-width="7"></circle>
            <circle cx="82" cy="98" r="4" fill="currentColor" stroke="currentColor" stroke-width="7"></circle>
            <circle cx="46" cy="30" r="4" fill="currentColor" stroke="currentColor" stroke-width="7"></circle>
            <circle cx="46" cy="64" r="4" fill="currentColor" stroke="currentColor" stroke-width="7"></circle>
            <circle cx="46" cy="98" r="4" fill="currentColor" stroke="currentColor" stroke-width="7"></circle>
        </svg>
    `;
}

function presetCreatedAtValue(guide) {
    return guide.createdAt ? new Date(guide.createdAt).getTime() : 0;
}

function youtubeEmbedUrl(url) {
    if (!url) {
        return "";
    }

    try {
        const parsedUrl = new URL(url);
        const host = parsedUrl.hostname.replace("www.", "");
        let videoId = "";

        if (host === "youtu.be") {
            videoId = parsedUrl.pathname.split("/").filter(Boolean)[0] || "";
        }

        if (host === "youtube.com" || host === "m.youtube.com") {
            if (parsedUrl.pathname === "/watch") {
                videoId = parsedUrl.searchParams.get("v") || "";
            }

            if (parsedUrl.pathname.startsWith("/embed/") || parsedUrl.pathname.startsWith("/shorts/")) {
                videoId = parsedUrl.pathname.split("/").filter(Boolean)[1] || "";
            }
        }

        if (!/^[A-Za-z0-9_-]{6,}$/.test(videoId)) {
            return "";
        }

        return `https://www.youtube.com/embed/${videoId}`;
    } catch (error) {
        return "";
    }
}

function guidePopularityScore(guide) {
    const ratingAverage = Number(guide.ratingAverage || 0);
    const ratingCount = Number(guide.ratingCount || 0);
    const importCount = Number(guide.importCount || 0);

    return ratingAverage * 20 + ratingCount * 4 + importCount * 6;
}

function sortGuides(guides) {
    const sortedGuides = [...guides];

    if (state.presetSort === "rating") {
        return sortedGuides.sort((first, second) =>
            (second.ratingAverage || 0) - (first.ratingAverage || 0)
            || (second.ratingCount || 0) - (first.ratingCount || 0)
        );
    }

    if (state.presetSort === "imports") {
        return sortedGuides.sort((first, second) =>
            (second.importCount || 0) - (first.importCount || 0)
        );
    }

    if (state.presetSort === "recent") {
        return sortedGuides.sort((first, second) =>
            presetCreatedAtValue(second) - presetCreatedAtValue(first)
        );
    }

    if (state.presetSort === "title") {
        return sortedGuides.sort((first, second) =>
            String(first.title || "").localeCompare(String(second.title || ""))
        );
    }

    return sortedGuides.sort((first, second) =>
        guidePopularityScore(second) - guidePopularityScore(first)
        || presetCreatedAtValue(second) - presetCreatedAtValue(first)
    );
}

function showToast(message) {
    els.toast.textContent = message;
    els.toast.classList.add("visible");

    window.clearTimeout(showToast.timeoutId);
    showToast.timeoutId = window.setTimeout(() => {
        els.toast.classList.remove("visible");
    }, 2400);
}

function askConfirmation(title, message, acceptText = "Borrar") {
    return new Promise(resolve => {
        els.confirmTitle.textContent = title;
        els.confirmMessage.textContent = message;
        els.confirmAcceptButton.textContent = acceptText;
        els.confirmOverlay.hidden = false;
        els.confirmCancelButton.focus();

        const close = confirmed => {
            els.confirmOverlay.hidden = true;
            els.confirmCancelButton.removeEventListener("click", onCancel);
            els.confirmAcceptButton.removeEventListener("click", onAccept);
            els.confirmOverlay.removeEventListener("click", onOverlayClick);
            document.removeEventListener("keydown", onKeyDown);
            resolve(confirmed);
        };

        const onCancel = () => close(false);
        const onAccept = () => close(true);
        const onOverlayClick = event => {
            if (event.target === els.confirmOverlay) {
                close(false);
            }
        };
        const onKeyDown = event => {
            if (event.key === "Escape") {
                close(false);
            }
        };

        els.confirmCancelButton.addEventListener("click", onCancel);
        els.confirmAcceptButton.addEventListener("click", onAccept);
        els.confirmOverlay.addEventListener("click", onOverlayClick);
        document.addEventListener("keydown", onKeyDown);
    });
}

async function loadWorlds() {
    state.worlds = await api("/worlds");

    if (state.worlds.length === 0) {
        state.selectedWorldId = null;
    }

    if (state.selectedWorldId === null && state.worlds.length > 0) {
        state.selectedWorldId = state.worlds[0].id;
    }

    if (state.selectedWorldId !== null && !state.worlds.some(world => world.id === state.selectedWorldId)) {
        state.selectedWorldId = state.worlds.length > 0 ? state.worlds[0].id : null;
    }

    renderWorlds();
    await loadSelectedWorld();
}

async function loadGuides() {
    state.guides = await api("/guides");
    renderPresetFilters();
    renderGuides();
}

async function loadSelectedWorld() {
    const world = selectedWorld();

    if (!world) {
        state.members = [];
        state.tasks = [];
        state.selectedTaskId = null;
        state.resources = [];
        state.summary = null;
        state.dashboard = null;
        resetWorldForm();
        renderAll();
        return;
    }

    fillWorldForm(world);
    state.dashboard = await api(`/worlds/${world.id}/dashboard`);
    state.members = await api(`/worlds/${world.id}/members`);
    state.tasks = await api(`/worlds/${world.id}/tasks`);

    if (state.tasks.length === 0) {
        state.selectedTaskId = null;
    }

    if (state.selectedTaskId === null && state.tasks.length > 0) {
        state.selectedTaskId = state.tasks[0].id;
    }

    if (state.selectedTaskId !== null && !state.tasks.some(task => task.id === state.selectedTaskId)) {
        state.selectedTaskId = state.tasks.length > 0 ? state.tasks[0].id : null;
    }

    await loadSelectedTaskResources();
    renderAll();
}

async function loadSelectedTaskResources() {
    const world = selectedWorld();
    const task = selectedTask();

    if (!world || !task) {
        state.resources = [];
        state.summary = null;
        return;
    }

    state.resources = await api(`/worlds/${world.id}/tasks/${task.id}/resources`);
    state.summary = await api(`/worlds/${world.id}/tasks/${task.id}/summary`);
}

function renderAll() {
    renderWorldHeader();
    renderMetrics();
    renderDashboardInsights();
    renderMembers();
    renderTasks();
    renderResources();
    renderPresetWorldSelect();
    renderUsers();
    renderProfile();
    renderPresetFilters();
    renderGuides();
    setResourceFormEnabled(Boolean(selectedTask()));
}

function renderAuth() {
    const loggedIn = Boolean(state.currentUser);
    const label = activeUserName() || "Usuario";

    els.authLoggedOut.hidden = loggedIn;
    els.authLoggedIn.hidden = !loggedIn;
    els.currentUserLabel.textContent = label;
    els.activeUserLabel.textContent = loggedIn
        ? label
        : "Inicia sesion para publicar y votar";

    if (!loggedIn) {
        els.guideAuthor.value = "";
    }
}

function renderWorlds() {
    els.worldCount.textContent = state.worlds.length;

    if (state.worlds.length === 0) {
        els.worldList.innerHTML = `<div class="empty-state">Sin mundos todavia</div>`;
        return;
    }

    els.worldList.innerHTML = state.worlds.map(world => {
        const active = world.id === state.selectedWorldId ? " active" : "";

        return `
            <div class="world-card${active}">
                <button type="button" data-world-id="${world.id}">
                    <strong>${escapeHtml(world.name)}</strong>
                    <span>${escapeHtml(world.game)}</span>
                </button>
            </div>
        `;
    }).join("");
}

function renderPresetWorldSelect() {
    if (state.worlds.length === 0) {
        els.presetWorldSelect.innerHTML = `<option value="">Sin mundos creados</option>`;
        els.presetActiveWorldName.textContent = "Crea un mundo para anadir guias";
        return;
    }

    const selectedId = state.selectedWorldId || state.worlds[0].id;
    els.presetWorldSelect.innerHTML = state.worlds.map(world => {
        const selected = world.id === selectedId ? " selected" : "";

        return `<option value="${world.id}"${selected}>${escapeHtml(world.name)}</option>`;
    }).join("");

    const world = state.worlds.find(item => item.id === selectedId);
    els.presetActiveWorldName.textContent = world
        ? `Las guias se anadiran a ${world.name}`
        : "Selecciona un mundo para anadir guias";
}

function renderUsers() {
    if (!state.currentUser) {
        els.activeUserLabel.textContent = "Inicia sesion para publicar y votar";
        els.guideAuthor.value = "";
        return;
    }

    els.activeUserLabel.textContent = activeUserName();

    if (!els.guideId.value) {
        els.guideAuthor.value = activeUserName();
    }
}

function renderProfile() {
    const user = activeUser();

    if (!user) {
        els.profileDisplayName.textContent = "Sesion no iniciada";
        els.profileAccountInfo.textContent = "Inicia sesion para ver tu perfil.";
        els.profileWorldCount.textContent = "0";
        els.profileGuideCount.textContent = "0";
        els.profileFavoriteCount.textContent = "0";
        els.profileVoteCount.textContent = "0";
        els.profileWorldList.innerHTML = `<div class="empty-state">Inicia sesion</div>`;
        els.profileGuideList.innerHTML = `<div class="empty-state">Inicia sesion</div>`;
        els.profileFavoriteList.innerHTML = `<div class="empty-state">Inicia sesion</div>`;
        return;
    }

    const ownedGuides = state.guides.filter(isGuideFromActiveUser);
    const favoriteGuides = state.guides.filter(guide => guide.favoriteByCurrentUser);
    const votedGuides = state.guides.filter(guide => Number(guide.currentUserRating || 0) > 0);

    els.profileDisplayName.textContent = activeUserName();
    els.profileAccountInfo.textContent = `${user.username}${user.email ? ` · ${user.email}` : ""}`;
    els.profileWorldCount.textContent = state.worlds.length;
    els.profileGuideCount.textContent = ownedGuides.length;
    els.profileFavoriteCount.textContent = favoriteGuides.length;
    els.profileVoteCount.textContent = votedGuides.length;

    els.profileWorldList.innerHTML = state.worlds.length > 0
        ? state.worlds.map(world => `
            <button type="button" class="profile-list-item" data-profile-world="${world.id}">
                <strong>${escapeHtml(world.name)}</strong>
                <span>${escapeHtml(world.game)}</span>
            </button>
        `).join("")
        : `<div class="empty-state">Todavia no tienes mundos</div>`;

    els.profileGuideList.innerHTML = ownedGuides.length > 0
        ? ownedGuides.map(guide => `
            <button type="button" class="profile-list-item" data-profile-guide="${guide.id}">
                <strong>${escapeHtml(guide.title)}</strong>
                <span>${escapeHtml(guide.game)} · ${presetRatingText(guide)}</span>
            </button>
        `).join("")
        : `<div class="empty-state">Todavia no has publicado guias</div>`;

    els.profileFavoriteList.innerHTML = favoriteGuides.length > 0
        ? favoriteGuides.map(guide => `
            <button type="button" class="profile-list-item" data-profile-guide="${guide.id}">
                <strong>${escapeHtml(guide.title)}</strong>
                <span>${escapeHtml(guide.game)} · ${guide.importCount || 0} importaciones</span>
            </button>
        `).join("")
        : `<div class="empty-state">No tienes guias guardadas</div>`;
}

function renderPresetFilters() {
    const games = [...new Set(state.guides.map(guide => guide.game).filter(Boolean))].sort();
    const types = [...new Set(state.guides.map(guide => guide.type).filter(Boolean))].sort();

    els.presetGameFilter.innerHTML = `<option value="all">Todos los juegos</option>` + games.map(game => {
        const selected = game === state.presetGameFilter ? " selected" : "";

        return `<option value="${escapeHtml(game)}"${selected}>${escapeHtml(game)}</option>`;
    }).join("");

    els.presetTypeFilter.innerHTML = `<option value="all">Todos los tipos</option>` + types.map(type => {
        const selected = type === state.presetTypeFilter ? " selected" : "";

        return `<option value="${escapeHtml(type)}"${selected}>${escapeHtml(type)}</option>`;
    }).join("");
}

function renderWorldHeader() {
    const world = selectedWorld();
    els.deleteWorldButton.disabled = !world;

    if (!world) {
        els.selectedWorldGame.textContent = "Sin mundo seleccionado";
        els.selectedWorldName.textContent = "Survival Hub";
        els.selectedWorldDescription.textContent = "Crea un mundo para empezar.";
        return;
    }

    els.selectedWorldGame.textContent = world.game;
    els.selectedWorldName.textContent = world.name;
    els.selectedWorldDescription.textContent = world.description || "Sin descripcion";
}

function renderMetrics() {
    const dashboard = state.dashboard || {
        memberCount: 0,
        taskCount: 0,
        resourceCount: 0,
        averageTaskProgress: 0
    };

    els.memberMetric.textContent = dashboard.memberCount;
    els.taskMetric.textContent = dashboard.taskCount;
    els.resourceMetric.textContent = dashboard.resourceCount;
    els.progressMetric.textContent = `${dashboard.averageTaskProgress}%`;
}

function renderDashboardInsights() {
    const pendingTasks = state.tasks.filter(task => !task.completed);
    const completedTasks = state.tasks.filter(task => task.completed);
    const highPriorityTasks = pendingTasks.filter(task => normalizedText(task.priority) === "alta");
    const nextFocusTask = highPriorityTasks[0] || pendingTasks[0] || null;

    els.pendingTaskInsight.textContent = pendingTasks.length;
    els.completedTaskInsight.textContent = completedTasks.length;
    els.highPriorityInsight.textContent = highPriorityTasks.length;
    els.nextFocusInsight.textContent = nextFocusTask ? nextFocusTask.title : "Sin tareas";
}

function renderMembers() {
    if (!selectedWorld()) {
        els.memberList.innerHTML = `<div class="empty-state">Selecciona un mundo</div>`;
        return;
    }

    if (state.members.length === 0) {
        els.memberList.innerHTML = `<div class="empty-state">Sin miembros</div>`;
        return;
    }

    els.memberList.innerHTML = state.members.map(member => `
        <article class="item-card">
            <div>
                <strong>${escapeHtml(member.name)}</strong>
                <span>${escapeHtml(member.role)}</span>
            </div>
            <div class="item-actions">
                <button type="button" class="small-button" data-edit-member="${member.id}">Editar</button>
                <button type="button" class="small-button danger" data-delete-member="${member.id}">Borrar</button>
            </div>
        </article>
    `).join("");
}

function renderTasks() {
    if (!selectedWorld()) {
        els.taskList.innerHTML = `<div class="empty-state">Selecciona un mundo</div>`;
        return;
    }

    const filteredTasks = state.tasks.filter(task => {
        if (state.taskFilter === "completed") {
            return task.completed;
        }

        if (state.taskFilter === "pending") {
            return !task.completed;
        }

        return true;
    });

    if (filteredTasks.length === 0) {
        els.taskList.innerHTML = `<div class="empty-state">Sin tareas</div>`;
        return;
    }

    els.taskList.innerHTML = filteredTasks.map(task => {
        const canDrag = state.taskFilter === "all";
        const selected = task.id === state.selectedTaskId ? " selected" : "";
        const statusClass = task.completed ? "" : " pending";
        const statusText = task.completed ? "Completada" : "Pendiente";
        const completeText = task.completed ? "Reabrir" : "Completar";
        const priority = task.priority || "Media";
        const priorityClass = priority.toLowerCase() === "alta"
            ? "high"
            : priority.toLowerCase() === "baja" ? "low" : "medium";

        return `
            <article class="item-card task-card${selected}" data-task-card="${task.id}" draggable="${canDrag}">
                <button type="button" class="task-select" data-select-task="${task.id}">
                    <div class="status-line">
                        <span class="task-title-line">
                            ${canDrag ? `<span class="drag-handle" title="Arrastrar tarea">${dragHandleIcon()}</span>` : ""}
                            <strong>${escapeHtml(task.title)}</strong>
                        </span>
                        <span class="status-pill${statusClass}">${statusText}</span>
                    </div>
                    <p>${escapeHtml(task.description || "Sin descripcion")}</p>
                    <div class="meta-row">
                        <span class="meta-chip ${priorityClass}">${escapeHtml(priority)}</span>
                    </div>
                </button>
                <div class="item-actions">
                    <button type="button" class="small-button complete" data-toggle-task-completed="${task.id}">${completeText}</button>
                    <button type="button" class="small-button" data-edit-task="${task.id}">Editar</button>
                    <button type="button" class="small-button danger" data-delete-task="${task.id}">Borrar</button>
                </div>
            </article>
        `;
    }).join("");
}

function renderResources() {
    const task = selectedTask();

    if (!task) {
        els.selectedTaskTitle.textContent = "Selecciona una tarea";
        els.progressLabel.textContent = "0 de 0 recursos";
        els.progressValue.textContent = "0%";
        els.progressBar.style.width = "0%";
        els.resourceList.innerHTML = `<div class="empty-state">Sin tarea seleccionada</div>`;
        return;
    }

    const summary = state.summary || {
        totalResources: 0,
        completedResources: 0,
        progressPercentage: 0
    };

    els.selectedTaskTitle.textContent = task.title;
    els.progressLabel.textContent = `${summary.completedResources} de ${summary.totalResources} recursos`;
    els.progressValue.textContent = `${summary.progressPercentage}%`;
    els.progressBar.style.width = `${Math.min(summary.progressPercentage, 100)}%`;

    const filteredResources = state.resources
        .filter(resource => {
            if (state.resourceFilter === "completed") {
                return resource.completed;
            }

            if (state.resourceFilter === "pending") {
                return !resource.completed;
            }

            return true;
        })
        .sort((first, second) => {
            const firstOrder = first.sortOrder || 0;
            const secondOrder = second.sortOrder || 0;

            if (firstOrder !== secondOrder) {
                return firstOrder - secondOrder;
            }

            return first.id - second.id;
        });

    if (filteredResources.length === 0) {
        els.resourceList.innerHTML = `<div class="empty-state">Sin recursos</div>`;
        return;
    }

    const canDrag = state.resourceFilter === "all";

    els.resourceList.innerHTML = filteredResources.map(resource => {
        const statusClass = resource.completed ? "" : " pending";
        const statusText = resource.completed ? "Completado" : "Pendiente";
        const percentage = resource.requiredQuantity === 0
            ? 100
            : Math.min(resource.collectedQuantity * 100 / resource.requiredQuantity, 100);

        return `
            <article class="item-card resource-card" data-resource-card="${resource.id}" draggable="${canDrag}">
                <div class="status-line">
                    <div class="resource-title-line">
                        ${canDrag ? `<span class="drag-handle" title="Arrastrar recurso">${dragHandleIcon()}</span>` : ""}
                        <strong>${escapeHtml(resource.name)}</strong>
                    </div>
                    <span class="status-pill${statusClass}">${statusText}</span>
                </div>
                <div class="resource-inline-edit">
                    <label>
                        <span>Conseguido</span>
                        <input
                            type="number"
                            min="0"
                            value="${resource.collectedQuantity}"
                            data-resource-quantity="${resource.id}"
                            data-quantity-field="collectedQuantity"
                            aria-label="Cantidad conseguida de ${escapeHtml(resource.name)}"
                        >
                    </label>
                    <label>
                        <span>Necesario</span>
                        <input
                            type="number"
                            min="0"
                            value="${resource.requiredQuantity}"
                            data-resource-quantity="${resource.id}"
                            data-quantity-field="requiredQuantity"
                            aria-label="Cantidad necesaria de ${escapeHtml(resource.name)}"
                        >
                    </label>
                </div>
                <div class="progress-track">
                    <div class="progress-bar" style="width: ${percentage}%"></div>
                </div>
                <div class="item-actions">
                    <button type="button" class="small-button" data-edit-resource="${resource.id}">Editar nombre</button>
                    <button type="button" class="small-button danger" data-delete-resource="${resource.id}">Borrar</button>
                </div>
            </article>
        `;
    }).join("");
}

function renderGuides() {
    const filteredGuides = state.guides.filter(guide => {
        const searchableText = `${guide.title} ${guide.game} ${guide.type} ${guide.description} ${guide.author}`.toLowerCase();
        const matchesSearch = searchableText.includes(state.presetSearch.toLowerCase());
        const matchesScope = state.guideScopeFilter === "all"
            || (state.guideScopeFilter === "mine" && isGuideFromActiveUser(guide))
            || (state.guideScopeFilter === "favorites" && guide.favoriteByCurrentUser);
        const matchesGame = state.presetGameFilter === "all" || guide.game === state.presetGameFilter;
        const matchesType = state.presetTypeFilter === "all" || guide.type === state.presetTypeFilter;

        return matchesSearch && matchesScope && matchesGame && matchesType;
    });

    els.presetCount.textContent = filteredGuides.length;

    if (state.guides.length === 0) {
        els.guideList.innerHTML = `<div class="empty-state">Sin guias publicadas</div>`;
        return;
    }

    if (filteredGuides.length === 0) {
        els.guideList.innerHTML = `<div class="empty-state">No hay guias con esos filtros</div>`;
        return;
    }

    const hasTargetWorld = Boolean(presetTargetWorld());

    els.guideList.innerHTML = sortGuides(filteredGuides).map(guide => {
        const steps = guide.steps || [];
        const resources = guide.resources || [];
        const orderedSteps = [...steps].sort((first, second) => first.stepNumber - second.stepNumber);
        const firstResources = resources.slice(0, 4).map(resource => `
            <span>${escapeHtml(resource.name)} x${resource.requiredQuantity}</span>
        `).join("");
        const extraResources = resources.length > 4
            ? `<span>+${resources.length - 4} mas</span>`
            : "";
        const youtubeLink = guide.youtubeUrl
            ? `<a class="guide-link" href="${escapeHtml(guide.youtubeUrl)}" target="_blank" rel="noreferrer">Ver video</a>`
            : "";
        const disabledText = hasTargetWorld ? "" : " disabled";
        const currentUserRating = Number(guide.currentUserRating || 0);
        const ratingButtons = [1, 2, 3, 4, 5].map(rating => `
            <button
                type="button"
                class="rating-button${currentUserRating === rating ? " active" : ""}"
                data-rate-guide="${guide.id}"
                data-rating="${rating}"
                aria-label="Valorar ${rating} de 5"
            >${rating}</button>
        `).join("");
        const canManageGuide = isGuideFromActiveUser(guide);
        const favoriteText = guide.favoriteByCurrentUser ? "Guardada" : "Guardar";
        const favoriteClass = guide.favoriteByCurrentUser ? " saved" : "";
        const userRatingText = currentUserRating > 0 ? `Tu voto: ${currentUserRating}/5` : "Sin votar";
        const isExpanded = state.expandedGuideIds.has(guide.id);
        const detailButtonText = isExpanded ? "Ocultar" : "Detalles";
        const embedUrl = youtubeEmbedUrl(guide.youtubeUrl);
        const detailSteps = orderedSteps.map(step => `
            <li>
                <strong>${step.stepNumber}. ${escapeHtml(step.title)}</strong>
                <p>${escapeHtml(step.description || "Sin descripcion")}</p>
            </li>
        `).join("");
        const detailResources = resources.map(resource => `
            <li>
                <span>${escapeHtml(resource.name)}</span>
                <strong>x${resource.requiredQuantity}</strong>
            </li>
        `).join("");
        const videoPanel = embedUrl
            ? `
                <div class="guide-video">
                    <iframe
                        src="${escapeHtml(embedUrl)}"
                        title="Video de ${escapeHtml(guide.title)}"
                        loading="lazy"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        allowfullscreen
                    ></iframe>
                </div>
            `
            : "";
        const detailPanel = isExpanded
            ? `
                <div class="guide-detail-panel">
                    <div>
                        <h4>Pasos de la guia</h4>
                        <ol class="guide-step-list">
                            ${detailSteps || "<li><strong>Sin pasos definidos</strong><p>El autor todavia no ha anadido pasos.</p></li>"}
                        </ol>
                    </div>
                    <div>
                        <h4>Recursos necesarios</h4>
                        <ul class="guide-resource-list">
                            ${detailResources || "<li><span>Sin recursos definidos</span><strong>-</strong></li>"}
                        </ul>
                    </div>
                    ${videoPanel}
                </div>
            `
            : "";
        const ownerActions = canManageGuide
            ? `
                <button type="button" class="small-button" data-edit-guide="${guide.id}">Editar</button>
                <button type="button" class="small-button danger" data-delete-guide="${guide.id}">Borrar</button>
            `
            : "";

        return `
            <article class="guide-card">
                <div class="guide-card-main">
                    <div>
                        <div class="status-line">
                            <strong>${escapeHtml(guide.title)}</strong>
                            <span class="meta-chip low">${escapeHtml(guide.type || "Guia")}</span>
                        </div>
                        <p>${escapeHtml(guide.description || "Sin descripcion")}</p>
                    </div>
                    <div class="guide-meta">
                        <span>${escapeHtml(guide.game)}</span>
                        <span>Por ${escapeHtml(guide.author || "Comunidad")}</span>
                        <span>${escapeHtml(guide.difficulty || "Media")}</span>
                        <span>${steps.length} pasos</span>
                        <span>${resources.length} recursos</span>
                        ${youtubeLink}
                    </div>
                </div>
                <div class="item-actions">
                    <button type="button" class="small-button" data-toggle-guide-details="${guide.id}">${detailButtonText}</button>
                    <button type="button" class="small-button${favoriteClass}" data-toggle-favorite-guide="${guide.id}" data-favorite="${guide.favoriteByCurrentUser}">${favoriteText}</button>
                    <button type="button" class="primary-button compact" data-apply-guide="${guide.id}"${disabledText}>Anadir al mundo</button>
                    ${ownerActions}
                </div>
                <div class="guide-card-wide">
                    <div class="guide-stats">
                        <span class="guide-stat"><strong>${presetRatingText(guide)}</strong><small>valoracion</small></span>
                        <span class="guide-stat"><strong>${guide.importCount || 0}</strong><small>importaciones</small></span>
                        <span class="guide-stat"><strong>${userRatingText}</strong><small>usuario actual</small></span>
                    </div>
                    <div class="guide-card-lower">
                        <div class="rating-row">
                            <span>Valorar</span>
                            <div class="rating-buttons">${ratingButtons}</div>
                        </div>
                        <div class="guide-resources">
                            ${firstResources || "<span>Sin recursos</span>"}
                            ${extraResources}
                        </div>
                    </div>
                </div>
                ${detailPanel}
            </article>
        `;
    }).join("");
}

function fillWorldForm(world) {
    els.worldId.value = world.id;
    els.worldName.value = world.name;
    els.worldGame.value = world.game;
    els.worldDescription.value = world.description || "";
    els.saveWorldButton.textContent = "Guardar mundo";
}

function resetWorldForm() {
    els.worldId.value = "";
    els.worldName.value = "";
    els.worldGame.value = "";
    els.worldDescription.value = "";
    els.saveWorldButton.textContent = "Crear mundo";
}

function resetMemberForm() {
    els.memberId.value = "";
    els.memberName.value = "";
    els.memberRole.value = "";
}

function resetTaskForm() {
    els.taskId.value = "";
    els.taskTitle.value = "";
    els.taskDescription.value = "";
    els.taskPriority.value = "Media";
}

function resetResourceForm() {
    els.resourceId.value = "";
    els.resourceName.value = "";
    els.requiredQuantity.value = "";
    els.collectedQuantity.value = "";
    els.resourceImportFile.value = "";
}

function resetGuideForm() {
    els.guideId.value = "";
    els.guideTitle.value = "";
    els.guideGame.value = "";
    els.guideAuthor.value = activeUserName();
    els.guideDifficulty.value = "Facil";
    els.guideType.value = "Estructura";
    els.guideYoutubeUrl.value = "";
    els.guideDescription.value = "";
    renderGuideStepRows();
    renderGuideResourceRows();
    els.saveGuideButton.textContent = "Publicar guia";
}

function fillGuideForm(guide) {
    els.guideId.value = guide.id;
    els.guideTitle.value = guide.title;
    els.guideGame.value = guide.game;
    els.guideAuthor.value = guide.author || "Comunidad";
    els.guideDifficulty.value = guide.difficulty || "Media";
    els.guideType.value = guide.type || "Estructura";
    els.guideYoutubeUrl.value = guide.youtubeUrl || "";
    els.guideDescription.value = guide.description || "";
    renderGuideStepRows(guide.steps || []);
    renderGuideResourceRows(guide.resources || []);
    els.saveGuideButton.textContent = "Guardar cambios";
}

function setResourceFormEnabled(enabled) {
    els.resourceName.disabled = !enabled;
    els.requiredQuantity.disabled = !enabled;
    els.collectedQuantity.disabled = !enabled;
    els.resourceForm.querySelector("button").disabled = !enabled;
    els.resourceImportFile.disabled = !enabled;
}

async function updateResourceQuantity(resourceId, field, value) {
    const world = selectedWorld();
    const task = selectedTask();
    const resource = state.resources.find(item => item.id === resourceId);

    if (!world || !task || !resource) {
        throw new Error("Recurso no disponible");
    }

    const payload = {
        name: resource.name,
        requiredQuantity: field === "requiredQuantity" ? value : resource.requiredQuantity,
        collectedQuantity: field === "collectedQuantity" ? value : resource.collectedQuantity
    };

    await api(`/worlds/${world.id}/tasks/${task.id}/resources/${resourceId}`, {
        method: "PUT",
        body: body(payload)
    });
}

async function updateTaskCompleted(taskId, completed) {
    const world = selectedWorld();
    const task = state.tasks.find(item => item.id === taskId);

    if (!world || !task) {
        throw new Error("Tarea no disponible");
    }

    await api(`/worlds/${world.id}/tasks/${taskId}`, {
        method: "PUT",
            body: body({
                title: task.title,
                description: task.description || "",
                priority: task.priority || "Media",
                completed
            })
    });
}

function getTaskCardAfterPointer(container, pointerY) {
    const cards = [...container.querySelectorAll("[data-task-card]:not(.dragging)")];

    return cards.reduce((closest, card) => {
        const box = card.getBoundingClientRect();
        const offset = pointerY - box.top - box.height / 2;

        if (offset < 0 && offset > closest.offset) {
            return {
                offset,
                element: card
            };
        }

        return closest;
    }, {
        offset: Number.NEGATIVE_INFINITY,
        element: null
    }).element;
}

function getResourceCardAfterPointer(container, pointerY) {
    const cards = [...container.querySelectorAll("[data-resource-card]:not(.dragging)")];

    return cards.reduce((closest, card) => {
        const box = card.getBoundingClientRect();
        const offset = pointerY - box.top - box.height / 2;

        if (offset < 0 && offset > closest.offset) {
            return {
                offset,
                element: card
            };
        }

        return closest;
    }, {
        offset: Number.NEGATIVE_INFINITY,
        element: null
    }).element;
}

async function saveTaskOrderFromDom() {
    const world = selectedWorld();

    if (!world || state.taskFilter !== "all") {
        return;
    }

    const taskIds = [...els.taskList.querySelectorAll("[data-task-card]")]
        .map(card => Number(card.dataset.taskCard));
    const currentTaskIds = state.tasks.map(task => task.id);
    const orderChanged = taskIds.some((taskId, index) => taskId !== currentTaskIds[index]);

    if (!orderChanged) {
        return;
    }

    try {
        state.tasks = await api(`/worlds/${world.id}/tasks/order`, {
            method: "PUT",
            body: body(taskIds)
        });
        renderTasks();
        showToast("Orden de tareas guardado");
    } catch (error) {
        showToast("No se pudo guardar el orden");
        await loadSelectedWorld();
    }
}

async function saveResourceOrderFromDom() {
    const world = selectedWorld();
    const task = selectedTask();

    if (!world || !task || state.resourceFilter !== "all") {
        return;
    }

    const resourceIds = [...els.resourceList.querySelectorAll("[data-resource-card]")]
        .map(card => Number(card.dataset.resourceCard));
    const currentResourceIds = state.resources.map(resource => resource.id);
    const orderChanged = resourceIds.some((resourceId, index) => resourceId !== currentResourceIds[index]);

    if (!orderChanged) {
        return;
    }

    try {
        state.resources = await api(`/worlds/${world.id}/tasks/${task.id}/resources/order`, {
            method: "PUT",
            body: body(resourceIds)
        });
        renderResources();
        showToast("Orden de recursos guardado");
    } catch (error) {
        showToast("No se pudo guardar el orden de recursos");
        await loadSelectedTaskResources();
        renderAll();
    }
}

function bindEvents() {
    els.showLoginButton.addEventListener("click", () => {
        showAuthView("login");
    });

    els.showRegisterButton.addEventListener("click", () => {
        showAuthView("register");
    });

    els.screenLoginForm.addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            username: els.screenLoginUsername.value.trim(),
            password: els.screenLoginPassword.value
        };

        try {
            const response = await api("/auth/login", { method: "POST", body: body(payload) });
            setSession(response.token, response.user);
            els.screenLoginPassword.value = "";
            showToast(`Bienvenido, ${activeUserName()}`);
            await startAuthenticatedApp();
        } catch (error) {
            showToast("Usuario o contrasena incorrectos");
        }
    });

    els.screenRegisterForm.addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            username: els.screenRegisterUsername.value.trim(),
            email: els.screenRegisterEmail.value.trim(),
            displayName: els.screenRegisterDisplayName.value.trim(),
            password: els.screenRegisterPassword.value
        };

        try {
            const response = await api("/auth/register", { method: "POST", body: body(payload) });
            setSession(response.token, response.user);
            els.screenRegisterForm.reset();
            showToast(`Cuenta creada para ${activeUserName()}`);
            await startAuthenticatedApp();
        } catch (error) {
            showToast("No se pudo crear la cuenta");
        }
    });

    els.logoutButton.addEventListener("click", () => {
        clearSession();
        showLoggedOutState();
        showAuthView("login");
        showToast("Sesion cerrada");
    });

    els.worldsNavButton.addEventListener("click", () => {
        setView("worlds");
    });

    els.presetsNavButton.addEventListener("click", () => {
        setView("presets");
        renderPresetWorldSelect();
        renderPresetFilters();
        renderGuides();
    });

    els.profileNavButton.addEventListener("click", () => {
        renderProfile();
        setView("profile");
    });

    els.profileWorldList.addEventListener("click", async event => {
        const button = event.target.closest("[data-profile-world]");

        if (!button) {
            return;
        }

        state.selectedWorldId = Number(button.dataset.profileWorld);
        state.selectedTaskId = null;
        resetTaskForm();
        resetResourceForm();
        renderWorlds();
        await loadSelectedWorld();
        setView("worlds");
    });

    els.profileGuideList.addEventListener("click", event => {
        const button = event.target.closest("[data-profile-guide]");

        if (!button) {
            return;
        }

        state.expandedGuideIds.add(Number(button.dataset.profileGuide));
        setView("presets");
        renderGuides();
    });

    els.profileFavoriteList.addEventListener("click", event => {
        const button = event.target.closest("[data-profile-guide]");

        if (!button) {
            return;
        }

        state.expandedGuideIds.add(Number(button.dataset.profileGuide));
        setView("presets");
        renderGuides();
    });

    els.presetWorldSelect.addEventListener("change", async () => {
        const worldId = presetTargetWorldId();

        if (!worldId) {
            renderGuides();
            return;
        }

        state.selectedWorldId = worldId;
        state.selectedTaskId = null;
        resetTaskForm();
        resetResourceForm();
        renderWorlds();
        await loadSelectedWorld();
        renderPresetWorldSelect();
    });

    els.presetSearch.addEventListener("input", () => {
        state.presetSearch = els.presetSearch.value.trim();
        renderGuides();
    });

    els.guideScopeFilter.addEventListener("change", () => {
        state.guideScopeFilter = els.guideScopeFilter.value;
        renderGuides();
    });

    els.presetGameFilter.addEventListener("change", () => {
        state.presetGameFilter = els.presetGameFilter.value;
        renderGuides();
    });

    els.presetTypeFilter.addEventListener("change", () => {
        state.presetTypeFilter = els.presetTypeFilter.value;
        renderGuides();
    });

    els.presetSort.addEventListener("change", () => {
        state.presetSort = els.presetSort.value;
        renderGuides();
    });

    els.worldList.addEventListener("click", async event => {
        const button = event.target.closest("[data-world-id]");

        if (!button) {
            return;
        }

        state.selectedWorldId = Number(button.dataset.worldId);
        state.selectedTaskId = null;
        resetMemberForm();
        resetTaskForm();
        resetResourceForm();
        renderWorlds();
        await loadSelectedWorld();
    });

    els.worldForm.addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            name: els.worldName.value.trim(),
            game: els.worldGame.value.trim(),
            description: els.worldDescription.value.trim()
        };

        const id = els.worldId.value;

        try {
            if (id) {
                await api(`/worlds/${id}`, { method: "PUT", body: body(payload) });
                showToast("Mundo actualizado");
            } else {
                const created = await api("/worlds", { method: "POST", body: body(payload) });
                state.selectedWorldId = created.id;
                showToast("Mundo creado");
            }

            await loadWorlds();
        } catch (error) {
            showToast("No se pudo guardar el mundo");
        }
    });

    els.resetWorldButton.addEventListener("click", resetWorldForm);

    els.deleteWorldButton.addEventListener("click", async () => {
        const world = selectedWorld();

        if (!world) {
            return;
        }

        const confirmed = await askConfirmation(
            "Borrar mundo",
            `Vas a borrar "${world.name}" junto con sus miembros, tareas y recursos.`,
            "Borrar mundo"
        );

        if (!confirmed) {
            return;
        }

        try {
            await api(`/worlds/${world.id}`, { method: "DELETE" });
            state.selectedWorldId = null;
            state.selectedTaskId = null;
            showToast("Mundo borrado");
            await loadWorlds();
        } catch (error) {
            showToast("No se pudo borrar el mundo");
        }
    });

    els.memberForm.addEventListener("submit", async event => {
        event.preventDefault();

        const world = selectedWorld();

        if (!world) {
            showToast("Selecciona un mundo");
            return;
        }

        const payload = {
            name: els.memberName.value.trim(),
            role: els.memberRole.value.trim()
        };

        const id = els.memberId.value;

        try {
            if (id) {
                await api(`/worlds/${world.id}/members/${id}`, { method: "PUT", body: body(payload) });
                showToast("Miembro actualizado");
            } else {
                await api(`/worlds/${world.id}/members`, { method: "POST", body: body(payload) });
                showToast("Miembro creado");
            }

            resetMemberForm();
            await loadSelectedWorld();
        } catch (error) {
            showToast("No se pudo guardar el miembro");
        }
    });

    els.memberList.addEventListener("click", async event => {
        const editButton = event.target.closest("[data-edit-member]");
        const deleteButton = event.target.closest("[data-delete-member]");
        const world = selectedWorld();

        if (!world) {
            return;
        }

        if (editButton) {
            const member = state.members.find(item => item.id === Number(editButton.dataset.editMember));
            els.memberId.value = member.id;
            els.memberName.value = member.name;
            els.memberRole.value = member.role;
            return;
        }

        if (deleteButton) {
            const memberId = Number(deleteButton.dataset.deleteMember);
            const member = state.members.find(item => item.id === memberId);

            const confirmed = await askConfirmation(
                "Borrar miembro",
                `Vas a borrar a "${member ? member.name : "este miembro"}" del mundo actual.`,
                "Borrar miembro"
            );

            if (!confirmed) {
                return;
            }

            await api(`/worlds/${world.id}/members/${memberId}`, { method: "DELETE" });
            resetMemberForm();
            showToast("Miembro borrado");
            await loadSelectedWorld();
        }
    });

    els.taskForm.addEventListener("submit", async event => {
        event.preventDefault();

        const world = selectedWorld();

        if (!world) {
            showToast("Selecciona un mundo");
            return;
        }

        const id = els.taskId.value;
        const existingTask = id
            ? state.tasks.find(task => task.id === Number(id))
            : null;
        const payload = {
            title: els.taskTitle.value.trim(),
            description: els.taskDescription.value.trim(),
            priority: els.taskPriority.value,
            completed: existingTask ? existingTask.completed : false
        };

        try {
            if (id) {
                await api(`/worlds/${world.id}/tasks/${id}`, { method: "PUT", body: body(payload) });
                state.selectedTaskId = Number(id);
                showToast("Tarea actualizada");
            } else {
                const created = await api(`/worlds/${world.id}/tasks`, { method: "POST", body: body(payload) });
                state.selectedTaskId = created.id;
                showToast("Tarea creada");
            }

            resetTaskForm();
            await loadSelectedWorld();
        } catch (error) {
            showToast("No se pudo guardar la tarea");
        }
    });

    els.taskFilter.addEventListener("change", () => {
        state.taskFilter = els.taskFilter.value;
        renderTasks();
    });

    els.taskList.addEventListener("dragstart", event => {
        const card = event.target.closest("[data-task-card]");

        if (!card || state.taskFilter !== "all") {
            return;
        }

        state.draggedTaskId = Number(card.dataset.taskCard);
        card.classList.add("dragging");
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("text/plain", card.dataset.taskCard);
    });

    els.taskList.addEventListener("dragover", event => {
        const draggingCard = els.taskList.querySelector(".dragging");

        if (!draggingCard || state.taskFilter !== "all") {
            return;
        }

        event.preventDefault();

        const afterCard = getTaskCardAfterPointer(els.taskList, event.clientY);

        if (afterCard === null) {
            els.taskList.appendChild(draggingCard);
        } else {
            els.taskList.insertBefore(draggingCard, afterCard);
        }
    });

    els.taskList.addEventListener("dragend", async event => {
        const card = event.target.closest("[data-task-card]");

        if (card) {
            card.classList.remove("dragging");
        }

        state.draggedTaskId = null;
        await saveTaskOrderFromDom();
    });

    els.taskList.addEventListener("click", async event => {
        const selectButton = event.target.closest("[data-select-task]");
        const editButton = event.target.closest("[data-edit-task]");
        const deleteButton = event.target.closest("[data-delete-task]");
        const completeButton = event.target.closest("[data-toggle-task-completed]");
        const world = selectedWorld();

        if (!world) {
            return;
        }

        if (completeButton) {
            const taskId = Number(completeButton.dataset.toggleTaskCompleted);
            const task = state.tasks.find(item => item.id === taskId);

            if (!task) {
                return;
            }

            try {
                await updateTaskCompleted(taskId, !task.completed);
                showToast(task.completed ? "Tarea reabierta" : "Tarea completada");
                await loadSelectedWorld();
            } catch (error) {
                showToast("No se pudo actualizar la tarea");
            }

            return;
        }

        if (selectButton) {
            state.selectedTaskId = Number(selectButton.dataset.selectTask);
            resetResourceForm();
            await loadSelectedTaskResources();
            renderAll();
            return;
        }

        if (editButton) {
            const task = state.tasks.find(item => item.id === Number(editButton.dataset.editTask));
            els.taskId.value = task.id;
            els.taskTitle.value = task.title;
            els.taskDescription.value = task.description || "";
            els.taskPriority.value = task.priority || "Media";
            return;
        }

        if (deleteButton) {
            const taskId = Number(deleteButton.dataset.deleteTask);
            const task = state.tasks.find(item => item.id === taskId);

            const confirmed = await askConfirmation(
                "Borrar tarea",
                `Vas a borrar "${task ? task.title : "esta tarea"}" y sus recursos asociados.`,
                "Borrar tarea"
            );

            if (!confirmed) {
                return;
            }

            await api(`/worlds/${world.id}/tasks/${taskId}`, { method: "DELETE" });

            if (state.selectedTaskId === taskId) {
                state.selectedTaskId = null;
            }

            resetTaskForm();
            resetResourceForm();
            showToast("Tarea borrada");
            await loadSelectedWorld();
        }
    });

    els.resourceForm.addEventListener("submit", async event => {
        event.preventDefault();

        const world = selectedWorld();
        const task = selectedTask();

        if (!world || !task) {
            showToast("Selecciona una tarea");
            return;
        }

        const payload = {
            name: els.resourceName.value.trim(),
            requiredQuantity: Number(els.requiredQuantity.value),
            collectedQuantity: Number(els.collectedQuantity.value)
        };

        const id = els.resourceId.value;

        try {
            if (id) {
                await api(`/worlds/${world.id}/tasks/${task.id}/resources/${id}`, {
                    method: "PUT",
                    body: body(payload)
                });
                showToast("Recurso actualizado");
            } else {
                await api(`/worlds/${world.id}/tasks/${task.id}/resources`, {
                    method: "POST",
                    body: body(payload)
                });
                showToast("Recurso creado");
            }

            resetResourceForm();
            await loadSelectedTaskResources();
            renderAll();
        } catch (error) {
            showToast("No se pudo guardar el recurso");
        }
    });

    els.resourceImportFile.addEventListener("change", async event => {
        const file = event.target.files[0];
        const world = selectedWorld();
        const task = selectedTask();

        if (!file) {
            return;
        }

        if (!world || !task) {
            showToast("Selecciona una tarea");
            event.target.value = "";
            return;
        }

        const formData = new FormData();
        formData.append("file", file);
        event.target.disabled = true;

        try {
            const result = await api(`/worlds/${world.id}/tasks/${task.id}/resources/import`, {
                method: "POST",
                body: formData
            });

            await loadSelectedTaskResources();
            renderAll();
            showToast(`Importados ${result.importedCount} materiales`);
        } catch (error) {
            showToast("No se pudo importar el archivo");
        } finally {
            event.target.value = "";
            event.target.disabled = !selectedTask();
        }
    });

    els.resourceFilter.addEventListener("change", () => {
        state.resourceFilter = els.resourceFilter.value;
        renderResources();
    });

    els.resourceList.addEventListener("dragstart", event => {
        const card = event.target.closest("[data-resource-card]");

        if (!card || state.resourceFilter !== "all") {
            return;
        }

        state.draggedResourceId = Number(card.dataset.resourceCard);
        card.classList.add("dragging");
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("text/plain", card.dataset.resourceCard);
    });

    els.resourceList.addEventListener("dragover", event => {
        const draggingCard = els.resourceList.querySelector(".dragging");

        if (!draggingCard || state.resourceFilter !== "all") {
            return;
        }

        event.preventDefault();

        const afterCard = getResourceCardAfterPointer(els.resourceList, event.clientY);

        if (afterCard === null) {
            els.resourceList.appendChild(draggingCard);
        } else {
            els.resourceList.insertBefore(draggingCard, afterCard);
        }
    });

    els.resourceList.addEventListener("dragend", async event => {
        const card = event.target.closest("[data-resource-card]");

        if (card) {
            card.classList.remove("dragging");
        }

        state.draggedResourceId = null;
        await saveResourceOrderFromDom();
    });

    els.resourceList.addEventListener("change", async event => {
        const input = event.target.closest("[data-resource-quantity]");

        if (!input) {
            return;
        }

        const resourceId = Number(input.dataset.resourceQuantity);
        const field = input.dataset.quantityField;
        const value = Number(input.value);

        if (!Number.isFinite(value) || value < 0) {
            showToast("Introduce una cantidad valida");
            renderResources();
            return;
        }

        input.disabled = true;

        try {
            await updateResourceQuantity(resourceId, field, value);
            await loadSelectedTaskResources();
            renderAll();
            showToast("Cantidad actualizada");
        } catch (error) {
            await loadSelectedTaskResources();
            renderAll();
            showToast("No se pudo actualizar la cantidad");
        }
    });

    els.resourceList.addEventListener("click", async event => {
        const editButton = event.target.closest("[data-edit-resource]");
        const deleteButton = event.target.closest("[data-delete-resource]");
        const world = selectedWorld();
        const task = selectedTask();

        if (!world || !task) {
            return;
        }

        if (editButton) {
            const resource = state.resources.find(item => item.id === Number(editButton.dataset.editResource));
            els.resourceId.value = resource.id;
            els.resourceName.value = resource.name;
            els.requiredQuantity.value = resource.requiredQuantity;
            els.collectedQuantity.value = resource.collectedQuantity;
            return;
        }

        if (deleteButton) {
            const resourceId = Number(deleteButton.dataset.deleteResource);
            const resource = state.resources.find(item => item.id === resourceId);

            const confirmed = await askConfirmation(
                "Borrar recurso",
                `Vas a borrar "${resource ? resource.name : "este recurso"}" de la tarea actual.`,
                "Borrar recurso"
            );

            if (!confirmed) {
                return;
            }

            await api(`/worlds/${world.id}/tasks/${task.id}/resources/${resourceId}`, { method: "DELETE" });
            resetResourceForm();
            showToast("Recurso borrado");
            await loadSelectedTaskResources();
            renderAll();
        }
    });

    els.guideForm.addEventListener("submit", async event => {
        event.preventDefault();

        const isEditing = Boolean(els.guideId.value);
        const authorName = isEditing ? els.guideAuthor.value.trim() : activeUserName();

        if (!authorName) {
            showToast("Selecciona o crea un usuario");
            return;
        }

        const payload = {
            title: els.guideTitle.value.trim(),
            game: els.guideGame.value.trim(),
            type: els.guideType.value,
            author: authorName,
            difficulty: els.guideDifficulty.value,
            description: els.guideDescription.value.trim(),
            youtubeUrl: els.guideYoutubeUrl.value.trim(),
            steps: parseGuideSteps(),
            resources: parseGuideResources()
        };
        const id = els.guideId.value;

        try {
            if (id) {
                await api(`/guides/${id}`, { method: "PUT", body: body(payload) });
                showToast("Guia actualizada");
            } else {
                await api("/guides", { method: "POST", body: body(payload) });
                showToast("Guia publicada");
            }

            resetGuideForm();
            await loadGuides();
        } catch (error) {
            showToast("No se pudo guardar la guia");
        }
    });

    els.resetGuideButton.addEventListener("click", resetGuideForm);

    els.addGuideStepButton.addEventListener("click", () => {
        addGuideStepRow();
    });

    els.addGuideResourceButton.addEventListener("click", () => {
        addGuideResourceRow();
    });

    els.guideSteps.addEventListener("click", event => {
        const removeButton = event.target.closest("[data-remove-guide-step]");

        if (!removeButton) {
            return;
        }

        removeButton.closest("[data-guide-step-row]").remove();

        if (els.guideSteps.querySelectorAll("[data-guide-step-row]").length === 0) {
            addGuideStepRow();
        }

        updateGuideBuilderIndexes();
    });

    els.guideResources.addEventListener("click", event => {
        const removeButton = event.target.closest("[data-remove-guide-resource]");

        if (!removeButton) {
            return;
        }

        removeButton.closest("[data-guide-resource-row]").remove();

        if (els.guideResources.querySelectorAll("[data-guide-resource-row]").length === 0) {
            addGuideResourceRow();
        }

        updateGuideBuilderIndexes();
    });

    els.guideList.addEventListener("click", async event => {
        const applyButton = event.target.closest("[data-apply-guide]");
        const editButton = event.target.closest("[data-edit-guide]");
        const deleteButton = event.target.closest("[data-delete-guide]");
        const rateButton = event.target.closest("[data-rate-guide]");
        const favoriteButton = event.target.closest("[data-toggle-favorite-guide]");
        const detailsButton = event.target.closest("[data-toggle-guide-details]");

        if (detailsButton) {
            const guideId = Number(detailsButton.dataset.toggleGuideDetails);

            if (state.expandedGuideIds.has(guideId)) {
                state.expandedGuideIds.delete(guideId);
            } else {
                state.expandedGuideIds.add(guideId);
            }

            renderGuides();
            return;
        }

        if (rateButton) {
            const guideId = Number(rateButton.dataset.rateGuide);
            const rating = Number(rateButton.dataset.rating);
            const userName = activeUserName();

            if (!userName) {
                showToast("Inicia sesion para votar");
                showAuthView("login");
                return;
            }

            try {
                await api(`/guides/${guideId}/rating`, {
                    method: "POST",
                    body: body({ rating })
                });
                showToast(`Voto de ${userName} guardado`);
                await loadGuides();
            } catch (error) {
                showToast("No se pudo guardar la valoracion");
            }

            return;
        }

        if (favoriteButton) {
            const guideId = Number(favoriteButton.dataset.toggleFavoriteGuide);
            const isFavorite = favoriteButton.dataset.favorite === "true";

            try {
                await api(`/guides/${guideId}/favorite`, {
                    method: isFavorite ? "DELETE" : "POST"
                });
                showToast(isFavorite ? "Guia quitada de guardadas" : "Guia guardada");
                await loadGuides();
            } catch (error) {
                showToast("No se pudo actualizar la guia guardada");
            }

            return;
        }

        if (editButton) {
            const guide = state.guides.find(item => item.id === Number(editButton.dataset.editGuide));

            if (guide) {
                fillGuideForm(guide);
                els.guideTitle.focus();
            }

            return;
        }

        if (deleteButton) {
            const guideId = Number(deleteButton.dataset.deleteGuide);
            const guide = state.guides.find(item => item.id === guideId);
            const confirmed = await askConfirmation(
                "Borrar guia",
                `Vas a borrar "${guide ? guide.title : "esta guia"}" de la biblioteca online simulada.`,
                "Borrar guia"
            );

            if (!confirmed) {
                return;
            }

            try {
                await api(`/guides/${guideId}`, { method: "DELETE" });
                showToast("Guia borrada");
                await loadGuides();
            } catch (error) {
                showToast("No se pudo borrar la guia");
            }

            return;
        }

        if (applyButton) {
            const worldId = presetTargetWorldId();
            const world = state.worlds.find(item => item.id === worldId);
            const guideId = Number(applyButton.dataset.applyGuide);
            const guide = state.guides.find(item => item.id === guideId);

            if (!world) {
                showToast("Selecciona un mundo destino");
                return;
            }

            const duplicateTask = state.selectedWorldId === world.id
                ? state.tasks.find(task => normalizedText(task.title) === normalizedText(guide ? guide.title : ""))
                : null;

            if (duplicateTask) {
                const goToTask = await askConfirmation(
                    "Tarea ya existente",
                    `El mundo "${world.name}" ya tiene una tarea llamada "${duplicateTask.title}". No se creara otra copia.`,
                    "Ver tarea"
                );

                if (goToTask) {
                    state.selectedWorldId = world.id;
                    state.selectedTaskId = duplicateTask.id;
                    resetTaskForm();
                    resetResourceForm();
                    await loadSelectedWorld();
                    renderWorlds();
                    setView("worlds");
                    showToast("Tarea existente seleccionada");
                }

                return;
            }

            const resourceCount = guide && guide.resources ? guide.resources.length : 0;
            const confirmed = await askConfirmation(
                "Anadir guia al mundo",
                `Se creara una tarea llamada "${guide ? guide.title : "guia seleccionada"}" en "${world.name}" con ${resourceCount} recursos iniciales.`,
                "Anadir guia"
            );

            if (!confirmed) {
                return;
            }

            try {
                const result = await api(`/guides/${guideId}/apply/worlds/${world.id}`, { method: "POST" });
                state.selectedWorldId = world.id;
                state.selectedTaskId = result.task.id;
                resetTaskForm();
                resetResourceForm();
                await loadSelectedWorld();
                await loadGuides();
                renderWorlds();
                setView("worlds");
                showToast(`Guia "${guide ? guide.title : "seleccionada"}" anadida a ${world.name}`);
            } catch (error) {
                if (error.status === 409) {
                    showToast("Ese mundo ya tiene una tarea con el mismo titulo");
                } else {
                    showToast("No se pudo anadir la guia");
                }
            }
        }
    });
}

async function startAuthenticatedApp() {
    renderAuth();
    resetGuideForm();
    await loadGuides();
    await loadWorlds();
    setView("worlds");
}

function showLoggedOutState() {
    state.worlds = [];
    state.members = [];
    state.tasks = [];
    state.resources = [];
    state.guides = [];
    state.selectedWorldId = null;
    state.selectedTaskId = null;
    state.summary = null;
    state.dashboard = null;

    renderAuth();
    resetWorldForm();
    resetMemberForm();
    resetTaskForm();
    resetResourceForm();
    resetGuideForm();

    els.worldCount.textContent = "0";
    els.worldList.innerHTML = `<div class="empty-state">Inicia sesion para cargar tus mundos</div>`;
    els.memberList.innerHTML = `<div class="empty-state">Inicia sesion</div>`;
    els.taskList.innerHTML = `<div class="empty-state">Inicia sesion</div>`;
    els.resourceList.innerHTML = `<div class="empty-state">Inicia sesion</div>`;
    els.guideList.innerHTML = `<div class="empty-state">Inicia sesion para usar la biblioteca</div>`;
    els.selectedWorldGame.textContent = "Sesion no iniciada";
    els.selectedWorldName.textContent = "Survival Hub";
    els.selectedWorldDescription.textContent = "Inicia sesion o crea una cuenta para gestionar tus mundos.";
    els.memberMetric.textContent = "0";
    els.taskMetric.textContent = "0";
    els.resourceMetric.textContent = "0";
    els.progressMetric.textContent = "0%";
    els.pendingTaskInsight.textContent = "0";
    els.completedTaskInsight.textContent = "0";
    els.highPriorityInsight.textContent = "0";
    els.nextFocusInsight.textContent = "Sin tareas";
    renderProfile();
    els.presetCount.textContent = "0";
    showAuthView("login");
}

bindEvents();
renderAuth();

if (state.token) {
    loadCurrentUser()
        .then(startAuthenticatedApp)
        .catch(() => {
            clearSession();
            showLoggedOutState();
            showToast("Inicia sesion de nuevo");
        });
} else {
    showLoggedOutState();
}
