# Projektübersicht: Web-Services Gruppenarbeit - "NutriGuesser"

## 1. Administrative Informationen
* **Modul:** Web-Services (Prüfungsleistung)
* **Studiengang:** Angewandte Informatik
* **Dozent:** Alexander Auch
* **Institution:** DHBW Mosbach
* **Team:** 4 Personen (Vorgabe: 4-5 Mitglieder)
* **Arbeitsaufwand:** Ca. 30 Stunden pro Person

---

## 2. Zeitplan & Deadlines
* **Aufgabenstellung:** Anfang Februar 2026
* **Vortragstermin:** 26.02.2026 (12 Min. Präsentation + 3 Min. Fragen)
* **Abgabe der Projekte:** Bis zum 29.03.2026 via GitLab und Docker
* **GitLab-Account:** Umgehende Registrierung und Zugriff auf die Gruppe "inf24" erforderlich (manuelle Freischaltung notwendig).

---

## 3. Projektidee: NutriGuesser
Das Projekt realisiert ein interaktives Lernspiel, das das Bewusstsein für die Kaloriendichte von Lebensmitteln schärft.
* **Spielmechanik:** Ein zufälliges Lebensmittel wird inklusive Bild von der OpenFoodFacts API geladen.
* **Interaktion:** Der Spieler muss schätzen, in welchem Bereich die tatsächlichen Kalorien (pro 100g) liegen. Je präziser die Wahl des Bereichs, desto mehr Punkte werden vergeben.
* **Abschluss:** Nach dem Spiel erfolgt eine Auswertung. Ein passendes Katzen-GIF (via Cataas API) spiegelt die Leistung wider. 
* **Highscore:** Spieler mit Top-Ergebnissen können sich in einem globalen Scoreboard verewigen.

---

## 4. Technische Architektur (WebService X)
Das Projekt basiert auf einem zentralen WebService X, der als "Composition" (Zusammensetzung) aus drei Diensten fungiert.

### Beteiligte Webservices (Y-Services)
1. **Y1: OpenFoodFacts API (Extern/REST):** Dient zum Abruf von Lebensmittelbildern und Nährwertdaten.
2. **Y2: Cataas API (Extern/REST):** Bereitstellung dynamischer Katzen-GIFs basierend auf der erreichten Punktzahl.
3. **Y3: Scoreboard-Service (Intern/Eigenentwicklung):** * Verwaltet die Top 10 Highscores.
    * Nutzt mindestens einen komplexen Datentyp.
    * Wird über ein OpenAPI-Schema (Swagger) beschrieben.

### Frameworks & Tools
* **Backend:** Frei wählbar (Empfehlung der Vorlesung: Quarkus).
* **Frontend:** Web-UI (z. B. React/Vue) ist als Client für WebService X integriert.
* **Deployment:** Alle Komponenten müssen in Linux-Docker-Containern auf dem DHBW-Host lauffähig sein.

---

## 5. Prozessmodellierung (BPMN)
Der Geschäftsprozess wird detailliert mit Signavio modelliert und dokumentiert:
* **Umfang:** Mindestens 20 Standard-BPMN-Elemente (Aktivitäten, Gateways, Ereignisse).
* **Hierarchie:** Anwendung der "Hierarchischen Expansion" zur übersichtlichen Gestaltung.
* **Rollen:** Einbindung von mindestens zwei verschiedenen Rollen (z. B. Spieler und System).
* **Spezialisierung:** Die Composition-Logik der WebServices wird ebenfalls grafisch als BPMN-Modell dargestellt.

---

## 6. Bewertungskriterien
Die Bewertung erfolgt als offizielle Prüfungsleistung mit folgendem Fokus:
* **Präsentation (20 Pkt):** Jeder Teilnehmer muss einen Teil des Vortrags übernehmen.
* **BPMN-Modell (10 Pkt):** Korrekte Anwendung der Standards und Verständlichkeit.
* **WSDL/OpenAPI (10 Pkt):** Korrekte Beschreibung des internen Services inkl. komplexer Datentypen.
* **Implementierung (20 Pkt):** Lauffähigkeit im Docker, Code-Qualität und Dokumentation.
* **Web-UI (10 Pkt):** Umsetzung eines Frontends (notwendig für eine Note besser als "gut").
* **Abgabe & Repo (10 Pkt):** Korrekte Struktur im GitLab (keine IDE-Artefakte, sauberes Dependency-Management).
* **Dokumentation & Plan (10 Pkt):** PDF-Dokumentation (~10 Seiten) inklusive Projektplan.
* **Idee & Komplexität (10 Pkt):** Originalität und korrekte Verknüpfung der Web-Services.

---

## 7. Verzeichnisse & URLs
* **GitLab Repository:** http://10.50.15.50/abgaben_ws/inf24
* **Docker Host:** http://10.50.15.53:9000/
* **Repository-Name:** `inf24-nutriguesser`

---

## 8. 🚀 Setup & Installation

### ⚡ Schritt-für-Schritt Installation

#### 1. Java 21 JDK installieren
```bash
sudo apt-get update
sudo apt-get install -y openjdk-21-jdk openjdk-21-jdk-headless
```

Verifizierung:
```bash
java -version
# Sollte: openjdk 21.x.x ausgeben
```

#### 2. JAVA_HOME konfigurieren
```bash
# Zu ~/.bashrc hinzufügen:
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

# Änderungen aktivieren:
source ~/.bashrc
```

Verifizierung:
```bash
echo $JAVA_HOME
# Sollte: /usr/lib/jvm/java-21-openjdk-amd64 ausgeben
```

#### 3. Maven Dependencies laden & Projekt bauen
```bash
cd backend
./mvnw clean install -DskipTests
```

Dies installiert automatisch:
- Alle Maven-Abhängigkeiten (Quarkus, Jakarta, etc.)
- Kompiliert den Java-Code
- Erstellt das JAR-Artefakt

### ▶️ Anwendung starten

```bash
cd backend
./mvnw quarkus:dev
```

**Dann im Browser öffnen:** http://localhost:8080

### 🎯 IntelliJ IDEA Setup

#### Projekt öffnen:
1. **File → Open** → Navigiere zur `backend/pom.xml`
2. **"Open as Project"** auswählen

#### JDK konfigurieren:
1. **File → Project Structure** (Strg+Alt+Shift+S)
2. **Project SDK:** **21** auswählen (Java 21.0.8 LTS empfohlen, nicht 25.0.2)
3. Falls nicht verfügbar: **Add SDK → JDK** → `/usr/lib/jvm/java-21-openjdk-amd64`

#### Maven-Plugin aktivieren (falls orange Tassen erscheinen):
Das Symbol zeigt an, dass IntelliJ das Maven-Projekt nicht erkannt hat.

**Lösung:**
1. **Maven-Plugin aktivieren:** `Settings/Preferences → Plugins`
   - Suche nach **"Maven Integration"** und stelle sicher, dass es aktiviert ist
   - Starte IntelliJ neu

2. **Projekt als Maven-Projekt importieren:**
   - Öffne `backend/pom.xml` direkt in IntelliJ
   - Klicke auf die gelbe Leiste oben: **"Add as Maven Project"**
   - Oder: Rechtsklick auf `pom.xml` → **Maven → Reload Project**

3. **Abhängigkeiten laden:**
   - **View → Tool Windows → Maven** (dann sollte das Maven-Fenster oben rechts erscheinen)
   - Rechtsklick auf `backend` im Maven-Fenster → **Reimport**
   - Oder im Terminal:
     ```bash
     cd backend
     ./mvnw -DskipTests clean compile
     ```

4. **Caches löschen (falls es nicht hilft):**
   - **File → Invalidate Caches / Restart**

### 📋 Systemanforderungen

- **OS:** Ubuntu 20.04+ oder äquivalent
- **RAM:** Mind. 4 GB
- **Java:** 21 LTS (OpenJDK empfohlen)
- **Maven:** 3.9.12+ (via Maven Wrapper inkludiert)
- **Docker:** Optional, für Container-Builds
- **Git:** Für Repository-Zugriff

### 🛠️ Häufige Probleme

| Problem | Lösung |
|---------|--------|
| Orange Tassen in IntelliJ (Maven nicht erkannt) | `Settings → Plugins → Maven Integration` aktivieren und IntelliJ neustarten |
| "Cannot resolve symbol 'jakarta'" | Maven-Projekt neu laden: `View → Tool Windows → Maven` → Rechtsklick `backend` → Reimport |
| "Release version 21 not supported" | `echo $JAVA_HOME` prüfen, muss `/usr/lib/jvm/java-21-openjdk-amd64` sein |
| Port 8080 belegt | `./mvnw quarkus:dev -Dquarkus.http.port=8081` verwenden |

---

## 9. 🛠️ Häufig verwendete Befehle

```bash
# Development-Modus (Hot-Reload)
./mvnw quarkus:dev

# Projekt bauen
./mvnw clean install

# Tests ausführen
./mvnw test

# Produktions-JAR erstellen
./mvnw clean package -DskipTests

# Docker-Image bauen (JVM)
docker build -f src/main/docker/Dockerfile.jvm -t nutri-guesser:latest .

# Docker starten
docker run -p 8080:8080 nutri-guesser:latest

# Native Image Build (benötigt GraalVM)
./mvnw clean install -DskipTests -Dnative
```

---

## 10. 📂 Backend-Projektstruktur

```
backend/
├── pom.xml                      # Maven Konfiguration
├── mvnw / mvnw.cmd              # Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── api/
│   │   │   │   └── CataasResource.java     # REST API Endpoints
│   │   │   ├── client/
│   │   │   │   └── CataasClient.java       # API Clients
│   │   │   ├── client.cataas.dto/
│   │   │   │   └── CataasResponseDTO.java  # Data Transfer Objects
│   │   │   └── service/
│   │   │       ├── CataasService.java      # Business Logic
│   │   │       └── ScoreTagService.java    # Score-Mapping
│   │   ├── docker/
│   │   │   ├── Dockerfile.jvm
│   │   │   ├── Dockerfile.native
│   │   │   └── ...
│   │   └── resources/
│   │       └── application.properties      # Konfiguration
│   └── test/
└── target/                      # Build Output
```

---

## 11. 🔧 Behobene Probleme bei Installation

✅ Java Security-Dateien repariert (Ubuntu 24.04 Fix)  
✅ JCE Cryptographic-Konfiguration behoben  
✅ Maven Wrapper aktiviert  
✅ Docker Service konfiguriert  

---

## 12. ⚠️ Troubleshooting

| Problem | Lösung |
|---------|--------|
| "Error loading java.security file" | Siehe SETUP_INSTRUCTIONS.md Schritt 2 |
| Maven-Abhängigkeiten nicht gefunden | Internetverbindung prüfen oder setup.sh neu ausführen |
| Port 8080 ist belegt | Anderen Port verwenden: `./mvnw quarkus:dev -Dquarkus.http.port=8081` |
| Docker benötigt sudo | `sudo usermod -aG docker $USER` (dann ab-/anmelden) |

---

## 13. 📞 Support

Bei Fragen oder Problemen:
1. Überprüfen Sie den Setup-Abschnitt (ab Punkt 8)
2. Führen Sie `./mvnw clean compile` aus
3. Überprüfen Sie Maven-Logs: `./mvnw clean install -X`
