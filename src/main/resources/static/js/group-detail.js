(() => {
  const segments = window.location.pathname.split("/").filter(Boolean);
  const groupId = document.body.dataset.groupId || (segments.length >= 3 ? segments[2] : null);
  const resultBox = document.getElementById("groupDetailResult");
  const btnReload = document.getElementById("btnReloadDetail");
  const currentUserInput = document.getElementById("detailCurrentUserId");

  const nameEl = document.getElementById("detailName");
  const descEl = document.getElementById("detailDescription");
  const ownerEl = document.getElementById("detailOwner");
  const roleEl = document.getElementById("detailMyRole");
  const memberCountEl = document.getElementById("detailMemberCount");

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
      showResult("success", "그룹 정보를 불러왔습니다.");
    } catch (err) {
      showResult("error", err.message || "그룹 정보를 불러오지 못했습니다.");
    }
  }

  btnReload?.addEventListener("click", loadGroupDetail);

  document.addEventListener("DOMContentLoaded", () => {
    fillCurrentUser();
    loadGroupDetail();
  });
})();
