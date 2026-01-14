package fr.insa.mas.AlarmControl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/AlarmControl/")
public class AlarmControlApplication {

    private boolean alarmTriggered = false;
    private static final Logger logger = LoggerFactory.getLogger(AlarmControlApplication.class);
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        SpringApplication.run(AlarmControlApplication.class, args);
    }

    private String buildHtml(String title, String body) {
        // Thème sécurité : rouge sombre + carte dark
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #3B0A0A; text-align: center; margin:0; padding:0; }" +
                ".container { padding: 18px; background-color: #111827; border-radius: 16px; margin: 60px auto; max-width: 520px; box-shadow: 0 10px 30px rgba(0,0,0,0.25); }" +
                "h1 { color: #FCA5A5; margin: 0; padding: 12px; border-radius: 12px; }" +
                "p { font-size: 16px; color: #EAEAEA; line-height: 1.5; }" +
                ".meta { font-size: 13px; color: #BDBDBD; margin-top: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>" + title + "</h1>" +
                "<p>" + body + "</p>" +
                "<div class='meta'>⏱️ " + LocalDateTime.now().format(DT) + "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // ✅ Status : état actuel
    @GetMapping("status")
    public ResponseEntity<String> getAlarmStatus() {
        String title = "État Alarme " + (alarmTriggered ? "🚨🔒" : "✅🔓");
        String body  = alarmTriggered
                ? "Alarme déclenchée : intrusion détectée ou mode sécurité actif."
                : "Alarme inactive : aucun déclenchement en cours.";

        logger.info("Alarm status: {}", alarmTriggered ? "TRIGGERED" : "OFF");
        return new ResponseEntity<>(buildHtml(title, body), HttpStatus.OK);
    }

    // ✅ Endpoint principal utilisé par le Controller
    // Exemple : /AlarmControl/setStatus/?oN=true
    @GetMapping("setStatus/")
    public ResponseEntity<String> setAlarmStatus(@RequestParam boolean oN) {
        alarmTriggered = oN;

        String title = oN ? "ALARME DÉCLENCHÉE 🚨🚨" : "Alarme désactivée ✅🟢";
        String body  = oN
                ? "Action : activation de l’alarme (mode intrusion)."
                : "Action : désactivation / arrêt de l’alarme.";

        logger.warn("Alarm setStatus -> {}", oN);
        return new ResponseEntity<>(buildHtml(title, body), HttpStatus.OK);
    }

    // ✅ Alias plus clair (optionnel)
    // Exemple : /AlarmControl/trigger/
    @GetMapping("trigger/")
    public ResponseEntity<String> trigger() {
        alarmTriggered = true;

        String title = "INTRUSION 🚨🔒";
        String body  = "Alarme déclenchée immédiatement.";

        logger.warn("Alarm TRIGGERED via /trigger/");
        return new ResponseEntity<>(buildHtml(title, body), HttpStatus.OK);
    }

    // ✅ Reset rapide (optionnel)
    // Exemple : /AlarmControl/reset/
    @GetMapping("reset/")
    public ResponseEntity<String> reset() {
        alarmTriggered = false;

        String title = "RESET ✅🔓";
        String body  = "L’alarme a été remise à l’état inactif.";

        logger.info("Alarm RESET via /reset/");
        return new ResponseEntity<>(buildHtml(title, body), HttpStatus.OK);
    }

    // ✅ Pour usage “machine” (retour JSON/boolean simple)
    // Exemple : /AlarmControl/status/raw
    @GetMapping("status/raw")
    public ResponseEntity<Boolean> getAlarmStatusRaw() {
        return new ResponseEntity<>(alarmTriggered, HttpStatus.OK);
    }
}
