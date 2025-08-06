import { MainNav } from "@/components/layout/main-nav";
import { Footer } from "@/components/layout/footer";
import { PageShell } from "@/components/layout/page-shell";

export default function PublicLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="min-h-screen flex flex-col w-full">
            <header className="w-full">
                <PageShell maxW="max-w-[80vw]" padded>
                    {/* MainNav 내부 정렬이 왼쪽 고정이면 className으로 중앙 정렬 */}
                    <MainNav className="mx-auto justify-center" />
                </PageShell>
            </header>

            <main className="flex-1 w-full">
                <PageShell maxW="max-w-[80vw]" padded>
                    {children}
                </PageShell>
            </main>

            <footer className="w-full">
                <PageShell maxW="max-w-[80vw]" padded>
                    <Footer className="mx-auto text-center" />
                </PageShell>
            </footer>
        </div>
    );
}
