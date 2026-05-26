import { createResource, fetchCollection, patchResource } from "./api.js";

const tableBody = document.querySelector("#review-table-body");
const emptyState = document.querySelector("#review-empty");
const countTarget = document.querySelector("#review-count");
const pageTarget = document.querySelector("#review-page");
const rangeTarget = document.querySelector("#review-range");
const messageBox = document.querySelector("#review-message");
const averageScoreTarget = document.querySelector("#review-average-score");
const dueTarget = document.querySelector("#review-due");
const lowScoreTarget = document.querySelector("#review-low-score");
const levelProgress = document.querySelector("#review-level-progress");
const prevButton = document.querySelector("#review-prev-page");
const nextButton = document.querySelector("#review-next-page");
const levelButtons = document.querySelectorAll("[data-review-level]");
const currentUser = document.querySelector("#current-user");
const authLink = document.querySelector("#auth-link");
const filterForm = document.querySelector("#review-filters");
const resetFiltersButton = document.querySelector("#review-reset-filters");

let currentLevel = "N5";
let currentKeyword = "";
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

function formatDate(dateTime) {
    if (!dateTime) {
        return "Next: not scheduled";
    }

    return `Next: ${new Date(dateTime).toLocaleDateString()}`;
}

function scoreClass(score) {
    if (score < 50) {
        return "low";
    }
    if (score < 80) {
        return "mid";
    }
    return "high";
}

function renderDashboard(progress) {
    averageScoreTarget.textContent = progress?.averageMemoryScore ?? 0;
    dueTarget.textContent = progress?.dueReviews ?? 0;
    lowScoreTarget.textContent = progress?.reviewNeeded ?? 0;
    levelProgress.innerHTML = "";

    for (const level of progress?.levels || []) {
        const item = document.createElement("article");
        item.className = "level-progress-item";
        item.innerHTML = `
            <strong>${escapeHtml(level.jlptLevel)}</strong>
            <span>${escapeHtml(level.averageMemoryScore)} pts</span>
            <progress max="100" value="${escapeHtml(level.averageMemoryScore)}"></progress>
            <small>${escapeHtml(level.masteredCount)} stage 7 / ${escapeHtml(level.studiedCount)} studied</small>
        `;
        levelProgress.appendChild(item);
    }
}

async function loadDashboard() {
    try {
        renderDashboard(await fetchCollection("/api/words/dashboard"));
    } catch (error) {
        renderMessage(error.message);
    }
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
        const score = Number(word.currentMemoryScore ?? word.memoryScore ?? 0);
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><strong>${escapeHtml(word.japanese)}</strong></td>
            <td>${escapeHtml(word.reading)}</td>
            <td>${escapeHtml(word.meaning)}</td>
            <td><span class="pill">${escapeHtml(word.jlptLevel)}</span></td>
            <td>
                <div class="score-cell">
                    <div class="score-row">
                        <strong>${escapeHtml(score.toFixed(1))}</strong>
                        <span class="pill">Stage ${escapeHtml(word.memoryStage ?? 1)} / 7</span>
                    </div>
                    <div class="score-meter ${scoreClass(score)}">
                        <span style="width: ${Math.max(0, Math.min(100, score))}%"></span>
                    </div>
                    <span class="table-note">${escapeHtml(formatDate(word.nextReviewAt))}</span>
                </div>
            </td>
            <td>
                <div class="review-actions">
                    <button type="button" class="secondary" data-review="known" data-word-id="${escapeHtml(word.id)}">Known</button>
                    <button type="button" class="danger" data-review="missed" data-word-id="${escapeHtml(word.id)}">Missed</button>
                </div>
            </td>
        `;
        row.querySelectorAll("[data-review]").forEach((button) => {
            button.addEventListener("click", handleReviewClick);
        });
        tableBody.appendChild(row);
    }
}

function renderActiveLevel() {
    for (const button of levelButtons) {
        button.classList.toggle("active", button.dataset.reviewLevel === currentLevel);
    }
}

async function loadReviewWords(page = 0) {
    clearMessage();
    const params = new URLSearchParams({
        page: String(page),
        size: String(currentPageSize)
    });

    if (currentLevel) {
        params.set("jlptLevel", currentLevel);
    }
    if (currentKeyword) {
        params.set("keyword", currentKeyword);
    }

    try {
        const data = await fetchCollection(`/api/words/review?${params.toString()}`);
        if (page > 0 && (data?.content || []).length === 0 && (data?.totalElements || 0) > 0) {
            await loadReviewWords(Math.max(0, (data?.totalPages || 1) - 1));
            return;
        }
        renderRows(data);
    } catch (error) {
        renderMessage(error.message);
    }
}

async function handleReviewClick(event) {
    const button = event.currentTarget;
    button.disabled = true;

    try {
        await patchResource(`/api/words/${button.dataset.wordId}/review`, {
            correct: button.dataset.review === "known"
        });
        renderMessage("Review score updated.", "success");
        await loadDashboard();
        await loadReviewWords(currentPage);
    } catch (error) {
        renderMessage(error.message);
    } finally {
        button.disabled = false;
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
        currentLevel = button.dataset.reviewLevel;
        renderActiveLevel();
        await loadReviewWords(0);
    });
}

filterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    currentKeyword = filterForm.keyword.value.trim();
    currentPageSize = Number(filterForm.pageSize.value);
    await loadReviewWords(0);
});

resetFiltersButton.addEventListener("click", async () => {
    filterForm.reset();
    currentLevel = "N5";
    currentKeyword = "";
    currentPageSize = 25;
    renderActiveLevel();
    await loadReviewWords(0);
});

prevButton.addEventListener("click", async () => {
    if (currentPage > 0) {
        await loadReviewWords(currentPage - 1);
    }
});

nextButton.addEventListener("click", async () => {
    if (currentPage + 1 < totalPages) {
        await loadReviewWords(currentPage + 1);
    }
});

renderActiveLevel();
await ensureLoggedIn();
await loadDashboard();
loadReviewWords();
