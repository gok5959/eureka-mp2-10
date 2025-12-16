// static/js/group-calendar.js
(() => {
  "use strict";

  const $ = (id) => document.getElementById(id);

  function safeJsonParse(s) {
    try { return JSON.parse(s); } catch { return null; }
  }

  function getMeta(name) {
    return document.querySelector(`meta[name="${name}"]`)?.getAttribute("content")?.trim() || "";
  }

  // localStorage -> meta
  function getCurrentUserId() {
    const user = safeJsonParse(localStorage.getItem("currentUser") || "null");
    const idFromUser = user?.id ?? user?.userId ?? null;

    const idFromLs = localStorage.getItem("currentUserId");
    const idFromMeta = getMeta("current-user-id");

    const raw = idFromUser ?? idFromLs ?? idFromMeta;
    if (!raw) throw new Error("current user id not found (localStorage/meta)");

    const id = Number(raw);
    if (!Number.isFinite(id) || id <= 0) throw new Error("invalid current user id: " + raw);

    return id;
  }

  function getGroupId() {
    const raw = getMeta("group-id");
    if (!raw) throw new Error("group-id meta missing");
    const id = Number(raw);
    if (!Number.isFinite(id) || id <= 0) throw new Error("invalid group id: " + raw);
    return id;
  }

  function pick(obj, keys) {
    for (const k of keys) if (obj && obj[k] != null) return obj[k];
    return null;
  }

  function normalizeScheduleDto(dto) {
    const id = pick(dto, ["id", "scheduleId"]);
    const title = pick(dto, ["title", "name", "scheduleTitle"]) || "(제목 없음)";
    const startAt = pick(dto, ["startAt", "start", "start_at", "startDateTime"]) || null;
    const endAt = pick(dto, ["endAt", "end", "end_at", "endDateTime"]) || startAt;

    if (!id || !startAt) return null;
    return { id: String(id), title, startAt, endAt };
  }

  function setLoading(on, text = "불러오는 중...") {
    const el = $("loadingOverlay");
    if (!el) return;
    const label = el.querySelector(".fw-bold");
    if (label) label.textContent = text;

    if (on) {
      el.classList.remove("d-none");
      el.classList.add("d-flex");
    } else {
      el.classList.add("d-none");
      el.classList.remove("d-flex");
    }
  }

  // authHelper.authFetch 있으면 그걸 쓰고, 없으면 fetch로 fallback
  async function request(url, options = {}) {
    const headers = { Accept: "application/json", ...(options.headers || {}) };

    if (window.authHelper?.authFetch) {
      return authHelper.authFetch(url, { ...options, headers });
    }
    return fetch(url, { credentials: "same-origin", ...options, headers });
  }

  async function requestJson(url, options = {}) {
    const res = await request(url, options);

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(text || `Request failed: ${res.status} ${url}`);
    }

    const ct = res.headers.get("content-type") || "";
    if (ct.includes("application/json")) return res.json();

    const text = await res.text().catch(() => "");
    return text ? safeJsonParse(text) : null;
  }

  // ===== state =====
  const state = {
    currentUserId: null,
    groupId: null,
    groupName: "그룹",
    isOwner: false,
    members: [],
    schedules: [],
    calendar: null,
    detail: null,
    handlersBound: false,
  };

  // ===== API loaders =====
  async function loadGroupDetail() {
    const data = await requestJson(`/groups/${state.groupId}?currentUserId=${state.currentUserId}`);
    state.detail = data;

    state.groupName = pick(data, ["groupName", "name", "title"]) || "그룹";
    const titleEl = $("groupTitle");
    if (titleEl) titleEl.textContent = state.groupName;

    // detail에 멤버가 포함되어 있으면 활용
    const members = pick(data, ["members", "memberList", "groupMembers"]) || [];
    if (Array.isArray(members) && members.length) state.members = members;

    return data;
  }

  async function loadGroupSchedules() {
    const data = await requestJson(`/groups/${state.groupId}/schedules`);
    const list = Array.isArray(data) ? data : (data?.content || data?.data || []);
    state.schedules = list.map(normalizeScheduleDto).filter(Boolean);
  }

  async function loadGroupMembers() {
    const data = await requestJson(`/groups/${state.groupId}/members?page=0&size=50`);
    const arr = Array.isArray(data) ? data : (data?.content || data?.data || []);
    state.members = Array.isArray(arr) ? arr : [];
  }

  // ===== owner 판단 =====
  function isOwnerFromDetail(detail) {
    const ownerId = detail?.ownerId ?? detail?.owner?.id ?? null;
    if (!ownerId) return false;
    return String(ownerId) === String(state.currentUserId);
  }

  function isOwnerFromMembers(members) {
    const my = (members || []).find((m) => {
      const uid = pick(m, ["userId", "memberUserId", "id", "user_id"]);
      return String(uid) === String(state.currentUserId);
    });

    const role = pick(my, ["role", "memberRole", "groupRole"]) || "";
    return String(role).toUpperCase() === "OWNER";
  }

  function computeIsOwner() {
    // detail에 ownerId가 있으면 그걸 우선
    if (isOwnerFromDetail(state.detail)) return true;
    // 없으면 members의 role로 판단
    return isOwnerFromMembers(state.members);
  }

  function toggleOwnerButtons() {
    const inviteBtn = $("btn-side-add-group-member");
    const editBtn = $("btn-edit-group");

    if (inviteBtn) {
      inviteBtn.classList.toggle("d-none", !state.isOwner);
      inviteBtn.disabled = !state.isOwner;
    }
    if (editBtn) {
      editBtn.classList.toggle("d-none", !state.isOwner);
      editBtn.disabled = !state.isOwner;
    }
  }

  // ===== renderers =====
	function renderMembers() {
	  const ul = $("memberList");
	  if (!ul) return;
	
	  ul.innerHTML = "";
	
	  if (!state.members.length) {
	    const li = document.createElement("li");
	    li.className = "list-group-item text-muted";
	    li.textContent = "멤버 정보가 없습니다.";
	    ul.appendChild(li);
	    return;
	  }
	
	  const roleOrder = { OWNER: 0, LEADER: 1, ADMIN: 2, MEMBER: 3 };
	
	  const sorted = [...state.members].sort((a, b) => {
	    const ra = String(pick(a, ["role", "memberRole", "groupRole"]) || "").toUpperCase();
	    const rb = String(pick(b, ["role", "memberRole", "groupRole"]) || "").toUpperCase();
	
	    const oa = roleOrder[ra] ?? 999;
	    const ob = roleOrder[rb] ?? 999;
	    if (oa !== ob) return oa - ob;
	
	    const na = String(pick(a, ["name", "username", "userName", "nickname"]) || "");
	    const nb = String(pick(b, ["name", "username", "userName", "nickname"]) || "");
	    return na.localeCompare(nb, "ko");
	  });
	
	  sorted.forEach((m) => {
	    const name = pick(m, ["name", "username", "userName", "nickname"]) || "멤버";
	    const role = pick(m, ["role", "memberRole", "groupRole"]) || "-";
	
	    const li = document.createElement("li");
	    li.className = "list-group-item d-flex justify-content-between align-items-center";
	    li.innerHTML = `<span>${escapeHtml(name)}</span><span class="badge text-bg-secondary">${escapeHtml(String(role))}</span>`;
	    ul.appendChild(li);
	  });
	}


  function renderUpcomingSchedules() {
    const ul = $("groupScheduleList");
    if (!ul) return;

    ul.innerHTML = "";

    const now = new Date();
    const upcoming = state.schedules
      .filter((s) => new Date(s.startAt) >= now)
      .sort((a, b) => new Date(a.startAt) - new Date(b.startAt))
      .slice(0, 10);

    if (!upcoming.length) {
      const li = document.createElement("li");
      li.className = "list-group-item text-muted";
      li.textContent = "다가오는 일정이 없습니다.";
      ul.appendChild(li);
      return;
    }

    upcoming.forEach((s) => {
      const li = document.createElement("li");
      li.className = "list-group-item";
      li.style.cursor = "pointer";
      li.textContent = s.title;
      // li.addEventListener("click", () => {
      //   window.location.href = `/group-schedules/${s.id}`;
      // });
      /** 추가2 **/
      li.addEventListener("click", () => {
        const params = new URLSearchParams();
        params.set("scheduleId", s.id);
        params.set("groupId", state.groupId);

        window.location.href = `/pages/schedule-detail.html?${params.toString()}`;
      });

      /** 여기까지 **/
      ul.appendChild(li);
    });
  }

  // ===== calendar =====
  function initCalendar() {
    const el = $("calendar");
    if (!el) return;

    if (!window.FullCalendar || !window.FullCalendar.Calendar) {
      el.innerHTML = `<div class="text-danger small p-3">FullCalendar 로드 실패</div>`;
      return;
    }

    state.calendar = new FullCalendar.Calendar(el, {
      initialView: "dayGridMonth",
      locale: "ko",
      height: "auto",
      headerToolbar: {
        left: "prev,next today",
        center: "title",
        right: "dayGridMonth,timeGridWeek,timeGridDay",
      },

      events: state.schedules.map((s) => ({
        id: s.id,
        title: s.title,
        start: s.startAt,
        end: s.endAt,
      })),
      /** 추가2 **/
      eventClick: (info) => {
        const params = new URLSearchParams();
        params.set("scheduleId", info.event.id);
        params.set("groupId", state.groupId);

        window.location.href = `/pages/schedule-detail.html?${params.toString()}`;
      },

      /** 여기까지 **/
    });

    state.calendar.render();
  }


  /** 추가1 **/
  function bindAddScheduleButtons() {
    const goGroupScheduleForm = () => {
      window.location.href = `/pages/schedule-form.html?groupId=${state.groupId}`;
    };

    $("btn-header-add-schedule")?.addEventListener("click", goGroupScheduleForm);
    $("btn-side-add-schedule")?.addEventListener("click", goGroupScheduleForm);
  }

  /** 여기까지 **/

  function refreshCalendar() {
    if (!state.calendar) return;
    state.calendar.removeAllEvents();
    state.calendar.addEventSource(
      state.schedules.map((s) => ({
        id: s.id,
        title: s.title,
        start: s.startAt,
        end: s.endAt,
      }))
    );
  }

  // ===== modal helpers =====
  function showInviteResult(type, message) {
    const box = $("inviteResult");
    if (!box) return;

    box.classList.remove("d-none", "alert-success", "alert-danger", "alert-info");
    box.classList.add(type === "success" ? "alert-success" : "alert-danger");
    box.textContent = message;
  }

  function clearInviteResult() {
    $("inviteResult")?.classList.add("d-none");
  }

  async function postInviteByEmail(email) {
  const url = `/groups/${state.groupId}/members/email?email=${encodeURIComponent(email)}`;
  return requestJson(url, { method: "POST" });
}

  function bindHandlersOnce() {
    if (state.handlersBound) return;
    state.handlersBound = true;

    // 그룹 정보 수정 버튼
    $("btn-edit-group")?.addEventListener("click", () => {
      if (!state.isOwner) return alert("그룹장(OWNER)만 수정할 수 있어요.");
      window.location.href = `/groups/page/${state.groupId}/edit`;
    });

    // 모달 submit
    $("inviteMemberForm")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearInviteResult();

      if (!state.isOwner) {
        showInviteResult("error", "그룹장(OWNER)만 멤버를 초대할 수 있어요.");
        return;
      }

      const email = $("inviteEmail")?.value?.trim();
      if (!email) return;

      try {
        await postInviteByEmail(email);
        showInviteResult("success", "초대 완료! 멤버로 추가되었습니다.");
        $("inviteEmail").value = "";

        // 멤버 목록 갱신
        await loadGroupMembers();
        renderMembers();
      } catch (err) {
        showInviteResult("error", err?.message || "초대에 실패했습니다.");
      }
    });

    // 모달 열릴 때 결과 초기화
    const modalEl = $("inviteMemberModal");
    if (modalEl) {
      modalEl.addEventListener("show.bs.modal", () => clearInviteResult());
    }
  }

  // ===== utils =====
  function escapeHtml(text) {
    if (text == null) return "";
    return String(text).replace(/[&<>"']/g, (c) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#39;",
    }[c]));
  }

  // ===== bootstrap =====
  async function bootstrap() {
    setLoading(true, "불러오는 중...");

    try {
      state.currentUserId = getCurrentUserId();
      state.groupId = getGroupId();


      // 디버깅 로그
      console.log("currentUserId:", state.currentUserId);
      console.log("groupId:", state.groupId);

      /** 추가1 **/
      bindAddScheduleButtons();

      // 1) 빈 화면 방지: 일단 달력부터(빈 이벤트로) 띄움
      initCalendar();

      // 2) 데이터 로딩
      await loadGroupDetail();
      await loadGroupSchedules();
      await loadGroupMembers();

      // 3) owner 판단 + 버튼 토글
      state.isOwner = computeIsOwner();
      toggleOwnerButtons();

      // 4) 렌더
      renderMembers();
      renderUpcomingSchedules();
      refreshCalendar();

      // 5) 이벤트 바인딩(1번만)
      bindHandlersOnce();
    } catch (e) {
      console.error("group-calendar bootstrap error:", e);
      const titleEl = $("groupTitle");
      if (titleEl) titleEl.textContent = "그룹(로딩 실패)";
      initCalendar();
    } finally {
      setLoading(false);
    }
  }

  document.addEventListener("DOMContentLoaded", bootstrap);
})();
