# PeerLink 

A lightweight, peer-to-peer file sharing web application. Upload a file, get an invite code, and share it with anyone to download directly.

**Live Demo**: [peer-linkk.netlify.app](https://peer-linkk.netlify.app/)

---

## Features

-  Upload any file and receive a unique invite code
-  Download files using just the invite code
-  Files are served directly from memory — no permanent storage
-  Fast and lightweight — no database required
-  Clean, responsive UI built with Next.js and Tailwind CSS

---

## Tech Stack

### Backend
- **Java 17** — raw `com.sun.net.httpserver.HttpServer` (no Spring Boot)
- **Maven** with Shade plugin for fat JAR packaging
- **Apache Commons IO** for multipart file parsing
- **Docker** for containerization

### Frontend
- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **Axios** for HTTP requests

### Deployment
- **Backend** → [Railway](https://railway.app)
- **Frontend** → [Netlify](https://netlify.com)

---

## How It Works

1. User uploads a file via the frontend
2. Frontend sends the file to the Next.js API route (`/api/upload`)
3. API route proxies the request to the Java backend
4. Java backend saves the file temporarily and assigns it a unique invite code
5. User shares the invite code with anyone
6. Receiver enters the code → file is streamed directly from the backend

---

## Project Structure

```
PeerLink/
├── src/                          # Java backend
│   └── main/java/p2p/
│       ├── App.java              # Entry point
│       ├── controller/
│       │   └── FileController.java   # HTTP server, upload/download handlers
│       ├── service/
│       │   └── FileSharer.java       # File storage and retrieval logic
│       └── utility/
│           └── UploadUtils.java      # Invite code generator
├── ui/                           # Next.js frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── page.tsx              # Main page
│   │   │   ├── layout.tsx
│   │   │   └── api/
│   │   │       ├── upload/
│   │   │       │   └── route.ts      # Upload proxy (60s timeout)
│   │   │       └── download/[port]/
│   │   │           └── route.ts      # Download proxy (60s timeout)
│   │   └── components/
│   │       ├── FileUpload.tsx
│   │       ├── FileDownload.tsx
│   │       └── InviteCode.tsx
│   ├── next.config.js
│   ├── netlify.toml
│   └── vercel.json
├── Dockerfile                    # Multi-stage Docker build
├── pom.xml                       # Maven config with Shade plugin
└── README.md
```

---

## Running Locally

### Backend

```bash
# Clone the repo
git clone https://github.com/pratapbhaskar264/PeerLink.git
cd PeerLink

# Build and run
mvn clean package -DskipTests
java -jar target/peerlink-1.0-SNAPSHOT.jar
```

Backend runs at `http://localhost:8080`

### Frontend

```bash
cd ui

# Install dependencies
npm install

# Create .env.local
echo "NEXT_PUBLIC_API_URL=http://localhost:8080" > .env.local

# Run dev server
npm run dev
```

Frontend runs at `http://localhost:3000`

---

## Deployment

### Backend (Railway)

1. Connect GitHub repo to [Railway](https://railway.app)
2. Railway auto-detects the `Dockerfile`
3. Set environment variables:
    - `FRONTEND_URL` = your Netlify URL
4. Generate a public domain in **Settings → Networking**

### Frontend (Netlify)

1. Connect GitHub repo to [Netlify](https://netlify.com)
2. Set build settings:
    - Base directory: `ui`
    - Build command: `npm run build`
    - Publish directory: `.next`
3. Set environment variables:
    - `NEXT_PUBLIC_API_URL` = your Railway URL
4. Deploy

---

## Environment Variables

### Backend (Railway)
| Variable | Description |
|---|---|
| `PORT` | Port to run the server on (auto-set by Railway) |
| `FRONTEND_URL` | Your Netlify frontend URL (for CORS) |

### Frontend (Netlify)
| Variable | Description |
|---|---|
| `NEXT_PUBLIC_API_URL` | Your Railway backend URL |

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/upload` | Upload a file, returns `{ "port": <code> }` |
| `GET` | `/download/:code` | Download file by invite code |
| `GET` | `/health` | Health check, returns `OK` |

---

## License

MIT
