"use client";

import React, { useEffect, useMemo, useState } from "react";

export interface TimetableSlot {
  sectionId: number;
  sectionCode: string;
  courseCode: string;
  courseName: string;
  credits: number;
  lecturerName: string;
  dayOfWeek: number;
  startPeriod: number;
  endPeriod: number;
  startWeek: number;
  endWeek: number;
  classroomCode: string;
  building: string;
}

interface TimetableGridProps {
  slots: TimetableSlot[];
  semesterStartDate?: string | null;
  semesterEndDate?: string | null;
}

const DAYS = [
  { value: 2, label: "Thứ 2" },
  { value: 3, label: "Thứ 3" },
  { value: 4, label: "Thứ 4" },
  { value: 5, label: "Thứ 5" },
  { value: 6, label: "Thứ 6" },
  { value: 7, label: "Thứ 7" },
];

const PERIOD_TIMES: Record<number, string> = {
  1: "07:00",
  2: "07:50",
  3: "08:50",
  4: "09:40",
  5: "10:40",
  6: "11:30",
  7: "13:00",
  8: "13:50",
  9: "14:50",
  10: "15:40",
  11: "17:00",
  12: "17:50",
};

const PERIODS = Array.from({ length: 12 }, (_, i) => i + 1);

function parseIsoDate(dateStr?: string | null): Date | null {
  if (!dateStr) return null;
  const parts = dateStr.split("-").map(Number);
  if (parts.length !== 3 || parts.some(Number.isNaN)) return null;
  return new Date(parts[0], parts[1] - 1, parts[2]);
}

function getMondayOfWeek(d: Date): Date {
  const copy = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  const day = copy.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  copy.setDate(copy.getDate() + diff);
  return copy;
}

function addDays(d: Date, days: number): Date {
  const copy = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  copy.setDate(copy.getDate() + days);
  return copy;
}

function fmtDdMm(d: Date): string {
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  return `${dd}/${mm}`;
}

function fmtDdMmYyyy(d: Date): string {
  return `${fmtDdMm(d)}/${d.getFullYear()}`;
}

export function TimetableGrid({ slots, semesterStartDate, semesterEndDate }: TimetableGridProps) {
  const maxWeekInSlots = useMemo(() => {
    const maxSlotWeek = slots.reduce((acc, s) => Math.max(acc, s.endWeek || 1), 15);
    return Math.max(15, maxSlotWeek);
  }, [slots]);

  const baseMonday = useMemo(() => {
    const start = parseIsoDate(semesterStartDate);
    return start ? getMondayOfWeek(start) : getMondayOfWeek(new Date());
  }, [semesterStartDate]);

  const totalWeeks = useMemo(() => {
    const end = parseIsoDate(semesterEndDate);
    if (!end) return maxWeekInSlots;
    const diffDays = Math.ceil((end.getTime() - baseMonday.getTime()) / (1000 * 60 * 60 * 24));
    const computed = Math.max(1, Math.ceil(diffDays / 7));
    return Math.max(maxWeekInSlots, Math.min(25, computed));
  }, [baseMonday, semesterEndDate, maxWeekInSlots]);

  const [viewMode, setViewMode] = useState<"week" | "all">("week");
  const [selectedWeek, setSelectedWeek] = useState<number>(1);

  useEffect(() => {
    const start = parseIsoDate(semesterStartDate);
    const end = parseIsoDate(semesterEndDate);
    const today = new Date();
    if (start && end && today >= start && today <= end) {
      const diff = Math.floor((today.getTime() - baseMonday.getTime()) / (1000 * 60 * 60 * 24 * 7)) + 1;
      setSelectedWeek(Math.min(totalWeeks, Math.max(1, diff)));
    } else {
      setSelectedWeek(1);
    }
  }, [semesterStartDate, semesterEndDate, baseMonday, totalWeeks]);

  const visibleSlots = useMemo(() => {
    if (viewMode === "all") return slots;
    return slots.filter((s) => selectedWeek >= s.startWeek && selectedWeek <= s.endWeek);
  }, [slots, viewMode, selectedWeek]);

  const currentWeekMonday = useMemo(
    () => addDays(baseMonday, (selectedWeek - 1) * 7),
    [baseMonday, selectedWeek]
  );

  return (
    <div className="space-y-4">
      {/* Thanh điều hướng Thời khóa biểu dạng tuần */}
      <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 bg-white p-4">
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => {
              setViewMode("week");
              setSelectedWeek((w) => Math.max(1, w - 1));
            }}
            disabled={viewMode === "week" && selectedWeek <= 1}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-40"
          >
            &larr; Tuần trước
          </button>

          <select
            aria-label="Chọn tuần học"
            value={viewMode === "all" ? "ALL" : String(selectedWeek)}
            onChange={(e) => {
              if (e.target.value === "ALL") {
                setViewMode("all");
              } else {
                setViewMode("week");
                setSelectedWeek(Number(e.target.value));
              }
            }}
            className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-800"
          >
            <option value="ALL">Tất cả các tuần (Tổng hợp học kỳ)</option>
            {Array.from({ length: totalWeeks }, (_, idx) => {
              const w = idx + 1;
              const mon = addDays(baseMonday, idx * 7);
              const sun = addDays(mon, 6);
              return (
                <option key={w} value={String(w)}>
                  Tuần {w} ({fmtDdMmYyyy(mon)} - {fmtDdMmYyyy(sun)})
                </option>
              );
            })}
          </select>

          <button
            type="button"
            onClick={() => {
              setViewMode("week");
              setSelectedWeek((w) => Math.min(totalWeeks, w + 1));
            }}
            disabled={viewMode === "week" && selectedWeek >= totalWeeks}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-40"
          >
            Tuần sau &rarr;
          </button>
        </div>

        <div className="flex items-center gap-2 text-xs">
          <button
            type="button"
            onClick={() => setViewMode("week")}
            className={`rounded-lg px-3 py-1.5 font-semibold transition ${
              viewMode === "week"
                ? "bg-red-700 text-white"
                : "border border-slate-300 text-slate-700 hover:bg-slate-50"
            }`}
          >
            Xem theo tuần
          </button>
          <button
            type="button"
            onClick={() => setViewMode("all")}
            className={`rounded-lg px-3 py-1.5 font-semibold transition ${
              viewMode === "all"
                ? "bg-red-700 text-white"
                : "border border-slate-300 text-slate-700 hover:bg-slate-50"
            }`}
          >
            Cả học kỳ
          </button>
        </div>
      </div>

      {visibleSlots.length === 0 ? (
        <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 p-8 text-center text-sm text-slate-500">
          {slots.length === 0
            ? "Chưa có lịch học / giảng dạy nào trong học kỳ đã chọn."
            : `Tuần ${selectedWeek} không có buổi học / giảng dạy nào.`}
        </div>
      ) : (
        <>
          {/* Lưới 12 tiết trên màn hình rộng */}
          <div
            tabIndex={0}
            role="region"
            aria-label="Lưới thời khóa biểu theo tuần"
            className="table-wrapper hidden rounded-xl border border-slate-200 bg-white md:block"
          >
            <table className="w-full border-collapse text-xs">
              <thead>
                <tr className="bg-slate-900 text-white">
                  <th className="w-24 border border-slate-700 px-2 py-2.5 text-center font-semibold">
                    Tiết / Giờ
                  </th>
                  {DAYS.map((d, i) => (
                    <th key={d.value} className="min-w-[140px] border border-slate-700 px-2 py-2.5 text-center font-semibold">
                      <div>{d.label}</div>
                      {viewMode === "week" && (
                        <div className="mt-0.5 text-[11px] font-normal text-slate-300">
                          {fmtDdMm(addDays(currentWeekMonday, i))}
                        </div>
                      )}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {PERIODS.map((period) => (
                  <tr key={period} className={period === 7 || period === 11 ? "border-t-2 border-slate-300" : ""}>
                    <th className="border border-slate-200 bg-slate-50 px-2 py-2 text-center font-semibold text-slate-700">
                      <div>Tiết {period}</div>
                      <div className="text-[10px] font-normal text-slate-500">{PERIOD_TIMES[period]}</div>
                    </th>
                    {DAYS.map((day) => {
                      const starting = visibleSlots.filter(
                        (s) => s.dayOfWeek === day.value && s.startPeriod === period
                      );
                      const covered = visibleSlots.some(
                        (s) => s.dayOfWeek === day.value && period > s.startPeriod && period <= s.endPeriod
                      );
                      if (covered && starting.length === 0) {
                        return null;
                      }
                      if (starting.length > 0) {
                        const first = starting[0];
                        const span = first.endPeriod - first.startPeriod + 1;
                        return (
                          <td
                            key={day.value}
                            rowSpan={span}
                            className="border border-slate-200 bg-red-50/70 p-2 align-top"
                          >
                            <div className="space-y-1">
                              <span className="inline-block rounded bg-red-700 px-1.5 py-0.5 text-[10px] font-bold text-white">
                                {first.courseCode}
                              </span>
                              <p className="font-semibold leading-tight text-slate-900">{first.courseName}</p>
                              <p className="text-[11px] text-slate-600">
                                Phòng: <strong>{first.classroomCode}</strong> ({first.building})
                              </p>
                              <p className="text-[11px] text-slate-600">
                                Nhóm: {first.sectionCode} · Tuần {first.startWeek}-{first.endWeek}
                              </p>
                              <p className="text-[11px] text-slate-500">{first.lecturerName}</p>
                            </div>
                          </td>
                        );
                      }
                      return <td key={day.value} className="h-9 border border-slate-100 bg-white" />;
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Dạng thẻ gọn cho Mobile */}
          <div className="grid gap-3 md:hidden">
            {visibleSlots.map((s, idx) => (
              <div key={`${s.sectionId}-${idx}`} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="flex items-center justify-between gap-2">
                  <span className="rounded bg-red-700 px-2 py-0.5 text-xs font-bold text-white">
                    Thứ {s.dayOfWeek}
                    {viewMode === "week" ? ` (${fmtDdMm(addDays(currentWeekMonday, s.dayOfWeek - 2))})` : ""} · Tiết{" "}
                    {s.startPeriod}-{s.endPeriod}
                  </span>
                  <span className="text-xs font-semibold text-slate-600">Phòng {s.classroomCode}</span>
                </div>
                <p className="mt-2 text-sm font-bold text-slate-900">
                  {s.courseCode} — {s.courseName}
                </p>
                <p className="mt-1 text-xs text-slate-500">
                  Nhóm: {s.sectionCode} · Tuần {s.startWeek}-{s.endWeek} · {s.lecturerName}
                </p>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
