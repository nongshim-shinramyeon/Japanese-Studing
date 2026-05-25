import { createResource, deleteResource, fetchCollection, updateResource } from "./api.js";

const form = document.querySelector("#grammar-form");
const messageBox = document.querySelector("#grammar-message");
const tableBody = document.querySelector("#grammar-table-body");
const emptyState = document.querySelector("#grammar-empty");
const countTarget = document.querySelector("#grammar-count");
const pageTarget = document.querySelector("#grammar-page");
const filterForm = document.querySelector("#grammar-filters");
const cancelEditButton = document.querySelector("#cancel-grammar-edit");

let editingId = null;
let currentPage = 0;
let totalPages = 1;
let currentFilters = {
    jlptLevel: "",
    studyStatus: ""
};

function renderMessage(text, type = "") {
    messageBox.textContent = text;
    messageBox.className = `message ${type}`.trim();
}

function clearMessage() {
    messageBox.textContent = "Î¨∏Î≤ï ?∏Ìä∏Î•??±Î°ù?òÍ±∞???òÏ†ï?òÎ©¥ ?¨Í∏∞??Í≤∞Í≥ºÍ∞Ä ?úÏãú?©Îãà??";
    messageBox.className = "message";
}

function resetForm() {
    form.reset();
    editingId = null;
    form.querySelector("button[type='submit']").textContent = "Î¨∏Î≤ï ?∏Ìä∏ ?Ä??;
    cancelEditButton.hidden = true;
}

function renderRows(page) {
    tableBody.innerHTML = "";
    const items = page?.content || [];
    totalPages = page?.totalPages || 1;
    countTarget.textContent = page?.totalElements ?? 0;
    pageTarget.textContent = `${(page?.number ?? 0) + 1} / ${totalPages}`;

    if (items.length === 0) {
        emptyState.hidden = false;
        return;
    }

    emptyState.hidden = true;
    for (const note of items) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>
                <div class="stack">
                    <strong>${note.title}</strong>
                    <span class="muted">${note.patternExpression}</span>
                </div>
            </td>
            <td>${note.meaning}</td>
            <td>${note.explanation}</td>
            <td>
                <div class="stack">
                    <span class="pill">${note.jlptLevel}</span>
                    <span class="pill">${note.studyStatus}</span>
                </div>
            </td>
            <td>${note.exampleSentence}</td>
            <td>
                <div class="button-row">
                    <button type="button" class="secondary" data-edit="${note.id}">?òÏ†ï</button>
                    <button type="button" class="danger" data-delete="${note.id}">??†ú</button>
                </div>
            </td>
        `;
        row.querySelector("[data-edit]").addEventListener("click", () => startEdit(note));
        row.querySelector("[data-delete]").addEventListener("click", () => handleDelete(note.id));
        tableBody.appendChild(row);
    }
}

function startEdit(note) {
    editingId = note.id;
    form.title.value = note.title;
    form.patternExpression.value = note.patternExpression;
    form.meaning.value = note.meaning;
    form.explanation.value = note.explanation;
    form.exampleSentence.value = note.exampleSentence;
    form.jlptLevel.value = note.jlptLevel;
    form.studyStatus.value = note.studyStatus;
    form.querySelector("button[type='submit']").textContent = "Î¨∏Î≤ï ?∏Ìä∏ ?òÏ†ï";
    cancelEditButton.hidden = false;
    renderMessage("?òÏ†ï Î™®ÎìúÎ°??ÑÌôò?àÏäµ?àÎã§.", "success");
}

async function loadNotes(page = 0) {
    currentPage = page;
    const params = new URLSearchParams({
        page: String(page),
        size: "10",
        sort: "createdAt,desc"
    });

    if (currentFilters.jlptLevel) {
        params.set("jlptLevel", currentFilters.jlptLevel);
    }
    if (currentFilters.studyStatus) {
        params.set("studyStatus", currentFilters.studyStatus);
    }

    try {
        const data = await fetchCollection(`/api/grammar-notes?${params.toString()}`);
        renderRows(data);
    } catch (error) {
        renderMessage(error.message, "error");
    }
}

async function handleDelete(id) {
    try {
        await deleteResource(`/api/grammar-notes/${id}`);
        renderMessage("Î¨∏Î≤ï ?∏Ìä∏Î•???†ú?àÏäµ?àÎã§.", "success");
        if (editingId === id) {
            resetForm();
        }
        await loadNotes(currentPage);
    } catch (error) {
        renderMessage(error.message, "error");
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        title: form.title.value,
        patternExpression: form.patternExpression.value,
        meaning: form.meaning.value,
        explanation: form.explanation.value,
        exampleSentence: form.exampleSentence.value,
        jlptLevel: form.jlptLevel.value,
        studyStatus: form.studyStatus.value
    };

    try {
        if (editingId) {
            await updateResource(`/api/grammar-notes/${editingId}`, body);
            renderMessage("Î¨∏Î≤ï ?∏Ìä∏Î•??òÏ†ï?àÏäµ?àÎã§.", "success");
        } else {
            await createResource("/api/grammar-notes", body);
            renderMessage("Î¨∏Î≤ï ?∏Ìä∏Î•??±Î°ù?àÏäµ?àÎã§.", "success");
        }
        resetForm();
        await loadNotes(0);
    } catch (error) {
        renderMessage(error.message, "error");
    }
});

filterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    currentFilters = {
        jlptLevel: filterForm.jlptLevel.value,
        studyStatus: filterForm.studyStatus.value
    };
    await loadNotes(0);
});

document.querySelector("#grammar-reset-filters").addEventListener("click", async () => {
    filterForm.reset();
    currentFilters = { jlptLevel: "", studyStatus: "" };
    await loadNotes(0);
});

document.querySelector("#grammar-prev-page").addEventListener("click", async () => {
    if (currentPage > 0) {
        await loadNotes(currentPage - 1);
    }
});

document.querySelector("#grammar-next-page").addEventListener("click", async () => {
    if (currentPage + 1 < totalPages) {
        await loadNotes(currentPage + 1);
    }
});

cancelEditButton.addEventListener("click", () => {
    resetForm();
    clearMessage();
});

clearMessage();
loadNotes();
