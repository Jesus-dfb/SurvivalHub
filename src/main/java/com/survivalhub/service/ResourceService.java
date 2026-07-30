package com.survivalhub.service;

import com.survivalhub.model.ResourceImportResult;
import com.survivalhub.model.TaskResource;
import com.survivalhub.model.TaskProgressSummary;
import com.survivalhub.repository.TaskResourceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class ResourceService {

    private final TaskResourceRepository taskResourceRepository;

    public ResourceService(TaskResourceRepository taskResourceRepository) {
        this.taskResourceRepository = taskResourceRepository;
    }

    public List<TaskResource> getResourcesByTaskId(Long taskId) {
        List<TaskResource> resources = taskResourceRepository.findByTaskIdOrderBySortOrderAscIdAsc(taskId);

        return normalizeResourceOrder(resources);
    }

    public TaskProgressSummary getTaskProgressSummary(Long taskId) {
        List<TaskResource> taskResources = getResourcesByTaskId(taskId);

        int totalResources = taskResources.size();
        int completedResources = 0;
        int totalRequiredQuantity = 0;
        int totalCollectedQuantity = 0;

        for (TaskResource resource : taskResources) {
            if (resource.isCompleted()) {
                completedResources++;
            }

            totalRequiredQuantity += resource.getRequiredQuantity();
            totalCollectedQuantity += Math.min(
                    resource.getCollectedQuantity(),
                    resource.getRequiredQuantity()
            );
        }

        double progressPercentage = 0;

        if (totalRequiredQuantity > 0) {
            progressPercentage = totalCollectedQuantity * 100.0 / totalRequiredQuantity;
            progressPercentage = Math.round(progressPercentage * 100.0) / 100.0;
        }

        boolean allResourcesCompleted = totalResources > 0 && completedResources == totalResources;

        return new TaskProgressSummary(
                taskId,
                totalResources,
                completedResources,
                progressPercentage,
                allResourcesCompleted
        );
    }

    public Optional<TaskResource> getResourceById(Long taskId, Long resourceId) {
        return taskResourceRepository.findByTaskIdAndId(taskId, resourceId);
    }

    public TaskResource createResource(Long taskId, TaskResource resource) {
        resource.setId(null);
        resource.setTaskId(taskId);

        if (resource.getStackSize() <= 0) {
            resource.setStackSize(64);
        }

        if (resource.getSortOrder() <= 0) {
            resource.setSortOrder((int) taskResourceRepository.countByTaskId(taskId) + 1);
        }

        return taskResourceRepository.save(resource);
    }

    @Transactional
    public ResourceImportResult importResourcesFromMaterialList(Long taskId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo esta vacio");
        }

        String content = decodeFile(file);
        List<TaskResource> importedResources = new ArrayList<>();
        List<TaskResource> existingResources = getResourcesByTaskId(taskId);
        Map<String, TaskResource> resourcesByName = new LinkedHashMap<>();

        for (TaskResource resource : existingResources) {
            resourcesByName.put(normalizeName(resource.getName()), resource);
        }

        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        int nextSortOrder = (int) taskResourceRepository.countByTaskId(taskId) + 1;

        for (String line : content.split("\\R")) {
            Optional<TaskResource> parsedResource = parseMaterialLine(line);

            if (parsedResource.isEmpty()) {
                if (line.trim().startsWith("|") && !line.contains("Item")) {
                    skippedCount++;
                }
                continue;
            }

            TaskResource parsed = parsedResource.get();
            String normalizedName = normalizeName(parsed.getName());
            TaskResource resource = resourcesByName.get(normalizedName);

            if (resource == null) {
                parsed.setTaskId(taskId);
                parsed.setSortOrder(nextSortOrder);
                resource = taskResourceRepository.save(parsed);
                resourcesByName.put(normalizedName, resource);
                nextSortOrder++;
                createdCount++;
            } else {
                resource.setRequiredQuantity(parsed.getRequiredQuantity());
                resource.setCollectedQuantity(parsed.getCollectedQuantity());
                resource = taskResourceRepository.save(resource);
                updatedCount++;
            }

            importedResources.add(resource);
        }

        if (importedResources.isEmpty()) {
            throw new IllegalArgumentException("No se detectaron materiales validos");
        }

        return new ResourceImportResult(
                importedResources.size(),
                createdCount,
                updatedCount,
                skippedCount,
                importedResources
        );
    }

    public Optional<TaskResource> updateResource(Long taskId, Long resourceId, TaskResource updatedResource) {
        Optional<TaskResource> resourceOptional = getResourceById(taskId, resourceId);

        if (resourceOptional.isEmpty()) {
            return Optional.empty();
        }

        TaskResource resource = resourceOptional.get();
        resource.setName(updatedResource.getName());
        resource.setRequiredQuantity(updatedResource.getRequiredQuantity());
        resource.setCollectedQuantity(updatedResource.getCollectedQuantity());
        resource.setStackSize(updatedResource.getStackSize());

        return Optional.of(taskResourceRepository.save(resource));
    }

    public List<TaskResource> reorderResources(Long taskId, List<Long> resourceIds) {
        List<TaskResource> resources = getResourcesByTaskId(taskId);
        Map<Long, TaskResource> resourceById = new HashMap<>();

        for (TaskResource resource : resources) {
            resourceById.put(resource.getId(), resource);
        }

        int sortOrder = 1;

        for (Long resourceId : resourceIds) {
            TaskResource resource = resourceById.remove(resourceId);

            if (resource != null) {
                resource.setSortOrder(sortOrder);
                sortOrder++;
            }
        }

        for (TaskResource resource : resources) {
            if (resourceById.containsKey(resource.getId())) {
                resource.setSortOrder(sortOrder);
                sortOrder++;
            }
        }

        taskResourceRepository.saveAll(resources);

        return getResourcesByTaskId(taskId);
    }

    public boolean deleteResource(Long taskId, Long resourceId) {
        Optional<TaskResource> resourceOptional = getResourceById(taskId, resourceId);

        if (resourceOptional.isEmpty()) {
            return false;
        }

        taskResourceRepository.delete(resourceOptional.get());

        return true;
    }

    @Transactional
    public void deleteResourcesByTaskId(Long taskId) {
        taskResourceRepository.deleteByTaskId(taskId);
    }

    private Optional<TaskResource> parseMaterialLine(String line) {
        String trimmedLine = line == null ? "" : line.trim();

        if (!trimmedLine.startsWith("|")) {
            return Optional.empty();
        }

        String[] columns = trimmedLine.split("\\|");

        if (columns.length < 5) {
            return Optional.empty();
        }

        String name = fixMojibake(columns[1].trim());

        if (name.isEmpty() || name.equalsIgnoreCase("Item")) {
            return Optional.empty();
        }

        try {
            int requiredQuantity = parseQuantity(columns[2]);
            int collectedQuantity = parseQuantity(columns[4]);

            TaskResource resource = new TaskResource(
                    null,
                    null,
                    name,
                    requiredQuantity,
                    collectedQuantity
            );
            resource.setStackSize(64);

            return Optional.of(resource);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private int parseQuantity(String value) {
        String cleanValue = value == null ? "" : value.replaceAll("[^0-9]", "");

        if (cleanValue.isEmpty()) {
            throw new NumberFormatException("Cantidad vacia");
        }

        return Integer.parseInt(cleanValue);
    }

    private String decodeFile(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        try {
            return StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }

    private String normalizeName(String name) {
        return String.valueOf(name).trim().toLowerCase(Locale.ROOT);
    }

    private List<TaskResource> normalizeResourceOrder(List<TaskResource> resources) {
        boolean needsUpdate = false;
        int sortOrder = 1;

        for (TaskResource resource : resources) {
            if (resource.getSortOrder() <= 0) {
                resource.setSortOrder(sortOrder);
                needsUpdate = true;
            }

            sortOrder++;
        }

        if (needsUpdate) {
            return taskResourceRepository.saveAll(resources);
        }

        return resources;
    }

    private String fixMojibake(String value) {
        if (value == null || (!value.contains("Ã") && !value.contains("Â"))) {
            return value;
        }

        return new String(value.getBytes(Charset.forName("windows-1252")), StandardCharsets.UTF_8);
    }
}
