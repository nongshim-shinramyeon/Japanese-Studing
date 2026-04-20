import { createResource, deleteResource, fetchCollection, updateResource } from "./api.js";

const form = document.querySelector("#word-form");
const messageBox = document.querySelector("#word-message");
const tableBody = document.querySelector("#word-table-body");
const emptyState = document.querySelector("#word-empty");
const countTarget = document.querySelector("#word-count");
const pageTarget = document.querySelector("#word-page");
const filterForm = document.querySelector("#word-filters");
const cancelEditButton = document.querySelector("#cancel-word-edit");

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
    messageBox.textContent = "단어를 등록하거나 수정하면 여기에 결과가 표시됩니다.";
    messageBox.className = "message";
}

function resetForm() {
    form.reset();
    editingId = null;
    form.querySelector("button[type='submit']").textContent = "단어 저장";
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
    for (const word of items) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>
                <div class="stack">
                    <strong>${word.japanese}</strong>
                    <span class="muted">${word.reading}</span>
                </div>
            </td>
            <td>${word.meaning}</td>
            <td><span class="pill">${word.partOfSpeech}</span></td>
            <td>
                <div class="stack">
                    <span class="pill">${word.jlptLevel}</span>
                    <span class="pill">${word.studyStatus}</span>
                </div>
            </td>
            <td>${word.exampleSentence}</td>
            <td>
                <div class="button-row">
                    <button type="button" class="secondary" data-edit="${word.id}">수정</button>
                    <button type="button" class="danger" data-delete="${word.id}">삭제</button>
                </div>
            </td>
        `;
        row.querySelector("[data-edit]").addEventListener("click", () => startEdit(word));
        row.querySelector("[data-delete]").addEventListener("click", () => handleDelete(word.id));
        tableBody.appendChild(row);
    }
}

function startEdit(word) {
    editingId = word.id;
    form.japanese.value = word.japanese;
    form.reading.value = word.reading;
    form.meaning.value = word.meaning;
    form.partOfSpeech.value = word.partOfSpeech;
    form.exampleSentence.value = word.exampleSentence;
    form.jlptLevel.value = word.jlptLevel;
    form.studyStatus.value = word.studyStatus;
    form.querySelector("button[type='submit']").textContent = "단어 수정";
    cancelEditButton.hidden = false;
    renderMessage("수정 모드로 전환했습니다.", "success");
}

async function loadWords(page = 0) {
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
        const data = await fetchCollection(`/api/words?${params.toString()}`);
        renderRows(data);
    } catch (error) {
        renderMessage(error.message, "error");
    }
}

async function handleDelete(id) {
    try {
        await deleteResource(`/api/words/${id}`);
        renderMessage("단어를 삭제했습니다.", "success");
        if (editingId === id) {
            resetForm();
        }
        await loadWords(currentPage);
    } catch (error) {
        renderMessage(error.message, "error");
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        japanese: form.japanese.value,
        reading: form.reading.value,
        meaning: form.meaning.value,
        partOfSpeech: form.partOfSpeech.value,
        exampleSentence: form.exampleSentence.value,
        jlptLevel: form.jlptLevel.value,
        studyStatus: form.studyStatus.value
    };

    try {
        if (editingId) {
            await updateResource(`/api/words/${editingId}`, body);
            renderMessage("단어를 수정했습니다.", "success");
        } else {
            await createResource("/api/words", body);
            renderMessage("단어를 등록했습니다.", "success");
        }
        resetForm();
        await loadWords(0);
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
    await loadWords(0);
});

document.querySelector("#word-reset-filters").addEventListener("click", async () => {
    filterForm.reset();
    currentFilters = { jlptLevel: "", studyStatus: "" };
    await loadWords(0);
});

document.querySelector("#word-prev-page").addEventListener("click", async () => {
    if (currentPage > 0) {
        await loadWords(currentPage - 1);
    }
});

document.querySelector("#word-next-page").addEventListener("click", async () => {
    if (currentPage + 1 < totalPages) {
        await loadWords(currentPage + 1);
    }
});

cancelEditButton.addEventListener("click", () => {
    resetForm();
    clearMessage();
});

clearMessage();
loadWords();
