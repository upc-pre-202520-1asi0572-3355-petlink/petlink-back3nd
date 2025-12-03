package mags.petlink.api.controller;

import mags.petlink.api.dto.request.LoginRequest;
import mags.petlink.api.dto.response.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String HARDCODED_EMAIL = "veterinaria.pekas@gmail.com";
    private static final String HARDCODED_PASSWORD = "ickkck67BR&";

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String email = request.email();
        String password = request.password();

        boolean emailCorrecto = HARDCODED_EMAIL.equals(email);
        boolean passwordCorrecta = HARDCODED_PASSWORD.equals(password);

        if (emailCorrecto && passwordCorrecta) {
            return ResponseEntity.ok(
                    new LoginResponse(true, "Login exitoso")
            );
        }

        if (!emailCorrecto && !passwordCorrecta) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Correo y contraseña incorrectos"));
        }

        if (!emailCorrecto) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Correo incorrecto"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, "Contraseña incorrecta"));
    }
}