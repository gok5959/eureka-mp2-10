(() => {
  const form = document.getElementById("signupForm");
  const resultBox = document.getElementById("signupResult");

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
      email: document.getElementById("signupEmail").value.trim(),
      name: document.getElementById("signupName").value.trim(),
      password: document.getElementById("signupPassword").value,
      role: document.getElementById("signupRole").value,
    };

    try {
      const res = await fetch("/users", {
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

      const user = await res.json();
      form.reset();
      form.classList.remove("was-validated");
      showResult("success", `${user.name ?? "사용자"}님 가입이 완료되었습니다.`);
    } catch (err) {
      showResult("error", err.message || "회원가입에 실패했습니다.");
    }
  });
})();
