import { OrdersRequest, OrderResponse } from "@/lib/types/order/types";

export const ordersApi = {
    // 공통 실행 함수
    executeTrade: async (
        walletId: number,
        payload: OrdersRequest
    ): Promise<OrderResponse> => {
        const res = await fetch(`/orders/wallet/${walletId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        if (!res.ok) {
            const error = await res.text();
            throw new Error(`Failed to execute trade: ${error}`);
        }

        return res.json();
    },

    // 매수용 함수
    executeTradeByBuying: async (
        walletId: number,
        payload: Omit<OrdersRequest, "tradeType">
    ): Promise<OrderResponse> => {
        return ordersApi.executeTrade(walletId, {
            ...payload,
            tradeType: "BUY",
        });
    },

    // 매도용 함수
    executeTradeBySelling: async (
        walletId: number,
        payload: Omit<OrdersRequest, "tradeType">
    ): Promise<OrderResponse> => {
        return ordersApi.executeTrade(walletId, {
            ...payload,
            tradeType: "SELL",
        });
    },
};