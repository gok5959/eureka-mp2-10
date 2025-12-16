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

  // localStorage -> meta (no silent "1" fallback)
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
	  const el = document.getElementById("loadingOverlay");
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


  async function fetchJson(url, options = {}) {
    const res = await fetch(url, {
      credentials: "same-origin",
      headers: { "Accept": "application/json", ...(options.headers || {}) },
      ...options,
    });

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(`Request failed: ${res.status} ${url}\n${text}`);
    }

    const ct = res.headers.get("content-type") || "";
    if (!ct.includes("application/json")) {
      const text = await res.text().catch(() => "");
      return text ? safeJsonParse(text) : null;
    }

    return res.json();
  }

  // ===== state =====
  const state = {
    currentUserId: null,
    isOwner: false,
    groupId: null,
    groupName: "그룹",
    members: [],
    schedules: [],
    calendar: null,
  };

  // ===== API loaders =====
  async function loadGroupDetail() {
    const data = await fetchJson(`/groups/${state.groupId}?currentUserId=${state.currentUserId}`);

    state.groupName = pick(data, ["groupName", "name", "title"]) || "그룹";
    const titleEl = $("groupTitle");
    if (titleEl) titleEl.textContent = state.groupName;

    // 만약 detail 응답에 멤버가 포함될 수도 있으니 받아두기(없으면 그대로)
    const members = pick(data, ["members", "memberList", "groupMembers"]) || [];
    if (Array.isArray(members) && members.length) state.members = members;
  }

  async function loadGroupSchedules() {
    const data = await fetchJson(`/groups/${state.groupId}/schedules`);
    const list = Array.isArray(data) ? data : (data?.content || data?.data || []);
    state.schedules = list.map(normalizeScheduleDto).filter(Boolean);
  }
  
  function isOwnerFromMembers(members) {
  // 멤버 DTO에 맞게 키들 넉넉히 잡음
	  const my = (members || []).find(m => {
	    const uid = pick(m, ["userId", "memberUserId", "id", "user_id"]);
	    return String(uid) === String(state.currentUserId);
	  });
	  const role = pick(my, ["role", "memberRole", "groupRole"]) || "";
	  return String(role).toUpperCase() === "OWNER"; // 필요하면 "LEADER"도 추가
	}
	
	function toggleInviteButton() {
	  const btn = $("btn-side-add-group-member");
	  if (!btn) return;
	  btn.classList.toggle("d-none", !state.isOwner);
	  btn.disabled = !state.isOwner;
	}


  async function loadGroupMembers() {
    const data = await fetchJson(`/groups/${state.groupId}/members?page=0&size=50`);
    const arr = Array.isArray(data) ? data : (data?.content || data?.data || []);
    state.members = Array.isArray(arr) ? arr : [];
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

    state.members.forEach((m) => {
      const name = pick(m, ["name", "username", "userName", "nickname"]) || "멤버";
      const role = pick(m, ["role"]) || "";

      const li = document.createElement("li");
      li.className = "list-group-item d-flex justify-content-between align-items-center";
      li.innerHTML = `<span>${name}</span><span class="badge text-bg-secondary">${role}</span>`;
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
      li.addEventListener("click", () => {
        window.location.href = `/group-schedules/${s.id}`;
      });
      ul.appendChild(li);
    });
  }

  // ===== calendar =====
  function initCalendar() {
    const el = $("calendar");
    if (!el) {
      console.warn("initCalendar: #calendar 엘리먼트가 없습니다.");
      return;
    }

    if (!window.FullCalendar || !window.FullCalendar.Calendar) {
      console.error("FullCalendar 로드 실패. CDN 스크립트 로드 여부 확인");
      el.innerHTML = `<div class="text-danger small p-3">
        FullCalendar 로드 실패 (Console/Network 확인)
      </div>`;
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
      eventClick: (info) => {
        window.location.href = `/group-schedules/${info.event.id}`;
      },
    });

    state.calendar.render();
  }

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

  // ===== bootstrap =====
  async function bootstrap() {
	setLoading(true, "불러오는 중...");
	
    try {
      state.currentUserId = getCurrentUserId();
      state.groupId = getGroupId();

      // 디버깅 로그
      console.log("currentUserId:", state.currentUserId);
      console.log("groupId:", state.groupId);

      // 1) 빈 화면 방지: 일단 달력부터(빈 이벤트로) 띄움
      initCalendar();

      // 2) 데이터 로딩
      await loadGroupDetail();
      await loadGroupSchedules();
      await loadGroupMembers();
      
      // ✅ 여기 추가
      state.isOwner = isOwnerFromMembers(state.members);
      toggleInviteButton();

      renderMembers();
      renderUpcomingSchedules();
      refreshCalendar();
    } catch (e) {
      console.error("group-calendar bootstrap error:", e);

      // 최소한 UI에 표시
      const titleEl = $("groupTitle");
      if (titleEl) titleEl.textContent = "그룹(로딩 실패)";

      // 달력이라도 띄우기
      initCalendar();
    }
    finally {
		setLoading(false);
	}
  }

  document.addEventListener("DOMContentLoaded", bootstrap);
  
  document.addEventListener("DOMContentLoaded", () => {
  $("btn-side-add-group-member")?.addEventListener("click", () => {
    if (!state.isOwner) {
      alert("그룹장(OWNER)만 멤버를 초대할 수 있어요.");
      return;
    }
    // 너희 초대 페이지/모달로 연결
    window.location.href = `/groups/${state.groupId}/members/invite`;
  });

  bootstrap();
});

})();
