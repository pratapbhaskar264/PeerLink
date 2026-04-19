import { NextRequest, NextResponse } from 'next/server';

export const maxDuration = 60;

export async function GET(
  request: NextRequest,
  { params }: { params: { port: string } }
) {
  const response = await fetch(`https://peerlink-hst3.onrender.com/download/${params.port}`);
  const blob = await response.blob();
  const contentDisposition = response.headers.get('content-disposition') || '';
  return new NextResponse(blob, {
    headers: {
      'Content-Disposition': contentDisposition,
      'Content-Type': 'application/octet-stream',
    },
  });
}
