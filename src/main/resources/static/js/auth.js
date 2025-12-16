(() => {
  const ACCESS_TOKEN_KEY = "accessToken";
  const CURRENT_USER_KEY = "currentUser";

  function setCurrentUser(user) {
    if (user) {
      localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(CURRENT_USER_KEY);
    }
  }

  function getCurrentUser() {
    const raw = localStorage.getItem(CURRENT_USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch (e) {
      localStorage.removeItem(CURRENT_USER_KEY);
      return null;
    }
  }

  async function refreshAccessToken() {
    try {
      const res = await fetch("/auth/refresh", {
        method: "POST",
        credentials: "include",
      });
      if (!res.ok) {
        throw new Error("토큰 갱신 실패");
      }
      const data = await res.json();
      const newAccess = data?.refreshToken;
      if (newAccess) {
        localStorage.setItem(ACCESS_TOKEN_KEY, newAccess);
        return newAccess;
      }
      throw new Error("새 액세스 토큰을 받지 못했습니다.");
    } catch (err) {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
      setCurrentUser(null);
      throw err;
    }
  }

  async function authFetch(url, options = {}) {
    const opts = {
      credentials: "include",
      ...options,
      headers: {
        ...(options.headers || {}),
      },
    };

    const applyToken = (token) => {
      if (token) {
        opts.headers.Authorization = `Bearer ${token}`;
      } else {
        delete opts.headers.Authorization;
      }
    };

    applyToken(localStorage.getItem(ACCESS_TOKEN_KEY));

    let response = await fetch(url, opts);
    if (response.status === 401) {
      try {
        const newToken = await refreshAccessToken();
        applyToken(newToken);
        response = await fetch(url, opts);
      } catch (refreshErr) {
        throw refreshErr;
      }
    }
    return response;
  }

  window.authHelper = {
    authFetch,
    getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
    clearAccessToken: () => localStorage.removeItem(ACCESS_TOKEN_KEY),
    setCurrentUser,
    getCurrentUser,
    clearCurrentUser: () => setCurrentUser(null),
  };
})();
