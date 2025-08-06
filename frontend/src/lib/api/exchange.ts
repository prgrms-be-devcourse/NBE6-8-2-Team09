import type {ExchangeDTO, InitialRequestDTO, PreviousDTO} from "@/lib/types/exchange/type";

export const exchangeApi = {
    getLatest: async (): Promise<ExchangeDTO[]> => {
        const res = await fetch("/api/exchange/coins-latest");
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        const data = await res.json();
        return data ?? [];
    },
    getInitialCandles: async (
        payload: InitialRequestDTO
    ): Promise<ExchangeDTO[]> => {
        const res = await fetch("/api/exchange/initial", {
            method: "POST",
            body: JSON.stringify(payload),
            headers: { "Content-Type": "application/json" },
        });

        if (!res.ok) throw new Error("Failed to fetch initial candles");
        return res.json();
    },

    getPreviousCandles: async (
        payload: PreviousDTO
    ): Promise<ExchangeDTO[]> => {
        const res = await fetch("/api/exchange/previous", {
            method: "POST",
            body: JSON.stringify(payload),
            headers: { "Content-Type": "application/json" },
        });

        if (!res.ok) throw new Error("Failed to fetch previous candles");
        return res.json();
    },
};