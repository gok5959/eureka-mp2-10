/* =====================
   API Helper
===================== */
const apiFetch = window.authHelper.authFetch;

/* =====================
   URL 파라미터
===================== */
const params = new URLSearchParams(location.search);
const scheduleId = params.get("scheduleId");

const rawGroupId = params.get("groupId");
const groupId = rawGroupId && rawGroupId !== "null" ? Number(rawGroupId) : null;

const isGroup = !!groupId;
const isEdit = !!scheduleId;

/* =====================
   current user
===================== */
const currentUser = window.authHelper.getCurrentUser();
const currentUserId = currentUser?.id;

if (!currentUserId) {
    alert("로그인이 필요합니다.");
    throw new Error("currentUserId is missing");
}

/* =====================
   DOM
===================== */
const pageTitle = document.getElementById("pageTitle");
const scheduleForm = document.getElementById("scheduleForm");

const titleInput = document.getElementById("title");
const descriptionInput = document.getElementById("description");
const placeNameInput = document.getElementById("placeName");
const startAtInput = document.getElementById("startAt");
const endAtInput = document.getElementById("endAt");

const useVotingLabel = document.getElementById("useVotingLabel");
const useVotingCheckbox = document.getElementById("useVoting");

const votingSection = document.getElementById("votingSection");
const minParticipantsInput = document.getElementById("minParticipants");
const voteDeadlineAtInput = document.getElementById("voteDeadlineAt");

const filesInput = document.getElementById("files");

/* =====================
   UI helpers
===================== */
function show(el) {
    el && el.classList.remove("hidden");
}
function hide(el) {
    el && el.classList.add("hidden");
}

/* =====================
   초기 UI 세팅
===================== */
pageTitle.innerText = isEdit ? "일정 수정" : "일정 등록";

/**
 * 기본 원칙
 * - 개인 일정: 투표 관련 UI 전부 숨김
 * - 그룹 일정: 체크박스만 보임
 * - 투표 체크 시: votingSection 표시
 */
useVotingCheckbox.checked = false;
hide(votingSection);

if (!isGroup) {
    // 개인 일정
    hide(useVotingLabel);
    hide(votingSection);
} else {
    // 그룹 일정
    show(useVotingLabel);
    hide(votingSection);
}

useVotingCheckbox.addEventListener("change", () => {
    if (useVotingCheckbox.checked) {
        show(votingSection);
    } else {
        hide(votingSection);
    }
});

/* =====================
   수정 모드 조회
===================== */
async function loadScheduleForEdit() {
    if (!isEdit) return;

    const url = isGroup
        ? `/group-schedules/${scheduleId}`
        : `/personal-schedules/${scheduleId}`;

    try {
        const res = await apiFetch(url);
        if (!res.ok) throw new Error();

        const data = await res.json();

        titleInput.value = data.title ?? "";
        descriptionInput.value = data.description ?? "";
        placeNameInput.value = data.placeName ?? "";
        startAtInput.value = data.startAt?.slice(0, 16) ?? "";
        endAtInput.value = data.endAt?.slice(0, 16) ?? "";

        if (isGroup && String(data.status).toUpperCase() === "VOTING") {
            useVotingCheckbox.checked = true;
            show(votingSection);
            minParticipantsInput.value = data.minParticipants ?? "";
            voteDeadlineAtInput.value = data.voteDeadlineAt?.slice(0, 16) ?? "";
        }
    } catch (e) {
        alert("일정 정보를 불러오지 못했습니다.");
    }
}

loadScheduleForEdit();

/* =====================
   저장
===================== */
scheduleForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (endAtInput.value < startAtInput.value) {
        alert("종료 시간은 시작 시간 이후여야 합니다.");
        return;
    }

    const payload = {
        title: titleInput.value,
        description: descriptionInput.value,
        startAt: startAtInput.value,
        endAt: endAtInput.value,
        placeName: placeNameInput.value,
        userVoting: isGroup ? useVotingCheckbox.checked : false,
    };

    if (isGroup) payload.groupId = groupId;

    if (isGroup && useVotingCheckbox.checked) {
        payload.minParticipants = Number(minParticipantsInput.value || 0);
        payload.voteDeadlineAt = voteDeadlineAtInput.value || null;
    }

    const baseUrl = isEdit
        ? (isGroup
            ? `/group-schedules/${scheduleId}`
            : `/personal-schedules/${scheduleId}`)
        : (isGroup
            ? `/groups/${groupId}/schedules`
            : `/personal-schedules`);

    const method = isEdit ? "PUT" : "POST";

    const res = await apiFetch(
        `${baseUrl}?currentUserId=${encodeURIComponent(currentUserId)}`,
        {
            method,
            body: JSON.stringify(payload),
        }
    );

    if (!res.ok) {
        alert("일정 저장 실패");
        return;
    }

    const savedId = isEdit ? scheduleId : await res.json();

    // 첨부파일 업로드
    for (const file of filesInput.files) {
        const fd = new FormData();
        fd.append("file", file);
        fd.append("fileType", "IMAGE");

        await apiFetch(
            `/schedules/${savedId}/attachments?userId=${encodeURIComponent(currentUserId)}`,
            {
                method: "POST",
                body: fd,
            }
        );
    }

    const nextParams = new URLSearchParams();
    nextParams.set("scheduleId", savedId);
    if (isGroup) nextParams.set("groupId", String(groupId));

    location.href = `/pages/schedule-detail.html?${nextParams.toString()}`;
});


/* ======================
   메인으로 이동 버튼
====================== */
const goMainBtn = document.getElementById("goMainBtn");

if (goMainBtn) {
    goMainBtn.addEventListener("click", () => {
        location.href = "/main"; // 메인 경로
    });
}
