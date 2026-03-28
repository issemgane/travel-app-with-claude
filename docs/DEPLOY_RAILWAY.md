# Deploy Wanderlust on Railway — Step by Step

## Prerequisites
- A GitHub account (repo: `issemgane/travel-app-with-claude`)
- A Railway account (sign up at https://railway.app with GitHub)

## Total time: ~15 minutes

---

## Step 1: Create a Railway Project

1. Go to https://railway.app/dashboard
2. Click **"New Project"**
3. Select **"Empty Project"**
4. Name it: `wanderlust`

---

## Step 2: Add PostgreSQL with PostGIS

1. In your project, click **"+ New"** → **"Database"** → **"PostgreSQL"**
2. Railway creates a PostgreSQL instance automatically
3. Click on the PostgreSQL service → **"Data"** tab → **"Query"**
4. Run the following SQL to enable PostGIS:
   ```sql
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   CREATE EXTENSION IF NOT EXISTS "postgis";
   ```
5. Then paste and run the entire contents of `db/init.sql` to create all tables
6. Go to **"Variables"** tab — note the `DATABASE_URL` (you'll need it)

---

## Step 3: Add Redis

1. Click **"+ New"** → **"Database"** → **"Redis"**
2. Railway creates it automatically
3. Note the `REDIS_URL` from its Variables tab

---

## Step 4: Deploy Backend (Spring Boot)

1. Click **"+ New"** → **"GitHub Repo"**
2. Select `issemgane/travel-app-with-claude`
3. Railway will ask for the root directory → set it to: **`backend`**
4. Go to **"Settings"** tab:
   - Root Directory: `backend`
   - Builder: **Dockerfile** (it will auto-detect `backend/Dockerfile`)
5. Go to **"Variables"** tab and add these:

| Variable | Value |
|----------|-------|
| `PORT` | `8080` |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | `jdbc:postgresql://<host>:<port>/<db>` (from Step 2, convert the Railway URL) |
| `DATABASE_USERNAME` | (from PostgreSQL variables) |
| `DATABASE_PASSWORD` | (from PostgreSQL variables) |
| `REDIS_URL` | (from Step 3) |
| `KEYCLOAK_ISSUER_URI` | See Step 6 |
| `KEYCLOAK_JWK_URI` | See Step 6 |
| `FRONTEND_URL` | (from Step 5 after deploy) |
| `S3_ENDPOINT` | See Step 7 |
| `S3_ACCESS_KEY` | See Step 7 |
| `S3_SECRET_KEY` | See Step 7 |
| `S3_BUCKET` | `wanderlust-media` |
| `S3_REGION` | `auto` |
| `CDN_BASE_URL` | See Step 7 |

> **Tip**: You can reference Railway service variables using `${{Postgres.DATABASE_URL}}` syntax.

6. Click **"Deploy"** — Railway builds and deploys the backend
7. Go to **"Settings"** → **"Networking"** → **"Generate Domain"**
8. You now have: `https://wanderlust-backend-xxx.up.railway.app`

---

## Step 5: Deploy Frontend (React)

1. Click **"+ New"** → **"GitHub Repo"**
2. Select `issemgane/travel-app-with-claude` again
3. Set root directory to: **`frontend`**
4. Go to **"Variables"** tab and add:

| Variable | Value |
|----------|-------|
| `VITE_API_URL` | `https://wanderlust-backend-xxx.up.railway.app/api` |
| `VITE_KEYCLOAK_URL` | See Step 6 |
| `VITE_KEYCLOAK_REALM` | `wanderlust` |
| `VITE_KEYCLOAK_CLIENT_ID` | `wanderlust-frontend` |

5. In nginx.conf, the `BACKEND_URL` env var needs to be set:
   - Add variable: `BACKEND_URL` = `https://wanderlust-backend-xxx.up.railway.app`
6. Deploy, then generate a public domain
7. You now have: `https://wanderlust-frontend-xxx.up.railway.app`

> **Important**: Go back to the Backend service and set `FRONTEND_URL` to this frontend URL.

---

## Step 6: Set Up Keycloak (Authentication)

### Option A: Keycloak on Railway (Full control)

1. Click **"+ New"** → **"Docker Image"**
2. Image: `quay.io/keycloak/keycloak:23.0`
3. Add variables:

| Variable | Value |
|----------|-------|
| `KEYCLOAK_ADMIN` | `admin` |
| `KEYCLOAK_ADMIN_PASSWORD` | (choose a strong password) |
| `KC_DB` | `postgres` |
| `KC_DB_URL` | (your Railway PostgreSQL JDBC URL) |
| `KC_DB_USERNAME` | (from PostgreSQL) |
| `KC_DB_PASSWORD` | (from PostgreSQL) |
| `KC_HOSTNAME_STRICT` | `false` |
| `KC_PROXY` | `edge` |

4. Start command: `start --optimized` (or `start-dev` for testing)
5. Generate domain → e.g., `https://wanderlust-keycloak-xxx.up.railway.app`
6. Open that URL → Log in with admin credentials
7. Create a new Realm called `wanderlust`
8. Create a Client:
   - Client ID: `wanderlust-frontend`
   - Client Protocol: `openid-connect`
   - Root URL: your frontend URL
   - Valid Redirect URIs: `https://wanderlust-frontend-xxx.up.railway.app/*`
   - Web Origins: `https://wanderlust-frontend-xxx.up.railway.app`
   - Access Type: `public`
9. Update backend variables:
   - `KEYCLOAK_ISSUER_URI` = `https://wanderlust-keycloak-xxx.up.railway.app/realms/wanderlust`
   - `KEYCLOAK_JWK_URI` = `https://wanderlust-keycloak-xxx.up.railway.app/realms/wanderlust/protocol/openid-connect/certs`
10. Update frontend variables:
    - `VITE_KEYCLOAK_URL` = `https://wanderlust-keycloak-xxx.up.railway.app`

### Option B: Use Auth0 Free Tier (Easier, no Keycloak hosting needed)

If you want a simpler auth setup, you can use Auth0's free tier. This requires code changes to swap Keycloak for Auth0 — let me know if you prefer this route.

---

## Step 7: Set Up Media Storage

### Option A: Cloudflare R2 (Recommended — free 10GB)

1. Sign up at https://dash.cloudflare.com
2. Go to **R2** → **Create bucket** → name: `wanderlust-media`
3. Create an API token with R2 read/write permissions
4. Set backend variables:
   - `S3_ENDPOINT` = `https://<account-id>.r2.cloudflarestorage.com`
   - `S3_ACCESS_KEY` = (from R2 API token)
   - `S3_SECRET_KEY` = (from R2 API token)
   - `S3_BUCKET` = `wanderlust-media`
   - `S3_REGION` = `auto`
   - `CDN_BASE_URL` = `https://pub-<hash>.r2.dev` (from R2 public access settings)

### Option B: MinIO on Railway

1. Click **"+ New"** → **"Docker Image"**
2. Image: `minio/minio`
3. Start command: `server /data --console-address :9001`
4. Add variables: `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`
5. Generate domain, create bucket via MinIO console

---

## Step 8: Verify Deployment

1. Backend health: `https://wanderlust-backend-xxx.up.railway.app/actuator/health`
   - Should return `{"status":"UP"}`
2. API docs: `https://wanderlust-backend-xxx.up.railway.app/swagger-ui.html`
3. Frontend: `https://wanderlust-frontend-xxx.up.railway.app`
   - Should show the Wanderlust feed page

---

## Quick Reference: All Railway Services

| Service | Type | Domain |
|---------|------|--------|
| PostgreSQL | Database | (internal only) |
| Redis | Database | (internal only) |
| Backend | GitHub Repo (backend/) | *.up.railway.app |
| Frontend | GitHub Repo (frontend/) | *.up.railway.app |
| Keycloak | Docker Image | *.up.railway.app |
| MinIO/R2 | External or Docker | varies |

---

## Estimated Railway Cost

On Railway's free tier ($5/month credit):
- PostgreSQL: ~$1/month
- Redis: ~$0.50/month
- Backend: ~$2/month
- Frontend: ~$0.50/month
- Keycloak: ~$2/month

**Total: ~$6/month** (slightly over free tier). Consider the $5 Hobby plan.

---

## Troubleshooting

**Backend won't start:**
- Check logs in Railway dashboard
- Verify `DATABASE_URL` format is `jdbc:postgresql://...` (not `postgresql://...`)
- Ensure PostGIS extension is installed in PostgreSQL

**Frontend shows blank page:**
- Check browser console for errors
- Verify `VITE_API_URL` is set correctly
- Ensure CORS is configured (backend `FRONTEND_URL` matches frontend domain)

**Auth not working:**
- Verify Keycloak realm and client are configured
- Check that redirect URIs in Keycloak match your frontend domain
- Ensure `VITE_KEYCLOAK_URL` does NOT have a trailing slash
