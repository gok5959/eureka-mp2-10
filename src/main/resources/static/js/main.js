// static/js/main.js
(() => {
  "use strict";

  const $ = (id) => document.getElementById(id);

  function safeJsonParse(s) {
    try { return JSON.parse(s); } catch { return null; }
  }

  // localStorage -> meta -> fallback
  function getCurrentUser() {
    const user = safeJsonParse(localStorage.getItem("currentUser") || "null");
    const idFromUser = user?.id ?? user?.userId ?? null;
    const nameFromUser = user?.name ?? user?.userName ?? user?.username ?? null;

    const idFromLs = localStorage.getItem("currentUserId");
    const nameFromLs = localStorage.getItem("currentUserName");

    const idFromMeta = document.querySelector('meta[name="current-user-id"]')?.getAttribute("content");
    const nameFromMeta = document.querySelector('meta[name="current-user-name"]')?.getAttribute("content");

    const id = Number(idFromUser ?? idFromLs ?? idFromMeta ?? 1) || 1;
    const name = String(nameFromUser ?? nameFromLs ?? nameFromMeta ?? "사용자 1");

    return { id, name };
  }

  const { id: CURRENT_USER_ID, name: CURRENT_USER_NAME } = getCurrentUser();

  // 화면 반영
  $("currentUserNameText") && ($("currentUserNameText").textContent = CURRENT_USER_NAME);

  // meta 동기화
  document.querySelector('meta[name="current-user-id"]')
    ?.setAttribute("content", String(CURRENT_USER_ID));
  document.querySelector('meta[name="current-user-name"]')
    ?.setAttribute("content", CURRENT_USER_NAME);

  // ===== state (단 1번만!) =====
  const state = {
    userId: CURRENT_USER_ID,
    groups: [],                      // {id, name, checked}
    personalSchedules: [],           // schedule[]
    groupSchedulesByGroupId: new Map(), // groupId -> schedule[]
    calendar: null,
  };

  // ===== util =====
  const pick = (obj, keys) => {
    for (const k of keys) if (obj && obj[k] != null) return obj[k];
    return null;
  };

  const formatDateTime = (iso) => {
    if (!iso) return "";
    const d = new Date(iso);
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getMonth() + 1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };

  function normalizeScheduleDto(dto, groupIdOrNull) {
    const id = pick(dto, ["id", "scheduleId"]);
    const title = pick(dto, ["title", "name", "scheduleTitle"]) || "(제목 없음)";
    const startAt = pick(dto, ["startAt", "start", "startDateTime"]) || null;
    const endAt = pick(dto, ["endAt", "end", "endDateTime"]) || startAt;

    if (!id || !startAt) return null;

    return {
      id: String(id),
      title,
      startAt,
      endAt,
      groupId: groupIdOrNull,
      isPersonal: groupIdOrNull == null,
    };
  }

  function getGroupName(groupId) {
    const g = state.groups.find((gg) => String(gg.id) === String(groupId));
    return g ? g.name : "그룹";
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

  // =========================
  // API (GET)
  // =========================
  async function fetchJson(url) {
    const res = await fetch(url, {
      method: "GET",
      credentials: "same-origin",
      headers: { "Accept": "application/json" },
    });

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(`Request failed: ${res.status} ${url}\n${text}`);
    }

    // 204 같은 케이스 방어
    const ct = res.headers.get("content-type") || "";
    if (!ct.includes("application/json")) {
      const text = await res.text().catch(() => "");
      return text ? safeJsonParse(text) : null;
    }

    return res.json();
  }

  async function loadPersonalSchedules() {
    const url = `/personal-schedules?ownerId=${state.userId}`;
    const data = await fetchJson(url);

    const list = Array.isArray(data) ? data : (data?.content || data?.data || []);
    state.personalSchedules = list.map((dto) => normalizeScheduleDto(dto, null)).filter(Boolean);
  }

  async function loadGroups() {
    const url = `/groups/users?currentUserId=${state.userId}&page=0&size=50`;
    const data = await fetchJson(url);

    const arr = Array.isArray(data) ? data : (data?.content || data?.data || []);
    state.groups = arr.map((dto) => {
      const id = pick(dto, ["groupId", "group_id", "id"]);
      const name = pick(dto, ["groupName", "group_name", "name", "title"]) || `그룹 ${id}`;
      if (!id) return null;
      return { id, name, checked: true };
    }).filter(Boolean);
  }

  async function loadGroupSchedulesForAllGroups() {
    const promises = state.groups.map(async (g) => {
      const url = `/groups/${g.id}/schedules`;
      const data = await fetchJson(url);

      const list = Array.isArray(data) ? data : (data?.content || data?.data || []);
      const norm = list.map((dto) => normalizeScheduleDto(dto, g.id)).filter(Boolean);
      state.groupSchedulesByGroupId.set(g.id, norm);
    });

    await Promise.all(promises);
  }

  // =========================
  // 데이터 합치기 / 이벤트 생성
  // =========================
  function getAllMySchedules() {
    const all = [...state.personalSchedules];
    for (const [, list] of state.groupSchedulesByGroupId.entries()) all.push(...list);
    return all;
  }

  function buildCalendarEvents() {
    const events = [];

    // 개인 일정
    for (const s of state.personalSchedules) {
      events.push({
        id: s.id,
        title: s.title,
        start: s.startAt,
        end: s.endAt,
        extendedProps: { groupId: s.groupId, isPersonal: true },
      });
    }

    // 그룹 일정 (체크된 그룹만)
    for (const g of state.groups) {
      if (!g.checked) continue;
      const list = state.groupSchedulesByGroupId.get(g.id) || [];
      for (const s of list) {
        events.push({
          id: s.id,
          title: s.title,
          start: s.startAt,
          end: s.endAt,
          extendedProps: { groupId: g.id, isPersonal: false },
        });
      }
    }

    return events;
  }

  // =========================
  // 렌더링: 사이드 리스트
  // =========================
  function renderUpcomingList() {
    const ul = $("myScheduleList");
    if (!ul) return;

    ul.innerHTML = "";

    const now = new Date();
    const all = getAllMySchedules()
      .filter((s) => new Date(s.startAt) >= now)
      .sort((a, b) => new Date(a.startAt) - new Date(b.startAt))
      .slice(0, 10);

    if (!all.length) {
      const li = document.createElement("li");
      li.className = "list-group-item text-muted";
      li.textContent = "다가오는 일정이 없습니다.";
      ul.appendChild(li);
      return;
    }

    all.forEach((s) => {
      const li = document.createElement("li");
      li.className = "list-group-item d-flex flex-column";
      li.style.cursor = "pointer";

      const top = document.createElement("div");
      top.className = "d-flex justify-content-between align-items-center";

      const titleSpan = document.createElement("span");
      titleSpan.textContent = s.title;

      const badge = document.createElement("span");
      badge.className = "badge text-bg-secondary";
      badge.textContent = s.isPersonal ? "개인" : getGroupName(s.groupId);

      top.appendChild(titleSpan);
      top.appendChild(badge);

      const bottom = document.createElement("small");
      bottom.className = "text-muted";
      bottom.textContent = formatDateTime(s.startAt);

      li.appendChild(top);
      li.appendChild(bottom);

      li.addEventListener("click", () => {
        // const base = s.isPersonal ? "/personal-schedules" : "/group-schedules";
        // window.location.href = `${base}/${s.id}`;
        /** 수정 **/
        const params = new URLSearchParams();
        params.set("scheduleId", s.id);

        if (!s.isPersonal && s.groupId) {
          params.set("groupId", s.groupId);
        }

        window.location.href = `/pages/schedule-detail.html?${params.toString()}`;
        /** 수정 **/
      });

      ul.appendChild(li);
    });
  }

  function renderGroupList() {
    const ul = $("myGroupList");
    if (!ul) return;

    ul.innerHTML = "";

    if (!state.groups.length) {
      const li = document.createElement("li");
      li.className = "list-group-item text-muted";
      li.textContent = "가입된 그룹이 없습니다.";
      ul.appendChild(li);
      return;
    }

    state.groups.forEach((g) => {
      const li = document.createElement("li");
      li.className = "list-group-item d-flex justify-content-between align-items-center group-item";

      const left = document.createElement("div");
      left.className = "d-flex align-items-center gap-2";

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.checked = g.checked;
      checkbox.addEventListener("click", (e) => {
        e.stopPropagation();
        g.checked = checkbox.checked;
        refreshCalendar();
      });

      const nameBtn = document.createElement("button");
      nameBtn.type = "button";
      nameBtn.className = "btn btn-link btn-sm p-0 text-start";
      nameBtn.textContent = g.name;
      nameBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        window.location.href = `/groups/${g.id}/calendar`;
      });

      left.appendChild(checkbox);
      left.appendChild(nameBtn);
      li.appendChild(left);

      ul.appendChild(li);
    });
  }

  // =========================
  // FullCalendar
  // =========================
  function initCalendar() {
    const el = $("calendar");
    if (!el) {
      console.warn("initCalendar: #calendar 엘리먼트가 없습니다.");
      return;
    }

    if (!window.FullCalendar || !window.FullCalendar.Calendar) {
      console.error("FullCalendar 로드 실패. CDN 스크립트가 로드되지 않았습니다.");
      el.innerHTML = `<div class="text-danger small p-3">
        FullCalendar 스크립트 로드 실패 (콘솔/Network 확인)
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
      events: buildCalendarEvents(),

      eventClick: (info) => {
        // const ext = info.event.extendedProps || {};
        // const base = ext.isPersonal ? "/personal-schedules" : "/group-schedules";
        // window.location.href = `${base}/${info.event.id}`;
        /** 수정 **/
        const ext = info.event.extendedProps || {};
        const params = new URLSearchParams();

        params.set("scheduleId", info.event.id);

        if (!ext.isPersonal && ext.groupId) {
          params.set("groupId", ext.groupId);
        }

        window.location.href = `/pages/schedule-detail.html?${params.toString()}`;
        /** 수정 **/
      },
    });

    state.calendar.render();
  }

  function refreshCalendar() {
    if (!state.calendar) return;
    state.calendar.removeAllEvents();
    state.calendar.addEventSource(buildCalendarEvents());
  }

  // =========================
  // 버튼 바인딩
  // =========================
  function bindButtons() {
    const goScheduleForm = () => {
      // TODO: 실제 일정 생성 페이지 URL로 변경
      window.location.href = "/pages/schedule-form.html"; /** 수정 **/ // window.location.href = "/schedules/new"; // 임시
    };
    const goGroupForm = () => {
      // TODO: 실제 그룹 생성 페이지 URL로 변경
      window.location.href = "/groups/page/new"; // 임시
    };
    const goGroupList = () => {
		window.location.href = "/groups/page/list"
	}

    ["btn-header-add-schedule", "btn-side-add-schedule"].forEach((id) => {
      $(id)?.addEventListener("click", goScheduleForm);
    });

    ["btn-header-add-group", "btn-side-add-group"].forEach((id) => {
      $(id)?.addEventListener("click", goGroupForm);
    });
    
    ["btn-side-reg-group"].forEach((id) => {
	  $(id)?.addEventListener("click", goGroupList);
	});

    $("btnCheckAllGroups")?.addEventListener("click", () => {
      state.groups.forEach((g) => (g.checked = true));
      renderGroupList();
      refreshCalendar();
    });

    $("btnUncheckAllGroups")?.addEventListener("click", () => {
      state.groups.forEach((g) => (g.checked = false));
      renderGroupList();
      refreshCalendar();
    });
  }

  // =========================
  // bootstrap
  // =========================
  async function bootstrap() {
    bindButtons();

    // 1) 일단 빈 달력이라도 먼저 띄우기 (사용자 체감 개선 + 디버깅 쉬움)
    renderGroupList();
    renderUpcomingList();
    initCalendar();
    
    setLoading(true, "불러오는 중...");

    // 2) 데이터 로딩 후 리프레시
    try {
      await loadPersonalSchedules();
      await loadGroups();
      await loadGroupSchedulesForAllGroups();

      renderGroupList();
      renderUpcomingList();
      refreshCalendar();
    } catch (e) {
      console.error("초기 로딩 중 오류:", e);
      // 에러나도 이미 빈 달력은 떠있음
    } finally {
		setLoading(false);
	}
  }

  document.addEventListener("DOMContentLoaded", bootstrap);
})();
