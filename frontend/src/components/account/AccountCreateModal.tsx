import { useEffect, useState } from "react";
import { Sparkles, X, Loader2 } from "lucide-react";
import type { AccountType, CreateAccountRequest } from "@/types/account";
import type { Department } from "@/types/department";
import { departmentApi } from "@/api/departmentApi";

interface AccountCreateModalProps {
  onClose: () => void;
  onSubmit: (data: CreateAccountRequest) => Promise<void>;
}

const EMPTY_FORM: CreateAccountRequest = {
  username: "",
  email: "",
  password: "",
  firstName: "",
  lastName: "",
  accountType: "EMPLOYEE",
  departmentId: undefined,
};

export default function AccountCreateModal({
  onClose,
  onSubmit,
}: AccountCreateModalProps) {
  const [form, setForm] = useState<CreateAccountRequest>(EMPTY_FORM);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [isLoadingDepartments, setIsLoadingDepartments] = useState(true);

  useEffect(() => {
    let cancelled = false;
    departmentApi
      .getDepartments({ pageNumber: 1, size: 100, status: "ACTIVE" })
      .then((response) => {
        if (!cancelled) setDepartments(response.data.items);
      })
      .catch(() => {
        if (!cancelled) setError("Không thể tải danh sách phòng ban");
      })
      .finally(() => {
        if (!cancelled) setIsLoadingDepartments(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const handleChange = <K extends keyof CreateAccountRequest>(
    key: K,
    value: CreateAccountRequest[K],
  ) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    try {
      await onSubmit(form);
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Tạo tài khoản thất bại");
    } finally {
      setIsSubmitting(false);
    }
  };

  const inputClass =
    "w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C] transition";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-md w-full p-6 animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-[#E8DBC5]">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-[#FAF6EE] text-[#C8792C]">
              <Sparkles className="w-4 h-4" />
            </div>
            <h3 className="font-bold text-base text-[#5B4632]">
              Tạo Tài Khoản Mới
            </h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg text-[#5B4632]/60 hover:text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Error */}
        {error && (
          <p className="mt-3 text-xs text-rose-600 bg-rose-50 border border-rose-200 rounded-xl px-3 py-2">
            {error}
          </p>
        )}

        {/* Form */}
        <form
          onSubmit={handleSubmit}
          autoComplete="off"
          className="mt-5 space-y-4"
        >
          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Tên đăng nhập (Username)
            </label>
            <input
              type="text"
              autoComplete="off"
              required
              placeholder="username"
              value={form.username}
              onChange={(e) => handleChange("username", e.target.value)}
              className={inputClass}
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Mật khẩu
            </label>
            <input
              type="password"
              autoComplete="new-password"
              required
              placeholder="••••••••"
              value={form.password ?? ""}
              onChange={(e) => handleChange("password", e.target.value)}
              className={inputClass}
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Email
            </label>
            <input
              type="email"
              autoComplete="off"
              required
              placeholder="user@example.com"
              value={form.email}
              onChange={(e) => handleChange("email", e.target.value)}
              className={inputClass}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
                Họ (Last Name)
              </label>
              <input
                type="text"
                required
                placeholder="Nguyễn"
                value={form.lastName}
                onChange={(e) => handleChange("lastName", e.target.value)}
                className={inputClass}
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
                Tên (First Name)
              </label>
              <input
                type="text"
                required
                placeholder="Văn An"
                value={form.firstName}
                onChange={(e) => handleChange("firstName", e.target.value)}
                className={inputClass}
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Vai trò (Role)
            </label>
            <select
              value={form.accountType}
              onChange={(e) =>
                handleChange("accountType", e.target.value as AccountType)
              }
              className={inputClass}
            >
              <option value="EMPLOYEE">EMPLOYEE</option>
              <option value="MANAGER">MANAGER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Phòng ban (tùy chọn)
            </label>
            <select
              value={form.departmentId ?? ""}
              disabled={isLoadingDepartments}
              onChange={(e) =>
                handleChange(
                  "departmentId",
                  e.target.value ? Number(e.target.value) : undefined,
                )
              }
              className={inputClass}
            >
              <option value="">
                {isLoadingDepartments
                  ? "Đang tải phòng ban..."
                  : "Không thuộc phòng ban"}
              </option>
              {departments.map((department) => (
                <option key={department.id} value={department.id}>
                  {department.name} ({department.type})
                </option>
              ))}
            </select>
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-[#E8DBC5]">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-[#5B4632] bg-white border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex items-center gap-2 px-5 py-2 text-xs font-bold text-white bg-[#C8792C] hover:bg-[#B26822] rounded-xl shadow-md shadow-[#C8792C]/30 cursor-pointer disabled:opacity-60"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              Tạo Tài Khoản
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
