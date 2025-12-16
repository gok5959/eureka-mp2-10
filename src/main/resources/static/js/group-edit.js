(() => {
  const segments = window.location.pathname.split("/").filter(Boolean);
  const groupIdFromPath = segments.length >= 3 ? segments[2] : null;
  const groupId = document.body.dataset.groupId || groupIdFromPath;
  const form = document.getElementById("groupEditForm");
  const resultBox = document.getElementById("groupEditResult");
  const btnReload = document.getElementById("btnReloadGroup");

  const nameInput = document.getElementById("editName");
  const descInput = document.getElementById("editDescription");
  const currentUserInput = document.getElementById("editCurrentUserId");

  function showResult(type, message) {
    resultBox.classList.remove("d-none", "alert-success", "alert-danger");
    resultBox.classList.add(type === "success" ? "alert-success" : "alert-danger");
    resultBox.textContent = message;
  }

  function getCsrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    return token && header ? { [header]: token } : {};
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

  async function loadGroup() {
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
      nameInput.value = detail.name ?? "";
      descInput.value = detail.description ?? "";
      showResult("success", "그룹 정보를 불러왔습니다.");
    } catch (err) {
      showResult("error", err.message || "그룹 정보를 불러오지 못했습니다.");
    }
  }

  btnReload?.addEventListener("click", loadGroup);

  form?.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!form.checkValidity()) {
      form.classList.add("was-validated");
      return;
    }

    const currentUserId = currentUserInput.value;
    if (!currentUserId) {
      showResult("error", "현재 사용자 ID를 입력해 주세요.");
      return;
    }

    const payload = {
      name: nameInput.value.trim(),
      description: descInput.value.trim(),
    };

    try {
      const res = await authHelper.authFetch(`/groups/${groupId}?currentUserId=${encodeURIComponent(currentUserId)}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          ...getCsrfHeaders(),
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error(await extractErrorMessage(res));
      }

      const data = await res.json();
      showResult("success", `그룹 정보가 수정되었습니다. (이름: ${data.name})`);
    } catch (err) {
      showResult("error", err.message || "그룹 수정에 실패했습니다.");
    }
  });

  document.addEventListener("DOMContentLoaded", () => {
    fillCurrentUser();
    loadGroup();
  });
})();
