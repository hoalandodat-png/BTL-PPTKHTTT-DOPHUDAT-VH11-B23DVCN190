export interface ApiFieldIssue {
  field: string;
  issue: string;
}

export interface ApiErrorPayload {
  code: string;
  message: string;
  details?: ApiFieldIssue[];
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiErrorPayload;
  timestamp: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  status: string;
  roles: string[];
  permissions: string[];
  redirectPath: string;
}

export class ApiClientError extends Error {
  code: string;
  status: number;
  details?: ApiFieldIssue[];

  constructor(status: number, code: string, message: string, details?: ApiFieldIssue[]) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

async function rawRequest<T>(path: string, init: RequestInit = {}): Promise<ApiResponse<T>> {
  const isFormData = typeof FormData !== "undefined" && init.body instanceof FormData;
  const headers: Record<string, string> = {
    "X-Requested-With": "XMLHttpRequest",
    ...(init.headers as Record<string, string> | undefined),
  };
  if (!isFormData && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(`/api/v1${path}`, {
    ...init,
    credentials: "include",
    headers,
  });

  const text = await res.text();
  let json: ApiResponse<T>;
  try {
    json = text ? JSON.parse(text) : ({ success: res.ok } as ApiResponse<T>);
  } catch {
    throw new ApiClientError(res.status, "INVALID_JSON", "Phản hồi không hợp lệ từ máy chủ");
  }

  if (!res.ok || !json.success) {
    throw new ApiClientError(
      res.status,
      json.error?.code || "REQUEST_FAILED",
      json.error?.message || `Lỗi HTTP ${res.status}`,
      json.error?.details
    );
  }
  return json;
}

let refreshPromise: Promise<boolean> | null = null;

async function tryRefreshToken(): Promise<boolean> {
  if (!refreshPromise) {
    refreshPromise = fetch("/api/v1/auth/refresh", {
      method: "POST",
      credentials: "include",
      headers: { "X-Requested-With": "XMLHttpRequest" },
    })
      .then((r) => r.ok)
      .catch(() => false)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  try {
    const res = await rawRequest<T>(path, init);
    return res.data as T;
  } catch (err) {
    if (err instanceof ApiClientError && err.status === 401 && !path.startsWith("/auth/")) {
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        const retry = await rawRequest<T>(path, init);
        return retry.data as T;
      }
    }
    throw err;
  }
}

export function cn(...classes: Array<string | false | null | undefined>): string {
  return classes.filter(Boolean).join(" ");
}
