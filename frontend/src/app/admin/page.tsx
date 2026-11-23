"use client";

import React, { useCallback, useEffect, useState } from "react";
import { AppShell, PortalIcons } from "@/components/AppShell";
import { Card, StatusNotice } from "@/components/UiHelpers";
import { apiFetch, PageResponse } from "@/lib/api";

const NAV_ITEMS = [
  { id: "users", label: "Quản lý Tài khoản", icon: PortalIcons.UserCheck },
  { id: "rbac", label: "Vai trò & Quyền hạn (RBAC)", icon: PortalIcons.ShieldCheck },
  { id: "audit", label: "Nhật ký Kiểm toán", icon: PortalIcons.ClipboardCheck },
  { id: "configs", label: "Thang điểm & Quy chế", icon: PortalIcons.BarChart3 },
];

export default function AdminPortalPage() {
  const [tab, setTab] = useState("users");
  const [notice, setNotice] = useState<{ type: "error" | "success" | "info"; text: string } | null>(null);

  const [users, setUsers] = useState<any[]>([]);
  const [roles, setRoles] = useState<any[]>([]);
  const [allPermissions, setAllPermissions] = useState<any[]>([]);
  const [selectedPermByRole, setSelectedPermByRole] = useState<Record<number, string>>({});
  const [busyRoleId, setBusyRoleId] = useState<number | null>(null);
  const [auditLogs, setAuditLogs] = useState<any[]>([]);
  const [scales, setScales] = useState<any[]>([]);
  const [configs, setConfigs] = useState<any[]>([]);
  const [lockTarget, setLockTarget] = useState<{ id: number; username: string; nextStatus: string; reason: string } | null>(null);

  const loadData = useCallback(async () => {
    try {
      if (tab === "users") {
        const res = await apiFetch<PageResponse<any>>("/admin/users?size=50");
        setUsers(res.items);
      } else if (tab === "rbac") {
        const [r, perms] = await Promise.all([
          apiFetch<any[]>("/admin/roles"),
          apiFetch<any[]>("/admin/permissions"),
        ]);
        setRoles(r);
        setAllPermissions(perms);
      } else if (tab === "audit") {
        const res = await apiFetch<PageResponse<any>>("/admin/audit-logs?size=50");
        setAuditLogs(res.items);
      } else if (tab === "configs") {
        const [sc, cfg] = await Promise.all([
          apiFetch<any[]>("/system-configs/grading-scales"),
          apiFetch<any[]>("/system-configs"),
        ]);
        setScales(sc);
        setConfigs(cfg);
      }
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }, [tab]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  async function handleConfirmLock(e: React.FormEvent) {
    e.preventDefault();
    if (!lockTarget) return;
    setNotice(null);
    try {
      await apiFetch(`/admin/users/${lockTarget.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: lockTarget.nextStatus, reason: lockTarget.reason }),
      });
      setNotice({
        type: "success",
        text: `Đã chuyển tài khoản ${lockTarget.username} sang ${lockTarget.nextStatus} và thu hồi Refresh Token.`,
      });
      setLockTarget(null);
      await loadData();
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    }
  }

  async function handleAddPermission(role: any) {
    const permIdStr = selectedPermByRole[role.id];
    if (!permIdStr) return;
    const permId = Number(permIdStr);
    const currentIds: number[] = role.permissions.map((p: any) => p.id);
    if (currentIds.includes(permId)) return;

    setNotice(null);
    setBusyRoleId(role.id);
    try {
      const updated = await apiFetch<any>(`/admin/roles/${role.id}/permissions`, {
        method: "PUT",
        body: JSON.stringify({ permissionIds: [...currentIds, permId] }),
      });
      setRoles((prev) => prev.map((r) => (r.id === role.id ? updated : r)));
      setSelectedPermByRole((prev) => ({ ...prev, [role.id]: "" }));
      setNotice({ type: "success", text: `Đã thêm quyền cho vai trò ${role.code} và ghi Audit Log.` });
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    } finally {
      setBusyRoleId(null);
    }
  }

  async function handleRemovePermission(role: any, perm: any) {
    if (role.code === "ADMIN" && perm.code === "RBAC_MANAGE") {
      setNotice({ type: "error", text: "Không thể xóa quyền RBAC_MANAGE khỏi vai trò ADMIN." });
      return;
    }
    const nextIds: number[] = role.permissions
      .filter((p: any) => p.id !== perm.id)
      .map((p: any) => p.id);
    if (nextIds.length === 0) {
      setNotice({ type: "error", text: `Vai trò ${role.code} phải giữ lại ít nhất 1 quyền.` });
      return;
    }

    setNotice(null);
    setBusyRoleId(role.id);
    try {
      const updated = await apiFetch<any>(`/admin/roles/${role.id}/permissions`, {
        method: "PUT",
        body: JSON.stringify({ permissionIds: nextIds }),
      });
      setRoles((prev) => prev.map((r) => (r.id === role.id ? updated : r)));
      setNotice({ type: "success", text: `Đã xóa quyền ${perm.code} khỏi vai trò ${role.code}.` });
    } catch (err) {
      setNotice({ type: "error", text: (err as Error).message });
    } finally {
      setBusyRoleId(null);
    }
  }

  return (
    <AppShell
      requiredRole="ADMIN"
      title="Quản trị Hệ thống (Admin)"
      subtitle="Quản lý tài khoản, khóa phiên tức thì, RBAC & Nhật ký kiểm toán"
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

          {tab === "users" && (
            <div className="space-y-6">
              {lockTarget && (
                <Card title={`${lockTarget.nextStatus === "LOCKED" ? "Khóa" : "Mở khóa"} tài khoản: ${lockTarget.username}`}>
                  <form onSubmit={handleConfirmLock} className="flex flex-wrap items-end gap-3 text-sm">
                    <div className="flex-1">
                      <label className="mb-1 block text-xs font-semibold">Lý do bắt buộc *</label>
                      <input
                        required
                        value={lockTarget.reason}
                        onChange={(e) => setLockTarget({ ...lockTarget, reason: e.target.value })}
                        placeholder="Nhập lý do khóa/mở khóa tài khoản..."
                        className="w-full rounded-lg border px-3 py-2"
                      />
                    </div>
                    <button type="submit" className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white">Xác nhận</button>
                    <button type="button" onClick={() => setLockTarget(null)} className="rounded-lg border px-3 py-2">Hủy</button>
                  </form>
                </Card>
              )}

              <Card title="Danh sách tài khoản người dùng">
                <div tabIndex={0} role="region" aria-label="Danh sách tài khoản" className="table-wrapper">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                      <tr>
                        <th className="px-3 py-2">Username</th>
                        <th className="px-3 py-2">Email</th>
                        <th className="px-3 py-2">Vai trò</th>
                        <th className="px-3 py-2 text-center">Trạng thái</th>
                        <th className="px-3 py-2 text-right">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {users.map((u) => (
                        <tr key={u.id} className="hover:bg-slate-50">
                          <td className="px-3 py-3 text-sm font-semibold tabular-nums text-red-700">{u.username}</td>
                          <td className="px-3 py-3 text-sm">{u.email}</td>
                          <td className="px-3 py-3 text-sm font-semibold">{u.roles.join(", ")}</td>
                          <td className="px-3 py-3 text-center">
                            <span className={`inline-block rounded-full px-3 py-0.5 text-sm font-semibold ${u.status === "ACTIVE" ? "bg-emerald-100 text-emerald-700" : "bg-red-100 text-red-700"}`}>
                              {u.status}
                            </span>
                          </td>
                          <td className="px-3 py-3 text-right">
                            <button
                              type="button"
                              onClick={() =>
                                setLockTarget({
                                  id: u.id,
                                  username: u.username,
                                  nextStatus: u.status === "ACTIVE" ? "LOCKED" : "ACTIVE",
                                  reason: "",
                                })
                              }
                              className="rounded border border-slate-300 px-3 py-1.5 text-sm font-semibold hover:bg-slate-50"
                            >
                              {u.status === "ACTIVE" ? "Khóa tài khoản" : "Mở khóa"}
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

          {tab === "rbac" && (
            <div className="grid gap-4 lg:grid-cols-2">
              {roles.map((r) => {
                const assignedIds = new Set(r.permissions.map((p: any) => p.id));
                const availablePerms = allPermissions.filter((p) => !assignedIds.has(p.id));
                const isBusy = busyRoleId === r.id;
                return (
                  <Card key={r.id} title={`${r.code} — ${r.name}`} subtitle={r.description}>
                    <div className="space-y-4">
                      <div className="flex flex-wrap gap-2">
                        {r.permissions.map((p: any) => {
                          const protectedAdminPerm = r.code === "ADMIN" && p.code === "RBAC_MANAGE";
                          return (
                            <span
                              key={p.id}
                              title={p.name}
                              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1 text-sm font-medium text-slate-800"
                            >
                              <span>{p.code}</span>
                              {!protectedAdminPerm && (
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  aria-label={`Xóa quyền ${p.code} khỏi ${r.code}`}
                                  onClick={() => handleRemovePermission(r, p)}
                                  className="rounded px-1 text-slate-400 transition hover:bg-red-100 hover:text-red-700 disabled:opacity-50"
                                >
                                  &times;
                                </button>
                              )}
                            </span>
                          );
                        })}
                      </div>

                      <div className="flex flex-wrap items-center gap-2 border-t border-slate-100 pt-3">
                        <select
                          aria-label={`Chọn quyền thêm cho ${r.code}`}
                          value={selectedPermByRole[r.id] || ""}
                          onChange={(e) =>
                            setSelectedPermByRole((prev) => ({ ...prev, [r.id]: e.target.value }))
                          }
                          disabled={availablePerms.length === 0 || isBusy}
                          className="min-w-0 flex-1 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm disabled:bg-slate-100"
                        >
                          <option value="">
                            {availablePerms.length === 0
                              ? "Đã có toàn bộ quyền"
                              : "-- Chọn quyền cần thêm --"}
                          </option>
                          {availablePerms.map((p) => (
                            <option key={p.id} value={p.id}>
                              {p.code} — {p.name}
                            </option>
                          ))}
                        </select>
                        <button
                          type="button"
                          disabled={!selectedPermByRole[r.id] || isBusy}
                          onClick={() => handleAddPermission(r)}
                          className="rounded-lg bg-red-700 px-3.5 py-1.5 text-sm font-semibold text-white hover:bg-red-800 disabled:opacity-50"
                        >
                          {isBusy ? "Đang lưu..." : "+ Thêm quyền"}
                        </button>
                      </div>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}

          {tab === "audit" && (
            <Card title="Nhật ký Kiểm toán bất biến (Audit Logs)">
              <div tabIndex={0} role="region" aria-label="Nhật ký kiểm toán" className="table-wrapper">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                    <tr>
                      <th className="px-3 py-2.5">Thời gian</th>
                      <th className="px-3 py-2.5">Người thực hiện</th>
                      <th className="px-3 py-2.5">Hành động</th>
                      <th className="px-3 py-2.5">Đối tượng</th>
                      <th className="px-3 py-2.5">Lý do & Chi tiết</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {auditLogs.map((a) => (
                      <tr key={a.id} className="hover:bg-slate-50">
                        <td className="px-3 py-2.5 whitespace-nowrap text-sm tabular-nums">{new Date(a.createdAt).toLocaleString("vi-VN")}</td>
                        <td className="px-3 py-2.5 text-sm font-semibold">{a.actorUsername}</td>
                        <td className="px-3 py-2.5 text-sm font-bold text-red-700">{a.action}</td>
                        <td className="px-3 py-2.5 text-sm">{a.entityType} #{a.entityId}</td>
                        <td className="px-3 py-2.5 text-sm">
                          <div>{a.reason}</div>
                          {a.newValueJson && <code className="text-xs text-slate-500">{a.newValueJson}</code>}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}

          {tab === "configs" && (
            <div className="grid gap-6 lg:grid-cols-2">
              <Card title="Bảng quy đổi Thang điểm (Grading Scales)">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-100 text-xs uppercase text-slate-600">
                    <tr>
                      <th className="px-3 py-2">Hệ 10</th>
                      <th className="px-3 py-2 text-center">Điểm chữ</th>
                      <th className="px-3 py-2 text-center">Hệ 4</th>
                      <th className="px-3 py-2 text-center">Kết quả</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {scales.map((s) => (
                      <tr key={s.id}>
                        <td className="px-3 py-2">{s.minScore10} – {s.maxScore10}</td>
                        <td className="px-3 py-2 text-center font-bold">{s.letterGrade}</td>
                        <td className="px-3 py-2 text-center">{Number(s.gpaScore4).toFixed(2)}</td>
                        <td className="px-3 py-2 text-center">{s.isPass ? "Đạt" : "Không đạt"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </Card>

              <Card title="Tham số quy chế cấu hình được (System Configs)">
                <div className="space-y-3 text-sm">
                  {configs.map((c) => (
                    <div key={c.configKey} className="rounded-lg border p-3">
                      <div className="font-mono text-xs font-bold text-red-700">{c.configKey}</div>
                      <div className="mt-1 font-semibold">{c.configValue}</div>
                      <div className="mt-0.5 text-xs text-slate-500">{c.description}</div>
                    </div>
                  ))}
                </div>
              </Card>
            </div>
          )}
        </div>
      )}
    </AppShell>
  );
}
