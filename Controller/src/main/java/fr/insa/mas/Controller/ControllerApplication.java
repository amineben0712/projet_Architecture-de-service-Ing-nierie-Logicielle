package fr.insa.mas.Controller;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@SpringBootApplication
@RequestMapping("/Controller/")
public class ControllerApplication {

    private DatabaseLogger databaseLogger = new DatabaseLogger();
    private final String MovDetectUrl = "http://localhost:8082/MovementDetection/";
    private final String LightControlUrl = "http://localhost:8081/LightControl/";
    private final String AlarmControlUrl = "http://localhost:8083/AlarmControl/";
    private static final Logger logger = LoggerFactory.getLogger(ControllerApplication.class);

    boolean AutoLightControl = false;
    boolean AutoAlarmControl = false;

    @GetMapping("/logTest")
    public String logTest() {
        databaseLogger.logEvent("Test event from Controller project.");
        return "Event logged successfully!";
    }

    // Automatic Light control with movement detection

    @GetMapping("isAutoLightControlActivated/")
    public ResponseEntity<String> isAutoLightControl() {
        String emoji = (AutoLightControl ? "💡" : "🔦"); 
        String statusMessage = "Le contrôle automatique des lumières est " + (AutoLightControl ? "activé" : "désactivé") + " " + emoji;

        String htmlResponse = "<html>" +
                                "<head>" +
                                    "<style>" +
                                        "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                                        ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                                        "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                                        "p { font-size: 18px; color: #333333; }" +
                                    "</style>" +
                                "</head>" +
                                "<body>" +
                                    "<div class='container'>" +
                                        "<h1>" + statusMessage + "</h1>" +
                                    "</div>" +
                                "</body>" +
                             "</html>";

        logger.info(statusMessage);
        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }

    @GetMapping("setAutoLightControl/")
    public ResponseEntity<String> setAutoLightControl(boolean autoLightControl) {
        AutoLightControl = autoLightControl;

        String statusMessage;
        if (autoLightControl) {
            statusMessage = "Le contrôle automatique des lumières a été activé. 💡";
        } else {
            statusMessage = "Le contrôle automatique des lumières a été désactivé. 🌑";
        }

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                "p { font-size: 18px; color: #333333; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>" + statusMessage + "</h1>" +
                "<p>Défini à : " + currentTime + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        logger.info(statusMessage);
        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }

    public void AutoLightControl() {
        String url_getDetection = MovDetectUrl + "getDetection/";
        String url_setLight = LightControlUrl + "setON/?oN=true";
        String url_unsetLight = LightControlUrl + "setON/?oN=false";
        RestTemplate restTemplate = new RestTemplate();

        boolean detection = restTemplate.getForObject(url_getDetection, boolean.class);

        if (detection) {
            restTemplate.getForObject(url_setLight, boolean.class);
            databaseLogger.logEvent("Le contrôle automatique des lumières a été activé suite à la détection de mouvement.");
        } else {
            restTemplate.getForObject(url_unsetLight, boolean.class);
            databaseLogger.logEvent("Le contrôle automatique des lumières a été désactivé suite à l'absence de détection de mouvement.");
        }
    }

    // Automatic alarm control with movement detection

    @GetMapping("isAutoAlarmControlActivated/")
    public ResponseEntity<String> isAutoAlarmControl() {
        String statusMessage = "Le contrôle automatique des alarmes est " + (AutoAlarmControl ? "activé" : "désactivé");
        String emoji = (AutoAlarmControl ? "🔔" : "🔕");

        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                "p { font-size: 18px; color: #333333; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>" + statusMessage + " " + emoji + "</h1>" +
                "</div>" +
                "</body>" +
                "</html>";

        logger.info(statusMessage);
        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }

    @GetMapping("setAutoAlarmControl/")
    public ResponseEntity<String> setAutoAlarmControl(boolean autoAlarmControl) {
        AutoAlarmControl = autoAlarmControl;

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        String action = (autoAlarmControl ? "activé" : "désactivé");
        String statusMessage = "Le contrôle automatique des alarmes a été " + action;
        String emoji = (AutoAlarmControl ? "🚨" : "🛑");

        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                "p { font-size: 18px; color: #333333; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>" + statusMessage + " " + emoji + "</h1>" +
                "<p>Défini à : " + formattedDateTime + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        logger.info(statusMessage);
        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }

    // Know if a room is used or not with movement detection

    @GetMapping("getRoomUsed/")
    public ResponseEntity<String> RoomUsed() {
        String url_getDetection = MovDetectUrl + "getDetection/";
        RestTemplate restTemplate = new RestTemplate();
        boolean room_used = restTemplate.getForObject(url_getDetection, boolean.class);
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String message;
        String statusColor;
        if (room_used) {
            message = String.format("[%s] La pièce est occupée", currentTime.format(formatter));
            statusColor = "#4CAF50"; // Green color for "Room is used"
        } else {
            message = String.format("[%s] La pièce est libre", currentTime.format(formatter));
            statusColor = "#f44336"; // Red color for "Room is not used"
        }

        String htmlResponse = "<html>" +
                                "<head>" +
                                    "<style>" +
                                        "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                                        ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                                        "h1 { color: " + statusColor + "; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                                        "p { font-size: 18px; color: #333333; }" +
                                    "</style>" +
                                "</head>" +
                                "<body>" +
                                    "<div class='container'>" +
                                        "<h1>" + message + "</h1>" +
                                    "</div>" +
                                "</body>" +
                             "</html>";

        logger.info(message);
        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }

    public void AutoAlarmControl() {
        String url_getDetection = MovDetectUrl + "getDetection/";
        String url_setAlarm = AlarmControlUrl + "/setStatus/?oN=true";    
        RestTemplate restTemplate = new RestTemplate();

        // Vérifie si un mouvement est détecté
        if (restTemplate.getForObject(url_getDetection, boolean.class)) {
            // Active l'alarme
            restTemplate.getForObject(url_setAlarm, boolean.class);
            databaseLogger.logEvent("L'alarme a été déclenchée suite à la détection d'un mouvement.");
        } else {
            databaseLogger.logEvent("Aucun mouvement détecté, l'alarme reste inactive.");
        }
    }

    public boolean getMovementDetection() {
        String url_getDetection = MovDetectUrl + "getDetection/";
        RestTemplate restTemplate = new RestTemplate();

        return (restTemplate.getForObject(url_getDetection, boolean.class));
    }

    @GetMapping("runAuto")
    public ResponseEntity<String> run() {
        StringBuilder responseMessage = new StringBuilder();

        if (AutoLightControl) {
            AutoLightControl();
            responseMessage.append("Le contrôle automatique des lumières a été activé. 🌟\n");
        }

        if (AutoAlarmControl) {
            AutoAlarmControl();
            responseMessage.append("Le contrôle automatique des alarmes a été activé. 🛡️\n");
        }

        // Number of times movement was detected
        if (getMovementDetection()) {
            RestTemplate restTemplate = new RestTemplate();
            int nbDetection = restTemplate.getForObject(MovDetectUrl + "getNbDetection/", int.class);
            restTemplate.getForObject(MovDetectUrl + "setNbDetection/?detection=" + (nbDetection + 1), void.class);
            responseMessage.append("Mouvement détecté ! Nombre total de détections : ").append(nbDetection + 1).append(" 🚨\n");
        }

        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                "p { font-size: 18px; color: #333333; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>Actions automatiques exécutées</h1>" +
                "<p>" + responseMessage.toString() + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        logger.info(responseMessage.toString());
        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }
}
