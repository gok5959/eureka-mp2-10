(() => {
  const form = document.getElementById("groupCreateForm");
  const resultBox = document.getElementById("groupCreateResult");

  function showResult(type, message) {
    resultBox.classList.remove("d-none", "alert-success", "alert-danger");
    resultBox.classList.add(type === "success" ? "alert-success" : "alert-danger");
    resultBox.innerHTML = message;
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
      document.getElementById("currentUserId").value = user.id;
    }
  }

  form?.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!form.checkValidity()) {
      form.classList.add("was-validated");
      return;
    }

    const currentUserId = document.getElementById("currentUserId").value;
    const payload = {
      name: document.getElementById("groupName").value.trim(),
      description: document.getElementById("groupDescription").value.trim(),
    };

    try {
      const res = await authHelper.authFetch(`/groups?currentUserId=${encodeURIComponent(currentUserId)}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...getCsrfHeaders(),
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error(await extractErrorMessage(res));
      }

      const group = await res.json();
      showResult(
        "success",
        `그룹이 생성되었습니다. <a href="/groups/page/${group.id}" class="alert-link">상세 보기</a>`
      );
      form.reset();
      form.classList.remove("was-validated");
      document.getElementById("currentUserId").value = currentUserId;
    } catch (err) {
      showResult("error", err.message || "그룹 생성에 실패했습니다.");
    }
  });

  document.addEventListener("DOMContentLoaded", fillCurrentUser);
})();
