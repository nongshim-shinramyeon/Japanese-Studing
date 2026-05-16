import { fetchCollection } from "./api.js";

const tableBody = document.querySelector("#word-table-body");
const emptyState = document.querySelector("#word-empty");
const countTarget = document.querySelector("#word-count");
const pageTarget = document.querySelector("#word-page");
const messageBox = document.querySelector("#word-message");
const prevButton = document.querySelector("#word-prev-page");
const nextButton = document.querySelector("#word-next-page");
const levelButtons = document.querySelectorAll("[data-level]");

let currentLevel = "N5";
let currentPage = 0;
let totalPages = 1;

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
        `;
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
        jlptLevel: currentLevel,
        page: String(page),
        size: "25",
        sort: "id,asc"
    });

    try {
        const data = await fetchCollection(`/api/words?${params.toString()}`);
        renderRows(data);
    } catch (error) {
        renderMessage(error.message);
    }
}

for (const button of levelButtons) {
    button.addEventListener("click", async () => {
        currentLevel = button.dataset.level;
        renderActiveLevel();
        await loadWords(0);
    });
}

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
loadWords();
