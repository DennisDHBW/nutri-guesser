# Nutri-Guesser Testdaten - Schnellstart-Anleitung

## ✅ Was wurde erstellt?

Umfangreiche Testdaten für alle Datenbanktabellen:

| Datei | Beschreibung | Anzahl |
|-------|--------------|--------|
| `01-products.sql` | Realistische Lebensmittel mit Nährwerten | 50 Produkte |
| `02-players.sql` | Spieler mit kreativen Nicknames | 150 Spieler |
| `03-game-sessions.sql` | Abgeschlossene Spielsessions | 120 Sessions |
| `04-rounds.sql` | Einzelne Runden mit Antworten | 600 Rounds |
| `05-leaderboard.sql` | Leaderboard-Rankings | 100 Einträge |

**Gesamt: ~970 Datensätze**

## 🚀 Testdaten aktivieren

### Variante 1: Automatisch beim Start (empfohlen)

Die Testdaten sind bereits in der `import.sql` kombiniert und werden automatisch geladen:

```bash
cd backend
./mvnw quarkus:dev
```

Beim Start werden alle Testdaten automatisch in die H2-Datenbank geladen.

### Variante 2: Testdaten neu generieren

Falls Sie die `import.sql` neu erstellen möchten:

```bash
cd backend/src/main/resources/test-data
./build-import-sql.sh
```

Dann Quarkus neu starten.

## 🔄 Testdaten deaktivieren

```bash
cd backend/src/main/resources
mv import.sql import.sql.disabled
```

Dann Quarkus neu starten - es werden keine Testdaten geladen.

## 🗑️ Datenbank zurücksetzen

Um die Datenbank komplett neu zu laden:

```bash
# 1. Quarkus stoppen (Ctrl+C)
# 2. H2-Datenbankdatei löschen
rm backend/data/dev-db.mv.db
# 3. Quarkus neu starten
cd backend
./mvnw quarkus:dev
```

Die Datenbank wird neu erstellt und alle Testdaten werden neu geladen.

## 📊 Was ist in den Testdaten?

### Produkte (50)
- **Snacks & Süßigkeiten**: Snickers, KitKat, Milka, Hanuta, Nutella, Haribo
- **Chips & Salzsnacks**: Pringles, Lay's, Funny-frisch, Chio
- **Getränke**: Coca-Cola, Fanta, Red Bull, Sprite
- **Pizza & Fertiggerichte**: Wagner, Dr. Oetker
- **Eis & Desserts**: Magnum, Ben & Jerry's, Cornetto
- Alle mit realistischen Kalorien-Angaben (z.B. Coca-Cola: 42 kcal/100g)

### Spieler (150)
- Kreative Namen wie: NutriMaster, CalorieKing, PizzaHunter, ChocolateLover
- Thematische Gruppen: Food, Vegetables, Fruits, Game-themed

### Game Sessions (120)
- **Top 10**: 3800-4850 Punkte (~90% richtige Antworten)
- **Durchschnitt**: 1000-2000 Punkte (~60% richtig)
- **Anfänger**: 160-500 Punkte (~20% richtig)
- Zeitstempel: 01. Feb - 12. Feb 2025

### Leaderboard (100)
- Vollständiges Ranking von Platz 1 bis 100
- Konsistente Scores und Zeitstempel
- Verknüpft mit entsprechenden Sessions und Spielern

## 🔍 Testdaten prüfen

### H2 Console öffnen
```
http://localhost:8080/q/dev
```

Dann "H2 Console" öffnen und Queries ausführen:

```sql
-- Anzahl Einträge pro Tabelle
SELECT COUNT(*) FROM PLAYER;           -- 150
SELECT COUNT(*) FROM PRODUCT;          -- 50
SELECT COUNT(*) FROM GAME_SESSION;     -- 120
SELECT COUNT(*) FROM ROUND;            -- 600
SELECT COUNT(*) FROM LEADERBOARD_ENTRY;-- 100

-- Top 10 Leaderboard
SELECT rank, score, p.nickname 
FROM LEADERBOARD_ENTRY le
JOIN PLAYER p ON le.player_id = p.player_id
ORDER BY rank
LIMIT 10;

-- Produkt mit den meisten Kalorien
SELECT name, brand, kcal_100g
FROM PRODUCT p
JOIN NUTRITION_FACTS nf ON p.barcode = nf.barcode
ORDER BY kcal_100g DESC
LIMIT 5;
```

## 🎯 API-Endpoints testen

Mit den Testdaten können Sie sofort alle Endpoints testen:

```bash
# Leaderboard abrufen
curl http://localhost:8080/api/leaderboard

# Spieler-Details
curl http://localhost:8080/api/players

# Produkte
curl http://localhost:8080/api/products
```

## 💡 Tipps

1. **Konsistenz**: Alle Daten sind referenziell korrekt verknüpft
2. **Realismus**: Produktdaten basieren auf echten Lebensmitteln
3. **Vielfalt**: Große Bandbreite an Scores für aussagekräftige Tests
4. **Performance**: 970 Einträge sind genug für Leaderboard-Tests ohne zu viel zu sein

## ⚙️ Anpassungen

### Mehr Daten generieren

Bearbeiten Sie die SQL-Dateien im `test-data/` Verzeichnis und führen Sie dann aus:

```bash
./build-import-sql.sh
```

### Einzelne Tabellen laden

Sie können auch nur bestimmte Dateien laden:

```sql
-- In import.sql nur gewünschte Dateien einbinden
-- Kommentieren Sie ungewünschte Zeilen aus
```

## 🐛 Probleme?

- **Daten werden nicht geladen**: Prüfen Sie, ob `import.sql` existiert
- **Constraint-Fehler**: Datenbank zurücksetzen (siehe oben)
- **Zu viele Daten**: Deaktivieren Sie einzelne SQL-Dateien in `import.sql`

## 📚 Weitere Informationen

Siehe: `test-data/README.md` für detaillierte Dokumentation

# ...existing code...

## Runtime-Endpoint (Dev)

Du kannst die Testdaten zur Laufzeit in der laufenden Dev-Instanz laden:

```bash
curl -X POST "http://localhost:8080/api/admin/testdata/load"
```

Optional kannst du vorher die Tabellen leeren:

```bash
curl -X POST "http://localhost:8080/api/admin/testdata/load?clear=true"
```

Optional kannst du nur bestimmte Dateien laden (Komma-separiert):

```bash
curl -X POST "http://localhost:8080/api/admin/testdata/load?files=01-products.sql,02-players.sql"
```

Hinweis: Der Endpoint ist nur im Dev-Profil aktiv.

