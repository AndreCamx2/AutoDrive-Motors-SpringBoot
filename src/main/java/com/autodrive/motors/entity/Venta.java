package com.autodrive.motors.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ventas",
        indexes = {
                @Index(name = "idx_ventas_fecha", columnList = "fecha_venta"),
                @Index(name = "idx_ventas_cliente", columnList = "cliente_id"),
                @Index(name = "idx_ventas_vehiculo", columnList = "vehiculo_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cliente", "vehiculo"})
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, 
            foreignKey = @ForeignKey(name = "fk_ventas_cliente"))
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_ventas_vehiculo"))
    private Vehiculo vehiculo;

    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "precio_base", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "monto_descuento", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    @Column(name = "total_pagado", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPagado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    void prePersist() {
        if (fechaVenta == null) {
            fechaVenta = LocalDateTime.now();
        }
        if (porcentajeDescuento == null) {
            porcentajeDescuento = BigDecimal.ZERO;
        }
        if (montoDescuento == null) {
            montoDescuento = BigDecimal.ZERO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venta other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
