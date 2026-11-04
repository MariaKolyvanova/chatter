const CATEGORIES = [
    "GENERAL", "NEWS", "POLITICS", "TECHNOLOGY", "SCIENCE", "EDUCATION",
    "BUSINESS", "SPORTS", "ENTERTAINMENT", "MUSIC", "GAMING", "ART",
    "FASHION", "FOOD", "TRAVEL", "HEALTH", "NATURE", "ANIMALS", "OTHER",
];
const COLORS = ["#7c6cf0", "#3dbe7a", "#f0b429", "#2f9e6b", "#e07a3d", "#1d9bf0"];

let me = null;
let followingIds = new Set();
let likedIds = new Set();
let repostByPost = new Map();
const likeCounts = new Map();
const users = new Map();

function showError(error) {
    const host = document.querySelector(".feed, .card");
    if (!host) return;
    let notice = host.querySelector(".notice");
    if (!notice) {
        notice = document.createElement("p");
        notice.className = "notice";
        host.prepend(notice);
    }
    notice.textContent = error.message || t("error.request-failed");
}

function requireAuth() {
    if (localStorage.getItem("token")) return true;
    location.href = "login.html";
    return false;
}

async function loadSession() {
    me = await api("/api/v1/users/me");
    users.set(me.id, me);
    const subscriptions = await api("/api/v1/subscriptions/by-subscriber?subscriberId=" + me.id + "&size=100");
    followingIds = new Set(subscriptions.content.map((item) => item.authorId));
    const likes = await api("/api/v1/likes/by-user?userId=" + me.id + "&size=100");
    likedIds = new Set(likes.content.map((item) => item.postId));
    const reposts = await api("/api/v1/reposts/by-user?userId=" + me.id + "&size=100");
    repostByPost = new Map(reposts.content.map((item) => [item.postId, item.id]));
}

function canModerate() {
    return me && (me.role === "MODERATOR" || me.role === "ADMIN");
}

function isAdmin() {
    return me && me.role === "ADMIN";
}

function roleName(role) {
    if (role === "ADMIN") return t("role.ADMIN");
    if (role === "MODERATOR") return t("role.MODERATOR");
    return t("role.USER");
}

function roleMark(role) {
    const mark = document.createElement("span");
    mark.className = "role role-" + (role || "USER");
    mark.textContent = roleName(role);
    return mark;
}

function caret(text) {
    return text + " ▾";
}

function profileTitle(displayName, role) {
    const line = document.createElement("div");
    line.className = "profile-name";
    const name = document.createElement("strong");
    name.textContent = displayName;
    line.append(name);
    if (role) line.append(roleMark(role));
    return line;
}

function bindCancel(form, restore) {
    const button = form.querySelector("[data-cancel]");
    if (!button) return;
    button.addEventListener("click", () => {
        form.reset();
        if (restore) restore();
        form.hidden = true;
    });
}

function togglePanel(selector) {
    const form = document.querySelector(selector);
    if (!form) return;
    const show = form.hidden;
    document.querySelectorAll("main.feed > .form").forEach((item) => {
        item.hidden = true;
    });
    form.hidden = !show;
}

function settingsMenu(items) {
    const wrap = document.createElement("div");
    wrap.className = "more settings";
    const toggle = document.createElement("button");
    toggle.type = "button";
    toggle.className = "more-btn";
    toggle.setAttribute("aria-label", t("action.settings"));
    toggle.innerHTML = '<svg viewBox="0 0 24 24"><path d="M19.14 12.94c.04-.31.06-.63.06-.94s-.02-.63-.06-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.48.48 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96a.48.48 0 0 0-.59.22L2.74 8.87a.47.47 0 0 0 .12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.47.47 0 0 0-.12-.61l-2.03-1.58zM12 15.6A3.6 3.6 0 1 1 15.6 12 3.6 3.6 0 0 1 12 15.6z"/></svg>';
    const menu = document.createElement("div");
    menu.className = "menu";
    menu.hidden = true;
    for (const item of items) {
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = item.label;
        button.addEventListener("click", () => {
            menu.hidden = true;
            item.onClick();
        });
        menu.append(button);
    }
    toggle.addEventListener("click", (event) => {
        event.stopPropagation();
        const open = !menu.hidden;
        closeMenus();
        menu.hidden = open;
    });
    wrap.append(toggle, menu);
    return wrap;
}

function bindModerationNav() {
    if (!canModerate()) return;
    const nav = document.querySelector(".nav");
    if (!nav || nav.querySelector("[data-moderation]")) return;
    const link = document.createElement("a");
    link.href = "moderation.html";
    link.dataset.moderation = "";
    if (document.body.dataset.page === "moderation") link.classList.add("active");
    link.innerHTML = '<svg viewBox="0 0 24 24"><path d="M12 2 4 6v6c0 4.5 3.2 7.6 8 9.5 4.8-1.9 8-5 8-9.5V6z"/></svg><span>' + t("nav.moderation") + '</span>';
    const profile = nav.querySelector('a[href="me.html"]');
    if (profile) profile.after(link);
    else nav.insertBefore(link, nav.querySelector(".lang-switch"));
}

function bindLogout() {
    const nav = document.querySelector(".nav");
    if (!nav || nav.querySelector("[data-logout]")) return;
    const button = document.createElement("button");
    button.type = "button";
    button.className = "nav-logout";
    button.innerHTML = '<svg viewBox="0 0 24 24"><path d="M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5v-2H5V5h5zm4.5 3.5-1.4 1.4 2.6 2.6H8v2h7.7l-2.6 2.6 1.4 1.4L20 12z"/></svg><span>' + t("nav.logout") + '</span>';
    button.addEventListener("click", () => {
        localStorage.removeItem("token");
        location.href = "login.html";
    });
    nav.append(button);
}

async function ensureLikeCount(postId) {
    if (likeCounts.has(postId)) return;
    try {
        const page = await api("/api/v1/likes/by-post?postId=" + postId + "&size=1");
        likeCounts.set(postId, page.totalElements);
    } catch (error) {
        if (error.status !== 404) throw error;
        likeCounts.set(postId, 0);
    }
}

async function postsByIds(ids) {
    const loaded = await Promise.all(ids.map(async (id) => {
        try {
            return await api("/api/v1/posts/" + id + "?countView=false");
        } catch (error) {
            if (error.status === 404) return null;
            throw error;
        }
    }));
    return loaded.filter(Boolean);
}

async function authorPosts(userId, replies, visibility) {
    if (userId === me.id) {
        const page = await api("/api/v1/posts/me?size=50&sort=createdAt,desc");
        return page.content.filter((post) => {
            if (replies ? !post.replyToId : post.replyToId) return false;
            if (replies || !canModerate() || !visibility || visibility === "all") return true;
            if (visibility === "hidden") return post.status === "HIDDEN";
            return post.status === "PUBLISHED";
        });
    }
    let path = "/api/v1/posts/by-author?authorId=" + userId;
    if (replies) path += "&replies=true";
    else if (canModerate() && visibility === "all") path += "&anyStatus=true";
    else if (canModerate() && visibility === "hidden") path += "&status=HIDDEN";
    else if (canModerate() && visibility === "published") path += "&status=PUBLISHED";
    const page = await api(path + "&size=50&sort=createdAt,desc");
    return page.content;
}

async function userById(id) {
    if (users.has(id)) return users.get(id);
    const user = await api("/api/v1/users/" + id);
    users.set(id, user);
    return user;
}

function label(category) {
    if (!category) return "";
    const fallback = category.charAt(0) + category.slice(1).toLowerCase();
    return t("category." + category, null, fallback);
}

function colorFor(name) {
    let hash = 0;
    for (const char of name || "?") hash = (hash + char.charCodeAt(0)) % COLORS.length;
    return COLORS[hash];
}

function hasAvatar(user) {
    return user.avatarKey && user.avatarKey !== "avatar/default.png";
}

function squareAvatar(file) {
    const size = 512;
    return new Promise((resolve, reject) => {
        const image = new Image();
        const url = URL.createObjectURL(file);
        image.onload = () => {
            const side = Math.min(image.naturalWidth, image.naturalHeight);
            const sx = Math.round((image.naturalWidth - side) / 2);
            const sy = Math.round((image.naturalHeight - side) / 2);
            const canvas = document.createElement("canvas");
            canvas.width = size;
            canvas.height = size;
            canvas.getContext("2d").drawImage(image, sx, sy, side, side, 0, 0, size, size);
            URL.revokeObjectURL(url);
            canvas.toBlob((blob) => {
                if (!blob) {
                    reject(new Error(t("error.avatar.crop")));
                    return;
                }
                resolve(new File([blob], "avatar.jpg", { type: "image/jpeg" }));
            }, "image/jpeg", 0.92);
        };
        image.onerror = () => {
            URL.revokeObjectURL(url);
            reject(new Error(t("error.avatar.crop")));
        };
        image.src = url;
    });
}

function avatarNode(user, large) {
    const node = document.createElement("span");
    node.className = large ? "avatar-lg" : "avatar";
    const letter = (user.displayName || user.username || "?").trim().charAt(0).toUpperCase();
    if (!hasAvatar(user)) {
        node.textContent = letter;
        node.style.setProperty("--avatar", colorFor(user.username));
        return node;
    }
    const image = document.createElement("img");
    image.alt = "";
    image.src = API_URL + "/api/v1/users/" + user.id + "/avatar";
    image.addEventListener("error", () => {
        image.remove();
        node.textContent = letter;
        node.style.setProperty("--avatar", colorFor(user.username));
    });
    node.append(image);
    return node;
}

function formatBirthDate(value) {
    if (!value) return "";
    const date = new Date(String(value).length === 10 ? value + "T00:00:00" : value);
    if (Number.isNaN(date.getTime())) return String(value);
    return date.toLocaleDateString(localeTag(), { day: "numeric", month: "long", year: "numeric" });
}

function when(iso, exact) {
    const date = new Date(iso);
    if (!iso || Number.isNaN(date.getTime())) return t("time.now");
    if (exact) {
        return date.toLocaleString(localeTag(), {
            day: "numeric",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    }
    const minutes = Math.round((Date.now() - date.getTime()) / 60000);
    if (minutes < 1) return t("time.now");
    if (minutes < 60) return minutes + "m";
    const hours = Math.round(minutes / 60);
    if (hours < 24) return hours + "h";
    return Math.round(hours / 24) + "d";
}

function paintFollow(button, authorId) {
    const on = followingIds.has(authorId);
    button.textContent = on ? t("action.following") : t("action.follow");
    button.className = on ? "btn secondary" : "btn";
}

async function toggleFollow(authorId, button) {
    if (followingIds.has(authorId)) {
        await api("/api/v1/subscriptions?authorId=" + authorId, { method: "DELETE" });
        followingIds.delete(authorId);
    } else {
        await api("/api/v1/subscriptions?authorId=" + authorId, { method: "POST" });
        followingIds.add(authorId);
    }
    paintFollow(button, authorId);
}

function personRow(user, showRole) {
    const row = document.createElement("div");
    row.className = "person";
    row.append(avatarNode(user));
    const info = document.createElement("a");
    info.href = profileUrl(user.id);
    const name = document.createElement("strong");
    name.textContent = user.displayName;
    if (showRole && isAdmin() && user.role) name.append(roleMark(user.role));
    const handle = document.createElement("span");
    handle.textContent = "@" + user.username;
    info.append(name, handle);
    row.append(info);
    if (me && user.id !== me.id) {
        const button = document.createElement("button");
        button.type = "button";
        paintFollow(button, user.id);
        button.addEventListener("click", () => toggleFollow(user.id, button).catch(showError));
        row.append(button);
    }
    return row;
}

function profileUrl(id) {
    return me && id === me.id ? "me.html" : "profile.html?id=" + id;
}

function watchUsername(input) {
    if (!input) return;
    input.addEventListener("input", () => {
        const next = input.value.toLowerCase();
        if (next === input.value) return;
        const pos = input.selectionStart;
        input.value = next;
        input.setSelectionRange(pos, pos);
    });
}

const ICONS = {
    reply: "M1.751 10c0-4.42 3.584-8 8.005-8h4.366c4.49 0 8.129 3.64 8.129 8.13 0 2.96-1.607 5.68-4.196 7.11l-8.054 4.46v-3.69h-.067c-4.49.1-8.183-3.51-8.183-8.01zm8.005-6c-3.317 0-6.005 2.69-6.005 6 0 3.37 2.77 6.08 6.138 6.01l.351-.01h1.761v2.3l5.087-2.81c1.951-1.08 3.163-3.13 3.163-5.36 0-3.39-2.744-6.13-6.129-6.13H9.756z",
    repost: "M4.5 3.88l4.432 4.14-1.364 1.46L5.5 7.55V16c0 1.1.896 2 2 2H13v2H7.5c-2.209 0-4-1.79-4-4V7.55L1.432 9.48.068 8.02 4.5 3.88zM16.5 6H11V4h5.5c2.209 0 4 1.79 4 4v8.45l2.068-1.93 1.364 1.46-4.432 4.14-4.432-4.14 1.364-1.46 2.068 1.93V8c0-1.1-.896-2-2-2z",
    like: "M16.697 5.5c-1.222-.06-2.679.51-3.89 2.16l-.805 1.09-.806-1.09C9.984 6.01 8.526 5.44 7.304 5.5c-1.243.07-2.349.78-2.91 1.91-.552 1.12-.633 2.78.479 4.82 1.074 1.97 3.257 4.27 7.129 6.61 3.87-2.34 6.052-4.64 7.126-6.61 1.111-2.04 1.03-3.7.477-4.82-.561-1.13-1.666-1.84-2.908-1.91zm4.187 7.69c-1.351 2.48-4.001 5.12-8.379 7.67l-.503.3-.504-.3c-4.379-2.55-7.029-5.19-8.382-7.67-1.36-2.5-1.41-4.86-.514-6.67.887-1.79 2.647-2.91 4.601-3.01 1.651-.09 3.368.56 4.798 2.01 1.429-1.45 3.146-2.1 4.796-2.01 1.954.1 3.714 1.22 4.601 3.01.896 1.81.846 4.17-.514 6.67z",
    liked: "M20.884 13.19c-1.351 2.48-4.001 5.12-8.379 7.67l-.503.3-.504-.3c-4.379-2.55-7.029-5.19-8.382-7.67-1.36-2.5-1.41-4.86-.514-6.67.887-1.79 2.647-2.91 4.601-3.01 1.651-.09 3.368.56 4.798 2.01 1.429-1.45 3.146-2.1 4.796-2.01 1.954.1 3.714 1.22 4.601 3.01.896 1.81.846 4.17-.514 6.67z",
    views: "M8.75 21V3h2v18h-2zM18 21V8.5h2V21h-2zM4 21l.004-10h2L6 21H4zm9.248 0v-7h2v7h-2z",
    more: "M3 12c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2zm9 2c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm7 0c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z",
};

function postPhoto(post) {
    const photo = document.createElement("img");
    photo.className = "post-photo";
    photo.alt = "";
    photo.hidden = true;
    const headers = {};
    const token = localStorage.getItem("token");
    if (token) headers.Authorization = "Bearer " + token;
    fetch(API_URL + "/api/v1/posts/" + post.id + "/image", { headers })
        .then((response) => {
            if (!response.ok) throw new Error();
            return response.blob();
        })
        .then((blob) => {
            photo.src = URL.createObjectURL(blob);
            photo.hidden = false;
        })
        .catch(() => photo.remove());
    return photo;
}

function svg(path) {
    const icon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    icon.setAttribute("viewBox", "0 0 24 24");
    icon.setAttribute("aria-hidden", "true");
    const shape = document.createElementNS("http://www.w3.org/2000/svg", "path");
    shape.setAttribute("d", path);
    icon.append(shape);
    return icon;
}

const replyCounts = new Map();
const repostCounts = new Map();

async function ensureReplyCount(postId) {
    if (replyCounts.has(postId)) return;
    try {
        const page = await api("/api/v1/posts/" + postId + "/replies?size=1");
        replyCounts.set(postId, page.totalElements);
    } catch (error) {
        if (error.status !== 404) throw error;
        replyCounts.set(postId, 0);
    }
}

async function ensureRepostCount(postId) {
    if (repostCounts.has(postId)) return;
    try {
        const page = await api("/api/v1/reposts/by-post?postId=" + postId + "&size=1");
        repostCounts.set(postId, page.totalElements);
    } catch (error) {
        if (error.status !== 404) throw error;
        repostCounts.set(postId, 0);
    }
}

async function showPeople(title, users) {
    let dialog = document.querySelector("#people-dialog");
    if (!dialog) {
        dialog = document.createElement("div");
        dialog.id = "people-dialog";
        dialog.className = "dialog";
        dialog.addEventListener("click", (event) => {
            if (event.target === dialog) dialog.hidden = true;
        });
        document.body.append(dialog);
    }
    const card = document.createElement("div");
    card.className = "dialog-card";
    const heading = document.createElement("h2");
    heading.textContent = title;
    card.append(heading);
    if (users.length === 0) {
        const empty = document.createElement("p");
        empty.className = "muted";
        empty.textContent = t("empty.none");
        card.append(empty);
    } else {
        for (const user of users) card.append(personRow(user));
    }
    const close = document.createElement("button");
    close.type = "button";
    close.className = "btn secondary";
    close.textContent = t("action.close");
    close.addEventListener("click", () => {
        dialog.hidden = true;
    });
    card.append(close);
    dialog.replaceChildren(card);
    dialog.hidden = false;
}

async function showLikers(postId) {
    const page = await api("/api/v1/likes/by-post?postId=" + postId + "&size=50");
    const people = [];
    for (const like of page.content) people.push(await userById(like.userId));
    await showPeople(t("post.liked-by"), people);
}

async function showReposters(postId) {
    const page = await api("/api/v1/reposts/by-post?postId=" + postId + "&size=50");
    const people = [];
    for (const repost of page.content) people.push(await userById(repost.userId));
    await showPeople(t("post.reposted-by"), people);
}

function countButton(value, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "count";
    button.textContent = value;
    button.addEventListener("click", () => onClick().catch(showError));
    return button;
}

function changedMark() {
    const mark = document.createElement("span");
    mark.className = "changed";
    mark.textContent = t("post.changed");
    return mark;
}

function renderPost(post, author, linked) {
    const own = me && post.authorId === me.id;
    const article = document.createElement("article");
    article.className = linked ? "post post-open" : "post";
    article.append(avatarNode(author));
    const body = document.createElement("div");
    const header = document.createElement("header");
    const name = document.createElement("a");
    name.href = profileUrl(author.id);
    const strong = document.createElement("strong");
    strong.textContent = author.displayName;
    name.append(strong);
    const handle = document.createElement("span");
    handle.textContent = "@" + author.username;
    const time = document.createElement("time");
    if (post.createdAt) time.dateTime = post.createdAt;
    time.textContent = when(post.createdAt, document.body.dataset.page === "post");
    const who = document.createElement("div");
    who.className = "who";
    who.append(name, handle, time);
    if (post.changed) who.append(changedMark());
    if (post.status === "HIDDEN") {
        const badge = document.createElement("span");
        badge.className = "badge";
        badge.textContent = t("post.hidden");
        who.append(badge);
    }
    header.append(who);
    const category = document.createElement("div");
    category.className = "post-category";
    category.textContent = label(post.category);
    header.append(category);
    const text = document.createElement("div");
    text.className = "content";
    text.textContent = post.content;
    if (own || canModerate()) header.append(postMenu(post, article, linked, text));
    const actions = document.createElement("div");
    actions.className = "actions";

    const reply = document.createElement("div");
    reply.className = "action reply";
    const replyLink = document.createElement("a");
    replyLink.className = "hit";
    replyLink.href = "post.html?id=" + post.id;
    replyLink.append(svg(ICONS.reply));
    replyLink.setAttribute("aria-label", t("aria.reply"));
    const replyCount = document.createElement("span");
    replyCount.className = "count";
    replyCount.textContent = replyCounts.get(post.id) || 0;
    reply.append(replyLink, replyCount);

    const repost = document.createElement("div");
    repost.className = "action repost" + (repostByPost.has(post.id) ? " active" : "");
    const repostCount = countButton(repostCounts.get(post.id) || 0, () => showReposters(post.id));
    if (!own) {
        const toggle = document.createElement("button");
        toggle.type = "button";
        toggle.className = "hit";
        toggle.append(svg(ICONS.repost));
        toggle.setAttribute("aria-label", t("aria.repost"));
        toggle.addEventListener("click", async () => {
            try {
                if (repostByPost.has(post.id)) {
                    await api("/api/v1/reposts/" + repostByPost.get(post.id), { method: "DELETE" });
                    repostByPost.delete(post.id);
                    repostCounts.set(post.id, Math.max(0, (repostCounts.get(post.id) || 0) - 1));
                } else {
                    const created = await api("/api/v1/reposts?postId=" + post.id, { method: "POST" });
                    repostByPost.set(post.id, created.id);
                    repostCounts.set(post.id, (repostCounts.get(post.id) || 0) + 1);
                }
                repost.classList.toggle("active", repostByPost.has(post.id));
                repostCount.textContent = repostCounts.get(post.id) || 0;
            } catch (error) {
                showError(error);
            }
        });
        repost.append(toggle);
    } else {
        const mark = document.createElement("span");
        mark.className = "hit";
        mark.append(svg(ICONS.repost));
        repost.append(mark);
    }
    repost.append(repostCount);

    const like = document.createElement("div");
    like.className = "action like" + (likedIds.has(post.id) ? " active" : "");
    const likeToggle = document.createElement("button");
    likeToggle.type = "button";
    likeToggle.className = "hit";
    likeToggle.append(svg(likedIds.has(post.id) ? ICONS.liked : ICONS.like));
    likeToggle.setAttribute("aria-label", t("aria.like"));
    const likeCount = countButton(likeCounts.get(post.id) || 0, () => showLikers(post.id));
    likeToggle.addEventListener("click", async () => {
        try {
            if (likedIds.has(post.id)) {
                await api("/api/v1/likes?postId=" + post.id, { method: "DELETE" });
                likedIds.delete(post.id);
                likeCounts.set(post.id, Math.max(0, (likeCounts.get(post.id) || 0) - 1));
            } else {
                await api("/api/v1/likes?postId=" + post.id, { method: "POST" });
                likedIds.add(post.id);
                likeCounts.set(post.id, (likeCounts.get(post.id) || 0) + 1);
            }
            like.classList.toggle("active", likedIds.has(post.id));
            likeToggle.replaceChildren(svg(likedIds.has(post.id) ? ICONS.liked : ICONS.like));
            likeCount.textContent = likeCounts.get(post.id) || 0;
        } catch (error) {
            showError(error);
        }
    });
    like.append(likeToggle, likeCount);

    const views = document.createElement("div");
    views.className = "action views";
    const viewIcon = document.createElement("span");
    viewIcon.className = "hit";
    viewIcon.append(svg(ICONS.views));
    const viewCount = document.createElement("span");
    viewCount.className = "count";
    viewCount.textContent = post.viewCount;
    views.append(viewIcon, viewCount);

    actions.append(reply, repost, like, views);
    body.append(header);
    if (post.replyToId) {
        const parent = document.createElement("a");
        parent.className = "parent-link";
        parent.href = "post.html?id=" + post.replyToId;
        parent.textContent = t("post.replying-to");
        body.append(parent);
        api("/api/v1/posts/" + post.replyToId + "?countView=false")
            .then((target) => userById(target.authorId))
            .then((user) => {
                parent.textContent = t("post.replying-to") + " @" + user.username;
            })
            .catch(() => {});
    }
    body.append(text);
    if (post.imageKey) body.append(postPhoto(post));
    body.append(actions);
    article.append(body);
    if (linked) {
        article.addEventListener("click", (event) => {
            if (event.target.closest("a, button, textarea, input, label, select, .file-field, .row")) return;
            location.href = "post.html?id=" + post.id;
        });
    }
    return article;
}

function postMenu(post, article, linked, text) {
    const own = me && post.authorId === me.id;
    const more = document.createElement("div");
    more.className = "more";
    const toggle = document.createElement("button");
    toggle.type = "button";
    toggle.className = "more-btn";
    toggle.setAttribute("aria-label", t("aria.more"));
    toggle.append(svg(ICONS.more));
    const menu = document.createElement("div");
    menu.className = "menu";
    menu.hidden = true;
    if (own) {
        const edit = document.createElement("button");
        edit.type = "button";
        edit.textContent = t("action.edit");
        const remove = document.createElement("button");
        remove.type = "button";
        remove.textContent = t("action.delete");
        edit.addEventListener("click", () => {
            menu.hidden = true;
            editPost(post, text);
        });
        remove.addEventListener("click", () => {
            menu.hidden = true;
            deletePost(post, article, linked).catch(showError);
        });
        menu.append(edit, remove);
    }
    if (canModerate()) {
        const visibility = document.createElement("button");
        visibility.type = "button";
        visibility.textContent = post.status === "HIDDEN" ? t("action.show") : t("action.hide");
        visibility.addEventListener("click", () => {
            menu.hidden = true;
            setPostVisibility(post, article, visibility).catch(showError);
        });
        menu.append(visibility);
    }
    toggle.addEventListener("click", (event) => {
        event.stopPropagation();
        const open = !menu.hidden;
        closeMenus();
        menu.hidden = open;
    });
    more.append(toggle, menu);
    return more;
}

async function setPostVisibility(post, article, button) {
    const hide = post.status !== "HIDDEN";
    if (!confirm(hide ? t("confirm.hide") : t("confirm.show"))) return;
    await api("/api/v1/posts/" + post.id + (hide ? "/hide" : "/show"), { method: "PUT" });
    post.status = hide ? "HIDDEN" : "PUBLISHED";
    button.textContent = hide ? t("action.show") : t("action.hide");
    const who = article.querySelector(".who");
    let badge = who.querySelector(".badge");
    if (hide) {
        if (!badge) {
            badge = document.createElement("span");
            badge.className = "badge";
            badge.textContent = t("post.hidden");
            who.append(badge);
        }
    } else if (badge) {
        badge.remove();
    }
}

function closeMenus() {
    document.querySelectorAll(".menu").forEach((menu) => {
        menu.hidden = true;
    });
}

function syncPostPhoto(post, text) {
    const body = text.parentElement;
    body.querySelector(".post-photo")?.remove();
    if (!post.imageKey) return;
    body.querySelector(".actions").before(postPhoto(post));
}

function editPost(post, text) {
    const photo = () => text.parentElement.querySelector(".post-photo");
    if (text.querySelector("textarea")) {
        text.replaceChildren(document.createTextNode(post.content));
        if (photo() && post.imageKey) photo().hidden = false;
        return;
    }
    const field = document.createElement("textarea");
    field.maxLength = 280;
    field.value = post.content;
    const file = document.createElement("input");
    file.type = "file";
    file.accept = "image/jpeg,image/png,image/heif";
    const fileLabel = document.createElement("label");
    fileLabel.className = "file-field";
    const fileName = document.createElement("span");
    fileName.textContent = post.imageKey ? t("action.change-photo") : t("action.add-photo");
    fileLabel.append(fileName, file);
    let removeImage = false;
    const remove = document.createElement("button");
    remove.type = "button";
    remove.className = "btn secondary";
    remove.textContent = t("action.remove-photo");
    remove.hidden = !post.imageKey;
    remove.addEventListener("click", () => {
        removeImage = true;
        file.value = "";
        remove.hidden = true;
        if (photo()) photo().hidden = true;
    });
    file.addEventListener("change", () => {
        if (!file.files[0]) return;
        removeImage = false;
        remove.hidden = true;
        if (photo()) photo().hidden = false;
    });
    const save = document.createElement("button");
    save.type = "button";
    save.className = "btn";
    save.textContent = t("action.save");
    const cancel = document.createElement("button");
    cancel.type = "button";
    cancel.className = "btn secondary";
    cancel.textContent = t("action.cancel");
    const bar = document.createElement("div");
    bar.className = "row";
    bar.append(cancel, remove, save);
    text.replaceChildren(field, fileLabel, bar);
    cancel.addEventListener("click", () => {
        text.replaceChildren(document.createTextNode(post.content));
        if (photo() && post.imageKey) photo().hidden = false;
    });
    save.addEventListener("click", async () => {
        try {
            const updated = await api("/api/v1/posts/" + post.id, {
                method: "PUT",
                body: { content: field.value, category: post.category },
            });
            post.content = updated.content;
            if (file.files[0]) {
                const form = new FormData();
                form.append("file", file.files[0]);
                const saved = await api("/api/v1/posts/" + post.id + "/image", { method: "POST", form });
                post.imageKey = saved.imageKey;
            } else if (removeImage) {
                await api("/api/v1/posts/" + post.id + "/image", { method: "DELETE" });
                post.imageKey = null;
            }
            text.replaceChildren(document.createTextNode(post.content));
            syncPostPhoto(post, text);
            const who = text.parentElement.querySelector(".who");
            if (updated.changed && who && !who.querySelector(".changed")) {
                who.querySelector("time").after(changedMark());
            }
        } catch (error) {
            showError(error);
        }
    });
}

async function deletePost(post, article, linked) {
    if (!confirm(t("confirm.delete-post"))) return;
    await api("/api/v1/posts/" + post.id, { method: "DELETE" });
    if (linked) article.remove();
    else location.href = "index.html";
}

async function showPosts(posts, box) {
    await Promise.all(posts.flatMap((post) => [
        ensureLikeCount(post.id),
        ensureReplyCount(post.id),
        ensureRepostCount(post.id),
    ]));
    box.replaceChildren();
    for (const post of posts) {
        box.append(renderPost(post, await userById(post.authorId), true));
    }
}

async function profileCounts(userId) {
    const [following, followers] = await Promise.all([
        api("/api/v1/subscriptions/by-subscriber?subscriberId=" + userId + "&size=1"),
        api("/api/v1/subscriptions/by-author?authorId=" + userId + "&size=1"),
    ]);
    const line = document.createElement("div");
    line.className = "profile-counts";
    for (const [count, label, name] of [
        [following.totalElements, t("stat.following"), "following"],
        [followers.totalElements, t("stat.followers"), "followers"],
    ]) {
        const item = document.createElement("span");
        const strong = document.createElement("strong");
        strong.dataset.count = name;
        strong.textContent = count;
        item.append(strong, document.createTextNode(" " + label));
        line.append(item);
    }
    return line;
}

async function setBlocked(user, block) {
    if (!confirm(block ? t("confirm.block") : t("confirm.unblock"))) return;
    await api("/api/v1/users/" + user.id + (block ? "/block" : "/unblock"), { method: "PUT" });
    location.reload();
}

function blockedRow(user, onUnblock) {
    const row = document.createElement("div");
    row.className = "person";
    row.append(avatarNode(user));
    const info = document.createElement("a");
    info.href = profileUrl(user.id);
    const name = document.createElement("strong");
    name.textContent = user.displayName;
    const handle = document.createElement("span");
    handle.textContent = "@" + user.username;
    info.append(name, handle);
    const button = document.createElement("button");
    button.type = "button";
    button.className = "btn secondary";
    button.textContent = t("action.unblock");
    button.addEventListener("click", () => onUnblock(user, row));
    row.append(info, button);
    return row;
}

async function bindProfileTabs(userId) {
    const box = document.querySelector("#posts");
    const empty = document.querySelector("#empty");
    const buttons = document.querySelectorAll("[data-profile-tab]");
    const postsButton = document.querySelector("[data-profile-tab='posts']");
    let visibility = "all";
    let menu = null;
    if (canModerate() && postsButton && !postsButton.closest(".tab-menu")) {
        const wrap = document.createElement("div");
        wrap.className = "tab-menu";
        postsButton.replaceWith(wrap);
        wrap.append(postsButton);
        menu = document.createElement("div");
        menu.className = "menu";
        menu.id = "posts-menu";
        menu.hidden = true;
        for (const [value, text] of [["all", t("tab.all-posts")], ["published", t("tab.public")], ["hidden", t("tab.hidden")]]) {
            const item = document.createElement("button");
            item.type = "button";
            item.dataset.postFilter = value;
            item.textContent = text;
            menu.append(item);
        }
        wrap.append(menu);
        postsButton.textContent = caret(t("tab.all-posts"));
    }

    async function select(tab) {
        buttons.forEach((button) => button.classList.toggle("active", button.dataset.profileTab === tab));
        if (tab === "followers") {
            const page = await api("/api/v1/subscriptions/by-author?authorId=" + userId + "&size=50&sort=createdAt,desc");
            const list = document.createElement("div");
            list.className = "people-list";
            for (const item of page.content) list.append(personRow(await userById(item.subscriberId), false));
            box.replaceChildren(list);
            if (empty) {
                empty.textContent = t("empty.none");
                empty.hidden = page.content.length > 0;
            }
            return;
        }
        let posts = [];
        if (tab === "posts" || tab === "replies") {
            posts = await authorPosts(userId, tab === "replies", visibility);
        } else if (tab === "likes") {
            const likes = await api("/api/v1/likes/by-user?userId=" + userId + "&size=50&sort=createdAt,desc");
            posts = await postsByIds(likes.content.map((item) => item.postId));
        } else {
            const reposts = await api("/api/v1/reposts/by-user?userId=" + userId + "&size=50&sort=createdAt,desc");
            posts = await postsByIds(reposts.content.map((item) => item.postId));
        }
        if (empty) {
            empty.textContent = t("empty.nothing");
            empty.hidden = posts.length > 0;
        }
        await showPosts(posts, box);
    }

    document.querySelector("#profile-tabs").addEventListener("click", (event) => {
        const filter = event.target.closest("[data-post-filter]");
        if (filter) {
            visibility = filter.dataset.postFilter;
            postsButton.textContent = caret(filter.textContent);
            menu.hidden = true;
            select("posts").catch(showError);
            return;
        }
        const button = event.target.closest("[data-profile-tab]");
        if (!button) return;
        if (button === postsButton && menu) {
            event.stopPropagation();
            const open = !menu.hidden;
            closeMenus();
            menu.hidden = open;
        }
        select(button.dataset.profileTab).catch(showError);
    });
    await select("posts");
}

function bindLogin() {
    const form = document.querySelector("form");
    watchUsername(form.elements.username);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(form);
        try {
            const auth = await api("/api/v1/auth/login", {
                method: "POST",
                body: { username: String(data.get("username") || "").toLowerCase(), password: data.get("password") },
            });
            localStorage.setItem("token", auth.accessToken);
            location.href = "index.html";
        } catch (error) {
            showError(error);
        }
    });
}

function bindRegister() {
    const form = document.querySelector("form");
    watchUsername(form.elements.username);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(form);
        const body = {
            username: String(data.get("username") || "").toLowerCase(),
            email: data.get("email"),
            password: data.get("password"),
            displayName: data.get("displayName"),
            bio: data.get("bio") || null,
            location: data.get("location") || null,
            birthDate: data.get("birthDate"),
        };
        try {
            await api("/api/v1/auth/register", { method: "POST", body });
            const auth = await api("/api/v1/auth/login", {
                method: "POST",
                body: { username: body.username, password: body.password },
            });
            localStorage.setItem("token", auth.accessToken);
            location.href = "index.html";
        } catch (error) {
            showError(error);
        }
    });
}

async function loadFollowing(aside) {
    aside.replaceChildren();
    const title = document.createElement("h2");
    title.textContent = t("side.following");
    aside.append(title);
    for (const id of followingIds) {
        aside.append(personRow(await userById(id), false));
    }
}

async function bindFeed() {
    const box = document.querySelector("#posts");
    const empty = document.querySelector("#empty");
    const menu = document.querySelector("#category-menu");
    document.querySelector("#composer-avatar").replaceWith(avatarNode(me));
    const categorySelect = document.querySelector("#composer select[name=category]");
    if (categorySelect) {
        for (const option of categorySelect.options) {
            const value = CATEGORIES.includes(option.value) ? option.value : option.textContent;
            option.value = value;
            option.textContent = label(value);
        }
    }

    menu.replaceChildren();
    for (const category of CATEGORIES) {
        const button = document.createElement("button");
        button.type = "button";
        button.dataset.filter = category;
        button.textContent = label(category);
        menu.append(button);
    }

    async function select(filter) {
        const category = filter !== "all" && filter !== "following";
        document.querySelectorAll(".tabs > button").forEach((button) => {
            button.classList.toggle("active", button.dataset.filter === filter);
        });
        const categoriesTab = document.querySelector("#categories-tab");
        categoriesTab.classList.toggle("active", category);
        categoriesTab.textContent = caret(category ? label(filter) : t("tab.categories"));
        menu.querySelectorAll("button").forEach((button) => {
            button.classList.toggle("active", button.dataset.filter === filter);
        });
        menu.hidden = true;
        let path = "/api/v1/posts?page=0&size=20&sort=createdAt,desc";
        if (filter === "following") path = "/api/v1/posts/feed?page=0&size=20&sort=createdAt,desc";
        if (category) path = "/api/v1/posts?category=" + filter + "&page=0&size=20&sort=createdAt,desc";
        const page = await api(path);
        empty.hidden = page.content.length > 0;
        await showPosts(page.content, box);
    }

    document.querySelector("#categories-tab").addEventListener("click", (event) => {
        event.stopPropagation();
        const open = !menu.hidden;
        closeMenus();
        menu.hidden = open;
    });

    document.querySelector(".feed").addEventListener("click", (event) => {
        const button = event.target.closest("[data-filter]");
        if (!button) return;
        select(button.dataset.filter).catch(showError);
    });

    const composer = document.querySelector("#composer");
    const photoInput = document.querySelector("#composer-file");
    const photoPreview = document.querySelector("#composer-preview");
    const photoImage = photoPreview.querySelector("img");
    let previewUrl = null;

    function clearComposerPhoto() {
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
        photoInput.value = "";
        photoImage.removeAttribute("src");
        photoPreview.hidden = true;
    }

    document.querySelector("#composer-photo").addEventListener("click", () => photoInput.click());
    photoInput.addEventListener("change", () => {
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        const file = photoInput.files[0];
        if (!file) {
            previewUrl = null;
            photoPreview.hidden = true;
            return;
        }
        previewUrl = URL.createObjectURL(file);
        photoImage.src = previewUrl;
        photoPreview.hidden = false;
    });
    document.querySelector("#composer-clear").addEventListener("click", clearComposerPhoto);
    composer.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(composer);
        const file = photoInput.files[0];
        let created = null;
        try {
            created = await api("/api/v1/posts", {
                method: "POST",
                body: { content: data.get("content"), category: data.get("category") },
            });
            if (file) {
                const form = new FormData();
                form.append("file", file);
                await api("/api/v1/posts/" + created.id + "/image", { method: "POST", form });
            }
            composer.reset();
            clearComposerPhoto();
            if (categorySelect) {
                for (const option of categorySelect.options) option.textContent = label(option.value);
            }
            await select("all");
        } catch (error) {
            if (created) {
                composer.reset();
                clearComposerPhoto();
                await select("all");
            }
            showError(error);
        }
    });

    await select("all");
    await loadFollowing(document.querySelector("#following"));
}

async function bindPost() {
    const id = new URLSearchParams(location.search).get("id");
    const post = await api("/api/v1/posts/" + id);
    const author = await userById(post.authorId);
    await Promise.all([ensureLikeCount(post.id), ensureReplyCount(post.id), ensureRepostCount(post.id)]);
    document.querySelector("#post").append(renderPost(post, author, false));
    const thread = await api("/api/v1/posts/" + id + "/replies?size=50&sort=createdAt,asc");
    await showPosts(thread.content, document.querySelector("#replies"));
    document.querySelector("#reply-avatar").replaceWith(avatarNode(me));
    const replyForm = document.querySelector("#reply");
    const photoInput = document.querySelector("#reply-file");
    const photoPreview = document.querySelector("#reply-preview");
    const photoImage = photoPreview.querySelector("img");
    let previewUrl = null;

    function clearReplyPhoto() {
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
        photoInput.value = "";
        photoImage.removeAttribute("src");
        photoPreview.hidden = true;
    }

    function showReply(reply) {
        likeCounts.set(reply.id, 0);
        replyCounts.set(reply.id, 0);
        repostCounts.set(reply.id, 0);
        replyCounts.set(id, (replyCounts.get(id) || 0) + 1);
        const shown = document.querySelector("#post .action.reply .count");
        if (shown) shown.textContent = replyCounts.get(id);
        document.querySelector("#replies").prepend(renderPost(reply, me, true));
    }

    document.querySelector("#reply-photo").addEventListener("click", () => photoInput.click());
    photoInput.addEventListener("change", () => {
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        const file = photoInput.files[0];
        if (!file) {
            previewUrl = null;
            photoPreview.hidden = true;
            return;
        }
        previewUrl = URL.createObjectURL(file);
        photoImage.src = previewUrl;
        photoPreview.hidden = false;
    });
    document.querySelector("#reply-clear").addEventListener("click", clearReplyPhoto);
    replyForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const content = new FormData(replyForm).get("content");
        const file = photoInput.files[0];
        let reply = null;
        try {
            reply = await api("/api/v1/posts", {
                method: "POST",
                body: { content, replyToId: id },
            });
            if (file) {
                const form = new FormData();
                form.append("file", file);
                const saved = await api("/api/v1/posts/" + reply.id + "/image", { method: "POST", form });
                reply.imageKey = saved.imageKey;
            }
            replyForm.reset();
            clearReplyPhoto();
            showReply(reply);
        } catch (error) {
            if (reply) {
                replyForm.reset();
                clearReplyPhoto();
                showReply(reply);
            }
            showError(error);
        }
    });
    await loadFollowing(document.querySelector("#following"));
}

async function bindProfile() {
    const id = new URLSearchParams(location.search).get("id");
    if (!id || id === me.id) {
        location.replace("me.html");
        return;
    }
    let user = await userById(id);
    if (canModerate()) {
        user = await api("/api/v1/users/" + id);
        users.set(id, user);
    }
    const head = document.querySelector("#profile");
    head.append(avatarNode(user, true));
    const info = document.createElement("div");
    info.className = "profile-info";
    const handle = document.createElement("div");
    handle.className = "muted";
    handle.textContent = "@" + user.username;
    const bio = document.createElement("p");
    bio.textContent = user.bio || "";
    const facts = document.createElement("div");
    facts.className = "muted";
    facts.textContent = [user.location, formatBirthDate(user.birthDate)].filter(Boolean).join(" · ");
    info.append(profileTitle(user.displayName, user.role), handle, bio, facts, await profileCounts(user.id));
    const menuItems = [];
    if (isAdmin() && user.role) {
        menuItems.push({ label: t("menu.change-role"), onClick: () => togglePanel("#role-form") });
        bindRoleForm(user);
    }
    if (canModerate()) {
        const blocked = user.status === "BLOCKED";
        menuItems.push({
            label: blocked ? t("action.unblock") : t("action.block"),
            onClick: () => setBlocked(user, !blocked).catch(showError),
        });
    }
    if (menuItems.length) head.append(settingsMenu(menuItems));
    if (user.id !== me.id) {
        const row = document.createElement("div");
        row.className = "row";
        const button = document.createElement("button");
        button.type = "button";
        paintFollow(button, user.id);
        button.addEventListener("click", async () => {
            const before = followingIds.has(user.id);
            try {
                await toggleFollow(user.id, button);
                if (before === followingIds.has(user.id)) return;
                const shown = info.querySelector("[data-count='followers']");
                if (!shown) return;
                const next = Number(shown.textContent) + (followingIds.has(user.id) ? 1 : -1);
                shown.textContent = String(Math.max(0, next));
            } catch (error) {
                showError(error);
            }
        });
        row.append(button);
        info.append(row);
    }
    head.append(info);
    await bindProfileTabs(user.id);
}

async function bindMe() {
    const head = document.querySelector("#profile");
    head.append(avatarNode(me, true));
    const info = document.createElement("div");
    info.className = "profile-info";
    const handle = document.createElement("div");
    handle.className = "muted";
    handle.textContent = "@" + me.username;
    const bio = document.createElement("p");
    bio.textContent = me.bio || "";
    const details = document.createElement("div");
    details.className = "muted";
    details.textContent = [me.email, me.location, formatBirthDate(me.birthDate)].filter(Boolean).join(" · ");
    info.append(profileTitle(me.displayName, me.role), handle, bio, details, await profileCounts(me.id));
    head.append(info, settingsMenu([
        { label: t("menu.edit-profile"), onClick: () => togglePanel("#edit-form") },
        { label: t("menu.change-password"), onClick: () => togglePanel("#password-form") },
        { label: t("menu.change-avatar"), onClick: () => togglePanel("#avatar-form") },
        { label: t("menu.delete-account"), onClick: () => deleteAccount() },
    ]));

    const editForm = document.querySelector("#edit-form");
    const fillEditForm = () => {
        editForm.elements.username.value = me.username;
        editForm.elements.email.value = me.email;
        editForm.elements.displayName.value = me.displayName;
        editForm.elements.bio.value = me.bio || "";
        editForm.elements.location.value = me.location || "";
        editForm.elements.birthDate.value = me.birthDate || "";
    };
    watchUsername(editForm.elements.username);
    fillEditForm();
    bindCancel(editForm, fillEditForm);
    editForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(editForm);
        try {
            me = await api("/api/v1/users/me", {
                method: "PUT",
                body: {
                    username: String(data.get("username") || "").toLowerCase(),
                    email: data.get("email"),
                    displayName: data.get("displayName"),
                    bio: data.get("bio") || null,
                    location: data.get("location") || null,
                    birthDate: data.get("birthDate"),
                },
            });
            location.reload();
        } catch (error) {
            showError(error);
        }
    });

    const passwordForm = document.querySelector("#password-form");
    bindCancel(passwordForm);
    passwordForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(passwordForm);
        try {
            await api("/api/v1/users/me/password", {
                method: "PUT",
                body: { currentPassword: data.get("currentPassword"), newPassword: data.get("newPassword") },
            });
            passwordForm.reset();
            passwordForm.hidden = true;
        } catch (error) {
            showError(error);
        }
    });

    const avatarForm = document.querySelector("#avatar-form");
    const avatarInput = avatarForm.elements.file;
    const avatarPreview = document.createElement("img");
    avatarPreview.className = "avatar-preview";
    avatarPreview.alt = "";
    avatarPreview.hidden = true;
    avatarForm.insertBefore(avatarPreview, avatarForm.querySelector(".form-actions"));
    let avatarFile = null;
    let previewUrl = null;

    function clearAvatarCrop() {
        avatarFile = null;
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
        avatarPreview.removeAttribute("src");
        avatarPreview.hidden = true;
    }

    avatarInput.addEventListener("change", async () => {
        clearAvatarCrop();
        const file = avatarInput.files[0];
        if (!file) return;
        try {
            avatarFile = await squareAvatar(file);
            previewUrl = URL.createObjectURL(avatarFile);
            avatarPreview.src = previewUrl;
            avatarPreview.hidden = false;
        } catch (error) {
            avatarInput.value = "";
            showError(error);
        }
    });
    bindCancel(avatarForm);
    avatarForm.querySelector("[data-cancel]").addEventListener("click", clearAvatarCrop);
    const deleteAvatar = document.querySelector("#delete-avatar");
    deleteAvatar.hidden = !hasAvatar(me);
    deleteAvatar.addEventListener("click", async () => {
        if (!confirm(t("confirm.delete-avatar"))) return;
        try {
            await api("/api/v1/users/me/avatar", { method: "DELETE" });
            location.reload();
        } catch (error) {
            showError(error);
        }
    });
    avatarForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const file = avatarFile || await squareAvatar(avatarInput.files[0]);
            const form = new FormData();
            form.append("file", file);
            await api("/api/v1/users/me/avatar", { method: "PUT", form });
            location.reload();
        } catch (error) {
            showError(error);
        }
    });

    await bindProfileTabs(me.id);
}

function staffList(title, people) {
    const section = document.createElement("section");
    const heading = document.createElement("h2");
    heading.className = "section-title";
    heading.textContent = title;
    const list = document.createElement("div");
    list.className = "people-list";
    if (people.length === 0) {
        const none = document.createElement("p");
        none.className = "empty";
        none.textContent = t("empty.none");
        list.append(none);
    } else {
        list.append(...people.map((user) => personRow(user, true)));
    }
    section.append(heading, list);
    return section;
}

function deleteAccount() {
    if (!confirm(t("confirm.delete-account"))) return;
    api("/api/v1/users/me", { method: "DELETE" })
        .then(() => {
            localStorage.removeItem("token");
            location.href = "login.html";
        })
        .catch(showError);
}

function ensureRoleForm() {
    const existing = document.querySelector("#role-form");
    if (existing) return existing;
    const form = document.createElement("form");
    form.id = "role-form";
    form.className = "form";
    form.hidden = true;
    form.innerHTML = '<label><span class="field-name">' + t("field.role") + '</span><select name="role" required>'
        + '<option value="USER">' + t("role.USER") + '</option><option value="MODERATOR">' + t("role.MODERATOR") + '</option><option value="ADMIN">' + t("role.ADMIN") + '</option>'
        + '</select></label><p class="muted">' + t("role.hint") + '</p>'
        + '<div class="row form-actions"><button class="btn secondary" type="button" data-cancel>' + t("action.cancel") + '</button>'
        + '<button class="btn" type="submit">' + t("action.save") + '</button></div>';
    document.querySelector("#profile-tabs").before(form);
    return form;
}

function bindRoleForm(user) {
    const form = ensureRoleForm();
    const apply = () => {
        form.elements.role.value = user.role;
    };
    apply();
    bindCancel(form, apply);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const role = form.elements.role.value;
        if (role === user.role) {
            form.hidden = true;
            return;
        }
        if (!confirm(t("confirm.role", { role: roleName(role) }))) return;
        try {
            await api("/api/v1/users/" + user.id + "/role?role=" + role, { method: "PUT" });
            user.role = role;
            const mark = document.querySelector(".profile-name .role");
            if (mark) {
                mark.className = "role role-" + user.role;
                mark.textContent = roleName(user.role);
            }
            form.hidden = true;
        } catch (error) {
            showError(error);
        }
    });
}

async function bindModeration() {
    const box = document.querySelector("#posts");
    const staff = document.querySelector("#staff");
    const empty = document.querySelector("#empty");
    const tabs = document.querySelector("#moderation-tabs");
    const postsButton = document.querySelector("#moderation-posts");
    const menu = document.querySelector("#moderation-posts-menu");
    let visibility = "all";
    if (isAdmin()) {
        const button = document.createElement("button");
        button.type = "button";
        button.dataset.moderation = "staff";
        button.textContent = t("tab.roles");
        tabs.append(button);
    }

    function paintPostsButton() {
        const labels = { all: t("tab.all-posts"), published: t("tab.public"), hidden: t("tab.hidden") };
        postsButton.textContent = caret(labels[visibility]);
    }

    async function select(tab) {
        tabs.querySelectorAll("[data-moderation]").forEach((button) => {
            button.classList.toggle("active", button.dataset.moderation === tab);
        });
        const people = tab === "staff" || tab === "blocked";
        box.hidden = people;
        staff.hidden = !people;
        if (tab === "blocked") {
            const page = await api("/api/v1/users?status=BLOCKED&size=100&sort=username,asc");
            const list = document.createElement("div");
            list.className = "people-list";
            for (const user of page.content) {
                list.append(blockedRow(user, async (person, row) => {
                    if (!confirm(t("confirm.unblock"))) return;
                    try {
                        await api("/api/v1/users/" + person.id + "/unblock", { method: "PUT" });
                        row.remove();
                        empty.hidden = list.querySelector(".person") != null;
                    } catch (error) {
                        showError(error);
                    }
                }));
            }
            staff.replaceChildren(list);
            empty.textContent = t("empty.none");
            empty.hidden = page.content.length > 0;
            return;
        }
        if (people) {
            const moderators = await api("/api/v1/users?role=MODERATOR&size=100&sort=username,asc");
            const admins = await api("/api/v1/users?role=ADMIN&size=100&sort=username,asc");
            staff.replaceChildren(
                staffList(t("side.moderators"), moderators.content),
                staffList(t("side.admins"), admins.content)
            );
            empty.hidden = true;
            return;
        }
        let path = "/api/v1/posts/moderation?page=0&size=20&sort=createdAt,desc";
        if (visibility === "published") path += "&status=PUBLISHED";
        if (visibility === "hidden") path += "&status=HIDDEN";
        const page = await api(path);
        empty.textContent = t("empty.nothing");
        empty.hidden = page.content.length > 0;
        await showPosts(page.content, box);
    }

    tabs.addEventListener("click", (event) => {
        const filter = event.target.closest("[data-post-filter]");
        if (filter) {
            visibility = filter.dataset.postFilter;
            paintPostsButton();
            menu.hidden = true;
            select("posts").catch(showError);
            return;
        }
        const button = event.target.closest("[data-moderation]");
        if (!button) return;
        if (button === postsButton) {
            event.stopPropagation();
            const open = !menu.hidden;
            closeMenus();
            menu.hidden = open;
        }
        select(button.dataset.moderation).catch(showError);
    });
    paintPostsButton();
    await select("posts");
    await loadFollowing(document.querySelector(".side"));
}

async function bindPeople() {
    const box = document.querySelector("#people-list");
    const empty = document.querySelector("#people-empty");
    const directory = (await api("/api/v1/users?size=50&sort=username,asc")).content;

    async function paint(query) {
        const text = query.trim().toLowerCase();
        let found = directory.filter((user) => !text
            || user.displayName.toLowerCase().includes(text)
            || user.username.toLowerCase().includes(text));
        if (found.length === 0 && text) {
            try {
                found = [await api("/api/v1/users/by-username?username=" + encodeURIComponent(text))];
            } catch (error) {
                if (error.status !== 404) throw error;
            }
        }
        box.replaceChildren(...found.map((user) => personRow(user, true)));
        empty.hidden = found.length > 0;
    }

    document.querySelector("[data-people-query]").addEventListener("input", (event) => {
        paint(event.target.value).catch(showError);
    });
    await paint("");
}

async function start() {
    document.addEventListener("click", (event) => {
        if (event.target.closest(".more, .tab-menu")) return;
        closeMenus();
    });
    const page = document.body.dataset.page;
    if (page === "login") return bindLogin();
    if (page === "register") return bindRegister();
    if (!requireAuth()) return;
    bindLogout();
    try {
        await loadSession();
        bindModerationNav();
        if (page === "moderation") {
            if (!canModerate()) {
                location.replace("index.html");
                return;
            }
            await bindModeration();
            return;
        }
        if (page === "feed") await bindFeed();
        if (page === "post") await bindPost();
        if (page === "profile") await bindProfile();
        if (page === "me") await bindMe();
        if (page === "people") await bindPeople();
    } catch (error) {
        if (error.status === 401) {
            localStorage.removeItem("token");
            location.href = "login.html";
            return;
        }
        showError(error);
    }
}

start();
