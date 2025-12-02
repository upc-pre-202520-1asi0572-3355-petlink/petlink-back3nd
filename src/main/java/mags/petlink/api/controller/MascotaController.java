package mags.petlink.api.controller;

import mags.petlink.api.dto.request.CambiarInternadoRequest;
import mags.petlink.api.dto.request.MascotaCreateRequest;
import mags.petlink.api.dto.response.HistorialLatidosResponse;
import mags.petlink.api.dto.response.MascotaMonitorResponse;
import mags.petlink.api.dto.response.MascotaResponse;
import mags.petlink.application.service.HistorialLatidosService;
import mags.petlink.application.service.MascotaService;
import mags.petlink.domain.model.HistorialLatidos;
import mags.petlink.domain.model.Mascota;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final HistorialLatidosService historialLatidosService;

    public MascotaController(MascotaService mascotaService,
                             HistorialLatidosService historialLatidosService) {
        this.mascotaService = mascotaService;
        this.historialLatidosService = historialLatidosService;
    }


    @GetMapping
    public ResponseEntity<List<MascotaResponse>> listarMascotas() {
        List<MascotaResponse> mascotas = mascotaService.listarTodas().stream()
                .map(this::toMascotaResponse)
                .toList();
        return ResponseEntity.ok(mascotas);
    }

    @PostMapping
    public ResponseEntity<MascotaResponse> crearMascota(@RequestBody MascotaCreateRequest request) {
        String textoHora = request.horaIngresa();
        LocalTime hora;

        if (textoHora != null && textoHora.contains("T")) {
            // Por si algún día mandas "2025-12-02T12:00:00"
            hora = java.time.LocalDateTime.parse(textoHora).toLocalTime();
        } else {
            // "12:00" o "12:00:00"
            hora = java.time.LocalTime.parse(textoHora);
        }

        Mascota mascota = mascotaService.crearMascota(
                request.nombre(),
                request.especie(),
                request.edad(),
                request.estadoSalud(),
                request.raza(),
                hora
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(mascota));
    }

    @GetMapping("/{id}/monitor")
    public ResponseEntity<MascotaMonitorResponse> monitorMascota(@PathVariable Long id) {
        Mascota mascota = mascotaService.obtenerMascota(id);
        List<Integer> lastSix = mascotaService.obtenerUltimosSeisBpm(id);

        Integer currentHeartRate;
        if( lastSix.isEmpty()){
            currentHeartRate = null;
        } else {
            currentHeartRate = lastSix.get(lastSix.size() - 1);
        }
        

        MascotaMonitorResponse response = new MascotaMonitorResponse(
                mascota.getNombre(),
                mascota.getEdad(),
                mascota.getRaza(),
                mascota.getHoraIngresa() != null ? mascota.getHoraIngresa().toString() : null,
                currentHeartRate,
                mascota.getEstadoSalud() != null ? mascota.getEstadoSalud().name() : null,
                lastSix
        );

        return ResponseEntity.ok(response);
    }

    private MascotaResponse toResponse(Mascota mascota) {
        Long collarId = mascota.getCollar() != null ? mascota.getCollar().getId() : null;

        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getEdad(),
                mascota.getEstadoSalud() != null ? mascota.getEstadoSalud().name() : null,
                mascota.getRaza(),
                mascota.isInternado(),
                mascota.getHoraIngresa() != null ? mascota.getHoraIngresa().toString() : null,
                collarId
        );
    }

    private LocalTime parseHora(String textoHora) {
        if (textoHora == null || textoHora.isBlank()) {
            return null;
        }
        if (textoHora.contains("T")) {
            // ej: 2025-12-02T12:00:00
            return LocalDateTime.parse(textoHora).toLocalTime();
        }
        // ej: 12:00 o 12:00:00
        return LocalTime.parse(textoHora);
    }

    @PutMapping("/{id}/internado")
    public ResponseEntity<MascotaResponse> cambiarInternado(
            @PathVariable Long id,
            @RequestBody CambiarInternadoRequest request) {

        Mascota mascota = mascotaService.cambiarEstadoInternado(id, request.internado());
        return ResponseEntity.ok(toMascotaResponse(mascota));
    }

    @PutMapping("/{idMascota}/collar/{idCollar}")
    public ResponseEntity<MascotaResponse> vincularCollar(
            @PathVariable Long idMascota,
            @PathVariable Long idCollar) {

        Mascota mascota = mascotaService.vincularCollar(idMascota, idCollar);
        return ResponseEntity.ok(toMascotaResponse(mascota));
    }

    @PutMapping("/{idMascota}/collar/desvincular")
    public ResponseEntity<MascotaResponse> desvincularCollar(@PathVariable Long idMascota) {
        Mascota mascota = mascotaService.desvincularCollar(idMascota);
        return ResponseEntity.ok(toMascotaResponse(mascota));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialLatidosResponse>> obtenerHistorial(@PathVariable Long id) {
        List<HistorialLatidos> registros = historialLatidosService.obtenerPorMascota(id);
        List<HistorialLatidosResponse> response = registros.stream()
                .map(this::toHistorialResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private MascotaResponse toMascotaResponse(Mascota mascota) {
        Long collarId = mascota.getCollar() != null ? mascota.getCollar().getId() : null;
        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getEdad(),
                mascota.getEstadoSalud() != null ? mascota.getEstadoSalud().name() : null,
                mascota.getRaza(),
                mascota.isInternado(),
                mascota.getHoraIngresa() != null ? mascota.getHoraIngresa().toString() : null,
                collarId
        );
    }

    private HistorialLatidosResponse toHistorialResponse(HistorialLatidos h) {

        return new HistorialLatidosResponse(
                h.getTiempo().toString(),
                h.getBpm()
        );
    }


}