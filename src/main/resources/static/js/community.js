import { createResource, deleteResource, fetchCollection, updateResource } from "./api.js";

const postForm = document.querySelector("#community-post-form");
const postMessage = document.querySelector("#community-post-message");
const postList = document.querySelector("#community-post-list");
const postEmpty = document.querySelector("#community-post-empty");
const postPageLabel = document.querySelector("#community-post-page");
const prevPostsButton = document.querySelector("#community-prev-posts");
const nextPostsButton = document.querySelector("#community-next-posts");
const cancelPostEditButton = document.querySelector("#cancel-post-edit");

const selectedTitle = document.querySelector("#community-selected-title");
const selectedMeta = document.querySelector("#community-selected-meta");
const selectedContent = document.querySelector("#community-selected-content");
const editPostButton = document.querySelector("#community-edit-post");
const deletePostButton = document.querySelector("#community-delete-post");

const commentForm = document.querySelector("#community-comment-form");
const commentMessage = document.querySelector("#community-comment-message");
const commentList = document.querySelector("#community-comment-list");
const commentEmpty = document.querySelector("#community-comment-empty");
const commentPageLabel = document.querySelector("#community-comment-page");
const prevCommentsButton = document.querySelector("#community-prev-comments");
const nextCommentsButton = document.querySelector("#community-next-comments");
const commentSubmitButton = document.querySelector("#community-comment-submit");
const cancelCommentEditButton = document.querySelector("#cancel-comment-edit");
const cancelReplyTargetButton = document.querySelector("#cancel-reply-target");
const replyTarget = document.querySelector("#community-reply-target");
const currentUser = document.querySelector("#current-user");
const authLink = document.querySelector("#auth-link");

let selectedPostId = null;
let editingPostId = null;
let editingCommentId = null;
let postPage = 0;
let postTotalPages = 1;
let commentPage = 0;
let commentTotalPages = 1;
let signedIn = false;

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function setMessage(target, text, type = "") {
    target.textContent = text;
    target.className = `message ${type}`.trim();
}

function resetPostMessage() {
    setMessage(postMessage, signedIn ? "Post results will appear here." : "Log in to write or edit posts.", signedIn ? "" : "error");
}

function resetCommentMessage() {
    setMessage(commentMessage, signedIn ? "Comment results will appear here." : "Log in to comment or reply.", signedIn ? "" : "error");
}

function formatDate(dateTime) {
    if (!dateTime) {
        return "";
    }
    return new Date(dateTime).toLocaleString();
}

function setPostFormEnabled(enabled) {
    postForm.querySelectorAll("input, textarea, button").forEach((control) => {
        control.disabled = !enabled;
    });
}

function resetPostForm() {
    postForm.reset();
    editingPostId = null;
    postForm.querySelector("button[type='submit']").textContent = "Save post";
    cancelPostEditButton.hidden = true;
}

function resetCommentForm() {
    commentForm.reset();
    commentForm.parentId.value = "";
    editingCommentId = null;
    replyTarget.textContent = "Reply target: none";
    commentSubmitButton.textContent = "Save comment";
    cancelCommentEditButton.hidden = true;
    cancelReplyTargetButton.hidden = true;
}

function renderSelectedPost(post) {
    if (!post) {
        selectedTitle.textContent = "Select a post";
        selectedMeta.innerHTML = "";
        selectedContent.textContent = "Once you pick a post, its content and comments will appear here.";
        editPostButton.disabled = true;
        deletePostButton.disabled = true;
        editPostButton.hidden = true;
        deletePostButton.hidden = true;
        commentSubmitButton.disabled = true;
        prevCommentsButton.disabled = true;
        nextCommentsButton.disabled = true;
        commentPageLabel.textContent = "1 / 1";
        commentList.innerHTML = "";
        commentEmpty.hidden = false;
        return;
    }

    selectedTitle.textContent = post.title;
    selectedMeta.innerHTML = `
        <span>Author: ${escapeHtml(post.authorName)}</span>
        <span>Created: ${escapeHtml(formatDate(post.createdAt))}</span>
        <span>Updated: ${escapeHtml(formatDate(post.updatedAt))}</span>
    `;
    selectedContent.textContent = post.content;
    editPostButton.hidden = !post.ownedByCurrentUser;
    deletePostButton.hidden = !post.ownedByCurrentUser;
    editPostButton.disabled = !post.ownedByCurrentUser;
    deletePostButton.disabled = !post.ownedByCurrentUser;
    commentSubmitButton.disabled = !signedIn;
}

function startPostEdit(post) {
    editingPostId = post.id;
    postForm.authorName.value = post.authorName;
    postForm.title.value = post.title;
    postForm.content.value = post.content;
    postForm.querySelector("button[type='submit']").textContent = "Update post";
    cancelPostEditButton.hidden = false;
    setMessage(postMessage, "Post edit mode is active.", "success");
}

function startCommentEdit(comment) {
    editingCommentId = comment.id;
    commentForm.authorName.value = comment.authorName;
    commentForm.content.value = comment.content;
    commentForm.parentId.value = comment.parentId ?? "";
    replyTarget.textContent = comment.parentId ? `Reply target: comment #${comment.parentId}` : "Reply target: none";
    commentSubmitButton.textContent = "Update comment";
    cancelCommentEditButton.hidden = false;
    cancelReplyTargetButton.hidden = !comment.parentId;
    setMessage(commentMessage, "Comment edit mode is active.", "success");
}

function setReplyTarget(commentId) {
    commentForm.parentId.value = String(commentId);
    replyTarget.textContent = `Reply target: comment #${commentId}`;
    cancelReplyTargetButton.hidden = false;
}

async function loadPosts(page = 0, preferredPostId = selectedPostId) {
    postPage = page;
    const data = await fetchCollection(`/api/community/posts?page=${page}&size=6&sort=createdAt,desc`);
    postTotalPages = data.totalPages || 1;
    postPageLabel.textContent = `${(data.number ?? 0) + 1} / ${postTotalPages}`;
    postList.innerHTML = "";

    if (!data.content || data.content.length === 0) {
        postEmpty.hidden = false;
        selectedPostId = null;
        renderSelectedPost(null);
        return;
    }

    postEmpty.hidden = true;

    data.content.forEach((post) => {
        const item = document.createElement("article");
        item.className = `list-item${post.id === preferredPostId ? " active" : ""}`;
        item.innerHTML = `
            <div class="stack">
                <strong>${escapeHtml(post.title)}</strong>
                <div class="meta">
                    <span>${escapeHtml(post.authorName)}</span>
                    <span>${escapeHtml(formatDate(post.createdAt))}</span>
                    ${post.ownedByCurrentUser ? "<span>Mine</span>" : ""}
                </div>
                <span class="muted">${escapeHtml(post.content.slice(0, 120))}</span>
            </div>
        `;
        item.addEventListener("click", async () => {
            selectedPostId = post.id;
            await loadSelectedPost(post.id);
            await loadPosts(postPage, post.id);
        });
        postList.appendChild(item);
    });

    const nextSelectedId = data.content.some((post) => post.id === preferredPostId)
            ? preferredPostId
            : data.content[0].id;

    if (nextSelectedId !== null) {
        selectedPostId = nextSelectedId;
        await loadSelectedPost(nextSelectedId);
    }
}

async function loadSelectedPost(postId) {
    const post = await fetchCollection(`/api/community/posts/${postId}`);
    renderSelectedPost(post);
    await loadComments(0);
}

function renderComments(pageData) {
    commentList.innerHTML = "";
    commentTotalPages = pageData.totalPages || 1;
    commentPageLabel.textContent = `${(pageData.number ?? 0) + 1} / ${commentTotalPages}`;
    prevCommentsButton.disabled = commentPage <= 0 || !selectedPostId;
    nextCommentsButton.disabled = commentPage + 1 >= commentTotalPages || !selectedPostId;

    const comments = pageData.content || [];
    if (comments.length === 0) {
        commentEmpty.hidden = false;
        return;
    }

    commentEmpty.hidden = true;
    const byParent = new Map();

    comments.forEach((comment) => {
        const key = comment.parentId ?? "root";
        if (!byParent.has(key)) {
            byParent.set(key, []);
        }
        byParent.get(key).push(comment);
    });

    const renderThread = (items, isReply = false) => {
        items.forEach((comment) => {
            const card = document.createElement("article");
            card.className = `comment-card${isReply ? " reply" : ""}`;
            card.innerHTML = `
                <div class="stack">
                    <div class="meta">
                        <span>${escapeHtml(comment.authorName)}</span>
                        <span>${escapeHtml(formatDate(comment.createdAt))}</span>
                        <span>Updated: ${escapeHtml(formatDate(comment.updatedAt))}</span>
                        ${comment.ownedByCurrentUser ? "<span>Mine</span>" : ""}
                    </div>
                    <div>${escapeHtml(comment.content)}</div>
                    <div class="button-row">
                        <button type="button" class="secondary" data-reply="${comment.id}" ${signedIn ? "" : "disabled"}>Reply</button>
                        <button type="button" class="secondary" data-edit="${comment.id}" ${comment.ownedByCurrentUser ? "" : "hidden disabled"}>Edit</button>
                        <button type="button" class="danger" data-delete="${comment.id}" ${comment.ownedByCurrentUser ? "" : "hidden disabled"}>Delete</button>
                    </div>
                </div>
            `;

            card.querySelector("[data-reply]").addEventListener("click", () => {
                setReplyTarget(comment.id);
                setMessage(commentMessage, "Reply target selected.", "success");
            });
            card.querySelector("[data-edit]").addEventListener("click", () => startCommentEdit(comment));
            card.querySelector("[data-delete]").addEventListener("click", async () => {
                await deleteComment(comment.id);
            });
            commentList.appendChild(card);

            const replies = byParent.get(comment.id) || [];
            if (replies.length > 0) {
                renderThread(replies, true);
            }
        });
    };

    renderThread(byParent.get("root") || []);
}

async function loadComments(page = 0) {
    if (!selectedPostId) {
        renderSelectedPost(null);
        return;
    }
    commentPage = page;
    const data = await fetchCollection(`/api/community/posts/${selectedPostId}/comments?page=${page}&size=20&sort=createdAt,asc`);
    renderComments(data);
}

async function deleteComment(commentId) {
    try {
        await deleteResource(`/api/community/comments/${commentId}`);
        setMessage(commentMessage, "Comment deleted.", "success");
        resetCommentForm();
        await loadComments(commentPage);
    } catch (error) {
        setMessage(commentMessage, error.message, "error");
    }
}

postForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        authorName: postForm.authorName.value,
        title: postForm.title.value,
        content: postForm.content.value
    };

    try {
        const result = editingPostId
                ? await updateResource(`/api/community/posts/${editingPostId}`, body)
                : await createResource("/api/community/posts", body);
        setMessage(postMessage, editingPostId ? "Post updated." : "Post created.", "success");
        resetPostForm();
        selectedPostId = result.id;
        await loadPosts(0, result.id);
    } catch (error) {
        setMessage(postMessage, error.message, "error");
    }
});

commentForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!selectedPostId) {
        setMessage(commentMessage, "Select a post first.", "error");
        return;
    }

    const body = {
        parentId: commentForm.parentId.value ? Number(commentForm.parentId.value) : null,
        authorName: commentForm.authorName.value,
        content: commentForm.content.value
    };

    try {
        if (editingCommentId) {
            await updateResource(`/api/community/comments/${editingCommentId}`, body);
            setMessage(commentMessage, "Comment updated.", "success");
        } else {
            await createResource(`/api/community/posts/${selectedPostId}/comments`, body);
            setMessage(commentMessage, "Comment saved.", "success");
        }
        resetCommentForm();
        await loadComments(0);
    } catch (error) {
        setMessage(commentMessage, error.message, "error");
    }
});

cancelPostEditButton.addEventListener("click", () => {
    resetPostForm();
    resetPostMessage();
});

cancelCommentEditButton.addEventListener("click", () => {
    resetCommentForm();
    resetCommentMessage();
});

cancelReplyTargetButton.addEventListener("click", () => {
    commentForm.parentId.value = "";
    replyTarget.textContent = "Reply target: none";
    cancelReplyTargetButton.hidden = true;
});

prevPostsButton.addEventListener("click", async () => {
    if (postPage > 0) {
        await loadPosts(postPage - 1);
    }
});

nextPostsButton.addEventListener("click", async () => {
    if (postPage + 1 < postTotalPages) {
        await loadPosts(postPage + 1);
    }
});

prevCommentsButton.addEventListener("click", async () => {
    if (commentPage > 0) {
        await loadComments(commentPage - 1);
    }
});

nextCommentsButton.addEventListener("click", async () => {
    if (commentPage + 1 < commentTotalPages) {
        await loadComments(commentPage + 1);
    }
});

editPostButton.addEventListener("click", async () => {
    if (!selectedPostId) {
        return;
    }
    try {
        const post = await fetchCollection(`/api/community/posts/${selectedPostId}`);
        startPostEdit(post);
    } catch (error) {
        setMessage(postMessage, error.message, "error");
    }
});

deletePostButton.addEventListener("click", async () => {
    if (!selectedPostId) {
        return;
    }
    try {
        await deleteResource(`/api/community/posts/${selectedPostId}`);
        setMessage(postMessage, "Post deleted.", "success");
        selectedPostId = null;
        resetPostForm();
        resetCommentForm();
        await loadPosts(Math.max(postPage - 1, 0));
    } catch (error) {
        setMessage(postMessage, error.message, "error");
    }
});

async function ensureSession() {
    try {
        const user = await fetchCollection("/api/auth/me");
        signedIn = true;
        currentUser.textContent = user.username;
        authLink.textContent = "Logout";
        authLink.href = "#";
        authLink.addEventListener("click", async (event) => {
            event.preventDefault();
            await createResource("/api/auth/logout", {});
            window.location.href = "/login.html";
        });
    } catch {
        signedIn = false;
        currentUser.textContent = "guest";
        authLink.textContent = "Login";
        authLink.href = "/login.html";
        setPostFormEnabled(false);
    }
    resetPostMessage();
    resetCommentMessage();
}

await ensureSession();
loadPosts().catch((error) => {
    setMessage(postMessage, error.message, "error");
});
