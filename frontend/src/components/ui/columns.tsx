"use client";

import { ColumnDef } from "@tanstack/react-table";

export type Transaction = {
    date: string;
    name: string;
    type: "매수" | "매도";
    amount: number;
    buySellAmount: number;
    qty: number;
};

export const columns: ColumnDef<Transaction>[] = [
    { accessorKey: "date", header: "거래날짜" },
    { accessorKey: "name", header: "가상화폐 이름" },
    { accessorKey: "type", header: "거래 구분" },
    {
        accessorKey: "amount",
        header: "금액",
        cell: ({ getValue }) => `$ ${Number(getValue()).toLocaleString()}`,
    },
    {
        accessorKey: "buySellAmount",
        header: "구매/판매 금액",
        cell: ({ getValue }) => `$ ${Number(getValue()).toLocaleString()}`,
    },
    { accessorKey: "qty", header: "구매/판매 수" },
];
