package co.com.crediya.autenticacion.model.usuario;
import co.com.crediya.autenticacion.model.rol.Rol;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Usuario {
    
    private String nombre;

    private String apellido;
    
    private String email;
    
    private String contrasena;
    
    private String documento_identidad;

    private String telefono;
    
    private Rol rolId; // Foreign key to Rol entity

    private int salario_base;
}
