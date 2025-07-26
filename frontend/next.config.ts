const nextConfig = {
    async rewrites() {
        return [
            {
                source: "/api/:path*",        // /api로 시작하는 모든 요청을
                destination: "http://localhost:8080/api/:path*", // 백엔드로 프록시
            },
        ];
    },
};
export default nextConfig;
