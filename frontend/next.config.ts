const nextConfig = {
    output: "standalone",

    async rewrites() {
        return [
            {
                source: "/api/:path*",
                destination:
                    process.env.NODE_ENV === "production"
                        ? "/api/:path*"                  // Nginx로 프록시 설정
                        : "http://localhost:8080/api/:path*", // dev 서버 프록시
            },
        ];
    },
};
export default nextConfig;
