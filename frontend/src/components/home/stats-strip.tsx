"use client";

import {motion} from "framer-motion";
import {stagger} from "@/lib/motion";
import {PageShell} from "@/components/layout/page-shell";

export function StatsStrip() {

    return (
        <section className="py-12">
            <PageShell maxW="max-w-[80vw]" padded>
                <motion.div
                    variants={stagger(0.1)}
                    initial="hidden"
                    whileInView="show"
                    viewport={{ once: true, amount: 0.2 }}
                    className="grid gap-6 md:grid-cols-3"
                >
                </motion.div>
            </PageShell>
        </section>
    );
}