# Instello

Instello is an ERP platform for organizations and educational institutions. It brings platform administration, organization management, and institution-specific experiences together with a shared backend API.

## Applications

| Application | Directory | Local URL | Purpose |
| --- | --- | --- | --- |
| Admin | `frontend/apps/admin` | `http://localhost:3000` | Platform administration |
| Organization | `frontend/apps/organization` | `http://localhost:3001` | Organization workspace |
| Institution | `frontend/apps/institution` | `http://localhost:3002` | Institution experience, routed by institution slug |
| Backend | `backend` | `http://localhost:8080` | Spring Boot API and supporting services |

The backend provides organization and institution management, membership, invitations, access requests, authentication, and academic pattern APIs. The frontend apps share UI components and a versioned API client.

## Run locally

### Backend

The backend requires Java 25, PostgreSQL, and Redis. Docker Compose also starts Mailpit for local email testing.

```bash
cd backend
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

### Frontend

The frontend uses Bun 1.4.4 and the workspace scripts to run all three apps.

```bash
cd frontend
bun install
bun run dev
```

See the [frontend guide](frontend/README.md) for workspace commands and app details, and [backend getting-started notes](backend/HELP.md) for backend references. Backend email template development is documented in [backend/emails/README.md](backend/emails/README.md).

## License and notices

Instello is proprietary. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
