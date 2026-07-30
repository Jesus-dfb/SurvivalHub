package com.survivalhub;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SurvivalHubApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsApiMessage() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Survival Hub API funcionando"));
    }

    @Test
    void userCanRegisterAndLogin() throws Exception {
        String username = unique("login_user");
        String password = "password123";

        register(username, password, "Login User");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.user.username").value(username));
    }

    @Test
    void userOnlySeesOwnWorlds() throws Exception {
        String firstToken = registerAndGetToken(unique("owner_user"), "Owner User");
        String secondToken = registerAndGetToken(unique("other_user"), "Other User");

        Long worldId = createWorld(firstToken, "Mundo privado", "Minecraft");

        mockMvc.perform(get("/api/worlds")
                        .header("Authorization", bearer(secondToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/worlds/" + worldId)
                        .header("Authorization", bearer(secondToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void importingSameGuideTwiceReturnsConflict() throws Exception {
        String token = registerAndGetToken(unique("guide_user"), "Guide User");
        Long worldId = createWorld(token, "Mundo de guias", "Minecraft");
        Long guideId = createGuide(token);

        mockMvc.perform(post("/api/guides/%d/apply/worlds/%d".formatted(guideId, worldId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.title").value("Granja segura"));

        mockMvc.perform(post("/api/guides/%d/apply/worlds/%d".formatted(guideId, worldId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
    }

    @Test
    void userCanImportResourcesFromMaterialListFile() throws Exception {
        String token = registerAndGetToken(unique("import_user"), "Import User");
        Long worldId = createWorld(token, "Mundo con materiales", "Minecraft");
        Long taskId = createTask(token, worldId, "Construir base");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "material_list.txt",
                "text/plain",
                """
                        +--------------------------------------------+-------+---------+-----------+
                        | Item                                       | Total | Missing | Available |
                        +--------------------------------------------+-------+---------+-----------+
                        | Piedra                                     |   100 |      80 |        20 |
                        | BotÃ³n de piedra                            |    48 |      48 |         0 |
                        +--------------------------------------------+-------+---------+-----------+
                        """.getBytes()
        );

        MvcResult importResult = mockMvc.perform(multipart("/api/worlds/%d/tasks/%d/resources/import".formatted(worldId, taskId))
                        .file(file)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedCount").value(2))
                .andExpect(jsonPath("$.createdCount").value(2))
                .andExpect(jsonPath("$.resources[0].name").value("Piedra"))
                .andExpect(jsonPath("$.resources[0].requiredQuantity").value(100))
                .andExpect(jsonPath("$.resources[0].collectedQuantity").value(20))
                .andExpect(jsonPath("$.resources[0].stackSize").value(64))
                .andExpect(jsonPath("$.resources[0].requiredStacks").value(1))
                .andExpect(jsonPath("$.resources[0].requiredLooseItems").value(36))
                .andExpect(jsonPath("$.resources[0].requiredStackSummary").value("1 stack y 36 bloques"))
                .andExpect(jsonPath("$.resources[1].name").value("Botón de piedra"))
                .andReturn();

        List<Long> importedResourceIds = readLongFields(importResult, "id");
        Long firstResourceId = importedResourceIds.get(0);
        Long secondResourceId = importedResourceIds.get(1);

        mockMvc.perform(put("/api/worlds/%d/tasks/%d/resources/order".formatted(worldId, taskId))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[%d,%d]".formatted(secondResourceId, firstResourceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Botón de piedra"))
                .andExpect(jsonPath("$[1].name").value("Piedra"));

        mockMvc.perform(get("/api/worlds/%d/tasks/%d/resources".formatted(worldId, taskId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Botón de piedra"))
                .andExpect(jsonPath("$[1].name").value("Piedra"));
    }

    private void register(String username, String password, String displayName) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s@survivalhub.test",
                                  "password": "%s",
                                  "displayName": "%s"
                                }
                                """.formatted(username, username, password, displayName)))
                .andExpect(status().isCreated());
    }

    private String registerAndGetToken(String username, String displayName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s@survivalhub.test",
                                  "password": "password123",
                                  "displayName": "%s"
                                }
                                """.formatted(username, username, displayName)))
                .andExpect(status().isCreated())
                .andReturn();

        return readTextField(result, "token");
    }

    private Long createWorld(String token, String name, String game) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/worlds")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "game": "%s",
                                  "description": "Mundo creado desde test"
                                }
                                """.formatted(name, game)))
                .andExpect(status().isCreated())
                .andReturn();

        return readLongField(result, "id");
    }

    private Long createTask(String token, Long worldId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/worlds/%d/tasks".formatted(worldId))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Tarea creada desde test",
                                  "priority": "Media",
                                  "completed": false
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn();

        return readLongField(result, "id");
    }

    private Long createGuide(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/guides")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Granja segura",
                                  "game": "Minecraft",
                                  "type": "Farm",
                                  "difficulty": "Media",
                                  "ratingAverage": 0,
                                  "ratingCount": 0,
                                  "importCount": 0,
                                  "description": "Guia creada para probar importaciones.",
                                  "youtubeUrl": "",
                                  "steps": [
                                    {
                                      "stepNumber": 1,
                                      "title": "Preparar zona",
                                      "description": "Elige una zona segura."
                                    }
                                  ],
                                  "resources": [
                                    {
                                      "name": "Piedra",
                                      "requiredQuantity": 64
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return readLongField(result, "id");
    }

    private String readTextField(MvcResult result, String fieldName) throws Exception {
        String json = result.getResponse().getContentAsString();
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new AssertionError("No se encontro el campo " + fieldName + " en la respuesta: " + json);
        }

        return matcher.group(1);
    }

    private Long readLongField(MvcResult result, String fieldName) throws Exception {
        String json = result.getResponse().getContentAsString();
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new AssertionError("No se encontro el campo " + fieldName + " en la respuesta: " + json);
        }

        return Long.valueOf(matcher.group(1));
    }

    private List<Long> readLongFields(MvcResult result, String fieldName) throws Exception {
        String json = result.getResponse().getContentAsString();
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        List<Long> values = new java.util.ArrayList<>();

        while (matcher.find()) {
            values.add(Long.valueOf(matcher.group(1)));
        }

        if (values.isEmpty()) {
            throw new AssertionError("No se encontro el campo " + fieldName + " en la respuesta: " + json);
        }

        return values;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
