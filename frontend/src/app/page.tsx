"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { apiFetch, UserProfile } from "@/lib/api";

export default function HomePage() {
  const router = useRouter();

  useEffect(() => {
    apiFetch<UserProfile>("/auth/me")
      .then((profile) => router.replace(profile.redirectPath || "/login"))
      .catch(() => router.replace("/login"));
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50">
      <div className="flex flex-col items-center gap-3 text-slate-600">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-red-700 border-t-transparent" />
        <p className="text-sm font-medium">Đang kiểm tra phiên đăng nhập...</p>
      </div>
    </div>
  );
}
