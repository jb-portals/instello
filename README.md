<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="banner-dark.png">
    <img src="banner.png" alt="Instello — One Platform. Every Possibility">
  </picture>
</p>

---

Instello helps academic teams move paperwork into digital workflows for managing students, attendance, marks, timetables, and more. It brings platform administration, organization management, and institution-specific experiences together with a shared backend API.

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

### Backend database tests

Backend database tests use Java 25 and Testcontainers. Testcontainers starts a
disposable PostgreSQL 17 database and supplies the connection settings. These
tests work without a running application, Compose services, or a local `.env`
file. The first run needs access to pull the PostgreSQL and Testcontainers
helper images.

#### macOS

Start Docker Desktop or OrbStack. In the same terminal where you will run Maven,
confirm that `docker info` succeeds. Then run:

```bash
cd backend
./mvnw -Dgroups=database test
```

If Docker works in the terminal but Testcontainers cannot find it, try the
active Docker context's endpoint. For OrbStack, the second setting gives the
Testcontainers cleanup container the Docker socket path inside the runtime.

```bash
export DOCKER_HOST="$(docker context inspect --format '{{.Endpoints.docker.Host}}')"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

Rerun Maven from that terminal. See the [Testcontainers Docker configuration guide](https://java.testcontainers.org/features/configuration/#customizing-docker-host-detection).

#### Windows

Start Docker Desktop with the WSL 2 backend and use Linux containers. In
PowerShell, confirm that `docker info --format '{{.OSType}}'` prints `linux`.
Then run:

```powershell
cd backend
.\mvnw.cmd "-Dgroups=database" test
```

If you run Maven inside WSL instead, enable Docker Desktop integration for your
distro, install Java 25 in that distro, and use `./mvnw` there. See the
[Docker Desktop WSL 2 guide](https://docs.docker.com/desktop/features/wsl/).

#### Linux

Start Docker Engine and confirm that `docker info` succeeds as the same user
who will run Maven or IntelliJ. If you get a socket permission error, follow
the [Docker Engine post-installation guide](https://docs.docker.com/engine/install/linux-postinstall/)
for Docker access. Then run:

```bash
cd backend
./mvnw -Dgroups=database test
```

With a custom Docker context, set `DOCKER_HOST` to its endpoint if
Testcontainers cannot discover it. Use the socket override only if the cleanup
container needs an explicit socket path for your runtime.

#### IntelliJ IDEA and GitHub Actions

In IntelliJ, select Java 25 for the JUnit run configuration and run a database
test class. If your Docker setup needs environment overrides, open
**Run → Edit Configurations → your JUnit configuration → Environment variables**
and add them there. For example, with OrbStack set
`DOCKER_HOST` to the literal endpoint printed by
`docker context inspect --format '{{.Endpoints.docker.Host}}'` and add the socket
override shown above. Shell exports from a separate terminal do not reach the
IDE's JUnit runner.

The [backend GitHub Actions workflow](.github/workflows/backend-tests.yml)
runs the database tests on its Docker-enabled Ubuntu runner as part of the
full Maven suite. Testcontainers provides PostgreSQL for these tests in CI too.
The workflow's PostgreSQL and Redis services are available to other backend tests.

#### Adding database tests

Annotate new JPA repository tests with `@PostgresJpaTest` from
`in.instello.support`. It loads the JPA slice, starts disposable PostgreSQL,
creates the schema, and rolls back each test transaction. It also applies the
JUnit `database` tag, so the commands above run all database tests. The full
suite still runs them with `./mvnw test` (or `.\mvnw.cmd test` on Windows).

For a full Spring Boot database integration test, import
`PostgresTestConfiguration` and apply `@Tag("database")` so the test is included
in the same database-only run.

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
