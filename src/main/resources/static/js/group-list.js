(() => {
  const resultBox = document.getElementById("groupListResult");
  const allGroupsBody = document.getElementById("allGroupsBody");
  const myGroupsBody = document.getElementById("myGroupsBody");
  const searchForm = document.getElementById("groupSearchForm");
  const keywordInput = document.getElementById("groupKeyword");
  const btnReloadAll = document.getElementById("btnReloadAll");
  const btnReloadMine = document.getElementById("btnReloadMine");

  let currentUser = authHelper?.getCurrentUser?.();
  let myGroupIdSet = new Set();

  function showResult(type, message) {
    if (!resultBox) return;
    resultBox.classList.remove("d-none", "alert-success", "alert-danger", "alert-info");
    const klass =
      type === "success"
        ? "alert-success"
        : type === "info"
        ? "alert-info"
        : "alert-danger";
    resultBox.classList.add(klass);
    resultBox.textContent = message;
  }

  function clearResult() {
    resultBox?.classList.add("d-none");
  }

  async function loadAllGroups(keyword = "") {
    try {
      allGroupsBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">불러오는 중...</td></tr>`;
      const params = new URLSearchParams({ page: 0, size: 50 });
      if (keyword) params.append("keyword", keyword);
      const res = await authHelper.authFetch(`/groups?${params}`);
      if (!res.ok) throw new Error(await extractError(res));
      const page = await res.json();
      const content = Array.isArray(page.content) ? page.content : [];
      if (content.length === 0) {
        allGroupsBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">검색 결과가 없습니다.</td></tr>`;
        return;
      }
      allGroupsBody.innerHTML = content.map(g => `
        <tr data-group-id="${g.id}">
          <td>${g.id}</td>
          <td>${escapeHtml(g.name)}</td>
          <td>${g.ownerName ? `${escapeHtml(g.ownerName)} (#${g.ownerId})` : `#${g.ownerId}`}</td>
          <td>${g.description ? escapeHtml(g.description) : ""}</td>
          <td>
            <div class="btn-group btn-group-sm">
              <a class="btn btn-outline-secondary" href="/groups/page/${g.id}">상세</a>
              ${renderJoinButton(g.id)}
            </div>
          </td>
        </tr>
      `).join("");
    } catch (err) {
      allGroupsBody.innerHTML = `<tr><td colspan="5" class="text-danger">${err.message || "그룹 목록을 불러오지 못했습니다."}</td></tr>`;
    }
  }

  function renderJoinButton(groupId) {
    if (!currentUser) return "";
    if (myGroupIdSet.has(groupId)) {
      return `<button type="button" class="btn btn-outline-success" disabled>가입됨</button>`;
    }
    return `<button type="button" class="btn btn-primary" data-action="join-group" data-group-id="${groupId}">가입</button>`;
  }

  async function loadMyGroups() {
    try {
      myGroupsBody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">불러오는 중...</td></tr>`;
      if (!currentUser?.id) {
        myGroupsBody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">로그인 사용자를 확인할 수 없습니다.</td></tr>`;
        return;
      }
      const params = new URLSearchParams({ currentUserId: currentUser.id, page: 0, size: 50 });
      const res = await authHelper.authFetch(`/groups/users?${params}`);
      if (!res.ok) throw new Error(await extractError(res));
      const page = await res.json();
      const content = Array.isArray(page.content) ? page.content : [];
      myGroupIdSet = new Set(content.map(g => g.id));
      if (content.length === 0) {
        myGroupsBody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">가입된 그룹이 없습니다.</td></tr>`;
        return;
      }
      myGroupsBody.innerHTML = content.map(g => `
        <tr>
          <td>${g.id}</td>
          <td>${escapeHtml(g.name)}</td>
          <td>${g.myRole ?? "-"}</td>
          <td>
            <div class="btn-group btn-group-sm">
              <a class="btn btn-outline-secondary" href="/groups/page/${g.id}">상세</a>
            </div>
          </td>
        </tr>
      `).join("");
      // 갱신된 가입 정보에 맞춰 전체 리스트도 다시 그릴 수 있도록 재로드
      loadAllGroups(keywordInput.value.trim());
    } catch (err) {
      myGroupsBody.innerHTML = `<tr><td colspan="4" class="text-danger">${err.message || "내 그룹 목록을 불러오지 못했습니다."}</td></tr>`;
    }
  }

  async function joinGroup(groupId) {
    if (!currentUser?.id) {
      showResult("error", "로그인 사용자를 확인할 수 없습니다.");
      return;
    }
    try {
      const res = await authHelper.authFetch(`/groups/${groupId}/members?userId=${currentUser.id}`, {
        method: "POST",
      });
      if (!res.ok) throw new Error(await extractError(res));
      showResult("success", "그룹에 가입되었습니다.");
      await loadMyGroups();
    } catch (err) {
      showResult("error", err.message || "그룹 가입에 실패했습니다.");
    }
  }

  async function extractError(response) {
    try {
      const data = await response.json();
      if (data?.message) return data.message;
      return JSON.stringify(data);
    } catch (e) {
      const text = await response.text();
      return text || "요청 처리 중 오류가 발생했습니다.";
    }
  }

  function escapeHtml(text) {
    if (text == null) return "";
    return text.replace(/[&<>"']/g, (c) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#39;",
    }[c]));
  }

  allGroupsBody?.addEventListener("click", (event) => {
    const btn = event.target.closest("[data-action='join-group']");
    if (!btn) return;
    const groupId = Number(btn.dataset.groupId);
    if (!groupId) return;
    joinGroup(groupId);
  });

  searchForm?.addEventListener("submit", (event) => {
    event.preventDefault();
    clearResult();
    const keyword = keywordInput.value.trim();
    loadAllGroups(keyword);
  });

  btnReloadAll?.addEventListener("click", () => {
    clearResult();
    loadAllGroups(keywordInput.value.trim());
  });

  btnReloadMine?.addEventListener("click", () => {
    clearResult();
    loadMyGroups();
  });

  document.addEventListener("DOMContentLoaded", () => {
    currentUser = authHelper?.getCurrentUser?.();
    loadMyGroups();
  });
})();
