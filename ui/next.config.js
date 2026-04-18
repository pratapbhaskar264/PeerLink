/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  async rewrites() {
    return [
      {
        source: '/api/upload',
        destination: 'https://peerlink-hst3.onrender.com/upload',
      },
      {
        source: '/api/download/:port',
        destination: 'https://peerlink-hst3.onrender.com/download/:port',
      },
    ];
  },
}

module.exports = nextConfig