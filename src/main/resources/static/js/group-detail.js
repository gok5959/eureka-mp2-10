(() => {
  const segments = window.location.pathname.split("/").filter(Boolean);
  const groupId = segments.length >= 3 ? segments[2] : null;
  const resultBox = document.getElementById("groupDetailResult");
  const btnReloadDetail = document.getElementById("btnReloadDetail");
  const btnReloadMembers = document.getElementById("btnReloadMembers");
  const currentUserInput = document.getElementById("detailCurrentUserId");

  const nameEl = document.getElementById("detailName");
  const descEl = document.getElementById("detailDescription");
  const ownerEl = document.getElementById("detailOwner");
  const roleEl = document.getElementById("detailMyRole");
  const memberCountEl = document.getElementById("detailMemberCount");
  const memberListBody = document.getElementById("memberListBody");
  const ownerActions = document.getElementById("ownerActions");
  const addMemberForm = document.getElementById("addMemberForm");
  const addMemberInput = document.getElementById("addMemberUserId");
  const addMemberResult = document.getElementById("addMemberResult");
  const addMemberEmailForm = document.getElementById("addMemberEmailForm");
  const addMemberEmailInput = document.getElementById("addMemberEmail");
  const addMemberEmailResult = document.getElementById("addMemberEmailResult");

  let isOwner = false;

  function showResult(type, message) {
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

  function showAddMemberResult(type, message) {
    addMemberResult.classList.remove("d-none", "alert-success", "alert-danger");
    addMemberResult.classList.add(type === "success" ? "alert-success" : "alert-danger");
    addMemberResult.textContent = message;
  }

  async function extractErrorMessage(response) {
    try {
      const data = await response.json();
      if (data?.message) return data.message;
      return JSON.stringify(data);
    } catch (e) {
      const text = await response.text();
      return text || "요청 처리 중 오류가 발생했습니다.";
    }
  }

  function fillCurrentUser() {
    const user = authHelper?.getCurrentUser?.();
    if (user?.id) {
      currentUserInput.value = user.id;
    }
  }

  function toggleOwnerUI(flag) {
    if (!ownerActions) return;
    ownerActions.classList.toggle("d-none", !flag);
  }

  async function loadGroupDetail() {
    if (!groupId) {
      showResult("error", "그룹 ID를 확인할 수 없습니다.");
      return;
    }
    const currentUserId = currentUserInput.value;
    if (!currentUserId) {
      showResult("error", "현재 사용자 ID를 입력해 주세요.");
      return;
    }

    try {
      const res = await authHelper.authFetch(`/groups/${groupId}?currentUserId=${encodeURIComponent(currentUserId)}`);
      if (!res.ok) {
        throw new Error(await extractErrorMessage(res));
      }

      const detail = await res.json();
      nameEl.textContent = detail.name ?? "(이름 없음)";
      descEl.textContent = detail.description ?? "설명 없음";
      ownerEl.textContent = detail.ownerName
        ? `${detail.ownerName} (#${detail.ownerId})`
        : `#${detail.ownerId}`;
      roleEl.textContent = detail.myRole ?? "-";
      memberCountEl.textContent = detail.memberCount ?? 0;
      isOwner = detail.myRole === "OWNER";
      toggleOwnerUI(isOwner);
      showResult("success", "그룹 정보를 불러왔습니다.");
    } catch (err) {
      showResult("error", err.message || "그룹 정보를 불러오지 못했습니다.");
    }
  }

  async function loadMembers() {
    if (!groupId) {
      showResult("error", "그룹 ID를 확인할 수 없습니다.");
      return;
    }
    memberListBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">불러오는 중...</td></tr>`;
    try {
      const res = await authHelper.authFetch(`/groups/${groupId}/members?page=0&size=50`);
      if (!res.ok) {
        throw new Error(await extractErrorMessage(res));
      }
      const page = await res.json();
      const members = Array.isArray(page.content) ? page.content : [];
      if (members.length === 0) {
        memberListBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">멤버가 없습니다.</td></tr>`;
        return;
      }
      const currentUserId = Number(currentUserInput.value);
      memberListBody.innerHTML = members.map(m => {
        const removeBtn = (isOwner && m.role !== "OWNER")
          ? `<button type="button" class="btn btn-sm btn-outline-danger" data-action="remove-member" data-user-id="${m.userId}">제거</button>`
          : "";
        return `
          <tr>
            <td>${m.userId ?? "-"}</td>
            <td>${escapeHtml(m.userName ?? "")}</td>
            <td>${escapeHtml(m.email ?? "")}</td>
            <td>${m.role ?? "-"}</td>
            <td>${removeBtn}</td>
          </tr>
        `;
      }).join("");
    } catch (err) {
      memberListBody.innerHTML = `<tr><td colspan="5" class="text-danger">${err.message || "멤버 목록을 불러오지 못했습니다."}</td></tr>`;
    }
  }

  async function addMember(event) {
    event.preventDefault();
    if (!isOwner) {
      showAddMemberResult("error", "멤버 추가 권한이 없습니다.");
      return;
    }
    const targetUserId = addMemberInput.value.trim();
    if (!targetUserId) {
      showAddMemberResult("error", "사용자 ID를 입력해 주세요.");
      return;
    }
    try {
      const res = await authHelper.authFetch(`/groups/${groupId}/members?userId=${encodeURIComponent(targetUserId)}`, {
        method: "POST",
      });
      if (!res.ok) {
        throw new Error(await extractErrorMessage(res));
      }
      addMemberInput.value = "";
      showAddMemberResult("success", "멤버가 추가되었습니다.");
      await loadMembers();
    } catch (err) {
      showAddMemberResult("error", err.message || "멤버 추가에 실패했습니다.");
    }
  }

  async function addMemberByEmail(event) {
    event.preventDefault();
    if (!isOwner) {
      showAddMemberByEmailResult("error", "멤버 추가 권한이 없습니다.");
      return;
    }
    const email = addMemberEmailInput.value.trim();
    if (!email) {
      showAddMemberByEmailResult("error", "이메일을 입력해 주세요.");
      return;
    }
    try {
      const res = await authHelper.authFetch(`/groups/${groupId}/members/email?email=${encodeURIComponent(email)}`, {
        method: "POST",
      });
      if (!res.ok) throw new Error(await extractErrorMessage(res));
      addMemberEmailInput.value = "";
      showAddMemberByEmailResult("success", "멤버가 추가되었습니다.");
      await loadMembers();
    } catch (err) {
      showAddMemberByEmailResult("error", err.message || "멤버 추가에 실패했습니다.");
    }
  }

  function showAddMemberByEmailResult(type, message) {
    addMemberEmailResult.classList.remove("d-none", "alert-success", "alert-danger");
    addMemberEmailResult.classList.add(type === "success" ? "alert-success" : "alert-danger");
    addMemberEmailResult.textContent = message;
  }

  async function removeMember(targetUserId) {
    if (!isOwner) {
      showResult("error", "멤버 제거 권한이 없습니다.");
      return;
    }
    const currentUserId = currentUserInput.value;
    if (!currentUserId) {
      showResult("error", "현재 사용자 ID를 입력해 주세요.");
      return;
    }
    try {
      const res = await authHelper.authFetch(
        `/groups/${groupId}/members/${targetUserId}?currentUserId=${encodeURIComponent(currentUserId)}`,
        { method: "DELETE" }
      );
      if (!res.ok) {
        throw new Error(await extractErrorMessage(res));
      }
      showResult("success", "멤버를 제거했습니다.");
      await loadMembers();
    } catch (err) {
      showResult("error", err.message || "멤버 제거에 실패했습니다.");
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

  memberListBody?.addEventListener("click", (event) => {
    const btn = event.target.closest("[data-action='remove-member']");
    if (!btn) return;
    const targetUserId = btn.dataset.userId;
    if (!targetUserId) return;
    removeMember(targetUserId);
  });

  addMemberForm?.addEventListener("submit", addMember);
  addMemberEmailForm?.addEventListener("submit", addMemberByEmail);
  btnReloadDetail?.addEventListener("click", loadGroupDetail);
  btnReloadMembers?.addEventListener("click", loadMembers);

  document.addEventListener("DOMContentLoaded", () => {
    fillCurrentUser();
    loadGroupDetail();
    loadMembers();
  });
})();
