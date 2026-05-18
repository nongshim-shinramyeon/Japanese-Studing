import { createResource, fetchCollection, patchResource } from "./api.js";

const tableBody = document.querySelector("#word-table-body");
const emptyState = document.querySelector("#word-empty");
const countTarget = document.querySelector("#word-count");
const pageTarget = document.querySelector("#word-page");
const rangeTarget = document.querySelector("#word-range");
const messageBox = document.querySelector("#word-message");
const prevButton = document.querySelector("#word-prev-page");
const nextButton = document.querySelector("#word-next-page");
const levelButtons = document.querySelectorAll("[data-level]");
const currentUser = document.querySelector("#current-user");
const authLink = document.querySelector("#auth-link");
const filterForm = document.querySelector("#word-filters");
const resetFiltersButton = document.querySelector("#word-reset-filters");

let currentLevel = "N5";
let currentStatus = "";
let currentPage = 0;
let totalPages = 1;
let currentPageSize = 25;

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function renderMessage(text, type = "error") {
    messageBox.textContent = text;
    messageBox.className = `message ${type}`.trim();
    messageBox.hidden = false;
}

function clearMessage() {
    messageBox.hidden = true;
    messageBox.textContent = "";
}

function renderRows(page) {
    const items = page?.content || [];
    totalPages = Math.max(page?.totalPages || 1, 1);
    currentPage = page?.number ?? 0;

    tableBody.innerHTML = "";
    countTarget.textContent = page?.totalElements ?? 0;
    pageTarget.textContent = `${currentPage + 1} / ${totalPages}`;
    prevButton.disabled = currentPage <= 0;
    nextButton.disabled = currentPage + 1 >= totalPages;

    const start = items.length === 0 ? 0 : (currentPage * currentPageSize) + 1;
    const end = items.length === 0 ? 0 : start + items.length - 1;
    rangeTarget.textContent = `Showing ${start}-${end} of ${page?.totalElements ?? 0}`;

    if (items.length === 0) {
        emptyState.hidden = false;
        return;
    }

    emptyState.hidden = true;
    for (const word of items) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><strong>${escapeHtml(word.japanese)}</strong></td>
            <td>${escapeHtml(word.reading)}</td>
            <td>${escapeHtml(word.meaning)}</td>
            <td><span class="pill">${escapeHtml(word.jlptLevel)}</span></td>
            <td>
                <select class="status-select" data-word-id="${escapeHtml(word.id)}">
                    <option value="NEW" ${word.studyStatus === "NEW" ? "selected" : ""}>NEW</option>
                    <option value="LEARNING" ${word.studyStatus === "LEARNING" ? "selected" : ""}>LEARNING</option>
                    <option value="REVIEW_NEEDED" ${word.studyStatus === "REVIEW_NEEDED" ? "selected" : ""}>REVIEW_NEEDED</option>
                    <option value="MASTERED" ${word.studyStatus === "MASTERED" ? "selected" : ""}>MASTERED</option>
                </select>
            </td>
        `;
        row.querySelector("[data-word-id]").addEventListener("change", handleStatusChange);
        tableBody.appendChild(row);
    }
}

function renderActiveLevel() {
    for (const button of levelButtons) {
        button.classList.toggle("active", button.dataset.level === currentLevel);
    }
}

async function loadWords(page = 0) {
    clearMessage();
    const params = new URLSearchParams({
        page: String(page),
        size: String(currentPageSize),
        sort: "id,asc"
    });

    if (currentLevel) {
        params.set("jlptLevel", currentLevel);
    }
    if (currentStatus) {
        params.set("studyStatus", currentStatus);
    }

    try {
        const data = await fetchCollection(`/api/words?${params.toString()}`);
        if (page > 0 && (data?.content || []).length === 0 && (data?.totalElements || 0) > 0) {
            await loadWords(Math.max(0, (data?.totalPages || 1) - 1));
            return;
        }
        renderRows(data);
    } catch (error) {
        renderMessage(error.message);
    }
}

async function handleStatusChange(event) {
    const select = event.currentTarget;
    select.disabled = true;

    try {
        await patchResource(`/api/words/${select.dataset.wordId}/status`, {
            studyStatus: select.value
        });
        await loadWords(currentPage);
    } catch (error) {
        renderMessage(error.message);
    } finally {
        select.disabled = false;
    }
}

async function ensureLoggedIn() {
    try {
        const user = await fetchCollection("/api/auth/me");
        currentUser.textContent = user.username;
        authLink.textContent = "Logout";
        authLink.href = "#";
        authLink.addEventListener("click", async (event) => {
            event.preventDefault();
            await createResource("/api/auth/logout", {});
            window.location.href = "/login.html";
        });
    } catch {
        window.location.href = "/login.html";
    }
}

for (const button of levelButtons) {
    button.addEventListener("click", async () => {
        currentLevel = button.dataset.level;
        renderActiveLevel();
        await loadWords(0);
    });
}

filterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    currentStatus = filterForm.studyStatus.value;
    currentPageSize = Number(filterForm.pageSize.value);
    await loadWords(0);
});

resetFiltersButton.addEventListener("click", async () => {
    filterForm.reset();
    currentLevel = "N5";
    currentStatus = "";
    currentPageSize = 25;
    renderActiveLevel();
    await loadWords(0);
});

prevButton.addEventListener("click", async () => {
    if (currentPage > 0) {
        await loadWords(currentPage - 1);
    }
});

nextButton.addEventListener("click", async () => {
    if (currentPage + 1 < totalPages) {
        await loadWords(currentPage + 1);
    }
});

renderActiveLevel();
await ensureLoggedIn();
loadWords();
