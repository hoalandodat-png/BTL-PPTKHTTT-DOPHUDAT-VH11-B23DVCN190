"use client";

import React, { useCallback, useEffect, useState } from "react";
import { AppShell, PortalIcons } from "@/components/AppShell";
import { Card, StatusNotice } from "@/components/UiHelpers";
import { TimetableGrid, TimetableSlot } from "@/components/TimetableGrid";
import { apiFetch, PageResponse } from "@/lib/api";

interface SemesterItem {
  id: number;
  code: string;
  name: string;
  startDate?: string;
  endDate?: string;
  isRegistrationOpen: boolean;
  minCredits: number;
  maxCredits: number;
}

interface ExamScheduleItem {
  id: number;
  sectionId: number;
  sectionCode: string;
  courseCode: string;
  courseName: string;
  credits: number;
  classroomCode: string;
  building: string;
  examDate: string;
  startTime: string;
  endTime: string;
  examFormat: string;
  note: string | null;
}

interface ScheduleItem {
  id: number;
  dayOfWeek: number;
  startPeriod: number;
  endPeriod: number;
  startWeek: number;
  endWeek: number;
  classroomCode: string;
}

interface CourseSectionItem {
  id: number;
  sectionCode: string;
  courseCode: string;
  courseName: string;
  credits: number;
  lecturerName: string | null;
  maxCapacity: number;
  currentEnrolled: number;
  status: string;
  schedules: ScheduleItem[];
}

interface EnrollmentItem {
  id: number;
  sectionId: number;
  sectionCode: string;
  courseCode: string;
  courseName: string;
  credits: number;
  status: string;
  attemptNumber: number;
  currentEnrolled: number;
  maxCapacity: number;
}

interface StudentSemesterEnrollment {
  semesterId: number;
  semesterCode: string;
  semesterName: string;
  minCredits: number;
  maxCredits: number;
  totalEnrolledCredits: number;
  belowMinCreditsWarning: boolean;
  isRegistrationOpen: boolean;
  termType: string;
  degreeLevel: string;
  items: EnrollmentItem[];
}

interface AcademicTranscript {
  studentCode: string;
  fullName: string;
  majorName: string;
  academicClassCode: string;
  cumulativeCpa4: number;
  totalAccumulatedCredits: number;
  semesters: Array<{
    semesterId: number;
    semesterCode: string;
    semesterName: string;
    registeredCredits: number;
    semesterGpa4: number;
    courses: Array<{
      enrollmentId: number;
      sectionCode: string;
      courseCode: string;
      courseName: string;
      credits: number;
      attemptNumber: number;
      gradeStatus: string;
      componentScoresByName: Record<string, number>;
      finalScore10: number | null;
      letterGrade: string | null;
      gpaScore4: number | null;
      isPassed: boolean | null;
    }>;
  }>;
}

interface CurriculumProgress {
  curriculumCode: string;
  curriculumName: string;
  totalCreditsRequired: number;
  accumulatedCredits: number;
  items: Array<{
    courseId: number;
    courseCode: string;
    courseName: string;
    credits: number;
    recommendedSemester: number;
    isRequired: boolean;
    completionStatus: string;
    bestLetterGrade: string | null;
  }>;
}

interface StudentProfile {
  studentCode: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
  nationalId: string;
  email: string;
  personalEmail: string | null;
  phone: string | null;
  address: string | null;
  avatarUrl: string | null;
  academicClassCode: string;
  majorName: string;
  facultyName: string;
  cohortYear: number;
  academicStatus: string;
}

const NAV_ITEMS = [
  { id: "enrollment", label: "Đăng ký học phần", icon: PortalIcons.ClipboardCheck },
  { id: "timetable", label: "Thời khóa biểu tuần", icon: PortalIcons.CalendarDays },
  { id: "exams", label: "Lịch thi", icon: PortalIcons.ClipboardCheck },
  { id: "results", label: "Kết quả học tập", icon: PortalIcons.Award },
  { id: "curriculum", label: "Chương trình đào tạo", icon: PortalIcons.BookOpen },
  { id: "profile", label: "Hồ sơ cá nhân", icon: PortalIcons.UserCheck },
];

export default function StudentPortalPage() {
  const [tab, setTab] = useState("enrollment");
  const [notice, setNotice] = useState<{ type: "error" | "success" | "info"; text: string } | null>(null);

  const [semesters, setSemesters] = useState<SemesterItem[]>([]);
  const [selectedSemId, setSelectedSemId] = useState<number | null>(null);
  const [sections, setSections] = useState<CourseSectionItem[]>([]);
  const [myEnroll, setMyEnroll] = useState<StudentSemesterEnrollment | null>(null);
  const [keyword, setKeyword] = useState("");
  const [busySectionId, setBusySectionId] = useState<number | null>(null);

  const [slots, setSlots] = useState<TimetableSlot[]>([]);
  const [exams, setExams] = useState<ExamScheduleItem[]>([]);
  const [transcript, setTranscript] = useState<AcademicTranscript | null>(null);
  const [curriculum, setCurriculum] = useState<CurriculumProgress | null>(null);
  const [profile, setProfile] = useState<StudentProfile | null>(null);

  const [phone, setPhone] = useState("");
  const [personalEmail, setPersonalEmail] = useState("");
  const [address, setAddress] = useState("");
  const [avatarUrl, setAvatarUrl] = useState("");

  useEffect(() => {
    apiFetch<SemesterItem[]>("/semesters")
      .then((list) => {
        setSemesters(list);
        const active = list.find((s) => s.isRegistrationOpen) || list[0];
        if (active) setSelectedSemId(active.id);
      })
      .catch(() => {});
  }, []);

  const loadEnrollmentData = useCallback(async (semId: number, kw: string) => {
    try {
      const [secPage, enrollRes] = await Promise.all([
        apiFetch<PageResponse<CourseSectionItem>>(
          `/sections?semesterId=${semId}&keyword=${encodeURIComponent(kw)}&size=50`
        ),
        apiFetch<StudentSemesterEnrollment>(`/enrollments/me?semesterId=${semId}`),
      ]);
      setSections(secPage.items);
      setMyEnroll(enrollRes);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, []);

  useEffect(() => {
    if (!selectedSemId) return;
    if (tab === "enrollment") {
      loadEnrollmentData(selectedSemId, keyword);
    } else if (tab === "timetable") {
      apiFetch<{ slots: TimetableSlot[] }>(`/schedules/me?semesterId=${selectedSemId}`)
        .then((res) => setSlots(res.slots))
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    } else if (tab === "exams") {
      apiFetch<ExamScheduleItem[]>(`/exams/me?semesterId=${selectedSemId}`)
        .then(setExams)
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    }
  }, [tab, selectedSemId, keyword, loadEnrollmentData]);

  useEffect(() => {
    if (tab === "results") {
      apiFetch<AcademicTranscript>("/students/me/academic-results")
        .then(setTranscript)
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    } else if (tab === "curriculum") {
      apiFetch<CurriculumProgress>("/students/me/curriculum-progress")
        .then(setCurriculum)
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    } else if (tab === "profile") {
      apiFetch<StudentProfile>("/students/me")
        .then((p) => {
          setProfile(p);
          setPhone(p.phone || "");
          setPersonalEmail(p.personalEmail || "");
          setAddress(p.address || "");
          setAvatarUrl(p.avatarUrl || "");
        })
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    }
  }, [tab]);

  async function handleEnroll(sectionId: number) {
    if (!selectedSemId) return;
    setNotice(null);
    setBusySectionId(sectionId);
    try {
      const res = await apiFetch<EnrollmentItem>("/enrollments", {
        method: "POST",
        body: JSON.stringify({ sectionId }),
      });
      setNotice({ type: "success", text: `Đăng ký thành công lớp ${res.sectionCode} (${res.courseName}).` });
      await loadEnrollmentData(selectedSemId, keyword);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    } finally {
      setBusySectionId(null);
    }
  }

  async function handleCancel(enrollmentId: number, sectionCode: string) {
    if (!selectedSemId) return;
    setNotice(null);
    try {
      await apiFetch(`/enrollments/${enrollmentId}`, { method: "DELETE" });
      setNotice({ type: "success", text: `Đã hủy đăng ký lớp ${sectionCode}.` });
      await loadEnrollmentData(selectedSemId, keyword);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleSaveContact(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      const updated = await apiFetch<StudentProfile>("/students/me/contact", {
        method: "PATCH",
        body: JSON.stringify({ phone, personalEmail, address, avatarUrl }),
      });
      setProfile(updated);
      setNotice({ type: "success", text: "Đã cập nhật thông tin liên lạc cá nhân." });
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  return (
    <AppShell
      requiredRole="STUDENT"
      title="Cổng thông tin Sinh viên PTIT"
      subtitle="Đăng ký tín chỉ, thời khóa biểu và kết quả học tập"
      navItems={NAV_ITEMS}
      activeTab={tab}
      onTabChange={(id) => {
        setNotice(null);
        setTab(id);
      }}
    >
      {() => (
        <div className="space-y-6">
          <StatusNotice notice={notice} onClear={() => setNotice(null)} />

          {tab === "enrollment" && (
            <div className="space-y-6">
              <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white p-4">
                <div className="flex flex-wrap items-center gap-3">
                  <label htmlFor="stu-sem" className="text-sm font-semibold text-slate-700">
                    Học kỳ:
                  </label>
                  <select
                    id="stu-sem"
                    value={selectedSemId ?? ""}
                    onChange={(e) => setSelectedSemId(Number(e.target.value))}
                    className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
                  >
                    {semesters.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.code} — {s.name} {s.isRegistrationOpen ? "(Đang mở ĐK)" : ""}
                      </option>
                    ))}
                  </select>
                </div>

                {myEnroll && (
                  <div className="flex flex-wrap items-center gap-3 text-sm">
                    <span className="rounded-lg bg-slate-100 px-3 py-1.5 font-semibold text-slate-800">
                      Đã đăng ký: {myEnroll.totalEnrolledCredits} / {myEnroll.maxCredits} TC{" "}
                      <span className="font-normal text-slate-600">
                        ({myEnroll.termType === "SUMMER"
                          ? "Học kỳ phụ (hè) · Không quy định tối thiểu · Tối đa 12 TC"
                          : `${myEnroll.degreeLevel === "ENGINEER" ? "Hệ kỹ sư" : "Hệ cử nhân"} · Tối thiểu ${myEnroll.minCredits} · Tối đa ${myEnroll.maxCredits} TC`})
                      </span>
                    </span>
                    {myEnroll.belowMinCreditsWarning && myEnroll.minCredits > 0 && (
                      <span className="rounded-lg bg-amber-50 px-3 py-1.5 text-xs font-medium text-amber-800">
                        Lưu ý: dưới mức tối thiểu {myEnroll.minCredits} TC
                      </span>
                    )}
                  </div>
                )}
              </div>

              <Card
                title="Danh sách lớp học phần mở trong kỳ"
                subtitle="Hệ thống kiểm tra tự động điều kiện tiên quyết, giới hạn tín chỉ, trùng lịch & khóa dòng đồng thời"
                action={
                  <input
                    type="search"
                    placeholder="Tìm mã môn / tên môn..."
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    className="w-64 rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
                  />
                }
              >
                <div tabIndex={0} role="region" aria-label="Danh sách lớp học phần" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2.5">Mã lớp HP</th>
                        <th className="px-3 py-2.5">Học phần</th>
                        <th className="px-3 py-2.5 text-center">TC</th>
                        <th className="px-3 py-2.5">Giảng viên</th>
                        <th className="px-3 py-2.5">Lịch học</th>
                        <th className="px-3 py-2.5 text-center">Sĩ số</th>
                        <th className="px-3 py-2.5 text-right">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {sections.map((sec) => {
                        const alreadyEnrolled = myEnroll?.items.some((i) => i.sectionId === sec.id);
                        const full = sec.currentEnrolled >= sec.maxCapacity;
                        const maxLimit = myEnroll ? myEnroll.maxCredits : 25;
                        const overMaxCredits = myEnroll ? myEnroll.totalEnrolledCredits + sec.credits > maxLimit : false;
                        return (
                          <tr key={sec.id} className="hover:bg-slate-50">
                            <td className="px-3 py-3 text-sm font-semibold tabular-nums text-red-700">
                              {sec.sectionCode}
                            </td>
                            <td className="px-3 py-3 text-sm">
                              <div className="font-semibold text-slate-900">{sec.courseName}</div>
                              <div className="text-slate-500">{sec.courseCode}</div>
                            </td>
                            <td className="px-3 py-3 text-center text-sm font-medium tabular-nums">{sec.credits}</td>
                            <td className="px-3 py-3 text-sm text-slate-700">{sec.lecturerName || "Chưa phân công"}</td>
                            <td className="px-3 py-3 text-sm text-slate-600">
                              {sec.schedules.map((sch) => (
                                <div key={sch.id}>
                                  Thứ {sch.dayOfWeek}, Tiết {sch.startPeriod}-{sch.endPeriod} (Tuần {sch.startWeek}-{sch.endWeek}) ·{" "}
                                  <strong className="text-slate-800">{sch.classroomCode}</strong>
                                </div>
                              ))}
                            </td>
                            <td className="px-3 py-3 text-center">
                              <span
                                className={`inline-block rounded-full px-2.5 py-0.5 text-sm font-semibold tabular-nums ${
                                  full ? "bg-red-100 text-red-700" : "bg-emerald-100 text-emerald-700"
                                }`}
                              >
                                {sec.currentEnrolled}/{sec.maxCapacity}
                              </span>
                            </td>
                            <td className="px-3 py-3 text-right">
                              {alreadyEnrolled ? (
                                <span className="inline-block rounded-md bg-slate-100 px-2.5 py-1 text-sm font-semibold text-slate-600">
                                  Đã đăng ký
                                </span>
                              ) : (
                                <button
                                  type="button"
                                  disabled={busySectionId === sec.id || overMaxCredits}
                                  title={overMaxCredits ? `Vượt quá giới hạn tối đa ${maxLimit} tín chỉ/học kỳ` : undefined}
                                  onClick={() => handleEnroll(sec.id)}
                                  className="rounded-lg bg-red-700 px-3 py-1.5 text-sm font-semibold text-white hover:bg-red-800 disabled:opacity-50"
                                >
                                  {busySectionId === sec.id
                                    ? "Đang xử lý..."
                                    : overMaxCredits
                                    ? `Vượt ${maxLimit} TC`
                                    : "Đăng ký"}
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </Card>

              <Card title="Học phần đã đăng ký trong kỳ">
                {myEnroll && myEnroll.items.length > 0 ? (
                  <div tabIndex={0} role="region" aria-label="Lớp đã đăng ký" className="table-wrapper">
                    <table className="w-full text-left text-sm">
                      <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                        <tr>
                          <th className="px-3 py-2">Mã lớp HP</th>
                          <th className="px-3 py-2">Học phần</th>
                          <th className="px-3 py-2 text-center">TC</th>
                          <th className="px-3 py-2 text-center">Lần học</th>
                          <th className="px-3 py-2 text-right">Thao tác</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {myEnroll.items.map((item) => (
                          <tr key={item.id} className="hover:bg-slate-50">
                            <td className="px-3 py-3 text-sm font-semibold tabular-nums text-red-700">{item.sectionCode}</td>
                            <td className="px-3 py-3 text-sm font-medium text-slate-900">
                              {item.courseCode} — {item.courseName}
                            </td>
                            <td className="px-3 py-3 text-center text-sm tabular-nums">{item.credits}</td>
                            <td className="px-3 py-3 text-center text-sm tabular-nums">Lần {item.attemptNumber}</td>
                            <td className="px-3 py-3 text-right">
                              <button
                                type="button"
                                onClick={() => handleCancel(item.id, item.sectionCode)}
                                className="rounded-lg border border-red-200 bg-red-50 px-3 py-1.5 text-sm font-semibold text-red-700 hover:bg-red-100"
                              >
                                Hủy đăng ký
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <p className="text-sm text-slate-500">Bạn chưa đăng ký học phần nào trong học kỳ này.</p>
                )}
              </Card>
            </div>
          )}

          {tab === "timetable" && (
            <div className="space-y-4">
              <div className="flex flex-wrap items-center gap-4 rounded-xl border border-slate-200 bg-white p-4">
                <div className="flex items-center gap-2">
                  <label htmlFor="tt-sem" className="text-sm font-semibold">Học kỳ:</label>
                  <select
                    id="tt-sem"
                    value={selectedSemId ?? ""}
                    onChange={(e) => setSelectedSemId(Number(e.target.value))}
                    className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
                  >
                    {semesters.map((s) => (
                      <option key={s.id} value={s.id}>{s.code} — {s.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              {(() => {
                const activeSem = semesters.find((s) => s.id === selectedSemId);
                return (
                  <TimetableGrid
                    slots={slots}
                    semesterStartDate={activeSem?.startDate}
                    semesterEndDate={activeSem?.endDate}
                  />
                );
              })()}
            </div>
          )}

          {tab === "exams" && (
            <div className="space-y-4">
              <div className="flex flex-wrap items-center gap-4 rounded-xl border border-slate-200 bg-white p-4">
                <div className="flex items-center gap-2">
                  <label htmlFor="exam-sem" className="text-sm font-semibold">Học kỳ:</label>
                  <select
                    id="exam-sem"
                    value={selectedSemId ?? ""}
                    onChange={(e) => setSelectedSemId(Number(e.target.value))}
                    className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
                  >
                    {semesters.map((s) => (
                      <option key={s.id} value={s.id}>{s.code} — {s.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              <Card
                title="Lịch thi kết thúc học phần của tôi"
                subtitle="Hiển thị lịch thi các lớp học phần bạn đã đăng ký trong học kỳ"
              >
                {exams.length > 0 ? (
                  <div tabIndex={0} role="region" aria-label="Lịch thi của sinh viên" className="table-wrapper">
                    <table className="w-full text-left text-sm">
                      <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                        <tr>
                          <th className="px-3 py-2.5">Mã lớp HP</th>
                          <th className="px-3 py-2.5">Học phần</th>
                          <th className="px-3 py-2.5 text-center">Ngày thi</th>
                          <th className="px-3 py-2.5 text-center">Giờ thi</th>
                          <th className="px-3 py-2.5 text-center">Phòng thi</th>
                          <th className="px-3 py-2.5">Hình thức</th>
                          <th className="px-3 py-2.5">Ghi chú</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {exams.map((ex) => (
                          <tr key={ex.id} className="hover:bg-slate-50">
                            <td className="px-3 py-3 text-sm font-semibold tabular-nums text-red-700">
                              {ex.sectionCode}
                            </td>
                            <td className="px-3 py-3 text-sm font-medium text-slate-900">
                              {ex.courseCode} — {ex.courseName}
                            </td>
                            <td className="px-3 py-3 text-center text-sm font-semibold tabular-nums text-slate-800">{ex.examDate}</td>
                            <td className="px-3 py-3 text-center text-sm font-medium tabular-nums text-slate-800">
                              {ex.startTime.slice(0, 5)} – {ex.endTime.slice(0, 5)}
                            </td>
                            <td className="px-3 py-3 text-center text-sm font-semibold text-slate-800">
                              {ex.classroomCode} ({ex.building})
                            </td>
                            <td className="px-3 py-3 text-sm text-slate-700">{ex.examFormat}</td>
                            <td className="px-3 py-3 text-sm text-slate-600">{ex.note || "—"}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <p className="text-sm text-slate-500">
                    Chưa có lịch thi nào cho các học phần bạn đã đăng ký trong học kỳ này.
                  </p>
                )}
              </Card>
            </div>
          )}

          {tab === "results" && transcript && (
            <div className="space-y-6">
              <div className="grid gap-4 sm:grid-cols-3">
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                  <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Sinh viên</p>
                  <p className="mt-1.5 text-2xl font-bold text-slate-900">{transcript.fullName}</p>
                  <p className="mt-1 text-sm text-slate-500">
                    {transcript.studentCode} · {transcript.academicClassCode}
                  </p>
                </div>
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                  <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">CPA tích lũy (Hệ 4)</p>
                  <p className="mt-1.5 text-2xl font-bold tabular-nums text-red-700">{transcript.cumulativeCpa4.toFixed(2)}</p>
                  <p className="mt-1 text-sm text-slate-500">Học lại lấy điểm cao nhất</p>
                </div>
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                  <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Tín chỉ tích lũy</p>
                  <p className="mt-1.5 text-2xl font-bold tabular-nums text-slate-900">{transcript.totalAccumulatedCredits}</p>
                  <p className="mt-1 text-sm text-slate-500">Ngành {transcript.majorName}</p>
                </div>
              </div>

              {transcript.semesters.map((sem) => (
                <Card
                  key={sem.semesterId}
                  title={`${sem.semesterCode} — ${sem.semesterName}`}
                  subtitle={`Tổng tín chỉ ĐK: ${sem.registeredCredits} · GPA học kỳ: ${sem.semesterGpa4.toFixed(2)}`}
                >
                  <div tabIndex={0} role="region" aria-label={`Bảng điểm ${sem.semesterCode}`} className="table-wrapper">
                    <table className="w-full text-left text-sm">
                      <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                        <tr>
                          <th className="px-3 py-2.5">Mã môn</th>
                          <th className="px-3 py-2.5">Tên học phần</th>
                          <th className="px-3 py-2.5 text-center">TC</th>
                          <th className="px-3 py-2.5">Điểm thành phần</th>
                          <th className="px-3 py-2.5 text-center">Hệ 10</th>
                          <th className="px-3 py-2.5 text-center">Chữ</th>
                          <th className="px-3 py-2.5 text-center">Hệ 4</th>
                          <th className="px-3 py-2.5 text-center">Trạng thái</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {sem.courses.map((c) => (
                          <tr key={c.enrollmentId} className="hover:bg-slate-50">
                            <td className="px-3 py-3 text-sm font-semibold tabular-nums text-red-700">{c.courseCode}</td>
                            <td className="px-3 py-3 text-sm font-medium text-slate-900">{c.courseName}</td>
                            <td className="px-3 py-3 text-center text-sm tabular-nums">{c.credits}</td>
                            <td className="px-3 py-3 text-sm text-slate-600">
                              {c.gradeStatus === "DRAFT" ? (
                                <span className="italic text-slate-400">Chưa công bố</span>
                              ) : (
                                Object.entries(c.componentScoresByName)
                                  .map(([k, v]) => `${k}: ${v}`)
                                  .join(" | ")
                              )}
                            </td>
                            <td className="px-3 py-3 text-center text-sm font-semibold tabular-nums">
                              {c.finalScore10 !== null ? c.finalScore10.toFixed(1) : "—"}
                            </td>
                            <td className="px-3 py-3 text-center text-sm font-bold">{c.letterGrade || "—"}</td>
                            <td className="px-3 py-3 text-center text-sm tabular-nums">
                              {c.gpaScore4 !== null ? c.gpaScore4.toFixed(2) : "—"}
                            </td>
                            <td className="px-3 py-3 text-center">
                              {c.gradeStatus === "DRAFT" ? (
                                <span className="inline-block rounded-full bg-slate-100 px-3 py-0.5 text-sm font-medium text-slate-600">Chưa công bố</span>
                              ) : c.isPassed ? (
                                <span className="inline-block rounded-full bg-emerald-100 px-3 py-0.5 text-sm font-semibold text-emerald-700">Đạt</span>
                              ) : (
                                <span className="inline-block rounded-full bg-red-100 px-3 py-0.5 text-sm font-semibold text-red-700">Không đạt</span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </Card>
              ))}
            </div>
          )}

          {tab === "curriculum" && curriculum && (
            <Card
              title={`${curriculum.curriculumCode} — ${curriculum.curriculumName}`}
              subtitle={`Tiến độ tích lũy: ${curriculum.accumulatedCredits} / ${curriculum.totalCreditsRequired} tín chỉ`}
            >
              <div tabIndex={0} role="region" aria-label="Tiến độ chương trình đào tạo" className="table-wrapper">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                    <tr>
                      <th className="px-3 py-2.5">Kỳ KN</th>
                      <th className="px-3 py-2.5">Mã môn</th>
                      <th className="px-3 py-2.5">Tên học phần</th>
                      <th className="px-3 py-2.5 text-center">TC</th>
                      <th className="px-3 py-2.5 text-center">Loại</th>
                      <th className="px-3 py-2.5 text-center">Trạng thái</th>
                      <th className="px-3 py-2.5 text-center">Điểm cao nhất</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {curriculum.items.map((item) => (
                      <tr key={item.courseId} className="hover:bg-slate-50">
                        <td className="px-3 py-3 text-sm font-medium text-slate-700">Kỳ {item.recommendedSemester}</td>
                        <td className="px-3 py-3 text-sm font-semibold tabular-nums text-red-700">{item.courseCode}</td>
                        <td className="px-3 py-3 text-sm font-medium text-slate-900">{item.courseName}</td>
                        <td className="px-3 py-3 text-center text-sm tabular-nums">{item.credits}</td>
                        <td className="px-3 py-3 text-center text-sm text-slate-700">
                          {item.isRequired ? "Bắt buộc" : "Tự chọn"}
                        </td>
                        <td className="px-3 py-3 text-center">
                          <span
                            className={`inline-block rounded-full px-3 py-0.5 text-sm font-semibold ${
                              item.completionStatus === "PASSED"
                                ? "bg-emerald-100 text-emerald-700"
                                : item.completionStatus === "ENROLLED"
                                ? "bg-sky-100 text-sky-700"
                                : item.completionStatus === "FAILED"
                                ? "bg-red-100 text-red-700"
                                : "bg-slate-100 text-slate-600"
                            }`}
                          >
                            {item.completionStatus === "PASSED"
                              ? "Đã qua"
                              : item.completionStatus === "ENROLLED"
                              ? "Đang học"
                              : item.completionStatus === "FAILED"
                              ? "Chưa đạt"
                              : "Chưa học"}
                          </span>
                        </td>
                        <td className="px-3 py-3 text-center text-sm font-bold">{item.bestLetterGrade || "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}

          {tab === "profile" && profile && (
            <div className="grid gap-6 lg:grid-cols-2">
              <Card title="Thông tin học vụ (Chỉ Phòng Đào tạo được chỉnh sửa)">
                <dl className="grid grid-cols-2 gap-3 text-sm">
                  <div><dt className="text-xs text-slate-500">Mã sinh viên</dt><dd className="font-semibold">{profile.studentCode}</dd></div>
                  <div><dt className="text-xs text-slate-500">Họ và tên</dt><dd className="font-semibold">{profile.fullName}</dd></div>
                  <div><dt className="text-xs text-slate-500">Ngày sinh</dt><dd>{profile.dateOfBirth}</dd></div>
                  <div><dt className="text-xs text-slate-500">Số CCCD</dt><dd>{profile.nationalId}</dd></div>
                  <div><dt className="text-xs text-slate-500">Lớp hành chính</dt><dd className="font-semibold">{profile.academicClassCode}</dd></div>
                  <div><dt className="text-xs text-slate-500">Ngành đào tạo</dt><dd>{profile.majorName}</dd></div>
                  <div><dt className="text-xs text-slate-500">Khóa</dt><dd>Khóa {profile.cohortYear}</dd></div>
                  <div><dt className="text-xs text-slate-500">Trạng thái học vụ</dt><dd className="font-bold text-emerald-700">{profile.academicStatus}</dd></div>
                </dl>
              </Card>

              <Card title="Tự cập nhật thông tin liên lạc (BR-09)">
                <form onSubmit={handleSaveContact} className="space-y-4">
                  <div>
                    <label htmlFor="p-phone" className="mb-1 block text-sm font-medium text-slate-700">Số điện thoại</label>
                    <input
                      id="p-phone"
                      type="tel"
                      autoComplete="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    />
                  </div>
                  <div>
                    <label htmlFor="p-email" className="mb-1 block text-sm font-medium text-slate-700">Email cá nhân</label>
                    <input
                      id="p-email"
                      type="email"
                      autoComplete="email"
                      value={personalEmail}
                      onChange={(e) => setPersonalEmail(e.target.value)}
                      className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    />
                  </div>
                  <div>
                    <label htmlFor="p-addr" className="mb-1 block text-sm font-medium text-slate-700">Địa chỉ liên hệ</label>
                    <textarea
                      id="p-addr"
                      rows={2}
                      value={address}
                      onChange={(e) => setAddress(e.target.value)}
                      className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    />
                  </div>
                  <div>
                    <label htmlFor="p-avatar" className="mb-1 block text-sm font-medium text-slate-700">URL Ảnh đại diện</label>
                    <input
                      id="p-avatar"
                      type="url"
                      value={avatarUrl}
                      onChange={(e) => setAvatarUrl(e.target.value)}
                      className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    />
                  </div>
                  <button
                    type="submit"
                    className="rounded-lg bg-red-700 px-4 py-2 text-sm font-semibold text-white hover:bg-red-800"
                  >
                    Lưu thông tin liên lạc
                  </button>
                </form>
              </Card>
            </div>
          )}
        </div>
      )}
    </AppShell>
  );
}
