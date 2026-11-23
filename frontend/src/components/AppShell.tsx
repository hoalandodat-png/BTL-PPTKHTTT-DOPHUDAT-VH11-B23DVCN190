"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { apiFetch, cn, UserProfile } from "@/lib/api";
import {
  GraduationCap,
  LogOut,
  Menu,
  X,
  KeyRound,
  ShieldCheck,
  Building2,
  BookOpen,
  CalendarDays,
  ClipboardCheck,
  Award,
  BarChart3,
  UserCheck,
} from "lucide-react";

export interface NavItem {
  id: string;
  label: string;
  icon?: React.ReactNode;
}

interface AppShellProps {
  requiredRole?: string;
  title: string;
  subtitle?: string;
  navItems: NavItem[];
  activeTab: string;
  onTabChange: (id: string) => void;
  children: (user: UserProfile) => React.ReactNode;
}

export function AppShell({
  requiredRole,
  title,
  subtitle,
  navItems,
  activeTab,
  onTabChange,
  children,
}: AppShellProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [pwdOpen, setPwdOpen] = useState(false);

  useEffect(() => {
    let active = true;
    apiFetch<UserProfile>("/auth/me")
      .then((profile) => {
        if (!active) return;
        if (requiredRole && !profile.roles.includes(requiredRole)) {
          router.replace(profile.redirectPath || "/login");
          return;
        }
        setUser(profile);
        setLoading(false);
      })
      .catch(() => {
        if (active) router.replace("/login");
      });
    return () => {
      active = false;
    };
  }, [requiredRole, router, pathname]);

  async function handleLogout() {
    try {
      await apiFetch("/auth/logout", { method: "POST" });
    } catch {
      // ignore
    }
    router.replace("/login");
  }

  if (loading || !user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3 text-slate-600">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-red-700 border-t-transparent" />
          <p className="text-sm font-medium">Đang tải không gian làm việc...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Desktop Sidebar */}
      <aside className="hidden w-64 shrink-0 flex-col bg-slate-900 text-slate-100 lg:flex">
        <div className="flex items-center gap-3 border-b border-slate-800 px-5 py-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-red-700 text-white">
            <GraduationCap className="h-6 w-6" />
          </div>
          <div>
            <p className="text-sm font-bold tracking-wide text-white">PTIT PORTAL</p>
            <p className="text-xs text-slate-400">Quản lý Đào tạo</p>
          </div>
        </div>

        <nav className="flex-1 space-y-1 p-3" aria-label="Điều hướng chính">
          {navItems.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => onTabChange(item.id)}
              className={cn(
                "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm font-medium transition-colors",
                activeTab === item.id
                  ? "bg-red-700 text-white shadow-sm"
                  : "text-slate-300 hover:bg-slate-800 hover:text-white"
              )}
            >
              {item.icon}
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        <div className="border-t border-slate-800 p-4 space-y-2">
          <div className="rounded-lg bg-slate-800/70 px-3 py-2">
            <p className="text-xs font-semibold text-white truncate">{user.username}</p>
            <p className="text-[11px] text-slate-400 truncate">{user.email}</p>
          </div>
          <button
            type="button"
            onClick={() => setPwdOpen(true)}
            className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-xs font-medium text-slate-300 hover:bg-slate-800 hover:text-white"
          >
            <KeyRound className="h-4 w-4" />
            Đổi mật khẩu
          </button>
          <button
            type="button"
            onClick={handleLogout}
            className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-xs font-medium text-red-300 hover:bg-red-950/60 hover:text-red-200"
          >
            <LogOut className="h-4 w-4" />
            Đăng xuất
          </button>
        </div>
      </aside>

      {/* Mobile Drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 flex lg:hidden">
          <div className="fixed inset-0 bg-black/50" onClick={() => setMobileOpen(false)} />
          <div className="relative flex w-72 max-w-[85vw] flex-col bg-slate-900 text-white shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-800 px-4 py-3">
              <span className="font-bold">PTIT PORTAL</span>
              <button
                type="button"
                aria-label="Đóng menu"
                onClick={() => setMobileOpen(false)}
                className="rounded p-1.5 hover:bg-slate-800"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <nav className="flex-1 space-y-1 p-3">
              {navItems.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => {
                    onTabChange(item.id);
                    setMobileOpen(false);
                  }}
                  className={cn(
                    "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm font-medium",
                    activeTab === item.id ? "bg-red-700 text-white" : "text-slate-300 hover:bg-slate-800"
                  )}
                >
                  {item.icon}
                  <span>{item.label}</span>
                </button>
              ))}
            </nav>
            <div className="border-t border-slate-800 p-3 space-y-2">
              <button
                type="button"
                onClick={() => {
                  setMobileOpen(false);
                  setPwdOpen(true);
                }}
                className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-slate-300 hover:bg-slate-800"
              >
                <KeyRound className="h-4 w-4" /> Đổi mật khẩu
              </button>
              <button
                type="button"
                onClick={handleLogout}
                className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-red-300 hover:bg-red-950/60"
              >
                <LogOut className="h-4 w-4" /> Đăng xuất
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Main Content */}
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 sm:px-6">
          <div className="flex items-center gap-3">
            <button
              type="button"
              aria-label="Mở menu"
              onClick={() => setMobileOpen(true)}
              className="rounded-lg border border-slate-200 p-2 text-slate-700 hover:bg-slate-100 lg:hidden"
            >
              <Menu className="h-5 w-5" />
            </button>
            <div>
              <h1 className="text-base font-bold text-slate-900 sm:text-lg">{title}</h1>
              {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-700 sm:inline-flex">
              {user.roles.join(" · ")}
            </span>
            <Link
              href="/login"
              className="text-xs font-medium text-slate-500 hover:text-slate-900 hidden sm:inline"
            >
              Đổi tài khoản
            </Link>
          </div>
        </header>

        <main className="flex-1 p-4 sm:p-6">{children(user)}</main>
      </div>

      {pwdOpen && <ChangePasswordDialog onClose={() => setPwdOpen(false)} />}
    </div>
  );
}

function ChangePasswordDialog({ onClose }: { onClose: () => void }) {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [status, setStatus] = useState<{ type: "error" | "success"; text: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setStatus(null);
    if (newPassword.length < 8) {
      setStatus({ type: "error", text: "Mật khẩu mới phải từ 8 ký tự trở lên." });
      return;
    }
    if (newPassword !== confirmPassword) {
      setStatus({ type: "error", text: "Xác nhận mật khẩu mới không khớp." });
      return;
    }
    setSubmitting(true);
    try {
      await apiFetch("/auth/change-password", {
        method: "PUT",
        body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
      });
      setStatus({ type: "success", text: "Đổi mật khẩu thành công!" });
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setStatus({ type: "error", text: (err as Error).message });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="change-pwd-title"
    >
      <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 id="change-pwd-title" className="text-lg font-bold text-slate-900">
            Đổi mật khẩu cá nhân
          </h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Đóng hộp thoại"
            className="rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="cp-current" className="mb-1 block text-sm font-medium text-slate-700">
              Mật khẩu hiện tại *
            </label>
            <input
              id="cp-current"
              type="password"
              required
              autoComplete="current-password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label htmlFor="cp-new" className="mb-1 block text-sm font-medium text-slate-700">
              Mật khẩu mới (tối thiểu 8 ký tự) *
            </label>
            <input
              id="cp-new"
              type="password"
              required
              minLength={8}
              autoComplete="new-password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label htmlFor="cp-confirm" className="mb-1 block text-sm font-medium text-slate-700">
              Nhập lại mật khẩu mới *
            </label>
            <input
              id="cp-confirm"
              type="password"
              required
              minLength={8}
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>

          {status && (
            <div
              role="status"
              aria-live="polite"
              className={cn(
                "rounded-lg px-3 py-2 text-sm font-medium",
                status.type === "error" ? "bg-red-50 text-red-700" : "bg-emerald-50 text-emerald-700"
              )}
            >
              {status.text}
            </div>
          )}

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Đóng
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="rounded-lg bg-red-700 px-4 py-2 text-sm font-semibold text-white hover:bg-red-800 disabled:opacity-50"
            >
              {submitting ? "Đang lưu..." : "Cập nhật mật khẩu"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export const PortalIcons = {
  ShieldCheck: <ShieldCheck className="h-4 w-4" />,
  Building2: <Building2 className="h-4 w-4" />,
  UserCheck: <UserCheck className="h-4 w-4" />,
  BookOpen: <BookOpen className="h-4 w-4" />,
  CalendarDays: <CalendarDays className="h-4 w-4" />,
  ClipboardCheck: <ClipboardCheck className="h-4 w-4" />,
  Award: <Award className="h-4 w-4" />,
  BarChart3: <BarChart3 className="h-4 w-4" />,
};
