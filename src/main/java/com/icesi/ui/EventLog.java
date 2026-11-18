package com.icesi.ui;

import com.icesi.structures.LinkedList;
import javafx.scene.paint.Color;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Registro de lo que va pasando en la ciudad (alertas, despachos, resoluciones), el mas nuevo primero.
// Lo comparten las pantallas: unas escriben y el Centro de Monitoreo lo muestra.
public class EventLog {

    private static final int MAX_ENTRIES = 30;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    // tipos de evento, cada uno con su color en la pantalla
    public static final Color ALERT = RetroTheme.PINK;
    public static final Color DISPATCH = RetroTheme.BLUE;
    public static final Color RESOLVED = RetroTheme.GREEN;
    public static final Color SYSTEM = RetroTheme.LAVENDER;

    private LinkedList<Entry> entries = new LinkedList<>();
    private Runnable onChange;

    // Agrega un evento con la hora actual y avisa a quien este mostrando el registro.
    public void add(String text, Color color) {
        entries.addFirst(new Entry(LocalTime.now().format(TIME), text, color));
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1); // se borra el mas viejo
        }
        if (onChange != null) {
            onChange.run();
        }
    }

    public LinkedList<Entry> getEntries() {
        return entries;
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    // Un evento del registro: hora, texto y color.
    public static class Entry {
        private String time;
        private String text;
        private Color color;

        public Entry(String time, String text, Color color) {
            this.time = time;
            this.text = text;
            this.color = color;
        }

        public String getTime() {
            return time;
        }

        public String getText() {
            return text;
        }

        public Color getColor() {
            return color;
        }
    }
}
