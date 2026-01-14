package fr.insa.mas.LightControl;

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

@RestController
@SpringBootApplication
@RequestMapping("/LightControl/")
public class LightControlApplication {

    private boolean on = false; // par défaut OFF (économie)
    private static final Logger logger = LoggerFactory.getLogger(LightControlApplication.class);
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        SpringApplication.run(LightControlApplication.class, args);
    }

    private String buildHtml(String title, String body, String titleColor) {
        // Thème énergie : vert foncé + carte dark
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #0B3D2E; text-align: center; margin:0; padding:0; }" +
                ".container { padding: 18px; background-color: #0F172A; border-radius: 16px; margin: 60px auto; max-width: 520px; box-shadow: 0 10px 30px rgba(0,0,0,0.25); }" +
                "h1 { color: " + titleColor + "; margin: 0; padding: 12px; border-radius: 12px; }" +
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

    // ✅ État de la lumière (HTML)
    @GetMapping("isON/")
    public ResponseEntity<String> isON() {
        String title = on ? "Lumière ALLUMÉE 💡✨" : "Lumière ÉTEINTE 🌙📴";
        String body  = on
                ? "Éclairage actif : la salle est considérée comme occupée."
                : "Éclairage coupé : aucune présence détectée ou mode économie actif.";

        String titleColor = on ? "#A7F3D0" : "#93C5FD";

        logger.info("Light state: {}", on ? "ON" : "OFF");
        return new ResponseEntity<>(buildHtml(title, body, titleColor), HttpStatus.OK);
    }

    // ✅ Commande ON/OFF (compatible avec ton Controller: setON/?oN=true)
    @GetMapping("setON/")
    public ResponseEntity<String> setON(@RequestParam boolean oN) {
        on = oN;

        String title = on ? "Commande reçue : ON 💡✅" : "Commande reçue : OFF 🌙✅";
        String body  = on
                ? "La lumière a été allumée (présence détectée ou activation manuelle)."
                : "La lumière a été éteinte (absence de présence ou activation mode économie).";

        String titleColor = on ? "#A7F3D0" : "#93C5FD";

        logger.info("Light setON -> {}", on);
        return new ResponseEntity<>(buildHtml(title, body, titleColor), HttpStatus.OK);
    }

    // ✅ Optionnel : endpoint raw (utile côté automatisation / tests)
    @GetMapping("state/raw")
    public ResponseEntity<Boolean> stateRaw() {
        return new ResponseEntity<>(on, HttpStatus.OK);
    }
}
