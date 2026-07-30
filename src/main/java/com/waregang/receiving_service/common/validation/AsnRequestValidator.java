package com.waregang.receiving_service.common.validation;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsnRequestValidator implements ConstraintValidator<ValidAsnRequest, CreateAsnRequest> {

    @Override
    public boolean isValid(CreateAsnRequest request, ConstraintValidatorContext context) {
        if (request == null || request.unitRequests() == null || request.contents() == null) {
            return true; // другие валидации могут обработать null, пропускаем
        }

        Set<String> unitLpns = request.unitRequests().stream()
                .map(CreateUnitRequest::lpn)
                .collect(Collectors.toSet());

        boolean isValid = true;

        // 1. Все parentLpn существуют
        for (CreateContentRequest content : request.contents()) {
            if (!unitLpns.contains(content.parentLpn())) {
                isValid = false;
                addParentLpnMissingViolation(context);
            }
        }

        for (CreateUnitRequest unit : request.unitRequests()) {
            if (unit.parentLpn() != null && !unitLpns.contains(unit.parentLpn())) {
                isValid = false;
                addParentLpnMissingViolation(context);
            }
        }

        // 2. Листовые юниты имеют контент
        Set<String> parentLpns = request.unitRequests().stream()
                .map(CreateUnitRequest::parentLpn)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> contentContainerLpns = request.contents().stream()
                .map(CreateContentRequest::parentLpn)
                .collect(Collectors.toSet());

        for (CreateUnitRequest unit : request.unitRequests()) {
            boolean isLeaf = !parentLpns.contains(unit.lpn());
            boolean hasContent = contentContainerLpns.contains(unit.lpn());

            if (isLeaf && !hasContent) {
                isValid = false;
                addEmptyLeafViolation(context);
            }
        }

        // 3. Проверка на наличие хотя бы одного корневого элемента (parentLpn == null)
        // Если все юниты имеют родителя, то сортировка не сможет начаться.
        if (request.unitRequests().stream().allMatch(u -> u.parentLpn() != null)) {
            isValid = false;
            addNoRootViolation(context);
        }

        // 4. Проверка на циклические зависимости
        if (hasCyclicDependency(request.unitRequests())) {
            isValid = false;
            addCyclicDependencyViolation(context);
        }

        return isValid;
    }

    /**
     * Проверяет наличие циклов в иерархии parentLpn.
     */
    private boolean hasCyclicDependency(List<CreateUnitRequest> units) {
        Map<String, String> parentMap = units.stream()
                .collect(Collectors.toMap(
                        CreateUnitRequest::lpn,
                        u -> u.parentLpn() != null ? u.parentLpn() : "",
                        (a, b) -> a // если дубликаты, но уникальность LPN должна быть гарантирована
                ));

        // Удаляем записи без родителя (корни)
        Map<String, String> nonRootMap = parentMap.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String lpn : nonRootMap.keySet()) {
            if (hasCycle(lpn, nonRootMap, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycle(String node, Map<String, String> parentMap,
                             Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(node)) {
            return true; // цикл найден
        }
        if (visited.contains(node)) {
            return false; // уже проверено
        }

        visited.add(node);
        recursionStack.add(node);

        String parent = parentMap.get(node);
        if (parent != null && !parent.isEmpty()) {
            // Если родитель не существует в parentMap (невалидный parentLpn) – это уже отловлено
            // Но на всякий случай проверяем
            if (parentMap.containsKey(parent)) {
                if (hasCycle(parent, parentMap, visited, recursionStack)) {
                    return true;
                }
            } else {
                // родитель не существует – этот случай уже покрыт предыдущей проверкой, но оставляем
                // можно считать, что цикла нет
            }
        }

        recursionStack.remove(node);
        return false;
    }

    // --- Методы для добавления сообщений ---

    private void addParentLpnMissingViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("A non-existent LPN is referenced.")
                .addConstraintViolation();
    }

    private void addEmptyLeafViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("A leaf unit must not be empty.")
                .addConstraintViolation();
    }

    private void addNoRootViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("At least one root unit (without parentLpn) is required.")
                .addConstraintViolation();
    }

    private void addCyclicDependencyViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Cyclic dependency detected in unit hierarchy.")
                .addConstraintViolation();
    }
}