// static/js/api.js
export function getCurrentUserId() {
  const qs = new URLSearchParams(location.search);
  const v = qs.get("currentUserId");
  return v ? Number(v) : 1; // 없으면 1로 임시
}

export function csrfFetch(url, options = {}) {
  const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
  const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

  const opts = { ...options };
  opts.headers = { ...(options.headers || {}) };

  if (opts.body && !(opts.body instanceof FormData) && !opts.headers["Content-Type"]) {
    opts.headers["Content-Type"] = "application/json";
  }

  if (token && header && opts.method && opts.method !== "GET") {
    opts.headers[header] = token;
  }

  return fetch(url, opts);
}

export function pick(obj, keys) {
  for (const k of keys) if (obj && obj[k] != null) return obj[k];
  return null;
}
