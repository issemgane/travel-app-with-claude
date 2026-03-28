# Deploy Wanderlust on Railway

## What You Deploy

**ONE service** — a single Docker image that contains:
- Spring Boot backend (API)
- React frontend (served as static files)
- Hibernate auto-creates all database tables

**ONE database** — Railway PostgreSQL (auto-provisioned)

**That's it.** No Keycloak, no Redis, no S3, no MinIO.

---

## Steps (5 minutes)

### 1. Go to Railway

Open https://railway.app and sign in with GitHub.

### 2. Create Project from GitHub

1. Click **"New Project"**
2. Click **"Deploy from GitHub Repo"**
3. Select **`issemgane/travel-app-with-claude`**
4. Railway detects the `Dockerfile` and starts building

### 3. Add PostgreSQL

1. In your project dashboard, click **"+ New"** → **"Database"** → **"PostgreSQL"**
2. Done. Railway creates it automatically.

### 4. Set Environment Variables

Click on your **app service** (not the database) → **"Variables"** tab → **"New Variable"**:

| Variable | Value |
|----------|-------|
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (click "Add Reference" and select Postgres) |
| `DATABASE_USERNAME` | `${{Postgres.PGUSER}}` |
| `DATABASE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `JWT_SECRET` | Any random string, at least 64 characters (e.g., generate at https://generate-secret.vercel.app/64) |
| `PORT` | `8080` |

> **Tip**: For `DATABASE_URL`, Railway's Postgres gives you a URL like `postgresql://user:pass@host:port/db`.
> You need to convert it to JDBC format: `jdbc:postgresql://host:port/db`
> Or use the reference variables: set `DATABASE_URL` to `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}`

### 5. Deploy

Railway auto-deploys when variables are set. Wait for the build (~2-3 minutes).

### 6. Generate Public URL

1. Click your app service → **"Settings"** tab
2. Under **"Networking"** → click **"Generate Domain"**
3. You get a URL like: `https://wanderlust-production-xxxx.up.railway.app`

### 7. Open Your App

Visit your URL. You should see:
- The Wanderlust homepage
- "Sign In" / "Sign Up" buttons
- Register a new account → start creating Travel Cards

---

## That's It

No manual SQL. No Keycloak setup. No S3 configuration.

Just: GitHub repo → Railway → PostgreSQL → Environment variables → Live URL.

---

## Troubleshooting

**Build fails:**
- Check build logs in Railway dashboard
- Common issue: Node.js or Java version — the Dockerfile handles both

**App starts but shows errors:**
- Check that `DATABASE_URL` is in JDBC format: `jdbc:postgresql://...`
- Check that `JWT_SECRET` is at least 32 characters

**Can't register/login:**
- Open browser dev tools → Network tab → check API responses
- Verify the backend is running: visit `https://your-url.up.railway.app/actuator/health`

**PostGIS not available:**
- Railway's default PostgreSQL doesn't include PostGIS
- If you get PostGIS errors, you may need to use Railway's PostgreSQL and run: `CREATE EXTENSION IF NOT EXISTS postgis;` via the Railway data tab or connect with psql
