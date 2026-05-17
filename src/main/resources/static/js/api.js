const DEFAULT_HEADERS = {
    "Content-Type": "application/json"
};

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: DEFAULT_HEADERS,
        ...options
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok || payload?.success === false) {
        const message = payload?.error?.message || "Request failed.";
        throw new Error(message);
    }

    return payload?.data;
}

export async function fetchCollection(url) {
    return request(url, { method: "GET" });
}

export async function createResource(url, body) {
    return request(url, {
        method: "POST",
        body: JSON.stringify(body)
    });
}

export async function updateResource(url, body) {
    return request(url, {
        method: "PUT",
        body: JSON.stringify(body)
    });
}

export async function patchResource(url, body) {
    return request(url, {
        method: "PATCH",
        body: JSON.stringify(body)
    });
}

export async function deleteResource(url) {
    return request(url, { method: "DELETE" });
}
