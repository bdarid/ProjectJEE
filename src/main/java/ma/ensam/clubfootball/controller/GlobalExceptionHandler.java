package ma.ensam.clubfootball.controller;

import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.exception.RessourceIntrouvableException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public String handleNotFound(RessourceIntrouvableException ex, Model model) {
        model.addAttribute("titreErreur", "Ressource introuvable");
        model.addAttribute("messageErreur", ex.getMessage());
        return "error/erreur";
    }

    @ExceptionHandler(RegleGestionException.class)
    public String handleBusinessRule(RegleGestionException ex, Model model) {
        model.addAttribute("titreErreur", "Action non autorisée");
        model.addAttribute("messageErreur", ex.getMessage());
        return "error/erreur";
    }
}