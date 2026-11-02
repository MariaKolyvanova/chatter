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
    notice.textContent = error.message || "Request failed";
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

function bindLogout() {
    const nav = document.querySelector(".nav");
    if (!nav || nav.querySelector("[data-logout]")) return;
    const button = document.createElement("button");
    button.type = "button";
    button.className = "nav-logout";
    button.innerHTML = '<svg viewBox="0 0 24 24"><path d="M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5v-2H5V5h5zm4.5 3.5-1.4 1.4 2.6 2.6H8v2h7.7l-2.6 2.6 1.4 1.4L20 12z"/></svg><span>Log out</span>';
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

async function authorPosts(userId, replies) {
    if (userId === me.id) {
        const page = await api("/api/v1/posts/me?size=50&sort=createdAt,desc");
        return page.content.filter((post) => replies ? post.replyToId : !post.replyToId);
    }
    const kind = replies ? "&replies=true" : "";
    const page = await api("/api/v1/posts/by-author?authorId=" + userId + kind + "&size=50&sort=createdAt,desc");
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
    return category.charAt(0) + category.slice(1).toLowerCase();
}

function colorFor(name) {
    let hash = 0;
    for (const char of name || "?") hash = (hash + char.charCodeAt(0)) % COLORS.length;
    return COLORS[hash];
}

function hasAvatar(user) {
    return user.avatarKey && user.avatarKey !== "avatar/default.png";
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

function when(iso) {
    const date = new Date(iso);
    if (!iso || Number.isNaN(date.getTime())) return "now";
    const minutes = Math.round((Date.now() - date.getTime()) / 60000);
    if (minutes < 1) return "now";
    if (minutes < 60) return minutes + "m";
    const hours = Math.round(minutes / 60);
    if (hours < 24) return hours + "h";
    return Math.round(hours / 24) + "d";
}

function paintFollow(button, authorId) {
    const on = followingIds.has(authorId);
    button.textContent = on ? "Following" : "Follow";
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

function personRow(user) {
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
        empty.textContent = "No one yet.";
        card.append(empty);
    } else {
        for (const user of users) card.append(personRow(user));
    }
    const close = document.createElement("button");
    close.type = "button";
    close.className = "btn secondary";
    close.textContent = "Close";
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
    await showPeople("Liked by", people);
}

async function showReposters(postId) {
    const page = await api("/api/v1/reposts/by-post?postId=" + postId + "&size=50");
    const people = [];
    for (const repost of page.content) people.push(await userById(repost.userId));
    await showPeople("Reposted by", people);
}

function countButton(value, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "count";
    button.textContent = value;
    button.addEventListener("click", () => onClick().catch(showError));
    return button;
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
    time.textContent = when(post.createdAt);
    const who = document.createElement("div");
    who.className = "who";
    who.append(name, handle, time);
    if (post.status === "HIDDEN") {
        const badge = document.createElement("span");
        badge.className = "badge";
        badge.textContent = "Hidden";
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
    if (own) header.append(postMenu(post, article, linked, text));
    const actions = document.createElement("div");
    actions.className = "actions";

    const reply = document.createElement("div");
    reply.className = "action reply";
    const replyLink = document.createElement("a");
    replyLink.className = "hit";
    replyLink.href = "post.html?id=" + post.id;
    replyLink.append(svg(ICONS.reply));
    replyLink.setAttribute("aria-label", "Reply");
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
        toggle.setAttribute("aria-label", "Repost");
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
    likeToggle.setAttribute("aria-label", "Like");
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
        parent.textContent = "Replying to";
        body.append(parent);
        api("/api/v1/posts/" + post.replyToId + "?countView=false")
            .then((target) => userById(target.authorId))
            .then((user) => {
                parent.textContent = "Replying to @" + user.username;
            })
            .catch(() => {});
    }
    body.append(text, actions);
    article.append(body);
    if (linked) {
        article.addEventListener("click", (event) => {
            if (event.target.closest("a, button, textarea")) return;
            location.href = "post.html?id=" + post.id;
        });
    }
    return article;
}

function postMenu(post, article, linked, text) {
    const more = document.createElement("div");
    more.className = "more";
    const toggle = document.createElement("button");
    toggle.type = "button";
    toggle.className = "more-btn";
    toggle.setAttribute("aria-label", "More");
    toggle.append(svg(ICONS.more));
    const menu = document.createElement("div");
    menu.className = "menu";
    menu.hidden = true;
    const edit = document.createElement("button");
    edit.type = "button";
    edit.textContent = "Edit";
    const remove = document.createElement("button");
    remove.type = "button";
    remove.textContent = "Delete";
    edit.addEventListener("click", () => {
        menu.hidden = true;
        editPost(post, text);
    });
    remove.addEventListener("click", () => {
        menu.hidden = true;
        deletePost(post, article, linked).catch(showError);
    });
    toggle.addEventListener("click", (event) => {
        event.stopPropagation();
        const open = !menu.hidden;
        closeMenus();
        menu.hidden = open;
    });
    menu.append(edit, remove);
    more.append(toggle, menu);
    return more;
}

function closeMenus() {
    document.querySelectorAll(".menu").forEach((menu) => {
        menu.hidden = true;
    });
}

function editPost(post, text) {
    if (text.querySelector("textarea")) {
        text.replaceChildren(document.createTextNode(post.content));
        return;
    }
    const field = document.createElement("textarea");
    field.maxLength = 280;
    field.value = post.content;
    const save = document.createElement("button");
    save.type = "button";
    save.className = "btn";
    save.textContent = "Save";
    const cancel = document.createElement("button");
    cancel.type = "button";
    cancel.className = "btn secondary";
    cancel.textContent = "Cancel";
    const bar = document.createElement("div");
    bar.className = "row";
    bar.append(save, cancel);
    text.replaceChildren(field, bar);
    cancel.addEventListener("click", () => text.replaceChildren(document.createTextNode(post.content)));
    save.addEventListener("click", async () => {
        try {
            const updated = await api("/api/v1/posts/" + post.id, {
                method: "PUT",
                body: { content: field.value, category: post.category },
            });
            post.content = updated.content;
            text.replaceChildren(document.createTextNode(updated.content));
        } catch (error) {
            showError(error);
        }
    });
}

async function deletePost(post, article, linked) {
    if (!confirm("Delete this post?")) return;
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

async function bindProfileTabs(userId) {
    const box = document.querySelector("#posts");
    const empty = document.querySelector("#empty");
    const buttons = document.querySelectorAll("[data-profile-tab]");

    async function select(tab) {
        buttons.forEach((button) => button.classList.toggle("active", button.dataset.profileTab === tab));
        let posts = [];
        if (tab === "posts" || tab === "replies") {
            posts = await authorPosts(userId, tab === "replies");
        } else if (tab === "likes") {
            const likes = await api("/api/v1/likes/by-user?userId=" + userId + "&size=50&sort=createdAt,desc");
            posts = await postsByIds(likes.content.map((item) => item.postId));
        } else {
            const reposts = await api("/api/v1/reposts/by-user?userId=" + userId + "&size=50&sort=createdAt,desc");
            posts = await postsByIds(reposts.content.map((item) => item.postId));
        }
        if (empty) empty.hidden = posts.length > 0;
        await showPosts(posts, box);
    }

    document.querySelector("#profile-tabs").addEventListener("click", (event) => {
        const button = event.target.closest("[data-profile-tab]");
        if (!button) return;
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
    title.textContent = "Following";
    aside.append(title);
    for (const id of followingIds) {
        aside.append(personRow(await userById(id)));
    }
}

async function bindFeed() {
    const box = document.querySelector("#posts");
    const empty = document.querySelector("#empty");
    const menu = document.querySelector("#category-menu");
    document.querySelector("#composer-avatar").replaceWith(avatarNode(me));

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
        categoriesTab.textContent = category ? label(filter) : "Categories";
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
    composer.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(composer);
        try {
            await api("/api/v1/posts", {
                method: "POST",
                body: { content: data.get("content"), category: data.get("category") },
            });
            composer.reset();
            await select("all");
        } catch (error) {
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
    replyForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const content = new FormData(replyForm).get("content");
        try {
            const reply = await api("/api/v1/posts", {
                method: "POST",
                body: { content, replyToId: id },
            });
            replyForm.reset();
            likeCounts.set(reply.id, 0);
            replyCounts.set(reply.id, 0);
            repostCounts.set(reply.id, 0);
            replyCounts.set(id, (replyCounts.get(id) || 0) + 1);
            const shown = document.querySelector("#post .action.reply .count");
            if (shown) shown.textContent = replyCounts.get(id);
            document.querySelector("#replies").prepend(renderPost(reply, me, true));
        } catch (error) {
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
    const user = await userById(id);
    const head = document.querySelector("#profile");
    head.append(avatarNode(user, true));
    const info = document.createElement("div");
    const name = document.createElement("strong");
    name.textContent = user.displayName;
    const handle = document.createElement("div");
    handle.className = "muted";
    handle.textContent = "@" + user.username;
    const bio = document.createElement("p");
    bio.textContent = user.bio || "";
    const locationLine = document.createElement("div");
    locationLine.className = "muted";
    locationLine.textContent = user.location || "";
    info.append(name, handle, bio, locationLine);
    if (user.id !== me.id) {
        const row = document.createElement("div");
        row.className = "row";
        const button = document.createElement("button");
        button.type = "button";
        paintFollow(button, user.id);
        button.addEventListener("click", () => toggleFollow(user.id, button).catch(showError));
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
    const name = document.createElement("strong");
    name.textContent = me.displayName;
    const handle = document.createElement("div");
    handle.className = "muted";
    handle.textContent = "@" + me.username;
    const bio = document.createElement("p");
    bio.textContent = me.bio || "";
    const details = document.createElement("div");
    details.className = "muted";
    details.textContent = [me.email, me.location, me.birthDate].filter(Boolean).join(" · ");
    const row = document.createElement("div");
    row.className = "row";
    const edit = document.createElement("button");
    edit.className = "btn secondary";
    edit.type = "button";
    edit.textContent = "Edit profile";
    const password = document.createElement("button");
    password.className = "btn secondary";
    password.type = "button";
    password.textContent = "Change password";
    const avatar = document.createElement("button");
    avatar.className = "btn secondary";
    avatar.type = "button";
    avatar.textContent = "Change avatar";
    const remove = document.createElement("button");
    remove.className = "btn secondary";
    remove.type = "button";
    remove.textContent = "Delete account";
    edit.addEventListener("click", () => {
        const form = document.querySelector("#edit-form");
        form.hidden = !form.hidden;
    });
    password.addEventListener("click", () => {
        const form = document.querySelector("#password-form");
        form.hidden = !form.hidden;
    });
    avatar.addEventListener("click", () => {
        const form = document.querySelector("#avatar-form");
        form.hidden = !form.hidden;
    });
    remove.addEventListener("click", async () => {
        if (!confirm("Delete your account? This cannot be undone.")) return;
        try {
            await api("/api/v1/users/me", { method: "DELETE" });
            localStorage.removeItem("token");
            location.href = "login.html";
        } catch (error) {
            showError(error);
        }
    });
    row.append(edit, password, avatar, remove);
    info.append(name, handle, bio, details, row);
    head.append(info);

    const editForm = document.querySelector("#edit-form");
    watchUsername(editForm.elements.username);
    editForm.elements.username.value = me.username;
    editForm.elements.email.value = me.email;
    editForm.elements.displayName.value = me.displayName;
    editForm.elements.bio.value = me.bio || "";
    editForm.elements.location.value = me.location || "";
    editForm.elements.birthDate.value = me.birthDate || "";
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

    document.querySelector("#avatar-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData();
        form.append("file", event.currentTarget.elements.file.files[0]);
        try {
            await api("/api/v1/users/me/avatar", { method: "PUT", form });
            location.reload();
        } catch (error) {
            showError(error);
        }
    });

    await bindProfileTabs(me.id);
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
        box.replaceChildren(...found.map(personRow));
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
