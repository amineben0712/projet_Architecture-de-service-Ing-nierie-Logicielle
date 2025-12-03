package fr.insa.mas.AlarmControl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@RestController
@RequestMapping("/AlarmControl/")
public class AlarmControlApplication {

    private boolean alarmTriggered = false;
    private Logger logger = LoggerFactory.getLogger(AlarmControlApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AlarmControlApplication.class, args);
    }

    @GetMapping("/status")
    public ResponseEntity<String> getAlarmStatus() {
        String statusMessage = "État de l'alarme : " + (alarmTriggered ? "Déclenchée 🚨" : "Non déclenchée ✅");

        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                "p { font-size: 18px; color: #ffffff; }" +
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

    @GetMapping("/setStatus/")
    public ResponseEntity<String> setAlarmStatus(boolean oN) {
        alarmTriggered = oN;
        String actionMessage = "L'alarme a été " + (oN ? "déclenchée 🚨" : "désactivée ✅");

        logger.info(actionMessage);

        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #27ae60; text-align: center; }" +
                ".container { padding: 20px; background-color: #000000; border-radius: 10px; margin: 50px auto; max-width: 400px; }" +
                "h1 { color: #ffffff; background-color: #000000; border-radius: 5px; padding: 10px; }" +
                "p { font-size: 18px; color: #ffffff; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>" + actionMessage + "</h1>" +
                "</div>" +
                "</body>" +
                "</html>";

        return new ResponseEntity<>(htmlResponse, HttpStatus.OK);
    }
}
