"use client";

import React, { useCallback, useEffect, useState } from "react";
import { AppShell, PortalIcons } from "@/components/AppShell";
import { Card, StatusNotice } from "@/components/UiHelpers";
import { TimetableGrid, TimetableSlot } from "@/components/TimetableGrid";
import { apiFetch, PageResponse } from "@/lib/api";

interface SemesterItem { id: number; code: string; name: string; startDate?: string; endDate?: string; isRegistrationOpen?: boolean; status?: string; }
interface AcademicClassItem { id: number; code: string; name: string; majorId: number; majorName: string; cohortYear: number; }
interface StudentSummaryItem { id: number; studentCode: string; fullName: string; email: string; academicClassCode: string; majorName: string; academicStatus: string; }
interface CourseSectionItem {
  id: number;
  sectionCode: string;
  courseCode: string;
  courseName: string;
  credits: number;
  lecturerId: number | null;
  lecturerName: string | null;
  currentEnrolled: number;
  maxCapacity: number;
  gradeStatus: string;
}

interface SectionGradeBook {
  sectionId: number;
  sectionCode: string;
  courseCode: string;
  courseName: string;
  gradeStatus: string;
  components: Array<{
    id: number;
    code: string;
    name: string;
    weightPercent: number;
    isFinalExam: boolean;
    minThreshold: number;
  }>;
  rows: Array<{
    enrollmentId: number;
    studentCode: string;
    fullName: string;
    academicClassCode: string;
    componentScores: Record<number, number>;
    finalScore10: number | null;
    letterGrade: string | null;
    gpaScore4: number | null;
    isPassed: boolean | null;
  }>;
}

interface GradeDistReport {
  totalGraded: number;
  passedCount: number;
  failedCount: number;
  passRatePercent: number;
  distribution: Array<{ label: string; count: number }>;
}

const NAV_ITEMS = [
  { id: "sections", label: "Lớp giảng dạy & Nhập điểm", icon: PortalIcons.Award },
  { id: "timetable", label: "Thời khóa biểu tuần", icon: PortalIcons.CalendarDays },
  { id: "students", label: "Thêm tài khoản Sinh viên", icon: PortalIcons.UserCheck },
  { id: "reports", label: "Thống kê phổ điểm", icon: PortalIcons.BarChart3 },
];

export default function LecturerPortalPage() {
  const [tab, setTab] = useState("sections");
  const [notice, setNotice] = useState<{ type: "error" | "success" | "info"; text: string } | null>(null);
  const [semesters, setSemesters] = useState<SemesterItem[]>([]);
  const [semId, setSemId] = useState<number | null>(null);
  const [sections, setSections] = useState<CourseSectionItem[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [gradeBook, setGradeBook] = useState<SectionGradeBook | null>(null);
  const [draftScores, setDraftScores] = useState<Record<string, string>>({});
  const [slots, setSlots] = useState<TimetableSlot[]>([]);
  const [distReport, setDistReport] = useState<GradeDistReport | null>(null);
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [classes, setClasses] = useState<AcademicClassItem[]>([]);
  const [students, setStudents] = useState<StudentSummaryItem[]>([]);
  const [stuForm, setStuForm] = useState({
    studentCode: "",
    fullName: "",
    dateOfBirth: "2004-01-15",
    gender: "MALE",
    nationalId: "",
    email: "",
    academicClassId: "",
    initialPassword: "",
  });

  useEffect(() => {
    apiFetch<SemesterItem[]>("/semesters").then((list) => {
      setSemesters(list);
      const preferred = list.find((s) => s.isRegistrationOpen || s.status === "ONGOING") || list[0];
      if (preferred) setSemId(preferred.id);
    });
  }, []);

  const loadLecturerSections = useCallback(async (semesterId: number) => {
    try {
      const [secRes, ttRes] = await Promise.all([
        apiFetch<PageResponse<CourseSectionItem>>(`/sections?semesterId=${semesterId}&size=100`),
        apiFetch<{ slots: TimetableSlot[] }>(`/schedules/me?semesterId=${semesterId}`),
      ]);
      const mySecIds = new Set(ttRes.slots.map((s) => s.sectionId));
      const mySections = secRes.items.filter((s) => mySecIds.has(s.id));
      setSections(mySections);
      setSlots(ttRes.slots);
      if (mySections.length > 0 && !selectedSectionId) {
        setSelectedSectionId(mySections[0].id);
      }
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, [selectedSectionId]);

  const loadGradeBook = useCallback(async (sectionId: number) => {
    try {
      const gb = await apiFetch<SectionGradeBook>(`/sections/${sectionId}/grades`);
      setGradeBook(gb);
      const map: Record<string, string> = {};
      for (const row of gb.rows) {
        for (const comp of gb.components) {
          const val = row.componentScores[comp.id];
          map[`${row.enrollmentId}-${comp.id}`] = val !== undefined && val !== null ? String(val) : "";
        }
      }
      setDraftScores(map);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, []);

  const loadStudentsTab = useCallback(async () => {
    try {
      const [clsList, stuPage] = await Promise.all([
        apiFetch<AcademicClassItem[]>("/academic-classes"),
        apiFetch<PageResponse<StudentSummaryItem>>("/students?size=50"),
      ]);
      setClasses(clsList);
      setStudents(stuPage.items);
      setStuForm((prev) => ({
        ...prev,
        academicClassId: prev.academicClassId || String(clsList[0]?.id || ""),
      }));
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, []);

  useEffect(() => {
    if (tab === "students") {
      loadStudentsTab();
      return;
    }
    if (!semId) return;
    if (tab === "sections" || tab === "timetable") {
      loadLecturerSections(semId);
    } else if (tab === "reports") {
      apiFetch<GradeDistReport>(`/reports/grade-distribution?semesterId=${semId}`)
        .then(setDistReport)
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    }
  }, [tab, semId, loadLecturerSections, loadStudentsTab]);

  useEffect(() => {
    if (selectedSectionId && tab === "sections") {
      loadGradeBook(selectedSectionId);
    }
  }, [selectedSectionId, tab, loadGradeBook]);

  async function handleSaveGrades(e: React.FormEvent) {
    e.preventDefault();
    if (!gradeBook) return;
    setNotice(null);
    const rows = gradeBook.rows
      .map((r) => {
        const scores = gradeBook.components
          .map((c) => {
            const raw = draftScores[`${r.enrollmentId}-${c.id}`];
            if (raw === undefined || raw.trim() === "") return null;
            return { gradeComponentId: c.id, score: Number(raw) };
          })
          .filter(Boolean);
        return scores.length > 0 ? { enrollmentId: r.enrollmentId, scores } : null;
      })
      .filter(Boolean);

    if (rows.length === 0) {
      setNotice({ type: "error", text: "Vui lòng nhập ít nhất một điểm thành phần trước khi lưu." });
      return;
    }

    try {
      const updated = await apiFetch<SectionGradeBook>(`/sections/${gradeBook.sectionId}/grades`, {
        method: "PUT",
        body: JSON.stringify({ rows }),
      });
      setGradeBook(updated);
      setNotice({ type: "success", text: "Đã lưu bảng điểm nháp và tự động tính điểm tổng kết." });
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handlePublish() {
    if (!gradeBook) return;
    setNotice(null);
    try {
      const updated = await apiFetch<SectionGradeBook>(`/sections/${gradeBook.sectionId}/grades/publish`, {
        method: "POST",
      });
      setGradeBook(updated);
      setNotice({ type: "success", text: `Đã công bố bảng điểm lớp ${updated.sectionCode}.` });
      if (semId) await loadLecturerSections(semId);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleImportCsv() {
    if (!gradeBook || !csvFile) return;
    setNotice(null);
    const fd = new FormData();
    fd.append("file", csvFile);
    try {
      const res = await apiFetch<{ updatedCount: number }>(`/sections/${gradeBook.sectionId}/grades/import?dryRun=false`, {
        method: "POST",
        body: fd,
      });
      setNotice({ type: "success", text: `Đã import điểm cho ${res.updatedCount} sinh viên.` });
      await loadGradeBook(gradeBook.sectionId);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateStudent(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    const cls = classes.find((c) => c.id === Number(stuForm.academicClassId));
    if (!cls) {
      setNotice({ type: "error", text: "Vui lòng chọn lớp hành chính hợp lệ." });
      return;
    }
    try {
      const created = await apiFetch<StudentSummaryItem>("/students", {
        method: "POST",
        body: JSON.stringify({
          studentCode: stuForm.studentCode.trim().toUpperCase(),
          fullName: stuForm.fullName.trim(),
          dateOfBirth: stuForm.dateOfBirth,
          gender: stuForm.gender,
          nationalId: stuForm.nationalId.trim(),
          email: stuForm.email.trim().toLowerCase(),
          academicClassId: cls.id,
          majorId: cls.majorId,
          cohortYear: cls.cohortYear,
          initialPassword: stuForm.initialPassword.trim() || undefined,
        }),
      });
      setNotice({
        type: "success",
        text: `Đã tạo tài khoản sinh viên ${created.studentCode} (${created.fullName}) chưa có dữ liệu đăng ký/điểm. Mật khẩu: ${stuForm.initialPassword.trim() || "Ptit@12345"}`,
      });
      setStuForm((prev) => ({
        ...prev,
        studentCode: "",
        fullName: "",
        nationalId: "",
        email: "",
        initialPassword: "",
      }));
      await loadStudentsTab();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  return (
    <AppShell
      requiredRole="LECTURER"
      title="Cổng Giảng viên PTIT"
      subtitle="Quản lý lớp học phần được phân công, nhập điểm và công bố bảng điểm"
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

          <div className="flex flex-wrap items-center gap-4 rounded-xl border border-slate-200 bg-white p-4">
            <label htmlFor="lec-sem" className="text-sm font-semibold">Học kỳ:</label>
            <select
              id="lec-sem"
              value={semId ?? ""}
              onChange={(e) => {
                setSelectedSectionId(null);
                setSemId(Number(e.target.value));
              }}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
            >
              {semesters.map((s) => (
                <option key={s.id} value={s.id}>{s.code} — {s.name}</option>
              ))}
            </select>
          </div>

          {tab === "sections" && (
            <div className="space-y-6">
              <Card title="Lớp học phần được phân công">
                <div className="flex flex-wrap gap-2">
                  {sections.map((s) => (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => setSelectedSectionId(s.id)}
                      className={`rounded-lg border px-4 py-2 text-left text-sm transition ${
                        selectedSectionId === s.id
                          ? "border-red-700 bg-red-50 text-red-900"
                          : "border-slate-200 bg-white hover:bg-slate-50"
                      }`}
                    >
                      <div className="font-mono text-xs font-bold">{s.sectionCode}</div>
                      <div className="font-semibold">{s.courseName}</div>
                      <div className="text-xs text-slate-500">
                        Sĩ số: {s.currentEnrolled}/{s.maxCapacity} · Điểm: <strong>{s.gradeStatus}</strong>
                      </div>
                    </button>
                  ))}
                  {sections.length === 0 && <p className="text-sm text-slate-500">Chưa có lớp học phần nào trong kỳ này.</p>}
                </div>
              </Card>

              {gradeBook && (
                <Card
                  title={`Bảng điểm: ${gradeBook.sectionCode} — ${gradeBook.courseName}`}
                  subtitle={`Trạng thái bảng điểm: ${gradeBook.gradeStatus}`}
                  action={
                    gradeBook.gradeStatus === "DRAFT" && (
                      <div className="flex flex-wrap items-center gap-2">
                        <input
                          type="file"
                          accept=".csv"
                          aria-label="Chọn file CSV điểm"
                          onChange={(e) => setCsvFile(e.target.files?.[0] || null)}
                          className="text-xs"
                        />
                        <button
                          type="button"
                          onClick={handleImportCsv}
                          disabled={!csvFile}
                          className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold hover:bg-slate-50 disabled:opacity-50"
                        >
                          Import CSV
                        </button>
                        <button
                          type="button"
                          onClick={handlePublish}
                          className="rounded-lg bg-emerald-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-800"
                        >
                          Công bố điểm
                        </button>
                      </div>
                    )
                  }
                >
                  <form onSubmit={handleSaveGrades}>
                    <div tabIndex={0} role="region" aria-label="Bảng nhập điểm sinh viên" className="table-wrapper">
                      <table className="w-full text-left text-sm">
                        <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                          <tr>
                            <th className="px-3 py-2">Mã SV</th>
                            <th className="px-3 py-2">Họ và tên</th>
                            <th className="px-3 py-2">Lớp HC</th>
                            {gradeBook.components.map((c) => (
                              <th key={c.id} className="px-2 py-2 text-center">
                                {c.name} ({c.weightPercent}%)
                              </th>
                            ))}
                            <th className="px-3 py-2 text-center">Tổng kết</th>
                            <th className="px-3 py-2 text-center">Chữ</th>
                            <th className="px-3 py-2 text-center">Đạt</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {gradeBook.rows.length === 0 && (
                            <tr>
                              <td
                                colSpan={gradeBook.components.length + 6}
                                className="px-3 py-6 text-center text-sm text-slate-500"
                              >
                                Lớp học phần này hiện chưa có sinh viên nào đăng ký.
                              </td>
                            </tr>
                          )}
                          {gradeBook.rows.map((row) => (
                            <tr key={row.enrollmentId} className="hover:bg-slate-50">
                              <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{row.studentCode}</td>
                              <td className="px-3 py-2.5 text-sm font-medium">{row.fullName}</td>
                              <td className="px-3 py-2.5 text-sm text-slate-600">{row.academicClassCode}</td>
                              {gradeBook.components.map((c) => (
                                <td key={c.id} className="px-2 py-2.5 text-center">
                                  <input
                                    type="number"
                                    step="0.1"
                                    min="0"
                                    max="10"
                                    disabled={gradeBook.gradeStatus !== "DRAFT"}
                                    aria-label={`Điểm ${c.name} của ${row.studentCode}`}
                                    value={draftScores[`${row.enrollmentId}-${c.id}`] ?? ""}
                                    onChange={(e) =>
                                      setDraftScores((prev) => ({
                                        ...prev,
                                        [`${row.enrollmentId}-${c.id}`]: e.target.value,
                                      }))
                                    }
                                    className="w-16 rounded border border-slate-300 px-2 py-1 text-center text-sm disabled:bg-slate-100"
                                  />
                                </td>
                              ))}
                              <td className="px-3 py-2.5 text-center text-sm font-bold tabular-nums">
                                {row.finalScore10 !== null ? row.finalScore10.toFixed(1) : "—"}
                              </td>
                              <td className="px-3 py-2.5 text-center text-sm font-bold">{row.letterGrade || "—"}</td>
                              <td className="px-3 py-2.5 text-center text-sm">
                                {row.isPassed === null ? "—" : row.isPassed ? "Đạt" : "Trượt"}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>

                    {gradeBook.gradeStatus === "DRAFT" && gradeBook.rows.length > 0 && (
                      <div className="mt-4 flex justify-end">
                        <button
                          type="submit"
                          className="rounded-lg bg-red-700 px-5 py-2 text-sm font-semibold text-white hover:bg-red-800"
                        >
                          Lưu bảng điểm nháp
                        </button>
                      </div>
                    )}
                  </form>
                </Card>
              )}
            </div>
          )}

          {tab === "timetable" && (() => {
            const activeSem = semesters.find((s) => s.id === semId);
            return (
              <TimetableGrid
                slots={slots}
                semesterStartDate={activeSem?.startDate}
                semesterEndDate={activeSem?.endDate}
              />
            );
          })()}

          {tab === "students" && (
            <div className="space-y-6">
              <Card
                title="Thêm tài khoản Sinh viên mới"
                subtitle="Tài khoản mới khởi tạo sạch hoàn toàn (0 tín chỉ, chưa đăng ký môn nào) để sinh viên tự đăng ký tín chỉ"
              >
                <form onSubmit={handleCreateStudent} className="grid gap-3 text-sm sm:grid-cols-3">
                  <input
                    required
                    placeholder="Mã SV (VD: B23DCCN010)"
                    value={stuForm.studentCode}
                    onChange={(e) => setStuForm({ ...stuForm, studentCode: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  />
                  <input
                    required
                    placeholder="Họ và tên"
                    value={stuForm.fullName}
                    onChange={(e) => setStuForm({ ...stuForm, fullName: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  />
                  <input
                    required
                    type="date"
                    aria-label="Ngày sinh"
                    value={stuForm.dateOfBirth}
                    onChange={(e) => setStuForm({ ...stuForm, dateOfBirth: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  />
                  <select
                    aria-label="Giới tính"
                    value={stuForm.gender}
                    onChange={(e) => setStuForm({ ...stuForm, gender: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  >
                    <option value="MALE">Nam</option>
                    <option value="FEMALE">Nữ</option>
                    <option value="OTHER">Khác</option>
                  </select>
                  <input
                    required
                    placeholder="Số CCCD"
                    value={stuForm.nationalId}
                    onChange={(e) => setStuForm({ ...stuForm, nationalId: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  />
                  <input
                    required
                    type="email"
                    placeholder="Email sinh viên (VD: b23dccn010@stu.ptit-demo.vn)"
                    value={stuForm.email}
                    onChange={(e) => setStuForm({ ...stuForm, email: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  />
                  <select
                    aria-label="Lớp hành chính"
                    value={stuForm.academicClassId}
                    onChange={(e) => setStuForm({ ...stuForm, academicClassId: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2 sm:col-span-2"
                  >
                    {classes.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.code} — {c.name} ({c.majorName})
                      </option>
                    ))}
                  </select>
                  <input
                    placeholder="Mật khẩu khởi tạo (để trống = Ptit@12345)"
                    value={stuForm.initialPassword}
                    onChange={(e) => setStuForm({ ...stuForm, initialPassword: e.target.value })}
                    className="rounded-lg border border-slate-300 px-3 py-2"
                  />
                  <div className="sm:col-span-3">
                    <button
                      type="submit"
                      className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white hover:bg-red-800"
                    >
                      Tạo tài khoản Sinh viên
                    </button>
                  </div>
                </form>
              </Card>

              <Card title="Danh sách tài khoản Sinh viên">
                <div tabIndex={0} role="region" aria-label="Danh sách sinh viên" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Mã SV (Username)</th>
                        <th className="px-3 py-2">Họ tên</th>
                        <th className="px-3 py-2">Email</th>
                        <th className="px-3 py-2">Lớp HC</th>
                        <th className="px-3 py-2">Ngành</th>
                        <th className="px-3 py-2 text-center">Trạng thái</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {students.map((st) => (
                        <tr key={st.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{st.studentCode}</td>
                          <td className="px-3 py-2.5 text-sm font-medium">{st.fullName}</td>
                          <td className="px-3 py-2.5 text-sm text-slate-600">{st.email}</td>
                          <td className="px-3 py-2.5 text-sm">{st.academicClassCode}</td>
                          <td className="px-3 py-2.5 text-sm">{st.majorName}</td>
                          <td className="px-3 py-2.5 text-center text-sm font-semibold">{st.academicStatus}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            </div>
          )}

          {tab === "reports" && distReport && (
            <Card
              title="Thống kê phổ điểm các lớp giảng dạy"
              subtitle={`Tổng số đã chấm: ${distReport.totalGraded} · Tỷ lệ qua môn: ${distReport.passRatePercent}%`}
            >
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-8">
                {distReport.distribution.map((d) => (
                  <div key={d.label} className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-center">
                    <div className="text-lg font-extrabold text-slate-900">{d.count}</div>
                    <div className="text-xs font-semibold text-slate-600">Điểm {d.label}</div>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>
      )}
    </AppShell>
  );
}
