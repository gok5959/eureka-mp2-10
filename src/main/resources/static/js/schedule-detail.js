const apiFetch = window.authHelper.authFetch;

/* ======================
   URL 파라미터
====================== */
const params = new URLSearchParams(location.search);
const scheduleId = params.get("scheduleId");

const rawGroupId = params.get("groupId");
const groupId = rawGroupId && rawGroupId !== "null" ? Number(rawGroupId) : null;
const type = groupId ? "group" : "personal";

/* ======================
   로그인 유저
====================== */
const currentUser = window.authHelper.getCurrentUser();
const currentUserId = currentUser?.id;

if (!currentUserId) {
    alert("로그인이 필요합니다.");
    throw new Error("currentUserId missing");
}

/* ======================
   DOM
====================== */
const titleEl = document.getElementById("title");
const timeEl = document.getElementById("time");
const placeEl = document.getElementById("place");
const statusBadge = document.getElementById("statusBadge");
const descriptionEl = document.getElementById("description");

const actionButtons = document.getElementById("actionButtons");
const attachmentsEl = document.getElementById("attachments");
const commentsEl = document.getElementById("comments");
const commentInput = document.getElementById("commentInput");
const commentBtn = document.getElementById("commentBtn");

/* 이미지 모달 */
const modal = document.getElementById("imageModal");
const modalImage = document.getElementById("modalImage");
const closeModal = document.getElementById("closeModal");

/* ======================
   상세 조회
====================== */
const detailUrl =
    type === "group"
        ? `/group-schedules/${scheduleId}`
        : `/personal-schedules/${scheduleId}`;

apiFetch(detailUrl)
    .then(res => res.json())
    .then(renderSchedule)
    .catch(() => alert("일정 조회 실패"));

function renderSchedule(data) {

    console.log("📌 schedule detail data:", data); // ✅ 여기!!!!!

    titleEl.innerText = data.title;
    descriptionEl.innerText = data.description || "";
    timeEl.innerText = `${fmt(data.startAt)} ~ ${fmt(data.endAt)}`;
    placeEl.innerText = data.placeName ? `📍 ${data.placeName}` : "";
    statusBadge.innerText = data.status;

    /* 🔥 게시글 작성자만 수정/삭제 */
    if (data.ownerId === currentUserId) {
        renderActions();
    }

    // ✅ 여기만 중요
    if (type === "group" && data.voteDeadlineAt) {
        initVoting(data);
    } else {
        document.getElementById("votingBox")?.classList.add("hidden");
    }

    renderAttachments(data.attachments || [], data.ownerId);
    renderComments(data.comments || []);
}

/* ======================
   게시글 수정 / 삭제
====================== */
function renderActions() {
    const editBtn = document.createElement("button");
    editBtn.innerText = "수정";
    editBtn.onclick = () => {
        location.href =
            `/pages/schedule-form.html?scheduleId=${scheduleId}&groupId=${groupId}`;
    };

    const delBtn = document.createElement("button");
    delBtn.innerText = "삭제";
    delBtn.onclick = deleteSchedule;

    actionButtons.append(editBtn, delBtn);
}

async function deleteSchedule() {
    if (!confirm("일정을 삭제할까요?")) return;

    const url =
        type === "group"
            ? `/group-schedules/${scheduleId}`
            : `/personal-schedules/${scheduleId}`;

    const res = await apiFetch(url, { method: "DELETE" });
    if (res.ok) {
        alert("삭제 완료");
        history.back();
    } else {
        alert("삭제 실패");
    }
}

/* ======================
   첨부파일 + 이미지 모달
====================== */
function renderAttachments(list, ownerId) {
    attachmentsEl.innerHTML = "";

    if (list.length === 0) {
        attachmentsEl.innerHTML = "<li>첨부파일 없음</li>";
        return;
    }

    list.forEach(a => {
        const li = document.createElement("li");

        if (a.contentType?.startsWith("image")) {
            const img = document.createElement("img");
            img.src = a.fileUrl;
            img.style.maxWidth = "200px";
            img.style.cursor = "pointer";
            img.onclick = () => {
                modalImage.src = a.fileUrl;
                modal.classList.remove("hidden");
            };
            li.appendChild(img);
        }

        const link = document.createElement("span");
        link.innerText = a.originalName;
        li.appendChild(link);

        /* 🔥 작성자만 삭제 */
        if (ownerId === currentUserId) {
            const delBtn = document.createElement("button");
            delBtn.innerText = "삭제";
            delBtn.style.marginLeft = "8px";
            delBtn.onclick = () => deleteAttachment(a.id, li);
            li.appendChild(delBtn);
        }

        attachmentsEl.appendChild(li);
    });
}

closeModal.onclick = () => {
    modal.classList.add("hidden");
    modalImage.src = "";
};

async function deleteAttachment(attachmentId, li) {
    if (!confirm("첨부파일을 삭제할까요?")) return;

    const res = await apiFetch(
        `/schedules/${scheduleId}/attachments/${attachmentId}?userId=${currentUserId}`,
        { method: "DELETE" }
    );

    if (res.ok) li.remove();
    else alert("첨부파일 삭제 실패");
}

/*====
투표 초기화 함수
 */
async function initVoting(schedule) {
    const votingBox = document.getElementById("votingBox");
    if (!votingBox) return;

    // ❗ voteDeadlineAt 없으면 투표 아님
    if (!schedule.voteDeadlineAt) {
        votingBox.classList.add("hidden");
        return;
    }

    votingBox.classList.remove("hidden");

    document.getElementById("voteDeadline").innerText =
        new Date(schedule.voteDeadlineAt).toLocaleString();

    await loadParticipants(schedule.id);

    const btn = document.getElementById("voteActionBtn");

    if (schedule.status !== "VOTING") {
        btn.classList.add("hidden");
        return;
    }

    btn.classList.remove("hidden");

    const my = await getMyParticipation(schedule.id);
    btn.innerText = my?.status === "ACCEPTED" ? "참여 취소" : "참여하기";

    btn.onclick = async () => {
        try {
            const status = my?.status === "ACCEPTED"
                ? "DECLINED"
                : "ACCEPTED";

            await vote(schedule.id, status);
            location.reload();
        } catch {
            alert("투표할 수 없는 상태입니다.");
        }
    };
}


/*==========
내 참여상태 조회
 */
async function getMyParticipation(scheduleId) {
    try {
        const res = await apiFetch(
            `/group-schedules/${scheduleId}/participations/me?userId=${currentUserId}`
        );
        return await res.json();
    } catch {
        return null; // 아직 투표 안 한 경우
    }
}


/* ============
참여/취소 api
 */
async function vote(scheduleId, status) {
    await apiFetch(
        `/group-schedules/${scheduleId}/participations?userId=${currentUserId}`,
        {
            method: "POST",
            body: JSON.stringify({ status })
        }
    );
}


/*=========
참여자 목록 로드
 */
async function loadParticipants(scheduleId) {
    const ul = document.getElementById("participantList");
    ul.innerHTML = "";

    const list = await apiFetch(
        `/group-schedules/${scheduleId}/participations/list`
    ).then(res => res.json());

    if (!list.length) {
        ul.innerHTML = `<li class="list-group-item text-muted">참여자 없음</li>`;
        return;
    }

    list.forEach(p => {
        const li = document.createElement("li");
        li.className = "list-group-item d-flex justify-content-between";
        li.innerHTML = `
          <span>사용자 ${p.userId}</span>
          <span class="badge bg-primary">${p.status}</span>
        `;
        ul.appendChild(li);
    });
}



/* ======================
   댓글
====================== */
function renderComments(list) {
    commentsEl.innerHTML = "";

    if (list.length === 0) {
        commentsEl.innerHTML = "<div>댓글 없음</div>";
        return;
    }

    list.forEach(c => {
        const wrapper = document.createElement("div");
        wrapper.style.borderBottom = "1px solid #ddd";
        wrapper.style.padding = "6px 0";

        const contentSpan = document.createElement("span");
        contentSpan.innerText = c.content;
        wrapper.appendChild(contentSpan);

        /* 🔥 댓글 작성자만 수정/삭제 */
        if (c.userId === currentUserId) {
            const editBtn = document.createElement("button");
            editBtn.innerText = "수정";
            editBtn.style.marginLeft = "8px";

            const delBtn = document.createElement("button");
            delBtn.innerText = "삭제";
            delBtn.style.marginLeft = "4px";

            editBtn.onclick = () => {
                const textarea = document.createElement("textarea");
                textarea.value = c.content;
                textarea.rows = 2;

                const saveBtn = document.createElement("button");
                saveBtn.innerText = "저장";

                saveBtn.onclick = async () => {
                    const newContent = textarea.value.trim();
                    if (!newContent) return;

                    const res = await apiFetch(
                        `/schedules/${scheduleId}/comments/${c.id}?userId=${currentUserId}`,
                        {
                            method: "PATCH",
                            body: JSON.stringify({ content: newContent })
                        }
                    );

                    if (res.ok) {
                        location.reload();
                    } else {
                        alert("댓글 수정 실패");
                    }
                };

                wrapper.innerHTML = "";
                wrapper.append(textarea, saveBtn);
            };

            delBtn.onclick = () => deleteComment(c.id);

            wrapper.append(editBtn, delBtn);
        }

        commentsEl.appendChild(wrapper);
    });
}


/* 댓글 등록 */
commentBtn.onclick = async () => {
    const content = commentInput.value.trim();
    if (!content) return;

    const res = await apiFetch(
        `/schedules/${scheduleId}/comments?userId=${currentUserId}`,
        {
            method: "POST",
            body: JSON.stringify({ content })
        }
    );

    if (res.ok) {
        commentInput.value = "";
        location.reload();
    } else {
        alert("댓글 등록 실패");
    }
};

async function deleteComment(commentId) {
    if (!confirm("댓글을 삭제할까요?")) return;

    const res = await apiFetch(
        `/schedules/${scheduleId}/comments/${commentId}?userId=${currentUserId}`,
        { method: "DELETE" }
    );

    if (res.ok) location.reload();
    else alert("댓글 삭제 실패");
}

/* ======================
   util
====================== */
function fmt(dt) {
    return dt ? dt.replace("T", " ").slice(0, 16) : "";
}

