import { columns, Transaction } from "@/components/ui/columns";
import { DataTable } from "@/components/ui/data-table";

const mockAll: Transaction[] = [
    { date: "2025.07.24", name: "BTC Invest reduction", type: "매수", amount: 2000, buySellAmount: 2000, qty: 0.0001 },
    { date: "2025.07.25", name: "ETH Refund", type: "매도", amount: 8000, buySellAmount: 8000, qty: 0.22 },
];

export default async function CoinTransactionsPage({ params }: { params: Promise<{ symbol: string }> }) {
    const { symbol } = await params;
    const data = mockAll.filter((t) => t.name.toLowerCase().includes(symbol.toLowerCase()));

    return (
        <div className="container py-8 space-y-6">
            <h1 className="text-2xl font-bold">{symbol.toUpperCase()} 거래내역</h1>
            <DataTable columns={columns} data={data} pageSize={10} />
        </div>
    );
}
