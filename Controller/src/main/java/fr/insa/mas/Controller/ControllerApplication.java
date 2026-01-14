package fr.insa.mas.Controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

@RestController
@SpringBootApplication
@RequestMapping("/Controller/")
public class ControllerApplication {

    private final DatabaseLogger databaseLogger = new DatabaseLogger();
    private static final Logger logger = LoggerFactory.getLogger(ControllerApplication.class);

    // Microservices URLs existants
    private final String MovDetectUrl     = "http://localhost:8082/MovementDetection/";
    private final String LightControlUrl  = "http://localhost:8081/LightControl/";
    private final String AlarmControlUrl  = "http://localhost:8083/AlarmControl/";

    // Flags d’automatisation
    private boolean autoEnergyLight = false;   // Scenario 3 (lumières)
    private boolean autoSecurity    = false;   // Scenario 2 (alarme hors horaires)
    private boolean autoAccessSim   = false;   // Scenario 1 SIMULE (barrière simulée)

    // Horaires de sécurité (Scenario 2)
    private LocalTime securityStart = LocalTime.of(8, 0);
    private LocalTime securityEnd   = LocalTime.of(18, 0);

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==========================
    // Scenario 3+ (NOUVEAU) : compteur énergie (sans microservice)
    // ==========================
    // Hypothèses : lampe = 60W (0.06 kW) -> modifiable
    private double lampPowerKw = 0.06;

    private Boolean lastLightState = null;           // état connu par le Controller
    private LocalDateTime lightOnSince = null;       // début dernier ON
    private long totalLightOnSeconds = 0;            // cumul temps ON

    // ==========================
    // Utils
    // ==========================
    private String buildHtml(String title, String body, String bgColor, String cardColor, String titleColor) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: " + bgColor + "; text-align: center; margin: 0; padding: 0; }" +
                ".container { padding: 18px; background-color: " + cardColor + "; border-radius: 16px; margin: 60px auto; max-width: 560px; box-shadow: 0 10px 30px rgba(0,0,0,0.25); }" +
                "h1 { color: " + titleColor + "; margin: 0; padding: 12px; border-radius: 12px; }" +
                "p { font-size: 16px; color: #EAEAEA; line-height: 1.6; }" +
                ".meta { font-size: 13px; color: #BDBDBD; margin-top: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>" + title + "</h1>" +
                "<p>" + body + "</p>" +
                "<div class='meta'>Time: " + LocalDateTime.now().format(DT) + "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private RestTemplate rt() {
        return new RestTemplate();
    }

    private boolean isOutsideSecurityHours() {
        LocalTime now = LocalTime.now();
        return now.isBefore(securityStart) || now.isAfter(securityEnd);
    }

    // ==========================
    // Logs test
    // ==========================
    @GetMapping("/logTest")
    public String logTest() {
        databaseLogger.logEvent("Test event from Controller project.");
        return "Event logged successfully!";
    }

    // =========================================================
    // SCENARIO 1 (SIMULE) — Détection présence -> accès parking "ouvert/fermé"
    // (Sans barrière réelle, sans nouveau microservice)
    // =========================================================

    @GetMapping("isAutoAccessSimActivated/")
    public ResponseEntity<String> isAutoAccessSimActivated() {
        String title = "Scenario 1 - Acces parking (simulation)";
        String body  = autoAccessSim
                ? "Mode simulation actif : la detection presence pilote un etat 'acces ouvert/ferme'."
                : "Mode simulation inactif.";

        return new ResponseEntity<>(buildHtml(title, body, "#0A2540", "#0B1220", "#93C5FD"), HttpStatus.OK);
    }

    @GetMapping("setAutoAccessSim/")
    public ResponseEntity<String> setAutoAccessSim(@RequestParam boolean enabled) {
        autoAccessSim = enabled;

        String title = "Scenario 1 - Mise a jour";
        String body  = enabled
                ? "Simulation acces activee. La presence detectee implique 'acces ouvert'."
                : "Simulation acces desactivee.";

        databaseLogger.logEvent("AutoAccessSim = " + enabled);
        logger.info("AutoAccessSim={}", enabled);

        return new ResponseEntity<>(buildHtml(title, body, "#0A2540", "#0B1220", "#93C5FD"), HttpStatus.OK);
    }

    private String runAutoAccessSim() {
        boolean presence = rt().getForObject(MovDetectUrl + "getDetection/", boolean.class);

        if (presence) {
            databaseLogger.logEvent("Scenario1: presence detectee -> acces parking OUVERT (simulation).");
            return "Scenario1: Acces OUVERT (simulation)<br/>";
        } else {
            databaseLogger.logEvent("Scenario1: aucune presence -> acces parking FERME (simulation).");
            return "Scenario1: Acces FERME (simulation)<br/>";
        }
    }

    // =========================================================
    // SCENARIO 3 — Énergie : mouvement -> contrôle éclairage (microservice LightControl)
    // + suivi énergie (sans microservice) (NOUVEAU)
    // =========================================================

    @GetMapping("isAutoEnergyLightActivated/")
    public ResponseEntity<String> isAutoEnergyLightActivated() {
        String title = "Scenario 3 - Eclairage automatique";
        String body  = autoEnergyLight
                ? "Mode economie actif : lumiere ON/OFF selon presence (capteur mouvement)."
                : "Mode economie inactif : lumiere en manuel.";

        return new ResponseEntity<>(buildHtml(title, body, "#0B3D2E", "#0F172A", "#A7F3D0"), HttpStatus.OK);
    }

    @GetMapping("setAutoEnergyLight/")
    public ResponseEntity<String> setAutoEnergyLight(@RequestParam boolean enabled) {
        autoEnergyLight = enabled;

        String title = "Scenario 3 - Mise a jour";
        String body  = enabled
                ? "Eclairage automatique active."
                : "Eclairage automatique desactive.";

        databaseLogger.logEvent("AutoEnergyLight = " + enabled);
        logger.info("AutoEnergyLight={}", enabled);

        return new ResponseEntity<>(buildHtml(title, body, "#0B3D2E", "#0F172A", "#A7F3D0"), HttpStatus.OK);
    }

    // Optionnel : régler la puissance de la lampe (pour rapport/paramétrage)
    @GetMapping("setLampPowerKw/")
    public ResponseEntity<String> setLampPowerKw(@RequestParam double kw) {
        this.lampPowerKw = kw;

        String title = "Parametrage energie";
        String body = "Puissance lampe definie a " + kw + " kW (ex: 0.06 = 60W).";

        databaseLogger.logEvent("LampPowerKw = " + kw);
        return new ResponseEntity<>(buildHtml(title, body, "#0B3D2E", "#0F172A", "#A7F3D0"), HttpStatus.OK);
    }

    private void updateEnergyTracking(boolean newLightState) {
        LocalDateTime now = LocalDateTime.now();

        if (lastLightState == null) {
            lastLightState = newLightState;
            if (newLightState) {
                lightOnSince = now;
            }
            return;
        }

        // Transition OFF -> ON
        if (!lastLightState && newLightState) {
            lightOnSince = now;
        }

        // Transition ON -> OFF : on cumule la durée
        if (lastLightState && !newLightState) {
            if (lightOnSince != null) {
                long seconds = Duration.between(lightOnSince, now).getSeconds();
                if (seconds > 0) totalLightOnSeconds += seconds;
            }
            lightOnSince = null;
        }

        lastLightState = newLightState;
    }

    private String runAutoEnergyLight() {
        String url_getDetection = MovDetectUrl + "getDetection/";
        String url_setLightOn   = LightControlUrl + "setON/?oN=true";
        String url_setLightOff  = LightControlUrl + "setON/?oN=false";

        boolean movement = rt().getForObject(url_getDetection, boolean.class);

        if (movement) {
            rt().getForObject(url_setLightOn, String.class);
            updateEnergyTracking(true);
            databaseLogger.logEvent("Scenario3: mouvement detecte -> lumiere ON.");
            return "Scenario3: Lumiere ON<br/>";
        } else {
            rt().getForObject(url_setLightOff, String.class);
            updateEnergyTracking(false);
            databaseLogger.logEvent("Scenario3: aucun mouvement -> lumiere OFF.");
            return "Scenario3: Lumiere OFF<br/>";
        }
    }

    // NOUVEAU : page pour afficher la conso estimée (sans microservice)
    @GetMapping("energyReport/")
    public ResponseEntity<String> energyReport() {
        // si la lumière est encore ON, on calcule un aperçu sans modifier le cumul
        long seconds = totalLightOnSeconds;
        if (lastLightState != null && lastLightState && lightOnSince != null) {
            seconds += Duration.between(lightOnSince, LocalDateTime.now()).getSeconds();
        }

        double hours = seconds / 3600.0;
        double kwh = hours * lampPowerKw;

        // estimation CO2 (très approximatif) : 0.05 kgCO2/kWh (électricité bas carbone)
        double kgCo2 = kwh * 0.05;

        String title = "Rapport energie (estimation)";
        String body =
                "Temps total lumiere ON: " + String.format("%.2f", hours) + " h<br/>" +
                "Puissance lampe: " + lampPowerKw + " kW<br/>" +
                "Consommation estimee: " + String.format("%.4f", kwh) + " kWh<br/>" +
                "CO2 estime: " + String.format("%.4f", kgCo2) + " kgCO2<br/>" +
                "<br/>Note: estimation a but pedagogique.";

        return new ResponseEntity<>(buildHtml(title, body, "#0B3D2E", "#0F172A", "#A7F3D0"), HttpStatus.OK);
    }

    // =========================================================
    // SCENARIO 2 — Sécurité : mouvement -> alarme (hors horaires)
    // =========================================================

    @GetMapping("isAutoSecurityActivated/")
    public ResponseEntity<String> isAutoSecurityActivated() {
        String title = "Scenario 2 - Securite parking";
        String body  = autoSecurity
                ? "Surveillance active : si mouvement detecte hors horaires, alarme ON."
                : "Surveillance inactive.";

        String extra = "<br/>Horaires autorises: " + securityStart + " -> " + securityEnd +
                "<br/>Etat actuel: " + (isOutsideSecurityHours() ? "Hors horaires" : "Dans horaires");

        return new ResponseEntity<>(buildHtml(title, body + extra, "#3B0A0A", "#111827", "#FCA5A5"), HttpStatus.OK);
    }

    @GetMapping("setAutoSecurity/")
    public ResponseEntity<String> setAutoSecurity(@RequestParam boolean enabled) {
        autoSecurity = enabled;

        String title = "Scenario 2 - Mise a jour";
        String body  = enabled
                ? "Surveillance activee."
                : "Surveillance desactivee.";

        databaseLogger.logEvent("AutoSecurity = " + enabled);
        logger.info("AutoSecurity={}", enabled);

        return new ResponseEntity<>(buildHtml(title, body, "#3B0A0A", "#111827", "#FCA5A5"), HttpStatus.OK);
    }

    @GetMapping("setSecurityHours/")
    public ResponseEntity<String> setSecurityHours(@RequestParam String start, @RequestParam String end) {
        securityStart = LocalTime.parse(start);
        securityEnd   = LocalTime.parse(end);

        String title = "Scenario 2 - Horaires mis a jour";
        String body  = "Nouveaux horaires autorises: " + securityStart + " -> " + securityEnd +
                "<br/>Alarme declenchee uniquement hors de cette plage.";

        databaseLogger.logEvent("SecurityHours = " + securityStart + " -> " + securityEnd);
        return new ResponseEntity<>(buildHtml(title, body, "#3B0A0A", "#111827", "#FCA5A5"), HttpStatus.OK);
    }

    private String runAutoSecurity() {
        boolean movement = rt().getForObject(MovDetectUrl + "getDetection/", boolean.class);

        if (!isOutsideSecurityHours()) {
            if (movement) {
                databaseLogger.logEvent("Scenario2: mouvement detecte dans horaires -> alarme non declenchee.");
                return "Scenario2: Mouvement dans horaires, alarme OFF<br/>";
            } else {
                databaseLogger.logEvent("Scenario2: aucun mouvement dans horaires.");
                return "Scenario2: Aucun mouvement (dans horaires)<br/>";
            }
        }

        // Hors horaires
        if (movement) {
            rt().getForObject(AlarmControlUrl + "setStatus/?oN=true", String.class);
            databaseLogger.logEvent("Scenario2: INTRUSION -> alarme ON.");
            return "Scenario2: Intrusion detectee, alarme ON<br/>";
        } else {
            databaseLogger.logEvent("Scenario2: hors horaires, aucun mouvement -> alarme OFF.");
            return "Scenario2: Hors horaires, aucun mouvement<br/>";
        }
    }

    // =========================================================
    // Informations utiles
    // =========================================================

    @GetMapping("getParkingSpotStatus/")
    public ResponseEntity<String> getParkingSpotStatus() {
        boolean occupied = rt().getForObject(MovDetectUrl + "getDetection/", boolean.class);

        String title = occupied ? "Etat place: occupee" : "Etat place: libre";
        String body  = occupied
                ? "Presence detectee sur la place (capteur mouvement)."
                : "Aucune presence detectee sur la place.";

        databaseLogger.logEvent("SpotStatus = " + (occupied ? "OCCUPIED" : "FREE"));
        return new ResponseEntity<>(buildHtml(title, body, "#111827", "#0B1220", "#E5E7EB"), HttpStatus.OK);
    }

    // =========================================================
    // RUN GLOBAL : exécute toutes les automatisations activées
    // =========================================================

    @GetMapping("runAuto/")
    public ResponseEntity<String> runAuto() {
        StringBuilder actions = new StringBuilder();

        if (autoAccessSim) {
            try {
                actions.append(runAutoAccessSim());
            } catch (Exception e) {
                actions.append("Scenario1: ERROR (").append(e.getMessage()).append(")<br/>");
                logger.error("Erreur Scenario1 (AccessSim)", e);
            }
        }

        if (autoSecurity) {
            try {
                actions.append(runAutoSecurity());
            } catch (Exception e) {
                actions.append("Scenario2: ERROR (").append(e.getMessage()).append(")<br/>");
                logger.error("Erreur Scenario2 (Security)", e);
            }
        }

        if (autoEnergyLight) {
            try {
                actions.append(runAutoEnergyLight());
            } catch (Exception e) {
                actions.append("Scenario3: ERROR (").append(e.getMessage()).append(")<br/>");
                logger.error("Erreur Scenario3 (EnergyLight)", e);
            }
        }

        String title = "Execution automatisations";
        String body  = actions.length() == 0
                ? "Aucune automatisation active. Active au moins un mode."
                : actions.toString();

        return new ResponseEntity<>(buildHtml(title, body, "#111827", "#0B1220", "#E5E7EB"), HttpStatus.OK);
    }

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }
}
