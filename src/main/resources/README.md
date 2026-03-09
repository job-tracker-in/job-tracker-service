<img width="1440" height="900" alt="Screenshot 2026-02-25 at 7 27 33 PM" src="https://github.com/user-attachments/assets/d78945a2-bb3d-4a77-b0b1-fc4299c2bf82" /># 🎯 Job Tracker — Free Job Application Manager

> A production-ready, full-stack web application to help job seekers organise and track their job applications efficiently.

🌐 **Live App:** [https://job-tracker.in](https://job-tracker.in)

---

## 📸 Screenshots
<img width="1440" height="900" alt="Screenshot 2026-02-25 at 7 22 38 PM" src="https://github.com/user-attachments/assets/7a656e46-e2e0-4822-8c15-0b1d9a43aa09" />
<img width="1440" height="900" alt="Screenshot 2026-02-25 at 7 22 55 PM" src="https://github.com/user-attachments/assets/27f42016-2ca8-46ea-befa-7c3325166597" />
<img width="1440" height="900" alt="Screenshot 2026-02-25 at 7 23 14 PM" src="https://github.com/user-attachments/assets/4ac1c477-cfa2-4ab9-a9dd-dbdb27451e0c" />
<img width="1440" height="900" alt="Screenshot 2026-02-25 at 7 27 49 PM" src="https://github.com/user-attachments/assets/dbd2ff21-6c01-4747-a325-9d286fd3765c" />
<img width="1440" height="900" alt="Screenshot 2026-02-25 at 7 27 59 PM" src="https://github.com/user-attachments/assets/205f3001-202b-4ab6-ba41-a8d127516fee" />

---

## 🚀 Features

- ✅ Track unlimited job applications
- ✅ Organise by status: **Applied → Interview → Offer → Rejected**
- ✅ Secure login via Keycloak (OAuth2 / OpenID Connect)
- ✅ Fast, responsive UI
- ✅ 100% Free to use

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                     Vercel (UI)                     │
│                   React Frontend                    │
└──────────────────────┬──────────────────────────────┘
                       │ HTTPS
┌──────────────────────▼──────────────────────────────┐
│              DigitalOcean Droplet                   │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │  Nginx   │  │ Spring    │  │    Keycloak 26   │  │
│  │ (Proxy + │→ │ Boot API  │  │  auth.job-       │  │
│  │  SSL)    │  │ :8081     │  │  tracker.in:8080 │  │
│  └──────────┘  └─────┬─────┘  └────────┬─────────┘  │
│                      │                 │            │
│            ┌─────────▼─────────────────▼─────────┐  │
│            │         PostgreSQL 15               │  │
│            │  [jobtracker DB] + [keycloak DB]    │  │
│            └─────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| **Java 21** | Core language (LTS) |
| **Spring Boot** | REST API framework |
| **JDBC Template** | Direct SQL execution with full control |
| **HikariCP** | High-performance connection pooling |
| **PostgreSQL 15** | Primary database |
| **Keycloak 26** | Enterprise Identity & Access Management (IAM) |
| **OAuth2 / OpenID Connect** | Secure authentication & authorisation |
| **Spring Actuator** | Health monitoring & metrics |
| **Caffeine Cache** | In-memory caching for performance |

### Frontend
| Technology | Purpose |
|---|---|
| **React.js** | Dynamic, responsive UI |
| **Vercel** | Frontend deployment & CDN |

### DevOps & Infrastructure
| Technology | Purpose |
|---|---|
| **Docker** | Containerisation (multi-stage builds) |
| **Docker Compose** | Multi-container orchestration |
| **GitHub Actions** | CI/CD pipeline |
| **DigitalOcean** | Backend cloud hosting |
| **Nginx** | Reverse proxy + SSL termination |

---

## ⚙️ CI/CD Pipeline

```
Git Push (main branch)
        │
        ▼
GitHub Actions
        │
        ├── 1. Checkout Code
        ├── 2. Setup Java 21 (Temurin)
        ├── 3. Build Spring Boot JAR (Maven)
        ├── 4. Build Docker Image (AMD64)
        ├── 5. Push to Docker Hub
        └── 6. SSH into DigitalOcean
                    │
                    ├── docker compose pull
                    └── docker compose up -d
```

---

## 🐳 Docker Setup

### Multi-Stage Dockerfile
- **Stage 1 (Builder):** Compiles the Spring Boot JAR using Maven
- **Stage 2 (Runtime):** Lightweight JRE image with non-root user for security

### Services (docker-compose.yml)
| Service | Image | Port |
|---|---|---|
| **PostgreSQL** | postgres:15-alpine | Internal |
| **Keycloak** | keycloak:26.0 | 127.0.0.1:8080 |
| **Spring Boot API** | krkzero9/jobtracker-api | 127.0.0.1:8081 |
| **Nginx** | nginx:alpine | 80, 443 |

---

## 🔐 Security

- **Keycloak** handles all authentication — no passwords stored in the app
- **OAuth2 Resource Server** validates JWT tokens on every request
- **Secrets** managed via environment variables (never hardcoded)
- **Non-root Docker user** for runtime security
- **Nginx** handles SSL termination

---

## 📦 Local Development Setup

### Prerequisites
- Java 21+
- Maven
- Docker & Docker Compose
- Node.js (for frontend)

### Backend
```bash
# Clone the repo
git clone https://github.com/yourusername/job-tracker.git
cd job-tracker

# Start infrastructure (PostgreSQL + Keycloak)
docker compose up postgres keycloak -d

# Run Spring Boot app
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Environment Variables
```env
DATABASE_URL=jdbc:postgresql://localhost:5433/jobtracker
DATABASE_USER=your_db_user
DATABASE_PASSWORD=your_db_password
KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/jobtracker
POSTGRES_PASSWORD=your_postgres_password
KC_ADMIN_PASSWORD=your_keycloak_admin_password
```

---

## 📊 Performance Configuration

- **HikariCP** pool: min 2 / max 5 connections (production-tuned)
- **Container memory limits:** PostgreSQL 350MB, Keycloak 650MB, API 450MB, Nginx 64MB
- **Caffeine cache:** 500 users, 30-minute TTL
- **JVM:** Container-aware (`UseContainerSupport`, 65% max RAM)
- **Graceful shutdown:** 30s timeout for in-flight requests

---

## 🤝 Contributing

Feedback and contributions are welcome! Feel free to:
- 🐛 Report bugs via [Issues](https://github.com/yourusername/job-tracker/issues)
- 💡 Suggest features
- 🔧 Submit pull requests

---

## 📄 License

MIT License — free to use and modify.

---

> Built with ❤️ to solve a real problem faced during job hunting.
> If this helped you, please ⭐ the repo!
