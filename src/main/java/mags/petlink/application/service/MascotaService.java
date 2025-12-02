package mags.petlink.application.service;

import mags.petlink.api.dto.response.MascotaResponse;
import mags.petlink.domain.enums.EstadoCollar;
import mags.petlink.domain.enums.EstadoSalud;
import mags.petlink.domain.model.Collar;
import mags.petlink.domain.model.HistorialLatidos;
import mags.petlink.domain.model.Mascota;
import mags.petlink.infrastructure.repository.CollarRepository;
import mags.petlink.infrastructure.repository.HistorialLatidosRepository;
import mags.petlink.infrastructure.repository.MascotaRepository;
import mags.petlink.shared.exception.NotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final CollarRepository collarRepository;
    private final HistorialLatidosRepository historialLatidosRepository;

    public MascotaService(MascotaRepository mascotaRepository,
            CollarRepository collarRepository,
            HistorialLatidosRepository historialLatidosRepository) {
        this.mascotaRepository = mascotaRepository;
        this.collarRepository = collarRepository;
        this.historialLatidosRepository = historialLatidosRepository;
    }

    public Mascota crearMascota(String nombre, String especie, Integer edad, EstadoSalud estadoSalud, String raza,
            LocalTime horaIngresa) {

        Mascota mascota = Mascota.builder()
                .nombre(nombre)
                .especie(especie)
                .edad(edad)
                .estadoSalud(estadoSalud != null ? estadoSalud : EstadoSalud.ESTABLE)
                .raza(raza)
                .horaIngresa(horaIngresa)
                .build();

        return mascotaRepository.save(mascota);
    }

    public Mascota obtenerMascota(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con id " + id));
    }

    public Mascota obtenerPorId(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada con id " + id));
    }

    public List<Integer> obtenerUltimosSeisBpm(Long mascotaId) {
        Mascota mascota = obtenerMascota(mascotaId);

        List<HistorialLatidos> registrosDesc = historialLatidosRepository.findTop6ByMascotaOrderByTiempoDesc(mascota);

        return registrosDesc.stream()
                .sorted(Comparator.comparing(HistorialLatidos::getTiempo))
                .map(HistorialLatidos::getBpm)
                .toList();
    }

    public Mascota cambiarEstadoInternado(Long mascotaId, boolean internado) {
        Mascota mascota = obtenerPorId(mascotaId);
        mascota.setInternado(internado);
        return mascotaRepository.save(mascota);
    }

    public Mascota vincularCollar(Long mascotaId, Long collarId) {
        Mascota mascota = obtenerPorId(mascotaId);

        Collar collar = collarRepository.findById(collarId)
                .orElseThrow(() -> new NotFoundException("Collar no encontrado con id " + collarId));

        // Asignar bidireccionalmente
        mascota.setCollar(collar);
        collar.setMascota(mascota);
        collar.setEstado(EstadoCollar.OCUPADO);

        collarRepository.save(collar);
        return mascotaRepository.save(mascota);
    }

    public Mascota desvincularCollar(Long mascotaId) {
        Mascota mascota = obtenerPorId(mascotaId);

        Collar collar = mascota.getCollar();
        if (collar != null) {
            mascota.setCollar(null);
            collar.setMascota(null);
            collar.setEstado(EstadoCollar.DISPONIBLE);
            collarRepository.save(collar);
        }

        return mascotaRepository.save(mascota);
    }

    public List<Mascota> listarTodas() {
        return mascotaRepository.findAll();
    }

    

}
