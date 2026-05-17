import { createResource, fetchCollection } from "./api.js";

const form = document.querySelector("#login-form");
const signupButton = document.querySelector("#signup-button");
const messageBox = document.querySelector("#login-message");

function setMessage(text, type = "") {
    messageBox.textContent = text;
    messageBox.className = `message ${type}`.trim();
}

function credentials() {
    return {
        username: form.username.value.trim(),
        password: form.password.value
    };
}

async function submitAuth(url) {
    try {
        await createResource(url, credentials());
        window.location.href = "/words.html";
    } catch (error) {
        setMessage(error.message, "error");
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitAuth("/api/auth/login");
});

signupButton.addEventListener("click", async () => {
    await submitAuth("/api/auth/signup");
});

try {
    await fetchCollection("/api/auth/me");
    window.location.href = "/words.html";
} catch {
    setMessage("Enter your account information.");
}
