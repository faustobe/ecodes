#!/bin/bash

echo "========================================="
echo "Build e Install APK da terminale"
echo "========================================="
echo ""

# 1. Verifica connessione ADB
echo "1. Verifica dispositivi connessi..."
DEVICES=$(adb devices | grep -w "device$" | awk '{print $1}')
DEVICE_COUNT=$(echo "$DEVICES" | wc -l)

if [ -z "$DEVICES" ]; then
    echo "❌ Nessun dispositivo Android connesso!"
    echo "   Collega il dispositivo e abilita USB debugging"
    echo "   Oppure avvia un emulatore"
    exit 1
fi

echo "✓ Trovati $DEVICE_COUNT dispositivo/i:"
for device in $DEVICES; do
    MODEL=$(adb -s "$device" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    echo "   - $device ($MODEL)"
done
echo ""

# 2. Build APK
echo "2. Compilazione APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Errore durante la compilazione!"
    exit 1
fi
echo "✓ APK compilato"
echo ""

# 3. Trova APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK non trovato in $APK_PATH"
    exit 1
fi

# 4. Disinstalla e installa su tutti i dispositivi
PACKAGE_NAME="it.faustobe.ecodes"
OLD_PACKAGE_NAME="com.example.ecodereader"

echo "3. Disinstallazione e installazione su tutti i dispositivi..."
echo ""

INSTALL_SUCCESS=0
INSTALL_FAILED=0

for device in $DEVICES; do
    MODEL=$(adb -s "$device" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    echo "📱 Dispositivo: $device ($MODEL)"

    # Disinstalla versioni precedenti
    adb -s "$device" uninstall "$OLD_PACKAGE_NAME" 2>/dev/null >/dev/null
    adb -s "$device" uninstall "$PACKAGE_NAME" 2>/dev/null >/dev/null

    # Installa APK
    echo "   Installazione in corso..."
    if adb -s "$device" install -r "$APK_PATH" 2>&1 | grep -q "Success"; then
        echo "   ✓ Installazione completata"
        INSTALL_SUCCESS=$((INSTALL_SUCCESS + 1))
    else
        echo "   ❌ Errore installazione"
        INSTALL_FAILED=$((INSTALL_FAILED + 1))
    fi
    echo ""
done

echo "========================================="
if [ $INSTALL_FAILED -eq 0 ]; then
    echo "✓ App installata su tutti i $INSTALL_SUCCESS dispositivi!"
else
    echo "⚠ Installazione completata su $INSTALL_SUCCESS/$DEVICE_COUNT dispositivi"
    echo "  ($INSTALL_FAILED falliti)"
fi
echo "========================================="
echo ""
echo "Apri l'app 'E-Codes' sui dispositivi"
echo ""
