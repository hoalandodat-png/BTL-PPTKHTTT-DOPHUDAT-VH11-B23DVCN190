"use client";

import React, { Suspense, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { apiFetch } from "@/lib/api";

function ResetForm() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") || "";
  const [newPassword, setNewPassword] = useState("");
  const [status, setStatus] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setStatus(null);
    setError(null);
    setSubmitting(true);
    try {
      const res = await apiFetch<{ message: string }>("/auth/reset-password", {
        method: "POST",
        body: JSON.stringify({ token, newPassword }),
      });
      setStatus(res.message);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-lg sm:p-8">
      <h1 className="text-xl font-bold text-slate-900">Đặt lại mật khẩu mới</h1>
      <form onSubmit={handleSubmit} className="mt-5 space-y-4">
        <div>
          <label htmlFor="rp-pwd" className="mb-1 block text-sm font-medium text-slate-700">
            Mật khẩu mới (tối thiểu 8 ký tự) *
          </label>
          <input
            id="rp-pwd"
            type="password"
            required
            minLength={8}
            autoComplete="new-password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>
        {status && <div role="status" className="rounded-lg bg-emerald-50 p-3 text-sm text-emerald-700">{status}</div>}
        {error && <div role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>}
        <button
          type="submit"
          disabled={submitting || !token}
          className="w-full rounded-lg bg-red-700 py-2.5 text-sm font-semibold text-white hover:bg-red-800 disabled:opacity-50"
        >
          {submitting ? "Đang cập nhật..." : "Xác nhận đặt lại mật khẩu"}
        </button>
        <div className="text-center">
          <Link href="/login" className="text-xs font-medium text-red-700 hover:underline">
            Về trang đăng nhập
          </Link>
        </div>
      </form>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <Suspense fallback={<div>Đang tải...</div>}>
        <ResetForm />
      </Suspense>
    </div>
  );
}
