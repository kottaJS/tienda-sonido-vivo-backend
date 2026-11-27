package cl.sonidovivo.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SonidoVivoBackendTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtenerToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void catalogoPublicoYSwaggerDisponiblesSinToken() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(28))));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void loginYMeConTokenValidoEInvalido() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer token-invalido-123"))
                .andExpect(status().isUnauthorized());

        String adminToken = obtenerToken("maximiliano.dueno@sonidovivo.cl", "AdminDemo2026!");
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("maximiliano.dueno@sonidovivo.cl")))
                .andExpect(jsonPath("$.rol", is("Administrador")));
    }

    @Test
    void controlAccesoPorRolesEnProductosYUsuarios() throws Exception {
        String clienteToken = obtenerToken("cliente@gmail.com", "ClienteDemo2026!");
        String vendedorToken = obtenerToken("patricio.ventas@sonidovivo.cl", "VendedorDemo2026!");
        String adminToken = obtenerToken("maximiliano.dueno@sonidovivo.cl", "AdminDemo2026!");

        String nuevoProducto = objectMapper.writeValueAsString(Map.of(
                "id", "TEST01",
                "categoria", "Accesorios",
                "nombre", "Afinador Clip Cromático",
                "marca", "Fender",
                "modelo", "FT-1",
                "stock", 12,
                "precio", 14990,
                "descripcion", "Afinador de prueba para integración",
                "imagen", "/images/products/AC001.webp"
        ));

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoProducto))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + vendedorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoProducto))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoProducto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("TEST01")));

        mockMvc.perform(patch("/api/productos/TEST01/stock")
                        .header("Authorization", "Bearer " + vendedorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stock", 20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(20)));

        String productoEditado = objectMapper.writeValueAsString(Map.of(
                "id", "TEST01",
                "categoria", "Accesorios",
                "nombre", "Afinador Clip Pro",
                "marca", "Fender",
                "modelo", "FT-2",
                "stock", 15,
                "precio", 17990,
                "descripcion", "Afinador actualizado",
                "imagen", "/images/products/AC001.webp"
        ));

        mockMvc.perform(put("/api/productos/TEST01")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoEditado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Afinador Clip Pro")));

        mockMvc.perform(delete("/api/productos/TEST01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void flujoCheckoutYCancelacionRestauraStock() throws Exception {
        String clienteToken = obtenerToken("cliente@gmail.com", "ClienteDemo2026!");
        String vendedorToken = obtenerToken("patricio.ventas@sonidovivo.cl", "VendedorDemo2026!");

        MvcResult beforeRes = mockMvc.perform(get("/api/productos/GA003"))
                .andExpect(status().isOk())
                .andReturn();
        int stockInicial = objectMapper.readTree(beforeRes.getResponse().getContentAsString()).get("stock").asInt();

        String pedidoReq = objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("nombre", "Cliente"),
                Map.entry("apellido", "Particular"),
                Map.entry("rut", "11.111.111-1"),
                Map.entry("email", "cliente@gmail.com"),
                Map.entry("telefono", "+56 9 8765 4321"),
                Map.entry("tipoEntrega", "domicilio"),
                Map.entry("direccion", "Av. Valparaíso 580"),
                Map.entry("ciudad", "Viña del Mar"),
                Map.entry("comuna", "Viña del Mar"),
                Map.entry("metodoPago", "Webpay / Débito"),
                Map.entry("notas", "Entregar en conserjería"),
                Map.entry("items", List.of(Map.of("productoId", "GA003", "cantidad", 2)))
        ));

        MvcResult crearRes = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costoEnvio", is(4990)))
                .andExpect(jsonPath("$.estado", is("En preparación")))
                .andReturn();

        String pedidoId = objectMapper.readTree(crearRes.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/productos/GA003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(stockInicial - 2)));

        mockMvc.perform(get("/api/pedidos/mis-pedidos")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(pedidoId)));

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/estado")
                        .header("Authorization", "Bearer " + vendedorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "Cancelado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("Cancelado")));

        mockMvc.perform(get("/api/productos/GA003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(stockInicial)));
    }

    @Test
    void registroNuevoClienteConRutValido() throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of(
                "nombre", "Camila Rojas",
                "email", "camila.rojas@test.cl",
                "password", "ClaveSegura2026!",
                "rut", "12.345.678-5",
                "ciudad", "Quilpué"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol", is("Cliente")));
    }
}
