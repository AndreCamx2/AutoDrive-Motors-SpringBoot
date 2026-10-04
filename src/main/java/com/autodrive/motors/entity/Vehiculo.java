package com.autodrive.motors.entity;

import com.autodrive.motors.entity.enums.EstadoVehiculo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "vehiculos",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehiculos_placa", columnNames = "placa"),
        indexes = {
                @Index(name = "idx_vehiculos_marca", columnList = "marca"),
                @Index(name = "idx_vehiculos_estado", columnList = "estado")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"mantenimientos", "venta"})
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "vehiculo", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Mantenimiento> mantenimientos = new ArrayList<>();

    @OneToOne(mappedBy = "vehiculo", fetch = FetchType.LAZY)
    private Venta venta;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoVehiculo.DISPONIBLE;
        }
        normalizar();
    }

    @PreUpdate
    void preUpdate() {
        normalizar();
    }

    /** Placa siempre en mayúsculas y sin espacios/guiones para garantizar unicidad real. */
    private void normalizar() {
        if (placa != null) {
            placa = placa.replaceAll("[\\s-]", "").toUpperCase();
        }
    }

    // ---------- Comportamiento de dominio ----------

    public boolean estaDisponibleParaVenta() {
        return estado != null && estado.permiteVenta();
    }

    public void marcarComoVendido() {
        this.estado = EstadoVehiculo.VENDIDO;
    }

    public void enviarAMantenimiento() {
        this.estado = EstadoVehiculo.EN_MANTENIMIENTO;
    }

    public void marcarComoDisponible() {
        this.estado = EstadoVehiculo.DISPONIBLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehiculo other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

