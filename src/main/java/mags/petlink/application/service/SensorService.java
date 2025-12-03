package mags.petlink.application.service;

import mags.petlink.api.dto.request.SensorDataRequest;
import mags.petlink.domain.enums.EstadoCollar;
import mags.petlink.domain.enums.EstadoSalud;
import mags.petlink.domain.model.Collar;
import mags.petlink.domain.model.HistorialLatidos;
import mags.petlink.domain.model.Mascota;
import mags.petlink.infrastructure.repository.CollarRepository;
import mags.petlink.infrastructure.repository.HistorialLatidosRepository;
import mags.petlink.infrastructure.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SensorService {

    private final CollarRepository collarRepository;
    private final HistorialLatidosRepository historialLatidosRepository;
    private final MascotaRepository mascotaRepository;

    public SensorService(CollarRepository collarRepository,
                         HistorialLatidosRepository historialLatidosRepository,
                         MascotaRepository mascotaRepository) {
        this.collarRepository = collarRepository;
        this.historialLatidosRepository = historialLatidosRepository;
        this.mascotaRepository = mascotaRepository;
    }

    @Transactional
    public void procesarLectura(SensorDataRequest request) {
        String code = request.device_id();

        Collar collar = collarRepository.findByCode(code)
                .orElseGet(() -> crearNuevoCollar(code));

        Mascota mascota = collar.getMascota();

        if (mascota == null || !mascota.isInternado()) {
            return;
        }

        int bpm = request.bpm();

        long count = historialLatidosRepository.countByMascota(mascota);
        if (count >= 6) {
            historialLatidosRepository.findFirstByMascotaOrderByTiempoAsc(mascota)
                    .ifPresent(historialLatidosRepository::delete);
        }

        HistorialLatidos registro = HistorialLatidos.builder()
                .mascota(mascota)
                .tiempo(Instant.parse(request.timestamp()))
                .bpm(bpm)
                .build();

        historialLatidosRepository.save(registro);

        EstadoSalud esStatus = calcularEstadoSalud(bpm);
        mascota.setEstadoSalud(esStatus);

        mascotaRepository.save(mascota);
    }

    private Collar crearNuevoCollar(String code) {
        Collar collar = Collar.builder()
                .code(code)
                .estado(EstadoCollar.DISPONIBLE)
                .build();
        return collarRepository.save(collar);
    }

    private EstadoSalud calcularEstadoSalud(int bpm) {
        if (bpm < 50 || bpm > 160) {
            return EstadoSalud.CRITICO;
        } else if (bpm >= 60 && bpm <= 120) {
            return EstadoSalud.ESTABLE;
        } else {
            return EstadoSalud.ALERTA;
        }
    }
}
