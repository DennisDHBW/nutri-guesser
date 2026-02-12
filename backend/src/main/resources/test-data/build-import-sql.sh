#!/bin/bash
# ============================================================================
# Script zum Kombinieren aller Testdaten in eine import.sql Datei
# ============================================================================
# Usage: ./build-import-sql.sh
# Output: import.sql (überschreibt existing!)
# ============================================================================

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
OUTPUT_FILE="$SCRIPT_DIR/../import.sql"

echo "Erstelle kombinierte import.sql Datei..."

cat > "$OUTPUT_FILE" << 'EOF'
-- ============================================================================
-- AUTO-GENERATED TEST DATA IMPORT
-- ============================================================================
-- Diese Datei wurde automatisch mit build-import-sql.sh generiert
-- Um Testdaten zu deaktivieren: import.sql -> import.sql.disabled umbenennen
-- ============================================================================

EOF

# Füge alle Testdaten-Dateien zusammen
for file in "$SCRIPT_DIR"/01-products.sql \
            "$SCRIPT_DIR"/02-players.sql \
            "$SCRIPT_DIR"/03-game-sessions.sql \
            "$SCRIPT_DIR"/04-rounds.sql \
            "$SCRIPT_DIR"/05-leaderboard.sql; do

    if [ -f "$file" ]; then
        echo "Füge hinzu: $(basename $file)"
        cat "$file" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
    else
        echo "WARNUNG: Datei nicht gefunden: $file"
    fi
done

echo ""
echo "✓ Fertig! import.sql wurde erstellt in:"
echo "  $OUTPUT_FILE"
echo ""
echo "Zum Aktivieren der Testdaten:"
echo "  1. Starte Quarkus im Dev-Modus: ./mvnw quarkus:dev"
echo "  2. Die Daten werden automatisch geladen"
echo ""
echo "Zum Deaktivieren:"
echo "  mv import.sql import.sql.disabled"

