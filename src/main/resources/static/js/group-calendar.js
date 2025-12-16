(() => {
  const $ = (id) => document.getElementById(id);

  const CURRENT_USER_ID = Number(document.querySelector('meta[name="current-user-id"]')?.content || "1");
  const groupIdMeta = document.querySelector('meta[name="group-id"]');
	const GROUP_ID = Number(groupIdMeta?.getAttribute("content") ?? "0");
	
	console.log("GROUP_ID meta:", groupIdMeta?.outerHTML);
	console.log("GROUP_ID parsed:", GROUP_ID);


  const state = {
    groupId: GROUP_ID,
    groupName: "그룹",
    members: [],
    schedules: [],
    calendar: null,
  };

  async function fetchJson(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`${res.status} ${url}`);
    return res.json();
  }

  const pick = (obj, keys) => {
    for (const k of keys) if (obj && obj[k] != null) return obj[k];
    return null;
  };

  function normalizeScheduleDto(dto) {
    const id = pick(dto, ["id", "scheduleId"]);
    const title = pick(dto, ["title", "name", "scheduleTitle"]) || "(제목 없음)";
    const startAt = pick(dto, ["startAt", "start", "start_at"]) || null;
    const endAt = pick(dto, ["endAt", "end", "end_at"]) || startAt;
    if (!id || !startAt) return null;
    return { id: String(id), title, startAt, endAt };
  }

  async function loadGroupDetail() {
    // ✅ 기존 API 활용 (GroupController.getGroupDetail)
    const data = await fetchJson(`/groups/${state.groupId}?currentUserId=${CURRENT_USER_ID}`);

    // groupName 키는 너희 DTO에 맞게 pick
    state.groupName = pick(data, ["groupName", "name", "title"]) || "그룹";
    $("groupTitle").textContent = state.groupName;

    // 멤버 리스트도 내려준다면 여기서 파싱
    const members = pick(data, ["members", "memberList", "groupMembers"]) || [];
    state.members = Array.isArray(members) ? members : [];
  }

  async function loadGroupSchedules() {
    const data = await fetchJson(`/groups/${state.groupId}/schedules`);
    const list = Array.isArray(data) ? data : data.content || data.data || [];
    state.schedules = list.map(normalizeScheduleDto).filter(Boolean);
  }

  function renderMembers() {
    const ul = $("memberList");
    ul.innerHTML = "";

    if (!state.members.length) {
      const li = document.createElement("li");
      li.className = "list-group-item text-muted";
      li.textContent = "멤버 정보가 없습니다. (group detail 응답에 멤버 리스트가 포함되어야 함)";
      ul.appendChild(li);
      return;
    }

    state.members.forEach(m => {
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
    ul.innerHTML = "";

    const now = new Date();
    const upcoming = state.schedules
      .filter(s => new Date(s.startAt) >= now)
      .sort((a, b) => new Date(a.startAt) - new Date(b.startAt))
      .slice(0, 10);

    if (!upcoming.length) {
      const li = document.createElement("li");
      li.className = "list-group-item text-muted";
      li.textContent = "다가오는 일정이 없습니다.";
      ul.appendChild(li);
      return;
    }

    upcoming.forEach(s => {
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

  function initCalendar() {
    const el = $("calendar");
    state.calendar = new FullCalendar.Calendar(el, {
      initialView: "dayGridMonth",
      height: "auto",
      headerToolbar: { left: "prev,next today", center: "title", right: "dayGridMonth,timeGridWeek,timeGridDay" },
      events: state.schedules.map(s => ({ id: s.id, title: s.title, start: s.startAt, end: s.endAt })),
      eventClick: (info) => window.location.href = `/group-schedules/${info.event.id}`,
    });
    state.calendar.render();
  }

  async function bootstrap() {
    try {
      await loadGroupDetail();
      await loadGroupSchedules();
      renderMembers();
      renderUpcomingSchedules();
      initCalendar();
    } catch (e) {
      console.error(e);
      $("groupTitle").textContent = "그룹(로딩 실패)";
      initCalendar();
    }
  }

  document.addEventListener("DOMContentLoaded", bootstrap);
})();
