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
