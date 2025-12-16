(() => {
  const form = document.getElementById("loginForm");
  const resultBox = document.getElementById("loginResult");

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

  form?.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!form.checkValidity()) {
      form.classList.add("was-validated");
      return;
    }

    const payload = {
      email: document.getElementById("loginEmail").value.trim(),
      password: document.getElementById("loginPassword").value,
    };

    try {
      const res = await fetch("/auth/login", {
        method: "POST",
        credentials: "include",
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
      localStorage.setItem("accessToken", data.accessToken ?? "");
      authHelper?.setCurrentUser?.(data.user || null);
      showResult("success", `${data.user?.name ?? "사용자"}님 환영합니다!`);
      window.location.href = '/testmain'
    } catch (err) {
      showResult("error", err.message || "로그인에 실패했습니다.");
    }
  });
})();
