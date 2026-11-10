interface AccountPaginationProps {
  page: number
  totalPages: number
  totalElements: number
  onPrev: () => void
  onNext: () => void
}

export default function AccountPagination({
  page,
  totalPages,
  totalElements,
  onPrev,
  onNext,
}: AccountPaginationProps) {
  return (
    <div className="p-4 border-t border-[#E8DBC5] flex items-center justify-between text-xs text-[#5B4632]/80 bg-[#FAF6EE]/30">
      <span>
        Trang {page} / {totalPages} (Tổng số {totalElements} tài khoản)
      </span>

      <div className="flex items-center gap-1.5">
        <button
          type="button"
          disabled={page <= 1}
          onClick={onPrev}
          className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632] hover:bg-[#FAF6EE] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
        >
          Trước
        </button>

        <span className="px-3.5 py-1.5 rounded-lg bg-[#C8792C] text-white font-bold shadow-xs">
          {page}
        </span>

        <button
          type="button"
          disabled={page >= totalPages}
          onClick={onNext}
          className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632] hover:bg-[#FAF6EE] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
        >
          Sau
        </button>
      </div>
    </div>
  )
}

