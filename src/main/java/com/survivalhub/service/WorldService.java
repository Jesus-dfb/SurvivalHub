package com.survivalhub.service;

import com.survivalhub.model.World;
import com.survivalhub.repository.WorldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorldService {

    private final WorldRepository worldRepository;

    public WorldService(WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
    }

    public List<World> getAllWorlds() {
        return worldRepository.findAll();
    }

    public List<World> getWorldsByOwner(Long ownerUserId) {
        return worldRepository.findByOwnerUserIdOrderByIdAsc(ownerUserId);
    }

    public Optional<World> getWorldById(Long id) {
        return worldRepository.findById(id);
    }

    public Optional<World> getWorldByIdForOwner(Long id, Long ownerUserId) {
        return worldRepository.findByIdAndOwnerUserId(id, ownerUserId);
    }

    public World createWorld(World world) {
        world.setId(null);
        return worldRepository.save(world);
    }

    public World createWorldForOwner(World world, Long ownerUserId) {
        world.setId(null);
        world.setOwnerUserId(ownerUserId);
        return worldRepository.save(world);
    }

    public Optional<World> updateWorld(Long id, World updatedWorld) {
        Optional<World> worldOptional = getWorldById(id);

        if (worldOptional.isEmpty()) {
            return Optional.empty();
        }

        World world = worldOptional.get();
        world.setName(updatedWorld.getName());
        world.setGame(updatedWorld.getGame());
        world.setDescription(updatedWorld.getDescription());

        return Optional.of(worldRepository.save(world));
    }

    public Optional<World> updateWorldForOwner(Long id, Long ownerUserId, World updatedWorld) {
        Optional<World> worldOptional = getWorldByIdForOwner(id, ownerUserId);

        if (worldOptional.isEmpty()) {
            return Optional.empty();
        }

        World world = worldOptional.get();
        world.setName(updatedWorld.getName());
        world.setGame(updatedWorld.getGame());
        world.setDescription(updatedWorld.getDescription());

        return Optional.of(worldRepository.save(world));
    }

    public boolean deleteWorld(Long id) {
        if (!worldRepository.existsById(id)) {
            return false;
        }

        worldRepository.deleteById(id);

        return true;
    }

    public boolean deleteWorldForOwner(Long id, Long ownerUserId) {
        Optional<World> worldOptional = getWorldByIdForOwner(id, ownerUserId);

        if (worldOptional.isEmpty()) {
            return false;
        }

        worldRepository.deleteById(id);

        return true;
    }
}
