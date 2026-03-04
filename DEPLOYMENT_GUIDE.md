# 🚀 Nutri-Guesser – Deployment Guide

## Übersicht

Diese Anleitung beschreibt das containerisierte Deployment der Nutri-Guesser-Anwendung.

| Service   | Technologie          | Container-Port | Default Host-Port |
|-----------|---------------------|----------------|-------------------|
| Frontend  | React + Vite + Nginx | 80             | 3000              |
| Backend   | Java 21 + Quarkus   | 8080           | 8080              |
| Datenbank | H2 (file-based)     | –              | –                 |

### Architektur

```
┌─────────────┐       ┌──────────────┐       ┌──────────────┐
│   Browser   │──────▶│   Frontend   │──────▶│   Backend    │
│             │ :3000 │ (Nginx)      │ /api  │ (Quarkus)    │
└─────────────┘       └──────────────┘       └──────┬───────┘
                                                     │
                                              ┌──────▼───────┐
                                              │  H2 Database  │
                                              │ (Volume Mount)│
                                              └──────────────┘
```

Der Nginx-Server im Frontend fungiert als Reverse-Proxy: Alle Anfragen an `/api/` werden
an den Backend-Service (`http://backend:8080`) weitergeleitet.

---

## Teil A: Lokales Testen (vor dem Portainer-Deployment)

### 1. Voraussetzungen

- **Docker** ≥ 24.0 (oder Docker Desktop)
- **Docker Compose** ≥ 2.20 (Plugin-Variante `docker compose`)

Prüfen:
```bash
docker --version
docker compose version
```

### 2. Umgebungsvariablen konfigurieren

```bash
# Im Projekt-Stammverzeichnis:
cp .env.example .env
```

Öffne die `.env`-Datei und setze mindestens diese Werte:

| Variable       | Beschreibung                          | Pflicht | Beispiel                                                    |
|---------------|---------------------------------------|---------|-------------------------------------------------------------|
| `DB_USER`     | H2-Datenbank-Benutzername             | ✅       | `CALORIE_APP`                                               |
| `DB_PASSWORD` | H2-Datenbank-Passwort                 | ✅       | `mein_sicheres_passwort`                                    |
| `DB_JDBC_URL` | JDBC-URL für H2 (file-mode)           | ⚙️      | `jdbc:h2:file:/data/h2/calorie-game-db;NON_KEYWORDS=USER` |
| `FRONTEND_PORT` | Host-Port für das Frontend          | ⚙️      | `3000`                                                      |
| `BACKEND_PORT`  | Host-Port für das Backend           | ⚙️      | `8080`                                                      |
| `CORS_ORIGINS`  | Erlaubte CORS-Origins               | ⚙️      | `http://localhost:3000`                                     |

> ⚙️ = Optional (hat sinnvolle Defaults)

### 3. H2-Datenbankdatei bereitstellen

Die bestehende H2-Datenbank liegt bereits im Projektverzeichnis unter `database/h2/calorie-game-db.mv.db`.
Die `docker-compose.yml` verwendet einen **Bind Mount**, der dieses Verzeichnis direkt in den Container
mountet (`./database/h2` → `/data/h2`). Es muss nichts kopiert werden.

**Wichtig – Dateiberechtigungen prüfen:**

Der Backend-Container läuft als User `appuser`. Die DB-Datei muss les- und schreibbar sein:

```bash
chmod 666 database/h2/calorie-game-db.mv.db
```

> ⚠️ Die `.mv.db`-Datei darf während des Starts von keinem anderen Prozess (z.B. einer
> lokalen Quarkus-Instanz) gesperrt sein. H2 im File-Mode unterstützt nur einen Zugriff gleichzeitig.

### 4. Docker Images bauen

```bash
docker compose build
```

Dies baut beide Images (Frontend und Backend) in Multi-Stage-Builds.
Der erste Build dauert einige Minuten (Maven-Dependencies, npm install).

### 5. Stack starten

```bash
docker compose up -d
```

### 6. Health-Checks durchführen

```bash
# Frontend erreichbar?
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000
# Erwartete Antwort: 200

# Backend Health-Endpoint:
curl http://localhost:8080/q/health/ready
# Erwartete Antwort: {"status":"UP", ...}

# Backend Live-Endpoint:
curl http://localhost:8080/q/health/live
# Erwartete Antwort: {"status":"UP", ...}

# API über Frontend-Proxy testen:
curl http://localhost:3000/api/leaderboard
# Erwartete Antwort: JSON-Array mit Leaderboard-Daten

# Swagger UI (falls aktiviert):
# Browser: http://localhost:8080/q/swagger-ui
```

### 7. Logs prüfen

```bash
# Alle Services:
docker compose logs -f

# Nur Backend:
docker compose logs -f backend

# Nur Frontend:
docker compose logs -f frontend
```

### 8. Stack stoppen

```bash
# Stoppen (Daten bleiben erhalten):
docker compose down

# Stoppen und Volumes löschen (⚠️ Datenverlust!):
docker compose down -v
```

### 9. Troubleshooting

| Problem | Lösung |
|---------|--------|
| **Port bereits belegt** | `FRONTEND_PORT` oder `BACKEND_PORT` in `.env` ändern |
| **Backend startet nicht** | `docker compose logs backend` prüfen – häufig DB-Pfad oder fehlende ENV-Variable |
| **DB-Fehler: "Database not found"** | Prüfen, ob die `.mv.db`-Datei im Projektverzeichnis liegt: `ls -la database/h2/` |
| **DB-Fehler: "Wrong user name or password"** | `DB_USER` und `DB_PASSWORD` in `.env` müssen mit den Werten übereinstimmen, die bei der DB-Erstellung verwendet wurden |
| **Frontend zeigt leere Seite** | Browser-Konsole prüfen – API-Aufrufe müssen über `/api/` gehen |
| **CORS-Fehler im Browser** | `CORS_ORIGINS` in `.env` muss die Frontend-URL enthalten |
| **H2-Datei wird gelockt** | H2 im File-Mode unterstützt nur einen Prozess gleichzeitig. Sicherstellen, dass keine andere Instanz läuft |
| **Build-Fehler (Maven)** | `docker compose build --no-cache backend` versuchen |
| **Build-Fehler (npm)** | `docker compose build --no-cache frontend` versuchen |

---

## Teil B: Deployment auf Portainer

### 1. Images vorbereiten

**Option A: Docker Hub / Registry verwenden**

```bash
# Images taggen:
docker tag nutri-guesser-frontend:latest registry.example.com/nutri-guesser-frontend:latest
docker tag nutri-guesser-backend:latest registry.example.com/nutri-guesser-backend:latest

# Images pushen:
docker push registry.example.com/nutri-guesser-frontend:latest
docker push registry.example.com/nutri-guesser-backend:latest
```

Dann in `docker-compose.yml` die `image:`-Einträge auf die Registry-URLs setzen
(via `FRONTEND_IMAGE` und `BACKEND_IMAGE` ENV-Variablen).

**Option B: Portainer Git-Integration**

Portainer kann direkt aus einem Git-Repository bauen. Nutze die `build:`-Kontexte
in der `docker-compose.yml`.

### 2. Stack in Portainer anlegen

1. Portainer öffnen → **Stacks** → **Add Stack**
2. Name: `nutri-guesser`
3. **Build Method**: Wähle je nach Option:
   - **Web Editor**: Inhalt der `docker-compose.yml` einfügen
   - **Repository**: Git-URL angeben
   - **Upload**: `docker-compose.yml` hochladen

### 3. Environment Variables eintragen

In Portainer unter dem Stack → **Environment Variables**:

Trage alle Variablen aus `.env.example` ein:

```
DB_USER=CALORIE_APP
DB_PASSWORD=<sicheres_passwort>
DB_JDBC_URL=jdbc:h2:file:/data/h2/calorie-game-db;NON_KEYWORDS=USER
FRONTEND_PORT=3000
BACKEND_PORT=8080
CORS_ORIGINS=https://deine-domain.de
FRONTEND_IMAGE=nutri-guesser-frontend:latest
BACKEND_IMAGE=nutri-guesser-backend:latest
```

> ⚠️ Passe `CORS_ORIGINS` an die tatsächliche Domain an!

### 4. H2-Datenbank für Portainer

Die `docker-compose.yml` verwendet einen Bind Mount (`./database/h2:/data/h2`).

**Für Portainer-Deployments:**

Stelle sicher, dass das `database/h2/`-Verzeichnis mit der `calorie-game-db.mv.db`-Datei
auf dem Docker-Host im Projekt-Verzeichnis vorhanden ist. Bei Git-basiertem Deployment
wird es automatisch mitgeklont.

Falls du die DB-Datei manuell bereitstellen musst:

```bash
# Auf dem Docker-Host im Projekt-Verzeichnis:
mkdir -p database/h2
cp /pfad/zur/calorie-game-db.mv.db database/h2/
chmod 666 database/h2/calorie-game-db.mv.db
```

### 5. Stack deployen

Klicke in Portainer auf **Deploy the stack**.

### 6. Deployment verifizieren

1. **Container-Status**: Portainer → Stack → Alle Container sollten `running` und `healthy` sein
2. **Logs prüfen**: Klicke auf den jeweiligen Container → **Logs**
3. **Health-Status**: Portainer zeigt den Health-Status direkt an den Containern an
4. **Frontend testen**: Öffne `http://<host>:<FRONTEND_PORT>` im Browser
5. **Backend API**: `http://<host>:<BACKEND_PORT>/q/health`

### 7. Updates deployen

```bash
# Neue Images bauen und pushen (lokal):
docker compose build
docker push registry.example.com/nutri-guesser-frontend:latest
docker push registry.example.com/nutri-guesser-backend:latest
```

In Portainer: Stack → **Update the stack** → **Pull and redeploy**

---

## Dateistruktur der Deployment-Konfiguration

```
nutri-guesser/
├── .env.example              ← Vorlage für Umgebungsvariablen
├── .env                      ← Deine lokale Konfiguration (nicht im Git!)
├── .gitignore                ← Schützt .env vor Git-Commit
├── docker-compose.yml        ← Docker Compose Stack-Definition
├── DEPLOYMENT_GUIDE.md       ← Diese Anleitung
├── frontend/
│   ├── Dockerfile            ← Multi-Stage: Node Build → Nginx
│   ├── nginx.conf            ← Nginx-Konfiguration mit Reverse-Proxy
│   └── .dockerignore
├── backend/
│   ├── Dockerfile            ← Multi-Stage: Maven Build → JRE Runtime
│   └── .dockerignore
└── database/
    └── h2/
        └── calorie-game-db.mv.db  ← Bestehende H2-Datenbank
```

