// static/js/main.js
(() => {
  const $ = (id) => document.getElementById(id);

  // =========================
  //  현재 유저 (임시: id = 1)
  // =========================
  const CURRENT_USER_ID = Number(
    document.querySelector('meta[name="current-user-id"]')?.content || "1"
  ); // id 삭제 필요
  // TODO: Security 적용 후에는, 백엔드에서 로그인 유저 정보를 내려받아서 사용
  // 예시 (Controller)
  // @GetMapping("/main")
  // public String main(@AuthenticationPrincipal UserPrincipal principal, Model model) {
  //     Long currentUserId = principal.getId();
  //     model.addAttribute("currentUserId", currentUserId);
  //     model.addAttribute("currentUserName", principal.getName());
  //     return "main";
  // }

  // ===== util =====
  const pick = (obj, keys) => {
    for (const k of keys) if (obj && obj[k] != null) return obj[k];
    return null;
  };

  const formatDateTime = (iso) => {
    if (!iso) return "";
    const d = new Date(iso);
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getMonth() + 1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(
      d.getMinutes()
    )}`;
  };

  function normalizeScheduleDto(dto, groupIdOrNull) {
    const id = pick(dto, ["id", "scheduleId"]);
    const title =
      pick(dto, ["title", "name", "scheduleTitle"]) || "(제목 없음)";
    const startAt =
      pick(dto, ["startAt", "start", "startDateTime"]) || null;
    const endAt =
      pick(dto, ["endAt", "end", "endDateTime"]) || startAt;

    if (!id || !startAt) return null;

    return {
      id: String(id),
      title,
      startAt,
      endAt,
      groupId: groupIdOrNull, // null이면 개인 일정
      isPersonal: groupIdOrNull == null,
    };
  }

  function getGroupName(groupId) {
    const g = state.groups.find((gg) => String(gg.id) === String(groupId));
    return g ? g.name : "그룹";
  }

  // ===== state =====
  const state = {
    userId: CURRENT_USER_ID,
    groups: [], // {id, name, checked}
    personalSchedules: [], // [{id,title,startAt,endAt,isPersonal:true}]
    groupSchedulesByGroupId: new Map(), // groupId -> [schedule]
    calendar: null,
  };

  // =========================
  //  API 호출 (전부 GET만 사용)
  // =========================

  async function fetchJson(url) {
    const res = await fetch(url, { method: "GET" });
    if (!res.ok) {
      throw new Error(`Request failed: ${res.status} ${url}`);
    }
    return res.json();
  }

  async function loadPersonalSchedules() {
    const url = `/personal-schedules?ownerId=${state.userId}`; // id 삭제 필요
    const data = await fetchJson(url);

    const list = Array.isArray(data)
      ? data
      : data.content || data.data || [];

    state.personalSchedules = list
      .map((dto) => normalizeScheduleDto(dto, null))
      .filter(Boolean);
  }

  async function loadGroups() {
    // 현재 로그인 유저가 속한 그룹 목록
    const url = `/groups/users?currentUserId=${state.userId}&page=0&size=50`; // id 삭제 필요
    const data = await fetchJson(url);
    const arr = Array.isArray(data) ? data : data.content || [];

    state.groups = arr
      .map((dto) => {
        const id = pick(dto, ["groupId", "group_id", "id"]);
		const name = pick(dto, ["groupName", "group_name", "name", "title"]) || `그룹 ${id}`;
        if (!id) return null;
        return { id, name, checked: true }; // 처음 진입 시 모두 체크
      })
      .filter(Boolean);
  }

  async function loadGroupSchedulesForAllGroups() {
    const promises = state.groups.map(async (g) => {
      const url = `/groups/${g.id}/schedules`;
      const data = await fetchJson(url);
      const list = Array.isArray(data)
        ? data
        : data.content || data.data || [];

      const norm = list
        .map((dto) => normalizeScheduleDto(dto, g.id))
        .filter(Boolean);

      state.groupSchedulesByGroupId.set(g.id, norm);
    });

    await Promise.all(promises);
  }

  // =========================
  //  데이터 합치기
  // =========================

  function getAllMySchedules() {
    const all = [...state.personalSchedules];
    for (const [gid, list] of state.groupSchedulesByGroupId.entries()) {
      all.push(...list);
    }
    return all;
  }

  function buildCalendarEvents() {
    const events = [];

    // 개인 일정: 항상 표시
    for (const s of state.personalSchedules) {
      events.push({
        id: s.id,
        title: s.title,
        start: s.startAt,
        end: s.endAt,
        extendedProps: {
          groupId: s.groupId,
          isPersonal: true,
        },
      });
    }

    // 그룹 일정: 체크된 그룹만 표시
    for (const g of state.groups) {
      if (!g.checked) continue;
      const list = state.groupSchedulesByGroupId.get(g.id) || [];
      for (const s of list) {
        events.push({
          id: s.id,
          title: s.title,
          start: s.startAt,
          end: s.endAt,
          extendedProps: {
            groupId: g.id,
            isPersonal: false,
          },
        });
      }
    }

    return events;
  }

  // =========================
  //  렌더링: 나의 일정 (TOP 10)
  // =========================
  function renderUpcomingList() {
    const ul = $("myScheduleList");
    ul.innerHTML = "";

    const now = new Date();
    const all = getAllMySchedules()
      .filter((s) => new Date(s.startAt) >= now)
      .sort(
        (a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime()
      )
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

      // 클릭 → 일정 상세 페이지로 이동 (모달 X)
      li.style.cursor = "pointer";
      li.addEventListener("click", () => {
        const base =
          s.isPersonal ? "/personal-schedules" : "/group-schedules";
        // TODO: 여기서 실제 상세 페이지 URL로 연결
        window.location.href = `${base}/${s.id}`;
      });

      ul.appendChild(li);
    });
  }

  // =========================
  //  렌더링: 내 그룹 리스트
  // =========================
  function renderGroupList() {
    const ul = $("myGroupList");
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
      li.className =
        "list-group-item d-flex justify-content-between align-items-center group-item";

      const left = document.createElement("div");
      left.className = "d-flex align-items-center gap-2";

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.checked = g.checked;
      checkbox.addEventListener("click", (e) => {
        e.stopPropagation(); // 그룹 클릭 이벤트로 안 번지게
        g.checked = checkbox.checked;
        refreshCalendar();
      });

      const nameBtn = document.createElement("button");
      nameBtn.type = "button";
      nameBtn.className = "btn btn-link btn-sm p-0 text-start";
      nameBtn.textContent = g.name;
      nameBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        // 그룹 클릭 → 그룹 페이지로 이동
        // TODO: 실제 그룹 페이지 URL로 연결
        window.location.href = `/groups/${g.id}`; // 예: /groups/{id} 상세 페이지
      });

      left.appendChild(checkbox);
      left.appendChild(nameBtn);
      li.appendChild(left);

      ul.appendChild(li);
    });
  }

  // =========================
  //  FullCalendar 초기화
  // =========================
  function initCalendar() {
    const el = $("calendar");
    state.calendar = new FullCalendar.Calendar(el, {
      initialView: "dayGridMonth",
      height: "auto",
      headerToolbar: {
        left: "prev,next today",
        center: "title",
        right: "dayGridMonth,timeGridWeek,timeGridDay",
      },
      events: buildCalendarEvents(),

      // 달력에서 일정 클릭 → 상세 페이지 이동
      eventClick: (info) => {
        const ext = info.event.extendedProps || {};
        const isPersonal = !!ext.isPersonal;
        const base = isPersonal ? "/personal-schedules" : "/group-schedules";
        // TODO: 실제 상세 페이지 URL로 연결
        window.location.href = `${base}/${info.event.id}`;
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
  //  버튼 바인딩 (페이지 이동용)
  // =========================
  function bindButtons() {
    const goScheduleForm = () => {
      // TODO: 실제 일정 생성 페이지 URL로 변경
      window.location.href = "/schedules/new"; // 임시
    };
    const goGroupForm = () => {
      // TODO: 실제 그룹 생성 페이지 URL로 변경
      window.location.href = "/groups/new"; // 임시
    };

    ["btn-header-add-schedule", "btn-side-add-schedule"].forEach((id) => {
      $(id)?.addEventListener("click", goScheduleForm);
    });

    ["btn-header-add-group", "btn-side-add-group"].forEach((id) => {
      $(id)?.addEventListener("click", goGroupForm);
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
  //  부트스트랩
  // =========================
  async function bootstrap() {
    try {
      bindButtons();

      await loadPersonalSchedules();
      await loadGroups();
      await loadGroupSchedulesForAllGroups();

      renderGroupList();
      renderUpcomingList();
      initCalendar();
    } catch (e) {
      console.error("초기 로딩 중 오류:", e);
      // 최소한 달력은 빈 상태로라도 띄우자
      renderGroupList();
      renderUpcomingList();
      initCalendar();
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    bootstrap();
  });
})();
