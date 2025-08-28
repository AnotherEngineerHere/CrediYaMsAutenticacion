package co.com.crediya.autenticacion.r2dbc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usuario", schema = "auth") // <-- esquema explícito
public class UsuarioEntity {

    @Id
    @Column("id_usuario")
    private Long id;

    @Column("nombre")
    private String nombre;

    @Column("apellido")
    private String apellido;

    @Column("email")
    private String email;

    @Column("contrasena")
    private String contrasena;

    @Column("documento_identidad")
    private String documentoIdentidad;

    @Column("telefono")
    private String telefono;

    @Column("id_rol")
    private Long rolId;

    @Column("salario_base")
    private int salarioBase;

    @Column("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column("direccion")
    private String direccion;
}
