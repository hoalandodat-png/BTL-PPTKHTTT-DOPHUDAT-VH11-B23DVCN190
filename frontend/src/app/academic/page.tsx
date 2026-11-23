"use client";

import React, { useCallback, useEffect, useState } from "react";
import { AppShell, PortalIcons } from "@/components/AppShell";
import { Card, StatusNotice } from "@/components/UiHelpers";
import { apiFetch, PageResponse } from "@/lib/api";

const NAV_ITEMS = [
  { id: "sections", label: "Lớp học phần & Khóa điểm", icon: PortalIcons.ClipboardCheck },
  { id: "students", label: "Hồ sơ Sinh viên", icon: PortalIcons.UserCheck },
  { id: "courses", label: "Học phần & Tiên quyết", icon: PortalIcons.BookOpen },
  { id: "semesters", label: "Học kỳ & Đợt đăng ký", icon: PortalIcons.CalendarDays },
  { id: "org", label: "Khoa, Ngành, Lớp & GV", icon: PortalIcons.Building2 },
  { id: "reports", label: "Báo cáo thống kê", icon: PortalIcons.BarChart3 },
];

export default function AcademicAffairsPage() {
  const [tab, setTab] = useState("sections");
  const [notice, setNotice] = useState<{ type: "error" | "success" | "info"; text: string } | null>(null);

  const [semesters, setSemesters] = useState<any[]>([]);
  const [courses, setCourses] = useState<any[]>([]);
  const [lecturers, setLecturers] = useState<any[]>([]);
  const [classrooms, setClassrooms] = useState<any[]>([]);
  const [sections, setSections] = useState<any[]>([]);
  const [students, setStudents] = useState<any[]>([]);
  const [faculties, setFaculties] = useState<any[]>([]);
  const [departments, setDepartments] = useState<any[]>([]);
  const [majors, setMajors] = useState<any[]>([]);
  const [academicClasses, setAcademicClasses] = useState<any[]>([]);

  // Form states for Section creation
  const [secForm, setSecForm] = useState({
    courseId: "", semesterId: "", groupNumber: "2", lecturerId: "", maxCapacity: "50",
    dayOfWeek: "2", startPeriod: "1", endPeriod: "3", startWeek: "1", endWeek: "15", classroomId: "",
  });

  // Override enrollment form
  const [ovrForm, setOvrForm] = useState({ studentId: "", sectionId: "", action: "ENROLL", reason: "" });

  // Student form & CSV
  const [stuForm, setStuForm] = useState({
    studentCode: "", fullName: "", dateOfBirth: "2004-01-15", gender: "MALE",
    nationalId: "", email: "", academicClassId: "", majorId: "", cohortYear: "2022", initialPassword: "",
  });
  const [stuCsv, setStuCsv] = useState<File | null>(null);

  // Status update modal inline
  const [statusEdit, setStatusEdit] = useState<{ id: number; status: string; reason: string } | null>(null);

  // Course creation & prerequisites forms
  const [crsForm, setCrsForm] = useState({ code: "", name: "", credits: "3", departmentId: "" });
  const [selectedCourseId, setSelectedCourseId] = useState<string>("");
  const [coursePrereqs, setCoursePrereqs] = useState<any[]>([]);
  const [prereqAdd, setPrereqAdd] = useState({ prerequisiteCourseId: "", conditionType: "PREREQUISITE" });

  // Organization creation forms (Khoa, Bộ môn, Ngành, Lớp hành chính, Giảng viên)
  const [facForm, setFacForm] = useState({ code: "", name: "", description: "" });
  const [deptForm, setDeptForm] = useState({ code: "", name: "", facultyId: "" });
  const [majForm, setMajForm] = useState({ code: "", name: "", facultyId: "", totalCreditsRequired: "150", degreeLevel: "BACHELOR" });
  const [clsForm, setClsForm] = useState({ code: "", name: "", majorId: "", cohortYear: "2024" });
  const [lecForm, setLecForm] = useState({
    lecturerCode: "", fullName: "", email: "", departmentId: "", degree: "ThS", academicRank: "", phone: "", initialPassword: "",
  });

  // Exam schedule management
  const [examSemId, setExamSemId] = useState<string>("");
  const [exams, setExams] = useState<any[]>([]);
  const [examForm, setExamForm] = useState({
    sectionId: "", classroomId: "", examDate: "2026-12-25", startTime: "07:30", endTime: "09:00",
    examFormat: "Thi viết (90 phút)", note: "",
  });

  // Reports
  const [stuStats, setStuStats] = useState<any>(null);
  const [capReport, setCapReport] = useState<any>(null);
  const [distReport, setDistReport] = useState<any>(null);

  const loadMasterData = useCallback(async () => {
    try {
      const [sems, crsPage, lecPage, rooms, facs, depts, majs, cls] = await Promise.all([
        apiFetch<any[]>("/semesters"),
        apiFetch<PageResponse<any>>("/courses?size=100"),
        apiFetch<PageResponse<any>>("/lecturers?size=100"),
        apiFetch<any[]>("/classrooms"),
        apiFetch<any[]>("/faculties"),
        apiFetch<any[]>("/departments"),
        apiFetch<any[]>("/majors"),
        apiFetch<any[]>("/academic-classes"),
      ]);
      setSemesters(sems);
      setCourses(crsPage.items);
      setLecturers(lecPage.items);
      setClassrooms(rooms);
      setFaculties(facs);
      setDepartments(depts);
      setMajors(majs);
      setAcademicClasses(cls);
      setSecForm((prev) => ({
        ...prev,
        courseId: prev.courseId || String(crsPage.items[0]?.id || ""),
        semesterId: prev.semesterId || String(sems[0]?.id || ""),
        lecturerId: prev.lecturerId || String(lecPage.items[0]?.id || ""),
        classroomId: prev.classroomId || String(rooms[0]?.id || ""),
      }));
      setStuForm((prev) => ({
        ...prev,
        academicClassId: prev.academicClassId || String(cls[0]?.id || ""),
        majorId: prev.majorId || String(cls[0]?.majorId || majs[0]?.id || ""),
        cohortYear: prev.cohortYear || String(cls[0]?.cohortYear || "2022"),
      }));
      setCrsForm((prev) => ({ ...prev, departmentId: prev.departmentId || String(depts[0]?.id || "") }));
      setSelectedCourseId((prev) => prev || String(crsPage.items[0]?.id || ""));
      setDeptForm((prev) => ({ ...prev, facultyId: prev.facultyId || String(facs[0]?.id || "") }));
      setMajForm((prev) => ({ ...prev, facultyId: prev.facultyId || String(facs[0]?.id || "") }));
      setClsForm((prev) => ({ ...prev, majorId: prev.majorId || String(majs[0]?.id || "") }));
      setLecForm((prev) => ({ ...prev, departmentId: prev.departmentId || String(depts[0]?.id || "") }));
      setExamSemId((prev) => prev || String(sems[0]?.id || ""));
      setExamForm((prev) => ({ ...prev, classroomId: prev.classroomId || String(rooms[0]?.id || "") }));
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, []);

  const loadSections = useCallback(async () => {
    const res = await apiFetch<PageResponse<any>>("/sections?size=100");
    setSections(res.items);
    setExamForm((prev) => ({ ...prev, sectionId: prev.sectionId || String(res.items[0]?.id || "") }));
  }, []);

  const loadCoursePrerequisites = useCallback(async (courseId: string) => {
    if (!courseId) {
      setCoursePrereqs([]);
      return;
    }
    try {
      const detail = await apiFetch<any>(`/courses/${courseId}`);
      setCoursePrereqs(detail.prerequisites || []);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, []);

  const loadExams = useCallback(async (semesterId: string) => {
    if (!semesterId) return;
    try {
      const list = await apiFetch<any[]>(`/exams?semesterId=${semesterId}`);
      setExams(list);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, []);

  const loadStudents = useCallback(async () => {
    const res = await apiFetch<PageResponse<any>>("/students?size=100");
    setStudents(res.items);
  }, []);

  useEffect(() => {
    loadMasterData();
    loadSections();
    loadStudents();
  }, [loadMasterData, loadSections, loadStudents]);

  useEffect(() => {
    if (tab === "courses" && selectedCourseId) {
      loadCoursePrerequisites(selectedCourseId);
    }
  }, [tab, selectedCourseId, loadCoursePrerequisites]);

  useEffect(() => {
    if (tab === "semesters" && examSemId) {
      loadExams(examSemId);
    }
  }, [tab, examSemId, loadExams]);

  useEffect(() => {
    if (tab === "reports") {
      Promise.all([
        apiFetch<any>("/reports/student-statistics"),
        apiFetch<any>("/reports/enrollment-capacity"),
        apiFetch<any>("/reports/grade-distribution"),
      ])
        .then(([s, c, d]) => {
          setStuStats(s);
          setCapReport(c);
          setDistReport(d);
        })
        .catch((err) => setNotice({ type: "error", text: (err as Error).message }));
    }
  }, [tab]);

  async function handleCreateSection(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/sections", {
        method: "POST",
        body: JSON.stringify({
          courseId: Number(secForm.courseId),
          semesterId: Number(secForm.semesterId),
          groupNumber: Number(secForm.groupNumber),
          lecturerId: secForm.lecturerId ? Number(secForm.lecturerId) : null,
          maxCapacity: Number(secForm.maxCapacity),
          schedules: [
            {
              dayOfWeek: Number(secForm.dayOfWeek),
              startPeriod: Number(secForm.startPeriod),
              endPeriod: Number(secForm.endPeriod),
              startWeek: Number(secForm.startWeek),
              endWeek: Number(secForm.endWeek),
              classroomId: Number(secForm.classroomId),
            },
          ],
        }),
      });
      setNotice({ type: "success", text: "Mở lớp học phần và xếp lịch thành công (không trùng phòng/GV)." });
      await loadSections();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleLockGrades(sectionId: number) {
    setNotice(null);
    try {
      await apiFetch(`/sections/${sectionId}/grades/lock`, { method: "POST" });
      setNotice({ type: "success", text: "Đã khóa sổ điểm lớp học phần." });
      await loadSections();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleOverrideEnroll(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/enrollments/override", {
        method: "POST",
        body: JSON.stringify({
          studentId: Number(ovrForm.studentId),
          sectionId: Number(ovrForm.sectionId),
          action: ovrForm.action,
          reason: ovrForm.reason,
        }),
      });
      setNotice({ type: "success", text: "Đã thực hiện đăng ký/hủy hộ và ghi Audit Log." });
      setOvrForm((p) => ({ ...p, reason: "" }));
      await loadSections();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateStudent(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    const chosenCls = academicClasses.find((c) => c.id === Number(stuForm.academicClassId));
    try {
      await apiFetch("/students", {
        method: "POST",
        body: JSON.stringify({
          studentCode: stuForm.studentCode.trim().toUpperCase(),
          fullName: stuForm.fullName.trim(),
          dateOfBirth: stuForm.dateOfBirth,
          gender: stuForm.gender,
          nationalId: stuForm.nationalId.trim(),
          email: stuForm.email.trim().toLowerCase(),
          academicClassId: Number(stuForm.academicClassId),
          majorId: chosenCls ? chosenCls.majorId : Number(stuForm.majorId),
          cohortYear: chosenCls ? chosenCls.cohortYear : Number(stuForm.cohortYear),
          initialPassword: stuForm.initialPassword.trim() || undefined,
        }),
      });
      setNotice({
        type: "success",
        text: `Đã tạo tài khoản sinh viên ${stuForm.studentCode.trim().toUpperCase()} (Mật khẩu: ${stuForm.initialPassword.trim() || "Ptit@12345"}).`,
      });
      setStuForm((p) => ({ ...p, studentCode: "", fullName: "", nationalId: "", email: "", initialPassword: "" }));
      await loadStudents();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleImportStudents() {
    if (!stuCsv) return;
    setNotice(null);
    const fd = new FormData();
    fd.append("file", stuCsv);
    try {
      const res = await apiFetch<{ importedCount: number }>("/students/import?dryRun=false", {
        method: "POST",
        body: fd,
      });
      setNotice({ type: "success", text: `Đã import thành công ${res.importedCount} sinh viên.` });
      await loadStudents();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleSaveStudentStatus(e: React.FormEvent) {
    e.preventDefault();
    if (!statusEdit) return;
    setNotice(null);
    try {
      await apiFetch(`/students/${statusEdit.id}/academic-status`, {
        method: "PATCH",
        body: JSON.stringify({ newStatus: statusEdit.status, reason: statusEdit.reason }),
      });
      setNotice({ type: "success", text: "Đã cập nhật trạng thái học vụ và lưu lịch sử." });
      setStatusEdit(null);
      await loadStudents();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateCourse(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/courses", {
        method: "POST",
        body: JSON.stringify({
          code: crsForm.code,
          name: crsForm.name,
          credits: Number(crsForm.credits),
          theoryHours: 30,
          practiceHours: 15,
          departmentId: Number(crsForm.departmentId),
          gradeComponents: [
            { code: "ATTENDANCE", name: "Chuyên cần", weightPercent: 10, isFinalExam: false, minThreshold: 0, displayOrder: 1 },
            { code: "PRACTICE", name: "Thực hành", weightPercent: 20, isFinalExam: false, minThreshold: 0, displayOrder: 2 },
            { code: "MIDTERM", name: "Giữa kỳ", weightPercent: 20, isFinalExam: false, minThreshold: 0, displayOrder: 3 },
            { code: "FINAL", name: "Cuối kỳ", weightPercent: 50, isFinalExam: true, minThreshold: 4.0, displayOrder: 4 },
          ],
        }),
      });
      setNotice({ type: "success", text: `Đã tạo học phần ${crsForm.code} với 4 đầu điểm chuẩn (10-20-20-50).` });
      setCrsForm((p) => ({ ...p, code: "", name: "" }));
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function toggleRegistration(sem: any) {
    setNotice(null);
    try {
      await apiFetch(`/semesters/${sem.id}/registration-period`, {
        method: "PATCH",
        body: JSON.stringify({
          registrationStartAt: sem.registrationStartAt,
          registrationEndAt: sem.registrationEndAt,
          isRegistrationOpen: !sem.isRegistrationOpen,
        }),
      });
      setNotice({ type: "success", text: `Đã ${!sem.isRegistrationOpen ? "MỞ" : "ĐÓNG"} đợt đăng ký kỳ ${sem.code}.` });
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleAddPrerequisite(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedCourseId || !prereqAdd.prerequisiteCourseId) return;
    setNotice(null);
    const nextList = [
      ...coursePrereqs.map((p) => ({
        prerequisiteCourseId: p.prerequisiteCourseId,
        conditionType: p.conditionType,
      })),
      {
        prerequisiteCourseId: Number(prereqAdd.prerequisiteCourseId),
        conditionType: prereqAdd.conditionType,
      },
    ];
    try {
      const updated = await apiFetch<any[]>(`/courses/${selectedCourseId}/prerequisites`, {
        method: "PUT",
        body: JSON.stringify({ prerequisites: nextList }),
      });
      setCoursePrereqs(updated);
      setNotice({ type: "success", text: "Đã cập nhật điều kiện tiên quyết (đã kiểm tra không tạo chu trình)." });
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleRemovePrerequisite(prereqCourseId: number) {
    if (!selectedCourseId) return;
    setNotice(null);
    const nextList = coursePrereqs
      .filter((p) => p.prerequisiteCourseId !== prereqCourseId)
      .map((p) => ({ prerequisiteCourseId: p.prerequisiteCourseId, conditionType: p.conditionType }));
    try {
      const updated = await apiFetch<any[]>(`/courses/${selectedCourseId}/prerequisites`, {
        method: "PUT",
        body: JSON.stringify({ prerequisites: nextList }),
      });
      setCoursePrereqs(updated);
      setNotice({ type: "success", text: "Đã gỡ bỏ học phần điều kiện." });
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateFaculty(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/faculties", { method: "POST", body: JSON.stringify(facForm) });
      setNotice({ type: "success", text: `Đã thêm Khoa ${facForm.code}.` });
      setFacForm({ code: "", name: "", description: "" });
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateDepartment(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/departments", {
        method: "POST",
        body: JSON.stringify({ ...deptForm, facultyId: Number(deptForm.facultyId) }),
      });
      setNotice({ type: "success", text: `Đã thêm Bộ môn ${deptForm.code}.` });
      setDeptForm((p) => ({ ...p, code: "", name: "" }));
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateMajor(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/majors", {
        method: "POST",
        body: JSON.stringify({
          code: majForm.code,
          name: majForm.name,
          facultyId: Number(majForm.facultyId),
          totalCreditsRequired: Number(majForm.totalCreditsRequired),
          degreeLevel: majForm.degreeLevel,
        }),
      });
      setNotice({ type: "success", text: `Đã thêm Ngành ${majForm.code}.` });
      setMajForm((p) => ({ ...p, code: "", name: "" }));
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateAcademicClass(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/academic-classes", {
        method: "POST",
        body: JSON.stringify({
          code: clsForm.code,
          name: clsForm.name,
          majorId: Number(clsForm.majorId),
          cohortYear: Number(clsForm.cohortYear),
        }),
      });
      setNotice({ type: "success", text: `Đã thêm Lớp hành chính ${clsForm.code}.` });
      setClsForm((p) => ({ ...p, code: "", name: "" }));
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateLecturer(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/lecturers", {
        method: "POST",
        body: JSON.stringify({
          lecturerCode: lecForm.lecturerCode.trim().toUpperCase(),
          fullName: lecForm.fullName.trim(),
          email: lecForm.email.trim().toLowerCase(),
          departmentId: Number(lecForm.departmentId),
          degree: lecForm.degree.trim() || undefined,
          academicRank: lecForm.academicRank.trim() || undefined,
          phone: lecForm.phone.trim() || undefined,
          initialPassword: lecForm.initialPassword.trim() || undefined,
        }),
      });
      setNotice({
        type: "success",
        text: `Đã thêm Giảng viên ${lecForm.lecturerCode.trim().toUpperCase()} kèm tài khoản đăng nhập (MK: ${lecForm.initialPassword.trim() || "Ptit@12345"}).`,
      });
      setLecForm((p) => ({ ...p, lecturerCode: "", fullName: "", email: "", academicRank: "", phone: "", initialPassword: "" }));
      await loadMasterData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleCreateExam(e: React.FormEvent) {
    e.preventDefault();
    setNotice(null);
    try {
      await apiFetch("/exams", {
        method: "POST",
        body: JSON.stringify({
          sectionId: Number(examForm.sectionId),
          classroomId: Number(examForm.classroomId),
          examDate: examForm.examDate,
          startTime: examForm.startTime,
          endTime: examForm.endTime,
          examFormat: examForm.examFormat,
          note: examForm.note || undefined,
        }),
      });
      setNotice({ type: "success", text: "Đã xếp lịch thi mới thành công." });
      setExamForm((p) => ({ ...p, note: "" }));
      await loadExams(examSemId);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleDeleteExam(id: number) {
    setNotice(null);
    try {
      await apiFetch(`/exams/${id}`, { method: "DELETE" });
      setNotice({ type: "success", text: "Đã xóa lịch thi." });
      await loadExams(examSemId);
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  return (
    <AppShell
      requiredRole="ACADEMIC_AFFAIRS"
      title="Phòng Quản lý Đào tạo"
      subtitle="Điều phối học vụ, mở lớp kiểm tra trùng lịch, hồ sơ sinh viên & khóa sổ điểm"
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

          {tab === "sections" && (
            <div className="space-y-6">
              <div className="grid gap-6 lg:grid-cols-2">
                <Card title="Mở Lớp học phần mới (Kiểm tra trùng Phòng & Giảng viên)">
                  <form onSubmit={handleCreateSection} className="grid grid-cols-2 gap-3 text-sm">
                    <div className="col-span-2 sm:col-span-1">
                      <label className="mb-1 block text-xs font-semibold">Học kỳ</label>
                      <select value={secForm.semesterId} onChange={(e) => setSecForm({ ...secForm, semesterId: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        {semesters.map((s) => <option key={s.id} value={s.id}>{s.code} - {s.name}</option>)}
                      </select>
                    </div>
                    <div className="col-span-2 sm:col-span-1">
                      <label className="mb-1 block text-xs font-semibold">Học phần</label>
                      <select value={secForm.courseId} onChange={(e) => setSecForm({ ...secForm, courseId: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        {courses.map((c) => <option key={c.id} value={c.id}>{c.code} - {c.name}</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Nhóm lớp</label>
                      <input type="number" min={1} value={secForm.groupNumber} onChange={(e) => setSecForm({ ...secForm, groupNumber: e.target.value })} className="w-full rounded-lg border px-2.5 py-2" />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Sĩ số tối đa</label>
                      <input type="number" min={1} value={secForm.maxCapacity} onChange={(e) => setSecForm({ ...secForm, maxCapacity: e.target.value })} className="w-full rounded-lg border px-2.5 py-2" />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Giảng viên</label>
                      <select value={secForm.lecturerId} onChange={(e) => setSecForm({ ...secForm, lecturerId: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        {lecturers.map((l) => <option key={l.id} value={l.id}>{l.lecturerCode} - {l.fullName}</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Phòng học</label>
                      <select value={secForm.classroomId} onChange={(e) => setSecForm({ ...secForm, classroomId: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        {classrooms.map((r) => <option key={r.id} value={r.id}>{r.code} (SC: {r.capacity})</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Thứ (2-7)</label>
                      <input type="number" min={2} max={7} value={secForm.dayOfWeek} onChange={(e) => setSecForm({ ...secForm, dayOfWeek: e.target.value })} className="w-full rounded-lg border px-2.5 py-2" />
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="mb-1 block text-xs font-semibold">Từ tiết</label>
                        <input type="number" min={1} max={12} value={secForm.startPeriod} onChange={(e) => setSecForm({ ...secForm, startPeriod: e.target.value })} className="w-full rounded-lg border px-2.5 py-2" />
                      </div>
                      <div>
                        <label className="mb-1 block text-xs font-semibold">Đến tiết</label>
                        <input type="number" min={1} max={12} value={secForm.endPeriod} onChange={(e) => setSecForm({ ...secForm, endPeriod: e.target.value })} className="w-full rounded-lg border px-2.5 py-2" />
                      </div>
                    </div>
                    <div className="col-span-2 pt-2">
                      <button type="submit" className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white hover:bg-red-800">
                        Mở lớp học phần
                      </button>
                    </div>
                  </form>
                </Card>

                <Card title="Đăng ký / Hủy học phần hộ Sinh viên (Override + Audit Log)">
                  <form onSubmit={handleOverrideEnroll} className="space-y-3 text-sm">
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Sinh viên</label>
                      <select required value={ovrForm.studentId} onChange={(e) => setOvrForm({ ...ovrForm, studentId: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        <option value="">-- Chọn sinh viên --</option>
                        {students.map((s) => <option key={s.id} value={s.id}>{s.studentCode} - {s.fullName}</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Lớp học phần</label>
                      <select required value={ovrForm.sectionId} onChange={(e) => setOvrForm({ ...ovrForm, sectionId: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        <option value="">-- Chọn lớp học phần --</option>
                        {sections.map((s) => <option key={s.id} value={s.id}>{s.sectionCode} - {s.courseName} ({s.currentEnrolled}/{s.maxCapacity})</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Thao tác</label>
                      <select value={ovrForm.action} onChange={(e) => setOvrForm({ ...ovrForm, action: e.target.value })} className="w-full rounded-lg border px-2.5 py-2">
                        <option value="ENROLL">Đăng ký hộ</option>
                        <option value="CANCEL">Hủy đăng ký hộ</option>
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Lý do bắt buộc *</label>
                      <input required value={ovrForm.reason} onChange={(e) => setOvrForm({ ...ovrForm, reason: e.target.value })} placeholder="VD: Giải quyết đơn bổ sung tín chỉ..." className="w-full rounded-lg border px-2.5 py-2" />
                    </div>
                    <button type="submit" className="rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white hover:bg-slate-800">
                      Thực hiện thao tác hộ
                    </button>
                  </form>
                </Card>
              </div>

              <Card title="Danh sách Lớp học phần & Trạng thái khóa sổ điểm">
                <div tabIndex={0} role="region" aria-label="Bảng lớp học phần" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Mã lớp HP</th>
                        <th className="px-3 py-2">Môn học</th>
                        <th className="px-3 py-2">Giảng viên</th>
                        <th className="px-3 py-2 text-center">Sĩ số</th>
                        <th className="px-3 py-2 text-center">Bảng điểm</th>
                        <th className="px-3 py-2 text-right">Khóa sổ điểm</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {sections.map((s) => (
                        <tr key={s.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{s.sectionCode}</td>
                          <td className="px-3 py-2.5 text-sm font-medium">{s.courseName}</td>
                          <td className="px-3 py-2.5 text-sm">{s.lecturerName || "—"}</td>
                          <td className="px-3 py-2.5 text-center text-sm tabular-nums">{s.currentEnrolled}/{s.maxCapacity}</td>
                          <td className="px-3 py-2.5 text-center text-sm font-semibold">{s.gradeStatus}</td>
                          <td className="px-3 py-2.5 text-right">
                            {s.gradeStatus === "PUBLISHED" && (
                              <button
                                type="button"
                                onClick={() => handleLockGrades(s.id)}
                                className="rounded bg-amber-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-amber-700"
                              >
                                Khóa sổ điểm
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            </div>
          )}

          {tab === "students" && (
            <div className="space-y-6">
              <Card
                title="Thêm mới Sinh viên (Tự động tạo tài khoản User)"
                action={
                  <div className="flex items-center gap-2">
                    <input type="file" accept=".csv" onChange={(e) => setStuCsv(e.target.files?.[0] || null)} className="text-xs" />
                    <button type="button" onClick={handleImportStudents} disabled={!stuCsv} className="rounded bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white disabled:opacity-50">
                      Import CSV (All-or-Nothing)
                    </button>
                  </div>
                }
              >
                <form onSubmit={handleCreateStudent} className="grid gap-3 text-sm sm:grid-cols-3">
                  <input required placeholder="Mã SV (VD: B22DCCN010)" value={stuForm.studentCode} onChange={(e) => setStuForm({ ...stuForm, studentCode: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <input required placeholder="Họ và tên" value={stuForm.fullName} onChange={(e) => setStuForm({ ...stuForm, fullName: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <input required type="date" value={stuForm.dateOfBirth} onChange={(e) => setStuForm({ ...stuForm, dateOfBirth: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <select value={stuForm.gender} onChange={(e) => setStuForm({ ...stuForm, gender: e.target.value })} className="rounded-lg border px-3 py-2">
                    <option value="MALE">Nam</option>
                    <option value="FEMALE">Nữ</option>
                    <option value="OTHER">Khác</option>
                  </select>
                  <input required placeholder="Số CCCD" value={stuForm.nationalId} onChange={(e) => setStuForm({ ...stuForm, nationalId: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <input required type="email" placeholder="Email trường" value={stuForm.email} onChange={(e) => setStuForm({ ...stuForm, email: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <select value={stuForm.academicClassId} onChange={(e) => setStuForm({ ...stuForm, academicClassId: e.target.value })} className="rounded-lg border px-3 py-2 sm:col-span-2">
                    {academicClasses.map((c) => <option key={c.id} value={c.id}>{c.code} — {c.name} ({c.majorName})</option>)}
                  </select>
                  <input placeholder="Mật khẩu khởi tạo (trống = Ptit@12345)" value={stuForm.initialPassword} onChange={(e) => setStuForm({ ...stuForm, initialPassword: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <div className="sm:col-span-3">
                    <button type="submit" className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white hover:bg-red-800">
                      Tạo hồ sơ & tài khoản sinh viên
                    </button>
                  </div>
                </form>
              </Card>

              {statusEdit && (
                <Card title={`Chuyển trạng thái học vụ (Sinh viên ID #${statusEdit.id})`}>
                  <form onSubmit={handleSaveStudentStatus} className="flex flex-wrap items-end gap-3 text-sm">
                    <div>
                      <label className="mb-1 block text-xs font-semibold">Trạng thái mới</label>
                      <select value={statusEdit.status} onChange={(e) => setStatusEdit({ ...statusEdit, status: e.target.value })} className="rounded-lg border px-3 py-2">
                        {["ACTIVE", "ON_LEAVE", "SUSPENDED", "GRADUATED", "DROPPED_OUT"].map((st) => <option key={st} value={st}>{st}</option>)}
                      </select>
                    </div>
                    <div className="flex-1">
                      <label className="mb-1 block text-xs font-semibold">Lý do / Số quyết định *</label>
                      <input required value={statusEdit.reason} onChange={(e) => setStatusEdit({ ...statusEdit, reason: e.target.value })} className="w-full rounded-lg border px-3 py-2" />
                    </div>
                    <button type="submit" className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white">Xác nhận</button>
                    <button type="button" onClick={() => setStatusEdit(null)} className="rounded-lg border px-3 py-2">Hủy</button>
                  </form>
                </Card>
              )}

              <Card title="Danh sách Sinh viên">
                <div tabIndex={0} role="region" aria-label="Danh sách sinh viên" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Mã SV</th>
                        <th className="px-3 py-2">Họ tên</th>
                        <th className="px-3 py-2">Lớp HC</th>
                        <th className="px-3 py-2">Ngành</th>
                        <th className="px-3 py-2 text-center">Trạng thái</th>
                        <th className="px-3 py-2 text-right">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {students.map((st) => (
                        <tr key={st.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{st.studentCode}</td>
                          <td className="px-3 py-2.5 text-sm font-medium">{st.fullName}</td>
                          <td className="px-3 py-2.5 text-sm">{st.academicClassCode}</td>
                          <td className="px-3 py-2.5 text-sm">{st.majorName}</td>
                          <td className="px-3 py-2.5 text-center text-sm font-semibold">{st.academicStatus}</td>
                          <td className="px-3 py-2.5 text-right">
                            <button
                              type="button"
                              onClick={() => setStatusEdit({ id: st.id, status: st.academicStatus, reason: "" })}
                              className="rounded border border-slate-300 px-3 py-1.5 text-sm font-medium hover:bg-slate-50"
                            >
                              Đổi trạng thái
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            </div>
          )}

          {tab === "courses" && (
            <div className="space-y-6">
              <Card title="Tạo Học phần mới (Thành phần điểm chuẩn 10-20-20-50)">
                <form onSubmit={handleCreateCourse} className="grid gap-3 text-sm sm:grid-cols-4">
                  <input required placeholder="Mã môn (VD: INT1400)" value={crsForm.code} onChange={(e) => setCrsForm({ ...crsForm, code: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <input required placeholder="Tên học phần" value={crsForm.name} onChange={(e) => setCrsForm({ ...crsForm, name: e.target.value })} className="rounded-lg border px-3 py-2 sm:col-span-2" />
                  <input required type="number" min={1} max={15} placeholder="Số TC" value={crsForm.credits} onChange={(e) => setCrsForm({ ...crsForm, credits: e.target.value })} className="rounded-lg border px-3 py-2" />
                  <select value={crsForm.departmentId} onChange={(e) => setCrsForm({ ...crsForm, departmentId: e.target.value })} className="rounded-lg border px-3 py-2 sm:col-span-2">
                    {departments.map((d) => <option key={d.id} value={d.id}>{d.code} - {d.name}</option>)}
                  </select>
                  <button type="submit" className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white">Thêm học phần</button>
                </form>
              </Card>

              <Card title="Quản lý Điều kiện Tiên quyết / Song hành (Chống chu trình DAG)">
                <div className="space-y-4 text-sm">
                  <div className="flex flex-wrap items-center gap-3">
                    <label htmlFor="prereq-course" className="font-semibold text-slate-700">Học phần đang cấu hình:</label>
                    <select
                      id="prereq-course"
                      value={selectedCourseId}
                      onChange={(e) => setSelectedCourseId(e.target.value)}
                      className="rounded-lg border px-3 py-2"
                    >
                      {courses.map((c) => (
                        <option key={c.id} value={c.id}>{c.code} — {c.name}</option>
                      ))}
                    </select>
                  </div>

                  <form onSubmit={handleAddPrerequisite} className="grid gap-3 sm:grid-cols-4">
                    <select
                      aria-label="Chọn môn tiên quyết"
                      value={prereqAdd.prerequisiteCourseId}
                      onChange={(e) => setPrereqAdd({ ...prereqAdd, prerequisiteCourseId: e.target.value })}
                      className="rounded-lg border px-3 py-2 sm:col-span-2"
                    >
                      <option value="">-- Chọn học phần điều kiện --</option>
                      {courses
                        .filter((c) => String(c.id) !== selectedCourseId)
                        .map((c) => (
                          <option key={c.id} value={c.id}>{c.code} — {c.name}</option>
                        ))}
                    </select>
                    <select
                      aria-label="Loại điều kiện"
                      value={prereqAdd.conditionType}
                      onChange={(e) => setPrereqAdd({ ...prereqAdd, conditionType: e.target.value })}
                      className="rounded-lg border px-3 py-2"
                    >
                      <option value="PREREQUISITE">Tiên quyết (Phải qua)</option>
                      <option value="COREQUISITE">Song hành / Học trước</option>
                    </select>
                    <button
                      type="submit"
                      disabled={!prereqAdd.prerequisiteCourseId}
                      className="rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white hover:bg-slate-800 disabled:opacity-50"
                    >
                      Thêm tiên quyết
                    </button>
                  </form>

                  <div className="space-y-2">
                    {coursePrereqs.length === 0 ? (
                      <p className="text-slate-500">Học phần này chưa có điều kiện tiên quyết nào.</p>
                    ) : (
                      coursePrereqs.map((p) => (
                        <div key={p.prerequisiteCourseId} className="flex items-center justify-between rounded-lg border bg-slate-50 px-3 py-2">
                          <span>
                            <strong className="font-mono text-red-700">{p.prerequisiteCourseCode}</strong> — {p.prerequisiteCourseName}{" "}
                            <span className="ml-2 rounded bg-slate-200 px-2 py-0.5 text-xs font-semibold">{p.conditionType}</span>
                          </span>
                          <button
                            type="button"
                            onClick={() => handleRemovePrerequisite(p.prerequisiteCourseId)}
                            className="rounded border border-red-200 bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700 hover:bg-red-100"
                          >
                            Gỡ bỏ
                          </button>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </Card>

              <Card title="Danh mục Học phần">
                <div tabIndex={0} role="region" aria-label="Danh mục học phần" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Mã môn</th>
                        <th className="px-3 py-2">Tên học phần</th>
                        <th className="px-3 py-2 text-center">Tín chỉ</th>
                        <th className="px-3 py-2">Bộ môn</th>
                        <th className="px-3 py-2">Khoa</th>
                        <th className="px-3 py-2 text-right">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {courses.map((c) => (
                        <tr key={c.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{c.code}</td>
                          <td className="px-3 py-2.5 text-sm font-medium">{c.name}</td>
                          <td className="px-3 py-2.5 text-center text-sm tabular-nums">{c.credits}</td>
                          <td className="px-3 py-2.5 text-sm">{c.departmentName}</td>
                          <td className="px-3 py-2.5 text-sm">{c.facultyName}</td>
                          <td className="px-3 py-2.5 text-right">
                            <button
                              type="button"
                              onClick={() => setSelectedCourseId(String(c.id))}
                              className="rounded border border-slate-300 px-3 py-1.5 text-sm font-semibold hover:bg-slate-50"
                            >
                              Cấu hình tiên quyết
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            </div>
          )}

          {tab === "semesters" && (
            <div className="space-y-6">
              <Card title="Quy định số tín chỉ đăng ký theo loại học kỳ & hệ đào tạo">
                <div tabIndex={0} role="region" aria-label="Quy định tín chỉ học kỳ" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Loại học kỳ</th>
                        <th className="px-3 py-2 text-center">Tối thiểu</th>
                        <th className="px-3 py-2 text-center">Tối đa</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      <tr>
                        <td className="px-3 py-2 font-medium">Học kỳ chính – hệ cử nhân</td>
                        <td className="px-3 py-2 text-center font-semibold">15</td>
                        <td className="px-3 py-2 text-center font-semibold text-red-700">25</td>
                      </tr>
                      <tr>
                        <td className="px-3 py-2 font-medium">Học kỳ chính – hệ kỹ sư</td>
                        <td className="px-3 py-2 text-center font-semibold">16</td>
                        <td className="px-3 py-2 text-center font-semibold text-red-700">25</td>
                      </tr>
                      <tr>
                        <td className="px-3 py-2 font-medium">Học kỳ phụ (hè)</td>
                        <td className="px-3 py-2 text-center text-slate-600">Không quy định chung</td>
                        <td className="px-3 py-2 text-center font-semibold text-red-700">12</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </Card>

              <Card title="Quản lý Học kỳ & Đóng/Mở Đợt đăng ký học phần">
                <div tabIndex={0} role="region" aria-label="Danh sách học kỳ" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Mã kỳ</th>
                        <th className="px-3 py-2">Tên học kỳ</th>
                        <th className="px-3 py-2 text-center">Loại kỳ</th>
                        <th className="px-3 py-2 text-center">Tín chỉ (Min-Max)</th>
                        <th className="px-3 py-2 text-center">Đợt đăng ký</th>
                        <th className="px-3 py-2 text-right">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {semesters.map((s) => (
                        <tr key={s.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{s.code}</td>
                          <td className="px-3 py-2.5 text-sm font-medium">{s.name}</td>
                          <td className="px-3 py-2.5 text-center text-sm font-semibold">
                            {s.termType === "SUMMER" ? "Phụ (hè)" : "Chính"}
                          </td>
                          <td className="px-3 py-2.5 text-center text-sm tabular-nums">
                            {s.termType === "SUMMER"
                              ? `Không quy định chung – ${Math.min(s.maxCredits, 12)} TC`
                              : `${s.minCredits} (CN:15 / KS:16) – ${s.maxCredits} TC`}
                          </td>
                          <td className="px-3 py-2.5 text-center">
                            <span className={`inline-block rounded-full px-3 py-0.5 text-sm font-semibold ${s.isRegistrationOpen ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-600"}`}>
                              {s.isRegistrationOpen ? "ĐANG MỞ" : "ĐÃ ĐÓNG"}
                            </span>
                          </td>
                          <td className="px-3 py-2.5 text-right">
                            <button
                              type="button"
                              onClick={() => toggleRegistration(s)}
                              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold hover:bg-slate-50"
                            >
                              {s.isRegistrationOpen ? "Đóng đợt ĐK" : "Mở đợt ĐK"}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>

              <Card title="Xếp & Quản lý Lịch thi (Chống trùng phòng thi)">
                <div className="space-y-4 text-sm">
                  <form onSubmit={handleCreateExam} className="grid gap-3 sm:grid-cols-4">
                    <select value={examForm.sectionId} onChange={(e) => setExamForm({ ...examForm, sectionId: e.target.value })} className="rounded-lg border px-3 py-2 sm:col-span-2">
                      {sections.map((s) => <option key={s.id} value={s.id}>{s.sectionCode} — {s.courseName}</option>)}
                    </select>
                    <select value={examForm.classroomId} onChange={(e) => setExamForm({ ...examForm, classroomId: e.target.value })} className="rounded-lg border px-3 py-2">
                      {classrooms.map((r) => <option key={r.id} value={r.id}>{r.code} ({r.building})</option>)}
                    </select>
                    <input required type="date" value={examForm.examDate} onChange={(e) => setExamForm({ ...examForm, examDate: e.target.value })} className="rounded-lg border px-3 py-2" />
                    <input required type="time" value={examForm.startTime} onChange={(e) => setExamForm({ ...examForm, startTime: e.target.value })} className="rounded-lg border px-3 py-2" />
                    <input required type="time" value={examForm.endTime} onChange={(e) => setExamForm({ ...examForm, endTime: e.target.value })} className="rounded-lg border px-3 py-2" />
                    <input required placeholder="Hình thức thi" value={examForm.examFormat} onChange={(e) => setExamForm({ ...examForm, examFormat: e.target.value })} className="rounded-lg border px-3 py-2" />
                    <input placeholder="Ghi chú" value={examForm.note} onChange={(e) => setExamForm({ ...examForm, note: e.target.value })} className="rounded-lg border px-3 py-2" />
                    <div className="sm:col-span-4 flex flex-wrap items-center justify-between gap-3">
                      <div className="flex items-center gap-2">
                        <label htmlFor="ex-sem-filter" className="text-xs font-semibold text-slate-600">Xem lịch thi kỳ:</label>
                        <select id="ex-sem-filter" value={examSemId} onChange={(e) => setExamSemId(e.target.value)} className="rounded-lg border px-2.5 py-1.5 text-xs">
                          {semesters.map((s) => <option key={s.id} value={s.id}>{s.code}</option>)}
                        </select>
                      </div>
                      <button type="submit" className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white hover:bg-red-800">
                        Thêm lịch thi
                      </button>
                    </div>
                  </form>

                  <div tabIndex={0} role="region" aria-label="Danh sách lịch thi" className="table-wrapper">
                    <table className="w-full text-left text-sm">
                      <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                        <tr>
                          <th className="px-3 py-2">Lớp HP</th>
                          <th className="px-3 py-2">Học phần</th>
                          <th className="px-3 py-2 text-center">Ngày thi</th>
                          <th className="px-3 py-2 text-center">Giờ thi</th>
                          <th className="px-3 py-2 text-center">Phòng</th>
                          <th className="px-3 py-2">Hình thức</th>
                          <th className="px-3 py-2 text-right">Xóa</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {exams.map((ex) => (
                          <tr key={ex.id} className="hover:bg-slate-50">
                            <td className="px-3 py-2.5 text-sm font-semibold tabular-nums text-red-700">{ex.sectionCode}</td>
                            <td className="px-3 py-2.5 text-sm font-medium">{ex.courseCode} — {ex.courseName}</td>
                            <td className="px-3 py-2.5 text-center text-sm tabular-nums">{ex.examDate}</td>
                            <td className="px-3 py-2.5 text-center text-sm tabular-nums">{ex.startTime.slice(0, 5)}–{ex.endTime.slice(0, 5)}</td>
                            <td className="px-3 py-2.5 text-center text-sm">{ex.classroomCode}</td>
                            <td className="px-3 py-2.5 text-sm">{ex.examFormat}</td>
                            <td className="px-3 py-2.5 text-right">
                              <button type="button" onClick={() => handleDeleteExam(ex.id)} className="rounded border border-red-200 bg-red-50 px-3 py-1.5 text-sm font-semibold text-red-700">
                                Xóa
                              </button>
                            </td>
                          </tr>
                        ))}
                        {exams.length === 0 && (
                          <tr><td colSpan={7} className="px-3 py-4 text-center text-slate-500">Chưa có lịch thi nào trong học kỳ này.</td></tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </Card>
            </div>
          )}

          {tab === "org" && (
            <div className="grid gap-6 lg:grid-cols-2">
              <Card title="Khoa & Bộ môn">
                <form onSubmit={handleCreateFaculty} className="mb-4 grid gap-2 rounded-lg border bg-slate-50 p-3 text-sm sm:grid-cols-3">
                  <input required placeholder="Mã Khoa (VD: DTVT)" value={facForm.code} onChange={(e) => setFacForm({ ...facForm, code: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                  <input required placeholder="Tên Khoa" value={facForm.name} onChange={(e) => setFacForm({ ...facForm, name: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2" />
                  <button type="submit" className="rounded bg-red-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-800 sm:col-span-3">
                    + Thêm Khoa mới
                  </button>
                </form>

                <ul className="space-y-2 text-sm">
                  {faculties.map((f) => <li key={f.id} className="rounded-lg border p-2.5"><strong>{f.code}</strong> — {f.name}</li>)}
                </ul>

                <h3 className="mt-5 mb-2 text-xs font-bold uppercase text-slate-500">Bộ môn trực thuộc</h3>
                <form onSubmit={handleCreateDepartment} className="mb-3 grid gap-2 rounded-lg border bg-slate-50 p-3 text-sm sm:grid-cols-3">
                  <input required placeholder="Mã BM (VD: KHMT)" value={deptForm.code} onChange={(e) => setDeptForm({ ...deptForm, code: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                  <input required placeholder="Tên Bộ môn" value={deptForm.name} onChange={(e) => setDeptForm({ ...deptForm, name: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2" />
                  <select value={deptForm.facultyId} onChange={(e) => setDeptForm({ ...deptForm, facultyId: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2">
                    {faculties.map((f) => <option key={f.id} value={f.id}>{f.code} — {f.name}</option>)}
                  </select>
                  <button type="submit" className="rounded bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white hover:bg-slate-800">
                    + Thêm Bộ môn
                  </button>
                </form>
                <ul className="space-y-1.5 text-sm">
                  {departments.map((d) => <li key={d.id} className="rounded border bg-slate-50 px-3 py-1.5">{d.code} — {d.name} ({d.facultyName})</li>)}
                </ul>
              </Card>

              <Card title="Ngành, Lớp hành chính & Giảng viên">
                <div className="space-y-5 text-sm">
                  <div>
                    <h3 className="mb-2 text-xs font-bold uppercase text-slate-500">Ngành đào tạo</h3>
                    <form onSubmit={handleCreateMajor} className="mb-3 grid gap-2 rounded-lg border bg-slate-50 p-3 sm:grid-cols-3">
                      <input required placeholder="Mã ngành (VD: DTVT)" value={majForm.code} onChange={(e) => setMajForm({ ...majForm, code: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <input required placeholder="Tên ngành" value={majForm.name} onChange={(e) => setMajForm({ ...majForm, name: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2" />
                      <select value={majForm.facultyId} onChange={(e) => setMajForm({ ...majForm, facultyId: e.target.value })} className="rounded border bg-white px-2.5 py-1.5">
                        {faculties.map((f) => <option key={f.id} value={f.id}>{f.code}</option>)}
                      </select>
                      <select aria-label="Hệ đào tạo" value={majForm.degreeLevel} onChange={(e) => setMajForm({ ...majForm, degreeLevel: e.target.value })} className="rounded border bg-white px-2.5 py-1.5">
                        <option value="BACHELOR">Hệ cử nhân (15–25 TC)</option>
                        <option value="ENGINEER">Hệ kỹ sư (16–25 TC)</option>
                      </select>
                      <input required type="number" min={1} placeholder="Tổng TC" value={majForm.totalCreditsRequired} onChange={(e) => setMajForm({ ...majForm, totalCreditsRequired: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <button type="submit" className="rounded bg-red-700 px-3 py-1.5 text-xs font-semibold text-white sm:col-span-3">+ Thêm Ngành</button>
                    </form>
                    {majors.map((m) => (
                      <div key={m.id} className="mb-1 rounded border px-3 py-1.5">
                        <strong>{m.code}</strong> — {m.name} ({m.degreeLevel === "ENGINEER" ? "Hệ kỹ sư" : "Hệ cử nhân"} · {m.totalCreditsRequired} TC · {m.facultyName})
                      </div>
                    ))}
                  </div>

                  <div>
                    <h3 className="mb-2 text-xs font-bold uppercase text-slate-500">Lớp hành chính</h3>
                    <form onSubmit={handleCreateAcademicClass} className="mb-3 grid gap-2 rounded-lg border bg-slate-50 p-3 sm:grid-cols-3">
                      <input required placeholder="Mã lớp (VD: D24CQCN01-B)" value={clsForm.code} onChange={(e) => setClsForm({ ...clsForm, code: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <input required placeholder="Tên lớp" value={clsForm.name} onChange={(e) => setClsForm({ ...clsForm, name: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2" />
                      <select value={clsForm.majorId} onChange={(e) => setClsForm({ ...clsForm, majorId: e.target.value })} className="rounded border bg-white px-2.5 py-1.5">
                        {majors.map((m) => <option key={m.id} value={m.id}>{m.code} — {m.name}</option>)}
                      </select>
                      <input required type="number" min={2000} max={2100} placeholder="Khóa" value={clsForm.cohortYear} onChange={(e) => setClsForm({ ...clsForm, cohortYear: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <button type="submit" className="rounded bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white">+ Thêm Lớp HC</button>
                    </form>
                    {academicClasses.map((ac) => <div key={ac.id} className="mb-1 rounded border px-3 py-1.5">{ac.code} — {ac.name} (Ngành {ac.majorCode} · Khóa {ac.cohortYear})</div>)}
                  </div>

                  <div>
                    <h3 className="mb-2 text-xs font-bold uppercase text-slate-500">Giảng viên (Tự cấp tài khoản đăng nhập)</h3>
                    <form onSubmit={handleCreateLecturer} className="mb-3 grid gap-2 rounded-lg border bg-slate-50 p-3 sm:grid-cols-3">
                      <input required placeholder="Mã GV (VD: GV003)" value={lecForm.lecturerCode} onChange={(e) => setLecForm({ ...lecForm, lecturerCode: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <input required placeholder="Họ tên GV" value={lecForm.fullName} onChange={(e) => setLecForm({ ...lecForm, fullName: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2" />
                      <input required type="email" placeholder="Email GV" value={lecForm.email} onChange={(e) => setLecForm({ ...lecForm, email: e.target.value })} className="rounded border bg-white px-2.5 py-1.5 sm:col-span-2" />
                      <select value={lecForm.departmentId} onChange={(e) => setLecForm({ ...lecForm, departmentId: e.target.value })} className="rounded border bg-white px-2.5 py-1.5">
                        {departments.map((d) => <option key={d.id} value={d.id}>{d.code} — {d.name}</option>)}
                      </select>
                      <input placeholder="Học vị (ThS/TS)" value={lecForm.degree} onChange={(e) => setLecForm({ ...lecForm, degree: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <input placeholder="Mật khẩu (trống = Ptit@12345)" value={lecForm.initialPassword} onChange={(e) => setLecForm({ ...lecForm, initialPassword: e.target.value })} className="rounded border bg-white px-2.5 py-1.5" />
                      <button type="submit" className="rounded bg-red-700 px-3 py-1.5 text-xs font-semibold text-white">+ Thêm Giảng viên</button>
                    </form>
                    {lecturers.map((l) => <div key={l.id} className="mb-1 rounded border px-3 py-1.5">{l.lecturerCode} — {l.fullName} ({l.departmentName} · {l.email})</div>)}
                  </div>
                </div>
              </Card>
            </div>
          )}

          {tab === "reports" && (
            <div className="space-y-6">
              {stuStats && (
                <Card title="Thống kê quân số Sinh viên" subtitle={`Tổng số sinh viên: ${stuStats.totalStudents}`}>
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <h3 className="mb-2 text-xs font-bold uppercase text-slate-500">Theo trạng thái học vụ</h3>
                      {stuStats.byAcademicStatus.map((x: any) => (
                        <div key={x.label} className="flex justify-between border-b py-1.5 text-sm"><span>{x.label}</span><strong>{x.count}</strong></div>
                      ))}
                    </div>
                    <div>
                      <h3 className="mb-2 text-xs font-bold uppercase text-slate-500">Theo ngành</h3>
                      {stuStats.byMajor.map((x: any) => (
                        <div key={x.label} className="flex justify-between border-b py-1.5 text-sm"><span>{x.label}</span><strong>{x.count}</strong></div>
                      ))}
                    </div>
                  </div>
                </Card>
              )}
              {capReport && (
                <Card title="Tỷ lệ lấp đầy các lớp học phần" subtitle={`Tổng chỗ đã ĐK: ${capReport.totalEnrolledSeats} / ${capReport.totalMaxCapacity}`}>
                  <div className="space-y-2 text-sm">
                    {capReport.sections.map((s: any) => (
                      <div key={s.sectionId} className="flex items-center justify-between border-b py-1.5">
                        <span><strong>{s.sectionCode}</strong> — {s.courseName}</span>
                        <span>{s.currentEnrolled}/{s.maxCapacity} ({s.fillRatePercent}%)</span>
                      </div>
                    ))}
                  </div>
                </Card>
              )}
              {distReport && (
                <Card title="Phổ điểm toàn trường" subtitle={`Tỷ lệ đạt: ${distReport.passRatePercent}% (${distReport.passedCount}/${distReport.totalGraded})`}>
                  <div className="grid grid-cols-4 gap-2 sm:grid-cols-8">
                    {distReport.distribution.map((d: any) => (
                      <div key={d.label} className="rounded border bg-slate-50 p-2 text-center">
                        <div className="font-bold">{d.count}</div>
                        <div className="text-xs text-slate-600">{d.label}</div>
                      </div>
                    ))}
                  </div>
                </Card>
              )}
            </div>
          )}
        </div>
      )}
    </AppShell>
  );
}
