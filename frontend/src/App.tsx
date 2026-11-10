import { useEffect } from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "@/routes/AppRoutes";
import { useAuthStore } from "@/stores/useAuthStore";

export default function App() {
  const { checkAuth } = useAuthStore();

  useEffect(() => {
    // Tự động kiểm tra phiên đăng nhập từ HttpOnly Cookie khi ứng dụng khởi chạy / F5
    checkAuth();
  }, [checkAuth]);

  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}
