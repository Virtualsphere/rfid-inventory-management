# RFID Asset Tracking - Backend API

Shared REST backend for the **mobile handheld app** and the **admin web panel**,
per the Virtualsphere Technologies BRD for Daya Worldwide Limited. Built with
Spring Boot + Spring Security (JWT) + Spring Data JPA, sharing the same MySQL
schema as the `admin-rfid-asset-tracking` desktop app.

## How this relates to the desktop app

- `User` and `InventoryItem` here are field-for-field, column-for-column copies
  of the entities in the desktop app - same table names, same columns. Point
  both apps at the same database and they read/write the same data.
- Two new tables are added on top: `locations` (master list of warehouse
  locations) and `activity_logs` (audit trail / dashboard activity feed). These
  are additive - the desktop app's schema is untouched.
- The desktop app is a JavaFX client talking to MySQL directly. This backend is
  a separate HTTP service for the mobile app and a future web admin panel -
  they don't call each other, they just share a database.

## Default login

Seeded automatically on first run if no `admin` user exists yet (same as the
desktop app):
- username: `admin`
- password: `admin123`

## Tech stack

Java 17, Spring Boot 3.3, Spring Security + JWT (`io.jsonwebtoken`/jjwt 0.12),
Spring Data JPA, MySQL, Lombok.

## Setup

1. Update `src/main/resources/application.properties` with your MySQL
   credentials - point it at the **same database** the desktop app uses if you
   want shared data, or a separate one if you'd rather keep them apart.
2. Generate a real JWT secret for anything beyond local dev and replace
   `app.jwt.secret` (currently a random dev-only value):
   ```
   openssl rand -base64 32
   ```
3. Run:
   ```
   mvn spring-boot:run
   ```
   Server starts on `http://localhost:8080` (change `server.port` if needed).

## Swagger / OpenAPI

Once the app is running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Raw OpenAPI spec: `http://localhost:8080/v3/api-docs`

To call protected endpoints from the UI: `POST /api/auth/login`, copy the
`token` from the response, click **Authorize** (top right), and paste just the
token itself (no `Bearer ` prefix - Swagger UI adds that for you).

## Authentication

All endpoints except `/api/auth/login`, `/api/auth/me` (needs a token) and
`/api/health` require a JWT.

```
POST /api/auth/login
{ "username": "admin", "password": "admin123" }

-> 200 { "token": "...", "username": "admin", "fullName": "...", "role": "ADMIN", "location": null }
```

Send the token on every subsequent request:
```
Authorization: Bearer <token>
```

`GET /api/auth/me` returns the current user's profile from the token.

## Roles & location scoping

- **ADMIN**: full access to every endpoint, sees inventory/dashboard data
  across all locations.
- **USER**: can use inventory + dashboard endpoints, but is always scoped to
  their own `location` field (set on their user record) - they can't read or
  modify inventory at other locations, even if they pass a different
  `location` query param. User/Location management endpoints are admin-only.

## Endpoints

### Users (admin only)
```
GET    /api/users
GET    /api/users/{id}
POST   /api/users          { username, password, fullName, role, location, active }
PUT    /api/users/{id}     (any field omitted = unchanged; password optional)
DELETE /api/users/{id}
```

### Locations
```
GET    /api/locations                 (any authenticated user)
POST   /api/locations                 { name, address }              (admin)
PUT    /api/locations/{id}            { name, address }              (admin)
DELETE /api/locations/{id}                                           (admin)
```

### Inventory
```
GET    /api/inventory?q=&status=&location=&rackId=&modelNo=&category=
GET    /api/inventory/{epc}
POST   /api/inventory       { epc, productName, modelNo, price, category, location, rackId, status }
PUT    /api/inventory/{id}  (any field omitted = unchanged)
DELETE /api/inventory/{id}                                           (admin)
```

`status` is one of `IN`, `OUT`, `MISSING`.

### Mobile bulk scan verification
The handheld app's "detect items marked IN but not found during physical
scanning" workflow (BRD 2.1.2):
```
POST /api/inventory/scan-verify
{ "location": "Warehouse A", "scannedEpcs": ["E2004715...", "E2004716..."] }

-> 200 {
     "totalExpected": 42,
     "totalScanned": 39,
     "totalMissing": 3,
     "missingItems": [ { ...inventory item... }, ... ]
   }
```
Everything expected at that location (status `IN`) but absent from
`scannedEpcs` is flagged `MISSING` and returned in the report. Everything
present gets its `lastScannedAt` updated.

### Dashboard
```
GET /api/dashboard/summary

-> 200 {
     "totalAssets": 120,
     "inCount": 100,
     "outCount": 15,
     "missingCount": 5,
     "alerts": [ ...MISSING items... ],
     "recentActivity": [ ...last 20 activity log entries... ]
   }
```
Scoped to the requester's location for `USER` role, unrestricted for `ADMIN`.

## Not yet built (BRD items beyond this pass)

- **Fixed-reader entry/exit integration**: this backend accepts inventory
  status updates over HTTP; wiring the actual UDM9R fixed readers to call
  it (e.g. via a small bridge service near the reader, similar to the desktop
  app's `RfidReaderService`) is a separate piece of work.
- **"Unauthorized movement" alerts**: currently only `MISSING` status feeds the
  alerts list. Detecting movement without a corresponding scan event needs
  business rules (e.g. a time window / expected route) that weren't specified
  yet - flagged here rather than guessed at.
- **Pagination** on `/api/inventory` and `/api/users` - fine at BRD's expected
  scale, but worth adding via `Pageable` if item counts grow large.
- **CORS is wide open** (`*`) for development convenience - narrow
  `allowedOriginPatterns` in `SecurityConfig` to your real web admin panel
  origin before deploying.
