async function api(path, { method = "GET", body, form } = {}) {
    const headers = {};
    const token = localStorage.getItem("token");
    if (token) headers.Authorization = "Bearer " + token;
    if (body !== undefined) headers["Content-Type"] = "application/json";

    const response = await fetch(API_URL + path, {
        method,
        headers,
        body: form !== undefined ? form : body === undefined ? undefined : JSON.stringify(body),
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
        const error = new Error(data && data.message ? data.message : "Request failed");
        error.status = response.status;
        throw error;
    }
    return data;
}
