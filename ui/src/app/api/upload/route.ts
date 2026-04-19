import { NextRequest, NextResponse } from 'next/server';

export const maxDuration = 60;

export async function POST(request: NextRequest) {
  const formData = await request.formData();
  const response = await fetch('https://peerlink-production-7083.up.railway.app/upload', {
    method: 'POST',
    body: formData,
  });
  const data = await response.json();
  return NextResponse.json(data);
}
