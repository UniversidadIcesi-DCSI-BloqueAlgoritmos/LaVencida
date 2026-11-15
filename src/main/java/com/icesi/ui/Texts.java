package com.icesi.ui;

import com.icesi.model.IncidentStatus;
import com.icesi.model.IncidentType;
import com.icesi.model.Severity;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;

// Nombres en espanol de los enums del modelo, para mostrarlos en la interfaz.
public final class Texts {

    private Texts() {
    }

    public static String of(Severity severity) {
        if (severity == Severity.HIGH) {
            return "ALTA";
        }
        if (severity == Severity.MEDIUM) {
            return "MEDIA";
        }
        return "BAJA";
    }

    public static String of(IncidentStatus status) {
        if (status == IncidentStatus.PENDING) {
            return "PENDIENTE";
        }
        if (status == IncidentStatus.IN_PROGRESS) {
            return "EN PROCESO";
        }
        return "RESUELTO";
    }

    public static String of(VehicleStatus status) {
        if (status == VehicleStatus.AVAILABLE) {
            return "DISPONIBLE";
        }
        if (status == VehicleStatus.EN_ROUTE) {
            return "EN RUTA";
        }
        if (status == VehicleStatus.ATTENDING) {
            return "ATENDIENDO";
        }
        return "FUERA DE SERVICIO";
    }

    public static String of(IncidentType type) {
        if (type == IncidentType.THEFT) {
            return "ROBO";
        }
        if (type == IncidentType.FIRE) {
            return "INCENDIO";
        }
        return "ACCIDENTE";
    }

    public static String of(VehicleType type) {
        if (type == VehicleType.PATROL) {
            return "PATRULLA";
        }
        if (type == VehicleType.AMBULANCE) {
            return "AMBULANCIA";
        }
        return "BOMBEROS";
    }
}
