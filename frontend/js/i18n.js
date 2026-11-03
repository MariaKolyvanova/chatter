const DICTIONARY = {
    en: {
        "nav.home": "Home",
        "nav.people": "People",
        "nav.profile": "Profile",
        "nav.moderation": "Moderation",
        "nav.logout": "Log out",
        "title.home": "Chatter",
        "title.login": "Log in · Chatter",
        "title.register": "Register · Chatter",
        "title.profile": "Profile · Chatter",
        "title.people": "People · Chatter",
        "title.post": "Post · Chatter",
        "title.moderation": "Moderation · Chatter",
        "login.title": "Log in",
        "login.username": "Username",
        "login.password": "Password",
        "login.submit": "Log in",
        "login.no-account": "No account yet?",
        "login.register": "Register",
        "register.title": "Create your account",
        "register.submit": "Register",
        "register.have-account": "Already registered?",
        "register.login": "Log in",
        "field.username": "Username",
        "field.email": "Email",
        "field.password": "Password",
        "field.display-name": "Display name",
        "field.bio": "Bio",
        "field.location": "Location",
        "field.birth-date": "Birth date",
        "field.current-password": "Current password",
        "field.new-password": "New password",
        "field.avatar": "Avatar",
        "field.role": "Role",
        "field.category": "Category",
        "tip.username": "3–50 characters. Latin letters, digits and underscore. Saved in lowercase.",
        "tip.email": "A valid email, up to 255 characters.",
        "tip.password": "8–100 characters.",
        "tip.display-name": "3–100 characters.",
        "tip.bio": "Optional.",
        "tip.location": "Optional. Up to 100 characters.",
        "tip.birth-date": "A date in the past.",
        "tip.avatar": "JPEG, PNG or HEIF. Maximum 5 MB.",
        "action.cancel": "Cancel",
        "action.save": "Save",
        "action.change-password": "Change password",
        "action.save-avatar": "Save avatar",
        "action.post": "Post",
        "action.reply": "Reply",
        "action.follow": "Follow",
        "action.following": "Following",
        "action.edit": "Edit",
        "action.delete": "Delete",
        "action.hide": "Hide",
        "action.show": "Show",
        "action.close": "Close",
        "action.settings": "Settings",
        "tab.all": "All",
        "tab.following": "Following",
        "tab.categories": "Categories",
        "tab.posts": "Posts",
        "tab.replies": "Replies",
        "tab.likes": "Likes",
        "tab.reposts": "Reposts",
        "tab.all-posts": "All posts",
        "tab.public": "Public",
        "tab.hidden": "Hidden",
        "tab.roles": "Roles",
        "empty.nothing": "Nothing here yet.",
        "empty.none": "No one yet.",
        "empty.search": "No one found.",
        "composer.placeholder": "What's new",
        "reply.placeholder": "Post your reply",
        "search.placeholder": "Search by name or username",
        "search.aria": "Search people",
        "post.replying-to": "Replying to",
        "post.hidden": "Hidden",
        "post.liked-by": "Liked by",
        "post.reposted-by": "Reposted by",
        "aria.reply": "Reply",
        "aria.repost": "Repost",
        "aria.like": "Like",
        "aria.more": "More",
        "menu.edit-profile": "Edit profile",
        "menu.change-password": "Change password",
        "menu.change-avatar": "Change avatar",
        "menu.delete-account": "Delete account",
        "menu.change-role": "Change role",
        "confirm.delete-post": "Delete this post?",
        "confirm.hide": "Hide this post?",
        "confirm.show": "Show this post?",
        "confirm.delete-account": "Delete your account? This cannot be undone.",
        "confirm.role": "Change this role to {role}? It takes effect on their next login.",
        "role.hint": "The new role takes effect on their next login.",
        "role.USER": "User",
        "role.MODERATOR": "Moderator",
        "role.ADMIN": "Admin",
        "side.following": "Following",
        "side.moderators": "Moderators",
        "side.admins": "Admins",
        "time.now": "now",
        "category.GENERAL": "General",
        "category.NEWS": "News",
        "category.POLITICS": "Politics",
        "category.TECHNOLOGY": "Technology",
        "category.SCIENCE": "Science",
        "category.EDUCATION": "Education",
        "category.BUSINESS": "Business",
        "category.SPORTS": "Sports",
        "category.ENTERTAINMENT": "Entertainment",
        "category.MUSIC": "Music",
        "category.GAMING": "Gaming",
        "category.ART": "Art",
        "category.FASHION": "Fashion",
        "category.FOOD": "Food",
        "category.TRAVEL": "Travel",
        "category.HEALTH": "Health",
        "category.NATURE": "Nature",
        "category.ANIMALS": "Animals",
        "category.OTHER": "Other",
        "error.request-failed": "Request failed",
        "error.unexpected": "An unexpected error occurred",
        "error.validation": "Request validation failed",
        "error.invalid-value": "Invalid value",
        "error.access-denied": "Access is denied",
        "error.not-authenticated": "Authentication is required",
        "error.not-author": "You cannot change this post",
        "error.auth.invalid-credentials": "Invalid credentials",
        "error.user.not-found": "User not found",
        "error.user.email-taken": "Email is already registered",
        "error.user.username-taken": "Username is already taken",
        "error.user.role-self": "You cannot change your own role",
        "error.password.current": "Current password is incorrect",
        "error.post.not-found": "Post not found",
        "error.like.not-found": "Like not found",
        "error.like.already-exists": "User has already liked this post",
        "error.repost.not-found": "Repost not found",
        "error.repost.already-exists": "User has already reposted this post",
        "error.subscription.not-found": "Subscription not found",
        "error.subscription.already-exists": "Subscription already exists",
        "error.subscription.self": "A user cannot subscribe to themselves",
        "error.image.not-found": "Image not found",
        "error.file.not-found": "File not found",
        "error.file.empty": "File is empty",
        "error.file.too-large": "File is too large",
        "error.file.unsupported": "Unsupported file type: {0}",
        "error.file.save": "Failed to save file",
        "error.file.load": "Failed to load file",
        "error.file.path": "File path is not allowed",
        "user.username.required": "Username is required",
        "user.username.size": "Username must be between {0} and {1} characters",
        "user.username.invalid": "Username may contain only latin letters, digits and underscore",
        "user.email.required": "Email is required",
        "user.email.invalid": "Email must be a valid address",
        "user.email.size": "Email must be at most {0} characters",
        "user.password.required": "Password is required",
        "user.password.size": "Password must be between {0} and {1} characters",
        "user.display-name.required": "Display name is required",
        "user.display-name.size": "Display name must be between {0} and {1} characters",
        "user.location.size": "Location must be at most {0} characters",
        "user.birth-date.past": "Birth date must be in the past",
        "user.password.current-required": "Current password is required",
        "user.password.new-required": "New password is required",
        "user.password.new-size": "New password must be between {0} and {1} characters",
        "post.content.required": "Post text is required",
        "post.content.size": "Post text must be at most {0} characters",
        "post.category.required": "Category is required",
        "auth.username.required": "Username is required",
        "auth.password.required": "Password is required"
    },
    ru: {
        "nav.home": "Главная",
        "nav.people": "Люди",
        "nav.profile": "Профиль",
        "nav.moderation": "Модерация",
        "nav.logout": "Выйти",
        "title.home": "Chatter",
        "title.login": "Вход · Chatter",
        "title.register": "Регистрация · Chatter",
        "title.profile": "Профиль · Chatter",
        "title.people": "Люди · Chatter",
        "title.post": "Пост · Chatter",
        "title.moderation": "Модерация · Chatter",
        "login.title": "Вход",
        "login.username": "Имя пользователя",
        "login.password": "Пароль",
        "login.submit": "Войти",
        "login.no-account": "Нет аккаунта?",
        "login.register": "Регистрация",
        "register.title": "Создайте аккаунт",
        "register.submit": "Зарегистрироваться",
        "register.have-account": "Уже есть аккаунт?",
        "register.login": "Войти",
        "field.username": "Имя пользователя",
        "field.email": "Почта",
        "field.password": "Пароль",
        "field.display-name": "Отображаемое имя",
        "field.bio": "О себе",
        "field.location": "Место",
        "field.birth-date": "Дата рождения",
        "field.current-password": "Текущий пароль",
        "field.new-password": "Новый пароль",
        "field.avatar": "Аватар",
        "field.role": "Роль",
        "field.category": "Категория",
        "tip.username": "3–50 символов. Латиница, цифры и подчёркивание. Сохраняется в нижнем регистре.",
        "tip.email": "Корректная почта, до 255 символов.",
        "tip.password": "8–100 символов.",
        "tip.display-name": "3–100 символов.",
        "tip.bio": "Необязательно.",
        "tip.location": "Необязательно. До 100 символов.",
        "tip.birth-date": "Дата в прошлом.",
        "tip.avatar": "JPEG, PNG или HEIF. Максимум 5 МБ.",
        "action.cancel": "Отмена",
        "action.save": "Сохранить",
        "action.change-password": "Сменить пароль",
        "action.save-avatar": "Сохранить аватар",
        "action.post": "Опубликовать",
        "action.reply": "Ответить",
        "action.follow": "Подписаться",
        "action.following": "Вы подписаны",
        "action.edit": "Изменить",
        "action.delete": "Удалить",
        "action.hide": "Скрыть",
        "action.show": "Показать",
        "action.close": "Закрыть",
        "action.settings": "Настройки",
        "tab.all": "Все",
        "tab.following": "Подписки",
        "tab.categories": "Категории",
        "tab.posts": "Посты",
        "tab.replies": "Ответы",
        "tab.likes": "Лайки",
        "tab.reposts": "Репосты",
        "tab.all-posts": "Все посты",
        "tab.public": "Публичные",
        "tab.hidden": "Скрытые",
        "tab.roles": "Роли",
        "empty.nothing": "Пока ничего нет.",
        "empty.none": "Пока никого нет.",
        "empty.search": "Никого не нашлось.",
        "composer.placeholder": "Что нового",
        "reply.placeholder": "Напишите ответ",
        "search.placeholder": "Поиск по имени или имени пользователя",
        "search.aria": "Поиск людей",
        "post.replying-to": "Ответ",
        "post.hidden": "Скрыт",
        "post.liked-by": "Лайкнули",
        "post.reposted-by": "Репостнули",
        "aria.reply": "Ответить",
        "aria.repost": "Репост",
        "aria.like": "Лайк",
        "aria.more": "Ещё",
        "menu.edit-profile": "Изменить профиль",
        "menu.change-password": "Сменить пароль",
        "menu.change-avatar": "Сменить аватар",
        "menu.delete-account": "Удалить аккаунт",
        "menu.change-role": "Изменить роль",
        "confirm.delete-post": "Удалить этот пост?",
        "confirm.hide": "Скрыть этот пост?",
        "confirm.show": "Показать этот пост?",
        "confirm.delete-account": "Удалить аккаунт? Это нельзя отменить.",
        "confirm.role": "Сменить роль на {role}? Она применится при следующем входе.",
        "role.hint": "Новая роль применится при следующем входе.",
        "role.USER": "Пользователь",
        "role.MODERATOR": "Модератор",
        "role.ADMIN": "Админ",
        "side.following": "Подписки",
        "side.moderators": "Модераторы",
        "side.admins": "Админы",
        "time.now": "сейчас",
        "category.GENERAL": "Общее",
        "category.NEWS": "Новости",
        "category.POLITICS": "Политика",
        "category.TECHNOLOGY": "Технологии",
        "category.SCIENCE": "Наука",
        "category.EDUCATION": "Образование",
        "category.BUSINESS": "Бизнес",
        "category.SPORTS": "Спорт",
        "category.ENTERTAINMENT": "Развлечения",
        "category.MUSIC": "Музыка",
        "category.GAMING": "Игры",
        "category.ART": "Искусство",
        "category.FASHION": "Мода",
        "category.FOOD": "Еда",
        "category.TRAVEL": "Путешествия",
        "category.HEALTH": "Здоровье",
        "category.NATURE": "Природа",
        "category.ANIMALS": "Животные",
        "category.OTHER": "Другое",
        "error.request-failed": "Запрос не выполнен",
        "error.unexpected": "Произошла непредвиденная ошибка",
        "error.validation": "Запрос не прошёл проверку",
        "error.invalid-value": "Некорректное значение",
        "error.access-denied": "Доступ запрещён",
        "error.not-authenticated": "Нужна аутентификация",
        "error.not-author": "Нельзя изменить этот пост",
        "error.auth.invalid-credentials": "Неверные учётные данные",
        "error.user.not-found": "Пользователь не найден",
        "error.user.email-taken": "Эта почта уже зарегистрирована",
        "error.user.username-taken": "Это имя пользователя уже занято",
        "error.user.role-self": "Нельзя изменить собственную роль",
        "error.password.current": "Текущий пароль неверный",
        "error.post.not-found": "Пост не найден",
        "error.like.not-found": "Лайк не найден",
        "error.like.already-exists": "Пост уже лайкнут",
        "error.repost.not-found": "Репост не найден",
        "error.repost.already-exists": "Пост уже репостнут",
        "error.subscription.not-found": "Подписка не найдена",
        "error.subscription.already-exists": "Подписка уже существует",
        "error.subscription.self": "Нельзя подписаться на себя",
        "error.image.not-found": "Изображение не найдено",
        "error.file.not-found": "Файл не найден",
        "error.file.empty": "Файл пустой",
        "error.file.too-large": "Файл слишком большой",
        "error.file.unsupported": "Неподдерживаемый тип файла: {0}",
        "error.file.save": "Не удалось сохранить файл",
        "error.file.load": "Не удалось прочитать файл",
        "error.file.path": "Путь к файлу недопустим",
        "user.username.required": "Имя пользователя обязательно",
        "user.username.size": "Имя пользователя должно быть от {0} до {1} символов",
        "user.username.invalid": "Имя пользователя может содержать только латиницу, цифры и подчёркивание",
        "user.email.required": "Почта обязательна",
        "user.email.invalid": "Почта должна быть корректным адресом",
        "user.email.size": "Почта должна быть не длиннее {0} символов",
        "user.password.required": "Пароль обязателен",
        "user.password.size": "Пароль должен быть от {0} до {1} символов",
        "user.display-name.required": "Отображаемое имя обязательно",
        "user.display-name.size": "Отображаемое имя должно быть от {0} до {1} символов",
        "user.location.size": "Место должно быть не длиннее {0} символов",
        "user.birth-date.past": "Дата рождения должна быть в прошлом",
        "user.password.current-required": "Текущий пароль обязателен",
        "user.password.new-required": "Новый пароль обязателен",
        "user.password.new-size": "Новый пароль должен быть от {0} до {1} символов",
        "post.content.required": "Текст поста обязателен",
        "post.content.size": "Текст поста должен быть не длиннее {0} символов",
        "post.category.required": "Категория обязательна",
        "auth.username.required": "Имя пользователя обязательно",
        "auth.password.required": "Пароль обязателен"
    }
};

function language() {
    return localStorage.getItem("language") === "ru" ? "ru" : "en";
}

function localeTag() {
    return language() === "ru" ? "ru" : "en";
}

function t(key, args, fallback) {
    const text = (DICTIONARY[language()] && DICTIONARY[language()][key])
        || (DICTIONARY.en && DICTIONARY.en[key])
        || fallback
        || key;
    if (args == null) return text;
    if (Array.isArray(args)) {
        return args.reduce((result, value, index) => result.replaceAll("{" + index + "}", value), text);
    }
    return Object.entries(args).reduce(
        (result, [name, value]) => result.replaceAll("{" + name + "}", value),
        text
    );
}

function translateError(data) {
    if (!data) return t("error.request-failed");
    const fields = data.fieldErrors ? Object.values(data.fieldErrors) : [];
    if (fields.length) {
        return fields.map((field) => t(field.code, field.args, field.message)).join("\n");
    }
    return t(data.code, data.args, data.message || t("error.request-failed"));
}

function applyI18n(root) {
    const scope = root || document;
    document.documentElement.lang = localeTag();
    scope.querySelectorAll("[data-i18n]").forEach((node) => {
        node.textContent = t(node.dataset.i18n);
    });
    scope.querySelectorAll("[data-i18n-placeholder]").forEach((node) => {
        node.placeholder = t(node.dataset.i18nPlaceholder);
    });
    scope.querySelectorAll("[data-i18n-aria]").forEach((node) => {
        node.setAttribute("aria-label", t(node.dataset.i18nAria));
    });
    const title = document.querySelector("title[data-i18n]");
    if (title) document.title = t(title.dataset.i18n);
}

function mountLanguageSwitch() {
    if (document.querySelector("[data-language]")) return;
    const current = language();
    const wrap = document.createElement("div");
    wrap.className = "lang-switch";
    wrap.dataset.language = "";
    for (const code of ["ru", "en"]) {
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = code.toUpperCase();
        if (code === current) button.className = "active";
        button.addEventListener("click", () => {
            if (code === current) return;
            localStorage.setItem("language", code);
            location.reload();
        });
        wrap.append(button);
    }
    const nav = document.querySelector(".nav");
    if (nav) {
        nav.append(wrap);
        return;
    }
    const card = document.querySelector(".card");
    if (card) card.insertBefore(wrap, card.firstChild);
}

applyI18n();
mountLanguageSwitch();
