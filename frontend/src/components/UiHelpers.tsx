import React from "react";
import { cn } from "@/lib/api";

export function StatusNotice({
  notice,
  onClear,
}: {
  notice: { type: "error" | "success" | "info"; text: string } | null;
  onClear?: () => void;
}) {
  if (!notice) return null;
  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        "mb-4 flex items-center justify-between rounded-lg border px-4 py-3 text-sm font-medium",
        notice.type === "error" && "border-red-200 bg-red-50 text-red-800",
        notice.type === "success" && "border-emerald-200 bg-emerald-50 text-emerald-800",
        notice.type === "info" && "border-sky-200 bg-sky-50 text-sky-800"
      )}
    >
      <span>{notice.text}</span>
      {onClear && (
        <button
          type="button"
          onClick={onClear}
          className="ml-4 text-xs underline opacity-75 hover:opacity-100"
        >
          Ẩn
        </button>
      )}
    </div>
  );
}

export function Card({
  title,
  subtitle,
  action,
  children,
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <section className="rounded-xl border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4">
        <div>
          <h2 className="text-base font-bold text-slate-900">{title}</h2>
          {subtitle && <p className="mt-0.5 text-sm text-slate-500">{subtitle}</p>}
        </div>
        {action}
      </div>
      <div className="p-5">{children}</div>
    </section>
  );
}
