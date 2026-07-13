package com.survivalhub.config;

import com.survivalhub.model.Guide;
import com.survivalhub.model.GuideResource;
import com.survivalhub.model.GuideStep;
import com.survivalhub.model.Member;
import com.survivalhub.model.Task;
import com.survivalhub.model.TaskResource;
import com.survivalhub.model.World;
import com.survivalhub.model.AppUser;
import com.survivalhub.repository.AppUserRepository;
import com.survivalhub.repository.GuideRepository;
import com.survivalhub.repository.MemberRepository;
import com.survivalhub.repository.TaskRepository;
import com.survivalhub.repository.TaskResourceRepository;
import com.survivalhub.repository.WorldRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final WorldRepository worldRepository;
    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final TaskResourceRepository taskResourceRepository;
    private final GuideRepository guideRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            WorldRepository worldRepository,
            MemberRepository memberRepository,
            TaskRepository taskRepository,
            TaskResourceRepository taskResourceRepository,
            GuideRepository guideRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.worldRepository = worldRepository;
        this.memberRepository = memberRepository;
        this.taskRepository = taskRepository;
        this.taskResourceRepository = taskResourceRepository;
        this.guideRepository = guideRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (appUserRepository.count() == 0) {
            seedUsers();
        } else {
            ensureDemoUsersCanLogin();
        }

        if (worldRepository.count() == 0) {
            seedWorlds();
        }

        ensureWorldsHaveOwner();

        if (guideRepository.count() == 0) {
            seedGuides();
        }
    }

    private void seedUsers() {
        appUserRepository.save(new AppUser(null, "alexcraft", "alexcraft@survivalhub.local", passwordEncoder.encode("password123"), "AlexCraft", LocalDateTime.now()));
        appUserRepository.save(new AppUser(null, "lunasurvival", "luna@survivalhub.local", passwordEncoder.encode("password123"), "LunaSurvival", LocalDateTime.now()));
        appUserRepository.save(new AppUser(null, "survivalfan", "survivalfan@survivalhub.local", passwordEncoder.encode("password123"), "SurvivalFan", LocalDateTime.now()));
    }

    private void ensureDemoUsersCanLogin() {
        List<AppUser> users = appUserRepository.findAll();

        for (AppUser user : users) {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode("password123"));
                appUserRepository.save(user);
            }
        }
    }

    private void ensureWorldsHaveOwner() {
        AppUser defaultOwner = appUserRepository.findByUsernameIgnoreCase("alexcraft")
                .orElse(null);

        if (defaultOwner == null) {
            return;
        }

        List<World> worlds = worldRepository.findAll();

        for (World world : worlds) {
            if (world.getOwnerUserId() == null) {
                world.setOwnerUserId(defaultOwner.getId());
                worldRepository.save(world);
            }
        }
    }

    private void seedWorlds() {
        Long defaultOwnerId = appUserRepository.findByUsernameIgnoreCase("alexcraft")
                .map(AppUser::getId)
                .orElse(null);

        World minecraftWorld = worldRepository.save(new World(
                null,
                "Minecraft con amigos",
                "Minecraft",
                defaultOwnerId,
                "Mundo cooperativo para construir base y automatizaciones"
        ));

        World valheimWorld = worldRepository.save(new World(
                null,
                "Valheim Server",
                "Valheim",
                defaultOwnerId,
                "Servidor para explorar, construir y derrotar bosses"
        ));

        memberRepository.save(new Member(null, minecraftWorld.getId(), "Alex", "Builder"));
        memberRepository.save(new Member(null, minecraftWorld.getId(), "Sam", "Farmer"));
        memberRepository.save(new Member(null, minecraftWorld.getId(), "Nora", "Explorer"));
        memberRepository.save(new Member(null, valheimWorld.getId(), "Leo", "Viking"));

        Task ironFarmTask = taskRepository.save(new Task(
                null,
                minecraftWorld.getId(),
                "Construir granja de hierro",
                "Reunir materiales y montar una granja automatica de hierro",
                "Alta",
                "2026-07-10",
                false
        ));

        Task storageTask = taskRepository.save(new Task(
                null,
                minecraftWorld.getId(),
                "Crear zona de almacenamiento",
                "Organizar cofres por tipo de recurso en la base principal",
                "Media",
                "2026-07-14",
                false
        ));

        taskRepository.save(new Task(
                null,
                minecraftWorld.getId(),
                "Preparar portal del Nether",
                "Buscar obsidiana y proteger la entrada al Nether",
                "Baja",
                "2026-07-05",
                true
        ));

        taskRepository.save(new Task(
                null,
                valheimWorld.getId(),
                "Derrotar al primer boss",
                "Preparar comida, armas y armaduras para el combate",
                "Alta",
                "2026-07-18",
                false
        ));

        taskResourceRepository.save(new TaskResource(null, ironFarmTask.getId(), "Bloques de piedra", 64, 40));
        taskResourceRepository.save(new TaskResource(null, ironFarmTask.getId(), "Camas", 10, 10));
        taskResourceRepository.save(new TaskResource(null, ironFarmTask.getId(), "Hierro", 30, 22));
        taskResourceRepository.save(new TaskResource(null, storageTask.getId(), "Cofres", 20, 8));
    }

    private void seedGuides() {
        List<GuideStep> ironFarmSteps = new ArrayList<>();
        ironFarmSteps.add(new GuideStep(null, 1, "Elegir ubicacion", "Construye la granja lejos de aldeas existentes."));
        ironFarmSteps.add(new GuideStep(null, 2, "Mover aldeanos", "Coloca aldeanos en una zona segura con camas."));
        ironFarmSteps.add(new GuideStep(null, 3, "Crear plataforma", "Prepara la plataforma donde aparecera el golem."));
        ironFarmSteps.add(new GuideStep(null, 4, "Canalizar golems", "Usa agua para empujar los golems hacia la zona de recogida."));

        List<GuideResource> ironFarmResources = new ArrayList<>();
        ironFarmResources.add(new GuideResource(null, "Bloques de piedra", 64));
        ironFarmResources.add(new GuideResource(null, "Camas", 10));
        ironFarmResources.add(new GuideResource(null, "Hierro", 30));
        ironFarmResources.add(new GuideResource(null, "Cubos de agua", 2));

        guideRepository.save(new Guide(
                null,
                "Granja de hierro basica",
                "Minecraft",
                "Estructura",
                "AlexCraft",
                "Media",
                4.6,
                12,
                31,
                LocalDateTime.now(),
                "Guia para crear una tarea de granja de hierro con materiales iniciales.",
                "https://www.youtube.com/",
                ironFarmSteps,
                ironFarmResources
        ));

        List<GuideStep> terrariaSteps = new ArrayList<>();
        terrariaSteps.add(new GuideStep(null, 1, "Derrotar Rey Slime", "Invocalo con una Slime Crown o espera el evento Slime Rain."));
        terrariaSteps.add(new GuideStep(null, 2, "Derrotar Ojo de Cthulhu", "Usa Suspicious Looking Eye de noche."));
        terrariaSteps.add(new GuideStep(null, 3, "Preparar boss de corrupcion/carmesi", "Rompe orbes o usa el objeto de invocacion del bioma."));

        List<GuideResource> terrariaResources = new ArrayList<>();
        terrariaResources.add(new GuideResource(null, "Antorchas", 40));
        terrariaResources.add(new GuideResource(null, "Plataformas", 100));
        terrariaResources.add(new GuideResource(null, "Pociones de curacion", 10));

        guideRepository.save(new Guide(
                null,
                "Progresion Terraria pre-hardmode",
                "Terraria",
                "Progresion",
                "LunaSurvival",
                "Facil",
                4.2,
                8,
                19,
                LocalDateTime.now(),
                "Ruta sencilla para avanzar por los primeros bosses de Terraria.",
                "https://www.youtube.com/",
                terrariaSteps,
                terrariaResources
        ));
    }
}
