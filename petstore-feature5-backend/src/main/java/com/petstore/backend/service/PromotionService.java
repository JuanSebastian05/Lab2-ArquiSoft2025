package com.petstore.backend.service;

import com.petstore.backend.dto.CategoryDTO;
import com.petstore.backend.dto.PromotionDTO;
import com.petstore.backend.entity.Promotion;
import com.petstore.backend.repository.PromotionRepository;
import com.petstore.backend.repository.StatusRepository;
import com.petstore.backend.repository.UserRepository;
import com.petstore.backend.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public PromotionService(PromotionRepository promotionRepository,
                           StatusRepository statusRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository) {
        this.promotionRepository = promotionRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Obtiene todas las promociones activas y vigentes
     */
    public List<PromotionDTO> getAllActivePromotions() {
        LocalDate today = LocalDate.now();
        
        // Buscar promociones activas
        List<Promotion> activePromotions = promotionRepository.findActivePromotions();
        
        // Filtrar las que están vigentes (fecha actual entre start y end)
        return activePromotions.stream()
                .filter(promotion -> !today.isBefore(promotion.getStartDate()) && !today.isAfter(promotion.getEndDate()))
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Obtiene todas las promociones (activas e inactivas) para administración
     */
    public List<PromotionDTO> getAllPromotions() {
        List<Promotion> allPromotions = promotionRepository.findAll();
        
        return allPromotions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Obtiene promociones por categoría
     */
    public List<PromotionDTO> getPromotionsByCategory(Integer categoryId) {
        List<Promotion> promotions = promotionRepository.findByCategoryCategoryId(categoryId);
        
        return promotions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Obtiene promociones vigentes para la fecha actual
     */
    public List<PromotionDTO> getValidPromotions() {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository.findValidPromotions(today);
        
        return promotions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Convierte una entidad Promotion a PromotionDTO
     */
    private PromotionDTO convertToDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        
        dto.setPromotionId(promotion.getPromotionId());
        dto.setPromotionName(promotion.getPromotionName());
        dto.setDescription(promotion.getDescription());
        
        // Convertir discount value de Double a BigDecimal
        if (promotion.getDiscountValue() != null) {
            dto.setDiscountPercentage(BigDecimal.valueOf(promotion.getDiscountValue()));
        }
        
        // Convertir LocalDate a LocalDateTime (agregando hora 00:00:00)
        if (promotion.getStartDate() != null) {
            dto.setStartDate(promotion.getStartDate().atStartOfDay());
        }
        if (promotion.getEndDate() != null) {
            dto.setEndDate(promotion.getEndDate().atTime(23, 59, 59));
        }
        
        // Nota: Las entidades Promotion no tienen createdAt/updatedAt en el esquema actual
        // Esto se puede agregar más adelante si es necesario
        
        // Convertir status
        if (promotion.getStatus() != null) {
            dto.setStatus(promotion.getStatus().getStatusName());
        }
        
        // Convertir category si existe
        if (promotion.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setCategoryId(promotion.getCategory().getCategoryId());
            categoryDTO.setCategoryName(promotion.getCategory().getCategoryName());
            categoryDTO.setDescription(promotion.getCategory().getDescription());
            dto.setCategory(categoryDTO);
        }
        
        // Nota: La entidad Promotion actual no tiene relación directa con Product
        // Solo tiene relación con Category, que puede contener productos
        dto.setProduct(null);
        
        return dto;
    }

    // === MÉTODOS PARA GRAPHQL que retornan entidades directamente ===

    /**
     * Obtiene todas las promociones activas como entidades para GraphQL
     */
    public List<Promotion> getAllActivePromotionsEntities() {
        LocalDate today = LocalDate.now();
        
        // Buscar promociones activas
        List<Promotion> activePromotions = promotionRepository.findActivePromotions();
        
        // Filtrar las que están vigentes (fecha actual entre start y end)
        return activePromotions.stream()
                .filter(promotion -> !today.isBefore(promotion.getStartDate()) && !today.isAfter(promotion.getEndDate()))
                .toList();
    }

    /**
     * Obtiene promociones expiradas como entidades para GraphQL
     */
    public List<Promotion> getAllExpiredPromotionsEntities() {
        return promotionRepository.findExpiredPromotions();
    }

    /**
     * Obtiene promociones programadas como entidades para GraphQL
     */
    public List<Promotion> getAllScheduledPromotionsEntities() {
        return promotionRepository.findScheduledPromotions();
    }

    /**
     * Obtiene promociones por estado específico como entidades para GraphQL
     */
    public List<Promotion> getPromotionsByStatusEntities(String statusName) {
        return promotionRepository.findByStatusName(statusName);
    }

    /**
     * Obtiene promociones por categoría como entidades para GraphQL
     */
    public List<Promotion> getPromotionsByCategoryEntities(Integer categoryId) {
        return promotionRepository.findByCategoryCategoryId(categoryId);
    }

    /**
     * Obtiene una promoción por ID como entidad para GraphQL
     */
    public Promotion getPromotionByIdEntity(Integer id) {
        return promotionRepository.findById(id).orElse(null);
    }

    // === MÉTODOS CRUD PARA MUTACIONES ===

    /**
     * Crea una nueva promoción
     */
    public Promotion createPromotion(String promotionName, String description, 
                                   LocalDate startDate, LocalDate endDate, 
                                   Double discountValue, Integer statusId, 
                                   Integer userId, Integer categoryId) {
        Promotion promotion = new Promotion();
        promotion.setPromotionName(promotionName);
        promotion.setDescription(description);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setDiscountValue(discountValue);
        
        // Asignar entidades relacionadas usando helper method
        setPromotionRelations(promotion, statusId, userId, categoryId);
        
        return promotionRepository.save(promotion);
    }

    /**
     * Actualiza una promoción existente
     */
    public Promotion updatePromotion(Integer promotionId, String promotionName, String description,
                                   LocalDate startDate, LocalDate endDate,
                                   Double discountValue, Integer statusId,
                                   Integer userId, Integer categoryId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
        if (promotion == null) {
            return null;
        }
        
        // Actualizar campos
        if (promotionName != null) promotion.setPromotionName(promotionName);
        if (description != null) promotion.setDescription(description);
        if (startDate != null) promotion.setStartDate(startDate);
        if (endDate != null) promotion.setEndDate(endDate);
        if (discountValue != null) promotion.setDiscountValue(discountValue);
        
        // Actualizar entidades relacionadas usando helper method
        setPromotionRelations(promotion, statusId, userId, categoryId);
        
        return promotionRepository.save(promotion);
    }

    /**
     * Helper method para asignar entidades relacionadas a una promoción
     */
    private void setPromotionRelations(Promotion promotion, Integer statusId, 
                                      Integer userId, Integer categoryId) {
        if (statusId != null) {
            statusRepository.findById(statusId).ifPresent(promotion::setStatus);
        }
        
        if (userId != null) {
            userRepository.findById(userId).ifPresent(promotion::setUser);
        }
        
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(promotion::setCategory);
        }
    }

    /**
     * Elimina una promoción
     */
    public boolean deletePromotion(Integer promotionId) {
        try {
            if (promotionRepository.existsById(promotionId)) {
                promotionRepository.deleteById(promotionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting promotion: {}", e.getMessage(), e);
            return false;
        }
    }
}
