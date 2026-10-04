package com.autodrive.motors.entity;

import com.autodrive.motors.entity.enums.EstadoMantenimiento;
import com.autodrive.motors.entity.enums.TipoMantenimiento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mantenimientos",
        indexes = @Index(name = "idx_mantenimientos_vehiculo", columnList = "vehiculo_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "vehiculo")
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mantenimientos_vehiculo"))
    private Vehiculo vehiculo;

    @Column(name = "fecha_ingreso", nullable = false, updatable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMantenimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoMantenimiento estado = EstadoMantenimiento.EN_PROCESO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @PrePersist
    void prePersist() {
        if (fechaIngreso == null) {
            fechaIngreso = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoMantenimiento.EN_PROCESO;
        }
    }

    public void finalizar() {
        this.estado = EstadoMantenimiento.FINALIZADO;
        this.fechaSalida = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mantenimiento other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

