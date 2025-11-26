import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
    output: 'standalone',
    async rewrites() {
        return [
            {
                source: '/api/v1/:path*',
                destination: 'http://host.docker.internal:8080/api/v1/:path*',
            },
        ];
    },
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: '**',
            },
            {
                protocol: 'http',
                hostname: 'backend',
                port: '8080',
            },
            {
                protocol: 'http',
                hostname: 'localhost',
                port: '8080',
            },
        ],
    },
};

export default nextConfig;
